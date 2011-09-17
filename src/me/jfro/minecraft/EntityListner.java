package me.jfro.minecraft;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;


/**
 * Created by IntelliJ IDEA.
 * User: jerome
 * Date: 9/14/11
 * Time: 7:16 PM
 * Listens for entity events like deaths to log them for stats
 */
public class EntityListner extends org.bukkit.event.entity.EntityListener {

    private UserStatsPlugin plugin;

    public EntityListner(UserStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        // SOMEONE DIED (or something....)
        Entity entity = event.getEntity();
        if(entity instanceof Player) {
            Player player = (Player)entity;
            String reason = "unknown";
            String source = null;
            EntityDamageEvent lastDamage = player.getLastDamageCause();

            if(lastDamage instanceof EntityDamageByBlockEvent) {
                Block damager = ((EntityDamageByBlockEvent) lastDamage).getDamager();
                this.plugin.logInfo("Damaged by block: " + damager.getClass().toString() + " " + damager.getType().toString());
                source = StatsUtils.keyForBlock(damager);
            }
            else if(lastDamage instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();
                this.plugin.logInfo("Damaged by entity: " + damager.getClass().toString());
                source = StatsUtils.keyForEntity(damager);
            }
            
            reason = StatsUtils.keyForDamageCause(lastDamage.getCause());
            String key = "death." + reason;
            if(source != null) {
                key += "_" + source;
            }
            this.plugin.logInfo(player.getName() + " died: " + key);
            try {
                this.plugin.getData().increasePlayerStat(player, key);
            } catch (DataProviderException e) {
                this.plugin.logWarning("Failed to update " + key + " for " + player.getName());
                e.printStackTrace();
            }
        }
    }
}
