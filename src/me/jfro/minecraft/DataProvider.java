package me.jfro.minecraft;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: jerome
 * Date: 8/28/11
 * Time: 8:32 PM
 * Provides the base code for a data provider
 */
public abstract class DataProvider {
    Configuration config;
    Logger logger;
    
    public DataProvider(Configuration config, Logger logger) throws DataProviderException {
        this.config = config;
        this.logger = logger;
    }
    
    public abstract void playerJoined(Player player) throws DataProviderException;
    public abstract void playerLeft(Player player) throws DataProviderException;
    public abstract void increasePlayerStat(Player player, String statisticKey) throws DataProviderException;

    public abstract Long getPlayerLongStat(Player player, String statisticKey) throws DataProviderException;
    public abstract Date getPlayerDateStat(Player player, String statisticKey) throws DataProviderException;
    public abstract String getPlayerStringStat(Player player, String statisticKey) throws DataProviderException;
    public abstract Long getPlayerLongStat(String playerName, String statisticKey) throws DataProviderException;
    public abstract Date getPlayerDateStat(String playerName, String statisticKey) throws DataProviderException;
    public abstract String getPlayerStringStat(String playerName, String statisticKey) throws DataProviderException;
}
