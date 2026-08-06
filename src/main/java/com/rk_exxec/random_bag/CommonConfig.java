package com.rk_exxec.random_bag;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_STRUCTURE_POOLS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> BAG_SIZE_MULTS;
    public static final Integer defaultMult = 1;

    static {
        BUILDER.push("random_bag");

        ALLOWED_STRUCTURE_POOLS = BUILDER.comment("#\n A list of structure pool resource locations the selection algorithm should apply to (with optional wildcard patterns). \n E.g. 'minecraft:trail_ruins*', 'minecraft:village/desert/*', mydatapack:mystructurepool'.")
                .defineList("Allowed structure pools", List.of("minecraft:village*"), loc -> loc instanceof String);
        BAG_SIZE_MULTS = BUILDER.comment("#\n A list of multipliers that specifies how often the bag should contain all templates of each structure type. \n May allow for more variation.")
                .defineList("Bag Size Multiplier", List.of(defaultMult), mult -> mult instanceof Integer);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void onLoad()
	{
        
	}
}
