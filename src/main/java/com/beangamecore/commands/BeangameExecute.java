package com.beangamecore.commands;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.*;
import com.beangamecore.items.type.damage.BGDamageArmorI;
import com.beangamecore.items.type.damage.BGDamageHeldI;
import com.beangamecore.items.type.damage.BGDamageInvI;
import com.beangamecore.items.type.damage.BGLateDamageInvI;
import com.beangamecore.items.type.damage.entity.*;
import com.beangamecore.items.type.death.BGDeathArmorI;
import com.beangamecore.items.type.death.BGDeathHeldI;
import com.beangamecore.items.type.death.BGDeathInvI;
import com.beangamecore.items.type.death.BGGlobalDeath;
import com.beangamecore.items.type.talisman.BGTalismanI;
import com.beangamecore.registry.BeangameItemRegistry;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
public class BeangameExecute implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender.hasPermission("bg.use")){
            // /bgexec <item> <player 1> [player 2]
            if(args.length < 2){
                TextComponent c = new TextComponent("Usage: /bgexec <item> <player 1> [player 2] [bypass cooldown]");
                c.setColor(ChatColor.RED.asBungee());
                commandSender.spigot().sendMessage(c);
                return true;
            }
            if(args[0].equalsIgnoreCase("all")){
                BeangameItemRegistry.collection().forEach(item -> item(item, args));
            }
            BeangameItemRegistry.get(NamespacedKey.fromString(args[0])).ifPresent(item -> item(item, args));
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            List<String> keys = BeangameItemRegistry.collection().stream()
                    .map(item -> item.getKey().toString())
                    .toList();
            List<String> returnValue = new ArrayList<>();
            StringUtil.copyPartialMatches(strings[0], keys, returnValue);
            // Retry with "beangame:" prefix if no match was found
            if (returnValue.isEmpty()) {
                StringUtil.copyPartialMatches("beangame:" + strings[0], keys, returnValue);
            }
            return returnValue;
        } else if (strings.length == 2 || strings.length == 3) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
            List<String> returnValue = new ArrayList<>();
            StringUtil.copyPartialMatches(strings[1], playerNames, returnValue);
            return returnValue;
        } else if(strings.length == 4){
            List<String> returnValue = new ArrayList<>();
            StringUtil.copyPartialMatches(strings[1], List.of("true"), returnValue);
            return returnValue;
        }
        return List.of();
    }
    void item(BeangameItem item, String[] args){
        boolean bypass = false;
        if(args.length > 3 && args[3].equalsIgnoreCase("true")){
            bypass = true;
        }
        if(bypass) item.resetCooldown(Bukkit.getPlayer(args[1]).getUniqueId());
        if(item instanceof BGRClickableI i){
            Player p1 = Bukkit.getPlayer(args[1]);
            ItemStack stack = item.asItem();
            PlayerInteractEvent event = new PlayerInteractEvent(p1, Action.RIGHT_CLICK_AIR, stack, null, BlockFace.SELF);
            i.onRightClickWithAnimation(event, stack, true);
        }
        if(item instanceof BGLClickableI i){
            Player p1 = Bukkit.getPlayer(args[1]);
            ItemStack stack = item.asItem();
            PlayerInteractEvent event = new PlayerInteractEvent(p1, Action.LEFT_CLICK_AIR, stack, null, BlockFace.SELF);
            i.onLeftClick(event, stack);
        }
        if(item instanceof BGDamageArmorI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageEvent event = new EntityDamageEvent(Bukkit.getPlayer(args[1]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 1);
            i.onDamageArmor(event, stack);
        }
        if(item instanceof BGDamageHeldI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageEvent event = new EntityDamageEvent(Bukkit.getPlayer(args[1]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 1);
            i.onDamageHeldItem(event, stack);
        }
        if(item instanceof BGDamageInvI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageEvent event = new EntityDamageEvent(Bukkit.getPlayer(args[1]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 1);
            i.onDamageInventory(event, stack);
        }
        if(item instanceof BGDDealerArmorI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(Bukkit.getPlayer(args[1]), Bukkit.getPlayer(args[2]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 1);
            i.attackerOnHitArmor(event, stack);
        }
        if(item instanceof BGDDealerHeldI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(Bukkit.getPlayer(args[1]), Bukkit.getPlayer(args[2]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 1);
            i.attackerOnHit(event, stack);
        }
        if(item instanceof BGDDealerInvI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(Bukkit.getPlayer(args[1]), Bukkit.getPlayer(args[2]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 1);
            i.attackerInventoryOnHit(event, stack);
        }
        if(item instanceof BGDReceiverArmorI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(Bukkit.getPlayer(args[2]), Bukkit.getPlayer(args[1]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 1);
            i.victimOnHitArmor(event, stack);
        }
        if(item instanceof BGDReceiverFinalInvI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(Bukkit.getPlayer(args[2]), Bukkit.getPlayer(args[1]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 1);
            i.victimFinalInventoryOnHit(event, stack);
        }
        if(item instanceof BGDReceiverHeldI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(Bukkit.getPlayer(args[2]), Bukkit.getPlayer(args[1]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 1);
            i.victimOnHit(event, stack);
        }
        if(item instanceof BGDReceiverInvI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(Bukkit.getPlayer(args[2]), Bukkit.getPlayer(args[1]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 1);
            i.victimInventoryOnHit(event, stack);
        }
        if(item instanceof BGDeathArmorI i){
            ItemStack stack = item.asItem();
            EntityDeathEvent event = new EntityDeathEvent(Bukkit.getPlayer(args[1]), DamageSource.builder(DamageType.BAD_RESPAWN_POINT).build(), new ArrayList<>());
            i.onDeathArmor(event, stack);
        }
        if(item instanceof BGDeathHeldI i){
            ItemStack stack = item.asItem();
            EntityDeathEvent event = new EntityDeathEvent(Bukkit.getPlayer(args[1]), DamageSource.builder(DamageType.BAD_RESPAWN_POINT).build(), new ArrayList<>());
            i.onDeathHeldItem(event, stack);
        }
        if(item instanceof BGDeathInvI i){
            ItemStack stack = item.asItem();
            EntityDeathEvent event = new EntityDeathEvent(Bukkit.getPlayer(args[1]), DamageSource.builder(DamageType.BAD_RESPAWN_POINT).build(), new ArrayList<>());
            i.onDeathInventory(event, stack);
        }
        if(item instanceof BGLateDamageInvI i){
            ItemStack stack = item.asItem();
            DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
            if(args.length > 2){
                builder.withDirectEntity(Bukkit.getPlayer(args[2]));
            }
            EntityDamageEvent event = new EntityDamageEvent(Bukkit.getPlayer(args[1]), EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), 100);
            i.onLateDamageInventory(event, stack);
        }
        if(item instanceof BGTalismanI i){
            i.applyTalismanEffects(Bukkit.getPlayer(args[1]), item.asItem());
        }
        if(item instanceof BGLPGlobalTick i){
            i.tick();
        }
        if(item instanceof BGGlobalDeath i){
            i.onGlobalDeath(Bukkit.getPlayer(args[1]), item.asItem());
        }
        if(item instanceof BGArmorI i){
            i.applyArmorEffects(Bukkit.getPlayer(args[1]), item.asItem());
        }
        if(item instanceof BGConsumableI i){
            i.onConsume(new PlayerItemConsumeEvent(Bukkit.getPlayer(args[1]), item.asItem(), EquipmentSlot.HAND));
        }
        if(item instanceof BGFlightArmorI i){
            i.onToggleFlightArmor(new PlayerToggleFlightEvent(Bukkit.getPlayer(args[1]), true), item.asItem());
        }
        if(item instanceof BGSneakArmorI i){
            i.onSneakArmor(new PlayerToggleSneakEvent(Bukkit.getPlayer(args[1]), true), item.asItem());
        }
        if(item instanceof BGSneakHeldI i){
            i.onToggleHeldItemSneak(new PlayerToggleSneakEvent(Bukkit.getPlayer(args[1]), true), item.asItem());
        }
        if(item instanceof BGSneakInvI i){
            i.onToggleInventoryItemSneak(new PlayerToggleSneakEvent(Bukkit.getPlayer(args[1]), true), item);
        }
    }
}

