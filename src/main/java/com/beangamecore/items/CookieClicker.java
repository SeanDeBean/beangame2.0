package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.general.BGResetableI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.type.talisman.BGInvUnstackable;
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class CookieClicker extends BeangameItem implements BGRClickableI, BGMPTalismanI, BGInvUnstackable, BGResetableI {
    
    public HashMap<UUID, CookieClicker.Instance> instances = new HashMap<>();

    @Override
    public void resetItem(){
        for(UUID u : instances.keySet()){
            Player p = Bukkit.getPlayer(u);
            if(p != null && p.getOpenInventory().getTitle().equals("§6Cookie Clicker ™")) p.closeInventory();
            instances = new HashMap<>();
        }
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        Instance i = instances.get(player.getUniqueId());
        if(i != null) i.tick(player);
    }

    public static class Instance{
        HashMap<Integer, ItemStack> items = new HashMap<>();
        Inventory inventory;
        ItemStack fill;
        ItemStack cookie;
        ItemStack beanminesupgrade;
        ItemStack beanfactoriesupgrade;
        ItemStack beanbanksupgrade;
        ItemStack beantemplesupgrade;
        ItemStack beangamesupgrade;
        ItemStack beanchroniclespruchase;
        ItemStack onecpsupgrade;
        ItemStack twocpsupgrade;
        ItemStack threecpsupgrade;
        ItemStack fourcpsupgrade;
        ItemStack onecpcupgrade;
        ItemStack twocpcupgrade;
        ItemStack threecpcupgrade;
        ItemStack fourcpcupgrade;
        ItemStack cookietoggle;
        boolean cookies_enabled = true;
        boolean onecpsupgrade_unlocked = false;
        boolean twocpsupgrade_unlocked = false;
        boolean threecpsupgrade_unlocked = false;
        boolean fourcpsupgrade_unlocked = false;
        boolean onecpcupgrade_unlocked = false;
        boolean twocpcupgrade_unlocked = false;
        boolean threecpcupgrade_unlocked = false;
        boolean fourcpcupgrade_unlocked = false;
        int beans = 0;
        double bpc = 1;
        double bps = 0;
        double bpcMult = 1;
        double bpsMult = 1;
        int beanmines = 0;
        int beanminescost = 100;
        int beanfactories = 0;
        int beanfactoriescost = 500;
        int beanbanks = 0;
        int beanbankscost = 2500;
        int beantemples = 0;
        int beantemplescost = 12500;
        int beangames = 0;
        int beangamescost = 62500;
        int beanchroniclescount = 0;
        int beanchroniclesmax = 4;
        int beanchroniclescost = 1000000;
        int onecpc_cost = 250;
        int onecps_cost = 1000;
        int twocpc_cost = 1000;
        int twocps_cost = 5000;
        int threecpc_cost = 7500;
        int threecps_cost = 12500;
        int fourcpc_cost = 32500;
        int fourcps_cost = 62500;

        public Instance(){
            inventory = Bukkit.createInventory(null, 54, "§6Cookie Clicker ™");
            fill = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta m0 = fill.getItemMeta();
            m0.setHideTooltip(true);
            fill.setItemMeta(m0);
            cookie = BeangameItemRegistry.getRaw(Key.bg("cookieclicker")).asItem();
            ItemMeta m = cookie.getItemMeta();
            m.setDisplayName(ChatColor.GOLD+"Beans: "+ChatColor.YELLOW+"0");
            cookie.setItemMeta(m);
            beanminesupgrade = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta meta1 = beanminesupgrade.getItemMeta();
            meta1.setDisplayName(ChatColor.GOLD+"Bean Mine");
            meta1.setLore(List.of(ChatColor.RED+"Cost: "+beanminescost+" beans", ChatColor.WHITE+"Owned: "+beanmines));
            beanminesupgrade.setItemMeta(meta1);

            beanfactoriesupgrade = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta meta2 = beanfactoriesupgrade.getItemMeta();
            meta2.setDisplayName(ChatColor.GOLD+"Bean Factory");
            meta2.setLore(List.of(ChatColor.RED+"Cost: "+beanfactoriescost+" beans", ChatColor.WHITE+"Owned: "+beanfactories));
            beanfactoriesupgrade.setItemMeta(meta2);

            beanbanksupgrade = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta meta3 = beanbanksupgrade.getItemMeta();
            meta3.setDisplayName(ChatColor.GOLD+"Bean Bank");
            meta3.setLore(List.of(ChatColor.RED+"Cost: "+beanbankscost+" beans", ChatColor.WHITE+"Owned: "+beanbanks));
            beanbanksupgrade.setItemMeta(meta3);

            beantemplesupgrade = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta meta4 = beantemplesupgrade.getItemMeta();
            meta4.setDisplayName(ChatColor.GOLD+"Bean Temple");
            meta4.setLore(List.of(ChatColor.RED+"Cost: "+beantemplescost+" beans", ChatColor.WHITE+"Owned: "+beantemples));
            beantemplesupgrade.setItemMeta(meta4);

            beangamesupgrade = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta meta5 = beangamesupgrade.getItemMeta();
            meta5.setDisplayName(ChatColor.GOLD+"Beangame");
            meta5.setLore(List.of(ChatColor.RED+"Cost: "+beangamescost+" beans", ChatColor.WHITE+"Owned: "+beangames));
            beangamesupgrade.setItemMeta(meta5);

            beanchroniclespruchase = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta14 = beanchroniclespruchase.getItemMeta();
            meta14.setDisplayName(ChatColor.GOLD+"Purchase Bean Chronicles");
            meta14.setCustomModelData(102);
            if(beanchroniclescount < beanchroniclesmax){
                meta14.setLore(List.of(ChatColor.RED + "Cost: " + beanchroniclescost + " beans", ChatColor.WHITE + "Purchased: " + beanchroniclescount));
            } else {
                meta14.setLore(List.of(ChatColor.RED + "Cost: SOLD OUT", ChatColor.WHITE + "Purchased: " + beanchroniclescount));
            }
            beanchroniclespruchase.setItemMeta(meta14);

            boolean owned;

            owned = onecpsupgrade_unlocked;
            onecpsupgrade = new ItemStack(Material.BOOK);
            ItemMeta meta6 = onecpsupgrade.getItemMeta();
            meta6.setDisplayName(ChatColor.GOLD+"1 Cookie Per Second Upgrade");
            meta6.setLore(List.of(ChatColor.RED+"Cost: "+onecps_cost+" beans", ChatColor.WHITE+"Owned: "+owned));
            onecpsupgrade.setItemMeta(meta6);

            owned = onecpcupgrade_unlocked;
            onecpcupgrade = new ItemStack(Material.BOOK);
            ItemMeta meta7 = onecpcupgrade.getItemMeta();
            meta7.setDisplayName(ChatColor.GOLD+"1 Cookie Per Click Upgrade");
            meta7.setLore(List.of(ChatColor.RED+"Cost: "+onecpc_cost+" beans", ChatColor.WHITE+"Owned: "+owned));
            onecpcupgrade.setItemMeta(meta7);

            owned = twocpsupgrade_unlocked;
            twocpsupgrade = new ItemStack(Material.BOOK);
            ItemMeta meta8 = twocpsupgrade.getItemMeta();
            meta8.setDisplayName(ChatColor.GOLD+"2 Cookies Per Second Upgrade");
            meta8.setLore(List.of(ChatColor.RED+"Cost: "+twocps_cost+" beans", ChatColor.WHITE+"Owned: "+owned));
            twocpsupgrade.setItemMeta(meta8);

            owned = twocpcupgrade_unlocked;
            twocpcupgrade = new ItemStack(Material.BOOK);
            ItemMeta meta9 = twocpcupgrade.getItemMeta();
            meta9.setDisplayName(ChatColor.GOLD+"2 Cookies Per Click Upgrade");
            meta9.setLore(List.of(ChatColor.RED+"Cost: "+twocpc_cost+" beans", ChatColor.WHITE+"Owned: "+owned));
            twocpcupgrade.setItemMeta(meta9);

            owned = threecpsupgrade_unlocked;
            threecpsupgrade = new ItemStack(Material.BOOK);
            ItemMeta meta10 = threecpsupgrade.getItemMeta();
            meta10.setDisplayName(ChatColor.GOLD+"3 Cookies Per Second Upgrade");
            meta10.setLore(List.of(ChatColor.RED+"Cost: "+threecps_cost+" beans", ChatColor.WHITE+"Owned: "+owned));
            threecpsupgrade.setItemMeta(meta10);

            owned = threecpcupgrade_unlocked;
            threecpcupgrade = new ItemStack(Material.BOOK);
            ItemMeta meta11 = threecpcupgrade.getItemMeta();
            meta11.setDisplayName(ChatColor.GOLD+"3 Cookies Per Click Upgrade");
            meta11.setLore(List.of(ChatColor.RED+"Cost: "+threecpc_cost+" beans", ChatColor.WHITE+"Owned: "+owned));
            threecpcupgrade.setItemMeta(meta11);

            owned = fourcpsupgrade_unlocked;
            fourcpsupgrade = new ItemStack(Material.BOOK);
            ItemMeta meta12 = fourcpsupgrade.getItemMeta();
            meta12.setDisplayName(ChatColor.GOLD+"4 Cookies Per Second Upgrade");
            meta12.setLore(List.of(ChatColor.RED+"Cost: "+fourcps_cost+" beans", ChatColor.WHITE+"Owned: "+owned));
            fourcpsupgrade.setItemMeta(meta12);

            owned = fourcpcupgrade_unlocked;
            fourcpcupgrade = new ItemStack(Material.BOOK);
            ItemMeta meta13 = fourcpcupgrade.getItemMeta();
            meta13.setDisplayName(ChatColor.GOLD+"4 Cookies Per Click Upgrade");
            meta13.setLore(List.of(ChatColor.RED+"Cost: "+fourcpc_cost+" beans", ChatColor.WHITE+"Owned: "+owned));
            fourcpcupgrade.setItemMeta(meta13);

            cookietoggle = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta metafinal = cookietoggle.getItemMeta();
            metafinal.setDisplayName(ChatColor.GOLD+"Cookies Enabled: "+cookies_enabled);
            cookietoggle.setItemMeta(metafinal);

            items.put(22, cookie);
            items.put(15, beanminesupgrade);
            items.put(16, beanfactoriesupgrade);
            items.put(24, beanbanksupgrade);
            items.put(25, beantemplesupgrade);
            items.put(33, beangamesupgrade);
            items.put(10, onecpsupgrade);
            items.put(11, onecpcupgrade);
            items.put(19, twocpsupgrade);
            items.put(20, twocpcupgrade);
            items.put(28, threecpsupgrade);
            items.put(29, threecpcupgrade);
            items.put(37, fourcpsupgrade);
            items.put(38, fourcpcupgrade);
            items.put(40, cookietoggle);
            items.put(43, beanchroniclespruchase);
        }

        public Inventory getAndInitInventory(){
            for(int i = 0; i < 54; i++){
                inventory.setItem(i, items.getOrDefault(i, fill));
            }
            return inventory;
        }

        void click(InventoryClickEvent event){
            ItemStack i = items.get(event.getSlot());
            if(i == null) return;
            if(i.equals(cookie)){
                click();
                ItemMeta meta = cookie.getItemMeta();
                event.getView().getTopInventory().getItem(22).setItemMeta(meta);
            } else if(i.equals(cookietoggle)){
                cookies_enabled = !cookies_enabled;
                ItemMeta m = cookietoggle.getItemMeta();
                m.setDisplayName(ChatColor.GOLD+"Cookies Enabled: "+cookies_enabled);
                cookietoggle.setItemMeta(m);
                event.getView().getTopInventory().getItem(40).setItemMeta(m);
            } else if(i.equals(beanminesupgrade)){
                if(beans >= beanminescost){
                    beans -= beanminescost;
                    beanmines++;
                    beanminescost *= 1.5f;
                    bps += beanmines * 1.5;
                    bpc += 0.5;
                    ItemMeta m = beanminesupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+beanminescost+" beans", ChatColor.WHITE+"Owned: "+beanmines));
                    beanminesupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(15).setItemMeta(beanminesupgrade.getItemMeta());
                }
            } else if(i.equals(beanfactoriesupgrade)){
                if(beans >= beanfactoriescost){
                    beans -= beanfactoriescost;
                    beanfactories++;
                    beanfactoriescost *= 1.475f;
                    bps += beanmines * 1.75;
                    bpc += 2.5;
                    ItemMeta m = beanfactoriesupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+beanfactoriescost+" beans", ChatColor.WHITE+"Owned: "+beanfactories));
                    beanfactoriesupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(16).setItemMeta(beanfactoriesupgrade.getItemMeta());
                }
            } else if(i.equals(beanbanksupgrade)){
                if(beans >= beanbankscost){
                    beans -= beanbankscost;
                    beanbanks++;
                    beanbankscost *= 1.45f;
                    bps += beanbanks * 2;
                    bpc += 12.5;
                    ItemMeta m = beanbanksupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+beanbankscost+" beans", ChatColor.WHITE+"Owned: "+beanbanks));
                    beanbanksupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(24).setItemMeta(beanbanksupgrade.getItemMeta());
                }
            } else if(i.equals(beantemplesupgrade)){
                if(beans >= beantemplescost){
                    beans -= beantemplescost;
                    beantemples++;
                    beantemplescost *= 1.425f;
                    bps += beantemples * 2.5;
                    bpc += 62.25;
                    ItemMeta m = beantemplesupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+beantemplescost+" beans", ChatColor.WHITE+"Owned: "+beantemples));
                    beantemplesupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(25).setItemMeta(beantemplesupgrade.getItemMeta());
                }
            } else if(i.equals(beangamesupgrade)){
                if(beans >= beangamescost){
                    beans -= beangamescost;
                    beangames++;
                    beangamescost *= 1.4f;
                    bps += beangames * 12.5;
                    bpc += 313.75;
                    ItemMeta m = beangamesupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+beangamescost+" beans", ChatColor.WHITE+"Owned: "+beangames));
                    beangamesupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(33).setItemMeta(beangamesupgrade.getItemMeta());
                }
            } else if(i.equals(onecpsupgrade)) {
                if(beans >= onecps_cost && !onecpsupgrade_unlocked){
                    beans -= onecps_cost;
                    onecpsupgrade_unlocked = true;
                    ItemMeta m = onecpsupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+onecps_cost+" beans", ChatColor.WHITE+"Owned: true"));
                    onecpsupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(10).setItemMeta(onecpsupgrade.getItemMeta());
                }
            } else if(i.equals(onecpcupgrade)) {
                if(beans >= onecpc_cost && !onecpcupgrade_unlocked){
                    beans -= onecpc_cost;
                    onecpcupgrade_unlocked = true;
                    ItemMeta m = onecpcupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+onecpc_cost+" beans", ChatColor.WHITE+"Owned: true"));
                    onecpcupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(11).setItemMeta(onecpcupgrade.getItemMeta());
                }
            } else if(i.equals(twocpsupgrade)) {
                if(beans >= twocps_cost && !twocpsupgrade_unlocked){
                    beans -= twocps_cost;
                    twocpsupgrade_unlocked = true;
                    ItemMeta m = twocpsupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+twocps_cost+" beans", ChatColor.WHITE+"Owned: true"));
                    twocpsupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(19).setItemMeta(twocpsupgrade.getItemMeta());
                }
            } else if(i.equals(twocpcupgrade)) {
                if(beans >= twocpc_cost && !twocpcupgrade_unlocked){
                    beans -= twocpc_cost;
                    twocpcupgrade_unlocked = true;
                    ItemMeta m = twocpcupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+twocpc_cost+" beans", ChatColor.WHITE+"Owned: true"));
                    twocpcupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(20).setItemMeta(twocpcupgrade.getItemMeta());
                }
            } else if(i.equals(threecpsupgrade)) {
                if(beans >= threecps_cost && !threecpsupgrade_unlocked){
                    beans -= threecps_cost;
                    threecpsupgrade_unlocked = true;
                    ItemMeta m = threecpsupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+threecps_cost+" beans", ChatColor.WHITE+"Owned: true"));
                    threecpsupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(28).setItemMeta(threecpsupgrade.getItemMeta());
                }
            } else if(i.equals(threecpcupgrade)) {
                if(beans >= threecpc_cost && !threecpcupgrade_unlocked){
                    beans -= threecpc_cost;
                    threecpcupgrade_unlocked = true;
                    ItemMeta m = threecpcupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+threecpc_cost+" beans", ChatColor.WHITE+"Owned: true"));
                    threecpcupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(29).setItemMeta(threecpcupgrade.getItemMeta());
                }
            } else if(i.equals(fourcpsupgrade)) {
                if(beans >= fourcps_cost && !fourcpsupgrade_unlocked){
                    beans -= fourcps_cost;
                    fourcpsupgrade_unlocked = true;
                    ItemMeta m = fourcpsupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+fourcps_cost+" beans", ChatColor.WHITE+"Owned: true"));
                    fourcpsupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(37).setItemMeta(fourcpsupgrade.getItemMeta());
                }
            } else if(i.equals(fourcpcupgrade)) {
                if(beans >= fourcpc_cost && !fourcpcupgrade_unlocked){
                    beans -= fourcpc_cost;
                    fourcpcupgrade_unlocked = true;
                    ItemMeta m = fourcpcupgrade.getItemMeta();
                    m.setLore(List.of(ChatColor.RED+"Cost: "+fourcpc_cost+" beans", ChatColor.WHITE+"Owned: true"));
                    fourcpcupgrade.setItemMeta(m);
                    event.getView().getTopInventory().getItem(38).setItemMeta(fourcpcupgrade.getItemMeta());
                }
            } else if(i.equals(beanchroniclespruchase)) {
                if(beans >= beanchroniclescost && beanchroniclescount < beanchroniclesmax){
                    beans -= beanchroniclescost;
                    beanchroniclescount++;
                    beanchroniclescost *= 2;

                    ItemMeta m = beanchroniclespruchase.getItemMeta();
                    if(beanchroniclescount < beanchroniclesmax){
                        m.setLore(List.of(ChatColor.RED + "Cost: " + beanchroniclescost + " beans", ChatColor.WHITE + "Purchased: " + beanchroniclescount));
                    } else {
                        m.setLore(List.of(ChatColor.RED + "Cost: SOLD OUT", ChatColor.WHITE + "Purchased: " + beanchroniclescount));
                    }
                    beanchroniclespruchase.setItemMeta(m);
                    event.getView().getTopInventory().getItem(43).setItemMeta(beanchroniclespruchase.getItemMeta());

                    // buy
                    BeangameItem item = BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:beanchronicles"));
                    if (event.getWhoClicked().getInventory().firstEmpty() != -1) {
                        event.getWhoClicked().getInventory().addItem(item.asItem());
                    } else {
                        event.getWhoClicked().getWorld().dropItemNaturally(event.getWhoClicked().getLocation(), item.asItem());
                    }
                }
            }
        }
        
        void click(){
            beans += bpc * bpcMult;
            ItemMeta m = cookie.getItemMeta();
            m.setDisplayName(ChatColor.GOLD+"Beans: "+ChatColor.YELLOW+beans);
            cookie.setItemMeta(m);
        }

        void tick(Player player){
            beans += bps * bpsMult;
            if(cookies_enabled){
                if(fourcpsupgrade_unlocked){
                    player.getInventory().addItem(new ItemStack(Material.COOKIE, 4));
                } else if(threecpsupgrade_unlocked){
                    player.getInventory().addItem(new ItemStack(Material.COOKIE, 3));
                } else if(twocpsupgrade_unlocked){
                    player.getInventory().addItem(new ItemStack(Material.COOKIE, 2));
                } else if(onecpsupgrade_unlocked){
                    player.getInventory().addItem(new ItemStack(Material.COOKIE, 1));
                }
            }
            ItemMeta meta = cookie.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD+"Beans: "+ChatColor.YELLOW+beans);
            cookie.setItemMeta(meta);
            if(player.getOpenInventory().getTitle().equals("§6Cookie Clicker ™")){
                player.getOpenInventory().getTopInventory().getItem(22).setItemMeta(meta);
            }
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        if(!instances.containsKey(event.getPlayer().getUniqueId())) instances.put(event.getPlayer().getUniqueId(), new Instance());
        event.getPlayer().openInventory(instances.get(event.getPlayer().getUniqueId()).getAndInitInventory());
        return true;
    }

    public void cookieclickerInventoryClick(InventoryClickEvent event){
        event.setCancelled(true);
        Player player = (Player)event.getWhoClicked();
        if(event.getCurrentItem() == null){
            return;
        }
        Instance i = instances.get(event.getWhoClicked().getUniqueId());
        if(event.getCurrentItem().getType() == Material.COOKIE && i.cookies_enabled){
            if(player.getInventory().firstEmpty() != -1){
                assert event.getCurrentItem() != null;
                int count = 0;
                if(i.fourcpcupgrade_unlocked){
                    count = 4;
                } else if(i.threecpcupgrade_unlocked){
                    count = 3;
                } else if(i.twocpcupgrade_unlocked){
                    count = 2;
                } else if(i.onecpcupgrade_unlocked){
                    count = 1;
                }
                if(count != 0) player.getInventory().addItem(new ItemStack(Material.COOKIE, count));
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6You need inventory space to use the cookie clicker!"));
            }
        }
        // prevents dupes
        event.getCurrentItem().setType(Material.AIR);
        instances.get(event.getWhoClicked().getUniqueId()).click(event);
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "cookieclicker";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }
    @Override
    public boolean isInFoodItemRotation(){
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§6Cookie Clicker ™";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to open Cookie Clicker",
            "§aminigame. Earn cookies by clicking and",
            "§apurchasing upgrades. Generates cookies",
            "§aper second when upgrades are unlocked.",
            "",
            "§aSupport",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.COOKIE;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
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

