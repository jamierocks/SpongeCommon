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
package org.spongepowered.common.util;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import gnu.trove.map.hash.TObjectLongHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTransaction;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.manipulator.mutable.entity.VelocityData;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.configuration.SpongeConfig;
import org.spongepowered.common.configuration.SpongeConfig.WorldConfig;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.IMixinWorldProvider;
import org.spongepowered.common.world.CaptureType;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.management.MBeanServer;

public class SpongeHooks {

    public static int tickingDimension = 0;
    public static ChunkCoordIntPair tickingChunk = null;

    private static TObjectLongHashMap<CollisionWarning> recentWarnings = new TObjectLongHashMap<>();

    public static void logInfo(String msg, Object... args) {
        Sponge.getLogger().info(MessageFormat.format(msg, args));
    }

    public static void logWarning(String msg, Object... args) {
        Sponge.getLogger().warn(MessageFormat.format(msg, args));
    }

    public static void logSevere(String msg, Object... args) {
        Sponge.getLogger().fatal(MessageFormat.format(msg, args));
    }

    public static void logStack(SpongeConfig<?> config) {
        if (config.getConfig().getLogging().logWithStackTraces()) {
            Throwable ex = new Throwable();
            ex.fillInStackTrace();
            ex.printStackTrace();
        }
    }

