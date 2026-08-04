package com.rk_exxec.random_bag.mixins;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import com.rk_exxec.random_bag.CommonConfig;
import com.rk_exxec.random_bag.RandomBag;

import java.util.regex.Pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;




@Mixin(JigsawPlacement.class)
public class JigsawPlacementMixin {

    @Unique
    private static final HashMap<String, Tuple<Integer,LinkedList<StructurePoolElement>>> randomBags = new HashMap<>();
    //TODO 
    // why so many pulls at once?? -> switch all logging to info again
    // try move compstruct boulders in its own category somehow to test patternmatching, boulder selection doesnt work rn cus compstruct doesnt have subcategories
    // check also startJigsawName
    @Redirect(method = "addPieces",
        at = @At(value = "INVOKE", 
            target = "Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;getRandomTemplate(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/level/levelgen/structure/pools/StructurePoolElement;"))
    private static StructurePoolElement randomBag$getRandomTemplate(
            @Nonnull StructureTemplatePool instance,
            @Nonnull RandomSource random,
            Structure.GenerationContext context,
            Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName,
            int maxDepth,
            BlockPos pos,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            int maxDistanceFromCenter){        
        // getting structure pool location
        Registry<StructureTemplatePool> poolRegistry = context.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);

        ResourceLocation poolId = poolRegistry.getKey(instance);
        if (poolId == null) {
            throw new IllegalStateException("Unregistered pool: " + instance);
        }

        String resourceLoc = poolId.toString();
        RandomBag.LOGGER.debug("Checking structure " + resourceLoc + " for random bag");
        // "compstruct:aluminium_boulder etc"
        Integer matchingPatternIdx = -1;

        // skip matching if bag already exists
        if(!randomBags.containsKey(resourceLoc)){
            matchingPatternIdx = doesResourceMatch(resourceLoc);

            // no match, will use normal randomization
            if(matchingPatternIdx == -1) {
                RandomBag.LOGGER.debug("Bag patterns didnt match " + resourceLoc);
                // RandomBag.matchPatternCache.put(resourceLoc,0);
                return instance.getRandomTemplate(random);
            }
            else
            {
                randomBags.put(resourceLoc, new Tuple<>(matchingPatternIdx, new LinkedList<StructurePoolElement>()));
            }
        }
        // else: random bag
        RandomBag.LOGGER.info("Pulling one (1) variant out of the bag for: " + resourceLoc);
        LinkedList<StructurePoolElement> bag = randomBags.get(resourceLoc).getB();
        if (bag == null || bag.isEmpty()) {
            RandomBag.LOGGER.info("Bag empty, reshuffling...");
            randomBags.get(resourceLoc).setB(new LinkedList<StructurePoolElement>());
            // depending on the config BAG_SIZE_MULTS add multiple times to allow for more permutations
            Integer multiplier = CommonConfig.BAG_SIZE_MULTS.get().get(matchingPatternIdx);
            for(int i=0; i<multiplier ; i++){
                randomBags.get(resourceLoc).getB().addAll(instance.getShuffledTemplates(random));
            }
            if(multiplier > 1) Collections.shuffle(randomBags.get(resourceLoc).getB());
        }
        StructurePoolElement curSelection = randomBags.get(resourceLoc).getB().removeLast();
        RandomBag.LOGGER.info("Pulled: " + curSelection + " | " + randomBags.get(resourceLoc).getB().size() + " variants left in this bag.");
        return curSelection;
    }

    private static Integer doesResourceMatch(String resourceLoc){
        Integer matchingIdx = -1;
        // match current structure location to allowed config
        RandomBag.LOGGER.debug("First time matching " + resourceLoc);
        int i = 0;
        for (Map.Entry<Pattern,Integer> set : RandomBag.matchPatternCycle.entrySet()) {
            
            if(set.getKey().matcher(resourceLoc).find()) {
                // anyMatch = true;
                // RandomBag.matchPatternCache.put(resourceLoc, set.getValue());
                RandomBag.LOGGER.debug("Found match " + set.getKey().toString());
                matchingIdx = i;
                break;
            }
            RandomBag.LOGGER.debug("Bag pattern " + set.getKey().toString() + " didnt match " + resourceLoc);
            i++;
        }
        // }
        return matchingIdx;
    }
}