package com.beangamecore;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.beangamecore.blocks.*;
import com.beangamecore.commands.*;
import com.beangamecore.data.Configuration;
import com.beangamecore.data.DataAPI;
import com.beangamecore.data.DatabaseManager;
import com.beangamecore.entities.seacreatures.GuardianPopsicle;
import com.beangamecore.entities.seacreatures.SeaCreatureRegistry;
import com.beangamecore.events.AsyncPlayerChat;
import com.beangamecore.events.BlockBreak;
import com.beangamecore.events.BlockPlace;
import com.beangamecore.events.ChunkLoad;
import com.beangamecore.events.ChunkUnload;
import com.beangamecore.events.DropItem;
import com.beangamecore.events.EntityDamage;
import com.beangamecore.events.EntityDamageByEntity;
import com.beangamecore.events.EntityDeath;
import com.beangamecore.events.EntityShootBow;
import com.beangamecore.events.FeedMob;
import com.beangamecore.events.FurnaceSmelt;
import com.beangamecore.events.InventoryClick;
import com.beangamecore.events.LivingEntityTarget;
import com.beangamecore.events.NoteBlockPlay;
import com.beangamecore.events.PlayerChangeSign;
import com.beangamecore.events.PlayerDeath;
import com.beangamecore.events.PlayerFish;
import com.beangamecore.events.PlayerInteract;
import com.beangamecore.events.PlayerItemConsume;
import com.beangamecore.events.PlayerItemHeld;
import com.beangamecore.events.PlayerMove;
import com.beangamecore.events.PlayerRespawn;
import com.beangamecore.events.PlayerToggleFlight;
import com.beangamecore.events.PlayerToggleSneak;
import com.beangamecore.events.ProjectileHandler;
import com.beangamecore.events.ServerLoad;
import com.beangamecore.events.Teleport;
import com.beangamecore.items.*;
import com.beangamecore.items.armorsets.acacia.*;
import com.beangamecore.items.armorsets.birch.*;
import com.beangamecore.items.armorsets.cherry.*;
import com.beangamecore.items.armorsets.darkoak.*;
import com.beangamecore.items.armorsets.jungle.*;
import com.beangamecore.items.armorsets.mangrove.*;
import com.beangamecore.items.armorsets.oak.*;
import com.beangamecore.items.armorsets.paleoak.*;
import com.beangamecore.items.armorsets.spruce.*;
import com.beangamecore.items.fish.common.*;
import com.beangamecore.items.fish.epic.*;
import com.beangamecore.items.fish.legendary.*;
import com.beangamecore.items.fish.rare.*;
import com.beangamecore.items.fish.uncommon.*;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.material.*;
import com.beangamecore.particles.BeangameParticleManager;
import com.beangamecore.recipes.RecipeAPI;
import com.beangamecore.recipes.RecipeManager;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Booleans;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.GlobalCooldowns;
import com.beangamecore.util.Key;
import com.beangamecore.util.Longs;
import com.beangamecore.util.ResetItems;
import com.onarandombox.MultiverseCore.MultiverseCore;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;


public class Main extends JavaPlugin {
  public static final boolean CORE_ITEMS_ENABLED = true;
  private static Main plugin;
  public static DataAPI dataAPI;
  public static RecipeAPI recipeAPI;
  private static File cfgFile;
  private Configuration config;
  private FishingManager fishingManager;
  private SeaCreatureRegistry seaCreatureRegistry;
  private BeangameParticleManager particleManager;
  private static MultiverseCore multiverseCore;
  private LevelingSystem levelingSystem;
  private BeangameModes modes;
  
