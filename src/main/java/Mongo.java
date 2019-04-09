import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOneModel;
import org.bson.Document;

import java.util.List;

class Mongo {

    static MongoCollection<Document> getCollection(String collection){
        final String MONGO_HOST = "mongodb://192.168.1.237:27017";
        final String MONGO_HOST_AUTH = "mongodb://test:test@192.168.1.181:27017/?authSource=test";
        final String DB = "test";

        MongoClient mongoClient = MongoClients.create(MONGO_HOST_AUTH);
        MongoDatabase mdb = mongoClient.getDatabase(DB);
        return mdb.getCollection(collection);
    }

    static void bulkWrite(List<ReplaceOneModel<Document>> replaceOneModels, MongoCollection<Document> mongoCollection) {
        if(replaceOneModels.size() > 0){
//            System.out.print("writing " + replaceOneModels.size() + " documents to mongoDB... ");
            mongoCollection.bulkWrite(replaceOneModels);
            replaceOneModels.clear();
//            System.out.println("completed");
        }
    }
}
