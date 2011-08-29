package me.jfro.minecraft;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by IntelliJ IDEA.
 * User: jerome
 * Date: 8/28/11
 * Time: 9:41 PM
 * Handles player events the plugin is interested in
 */
public class PlayerListener extends org.bukkit.event.player.PlayerListener {

    private UserStatsPlugin plugin;

    public PlayerListener(UserStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            plugin.getData().playerJoined(event.getPlayer());
        }
        catch(DataProviderException e) {
            plugin.logWarning("Failed to log player join event: "+e.getLocalizedMessage());
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            plugin.getData().playerLeft(event.getPlayer());
        }
        catch(DataProviderException e) {
            plugin.logWarning("Failed to log player quit event: "+e.getLocalizedMessage());
        }
    }

    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        try {
            plugin.getData().playerLeft(event.getPlayer());
        }
        catch(DataProviderException e) {
            plugin.logWarning("Failed to log player kick event: "+e.getLocalizedMessage());
        }
    }
}
