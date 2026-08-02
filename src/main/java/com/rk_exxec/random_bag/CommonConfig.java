package com.rk_exxec.random_bag;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_RESOURCE_LOCATIONS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> BAG_SIZE_MULTS;
    public static final Integer defaultMult = 1;

    static {
        BUILDER.push("Random Bag Structure Select Config");

        ALLOWED_RESOURCE_LOCATIONS = BUILDER.comment("A list of structure resource locations with optional wildcard patterns the selection algorithm should apply to. E.g. 'compstruct:*_boulder'. 'minecraft:village*', 'minecraft:village/desert/*'")
                .defineList("Allowed resource locations", List.of("compstruct:*_boulder"), loc -> loc instanceof String);
        BAG_SIZE_MULTS = BUILDER.comment("A list of multipliers that specifies how often the bag should contain all templates of each structure type.")
                .defineList("Bag Size Multiplier", List.of(defaultMult), mult -> mult instanceof Integer);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
