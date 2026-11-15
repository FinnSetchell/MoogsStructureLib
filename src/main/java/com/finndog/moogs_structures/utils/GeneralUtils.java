package com.finndog.moogs_structures.utils;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
//? if >=1.21.1 {
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
//?}
//? if >=1.21.1 <26.1.2 {
import net.minecraft.nbt.CompoundTag;
//?}
//? if >=1.21.1 <1.21.5 {
import net.minecraft.nbt.StringTag;
//?}
//? if <1.21.1 {
/*import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
*///?}
//? if >=1.21.11 <26.1.2 {
/*import net.minecraft.nbt.StringTag;
*///?}
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
//? if >=1.21.2 <1.21.5 {
/*import net.minecraft.tags.EnchantmentTags;
*///?}
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
//? if <1.21.2 {
import net.minecraft.world.level.BlockGetter;
//?}
//? if >=1.21.5 {
/*import net.minecraft.world.level.BlockGetter;
*///?}
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
//? if >=1.21.5 {
/*import net.minecraft.core.Holder.Reference;
*///?}
import java.nio.charset.StandardCharsets;
//? if <1.21.5 {
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//?} else {
/*import java.util.*;
*///?}
import java.util.concurrent.ConcurrentHashMap;

public final class GeneralUtils {
    private GeneralUtils() {}

    // Weighted Random from: https://stackoverflow.com/a/6737362
    public static <T> T getRandomEntry(List<Pair<T, Integer>> rlList, RandomSource random) {
        double totalWeight = 0.0;

        // Compute the total weight of all items together.
        for (Pair<T, Integer> pair : rlList) {
            totalWeight += pair.getSecond();
        }

        // Now choose a random item.
        int index = 0;
        for (double randomWeightPicked = random.nextFloat() * totalWeight; index < rlList.size() - 1; ++index) {
            randomWeightPicked -= rlList.get(index).getSecond();
            if (randomWeightPicked <= 0.0) break;
        }

        return rlList.get(index).getFirst();
    }

    //////////////////////////////

    private static final Map<BlockState, Boolean> IS_FULLCUBE_MAP = new ConcurrentHashMap<>();

    //? if <1.21.2 {
    public static boolean isFullCube(BlockGetter world, BlockPos pos, BlockState state) {
    //?}
    //? if >=1.21.2 <1.21.5 {
    /*public static boolean isFullCube(BlockState state) {
    *///?}
    //? if >=1.21.5 {
    /*public static boolean isFullCube(BlockGetter world, BlockPos pos, BlockState state) {
    *///?}
        if(state == null) return false;
        //? if <1.21.2 {
        return IS_FULLCUBE_MAP.computeIfAbsent(state, (stateIn) -> Block.isShapeFullBlock(stateIn.getOcclusionShape(world, pos)));
        //?} else {
        /*return IS_FULLCUBE_MAP.computeIfAbsent(state, (stateIn) -> Block.isShapeFullBlock(stateIn.getOcclusionShape()));
        *///?}
    }

    //////////////////////////////////////////////

    public static BlockState copyBlockProperties(BlockState oldBlockState, BlockState newBlockState) {
        for (Property<?> property : oldBlockState.getProperties()) {
            if (newBlockState.hasProperty(property)) {
                newBlockState = getStateWithProperty(newBlockState, oldBlockState, property);
            }
        }
        return newBlockState;
    }

    public static <T extends Comparable<T>> BlockState getStateWithProperty(BlockState state, BlockState stateToCopy, Property<T> property) {
        return state.setValue(property, stateToCopy.getValue(property));
    }

    //////////////////////////////////////////////

