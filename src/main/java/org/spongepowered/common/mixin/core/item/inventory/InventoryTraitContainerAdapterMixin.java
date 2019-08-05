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
package org.spongepowered.common.mixin.core.item.inventory;

import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntityLockable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.item.inventory.InventoryAdapterBridge;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Mixin into all known vanilla {@link IInventory} and {@link Container}
 *
 * <p>To work {@link InventoryAdapter#bridge$getSlotProvider()} and {@link InventoryAdapter#bridge$getRootLens()} need to be implemented</p>
 */
@Mixin(value = {
        Slot.class,
        Container.class,
        InventoryPlayer.class,
        EntityVillager.class,
        InventoryLargeChest.class,
        TileEntityLockable.class,
        CustomInventory.class,
        InventoryBasic.class,
        SpongeUserInventory.class,
        InventoryCrafting.class,
        InventoryCraftResult.class,
        EntityMinecartContainer.class
}, priority = 999)
@Implements(@Interface(iface = Inventory.class, prefix = "inventory$"))
public abstract class InventoryTraitContainerAdapterMixin implements InventoryAdapter, InventoryAdapterBridge {

    private List<Inventory> impl$children = new ArrayList<Inventory>();
    @Nullable private SlotProvider impl$provider;
    @Nullable private Lens impl$lens;
    @Nullable private Fabric impl$fabric;
    @Nullable private PluginContainer impl$PluginParent;

    @Override
    public Inventory bridge$getChild(final int index) {
        if (index < 0 || index >= this.bridge$getRootLens().getChildren().size()) {
            throw new IndexOutOfBoundsException("No child at index: " + index);
        }
        while (index >= this.impl$children.size()) {
            this.impl$children.add(null);
        }
        Inventory child = this.impl$children.get(index);
        if (child == null) {
            child = (Inventory) this.bridge$getRootLens().getChildren().get(index).getAdapter(this.bridge$getFabric(), (Inventory) this);
            this.impl$children.set(index, child);
        }
        return child;
    }


    @Override
    public Fabric bridge$getFabric() {
        if (this.impl$fabric == null) {
            this.impl$fabric = bridge$generateFabric();
        }
        return this.impl$fabric;
    }

    @Override
    public SlotProvider bridge$getSlotProvider() {
        if (this.impl$provider == null) {
            this.impl$provider = this.bridge$generateSlotProvider();
            return this.impl$provider;
        }
        return this.impl$provider;
    }

    @Override
    public void bridge$setSlotProvider(final SlotProvider provider) {
        this.impl$provider = provider;
    }

    @Override
    public Lens bridge$getRootLens() {
        if (this.impl$lens == null) {
            this.impl$lens = this.bridge$generateLens();
        }
        return this.impl$lens;
    }

    @Override
    public void bridge$setLens(final Lens lens) {
        this.impl$lens = lens;
    }

    @Override
    public void bridge$setFabric(final Fabric fabric) {
        this.impl$fabric = fabric;
    }

    @Override
    public PluginContainer bridge$getPlugin() {
        if (this.impl$PluginParent == null) {
            this.impl$PluginParent = InventoryUtil.getPluginContainer(this);
        }
        return this.impl$PluginParent;
    }

    @Override
    public void bridge$setPlugin(final PluginContainer container) {
        this.impl$PluginParent = container;
    }

}
