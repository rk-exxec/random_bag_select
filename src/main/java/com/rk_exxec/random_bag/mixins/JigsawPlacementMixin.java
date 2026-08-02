// Random Bag Structure Select
// Copyright (C) 2026  rk-exxec

// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.


package com.rk_exxec.random_bag.mixins;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import com.rk_exxec.random_bag.RandomBag;

import java.util.regex.Pattern;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;




@Mixin(JigsawPlacement.class)
public class JigsawPlacementMixin {

    @Unique
    private static final LinkedList<StructurePoolElement> randomBagCurrentSet = new LinkedList<>();

    @Redirect(method = "addPieces",
        at = @At(value = "INVOKE", 
            target = "Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;getRandomTemplate(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/level/levelgen/structure/pools/StructurePoolElement;"))
    private static StructurePoolElement randomBag$getRandomTemplate(StructureTemplatePool instance, @Nonnull RandomSource random) {
        RandomBag.LOGGER.debug("Checking structure for random bag");
        StructurePoolElement tester = instance.getRandomTemplate(random);
        String resourceLoc = tester.toString();
        // "compstruct:aluminium_boulder etc"
        boolean anyMatch = false;
        // match current structure location to allowed config
        // did we already check this resource loc? if so skip iterating over dict
        if(RandomBag.matchPatternCache.containsKey(resourceLoc)){
            anyMatch = RandomBag.matchPatternCache.get(resourceLoc) > 0;
            RandomBag.LOGGER.debug("Bag patterns load cache for " + resourceLoc + " | " + (anyMatch ? "allowed":"prohibited"));
        }
        else{
            RandomBag.LOGGER.debug("Bag patterns no cache for " + resourceLoc);
            // match 
            for (Map.Entry<Pattern,Integer> set : RandomBag.matchPatternCycle.entrySet()) {
                RandomBag.LOGGER.debug("First time matching " + resourceLoc);
                if(set.getKey().matcher(resourceLoc).find()) {
                    anyMatch = true;
                    RandomBag.matchPatternCache.put(resourceLoc, set.getValue());
                    RandomBag.LOGGER.debug("Found match " + set.getKey().toString());
                    break;
                }
                RandomBag.LOGGER.debug("Bag pattern " + set.getKey().toString() + " didnt match " + resourceLoc);
            }
        }
        // no match, will use normal randomization
        if(!anyMatch) {
            RandomBag.LOGGER.debug("Bag patterns didnt match " + resourceLoc);
            RandomBag.matchPatternCache.put(resourceLoc,0);
            return tester;
        }
        
        // else: random bag
        RandomBag.LOGGER.info("Pulling one (1) variant out of the bag for: " + instance.toString());
        if (randomBagCurrentSet.isEmpty()) {
            RandomBag.LOGGER.info("Bag empty, reshuffling...");
            // depending on the config BAG_SIZE_MULTS add multiple times to allow for more permutations
            for(int i=0; i< RandomBag.matchPatternCache.get(resourceLoc); i++){
                randomBagCurrentSet.addAll(instance.getShuffledTemplates(random));
            }
            Collections.shuffle(randomBagCurrentSet);
        }
        StructurePoolElement curSelection = randomBagCurrentSet.removeLast();
        RandomBag.LOGGER.info("Pulled: " + curSelection + " | " + randomBagCurrentSet.size() + " variants left in this bag.");
        return curSelection;
    }
}