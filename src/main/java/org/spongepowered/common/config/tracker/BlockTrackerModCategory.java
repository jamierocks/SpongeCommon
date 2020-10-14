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
package org.spongepowered.common.config.tracker;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class BlockTrackerModCategory {

    @Setting("enabled")
    @Comment("If 'false', all tracking for this mod will be ignored.")
    private boolean isEnabled = true;

    @Setting(TrackerConfig.BLOCK_BULK_CAPTURE)
    @Comment("Set to true to perform block bulk capturing during block ticks. (Default: true)")
    private Map<String, Boolean> blockBulkCaptureMap = new HashMap<>();
    @Setting(TrackerConfig.ENTITY_BULK_CAPTURE)
    @Comment("Set to true to perform entity bulk capturing during block ticks. (Default: true)")
    private Map<String, Boolean> entityBulkCaptureMap = new HashMap<>();
    @Setting(TrackerConfig.BLOCK_EVENT_CREATION)
    @Comment("Set to true to create and fire block events during block ticks. (Default: true)")
    private Map<String, Boolean> blockEventCreationMap = new HashMap<>();
    @Setting(TrackerConfig.ENTITY_EVENT_CREATION)
    @Comment("Set to true to create and fire entity events during block ticks. (Default: true)")
    private Map<String, Boolean> entityEventCreationMap = new HashMap<>();

    public BlockTrackerModCategory() {
    }

    public BlockTrackerModCategory(final String modId) {
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public Map<String, Boolean> getBlockBulkCaptureMap() {
        return this.blockBulkCaptureMap;
    }

    public Map<String, Boolean> getEntityBulkCaptureMap() {
        return this.entityBulkCaptureMap;
    }

    public Map<String, Boolean> getBlockEventCreationMap() {
        return this.blockEventCreationMap;
    }

    public Map<String, Boolean> getEntityEventCreationMap() {
        return this.entityEventCreationMap;
    }
}
