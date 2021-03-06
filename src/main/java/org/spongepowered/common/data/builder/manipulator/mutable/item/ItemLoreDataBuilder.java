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
package org.spongepowered.common.data.builder.manipulator.mutable.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableLoreData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeLoreData;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class ItemLoreDataBuilder implements DataManipulatorBuilder<LoreData, ImmutableLoreData> {

    @Override
    public LoreData create() {
        return new SpongeLoreData();
    }

    @Override
    public Optional<LoreData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            final ItemStack itemStack = (ItemStack) dataHolder;
            final NBTTagCompound subCompound = itemStack.getSubCompound(NbtDataUtil.ITEM_DISPLAY, false);
            if (subCompound == null) {
                return Optional.empty();
            }
            if (!subCompound.hasKey(NbtDataUtil.ITEM_LORE, NbtDataUtil.TAG_LIST)) {
                return Optional.empty();
            }
            final NBTTagList list = subCompound.getTagList(NbtDataUtil.ITEM_LORE, NbtDataUtil.TAG_STRING);
            return Optional.of(new SpongeLoreData(SpongeTexts.fromLegacy(list)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<LoreData> build(DataView container) throws InvalidDataException {
        DataUtil.checkDataExists(container, Keys.ITEM_LORE.getQuery());
        final List<String> json = container.getStringList(Keys.ITEM_LORE.getQuery()).get();
        return Optional.of(new SpongeLoreData(SpongeTexts.fromJson(json)));
    }
}
