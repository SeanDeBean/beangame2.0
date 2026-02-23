package com.beangamecore.data;

import com.beangamecore.builders.EquipmentBuilder;
import org.bukkit.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.*;

public class BGItemMeta {

    private transient Material material;
    private transient List<ItemFlag> itemFlags;
    private transient ArmorTrim trim;
    private transient EquippableComponent equippable;

    private boolean in_rotation;
    private boolean in_food_rotation;

    private String item;
    private String name;
    private List<String> lore;
    private int custom_model_data;
    private int max_stack_size;
    private String equipment_slot;
    private String equipment_model;
    private String equipment_sound;
    private String equipment_camera_overlay;
    private boolean equipment_damageonhurt;
    private boolean equipment_swappable;
    private boolean glide_armor;

    private Map<String, Integer> enchantments;

    private boolean unbreakable;

    private List<String> flags;

    private String trim_pattern;
    private String trim_material;

    private Color color;

    private int armor;
    private EquipmentSlotGroup slot;

    void serialize(){
        if(equippable != null){
            equipment_slot = equippable.getSlot() == null ? null : equippable.getSlot().name();
            equipment_model = equippable.getModel() == null ? null : equippable.getModel().toString();
            equipment_sound = equippable.getEquipSound() == null ? null : equippable.getEquipSound().getKeyOrNull().toString();
            equipment_camera_overlay = equippable.getCameraOverlay() == null ? null : equippable.getCameraOverlay().toString();
            equipment_damageonhurt = equippable.isDamageOnHurt();
            equipment_swappable = equippable.isSwappable();
        }
        trim_pattern = trim == null ? null : trim.getPattern().getKeyOrNull().toString();
        trim_material = trim == null ? null : trim.getMaterial().getKeyOrNull().toString();
        flags = new ArrayList<>();
        itemFlags.forEach((f) -> flags.add(f.toString()));
        item = material.name();
    }

    private void deserializeEquipment() {
        EquipmentBuilder c = EquipmentBuilder.create();
        boolean exists = false;
        if(equipment_slot != null){
            exists = true;
            c.withSlot(EquipmentSlot.valueOf(equipment_slot));
        }
        if(equipment_model != null) c.withModel(NamespacedKey.fromString(equipment_model));
        if(equipment_sound != null) c.withEquipSound(Registry.SOUNDS.get(NamespacedKey.fromString(equipment_sound)));
        if(equipment_camera_overlay != null) c.withCameraOverlay(NamespacedKey.fromString(equipment_camera_overlay));
        c.withDamageOnHurt(equipment_damageonhurt);
        c.withSwappable(equipment_swappable);
        equippable = exists ? c.build() : null;
    }

    private void deserializeTrim() {
        try {
            if(trim_material == null || trim_pattern == null) {
                trim = null;
            } else {
                trim = new ArmorTrim(
                    Registry.TRIM_MATERIAL.get(NamespacedKey.fromString(trim_material)),
                    Registry.TRIM_PATTERN.get(NamespacedKey.fromString(trim_pattern))
                );
            }
        } catch (Exception e) {
            trim = null;
        }
    }

    void deserialize() {
        deserializeEquipment();
        deserializeTrim();
        itemFlags = new ArrayList<>();
        flags.forEach(flag -> itemFlags.add(ItemFlag.valueOf(flag)));
        material = Material.getMaterial(item);
    }

    public Material getMaterial(){
        return material;
    }

    public void setMaterial(Material material){
        this.material = material;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public List<String> getLore(){
        return lore;
    }

    public void setLore(List<String> lore){
        this.lore = lore;
    }

    public int getCustomModelData(){
        return custom_model_data;
    }

    public void setCustomModelData(int data){
        this.custom_model_data = data;
    }

    public Map<String, Integer> getEnchantments(){
        return enchantments;
    }

    public void setEnchantments(Map<String, Integer> enchantments){
        this.enchantments = enchantments;
    }

    public List<ItemFlag> getItemFlags(){
        return itemFlags;
    }

    public void setItemFlags(List<ItemFlag> flags){
        this.itemFlags = flags;
    }

    public void setArmorTrim(ArmorTrim trim){
        this.trim = trim;
        if(trim != null){
            trim_pattern = trim.getPattern().getKeyOrNull().toString();
            trim_material = trim.getMaterial().getKeyOrNull().toString();
        }
    }

    public ArmorTrim getArmorTrim(){
        return trim;
    }

    public Color getColor(){
        return color;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public boolean inRotation(){
        return in_rotation;
    }

    public boolean inFoodRotation(){
        return in_food_rotation;
    }

    public void setInRotation(boolean value){
        in_rotation = value;
    }
    
    public void setInFoodRotation(boolean value){
        in_food_rotation = value;
    }

    public boolean isUnbreakable(){
        return unbreakable;
    }

    public void setUnbreakable(boolean unbreakable){
        this.unbreakable = unbreakable;
    }

    public void setArmor(int value){
        this.armor = value;
    }

    public int getArmor(){
        return armor;
    }

    public void setSlot(EquipmentSlotGroup slot){
        this.slot = slot;
    }

    public EquipmentSlotGroup getSlot(){
        return slot;
    }

    public int getMaxStackSize(){
        return max_stack_size;
    }

    public void setMaxStackSize(int value){
        this.max_stack_size = value;
    }

    public EquippableComponent getEquipmentData(){
        return equippable;
    }

    public void setEquipmentData(EquippableComponent c){
        equippable = c;
    }

    public boolean isGlideArmor(){
        return glide_armor;
    }

    public void setGlideArmor(boolean glide){
        glide_armor = glide;
    }

}

