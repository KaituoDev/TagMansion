package fun.kaituo.tagmansion.item;

import fun.kaituo.tagmansion.TagMansion;
import fun.kaituo.tagmansion.state.HuntState;
import fun.kaituo.tagmansion.util.Item;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class ExtendTime extends Item {
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
        HuntState.INST.setRemainingTime(HuntState.INST.getRemainingTime() + 300);
        for (Player player : TagMansion.inst().getPlayers()) {
            player.sendMessage("§c鬼使用了道具，游戏时间增加了15秒！");
        }
        return true;
    }
}