    // Get the plugin instance safely
    public static Main getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("Plugin instance is not initialized yet!");
        }
        return plugin;
    }
  
    public static Configuration getConfiguration() {
        return getPlugin().config;
    }
  
    public static Logger logger() {
        return plugin.getLogger();
    }
  
    public void reloadConfig() {
        config = dataAPI.loadData(cfgFile, Configuration.class);
        if (config == null) config = new Configuration();
    }

    public FishingManager getFishingManager() {
        return fishingManager;
    }

    public SeaCreatureRegistry getSeaCreatureRegistry() {
        return seaCreatureRegistry;
    }
  
    public BeangameParticleManager getParticleManager() {
        return particleManager;
    }
  
    public static MultiverseCore getMultiverseCore(){
      return multiverseCore;
    }

    public LevelingSystem getLevelingSystem() {
        return levelingSystem;
    }

    public BeangameModes getBeangameModes(){
        return modes;
    }

  @Override
  public void onEnable() {
      plugin = this;

      setupLevelingSystem();

      modes = new BeangameModes(this);

      getServer().getServicesManager().getKnownServices().forEach(service -> {
        logger().info(() -> "Registered Service: " + service.getName() + " | ClassLoader: " + service.getClass().getClassLoader());
      });
      logger().info(() -> "BukkitVoicechatService ClassLoader: " + BukkitVoicechatService.class.getClassLoader());

            RegisteredServiceProvider<BukkitVoicechatService> provider =
                getServer().getServicesManager().getRegistration(BukkitVoicechatService.class);
        
            if (provider != null) {
                BukkitVoicechatService service = provider.getProvider();
                if (service != null) {
                    logger().info("Found voicechat service after delay!");
                    service.registerPlugin(new VoicechatIntegration());
                } else {
                    logger().severe("Registered provider is null after delay!");
                }
            } else {
                logger().severe("Could not find voicechat service provider after delay!");
            }
            
      initializeKeys();
      initializeAPIs();
      loadConfiguration();
      
      if (CORE_ITEMS_ENABLED) {
          registerItems();
      }
      fishingManager = new FishingManager();
      seaCreatureRegistry = new SeaCreatureRegistry();

      registerSeaCreatures();

      new RecipeManager().init(this);
      particleManager = new BeangameParticleManager();
      multiverseCore = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

      registerCommands();
      registerCounters();
      registerEvents();

      File modelDir = new File(getDataFolder(), "models/vosk-model-small-en-us-0.15");
      try {
        Spellbook.initModel(modelDir);
      } catch (IOException e) {
        getLogger().severe("Failed to load Vosk model: " + e.getMessage());
        getLogger().info("Download from https://alphacephei.com/vosk/models and extract to " + modelDir.getParent());
      }

      logger().info("Beangame Enabled");
  }

  private void setupLevelingSystem(){
    levelingSystem = new LevelingSystem();

    getServer().getPluginManager().registerEvents(new Listener() {
        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            levelingSystem.loadPlayer(event.getPlayer());
        }
            
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            levelingSystem.unloadPlayer(event.getPlayer());
        }
    }, this);
        
    // Periodic save every 5 minutes
    Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
        for (Player player : Bukkit.getOnlinePlayers()) {
            levelingSystem.savePlayer(player);
        }
    }, 6000L, 6000L);
  }

  private void registerSeaCreatures() {
      SeaCreatureRegistry.registerSeaCreature(new GuardianPopsicle());
  }

  @Override
  public void onDisable() {
      dataAPI.saveData(cfgFile, config);
      for (Player player : Bukkit.getOnlinePlayers()) {
        levelingSystem.unloadPlayer(player);
      }
      DatabaseManager.shutdown();
      seaCreatureRegistry.clearRegistry();
      new ResetItems().resetAllItems();
      logger().info("Beangame Disabled");
  }

  private void initializeKeys() {
      Key.BLOCKS = new NamespacedKey(this, "blocks");
      Key.ID = new NamespacedKey(this, "key");
      Key.POSITION = new NamespacedKey(this, "position");
      Key.MATERIAL = new NamespacedKey(this, "type");
  }

  private void initializeAPIs() {
      dataAPI = new DataAPI(this);
      recipeAPI = new RecipeAPI(this);
  }

  private void loadConfiguration() {
      cfgFile = dataAPI.createFile(this, "items", new Configuration());
      config = dataAPI.loadData(cfgFile, Configuration.class);
      if(config == null){
          config = new Configuration();
      }
      if(config.update()){
          logger().info(() -> "Updated config to version " + Configuration.VERSION);
      } else {
          logger().info(() -> "Using config version " + Configuration.VERSION);
      }
  }

  private void registerItems() {
    // Create a list of all items to register
    List<BeangameItem> items = Arrays.asList(
        new AngelicCrown(),
        new AngelicShield(),
        new AnvilBow(),
        new ApollosBow(),
        new AppleCutters(),
        new ArchersQuiver(),
        new ARedBase(),
        new ArmorersBundle(),
        new ArmorersForge(),
        new AssassinsHitlist(),
        new Baleout(),
        new Bandage(),
        new BandageCannon(),
        new BastionBarracks(),
        new BatCape(),
        new Bean(),
        new BeanChronicles(),
        new BeefCity(),
        new BerserkersEssence(),
        new BiggestSnowballEver(),
        new BladeOfOlympus(),
        new Bleach(),
        new Blice(),
        new BloodSniffer(),
        new Blowbow(),
        new BlueShell(),
        new Boogiewoogie(),
        new Boomstick(),
        new BorderManipulator(),
        new BouncyShield(),
        new BrilliantBehemoth(),
        new Bungus(),
        new ButchersKnife(),
        new CarrionCall(),
        new CarrotSlides(),
        new CatKeyboard(),
        new CellPhone(),
        new CenterOfAttention(),
        new ChainedSword(),
        new CheeseTouch(),
        new ChorusBomb(),
        new Chronobreak(),
        new Cleanse(),
        new CloakOfTheCobra(),
        new CloakOfTheSpy(),
        new CoconutMilkCutlass(),
        new Coin(),
        new ColdWetSock(),
        new ConstructionHelmet(),
        new Contagion(),
        new ConverterPickaxe(),
        new CookedMeatPickaxe(),
        new CookieClicker(),
        new CosmicBrownie(),
        new CosmicEye(),
        new CosmicFury(),
        new CosmicIngot(),
        new Craig(),
        new CreepingDownfall(),
        new CrossNecklace(),
        new CrownOfTheCosmos(),
        new CrownOfTheGreedyKing(),
        new CrystalHoe(),
        new Dash(),
        new DayForTheErased(),
        new DealSealer(),
        new Depthcaller(),
        new Disoriedge(),
        new Donkinator(),
        new Drit(),
        new DropTheCarts(),
        new Drownmet(),
        new DrunicEdge(),
        new DuelistsDance(),
        new DweamSword(),
        new EarthPiercer(),
        new Egocentrism(),
        new EmotionalSupportAnimal(),
        new Encouragement(),
        new EnergySword(),
        new EquilibriumAmulet(),
        new ExplosiveBow(),
        new ExplosiveLauncher(),
        new ExplosivePickaxe(),
        new ExternalSight(),
        new FangFlare(),
        new FateFumbler(),
        new Feast(),
        new FifteenShrimp(),
        new FireExtinguisher(),
        new FireFlower(),
        new FireGauntlet(),
        new FLDSMDFR(),
        new FrostiFist(),
        new GenreCheck(),
        new GhastlyStaff(),
        new GhostBridge(),
        new GhostBridgeGenerator(),
        new GhoulbindCharm(),
        new GolemsGift(),
        new GracefulWaders(),
        new Grapple(),
        new GunkSword(),
        new HasteConverter(),
        new HeartOfIron(),
        new Heatseeker(),
        new HogRider(),
        new Holewand(),
        new Hook(),
        new HulaHoopingPants(),
        new IcicleBow(),
        new IdentitySeparationCore(),
        new Ignite(),
        new IHadACake(),
        new IllagerWannabe(),
        new IllusionistsInstigator(),
        new Immobilizer(),
        new Infinicake(),
        new Infinicobble(),
        new Infinimilk(),
        new Infinisteak(),
        new InstrumentsOfTheEnchanter(),
        new ItemMagnet(),
        new ItsJeff(),
        new Jailbreak(),
        new JunkRift(),
        new Kneecapper(),
        new KnockupGlove(),
        new LagConjurer(),
        new LaunchPad(),
        new LeapingSword(),
        new LoafLaunderer(),
        new LetItGrow(),
        new LuckbringersRemedy(),
        new LuckyHorseShoe(),
        new Lunchly(),
        new Magnemite(),
        new MasterWusStaff(),
        new MeatPickaxe(),
        new MedicsBeam(),
        new MedievalVaccine(),
        new MelonAxe(),
        new Microphone(),
        new MinersDream(),
        new Mirror(),
        new MobExterminator(),
        new MoltenPickaxe(),
        new MrMunchsMask(),
        new Multipants(),
        new Multisword(),
        new MysticBloom(),
        new NecroticScythe(),
        new NowThatsHeadCannon(),
        new ObsidianSkull(),
        new OceanOmnipotenceOrb(),
        new OhMyPants(),
        new Oobmab(),
        new OozingAegis(),
        new PanicNecklace(),
        new ParachutePants(),
        new PearlComposer(),
        new PhantomPiercer(),
        new Pizza(),
        new Plaxe(),
        new PortableCarrotFarm(),
        new PortableChickenFarm(),
        new PortableHoglinFarm(),
        new PortableIronGolemFarm(),
        new PortableSkeletonFarm(),
        new PortableZombiePiglinFarm(),
        new PortalMaker(),
        new Prickleplate(),
        new Protato(),
        new PufferfishPillar(),
        new PulsatingAxe(),
        new RavagerRocket(),
        new Rebirth(),
        new Recall(),
        new Redbull(),
        new Revive(),
        new ReviveWithInventory(),
        new RunningShoes(),
        new Sandsplitter(),
        new SayCheese(),
        new ScarletShardstorm(),
        new SculkstepCloak(),
        new SealOfTheSchizophrenic(),
        new SentientBeehive(),
        new SeraphimsDecree(),
        new ShadowBomb(),
        new ShrapnelShirt(),
        new SilenceOfTheLambs(),
        new SippingStraw(),
        new SlotEnforcer(),
        new Soul(),
        new SpawnCore(),
        new SpearOfAres(),
        new SpectralRod(),
        new SpeedEnrichment(),
        new Spellbook(),
        new SpoonsChance(),
        new SpringBean(),
        new StabilityCore(),
        new StackOStallions(),
        new StaffOfHunger(),
        new StickyBow(),
        new StickySword(),
        new StopSign(),
        new Stopwatch(),
        new StructureSpawner(),
        new SuctionCupShoes(),
        new SuicidalSpider(),
        new SuicideVest(),
        new SuperStar(),
        new Swapper(),
        new TalismanOfJumbledFates(),
        new Telemole(),
        new TennisBallItem(),
        new TestamentToTheDragon(),
        new Titanbone(),
        new TNFreeze(),
        new TntChestplate(),
        new TNTimer(),
        new Tomatior(),
        new TomeOfBlastsight(),
        new TomeOfRegeneration(),
        new TornadoTosser(),
        new TrappersCapital(),
        new Treat(),
        new Treecapitator(),
        new TrickPearl(),
        new Tromboner(),
        new TungstenBoots(),
        new UltimateGamble(),
        new VampireFang(),
        new VerdantSurge(),
        new VoiceSwappingStaff(),
        new VoidTracer(),
        new WallBreakers(),
        new WardenLauncher(),
        new WardensRage(),
        new WarpingTrader(),
        new WaWaWoodArmor(),
        new WebSlasher(),
        new WheatWizardsWand(),
        new Whisperfang(),
        new WitchsBrew(),
        new WitherScepter(),
        new WithersGift(),
        new XrayHelmet(),
        new ZiplockBag(),
        new WalkieTalkie(),
        new WalkieTalkieKit(),

        // Wooden armor sets
        new OakHelmet(),
        new OakChestplate(),
        new OakLeggings(),
        new OakBoots(),
        new SpruceHelmet(),
        new SpruceChestplate(),
        new SpruceLeggings(),
        new SpruceBoots(),
        new BirchHelmet(),
        new BirchChestplate(),
        new BirchLeggings(),
        new BirchBoots(),
        new JungleHelmet(),
        new JungleChestplate(),
        new JungleLeggings(),
        new JungleBoots(),
        new AcaciaHelmet(),
        new AcaciaChestplate(),
        new AcaciaLeggings(),
        new AcaciaBoots(),
        new DarkOakHelmet(),
        new DarkOakChestplate(),
        new DarkOakLeggings(),
        new DarkOakBoots(),
        new MangroveHelmet(),
        new MangroveChestplate(),
        new MangroveLeggings(),
        new MangroveBoots(),
        new CherryHelmet(),
        new CherryChestplate(),
        new CherryLeggings(),
        new CherryBoots(),
        new PaleOakHelmet(),
        new PaleOakChestplate(),
        new PaleOakLeggings(),
        new PaleOakBoots(),

        // Fish
        new RawCod(),
        new CookedCod(),
        new RawSalmon(),
        new CookedSalmon(),
        new Pufferfish(),
        new RawBlueTang(),
        new CookedBlueTang(),
        new Creeperfish(),
        new RawNoodlefish(),
        new CookedNoodlefish(),
        new BubbleBass(),
        new Goldfish(),
        new Pretzelfish(),
        new RawZombiePiranha(),
        new CookedZombiePiranha(),
        new RawRainbowTrout(),
        new CookedRainbowTrout(),
        new RawFlyingFish(),
        new CookedFlyingFish(),
        new Jellyfish(),
        new TNTuna(),
        new ChickenOfTheSea(),
        new Mimefish(),
        new RawSirBubbles(),
        new CookedSirBubbles(),
        new RawAnglerfish(),
        new CookedAnglerfish(),
        new PhantomFish(),
        new Beanfish(),
        new RawCarbonatedFish(),
        new CookedCarbonatedFish(),
        new Doughfish(),
        new Loaffish(),
        new BasedPridefish(),
        new CookedPridefish(),
        new Swordfish(),
        new CookedSwordfish()
    );

    // Register all items in batches
    BeangameItemRegistry.register(items);
  }

  private void registerCounters() {
      Cooldowns.register("attack");
      Cooldowns.register("use_item");
      Cooldowns.register("slot_enforced");
      Cooldowns.register("immobilized");
      Cooldowns.register("silenced");
      Cooldowns.register("schizophrenic");
      Cooldowns.register("jumbling");
      Cooldowns.register("untargetable");
      Cooldowns.register("redacted");
      
      Cooldowns.register("fall_damage_immunity");
      Cooldowns.register("explosion_immunity");
      Cooldowns.register("suffocation_immunity");

      GlobalCooldowns.register("equilibriumamulet");
      GlobalCooldowns.register("lootswappingstaff");

      Booleans.register("cloakofthespy_active");
      Booleans.register("gracefulwaders_active");

      Longs.register("assassinshitlist_hits");
      Longs.register("spearofares_hits");
      Longs.register("trapperscapital_stacks");
      Longs.register("suffocation_stacks");
  }

  private void registerCommands() {
      // commands
      getCommand("bg").setExecutor(new GiveCommand());
      getCommand("bg").setTabCompleter(new GiveTabCompleter());
      getCommand("bgitemlist").setExecutor(new BeangameItemlist());

      getCommand("bgdeathspectate").setExecutor(new DeathSpectateCommand());
      getCommand("bgdistribute").setExecutor(new BeangameDistribute());
      getCommand("bgdistributefood").setExecutor(new BeangameDistributeFood());
      getCommand("bgpvptoggle").setExecutor(new PvpToggleCommand());
      getCommand("bgstart").setExecutor(new BeangameStart());
      getCommand("bggrantrefund").setExecutor(new GrantRefund());
      getCommand("bgmute").setExecutor(new MuteCommand());
      getCommand("bgautoroll").setExecutor(new BeangameAutoroll());
      getCommand("bggamemodes").setExecutor(new GamemodesCommand());

      getCommand("bgactiveitemlist").setExecutor(new BeangameActiveItemlist());
      getCommand("bginvsee").setExecutor(new BeangameInvsee());
      getCommand("bgrefund").setExecutor(new Refund());

      getCommand("bgexec").setExecutor(new BeangameExecute());
      getCommand("bgexec").setTabCompleter(new BeangameExecute());
      
      getCommand("swapinventories").setExecutor(new SwapInventories());
  }

  private void registerEvents() {
      PluginManager pm = getServer().getPluginManager();
      pm.registerEvents(new BlockBreak(), this);
      pm.registerEvents(new BlockPlace(), this);
      pm.registerEvents(new EntityDamageByEntity(), this);
      pm.registerEvents(new EntityDamage(), this);
      pm.registerEvents(new EntityDeath(), this);
      pm.registerEvents(new EntityShootBow(), this);
      pm.registerEvents(new FurnaceSmelt(), this);
      pm.registerEvents(new InventoryClick(), this);
      pm.registerEvents(new LivingEntityTarget(), this);
      pm.registerEvents(new PlayerChangeSign(), this);
      pm.registerEvents(new PlayerDeath(), this);
      pm.registerEvents(new PlayerFish(), this);
      pm.registerEvents(new PlayerInteract(), this);
      pm.registerEvents(new PlayerItemConsume(), this);
      pm.registerEvents(new PlayerItemHeld(), this);
      pm.registerEvents(new PlayerMove(), this);
      pm.registerEvents(new PlayerRespawn(), this);
      pm.registerEvents(new PlayerToggleFlight(), this);
      pm.registerEvents(new PlayerToggleSneak(), this);
      pm.registerEvents(new ProjectileHandler(), this);
      pm.registerEvents(new ServerLoad(), this);
      pm.registerEvents(new ChunkLoad(), this);
      pm.registerEvents(new ChunkUnload(), this);
      pm.registerEvents(new AsyncPlayerChat(), this);
      pm.registerEvents(new Teleport(), this);
      pm.registerEvents(new DropItem(), this);
      pm.registerEvents(new NoteBlockPlay(), this);
      pm.registerEvents(new FeedMob(), this);
  }
}
