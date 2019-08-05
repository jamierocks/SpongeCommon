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
package org.spongepowered.common.item.inventory.lens.impl.fabric;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class ContainerFabric extends MinecraftFabric {

    private Translation displayName;
    private final Container container;
    private final Set<IInventory> all;

    public ContainerFabric(Container container) {
        this(ContainerFabric.getFirstDisplayName(container), container);
    }

    private ContainerFabric(Translation displayName, Container container) {
        this.displayName = displayName;
        this.container = container;

        List<Slot> slots = this.container.field_75151_b;

        Builder<IInventory> builder = ImmutableSet.<IInventory>builder();
        for (Slot slot : slots) {
            if (slot.field_75224_c != null) {
                builder.add(slot.field_75224_c);
            }
        }
        this.all = builder.build();
    }

    @Override
    public Collection<?> allInventories() {
        return this.all;
    }

    @Override
    public IInventory get(int index) {
        if (this.container.field_75151_b.isEmpty()) {
            return null; // Somehow we got an empty container
        }
        return this.container.func_75139_a(index).field_75224_c;
    }

    @Override
    public ItemStack getStack(int index) {
        return this.container.func_75139_a(index).func_75211_c();
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        this.container.func_75139_a(index).func_75215_d(stack);
    }

    @Override
    public int getMaxStackSize() {
        int max = 0;
        for (IInventory inv : this.all) {
            max = Math.max(max, inv.func_70297_j_());
        }
        return max;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    @Override
    public int getSize() {
        return this.container.field_75151_b.size();
    }

    @Override
    public void clear() {
        this.all.forEach(IInventory::func_174888_l);
    }

    @Override
    public void markDirty() {
        this.container.func_75142_b();
    }

    static Translation getFirstDisplayName(Container container) {
        if (container.field_75151_b.size() == 0) {
            return new FixedTranslation("Container");
        }

        try
        {
            Slot slot = container.func_75139_a(0);
            return slot.field_75224_c != null && slot.field_75224_c.func_145748_c_() != null ?
                    new FixedTranslation(slot.field_75224_c.func_145748_c_().func_150260_c()) :
                    new FixedTranslation("UNKNOWN: " + container.getClass().getName());
        }
        catch (AbstractMethodError e)
        {
            SpongeImpl.getLogger().warn("AbstractMethodError! Could not find displayName for " +
                    container.func_75139_a(0).field_75224_c.getClass().getName(), e);
            return new FixedTranslation("UNKNOWN: " + container.getClass().getName());
        }
    }

    public Container getContainer() {
        return this.container;
    }
}
