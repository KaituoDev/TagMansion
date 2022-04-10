package fun.kaituo;

import fun.kaituo.event.PlayerChangeGameEvent;
import fun.kaituo.event.PlayerEndGameEvent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static fun.kaituo.GameUtils.*;

public class Tag2Game extends Game implements Listener {
    private static final Tag2Game instance = new Tag2Game((Tag2) Bukkit.getPluginManager().getPlugin("Tag2"));
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    Scoreboard tag2 = Bukkit.getScoreboardManager().getNewScoreboard();
    List<Player> humans = new ArrayList<>();
    List<Player> devils = new ArrayList<>();
    long startTime;
    long gameTime;
    Team team;
    boolean running = false;
    int countDownSeconds = 10;
    ItemStack diamond = generateItemStack(Material.DIAMOND, "冰冻鬼5秒", 1);
    ItemStack red = generateItemStack(Material.RED_DYE, "回满血", 1);
    ItemStack heart = generateItemStack(Material.HEART_OF_THE_SEA, "隐身10秒", 1);
    ItemStack clock = generateItemStack(Material.CLOCK, "延时15秒", 1);
    ItemStack emerald = generateItemStack(Material.EMERALD, "人发光5秒", 1);
    ItemStack sugar = generateItemStack(Material.SUGAR, "加速", 1);
    Location[]
            locations = new Location[]{
            //new Location(world,-987,59,-20),
            //new Location(world,-987,59,1),
            //new Location(world,-987,58,24),
            //new Location(world,-1025,60,21),
            //new Location(world,-1025,60,15),
            //new Location(world,-1041,52,-6),
            //new Location(world,-1019,66,0),
            //new Location(world,-1002,51,22),
            //new Location(world,-1025,60,-20),
            //new Location(world,-1003,53,-16),
            //new Location(world,-1010,53,-16),
            //new Location(world,-1005,59,-20),
            new Location(world, -993, 52, 16),
            new Location(world, -1025, 52, 24),
            new Location(world, -1028, 47, -16),
            new Location(world, -987, 52, -16),
            new Location(world, -1007, 53, -16),
            new Location(world, -1028, 58, -3),
            new Location(world, -1041, 52, -6),
            new Location(world, -1014, 59, 18),
            new Location(world, -987, 59, 24),
            new Location(world, -987, 63, 1),
            new Location(world, -1004, 63, -19),
            new Location(world, -1025, 60, -20),
            new Location(world, -1010, 63, 0)
    };

