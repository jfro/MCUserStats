package me.jfro.minecraft;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import java.sql.*;
import java.util.logging.Logger;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jerome
 * Date: 8/29/11
 * Time: 2:13 PM
 * Provides storage to MySQL/SQLite/PostgreSQL (or eventually)
 */
public class SQLDataProvider extends DataProvider {
    PreparedStatement add_user;
    PreparedStatement stats_update;
    PreparedStatement stats_create;
    PreparedStatement get_user_id;
    PreparedStatement get_stat;
    
    public SQLDataProvider(Configuration config, Logger logger) throws DataProviderException {
        super(config, logger);
        String url = config.getString("storage.uri");
        String user = config.getString("storage.username");
        String password = config.getString("storage.password");

        try {
            init(url, user, password);
        }
        catch(SQLException e) {
            logger.warning("Failed to initialize connection to SQL server: " + e.getLocalizedMessage());
        }
    }

    protected void init(String url, String username, String password) throws SQLException {
        MysqlDataSource server = new MysqlDataSource();
        server.setUrl(url);
        server.setUser(username);
        server.setPassword(password);
        server.setUseServerPrepStmts(true);
        server.setCachePreparedStatements(true);
        server.setPreparedStatementCacheSize(50);
        Connection db = server.getConnection();
        Statement direct = db.createStatement();

        // sharing with Perms users table, adding our own UserStats table
        direct.executeUpdate("CREATE TABLE IF NOT EXISTS Users (userid INT NOT NULL AUTO_INCREMENT, username VARCHAR(64) NOT NULL, PRIMARY KEY(userid), INDEX (username))");
        direct.executeUpdate("CREATE TABLE IF NOT EXISTS UserStats (statid INT NOT NULL AUTO_INCREMENT, userid INT NOT NULL, statname VARCHAR(64) NOT NULL, statvalue VARCHAR(128),PRIMARY KEY(statid), INDEX (userid), INDEX(statname))");

        add_user = db.prepareStatement("INSERT INTO Users SET username=?");
        get_stat = db.prepareStatement("SELECT statvalue FROM UserStats WHERE userid = ? AND statname = ?");
        stats_update = db.prepareStatement("UPDATE UserStats SET statvalue = ? WHERE userid=? AND statname=?");
        stats_create = db.prepareStatement("INSERT INTO UserStats (userid, statname, statvalue) VALUES(?,?,?)");
        get_user_id = db.prepareStatement("SELECT userid FROM Users WHERE username=?");
    }

    // Returns true if the user needed to be created.
    public synchronized boolean createUser(String user) throws DataProviderException {
        if (getUserID(user) == null) {
            execute(add_user, user);
            return true;
        }
        return false;
    }

    // Returns null if no such user.
    public Integer getUserID(String user) throws DataProviderException {
        return getInteger(get_user_id, user);
    }

    /*
     *  Protected methods for managing PreparedStatements and translating
     *  SQLExceptions into PermissionsDataExceptions.
     */

    protected synchronized ResultSet query(PreparedStatement statement, Object ... parameters) throws DataProviderException {
        try {
            for (int i=0; i<parameters.length; i++) {
                statement.setObject(i+1, parameters[i]);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new DataProviderException("Unable to query database.", e);
        }
    }

    protected synchronized int execute(PreparedStatement statement, Object ... parameters) throws DataProviderException {
        try {
            for (int i=0; i<parameters.length; i++) {
                statement.setObject(i+1, parameters[i]);
            }
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataProviderException("Unable to query database.", e);
        }
    }

    protected synchronized String getString(PreparedStatement statement, Object ... parameters) throws DataProviderException {
        ResultSet result = query(statement, parameters);

        try {
            if (result.next()) {
                return result.getString(1);
            }
        } catch (SQLException e) {
            throw new DataProviderException("Unable to retrieve value.", e);
        }
        return null;
    }

    protected synchronized Integer getInteger(PreparedStatement statement, Object ... parameters) throws DataProviderException {
        ResultSet result = query(statement, parameters);

        try {
            if (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataProviderException("Unable to retrieve value.", e);
        }
        return null;
    }

    protected synchronized Long getLong(PreparedStatement statement, Object ... parameters) throws DataProviderException {
        ResultSet result = query(statement, parameters);

        try {
            if (result.next()) {
                return result.getLong(1);
            }
        } catch (SQLException e) {
            throw new DataProviderException("Unable to retrieve value.", e);
        }
        return null;
    }

    protected synchronized Boolean getBoolean(PreparedStatement statement, Object ... parameters) throws DataProviderException {
        ResultSet result = query(statement, parameters);

        try {
            if (result.next()) {
                return result.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new DataProviderException("Unable to retrieve value.", e);
        }
        return null;
    }

    protected synchronized Date getDate(PreparedStatement statement, Object ... parameters) throws DataProviderException {
        ResultSet result = query(statement, parameters);

        try {
            if (result.next()) {
                return result.getTimestamp(1);
            }
        } catch (SQLException e) {
            throw new DataProviderException("Unable to retrieve value.", e);
        }
        return null;
    }

    protected void updateStat(Integer userid, String stat, Object value) throws DataProviderException {
        ResultSet result = query(get_stat, userid, stat);

        try {
            if (result.next()) {
                execute(stats_update, value, userid, stat);
            }
            else {
                execute(stats_create, userid, stat, value);
            }
        } catch (SQLException e) {
            throw new DataProviderException("Unable to retrieve value.", e);
        }
    }

    // actual provider calls
    public void playerJoined(Player player) throws DataProviderException {
        createUser(player.getName());
        Integer userid = getUserID(player.getName());
        updateStat(userid, "online", true);
        updateStat(userid, "last_connect_date", new Date());
        Integer joins = getInteger(get_stat, userid, "joins");
        if(joins == null)
            joins = 0;
        updateStat(userid, "joins", joins + 1);
        if(getDate(get_stat, userid, "first_seen_date") == null) {
            updateStat(userid, "first_seen_date", new Date());
        }
    }

    public void playerLeft(Player player) throws DataProviderException {
        Date disconnectDate = new Date();
        createUser(player.getName());
        Integer userid = getUserID(player.getName());
        updateStat(userid, "online", false);
        updateStat(userid, "last_disconnect_date", disconnectDate);

        Date connectDate = getDate(get_stat, userid, "last_connect_date");
        if(connectDate != null) {
            Long time_played = getLong(get_stat, userid, "time_played");
            if(time_played == null)
                time_played = (long)0;
            Long diff = (disconnectDate.getTime()/1000 - connectDate.getTime()/1000);
            time_played += diff;
            updateStat(userid, "time_played", time_played);
        }
    }

    public void increasePlayerStat(Player player, String statisticKey) throws DataProviderException {
        createUser(player.getName());
        Integer userid = getUserID(player.getName());
        Long currentValue = getLong(get_stat, userid, statisticKey);
        if(currentValue == null)
            currentValue = 0L;
        updateStat(userid, statisticKey, currentValue + 1);
    }
}
