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

import com.illusivesoulworks.radiantgear.compat.CompatState;
import com.illusivesoulworks.radiantgear.compat.CuriosLightSampler;
import com.illusivesoulworks.radiantgear.compat.LuminanceMath;
import com.illusivesoulworks.radiantgear.config.RadiantGearConfig;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** Entity luminance = brightest item in any (non-excluded) Curios slot, scaled by config. */
public final class CuriosEntityLuminance implements EntityLuminance {

  public static final CuriosEntityLuminance INSTANCE = new CuriosEntityLuminance();

  private CuriosEntityLuminance() {}

  @Override
  public Type type() {
    return RadiantGearDynamicLights.curiosType;
  }

  @Override
  public int getLuminance(ItemLightSourceManager items, Entity entity) {
    if (CompatState.isDisabled() || !(entity instanceof LivingEntity living)
        || entity.isSpectator()) {
      return 0;
    }
    try {
      // Same water rule as LambDynamicLights uses for held items.
      boolean submerged = entity.isEyeInFluid(NeoForgeMod.WATER_TYPE.value());
      int raw = CuriosLightSampler.maxLuminance(living, RadiantGearConfig.excludedSlots(),
          stack -> {
            int light = items.getLuminance(stack, submerged);
            if (RadiantGearDebug.enabled() && RadiantGearDebug.shouldLogEntity(entity.getId())) {
              RadiantGearDebug.log("  curios stack {} x{} -> LDL item light {}",
                  stack.getItem(), stack.getCount(), light);
            }
            return light;
          });
      int result = LuminanceMath.scale(raw, RadiantGearConfig.lightMultiplier());
      if (RadiantGearDebug.enabled() && RadiantGearDebug.shouldLogEntity(entity.getId())) {
        RadiantGearDebug.log("luminance {} ({}): curios max={}, multiplier={}, excluded={},"
                + " eyeInWater={} -> {}", EntityType.getKey(entity.getType()),
            entity.getId(), raw, RadiantGearConfig.lightMultiplier(),
            RadiantGearConfig.excludedSlots(), submerged, result);
      }
      return result;
    } catch (LinkageError | RuntimeException e) { // API drift at runtime: switch off, log once
      CompatState.disable("error while reading Curios slots", e);
      return 0;
    }
  }
}
