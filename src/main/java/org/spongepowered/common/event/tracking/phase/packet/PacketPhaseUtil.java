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
package org.spongepowered.common.event.tracking.phase.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;

import javax.annotation.Nullable;

public final class PacketPhaseUtil {

    @SuppressWarnings("rawtypes")
    public static void handleSlotRestore(final EntityPlayer player, @Nullable final Container openContainer, final List<SlotTransaction> slotTransactions, final boolean eventCancelled) {
        for (final SlotTransaction slotTransaction : slotTransactions) {

            if ((!slotTransaction.getCustom().isPresent() && slotTransaction.isValid()) && !eventCancelled) {
                continue;
            }
            final SlotAdapter slot = (SlotAdapter) slotTransaction.getSlot();
            final ItemStackSnapshot snapshot = eventCancelled || !slotTransaction.isValid() ? slotTransaction.getOriginal() : slotTransaction.getCustom().get();
            final ItemStack originalStack = ItemStackUtil.fromSnapshotToNative(snapshot);
            if (openContainer == null) {
                slot.set(((org.spongepowered.api.item.inventory.ItemStack) originalStack));
            } else {
                final int slotNumber = slot.slotNumber;
                final Slot nmsSlot = openContainer.func_75139_a(slotNumber);
                if (nmsSlot != null) {
                    nmsSlot.func_75215_d(originalStack);
                }
            }
        }
        if (openContainer != null) {
            final boolean capture = ((TrackedInventoryBridge) openContainer).bridge$capturingInventory();
            ((TrackedInventoryBridge) openContainer).bridge$setCaptureInventory(false);
            openContainer.func_75142_b();
            ((TrackedInventoryBridge) openContainer).bridge$setCaptureInventory(capture);
            // If event is cancelled, always resync with player
            // we must also validate the player still has the same container open after the event has been processed
            if (eventCancelled && player.field_71070_bA == openContainer && player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).func_71120_a(openContainer);
            }
        }
    }

    public static void handleCustomCursor(final EntityPlayerMP player, final ItemStackSnapshot customCursor) {
        final ItemStack cursor = ItemStackUtil.fromSnapshotToNative(customCursor);
        player.field_71071_by.func_70437_b(cursor);
        player.field_71135_a.func_147359_a(new SPacketSetSlot(-1, -1, cursor));
    }

    public static void validateCapturedTransactions(final int slotId, final Container openContainer, final List<SlotTransaction> capturedTransactions) {
        if (capturedTransactions.size() == 0 && slotId >= 0 && slotId < openContainer.field_75151_b.size()) {
            final Slot slot = openContainer.func_75139_a(slotId);
            if (slot != null) {
                final ItemStackSnapshot snapshot = slot.func_75216_d() ? ((org.spongepowered.api.item.inventory.ItemStack) slot.func_75211_c()).createSnapshot() : ItemStackSnapshot.NONE;
                final SlotTransaction slotTransaction = new SlotTransaction(ContainerUtil.getSlot(openContainer, slotId), snapshot, snapshot);
                capturedTransactions.add(slotTransaction);
            }
        }
    }

    public static void handlePlayerSlotRestore(final EntityPlayerMP player, final ItemStack itemStack, final EnumHand hand) {
        if (itemStack.func_190926_b()) { // No need to check if it's NONE, NONE is checked by isEmpty.
            return;
        }

        player.field_71137_h = false;
        int slotId = 0;
        if (hand == EnumHand.OFF_HAND) {
            player.field_71071_by.field_184439_c.set(0, itemStack);
            slotId = (player.field_71071_by.field_70462_a.size() + InventoryPlayer.func_70451_h());
        } else {
            player.field_71071_by.field_70462_a.set(player.field_71071_by.field_70461_c, itemStack);
            final Slot slot = player.field_71070_bA.func_75147_a(player.field_71071_by, player.field_71071_by.field_70461_c);
            slotId = slot.field_75222_d;
        }

        player.field_71070_bA.func_75142_b();
        player.field_71137_h = false;
        player.field_71135_a.func_147359_a(new SPacketSetSlot(player.field_71070_bA.field_75152_c, slotId, itemStack));
    }

    // Check if all transactions are invalid
    public static boolean allTransactionsInvalid(final List<SlotTransaction> slotTransactions) {
        if (slotTransactions.size() == 0) {
            return false;
        }

        for (final SlotTransaction slotTransaction : slotTransactions) {
            if (slotTransaction.isValid()) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onProcessPacket(final Packet packetIn, final INetHandler netHandler) {
        if (netHandler instanceof NetHandlerPlayServer) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                EntityPlayerMP packetPlayer = ((NetHandlerPlayServer) netHandler).field_147369_b;
                frame.pushCause(packetPlayer);
                if (SpongeImplHooks.creativeExploitCheck(packetIn, packetPlayer)) {
                    return;
                }

                // Don't process movement capture logic if player hasn't moved
                final boolean ignoreMovementCapture;
                if (packetIn instanceof CPacketPlayer) {
                    final CPacketPlayer movingPacket = ((CPacketPlayer) packetIn);
                    if (movingPacket instanceof CPacketPlayer.Rotation) {
                        ignoreMovementCapture = true;
                    } else if (packetPlayer.field_70165_t == movingPacket.field_149479_a && packetPlayer.field_70163_u == movingPacket.field_149477_b && packetPlayer.field_70161_v == movingPacket.field_149478_c) {
                        ignoreMovementCapture = true;
                    } else {
                        ignoreMovementCapture = false;
                    }
                } else {
                    ignoreMovementCapture = false;
                }
                if (ignoreMovementCapture || (packetIn instanceof CPacketClientSettings)) {
                    packetIn.func_148833_a(netHandler);
                } else {
                    final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(packetPlayer.field_71071_by.func_70445_o());
                    final IPhaseState<? extends PacketContext<?>> packetState = PacketPhase.getInstance().getStateForPacket(packetIn);
                    // At the very least make an unknown packet state case.
                    final PacketContext<?> context = packetState.createPhaseContext();
                    if (!PacketPhase.getInstance().isPacketInvalid(packetIn, packetPlayer, packetState)) {
                        context
                            .source(packetPlayer)
                            .packetPlayer(packetPlayer)
                            .packet(packetIn)
                            .cursor(cursor);

                        PacketPhase.getInstance().populateContext(packetIn, packetPlayer, packetState, context);
                        context.owner((Player) packetPlayer);
                        context.notifier((Player) packetPlayer);
                    }
                    try (final PhaseContext<?> packetContext = context) {
                        packetContext.buildAndSwitch();
                        packetIn.func_148833_a(netHandler);

                    }

                    if (packetIn instanceof CPacketClientStatus) {
                        // update the reference of player
                        packetPlayer = ((NetHandlerPlayServer) netHandler).field_147369_b;
                    }
                    ((EntityPlayerMPBridge) packetPlayer).bridge$setPacketItem(ItemStack.field_190927_a);
                }
            }
        } else { // client
            packetIn.func_148833_a(netHandler);
        }
    }
}
