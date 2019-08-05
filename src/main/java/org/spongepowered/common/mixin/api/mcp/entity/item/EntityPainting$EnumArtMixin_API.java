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
package org.spongepowered.common.mixin.api.mcp.entity.item;

import net.minecraft.entity.item.EntityPainting;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;

import org.spongepowered.common.mixin.api.mcp.entity.item.EntityPainting.EnumArtMixin_API;

@Mixin(EntityPainting.EnumArt.class)
public abstract class EntityPainting$EnumArtMixin_API implements Art {

    @Shadow @Final public String title;
    @Shadow @Final public int sizeX;
    @Shadow @Final public int sizeY;

    @Override
    public String getId() {
        return this.title;
    }

    @Override
    public String getName() {
        return this.title;
    }

    @Override
    public int getHeight() {
        return this.sizeY / Constants.Block.PIXELS_PER_BLOCK;
    }

    @Override
    public int getWidth() {
        return this.sizeX / Constants.Block.PIXELS_PER_BLOCK;
    }


}
