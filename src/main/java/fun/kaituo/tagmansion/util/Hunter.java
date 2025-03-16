package fun.kaituo.tagmansion.util;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import fun.kaituo.tagmansion.TagMansion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Hunter extends PlayerData{
    public Hunter(Player p) {
        super(p);
    }

    @Override
    public void applyPotionEffects() {
        super.applyPotionEffects();
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, -1, 4, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, -1, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 0, false, false));
    }

    @EventHandler
    public void preventJumpDuringAttackCooldown(PlayerJumpEvent e) {
        if (!e.getPlayer().getUniqueId().equals(playerId)) {
            return;
        }
        if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!e.getDamager().getUniqueId().equals(playerId)) {
            return;
        }
        PlayerData victimData = TagMansion.inst().idDataMap.get(e.getEntity().getUniqueId());
        if (victimData == null) {
            return;
        }
        if (victimData instanceof Hunter) {
            e.setCancelled(true);
        } else if (victimData instanceof Human) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 99));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 4));
        }
    }
}
