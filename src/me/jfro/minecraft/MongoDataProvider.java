package me.jfro.minecraft;

import com.mongodb.*;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: jerome
 * Date: 8/28/11
 * Time: 8:34 PM
 * Stores player stats in MongoDB
 */
public class MongoDataProvider extends DataProvider {
    DB db;
    String playersCollectionName = "users"; // to share storage with other mongo using plugins

    public MongoDataProvider(Configuration config, Logger logger) throws DataProviderException {
        super(config, logger);

        String url = config.getString("storage.uri");
        String user = config.getString("storage.username");
        String password = config.getString("storage.password");

        // connect and stuff
        try {
//            logInfo("MongoDB permissions storage initializing");
            db = Mongo.connect(new DBAddress(url));
            if(user != null && user.length() > 0 && password != null) {
                if(!db.authenticate(user, password.toCharArray())) {
                    throw new DataProviderException("Failed to authenticate with mongodb");
                }
            }
        }
        catch(MongoException e) {
            throw new DataProviderException("Error connecting to mongodb: " + url);
        }
        catch(UnknownHostException e) {
            throw new DataProviderException("Unknown host: " + url);
        }
    }

    public boolean playerNameExists(String playerName) throws DataProviderException {
        return getPlayerObject(playerName) != null;
    }

    public void playerJoined(Player player) {
        BasicDBObject playerObj = getPlayerObject(player);
        if(playerObj == null) {
            logger.info("Player not found: "+player.getName());
            return;
        }
        BasicDBObject stats = (BasicDBObject)playerObj.get("stats");
        if(stats == null)
            stats = new BasicDBObject().append("joins", 0);
        Date now = new Date();
        BasicDBObject changes = new BasicDBObject();
        changes.put("online", true);
        changes.put("last_connect_date", now);
        changes.put("stats.joins", stats.getLong("joins")+1);
        // update first seen if it's not been set yet
        if(playerObj.get("first_seen_date") == null) {
            changes.put("first_seen_date", now);
        }
        updatePlayerInfo(player, changes);
    }

    public void playerLeft(Player player) {
        BasicDBObject playerObj = getPlayerObject(player);
        if(playerObj == null) {
            return;
        }
        try {
            boolean isOnline = playerObj.getBoolean("online");
            if(!isOnline)
                return; // don't update if they're already offline, in case of double-calls
        }
        catch(NullPointerException e) {
            return;
        }

        Date connectDate = (Date)playerObj.get("last_connect_date");
        Date now = new Date();
        BasicDBObject changes = new BasicDBObject();
        changes.put("online", false);
        changes.put("last_disconnect_date", now);
        // update time online
        if(connectDate != null) {
            long time_online = 0;
            if(playerObj.containsField("time_played") && playerObj.get("time_played") != null)
                time_online = playerObj.getLong("time_played");
            time_online += (now.getTime() - connectDate.getTime()) / 1000; // we store in seconds not ms
            changes.put("time_played", time_online);
        }
        updatePlayerInfo(player, changes);
    }

    public void increasePlayerStat(Player player, String statisticKey) {
        BasicDBObject playerObj = getPlayerObject(player);
        BasicDBObject changes = new BasicDBObject();
        String fullKey = "stats." + statisticKey;
        changes.put(fullKey, 1); // increase the keypath value by 1, sets to 1 if doesn't exist yet
        BasicDBObject query = new BasicDBObject().append("lowercase_username", playerObj.getString("lowercase_username"));
        BasicDBObject set = new BasicDBObject();
        set.put("$inc", changes);
        WriteResult result = db.getCollection(playersCollectionName).update(query, set);
        if(result.getN() == 0) {
            logger.warning("Failed to update "+playerObj.getString("lowercase_username")+" with changes: " + set.toString());
            logger.warning("Error: " + result.getError());
        }
    }

    public Long getPlayerLongStat(Player player, String statisticKey) throws DataProviderException {
        return getPlayerLongStat(player.getName(), statisticKey);
    }

    public Date getPlayerDateStat(Player player, String statisticKey) throws DataProviderException {
        return getPlayerDateStat(player.getName(), statisticKey);
    }

    public String getPlayerStringStat(Player player, String statisticKey) throws DataProviderException {
        return getPlayerStringStat(player.getName(), statisticKey);
    }

    public Long getPlayerLongStat(String playerName, String statisticKey) throws DataProviderException {
        BasicDBObject playerObj = getPlayerObject(playerName);
        if(playerObj != null) {
            BasicDBObject stats = (BasicDBObject)playerObj.get("stats");
            if(stats != null) {
                Object value = valueForKeyPath(stats, statisticKey);
                if(value instanceof Double)
                    return ((Double)value).longValue();
                else
                    return (Long)value;
            }
            else
                this.logger.info("No stats found");
        }
        return 0L;
    }

