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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LuminanceMathTest {

  @Test
  void scaleKeepsIdentity() {
    for (int i = 0; i <= 15; i++) {
      assertEquals(i, LuminanceMath.scale(i, 1.0D));
    }
  }

  @Test
  void scaleRoundsAndClamps() {
    assertEquals(8, LuminanceMath.scale(15, 0.5D)); // 7.5 -> 8
    assertEquals(15, LuminanceMath.scale(10, 2.0D));
    assertEquals(0, LuminanceMath.scale(15, 0.0D));
    assertEquals(0, LuminanceMath.scale(0, 2.0D));
    assertEquals(0, LuminanceMath.scale(15, Double.NaN));
  }

  @Test
  void versionGate() {
    assertTrue(LuminanceMath.isSupported("4.13.0+26.3", 4, 13));
    assertTrue(LuminanceMath.isSupported("4.14.2+26.4", 4, 13));
    assertFalse(LuminanceMath.isSupported("4.12.4+26.2", 4, 13));
    assertFalse(LuminanceMath.isSupported("5.0.0+27.1", 4, 13));
    assertTrue(LuminanceMath.isSupported("17.0.0-beta.3+26.3", 17, 0));
    assertFalse(LuminanceMath.isSupported("9.0.0+1.21.1", 17, 0));
    assertFalse(LuminanceMath.isSupported("garbage", 17, 0));
    assertFalse(LuminanceMath.isSupported(null, 17, 0));
  }
}
