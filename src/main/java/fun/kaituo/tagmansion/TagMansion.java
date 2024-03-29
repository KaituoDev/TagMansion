package fun.kaituo.tagmansion;

import fun.kaituo.gameutils.GameUtils;
import fun.kaituo.gameutils.event.PlayerChangeGameEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;


public class TagMansion extends JavaPlugin implements Listener {
    private GameUtils gameUtils;
    static List<Player> players;
    static long gameTime;

    public static TagMansionGame getGameInstance() {
        return TagMansionGame.getInstance();
    }

    @EventHandler
    public void onButtonClicked(PlayerInteractEvent pie) {
        if (!pie.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (!pie.getClickedBlock().getType().equals(Material.OAK_BUTTON)) {
            return;
        }
        if (pie.getClickedBlock().getLocation().equals(new Location(gameUtils.getWorld(), -1000, 203, 5))) {
            TagMansionGame.getInstance().startGame();
        }
    }

    @EventHandler
    public void setGameTime(PlayerInteractEvent pie) {
        if (pie.getClickedBlock() == null) {
            return;
        }
        Location location = pie.getClickedBlock().getLocation();
        long x = location.getBlockX();
        long y = location.getBlockY();
        long z = location.getBlockZ();
        if (x == -1000 && y == 204 && z == 5) {
            switch ((int) gameTime) {
                case 3600:
                case 6000:
                case 8400:
                    gameTime += 2400;
                    break;
                case 10800:
                    gameTime = 3600;
                    break;
                default:
                    break;
            }
            Sign sign = (Sign) pie.getClickedBlock().getState();
            sign.setLine(2, "当前时间为 " + gameTime / 1200 + " 分钟");
            sign.update();
        }
    }

    public void onEnable() {
        gameUtils = (GameUtils) Bukkit.getPluginManager().getPlugin("GameUtils");
        players = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, this);
        gameTime = 6000;
        Sign sign = (Sign) gameUtils.getWorld().getBlockAt(-1000, 204, 5).getState();
        sign.setLine(2, "当前时间为 " + gameTime / 1200 + " 分钟");
        sign.update();
        gameUtils.registerGame(getGameInstance());
    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll((Plugin) this);
        for (Player p: Bukkit.getOnlinePlayers()) {
            if (gameUtils.getPlayerGame(p) == getGameInstance()) {
                Bukkit.dispatchCommand(p, "join Lobby");
            }
        }
        gameUtils.unregisterGame(getGameInstance());
    }

}

