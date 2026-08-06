# Random Bag Structure Select

A simple mod to change the selection algorithm for minecraft structure variants.    
Instead of truly random selections, where sometimes a certain variant JUST WILL NOT SPAWN, all the variants are shuffled like a deck of cards and then drawn 1 by 1.  
This guarantees every type will generate at least once before there are duplicates.  

Works with custom datapacks.
The weight specified in the datapack is still respected, if >1 the variant is added multiple times to the deck.



## Config

- ALLOWED_STRUCTURE_POOLS: Array of strings of the resource locations of your structures affected by the change. Supports wildcards (*). Examples: `minecraft:trail_ruins*`, `minecraft:village/*/houses`, `mydatapck:*`
- BAG_SIZE_MULTS: This determines how many "decks" are shuffeled together at once. Default is 1, but can be increased to allow some more variation.

