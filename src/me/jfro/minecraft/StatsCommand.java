package me.jfro.minecraft;

import com.ocpsoft.pretty.time.Duration;
import com.ocpsoft.pretty.time.PrettyTime;
import com.ocpsoft.pretty.time.units.Second;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Locale;


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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        String playerName = sender.getName();
        if(split.length > 0) {
            playerName = split[0];
        }

        if(playerName.equalsIgnoreCase("console")) {
            sender.sendMessage(ChatColor.RED + "usage: stats <player>");
            return true;
        }

        try {
            if(!plugin.getData().playerNameExists(playerName)) {
                sender.sendMessage(ChatColor.RED + "no such player: " + playerName);
                return true;
            }
            sender.sendMessage(ChatColor.RED + "[====" + ChatColor.GREEN + " Stats for " + playerName + " " + ChatColor.RED + "====]");
            String time_played = this.plugin.getData().getPlayerStringInfo(playerName, "time_played");
            if(time_played != null) {
                time_played = StatsUtils.elapsedTimeString(new Double(time_played));
            }
            String joins = this.plugin.getData().getPlayerStringInfo(playerName, "stats.joins");
            Long deaths = this.plugin.getData().getPlayerLongStat(playerName, "death.total");
            sender.sendMessage("Joins: " + joins);
            Player player = this.plugin.getServer().getPlayer(playerName);
            PrettyTime p = new PrettyTime();
            if(player != null && player.isOnline()) {
                Date last_connect = this.plugin.getData().getPlayerDateInfo(playerName, "last_connect_date");
                sender.sendMessage("Online since: " + p.format(last_connect));
            }
            else {
                Date last_disconnect = this.plugin.getData().getPlayerDateInfo(playerName, "last_disconnect_date");
                sender.sendMessage("Last online: " + p.format(last_disconnect));
            }
            sender.sendMessage("Time played: " + time_played);
            sender.sendMessage("Deaths: " + deaths.toString());
        } catch (DataProviderException e) {
            this.plugin.logWarning("Failed to fetch statistics for " + playerName);
            e.printStackTrace();
        }
        return true;
    }
}
