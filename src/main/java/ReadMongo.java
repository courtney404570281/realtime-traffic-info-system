import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

public class ReadMongo {

    public static void main(String[] args) {

        MongoCollection<Document> collection = Mongo.getCollection("icb_0");
        System.out.println(collection.countDocuments());

        Iterable<Document> it = collection.find(eq("PlateNumb", "009-FZ"));
        it.forEach(printBlock);

        System.out.println("done");
    }

    private static Consumer<Document> printBlock = (document) -> System.out.println(document);
}
