/*
 * Copyright (C) 2022 Illusive Soulworks
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
 *
 * MODIFIED: This file was changed by DEHunter58711 on 2026-10-13 for the unofficial
 * Minecraft 26.3 / NeoForge port (see PORTING_NOTES.md). It is not the original file.
 */

package com.illusivesoulworks.radiantgear.compat;

import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * Port of upstream {@code platform/services/LambDynamicLightsCurios} (NeoForge): computes the
 * brightest light level of all equipped Curios items of an entity.
 * <p>
 * Changes: Curios 17 replaced {@code IItemHandler} with NeoForge's {@code ResourceHandler}; the
 * port walks every slot type ({@code getCurios()}) so single slot ids can be excluded by config,
 * and the per-item light comes from the caller (LambDynamicLights' item rules). Only touch this
 * class after {@link CompatState#curiosUsable()} said yes: it links against the Curios API.
 */
public final class CuriosLightSampler {

  private CuriosLightSampler() {}

  public static int maxLuminance(LivingEntity entity, Set<String> excludedSlots,
                                 ToIntFunction<ItemStack> stackLuminance) {
    ICuriosItemHandler inventory = CuriosApi.getCuriosInventoryOrNull(entity);
    if (inventory == null) {
      return 0;
    }
    int luminance = 0;
    for (Map.Entry<String, ICurioStacksHandler> entry : inventory.getCurios().entrySet()) {
      if (excludedSlots.contains(entry.getKey())) {
        continue;
      }
      IDynamicStackHandler stacks = entry.getValue().getStacks();
      for (int i = 0; i < stacks.size(); i++) {
        ItemResource resource = stacks.getResource(i);
        if (resource == null || resource.isEmpty()) {
          continue;
        }
        int amount = Math.max(1, stacks.getAmountAsInt(i));
        luminance = Math.max(luminance, stackLuminance.applyAsInt(resource.toStack(amount)));
        if (luminance >= 15) {
          return 15;
        }
      }
    }
    return luminance;
  }
}
