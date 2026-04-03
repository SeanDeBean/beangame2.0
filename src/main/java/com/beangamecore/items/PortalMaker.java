package com.beangamecore.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.general.BG1sTickingI;
import com.beangamecore.items.type.general.BGResetableI;
import com.beangamecore.util.BlockCategories;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import com.beangamecore.Main;

public class PortalMaker extends BeangameItem implements BGRClickableI, BG1sTickingI, BGResetableI {
    private static Map<UUID, Long> portalmakerteleportcd = new HashMap<>();

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() == Material.AIR) return false;

        Player player = event.getPlayer();
        if (onCooldown(player.getUniqueId())) return false;
        
        applyCooldown(player.getUniqueId());
        Material type = clickedBlock.getType();
        
        if (type == Material.CRYING_OBSIDIAN) {
            portalmakerReset(player);
            return false;
        }
        
        if (BlockCategories.getFunctionalBlocks().contains(type)) return false;

        // Valid placement - handle portal creation
        int portalCount = getPlayerPortalCount(player);
        if (portalCount >= 2) portalmakerReset(player);
        
        portalmakerCreatePortal(player, clickedBlock.getLocation());
        return true;
    }
    
    private int getPlayerPortalCount(Player player) {
        int count = 0;
        NamespacedKey portalKey = new NamespacedKey(Main.getPlugin(), "portalmaker_beangame");
        NamespacedKey nameKey = new NamespacedKey(Main.getPlugin(), "name_beangame");
        
        for (World world : Bukkit.getServer().getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                PersistentDataContainer data = stand.getPersistentDataContainer();
                if (data.has(portalKey, PersistentDataType.BOOLEAN) && 
                    player.getDisplayName().equals(data.get(nameKey, PersistentDataType.STRING))) {
                    count++;
                }
            }
        }
        return count;
    }
    
    private void portalmakerCreatePortal(Player player, Location loc) {
        World world = loc.getWorld();
        loc.add(0.5, 1, 0.5);
        world.spawnParticle(Particle.ENCHANT, loc, 30);
        world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1, 0);

        ArmorStand storage = createPortalArmorStand(world, loc, player);
        loc.subtract(0, 0.5, 0);
        storePortalData(storage, loc, player);
        loc.getBlock().setType(Material.CRYING_OBSIDIAN);
    }
    
    private ArmorStand createPortalArmorStand(World world, Location loc, Player player) {
        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setBasePlate(false);
        stand.setCanPickupItems(false);
        stand.setCollidable(false);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.isSilent();
        stand.setAI(false);
        stand.setGravity(false);
        stand.setMarker(false);
        for(Player p : Bukkit.getOnlinePlayers()){
            p.hideEntity(Main.getPlugin(), stand);
        }
        return stand;
    }
    
    private void storePortalData(ArmorStand stand, Location loc, Player player) {
        PersistentDataContainer data = stand.getPersistentDataContainer();
        data.set(new NamespacedKey(Main.getPlugin(), "portalmaker_beangame"), PersistentDataType.BOOLEAN, true);
        data.set(new NamespacedKey(Main.getPlugin(), "material_beangame"), PersistentDataType.STRING, String.valueOf(loc.getBlock().getType()));
        data.set(new NamespacedKey(Main.getPlugin(), "coords_beangame"), PersistentDataType.INTEGER_ARRAY, 
                new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()});
        data.set(new NamespacedKey(Main.getPlugin(), "name_beangame"), PersistentDataType.STRING, player.getDisplayName());
    }

    public void portalmakerReset(Player player) {
        for (World world : Bukkit.getServer().getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (isPlayerPortal(stand, player)) {
                    restoreBlock(stand, world);
                    stand.remove();
                }
            }
        }
    }
    
    private boolean isPlayerPortal(ArmorStand stand, Player player) {
        PersistentDataContainer data = stand.getPersistentDataContainer();
        return data.has(new NamespacedKey(Main.getPlugin(), "portalmaker_beangame"), PersistentDataType.BOOLEAN) && 
               player.getDisplayName().equals(data.get(new NamespacedKey(Main.getPlugin(), "name_beangame"), PersistentDataType.STRING));
    }
    
    private void restoreBlock(ArmorStand stand, World world) {
        PersistentDataContainer data = stand.getPersistentDataContainer();
        int[] coords = data.get(new NamespacedKey(Main.getPlugin(), "coords_beangame"), PersistentDataType.INTEGER_ARRAY);
        String material = data.get(new NamespacedKey(Main.getPlugin(), "material_beangame"), PersistentDataType.STRING);
        world.getBlockAt(coords[0], coords[1], coords[2]).setType(Material.valueOf(material));
    }

    public void portalmakerTeleport(PlayerToggleSneakEvent event, ArmorStand armorStand) {
        Player player = event.getPlayer();
        if (player.isSneaking() || player.getGameMode().equals(GameMode.SPECTATOR)) return;
        if (portalmakerteleportcd.containsKey(player.getUniqueId()) && 
            portalmakerteleportcd.get(player.getUniqueId()) > System.currentTimeMillis()) return;
        
        portalmakerteleportcd.put(player.getUniqueId(), System.currentTimeMillis() + 500L);
        
        ArmorStand targetPortal = findTargetPortal(armorStand);
        if (targetPortal != null) {
            teleportToPortal(player, targetPortal);
        }
    }
    
    private ArmorStand findTargetPortal(ArmorStand sourcePortal) {
        String owner = sourcePortal.getPersistentDataContainer().get(
                new NamespacedKey(Main.getPlugin(), "name_beangame"), PersistentDataType.STRING);
        NamespacedKey portalKey = new NamespacedKey(Main.getPlugin(), "portalmaker_beangame");
        NamespacedKey nameKey = new NamespacedKey(Main.getPlugin(), "name_beangame");

        for (World world : Bukkit.getServer().getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (isTargetPortal(stand, portalKey, nameKey, owner, sourcePortal)) {
                    return stand;
                }
            }
        }
        return null;
    }

    private boolean isTargetPortal(ArmorStand stand, NamespacedKey portalKey, NamespacedKey nameKey, String owner,
            ArmorStand sourcePortal) {
        PersistentDataContainer data = stand.getPersistentDataContainer();
        return data.has(portalKey, PersistentDataType.BOOLEAN) &&
                owner.equals(data.get(nameKey, PersistentDataType.STRING)) &&
                stand != sourcePortal;
    }
    
    private void teleportToPortal(Player player, ArmorStand targetPortal) {
        PersistentDataContainer data = targetPortal.getPersistentDataContainer();
        int[] coords = data.get(new NamespacedKey(Main.getPlugin(), "coords_beangame"), PersistentDataType.INTEGER_ARRAY);
        
        Location playerLoc = player.getLocation();
        Location targetLoc = new Location(targetPortal.getWorld(), coords[0] + 0.5, coords[1] + 1, coords[2] + 0.5);
        targetLoc.setPitch(playerLoc.getPitch());
        targetLoc.setYaw(playerLoc.getYaw());
        
        player.teleport(targetLoc, TeleportCause.SPECTATE);
        targetLoc.getWorld().spawnParticle(Particle.FLAME, targetLoc, 30);
        targetLoc.getWorld().playSound(playerLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0);
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0);
    }

    @Override
    public void resetItem() {
        NamespacedKey portalKey = new NamespacedKey(Main.getPlugin(), "portalmaker_beangame");
        
        for (World world : Bukkit.getServer().getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (stand.getPersistentDataContainer().has(portalKey, PersistentDataType.BOOLEAN)) {
                    restoreBlock(stand, world);
                    stand.remove();
                }
            }
        }
    }

    @Override
    public void tick() {
        NamespacedKey portalKey = new NamespacedKey(Main.getPlugin(), "portalmaker_beangame");
        NamespacedKey nameKey = new NamespacedKey(Main.getPlugin(), "name_beangame");
        
        for (World world : Bukkit.getServer().getWorlds()) {
            for (ArmorStand stand1 : world.getEntitiesByClass(ArmorStand.class)) {
                if (!stand1.getPersistentDataContainer().has(portalKey, PersistentDataType.BOOLEAN)) continue;
                
                String owner = stand1.getPersistentDataContainer().get(nameKey, PersistentDataType.STRING);
                ArmorStand stand2 = findPortalPair(stand1, owner, portalKey, nameKey);
                
                if (stand2 != null) {
                    drawParticleLine(stand1.getLocation().add(0, 2, 0), stand1.getLocation());
                }
            }
        }
    }
    
    private ArmorStand findPortalPair(ArmorStand sourceStand, String owner, NamespacedKey portalKey,
            NamespacedKey nameKey) {
        for (World world : Bukkit.getServer().getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (isPortalPair(sourceStand, owner, portalKey, nameKey, stand)) {
                    return stand;
                }
            }
        }
        return null;
    }

    private boolean isPortalPair(ArmorStand sourceStand, String owner, NamespacedKey portalKey, NamespacedKey nameKey,
            ArmorStand stand) {
        PersistentDataContainer data = stand.getPersistentDataContainer();
        return data.has(portalKey, PersistentDataType.BOOLEAN) &&
                stand != sourceStand &&
                owner.equals(data.get(nameKey, PersistentDataType.STRING));
    }
    
    private void drawParticleLine(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector()).normalize().multiply(0.075);
        double distance = start.distance(end);
        Location current = start.clone();
        DustOptions dustOptions = new DustOptions(Color.fromRGB(160, 32, 240), 1);
        
        for (double i = 0; i < distance; i += 0.075) {
            current.add(direction);
            current.getWorld().spawnParticle(Particle.DUST, current, 1, dustOptions);
        }
    }

    @Override
    public long getBaseCooldown() { return 250L; }

    @Override
    public String getId() { return "portalmaker"; }

    @Override
    public boolean isInItemRotation() { return true; }

    @Override
    public CraftingRecipe getCraftingRecipe() { return null; }

    @Override
    public String getName() { return "§ePortal Maker"; }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click blocks to create portal",
            "§9anchors. Shift at one portal to",
            "§9teleport to the other. Right-click",
            "§9crying obsidian to reset portals.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() { return Map.of(); }

    @Override
    public Material getMaterial() { return Material.QUARTZ; }

    @Override
    public int getCustomModelData() { return 0; }

    @Override
    public List<ItemFlag> getItemFlags() { return List.of(); }

    @Override
    public ArmorTrim getArmorTrim() { return null; }

    @Override
    public Color getColor() { return null; }

    @Override
    public int getArmor() { return 0; }

    @Override
    public EquipmentSlotGroup getSlot() { return null; }

    @Override
    public int getMaxStackSize() { return 1; }
}
