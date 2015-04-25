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
package org.spongepowered.common.world.gen.builders;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import org.spongepowered.api.util.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.world.gen.populator.HugeTree;
import org.spongepowered.api.world.gen.populator.HugeTree.Builder;
import org.spongepowered.api.world.gen.type.BiomeTreeType;
import org.spongepowered.common.world.gen.populators.HugeTreePopulator;

import java.util.Collection;
import java.util.List;


public class HugeTreeBuilder implements HugeTree.Builder {
    
    private List<WeightedObject<BiomeTreeType>> types;
    private VariableAmount count;
    
    public HugeTreeBuilder() {
        reset();
    }

    @Override
    public Builder perChunk(VariableAmount count) {
        this.count = count;
        return this;
    }

    @Override
    public Builder type(WeightedObject<BiomeTreeType>... types) {
        checkNotNull(types, "types");
        this.types.clear();
        for (WeightedObject<BiomeTreeType> type : types) {
            if (type != null) {
                this.types.add(type);
            }
        }
        return this;
    }

    @Override
    public Builder type(Collection<WeightedObject<BiomeTreeType>> types) {
        checkNotNull(types, "types");
        this.types.clear();
        for (WeightedObject<BiomeTreeType> type : types) {
            if (type != null) {
                this.types.add(type);
            }
        }
        return this;
    }

    @Override
    public Builder reset() {
        this.types = Lists.newArrayList();
        this.count = VariableAmount.fixed(1);
        return this;
    }

    @Override
    public HugeTree build() throws IllegalStateException {
        HugeTree pop = new HugeTreePopulator();
        pop.setTreesPerChunk(this.count);
        pop.getType().clear();
        pop.getType().addAll(this.types);
        return pop;
    }

}
