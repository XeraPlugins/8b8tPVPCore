package ca.xera.core;

import ca.xera.core.common.NBTHelper;
import ca.xera.core.common.Utils;
import ca.xera.core.common.velocity.PluginMessaging;
import ca.xera.core.kit.Kit;
import ca.xera.core.kit.KitHelper;
import ca.xera.core.velocity.ChatListener;
import ca.xera.core.velocity.InventorySync;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import me.txmc.protocolapi.reflection.ClassProcessor;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

@Getter
public final class PVPCore extends JavaPlugin {

    public static PluginMessaging messaging;
    private static PVPCore plugin;
    private MongoClient client;

    public static PVPCore get() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        messaging = new PluginMessaging(this);
        client = MongoClients.create("mongodb://vps01.iceanarchy.org:27017");

        // load runtime mixin injections
        loadMixins();

        // register plugin messaging
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "xera:invrequest");
        getServer().getMessenger().registerIncomingPluginChannel(this, "xera:invreply", new InventorySync());

        // register events
        registerListener(new ChatListener());

        // plugin messaging test command
        registerCommand("connect", (sender, command, label, args) -> {
            if (sender instanceof Player player && args.length > 0) {
                sendSyncReply(player);
                messaging.connect(player, args[0]);
            }
            return true;
        });

        // test kit serialization to the database. NOT PRODUCTION YET, TESTING PURPOSES FOR NOW
        registerCommand("savekit", (sender, command, label, args) -> {
            if (sender instanceof Player player && args.length > 0) {
                NBTTagList items = new NBTTagList();
                NBTHelper.savePlayerInventoryToTaglist(player, items);
                NBTTagCompound invetoryTag = new NBTTagCompound();
                invetoryTag.set("InvContents", items);
                Kit kit = new Kit(args[0], invetoryTag);
                KitHelper.saveKitToDatabase(kit);
                Utils.sendMessage(player, String.format("&a%s &3saved to the database.", args[0]));
            }
            return true;
        });

        registerCommand("loadkit", (sender, command, label, args) -> {
            if (sender instanceof Player player && args.length > 0) {
                Kit kit = KitHelper.getKit(args[0]);
                if (kit == null) {
                    Utils.sendMessage(player, String.format("&c%s is not a kit!", args[0]));
                } else {
                    kit.setLoadout(player);
                    Utils.sendMessage(player, String.format("&3Equipped kit &a%s", args[0]));
                }
            }
            return true;
        });
    }

    private void sendSyncReply(Player player) {
        NBTTagList items = new NBTTagList();
        NBTHelper.savePlayerInventoryToTaglist(player, items);
        NBTTagCompound requested = new NBTTagCompound();
        requested.set("InvContents", items);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        NBTHelper.writeNBT(requested, out);
        player.sendPluginMessage(PVPCore.get(), "xera:invrequest", out.toByteArray());
    }

    @Override
    public void onDisable() {
        // make sure to unregister the registered channels in case of a reload
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    private void loadMixins() {
        File mixinJar = new File(".", "mixins-temp.jar");
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("mixins.dat");
            if (is == null) throw new RuntimeException("The plugin jar is missing the mixins");
            Files.copy(is, mixinJar.toPath());
            URLClassLoader ccl = new URLClassLoader(new URL[]{mixinJar.toURI().toURL()});
            Class<?> mixinMainClass = Class.forName(String.format("%s.mixin.MixinMain", getClass().getPackage().getName()), true, ccl);
            Object instance = mixinMainClass.newInstance();
            Method mainM = instance.getClass().getDeclaredMethod("init", JavaPlugin.class);
            mainM.invoke(instance, this);
        } catch (Throwable t) {
            getLogger().severe(String.format("Failed to load mixins due to %s. Please see the stacktrace below for more info", t.getClass().getName()));
            t.printStackTrace();
        } finally {
            if (mixinJar.exists()) mixinJar.delete();
        }
    }

    public void registerListener(Listener listener) {
        if (ClassProcessor.hasAnnotation(listener)) ClassProcessor.process(listener);
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public void registerCommand(String name, CommandExecutor command) {
        CraftServer cs = (CraftServer) Bukkit.getServer();
        if (ClassProcessor.hasAnnotation(command)) ClassProcessor.process(command);
        cs.getCommandMap().register(name, new org.bukkit.command.Command(name) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                command.onCommand(sender, this, commandLabel, args);
                return true;
            }
        });

    }
}
