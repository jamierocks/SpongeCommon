/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.world.gen;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.gen.IFlaggedPopulator;
import org.spongepowered.common.util.gen.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.util.gen.ChunkPrimerBuffer;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Similar class to {@link ChunkProviderGenerate}, but instead gets its blocks
 * from a custom chunk generator.
 */
public class SpongeChunkProvider implements WorldGenerator, IChunkProvider {

    private static final Vector2i CHUNK_AREA = new Vector2i(16, 16);

    protected BiomeGenerator biomeGenerator;
    protected GeneratorPopulator baseGenerator;
    protected List<GeneratorPopulator> genpop;
    protected List<Populator> pop;
    protected Map<BiomeType, BiomeGenerationSettings> overrides;
    protected final World world;
    private final ByteArrayMutableBiomeBuffer cachedBiomes;

    public SpongeChunkProvider(World world, GeneratorPopulator base, BiomeGenerator biomegen) {
        this.world = checkNotNull(world, "world");
        this.baseGenerator = checkNotNull(base, "baseGenerator");
        this.biomeGenerator = checkNotNull(biomegen, "biomeGenerator");

        // Make initially empty biome cache
        this.cachedBiomes = new ByteArrayMutableBiomeBuffer(Vector2i.ZERO, CHUNK_AREA);
        this.cachedBiomes.detach();
        
        this.genpop = Lists.newArrayList();
        this.pop = Lists.newArrayList();
        this.overrides = Maps.newHashMap();
    }

    @Override
    public GeneratorPopulator getBaseGeneratorPopulator() {
        return this.baseGenerator;
    }

    @Override
    public void setBaseGeneratorPopulator(GeneratorPopulator baseGeneratorPopulator) {
        this.baseGenerator = baseGeneratorPopulator;
    }

    @Override
    public List<GeneratorPopulator> getGeneratorPopulators() {
        return this.genpop;
    }

    public void setGeneratorPopulators(List<GeneratorPopulator> generatorPopulators) {
        this.genpop = Lists.newArrayList(generatorPopulators);
    }

    @Override
    public List<Populator> getPopulators() {
        return this.pop;
    }

    public void setPopulators(List<Populator> populators) {
        this.pop = Lists.newArrayList(populators);
    }

    public Map<BiomeType, BiomeGenerationSettings> getBiomeOverrides() {
        return this.overrides;
    }

    public void setBiomeOverrides(Map<BiomeType, BiomeGenerationSettings> biomeOverrides) {
        this.overrides = Maps.newHashMap(biomeOverrides);
    }

    @Override
    public BiomeGenerator getBiomeGenerator() {
        return this.biomeGenerator;
    }

    @Override
    public void setBiomeGenerator(BiomeGenerator biomeGenerator) {
        this.biomeGenerator = biomeGenerator;
    }

    @Override
    public Optional<BiomeGenerationSettings> getBiomeOverride(BiomeType type) {
        return Optional.fromNullable(this.overrides.get(type));
    }

    @Override
    public boolean isBiomeOverriden(BiomeType type) {
        return this.overrides.containsKey(type);
    }

    @Override
    public void setBiomeOverride(BiomeType type, BiomeGenerationSettings settings) {
        this.overrides.put(type, settings);
    }

    @Override
    public Chunk provideChunk(int chunkX, int chunkZ) {

        this.cachedBiomes.reuse(new Vector2i(chunkX * 16, chunkZ * 16));
        this.biomeGenerator.generateBiomes(this.cachedBiomes);

        // Generate base terrain
        ChunkPrimer chunkprimer = new ChunkPrimer();
        MutableBlockVolume blockBuffer = new ChunkPrimerBuffer(chunkprimer, chunkX, chunkZ);
        ImmutableBiomeArea biomeBuffer = this.cachedBiomes.getImmutableBiomeCopy();
        this.baseGenerator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);

        // Apply the generator populators to complete the blockBuffer
        for (GeneratorPopulator populator : this.genpop) {
            populator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);
        }
        
        //Get unique biomes to determine what generator populators to run
        List<BiomeType> uniqueBiomes = Lists.newArrayList();
        BiomeType biome;
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                biome = this.cachedBiomes.getBiome(x, z);
                if(!uniqueBiomes.contains(biome)) {
                    uniqueBiomes.add(biome);
                }
            }
        }
        
        //run our generator populators, checking for overrides from the generator
        for(BiomeType type: uniqueBiomes) {
            if(this.overrides.containsKey(type)) {
                for(GeneratorPopulator populator: this.overrides.get(type).getGeneratorPopulators()) {
                    populator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);
                    
                }
            } else {
                for(GeneratorPopulator populator: type.getGenerationSettings().getGeneratorPopulators()) {
                    populator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);
                    
                }
            }
        }

        // Assemble chunk
        Chunk chunk = new Chunk(this.world, chunkprimer, chunkX, chunkZ);
        byte[] biomeArray = chunk.getBiomeArray();
        System.arraycopy(this.cachedBiomes.detach(), 0, biomeArray, 0, biomeArray.length);
        chunk.generateSkylightMap();

        return chunk;
    }

    @Override
    public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ) {
        Random random = new Random(chunkX * 341873128712L + chunkZ * 132897987541L);
        BlockFalling.fallInstantly = true;

        BlockPos blockpos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        BiomeType biome = (BiomeType) this.world.getBiomeGenForCoords(blockpos.add(16, 0, 16));

        // Calling the events makes the Sponge-added populators fire
        org.spongepowered.api.world.Chunk chunk = (org.spongepowered.api.world.Chunk) this.world.getChunkFromChunkCoords(chunkX, chunkZ);
        
        List<Populator> populators = Lists.newArrayList(this.pop);
        if (this.overrides.containsKey(biome)) {
            populators.addAll(this.overrides.get(biome).getPopulators());
        } else {
            populators.addAll(biome.getGenerationSettings().getPopulators());
        }
        Sponge.getGame().getEventManager().post(SpongeEventFactory.createChunkPrePopulate(Sponge.getGame(), chunk, populators));

        List<String> flags = Lists.newArrayList();
        for (Populator populator : populators) {
            if (populator instanceof IFlaggedPopulator) {
                ((IFlaggedPopulator) populator).populate(chunkProvider, chunk, random, flags);
            } else {
                populator.populate(chunk, random);
            }
        }

        Sponge.getGame().getEventManager().post(SpongeEventFactory.createChunkPostPopulate(Sponge.getGame(), chunk));

        BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean func_177460_a(IChunkProvider chunkProvider, Chunk chunk, int chunkX, int chunkZ) {
        // This method normally places ocean monuments in existing chunks
        return false;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        BiomeGenBase biome = this.world.getBiomeGenForCoords(pos);
        @SuppressWarnings("unchecked")
        List<SpawnListEntry> creatures = biome.getSpawnableList(creatureType);
        return creatures;
    }

    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        // TODO Maybe allow to register stronghold locations?
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunk, int chunkX, int chunkZ) {
        // TODO No structure support
    }

    // Methods below are simply mirrors of the methods in ChunkProviderGenerate

    @Override
    public Chunk provideChunk(BlockPos blockPosIn) {
        return provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public String makeString() {
        return "RandomLevelSource";
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public boolean saveChunks(boolean bool, IProgressUpdate progressUpdate) {
        return true;
    }

    @Override
    public void saveExtraData() {

    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return true;
    }

}
