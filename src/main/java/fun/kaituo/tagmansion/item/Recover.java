package fun.kaituo.tagmansion.item;

import fun.kaituo.tagmansion.util.Item;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class Recover extends Item {
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
        p.setHealth(p.getMaxHealth());
        p.sendMessage("§a生命全部恢复！");
        return true;
    }
}
