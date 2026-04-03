package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.items.type.general.BG3sTickingI;
import com.beangamecore.items.type.voicechat.BGVoicechat;
import com.beangamecore.recipes.RecipeAPI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Key;
import de.maxhenkel.voicechat.api.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class WalkieTalkie extends BeangameItem implements BGVoicechat, BG3sTickingI, BGRClickableI, BeangameSoftItem {
    
    public static final float MAX_FREQUENCY = 500f;
    public static final float MIN_FREQUENCY = 27f;

    VoicechatServerApi api;

    public static List<Player> frequencyChat = new ArrayList<>();

    HashMap<Float, WalkieTalkie.WTServer> network = new HashMap<>();
    HashMap<UUID, Float> tempFreq = new HashMap<>();

    public void walkieTalkieInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            Material m = event.getCurrentItem().getType();
            Player player = (Player) event.getWhoClicked(); // Get the player
    
            if (m == Material.IRON_PICKAXE) { // Connect
                if (!tempFreq.containsKey(player.getUniqueId())) return;
                float f = tempFreq.get(player.getUniqueId());
                join(f, player); // Join the frequency
                inventory(player); // Open inventory after connecting
            }
    
            if (m == Material.BARRIER) { // Disconnect
                WTServer server = getServerFromPlayer(player);
                if (server != null) {
                    server.removeUser(player); // Remove user from the server
                }
                inventory(player); // Open inventory after disconnecting
            }
    
            if (m == Material.WRITABLE_BOOK) { // Set Frequency
                openSignForFrequency(player); // Open the writable book for frequency input
            }
        }
    }

    public void openSignForFrequency(Player player) {
        
        Location loc = player.getLocation();
        loc.setY(loc.getY() - 5);
        if(loc.getY() > 319 || loc.getY() < -64){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "Too far from reception!"));
        }
        Block block = loc.getBlock();
        Material saveType = block.getType();
        block.setType(Material.OAK_SIGN, false);

        Sign sign = (Sign) block.getState();
        SignSide side = sign.getSide(Side.FRONT);
        side.setLine(1, "Put Value Above");
        side.setLine(2, "Must Be Between");
        side.setLine(3, "27 and 500 MHz");
        sign.update();

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> player.openSign(sign, Side.FRONT), 4L);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            block.setType(saveType);
        }, 200L);
    }

    public void onSignChange(SignChangeEvent event){
        EntityEquipment equipment = event.getPlayer().getEquipment();
        if(!(this.asItem().isSimilar(equipment.getItemInMainHand()) || this.asItem().isSimilar(equipment.getItemInOffHand()))){
            return;
        }

        Player player = event.getPlayer();
        String input = event.getLine(0);

        try {
            float frequency = Float.parseFloat(input.trim());

            // validate range
            if(frequency >= MIN_FREQUENCY && frequency <= MAX_FREQUENCY){
                // store the frequency
                tempFreq.put(player.getUniqueId(), frequency);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.GREEN + "Frequency set to: " + frequency + " MHz"));
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "Please enter a valid frequency between " + MIN_FREQUENCY + " and " + MAX_FREQUENCY + " MHz"));
            }
        } catch (NumberFormatException e){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "Invalid input! Please enter a valid number."));
        }
    }

    void inventory(Player player){
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Walkie Talkie!");
        ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemStack connect = BeangameItemRegistry.getRaw(Key.bg("lagconjurer")).asItem();
        ItemStack disconnect = new ItemStack(Material.BARRIER);
        ItemStack setFrequency = new ItemStack(Material.WRITABLE_BOOK);
        ItemStack frequency = new ItemStack(Material.PAPER);
        ItemMeta meta = connect.getItemMeta();

        float freq = getFrequencyFromPlayer(player);

        meta.setLore(null);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.AQUA + "Connect");
        connect.setItemMeta(meta);
        ItemMeta meta1 = disconnect.getItemMeta();
        meta1.setDisplayName(ChatColor.RED + "Disconnect");
        disconnect.setItemMeta(meta1);
        ItemMeta meta2 = empty.getItemMeta();
        meta2.setHideTooltip(true);
        empty.setItemMeta(meta2);
        ItemMeta meta3 = frequency.getItemMeta();
        meta3.setDisplayName(ChatColor.YELLOW + "Current Frequency: " + freq + " MHz");
        frequency.setItemMeta(meta3);
        ItemMeta meta4 = setFrequency.getItemMeta();
        meta4.setDisplayName(ChatColor.YELLOW + "Set Frequency");
        setFrequency.setItemMeta(meta4);

        boolean connected = getServerFromPlayer(player) != null;

        for(int i = 0; i < inv.getContents().length; i++){
            if(connected){
                switch (i){
                    default -> inv.setItem(i, empty);
                    case 11 -> inv.setItem(i, frequency);
                    case 15 -> inv.setItem(i, disconnect);
                }
            } else {
                switch (i){
                    default -> inv.setItem(i, empty);
                    case 11 -> inv.setItem(i, setFrequency);
                    case 15 -> inv.setItem(i, connect);
                }
            }
        }

        player.openInventory(inv);
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        
        inventory(event.getPlayer());

        return false;
    }

    WalkieTalkie.WTServer getServerFromPlayer(Player player){
        AtomicReference<WalkieTalkie.WTServer> s = new AtomicReference<>(null);
        network.forEach((freq, serv) -> {
            if(serv.users.contains(player)) s.set(serv);
        });
        return s.get();
    }

    float getFrequencyFromPlayer(Player player){
        AtomicReference<Float> s = new AtomicReference<>(0f);
        network.forEach((freq, serv) -> {
            if(serv.users.contains(player)) s.set(freq);
        });
        return s.get();
    }

    public void setTempFreq(Player player, float finalFreq) {
        tempFreq.put(player.getUniqueId(), finalFreq);
        inventory(player);
    }

    public static class WTServer {
        public WTServer(Group g, VoicechatServerApi api, WalkieTalkie w){
            group = g;
            this.api = api;
            this.w = w;
        }

        WalkieTalkie w;
        VoicechatServerApi api;
        Group group;
        List<Player> users = new ArrayList<>();

        void tick(){
            for(Player player : users){
                VoicechatConnection connection = api.getConnectionOf(player.getUniqueId());
                if (connection == null || !connection.isConnected()) {
                    continue;
                }
                boolean hasWalkieTalkie = w.hasWalkieTalkie(player);
                if (hasWalkieTalkie && !group.equals(connection.getGroup())) {
                    connection.setGroup(group);
                }
                if (!hasWalkieTalkie && group.equals(connection.getGroup())) {
                    connection.setGroup(null);
                }
            }

        }
        public void addUser(Player user){
            users.add(user);
            api.getConnectionOf(user.getUniqueId()).setGroup(group);
        }
        public void removeUser(Player user){
            users.remove(user);
            api.getConnectionOf(user.getUniqueId()).setGroup(null);
        }
    }

    void join(float frequency, Player player){
        if(frequency > MAX_FREQUENCY) frequency = MAX_FREQUENCY;
        if(frequency < MIN_FREQUENCY) frequency = MIN_FREQUENCY;
        if(network.get(frequency) == null) createFrequency(frequency);
        network.get(frequency).addUser(player);
    }

    void createFrequency(float frequency){
        if(frequency > MAX_FREQUENCY) frequency = MAX_FREQUENCY;
        if(frequency < MIN_FREQUENCY) frequency = MIN_FREQUENCY;
        Group group = api.groupBuilder().setType(Group.Type.OPEN).setHidden(true).setPassword("bg").setName("Walkie Talkie").setPersistent(false).build();

        network.put(frequency, new WalkieTalkie.WTServer(group, api, this));
    }

    void removeAllEmptyGroups(){
        List<Float> list = new ArrayList<>();
        network.forEach((f, s) -> {
            if(s.users.isEmpty()) {
                list.add(f);
                if(s.group != null) api.removeGroup(s.group.getId());
            }
        });
        list.forEach(f -> network.remove(f));
    }

    @Override
    public void tick() {
        if (api == null) {
            return; // Exit early if API is not initialized
        }
        for(WalkieTalkie.WTServer server : network.values()){
            server.tick();
        }
        removeAllEmptyGroups();
    }

    public boolean hasWalkieTalkie(Player player){
        return BeangameItemRegistry.hasBeangameItem(player, this);
    }

    @Override
    public void onInitialize(VoicechatServerApi api) {
        this.api = api;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }


    @Override
    public String getId() {
        return "walkietalkie";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        RecipeAPI r = Main.recipeAPI;
        return r.bgShapedRecipe(this, "  R", "ISI", "NIN", r.mCFromMaterial(Material.REDSTONE_TORCH), r.mCFromMaterial(Material.IRON_INGOT), r.mCFromMaterial(Material.COCOA_BEANS), r.mCFromMaterial(Material.IRON_NUGGET));
    }

    @Override
    public String getName() {
        return "§bWalkie Talkie";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame");
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;
    }

    @Override
    public int getCustomModelData() {
        return 103;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 4;
    }
}
