package allBiomes;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.mathutils.util.Mth;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.mc.seed.RegionSeed;

import java.io.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class AllBiomes {

    public static final SwampHut SWAMP_HUT = new SwampHut(MCVersion.v1_16);
    public static final Mansion MANSION = new Mansion(MCVersion.v1_16);
    public static final DesertPyramid DESERT_TEMPLE = new DesertPyramid(MCVersion.v1_16);
    public static final Village VILLAGE = new Village(MCVersion.v1_16);
    public static final PillagerOutpost OUTPOST = new PillagerOutpost(MCVersion.v1_16);
    public static final Monument OCEAN_MONUMENT = new Monument(MCVersion.v1_16);
    public static final JunglePyramid JUNGLE_TEMPLE = new JunglePyramid(MCVersion.v1_16);
    public static final Igloo IGLOO = new Igloo(MCVersion.v1_16);

    public static final int MAX_QUAD_DISTANCE = (2048 >> 4) / SWAMP_HUT.getSpacing();

    public static final int BIOME_DISTANCE = 4000;
    public static final int BIOME_DISTANCE_256 = BIOME_DISTANCE / 256;

    public static void main(String[] args) {
        System.out.println("Starting the process, this might take a while...");
        List<Long> quads = getQuadRegionSeeds().boxed().collect(Collectors.toList());
        Collections.shuffle(quads);
        List<Long> quad_output = new ArrayList<>();
        long time=System.nanoTime();
        long i=0;
        for (Long regionSeed : quads) {
            for (int regionX = -MAX_QUAD_DISTANCE; regionX < MAX_QUAD_DISTANCE; regionX++) {
                for (int regionZ = -MAX_QUAD_DISTANCE; regionZ < MAX_QUAD_DISTANCE; regionZ++) {
                    long structureSeed = regionSeed - SWAMP_HUT.getSalt() - regionX * RegionSeed.A - regionZ * RegionSeed.B;
                    quad_output.addAll(searchSeed(structureSeed, regionX, regionZ));
                }
            }
            i++;
            if (i%100==0){
                System.out.println("We are at "+i/(double)quads.size()*100+"% over the total and at time "+(System.nanoTime()-time)/1.0e9);
            }
        }
        quad_output.forEach(System.out::println);
    }

    private static ArrayList<Long> searchSeed(long structureSeed, int quadRegionX, int quadRegionZ) {
        ArrayList<Long> res = new ArrayList<>();
        ChunkRand rand = new ChunkRand();
        CPos hut1 = SWAMP_HUT.getInRegion(structureSeed, quadRegionX, quadRegionZ, rand);
        CPos hut2 = SWAMP_HUT.getInRegion(structureSeed, quadRegionX - 1, quadRegionZ, rand);
        CPos hut3 = SWAMP_HUT.getInRegion(structureSeed, quadRegionX, quadRegionZ - 1, rand);
        CPos hut4 = SWAMP_HUT.getInRegion(structureSeed, quadRegionX - 1, quadRegionZ - 1, rand);

        List<CPos> potentialMushroomRegions = getPotentialMushroomRegions(structureSeed);
        if (potentialMushroomRegions.isEmpty()) return res;

        for (long upperBits = 0; upperBits < 1L << 16; upperBits++) {
            long worldSeed = (upperBits << 48) | structureSeed;
            OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
            if (!checkMushroom(potentialMushroomRegions, source)) continue;
            if (!SWAMP_HUT.canSpawn(hut1.getX(), hut1.getZ(), source)) continue;
            if (!SWAMP_HUT.canSpawn(hut2.getX(), hut2.getZ(), source)) continue;
            if (!SWAMP_HUT.canSpawn(hut3.getX(), hut3.getZ(), source)) continue;
            if (!SWAMP_HUT.canSpawn(hut4.getX(), hut4.getZ(), source)) continue;
            // check for the structures requirements
            if (!isValidArea(worldSeed, AllBiomes::hasMansion)) continue;
            if (!isValidArea(worldSeed, AllBiomes::hasDesertTemple)) continue;
            if (!isValidArea(worldSeed, AllBiomes::hasIgloo)) continue;
            if (!isValidArea(worldSeed, AllBiomes::hasJungleTemple)) continue;
            if (!isValidArea(worldSeed, AllBiomes::hasVillage)) continue;
            if (!isValidArea(worldSeed, AllBiomes::hasOceanMonument)) continue;
            if (!isValidArea(worldSeed, AllBiomes::hasOutpost)) continue;
            ////check the biomes requirements
            if (!isValidBiome(b -> b.getCategory() == Biome.Category.JUNGLE, 512, source)) continue;
            if (!isValidBiome(b -> b == Biome.BADLANDS, 128, source)) continue;
            if (!isValidBiome(b -> b == Biome.LUKEWARM_OCEAN, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.ICE_SPIKES, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.GIANT_TREE_TAIGA, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.FLOWER_FOREST, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.FROZEN_OCEAN, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.FLOWER_FOREST, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.DARK_FOREST, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.BIRCH_FOREST, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.BAMBOO_JUNGLE, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.TAIGA, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.SAVANNA, 128, source)) continue;
            //if (!isValidBiome(b -> b == Biome.DESERT, 128, source)) continue;
            System.out.println(worldSeed);
            res.add(worldSeed);
        }
        System.out.printf("Found %d world seeds for structure seed %d%n",res.size(),structureSeed);
        return res;
    }

    private static boolean isValidArea(long worldSeed, BiFunction<Long, CPos, Boolean> filter) {
        for (int regionX = -MAX_QUAD_DISTANCE; regionX < MAX_QUAD_DISTANCE; regionX++) {
            for (int regionZ = -MAX_QUAD_DISTANCE; regionZ < MAX_QUAD_DISTANCE; regionZ++) {
                if (filter.apply(worldSeed, new CPos(regionX, regionZ))) return true;
            }
        }
        return false;
    }

    private static boolean hasMansion(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos structure = MANSION.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return structure!=null && MANSION.canSpawn(structure.getX(), structure.getZ(), source);
    }

    private static boolean hasIgloo(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos structure = IGLOO.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return structure!=null && IGLOO.canSpawn(structure.getX(), structure.getZ(), source);
    }

    private static boolean hasDesertTemple(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos structure = DESERT_TEMPLE.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return structure!=null && DESERT_TEMPLE.canSpawn(structure.getX(), structure.getZ(), source);
    }

    private static boolean hasVillage(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos structure = VILLAGE.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return structure!=null && VILLAGE.canSpawn(structure.getX(), structure.getZ(), source);
    }

    private static boolean hasJungleTemple(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos structure = JUNGLE_TEMPLE.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return structure!=null && JUNGLE_TEMPLE.canSpawn(structure.getX(), structure.getZ(), source);
    }

    private static boolean hasOutpost(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos structure = OUTPOST.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return structure!=null && OUTPOST.canSpawn(structure.getX(), structure.getZ(), source);
    }

    private static boolean hasOceanMonument(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos structure = OCEAN_MONUMENT.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return structure!=null && OCEAN_MONUMENT.canSpawn(structure.getX(), structure.getZ(), source);
    }



    private static List<CPos> getPotentialMushroomRegions(long structureSeed) {
        List<CPos> regions = new ArrayList<>();
        long layerSeed = BiomeLayer.getLayerSeed(structureSeed, 5L);

        for (int regionX = -BIOME_DISTANCE_256; regionX < BIOME_DISTANCE_256; regionX++) {
            for (int regionZ = -BIOME_DISTANCE_256; regionZ < BIOME_DISTANCE_256; regionZ++) {
                //nextInt(100) == 0 implies nextInt(4) == 0 on the 26 lowest bits of world seed
                long localSeed = BiomeLayer.getLocalSeed(layerSeed, regionX, regionZ) >> 24;
                if (localSeed % 4 == 0) regions.add(new CPos(regionX, regionZ));
            }
        }

        return regions;
    }
    private static long floorMod(long x, long y) {
        long mod = x % y;
        // if the signs are different and modulo not zero, adjust result
        if ((x ^ y) < 0 && mod != 0) {
            mod += y;
        }
        return mod;
    }
    private static boolean checkMushroom(List<CPos> regions, OverworldBiomeSource source) {
        long layerSeed = BiomeLayer.getLayerSeed(source.getWorldSeed(), 5L);
        for (CPos region : regions) {
            long localSeed = BiomeLayer.getLocalSeed(layerSeed, region.getX(), region.getZ()) >> 24;
            if (floorMod(localSeed, 100) != 0) continue; //nextInt(100) == 0 in the region to get mushroom
            if (source.base.sample(region.getX(), 0, region.getZ()) != Biome.MUSHROOM_FIELDS.getId()) continue;
            return true;
        }
        return false;
    }

    private static boolean isValidBiome(Predicate<Biome> biomePredicate, int increment, OverworldBiomeSource source) {
        for (int ox = -BIOME_DISTANCE; ox < BIOME_DISTANCE; ox += increment) {
            for (int oz = -BIOME_DISTANCE; oz < BIOME_DISTANCE; oz += increment) {
                Biome biome = source.getBiomeForNoiseGen(ox >> 2, 0, oz >> 2);
                if (biomePredicate.test(biome)) return true;
            }
        }
        return false;
    }

    private static LongStream getQuadRegionSeeds() {
        InputStream in = AllBiomes.class.getResourceAsStream("all_quad_region_seeds.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return reader.lines().mapToLong(Long::parseLong);
    }

}
