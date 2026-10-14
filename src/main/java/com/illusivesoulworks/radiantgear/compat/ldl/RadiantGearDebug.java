/*
 * Copyright (C) 2026 DEHunter58711
 *
 * This file is part of the unofficial Minecraft 26.3 / NeoForge port of Radiant Gear
 * (original mod: Copyright (C) 2022 Illusive Soulworks). Added by DEHunter58711 on 2026-10-13.
 *
 * Radiant Gear is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Radiant Gear is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Radiant Gear.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.radiantgear.compat.ldl;

import com.illusivesoulworks.radiantgear.RadiantGearConstants;
import com.illusivesoulworks.radiantgear.config.RadiantGearConfig;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/** Optional diagnostics, enabled with debugLogging=true in radiantgear-client.toml. */
final class RadiantGearDebug {

  private static final long ENTITY_INTERVAL_MS = 5000L;
  private static final Map<Integer, Long> LAST_LOG = new ConcurrentHashMap<>();

  private RadiantGearDebug() {}

  static boolean enabled() {
    return RadiantGearConfig.debugLogging();
  }

  static void log(String message, Object... args) {
    if (enabled()) {
      RadiantGearConstants.LOG.info("[Radiant Gear debug] " + message, args);
    }
  }

  /** At most one block of lines per entity every 5 s; the current tick's lines stay together. */
  static boolean shouldLogEntity(int entityId) {
    long now = System.currentTimeMillis();
    Long last = LAST_LOG.get(entityId);
    if (last == null || now - last >= ENTITY_INTERVAL_MS) {
      if (LAST_LOG.size() > 512) {
        LAST_LOG.clear();
      }
      LAST_LOG.put(entityId, now);
      return true;
    }
    return now - last < 2L;
  }
}
