package ca.xera.core.db;

import ca.xera.core.PVPCore;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;

import java.util.logging.Level;

@Getter
public class MongoConnector {

    private MongoClient client;

    public MongoConnector() {
        String connectionString = "mongodb://vps01.iceanarchy.org:27017";
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                this.client = mongoClient;
                PVPCore.get().getLogger().log(Level.INFO, String.format("A successful database connection has been made to %s", connectionString));
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
}
