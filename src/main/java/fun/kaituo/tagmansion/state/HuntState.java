package fun.kaituo.tagmansion.state;

import fun.kaituo.gameutils.game.GameState;
import fun.kaituo.tagmansion.TagMansion;
import fun.kaituo.tagmansion.util.Item;
import fun.kaituo.tagmansion.util.LootChest;
import fun.kaituo.tagmansion.util.PlayerData;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.BoundingBox;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static fun.kaituo.gameutils.util.Misc.spawnFireworks;
import static fun.kaituo.tagmansion.util.Misc.isCharacterHuman;

public class HuntState implements GameState, Listener {
    public static final int HIDE_SECONDS = 20;
    public static final String START_MESSAGE = "§c感受到了§7『§0黑§7』§c的气息...";
    public static final String NO_HUNTER_MESSAGE = "§e鬼不复存在，人类胜利";
    public static final String NO_HUMAN_MESSAGE = "§c人类全灭，鬼胜利";
    public static final String TIME_UP_MESSAGE = "§e时间到，人类胜利";

    public static final HuntState INST = new HuntState();
    private HuntState() {}

    private TagMansion game;
    @Getter @Setter
    private int remainingTime = 0;
    private Location start;
    private Location wait;
    private Location waitBlock;
    private Objective remainingTimeObjective;
    private Score remainingTimeScore;
    private BoundingBox box;
    private final Set<Integer> taskIds = new HashSet<>();
    public final Set<Item> items = new HashSet<>();

    private final Set<LootChest> chests = new HashSet<>();

    private final Random random = new Random();

    @Getter
    private boolean isEnded = true;

    public void init() {
        game = TagMansion.inst();
        start = game.getLoc("start");
        wait = game.getLoc("wait");
        waitBlock = game.getLoc("waitBlock");
        remainingTimeObjective = game.getTagBoard().registerNewObjective("remainingTime", Criteria.DUMMY, "鬼抓人");
        remainingTimeScore = remainingTimeObjective.getScore("剩余时间");
        initBox();
        initItems();
        initChests();
    }

    private void initBox() {
        Location pos1 = game.getLoc("pos1");
        Location pos2 = game.getLoc("pos2");
        assert pos1 != null;
        assert pos2 != null;
        box = new BoundingBox(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
    }

    @Override
    public void enter() {
        isEnded = false;
        remainingTime = WaitState.INST.getGameTimeMinutes() * 60 * 20;
        remainingTimeObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (Player p : game.getPlayers()) {
            PlayerData data;
            Class<? extends PlayerData> characterClass = game.playerCharacterChoices.get(p.getUniqueId());
            try {
                Constructor<? extends PlayerData> constructor = characterClass.getConstructor(Player.class);
                data = constructor.newInstance(p);
            } catch (Exception e) {
                p.sendMessage("初始化角色" + characterClass.getSimpleName() + "失败");
                throw new RuntimeException(e);
            }
            game.idDataMap.put(p.getUniqueId(), data);
            game.getTagTeam().addPlayer(p);
        }
        for (Player p : getHunters()) {
            p.teleport(wait);
        }
        for (Player p : getHumans()) {
            p.teleport(start);
        }
        Bukkit.getPluginManager().registerEvents(this, game);
        enableItems();
        taskIds.add(Bukkit.getScheduler().runTaskLater(game, () -> {
            enableChests();
            removePlatform();
            for (Player p : game.getPlayers()) {
                p.sendTitle(START_MESSAGE, "", 10, 30, 20);
                p.playSound(p, Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1, 1);
            }
        }, HIDE_SECONDS * 20).getTaskId());
    }

    private void initChests() {
        Location pos1 = game.getLoc("pos1");
        Location pos2 = game.getLoc("pos2");
        assert pos1 != null;
        assert pos2 != null;
        BoundingBox box = new BoundingBox(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
        for (int x = (int) box.getMinX(); x <= box.getMaxX(); x += 1) {
            for (int y = (int) box.getMinY(); y <= box.getMaxY(); y += 1) {
                for (int z = (int) box.getMinZ(); z <= box.getMaxZ(); z += 1) {
                    World world = pos1.getWorld();
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
                        chests.add(new LootChest(new Location(world, x, y, z)));
                    }
                }
            }
        }
    }

    private void enableChests() {
        for (LootChest chest : chests) {
            chest.enable();
        }
    }

    private void disableChests() {
        for (LootChest chest : chests) {
            chest.disable();
        }
    }

    private void enableItems() {
        for (Item item : items) {
            item.enable();
        }
    }

    private void disableItems() {
        for (Item item : items) {
            item.disable();
        }
    }

    private void initItems() {
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages("fun.kaituo.tagmansion.item") // 指定扫描的包
                .scan()) {

            Set<Class<? extends Item>> itemClasses = new HashSet<>(scanResult
                    .getSubclasses(Item.class.getName())
                    .loadClasses(Item.class));

            for (Class<? extends Item> itemClass : itemClasses) {
                Constructor<? extends Item> constructor = itemClass.getConstructor();
                Item item = constructor.newInstance();
                items.add(item);
            }
        } catch (Exception e) {
            game.getLogger().warning("Failed to register");
            throw new RuntimeException(e);
        }
    }

    private void removePlatform() {
        for (int x = waitBlock.getBlockX(); x <= waitBlock.getBlockX(); x++) {
            for (int z = waitBlock.getBlockZ(); z <= waitBlock.getBlockZ() + 2; z++) {
                waitBlock.getWorld().getBlockAt(x, waitBlock.getBlockY(), z).setType(Material.AIR);
            }
        }
    }