    //? if >=1.21.1 {
    public static ItemStack enchantRandomly(RegistryAccess registryAccess, RandomSource random, ItemStack itemToEnchant, float chance) {
    //?} else {
    /*public static ItemStack enchantRandomly(RandomSource random, ItemStack itemToEnchant, float chance) {
    *///?}
        //? if >=1.21.5 {
        /*Registry<Enchantment> enchReg = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        *///?}
        if(random.nextFloat() < chance) {
            //? if >=1.21.1 <1.21.2 {
            List<Holder.Reference<Enchantment>> list = registryAccess.registryOrThrow(Registries.ENCHANTMENT).holders()
                    .filter(holder -> holder.value().canEnchant(itemToEnchant)).toList();
            //?}
            //? if <1.21.1 {
            /*List<Enchantment> list = BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isDiscoverable)
                    .filter((enchantmentToCheck) -> enchantmentToCheck.canEnchant(itemToEnchant)).toList();
            *///?}
            //? if >=1.21.2 <1.21.5 {
            /*List<Holder.Reference<Enchantment>> list = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).listElements()
                    .filter(holder -> holder.value().canEnchant(itemToEnchant) && holder.is(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)).toList();
            *///?}
            //? if >=1.21.5 {
            /*List<Enchantment> list = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).stream()
                    .filter(holder -> holder.canEnchant(itemToEnchant)).toList();
            *///?}
            if(!list.isEmpty()) {
                //? if >=1.21.1 <1.21.5 {
                Holder.Reference<Enchantment> enchantment = list.get(random.nextInt(list.size()));
                //?}
                //? if <1.21.1 {
                /*Enchantment enchantment = list.get(random.nextInt(list.size()));
                *///?}
                // bias towards weaker enchantments
                //? if >=1.21.1 <1.21.5 {
                int enchantmentLevel = random.nextInt(Mth.nextInt(random, enchantment.value().getMinLevel(), enchantment.value().getMaxLevel()) + 1);
                //?}
                //? if <1.21.1 {
                /*int enchantmentLevel = random.nextInt(Mth.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel()) + 1);
                *///?}
                //? if <1.21.5 {
                itemToEnchant.enchant(enchantment, enchantmentLevel);
                //?} else {
                /*Optional<Reference<Enchantment>> enchantment = enchReg.get(enchReg.getKey(list.get(random.nextInt(list.size()))));
                enchantment.ifPresent(val -> {
                    int enchantmentLevel = random.nextInt(Mth.nextInt(random, val.value().getMinLevel(), val.value().getMaxLevel()) + 1);
                    itemToEnchant.enchant(val, enchantmentLevel);
                });
                *///?}
            }
        }

        return itemToEnchant;
    }

    //////////////////////////////////////////////

    public static int getMaxTerrainLimit(ChunkGenerator chunkGenerator) {
        return chunkGenerator.getMinY() + chunkGenerator.getGenDepth();
    }

    //////////////////////////////

    public static BlockPos getHighestLand(ChunkGenerator chunkGenerator, RandomState randomState, BoundingBox boundingBox, LevelHeightAccessor heightLimitView, boolean canBeOnLiquid) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(boundingBox.getCenter().getX(), getMaxTerrainLimit(chunkGenerator) - 40, boundingBox.getCenter().getZ());
        NoiseColumn blockView = chunkGenerator.getBaseColumn(mutable.getX(), mutable.getZ(), heightLimitView, randomState);
        BlockState currentBlockstate;
        while (mutable.getY() > chunkGenerator.getSeaLevel()) {
            currentBlockstate = blockView.getBlock(mutable.getY());
            if (!currentBlockstate.canOcclude()) {
                mutable.move(Direction.DOWN);
                continue;
            }
            else if (blockView.getBlock(mutable.getY() + 3).isAir() && (canBeOnLiquid ? !currentBlockstate.isAir() : currentBlockstate.canOcclude())) {
                return mutable;
            }
            mutable.move(Direction.DOWN);
        }

