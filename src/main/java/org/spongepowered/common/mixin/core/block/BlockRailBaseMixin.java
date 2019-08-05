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
package org.spongepowered.common.mixin.core.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableRailDirectionData;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeRailDirectionData;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(BlockRailBase.class)
public abstract class BlockRailBaseMixin extends BlockMixin {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final IBlockState blockState) {
        final ImmutableRailDirectionData railDirection = impl$getRailDirectionFor(blockState);
        if (railDirection == null) { // Extra safety check
            return ImmutableList.of();
        }
        return ImmutableList.of(railDirection);
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableRailDirectionData.class.isAssignableFrom(immutable);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public Optional<BlockState> bridge$getStateWithData(final IBlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableRailDirectionData) {
            final RailDirection apiDirection = ((ImmutableRailDirectionData) manipulator).type().get();
            final BlockRailBase.EnumRailDirection railDirection = (BlockRailBase.EnumRailDirection) (Object) apiDirection;
            final Optional<BlockState> state = impl$getStateForDirection(blockState, railDirection);
            if (state.isPresent()) {
                return state;
            }
            // For mods that extend BlockRailBase
            for (final Map.Entry<IProperty<?>, Comparable<?>> entry :  blockState.func_177228_b().entrySet
                    ()) {
                if (entry.getValue() instanceof BlockRailBase.EnumRailDirection) {
                    if (entry.getKey().func_177700_c().contains(railDirection)) {
                        final PropertyEnum<BlockRailBase.EnumRailDirection> property = (PropertyEnum<BlockRailBase.EnumRailDirection>) entry.getKey();
                        final IBlockState newState = blockState.func_177226_a(property, railDirection);
                        return Optional.of((BlockState) newState);
                    }
                }
            }
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final IBlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.RAIL_DIRECTION)) {
            final BlockRailBase.EnumRailDirection railDirection = (BlockRailBase.EnumRailDirection) value;
            final Optional<BlockState> x = impl$getStateForDirection(blockState, railDirection);
            if (x.isPresent()) {
                return x;
            }

        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private Optional<BlockState> impl$getStateForDirection(final IBlockState blockState, final BlockRailBase.EnumRailDirection railDirection) {
        if (blockState.func_177230_c() instanceof BlockRail) {
            return Optional.of((BlockState) blockState.func_177226_a(BlockRail.field_176565_b, railDirection));
        }
        if (blockState.func_177230_c() instanceof BlockRailPowered) {
            if (!BlockRailPowered.field_176568_b.func_177700_c().contains(railDirection)) {
                return Optional.empty();
            }
            return Optional.of((BlockState) blockState.func_177226_a(BlockRailPowered.field_176568_b, railDirection));
        }
        if (blockState.func_177230_c() instanceof BlockRailDetector) {
            if (!BlockRailDetector.field_176573_b.func_177700_c().contains(railDirection)) {
                return Optional.empty();
            }
            return Optional.of((BlockState) blockState.func_177226_a(BlockRailDetector.field_176573_b, railDirection));
        }
        return Optional.empty();
    }

    @Nullable
    private ImmutableRailDirectionData impl$getRailDirectionFor(final IBlockState blockState) {
        if (blockState.func_177230_c() instanceof BlockRail) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class, blockState.func_177229_b(BlockRail.field_176565_b));
        }
        if (blockState.func_177230_c() instanceof BlockRailPowered) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class, blockState.func_177229_b(BlockRailPowered.field_176568_b));
        }
        if (blockState.func_177230_c() instanceof BlockRailDetector) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class, blockState.func_177229_b(BlockRailDetector.field_176573_b));
        } // For mods extending BlockRailBase
        for (final Map.Entry<IProperty<?>, Comparable<?>> entry :  blockState.func_177228_b().entrySet()) {
            if (entry.getValue() instanceof BlockRailBase.EnumRailDirection) {
                return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class, entry.getValue());
            }
        }
        return null;
    }
}
