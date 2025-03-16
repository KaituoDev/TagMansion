package fun.kaituo.tagmansion.util;

import fun.kaituo.gameutils.util.GameInventory;
import fun.kaituo.tagmansion.TagMansion;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class PlayerData implements Listener {
    @Getter
    protected final UUID playerId;
    @Getter
    protected Player player;

    protected Location location;
    protected final Collection<PotionEffect> potionEffects = new ArrayList<>();
    protected double health;
    protected GameInventory inventory;

    protected final Set<Integer> taskIds = new HashSet<>();

    public PlayerData(Player player) {
        playerId = player.getUniqueId();
        this.player = player;
        applyInventory();
        applyPotionEffects();
        player.setHealth(20);
        Bukkit.getPluginManager().registerEvents(this, TagMansion.inst());
    }

    public void resetPlayer() {
        player.getInventory().clear();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.setExp(0);
        player.setLevel(0);
    }

    public void save() {
        location = player.getLocation();
        potionEffects.clear();
        potionEffects.addAll(player.getActivePotionEffects());
        health = player.getHealth();
        inventory = new GameInventory(player);
    }

    public void onDestroy() {
        resetPlayer();
        HandlerList.unregisterAll(this);
        for (int i : taskIds) {
            Bukkit.getScheduler().cancelTask(i);
        }
        taskIds.clear();
        player = null;
    }

    public void applyPotionEffects() {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, -1, 0, false, false));
    }

    public void applyInventory() {
        GameInventory inv = TagMansion.inst().getInv(this.getClass().getSimpleName());
        if (inv != null) {
            inv.apply(player);
        } else {
            player.sendMessage("§cCharacter inventory " + this.getClass().getSimpleName() + " not found!");
        }
    }

    public void onQuit() {
        save();
        resetPlayer();
        HandlerList.unregisterAll(this);
        for (int i : taskIds) {
            Bukkit.getScheduler().cancelTask(i);
        }
        taskIds.clear();
        player = null;
    }

    public void onRejoin() {
        Player p = Bukkit.getPlayer(playerId);
        assert p != null;
        this.player = p;
        p.teleport(location);
        p.addPotionEffects(potionEffects);
        p.setHealth(health);
        inventory.apply(p);
        Bukkit.getPluginManager().registerEvents(this, TagMansion.inst());
    }
}