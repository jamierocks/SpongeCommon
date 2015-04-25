/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
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
package org.spongepowered.common.world.biome;

import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeGenerationSettingsBuilder;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;

import java.util.Collection;

public class SpongeBiomeGenerationSettingsBuilder implements BiomeGenerationSettingsBuilder {
    
    private float max;
    private float min;

    @Override
    public BiomeGenerationSettingsBuilder minHeight(float height) {
        this.min = height;
        return this;
    }

    @Override
    public BiomeGenerationSettingsBuilder maxHeight(float height) {
        this.max = height;
        return this;
    }

    @Override
    public BiomeGenerationSettingsBuilder groundCoverLayers(GroundCoverLayer... layers) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BiomeGenerationSettingsBuilder groundCoverLayers(Collection<BiomeGenerationSettingsBuilder> layers) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BiomeGenerationSettingsBuilder generatorPopulators(GeneratorPopulator... genpops) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BiomeGenerationSettingsBuilder generatorPopulators(Collection<GeneratorPopulator> genpops) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BiomeGenerationSettingsBuilder populators(Populator... pops) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BiomeGenerationSettingsBuilder populators(Collection<Populator> pops) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BiomeGenerationSettingsBuilder reset() {
        this.min = 0.1f;
        return null;
    }

    @Override
    public BiomeGenerationSettingsBuilder reset(BiomeType type) {
        BiomeGenerationSettings other = type.getGenerationSettings();
        
        return null;
    }

    @Override
    public BiomeGenerationSettings build() throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

}
