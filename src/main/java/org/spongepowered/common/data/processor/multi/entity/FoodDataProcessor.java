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
package org.spongepowered.common.data.processor.multi.entity;


import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFoodData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.mixin.core.util.FoodStatsAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Map;
import java.util.Optional;

public class FoodDataProcessor extends AbstractEntityDataProcessor<EntityPlayer, FoodData, ImmutableFoodData> {

    public FoodDataProcessor() {
        super(EntityPlayer.class);
    }

    @Override
    protected FoodData createManipulator() {
        return new SpongeFoodData(Constants.Entity.Player.DEFAULT_FOOD_LEVEL, Constants.Entity.Player.DEFAULT_SATURATION, Constants.Entity.Player.DEFAULT_EXHAUSTION);
    }

    @Override
    protected boolean doesDataExist(final EntityPlayer entity) {
        return true;
    }

    @Override
    protected boolean set(final EntityPlayer entity, final Map<Key<?>, Object> keyValues) {
        entity.func_71024_bL().func_75114_a((Integer) keyValues.get(Keys.FOOD_LEVEL));
        ((FoodStatsAccessor) entity.func_71024_bL()).accessor$setFoodSaturationLevel(((Double) keyValues.get(Keys.SATURATION)).floatValue());
        ((FoodStatsAccessor) entity.func_71024_bL()).accessor$setFoodExhaustionLevel(((Double) keyValues.get(Keys.EXHAUSTION)).floatValue());
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(final EntityPlayer entity) {
        final int food = entity.func_71024_bL().func_75116_a();
        final double saturation = entity.func_71024_bL().func_75115_e();
        final double exhaustion = ((FoodStatsAccessor) entity.func_71024_bL()).accessor$getFoodExhaustionLevel();
        return ImmutableMap.<Key<?>, Object>of(Keys.FOOD_LEVEL, food,
                                               Keys.SATURATION, saturation,
                                               Keys.EXHAUSTION, exhaustion);
    }

    @Override
    public Optional<FoodData> fill(final DataContainer container, final FoodData foodData) {
        foodData.set(Keys.FOOD_LEVEL, getData(container, Keys.FOOD_LEVEL));
        foodData.set(Keys.SATURATION, getData(container, Keys.SATURATION));
        foodData.set(Keys.EXHAUSTION, getData(container, Keys.EXHAUSTION));
        return Optional.of(foodData);
    }

    @Override
    public DataTransactionResult remove(final DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

}
