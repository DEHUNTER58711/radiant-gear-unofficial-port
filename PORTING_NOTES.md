# Porting notes – Radiant Gear → Minecraft 26.3 / NeoForge (unofficial)

Unofficial port by **DEHunter58711**, 2026-10-13. Original mod **Radiant Gear** by
**Illusive Soulworks / TheIllusiveC4** (LGPL-3.0-or-later). Not affiliated with or endorsed by the
original author – please do not report problems with this port upstream.

## Base

* Upstream: https://github.com/illusivesoulworks/radiantgear, branch `1.20.6`, commit `e2ef2eb`
  ("4.0.0", 2024-05-30), version `4.0.0+1.20.6` (latest upstream code).
* Port version: `4.0.0+mc26.3-neoforge-unofficial.2`, MC range `[26.3,26.4)`.
* Toolchain: NeoForge 26.3.0.51-beta, ModDevGradle 2.0.147, Gradle 9.2.1, Java 25, Mojang names
  (upstream: MultiLoader template with NeoGradle/ForgeGradle/Loom, Parchment, Java 21).
* Single NeoForge module; the `common`, `fabric`, `forge`, `neoforge` and `buildSrc` projects and
  `checkstyle.xml` were removed.

## License / attribution

* `LICENSE`, `COPYING`, `COPYING.LESSER` unchanged (verbatim) and shipped in the jar as
  `LICENSE_radiantgear`, `COPYING_radiantgear`, `COPYING.LESSER_radiantgear`, plus this file as
  `PORTING_NOTES_radiantgear.md`.
* Every source file derived from upstream carries a "MODIFIED … by DEHunter58711 on 2026-10-13"
  notice below the original header (LGPL-3.0 §2 / GPL-3.0 §5a). New files are marked as added by
  DEHunter58711 under the same license.
* `neoforge.mods.toml`: displayName "Radiant Gear (Unofficial Port)", credits/authors name the
  original author; no `issueTrackerURL`, `displayURL`, `updateJSONURL`, `displayTest`.
