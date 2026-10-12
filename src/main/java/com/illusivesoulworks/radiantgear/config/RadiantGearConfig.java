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

package com.illusivesoulworks.radiantgear.config;

import java.util.List;
import java.util.Set;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Client config (config/radiantgear-client.toml). */
public final class RadiantGearConfig {

  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_SLOTS;
  public static final ModConfigSpec.DoubleValue LIGHT_MULTIPLIER;

  static {
    ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    EXCLUDED_SLOTS = builder
        .comment("Curios slot ids whose items should NOT emit dynamic light through Radiant Gear,",
            "e.g. [\"belt\"] if another mod already lights that slot. LambDynamicLights uses the",
            "brightest source, so overlapping mods never add up; this is only needed to hand a",
            "slot over to another mod completely. Default: [] (all slots).")
        .defineListAllowEmpty("excludedSlots", List.of(), () -> "belt",
            o -> o instanceof String s && !s.isBlank());
    LIGHT_MULTIPLIER = builder
        .comment("Multiplier for the light level of items in Curios slots (Radiant Gear's share",
            "only; held items and other LambDynamicLights sources are not affected).",
            "Result is rounded and clamped to 0..15. 1.0 = same as holding the item.")
        .defineInRange("lightMultiplier", 1.0D, 0.0D, 2.0D);
    SPEC = builder.build();
  }

  private RadiantGearConfig() {}

  public static Set<String> excludedSlots() {
    if (!SPEC.isLoaded()) {
      return Set.of();
    }
    return Set.copyOf(EXCLUDED_SLOTS.get());
  }

  public static double lightMultiplier() {
    return SPEC.isLoaded() ? LIGHT_MULTIPLIER.get() : 1.0D;
  }
}
