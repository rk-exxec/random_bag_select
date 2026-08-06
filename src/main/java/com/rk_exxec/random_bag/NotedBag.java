package com.rk_exxec.random_bag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;

public record NotedBag(
    Integer matchIdx,
    ObjectArrayList<StructurePoolElement> bag
){}
