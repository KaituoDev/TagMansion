package fun.kaituo.tagmansion.state;

import fun.kaituo.gameutils.game.GameState;
import fun.kaituo.tagmansion.TagMansion;
import fun.kaituo.tagmansion.util.ChooseCharacterSign;
import fun.kaituo.tagmansion.util.GameTimeSign;
import fun.kaituo.tagmansion.util.Human;
import fun.kaituo.tagmansion.util.Hunter;
import fun.kaituo.tagmansion.util.PlayerData;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static fun.kaituo.tagmansion.util.Misc.isCharacterHuman;

public class WaitState implements GameState, Listener {
    public static final WaitState INST = new WaitState();
    private WaitState() {}

    private TagMansion game;
    private final Set<ChooseCharacterSign> signs = new HashSet<>();
    private GameTimeSign gameTimeSign;
    private ItemStack menu;
    private Location startButtonLoc;
    private Objective characterCountObjective;

    public int getGameTimeMinutes() {
        return gameTimeSign.getGameTimeMinutes();
    }

    public void init() {
        game = TagMansion.inst();
        menu = game.getItem("menu");
        startButtonLoc = game.getLoc("startButton");
        initSigns();
        characterCountObjective = game.getTagBoard().registerNewObjective("characterCount", Criteria.DUMMY, "当前角色数量");
    }

    private void initSigns() {
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages("fun.kaituo.tagmansion.character") // 指定扫描的包
                .scan()) {

            Set<Class<? extends PlayerData>> characterClasses = new HashSet<>(scanResult
                    .getSubclasses(PlayerData.class.getName()) // 获取子类
                    .loadClasses(PlayerData.class))
                    .stream()
                    .filter(clazz -> !clazz.equals(Human.class) && !clazz.equals(Hunter.class))
                    .collect(Collectors.toSet());

            for (Class<? extends PlayerData> characterClass : characterClasses) {
                ChooseCharacterSign sign = new ChooseCharacterSign(game, game.getLoc(characterClass.getSimpleName()), characterClass);
                signs.add(sign);
            }
        } catch (Exception e) {
            game.getLogger().warning("Failed to scan for character classes");
            throw new RuntimeException(e);
        }

        gameTimeSign = new GameTimeSign(game, game.getLoc("gameTime"));
    }

    @EventHandler
    public void onPlayerClickStartButton(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        Block b = e.getClickedBlock();
        if (!b.getLocation().equals(startButtonLoc)) {
            return;
        }
        if (getHumanCount() < 1 || getHunterCount() < 1) {
            e.getPlayer().sendMessage("§c至少需要一个鬼和一个人类才能开始游戏");
            return;
        }
        game.setState(ReadyState.INST);
    }

    private int getHumanCount() {
        int humanCount = 0;
        for (UUID id : game.playerIds) {
            Class<? extends PlayerData> characterChoiceClass = game.playerCharacterChoices.get(id);
            if (isCharacterHuman(characterChoiceClass)) {
                humanCount += 1;
            }
        }
        return humanCount;
    }

    private int getHunterCount() {
        int hunterCount = 0;
        for (UUID id : game.playerIds) {
            Class<? extends PlayerData> characterChoiceClass = game.playerCharacterChoices.get(id);
            if (!isCharacterHuman(characterChoiceClass)) {
                hunterCount += 1;
            }
        }
        return hunterCount;
    }

    @Override
    public void enter() {
        Bukkit.getPluginManager().registerEvents(this, game);
        Bukkit.getPluginManager().registerEvents(gameTimeSign, game);
        for (ChooseCharacterSign sign : signs) {
            Bukkit.getPluginManager().registerEvents(sign, game);
        }
        characterCountObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (Player p : game.getPlayers()) {
            addPlayer(p);
        }
    }

    @Override
    public void exit() {
        HandlerList.unregisterAll(this);
        HandlerList.unregisterAll(gameTimeSign);
        for (ChooseCharacterSign sign: signs) {
            HandlerList.unregisterAll(sign);
        }
        characterCountObjective.setDisplaySlot(null);
        for (Player p : game.getPlayers()) {
            removePlayer(p);
        }
    }

    private void updateCharacterCount() {
        Score humanCount = characterCountObjective.getScore("§a人类数量");
        humanCount.setScore(getHumanCount());
        Score hunterCount = characterCountObjective.getScore("§c鬼数量");
        hunterCount.setScore(getHunterCount());
    }

    @Override
    public void tick() {
        updateCharacterCount();
    }

    @Override
    public void addPlayer(Player p) {
        p.getInventory().addItem(menu);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, -1, 4, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, -1, 0, false, false));
        p.teleport(game.getLocation());
        // Update player name tag color to the team color
        for (ChooseCharacterSign sign : signs) {
            Class<? extends PlayerData> choiceClass = game.playerCharacterChoices.get(p.getUniqueId());
            if (sign.getCharacterClass().equals(choiceClass)) {
                sign.getTeam().addPlayer(p);
                break;
            }
        }
    }

    @Override
    public void removePlayer(Player p) {
        p.getInventory().clear();
        p.removePotionEffect(PotionEffectType.RESISTANCE);
        p.removePotionEffect(PotionEffectType.SATURATION);
        for (ChooseCharacterSign sign : signs) {
            Class<? extends PlayerData> choiceClass = game.playerCharacterChoices.get(p.getUniqueId());
            if (sign.getCharacterClass().equals(choiceClass)) {
                sign.getTeam().removePlayer(p);
                break;
            }
        }
    }

    @Override
    public void forceStop() {

    }
}
