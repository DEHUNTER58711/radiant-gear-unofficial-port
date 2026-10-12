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

import com.illusivesoulworks.radiantgear.RadiantGearConstants;
import java.util.Optional;
import net.neoforged.fml.ModList;

/**
 * Version gate and kill switch for the optional integrations. Every failure path logs one
 * warning and disables the integration instead of crashing the game.
 */
public final class CompatState {

  /** Curios major version this port was built against (17.x for MC 26.3). */
  public static final int CURIOS_MAJOR = 17;
  /** LambDynamicLights v4 API, 4.13+ (first release for MC 26.3). */
  public static final int LDL_MAJOR = 4;
  public static final int LDL_MIN_MINOR = 13;

  private static volatile boolean disabled = false;

  private CompatState() {}

  public static Optional<String> modVersion(String modId) {
    return ModList.get().getModContainerById(modId)
        .map(c -> c.getModInfo().getVersion().toString());
  }

  /** Checks presence/versions and probes the Curios API classes. Never throws. */
  public static boolean curiosUsable() {
    if (disabled) {
      return false;
    }
    Optional<String> curios = modVersion(RadiantGearConstants.CURIOS_ID);
    if (curios.isEmpty()) {
      disable("Curios API is not installed", null);
      return false;
    }
    if (!LuminanceMath.isSupported(curios.get(), CURIOS_MAJOR, 0)) {
      disable("unsupported Curios version " + curios.get() + " (expected " + CURIOS_MAJOR + ".x)",
          null);
      return false;
    }
    try {
      // Probe exactly the API members CuriosLightSampler links against.
      Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
      api.getMethod("getCuriosInventoryOrNull", net.minecraft.world.entity.LivingEntity.class);
      Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler")
          .getMethod("getCurios");
      Class<?> handler =
          Class.forName("top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler");
      handler.getMethod("size");
      handler.getMethod("getResource", int.class);
      handler.getMethod("getAmountAsInt", int.class);
      return true;
    } catch (ReflectiveOperationException | LinkageError e) {
      disable("Curios API " + curios.get() + " does not match the expected API", e);
      return false;
    }
  }

  public static boolean ldlVersionOk() {
    Optional<String> ldl = modVersion(RadiantGearConstants.LDL_ID);
    if (ldl.isEmpty()) {
      return false;
    }
    if (!LuminanceMath.isSupported(ldl.get(), LDL_MAJOR, LDL_MIN_MINOR)) {
      disable("unsupported LambDynamicLights version " + ldl.get() + " (expected " + LDL_MAJOR
          + "." + LDL_MIN_MINOR + "+)", null);
      return false;
    }
    return true;
  }

  public static boolean isDisabled() {
    return disabled;
  }

  public static void disable(String reason, Throwable cause) {
    if (disabled) {
      return;
    }
    disabled = true;
    if (cause != null) {
      RadiantGearConstants.LOG.warn("Radiant Gear: dynamic lights for Curios slots disabled: {}",
          reason, cause);
    } else {
      RadiantGearConstants.LOG.warn("Radiant Gear: dynamic lights for Curios slots disabled: {}",
          reason);
    }
  }
}
