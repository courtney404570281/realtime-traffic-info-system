import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import org.apache.commons.cli.*;
import org.bson.Document;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class Ptx2MongoFixedAndDaily_bak {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("f", "fixed", false, "update fixed data");
        options.addOption("d", "daily", false, "update daily data");
        CommandLine cmd;
        try {
            cmd = new DefaultParser().parse( options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            new HelpFormatter().printHelp("Ptx2MongoFixedAndDaily.jar", options);
            return;
        }

        final int FETCH_ALLOW = 5000;
        int totalLength = 0; // 該次執行取得的總資料量
        int topicLength; // 該topic取得的總資料量
        List<String> urls = new ArrayList<>();
        List<String> collections = new ArrayList<>();

        // 在此新增要抓的資料url
        if(cmd.hasOption("f")){
            // 台鐵車站資料
            urls.add("http://ptx.transportdata.tw/MOTC/v2/Rail/TRA/Station?$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("tra_station");
            // 台鐵票價資料
            urls.add("http://ptx.transportdata.tw/MOTC/v2/Rail/TRA/ODFare?$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("tra_odfare");
            // 路線與站牌
            urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/StopOfRoute/InterCity?$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("icb_stopOfRoute");
        }
        /*if(cmd.hasOption("d")){
            // TODO
        }*/

        for (int i = 0; i < urls.size(); i++) { // 每次一個topic的資料
            int responseLength = FETCH_ALLOW; // 每次response得到的json array實際大小
            int loopCount = 0;
            topicLength = 0;

            String collection = collections.get(i);
            MongoCollection<Document> mongoCollection = Mongo.getCollection(collection);

            while (responseLength == FETCH_ALLOW) { // read_data
                responseLength = 0;
                String apiUrl = urls.get(i) + "&$skip=" + (loopCount * FETCH_ALLOW);
//                System.out.println("topic: " + i + ", collection: " + collection + ", loopCount: "+loopCount);
//                System.out.println("apiUrl: " + apiUrl);

                JsonArray ja;
                try {
                    System.out.print("Getting data of " + collection + "... ");
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
                    String docId = getIdOf(document, collection);
                    document.append("_id", docId);

                    ReplaceOneModel<Document> replaceOneModel = new ReplaceOneModel<>(
                            eq("_id", docId),
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
            totalLength += topicLength;
        }

        System.out.println("Total " + totalLength + " documents upserted in this execution.");
    }

    private static String getIdOf(Document document, String collection) {
        switch (collection) {
            case "tra_station":
                return document.getString("StationID");
            case "tra_odfare":
                return document.getString("OriginStationID") + "_" +
                        document.getString("DestinationStationID");
            case "icb_stopOfRoute":
                return document.getString("SubRouteID");
            default:
                return "Unexpected";
        }
    }
}