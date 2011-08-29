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

    private DataProvider data;

    public PlayerListener(DataProvider data) {
        this.data = data;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        data.playerJoined(event.getPlayer());
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        data.playerLeft(event.getPlayer());
    }

    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        data.playerLeft(event.getPlayer());
    }
}
