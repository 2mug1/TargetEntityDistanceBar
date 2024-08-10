package com.github.iamtakagi.entitydistancebar;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class TargetEntityDistanceBar extends JavaPlugin {

  private static TargetEntityDistanceBar instance;
  private TargetEntityDistanceBarConfig config;

  @Override
  public void onEnable() {
    instance = this;
    this.saveDefaultConfig();
    this.loadConfig();
    this.getServer().getScheduler().runTaskTimer(instance, new TargetEntityDistanceActionbarTask(), 0, 1);
  }

  @Override
  public void onDisable() {
    this.saveDefaultConfig();
  }

  public static TargetEntityDistanceBar getInstance() {
    return instance;
  }

  public TargetEntityDistanceBarConfig getTargetEntityDistanceBarConfig() {
    return config;
  }

  private void loadConfig() {
    this.config = new TargetEntityDistanceBarConfig((YamlConfiguration) this.getConfig());
  }

  class TargetEntityDistanceActionbarTask implements Runnable {

    @Override
    public void run() {
      for (Player player : getServer().getOnlinePlayers()) {
        Entity target = Utils.getTargetEntity(player);

        if (target == null) {
          return;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, player.getUniqueId(),
            new TextComponent(ChatColor.translateAlternateColorCodes('&',
                config.getFormat()
                    .replace("{TARGET}", target.getType().getEntityClass().getSimpleName()).replace("{DISTANCE}",
                        "" + (Math.floor(player.getLocation().distance(target.getLocation()) * 100)) / 100))));
      }
    }
  }

  class TargetEntityDistanceBarConfig {
    private boolean isEnabled;
    private String format;

    TargetEntityDistanceBarConfig(YamlConfiguration yaml) {
      this.isEnabled = yaml.getBoolean("enabled");
      this.format = yaml.getString("format");
    }

    public boolean isEnabled() {
      return this.isEnabled;
    }

    public String getFormat() {
      return this.format;
    }
  }
}

class Utils {

  public static Player getTargetPlayer(final Player player) {
    return getTarget(player, player.getWorld().getPlayers());
  }

  public static Entity getTargetEntity(final Entity entity) {
    return getTarget(entity, entity.getWorld().getEntities());
  }

  public static <T extends Entity> T getTarget(final Entity entity,
      final Iterable<T> entities) {
    if (entity == null)
      return null;
    T target = null;
    final double threshold = 1;
    for (final T other : entities) {
      final Vector n = other.getLocation().toVector()
          .subtract(entity.getLocation().toVector());
      if (entity.getLocation().getDirection().normalize().crossProduct(n)
          .lengthSquared() < threshold
          && n.normalize().dot(
              entity.getLocation().getDirection().normalize()) >= 0) {
        if (target == null
            || target.getLocation().distanceSquared(
                entity.getLocation()) > other.getLocation()
                    .distanceSquared(entity.getLocation()))
          target = other;
      }
    }
    return target;
  }
}