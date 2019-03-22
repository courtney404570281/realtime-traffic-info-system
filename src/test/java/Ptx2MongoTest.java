import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class Ptx2MongoTest {

    public static void main(String[] args) {

        final String apiUrl = "http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeByFrequency/InterCity?$top=3000&$format=JSON";
//        final String apiUrl = "https://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/City/Taipei/307?$top=100&$format=JSON";
        final String mongoHost = "mongodb://192.168.1.237:27017";
        int numDocAdded = 0;

        // connect to mongoDB
        MongoClient mongoClient = MongoClients.create(mongoHost);
        MongoDatabase mdb = mongoClient.getDatabase("test");
        System.out.println("Connect to database successfully");
        MongoCollection<Document> collection = mdb.getCollection("interCityBus");
        System.out.println("Get collection successfully");

        try {
            // create Ptx connection
            JsonArray ja = Ptx.getResponseJsonFrom(apiUrl);
            System.out.println("json array length: " + ja.size());

            List<Document> documents = new ArrayList<>();
            List replaceOneModels = new ArrayList();
            UpdateOptions updataOption = new UpdateOptions().upsert(true).bypassDocumentValidation(true);

            for (JsonElement je : ja) {
            System.out.println(je.toString());

                // append the document id
                Document document = Document.parse(je.toString());
                String plateNumb = document.getString("PlateNumb");
                document.append("_id", plateNumb);

                // create replaceOneModel and add it to replaceOneModels
                ReplaceOneModel replaceOneModel = new ReplaceOneModel<>(
                        eq("_id", plateNumb),
                        document,
                        updataOption);
                replaceOneModels.add(replaceOneModel);
                documents.add(document);
                numDocAdded++;

            if(numDocAdded % 100 == 0)
                System.out.println(numDocAdded);

                if(replaceOneModels.size() >= 1000){
                    System.out.println("upserting " + replaceOneModels.size() + " documents to mongoDB...");
                    collection.bulkWrite(replaceOneModels);
                    replaceOneModels.clear();
                    System.out.println("upsertion completed");
                }
            }

            if(replaceOneModels.size() > 0){
                System.out.println("upserting " + replaceOneModels.size() + " documents to mongoDB...");
                collection.bulkWrite(replaceOneModels);
                replaceOneModels.clear();
                System.out.println("upsertion completed");
            }

            System.out.println(collection.countDocuments() + " documents in the collection");
            System.out.println("numDocAdded: " + numDocAdded);

            Thread.sleep(10000);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}