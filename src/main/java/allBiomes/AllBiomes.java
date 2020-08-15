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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public static final int BIOME_DISTANCE = 1536;
    public static final int BIOME_DISTANCE_256 = BIOME_DISTANCE / 256;

    public static void main(String[] args) {
        List<Long> quads = getQuadRegionSeeds().boxed().collect(Collectors.toList());
        List<Long> quad_output = new ArrayList<>();
        quads.forEach(regionSeed -> {
            for (int regionX = -MAX_QUAD_DISTANCE; regionX < MAX_QUAD_DISTANCE; regionX++) {
                for (int regionZ = -MAX_QUAD_DISTANCE; regionZ < MAX_QUAD_DISTANCE; regionZ++) {
                    long structureSeed = regionSeed - SWAMP_HUT.getSalt() - regionX * RegionSeed.A - regionZ * RegionSeed.B;
                    quad_output.addAll(searchSeed(structureSeed, regionX, regionZ));
                }
            }
        });
        quad_output.removeIf(ws -> isInvalidArea(ws, AllBiomes::hasMansion));
        quad_output.removeIf(ws -> isInvalidArea(ws, AllBiomes::hasDesertTemple));
        quad_output.removeIf(ws -> isInvalidArea(ws, AllBiomes::hasIgloo));
        quad_output.removeIf(ws -> isInvalidArea(ws, AllBiomes::hasJungleTemple));
        quad_output.removeIf(ws -> isInvalidArea(ws, AllBiomes::hasVillage));
        quad_output.removeIf(ws -> isInvalidArea(ws, AllBiomes::hasOceanMonument));
        quad_output.removeIf(ws -> isInvalidArea(ws, AllBiomes::hasOutpost));
    }

    private static boolean isInvalidArea(long worldSeed, BiFunction<Long, CPos, Boolean> filter) {
        for (int regionX = -MAX_QUAD_DISTANCE; regionX < MAX_QUAD_DISTANCE; regionX++) {
            for (int regionZ = -MAX_QUAD_DISTANCE; regionZ < MAX_QUAD_DISTANCE; regionZ++) {
                if (!filter.apply(worldSeed, new CPos(regionX, regionZ))) return true;
            }
        }
        return false;
    }

    private static boolean hasMansion(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos mansion = MANSION.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return MANSION.canSpawn(mansion.getX(), mansion.getZ(), source);
    }

    private static boolean hasIgloo(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos mansion = IGLOO.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return IGLOO.canSpawn(mansion.getX(), mansion.getZ(), source);
    }

    private static boolean hasDesertTemple(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos mansion = DESERT_TEMPLE.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return DESERT_TEMPLE.canSpawn(mansion.getX(), mansion.getZ(), source);
    }

    private static boolean hasVillage(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos mansion = VILLAGE.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return VILLAGE.canSpawn(mansion.getX(), mansion.getZ(), source);
    }

    private static boolean hasJungleTemple(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos mansion = JUNGLE_TEMPLE.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return JUNGLE_TEMPLE.canSpawn(mansion.getX(), mansion.getZ(), source);
    }

    private static boolean hasOutpost(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos mansion = OUTPOST.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return OUTPOST.canSpawn(mansion.getX(), mansion.getZ(), source);
    }

    private static boolean hasOceanMonument(long worldSeed, CPos reg) {
        ChunkRand rand = new ChunkRand();
        CPos mansion = OCEAN_MONUMENT.getInRegion(worldSeed & Mth.MASK_48, reg.getX(), reg.getZ(), rand);
        OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
        return OCEAN_MONUMENT.canSpawn(mansion.getX(), mansion.getZ(), source);
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
            if (!checkBiome(b -> b.getCategory() == Biome.Category.JUNGLE, 512, source)) continue;
            if (!checkBiome(b -> b == Biome.ICE_SPIKES, 128, source)) continue;
            if (!checkBiome(b -> b == Biome.FLOWER_FOREST, 128, source)) continue;
            if (!SWAMP_HUT.canSpawn(hut2.getX(), hut2.getZ(), source)) continue;
            if (!SWAMP_HUT.canSpawn(hut3.getX(), hut3.getZ(), source)) continue;
            if (!SWAMP_HUT.canSpawn(hut4.getX(), hut4.getZ(), source)) continue;
            res.add(worldSeed);
        }
        return res;
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

    private static boolean checkMushroom(List<CPos> regions, OverworldBiomeSource source) {
        long layerSeed = BiomeLayer.getLayerSeed(source.getWorldSeed(), 5L);

        for (CPos region : regions) {
            long localSeed = BiomeLayer.getLocalSeed(layerSeed, region.getX(), region.getZ()) >> 24;
            if ((int) Math.floorMod(localSeed, 100) != 0) continue; //nextInt(100) == 0 in the region to get mushroom
            if (source.base.sample(region.getX(), 0, region.getZ()) != Biome.MUSHROOM_FIELDS.getId()) continue;
            return true;
        }

        return false;
    }

    private static boolean checkBiome(Predicate<Biome> biomePredicate, int increment, OverworldBiomeSource source) {
        for (int ox = -BIOME_DISTANCE; ox < BIOME_DISTANCE; ox += increment) {
            for (int oz = -BIOME_DISTANCE; oz < BIOME_DISTANCE; oz += increment) {
                Biome biome = source.getBiomeForNoiseGen(ox >> 2, 0, oz >> 2);
                if (biomePredicate.test(biome)) return true;
            }
        }

        return false;
    }

    private static LongStream getQuadRegionSeeds() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("allBiomes/all_quad_region_seeds.txt")));
            return reader.lines().mapToLong(Long::parseLong);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return LongStream.empty();
    }

}
