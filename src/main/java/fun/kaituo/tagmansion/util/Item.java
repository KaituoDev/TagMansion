package fun.kaituo.tagmansion.util;

import fun.kaituo.tagmansion.TagMansion;
import fun.kaituo.tagmansion.state.HuntState;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static fun.kaituo.gameutils.util.ItemUtils.removeItem;
import static fun.kaituo.tagmansion.util.Misc.isCharacterHuman;

public abstract class Item implements Listener {
    @Getter
    protected final ItemStack itemStack;
    protected final Set<Integer> taskIds = new HashSet<>();

    public abstract boolean isHumanItem();
    public abstract boolean isHunterItem();

    public Item() {
        itemStack = TagMansion.inst().getItem(this.getClass().getSimpleName());
    }

    public static @Nullable Item getItem(ItemStack itemStack) {
        for (Item item : HuntState.INST.items) {
            if (item.itemStack.isSimilar(itemStack)) {
                return item;
            }
        }
        return null;
    }

    public abstract boolean use(Player p);

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!TagMansion.inst().playerIds.contains(p.getUniqueId())) {
            return;
        }
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block b = e.getClickedBlock();
            assert b != null;
            if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
                return;
            }
        }
        ItemStack handItem = p.getInventory().getItemInMainHand().clone();
        if (!handItem.isSimilar(itemStack)) {
            return;
        }
        // Prevent player from using items such as experience bottles
        e.setCancelled(true);
        if (p.hasCooldown(itemStack.getType())) {
            return;
        }
        if (use(p)) {
            removeItem(p.getInventory(), itemStack);
            p.setCooldown(itemStack.getType(), 10);
        }
    }

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, TagMansion.inst());
    }

    public void disable() {
        HandlerList.unregisterAll(this);
        taskIds.forEach(Bukkit.getScheduler()::cancelTask);
        taskIds.clear();
    }

    @EventHandler
    public void preventIllegalPickUp(PlayerPickupItemEvent e) {
        if (!e.getItem().getItemStack().isSimilar(itemStack)) {
            return;
        }
        Player p = e.getPlayer();
        if (!TagMansion.inst().playerIds.contains(p.getUniqueId())) {
            return;
        }
        PlayerData data = TagMansion.inst().idDataMap.get(p.getUniqueId());
        if (data == null) {
            return;
        }
        if (isCharacterHuman(data.getClass()) && !isHumanItem()) {
            e.setCancelled(true);
        }
        if (!isCharacterHuman(data.getClass()) && !isHunterItem()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void preventIllegalClick(InventoryClickEvent e) {
        ItemStack currentItem = e.getCurrentItem();
        if (currentItem == null) {
            return;
        }
        if (!currentItem.isSimilar(itemStack)) {
            return;
        }
        HumanEntity entity = e.getWhoClicked();
        if (!TagMansion.inst().playerIds.contains(entity.getUniqueId())) {
            return;
        }
        PlayerData data = TagMansion.inst().idDataMap.get(entity.getUniqueId());
        if (data == null) {
            return;
        }
        if (isCharacterHuman(data.getClass()) && !isHumanItem()) {
            e.setCancelled(true);
        }
        if (!isCharacterHuman(data.getClass()) && !isHunterItem()) {
            e.setCancelled(true);
        }
    }
}