    private Tag2Game(Tag2 plugin) {
        this.plugin = plugin;
        initializeGame(plugin, "Tag2", "§c洋馆", new Location(world, -1000, 202, 0), new BoundingBox(-1044, 45, -25, -983, 70, 27));
        initializeButtons(new Location(world, -1000, 203, 5), BlockFace.NORTH,
                new Location(world, -1005, 204, 0), BlockFace.EAST);
        players = Tag2.players;
        tag2.registerNewObjective("tag2", "dummy", "鬼抓人");
        tag2.getObjective("tag2").setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public static Tag2Game getInstance() {
        return instance;
    }

    private void b(String msg) {
        Bukkit.broadcastMessage(msg);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent pde) {
        if (!players.contains(pde.getEntity())) {
            return;
        }
        devils.remove(pde.getEntity());
        humans.remove(pde.getEntity());
        pde.getEntity().getInventory().clear();
        pde.getEntity().setGameMode(GameMode.SPECTATOR);
        for (Player p : players) {
            p.sendMessage("§f" + pde.getEntity().getName() + " §c的灵魂被收割了！");
        }
    }

    @EventHandler
    public void freezeGui(EntityDamageByEntityEvent edbee) {
        if (!(edbee.getDamager() instanceof Player)) {
            return;
        }
        if (!(edbee.getEntity() instanceof Player)) {
            return;
        }
        if (humans.contains(edbee.getDamager())) {
            edbee.setCancelled(true);
        }
        if (devils.contains(edbee.getDamager()) && humans.contains(edbee.getEntity())) {
            if (((Player) edbee.getEntity()).hasPotionEffect(PotionEffectType.GLOWING)) {
                ((Player) edbee.getEntity()).damage(23333);
            }
            Location l = edbee.getDamager().getLocation().clone();
            int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                edbee.getDamager().teleport(l);
            }, 1, 1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.getScheduler().cancelTask(id);
            }, 100);
            ((Player) edbee.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 254, false, false));
            ((Player) edbee.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 190, false, false));
            ((Player) edbee.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 254, false, false));
        }
    }

    @EventHandler
    public void cancelItemMovement(InventoryClickEvent ice) {
        if (!(ice.getWhoClicked() instanceof Player)) {
            return;
        }
        if (ice.getCurrentItem() == null) {
            return;
        }
        Player p = (Player) ice.getWhoClicked();
        if (humans.contains(p)) {
            if (ice.getCurrentItem().getType().equals(Material.CLOCK)
                    || ice.getCurrentItem().getType().equals(Material.EMERALD)) {
                ice.setCancelled(true);
            }
        } else if (devils.contains(p)) {
            if (ice.getCurrentItem().getType().equals(Material.DIAMOND)
                    || ice.getCurrentItem().getType().equals(Material.RED_DYE)
                    || ice.getCurrentItem().getType().equals(Material.HEART_OF_THE_SEA)) {
                ice.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void cancelPickup(PlayerPickupItemEvent ppie) {
        Player p = ppie.getPlayer();
        if (humans.contains(p)) {
            if (ppie.getItem().getItemStack().getType().equals(Material.CLOCK)
                    || ppie.getItem().getItemStack().getType().equals(Material.EMERALD)) {
                ppie.setCancelled(true);
            }
        } else if (devils.contains(p)) {
            if (ppie.getItem().getItemStack().getType().equals(Material.DIAMOND)
                    || ppie.getItem().getItemStack().getType().equals(Material.RED_DYE)
                    || ppie.getItem().getItemStack().getType().equals(Material.HEART_OF_THE_SEA)) {
                ppie.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent pie) {
        if (!pie.getAction().equals(Action.RIGHT_CLICK_AIR) && !pie.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }// 不是右键
        Player executor = pie.getPlayer();
        if (!(players.contains(executor))) {
            return;
        }//不在gzr2里
        if (pie.getClickedBlock() != null) {
            if (pie.getClickedBlock().getType().equals(Material.TRAPPED_CHEST)) {
                if (!pie.getPlayer().isSneaking()) {
                    if (humans.contains(executor)) {
                        executor.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false));
                    }
                    return;
                }
            }
        }
        if (pie.getItem() == null) {
            return;
        }//没有物品
        if (pie.getItem().getItemMeta() == null) {
            return;
        }//没有meta
        //这里开始添加内容

        switch (pie.getItem().getItemMeta().getDisplayName()) {
            case "冰冻鬼5秒":
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§b身边的鬼被冰冻5秒！");
                for (Player p : devils) {
                    if (p.getLocation().distance(executor.getLocation()) < 6) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 254, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 190, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 254, false, false));
                        Location l = p.getLocation().clone();
                        int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                            p.teleport(l);
                        }, 1, 1);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            Bukkit.getScheduler().cancelTask(id);
                        }, 100);
                    }
                }
                break;
            case "回满血":
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§c回复全部生命值！");
                executor.setHealth(executor.getMaxHealth());
                break;
            case "隐身10秒":
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§c隐身10秒！");
                executor.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, false, false));
                break;
            case "延时15秒":
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§c延时15秒！");
                startTime += 300;
                break;
            case "人发光5秒":
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                for (Player p : players) {
                    p.sendMessage("§b所有人发光5秒！");
                    if (humans.contains(p)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, false, false));
                    }
                }
                break;
            case "加速":
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§b获得加速！");
                if (humans.contains(executor)) {
                    executor.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
                } else {
                    executor.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
                }
                break;
        }
    }

    @EventHandler
    public void preventRegen(EntityRegainHealthEvent erhe) {
        if (!(erhe.getEntity() instanceof Player)) {
            return;
        }
        if (!(players.contains(erhe.getEntity()))) {
            return;
        }
        if (!((Player) erhe.getEntity()).getGameMode().equals(GameMode.ADVENTURE)) {
            return;
        }
        if (erhe.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) {
            erhe.setCancelled(true);
        } else if (erhe.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.EATING)) {
            erhe.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChangeGame(PlayerChangeGameEvent pcge) {
        players.remove(pcge.getPlayer());
        humans.remove(pcge.getPlayer());
        devils.remove(pcge.getPlayer());
    }


    @Override
    protected void initializeGameRunnable() {

        gameRunnable = () -> {
            gameTime = Tag2.gameTime;
            team = tag2.registerNewTeam("tag2");
            team.setNameTagVisibility(NameTagVisibility.NEVER);
            team.setCanSeeFriendlyInvisibles(false);
            team.setAllowFriendlyFire(true);
            for (Player p : getPlayersNearHub(50, 50, 50)) {
                if (scoreboard.getTeam("tag2R").hasPlayer(p)) {
                    devils.add(p);
                    players.add(p);
                    team.addPlayer(p);
                } else if (scoreboard.getTeam("tag2B").hasPlayer(p)) {
                    humans.add(p);
                    players.add(p);
                    team.addPlayer(p);
                }
            }
            if (players.size() < 2) {
                for (Player p : players) {
                    p.sendMessage("§c至少需要2人才能开始游戏！");
                }
                players.clear();
                humans.clear();
                team.unregister();

            } else if (humans.size() == 0) {
                for (Player p : players) {
                    p.sendMessage("§c至少需要1个人类才能开始游戏！");
                }
                players.clear();
                humans.clear();
                team.unregister();
            } else if (devils.size() == 0) {
                for (Player p : players) {
                    p.sendMessage("§c至少需要1个鬼才能开始游戏！");
                }
                players.clear();
                humans.clear();
                team.unregister();
            } else {
                startTime = getTime(world) + countDownSeconds * 20 + 400;
                running = true;
                for (int i = -1004; i <= -996; i++) {
                    world.getBlockAt(i, 199, 4).setType(Material.AIR);
                }
                removeStartButton();
                startCountdown(countDownSeconds);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Bukkit.getPluginManager().registerEvents(this, plugin);
                    for (Player p : humans) {
                        p.teleport(new Location(world, -990, 52.0625, 0));
                    }
                    for (Player p : players) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999, 0, false, false));
                        p.setScoreboard(tag2);
                    }

                }, 20 * countDownSeconds);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Player p : players) {
                        p.sendTitle("§a5", null, 2, 16, 2);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 1f);
                    }
                }, 20 * countDownSeconds + 300);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Player p : players) {
                        p.sendTitle("§a4", null, 2, 16, 2);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 1f);
                    }
                }, 20 * countDownSeconds + 320);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Player p : players) {
                        p.sendTitle("§a3", null, 2, 16, 2);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 1f);
                    }
                }, 20 * countDownSeconds + 340);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Player p : players) {
                        p.sendTitle("§a2", null, 2, 16, 2);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 1f);
                    }
                }, 20 * countDownSeconds + 360);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Player p : players) {
                        p.sendTitle("§a1", null, 2, 16, 2);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 1f);
                    }
                }, 20 * countDownSeconds + 380);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    placeSpectateButton();
                    for (Player p : devils) {
                        p.teleport(new Location(world, -990, 52.0625, 0));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 4, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 1, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false));
                        ItemStack skull = new ItemStack(Material.WITHER_SKELETON_SKULL);
                        skull.addEnchantment(Enchantment.BINDING_CURSE, 1);
                        p.getInventory().setItem(EquipmentSlot.HEAD, skull);
                        ItemStack chestPlate = new ItemStack(Material.NETHERITE_CHESTPLATE);
                        chestPlate.addEnchantment(Enchantment.BINDING_CURSE, 1);
                        ItemMeta chestPlateMeta = chestPlate.getItemMeta().clone();
                        chestPlateMeta.setUnbreakable(true);
                        chestPlate.setItemMeta(chestPlateMeta);
                        ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
                        leggings.addEnchantment(Enchantment.BINDING_CURSE, 1);
                        ItemMeta leggingsMeta = leggings.getItemMeta().clone();
                        leggingsMeta.setUnbreakable(true);
                        leggings.setItemMeta(leggingsMeta);
                        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
                        boots.addEnchantment(Enchantment.BINDING_CURSE, 1);
                        ItemMeta bootsMeta = boots.getItemMeta().clone();
                        bootsMeta.setUnbreakable(true);
                        boots.setItemMeta(bootsMeta);
                        p.getInventory().setItem(EquipmentSlot.CHEST, chestPlate);
                        p.getInventory().setItem(EquipmentSlot.LEGS, leggings);
                        p.getInventory().setItem(EquipmentSlot.FEET, boots);
                    }
                    for (Player p : players) {
                        p.sendTitle("§e游戏开始！", null, 2, 16, 2);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 2f);
                    }
                }, 20 * countDownSeconds + 400);

                taskIds.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                    for (Player p : players) {
                        if (devils.contains(p)) {
                            for (Player victim : players) {
                                if (humans.contains(victim)) {
                                    if (p.getLocation().distance(victim.getLocation()) < 10) {
                                        victim.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.BLOCKS, 2f, 0f);
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            victim.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.BLOCKS, 2f, 0f);
                                        }, 3);
                                    }
                                    if (p.getLocation().distance(victim.getLocation()) < 5) {
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            victim.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.BLOCKS, 2f, 0f);
                                        }, 10);
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            victim.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.BLOCKS, 2f, 0f);
                                        }, 13);
                                    }

                                }
                            }
                        }
                    }
                }, 20 * countDownSeconds + 400, 20));

                taskIds.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                    for (Player p : players) {
                        p.sendMessage("§a道具已刷新！");
                    }
                    for (Location loc : locations) {
                        double spawnChance = random.nextDouble();
                        if (spawnChance < 0.5) {
                            double spawnNo = random.nextDouble();
                            if (spawnNo < (1f / 35 * 10)) {
                                ((Chest) (world.getBlockAt(loc).getState())).getBlockInventory().addItem(diamond);
                            } else if (spawnNo < 1f / 35 * 15) {
                                ((Chest) (world.getBlockAt(loc).getState())).getBlockInventory().addItem(red);
                            } else if (spawnNo < 1f / 35 * 20) {
                                ((Chest) (world.getBlockAt(loc).getState())).getBlockInventory().addItem(clock);
                            } else if (spawnNo < 1f / 35 * 24) {
                                ((Chest) (world.getBlockAt(loc).getState())).getBlockInventory().addItem(emerald);
                            } else if (spawnNo < 1f / 35 * 32) {
                                ((Chest) (world.getBlockAt(loc).getState())).getBlockInventory().addItem(sugar);
                            } else {
                                ((Chest) (world.getBlockAt(loc).getState())).getBlockInventory().addItem(heart);
                            }
                        }
                    }
                }, 20 * countDownSeconds + 1000, 1200));


                taskIds.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                    long time = getTime(world);
                    if (time - startTime > gameTime) {
                        List<Player> humansCopy = new ArrayList<>(humans);
                        List<Player> playersCopy = new ArrayList<>(players);
                        for (Player p : humansCopy) {
                            spawnFireworks(p);
                        }
                        for (Player p : playersCopy) {
                            p.sendTitle("§e时间到，人类获胜！", null, 5, 50, 5);
                            p.resetPlayerWeather();
                            p.resetPlayerTime();
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                p.teleport(new Location(world, -1000, 202, 0));
                                Bukkit.getPluginManager().callEvent(new PlayerEndGameEvent(p, this));
                            }, 100);
                        }
                        endGame();
                        return;
                    }
                    if (humans.size() <= 0) {
                        List<Player> devilsCopy = new ArrayList<>(devils);
                        List<Player> playersCopy = new ArrayList<>(players);
                        for (Player p : devilsCopy) {
                            spawnFireworks(p);
                        }
                        for (Player p : playersCopy) {
                            p.sendTitle("§e无人幸存，鬼获胜！", null, 5, 50, 5);
                            p.resetPlayerWeather();
                            p.resetPlayerTime();
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                p.teleport(new Location(world, -1000, 202, 0));
                                Bukkit.getPluginManager().callEvent(new PlayerEndGameEvent(p, this));
                            }, 100);
                        }
                        endGame();
                        return;
                    }
                    if (devils.size() <= 0) {
                        List<Player> humansCopy = new ArrayList<>(humans);
                        List<Player> playersCopy = new ArrayList<>(players);
                        for (Player p : humansCopy) {
                            spawnFireworks(p);
                        }
                        for (Player p : playersCopy) {
                            p.sendTitle("§e鬼不复存在，人类获胜！", null, 5, 50, 5);
                            p.resetPlayerWeather();
                            p.resetPlayerTime();
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                p.teleport(new Location(world, -1000, 202, 0));
                                Bukkit.getPluginManager().callEvent(new PlayerEndGameEvent(p, this));
                            }, 100);
                        }
                        endGame();
                        return;
                    }
                    tag2.getObjective("tag2").getScore("剩余人数").setScore(humans.size());
                    tag2.getObjective("tag2").getScore("剩余时间").setScore((int) ((gameTime - (time - startTime)) / 20));
                }, 20 * countDownSeconds + 400, 1));
            }
        };
    }

    public void savePlayerQuitData(Player p) throws IOException {
        PlayerQuitData quitData = new PlayerQuitData(p, this, gameUUID);
        quitData.getData().put("team", whichGroup(p));
        setPlayerQuitData(p.getUniqueId(), quitData);
        players.remove(p);
        humans.remove(p);
        devils.remove(p);
    }

    private ItemStack generateItemStack(Material material, String name, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private void clearChests() {
        for (Location l : locations) {
            ((Chest) (world.getBlockAt(l).getState())).getBlockInventory().clear();
        }
    }

    private List<Player> whichGroup(Player p) {
        if (humans.contains(p)) {
            return humans;
        } else if (devils.contains(p)) {
            return devils;
        } else {
            return null;
        }
    }

    public void rejoin(Player p) {
        if (!running) {
            p.sendMessage("§c游戏已经结束！");
            return;
        }
        if (!getPlayerQuitData(p.getUniqueId()).getGameUUID().equals(gameUUID)) {
            p.sendMessage("§c游戏已经结束！");
            return;
        }
        PlayerQuitData pqd = getPlayerQuitData(p.getUniqueId());
        pqd.restoreBasicData(p);
        players.add(p);
        team.addPlayer(p);
        p.setScoreboard(tag2);
        if (pqd.getData().get("team") != null) {
            ((List<Player>) pqd.getData().get("team")).add(p);
        }
        setPlayerQuitData(p.getUniqueId(), null);
    }

    private void endGame() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (int i = -1004; i <= -996; i++) {
                world.getBlockAt(i, 199, 4).setType(Material.REDSTONE_BLOCK);
            }
            for (Entity e : world.getNearbyEntities(new Location(world, -1000, 128, 0), 200, 200, 200)) {
                if (e instanceof Item) {
                    e.remove();
                }
            }
            clearChests();
            removeSpectateButton();
            placeStartButton();
            HandlerList.unregisterAll(this);
            tag2.getObjective("tag2").getScore("剩余人数").setScore(0);
            tag2.getObjective("tag2").getScore("剩余时间").setScore(0);
        }, 100);
        players.clear();
        humans.clear();
        devils.clear();
        team.unregister();
        running = false;
        gameUUID = UUID.randomUUID();
        cancelGameTasks();
    }
}
