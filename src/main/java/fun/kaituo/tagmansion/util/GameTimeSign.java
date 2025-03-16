package fun.kaituo.tagmansion.util;

import fun.kaituo.gameutils.util.AbstractSignListener;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GameTimeSign extends AbstractSignListener {
    @Getter
    private int gameTimeMinutes = 5;

    public GameTimeSign(JavaPlugin plugin, Location location) {
        super(plugin, location);
        lines.set(1, "§f游戏时间");
        lines.set(2, "§b" + gameTimeMinutes + " §f分钟");
    }

    @Override
    public void onRightClick(PlayerInteractEvent playerInteractEvent) {
        if (gameTimeMinutes < 10) {
            gameTimeMinutes += 1;
        }
        lines.set(2, "§b" + gameTimeMinutes + " §f分钟");
        update();
    }

    @Override
    public void onSneakingRightClick(PlayerInteractEvent playerInteractEvent) {
        if (gameTimeMinutes > 1) {
            gameTimeMinutes -= 1;
        }
        lines.set(2, "§b" + gameTimeMinutes + " §f分钟");
        update();
    }
}
