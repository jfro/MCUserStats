package me.jfro.minecraft;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA.
 * User: jerome
 * Date: 9/16/11
 * Time: 6:44 PM
 * Some useful utilities like getting names of entities
 */
public class StatsUtils {
    public static String elapsedTimeString(double value) {
        double t = value;
		double h = Math.floor(t / 3600);
        double d = Math.floor(h / 24);
        h %= 24;
		t %= 3600;
		double m = Math.floor(t / 60);
		double s = Math.floor(t % 60);
        NumberFormat formatter = new DecimalFormat("#");
        String df = formatter.format(d);
        String hf = formatter.format(h);
        String mf = formatter.format(m);
        String sf = formatter.format(s);
		return ( (d > 0 ? df + "d " : "") + (h > 0 ? hf + "h " + ((h > 1) ? "" : " ") : "") +
			(m > 0 ? mf + "m " + ((m > 1) ? "" : " ") : "") +
			sf + "s " + ((s > 1) ? "" : ""));
    }

    public static String firstPartOfKeyPath(String keyPath) {
        int dotIndex = keyPath
                .indexOf(".");
        if (dotIndex == -1) {
            return keyPath;
        } else {
            return keyPath.substring(0, dotIndex);
        }
    }

    public static String restOfKeyPath(String keyPath) {
        int dotIndex = keyPath
                .indexOf(".");
        if (dotIndex == -1 || keyPath.length() - 1 <= dotIndex) {
            return null;
        } else {
            return keyPath.substring(dotIndex + 1);
        }
    }
    static String keyForBlock(Block block) {
        return block.getType().name().toLowerCase();
    }

    static String keyForEntity(Entity entity) {
        String key = "unknownentity";
        if(entity instanceof Creeper) {
            key = "creeper";
        }
        else if(entity instanceof Skeleton) {
            key = "skeleton";
        }
        else if(entity instanceof PigZombie) {
            key = "pigzombie";
        }
        else if(entity instanceof Zombie) {
            key = "zombie";
        }
        else if(entity instanceof Ghast) {
            key = "ghast";
        }
        else if(entity instanceof Wolf) {
            key = "wolf";
        }
        else if(entity instanceof Enderman) {
            key = "enderman";
        }
        else if(entity instanceof CaveSpider) {
            key = "cavespider";
        }
        else if(entity instanceof Spider) {
            key = "spider";
        }
        else if(entity instanceof Silverfish) {
            key = "silverfish";
        }
        else if(entity instanceof Giant) {
            key = "giant";
        }
        else if(entity instanceof  Slime) {
            key = "slime";
        }
        else if(entity instanceof Player) {
//            key = "player"; // @todo option to not log player name
            key = "player_" + ((Player)entity).getName();
        }
        else if(entity instanceof TNTPrimed) {
            key = "tnt";
        }
        else if(entity instanceof Fireball) {
            key = "fireball";
        }
        else if(entity instanceof Arrow) {
            key = "arrow";
        }
        return key;
    }

    static String keyForDamageCause(EntityDamageEvent.DamageCause cause) {
        String key = "unknown";
        switch (cause) {
                case BLOCK_EXPLOSION:
                case ENTITY_EXPLOSION:
                    key = "explosion";
                    break;
                case FIRE:
                case FIRE_TICK:
                    key = "fire";
                    break;
                case ENTITY_ATTACK:
                    key = "attacked";
                    break;
                case FALL:
                    key = "fall";
                    break;
                case LAVA:
                    key = "lava";
                    break;
                case LIGHTNING:
                    key = "lightning";
                    break;
                case VOID:
                    key = "void";
                    break;
                case SUFFOCATION:
                    key = "suffocation";
                    break;
                case PROJECTILE:
                    key = "projectile";
                    break;
                case DROWNING:
                    key = "drowning";
                    break;
                case SUICIDE:
                    key = "suicide";
                    break;
                case CONTACT:
                    key = "contact";
                    break;
                case CUSTOM:
                    key = "unknown_custom";
                    break;
            }
        return key;
    }
}
