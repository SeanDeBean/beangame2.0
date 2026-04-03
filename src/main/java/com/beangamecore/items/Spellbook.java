package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.entities.livingtree.TreeComponentFactory;
import com.beangamecore.entities.livingtree.TreeGenerationConfig;
import com.beangamecore.entities.livingtree.TreeGenerator;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.general.BG30sTickingI;
import com.beangamecore.items.type.general.BGResetableI;
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import com.beangamecore.items.type.voicechat.BGVCMicPacket;
import com.beangamecore.items.type.voicechat.BGVoicechat;
import com.beangamecore.util.BlockCategories;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.vosk.Model;
import org.vosk.Recognizer;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Spellbook extends BeangameItem implements BGVoicechat, BGVCMicPacket, BGRClickableI, BGMPTalismanI, BG30sTickingI, BGResetableI {
    
    @Override
    public void resetItem(){
        tick();
    }

    private static Model voskModel;
    private final Map<UUID, OpusDecoder> decoders = new ConcurrentHashMap<>();
    private final Map<UUID, Recognizer> recognizers = new ConcurrentHashMap<>();
    private final Map<UUID, StringBuilder> partialResults = new ConcurrentHashMap<>();

    private static final Map<String, SpellType> SPELL_KEYWORDS = new HashMap<>();

    private static final long RECOGNIZER_TIMEOUT_MS = 60000; // 1 minute idle
    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();

    @Override
    public void tick() {
        long cutoff = System.currentTimeMillis() - RECOGNIZER_TIMEOUT_MS;
        lastActivity.entrySet().removeIf(e -> {
            if (e.getValue() < cutoff) {
                cleanup(e.getKey());
                return true;
            }
            return false;
        });
    }

    static {
        addKeywords(SpellType.SELF_DESTRUCT, "destruct", "explode", "detonate");
        addKeywords(SpellType.THUNDERBOLT, "thunderbolt", "lightning", "storm");
        addKeywords(SpellType.SIX_SEVEN, "six seven");
        addKeywords(SpellType.FIREBALL, "fireball", "flame", "blaze");
        addKeywords(SpellType.ICE_SHARDS, "shards", "frost", "freeze");
        addKeywords(SpellType.LET_IT_GROW, "grow", "tree");
        addKeywords(SpellType.BLINK, "blink", "teleport", "dash", "warp");
        addKeywords(SpellType.HEAL, "heal", "cure", "mend");
        addKeywords(SpellType.DRAGON, "dragon", "beast", "breath");
        addKeywords(SpellType.BLACK_HOLE, "void", "singularity", "hole");
        addKeywords(SpellType.PUSH, "push", "shove", "repel");
        addKeywords(SpellType.SHIELD, "shield", "protect", "barrier");
        addKeywords(SpellType.MACE_ATTACK, "mace", "smash", "crush");
        addKeywords(SpellType.LEAP, "leap", "jump", "launch");
        addKeywords(SpellType.SCHIZOPHRENIA, "schizophrenia", "madness", "chaos");
        addKeywords(SpellType.WALL, "wall", "barricade", "brick");
        addKeywords(SpellType.STEED, "steed", "horse", "mount", "mt");
        addKeywords(SpellType.CHICKENS, "chickens", "transform", "poultry");
        addKeywords(SpellType.DISAPPEAR, "disappear", "vanish", "invisible");
        addKeywords(SpellType.LUCK, "luck", "fortunate", "blessing");
        addKeywords(SpellType.DAMAGE_REDUCTION, "resistance", "armor");
        addKeywords(SpellType.LIGHT, "light", "illuminate", "bright");
        addKeywords(SpellType.BLOOD, "blood", "drain", "sacrifice");
        addKeywords(SpellType.GRASS, "grass", "overgrow", "nature");
        addKeywords(SpellType.STRENGTH, "strength", "power", "berserk");
        addKeywords(SpellType.BOOK, "book", "tome", "enchant");
        addKeywords(SpellType.THORN_LASH, "lash", "thorns", "vine");
        addKeywords(SpellType.DARKNESS, "darkness", "dark", "shadow");
    }
    
    private static void addKeywords(SpellType spell, String... keywords) {
        for (String keyword : keywords) {
            SPELL_KEYWORDS.put(keyword.toLowerCase(), spell);
        }
    }

    enum SpellType {
        // Combat/Destruction
        SELF_DESTRUCT("§c§lSelf Destruct§r", "§c", Color.fromRGB(255, 0, 0), 
                      Particle.LAVA, "§7Massive explosion, 100 self damage"),
        THUNDERBOLT("§e§lThunderbolt§r", "§b", Color.fromRGB(255, 255, 0), 
                    Particle.ELECTRIC_SPARK, "§7Lightning strike at target"),
        SIX_SEVEN("§4§lSix Seven§r", "§4", Color.fromRGB(139, 0, 0), 
                  Particle.DAMAGE_INDICATOR, "§7Reckless power, 100 self damage"),
        FIREBALL("§6§lFireball§r", "§6", Color.fromRGB(255, 140, 0), 
                 Particle.FLAME, "§7Launch explosive fireball"),
        
        // Ice/Nature
        ICE_SHARDS("§b§lIce Shards§r", "§b", Color.fromRGB(173, 216, 230), 
                   Particle.SNOWFLAKE, "§7Fire freezing shards"),
        LET_IT_GROW("§2§lLet It Grow§r", "§2", Color.fromRGB(34, 139, 34), 
                    Particle.HAPPY_VILLAGER, "§7Grow tree at target"),
        
        // Utility/Movement
        BLINK("§5§lBlink§r", "§5", Color.fromRGB(138, 43, 226), 
              Particle.PORTAL, "§7Teleport 4.5 blocks forward"),
        LEAP("§a§lLeap§r", "§7", Color.fromRGB(50, 205, 50), 
             Particle.CLOUD, "§7Launch into the air"),
        
        // Support/Healing
        HEAL("§f§lHeal§r", "§c", Color.fromRGB(255, 192, 203), 
             Particle.HEART, "§7Restore 1 heart instantly"),
        LUCK("§e§lLuck§r", "§2", Color.fromRGB(255, 215, 0), 
             Particle.HAPPY_VILLAGER, "§7Grant luck for 16s"),
        LIGHT("§f§lLight§r", "§b", Color.fromRGB(255, 255, 224), 
              Particle.END_ROD, "§7Clear darkness/blindness nearby"),
        
        // Summoning/Transformation
        DRAGON("§d§lDragon§r", "§d", Color.fromRGB(220, 20, 60), 
               Particle.DRAGON_BREATH, "§7Unleash draconic power"),
        STEED("§e§lSteed§r", "§6", Color.fromRGB(222, 184, 135), 
              Particle.NOTE, "§7Summon horse for 30s"),
        CHICKENS("§e§lChickens§r", "§6", Color.fromRGB(255, 255, 0), 
                 Particle.ITEM_SLIME, "§7Transform mobs to chickens"),
        
        // Void/Gravity
        BLACK_HOLE("§5§lBlack Hole§r", "§5", Color.fromRGB(75, 0, 130), 
                   Particle.SQUID_INK, "§7Gravity well pulls enemies"),
        DARKNESS("§8§lDarkness§r", "§0", Color.fromRGB(25, 25, 25), 
                 Particle.SMOKE, "§7Blind nearby enemies"),
        
        // Force/Defense
        PUSH("§f§lPush§r", "§7", Color.fromRGB(240, 248, 255), 
             Particle.CLOUD, "§7Repel all nearby"),
        SHIELD("§6§lShield§r", "§8", Color.fromRGB(255, 215, 0), 
               Particle.TOTEM_OF_UNDYING, "§7Become shield for 5s"),
        MACE_ATTACK("§c§lMace Attack§r", "§9", Color.fromRGB(205, 92, 92), 
                    Particle.CRIT, "§7Become mace for 5s"),
        WALL("§8§lWall§r", "§4", Color.fromRGB(139, 69, 19), 
             Particle.DUST, "§7Build brick barrier"),
        
        // Status/Mental
        SCHIZOPHRENIA("§d§lSchizophrenia§r", "§d", Color.fromRGB(255, 105, 180), 
                      Particle.WITCH, "§7Induce madness nearby"),
        
        // Stealth/Utility
        DISAPPEAR("§7§lDisappear§r", "§7", Color.fromRGB(192, 192, 192), 
                  Particle.WITCH, "§7Invisibility for 8s"),
        DAMAGE_REDUCTION("§8§lDamage Reduction§r", "§8", Color.fromRGB(105, 105, 105), 
                         Particle.TOTEM_OF_UNDYING, "§7Resistance for 8s"),
        
        // Nature/Blood
        GRASS("§a§lGrass§r", "§a", Color.fromRGB(124, 252, 0), 
              Particle.DUST, "§7Overgrow nearby terrain"),
        THORN_LASH("§2§lThorn Lash§r", "§2", Color.fromRGB(0, 100, 0), 
                   Particle.DUST, "§7Poisonous vine whip"),
        
        // Sacrifice/Power
        BLOOD("§4§lBlood§r", "§4", Color.fromRGB(139, 0, 0), 
              Particle.DAMAGE_INDICATOR, "§7Life for power beam"),
        STRENGTH("§4§lStrength§r", "§4", Color.fromRGB(178, 34, 34), 
                 Particle.ANGRY_VILLAGER, "§7Self damage for strength"),
        
        // Knowledge
        BOOK("§5§lBook§r", "§5", Color.fromRGB(153, 50, 204), 
             Particle.ENCHANT, "§7Random enchanted book");
        
        final String displayName;
        final String chatColor;
        final Color particleColor;
        final Particle particle;
        final String description;
        
        SpellType(String displayName, String chatColor, Color particleColor, 
                  Particle particle, String description) {
            this.displayName = displayName;
            this.chatColor = chatColor;
            this.particleColor = particleColor;
            this.particle = particle;
            this.description = description;
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        Player player = event.getPlayer();

        if(!player.isSneaking()) return false;

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookmeta = (BookMeta) book.getItemMeta();

        String nl = "\n";
        bookmeta.setAuthor(ChatColor.GOLD + "SeanDeBean");
        bookmeta.setTitle(ChatColor.GOLD + "Beangame Spellbook");
        
        StringBuilder page1 = new StringBuilder();
        page1.append(ChatColor.BLACK).append(ChatColor.BOLD).append("  ✦ SPELLBOOK ✦").append(nl);
        page1.append(ChatColor.DARK_GRAY).append("Hold in main hand, speak to cast.").append(nl);
        page1.append(ChatColor.DARK_GRAY).append("Cooldown: " + getBaseCooldown() / 1000 + "s").append(nl).append(nl);
        page1.append(SpellType.SELF_DESTRUCT.chatColor).append("💥 Self Destruct").append(nl);
        page1.append(SpellType.THUNDERBOLT.chatColor).append("⚡ Thunderbolt").append(nl);
        page1.append(SpellType.FIREBALL.chatColor).append("🔥 Fireball").append(nl);
        page1.append(SpellType.ICE_SHARDS.chatColor).append("❄ Ice Shards").append(nl);
        page1.append(SpellType.BLACK_HOLE.chatColor).append("🌑 Black Hole").append(nl);
        page1.append(SpellType.DRAGON.chatColor).append("🐉 Dragon").append(nl);
        page1.append(SpellType.WALL.chatColor).append("🧱 Wall").append(nl);
        page1.append(SpellType.STEED.chatColor).append("🐴 Steed").append(nl);
        page1.append(SpellType.LUCK.chatColor).append("🍀 Luck");
        bookmeta.addPage(page1.toString());
        
        StringBuilder page2 = new StringBuilder();
        page2.append(SpellType.LET_IT_GROW.chatColor).append("🌳 Let It Grow").append(nl);
        page2.append(SpellType.GRASS.chatColor).append("🌿 Grass").append(nl);
        page2.append(SpellType.THORN_LASH.chatColor).append("🌵 Thorn Lash").append(nl);
        page2.append(SpellType.BLINK.chatColor).append("💨 Blink").append(nl);
        page2.append(SpellType.LEAP.chatColor).append("⬆ Leap").append(nl);
        page2.append(SpellType.CHICKENS.chatColor).append("🐔 Chickens").append(nl);
        page2.append(SpellType.SHIELD.chatColor).append("🛡 Shield").append(nl);
        page2.append(SpellType.HEAL.chatColor).append("❤ Heal").append(nl);
        page2.append(SpellType.MACE_ATTACK.chatColor).append("🔨 Mace Attack").append(nl);
        page2.append(SpellType.WALL.chatColor).append("🧱 Wall").append(nl);
        page2.append(SpellType.PUSH.chatColor).append("💨 Push/Shove").append(nl);
        page2.append(SpellType.DAMAGE_REDUCTION.chatColor).append("🛡 Damage Reduction").append(nl);
        page2.append(SpellType.LIGHT.chatColor).append("☀ Light").append(nl);
        page2.append(SpellType.DISAPPEAR.chatColor).append("👤 Disappear");
        bookmeta.addPage(page2.toString());
        
        StringBuilder page3 = new StringBuilder();
        page3.append(SpellType.BLOOD.chatColor).append("🩸 Blood").append(nl);
        page3.append(SpellType.STRENGTH.chatColor).append("💪 Strength").append(nl);
        page3.append(SpellType.BOOK.chatColor).append("📖 Book").append(nl);
        page3.append(SpellType.DARKNESS.chatColor).append("🌑 Darkness").append(nl);
        page3.append(SpellType.SCHIZOPHRENIA.chatColor).append("♪ Schizophrenia");
        bookmeta.addPage(page3.toString());
    
        
        book.setItemMeta(bookmeta);
        player.openBook(book);
        return true;
    }
    
    public static void initModel(java.io.File modelDir) throws IOException {
        if (!modelDir.exists()) {
            throw new IOException("Vosk model not found at: " + modelDir.getAbsolutePath());
        }
        voskModel = new Model(modelDir.getAbsolutePath());
        Bukkit.getLogger().info("[Spellbook] Vosk model loaded successfully");
    }
    
    @Override
    public void onMicrophonePacket(MicrophonePacketEvent event) {
        if (event.isCancelled()) return;
        
        Player player = (Player) event.getSenderConnection().getPlayer().getPlayer();
        if (player == null) return;
        UUID uuid = player.getUniqueId();

        if (Cooldowns.onCooldown("silenced", uuid) || player.getGameMode().equals(GameMode.SPECTATOR)) return;

        UUID swappedPartner = VoiceSwappingStaff.getSwappedPartner(uuid);
        if (swappedPartner != null) {
            Player spellbookHolder = Bukkit.getPlayer(swappedPartner);
            if (spellbookHolder != null) {
                ItemStack mainHand = spellbookHolder.getInventory().getItemInMainHand();
                if (mainHand != null && mainHand.getType() != Material.AIR 
                    && ItemNBT.hasBeanGameTag(mainHand) 
                    && ItemNBT.isBeanGame(mainHand, getKey())) {
                    
                    // Redirect: process voice as the spellbook holder instead
                    player = spellbookHolder;
                    uuid = swappedPartner;
                } else {
                    return;
                }
            }
        }

        
        org.bukkit.inventory.ItemStack mainHand = player.getInventory().getItemInMainHand();
    
        if (mainHand == null || mainHand.getType() == Material.AIR) return;
        if (!ItemNBT.hasBeanGameTag(mainHand) || !ItemNBT.isBeanGame(mainHand, getKey())) return;
        
        if (voskModel == null) {
            player.sendMessage(ChatColor.RED + "Spellbook error: Speech model not loaded");
            return;
        }
    
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return;
        }
        
        OpusDecoder decoder = decoders.computeIfAbsent(uuid, u -> event.getVoicechat().createDecoder());
        Recognizer recognizer = recognizers.computeIfAbsent(uuid, u -> {
            try {
                Recognizer rec = new Recognizer(voskModel, 16000.0f);
                rec.setMaxAlternatives(0);
                rec.setWords(false);

                String grammar = "[\"" + String.join("\",\"", SPELL_KEYWORDS.keySet()) + "\"]";
                rec.setGrammar(grammar);

                partialResults.put(u, new StringBuilder());
                return rec;
            } catch (IOException e) {
                Bukkit.getLogger().severe("[Spellbook] Failed to create recognizer: " + e.getMessage());
                return null;
            }
        });
        
        if (recognizer == null) return;
        
        byte[] opusData = event.getPacket().getOpusEncodedData();
        
        if (opusData == null || opusData.length == 0) {
            processFinalResult(player, uuid, recognizer);
            return;
        }
        
        short[] pcmData = decoder.decode(opusData);
        if (pcmData == null || pcmData.length == 0) return;
        
        short[] resampled = resample48to16(pcmData);
        byte[] pcmBytes = shortsToBytes(resampled);
        
        if (recognizer.acceptWaveForm(pcmBytes, pcmBytes.length)) {
            String result = recognizer.getResult();
            String text = extractText(result);
            if (!text.isEmpty()) {
                checkForSpellTrigger(player, text);
                partialResults.get(uuid).setLength(0);
            }
        } else {
            String partial = recognizer.getPartialResult();
            String partialText = extractText(partial);
            if (!partialText.isEmpty()) {
                StringBuilder builder = partialResults.get(uuid);
                if (builder != null && !builder.toString().contains(partialText)) {
                    builder.append(" ").append(partialText);
                }
            }
        }
    }

    private void processFinalResult(Player player, UUID uuid, Recognizer recognizer) {
        String finalResult = recognizer.getFinalResult();
        String text = extractText(finalResult);
        
        StringBuilder partial = partialResults.get(uuid);
        String fullMessage = (partial != null && partial.length() > 0) 
            ? partial.toString().trim() + " " + text 
            : text;
        
        if (!fullMessage.trim().isEmpty()) {
            // broadcastMessage(player, text); // remove this once testing is done
            checkForSpellTrigger(player, fullMessage);
        }
        
        // recognizer.close();
        // recognizers.remove(uuid);
        // partialResults.remove(uuid);
    }

    private void checkForSpellTrigger(Player player, String lowerSpeech) {
        
        for (Map.Entry<String, SpellType> entry : SPELL_KEYWORDS.entrySet()) {
            if (lowerSpeech.contains(entry.getKey())) {
                castSpell(player, entry.getValue());
                return;
            }
        }
    }

    private final Map<UUID, SpellType> lastSpellCast = new ConcurrentHashMap<>();

    private void castSpell(Player player, SpellType spell) {
        UUID uuid = player.getUniqueId();

        SpellType lastCast = lastSpellCast.get(uuid);
        if (lastCast == spell) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                TextComponent.fromLegacy("§c✦ Cannot cast " + spell.displayName + " §cback-to-back!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            return;
        }
        
        // Schedule everything on main server thread to avoid async chunk access
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
            if(onCooldown(uuid)) {
                sendCooldownMessage(player);
                return;
            }
            applyCooldown(uuid);

            lastSpellCast.put(uuid, spell);
            lastActivity.put(uuid, System.currentTimeMillis());
            
            spawnWardingCircle(player, spell);
            
            String castMsg = spell.chatColor + "✦ " + player.getName() + " casts " + spell.displayName + "!";
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(castMsg));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
            
            executeSpellEffect(player, spell);
        });
    }

    private void spawnWardingCircle(Player player, SpellType spell) {
        Location center = player.getLocation().clone().add(0, 0.1, 0);
        
        switch (spell) {
            case SELF_DESTRUCT:
                drawExplosiveStar(player, center, Color.fromRGB(255, 0, 0), Particle.LAVA);
                break;
            case THUNDERBOLT:
                drawLightningBolts(player, center, Color.fromRGB(255, 255, 0), Particle.ELECTRIC_SPARK);
                break;
            case SIX_SEVEN:
                drawSixSevenPattern(player, center, Color.fromRGB(139, 0, 0), Particle.DAMAGE_INDICATOR);
                break;
            case FIREBALL:
                drawFireRings(player, center, Color.fromRGB(255, 140, 0), Particle.FLAME);
                break;
            case ICE_SHARDS:
                drawIceCrystals(player, center, Color.fromRGB(173, 216, 230), Particle.SNOWFLAKE);
                break;
            case LET_IT_GROW:
                drawTreeOfLife(player, center, Color.fromRGB(34, 139, 34), Particle.HAPPY_VILLAGER);
                break;
            case BLINK:
                drawTeleportSwirl(player, center, Color.fromRGB(138, 43, 226), Particle.PORTAL);
                break;
            case HEAL:
                drawHealingCross(player, center, Color.fromRGB(255, 192, 203), Particle.HEART);
                break;
            case DRAGON:
                break;
            case BLACK_HOLE:
                drawSpiralVortex(player, center, Color.fromRGB(75, 0, 130), Particle.SQUID_INK);
                break;
            case PUSH:
                drawShockwaveRings(player, center, Color.fromRGB(240, 248, 255), Particle.CLOUD);
                break;
            case SHIELD:
                drawDomeShield(player, center, Color.fromRGB(255, 215, 0), Particle.TOTEM_OF_UNDYING);
                break;
            case MACE_ATTACK:
                drawSpikedMace(player, center, Color.fromRGB(205, 92, 92), Particle.CRIT);
                break;
            case LEAP:
                drawUpwardArrows(player, center, Color.fromRGB(50, 205, 50), Particle.CLOUD);
                break;
            case SCHIZOPHRENIA:
                drawChaosSpiral(player, center, Color.fromRGB(255, 105, 180), Particle.WITCH);
                break;
            case WALL:
                break;
            case STEED:
                drawHorseshoe(player, center, Color.fromRGB(222, 184, 135), Particle.NOTE);
                break;
            case CHICKENS:
                break;
            case DISAPPEAR:
                drawFadingRings(player, center, Color.fromRGB(192, 192, 192), Particle.WITCH);
                break;
            case LUCK:
                drawCloverPattern(player, center, Color.fromRGB(255, 215, 0), Particle.HAPPY_VILLAGER);
                break;
            case DAMAGE_REDUCTION:
                drawIronShell(player, center, Color.fromRGB(105, 105, 105), Particle.TOTEM_OF_UNDYING);
                break;
            case LIGHT:
                drawRadiantSun(player, center, Color.fromRGB(255, 255, 224), Particle.END_ROD);
                break;
            case BLOOD:
                drawBloodDroplets(player, center, Color.fromRGB(139, 0, 0), Particle.DAMAGE_INDICATOR);
                break;
            case GRASS:
                drawOvergrowth(player, center, Color.fromRGB(124, 252, 0), Particle.DUST);
                break;
            case STRENGTH:
                drawMuscleFlex(player, center, Color.fromRGB(178, 34, 34), Particle.ANGRY_VILLAGER);
                break;
            case BOOK:
                drawEnchantingGlyphs(player, center, Color.fromRGB(153, 50, 204), Particle.ENCHANT);
                break;
            case THORN_LASH:
                drawThornWhip(player, center, Color.fromRGB(0, 100, 0), Particle.DUST);
                break;
            case DARKNESS:
                drawEclipse(player, center, Color.fromRGB(25, 25, 25), Particle.SMOKE);
                break;
        }
    }
    
    // 1. EXPLOSIVE STAR - Self Destruct
    private void drawExplosiveStar(Player player, Location center, Color color, Particle particle) {
        double radius = 4.0;
        int points = 8;
        // Outer explosion points
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            for (double r = 0; r <= radius; r += 0.5) {
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                spawnColoredParticle(player, center.clone().add(x, 0, z), color, particle, 1.5f);
            }
        }
        // Inner collapsing circle
        for (int i = 0; i < 32; i++) {
            double angle = 2 * Math.PI * i / 32;
            double x = Math.cos(angle) * (radius * 0.3);
            double z = Math.sin(angle) * (radius * 0.3);
            spawnColoredParticle(player, center.clone().add(x, 0.2, z), Color.BLACK, Particle.SMOKE, 1.0f);
        }
        // Vertical eruption column
        for (double y = 0; y < 3; y += 0.3) {
            spawnColoredParticle(player, center.clone().add(0, y, 0), color, Particle.LAVA, 2.0f);
        }
    }
    
    // 2. LIGHTNING BOLTS - Thunderbolt
    private void drawLightningBolts(Player player, Location center, Color color, Particle particle) {
        // Three lightning bolts radiating outward
        for (int bolt = 0; bolt < 3; bolt++) {
            double baseAngle = 2 * Math.PI * bolt / 3;
            // Zigzag pattern
            for (double d = 0; d <= 4; d += 0.3) {
                double offsetX = (Math.random() - 0.5) * 0.5;
                double offsetZ = (Math.random() - 0.5) * 0.5;
                double px = Math.cos(baseAngle) * d + offsetX;
                double pz = Math.sin(baseAngle) * d + offsetZ;
                spawnColoredParticle(player, center.clone().add(px, 0, pz), color, particle, 1.0f);
                // Vertical sparks
                spawnColoredParticle(player, center.clone().add(px, 0.5 + Math.random(), pz), color, Particle.ELECTRIC_SPARK, 0.5f);
            }
        }
        // Central charge orb
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 1;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, 0.5, Math.sin(angle)*r), 
                Color.WHITE, Particle.ELECTRIC_SPARK, 1.0f);
        }
    }
    
    // 3. SIX-SEVEN PATTERN - Six Seven
    private void drawSixSevenPattern(Player player, Location center, Color color, Particle particle) {
        double radius = 4.0;
        // Number 6 shape
        for (double t = 0; t < 2 * Math.PI; t += 0.1) {
            double x = Math.cos(t) * 1.5 - 1.5;
            double z = Math.sin(t) * 1.5;
            if (t < Math.PI * 1.5) {
                spawnColoredParticle(player, center.clone().add(x, 0, z), color, particle, 1.0f);
            }
        }
        // Number 7 shape
        for (double i = 0; i < 3; i += 0.2) {
            spawnColoredParticle(player, center.clone().add(1.5 + i*0.5, 0, 1.5 - i), color, particle, 1.0f);
        }
        spawnColoredParticle(player, center.clone().add(1.5, 0, -1.5), color, particle, 1.0f);
        spawnColoredParticle(player, center.clone().add(3, 0, -1.5), color, particle, 1.0f);
        
        // Danger rings
        for (int i = 0; i < 16; i++) {
            double angle = 2 * Math.PI * i / 16;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            spawnColoredParticle(player, center.clone().add(x, 0, z), Color.MAROON, Particle.DAMAGE_INDICATOR, 1.0f);
        }
    }
    
    // 4. FIRE RINGS - Fireball
    private void drawFireRings(Player player, Location center, Color color, Particle particle) {
        // Three concentric rings with rotation
        for (int ring = 0; ring < 3; ring++) {
            double r = 1.5 + ring * 1.0;
            int particles = 20 + ring * 8;
            for (int i = 0; i < particles; i++) {
                double angle = 2 * Math.PI * i / particles + (ring * Math.PI / 3);
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                double y = Math.sin(angle * 3) * 0.3; // Wavy
                spawnColoredParticle(player, center.clone().add(x, y, z), color, particle, 1.5f);
            }
        }
        // Central fire column
        for (double y = 0; y < 2.5; y += 0.2) {
            double spread = (2.5 - y) * 0.3;
            for (int i = 0; i < 8; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double dist = Math.random() * spread;
                spawnColoredParticle(player, center.clone().add(Math.cos(angle)*dist, y, Math.sin(angle)*dist), 
                    Color.RED, Particle.FLAME, 1.0f);
            }
        }
    }
    
    // 5. ICE CRYSTALS - Ice Shards
    private void drawIceCrystals(Player player, Location center, Color color, Particle particle) {
        // Six-pointed snowflake
        for (int arm = 0; arm < 6; arm++) {
            double angle = Math.PI * arm / 3;
            for (double d = 0; d <= 4; d += 0.4) {
                double x = Math.cos(angle) * d;
                double z = Math.sin(angle) * d;
                spawnColoredParticle(player, center.clone().add(x, 0, z), color, particle, 1.2f);
                // Side branches
                if (d > 1 && d < 3.5) {
                    double branchAngle1 = angle + Math.PI/6;
                    double branchAngle2 = angle - Math.PI/6;
                    spawnColoredParticle(player, center.clone().add(x + Math.cos(branchAngle1)*0.8, 0, z + Math.sin(branchAngle1)*0.8), 
                        color, particle, 0.8f);
                    spawnColoredParticle(player, center.clone().add(x + Math.cos(branchAngle2)*0.8, 0, z + Math.sin(branchAngle2)*0.8), 
                        color, particle, 0.8f);
                }
            }
        }
        // Central crystal formation
        for (double y = 0; y < 1.5; y += 0.3) {
            spawnColoredParticle(player, center.clone().add(0, y, 0), Color.WHITE, Particle.SNOWFLAKE, 1.0f);
        }
    }
    
    // 6. TREE OF LIFE - Let It Grow
    private void drawTreeOfLife(Player player, Location center, Color color, Particle particle) {
        // Roots spreading outward
        for (int root = 0; root < 5; root++) {
            double angle = 2 * Math.PI * root / 5;
            for (double d = 0; d <= 3; d += 0.3) {
                double x = Math.cos(angle) * d;
                double z = Math.sin(angle) * d;
                double y = Math.sin(d * 0.5) * 0.3;
                spawnColoredParticle(player, center.clone().add(x, y, z), Color.fromRGB(101, 67, 33), Particle.DUST, 1.0f);
            }
        }
        // Trunk rising
        for (double y = 0; y < 2.5; y += 0.2) {
            spawnColoredParticle(player, center.clone().add(0, y, 0), color, particle, 1.5f);
            // Branches at top
            if (y > 1.5) {
                for (int i = 0; i < 4; i++) {
                    double angle = Math.PI/2 * i + y;
                    double x = Math.cos(angle) * (y - 1.5) * 0.8;
                    double z = Math.sin(angle) * (y - 1.5) * 0.8;
                    spawnColoredParticle(player, center.clone().add(x, y, z), Color.fromRGB(34, 139, 34), Particle.HAPPY_VILLAGER, 1.0f);
                }
            }
        }
        // Leaves canopy
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = 1 + Math.random() * 2;
            double y = 2 + Math.random() * 1.5;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, y, Math.sin(angle)*r), 
                Color.fromRGB(0, 255, 0), Particle.HAPPY_VILLAGER, 1.0f);
        }
    }
    
    // 7. TELEPORT SWIRL - Blink
    private void drawTeleportSwirl(Player player, Location center, Color color, Particle particle) {
        // Spiral vortex going upward
        double radius = 4.0;
        for (double y = 0; y < 3; y += 0.1) {
            double angle = y * 4; // Twist rate
            double r = radius * (1 - y/3); // Narrowing toward top
            double x = Math.cos(angle) * r;
            double z = Math.sin(angle) * r;
            spawnColoredParticle(player, center.clone().add(x, y, z), color, particle, 1.0f);
        }
        // Portal center
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 1.5;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, 0.1, Math.sin(angle)*r), 
                Color.BLACK, Particle.PORTAL, 1.0f);
        }
        // Destination marker (ahead of player)
        Location ahead = center.clone().add(player.getLocation().getDirection().multiply(4.5));
        for (int i = 0; i < 16; i++) {
            double angle = 2 * Math.PI * i / 16;
            spawnColoredParticle(player, ahead.clone().add(Math.cos(angle)*0.5, 0, Math.sin(angle)*0.5), 
                Color.PURPLE, Particle.PORTAL, 1.0f);
        }
    }
    
    // 8. HEALING CROSS - Heal
    private void drawHealingCross(Player player, Location center, Color color, Particle particle) {
        // Medical cross shape
        double armLength = 3;
        double armWidth = 1;
        // Vertical bar
        for (double y = -armLength; y <= armLength; y += 0.3) {
            for (double x = -armWidth/2; x <= armWidth/2; x += 0.3) {
                spawnColoredParticle(player, center.clone().add(x, 0, y), color, particle, 1.5f);
            }
        }
        // Horizontal bar
        for (double x = -armLength; x <= armLength; x += 0.3) {
            for (double y = -armWidth/2; y <= armWidth/2; y += 0.3) {
                spawnColoredParticle(player, center.clone().add(x, 0, y), color, particle, 1.5f);
            }
        }
        // Outer ring of purity
        for (int i = 0; i < 24; i++) {
            double angle = 2 * Math.PI * i / 24;
            double x = Math.cos(angle) * 4;
            double z = Math.sin(angle) * 4;
            spawnColoredParticle(player, center.clone().add(x, 0, z), Color.fromRGB(255, 255, 255), Particle.END_ROD, 0.8f);
        }
    }

    
    // 10. SPIRAL VORTEX - Black Hole
    private void drawSpiralVortex(Player player, Location center, Color color, Particle particle) {
        // Accretion disk
        for (double r = 0.5; r <= 4; r += 0.3) {
            int count = (int)(r * 8);
            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count + (4 - r); // Spiral twist
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                // Pull toward center visual
                double pull = (4 - r) / 4;
                spawnColoredParticle(player, center.clone().add(x * pull, 0, z * pull), color, particle, 1.5f);
            }
        }
        // Event horizon (center)
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 0.8;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, 0.1, Math.sin(angle)*r), 
                Color.BLACK, Particle.SQUID_INK, 2.0f);
        }
        // Vertical pull columns
        for (double y = -1; y < 2; y += 0.2) {
            for (int i = 0; i < 8; i++) {
                double angle = 2 * Math.PI * i / 8 + y * 2;
                double r = 2 - Math.abs(y);
                spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, y + 1, Math.sin(angle)*r), 
                    Color.PURPLE, Particle.PORTAL, 0.8f);
            }
        }
    }
    
    // 11. SHOCKWAVE RINGS - Push
    private void drawShockwaveRings(Player player, Location center, Color color, Particle particle) {
        // Expanding rings
        for (int ring = 0; ring < 4; ring++) {
            double r = 1 + ring * 1.0;
            int count = 20 + ring * 10;
            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count;
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                double y = Math.sin(angle * 4) * 0.2; // Slight wave
                spawnColoredParticle(player, center.clone().add(x, y, z), color, particle, 1.0f + ring * 0.3f);
            }
        }
        // Force arrows pointing outward
        for (int i = 0; i < 12; i++) {
            double angle = 2 * Math.PI * i / 12;
            for (double d = 1; d <= 4; d += 0.4) {
                double x = Math.cos(angle) * d;
                double z = Math.sin(angle) * d;
                spawnColoredParticle(player, center.clone().add(x, 0.2, z), Color.WHITE, Particle.CLOUD, 0.8f);
            }
        }
    }
    
    // 12. DOME SHIELD - Shield
    private void drawDomeShield(Player player, Location center, Color color, Particle particle) {
        double radius = 4.0;
        // Hemispherical dome
        for (double phi = 0; phi <= Math.PI/2; phi += 0.15) {
            int count = (int)(Math.sin(phi) * 20) + 5;
            for (int i = 0; i < count; i++) {
                double theta = 2 * Math.PI * i / count;
                double x = radius * Math.sin(phi) * Math.cos(theta);
                double y = radius * Math.cos(phi);
                double z = radius * Math.sin(phi) * Math.sin(theta);
                spawnColoredParticle(player, center.clone().add(x, y, z), color, particle, 1.5f);
            }
        }
        // Base ring
        for (int i = 0; i < 32; i++) {
            double angle = 2 * Math.PI * i / 32;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            spawnColoredParticle(player, center.clone().add(x, 0, z), Color.ORANGE, Particle.TOTEM_OF_UNDYING, 1.0f);
        }
        // Shield symbols
        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI * i / 6;
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            spawnColoredParticle(player, center.clone().add(x, 2, z), Color.YELLOW, Particle.ENCHANT, 1.0f);
        }
    }
    
    // 13. SPIKED MACE - Mace Attack
    private void drawSpikedMace(Player player, Location center, Color color, Particle particle) {
        // Central sphere (mace head)
        for (double phi = 0; phi <= Math.PI; phi += 0.3) {
            for (double theta = 0; theta <= 2*Math.PI; theta += 0.3) {
                double r = 1.5;
                double x = r * Math.sin(phi) * Math.cos(theta);
                double y = r * Math.cos(phi) + 1.5;
                double z = r * Math.sin(phi) * Math.sin(theta);
                spawnColoredParticle(player, center.clone().add(x, y, z), color, particle, 1.0f);
            }
        }
        // Spikes radiating outward
        for (int spike = 0; spike < 8; spike++) {
            double angle = 2 * Math.PI * spike / 8;
            for (double d = 1.5; d <= 3.5; d += 0.3) {
                double x = Math.cos(angle) * d;
                double z = Math.sin(angle) * d;
                double y = 1.5 + (d - 1.5) * 0.3;
                spawnColoredParticle(player, center.clone().add(x, y, z), Color.fromRGB(139, 69, 19), Particle.CRIT, 1.2f);
            }
        }
        // Handle going down
        for (double y = 0; y < 1.5; y += 0.2) {
            spawnColoredParticle(player, center.clone().add(0, y, 0), Color.fromRGB(101, 67, 33), Particle.DUST, 1.0f);
        }
    }
    
    // 14. UPWARD ARROWS - Leap
    private void drawUpwardArrows(Player player, Location center, Color color, Particle particle) {
        // Three large arrows pointing up
        for (int arrow = 0; arrow < 3; arrow++) {
            double offsetX = (arrow - 1) * 1.5;
            // Arrow shaft
            for (double y = 0; y < 3; y += 0.2) {
                spawnColoredParticle(player, center.clone().add(offsetX, y, 0), color, particle, 1.2f);
                spawnColoredParticle(player, center.clone().add(offsetX + 0.3, y, 0), color, particle, 1.0f);
                spawnColoredParticle(player, center.clone().add(offsetX - 0.3, y, 0), color, particle, 1.0f);
            }
            // Arrow head
            for (double y = 3; y < 4; y += 0.2) {
                double spread = (4 - y) * 1.5;
                for (double x = -spread; x <= spread; x += 0.3) {
                    spawnColoredParticle(player, center.clone().add(offsetX + x, y, 0), color, particle, 1.5f);
                }
            }
        }
        // Launch pad circles
        for (int ring = 0; ring < 3; ring++) {
            double r = 1 + ring;
            for (int i = 0; i < 20; i++) {
                double angle = 2 * Math.PI * i / 20;
                spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, 0, Math.sin(angle)*r), 
                    Color.GREEN, Particle.HAPPY_VILLAGER, 0.8f);
            }
        }
    }
    
    // 15. CHAOS SPIRAL - Schizophrenia
    private void drawChaosSpiral(Player player, Location center, Color color, Particle particle) {
        // Multiple overlapping chaotic spirals
        Random random = new Random();
        for (int spiral = 0; spiral < 5; spiral++) {
            double offset = random.nextDouble() * 2 * Math.PI;
            for (double t = 0; t < 4 * Math.PI; t += 0.2) {
                double r = t * 0.3;
                double angle = t + offset;
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                double y = Math.sin(t * 3) * 0.5;
                Color chaoticColor = Color.fromRGB(
                    200 + random.nextInt(55),
                    50 + random.nextInt(100),
                    150 + random.nextInt(105)
                );
                spawnColoredParticle(player, center.clone().add(x, y, z), chaoticColor, particle, 1.0f);
            }
        }
        // Disorienting vertical lines
        for (int i = 0; i < 15; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double r = 1 + random.nextDouble() * 3;
            for (double y = 0; y < 3; y += 0.3) {
                spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, y, Math.sin(angle)*r), 
                    Color.FUCHSIA, Particle.WITCH, 0.8f);
            }
        }
    }
    
    // 17. HORSESHOE - Steed
    private void drawHorseshoe(Player player, Location center, Color color, Particle particle) {
        // U-shaped horseshoe
        for (double t = 0; t <= Math.PI; t += 0.1) {
            double x = Math.cos(t) * 3;
            double z = Math.sin(t) * 3 - 1;
            spawnColoredParticle(player, center.clone().add(x, 0, z), color, particle, 1.5f);
            // Thickness
            spawnColoredParticle(player, center.clone().add(x*0.9, 0, z*0.9), color, particle, 1.2f);
        }
        // Hoof prints around
        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI * i / 6;
            double x = Math.cos(angle) * 2.5;
            double z = Math.sin(angle) * 2.5;
            // Horseshoe print shape
            for (double hx = -0.3; hx <= 0.3; hx += 0.15) {
                for (double hz = -0.4; hz <= 0.4; hz += 0.15) {
                    spawnColoredParticle(player, center.clone().add(x + hx, 0, z + hz), 
                        Color.fromRGB(139, 69, 19), Particle.DUST, 0.8f);
                }
            }
        }
        // Musical notes floating up
        for (int i = 0; i < 8; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 2;
            for (double y = 0; y < 2; y += 0.4) {
                spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, y, Math.sin(angle)*r), 
                    Color.YELLOW, Particle.NOTE, 1.0f);
            }
        }
    }
    
    // 19. FADING RINGS - Disappear
    private void drawFadingRings(Player player, Location center, Color color, Particle particle) {
        // Concentric fading rings
        for (int ring = 0; ring < 5; ring++) {
            double r = 4 - ring * 0.7;
            int count = 16 + ring * 4;
            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count;
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                double alpha = 1.0 - (ring * 0.2);
                Color fadeColor = Color.fromRGB(
                    (int)(192 * alpha),
                    (int)(192 * alpha),
                    (int)(192 * alpha)
                );
                spawnColoredParticle(player, center.clone().add(x, ring * 0.1, z), fadeColor, particle, (float)(1.5 - ring * 0.2));
            }
        }
        // Transparency particles rising
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 2;
            double y = Math.random() * 3;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, y, Math.sin(angle)*r), 
                Color.GRAY, Particle.WITCH, 0.6f);
        }
    }
    
    // 20. CLOVER PATTERN - Luck
    private void drawCloverPattern(Player player, Location center, Color color, Particle particle) {
        // Four-leaf clover
        for (int leaf = 0; leaf < 4; leaf++) {
            double baseAngle = Math.PI/2 * leaf;
            for (double t = 0; t < Math.PI; t += 0.1) {
                double r = Math.sin(t) * 2;
                double angle = baseAngle + t * 0.5;
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                spawnColoredParticle(player, center.clone().add(x, 0, z), color, particle, 1.5f);
                spawnColoredParticle(player, center.clone().add(x, 0.1, z), Color.GREEN, Particle.HAPPY_VILLAGER, 1.2f);
            }
        }
        // Center stem
        for (double y = 0; y < 1; y += 0.1) {
            spawnColoredParticle(player, center.clone().add(0, y, 0), Color.fromRGB(34, 139, 34), Particle.DUST, 1.0f);
        }
        // Gold sparkles
        for (int i = 0; i < 25; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 3;
            double y = Math.random() * 1.5;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, y, Math.sin(angle)*r), 
                Color.YELLOW, Particle.HAPPY_VILLAGER, 1.0f);
        }
    }
    
    // 21. IRON SHELL - Damage Reduction
    private void drawIronShell(Player player, Location center, Color color, Particle particle) {
        // Interlocking plates
        for (int plate = 0; plate < 6; plate++) {
            double angle = Math.PI * plate / 3;
            for (double r = 1; r <= 4; r += 0.3) {
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                // Plate thickness
                for (double offset = -0.2; offset <= 0.2; offset += 0.1) {
                    spawnColoredParticle(player, center.clone().add(x + offset, 0, z), 
                        Color.fromRGB(105, 105, 105), Particle.DUST, 1.0f);
                }
            }
        }
        // Central iron core
        for (double y = 0; y < 2; y += 0.2) {
            for (int i = 0; i < 12; i++) {
                double angle = 2 * Math.PI * i / 12;
                double r = 0.8;
                spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, y, Math.sin(angle)*r), 
                    Color.fromRGB(169, 169, 169), Particle.TOTEM_OF_UNDYING, 1.2f);
            }
        }
        // Protective aura
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = 3 + Math.random();
            double y = Math.random() * 2;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, y, Math.sin(angle)*r), 
                Color.GRAY, Particle.ENCHANT, 0.8f);
        }
    }
    
    // 22. RADIANT SUN - Light
    private void drawRadiantSun(Player player, Location center, Color color, Particle particle) {
        // Sun center
        for (int i = 0; i < 40; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 1.5;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, 0.2, Math.sin(angle)*r), 
                Color.YELLOW, Particle.END_ROD, 1.5f);
        }
        // Radiating rays
        for (int ray = 0; ray < 12; ray++) {
            double angle = 2 * Math.PI * ray / 12;
            for (double d = 1.5; d <= 4; d += 0.2) {
                double x = Math.cos(angle) * d;
                double z = Math.sin(angle) * d;
                double y = Math.sin(d * 2) * 0.3;
                spawnColoredParticle(player, center.clone().add(x, y, z), color, particle, 1.2f);
                // Glow effect
                spawnColoredParticle(player, center.clone().add(x, y + 0.2, z), Color.WHITE, Particle.END_ROD, 0.8f);
            }
        }
        // Outer halo
        for (int i = 0; i < 32; i++) {
            double angle = 2 * Math.PI * i / 32;
            double x = Math.cos(angle) * 4;
            double z = Math.sin(angle) * 4;
            spawnColoredParticle(player, center.clone().add(x, 0, z), Color.fromRGB(255, 255, 224), Particle.END_ROD, 1.0f);
        }
    }
    
    // 23. BLOOD DROPLETS - Blood
    private void drawBloodDroplets(Player player, Location center, Color color, Particle particle) {
        // Blood splatter pattern
        Random random = new Random();
        for (int drop = 0; drop < 15; drop++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double r = random.nextDouble() * 3;
            double size = 0.5 + random.nextDouble();
            // Droplet shape
            for (double d = 0; d < size; d += 0.15) {
                double x = Math.cos(angle) * (r + d);
                double z = Math.sin(angle) * (r + d);
                double y = Math.sin(d * Math.PI) * 0.5;
                spawnColoredParticle(player, center.clone().add(x, y, z), color, particle, 1.5f);
            }
        }
        // Pool center
        for (int i = 0; i < 25; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 1.5;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, 0.1, Math.sin(angle)*r), 
                Color.fromRGB(100, 0, 0), Particle.DAMAGE_INDICATOR, 1.2f);
        }
        // Rising blood column (sacrifice)
        for (double y = 0; y < 2.5; y += 0.2) {
            spawnColoredParticle(player, center.clone().add(0, y, 0), color, Particle.WITCH, 1.0f);
        }
    }
    
    // 24. OVERGROWTH - Grass
    private void drawOvergrowth(Player player, Location center, Color color, Particle particle) {
        // Random grass blades
        Random random = new Random();
        for (int blade = 0; blade < 60; blade++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double r = random.nextDouble() * 4;
            double height = 0.5 + random.nextDouble() * 1.5;
            for (double y = 0; y < height; y += 0.2) {
                double lean = y * 0.1;
                spawnColoredParticle(player, center.clone().add(
                    Math.cos(angle)*(r + lean), y, Math.sin(angle)*(r + lean)), 
                    color, particle, 0.8f);
            }
        }
        // Flower spots
        for (int flower = 0; flower < 8; flower++) {
            double angle = 2 * Math.PI * flower / 8;
            double r = 2 + random.nextDouble();
            for (int i = 0; i < 5; i++) {
                double offsetAngle = random.nextDouble() * 2 * Math.PI;
                double offsetR = random.nextDouble() * 0.5;
                spawnColoredParticle(player, center.clone().add(
                    Math.cos(angle)*r + Math.cos(offsetAngle)*offsetR, 
                    0.3, 
                    Math.sin(angle)*r + Math.sin(offsetAngle)*offsetR), 
                    Color.fromRGB(255, 105, 180), Particle.HAPPY_VILLAGER, 1.0f);
            }
        }
        // Vine spiral
        for (double t = 0; t < 4 * Math.PI; t += 0.2) {
            double r = 1 + t * 0.2;
            double x = Math.cos(t) * r;
            double z = Math.sin(t) * r;
            double y = Math.sin(t * 2) * 0.3;
            spawnColoredParticle(player, center.clone().add(x, y, z), 
                Color.fromRGB(0, 100, 0), Particle.DUST, 0.8f);
        }
    }
    
    // 25. MUSCLE FLEX - Strength
    private void drawMuscleFlex(Player player, Location center, Color color, Particle particle) {
        // Bicep curl shape (two arcs)
        for (int side = 0; side < 2; side++) {
            double sideOffset = side == 0 ? -1.5 : 1.5;
            for (double t = 0; t < Math.PI; t += 0.1) {
                double x = sideOffset + Math.cos(t) * 1.2;
                double z = Math.sin(t) * 1.5;
                double y = Math.sin(t) * 0.5;
                spawnColoredParticle(player, center.clone().add(x, y, z), color, particle, 1.5f);
            }
        }
        // Flex lines (energy)
        for (int i = 0; i < 12; i++) {
            double angle = 2 * Math.PI * i / 12;
            for (double d = 2; d <= 4; d += 0.3) {
                double x = Math.cos(angle) * d;
                double z = Math.sin(angle) * d;
                spawnColoredParticle(player, center.clone().add(x, 0.2, z), 
                    Color.RED, Particle.ANGRY_VILLAGER, 1.0f);
            }
        }
        // Power aura
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = 1 + Math.random() * 2;
            double y = Math.random() * 2;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, y, Math.sin(angle)*r), 
                Color.ORANGE, Particle.FLAME, 0.8f);
        }
    }
    
    // 26. ENCHANTING GLYPHS - Book
    private void drawEnchantingGlyphs(Player player, Location center, Color color, Particle particle) {
        // Floating rune circles
        for (int rune = 0; rune < 4; rune++) {
            double y = rune * 0.6;
            double r = 2 + rune * 0.5;
            int symbols = 8 + rune * 2;
            for (int i = 0; i < symbols; i++) {
                double angle = 2 * Math.PI * i / symbols + (rune * Math.PI / 4);
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                spawnColoredParticle(player, center.clone().add(x, y, z), color, particle, 1.2f);
                // Magic sparkles
                if (i % 2 == 0) {
                    spawnColoredParticle(player, center.clone().add(x, y + 0.2, z), 
                        Color.PURPLE, Particle.ENCHANT, 0.8f);
                }
            }
        }
        // Book outline on ground
        for (double x = -1.5; x <= 1.5; x += 0.2) {
            for (double z = -2; z <= 2; z += 0.2) {
                if (Math.abs(x) > 1.3 || Math.abs(z) > 1.8) {
                    spawnColoredParticle(player, center.clone().add(x, 0, z), 
                        Color.fromRGB(139, 69, 19), Particle.DUST, 1.0f);
                }
            }
        }
        // Pages effect
        for (double y = 0; y < 1; y += 0.1) {
            spawnColoredParticle(player, center.clone().add(0, y, 0), Color.WHITE, Particle.ENCHANT, 0.6f);
        }
    }
    
    // 27. THORN WHIP - Thorn Lash
    private void drawThornWhip(Player player, Location center, Color color, Particle particle) {
        // Three whip lashes
        for (int whip = 0; whip < 3; whip++) {
            double baseAngle = 2 * Math.PI * whip / 3;
            for (double t = 0; t < 4; t += 0.2) {
                double wave = Math.sin(t * 3) * 0.5;
                double x = Math.cos(baseAngle + wave) * t;
                double z = Math.sin(baseAngle + wave) * t;
                double y = Math.abs(Math.sin(t)) * 0.5;
                spawnColoredParticle(player, center.clone().add(x, y, z), color, particle, 1.2f);
                // Thorns on whip
                if (t % 1 < 0.2) {
                    double thornAngle = baseAngle + wave + Math.PI/2;
                    spawnColoredParticle(player, center.clone().add(
                        x + Math.cos(thornAngle)*0.4, y, z + Math.sin(thornAngle)*0.4), 
                        Color.fromRGB(139, 0, 0), Particle.DAMAGE_INDICATOR, 0.8f);
                }
            }
        }
        // Poison cloud
        for (int i = 0; i < 15; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 3;
            double y = Math.random() * 1;
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, y, Math.sin(angle)*r), 
                Color.GREEN, Particle.WITCH, 0.8f);
        }
    }
    
    // 28. ECLIPSE - Darkness
    private void drawEclipse(Player player, Location center, Color color, Particle particle) {
        // Outer corona (fading light)
        for (int i = 0; i < 40; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = 3 + Math.random();
            spawnColoredParticle(player, center.clone().add(Math.cos(angle)*r, 0.1, Math.sin(angle)*r), 
                Color.GRAY, particle, 1.0f);
        }
        // Inner darkness (black circle)
        for (double x = -2; x <= 2; x += 0.2) {
            for (double z = -2; z <= 2; z += 0.2) {
                if (x*x + z*z <= 4) {
                    spawnColoredParticle(player, center.clone().add(x, 0, z), 
                        Color.BLACK, Particle.SQUID_INK, 1.5f);
                }
            }
        }
        // Shadow tendrils reaching out
        for (int tendril = 0; tendril < 8; tendril++) {
            double angle = 2 * Math.PI * tendril / 8;
            for (double d = 2; d <= 4; d += 0.2) {
                double wave = Math.sin(d * 3) * 0.3;
                double x = Math.cos(angle + wave) * d;
                double z = Math.sin(angle + wave) * d;
                spawnColoredParticle(player, center.clone().add(x, 0, z), color, particle, 1.0f);
            }
        }
    }

    // Helper method for colored particles
    private void spawnColoredParticle(Player player, Location loc, Color color, Particle particle, float size) {
        if (particle == Particle.DUST) {
            player.getWorld().spawnParticle(Particle.DUST, loc, 1, 
                new Particle.DustOptions(color, size));
        } else {
            player.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    private void executeSpellEffect(Player player, SpellType spell) {
        switch (spell) {
            case SELF_DESTRUCT -> effectSelfDestruct(player);
            case THUNDERBOLT -> effectThunderbolt(player);
            case SIX_SEVEN -> effectSixSeven(player);
            case FIREBALL -> effectFireball(player);
            case ICE_SHARDS -> effectIceShards(player);
            case LET_IT_GROW -> effectLetItGrow(player);
            case BLINK -> effectBlink(player);
            case HEAL -> effectHeal(player);
            case DRAGON -> effectDragon(player);
            case BLACK_HOLE -> effectBlackHole(player);
            case PUSH -> effectPush(player);
            case SHIELD -> effectShield(player);
            case MACE_ATTACK -> effectMaceAttack(player);
            case LEAP -> effectLeap(player);
            case SCHIZOPHRENIA -> effectSchizophrenia(player);
            case WALL -> effectWall(player);
            case STEED -> effectSteed(player);
            case CHICKENS -> effectChickens(player);
            case DISAPPEAR -> effectDisappear(player);
            case LUCK -> effectLuck(player);
            case DAMAGE_REDUCTION -> effectDamageReduction(player);
            case LIGHT -> effectLight(player);
            case BLOOD -> effectBlood(player);
            case GRASS -> effectGrass(player);
            case STRENGTH -> effectStrength(player);
            case BOOK -> effectBook(player);
            case THORN_LASH -> effectThornLash(player);
            case DARKNESS -> effectDarkness(player);
        }
    }
    
    private void effectSelfDestruct(Player player) {
        Location center = player.getLocation();
        World world = player.getWorld();
        
        // Visual/audio buildup
        world.playSound(center, Sound.ENTITY_TNT_PRIMED, 2.0f, 0.5f);
        
        // Warning particles - rapid pulsing red rings
        for (int i = 0; i < 5; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                double radius = 1.5 + tick * 0.8;
                int particles = 16 + tick * 4;
                for (int j = 0; j < particles; j++) {
                    double angle = 2 * Math.PI * j / particles;
                    Location particleLoc = center.clone().add(
                        Math.cos(angle) * radius, 
                        0.1 + (tick * 0.2), 
                        Math.sin(angle) * radius
                    );
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 
                        new Particle.DustOptions(Color.fromRGB(255, 50, 0), 1.5f));
                }
                world.playSound(center, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, 0.6f - (tick * 0.08f));
            }, i * 4L);
        }
        
        // Final flash warning
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            world.spawnParticle(Particle.FLASH, center.clone().add(0, 1, 0), 1, 0, 0, 0, 0);
            world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 2.0f);
        }, 18L);
        
        // The explosion
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            // Massive particle burst
            world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 3, 2, 2, 2, 0);
            world.spawnParticle(Particle.LAVA, center, 80, 3, 2, 3, 0.3);
            world.spawnParticle(Particle.FLAME, center, 60, 4, 1, 4, 0.2);
            
            // Sound shockwave
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 0.6f);
            world.playSound(center, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0f, 0.4f);
            
            // Explosion damage only (no direct entity damage)
            createExplosionAt(player, center, world);
            DamageSource source = DamageSource.builder(DamageType.PLAYER_EXPLOSION)
                            .withCausingEntity((Entity) player)
                            .build();
            player.damage(100, source);
            
        }, 24L); // ~1.2 second delay
    }

    private void createExplosionAt(LivingEntity entity, Location loc, World world) {
        // safer explosion attribution
        if (entity.isValid() && !entity.isDead()) {
            try {
                world.createExplosion(loc, 5.0F, false, true, entity);
            } catch (Exception e) {
                // fallback without attribution
                world.createExplosion(loc, 5.0F, false, true);
            }
        } else {
            world.createExplosion(loc, 5.0F, false, true);
        }
    }
    
    private void effectThunderbolt(Player player) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        World world = player.getWorld();
        
        // Find target location via raycast
        Block targetBlock = player.getTargetBlockExact(32);
        Location strikeLoc;
        
        if (targetBlock != null) {
            strikeLoc = targetBlock.getLocation().add(0.5, 0, 0.5);
        } else {
            // Max range air strike
            strikeLoc = eyeLoc.clone().add(direction.multiply(32));
        }
        
        // Charge-up telegraph at player
        world.playSound(eyeLoc, Sound.BLOCK_BEACON_AMBIENT, 1.0f, 2.0f);
        for (int i = 0; i < 8; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 1.5;
            world.spawnParticle(Particle.ELECTRIC_SPARK, 
                eyeLoc.clone().add(Math.cos(angle) * r, -0.5, Math.sin(angle) * r), 
                1, 0, 0, 0, 0.5);
        }
        
        // Delayed strike for impact
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            // Spawn actual lightning
            world.strikeLightning(strikeLoc);
            
            // Additional visual flair beyond vanilla lightning
            world.spawnParticle(Particle.FLASH, strikeLoc, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.END_ROD, strikeLoc, 20, 2, 2, 2, 0.3);
            
            // Chain lightning to nearby entities in dense packs
            double chainRadius = 4.0;
            int maxChains = 3;
            Set<UUID> hitEntities = new HashSet<>();
            
            // Get initial hit entities (within 2 blocks of strike)
            for (Entity entity : world.getNearbyEntities(strikeLoc, 2, 2, 2)) {
                if (entity instanceof LivingEntity target && !entity.getUniqueId().equals(player.getUniqueId())) {
                    hitEntities.add(target.getUniqueId());
                }
            }
            
            // Chain to nearby dense packs
            for (Entity entity : world.getNearbyEntities(strikeLoc, chainRadius, chainRadius, chainRadius)) {
                if (hitEntities.size() >= maxChains) break;
                if (!(entity instanceof LivingEntity target)) continue;
                if (entity.getUniqueId().equals(player.getUniqueId())) continue; // Skip caster for chain
                if (hitEntities.contains(entity.getUniqueId())) continue;
                
                // Check if this entity is near other enemies (dense pack definition: 2+ entities within 3 blocks)
                int nearbyEnemies = 0;
                for (Entity nearby : world.getNearbyEntities(entity.getLocation(), 3, 3, 3)) {
                    if (nearby instanceof LivingEntity && !nearby.getUniqueId().equals(player.getUniqueId())) {
                        nearbyEnemies++;
                    }
                }
                
                if (nearbyEnemies >= 2) {
                    // Chain strike visual
                    Location chainLoc = entity.getLocation();
                    world.spawnParticle(Particle.ELECTRIC_SPARK, chainLoc, 15, 0.5, 1, 0.5, 0.8);
                    
                    // Damage (lightning already hit primary, this is bonus chain damage)
                    target.damage(2, player); // 3 hearts chain damage
                    
                    // Small fire
                    target.setFireTicks(40);
                    
                    hitEntities.add(entity.getUniqueId());
                    
                    // Chain sound
                    world.playSound(chainLoc, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.8f, 1.5f);
                }
            }
            
            // Thunder sound (global, far reach)
            world.playSound(strikeLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 4.0f, 0.8f);
            
            // Terrain fire handled by vanilla lightning, but ensure it spreads a bit
            for (int i = 0; i < 5; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double r = Math.random() * 2;
                Location fireLoc = strikeLoc.clone().add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
                Block fireBlock = fireLoc.getBlock();
                if (fireBlock.getType() == Material.AIR && fireLoc.subtract(0, 1, 0).getBlock().getType().isSolid()) {
                    fireLoc.getBlock().setType(Material.FIRE);
                }
            }
            
            // Screen flash for nearby players
            for (Player p : world.getPlayers()) {
                if (p.getLocation().distance(strikeLoc) < 20) {
                    p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 0.6f);
                }
            }
            
        }, 6L); // 0.3s charge time
    }
    
    private void effectSixSeven(Player player) {
        Location center = player.getLocation();
        World world = player.getWorld();
        
        // Crimson blood burst from player
        for (int i = 0; i < 60; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 2;
            double y = Math.random() * 3;
            world.spawnParticle(Particle.DUST, 
                center.clone().add(Math.cos(angle) * r, y, Math.sin(angle) * r), 
                1, 0, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.5f));
        }
        
        // "67" pattern burst
        drawSixSevenPattern(player, center, Color.fromRGB(139, 0, 0), Particle.DAMAGE_INDICATOR);
        
        // Damage indicator particles floating up
        for (int i = 0; i < 20; i++) {
            world.spawnParticle(Particle.DAMAGE_INDICATOR, 
                center.clone().add(0, 1 + Math.random() * 2, 0), 
                1, 0.5, 0.5, 0.5, 0.2);
        }
        
        // Sound design
        world.playSound(center, Sound.ENTITY_WARDEN_DEATH, 1.5f, 0.4f);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.8f);
        world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 2.0f, 0.5f);
        
        DamageSource source = DamageSource.builder(DamageType.DRY_OUT)
            .build();
        player.damage(100, source);
    }
    
    private void effectFireball(Player player) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        World world = player.getWorld();
        
        // 15% chance for dragon fireball
        boolean isDragon = Math.random() < 0.15;
        
        org.bukkit.entity.Fireball fireball;
        if (isDragon) {
            fireball = (org.bukkit.entity.Fireball) world.spawnEntity(eyeLoc.clone().add(direction), EntityType.DRAGON_FIREBALL);
            // Dragon fireball: slower, more mass
            fireball.setVelocity(direction.multiply(0.8));
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!fireball.isValid() || fireball.isDead()) {
                        cancel();
                        return;
                    }
                    Location loc = fireball.getLocation();
                    world.spawnParticle(Particle.DRAGON_BREATH, loc, 3, 0.2, 0.2, 0.2, 0.01);
                    world.spawnParticle(Particle.DUST, loc, 2, 0.1, 0.1, 0.1, 0,
                        new Particle.DustOptions(Color.fromRGB(153, 50, 204), 1.0f));
                }
            }.runTaskTimer(Main.getPlugin(), 0L, 2L);
            
            world.playSound(eyeLoc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 0.7f);
        } else {
            fireball = (org.bukkit.entity.Fireball) world.spawnEntity(eyeLoc.clone().add(direction), EntityType.FIREBALL);
            // Standard: faster, explosive
            fireball.setVelocity(direction.multiply(1.5));
            fireball.setYield(1.0f); // Explosion power
            
            world.playSound(eyeLoc, Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
        }
        
        // Set shooter for proper attribution
        fireball.setShooter(player);
        
        // Despawn timer: remove if traveling too long (5 seconds / 100 blocks)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (fireball.isValid()) {
                    fireball.remove();
                }
            }
        }.runTaskLater(Main.getPlugin(), 100L);
    }
        
    private void effectIceShards(Player player) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        World world = player.getWorld();
        
        int shardCount = 7;
        double spreadAngle = 30.0; // Total spread degrees
        
        world.playSound(eyeLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
        world.playSound(eyeLoc, Sound.ENTITY_SNOW_GOLEM_SHOOT, 0.8f, 1.2f);
        
        for (int i = 0; i < shardCount; i++) {
            // Calculate spread offset
            double angleOffset = (i - (shardCount - 1) / 2.0) * (spreadAngle / (shardCount - 1));
            double radians = Math.toRadians(angleOffset);
            
            // Rotate direction vector around Y axis
            Vector shardDir = direction.clone();
            double x = shardDir.getX() * Math.cos(radians) - shardDir.getZ() * Math.sin(radians);
            double z = shardDir.getX() * Math.sin(radians) + shardDir.getZ() * Math.cos(radians);
            shardDir.setX(x).setZ(z).normalize();
            
            // Spawn snowball as shard projectile
            Snowball shard = (Snowball) world.spawnEntity(eyeLoc.clone().add(shardDir), EntityType.SNOWBALL);
            shard.setVelocity(shardDir.multiply(1.8));
            shard.setShooter(player);
            shard.setItem(new ItemStack(Material.PACKED_ICE));
            
            // Trail effect
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!shard.isValid() || shard.isDead()) {
                        cancel();
                        return;
                    }
                    Location loc = shard.getLocation();
                    world.spawnParticle(Particle.SNOWFLAKE, loc, 2, 0.1, 0.1, 0.1, 0.01);
                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(173, 216, 230), 0.8f));
                }
            }.runTaskTimer(Main.getPlugin(), 0L, 1L);
            
            // Impact detection and effect
            new BukkitRunnable() {
                
                @Override
                public void run() {
                    if (!shard.isValid() || shard.isDead()) {
                        // Hit something or despawned
                        cancel();
                        return;
                    }
                    
                    Location currentLoc = shard.getLocation();
                    
                    // Check for entity hit
                    for (Entity entity : world.getNearbyEntities(currentLoc, 1.5, 1.5, 1.5)) {
                        if (entity instanceof LivingEntity target && !entity.equals(player)) {
                            // Hit!
                            shard.remove();
                            
                            // Visual impact
                            world.spawnParticle(Particle.SNOWFLAKE, currentLoc, 8, 0.3, 0.3, 0.3, 0.1);
                            world.spawnParticle(Particle.BLOCK_CRUMBLE, currentLoc, 5, 0.2, 0.2, 0.2, 0, Material.ICE.createBlockData());
                            world.playSound(currentLoc, Sound.BLOCK_GLASS_BREAK, 0.6f, 1.8f);
                            
                            // Damage
                            target.damage(4, player);
                            
                            // Apply freeze effects
                            if (target instanceof Player targetPlayer) {
                                Cooldowns.setCooldown("immobilized", targetPlayer.getUniqueId(), 1000);
                            } else {
                                // Entities get Slowness X
                                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 9, false, true));
                            }
                            
                            cancel();
                            return;
                        }
                    }
                    
                    // Check for terrain collision
                    if (currentLoc.getBlock().getType().isSolid()) {
                        shard.remove();
                        world.spawnParticle(Particle.SNOWFLAKE, currentLoc, 5, 0.2, 0.2, 0.2, 0.05);
                        world.playSound(currentLoc, Sound.BLOCK_GLASS_BREAK, 0.4f, 2.0f);
                        cancel();
                        return;
                    }
                    
                }
            }.runTaskTimer(Main.getPlugin(), 0L, 1L);
            
            // Despawn timer (3 seconds / 60 blocks max)
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (shard.isValid()) {
                        shard.remove();
                    }
                }
            }.runTaskLater(Main.getPlugin(), 60L);
        }
    }
        
    private void effectLetItGrow(Player player) {
        
        // Raycast for target block
        Block targetBlock = player.getTargetBlockExact(32);
        if (targetBlock == null) return;
        
        // // Place sapling on top of targeted block
        // Location growLoc = targetBlock.getLocation().add(0.5, 1, 0.5);
        // Block growBlock = growLoc.getBlock();
        
        // // Must be air to place
        // if (growBlock.getType() != Material.AIR) return;
        
        // // REMOVED AZALEA — it's not a valid sapling
        // Material[] saplings = {
        //     Material.OAK_SAPLING,
        //     Material.BIRCH_SAPLING,
        //     Material.SPRUCE_SAPLING,
        //     Material.JUNGLE_SAPLING,
        //     Material.ACACIA_SAPLING,
        //     Material.DARK_OAK_SAPLING,
        //     Material.CHERRY_SAPLING
        // };
        
        // Material chosenSapling = saplings[(int) (Math.random() * saplings.length)];
        // TreeType treeType = randomTreeType(chosenSapling);
        
        // // Place sapling
        // growBlock.setType(chosenSapling);
        
        // // Particles & sound...
        // world.spawnParticle(Particle.HAPPY_VILLAGER, growLoc, 20, 0.5, 0.5, 0.5, 0);
        // world.spawnParticle(Particle.COMPOSTER, growLoc, 10, 0.3, 0.3, 0.3, 0);
        // world.playSound(growLoc, Sound.BLOCK_GRASS_PLACE, 1.0f, 0.8f);
        
        // // Instantly grow it
        // Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
        //     // Verify it's still our sapling
        //     if (growBlock.getType() != chosenSapling) return;
            
        //     // CRITICAL: Clear the sapling first!
        //     growBlock.setType(Material.AIR);
            
        //     // Now generate the tree
        //     boolean success = world.generateTree(growLoc, treeType);
            
        //     if (success) {
        //         // Growth burst particles
        //         world.spawnParticle(Particle.HAPPY_VILLAGER, growLoc.clone().add(0, 2, 0), 30, 2, 2, 2, 0);
        //         world.spawnParticle(Particle.DUST, growLoc.clone().add(0, 3, 0), 15, 1.5, 1.5, 1.5, 0,
        //             new Particle.DustOptions(Color.fromRGB(34, 139, 34), 2.0f));
        //         world.playSound(growLoc, Sound.BLOCK_GRASS_BREAK, 1.5f, 0.6f);
        //     }
        // }, 10L);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            treeGenerator.spawnTree(player, targetBlock.getLocation());
        }, 8L);

    }

    private static final TreeGenerator treeGenerator = new TreeGenerator(
        TreeGenerationConfig.getDefault(),
        new TreeComponentFactory()
    );

    // private TreeType randomTreeType(Material sapling) {
    //     return switch (sapling) {
    //         case BIRCH_SAPLING -> TreeType.BIRCH;
    //         case SPRUCE_SAPLING -> Math.random() < 0.3 ? TreeType.MEGA_REDWOOD : TreeType.REDWOOD;
    //         case JUNGLE_SAPLING -> Math.random() < 0.3 ? TreeType.JUNGLE_BUSH : TreeType.SMALL_JUNGLE;
    //         case ACACIA_SAPLING -> TreeType.ACACIA;
    //         case DARK_OAK_SAPLING -> TreeType.DARK_OAK;
    //         case CHERRY_SAPLING -> TreeType.CHERRY;
    //         // REMOVED: case AZALEA -> TreeType.AZALEA;
    //         default -> TreeType.TREE; // Oak
    //     };
    // }
    
    private void effectBlink(Player player) {
        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location start = player.getEyeLocation();
        Location destination = start.clone().add(direction.multiply(4.5));

        RayTraceResult result = player.getWorld().rayTraceBlocks(start, direction, 4.5,
                FluidCollisionMode.NEVER, true);

        if (result != null) {
            destination = result.getHitPosition().toLocation(player.getWorld())
                    .subtract(direction.multiply(0.5))
                    .setDirection(start.getDirection());
        }

        player.getWorld().playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.3f, 1.4f);
        player.getWorld().spawnParticle(Particle.SCULK_SOUL, start, 8, 0.3, 0.5, 0.3, 0.1);

        player.teleport(destination);

        player.getWorld().playSound(destination, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.3f, 0.7f);
        player.getWorld().spawnParticle(Particle.SCULK_SOUL, destination, 8, 0.3, 0.5, 0.3, 0.1);
        player.getWorld().spawnParticle(Particle.SONIC_BOOM, destination, 1, 0, 0, 0, 0);
    }
    
    private void effectHeal(Player player) {
        // Heal 2 health points (1 heart)
        double newHealth = Math.min(player.getHealth() + 2.0, player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setHealth(newHealth);
        
        // Visual feedback
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
    }
    
    private void effectDragon(Player player) {
        Location startLoc = player.getLocation().add(0, 0.5, 0);
        Vector direction = startLoc.getDirection().setY(0).normalize();
        World world = player.getWorld();
        
        // Move starting position forward by 1 block
        startLoc.add(direction.clone().multiply(1.0));
        
        // Sound - dragon roar and fire
        world.playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.2f);
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.5f);
        
        // Dragon travels forward
        for (int i = 0; i < 8; i++) {
            final int travelDistance = i;
            
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (travelDistance % 5 == 0) {
                    Location soundLoc = startLoc.clone().add(direction.clone().multiply(travelDistance));
                    world.playSound(soundLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.3f, 1.3f);
                }
                
                Location dragonPos = startLoc.clone().add(direction.clone().multiply(travelDistance));
                drawDragon(world, dragonPos, direction, travelDistance);
                
                // Damage check
                for (Entity entity : world.getNearbyEntities(dragonPos, 3.0, 3.0, 3.0)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player) && !(entity instanceof ArmorStand)) {
                        living.setFireTicks(50);
                        living.damage(2.5, player);
                        
                        Vector breathKnock = direction.clone().multiply(0.5).setY(0.3);
                        if(!(living.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                                living.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7)){
                            living.setVelocity(breathKnock);
                        }
                    }
                }
            }, travelDistance * 2L);
        }
    }

    private void drawDragon(World world, Location pos, Vector direction, int tick) {
        Vector perp = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        double wingBeat = Math.sin(tick * 0.5); // -1 to 1
        double bodyWave = Math.sin(tick * 0.3) * 0.3;
        
        // Colors
        Particle.DustOptions orangeBody = new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 100, 0), 1.2f);
        Particle.DustOptions redBody = new Particle.DustOptions(org.bukkit.Color.fromRGB(200, 50, 0), 1.0f);
        Particle.DustOptions tealAccent = new Particle.DustOptions(org.bukkit.Color.fromRGB(0, 200, 200), 0.8f);
        Particle.DustOptions brightTeal = new Particle.DustOptions(org.bukkit.Color.fromRGB(0, 255, 255), 1.0f);
        
        // === BODY SPINE (SOLID LINE) ===
        Location[] spine = new Location[9];
        spine[0] = pos.clone().add(direction.clone().multiply(0.3)).add(0, 2.0, 0); // Head forward
        spine[1] = pos.clone().add(0, 1.8, 0); // Neck start
        spine[2] = pos.clone().add(direction.clone().multiply(-1.0)).add(0, 1.5, 0); // Neck
        spine[3] = pos.clone().add(direction.clone().multiply(-2.0)).add(perp.clone().multiply(bodyWave * 0.2)).add(0, 1.3, 0); // Shoulders
        spine[4] = pos.clone().add(direction.clone().multiply(-3.0)).add(perp.clone().multiply(bodyWave * 0.4)).add(0, 1.2, 0); // Mid body
        spine[5] = pos.clone().add(direction.clone().multiply(-3.8)).add(perp.clone().multiply(bodyWave * 0.6)).add(0, 1.1, 0); // Rear
        spine[6] = pos.clone().add(direction.clone().multiply(-4.5)).add(perp.clone().multiply(bodyWave * 0.8)).add(0, 0.9, 0); // Tail start
        spine[7] = pos.clone().add(direction.clone().multiply(-5.2)).add(perp.clone().multiply(bodyWave * 1.0)).add(0, 0.7, 0); // Tail mid
        spine[8] = pos.clone().add(direction.clone().multiply(-5.8)).add(perp.clone().multiply(bodyWave * 1.2)).add(0, 0.5, 0); // Tail tip
        
        // Draw spine with teal
        for (int i = 0; i < spine.length; i++) {
            world.spawnParticle(Particle.DUST, spine[i], 1, 0, 0, 0, 0, tealAccent);
            
            // ADDED: Fire particles along spine
            if (tick % 3 == 0) {
                world.spawnParticle(Particle.FLAME, spine[i], 1, 0.1, 0.1, 0.1, 0);
            }
            
            // Connect spine segments
            if (i > 0) {
                Location prev = spine[i - 1];
                Location curr = spine[i];
                Vector between = curr.toVector().subtract(prev.toVector());
                int steps = 3;
                for (int j = 1; j < steps; j++) {
                    Location mid = prev.clone().add(between.clone().multiply((double) j / steps));
                    world.spawnParticle(Particle.DUST, mid, 1, 0, 0, 0, 0, tealAccent);
                    
                    // ADDED: Fire particles between spine segments
                    if (j % 2 == 0 && tick % 2 == 0) {
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, mid, 1, 0.05, 0.05, 0.05, 0);
                    }
                }
            }
        }
        
        // === HEAD ===
        Location head = spine[0];
        
        // Snout (extending forward)
        world.spawnParticle(Particle.DUST, head.clone().add(direction.clone().multiply(0.3)).add(0, -0.1, 0), 1, 0, 0, 0, 0, orangeBody);
        world.spawnParticle(Particle.DUST, head.clone().add(direction.clone().multiply(0.5)).add(0, -0.15, 0), 1, 0, 0, 0, 0, redBody);
        
        // Eyes - bright teal dots
        world.spawnParticle(Particle.DUST, head.clone().add(perp.clone().multiply(0.2)).add(0, 0.1, 0), 1, 0, 0, 0, 0, brightTeal);
        world.spawnParticle(Particle.DUST, head.clone().add(perp.clone().multiply(-0.2)).add(0, 0.1, 0), 1, 0, 0, 0, 0, brightTeal);
        world.spawnParticle(Particle.END_ROD, head.clone().add(perp.clone().multiply(0.2)).add(0, 0.1, 0), 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.END_ROD, head.clone().add(perp.clone().multiply(-0.2)).add(0, 0.1, 0), 1, 0, 0, 0, 0);
        
        // Horns - teal swept back
        for (int i = 0; i < 3; i++) {
            double hornLength = i * 0.25;
            world.spawnParticle(Particle.DUST, head.clone().add(perp.clone().multiply(0.3 + i * 0.1)).add(direction.clone().multiply(-hornLength)).add(0, 0.3 + i * 0.15, 0), 1, 0, 0, 0, 0, brightTeal);
            world.spawnParticle(Particle.DUST, head.clone().add(perp.clone().multiply(-0.3 - i * 0.1)).add(direction.clone().multiply(-hornLength)).add(0, 0.3 + i * 0.15, 0), 1, 0, 0, 0, 0, brightTeal);
            
            // ADDED: Soul fire on horn tips
            if (i == 2) {
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, head.clone().add(perp.clone().multiply(0.3 + i * 0.1)).add(direction.clone().multiply(-hornLength)).add(0, 0.3 + i * 0.15, 0), 1, 0.05, 0.05, 0.05, 0);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, head.clone().add(perp.clone().multiply(-0.3 - i * 0.1)).add(direction.clone().multiply(-hornLength)).add(0, 0.3 + i * 0.15, 0), 1, 0.05, 0.05, 0.05, 0);
            }
        }
        
        // Jaw/mouth outline
        world.spawnParticle(Particle.DUST, head.clone().add(perp.clone().multiply(0.15)).add(0, -0.2, 0), 1, 0, 0, 0, 0, redBody);
        world.spawnParticle(Particle.DUST, head.clone().add(perp.clone().multiply(-0.15)).add(0, -0.2, 0), 1, 0, 0, 0, 0, redBody);
        
        // Fire breath - actual flames (ENHANCED)
        for (int i = 0; i < 3; i++) {
            double distance = 0.6 + i * 0.4;
            world.spawnParticle(Particle.FLAME, head.clone().add(direction.clone().multiply(distance)), 3, 0.1 + i * 0.1, 0.1 + i * 0.1, 0.1 + i * 0.1, 0.02);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, head.clone().add(direction.clone().multiply(distance + 0.2)), 2, 0.15 + i * 0.1, 0.15 + i * 0.1, 0.15 + i * 0.1, 0.02);
            
            // ADDED: Additional fire breath variations
            if (i % 2 == 0) {
                world.spawnParticle(Particle.FLAME, head.clone().add(direction.clone().multiply(distance + 0.1)).add(perp.clone().multiply(0.15)), 1, 0.05, 0.05, 0.05, 0);
                world.spawnParticle(Particle.FLAME, head.clone().add(direction.clone().multiply(distance + 0.1)).add(perp.clone().multiply(-0.15)), 1, 0.05, 0.05, 0.05, 0);
            }
        }
        
        // ADDED: Continuous breath stream
        for (double d = 0.5; d <= 2.0; d += 0.2) {
            if (tick % 2 == 0) {
                world.spawnParticle(Particle.SMOKE, head.clone().add(direction.clone().multiply(d)), 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
        
        // === NECK ===
        // Neck sides (orange/red body)
        world.spawnParticle(Particle.DUST, spine[1].clone().add(perp.clone().multiply(0.25)), 1, 0, 0, 0, 0, orangeBody);
        world.spawnParticle(Particle.DUST, spine[1].clone().add(perp.clone().multiply(-0.25)), 1, 0, 0, 0, 0, orangeBody);
        world.spawnParticle(Particle.DUST, spine[2].clone().add(perp.clone().multiply(0.3)), 1, 0, 0, 0, 0, orangeBody);
        world.spawnParticle(Particle.DUST, spine[2].clone().add(perp.clone().multiply(-0.3)), 1, 0, 0, 0, 0, orangeBody);
        
        // ADDED: Fire particles on neck
        world.spawnParticle(Particle.FLAME, spine[1], 1, 0.15, 0.15, 0.15, 0);
        world.spawnParticle(Particle.FLAME, spine[2], 1, 0.15, 0.15, 0.15, 0);
        
        // === BODY (THICKER) ===
        // Shoulders
        world.spawnParticle(Particle.DUST, spine[3].clone().add(perp.clone().multiply(0.5)), 1, 0, 0, 0, 0, orangeBody);
        world.spawnParticle(Particle.DUST, spine[3].clone().add(perp.clone().multiply(-0.5)), 1, 0, 0, 0, 0, orangeBody);
        world.spawnParticle(Particle.DUST, spine[3].clone().add(perp.clone().multiply(0.35)).add(0, -0.2, 0), 1, 0, 0, 0, 0, redBody);
        world.spawnParticle(Particle.DUST, spine[3].clone().add(perp.clone().multiply(-0.35)).add(0, -0.2, 0), 1, 0, 0, 0, 0, redBody);
        
        // Mid body
        world.spawnParticle(Particle.DUST, spine[4].clone().add(perp.clone().multiply(0.5)), 1, 0, 0, 0, 0, orangeBody);
        world.spawnParticle(Particle.DUST, spine[4].clone().add(perp.clone().multiply(-0.5)), 1, 0, 0, 0, 0, orangeBody);
        world.spawnParticle(Particle.DUST, spine[4].clone().add(perp.clone().multiply(0.35)).add(0, -0.2, 0), 1, 0, 0, 0, 0, redBody);
        world.spawnParticle(Particle.DUST, spine[4].clone().add(perp.clone().multiply(-0.35)).add(0, -0.2, 0), 1, 0, 0, 0, 0, redBody);
        world.spawnParticle(Particle.DUST, spine[4].clone().add(0, -0.3, 0), 1, 0, 0, 0, 0, redBody); // belly
        
        // Rear
        world.spawnParticle(Particle.DUST, spine[5].clone().add(perp.clone().multiply(0.4)), 1, 0, 0, 0, 0, orangeBody);
        world.spawnParticle(Particle.DUST, spine[5].clone().add(perp.clone().multiply(-0.4)), 1, 0, 0, 0, 0, orangeBody);
        world.spawnParticle(Particle.DUST, spine[5].clone().add(0, -0.2, 0), 1, 0, 0, 0, 0, redBody);
        
        // ADDED: Enhanced fire on body
        for (int i = 3; i <= 5; i++) {
            world.spawnParticle(Particle.FLAME, spine[i].clone().add(0, 0.1, 0), 2, 0.2, 0.2, 0.2, 0);
            if (tick % 4 == 0) {
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, spine[i].clone().add(0, 0.2, 0), 1, 0.1, 0.1, 0.1, 0);
            }
        }
        
        // === WINGS (DEFINED STRUCTURE) ===
        double wingUp = wingBeat * 1.0;
        double wingForward = wingBeat * -0.4;
        
        // LEFT WING
        Location leftShoulder = spine[3].clone().add(perp.clone().multiply(-0.5));
        Location[] leftWing = new Location[5];
        leftWing[0] = leftShoulder; // Base
        leftWing[1] = leftShoulder.clone().add(perp.clone().multiply(-0.8)).add(0, 0.3 + wingUp * 0.8, 0).add(direction.clone().multiply(wingForward));
        leftWing[2] = leftShoulder.clone().add(perp.clone().multiply(-1.5)).add(0, 0.5 + wingUp, 0).add(direction.clone().multiply(wingForward * 0.8));
        leftWing[3] = leftShoulder.clone().add(perp.clone().multiply(-2.1)).add(0, 0.4 + wingUp * 0.7, 0).add(direction.clone().multiply(wingForward * 0.5));
        leftWing[4] = leftShoulder.clone().add(perp.clone().multiply(-2.5)).add(0, 0.2 + wingUp * 0.4, 0).add(direction.clone().multiply(wingForward * 0.3));
        
        // Wing bones - teal
        for (int i = 0; i < leftWing.length; i++) {
            world.spawnParticle(Particle.DUST, leftWing[i], 1, 0, 0, 0, 0, brightTeal);
            
            // ADDED: Fire on wing joints
            if (i > 0 && i < leftWing.length - 1) {
                world.spawnParticle(Particle.FLAME, leftWing[i], 1, 0.05, 0.05, 0.05, 0);
            }
            
            // Connect bones
            if (i > 0) {
                Location prev = leftWing[i - 1];
                Location curr = leftWing[i];
                Vector between = curr.toVector().subtract(prev.toVector());
                int steps = 4;
                for (int j = 1; j < steps; j++) {
                    Location mid = prev.clone().add(between.clone().multiply((double) j / steps));
                    world.spawnParticle(Particle.DUST, mid, 1, 0, 0, 0, 0, tealAccent);
                }
            }
        }
        
        // Wing membrane - orange/red dust particles
        for (int i = 1; i < leftWing.length; i++) {
            Location bone1 = leftWing[i - 1];
            Vector toSpine = spine[3].toVector().subtract(bone1.toVector());
            
            // Fill in membrane
            for (double t = 0.3; t <= 0.7; t += 0.4) {
                Location membrane = bone1.clone().add(toSpine.clone().multiply(t));
                world.spawnParticle(Particle.DUST, membrane, 1, 0, 0, 0, 0, orangeBody);
                
                // ADDED: Fire particles on wing membrane
                if (tick % 3 == 0) {
                    world.spawnParticle(Particle.FLAME, membrane, 1, 0.08, 0.08, 0.08, 0);
                }
            }
        }
        
        // RIGHT WING (mirror)
        Location rightShoulder = spine[3].clone().add(perp.clone().multiply(0.5));
        Location[] rightWing = new Location[5];
        rightWing[0] = rightShoulder;
        rightWing[1] = rightShoulder.clone().add(perp.clone().multiply(0.8)).add(0, 0.3 + wingUp * 0.8, 0).add(direction.clone().multiply(wingForward));
        rightWing[2] = rightShoulder.clone().add(perp.clone().multiply(1.5)).add(0, 0.5 + wingUp, 0).add(direction.clone().multiply(wingForward * 0.8));
        rightWing[3] = rightShoulder.clone().add(perp.clone().multiply(2.1)).add(0, 0.4 + wingUp * 0.7, 0).add(direction.clone().multiply(wingForward * 0.5));
        rightWing[4] = rightShoulder.clone().add(perp.clone().multiply(2.5)).add(0, 0.2 + wingUp * 0.4, 0).add(direction.clone().multiply(wingForward * 0.3));
        
        for (int i = 0; i < rightWing.length; i++) {
            world.spawnParticle(Particle.DUST, rightWing[i], 1, 0, 0, 0, 0, brightTeal);
            
            // ADDED: Fire on wing joints
            if (i > 0 && i < rightWing.length - 1) {
                world.spawnParticle(Particle.FLAME, rightWing[i], 1, 0.05, 0.05, 0.05, 0);
            }
            
            if (i > 0) {
                Location prev = rightWing[i - 1];
                Location curr = rightWing[i];
                Vector between = curr.toVector().subtract(prev.toVector());
                int steps = 4;
                for (int j = 1; j < steps; j++) {
                    Location mid = prev.clone().add(between.clone().multiply((double) j / steps));
                    world.spawnParticle(Particle.DUST, mid, 1, 0, 0, 0, 0, tealAccent);
                }
            }
        }
        
        for (int i = 1; i < rightWing.length; i++) {
            Location bone1 = rightWing[i - 1];
            Vector toSpine = spine[3].toVector().subtract(bone1.toVector());
            
            for (double t = 0.3; t <= 0.7; t += 0.4) {
                Location membrane = bone1.clone().add(toSpine.clone().multiply(t));
                world.spawnParticle(Particle.DUST, membrane, 1, 0, 0, 0, 0, orangeBody);
                
                // ADDED: Fire particles on wing membrane
                if (tick % 3 == 0) {
                    world.spawnParticle(Particle.FLAME, membrane, 1, 0.08, 0.08, 0.08, 0);
                }
            }
        }
        
        // === TAIL ===
        // Tail sides taper
        world.spawnParticle(Particle.DUST, spine[6].clone().add(perp.clone().multiply(0.25)), 1, 0, 0, 0, 0, redBody);
        world.spawnParticle(Particle.DUST, spine[6].clone().add(perp.clone().multiply(-0.25)), 1, 0, 0, 0, 0, redBody);
        world.spawnParticle(Particle.DUST, spine[7].clone().add(perp.clone().multiply(0.15)), 1, 0, 0, 0, 0, redBody);
        world.spawnParticle(Particle.DUST, spine[7].clone().add(perp.clone().multiply(-0.15)), 1, 0, 0, 0, 0, redBody);
        
        // Tail spikes - teal
        world.spawnParticle(Particle.DUST, spine[7].clone().add(0, 0.3, 0), 1, 0, 0, 0, 0, brightTeal);
        world.spawnParticle(Particle.DUST, spine[8].clone().add(0, 0.35, 0), 1, 0, 0, 0, 0, brightTeal);
        
        // ADDED: Fire along tail
        for (int i = 6; i <= 8; i++) {
            if (tick % 2 == 0) {
                world.spawnParticle(Particle.FLAME, spine[i], 1, 0.1, 0.1, 0.1, 0);
            }
        }
        
        // Tail tip flames (ENHANCED)
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, spine[8], 3, 0.15, 0.15, 0.15, 0.02);
        world.spawnParticle(Particle.FLAME, spine[8], 2, 0.1, 0.1, 0.1, 0.01);
        
        // === FLAME AURA (ENHANCED) ===
        for (int i = 0; i < spine.length; i += 2) {
            if (tick % 2 == 0) {
                // Fire aura around body
                world.spawnParticle(Particle.FLAME, spine[i].clone().add(
                    Math.random() * 0.4 - 0.2,
                    Math.random() * 0.4 - 0.2,
                    Math.random() * 0.4 - 0.2
                ), 1, 0, 0, 0, 0);
                
                // Soul fire aura
                if (i % 3 == 0) {
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, spine[i].clone().add(
                        Math.random() * 0.3 - 0.15,
                        Math.random() * 0.3 - 0.15,
                        Math.random() * 0.3 - 0.15
                    ), 1, 0, 0, 0, 0);
                }
            }
        }
        
        // ADDED: Trail of fire behind dragon
        for (double d = -0.5; d >= -2.5; d -= 0.5) {
            Location trailPos = pos.clone().add(direction.clone().multiply(d));
            if (tick % 3 == 0) {
                world.spawnParticle(Particle.FLAME, trailPos, 1, 0.2, 0.1, 0.2, 0);
                world.spawnParticle(Particle.SMOKE, trailPos, 1, 0.1, 0.05, 0.1, 0.005);
            }
        }
        
        // Ground scorch (ENHANCED)
        for (int i = 3; i <= 5; i++) {
            world.spawnParticle(Particle.SMOKE, spine[i].clone().add(0, -1.0, 0), 2, 0.3, 0.05, 0.3, 0.02);
            world.spawnParticle(Particle.ASH, spine[i].clone().add(0, -1.0, 0), 1, 0.2, 0.01, 0.2, 0);
            
            // ADDED: Ground fire effects
            if (tick % 4 == 0) {
                world.spawnParticle(Particle.FLAME, spine[i].clone().add(0, -0.8, 0), 1, 0.15, 0.02, 0.15, 0);
            }
        }
    }
    
    private void effectBlackHole(Player player) {
        Location center = player.getLocation();
        World world = player.getWorld();
        double radius = 12.0;
        double pullStrength = 0.8;
        
        // Spawn black hole core particle
        world.spawnParticle(Particle.DUST, center, 50, 0.5, 0.5, 0.5, 0, 
            new Particle.DustOptions(Color.BLACK, 3.0f));
        
        // Initial implosion sound
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 0.6f, 0.5f);
        
        // Pull entities over multiple ticks for sustained yank effect
        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 30; // 2 seconds of pulling
            
            @Override
            public void run() {
                if (ticks >= duration) {
                    // Final explosion/collapse
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 1, 0, 0, 0, 0);
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.8f);
                    cancel();
                    return;
                }
                
                // Spiral particles rotating around center
                double angle = ticks * 0.5;
                for (int i = 0; i < 8; i++) {
                    double r = 2 + (ticks % 10) * 0.3;
                    double x = Math.cos(angle + i * Math.PI / 4) * r;
                    double z = Math.sin(angle + i * Math.PI / 4) * r;
                    world.spawnParticle(Particle.DUST, center.clone().add(x, 0.5, z), 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.PURPLE, 1.5f));
                }
                
                // Dark core
                world.spawnParticle(Particle.DUST, center.clone().add(0, 1, 0), 6, 0.3, 0.3, 0.3, 0,
                    new Particle.DustOptions(Color.BLACK, 2.0f));
                
                // Get all entities in radius
                for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                    // Skip caster and dead entities
                    if (entity.equals(player) || entity.isDead()) continue;
                    
                    Location entityLoc = entity.getLocation();
                    double distance = entityLoc.distance(center);
                    
                    if (distance > radius || distance < 0.5) continue;
                    
                    // Calculate pull vector (stronger when closer)
                    Vector toCenter = center.toVector().subtract(entityLoc.toVector()).normalize();
                    double strength = pullStrength * (1 + (radius - distance) / radius); // Stronger pull when closer
                    
                    // Apply velocity yank
                    Vector velocity = toCenter.multiply(strength);
                    
                    // Lift slightly then slam down for dramatic effect
                    if (distance > 3) {
                        velocity.setY(0.3); // Slight lift while pulling in
                    } else {
                        velocity.setY(-0.5); // Slam down when close
                    }
                    
                    entity.setVelocity(velocity);
                }
                
                // Sucking sound every few ticks
                if (ticks % 8 == 0) {
                    world.playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 0.2f, 0.3f - (ticks * 0.005f));
                }
                
                ticks++;
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 1L);
    }
    
    private void effectPush(Player player) {
        Location center = player.getLocation();
        World world = player.getWorld();
        double radius = 6.0;
        double pushStrength = 1.5;
        double verticalLift = 0.6;
        
        // Visual buildup - shockwave charging at player feet
        for (int i = 0; i < 20; i++) {
            double angle = 2 * Math.PI * i / 20;
            double x = Math.cos(angle) * 1.5;
            double z = Math.sin(angle) * 1.5;
            world.spawnParticle(Particle.DUST, center.clone().add(x, 0.1, z), 1, 0, 0, 0, 0,
                new Particle.DustOptions(Color.WHITE, 1.5f));
        }
        
        // Charging sound
        world.playSound(center, Sound.BLOCK_PISTON_EXTEND, 1.0f, 0.8f);
        
        // Delayed push for impact timing
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            // Expanding ring visual
            for (int ring = 0; ring < 3; ring++) {
                double r = 2 + ring * 2;
                int particles = 16 + ring * 8;
                for (int i = 0; i < particles; i++) {
                    double angle = 2 * Math.PI * i / particles;
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    world.spawnParticle(Particle.CLOUD, center.clone().add(x, 0.2, z), 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.DUST, center.clone().add(x, 0.2, z), 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(240, 248, 255), 2.0f - ring * 0.5f));
                }
            }
            
            // Force burst particles
            world.spawnParticle(Particle.EXPLOSION, center, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.GUST, center.clone().add(0, 1, 0), 5, 2, 1, 2, 0.1);
            
            // Push sound effect
            world.playSound(center, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.5f, 0.6f);
            world.playSound(center, Sound.ENTITY_BREEZE_WIND_BURST, 0.8f, 0.8f);
            
            // Apply push to all nearby entities
            for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                // Skip the caster
                if (entity.equals(player)) continue;
                
                Location entityLoc = entity.getLocation();
                double distance = entityLoc.distance(center);
                
                if (distance > radius || distance < 0.5) continue;
                
                // Calculate push vector (away from player)
                Vector awayFromPlayer = entityLoc.toVector().subtract(center.toVector()).normalize();
                
                // Stronger push when closer
                double distanceMultiplier = 1 + (radius - distance) / radius;
                Vector pushVelocity = awayFromPlayer.multiply(pushStrength * distanceMultiplier);
                
                // Add vertical lift for dramatic effect
                pushVelocity.setY(verticalLift * distanceMultiplier);
                
                // Apply velocity
                entity.setVelocity(pushVelocity);
                
                // Visual trail on pushed entities
                if (entity instanceof LivingEntity) {
                    world.spawnParticle(Particle.CLOUD, entityLoc.clone().add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0.05);
                }
            }
            
            // Screen shake effect for nearby players
            for (Player p : world.getPlayers()) {
                if (p.getLocation().distance(center) < 10 && !p.equals(player)) {
                    p.playSound(p.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.5f, 0.5f);
                }
            }
            
        }, 4L); // 0.2s windup
    }
    
    private void effectShield(Player player) {
        ItemStack item = player.getEquipment().getItemInMainHand();
        if(ItemNBT.hasBeanGameTag(item) && ItemNBT.isBeanGame(item, getKey())){
            item.setType(Material.SHIELD);
        }
    }
    
    private void effectMaceAttack(Player player) {
        ItemStack item = player.getEquipment().getItemInMainHand();
        if(ItemNBT.hasBeanGameTag(item) && ItemNBT.isBeanGame(item, getKey())){
            item.setType(Material.MACE);
        }
    }
    
    private void effectLeap(Player player) {
        if(!(player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7) &&
                    !Cooldowns.onCooldown("attack", player.getUniqueId())){
            player.setVelocity(player.getLocation().getDirection().multiply(1.2D).setY(0.9D));
            Cooldowns.setCooldown("fall_damage_immunity", player.getUniqueId(), 4000L);
        }
    }
    
    private void effectSchizophrenia(Player player) {
        List<Entity> entities = player.getNearbyEntities(10, 10, 10);
        if(!entities.contains(player)) entities.add(player);
        for(Entity target : entities){
            if(target instanceof Player){
                UUID uuid = target.getUniqueId();
                Cooldowns.setCooldown("schizophrenic", uuid, 12000L);
            }
        }
    }
    
    private void effectWall(Player player) {
        Location center = player.getLocation();
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        World world = player.getWorld();
        
        // Wall placement: 3 blocks ahead of player
        Location wallCenter = center.clone().add(direction.clone().multiply(3));
        
        // Align wall perpendicular to player facing
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX());
        
        // Wall dimensions: 5 wide, 4 tall
        int width = 2; // blocks each side of center
        int height = 4;
        
        // Block types for variety
        Material[] wallMaterials = {
            Material.BRICKS,
            Material.BRICKS,
            Material.BRICKS,
            Material.GRANITE,
            Material.GRANITE,
            Material.TERRACOTTA,
            Material.TERRACOTTA,
            Material.RED_TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA
        };
        
        // Random for texture variety
        Random random = new Random();
        
        // Build wall from bottom up
        for (int y = 0; y < height; y++) {
            for (int x = -width; x <= width; x++) {
                // Calculate block position
                Vector offset = perpendicular.clone().multiply(x);
                Location blockLoc = wallCenter.clone().add(offset).add(0, y, 0);
                
                // Only replace air/liquid/soft blocks (don't destroy existing structures)
                Block targetBlock = blockLoc.getBlock();
                Material existing = targetBlock.getType();
                
                boolean canReplace = !BlockCategories.getFunctionalBlocks().contains(existing);
                
                if (canReplace) {
                    // Pick random material with brick bias
                    Material chosen = wallMaterials[random.nextInt(wallMaterials.length)];
                    
                    // Place block with slight delay for building animation effect
                    final Location finalLoc = blockLoc.clone();
                    final Material finalMaterial = chosen;
                    
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                        Block block = finalLoc.getBlock();
                        block.setType(finalMaterial);
                        
                        // Particles for block placement
                        world.spawnParticle(Particle.BLOCK_CRUMBLE, finalLoc.clone().add(0.5, 0.5, 0.5), 
                            4, 0.2, 0.2, 0.2, 0, finalMaterial.createBlockData());
                        
                    }, y * 2L + Math.abs(x)); // Staggered build effect
                }
            }
        }
        
        // Visual and audio feedback
        world.playSound(center, Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);
        world.playSound(wallCenter, Sound.BLOCK_ANVIL_LAND, 0.6f, 1.2f);
        
        // Ground crack effect at wall base
        for (int i = 0; i < 16; i++) {
            double angle = 2 * Math.PI * i / 16;
            double r = 1 + Math.random() * 2;
            Location crackLoc = wallCenter.clone().add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
            world.spawnParticle(Particle.BLOCK_CRUMBLE, crackLoc, 2, 0.3, 0.1, 0.3, 0, Material.STONE.createBlockData());
        }
        
        // Wall placement indicator particles
        for (int x = -width; x <= width; x++) {
            for (int y = 0; y < height; y++) {
                Vector offset = perpendicular.clone().multiply(x);
                Location previewLoc = wallCenter.clone().add(offset).add(0, y, 0);
                world.spawnParticle(Particle.DUST, previewLoc.clone().add(0.5, 0.5, 0.5), 2, 0.2, 0.2, 0.2, 0,
                    new Particle.DustOptions(Color.fromRGB(139, 69, 19), 1.0f));
            }
        }
    }
    
    private void effectSteed(Player player) {
        Location spawnLoc = player.getLocation().add(player.getLocation().getDirection().multiply(2));
        World world = player.getWorld();
        
        // Spawn horse
        Horse horse = (Horse) world.spawnEntity(spawnLoc, EntityType.HORSE);
        
        // Configure horse stats
        horse.setTamed(true);
        horse.setOwner(player);
        horse.setAdult();
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        
        // Set appearance - random variant for variety
        Horse.Style[] variants = Horse.Style.values();
        horse.setStyle(variants[(int)(Math.random() * variants.length)]);
        
        // Apply speed and jump attributes
        // Base horse speed: 0.1125 - 0.3375, we want fast (0.3+)
        horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.32);
        
        // Jump strength: 0.4 - 1.0, we want high jump (0.9+)
        horse.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0.95);
        
        // Extra health for durability
        horse.getAttribute(Attribute.MAX_HEALTH).setBaseValue(30.0);
        horse.setHealth(30.0);
        
        // Visual effects on spawn
        world.spawnParticle(Particle.HAPPY_VILLAGER, spawnLoc.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0);
        world.spawnParticle(Particle.NOTE, spawnLoc.clone().add(0, 2, 0), 10, 0.3, 0.3, 0.3, 0);
        world.playSound(spawnLoc, Sound.ENTITY_HORSE_AMBIENT, 1.0f, 1.0f);
        world.playSound(spawnLoc, Sound.ENTITY_HORSE_ARMOR, 0.8f, 1.2f);
        
        // Auto-mount player
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (horse.isValid() && !horse.isDead()) {
                horse.addPassenger(player);
            }
        }, 2L);
        
        // Despawn after 30 seconds
        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 30 * 20; // 30 seconds in ticks
            
            @Override
            public void run() {
                if (!horse.isValid() || horse.isDead()) {
                    cancel();
                    return;
                }
                
                ticks++;
                
                // Warning particles in last 5 seconds
                if (ticks >= duration - 100) { // Last 5 seconds
                    if (ticks % 10 == 0) { // Every 0.5 seconds
                        world.spawnParticle(Particle.WITCH, horse.getLocation().add(0, 1, 0), 5, 0.4, 0.4, 0.4, 0);
                    }
                }
                
                // Despawn
                if (ticks >= duration) {
                    // Dismount player if riding
                    if (horse.getPassengers().contains(player)) {
                        horse.removePassenger(player);
                    }
                    
                    // Despawn effects
                    Location despawnLoc = horse.getLocation();
                    world.spawnParticle(Particle.POOF, despawnLoc.clone().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                    world.playSound(despawnLoc, Sound.ENTITY_HORSE_DEATH, 0.6f, 0.8f);
                    
                    horse.remove();
                    cancel();
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 1L);
    }
    
    private void effectChickens(Player player) {
    Location center = player.getLocation();
    World world = player.getWorld();
    double radius = 8.0;
    
    List<LivingEntity> transformed = new ArrayList<>();
    
    // Draw egg-colored sphere outline at radius
    Color eggWhite = Color.fromRGB(255, 255, 240);
    Color eggYolk = Color.fromRGB(255, 215, 0);
    
    // Sphere outline particles
    int spherePoints = 64;
    for (int i = 0; i < spherePoints; i++) {
        double theta = 2 * Math.PI * i / spherePoints;
        for (int j = 0; j < 8; j++) {
            double phi = Math.PI * j / 8;
            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.cos(phi);
            double z = radius * Math.sin(phi) * Math.sin(theta);
            
            Color particleColor = (j % 2 == 0) ? eggWhite : eggYolk;
            world.spawnParticle(Particle.DUST, center.clone().add(x, y + 1, z), 1, 0, 0, 0, 0,
                new Particle.DustOptions(particleColor, 1.2f));
        }
    }
    
    // Ground ring
        for (int i = 0; i < 32; i++) {
            double angle = 2 * Math.PI * i / 32;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            world.spawnParticle(Particle.DUST, center.clone().add(x, 0.1, z), 1, 0, 0, 0, 0,
                new Particle.DustOptions(eggYolk, 1.5f));
        }
        
        // Sound cue
        world.playSound(center, Sound.ENTITY_CHICKEN_AMBIENT, 1.0f, 0.6f);
        
        // Find valid targets using spherical distance
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (entity instanceof Player) continue;
            if (entity instanceof ArmorStand) continue;
            if (entity instanceof Chicken) continue;
            if (entity.isDead()) continue;
            
            // Strict spherical check (not bounding box)
            double distance = entity.getLocation().distance(center);
            if (distance > radius) continue;
            
            transformed.add(target);
            
            // Transform to chicken
            Location entityLoc = target.getLocation();
            
            // Visual transformation effect
            world.spawnParticle(Particle.ITEM_SLIME, entityLoc.clone().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.1);
            world.spawnParticle(Particle.HAPPY_VILLAGER, entityLoc, 10, 0.4, 0.4, 0.4, 0);
            world.playSound(entityLoc, Sound.ENTITY_CHICKEN_AMBIENT, 1.0f, 1.2f);
            
            // Spawn chicken at location
            Chicken chicken = (Chicken) world.spawnEntity(entityLoc, EntityType.CHICKEN);
            chicken.setAdult();
            
            // Remove original entity
            target.remove();
            
            // Schedule egg pop after 3 seconds
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (!chicken.isValid() || chicken.isDead()) return;
                
                Location chickenLoc = chicken.getLocation();
                
                // Poof into egg
                world.spawnParticle(Particle.POOF, chickenLoc.clone().add(0, 0.5, 0), 8, 0.2, 0.2, 0.2, 0.05);
                world.spawnParticle(Particle.ITEM_SLIME, chickenLoc, 5, 0.1, 0.1, 0.1, 0);
                world.playSound(chickenLoc, Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.5f);
                
                // Drop egg item
                chicken.getWorld().dropItemNaturally(chickenLoc, new ItemStack(Material.EGG));
                
                // Remove chicken
                chicken.remove();
                
            }, 60L); // 3 seconds = 60 ticks
        }
    }
    
    private void effectDisappear(Player player) {
        if(!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*8, 0, false, false));
    }
    
    private void effectLuck(Player player) {
        if(!player.hasPotionEffect(PotionEffectType.LUCK)) player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 20*8, 0, false, false));
    }
    
    private void effectDamageReduction(Player player) {
        if(!player.hasPotionEffect(PotionEffectType.RESISTANCE)) player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20*8, 0, false, false));
    }
    
    private void effectLight(Player player) {
        Location center = player.getLocation();
        World world = player.getWorld();
        double radius = 12.0;
        
        // Affect self
        removeDarknessEffects(player);
        
        // Affect nearby players
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof Player target)) continue;
            
            // Spherical distance check
            if (target.getLocation().distance(center) > radius) continue;
            
            removeDarknessEffects(target);

            if(!target.hasPotionEffect(PotionEffectType.GLOWING)) target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 8, 0, false, false));
        }
        
        // Sound feedback only
        world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 0.6f, 1.8f);
    }

    private void removeDarknessEffects(Player player) {
        // Remove blindness
        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        
        // Remove darkness (1.19+ effect)
        if (player.hasPotionEffect(PotionEffectType.DARKNESS)) {
            player.removePotionEffect(PotionEffectType.DARKNESS);
        }
        
    }
    
    private void effectBlood(Player player) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        World world = player.getWorld();
        
        double range = 20.0;
        double beamWidth = 0.8;
        int damage = 8; // 6 hearts to target
        int selfDamage = 4; // 2 hearts to self
        
        // Self damage first (blood sacrifice)
        DamageSource selfSource = DamageSource.builder(DamageType.MAGIC).build();
        player.damage(selfDamage, selfSource);
        
        // Blood burst from player
        Location center = player.getLocation();
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 1.5;
            double y = Math.random() * 2;
            world.spawnParticle(Particle.DUST, 
                center.clone().add(Math.cos(angle) * r, y, Math.sin(angle) * r), 
                1, 0, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.5f));
        }
        
        // Blood spray sound
        world.playSound(center, Sound.ENTITY_PLAYER_HURT, 1.0f, 0.8f);
        world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 0.5f);
        
        // Beam travel
        Location current = eyeLoc.clone();
        Set<UUID> hitEntities = new HashSet<>();
        boolean hitSomething = false;
        
        for (double d = 0; d <= range; d += 0.5) {
            current.add(direction.clone().multiply(0.5));
            
            // Blood beam particles
            world.spawnParticle(Particle.DUST, current, 2, beamWidth/2, beamWidth/2, beamWidth/2, 0,
                new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.2f));
            world.spawnParticle(Particle.DAMAGE_INDICATOR, current, 1, 0.1, 0.1, 0.1, 0);
            
            // Trail of blood droplets
            if (d % 1 == 0) {
                world.spawnParticle(Particle.DUST, current.clone().add(0, -0.2, 0), 1, 0.2, 0, 0.2, 0,
                    new Particle.DustOptions(Color.fromRGB(100, 0, 0), 1.0f));
            }
            
            // Check for entity hits
            for (Entity entity : world.getNearbyEntities(current, beamWidth, beamWidth, beamWidth)) {
                if (!(entity instanceof LivingEntity target)) continue;
                if (entity.equals(player)) continue; // Skip self
                if (hitEntities.contains(entity.getUniqueId())) continue; // Already hit
                
                hitEntities.add(entity.getUniqueId());
                hitSomething = true;
                
                // Deal damage
                DamageSource beamSource = DamageSource.builder(DamageType.MAGIC)
                    .withCausingEntity(player)
                    .build();
                target.damage(damage, beamSource);
                
                // Blood splash on hit
                Location hitLoc = target.getLocation();
                world.spawnParticle(Particle.DUST, hitLoc.clone().add(0, 1, 0), 15, 0.4, 0.4, 0.4, 0.1,
                    new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.5f));
                world.spawnParticle(Particle.DAMAGE_INDICATOR, hitLoc.clone().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.2);
                world.playSound(hitLoc, Sound.ENTITY_PLAYER_HURT, 1.0f, 0.6f);
                
                // Lifesteal visual (blood returns to player)
                Vector toPlayer = center.toVector().subtract(hitLoc.toVector()).normalize();
                for (double t = 0; t < 1; t += 0.1) {
                    Location bloodReturn = hitLoc.clone().add(toPlayer.clone().multiply(t * hitLoc.distance(center)));
                    world.spawnParticle(Particle.DUST, bloodReturn, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(200, 0, 0), 0.8f));
                }
            }
            
            // Stop at blocks
            if (current.getBlock().getType().isSolid()) {
                // Blood splatter on wall
                world.spawnParticle(Particle.DUST, current, 10, 0.3, 0.3, 0.3, 0.1,
                    new Particle.DustOptions(Color.fromRGB(100, 0, 0), 1.5f));
                world.playSound(current, Sound.BLOCK_SLIME_BLOCK_HIT, 0.6f, 0.5f);
                break;
            }
        }
        
        // Hit confirmation sound
        if (hitSomething) {
            world.playSound(center, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 0.7f, 0.8f);
        }
    }
    
    private void effectGrass(Player player) {
        Location center = player.getLocation();
        World world = player.getWorld();
        double radius = 6.0;
        
        Random random = new Random();
        Set<Location> occupied = new HashSet<>();
        
        // 3D spherical iteration
        for (int x = -6; x <= 6; x++) {
            for (int y = -6; y <= 6; y++) {
                for (int z = -6; z <= 6; z++) {
                    double distSq = x * x + y * y + z * z;
                    if (distSq > radius * radius) continue;
                    
                    Location checkLoc = center.clone().add(x, y, z);
                    
                    if (occupied.contains(checkLoc)) continue;
                    
                    Block checkBlock = checkLoc.getBlock();
                    if (checkBlock.getType() != Material.AIR) continue;
                    
                    Block groundBlock = checkLoc.clone().subtract(0, 1, 0).getBlock();
                    Material groundType = groundBlock.getType();
                    
                    boolean validGround = groundType == Material.GRASS_BLOCK || 
                                    groundType == Material.DIRT ||
                                    groundType == Material.PODZOL ||
                                    groundType == Material.COARSE_DIRT ||
                                    groundType == Material.ROOTED_DIRT ||
                                    groundType == Material.MYCELIUM ||
                                    groundType == Material.MOSS_BLOCK;
                    
                    if (!validGround) continue;
                    
                    Block aboveBlock = checkLoc.clone().add(0, 1, 0).getBlock();
                    boolean canPlaceTall = aboveBlock.getType() == Material.AIR;
                    
                    Material grassType;
                    double roll = random.nextDouble();
                    
                    if (groundType == Material.PODZOL || groundType == Material.MYCELIUM) {
                        grassType = roll < 0.5 ? Material.TALL_GRASS : (roll < 0.8 ? Material.FERN : Material.SHORT_GRASS);
                    } else if (groundType == Material.MOSS_BLOCK) {
                        grassType = roll < 0.4 ? Material.MOSS_CARPET : (roll < 0.7 ? Material.TALL_GRASS : Material.SHORT_GRASS);
                    } else if (canPlaceTall && roll < 0.55) {
                        grassType = Material.TALL_GRASS;
                    } else if (roll < 0.75) {
                        grassType = Material.SHORT_GRASS;
                    } else if (roll < 0.88) {
                        grassType = Material.FERN;
                    } else {
                        Material[] flowers = {
                            Material.DANDELION, 
                            Material.POPPY, 
                            Material.BLUE_ORCHID,
                            Material.ALLIUM,
                            Material.AZURE_BLUET,
                            Material.OXEYE_DAISY,
                            Material.CORNFLOWER
                        };
                        grassType = flowers[random.nextInt(flowers.length)];
                    }
                    
                    final Location placeLoc = checkLoc.clone();
                    final Material finalType = grassType;
                    final boolean isTall = (grassType == Material.TALL_GRASS);
                    int delay = (int) (Math.sqrt(distSq) * 2);
                    
                    if (isTall) {
                        occupied.add(placeLoc.clone().add(0, 1, 0));
                    }
                    
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                        Block placeBlock = placeLoc.getBlock();
                        if (placeBlock.getType() != Material.AIR) return;
                        
                        if (finalType == Material.TALL_GRASS) {
                            // Place both halves in same tick to prevent breaking
                            Block topBlock = placeLoc.clone().add(0, 1, 0).getBlock();
                            
                            // Set top first (so bottom has support when placed)
                            if (topBlock.getType() == Material.AIR) {
                                topBlock.setType(Material.TALL_GRASS, false); // no physics update yet
                                org.bukkit.block.data.Bisected topData = (org.bukkit.block.data.Bisected) topBlock.getBlockData();
                                topData.setHalf(org.bukkit.block.data.Bisected.Half.TOP);
                                topBlock.setBlockData(topData, false);
                            }
                            
                            // Then bottom
                            placeBlock.setType(Material.TALL_GRASS, false);
                            org.bukkit.block.data.Bisected bottomData = (org.bukkit.block.data.Bisected) placeBlock.getBlockData();
                            bottomData.setHalf(org.bukkit.block.data.Bisected.Half.BOTTOM);
                            placeBlock.setBlockData(bottomData, true); // physics update on bottom
                            
                        } else {
                            placeBlock.setType(finalType);
                        }
                        
                    }, delay);
                    
                }
            }
        }
        
        world.playSound(center, Sound.BLOCK_GRASS_PLACE, 1.0f, 0.8f);
        world.playSound(center, Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 0.6f, 1.2f);
    }
    
    private void effectStrength(Player player) {
        if(!player.hasPotionEffect(PotionEffectType.STRENGTH)) player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20*8, 0, false, false));
    }
    
    private void effectBook(Player player) {
        World world = player.getWorld();
        Location loc = player.getLocation();
        
        // Get all registered enchantments
        List<Enchantment> possibleEnchants = new ArrayList<>();
        for (Enchantment enchant : Registry.ENCHANTMENT) {
            // Skip enchantments that can't be on books or are level 0
            if (enchant.getMaxLevel() >= 1) {
                possibleEnchants.add(enchant);
            }
        }
        
        // Pick random enchantment
        Enchantment chosen = possibleEnchants.get((int)(Math.random() * possibleEnchants.size()));
        
        // Create enchanted book
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(chosen, 1, true);
        book.setItemMeta(meta);
        
        // Give to player (or drop if inventory full)
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(book);
        } else {
            world.dropItemNaturally(loc, book);
        }
        
        // Sound effects
        world.playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
        world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
    }
    
    private void effectThornLash(Player player) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        World world = player.getWorld();
        
        double range = 12.0;
        double whipWidth = 1.2;
        int segments = 24;
        Set<UUID> hitEntities = new HashSet<>();
        
        // Sound - vine crack
        world.playSound(eyeLoc, Sound.BLOCK_VINE_BREAK, 1.0f, 0.6f);
        world.playSound(eyeLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 0.5f);
        
        // Whip motion - sine wave that intensifies toward the tip
        for (int i = 0; i < segments; i++) {
            final int segment = i;
            double distance = (i / (double) segments) * range;
            
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                // Calculate whip curve - wider swing at tip
                double progress = segment / (double) segments; // 0 to 1
                double waveIntensity = progress * 2.5; // Swing gets wider toward end
                double waveAngle = Math.sin(progress * Math.PI * 3) * waveIntensity;
                
                // Perpendicular vector for wave motion
                Vector perp = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
                
                // Main whip path
                Vector offset = direction.clone().multiply(distance)
                    .add(perp.clone().multiply(waveAngle));
                Location whipLoc = eyeLoc.clone().add(offset);
                
                // Dark green thorn particles
                world.spawnParticle(Particle.DUST, whipLoc, 3, 0.15, 0.15, 0.15, 0,
                    new Particle.DustOptions(Color.fromRGB(0, 100, 0), 1.0f));
                
                // Poison droplets
                world.spawnParticle(Particle.DUST, whipLoc, 2, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(Color.fromRGB(50, 205, 50), 0.6f));
                
                // Thorn spikes occasionally
                if (segment % 4 == 0) {
                    world.spawnParticle(Particle.DAMAGE_INDICATOR, whipLoc, 1, 0.05, 0.05, 0.05, 0);
                }
                
                // Hit detection along whip path
                for (Entity entity : world.getNearbyEntities(whipLoc, whipWidth/2, whipWidth/2, whipWidth/2)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (entity.equals(player)) continue;
                    if (hitEntities.contains(entity.getUniqueId())) continue;
                    
                    hitEntities.add(entity.getUniqueId());
                    
                    // Damage
                    target.damage(3, player);
                    
                    // Poison effect - 5 seconds
                    target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1, false, true));
                    
                    // Bleed visual
                    Location hitLoc = target.getLocation();
                    world.spawnParticle(Particle.DUST, hitLoc.clone().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.1,
                        new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.2f));
                    world.spawnParticle(Particle.WITCH, hitLoc.clone().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.05);
                    
                    // Hit sound
                    world.playSound(hitLoc, Sound.ENTITY_PLAYER_HURT, 0.6f, 0.8f);
                    world.playSound(hitLoc, Sound.BLOCK_VINE_BREAK, 0.5f, 0.4f);
                }
                
                // Block collision stops whip
                if (whipLoc.getBlock().getType().isSolid() && segment > 2) {
                    // Thorn splatter on wall
                    world.spawnParticle(Particle.BLOCK_CRUMBLE, whipLoc, 6, 0.2, 0.2, 0.2, 0.05, 
                        Material.OAK_LEAVES.createBlockData());
                    world.playSound(whipLoc, Sound.BLOCK_GRASS_BREAK, 0.5f, 0.6f);
                    return; // Stop further segments
                }
                
                // Tip effect - snap sound at end
                if (segment == segments - 1) {
                    world.playSound(whipLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7f, 0.4f);
                }
                
            }, segment); // 1 tick per segment for whip motion
        }
    }
    
    private void effectDarkness(Player player) {
        Location center = player.getLocation();
        World world = player.getWorld();
        double radius = 12.0;
        
        // Affect self
        applyDarknessEffect(player);
        
        // Affect nearby players
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof Player target)) continue;
            
            // Spherical distance check
            if (target.getLocation().distance(center) > radius) continue;
            
            applyDarknessEffect(target);
        }
        
        // Sound feedback only
        world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 0.6f, 1.8f);
    }

    private void applyDarknessEffect(LivingEntity target){
        if(!target.hasPotionEffect(PotionEffectType.DARKNESS)) target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 5* 20, 0, false, true));
    }

    private short[] resample48to16(short[] input) {
        // Simple downsampling: take every 3rd sample (48kHz -> 16kHz)
        int outputSize = input.length / 3;
        short[] output = new short[outputSize];
        for (int i = 0, j = 0; i < input.length && j < outputSize; i += 3, j++) {
            output[j] = input[i];
        }
        return output;
    }
    
    private byte[] shortsToBytes(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Vosk expects little-endian
        for (short s : shorts) {
            buffer.putShort(s);
        }
        return bytes;
    }
    
    private String extractText(String jsonResult) {
        if (jsonResult == null || jsonResult.isEmpty()) return "";
        
        // Bukkit.getLogger().fine("[Spellbook] Raw Vosk output: " + jsonResult);
        
        if (jsonResult.contains("\"text\" : \"\"")) return "";
        
        int start = jsonResult.indexOf("\"text\" : \"");
        if (start == -1) return "";
        
        start += 10;
        int end = jsonResult.indexOf("\"", start);
        if (end == -1) return "";
        
        return jsonResult.substring(start, end).trim().toLowerCase();
    }
    
    @Override
    public void onInitialize(VoicechatServerApi api) {
        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
                cleanup(event.getPlayer().getUniqueId());
            }
        }, Main.getPlugin());
    }
    
    private void cleanup(UUID uuid) {
        Recognizer rec = recognizers.remove(uuid);
        if (rec != null) rec.close();
        
        OpusDecoder dec = decoders.remove(uuid);
        if (dec != null) dec.close();
        
        partialResults.remove(uuid);
        lastSpellCast.remove(uuid);
    }
    
    @Override
    public long getBaseCooldown() { return 0; }
    
    @Override
    public String getId() { return "spellbook"; }
    
    @Override
    public String getName() { return ChatColor.GOLD + "Spellbook"; }
    
    @Override
    public List<String> getLore() {
        return List.of(
            "§9When held in main hand, your",
            "§9voice chat can be turned cast",
            "§9spells in real time.",
            "",
            "§9§obeangame"
        );
    }
    
    @Override
    public Map<String, Integer> getEnchantments() { return Map.of(); }
    
    @Override
    public Material getMaterial() { return Material.BOOK; }
    
    @Override
    public int getCustomModelData() { return 104; }
    
    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
    }
    
    @Override
    public int getMaxStackSize() { return 1; }

    @Override
    public boolean isInItemRotation() { return true; }

    @Override
    public ArmorTrim getArmorTrim(){
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    // private void broadcastMessage(Player player, String text) {
    //     StringBuilder partial = partialResults.get(player.getUniqueId());
    //     String fullMessage = (partial != null && partial.length() > 0) 
    //         ? partial.toString().trim() + " " + text 
    //         : text;
        
    //     if (fullMessage.trim().isEmpty()) return;
        
    //     String message = ChatColor.YELLOW + "📢 " + ChatColor.WHITE + player.getName() + 
    //                     ChatColor.GRAY + " says: " + ChatColor.WHITE + fullMessage;
        
    //     for (Player p : Bukkit.getOnlinePlayers()) {
    //         if (p.getWorld().equals(player.getWorld())) {
    //             p.sendMessage(message);
    //         }
    //     }
        
    //     // Bukkit.getLogger().info("[Speaker] " + player.getName() + ": " + fullMessage);
    // }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        
        if(Math.random() > 0.2) return;
        
        if(item.getType() != getMaterial()){
            item.setType(getMaterial());
        }
    }
}