package fun.kaituo.tagmansion.item;

import fun.kaituo.tagmansion.state.HuntState;
import fun.kaituo.tagmansion.util.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class Freeze extends Item {
    @Override
    public boolean use(Player p) {
        Set<Player> targets = new HashSet<>();
        for (Player hunter : HuntState.INST.getHunters()) {
            if (hunter.getLocation().distance(p.getLocation()) > 6) {
                continue;
            }
            targets.add(hunter);
        }
        if (targets.isEmpty()) {
            p.sendMessage("§c附近没有鬼！");
            return false;
        }
        for (Player hunter: targets) {
            hunter.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 99));
            hunter.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 99));
            hunter.sendMessage("§c你被冻住了！");
        }
        p.sendMessage("§a成功冻结身边的鬼！");
        return true;
    }

    @Override
    public boolean isHumanItem() {
        return true;
    }

    @Override
    public boolean isHunterItem() {
        return false;
    }
}
