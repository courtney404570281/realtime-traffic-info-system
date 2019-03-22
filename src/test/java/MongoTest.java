
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.util.Arrays;

public class MongoTest {

    public static void main(String[] args) {

        MongoClient mongoClient = MongoClients.create("mongodb://192.168.1.237:27017");

        MongoDatabase mdb = mongoClient.getDatabase("test");
        System.out.println("Connect to database successfully");

        MongoCollection<Document> collection = mdb.getCollection("interCityBus");
        System.out.println("Get collection successfully");

//        Document document = new Document("name", "Café Con Leche")
//                .append("contact", new Document("phone", "228-555-0149")
//                        .append("email", "cafeconleche@example.com")
//                        .append("location",Arrays.asList(-73.92502, 40.8279556)))
//                .append("stars", 3)
//                .append("categories", Arrays.asList("Bakery", "Coffee", "Pastries"));

//        String json = "{\"PlateNumb\":\"200-FT\",\"OperatorID\":\"31\",\"RouteUID\":\"THB0969\",\"RouteID\":\"0969\",\"RouteName\":{\"Zh_tw\":\"0969\",\"En\":\"0969\"},\"SubRouteUID\":\"THB096901\",\"SubRouteID\":\"096901\",\"SubRouteName\":{\"Zh_tw\":\"09690\",\"En\":\"09690\"},\"Direction\":0,\"BusPosition\":{\"PositionLat\":22.66634,\"PositionLon\":120.32156},\"Speed\":0.0,\"Azimuth\":348.0,\"DutyStatus\":1,\"BusStatus\":0,\"MessageType\":1,\"GPSTime\":\"2019-02-19T14:22:06+08:00\",\"SrcRecTime\":\"2019-02-19T14:22:05+08:00\",\"SrcTransTime\":\"2019-02-19T14:22:06+08:00\",\"UpdateTime\":\"2019-02-19T14:22:07+08:00\"}";
//        Document document = new Document().parse(json);
//        String plateNumb = document.getString("PlateNumb");
//        System.out.println("plateNumb: " + plateNumb);
//        document.append("_id", plateNumb);
//        System.out.printf("new document: " + document.toJson());

//        collection.insertOne(document);

//        System.out.println(collection.countDocuments());
//        collection.drop();
//        System.out.println(collection.countDocuments());
//        System.out.println("done");

//        collection.createCollection("firstColl");

        collection.bulkWrite(
                Arrays.asList(new InsertOneModel<>(new Document("_id", 4)),
                        new InsertOneModel<>(new Document("_id", 5)),
                        new InsertOneModel<>(new Document("_id", 6)),
                        new UpdateOneModel<>(new Document("_id", 1),
                                new Document("$set", new Document("x", 2))),
                        new DeleteOneModel<>(new Document("_id", 2)),
                        new ReplaceOneModel<>(new Document("_id", 3),
                                new Document("_id", 3).append("x", 4))),
                new BulkWriteOptions().ordered(false));
    }
}
