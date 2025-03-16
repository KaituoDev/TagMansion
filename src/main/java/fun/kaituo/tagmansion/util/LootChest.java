package fun.kaituo.tagmansion.util;

import fun.kaituo.tagmansion.TagMansion;
import fun.kaituo.tagmansion.state.HuntState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class LootChest {
    public static final double CHANCE = 0.1;
    private final Location location;
    private final Random random = new Random();

    public LootChest(Location location) {
        this.location = location;
    }

    private final Set<Integer> taskIds = new HashSet<>();

    public void enable() {
        taskIds.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(TagMansion.inst(), () -> {
            if (random.nextDouble() > CHANCE) {
                return;
            }
            addItem(HuntState.INST.getRandomItem());
        }, 100, 100));
    }

    public void disable() {
        for (int taskId : taskIds) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        taskIds.clear();
        clear();
    }

    public void addItem(ItemStack item) {
        if (location.getBlock().getType() != Material.CHEST && location.getBlock().getType() != Material.TRAPPED_CHEST) {
            Bukkit.broadcastMessage("x " + location.getX() + " y " + location.getY() + " z " + location.getZ() + " 不是箱子！");
        }
        ((Chest) (location.getBlock().getState())).getBlockInventory().addItem(item);
    }

    public void clear() {
        if (location.getBlock().getType() != Material.CHEST && location.getBlock().getType() != Material.TRAPPED_CHEST) {
            Bukkit.broadcastMessage("x " + location.getX() + " y " + location.getY() + " z " + location.getZ() + " 不是箱子！");
        }
        ((Chest) (location.getBlock().getState())).getBlockInventory().clear();
    }
}
