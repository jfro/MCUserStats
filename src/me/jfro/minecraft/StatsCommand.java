package me.jfro.minecraft;

import me.jfro.minecraft.UserStatsPlugin;
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
        sender.sendMessage(ChatColor.RED + "[====" + ChatColor.GREEN + " /stats " + ChatColor.RED + "====]");
        sender.sendMessage(ChatColor.RED + "Currently unimplemented");
        return true;
    }
}
