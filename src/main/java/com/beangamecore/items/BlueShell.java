package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.commands.PvpToggleCommand;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.ItemNBT;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BlueShell extends BeangameItem implements BGRClickableI {
    
    private static List<UUID> protectedList = new ArrayList<>();

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) { // this gets called when a player right
                                                                              // clicks with a blue shell
        // cooldown
        Player player = event.getPlayer(); // this player right clicked
        UUID uuid = player.getUniqueId(); // uuid is the right clicking player's uuid
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        // item event
        /*
         * On right click, target a player as victim of the shell
         * -> victim is not on the protected player list
         * -> victim is not the user of the item
         * Go to the victim, damage them and play animation
         * 
         * Add the victim to a list of protected players
         * 
         * delay
         * remove the victim from the list of protected players
         */

        Player target = selectTargetPlayer(uuid);

        // we have a target at this point
        if (target == null || target.isDead()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§9No target found!"));
            return false;
        }

        UUID tUuid = target.getUniqueId();
        protectedList.add(tUuid);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                protectedList.remove(tUuid);
            }
        }, 260L); // 13 seconds
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§9Blue shell sent at " + target.getDisplayName() + "!"));
        applyCooldown(uuid);
        shell(player, target);
        return true;
    }

    private Player selectTargetPlayer(UUID userUuid) {
        Player target = null;
        Integer maxNumItems = 0;
        // loops all online players (spectators and players)
        for (Player potentialTarget : Bukkit.getOnlinePlayers()) {
            UUID potentialTargetUuid = potentialTarget.getUniqueId();
            // only passes with players in game that are not the user
            if (isEligibleTarget(potentialTarget, potentialTargetUuid, userUuid)) {
                // confirms that potentialTarget is not on the protected list, and checks if
                // they have the most items
                Integer count = countItems(potentialTarget);
                if (!protectedList.contains(potentialTargetUuid) && count > maxNumItems) {
                    maxNumItems = count;
                    target = potentialTarget;
                }
            }
        }
        return target;
    }

    private int getItemLead(Player target) {
        int targetCount = countItems(target);

        int total = 0;
        int players = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(target)) continue;
            if (!isEligibleTarget(p, p.getUniqueId(), target.getUniqueId())) continue;

            total += countItems(p);
            players++;
        }

        if (players == 0) {
            return 0; // no one else in the game, no lead
        }

        double average = (double) total / players;
        return Math.max(0, (int) Math.floor(targetCount - average));
    }

    private boolean isEligibleTarget(Player potentialTarget, UUID potentialTargetUuid, UUID userUuid) {
        return !potentialTarget.getGameMode().equals(GameMode.SPECTATOR)
                && !potentialTarget.getGameMode().equals(GameMode.CREATIVE)
                && !potentialTargetUuid.equals(userUuid);
    }

    int aboveHead = 5;
    int damage = 4;

    private void shell(Player owner, Player victim){
        World world = victim.getWorld();
        Location vlocmax = victim.getEyeLocation().add(0, aboveHead, 0);
        ItemDisplay shell = (ItemDisplay) world.spawnEntity(vlocmax, EntityType.ITEM_DISPLAY);
        shell.setTeleportDuration(1);
        ItemStack shellItem = new ItemStack(getMaterial());
        ItemMeta shellMeta = shellItem.getItemMeta();
        shellMeta.setCustomModelData(getCustomModelData());
        shellItem.setItemMeta(shellMeta);
        shell.setItemStack(shellItem);
        shell.setDisplayWidth(2);
        shell.setDisplayHeight(2);
        
        world.spawnParticle(Particle.SPLASH, victim.getEyeLocation().add(0, aboveHead, 0), 16);
        world.spawnParticle(Particle.SONIC_BOOM, victim.getEyeLocation().add(0, aboveHead, 0), 4);
        world.playSound(vlocmax, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1, 1);

        AtomicInteger j = new AtomicInteger();
        j.set(1);

        // keeps the shell above the target from ticks 1 -> 27
        for(int i = 1; i <= 27; i++){
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                public void run(){
                    shell.teleport(victim.getEyeLocation().add(0, aboveHead, 0));
                }
            }, i); // delay is i ticks
        }

        // teleports the shell downwords during ticks 27->31
        for(int i = 28; i <= 31; i++){
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                public void run(){
                    shell.teleport(victim.getEyeLocation().add(0, aboveHead - ((aboveHead/4) * j.get()), 0));
                    j.set(j.get() + 1);
                }
            }, i); // delay is i ticks
        }

        /*
         * explosion particles
         * despawn the shell
         * sound effect
         * damage the victim
         */
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                world.spawnParticle(Particle.EXPLOSION, victim.getEyeLocation(), 3);
                world.playSound(victim.getEyeLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 1);
                shell.remove();
                victim.damage(1, owner);

                // base damage
                double totalDamage = damage;

                // bonus damage: +2 per 3 item lead
                int lead = getItemLead(victim);
                int bonusHearts = lead / 3; // integer division
                totalDamage += bonusHearts * 2; // 2 HP = 1 heart

                double newHealth = victim.getHealth() - totalDamage;
                boolean killFlag = false;
                if (newHealth < 1) {
                    newHealth = 1.0;
                    killFlag = true;
                }

                if (PvpToggleCommand.pvp) {
                    victim.setHealth(newHealth);
                    if (killFlag) {
                        DamageSource source = DamageSource.builder(DamageType.MAGIC)
                            .withCausingEntity((Entity) owner)
                            .build();
                        victim.damage(40, source);
                    }
                }
            }
        }, 32); // delay is 32 ticks

        
        // 16 ticks growing
        // 10 ticks constant
        // 4 ticks for falling
    }

    private Integer countItems(Player player) {
        Integer count = 0;
        for(ItemStack item : player.getInventory().getContents()){
            if(item == null){
                continue;
            }
            if(ItemNBT.hasBeanGameTag(item) && !(BeangameItemRegistry.getRaw(ItemNBT.getBeanGame(item)) instanceof BeangameSoftItem)){
                count++;
            }
        }
        return count;
    }

    @Override
    public long getBaseCooldown() {
        return 27000L;
    }

    @Override
    public String getId() {
        return "blueshell";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§9Blue Shell";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to target the player with",
            "§9the most beangame items. A blue shell",
            "§9will track them and explode after 1.6",
            "§9seconds, dealing bonus damage based on",
            "§9their item lead over other players.",
            "",
            "§9Castable",
            "§dOn Hit Extender",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.BLUE_WOOL;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }

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
    public int getMaxStackSize() {
        return 1;
    }
}