    public static void logEntityDeath(Entity entity) {
        if (entity == null || entity.worldObj.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (config.getConfig().getLogging().entityDeathLogging()) {
            logInfo("Dim: {0} setDead(): {1}",
                    entity.worldObj.provider.getDimensionId(), entity);
            logStack(config);
        }
    }

    public static void logEntityDespawn(Entity entity, String reason) {
        if (entity == null || entity.worldObj.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (config.getConfig().getLogging().entityDespawnLogging()) {
            logInfo("Dim: {0} Despawning ({1}): {2}", entity.worldObj.provider.getDimensionId(), reason, entity);
            logStack(config);
        }
    }

    public static void logEntitySpawn(Cause cause, Entity entity) {
        if (entity == null || entity.worldObj.isRemote) {
            return;
        }

        String spawnName = entity.getCommandSenderName();
        if (entity instanceof EntityItem) {
            spawnName = ((EntityItem) entity).getEntityItem().getDisplayName();
        }

        Optional<User> user = cause.first(User.class);
        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (config.getConfig().getLogging().entitySpawnLogging()) {
            logInfo("SPAWNED " + spawnName + " [RootCause: {0}][User: {1}][World: {2}][DimId: {3}]",
                    getFriendlyCauseName(cause),
                    user.isPresent() ? user.get().getName() : "None",
                    entity.worldObj.getWorldInfo().getWorldName(),
                    entity.worldObj.provider.getDimensionId());
            logStack(config);
        }
    }

    public static void logBlockTrack(World world, Block block, BlockPos pos, EntityPlayer player, boolean allowed) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(world);
        if (config.getConfig().getLogging().blockTrackLogging() && allowed) {
            logInfo("Tracking Block " + "[RootCause: {0}][World: {1}][Block: {2}][Pos: {3}]",
                    player.getCommandSenderName(),
                    world.getWorldInfo().getWorldName() + "(" + world.provider.getDimensionId() + ")",
                    ((BlockType)block).getId(),
                    pos);
            logStack(config);
        } else if (config.getConfig().getLogging().blockTrackLogging() && !allowed) {
            logInfo("Blacklisted! Unable to track Block " + "[RootCause: {0}][World: {1}][DimId: {2}][Block: {3}][Pos: {4}]",
                    player.getCommandSenderName(),
                    world.getWorldInfo().getWorldName(),
                    world.provider.getDimensionId(),
                    ((BlockType)block).getId(),
                    pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        }
    }

    public static void logBlockAction(Cause cause, World world, CaptureType type, BlockTransaction transaction) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(world);
        Optional<User> user = cause.first(User.class);
        if (config.getConfig().getLogging().blockBreakLogging() && type == CaptureType.BREAK
                || config.getConfig().getLogging().blockModifyLogging() && type == CaptureType.MODIFY
                || config.getConfig().getLogging().blockPlaceLogging() && type == CaptureType.PLACE
                || config.getConfig().getLogging().blockPopulateLogging() && type == CaptureType.POPULATE) {

            logInfo("Block " + type.name() + " [RootCause: {0}][User: {1}][World: {2}][DimId: {3}][OriginalState: {4}][NewState: {5}]",
                    getFriendlyCauseName(cause),
                    user.isPresent() ? user.get().getName() : "None",
                    world.getWorldInfo().getWorldName(),
                    world.provider.getDimensionId(),
                    transaction.getOriginal().getState(),
                    transaction.getFinalReplacement().getState());
            logStack(config);
        }
    }

    public static void logChunkLoad(World world, Vector3i chunkPos) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(world);
        if (config.getConfig().getLogging().chunkLoadLogging()) {
            logInfo("Load Chunk At [{0}] ({1}, {2})", world.provider.getDimensionId(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(config);
        }
    }

    public static void logChunkUnload(World world, Vector3i chunkPos) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(world);
        if (config.getConfig().getLogging().chunkUnloadLogging()) {
            logInfo("Unload Chunk At [{0}] ({1}, {2})", world.provider.getDimensionId(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(config);
        }
    }

    @SuppressWarnings("unused")
    private static void logChunkLoadOverride(ChunkProviderServer provider, int x, int z) {
        SpongeConfig<?> config = getActiveConfig(provider.worldObj);
        logInfo("Chunk Load Override: {0}, Dimension ID: {1}", provider.chunkLoadOverride,
                provider.worldObj.provider.getDimensionId());
    }

    public static boolean checkBoundingBoxSize(Entity entity, AxisAlignedBB aabb) {
        if (entity == null || entity.worldObj.isRemote) {
            return false;
        }

        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (!(entity instanceof EntityLivingBase) || entity instanceof EntityPlayer) {
            return false; // only check living entities that are not players
        }

        int maxBoundingBoxSize = config.getConfig().getEntity().getMaxBoundingBoxSize();
        if (maxBoundingBoxSize <= 0) {
            return false;
        }
        int x = MathHelper.floor_double(aabb.minX);
        int x1 = MathHelper.floor_double(aabb.maxX + 1.0D);
        int y = MathHelper.floor_double(aabb.minY);
        int y1 = MathHelper.floor_double(aabb.maxY + 1.0D);
        int z = MathHelper.floor_double(aabb.minZ);
        int z1 = MathHelper.floor_double(aabb.maxZ + 1.0D);

        int size = Math.abs(x1 - x) * Math.abs(y1 - y) * Math.abs(z1 - z);
        if (size > maxBoundingBoxSize) {
            logWarning("Entity being removed for bounding box restrictions");
            logWarning("BB Size: {0} > {1} avg edge: {2}", size, maxBoundingBoxSize, aabb.getAverageEdgeLength());
            logWarning("Motion: ({0}, {1}, {2})", entity.motionX, entity.motionY, entity.motionZ);
            logWarning("Calculated bounding box: {0}", aabb);
            logWarning("Entity bounding box: {0}", entity.getBoundingBox());
            logWarning("Entity: {0}", entity);
            NBTTagCompound tag = new NBTTagCompound();
            entity.writeToNBT(tag);
            logWarning("Entity NBT: {0}", tag);
            logStack(config);
            entity.setDead();
            return true;
        }
        return false;
    }

    public static boolean checkEntitySpeed(Entity entity, double x, double y, double z) {
        if (entity == null || entity.worldObj.isRemote) {
            return false;
        }

        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        int maxSpeed = config.getConfig().getEntity().getMaxSpeed();
        if (maxSpeed > 0) {
            double distance = x * x + z * z;
            if (distance > maxSpeed) {
                if (config.getConfig().getLogging().logEntitySpeedRemoval()) {
                    logInfo("Speed violation: {0} was over {1} - Removing Entity: {2}", distance, maxSpeed, entity);
                    if (entity instanceof EntityLivingBase) {
                        EntityLivingBase livingBase = (EntityLivingBase) entity;
                        logInfo("Entity Motion: ({0}, {1}, {2}) Move Strafing: {3} Move Forward: {4}",
                                entity.motionX, entity.motionY,
                                entity.motionZ,
                                livingBase.moveStrafing, livingBase.moveForward);
                    }

                    if (config.getConfig().getLogging().logWithStackTraces()) {
                        logInfo("Move offset: ({0}, {1}, {2})", x, y, z);
                        logInfo("Motion: ({0}, {1}, {2})", entity.motionX, entity.motionY, entity.motionZ);
                        logInfo("Entity: {0}", entity);
                        NBTTagCompound tag = new NBTTagCompound();
                        entity.writeToNBT(tag);
                        logInfo("Entity NBT: {0}", tag);
                        logStack(config);
                    }
                }
                if (entity instanceof EntityPlayer) { // Skip killing players
                    entity.motionX = 0;
                    entity.motionY = 0;
                    entity.motionZ = 0;
                    return false;
                }
                // Remove the entity;
                entity.isDead = true;
                return false;
            }
        }
        return true;
    }

    // TODO - needs to be hooked
    @SuppressWarnings("rawtypes")
    public static void logEntitySize(Entity entity, List list) {
        if (entity == null || entity.worldObj.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (!config.getConfig().getLogging().logEntityCollisionChecks()) {
            return;
        }
        int collisionWarnSize = config.getConfig().getEntity().getMaxCollisionSize();

        if (list == null) {
            return;
        }

        if (collisionWarnSize > 0 && (MinecraftServer.getServer().getTickCounter() % 10) == 0 && list.size() >= collisionWarnSize) {
            SpongeHooks.CollisionWarning warning = new SpongeHooks.CollisionWarning(entity.worldObj, entity);
            if (SpongeHooks.recentWarnings.contains(warning)) {
                long lastWarned = SpongeHooks.recentWarnings.get(warning);
                if ((MinecraftServer.getCurrentTimeMillis() - lastWarned) < 30000) {
                    return;
                }
            }
            SpongeHooks.recentWarnings.put(warning, System.currentTimeMillis());
            logWarning("Entity collision > {0, number} at: {1}", collisionWarnSize, entity);
        }
    }

    private static class CollisionWarning {

        public BlockPos blockPos;
        public int dimensionId;

        public CollisionWarning(World world, Entity entity) {
            this.dimensionId = world.provider.getDimensionId();
            this.blockPos = new BlockPos(entity.chunkCoordX, entity.chunkCoordY, entity.chunkCoordZ);
        }

        @Override
        public boolean equals(Object otherObj) {
            if (!(otherObj instanceof CollisionWarning) || (otherObj == null)) {
                return false;
            }
            CollisionWarning other = (CollisionWarning) otherObj;
            return (other.dimensionId == this.dimensionId) && other.blockPos.equals(this.blockPos);
        }

        @Override
        public int hashCode() {
            return this.blockPos.hashCode() + this.dimensionId;
        }
    }



    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void dumpHeap(File file, boolean live) {
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            Class clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Object hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
            Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
            m.invoke(hotspotMBean, file.getPath(), live);
        } catch (Throwable t) {
            logSevere("Could not write heap to {0}", file);
        }
    }

    public static void enableThreadContentionMonitoring() {
        if (!Sponge.getGlobalConfig().getConfig().getDebug().isEnableThreadContentionMonitoring()) {
            return;
        }
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        mbean.setThreadContentionMonitoringEnabled(true);
    }

    public static SpongeConfig<?> getActiveConfig(World world) {
        SpongeConfig<WorldConfig> config = ((IMixinWorld) world).getWorldConfig();
        if (config.getConfig().isConfigEnabled()) {
            return config;
        } else if (((IMixinWorldProvider) world.provider).getDimensionConfig() != null && ((IMixinWorldProvider) world.provider)
                .getDimensionConfig().getConfig().isConfigEnabled()) {
            return ((IMixinWorldProvider) world.provider).getDimensionConfig();
        } else {
            return Sponge.getGlobalConfig();
        }
    }

    public static void setBlockState(World world, int x, int y, int z, BlockState state) {
        setBlockState(world, new BlockPos(x, y, z), state);
    }

    public static void setBlockState(World world, BlockPos position, BlockState state) {
        if (state instanceof IBlockState) {
            // Notify neighbours or not?
            world.setBlockState(position, (IBlockState) state);
        } else {
            // TODO: Need to figure out what is sensible for other BlockState implementing classes.
            throw new UnsupportedOperationException("Custom BlockState implementations are not supported");
        }
    }

    public static void setBlockState(Chunk chunk, int x, int y, int z, BlockState state) {
        setBlockState(chunk, new BlockPos(x, y, z), state);
    }

    public static void setBlockState(Chunk chunk, BlockPos position, BlockState state) {
        if (state instanceof IBlockState) {
            // Notify neighbours or not?
            chunk.setBlockState(position, (IBlockState) state);
        } else {
            // TODO: Need to figure out what is sensible for other BlockState implementing classes.
            throw new UnsupportedOperationException("Custom BlockState implementations are not supported");
        }
    }

    public static String getFriendlyCauseName(Cause cause) {
        String causedBy = "Unknown";
        if (cause.root().isPresent()) {
            Object rootCause = cause.root().get();
            if (rootCause instanceof User) {
                User user = (User) rootCause;
                causedBy = user.getName();
            } else if (rootCause instanceof EntityItem) {
                EntityItem item = (EntityItem) rootCause;
                causedBy = item.getEntityItem().getDisplayName();
            }
            else if (rootCause instanceof Entity) {
                Entity causeEntity = (Entity) rootCause;
                causedBy = causeEntity.getCommandSenderName();
            }else if (rootCause instanceof BlockSnapshot) {
                BlockSnapshot snapshot = (BlockSnapshot) rootCause;
                causedBy = snapshot.getState().getType().getId();
            } else if (rootCause instanceof CatalogType) {
                CatalogType type = (CatalogType) rootCause;
                causedBy = type.getId();
            } else if (rootCause instanceof PluginContainer) {
                PluginContainer plugin = (PluginContainer) rootCause;
                causedBy = plugin.getId();
            } else {
                causedBy = rootCause.getClass().getName();
            }
        }
        return causedBy;
    }

    public static <T extends Projectile> T launchProjectile(World world, Vector3d position, ProjectileSource source, Class<T> projectileClass,
            @Nullable Vector3d velocity) {
        try {
            @SuppressWarnings("unchecked")
            T entity = (T) ConstructorUtils.invokeConstructor(Sponge.getSpongeRegistry().getEntity(projectileClass).get().getEntityClass(), world);
            entity.setLocation(entity.getLocation().setPosition(position));
            entity.setShooter(source);
            if (velocity != null) {
                VelocityData velocityData = entity.getOrCreate(VelocityData.class).get();
                velocityData.velocity().set(velocity);
                entity.offer(velocityData);
            }
            world.spawnEntityInWorld((net.minecraft.entity.Entity) entity);
            return entity;
        } catch (Exception e) {
            Sponge.getLogger().error(ExceptionUtils.getStackTrace(e));
        }

        return null;
    }
}
