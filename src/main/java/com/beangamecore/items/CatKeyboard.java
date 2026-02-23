package com.beangamecore.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Tameable;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.beangamecore.Main;
import com.beangamecore.commands.PvpToggleCommand;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class CatKeyboard extends BeangameItem implements BGRClickableI, BGLPTalismanI {
   
    private final Map<UUID, List<NoteType>> noteQueue = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> enemyNoteHits = new HashMap<>();

    public void clearNotes(){
        noteQueue.clear();
        enemyNoteHits.clear();
    }

    private static final int MAX_NOTES = 9;
    private static final int TETHER_REQUIREMENT = 2;

    public enum NoteType {
        BASS_DRUM(Material.OAK_PLANKS, "§6Bass Drum", "§7Creates waves that knocks back hit enemies"),
        SNARE_DRUM(Material.SAND, "§eSnare Drum", "§7Shoots out arrows on hit"),
        CLICK(Material.GLASS, "§bClick", "§7Deals armor durability damage"),
        GUITAR(Material.WHITE_WOOL, "§cGuitar", "§7Heals all nearby player on note hit"),
        BASS(Material.STONE, "§8Bass", "§7Laser beam that pierces and breaks blocks"),
        BELL(Material.GOLD_BLOCK, "§6Bell", "§7Clears all status effects"),
        CHIME(Material.PACKED_ICE, "§3Chime", "§7Immobilizes enemies"),
        XYLOPHONE(Material.BONE_BLOCK, "§fXylophone", "§7Summons sword-wielding strays"),
        FLUTE(Material.CLAY, "§aFlute", "§7Forces enemies to float with levitation"),
        DIDGERIDOO(Material.PUMPKIN, "§6Didgeridoo", "§7Projectiles pierce through walls"),
        BANJO(Material.HAY_BLOCK, "§eBanjo", "§7Bounces between enemies on hit"),
        PLING(Material.EMERALD_BLOCK, "§2Pling", "§7Bonus damage based on gold/emerald in inventory"),
        BIT(Material.NETHERITE_BLOCK, "§8Bit", "§7Deals true damage ignoring armor"),
        IRON_XYLOPHONE(Material.IRON_BLOCK, "§7Iron Xylophone", "§7Pulls enemies toward impact point"),
        COW_BELL(Material.SOUL_SAND, "§5Cow Bell", "§7Drains enemy health and regenerates user"),
        HARP(Material.DIRT, "§eHarp", "§7Default note"),
        BASALT(Material.BASALT, "§8Basalt", "§7Spawns fire and lava along projectile path"),
        AMETHYST(Material.AMETHYST_BLOCK, "§dAmethyst", "§7Applies glowing to nearby enemies"),
        SCULK(Material.SCULK, "§0Sculk", "§7Applies blindness to nearby enemies");

        private final Material triggerMaterial;
        private final String displayName;
        private final String description;

        NoteType(Material triggerMaterial, String displayName, String description){
            this.triggerMaterial = triggerMaterial;
            this.displayName = displayName;
            this.description = description;
        }

        public Material getTriggerMaterial() {
            return triggerMaterial;
        }

        public String getDisplayName(){
            return displayName;
        }

        public String getDescription(){
            return description;
        }

        public static NoteType fromMaterial(Material material){
            for(NoteType note : values()){
                if(note.getTriggerMaterial() == material){
                    return note;
                }
            }
            return HARP; // Default to HARP for unlisted materials
        }
    }

    public void onNoteBlockPlay(NotePlayEvent event){

        Block block = event.getBlock().getLocation().subtract(0, 1, 0).getBlock();

        Player player = findNearestPlayer(block.getLocation(), 10);
        if(player == null){
            return;
        }

        NoteType noteType = NoteType.fromMaterial(block.getType());
        if(noteType == null){
            return;
        }

        UUID playerUUID = player.getUniqueId();
        List<NoteType> notes = noteQueue.getOrDefault(playerUUID, new ArrayList<>());

        if(notes.size() >= MAX_NOTES){
            notes.remove(0);
        }
        notes.add(noteType);
        noteQueue.put(playerUUID, notes);

        createNoteAbsorptionEffect(event.getBlock().getLocation().add(0.5, 0, 0.5), player, noteType);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacy(noteType.getDisplayName() + " §7Absorbed! §8(" + notes.size() + "/" + MAX_NOTES + ")"));
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        NoteType noteType = NoteType.HARP;
        UUID playerUUID = player.getUniqueId();

        if(!player.isSneaking()){
            return;
        }

        List<NoteType> notes = noteQueue.getOrDefault(playerUUID, new ArrayList<>());
        if(notes.size() >= MAX_NOTES){
            notes.remove(0);
        }
        notes.add(noteType);
        noteQueue.put(playerUUID, notes);

        createNoteAbsorptionEffect(player.getLocation().subtract(0, 3, 0), player, noteType);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacy(noteType.getDisplayName() + " §7Absorbed! §8(" + notes.size() + "/" + MAX_NOTES + ")"));
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }

        List<NoteType> notes = noteQueue.getOrDefault(uuid, new ArrayList<>());
        if(notes.isEmpty()){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§cNo charges available!"));
            return false;
        }

        if(onCooldown(uuid)){
            return false;
        }
        applyCooldown(uuid);

        NoteType note = notes.remove(notes.size() - 1);
        noteQueue.put(uuid, notes);

        shootHarmonicEcho(player, note);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_PURREOW, 1f, 1.1f);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacy(note.getDisplayName() + " §7Note released! §8(" + notes.size() + "/" + MAX_NOTES + ")"));

        return true;
    }

    private void shootHarmonicEcho(Player player, NoteType note){
        Location startLoc = player.getEyeLocation().subtract(0, 0.3, 0);
        Vector direction = startLoc.getDirection();
        
        createMusicNoteProjectile(startLoc, note);

        // shoot projectile
        World world = player.getWorld();
        ArmorStand projectile = world.spawn(startLoc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setSmall(true);
            stand.setMarker(true);
            stand.setAI(false);
            stand.setGravity(false);
            stand.setInvulnerable(true);
        });
        for(Player online : Bukkit.getOnlinePlayers()){
            online.hideEntity(Main.getPlugin(), projectile);
        }

        projectile.setCustomName("Note: " + note.name());
        projectile.setCustomNameVisible(false);

        // movement task
        int[] ticks = {0};
        final int maxTicks = 100;
        boolean[] hit = {false};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if(hit[0] || ticks[0] >= maxTicks || !projectile.isValid()){
                projectile.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            Location newLoc = projectile.getLocation().add(direction.clone().multiply(0.8));
            projectile.teleport(newLoc);

            // Check for block hits
            if(newLoc.getBlock().getType().isSolid() && !note.equals(NoteType.DIDGERIDOO) && !note.equals(NoteType.BASS)){
                onProjectileBlockHit(projectile, newLoc.getBlock(), player, note);
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            createNoteTrail(projectile.getLocation(), note);

            for(Entity entity : projectile.getNearbyEntities(0.5, 0.5, 0.5)){
                if(entity instanceof LivingEntity target && target != player){
                    if(entity instanceof Player p && p.getGameMode().equals(GameMode.SPECTATOR)){
                        break;
                    }
                    hit[0] = true;
                    onProjectileHit(projectile, target, player, note);
                    break;
                }
            }

            applyTravelEffects(projectile.getLocation(), note, player);

            ticks[0]++;
        }, 0L, 1L).getTaskId();
    }

    private void applyTravelEffects(Location location, NoteType note, Player player) {
        World world = location.getWorld();
        
        switch (note) {
            case BASS_DRUM:
                // Shockwave effect - expanding ring (50% reduced particles)
                for (int i = 0; i < 2; i++) { // Reduced from 3 to 2 loops
                    double radius = i * 0.8;
                    for (int j = 0; j < 4; j++) { // Reduced from 8 to 4 particles per ring
                        double angle = 2 * Math.PI * j / 4;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location particleLoc = location.clone().add(x, 0, z);
                        world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0, 0, 0, 0.05);
                    }
                }
                break;
                
            case SNARE_DRUM:
                // Occasional additional projectile sparks
                if (System.currentTimeMillis() % 200 < 10) {
                    for (int i = 0; i < 2; i++) {
                        Vector offset = new Vector(
                            (Math.random() - 0.5) * 0.5,
                            (Math.random() - 0.5) * 0.5,
                            (Math.random() - 0.5) * 0.5
                        );
                        world.spawnParticle(Particle.CRIT, location.clone().add(offset), 1, 0, 0, 0, 0.1);
                    }
                }
                break;
                
            case CLICK:
                // Glass shatter particles
                world.spawnParticle(Particle.BLOCK_CRUMBLE, location, 2, 0.1, 0.1, 0.1, 
                                Material.GLASS.createBlockData());
                break;
                
            case GUITAR:
                // Healing notes - gentle music notes
                world.spawnParticle(Particle.NOTE, location, 1, 0.1, 0.1, 0.1, 0.5);
                break;
                
            case BASS:
                // Laser beam effect - electric sparks
                world.spawnParticle(Particle.ELECTRIC_SPARK, location, 2, 0.1, 0.1, 0.1, 0.05);
                
                // Break blocks in path if enabled
                if (location.getBlock().getType().isSolid() && location.getBlock().getType().getBlastResistance() < 10) {
                    location.getBlock().breakNaturally();
                    world.spawnParticle(Particle.BLOCK_CRUMBLE, location, 5, 0.2, 0.2, 0.2, 
                                    location.getBlock().getBlockData());
                }
                break;
                
            case BELL:
                // Bell particles - golden notes
                world.spawnParticle(Particle.NOTE, location, 1, 0.1, 0.1, 0.1, 1.0);
                break;
                
            case CHIME:
                // Ice crystals trailing
                world.spawnParticle(Particle.SNOWFLAKE, location, 3, 0.1, 0.1, 0.1, 0.05);
                break;
                
            case XYLOPHONE:
                // Bone particles trailing
                if (System.currentTimeMillis() % 100 < 10) {
                    world.spawnParticle(Particle.BLOCK_CRUMBLE, location, 1, 0.1, 0.1, 0.1, 
                                    Material.BONE_BLOCK.createBlockData());
                }
                break;
                
            case FLUTE:
                // Magical, ethereal particles
                world.spawnParticle(Particle.END_ROD, location, 2, 0.1, 0.1, 0.1, 0.03);
                world.spawnParticle(Particle.REVERSE_PORTAL, location, 1, 0.05, 0.05, 0.05, 0.01);
                break;
                
            case DIDGERIDOO:
                // Ghostly trail that passes through walls
                world.spawnParticle(Particle.SOUL, location, 1, 0.1, 0.1, 0.1, 0.05);
                break;
                
            case BANJO:
                // Bouncy, cheerful notes
                world.spawnParticle(Particle.NOTE, location, 1, 0.1, 0.1, 0.1, 0.8);
                break;
                
            case PLING:
                // Sparkly wealth particles
                world.spawnParticle(Particle.HAPPY_VILLAGER, location, 1, 0.1, 0.1, 0.1, 0.05);
                break;
                
            case BIT:
                // Digital, glitchy particles
                world.spawnParticle(Particle.ELECTRIC_SPARK, location, 1, 0.1, 0.1, 0.1, 0.1);
                break;
                
            case IRON_XYLOPHONE:
                // Metallic, magnetic particles
                world.spawnParticle(Particle.CRIT, location, 2, 0.1, 0.1, 0.1, 0.1);
                break;
                
            case COW_BELL:
                // Soul particles with health transfer effect
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 1, 0.1, 0.1, 0.1, 0.05);
                break;
                
            case HARP:
                // Echoing, copying effect - multiple notes
                for (int i = 0; i < 2; i++) {
                    Location offsetLoc = location.clone().add(
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3
                    );
                    world.spawnParticle(Particle.NOTE, offsetLoc, 1, 0, 0, 0, 0.3);
                }
                break;
                
            case BASALT:
                // Fire and lava trail
                world.spawnParticle(Particle.FLAME, location, 1, 0.1, 0.1, 0.1, 0.02);
                
                // Occasionally spawn fire on the ground
                if (System.currentTimeMillis() % 100 < 10) {
                    Location groundLoc = location.clone().subtract(0, 1, 0);
                    if (groundLoc.getBlock().getType().isSolid()) {
                        location.getBlock().setType(Material.FIRE);
                    }
                }
                break;
                
            case AMETHYST:
                // Glowing, crystalline particles
                world.spawnParticle(Particle.END_ROD, location, 1, 0.1, 0.1, 0.1, 0.05);
                break;
                
            case SCULK:
                // Dark, blindness particles
                world.spawnParticle(Particle.SQUID_INK, location, 1, 0.1, 0.1, 0.1, 0.05);
                break;
                
            default:
                // Default harp effect
                world.spawnParticle(Particle.NOTE, location, 1, 0.1, 0.1, 0.1, 0.3);
                break;
        }
    }


    private boolean applyNoteEffect(ArmorStand projectile, LivingEntity target, Player player, NoteType note){
        Location hitLoc = projectile.getLocation();
        switch (note) {
            case BASS_DRUM:
                target.getWorld().playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.8f);
                target.damage(3, player);

                boolean hasKBResistance = false;
                if(target instanceof Player){
                    Player pVictim = (Player) target;
                    hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                            pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
                }
                if (hasKBResistance) {
                    break;
                }
                target.setVelocity(target.getVelocity().add(new Vector(0, 0.5, 0)).add(player.getLocation().getDirection().multiply(-1.5)));
                break;
            
            case SNARE_DRUM:
                // Shoot additional projectiles on hit
                target.damage(4, player);
                for (int i = 0; i < 3; i++) {
                    shootSecondaryProjectile(hitLoc, player, note);
                }
                break;
        
            case CLICK:
                target.damage(1, player);
                if (target instanceof Player targetPlayer) {
                    damagePlayerArmor(targetPlayer, 5);
                }
                break;
            
            case GUITAR:
                healNearbyEntities(hitLoc, player, 4.0);
                break;
            
            case BASS:
                target.damage(4, player);
                // Already handled in travel effects
                break;
            
            case BELL:
                clearStatusEffects(hitLoc, player, 8.0);
                break;

            case CHIME:
                target.damage(2, player);
                if(target instanceof Player){
                    Cooldowns.setCooldown("immobilized", target.getUniqueId(), 1300L);
                }
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 0)); 
                target.setFreezeTicks(80);
                target.getWorld().spawnParticle(Particle.SNOWFLAKE, hitLoc, 20, 0.5, 0.5, 0.5, 0.1);
                break;
            
            case XYLOPHONE:
                target.damage(2, player);
                summonSkeleton(hitLoc, player, target); // Pass the target
                break;

            case FLUTE:
                target.damage(4, player);
                target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 0));
                break;

            case DIDGERIDOO:
                // Projectile continues through
                target.damage(4, player);
                return true;
            
            case BANJO:
                target.damage(4, player);
                bounceToNextTarget(target, hitLoc, player, note, 3);
                break;
            
            case PLING:
                double bonusDamage = calculatePlingDamage(player);
                target.damage(3 + bonusDamage, player);
                createWealthEffects(player, bonusDamage);
                target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, target.getLocation().add(0, 1, 0), 
                                  8, 0.5, 0.5, 0.5, 0.2);
                break;

            case BIT:
                // True damage ignoring armor
                if(PvpToggleCommand.pvp){
                    if (target instanceof Player targetPlayer) {
                        double health = targetPlayer.getHealth();
                        targetPlayer.setHealth(Math.max(1, health - 3));
                    } else {
                        target.damage(4, player);
                    }
                }
                break;
            
            case IRON_XYLOPHONE:
                target.damage(3, player);
                pullNearbyEntities(hitLoc, 6.0, 1.5); // Increased strength
                
                // Launch player upward and toward target
                Vector launchDirection = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                launchDirection.setY(0.7); // Upward boost

                if (player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                            player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7) {
                    break;
                }

                player.setVelocity(player.getVelocity().add(launchDirection.multiply(0.4)));
                
                // Visual effect for player launch
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.3, 0.3, 0.3, 0.1);
                break;

            case COW_BELL:
                double drainAmount = 3.0;
                if (target.getHealth() > drainAmount) {
                    target.damage(drainAmount, player);
                    player.setHealth(Math.min(player.getAttribute(Attribute.MAX_HEALTH).getValue(), player.getHealth() + drainAmount / 2));
                }
                break;

            case HARP:
                target.damage(3, player);
                break;
            
            case BASALT:
                target.damage(4, player);
                // Already handled in travel effects
                break;
            
            case AMETHYST:
                target.damage(3, player);
                for (Entity entity : hitLoc.getWorld().getNearbyEntities(hitLoc, 6.0, 6.0, 6.0)) {
                    if (entity instanceof LivingEntity living && !living.equals(player)) {
                        living.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));
                    }
                }
                break;
            
            case SCULK:
                target.damage(3, player);
                for (Entity entity : hitLoc.getWorld().getNearbyEntities(hitLoc, 6.0, 6.0, 6.0)) {
                    if (entity instanceof LivingEntity living && !living.equals(player)) {
                        living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                    }
                }
                break;

            default:
                target.damage(4, player);
                break;
        }
        return false;
    }


    private void onProjectileHit(ArmorStand projectile, LivingEntity target, Player player, NoteType note){
        Location hitLoc = projectile.getLocation();
        
        createNoteImpactEffect(hitLoc, note);

        // note specific effects
        boolean bool = applyNoteEffect(projectile, target, player, note);

        if(target instanceof LivingEntity){
            UUID targetUUID = target.getUniqueId();
            UUID playerUUID = player.getUniqueId();

            Map<UUID, Integer> hits = enemyNoteHits.getOrDefault(playerUUID, new HashMap<>());
            int hitCount = hits.getOrDefault(targetUUID, 0) + 1;
            hits.put(targetUUID, hitCount);
            enemyNoteHits.put(playerUUID, hits);

            if (hitCount >= TETHER_REQUIREMENT) {
                createAstralPillar(player, target, note);
                hits.remove(targetUUID); // reset counter
            }
        }

        if(!bool){
            projectile.remove();
        }
    }

    private void createAstralPillar(Player player, Entity target, NoteType note) {
        Location targetLoc = target.getLocation().clone();
        World world = player.getWorld();
        
        // Find a random location within 3 blocks of the victim that has air blocks
        Location spawnLocation = findValidAirSpawnLocation(targetLoc, 3.0);
        if (spawnLocation == null) {
            // If no valid air location found, don't spawn pillar
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§cNo space for Astral Pillar!"));
            return;
        }
        
        // Make final copies for use in the lambda
        final Location finalSpawnLocation = spawnLocation.clone();
        final Material finalBlockType = getRandomNoteMaterial();
        
        // Get the original block types (should be AIR)
        final Material originalBlock = finalSpawnLocation.getBlock().getType();
        final Material originalNoteBlock = finalSpawnLocation.clone().add(0, 1, 0).getBlock().getType();
        
        // Only proceed if both blocks are air (or water/lava for the note block)
        if (originalBlock != Material.AIR && originalBlock != Material.WATER && originalBlock != Material.LAVA) {
            return;
        }
        
        // Set the blocks
        finalSpawnLocation.getBlock().setType(finalBlockType);
        finalSpawnLocation.clone().add(0, 1, 0).getBlock().setType(Material.NOTE_BLOCK);
        
        // Spawn effects
        world.playSound(finalSpawnLocation, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        world.spawnParticle(Particle.END_ROD, finalSpawnLocation.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.REVERSE_PORTAL, finalSpawnLocation, 15, 0.3, 0.3, 0.3, 0.05);
        
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacy("§5§lASTRAL PILLAR §7summoned!"));
        
        // Play the note block sound
        world.playSound(finalSpawnLocation, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        
        // Despawn after 2.25 seconds
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            Block blockToRestore = finalSpawnLocation.getBlock();
            Block noteBlockToRestore = finalSpawnLocation.clone().add(0, 1, 0).getBlock();
            
            // Only restore if the blocks are still our pillar blocks
            if (blockToRestore.getType() == finalBlockType) {
                blockToRestore.setType(originalBlock);
            }
            if (noteBlockToRestore.getType() == Material.NOTE_BLOCK) {
                noteBlockToRestore.setType(originalNoteBlock);
            }
            
            // Despawn effects
            world.playSound(finalSpawnLocation, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.8f);
            world.spawnParticle(Particle.CLOUD, finalSpawnLocation.clone().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.1);
            world.spawnParticle(Particle.SMOKE, finalSpawnLocation, 10, 0.2, 0.2, 0.2, 0.05);
        }, 45L); // 2.25 seconds * 20 ticks
    }

    private Location findValidAirSpawnLocation(Location center, double radius) {
        
        // Try up to 15 random locations
        for (int i = 0; i < 15; i++) {
            // Generate random offset within the radius
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * radius;
            double xOffset = Math.cos(angle) * distance;
            double zOffset = Math.sin(angle) * distance;
            
            Location testLocation = center.clone().add(xOffset, 0, zOffset);
            
            // Check if both blocks are air (or water/lava for the note block)
            Block groundBlock = testLocation.getBlock();
            Block airBlock = testLocation.clone().add(0, 1, 0).getBlock();
            
            boolean isValidGround = groundBlock.getType() == Material.AIR || 
                                groundBlock.getType() == Material.WATER || 
                                groundBlock.getType() == Material.LAVA;
            
            boolean isValidAir = airBlock.getType() == Material.AIR || 
                                airBlock.getType() == Material.WATER || 
                                airBlock.getType() == Material.LAVA;
            
            // Also check that there's solid ground below the pillar
            Block groundBelow = testLocation.clone().subtract(0, 1, 0).getBlock();
            boolean hasSolidGround = groundBelow.getType().isSolid();
            
            if (isValidGround && isValidAir && hasSolidGround) {
                return testLocation;
            }
        }
        
        // If no valid air location found after 15 attempts, return null
        return null;
    }

    public static NoteType getRandomNoteType() {
        Random random = new Random();
        NoteType[] noteTypes = NoteType.values();
        return noteTypes[random.nextInt(noteTypes.length)];
    }

    public static Material getRandomNoteMaterial() {
        Random random = new Random();
        NoteType[] noteTypes = NoteType.values();
        NoteType randomNote = noteTypes[random.nextInt(noteTypes.length)];
        return randomNote.getTriggerMaterial();
    }

    private void onProjectileBlockHit(ArmorStand projectile, Block block, Player player, NoteType note) {
        Location hitLoc = projectile.getLocation();
        
        createNoteImpactEffect(hitLoc, note);
        
        // Handle GUITAR and BELL effects on block hit
        switch (note) {
            case GUITAR:
                healNearbyEntities(hitLoc, player, 4.0);
                break;
            case BELL:
                clearStatusEffects(hitLoc, player, 8.0);
                break;
            default:
                break;
        }
        
        projectile.remove();
    }


    private void bounceToNextTarget(Entity currentTarget, Location hitLoc, Player player, NoteType note, int maxBounces) {
        World world = hitLoc.getWorld();
        List<Entity> alreadyHit = new ArrayList<>();
        alreadyHit.add(currentTarget);
        
        // Increased range from 8 to 11 blocks
        double bounceRange = 11.0;
        
        // Visual effect for the bounce
        world.spawnParticle(Particle.NOTE, hitLoc, 10, 0.3, 0.3, 0.3, 1.0);
        world.playSound(hitLoc, Sound.BLOCK_NOTE_BLOCK_BANJO, 0.8f, 1.2f);
        
        // Use arrays for mutable variables
        int[] bouncesLeft = {maxBounces};
        Location[] currentLocation = {hitLoc.clone()};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (bouncesLeft[0] <= 0) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }
            
            // Find nearest enemy that hasn't been hit with increased range
            Entity nextTarget = null;
            double closestDistance = bounceRange * bounceRange;
            
            for (Entity entity : world.getNearbyEntities(currentLocation[0], bounceRange, bounceRange, bounceRange)) {
                if (entity instanceof LivingEntity living && !alreadyHit.contains(living) && 
                    !living.equals(player) && !living.isDead()) {
                    double distance = living.getLocation().distanceSquared(currentLocation[0]);
                    if (distance < closestDistance) {
                        nextTarget = living;
                        closestDistance = distance;
                    }
                }
            }
            
            if (nextTarget == null) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }
            
            // Rest of the method remains the same...
            // Bounce to next target
            Location targetLoc = nextTarget.getLocation().add(0, 1, 0);
            
            // Create bounce effect
            createBounceEffect(currentLocation[0], targetLoc);
            
            // Damage the target
            ((LivingEntity) nextTarget).damage(4.0, player);
            alreadyHit.add(nextTarget);
            
            // Update position and decrease bounces
            currentLocation[0] = targetLoc.clone();
            bouncesLeft[0]--;
            
            // Sound effect for each bounce
            world.playSound(targetLoc, Sound.BLOCK_NOTE_BLOCK_BANJO, 0.6f, 1.0f + (maxBounces - bouncesLeft[0]) * 0.1f);
        }, 5L, 5L).getTaskId();
    }

    private void createBounceEffect(Location from, Location to) {
        World world = from.getWorld();
        Vector direction = to.toVector().subtract(from.toVector());

        // Create arc particles
        for (int i = 0; i <= 10; i++) {
            double progress = (double) i / 10;
            double height = Math.sin(progress * Math.PI) * 1.5;
            Location particleLoc = from.clone().add(direction.clone().multiply(progress));
            particleLoc.add(0, height, 0);
            
            world.spawnParticle(Particle.CRIT, particleLoc, 1, 0, 0, 0, 0.1);
        }
    }

    private void summonSkeleton(Location location, Player player, LivingEntity target) {
        World world = location.getWorld();
        
        Stray skeleton = (Stray) world.spawnEntity(location, EntityType.STRAY);
        
        skeleton.setTarget(null);
        
        skeleton.setMetadata("summoned_by", new FixedMetadataValue(Main.getPlugin(), player.getUniqueId().toString()));
        skeleton.setMetadata("target_entity", new FixedMetadataValue(Main.getPlugin(), target != null ? target.getUniqueId().toString() : "none"));
        
        skeleton.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
        skeleton.getEquipment().setHelmetDropChance(0.0f);
        
        // 30/70 chance for bow or stone sword
        if (Math.random() > 0.7) {
            skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
        } else {
            skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
        }
        skeleton.getEquipment().setItemInMainHandDropChance(0.0f);
        
        int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (!skeleton.isValid() || skeleton.isDead()) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }
            
            // only set target if we have a valid target entity
            if (target != null && target.isValid() && !target.isDead()) {
                // check if current target is the player or wrong target
                LivingEntity currentTarget = skeleton.getTarget();
                if (currentTarget == null || !currentTarget.equals(target)) {
                    skeleton.setTarget(target);
                }
            }
            
            // prevent targeting the player under any circumstances
            if (skeleton.getTarget() instanceof Player) {
                Player currentTarget = (Player) skeleton.getTarget();
                if (currentTarget.equals(player)) {
                    skeleton.setTarget(null);
                }
            }
        }, 5L, 10L).getTaskId();
        
        // visual effects
        world.spawnParticle(Particle.SMOKE, location, 15, 0.5, 0.5, 0.5, 0.1);
        world.playSound(location, Sound.ENTITY_SKELETON_AMBIENT, 1.0f, 0.8f);
        
        // despawn after 16 seconds
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (skeleton.isValid()) {
                world.spawnParticle(Particle.SMOKE, skeleton.getLocation(), 10, 0.3, 0.3, 0.3, 0.1);
                skeleton.remove();
            }
        }, 320L); // 16 seconds * 20 ticks
    }


    private void clearStatusEffects(Location location, Player player, double radius) {
        World world = location.getWorld();
        
        // Visual effect - expanding ring of purification
        for (int i = 0; i < 16; i++) {
            double angle = 2 * Math.PI * i / 16;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location particleLoc = location.clone().add(x, 0, z);
            
            world.spawnParticle(Particle.HEART, particleLoc, 2, 0.1, 0.1, 0.1, 0.05);
        }
        
        // Clear effects for nearby players (including caster)
        for (Entity entity : world.getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof Player nearbyPlayer) {
                for (PotionEffect effect : nearbyPlayer.getActivePotionEffects()) {
                    // Don't remove positive effects from the caster
                    if (nearbyPlayer.equals(player) && isPositiveEffect(effect.getType())) {
                        continue;
                    }
                    nearbyPlayer.removePotionEffect(effect.getType());
                }
                
                // Visual feedback
                world.spawnParticle(Particle.HAPPY_VILLAGER, nearbyPlayer.getLocation().add(0, 1, 0), 
                                5, 0.3, 0.3, 0.3, 0.1);
            }
        }
        
        world.playSound(location, Sound.BLOCK_BELL_USE, 1.0f, 1.5f);
    }

    private boolean isPositiveEffect(PotionEffectType type) {
        return type == PotionEffectType.SPEED || type == PotionEffectType.HASTE ||
            type == PotionEffectType.STRENGTH || type == PotionEffectType.INSTANT_HEALTH ||
            type == PotionEffectType.JUMP_BOOST || type == PotionEffectType.REGENERATION ||
            type == PotionEffectType.RESISTANCE || type == PotionEffectType.FIRE_RESISTANCE ||
            type == PotionEffectType.WATER_BREATHING || type == PotionEffectType.INVISIBILITY ||
            type == PotionEffectType.NIGHT_VISION || type == PotionEffectType.HEALTH_BOOST ||
            type == PotionEffectType.ABSORPTION || type == PotionEffectType.SATURATION ||
            type == PotionEffectType.LUCK || type == PotionEffectType.SLOW_FALLING ||
            type == PotionEffectType.CONDUIT_POWER || type == PotionEffectType.DOLPHINS_GRACE;
    }

    private void healNearbyEntities(Location location, Player player, double radius) {
        World world = location.getWorld();
        
        // Visual effect - swirling healing particles
        for (int i = 0; i < 12; i++) {
            double angle = 2 * Math.PI * i / 12;
            double x = Math.cos(angle) * radius * 0.8;
            double z = Math.sin(angle) * radius * 0.8;
            Location particleLoc = location.clone().add(x, 0.5, z);
            
            world.spawnParticle(Particle.HEART, particleLoc, 1, 0, 0.1, 0, 0.05);
        }
        
        // Heal nearby entities (players and friendly mobs)
        for (Entity entity : world.getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof LivingEntity living) {
                // Only heal players and tameable mobs owned by the player
                if (entity instanceof Player || 
                    (entity instanceof Tameable tameable && tameable.isTamed() && tameable.getOwner() == player)) {
                    
                    double maxHealth = living.getAttribute(Attribute.MAX_HEALTH).getValue();
                    double newHealth = Math.min(maxHealth, living.getHealth() + 3.0);
                    living.setHealth(newHealth);
                    
                    // Visual feedback
                    world.spawnParticle(Particle.HEART, living.getLocation().add(0, 1, 0), 
                                    3, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }
        
        world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.8f);
    }

    private EquipmentSlot[] getValidEquipmentSlots() {
        return new EquipmentSlot[] {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET,
                EquipmentSlot.HAND,
                EquipmentSlot.OFF_HAND
        };
    }

    private void processVictimEquipmentSlot(LivingEntity victim, EquipmentSlot slot, int damage) {
        ItemStack equipment = victim.getEquipment().getItem(slot);

        if (equipment == null)
            return;
        if (equipment.getType().getMaxDurability() <= 0)
            return;

        ItemStack copy = equipment.clone();
        ItemMeta meta = copy.getItemMeta();

        if (!(meta instanceof Damageable damageable))
            return;
        if (meta.isUnbreakable())
            return;

        int newDamage = damageable.getDamage() + damage;
        damageable.setDamage(newDamage);
        copy.setItemMeta(meta);

        if (newDamage >= copy.getType().getMaxDurability()) {
            victim.getEquipment().setItem(slot, null);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        } else {
            victim.getEquipment().setItem(slot, copy);
        }
    }

    private void damagePlayerArmor(Player target, int damageAmount) {
        
        EquipmentSlot[] validSlots = getValidEquipmentSlots();

        for (EquipmentSlot slot : validSlots) {
            processVictimEquipmentSlot(target, slot, damageAmount);
        }
        
        // Visual and sound effects
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 
                                    10, 0.3, 0.3, 0.3, 0.2);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.5f);
    }

    private void shootSecondaryProjectile(Location location, Player player, NoteType note) {
        World world = location.getWorld();
        
        // Shoot 3 arrows in a spread pattern
        for (int i = 0; i < 3; i++) {
            Arrow arrow = world.spawnArrow(location, player.getLocation().getDirection(), 1.2f, 12.0f);
            arrow.setShooter(player);
            arrow.setDamage(3.0);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED); // Cannot be picked up
            
            // Add slight spread
            Vector spread = new Vector(
                (Math.random() - 0.5) * 0.3,
                (Math.random() - 0.5) * 0.3,
                (Math.random() - 0.5) * 0.3
            );
            arrow.setVelocity(arrow.getVelocity().add(spread));
            
            // Visual effect
            world.spawnParticle(Particle.CRIT, location, 3, 0.1, 0.1, 0.1, 0.05);
            
            // Despawn after 2 seconds
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (arrow.isValid()) {
                    arrow.remove();
                }
            }, 40L); // 2 seconds * 20 ticks
        }
        
        world.playSound(location, Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.2f);
    }


    private double calculatePlingDamage(Player player) {
        int goldCount = 0;
        int emeraldCount = 0;
        
        // Count gold and emerald items
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                if (item.getType() == Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                } else if (item.getType() == Material.EMERALD) {
                    emeraldCount += item.getAmount();
                }
            }
        }
        
        // Calculate total "wealth units" - 9 stacks of either gives full damage
        int totalWealth = goldCount + emeraldCount;
        double bonusDamage = Math.min(16.0, totalWealth * (16.0 / 576)); // 576 = 9 stacks * 64
        
        return bonusDamage;
    }

    private void createWealthEffects(Player player, double bonusDamage) {
        if (bonusDamage <= 0) return;
        
        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Simple particle effect
        world.spawnParticle(Particle.HAPPY_VILLAGER, loc, 5, 0.3, 0.3, 0.3, 0.1);
        
        // Simple sound effect
        world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 1.2f);
        
        // Simple action bar message
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacy("§2Wealth Bonus: §a+" + String.format("%.1f", bonusDamage) + " damage"));
    }

    private void pullNearbyEntities(Location center, double radius, double strengthMultiplier) {
        World world = center.getWorld();
        double radiusSquared = radius * radius;
        double baseStrength = 0.8 * strengthMultiplier;
        
        // Visual effect - swirling particles moving inward
        for (int i = 0; i < 30; i++) {
            double angle = 2 * Math.PI * i / 30;
            double distance = radius * 0.8;
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;
            
            Location particleLoc = center.clone().add(x, 0, z);
            Vector direction = center.toVector().subtract(particleLoc.toVector()).normalize().multiply(0.3);
            
            world.spawnParticle(Particle.CRIT, particleLoc, 1, 
                            direction.getX(), direction.getY(), direction.getZ(), 0.5);
        }
        
        // Sound effect - vacuum/whooshing sound
        world.playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.7f);
        world.playSound(center, Sound.BLOCK_PORTAL_AMBIENT, 0.6f, 1.5f);
        
        // Pull all nearby entities toward the center
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.isDead()) {
                // Calculate direction to center
                Vector direction = center.toVector().subtract(entity.getLocation().toVector());
                double distanceSquared = direction.lengthSquared();
                
                if (distanceSquared > 1.0 && distanceSquared <= radiusSquared) {
                    // Normalize and scale force based on distance (stronger when farther)
                    direction.normalize();
                    double force = baseStrength * (1 - (Math.sqrt(distanceSquared) / radius));
                    
                    // Apply velocity toward center
                    Vector currentVelocity = entity.getVelocity();
                    Vector pullVelocity = direction.multiply(force);
                    
                    // Preserve some of the existing Y velocity to prevent excessive falling
                    pullVelocity.setY(pullVelocity.getY() * 0.5 + currentVelocity.getY() * 0.3);
                    
                    boolean hasKBResistance = false;
                    if(entity instanceof Player){
                        Player pVictim = (Player) entity;
                        hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                                pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
                    }
                    if (hasKBResistance) {
                        continue;
                    }

                    entity.setVelocity(pullVelocity);
                    
                    // Visual effect on entity being pulled
                    if (entity instanceof Player) {
                        world.spawnParticle(Particle.ANGRY_VILLAGER, entity.getLocation().add(0, 1, 0), 
                                        3, 0.2, 0.2, 0.2, 0.02);
                    } else {
                        world.spawnParticle(Particle.SMOKE, entity.getLocation().add(0, 0.5, 0), 
                                        5, 0.2, 0.2, 0.2, 0.01);
                    }
                }
            }
        }
        
        // Center explosion effect
        world.spawnParticle(Particle.EXPLOSION, center, 15, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.CRIT, center, 20, 1.0, 1.0, 1.0, 0.2);
        
        // Delayed sound effect for the "impact" at center
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.8f);
        }, 5L);
    }

    private void createNoteAbsorptionEffect(Location location, Player player, NoteType note) {
        List<NoteType> notes = noteQueue.getOrDefault(player.getUniqueId(), new ArrayList<>());
        boolean isFullyCharged = notes.size() >= MAX_NOTES;
        
        // beam effect to player (also reduced when fully charged)
        if (!isFullyCharged) {
            int particleCount = isFullyCharged ? 3 : 15; // 20x less particles when fully charged
            float particleSize = isFullyCharged ? 0.1f : 0.5f; // 5x smaller particles when fully charged
            location.getWorld().spawnParticle(Particle.NOTE, location.add(0, 1, 0), particleCount, 0.3, 0.3, 0.3, particleSize);
            location.getWorld().playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 0.4f, 1.2f);
            createBeamEffect(location, player.getEyeLocation().subtract(0, 1, 0), getNoteColor(note));
        }
    }
    
    private void createMusicNoteProjectile(Location location, NoteType note) {
        location.getWorld().spawnParticle(Particle.NOTE, location, 7, 0.1, 0.1, 0.1, 1.0);
        location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.5f);
    }
    
    private void createNoteTrail(Location location, NoteType note) {
        location.getWorld().spawnParticle(Particle.NOTE, location, 2, 0.1, 0.1, 0.1, 0.05);
        
        // note-specific trail effects
        switch (note) {
            case BASALT:
                location.getWorld().spawnParticle(Particle.FLAME, location, 2, 0.1, 0.1, 0.1, 0.01);
                if (location.getBlock().getType() == Material.AIR) {
                    location.getBlock().setType(Material.FIRE);
                }
                break;
            case BASS:
                location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 3, 0.1, 0.1, 0.1, 0.05);
                break;
            case AMETHYST:
                location.getWorld().spawnParticle(Particle.END_ROD, location, 2, 0.1, 0.1, 0.1, 0.02);
                break;
            default:
                break;
        }
    }
    
    private void createNoteImpactEffect(Location location, NoteType note) {
        location.getWorld().spawnParticle(Particle.NOTE, location, 30, 0.5, 0.5, 0.5, 1.0);
        location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 0.8f);
        
        // note-specific impact effects
        switch (note) {
            case BASS_DRUM:
                location.getWorld().spawnParticle(Particle.EXPLOSION, location, 10, 0.5, 0.5, 0.5, 0.1);
                break;
            case PLING:
                location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 15, 0.5, 0.5, 0.5, 0.1);
                break;
            case SCULK:
                location.getWorld().spawnParticle(Particle.SQUID_INK, location, 20, 0.5, 0.5, 0.5, 0.1);
                break;
            default:
                break;
        }
    }
    
    private void createBeamEffect(Location from, Location to, Color color) {
        World world = from.getWorld();
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = from.distance(to);
        int particles = (int) (distance * 10);
        
        // Create dust options for different particle effects
        Particle.DustOptions mainBeamDust = new Particle.DustOptions(color, 1.5f);
        Color coreColor = color;
        Particle.DustOptions coreBeamDust = new Particle.DustOptions(coreColor, 0.8f);
        
        // Spawn particles along the beam
        for (int i = 0; i <= particles; i++) {
            double progress = (double) i / particles;
            Location particleLoc = from.clone().add(direction.clone().multiply(progress));
            
            // Main beam particles
            world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1, mainBeamDust);
            
            // Core beam particles (more concentrated)
            if (i % 3 == 0) {
                world.spawnParticle(Particle.DUST, particleLoc, 1, 0.05, 0.05, 0.05, coreBeamDust);
            }
            
            // Add some sparkle particles occasionally
            if (i % 7 == 0) {
                world.spawnParticle(Particle.FIREWORK, particleLoc, 1, 0.05, 0.05, 0.05, 0);
            }
        }
        
        // Endpoint effects
        createBeamEndpointEffect(from, color);
        createBeamEndpointEffect(to, color);
        
        // Sound effects
        float volume = (float) Math.min(1.0, 0.3 + (distance / 20.0));
        float pitch = (float) (1.8 - (distance / 30.0));
        
        world.playSound(from, Sound.BLOCK_BEACON_ACTIVATE, volume, pitch);
        world.playSound(to, Sound.BLOCK_BEACON_ACTIVATE, volume, pitch);
    }

    private void createBeamEndpointEffect(Location location, Color color) {
        World world = location.getWorld();
        
        // Create dust options
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.2f);
        Color brightColor = color;
        Particle.DustOptions brightDustOptions = new Particle.DustOptions(brightColor, 0.8f);
        
        // Spawn particles in a sphere pattern
        for (int i = 0; i < 20; i++) {
            double angle = 2 * Math.PI * i / 20;
            double radius = 0.5;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = location.clone().add(x, 0, z);
            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, dustOptions);
            
            // Inner circle
            if (i % 2 == 0) {
                Location innerLoc = location.clone().add(x * 0.3, 0, z * 0.3);
                world.spawnParticle(Particle.DUST, innerLoc, 1, 0, 0, 0, brightDustOptions);
            }
        }
        
        // Vertical circle
        for (int i = 0; i < 12; i++) {
            double angle = 2 * Math.PI * i / 12;
            double radius = 0.4;
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;
            
            Location particleLoc = location.clone().add(x, y, 0);
            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, dustOptions);
        }
        
        // Sparkle effects
        world.spawnParticle(Particle.FIREWORK, location, 10, 0.3, 0.3, 0.3, 0.1);
        world.spawnParticle(Particle.END_ROD, location, 5, 0.2, 0.2, 0.2, 0.05);
    }
    
    private Color getNoteColor(NoteType note) {
        switch (note) {
            case BASS_DRUM: return Color.fromRGB(150, 75, 0);
            case SNARE_DRUM: return Color.fromRGB(210, 180, 140);
            case CLICK: return Color.fromRGB(150, 200, 255);
            case GUITAR: return Color.fromRGB(255, 50, 50);
            case BASS: return Color.fromRGB(50, 50, 50);
            case BELL: return Color.fromRGB(255, 215, 0);
            case CHIME: return Color.fromRGB(100, 200, 255);
            case XYLOPHONE: return Color.fromRGB(240, 240, 240);
            case FLUTE: return Color.fromRGB(100, 200, 100);
            case DIDGERIDOO: return Color.fromRGB(255, 140, 0);
            case BANJO: return Color.fromRGB(210, 180, 140);
            case PLING: return Color.fromRGB(0, 200, 0);
            case BIT: return Color.fromRGB(50, 50, 70);
            case IRON_XYLOPHONE: return Color.fromRGB(180, 180, 180);
            case COW_BELL: return Color.fromRGB(100, 70, 50);
            case HARP: return Color.fromRGB(210, 180, 140);
            case BASALT: return Color.fromRGB(70, 70, 70);
            case AMETHYST: return Color.fromRGB(150, 50, 200);
            case SCULK: return Color.fromRGB(20, 30, 40);
            default: return Color.WHITE;
        }
    }


    private Player findNearestPlayer(Location location, double radius) {
        Player nearest = null;
        double nearestDistance = radius * radius;
        
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof Player player) {
                double distanceSquared = entity.getLocation().distanceSquared(location);
                if (distanceSquared < nearestDistance && isCarrying(player)) {
                    nearest = player;
                    nearestDistance = distanceSquared;
                }
            }
        }
        
        return nearest;
    }

    private Boolean isCarrying(Player player){
        for(ItemStack item : player.getInventory().getContents()){
            if(item != null && !item.getType().equals(Material.AIR)){
                if(ItemNBT.hasBeanGameTag(item) && ItemNBT.isBeanGame(item, getKey())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public long getBaseCooldown() {
        return 750;
    }

    @Override
    public String getId() {
        return "catkeyboard";
    }

    @Override
    public String getName() {
        return "§dCat Keyboard";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§dAbsorbs nearby note block sounds and",
            "§dstores them as charges. Right-click to",
            "§dfire stored notes as projectiles with",
            "§dunique effects. Chain hits to summon",
            "§dtemporary Astral Pillars.",
            "",
            "§aSupport",
            "§dOn Hit Extender",
            "§9§obeangame"
        );
    }

    @Override
    public Material getMaterial() {
        return Material.STONE_PICKAXE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot() {
        return null;
    }
}

