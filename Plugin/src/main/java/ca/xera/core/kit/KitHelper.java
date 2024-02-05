package ca.xera.core.kit;

import ca.xera.core.PVPCore;
import ca.xera.core.common.NBTHelper;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Base64;
import java.util.Optional;

public class KitHelper {

    public static void saveKitToDatabase(Kit kit) {
        ByteArrayDataOutput out = PVPCore.messaging.newDataOutput();
        NBTHelper.writeNBT(kit.getInventoryTag(), out);
        MongoDatabase client = PVPCore.get().getClient().getDatabase("pvpdata");
        MongoCollection<Document> kits = client.getCollection("kits");
        Document toInsert = new Document("_id", new ObjectId()).append("kitName", kit.getName()).append("data", Base64.getEncoder().encodeToString(out.toByteArray()));
        kits.insertOne(toInsert);
    }

    public static Optional<Document> query(String name) {
        MongoDatabase client = PVPCore.get().getClient().getDatabase("pvpdata");
        MongoCollection<Document> kits = client.getCollection("kits");
        kits.listIndexes().forEach(document -> System.out.println(document.toJson()));
        return Optional.ofNullable(kits.find(Filters.eq("kitName", name)).first());
    }

    public static Kit getKit(String name) {
        Optional<Document> query = query(name);
        if (query.isPresent()) {
            Document document = query.get();
            ByteArrayDataInput in = PVPCore.messaging.newDataInput(Base64.getDecoder().decode(document.getString("data")));
            NBTTagCompound inventoryTag = NBTHelper.readNBT(in);
            return new Kit(name, inventoryTag);
        }
        return null;
    }
}
