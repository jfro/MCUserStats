package me.jfro.minecraft;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: jerome
 * Date: 8/28/11
 * Time: 8:21 PM
 * Plugin for tracking users stats like online/offline in a database
 */
public class UserStatsPlugin extends JavaPlugin {
//    private DataProvider data;
    private PlayerListener playerListener;
    private DataProvider data;
    
    @Override
    public void onEnable() {
        // Write some default configuration
        if (!new File(getDataFolder(), "config.yml").exists()) {
            logInfo("Generating default config.yml");
            writeDefaultConfiguration();
        }

        
        try {
            String type = getConfiguration().getString("storage.type");
            logInfo("Using " + type + " storage engine");
            if(type.equalsIgnoreCase("mongodb")) {
                data = new MongoDataProvider(getConfiguration(), getServer().getLogger());
            }
            else if(type.equalsIgnoreCase("mysql")) {
                data = new SQLDataProvider(getConfiguration(), getServer().getLogger());
            }
            else {
                throw new RuntimeException("Unsupported UserStats storage type: "+type);
            }
        } catch (DataProviderException e) {
            throw new RuntimeException("Unable to load UserStats data!", e);
        }

        playerListener = new PlayerListener(this);

        PluginManager pm = getServer().getPluginManager();
//        pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Monitor, this);

        StatsCommand statsCommand = new StatsCommand(this);
        getCommand("stats").setExecutor(statsCommand);

        logInfo("Enabled");
    }

    @Override
    public void onDisable() {
        playerListener = null;
        logInfo("Disabled");
    }

    public DataProvider getData() {
        return this.data;
    }

    protected void logInfo(String message) {
        getServer().getLogger().info("[UserStats] "+message);
    }

    protected void logWarning(String message) {
        getServer().getLogger().warning("[UserStats] "+message);
    }

    protected void writeDefaultConfiguration() {
        HashMap<String, Object> storage = new HashMap<String, Object>();
        storage.put("type", "mongodb"); // can be mysql, pgsql, sqlite, mongodb, yml (only mongodb right now)
        storage.put("uri", "localhost/minecraft");
        storage.put("username", "");
        storage.put("password", "");

        getConfiguration().setProperty("storage", storage);
        getConfiguration().setHeader("# Default UserStats config.yml",
                "# Currently only mongodb and mysql are supported for type:",
                "# uri format for mongodb: 'host:port/dbname', port optional",
                "# uri format for mysql: jdbc:mysql://localhost:8889/minecraft"
                );
        getConfiguration().save();
    }
}
