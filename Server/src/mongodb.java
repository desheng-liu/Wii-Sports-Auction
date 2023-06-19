/*
 * EE422C Final Project submission by
 * Desheng Liu
 * dl36526
 * 17150
 * Spring 2023
 * Slip Days Used: 1
 */

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.google.gson.GsonBuilder;
import com.mongodb.MongoException;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;
import com.google.gson.Gson;

public class mongodb {
    private static MongoClient mongo;
    private static MongoDatabase database;
    private static MongoCollection<Document> item_collection;
    private static MongoCollection<Document> account_collection;
    private static MongoCollection<Document> sold_collection;
    private static MongoCollection<Document> nonvolatilehistory_collection;
    private static final String URI = "mongodb+srv://deshplop:eTCVipA0aaBd1e25@cluster0.hhduz3p.mongodb.net/?retryWrites=true&w=majority";
    public Gson gson;

        public mongodb(){
            startup();
        }

        public void startup() {
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
            mongo = MongoClients.create(URI);
            database = mongo.getDatabase("auction").withCodecRegistry(pojoCodecRegistry);
            item_collection = database.getCollection("items");
            account_collection = database.getCollection("accounts");
            sold_collection = database.getCollection("solditems");
            nonvolatilehistory_collection = database.getCollection("non-volatile history");
            gson = new GsonBuilder().setPrettyPrinting().create();
            ping();
        }

        public void addToNonVolHistory(Message message){
            Document doc = Document.parse(gson.toJson(message));
            nonvolatilehistory_collection.insertOne(doc);
            System.out.println("added to database logs.");
        }

        public void addUser(User user){
            User temp = new User(user.getUsername(), user.getSalt(), user.getHashedPassword(),user.getId_Count());
            Document doc = Document.parse(gson.toJson(temp));
            account_collection.insertOne(doc);
            System.out.println("added User to database.");
        }

        public User getUser(User user){
            Document doc = account_collection.find(Filters.eq("username", user.getUsername())).first();
            if(doc == null){
                return null;
            }
            return gson.fromJson(doc.toJson(), User.class);
        }

        public User checkPassword(User user){
            Document doc = account_collection.find(Filters.eq("username", user.getUsername())).first();
            if(doc == null){ // look for username again
                return null;
            }
            doc = account_collection.find(Filters.eq("password", user.getHashedPassword())).first();
            if(doc == null){
                return null;
            }
            return gson.fromJson(doc.toJson(), User.class);
        }
        public void addItem(Item item){ // add in feature of making sure adding only one item (no duplicates)
            Document doc = Document.parse(gson.toJson(item));
            item_collection.insertOne(doc);
        }
        public void removeItem(Item item){
            item_collection.deleteOne(Filters.eq("name",item.getName()));
        }

        public Item getItem(String itemname){
            Document doc = item_collection.find(Filters.eq("name", itemname)).first();
            if(doc == null){
                return null;
            }
            return gson.fromJson(doc.toJson(), Item.class);

        }
        public void updateItem(String itemname, String bidder, Double bid){
            item_collection.updateOne(Filters.eq("name", itemname), Updates.combine(Updates.set("currentPrice", bid), Updates.set("highestBidder", bidder)));
        }

        public void transferSoldDB(Item item){
            Document doc = item_collection.find(Filters.eq("name", item.getName())).first();
            item_collection.deleteOne(doc);
            sold_collection.insertOne(doc);
        }

    public static MongoCollection<Document> getItem_collection() {
        return item_collection;
    }
    public static MongoCollection<Document> getAccount_collection() {
        return account_collection;
    }
    public static MongoCollection<Document> getSold_collection() {
        return sold_collection;
    }

    public static void ping(){
            try{
                Bson command = new BsonDocument("ping", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
                System.out.println("Connected successfully to server");
            } catch (MongoException me){
                System.err.println("An error occured while attempting to run a command: "+me);
            }
        }
}

