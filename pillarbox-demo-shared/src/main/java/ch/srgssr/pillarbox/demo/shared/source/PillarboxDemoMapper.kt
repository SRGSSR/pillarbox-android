/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import ch.srgssr.pillarbox.standard.PlayerDataMapper

/**
 * Mapper for Pillarbox Demo.
 */
class PillarboxDemoMapper : PlayerDataMapper<CustomData> by PlayerDataMapper.Default()
