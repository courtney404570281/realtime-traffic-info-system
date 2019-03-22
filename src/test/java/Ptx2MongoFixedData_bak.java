import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class Ptx2MongoFixedData_bak {

    public static void main(String[] args) {

        final int FETCH_ALLOW = 5000;
        List<String> urls = new ArrayList<>();
        List<String> collections = new ArrayList<>();
        int numDocUpserted = 0; // 該執行緒的總資料量

        // 0: 台鐵車站資料
        urls.add("http://ptx.transportdata.tw/MOTC/v2/Rail/TRA/Station?$format=JSON&$top=" + FETCH_ALLOW);
        collections.add("tra_station");
        // 1: 台鐵票價資料
        urls.add("http://ptx.transportdata.tw/MOTC/v2/Rail/TRA/ODFare?$format=JSON&$top=" + FETCH_ALLOW);
        collections.add("tra_odfare");
        // 2: 路線與站牌
        urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/StopOfRoute/InterCity?$format=JSON&$top=" + FETCH_ALLOW);
        collections.add("icb_stopOfRoute");

        int topicLength; // 此次topic放入的總資料量

        for (int i = 0; i < urls.size(); i++) { // 每次一個topic的資料
            int responseLength = FETCH_ALLOW; // 每次response得到的json array實際大小
            int loopCount = 0;
            topicLength = 0;

            String collection = collections.get(i);
            MongoCollection<Document> mongoCollection = Mongo.getCollection(collection);

            while (responseLength == FETCH_ALLOW) { // read_data
                String apiUrl = urls.get(i) + "&$skip=" + (loopCount * FETCH_ALLOW);
//                System.out.println("topic: " + i + ", collection: " + collection + ", loopCount: "+loopCount);
//                System.out.println("apiUrl: " + apiUrl);

                JsonArray ja = null;
                try {
                    ja = Ptx.getResponseJsonFrom(apiUrl);
                } catch (IOException | SignatureException e) {
                    System.out.println("IOException or SignatureException occurred when getting response json from PTX");
                    e.printStackTrace();
                }

                switch (i) {
                    case 0:
                    case 2:
                        responseLength = mapRespJson_stationId(ja, mongoCollection);
                        break;
                    case 1:
                        responseLength = mapRespJson_odStationId(ja, mongoCollection);
                        break;
                    default:
                        // NOP
                }

                topicLength += responseLength;
                loopCount++;
            }
            System.out.println(topicLength + " documents upserted for " + collection);
//            System.out.println("------------------------------------------------");
            numDocUpserted += topicLength;
        }

        System.out.println("Total " + numDocUpserted + " documents upserted in this execution");
    }

    protected static int mapRespJson_stationId(JsonArray ja, MongoCollection<Document> mongoCollection) {

        int numDocCount = 0;
        List replaceOneModels = new ArrayList();
        UpdateOptions updateOptions = new UpdateOptions().upsert(true).bypassDocumentValidation(true);

        for (JsonElement je : ja) {
            Document document = Document.parse(je.toString());
            String docId = document.getString("StationID");
            document.append("_id", docId);

            ReplaceOneModel replaceOneModel = new ReplaceOneModel<>(
                    eq("_id", docId),
                    document,
                    updateOptions);
            replaceOneModels.add(replaceOneModel);
            numDocCount++;
        }

        Mongo.bulkWrite(replaceOneModels, mongoCollection);

        return numDocCount;
    }

    protected static int mapRespJson_odStationId(JsonArray ja, MongoCollection<Document> mongoCollection) {

        int numDocCount = 0;
        List replaceOneModels = new ArrayList();
        UpdateOptions updateOptions = new UpdateOptions().upsert(true).bypassDocumentValidation(true);

        for (JsonElement je : ja) {
            Document document = Document.parse(je.toString());
            String docId = document.getString("OriginStationID") + "_" +
                    document.getString("DestinationStationID");
            document.append("_id", docId);

            ReplaceOneModel replaceOneModel = new ReplaceOneModel<>(
                    eq("_id", docId),
                    document,
                    updateOptions);
            replaceOneModels.add(replaceOneModel);
            numDocCount++;
        }

        Mongo.bulkWrite(replaceOneModels, mongoCollection);

        return numDocCount;
    }
}