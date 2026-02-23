package com.beangamecore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.beangamecore.entities.seacreatures.BeangameSeaCreature;
import com.beangamecore.entities.seacreatures.SeaCreatureRegistry;
import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.registry.BeangameItemRegistry;

public class FishingManager {
    private final Random random;
    private final double DOUBLE_CATCH_CHANCE = 0.02; // 2% chance to catch two fish
    private final double SEA_CREATURE_CHANCE = 0.04; // 4% chance to catch a sea creature instead of a fish

    public FishingManager() {
        this.random = new Random();
    }

    private List<BeangameFish> getFishableFish() {
        return new ArrayList<>(BeangameItemRegistry.getFishableFish());
    }

    public void handlePlayerFishEvent(PlayerFishEvent event) {
        Player player = event.getPlayer();
        Location hookLocation = event.getHook().getLocation();
        boolean isRaining = hookLocation.getWorld().hasStorm();


        if (random.nextDouble() < SEA_CREATURE_CHANCE) {
            Optional<BeangameSeaCreature> creatureOpt = SeaCreatureRegistry.selectRandomSeaCreature(
                hookLocation, 
                player, 
                hookLocation.getWorld().hasStorm(), 
                random
            );

            if (creatureOpt.isPresent()) {
                BeangameSeaCreature creature = creatureOpt.get();

                if (event.getCaught() != null) {
                    event.getCaught().remove();
                }
                
                creature.spawn(hookLocation, player);
                SeaCreatureRegistry.registerActiveCreature(creature);

                event.setExpToDrop(0);
                return; // Skip normal fish catching
            }
        }



        List<BeangameFish> eligibleFish = getEligibleFish(hookLocation, player, isRaining);

        if (eligibleFish.isEmpty()) {
            return; // No fish available for these conditions
        }

        int totalWeight = eligibleFish.stream().mapToInt(BeangameFish::getWeight).sum();
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;
        BeangameFish caughtFish = null;
        for (BeangameFish fish : eligibleFish) {
            currentWeight += fish.getWeight();
            if (randomValue < currentWeight) {
                caughtFish = fish;
                break;
            }
        }

        if (caughtFish != null) {
            if (event.getCaught() != null) {
                event.getCaught().remove();
            }

            boolean doubleCatch = random.nextDouble() < DOUBLE_CATCH_CHANCE;

            ItemStack fishItem = caughtFish.asItem();
            if(doubleCatch) {
                fishItem.setAmount(2);
            }
            Item droppedFish = hookLocation.getWorld().dropItemNaturally(hookLocation, fishItem);

            Vector direction = player.getLocation().toVector().subtract(hookLocation.toVector()).normalize();
            droppedFish.setVelocity(direction.multiply(0.5).setY(0.3));

            event.setExpToDrop(0);
            caughtFish.onCaught(player, player.getInventory().getItemInMainHand());
        }
    }

    private List<BeangameFish> getEligibleFish(Location location, Player player, boolean isRaining) {
        return getFishableFish().stream()
                .filter(fish -> fish.canBeCaught(location, player, isRaining))
                .collect(Collectors.toList());
    }

    public Collection<BeangameFish> getAllRegisteredFish() {
        return getFishableFish();
    }

}

