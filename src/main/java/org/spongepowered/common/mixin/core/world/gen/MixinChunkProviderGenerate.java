/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <http://www.spongepowered.org>
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
package org.spongepowered.common.mixin.core.world.gen;

import com.google.common.collect.Lists;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.world.gen.populators.AnimalPopulator;

import java.util.List;
import java.util.Random;

@Mixin(ChunkProviderGenerate.class)
public abstract class MixinChunkProviderGenerate implements IChunkProvider {

    // @formatter:off

    @Shadow private Random rand;
    @Shadow private World worldObj;
    @Shadow private boolean mapFeaturesEnabled;
    @Shadow private BiomeGenBase[] biomesForGeneration;
    @Shadow private ChunkProviderSettings settings;
    @Shadow private MapGenBase caveGenerator;
    @Shadow private MapGenStronghold strongholdGenerator;
    @Shadow private MapGenVillage villageGenerator;
    @Shadow private MapGenMineshaft mineshaftGenerator;
    @Shadow private MapGenScatteredFeature scatteredFeatureGenerator;
    @Shadow private MapGenBase ravineGenerator;
    @Shadow private StructureOceanMonument oceanMonumentGenerator;

    @Shadow public abstract void setBlocksInChunk(int x, int z, ChunkPrimer chunk);
    @Shadow public abstract void replaceBlocksForBiome(int x, int z, ChunkPrimer chunk, BiomeGenBase[] biomes);
    
    // @formatter:on

    private List<Populator> populators;
    private List<GeneratorPopulator> genpopulators;

    @Inject(method = "<init>(Lnet/minecraft/world/World;JZLjava/lang/String;)V", at = @At("RETURN") )
    public void onConstructed(World worldIn, long seed, boolean mapFeatures, String generatorOptions, CallbackInfo ci) {
        this.populators = Lists.newArrayList();
        this.genpopulators = Lists.newArrayList();

        if (this.settings.useCaves) {
            this.genpopulators.add((GeneratorPopulator) this.caveGenerator);
        }

        if (this.settings.useRavines) {
            this.genpopulators.add((GeneratorPopulator) this.ravineGenerator);
        }

        if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
            this.genpopulators.add((GeneratorPopulator) this.mineshaftGenerator);
        }

        if (this.settings.useVillages && this.mapFeaturesEnabled) {
            this.genpopulators.add((GeneratorPopulator) this.villageGenerator);
        }

        if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
            this.genpopulators.add((GeneratorPopulator) this.strongholdGenerator);
        }

        if (this.settings.useTemples && this.mapFeaturesEnabled) {
            this.genpopulators.add((GeneratorPopulator) this.scatteredFeatureGenerator);
        }

        if (this.settings.useMonuments && this.mapFeaturesEnabled) {
            this.genpopulators.add((GeneratorPopulator) this.oceanMonumentGenerator);
        }

        // BEGIN populators

        if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
            this.populators.add((Populator) this.mineshaftGenerator);
        }

        if (this.settings.useVillages && this.mapFeaturesEnabled) {
            this.populators.add((Populator) this.villageGenerator);
        }

        if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
            this.populators.add((Populator) this.strongholdGenerator);
        }

        if (this.settings.useTemples && this.mapFeaturesEnabled) {
            this.populators.add((Populator) this.scatteredFeatureGenerator);
        }

        if (this.settings.useMonuments && this.mapFeaturesEnabled) {
            this.populators.add((Populator) this.oceanMonumentGenerator);
        }

        if (this.settings.useWaterLakes) {
            this.populators.add((Populator) new WorldGenLakes(Blocks.water));
        }

        if (this.settings.useLavaLakes) {
            this.populators.add((Populator) new WorldGenLakes(Blocks.lava));
        }

        if (this.settings.useDungeons) {
            this.populators.add((Populator) new WorldGenDungeons());
        }

        this.populators.add(new AnimalPopulator());
        // this.populators.add(new SnowPopulator());
    }

    /**
     * 
     * This overwrites the provideChunk method in order to remove the standard
     * calls to GeneratorPopulators as these GeneratorPopulators have instead
     * been added to the genpopulator list from the injection into the
     * constructor {@link #onConstructed}.
     * 
     * @author Deamon
     */
    @Override
    @Overwrite
    public Chunk provideChunk(int x, int z) {
        this.rand.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();

        this.setBlocksInChunk(x, z, chunkprimer);
        this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.replaceBlocksForBiome(x, z, chunkprimer, this.biomesForGeneration);

        // BEGIN populator removal
        // These populators are moved to the genpopulator list

//        if (this.settings.useCaves) {
//            this.caveGenerator.populate(this, this.worldObj, x, z, chunkprimer);
//        }
//
//        if (this.settings.useRavines) {
//            this.ravineGenerator.populate(this, this.worldObj, x, z, chunkprimer);
//        }
//
//        if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
//            this.mineshaftGenerator.populate(this, this.worldObj, x, z, chunkprimer);
//        }
//
//        if (this.settings.useVillages && this.mapFeaturesEnabled) {
//            this.villageGenerator.populate(this, this.worldObj, x, z, chunkprimer);
//        }
//
//        if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
//            this.strongholdGenerator.populate(this, this.worldObj, x, z, chunkprimer);
//        }
//
//        if (this.settings.useTemples && this.mapFeaturesEnabled) {
//            this.scatteredFeatureGenerator.populate(this, this.worldObj, x, z, chunkprimer);
//        }
//
//        if (this.settings.useMonuments && this.mapFeaturesEnabled) {
//            this.oceanMonumentGenerator.populate(this, this.worldObj, x, z, chunkprimer);
//        }

        // END populator removal

        Chunk chunk = new Chunk(this.worldObj, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int k = 0; k < abyte.length; ++k) {
            abyte[k] = (byte) this.biomesForGeneration[k].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
    }

}
