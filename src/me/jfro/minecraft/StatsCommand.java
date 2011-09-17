package me.jfro.minecraft;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


/**
 * Created by IntelliJ IDEA.
 * User: jerome
 * Date: 8/28/11
 * Time: 10:19 PM
 * Handles processing the /stats command
 */
class StatsCommand implements CommandExecutor {

    private UserStatsPlugin plugin;

    public StatsCommand(UserStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    /**
     * @todo implement this
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        String playerName = sender.getName();
        if(split.length > 0) {
            playerName = split[0];
        }

        sender.sendMessage(ChatColor.RED + "[====" + ChatColor.GREEN + " " + playerName + " stats " + ChatColor.RED + "====]");

        try {
            Long time_played = this.plugin.getData().getPlayerLongStat(playerName, "time_played");
            Long joins = this.plugin.getData().getPlayerLongStat(playerName, "joins");
            Long deaths = this.plugin.getData().getPlayerLongStat(playerName, "death.total");
            sender.sendMessage("Joins: " + joins);
            sender.sendMessage("Time played: " + time_played);
            sender.sendMessage("Deaths: " + deaths.toString());
        } catch (DataProviderException e) {
            this.plugin.logWarning("Failed to fetch statistics for " + playerName);
            e.printStackTrace();
        }
        return true;
    }
}
