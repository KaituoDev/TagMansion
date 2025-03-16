package fun.kaituo.tagmansion.item;

import fun.kaituo.tagmansion.TagMansion;
import fun.kaituo.tagmansion.state.HuntState;
import fun.kaituo.tagmansion.util.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Glow extends Item {
    @Override
    public boolean isHumanItem() {
        return false;
    }

    @Override
    public boolean isHunterItem() {
        return true;
    }

    @Override
    public boolean use(Player p) {
        for (Player target : HuntState.INST.getHumans()) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, false, false));
        }
        for (Player player : TagMansion.inst().getPlayers()) {
            player.sendMessage("§c鬼使用了道具，所有人获得发光效果！");
        }
        return true;
    }
}
