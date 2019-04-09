import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.io.IOException;
import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public abstract class Transportation implements Runnable{

    final int FETCH_ALLOW = 5000;
    int threadLength; // 該執行緒取得的總資料量
    List<String> urls = new ArrayList<>();
    List<String> collections = new ArrayList<>();

    @Override
    public void run() {
        int topicLength; // 此topic取得的總資料量

        for (int i = 0; i < urls.size(); i++) { // 每次一個topic的資料
            final String MONGO_HOST = "mongodb://192.168.1.237:27017";
            final String MONGO_HOST_AUTH = "mongodb://test:test@192.168.1.181:27017/?authSource=test";
            final String DB = "test";
            int responseLength = FETCH_ALLOW; // 每次response得到的json array實際大小
            int loopCount = 0;
            topicLength = 0;

            String collection = collections.get(i);
            MongoClient mongoClient = MongoClients.create(MONGO_HOST_AUTH);
            MongoCollection<Document> mongoCollection = mongoClient.getDatabase(DB).getCollection(collection);

            while (responseLength == FETCH_ALLOW) { // read_data
                responseLength = 0;
                String apiUrl = urls.get(i) + "&$skip=" + (loopCount * FETCH_ALLOW);
//                System.out.println("topic: " + i + ", collection: " + collection + ", loopCount: "+loopCount);
//                System.out.println("apiUrl: " + apiUrl);

                JsonArray ja;
                try {
                    System.out.print("Getting data of " + collection + " from PTX... ");
                    ja = Ptx.getResponseJsonFrom(apiUrl);
                    System.out.println(ja.size());
                } catch (IOException | SignatureException e) {
                    e.printStackTrace();
                    return;
                }

                List<ReplaceOneModel<Document>> replaceOneModels = new ArrayList<>();
                ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true).bypassDocumentValidation(true);

                for (JsonElement je : ja) {
                    Document document = Document.parse(je.toString());
                    String id = getIdOf(document, collection);
                    document.append("_id", id);

                    Date updateTime = null;
                    try {
                        updateTime = parseToDate(document.get("UpdateTime").toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    document.put("UpdateTime", updateTime);

                    if(collection.equals("icb_stop")) {
                        JsonObject stopPosition = je.getAsJsonObject().get("StopPosition").getAsJsonObject();
                        double lat = stopPosition.get("PositionLat").getAsDouble();
                        double lon = stopPosition.get("PositionLon").getAsDouble();
                        String json = "{type:\"Point\",coordinates:["+lat+","+lon+"]}";
                        Document embeddedDoc = Document.parse(json);
                        document.remove("StopPosition");
                        document.append("StopPosition", embeddedDoc);
                    }

                    ReplaceOneModel<Document> replaceOneModel = new ReplaceOneModel<>(
                            eq("_id", id),
                            document,
                            replaceOptions);
                    replaceOneModels.add(replaceOneModel);
                    responseLength++;
                }

                Mongo.bulkWrite(replaceOneModels, mongoCollection);
                topicLength += responseLength;
                loopCount++;
            }
            System.out.println(collection + ": " + topicLength + " documents upserted.");
            threadLength += topicLength;
            mongoClient.close();
        }
    }

    private Date parseToDate(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+08:00");
        return sdf.parse(dateString);
    }

    abstract String getIdOf(Document document, String collection);
}
