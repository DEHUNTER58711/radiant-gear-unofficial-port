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

package com.illusivesoulworks.radiantgear.compat;

/** Pure helpers (no Minecraft classes) so they can be unit-tested. */
public final class LuminanceMath {

  private LuminanceMath() {}

  /** Scales a 0..15 light level, rounds and clamps it back to 0..15. */
  public static int scale(int luminance, double multiplier) {
    if (luminance <= 0 || multiplier <= 0.0D || Double.isNaN(multiplier)) {
      return 0;
    }
    long scaled = Math.round(luminance * multiplier);
    return (int) Math.max(0L, Math.min(15L, scaled));
  }

  /**
   * Version gate. Returns true when {@code version} has the given major version and is not
   * older than {@code minMajor.minMinor}. Pre-release/build suffixes are ignored.
   */
  public static boolean isSupported(String version, int major, int minMinor) {
    if (version == null) {
      return false;
    }
    String core = version.split("[+-]", 2)[0];
    String[] parts = core.split("\\.");
    try {
      int maj = Integer.parseInt(parts[0]);
      int min = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
      return maj == major && min >= minMinor;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
