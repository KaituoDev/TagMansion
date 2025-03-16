package fun.kaituo.tagmansion.item;

import fun.kaituo.tagmansion.TagMansion;
import fun.kaituo.tagmansion.util.Item;
import fun.kaituo.tagmansion.util.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static fun.kaituo.tagmansion.util.Misc.isCharacterHuman;

@SuppressWarnings("unused")
public class Speed extends Item {
    @Override
    public boolean isHumanItem() {
        return true;
    }

    @Override
    public boolean isHunterItem() {
        return true;
    }

    @Override
    public boolean use(Player p) {
        PlayerData data = TagMansion.inst().idDataMap.get(p.getUniqueId());
        assert data != null;
        if (isCharacterHuman(data.getClass())) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
        } else {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        }
        p.sendMessage("§a获得加速！");
        return true;
    }
}
