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

package com.rk_exxec.random_bag;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import java.util.regex.Pattern; 
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(RandomBag.MODID)
public class RandomBag
{
    public static final String MODID = "random_bag";

    public static final Logger LOGGER = LogManager.getLogger(RandomBag.MODID);

    public static ArrayList<String> matchPatterns = new ArrayList<String>();

    public static Hashtable<Pattern, Integer> matchPatternCycle = new Hashtable<Pattern,Integer>();

    public static Hashtable<String, Integer> matchPatternCache = new Hashtable<String,Integer>();

    public RandomBag(FMLJavaModLoadingContext context)
    {
        LOGGER.info("Hello from " + MODID);
        IEventBus modEventBus = context.getModEventBus();
        // Register custom structure types for datapack structures
        // ModStructureTypes.STRUCTURE_TYPES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    // @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent e){
        LOGGER.info("Loading config");
        matchPatternCycle.clear();
        matchPatternCache.clear();

        if(CommonConfig.ALLOWED_RESOURCE_LOCATIONS.get().size() < 1){
            LOGGER.error("Not enough elements in allowed resource locations list, reverting to default.");
            CommonConfig.ALLOWED_RESOURCE_LOCATIONS.set(CommonConfig.ALLOWED_RESOURCE_LOCATIONS.getDefault());
        }

        int size_diff = CommonConfig.ALLOWED_RESOURCE_LOCATIONS.get().size() - CommonConfig.BAG_SIZE_MULTS.get().size() ;
        if(CommonConfig.BAG_SIZE_MULTS.get().size() < CommonConfig.ALLOWED_RESOURCE_LOCATIONS.get().size() ){
            LOGGER.error("Must contain same amount of parameters as 'Allowed resource locations', setting missing multis to 1");
            List<Integer> existIntegers = (List<Integer>) CommonConfig.BAG_SIZE_MULTS.get();
            for(int i=0; i<size_diff; i++){
                existIntegers.add(CommonConfig.defaultMult);
            }
            CommonConfig.BAG_SIZE_MULTS.set(existIntegers);
        }

        if(CommonConfig.BAG_SIZE_MULTS.get().size() > CommonConfig.ALLOWED_RESOURCE_LOCATIONS.get().size() ){
            LOGGER.error("Must contain same amount of parameters as 'Allowed resource locations', truncating additional values");

            CommonConfig.BAG_SIZE_MULTS.set(CommonConfig.BAG_SIZE_MULTS.get().subList(0,-size_diff));
        }

        for (int i = 0; i< CommonConfig.ALLOWED_RESOURCE_LOCATIONS.get().size(); i++) {
            Pattern pat = Pattern.compile("\\[" + CommonConfig.ALLOWED_RESOURCE_LOCATIONS.get().get(i).replace("*", ".*") + "\\]");
            matchPatternCycle.put(pat, CommonConfig.BAG_SIZE_MULTS.get().get(i));
            RandomBag.LOGGER.info("Parsing match pattern: "+ CommonConfig.ALLOWED_RESOURCE_LOCATIONS.get().get(i));
        }


    }
}