* Icon: `radiantgear_icon.png` (400×400 RGBA PNG) is the port icon made by Pixelkit
  (`/workspace/publish/radiant-gear/art/icon_400.png`), set via `iconFile`. The upstream
  `radiantgear_icon.png` (original author's branding) is not used.

## Dependencies

| | Upstream 4.0.0 (1.20.6) | Port |
|---|---|---|
| Curios | `8.0.0-beta.4+1.20.6`, required | `17.0.0-beta.3+26.3` (API jar, compile only), **optional** |
| Light backend | Dynamic Lights, DL Reforged, Lucent, Ars Nouveau, RyoamicLights (LDL fork) | **LambDynamicLights 4.13.0+26.3 only** (`lambdynamiclights-api`, mojmap, compile only), optional, client |

Both are `optional` in the toml *without* a hard version range on purpose: a wrong version only
logs one warning and disables the integration instead of stopping the game (see below).

## Changelog of the port

* **unofficial.2** (2026-10-14)
  * Fix: Curios items gave no light. In 26.3 `EntityType#getBaseClass()` always returns
    `Entity.class` (default from `EntityTypeTest`), so the .1 filter
    `LivingEntity.class.isAssignableFrom(type.getBaseClass())` matched **no** entity type and the
    `radiantgear:curios` light source was never registered. The source is now registered for every
    entity type; `CuriosEntityLuminance` returns 0 for non-living entities.
  * New client config `debugLogging` (default `false`): logs registration (type count, player
    included), the Curios stacks read with their LDL item light, and the computed light per entity
    (at most every 5 s per entity), prefixed `[Radiant Gear debug]`.
* **unofficial.1** (2026-10-13): first port.

## Code changes

| Area | Upstream | Port | Reason |
|---|---|---|---|
| LDL integration | `LambDynLightsModule`/`RyoamicModule`: `DynamicLightHandlers.registerDynamicLightHandler(type, handler)` on `EntityJoinLevelEvent` | `compat/ldl/RadiantGearDynamicLights` (`DynamicLightsInitializer`, entrypoint `lambdynlights:initializer` via `[modproperties.radiantgear."yumi:entrypoints"]`) registers a custom `EntityLuminance` type `radiantgear:curios` for every entity type (.2; see changelog) in `EntityLightSourceManager.onRegisterEvent()` | LDL v4 removed the handler API; v4 is data/entrypoint based |
| Light value | luminance per stack from the light mod | `CuriosEntityLuminance`: `ItemLightSourceManager.getLuminance(stack, eyeInWater)` for each stack, max over all slots | uses LDL's own item rules (JSON, `water_sensitive`) exactly like held items |
| Curios access | `getEquippedCurios()` as `IItemHandler` | `CuriosLightSampler`: `getCurios()` per slot id → `IDynamicStackHandler` as `ResourceHandler<ItemResource>` (`size/getResource/getAmountAsInt`) | Curios 17 removed `IItemHandlerModifiable`; per-slot loop allows excluding slots |
| Water check | `entity.isInWater()` | `entity.isEyeInFluid(NeoForgeMod.WATER_TYPE)` | same rule LDL uses for held items |
| Crash protection | none | `CompatState`: LDL must be 4.13+ (<5), Curios 17.x; Curios API members probed by reflection; any `LinkageError`/runtime error in the integration logs one WARN and switches it off | community issues #2/#11/#27/#32/#34/#35 were all crashes after API changes |
| Mod class | registered 5 backends, `IEventBus` listeners | registers client config + built-in data pack only; never touches LDL/Curios classes | dedicated server loads without either mod |
| Config (new) | – | `config/radiantgear-client.toml`: `excludedSlots` (default `[]`), `lightMultiplier` (0.0–2.0, default 1.0), `debugLogging` (default false, .2) | see "DynamicLantern" and "Light multiplier" |
| Data pack (new) | – | built-in pack `mod/radiantgear:datapacks/default_light_curios`, `PackSource.FEATURE` = **disabled by default** | issue #24/#15 + Reddit: users want working defaults |

### Built-in data pack `default_light_curios` (off by default)
* `data/radiantgear/curios/entities/default_light_curios.json`: gives `minecraft:player` the
  Curios `belt` and `charm` slots.
* `data/curios/tags/item/belt.json`: torch, soul torch, copper torch, lantern, soul lantern, all
  copper lanterns (copper ids `required: false`).
* `data/curios/tags/item/charm.json`: glowstone, glowstone dust, glow berries, glow ink sac,
  shroomlight, sea lantern, prismarine crystals, blaze rod.
* Enable: world creation → Data Packs, or `/datapack enable "mod/radiantgear:datapacks/default_light_curios"`.
  Needs Radiant Gear + Curios on the **server** (Curios slots/tags are server data).

## Investigations

### Does LDL 4.13 handle Curios itself? – No
`lambdynamiclights-4.13.0+26.3.jar` only contains `compat/AccessoriesCompat` and
`compat/TrinketsCompat`; there is no Curios layer. Radiant Gear is still needed for Curios.

### How do several light sources combine? – Maximum, never added
Bytecode of LDL 4.13: `LivingEntityMixin.dynamicLightTick` sets luminance from held/armor items
(`getLivingEntityLuminanceFromItems`, incl. compat layers), then `max` with
`getLuminanceFrom(entity)`; `EntityLightSources.getLuminance` takes `Math.max` over all registered
sources and `EntityLuminance.getLuminance(list)` is also a max. → A Curios item never makes the
player brighter than its own light value; two mods lighting the same item cannot "double" light.

### DynamicLantern (CurseForge 1552501, `dynamiclantern-2.0.0-mc26.3-neoforge.jar`, file 8923151)
* NeoForge 26.3 build **requires `architectury` and `trinkets_updated`**; it ships only a Trinkets
  backend (`compat/trinkets/*`, `data/trinkets/...`) – no Curios backend classes in this build.
* Light: mixin `LambDynamicLightsMixin` injects into `LambDynLights.getLivingEntityLuminanceFromItems`
  and adds its waist items; `LambDynamicLightsCuriosCompatMixin` targets
  `dev.lambdaurora.lambdynlights.compat.CuriosCompat`, which does not exist in LDL 4.13 (optional
  mixin, inactive).
* Result: **no double light.** On 26.3 NeoForge DynamicLantern reads Trinkets slots, Radiant Gear
  reads Curios slots; even if both lit the same item, LDL uses the maximum. `excludedSlots` is
  still provided (default `[]`) for anyone who wants another mod to own a slot completely (e.g.
  `["belt"]`).

### Light multiplier – implemented (scoped)
LDL's API has no global "scale" hook, but Radiant Gear computes its own entity luminance, so the
factor is applied cleanly to Radiant Gear's share only: `round(light × factor)` clamped to 0–15.
Limits: LDL light levels are 0–15, so >1.0 cannot exceed 15 and does not increase the *range* of a
level-15 item; held items and other LDL sources are not affected.

## In-game differences to the original

* Only LambDynamicLights is supported (Dynamic Lights, DL Reforged, Lucent, Ars Nouveau and
  RyoamicLights backends dropped: no 26.3 NeoForge versions / RyoamicLights is marked
  incompatible by LDL). No Fabric/Forge builds.
* Curios is optional (without it the mod does nothing and logs one warning).
* New client config (excluded slots, light multiplier) and optional data pack.
* LDL path (bytecode 4.13): our source is read via `LambDynLights.getLuminanceFrom`, which requires
  LDL's **Entities** setting, and for the own player also **Self light** (both default on), plus the
  per-entity-type toggle (Player) in LDL's entity list. Held items do NOT depend on "Entities", so a
  glowing held torch does not prove that option is on.
* Lighting falls under LDL's **Entities** category (LDL setting "Entities" must be on), like
  upstream with LDL (issue #9).
* Sides: light logic is client only. Server needs the mod only for the built-in data pack; Curios
  itself must be on both sides.

## Tests (2026-10-13)
* `./gradlew build` incl. JUnit `LuminanceMathTest` (scaling/rounding/clamping, version gate).
* Dedicated test server with radiantgear + Curios: start OK, pack listed as available
  `(feature)` and not enabled; after enable + restart Curios logs "Loaded 1 curio entities"; no tag
  errors; pack disabled again, clean stop. Server with radiantgear alone: start/stop OK.
* `runClient` (no GPU, `earlyWindowControl=false` in run/config): LDL calls the entrypoint →
  "LambDynamicLights integration for all Curios slots enabled"; with `-PnoCurios` → one WARN
  "disabled: Curios API is not installed", no crash; then SDL window creation fails (expected).
  Resource-reload registration and actual light are only testable in a real client.
