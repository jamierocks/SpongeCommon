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
package org.spongepowered.common.mixin.core.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.storage.loot.ILootContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.item.inventory.InventoryAdapterBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.IInventoryFabric;

import javax.annotation.Nullable;

@Mixin(EntityMinecartContainer.class)
public abstract class EntityMinecartContainerMixin extends EntityMinecartMixin implements ILockableContainer, ILootContainer, InventoryAdapter,
    InventoryAdapterBridge {

    @Shadow private boolean dropContentsWhenDead;

    protected Lens createLensOnConstruct() {
        return this.func_70302_i_() == 0
               ? new DefaultEmptyLens(this)
               : new OrderedInventoryLensImpl(0, this.func_70302_i_(), 1, this.bridge$getSlotProvider());
    }

    /**
     * @author Zidane - June 2019 - 1.12.2
     * @reason Only have this Minecart not drop contents if we actually changed dimension
     */
    @Override
    @Nullable
    public Entity changeDimension(int dimensionIn) {
        final Entity entity = super.changeDimension(dimensionIn);

        if (entity instanceof EntityMinecartContainer) {
            // We actually teleported so...
            this.dropContentsWhenDead = false;
        }

        return entity;
    }

    @Override
    public SlotProvider bridge$generateSlotProvider() {
        return new SlotCollection.Builder().add(this.func_70302_i_()).build();
    }

    @Override
    public Lens bridge$generateLens() {
        return createLensOnConstruct();
    }

    @Override
    public Fabric bridge$generateFabric() {
        return new IInventoryFabric(this);
    }
}