    private void restorePlatform() {
        for (int x = waitBlock.getBlockX(); x <= waitBlock.getBlockX(); x++) {
            for (int z = waitBlock.getBlockZ(); z <= waitBlock.getBlockZ() + 2; z++) {
                waitBlock.getWorld().getBlockAt(x, waitBlock.getBlockY(), z).setType(Material.NETHER_BRICKS);
            }
        }
    }

    private <T> T getRandomElement(Set<T> set) {
        if (set == null || set.isEmpty()) {
            throw new IllegalArgumentException("Set cannot be null or empty");
        }
        List<T> list = new ArrayList<>(set); // 转换为 List
        Random random = new Random();
        return list.get(random.nextInt(list.size())); // 随机索引访问
    }

    public ItemStack getRandomItem() {
        Item item = getRandomElement(items);
        return item.getItemStack();
    }

    @Override
    public void exit() {
        remainingTimeObjective.setDisplaySlot(null);
        for (UUID id : game.playerIds) {
            PlayerData data = game.idDataMap.get(id);
            if (data != null) {
                data.onDestroy();
            }
            Player p = Bukkit.getPlayer(id);
            assert p != null;
            game.getTagTeam().removePlayer(p);
        }
        game.idDataMap.clear();
        HandlerList.unregisterAll(this);
        disableItems();
        disableChests();
        for (int id : taskIds) {
            Bukkit.getScheduler().cancelTask(id);
        }
        restorePlatform();
        for (Entity entity : start.getWorld().getNearbyEntities(box)) {
            if (entity instanceof org.bukkit.entity.Item) {
                entity.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if (!game.playerIds.contains(p.getUniqueId())) {
            return;
        }
        PlayerData data = game.idDataMap.get(p.getUniqueId());
        if (data == null) {
            return;
        }
        for (Player player : TagMansion.inst().getPlayers()) {
            player.sendMessage("§f" + p.getName() + " §c 被逐出了箱庭！");
        }
        data.onDestroy();
        game.idDataMap.remove(p.getUniqueId());
        p.setGameMode(GameMode.SPECTATOR);
    }

    private void updateRemainingTime() {
        if (remainingTime > 0) {
            remainingTime -= 1;
        }
        remainingTimeScore.setScore((int) Math.ceil((double)remainingTime / 20));
    }

    public Set<Player> getPlayersAlive() {
        Set<Player> players = new HashSet<>();
        for (UUID id : game.playerIds) {
            PlayerData data = game.idDataMap.get(id);
            if (data != null) {
                players.add(data.getPlayer());
            }
        }
        return players;
    }

    public Set<Player> getHumans() {
        Set<Player> humans = new HashSet<>();
        for (Player p : getPlayersAlive()) {
            if (isCharacterHuman(game.idDataMap.get(p.getUniqueId()).getClass())) {
                humans.add(p);
            }
        }
        return humans;
    }

    public Set<Player> getHunters() {
        Set<Player> hunters = new HashSet<>();
        for (Player p : getPlayersAlive()) {
            if (!isCharacterHuman(game.idDataMap.get(p.getUniqueId()).getClass())) {
                hunters.add(p);
            }
        }
        return hunters;
    }

    private void end(Collection<Player> winners, String message) {
        isEnded = true;
        Set<Player> players = game.getPlayers();
        for (Player p : winners) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, -1, 4, false, false));
            taskIds.addAll(spawnFireworks(p, game));
        }
        for (Player p : players) {
            p.sendTitle(message, "", 10, 30, 20);
        }
        taskIds.add(Bukkit.getScheduler().runTaskLater(game, () -> {
            for (Player p : game.getPlayers()) {
                p.removePotionEffect(PotionEffectType.RESISTANCE);
                p.setGameMode(GameMode.ADVENTURE);
            }
            game.setState(WaitState.INST);
        }, 60).getTaskId());
    }

    private void checkForEnd() {
        Set<Player> humans = getHumans();
        Set<Player> hunters = getHunters();
        if (hunters.isEmpty()) {
            end(getHumans(), NO_HUNTER_MESSAGE);
            return;
        }
        if (humans.isEmpty()) {
            end(getHunters(), NO_HUMAN_MESSAGE);
            return;
        }
        if (remainingTime <= 0) {
            end(getHumans(), TIME_UP_MESSAGE);
        }
    }

    @Override
    public void tick() {
        updateRemainingTime();
        if (!isEnded) {
            checkForEnd();
        }
    }

    @Override
    public void addPlayer(Player p) {
        game.getTagTeam().addPlayer(p);
        PlayerData data = game.idDataMap.get(p.getUniqueId());
        if (data != null) {
            data.onRejoin();
        } else {
            p.setGameMode(GameMode.SPECTATOR);
            p.teleport(start);
        }
    }

    @Override
    public void removePlayer(Player p) {
        game.getTagTeam().removePlayer(p);
        PlayerData data = game.idDataMap.get(p.getUniqueId());
        if (data != null) {
            data.onQuit();
        } else {
            p.setGameMode(GameMode.ADVENTURE);
        }
    }

    @Override
    public void forceStop() {
        isEnded = true;
        for (Player p : game.getPlayers()) {
            p.removePotionEffect(PotionEffectType.RESISTANCE);
            p.setGameMode(GameMode.ADVENTURE);
        }
        game.setState(WaitState.INST);
    }
}
