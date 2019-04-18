import static com.mongodb.client.model.Filters.eq;

import com.google.gson.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.security.SignatureException;
import java.util.*;

public class Ptx2MongoSingleThread {

    public static void main(String[] args) throws InterruptedException, IOException, SignatureException {

        final String mongoHost = "mongodb://192.168.181:27017";
        final int fetch_allow = 5000;
        final String db = "test";
        int numDocAdded = 0;

        // 公路公車
        List<String> interCityBusUrl = new ArrayList<>();
        List<String> interCityBusColl = new ArrayList<>();
        // 0: 動態定點資料
        interCityBusUrl.add(0,"http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeNearStop/InterCity?$format=JSON&$top=" + fetch_allow);
        interCityBusColl.add(0,"icb_0");
        // 1: 預估到站資料
        interCityBusUrl.add(1,"http://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/InterCity?$filter=PlateNumb%20ne%20'-1'&$format=JSON&$top=" + fetch_allow);
        interCityBusColl.add(1,"icb_1");
        // 2: 動態定時資料
        interCityBusUrl.add(2,"http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeByFrequency/InterCity?$format=JSON&$top=" + fetch_allow);
        interCityBusColl.add(2,"icb_2");

        // 台北市公車
        List<String> busUrl = new ArrayList<>();
        List<String> busColl = new ArrayList<>();
        // 0: 動態定時資料
        busUrl.add(0,"http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeByFrequency/City/Taipei?$format=JSON&$top=" + fetch_allow);
        busColl.add(0,"bus_taipei_0");
        // 1: 動態定點資料
        busUrl.add(1,"http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeNearStop/City/Taipei?$format=JSON&$top=" + fetch_allow);
        busColl.add(1,"bus_taipei_1");
        // 2: 預估到站資料
        busUrl.add(2,"http://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/City/Taipei?$filter=PlateNumb%20ne%20'-1'&$format=JSON&$top=" + fetch_allow);
        busColl.add(2,"bus_taipei_2");

        while(true){ // 無限迴圈，每十秒重抓一次全部資料
            for(int i=0;i<interCityBusColl.size();i++){ // 每次一個topic的資料
                int jsonLength = fetch_allow; // 每次response得到的json array實際大小
                int topicLength = 0; // 此次topic放入的總資料量
                int loopCount = 0;

                String collection = interCityBusColl.get(i);
                MongoCollection<Document> mongoCollection = Mongo.getCollection(collection);

                while(jsonLength == fetch_allow){
                    String apiUrl = interCityBusUrl.get(i) + "&$skip=" + (loopCount * fetch_allow);
                    System.out.println("topic: " + i + ", collection: " + collection + ", loopCount: "+loopCount);
                    System.out.println("apiUrl: " + apiUrl);

                    // extract
                    JsonArray ja = Ptx.getResponseJsonFrom(apiUrl);
                    // transform and load
                    switch (i){
                        case 0:
                            jsonLength = mapResponseJson_0(ja, mongoCollection);
                            break;
                        case 1:
                            jsonLength = mapResponseJson_1(ja, mongoCollection);
                            break;
                        case 2:
                            jsonLength = mapResponseJson_0(ja, mongoCollection);
                            break;
                    }

                    System.out.println("jsonLength: " + jsonLength);
                    topicLength += jsonLength;
                    loopCount++;
                }
                System.out.println("topicLength: " + topicLength);
                System.out.println("------------------------");
            }

            for(int i=0;i<busColl.size();i++){ // 每次一個topic的資料
                int jsonLength = fetch_allow; // 每次response得到的json array實際大小
                int topicLength = 0; // 此次topic放入的總資料量
                int loopCount = 0;

                String collection = busColl.get(i);
                MongoCollection<Document> mongoCollection = Mongo.getCollection(collection);

                while(jsonLength == fetch_allow){
                    String apiUrl = busUrl.get(i) + "&$skip=" + (loopCount * fetch_allow);
                    System.out.println("topic: " + i + ", collection: " + collection + ", loopCount: "+loopCount);
                    System.out.println("apiUrl: " + apiUrl);

                    // extract
                    JsonArray ja = Ptx.getResponseJsonFrom(apiUrl);
                    // transform and load
                    switch (i){
                        case 0:
                            jsonLength = mapResponseJson_0(ja, mongoCollection);
                            break;
                        case 1:
                            jsonLength = mapResponseJson_0(ja, mongoCollection);
                            break;
                        case 2:
                            jsonLength = mapResponseJson_1(ja, mongoCollection);
                            break;
                    }

//                    System.out.println("jsonLength: " + jsonLength);
                    topicLength += jsonLength;
                    loopCount++;
                }
                System.out.println("topicLength: " + topicLength);
                System.out.println("------------------------");
            }
            Thread.sleep(10000);
        }
    }

    private static int mapResponseJson_0(JsonArray ja, MongoCollection<Document> mongoCollection) {

        int numDocCount = 0;
        List<Document> documents = new ArrayList<>();
        List replaceOneModels = new ArrayList();
        UpdateOptions updataOption = new UpdateOptions().upsert(true).bypassDocumentValidation(true);

        for (JsonElement je : ja) {
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
            numDocCount++;
        }

        Mongo.bulkWrite(replaceOneModels, mongoCollection);

        return numDocCount;
    }

    private static int mapResponseJson_1(JsonArray ja, MongoCollection<Document> mongoCollection) {

        int numDocCount = 0;
        List<Document> documents = new ArrayList<>();
        List replaceOneModels = new ArrayList();
        UpdateOptions updataOption = new UpdateOptions().upsert(true).bypassDocumentValidation(true);

        for (JsonElement je : ja) {
            // append the document id
            Document document = Document.parse(je.toString());
            String docId = document.getString("SubRouteID") + "_" + document.getString("StopID") +
                    "_" + document.getString("PlateNumb");
            document.append("_id", docId);

            // create replaceOneModel and add it to replaceOneModels
            ReplaceOneModel replaceOneModel = new ReplaceOneModel<>(
                    eq("_id", docId),
                    document,
                    updataOption);
            replaceOneModels.add(replaceOneModel);
            documents.add(document);
            numDocCount++;
        }

        Mongo.bulkWrite(replaceOneModels, mongoCollection);

        return numDocCount;
    }

    private static void logFile(String msg, boolean append) throws IOException {
        FileWriter f = new FileWriter("D:\\ptx2MongoLog.txt", append);
        f.write(msg + "\n");
        f.flush();
        f.close();
    }
}