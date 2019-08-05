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
package org.spongepowered.common.data.processor.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.common.item.SpongeFireworkShape;
import org.spongepowered.common.item.inventory.SpongeItemStackBuilder;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class FireworkUtils {

    public static final BiMap<Byte, SpongeFireworkShape> shapeMapping = ImmutableBiMap.<Byte, SpongeFireworkShape>builder()
            .put((byte) 0, new SpongeFireworkShape("minecraft:ball", "Ball"))
            .put((byte) 1, new SpongeFireworkShape("minecraft:large_ball", "Large Ball"))
            .put((byte) 2, new SpongeFireworkShape("minecraft:star", "Star"))
            .put((byte) 3, new SpongeFireworkShape("minecraft:creeper", "Creeper"))
            .put((byte) 4, new SpongeFireworkShape("minecraft:burst", "Burst"))
            .build();

    public static ItemStack getItem(EntityFireworkRocket firework) {
        ItemStack item = firework.func_184212_Q().func_187225_a(EntityFireworkRocket.field_184566_a);
        if (item.func_190926_b()) {
            item = (ItemStack) new SpongeItemStackBuilder().itemType(ItemTypes.FIREWORKS).build();
            firework.func_184212_Q().func_187227_b(EntityFireworkRocket.field_184566_a, item);
        }
        return item;
    }

    public static FireworkEffect getChargeEffect(ItemStack item) {
        Preconditions.checkArgument(item.func_77973_b() == Items.field_151154_bQ, "Item is not a firework!"); // FIREWORK_CHARGE
        NBTTagCompound firework = NbtDataUtil.getOrCreateCompound(item).func_74775_l("Explosion");
        if(firework == null) return null;

        return fromNbt(firework);
    }

    public static FireworkShape getShape(byte id) {
        if(id > 4) id = 0;
        return shapeMapping.get(id);
    }

    public static byte getShapeId(FireworkShape shape) {
        return shapeMapping.inverse().get(shape);
    }

    public static FireworkEffect fromNbt(NBTTagCompound effectNbt) {
        FireworkEffect.Builder builder = new SpongeFireworkEffectBuilder();
        if(effectNbt.func_74764_b("Flicker")) {
            builder.flicker(effectNbt.func_74767_n("Flicker"));
        }
        if(effectNbt.func_74764_b("Trail")) {
            builder.trail(effectNbt.func_74767_n("Trail"));
        }
        if(effectNbt.func_74764_b("Type")) {
            byte type = effectNbt.func_74771_c("Type");
            builder.shape(getShape(type));
        }
        if(effectNbt.func_74764_b("Colors")) {
            List<Color> colors = Lists.newArrayList();
            int[] colorsRaw = effectNbt.func_74759_k("Colors");
            for(int color : colorsRaw) {
                colors.add(Color.ofRgb(color));
            }
            builder.colors(colors);
        }
        if(effectNbt.func_74764_b("FadeColors")) {
            List<Color> fades = Lists.newArrayList();
            int[] fadesRaw = effectNbt.func_74759_k("FadeColors");
            for(int fade : fadesRaw) {
                fades.add(Color.ofRgb(fade));
            }
            builder.fades(fades);
        }

        return builder.build();
    }

    public static NBTTagCompound toNbt(FireworkEffect effect) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.func_74757_a("Flicker", effect.flickers());
        tag.func_74757_a("Trail", effect.hasTrail());
        tag.func_74774_a("Type", getShapeId(effect.getShape()));
        int[] colorsArray = new int[effect.getColors().size()];
        List<Color> colors = effect.getColors();
        for (int i = 0; i < colors.size(); i++) {
            colorsArray[i] = colors.get(i).getRgb();
        }
        tag.func_74783_a("Colors", colorsArray);
        int[] fadeArray = new int[effect.getFadeColors().size()];
        List<Color> fades = effect.getFadeColors();
        for (int i = 0; i < fades.size(); i++) {
            fadeArray[i] = fades.get(i).getRgb();
        }
        tag.func_74783_a("FadeColors", fadeArray);

        return tag;
    }

    public static boolean setFireworkEffects(Object object, List<FireworkEffect> effects) {
        ItemStack item = ItemStack.field_190927_a;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof EntityFireworkRocket) {
            item = getItem((EntityFireworkRocket) object);
        }
        if(item.func_190926_b()) return false;

        if(item.func_77973_b() == Items.field_151154_bQ) {
            if(effects.size() != 0) {
                NbtDataUtil.getOrCreateCompound(item).func_74782_a("Explosion", toNbt(effects.get(0)));
            } else {
                NbtDataUtil.getOrCreateCompound(item).func_82580_o("Explosion");
            }
            return true;
        } else if(item.func_77973_b() == Items.field_151152_bP) {
            NBTTagList nbtEffects = new NBTTagList();
            effects.stream().map(FireworkUtils::toNbt).forEach(nbtEffects::func_74742_a);

            NBTTagCompound fireworks = item.func_190925_c("Fireworks");
            fireworks.func_74782_a("Explosions", nbtEffects);
            return true;
        }
        return false;
    }

    public static Optional<List<FireworkEffect>> getFireworkEffects(Object object) {
        ItemStack item = ItemStack.field_190927_a;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof EntityFireworkRocket) {
            item = FireworkUtils.getItem((EntityFireworkRocket) object);
        }
        if(item.func_190926_b()) return Optional.empty();

        List<FireworkEffect> effects;
        if(item.func_77973_b() == Items.field_151152_bP) {
            NBTTagCompound fireworks = item.func_179543_a("Fireworks");
            if(fireworks == null || !fireworks.func_74764_b("Explosions")) return Optional.empty();

            NBTTagList effectsNbt = fireworks.func_150295_c("Explosions", Constants.NBT.TAG_COMPOUND);
            effects = Lists.newArrayList();
            for(int i = 0; i < effectsNbt.func_74745_c(); i++) {
                NBTTagCompound effectNbt = effectsNbt.func_150305_b(i);
                effects.add(fromNbt(effectNbt));
            }
        } else {
            FireworkEffect effect = FireworkUtils.getChargeEffect(item);
            if(effect == null) return Optional.empty();
            effects = ImmutableList.of(effect);
        }

        return Optional.of(effects);
    }

    public static boolean removeFireworkEffects(Object object) {
        ItemStack item = ItemStack.field_190927_a;
        if(object instanceof ItemStack) {
            item = (ItemStack) object;
        }
        if(object instanceof EntityFireworkRocket) {
            item = FireworkUtils.getItem((EntityFireworkRocket) object);
        }
        if(item.func_190926_b()) return false;

        if(item.func_77973_b() == Items.field_151154_bQ) {
            NbtDataUtil.getOrCreateCompound(item).func_82580_o("Explosion");
            return true;
        } else if(item.func_77973_b() == Items.field_151152_bP) {
            NBTTagCompound fireworks = item.func_190925_c("Fireworks");
            fireworks.func_82580_o("Explosions");
            return true;
        }

        return false;
    }

}