        return mutable;
    }


    public static BlockPos getLowestLand(ChunkGenerator chunkGenerator, RandomState randomState, BoundingBox boundingBox, LevelHeightAccessor heightLimitView, boolean canBeOnLiquid) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(boundingBox.getCenter().getX(), chunkGenerator.getSeaLevel() + 1, boundingBox.getCenter().getZ());
        NoiseColumn blockView = chunkGenerator.getBaseColumn(mutable.getX(), mutable.getZ(), heightLimitView, randomState);
        BlockState currentBlockstate = blockView.getBlock(mutable.getY());
        while (mutable.getY() <= getMaxTerrainLimit(chunkGenerator) - 40) {

            if((canBeOnLiquid ? !currentBlockstate.isAir() : currentBlockstate.canOcclude()) &&
                    blockView.getBlock(mutable.getY() + 1).isAir() &&
                    blockView.getBlock(mutable.getY() + 5).isAir())
            {
                mutable.move(Direction.UP);
                return mutable;
            }

            mutable.move(Direction.UP);
            currentBlockstate = blockView.getBlock(mutable.getY());
        }

        return mutable.set(mutable.getX(), chunkGenerator.getSeaLevel(), mutable.getZ());
    }

    /** {@link #getCeilingTop} found no ceiling the piece can hang from. */
    public static final int NO_CEILING = Integer.MIN_VALUE;

    /**
     * How far under the top of the terrain a hanging structure stays. The Nether's bedrock roof fills its top five
     * layers, so nothing hanging ever reaches into it or opens a way through.
     */
    public static final int CEILING_ROOF_MARGIN = 6;

    /**
     * The world Y for the top row of a piece hanging from a ceiling, or {@link #NO_CEILING}.
     *
     * <p>Reads the terrain before caves are carved, like the land searches above, so the answer only depends on
     * the seed. Scans down the piece's middle column from just under the roof for rock with air under it, and
     * looks at the piece's four corners too: the piece hangs from the highest of those five ceilings, so no
     * corner of its top hangs in the air, and its top is set into the rock where the ceiling is lower. A ceiling
     * only counts when, in all five columns, every row of the piece under the rock is air (never lava, water or
     * the floor) and every row set into the rock is rock. Otherwise the scan carries on down to the next ceiling.
     *
     * @param box             the piece's bounding box at its final x and z (its y doesn't matter)
     * @param sinkIntoRock    how many rows the piece's top goes up into the rock (0 = just under it)
     * @param lowestAllowedY  the lowest Y the piece's bottom may reach
     * @param highestAllowedY the highest Y the piece's top may reach
     * @param overGround      only hang where the air under the piece ends on rock, not on lava or water
     */
    public static int getCeilingTop(ChunkGenerator chunkGenerator, RandomState randomState, LevelHeightAccessor heightLimitView,
                                    BoundingBox box, int sinkIntoRock, int lowestAllowedY, int highestAllowedY, boolean overGround) {
        int height = box.getYSpan();
        int highestTop = Math.min(getMaxTerrainLimit(chunkGenerator) - CEILING_ROOF_MARGIN, highestAllowedY);
        int lowestBottom = Math.max(chunkGenerator.getMinY() + 1, lowestAllowedY);
        NoiseColumn[] columns = middleAndCorners(chunkGenerator, randomState, heightLimitView, box);
        NoiseColumn middle = columns[0];
        int[] ceilings = new int[columns.length];

        // The ceiling block, from the highest the piece's top may reach down to the lowest its bottom may reach.
        for (int ceiling = highestTop + 1 - sinkIntoRock; ceiling + sinkIntoRock - height >= lowestBottom; ceiling--) {
            if (!isRock(middle.getBlock(ceiling)) || !middle.getBlock(ceiling - 1).isAir()) {
                continue;
            }
            int anchor = ceiling;
            boolean fits = true;
            for (int i = 0; i < columns.length && fits; i++) {
                ceilings[i] = ceilingAt(columns[i], ceiling - 1, height);
                fits = ceilings[i] != NO_CEILING;
                anchor = Math.max(anchor, ceilings[i]);
            }
            int top = anchor - 1 + sinkIntoRock;
            int bottom = top - height + 1;
            if (!fits || top > highestTop || bottom < lowestBottom) {
                continue;
            }
            for (int i = 0; i < columns.length && fits; i++) {
                // The rows set into the rock must really be rock, not a thin shelf with a cave above it.
                for (int y = ceilings[i]; y <= top && fits; y++) {
                    fits = isRock(columns[i].getBlock(y));
                }
                int firstOpenRow = Math.min(top, ceilings[i] - 1);
                // A column wholly inside the rock means the piece is buried there, not hanging.
                fits = fits && firstOpenRow >= bottom;
                for (int y = firstOpenRow; y >= bottom && fits; y--) {
                    fits = columns[i].getBlock(y).isAir();
                }
                if (fits && overGround) {
                    fits = endsOnRock(columns[i], bottom - 1, chunkGenerator.getMinY());
                }
            }
            if (fits) {
                return top;
            }
        }
        return NO_CEILING;
    }

    /**
     * Whether a jigsaw child of a hanging structure, already at its final place, hangs clear: at its middle and
     * four corners, the rows under any rock it is set into are all air, never lava, water or the floor. With
     * {@code overGround}, the air under it must also end on rock. Read from the terrain before caves are carved,
     * like {@link #getCeilingTop}.
     */
    public static boolean hangsClear(ChunkGenerator chunkGenerator, RandomState randomState, LevelHeightAccessor heightLimitView,
                                     BoundingBox box, boolean overGround) {
        for (NoiseColumn column : middleAndCorners(chunkGenerator, randomState, heightLimitView, box)) {
            boolean inRock = true;
            for (int y = box.maxY(); y >= box.minY(); y--) {
                BlockState state = column.getBlock(y);
                if (inRock && isRock(state)) {
                    continue;
                }
                inRock = false;
                if (!state.isAir()) {
                    return false;
                }
            }
            if (overGround && !inRock && !endsOnRock(column, box.minY() - 1, chunkGenerator.getMinY())) {
                return false;
            }
        }
        return true;
    }

    /** The terrain columns at the middle and four corners of {@code box}, middle first. */
    private static NoiseColumn[] middleAndCorners(ChunkGenerator chunkGenerator, RandomState randomState, LevelHeightAccessor heightLimitView, BoundingBox box) {
        BlockPos center = box.getCenter();
        int[][] spots = {{center.getX(), center.getZ()}, {box.minX(), box.minZ()}, {box.minX(), box.maxZ()}, {box.maxX(), box.minZ()}, {box.maxX(), box.maxZ()}};
        NoiseColumn[] columns = new NoiseColumn[spots.length];
        for (int i = 0; i < spots.length; i++) {
            columns[i] = chunkGenerator.getBaseColumn(spots[i][0], spots[i][1], heightLimitView, randomState);
        }
        return columns;
    }

    /**
     * The ceiling over the open space at {@code row}: the first rock above it when {@code row} is air, or the
     * bottom of the rock {@code row} is in. Gives up after {@code reach} blocks, or at liquid.
     */
    private static int ceilingAt(NoiseColumn column, int row, int reach) {
        BlockState state = column.getBlock(row);
        if (state.isAir()) {
            for (int y = row + 1; y <= row + reach; y++) {
                state = column.getBlock(y);
                if (!state.isAir()) {
                    return isRock(state) ? y : NO_CEILING;
                }
            }
            return NO_CEILING;
        }
        if (!isRock(state)) {
            return NO_CEILING;
        }
        for (int y = row - 1; y > row - reach; y--) {
            state = column.getBlock(y);
            if (!isRock(state)) {
                return state.isAir() ? y + 1 : NO_CEILING;
            }
        }
        return NO_CEILING;
    }

    /** Whether the air going down from {@code y} ends on rock rather than liquid or the bottom of the world. */
    private static boolean endsOnRock(NoiseColumn column, int y, int minY) {
        for (; y >= minY; y--) {
            BlockState state = column.getBlock(y);
            if (!state.isAir()) {
                return isRock(state);
            }
        }
        return false;
    }

    /** The terrain before carving holds only the dimension's stone, its fluids and air. */
    private static boolean isRock(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    //////////////////////////////////////////////

    public static int getFirstLandYFromPos(LevelReader worldView, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        mutable.set(pos);
        ChunkAccess currentChunk = worldView.getChunk(mutable);
        BlockState currentState = currentChunk.getBlockState(mutable);

        //? if <1.21.2 {
        while(mutable.getY() >= worldView.getMinBuildHeight() && isReplaceableByStructures(currentState)) {
        //?} else {
        /*while(mutable.getY() >= worldView.getMinY() && isReplaceableByStructures(currentState)) {
        *///?}
            mutable.move(Direction.DOWN);
            currentState = currentChunk.getBlockState(mutable);
        }

        return mutable.getY();
    }

    private static boolean isReplaceableByStructures(BlockState blockState) {
        return blockState.isAir() || !blockState.getFluidState().isEmpty() || blockState.is(BlockTags.REPLACEABLE_BY_TREES);
    }

    //////////////////////////////////////////////

    public static void centerAllPieces(BlockPos targetPos, List<? extends StructurePiece> pieces) {
        if(pieces.isEmpty()) return;

        Vec3i structureCenter = pieces.get(0).getBoundingBox().getCenter();
        int xOffset = targetPos.getX() - structureCenter.getX();
        int zOffset = targetPos.getZ() - structureCenter.getZ();

        for(StructurePiece structurePiece : pieces) {
            structurePiece.move(xOffset, 0, zOffset);
        }
    }

    //////////////////////////////////////////////

    // More optimized with checking if the jigsaw blocks can connect
    //? if <1.21.2 {
    public static boolean canJigsawsAttach(StructureTemplate.StructureBlockInfo jigsaw1, StructureTemplate.StructureBlockInfo jigsaw2) {
        FrontAndTop prop1 = jigsaw1.state().getValue(JigsawBlock.ORIENTATION);
        FrontAndTop prop2 = jigsaw2.state().getValue(JigsawBlock.ORIENTATION);
    //?}
    //? if >=1.21.2 <1.21.5 {
    /*public static boolean canJigsawsAttach(StructureTemplate.JigsawBlockInfo jigsaw1, StructureTemplate.JigsawBlockInfo jigsaw2) {
        FrontAndTop prop1 = jigsaw1.info().state().getValue(JigsawBlock.ORIENTATION);
        FrontAndTop prop2 = jigsaw2.info().state().getValue(JigsawBlock.ORIENTATION);
    *///?}
    //? if >=1.21.5 {
    /*public static boolean canJigsawsAttach(StructureTemplate.StructureBlockInfo jigsaw1, StructureTemplate.StructureBlockInfo jigsaw2) {
        FrontAndTop prop1 = jigsaw1.state().getValue(JigsawBlock.ORIENTATION);
        FrontAndTop prop2 = jigsaw2.state().getValue(JigsawBlock.ORIENTATION);
    *///?}
        //? if >=26.1.2 {
        /*String joint = String.valueOf(jigsaw1.nbt().getString("joint"));
        if(joint.isEmpty()) {
            joint = prop1.front().getAxis().isHorizontal() ? "aligned" : "rollable";
        }
        *///?}
        //? if <1.21.1 {
        /*String joint = jigsaw1.nbt().getString("joint");
        if(joint.isEmpty()) {
            joint = prop1.front().getAxis().isHorizontal() ? "aligned" : "rollable";
        }
        *///?}

        //? if >=26.1.2 {
        /*boolean isRollable = joint.equals("rollable");
        *///?}
        //? if <1.21.1 {
        /*boolean isRollable = joint.equals("rollable");
        *///?}
        return prop1.front() == prop2.front().getOpposite() &&
                //? if >=1.21.1 <26.1.2 {
                (prop1.top() == prop2.top() || isRollableJoint(jigsaw1, prop1)) &&
                //?}
                //? if >=1.21.1 <1.21.2 {
                getStringMicroOptimised(jigsaw1.nbt(), "target").equals(getStringMicroOptimised(jigsaw2.nbt(), "name"));
                //?}
                //? if >=1.21.2 <1.21.5 {
                /*getStringMicroOptimised(jigsaw1.info().nbt(), "target").equals(getStringMicroOptimised(jigsaw2.info().nbt(), "name"));
                *///?}
                //? if >=1.21.5 <26.1.2 {
                /*getStringMicroOptimised(jigsaw1.nbt(), "target").equals(getStringMicroOptimised(jigsaw2.nbt(), "name"));
                *///?}
    //? if >=1.21.1 <26.1.2 {
    }

    //?}
    //? if >=1.21.1 <1.21.2 {
    private static boolean isRollableJoint(StructureTemplate.StructureBlockInfo jigsaw1, FrontAndTop prop1) {
        String joint = getStringMicroOptimised(jigsaw1.nbt(), "joint");
    //?}
    //? if >=1.21.2 <1.21.5 {
    /*private static boolean isRollableJoint(StructureTemplate.JigsawBlockInfo jigsaw1, FrontAndTop prop1) {
        String joint = getStringMicroOptimised(jigsaw1.info().nbt(), "joint");
    *///?}
    //? if >=1.21.5 <26.1.2 {
    /*private static boolean isRollableJoint(StructureTemplate.StructureBlockInfo jigsaw1, FrontAndTop prop1) {
        String joint = getStringMicroOptimised(jigsaw1.nbt(), "joint");
    *///?}
        //? if >=1.21.1 <26.1.2 {
        if(!joint.equals("rollable") && !joint.equals("aligned")) {
            return !prop1.front().getAxis().isHorizontal();
        }
        else {
            return joint.equals("rollable");
        }
    }

    public static String getStringMicroOptimised(CompoundTag tag, String key) {
        //?}
        //? if >=1.21.1 <1.21.5 {
        return tag.get(key) instanceof StringTag stringTag ? stringTag.getAsString() : "";
        //?}
                //? if <1.21.1 {
                /*(isRollable || prop1.top() == prop2.top()) &&
                jigsaw1.nbt().getString("target").equals(jigsaw2.nbt().getString("name"));
                *///?}
        //? if >=1.21.5 <1.21.11 {
        /*return tag.getString(key).orElse("");
        *///?}
        //? if >=1.21.11 <26.1.2 {
        /*return tag.get(key) instanceof StringTag stringTag ? stringTag.value() : "";
        *///?}
                //? if >=26.1.2 {
                /*(isRollable || prop1.top() == prop2.top()) &&
                jigsaw1.nbt().getString("target").equals(jigsaw2.nbt().getString("name"));
                *///?}
    }

    //////////////////////////////////////////////

    /**
     * Will grab all JSON objects from all datapacks's folder that is specified by the dataType parameter.
     *
     * @return - A map of paths (identifiers) to a list of all JSON elements found under it from all datapacks.
     */
    public static Map<ResourceLocation, List<JsonElement>> getAllDatapacksJSONElement(ResourceManager resourceManager, Gson gson, String dataType, int fileSuffixLength) {
        Map<ResourceLocation, List<JsonElement>> map = new HashMap<>();
        int dataTypeLength = dataType.length() + 1;

        // Finds all JSON files paths within the pool_additions folder. NOTE: this is just the path rn. Not the actual files yet.
        for (Map.Entry<ResourceLocation, List<Resource>> resourceStackEntry : resourceManager.listResourceStacks(dataType, (fileString) -> fileString.toString().endsWith(".json")).entrySet()) {
            String identifierPath = resourceStackEntry.getKey().getPath();
            //? if >=1.21.1 {
            ResourceLocation fileID = ResourceLocation.fromNamespaceAndPath(
            //?} else {
            /*ResourceLocation fileID = new ResourceLocation(
            *///?}
                    resourceStackEntry.getKey().getNamespace(),
                    identifierPath.substring(dataTypeLength, identifierPath.length() - fileSuffixLength));

            try {
                // getAllFileStreams will find files with the given ID. This part is what will loop over all matching files from all datapacks.
                for (Resource resource : resourceStackEntry.getValue()) {
                    InputStream fileStream = resource.open();
                    try (Reader bufferedReader = new BufferedReader(new InputStreamReader(fileStream, StandardCharsets.UTF_8))) {
                        // Get the JSON from the file
                        JsonElement countsJSONElement = GsonHelper.fromJson(gson, bufferedReader, (Class<? extends JsonElement>) JsonElement.class);
                        if (countsJSONElement != null) {

                            // Create list in map for the ID if non exists yet for that ID
                            if (!map.containsKey(fileID)) {
                                map.put(fileID, new ArrayList<>());
                            }
                            // Add the parsed json to the list we will merge later on
                            map.get(fileID).add(countsJSONElement);
                        }
                        else {
                            MoogsStructuresCommon.LOGGER.error(
                                    "(Moog's Structure Lib {} MERGER) Couldn't load data file {} from {} as it's null or empty",
                                    dataType,
                                    fileID,
                                    resourceStackEntry);
                        }
                    }
                }
            }
            catch (IllegalArgumentException | IOException | JsonParseException exception) {
                MoogsStructuresCommon.LOGGER.error(
                        "(Moog's Structure Lib {} MERGER) Couldn't parse data file {} from {}",
                        dataType,
                        fileID,
                        resourceStackEntry,
                        exception);
            }
        }

        return map;
    }

    ///////////////////////////////////////////

    private static final ConcurrentHashMap<HeightKey, Integer> CACHED_HEIGHT = new ConcurrentHashMap<>(2048);
    private record HeightKey(ChunkGenerator chunkGenerator, int x, int z, Heightmap.Types types) {}

    public static int getCachedFreeHeight(ChunkGenerator chunkGenerator, int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        HeightKey key = new HeightKey(chunkGenerator, x, z, types);
        Integer y = CACHED_HEIGHT.get(key);

        if (y == null) {
            if (CACHED_HEIGHT.size() >= 2048) {
                CACHED_HEIGHT.clear();
            }
            y = chunkGenerator.getFirstFreeHeight(x, z, types, levelHeightAccessor, randomState);
            CACHED_HEIGHT.put(key, y);
        }

        return y;
    }

}