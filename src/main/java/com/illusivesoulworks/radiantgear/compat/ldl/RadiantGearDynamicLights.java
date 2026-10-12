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
import com.illusivesoulworks.radiantgear.compat.CompatState;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * LambDynamicLights v4 entrypoint ({@code lambdynlights:initializer}, declared via
 * {@code yumi:entrypoints} in neoforge.mods.toml). Replaces the upstream
 * {@code LambDynLightsModule}/{@code RyoamicModule}, which used the removed
 * {@code DynamicLightHandlers} API. Only LambDynamicLights loads this class (client only).
 */
public final class RadiantGearDynamicLights implements DynamicLightsInitializer {

  static EntityLuminance.Type curiosType;

  @Override
  public void onInitializeDynamicLights(DynamicLightsContext context) {
    try {
      if (!CompatState.ldlVersionOk() || !CompatState.curiosUsable()) {
        return;
      }
      curiosType = EntityLuminance.Type.registerSimple(
          Identifier.fromNamespaceAndPath(RadiantGearConstants.MOD_ID, "curios"),
          CuriosEntityLuminance.INSTANCE);

      // Runs on every resource reload. LambDynamicLights takes the maximum of all sources of an
      // entity (held/armor items, entity JSON, ours), so registering is purely additive.
      context.entityLightSourceManager().onRegisterEvent().register(registerContext -> {
        if (CompatState.isDisabled()) {
          return;
        }
        int count = 0;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
          if (LivingEntity.class.isAssignableFrom(type.getBaseClass())) {
            registerContext.register(type, CuriosEntityLuminance.INSTANCE);
            count++;
          }
        }
        RadiantGearConstants.LOG.debug("Radiant Gear: Curios light source for {} entity types",
            count);
      });
      RadiantGearConstants.LOG.info(
          "Radiant Gear: LambDynamicLights integration for all Curios slots enabled");
    } catch (Throwable t) { // incompatible LambDynamicLights/Curios API: never crash the client
      if (t instanceof VirtualMachineError vme) {
        throw vme;
      }
      CompatState.disable("LambDynamicLights initialisation failed", t);
    }
  }
}
