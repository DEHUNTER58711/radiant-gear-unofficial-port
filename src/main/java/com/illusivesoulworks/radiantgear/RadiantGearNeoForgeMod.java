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

package com.illusivesoulworks.radiantgear;

import com.illusivesoulworks.radiantgear.config.RadiantGearConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * Port: the upstream class wired five dynamic-light backends (Curios + Dynamic Lights, DL Reforged,
 * Lucent, Ars Nouveau, RyoamicLights). On 26.3 NeoForge only LambDynamicLights v4 is left; its
 * integration is not referenced here at all. LambDynamicLights discovers
 * {@link com.illusivesoulworks.radiantgear.compat.ldl.RadiantGearDynamicLights} through the
 * {@code yumi:entrypoints} entry in neoforge.mods.toml, so this class loads fine on a dedicated
 * server and without LambDynamicLights or Curios installed.
 */
@Mod(RadiantGearConstants.MOD_ID)
public class RadiantGearNeoForgeMod {

  /** Built-in, disabled-by-default data pack with example Curios slots/tags (see FAQ). */
  public static final String DEFAULT_PACK_PATH = "datapacks/default_light_curios";

  public RadiantGearNeoForgeMod(IEventBus eventBus, ModContainer container) {
    container.registerConfig(ModConfig.Type.CLIENT, RadiantGearConfig.SPEC);
    eventBus.addListener(this::addPackFinders);
  }

  private void addPackFinders(AddPackFindersEvent event) {
    // PackSource.FEATURE = listed but NOT enabled automatically; the user turns it on.
    event.addPackFinders(
        Identifier.fromNamespaceAndPath(RadiantGearConstants.MOD_ID, DEFAULT_PACK_PATH),
        PackType.SERVER_DATA,
        Component.translatable("pack.radiantgear.default_light_curios"),
        PackSource.FEATURE, false, Pack.Position.TOP);
  }
}