    public String getPlayerStringInfo(String playerName, String infoKey) throws DataProviderException {
        BasicDBObject playerObj = getPlayerObject(playerName);
        if(playerObj != null) {
            Object value = valueForKeyPath(playerObj, infoKey);
            if(value != null)
                return value.toString();
        }
        return null;
    }

    public Date getPlayerDateInfo(String playerName, String infoKey) throws DataProviderException {
        BasicDBObject playerObj = getPlayerObject(playerName);
        if(playerObj != null) {
            Object value = valueForKeyPath(playerObj, infoKey);
            if(value != null && value instanceof Date)
                return (Date)value;
            else {
                this.logger.info("Unknown or null: " + value + " " + value.getClass().toString());
            }
        }
        return null;
    }

    public Date getPlayerDateStat(String playerName, String statisticKey) throws DataProviderException {

        return null;
    }

    public String getPlayerStringStat(String playerName, String statisticKey) throws DataProviderException {
        BasicDBObject playerObj = getPlayerObject(playerName);
        if(playerObj != null) {
            BasicDBObject stats = (BasicDBObject)playerObj.get("stats");
            if(stats != null) {
                Object value = valueForKeyPath(stats, statisticKey);
                return value.toString();
            }
        }
        return null;
    }

    // ----
    protected Object valueForKeyPath(BasicDBObject object, String keyPath) {
        String firstKey = StatsUtils.firstPartOfKeyPath(keyPath);
        String restOfPath = StatsUtils.restOfKeyPath(keyPath);
        Object value = object.get(firstKey);
        if(value != null && value instanceof BasicDBObject && restOfPath != null) {
            return valueForKeyPath((BasicDBObject)value, restOfPath);
        }
        return value;
    }

    protected boolean updatePlayerInfo(Player player, BasicDBObject changes) {
        BasicDBObject playerObj = getPlayerObject(player);
        if(playerObj == null) {
            logger.warning("Player not found: "+player.getName());
            return false;
        }

        BasicDBObject query = new BasicDBObject().append("lowercase_username", playerObj.getString("lowercase_username"));
        BasicDBObject set = new BasicDBObject();
        set.put("$set", changes);
        WriteResult result = db.getCollection(playersCollectionName).update(query, set);
        if(result.getN() == 0) {
            logger.warning("Failed to update "+playerObj.getString("lowercase_username")+" with changes: " + changes.toString());
            logger.warning("Error: " + result.getError());
            return false;
        }
        return true;
    }

    protected BasicDBObject getPlayerObject(String playerName) {
        BasicDBObject playerObject;
        BasicDBObject query = new BasicDBObject().append("lowercase_username", playerName.toLowerCase());
        DBCollection usersCollection = db.getCollection(playersCollectionName);
        playerObject = (BasicDBObject)usersCollection.findOne(query);
//        if(playerObject == null && createIfMissing) {
//            // create new object
//            playerObject = createNewPlayerObject(player);
//        }
        return playerObject;
    }

    /**
     * Fetches or creates a new player object
     * @param player player to fetch from database
     * @return A database object representing the player
     */
    protected BasicDBObject getPlayerObject(Player player) {
        return getPlayerObject(player, true);
    }

    protected BasicDBObject getPlayerObject(Player player, boolean createIfMissing) {
        BasicDBObject playerObject;
        BasicDBObject query = new BasicDBObject().append("lowercase_username", player.getName().toLowerCase());
        DBCollection usersCollection = db.getCollection(playersCollectionName);
        playerObject = (BasicDBObject)usersCollection.findOne(query);
        if(playerObject == null && createIfMissing) {
            // create new object
            playerObject = createNewPlayerObject(player);
        }
        return playerObject;
    }

    /**
     * Creates a new player object with default values, storing it in the database
     * @param player Player to create object for
     * @return A new BasicDBObject with default values
     */
    protected BasicDBObject createNewPlayerObject(Player player) {
        BasicDBObject playerObject = new BasicDBObject();
        playerObject.put("username", player.getName());
        playerObject.put("lowercase_username", player.getName().toLowerCase()); // used for queries
        playerObject.put("online", false);
        playerObject.put("time_played", 0);
        playerObject.put("last_connect_date", null);
        playerObject.put("last_disconnect_date", null);
        playerObject.put("first_seen_date", null);
        BasicDBObject stats = new BasicDBObject();
        stats.put("joins", 0);
        playerObject.put("stats", stats);
        db.getCollection(playersCollectionName).insert(playerObject);
        return playerObject;
    }
}
