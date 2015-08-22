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
package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import net.minecraft.entity.passive.EntityTameable;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTameableData;
import org.spongepowered.common.data.processor.data.entity.TameableDataProcessor;

import java.util.Optional;
import java.util.UUID;

public class TameableDataBuilder implements DataManipulatorBuilder<TameableData, ImmutableTameableData> {

    @Override
    public TameableData create() {
        return new SpongeTameableData();
    }

    @Override
    public Optional<TameableData> createFrom(DataHolder dataHolder) {
        if(dataHolder instanceof EntityTameable) {
            Optional<UUID> optionalUUID = TameableDataProcessor.getTamer((EntityTameable) dataHolder);
            return Optional.<TameableData>of(new SpongeTameableData(optionalUUID.orElse(null)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TameableData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.TAMED_OWNER.getQuery())) {
            final String uuidString = container.getString(Keys.TAMED_OWNER.getQuery()).get();
            if (uuidString.equals("none")) {
                return Optional.of(new SpongeTameableData());
            } else {
                final UUID uuid = UUID.fromString(uuidString);
                return Optional.of(new SpongeTameableData(uuid));
            }
        }
        return Optional.empty();
    }
}
