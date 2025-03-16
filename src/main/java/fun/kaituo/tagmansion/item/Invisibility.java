package fun.kaituo.tagmansion.item;

import fun.kaituo.tagmansion.util.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Invisibility extends Item {
    @Override
    public boolean isHumanItem() {
        return true;
    }

    @Override
    public boolean isHunterItem() {
        return false;
    }

    @Override
    public boolean use(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, false, false));
        p.sendMessage("§a你获得了隐身效果！");
        return true;
    }
}
