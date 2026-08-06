package com.rk_exxec.random_bag.mixins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import com.rk_exxec.random_bag.CommonConfig;
import com.rk_exxec.random_bag.RandomBag;
import com.rk_exxec.random_bag.NotedBag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.regex.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;


@Mixin(JigsawPlacement.class)
public class JigsawPlacementMixin {
    @Unique
    private static final HashMap<String, NotedBag> randomBags = new HashMap<>();
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
            int maxDistanceFromCenter
    )
    {        
        // getting structure pool location
        Registry<StructureTemplatePool> poolRegistry = context.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);

        ResourceLocation poolId = poolRegistry.getKey(instance);
        if (poolId == null) {
            throw new IllegalStateException("Unregistered pool: " + instance);
        }

        String resourceLoc = poolId.toString();
        matchIfNotCached(resourceLoc);
        var bagHolder = randomBags.get(resourceLoc);

        // if no pattern has matched, fall back to vanilla behaviour
        if (bagHolder == null) return instance.getRandomTemplate(random);

        // else: random bag
        RandomBag.LOGGER.info("Bagging a structure");
        RandomBag.LOGGER.debug("Pulling one (1) variant out of the bag for pool: <" + resourceLoc + ">");
        ObjectArrayList<StructurePoolElement> bag = bagHolder.bag();
        Integer multiplier = CommonConfig.BAG_SIZE_MULTS.get().get(bagHolder.matchIdx());

        if (bag == null || bag.isEmpty()) {
            RandomBag.LOGGER.debug("Bag empty, reshuffling...");
            bagHolder.bag().clear();
            // depending on the config BAG_SIZE_MULTS add multiple times to allow for more permutations
            for(int i=0; i<multiplier ; i++){
                bagHolder.bag().addAll(instance.getShuffledTemplates(random)); // there is no other way to get all templates easily
            }
            // shuffle again, cus appending shuffled lists is not the goal, this way elements may acutally come up twice in a row etc
            if(multiplier > 1) Collections.shuffle(bagHolder.bag()); 
        }
        StructurePoolElement curSelection = bagHolder.bag().pop();
        RandomBag.LOGGER.debug("Pulled: <" + curSelection + "> | " + bagHolder.bag().size() + "/" + instance.size()*multiplier + " variants left in this bag.");
        return curSelection;
    }

    /**
     * skip matching if bag already exists, use hashmap key as cache </br>
     * modifies static variable randomBags
     * @param resourceLoc location of the resource to match
     */
    private static void matchIfNotCached(String resourceLoc){

        if(!randomBags.containsKey(resourceLoc)){
            RandomBag.LOGGER.debug("Checking structure pool <" + resourceLoc + "> for random bag");
            @Nonnull Integer matchingPatternIdx = -1;
            matchingPatternIdx = doesResourceMatch(resourceLoc);

            // no match, will use normal randomization
            if(matchingPatternIdx == -1) {
                RandomBag.LOGGER.debug("Bag patterns didnt match <" + resourceLoc + ">");
                // RandomBag.matchPatternCache.put(resourceLoc,0);
                randomBags.put(resourceLoc, null);
            }
            else
            {
                RandomBag.LOGGER.debug("Structure pool <" + resourceLoc + "> allowed for random bag");
                randomBags.put(resourceLoc, new NotedBag(matchingPatternIdx, new ObjectArrayList<StructurePoolElement>()));
            }
        }
    }

    /**
     * Determines if a resource location matches any of the configured pattern filters.
     * 
     * @param resourceLoc the resource location string to match against configured patterns
     * @return the index of the matching pattern in the configuration, or -1 if no match is found
     */
    private static @Nonnull Integer doesResourceMatch(String resourceLoc){
        @Nonnull Integer matchingIdx = -1;
        // match current structure location to allowed config
        RandomBag.LOGGER.debug("First time matching <" + resourceLoc + ">");
        int i = 0;
        for (Map.Entry<Pattern,Integer> set : RandomBag.matchPatternCycle.entrySet()) {
            // "compstruct:aluminium_boulder etc"
            if(set.getKey().matcher(resourceLoc).find()) {
                RandomBag.LOGGER.debug("Found match <" + set.getKey().toString() + ">");
                matchingIdx = i;
                break;
            }
            RandomBag.LOGGER.debug("Bag pattern <" + set.getKey().toString() + "> didnt match <" + resourceLoc + ">");
            i++;
        }
        return matchingIdx;
    }
}