# Fluid Skills

A library mod to control how players see and interact with fluids using skill-based rectrictions created in KubeJS
scripts. This is similar to Ore Stages, but it targets fluids instead of blocks and interacts with JEI/REI for hiding
recipes that use fluids, complementing Item Skills.

[![CurseForge](https://cf.way2muchnoise.eu/short_837104.svg)](https://www.curseforge.com/minecraft/mc-mods/fluid-skills)
[![Modrinth](https://img.shields.io/modrinth/dt/fluid-skills?color=bcdeb7&label=%20&logo=modrinth&logoColor=096765&style=plastic)](https://modrinth.com/mod/fluid-skills)
[![MIT license](https://img.shields.io/github/license/impleri/fluid-skills?color=bcdeb7&label=Source&logo=github&style=flat)](https://github.com/impleri/fluid-skills)
[![Discord](https://img.shields.io/discord/1093178610950623233?color=096765&label=Community&logo=discord&logoColor=bcdeb7&style=plastic)](https://discord.com/invite/avxJgbaUmG)
[![1.19.2](https://img.shields.io/maven-metadata/v?label=1.19.2&color=096765&metadataUrl=https%3A%2F%2Fmaven.impleri.org%2Fminecraft%2Fnet%2Fimpleri%2Ffluid-skills-1.19.2%2Fmaven-metadata.xml&style=flat)](https://github.com/impleri/fluid-skills#developers)
[![1.18.2](https://img.shields.io/maven-metadata/v?label=1.18.2&color=096765&metadataUrl=https%3A%2F%2Fmaven.impleri.org%2Fminecraft%2Fnet%2Fimpleri%2Ffluid-skills-1.18.2%2Fmaven-metadata.xml&style=flat)](https://github.com/impleri/fluid-skills#developers)

### xSkills Mods

[Player Skills](https://github.com/impleri/player-skills)
| [Block Skills](https://github.com/impleri/block-skills)
| [Dimension Skills](https://github.com/impleri/dimension-skills)
| [Fluid Skills](https://github.com/impleri/fluid-skills)
| [Item Skills](https://github.com/impleri/item-skills)
| [Mob Skills](https://github.com/impleri/mob-skills)

## Concepts

This mod leans extensively on Player Skills by creating and consuming the Skill-based Restrictions. Out of the box, this
mod can restrict whether a fluid can be removed from the world, crafted, visible in JEI or REI, and whether it can
be identified in a tooltip. It also provides a way to mask fluids in world by replacing them with either air or another
fluid (e.g. make all crude oil appear **and function** as lava). Lastly, this mod also provides a way to manipulate
fluid finitiude (e.g. make water finite or make lava infinite).

## KubeJS API

When creating restrictions, you have two avenues: `replace` the fluid with a different fluid or `modify` what a player
can do with the actual fluid. Replacements will trump other modifications. One example where you may want to use both is
in cascading progress: when the player starts, the fluid is replaced with something basic (e.g. replace oil with lava)

### Register

We use the `FluidSkillEvents.register` event to register fluid restrictions. If the player ***matches*** the criteria,
the following restrictions are applied. This can cascade with other restrictions, so any restrictions which replaces a
fluid will trump any which only add restrictions to the fluid. Also, any restrictions which deny the ability
will trump any which allow it. We also expose these methods to indicate what restrictions are in place for when a player
meets that condition. By default, no restrictions are set, so be sure to set actual
restrictions. [See Player Skills documentation for the shared API](https://github.com/impleri/player-skills#kubejs-restrictions-api).

#### Replacement methods

- `replaceWithFluid(fluid: string)` - Replaces the targeted fluid with the named replacement. Flowing states will be
  matched if possible when replacing in-world fluids.
- `replaceWithAir()` - Replaces the fluid with air (it's completely hidden!)

#### Allow Restriction Methods

- `nothing()` - shorthand to apply all "allow" restrictions
- `bucketable()` - Player can pick up a source block with a bucket
- `producible()` - Player can see recipes in JEI/REI that produce this fluid
- `consumable()` - Player can see recipes in JEI/REI that use this fluid

#### Deny Restriction Methods

- `everything()` - shorthand to apply the below "deny" abilities
- `unbucketable()` - Player cannot pick up a source block with a bucket
- `unproducible()` - Player cannot see recipes in JEI/REI that produce this fluid
- `unconsumable()` - Player cannot see recipes in JEI/REI that use this fluid

### Other methods

These methods do not use player conditions, but dimension and
biome [facets](https://github.com/impleri/player-skills#kubejs-restrictions-api) will apply.

- `infinite()`: Allows fluid to create new source blocks (default behavior for water)
- `finite()`: Disallows creating new source blocks (default behavior for lava)

#### Supported fluids

Vanilla fluids and any others which extend `FlowingFluid` without overriding the `getNewLiquid` method can be altered to
be (in)finite. Everything else should work for all fluids.

### Examples

```js
FluidSkillEvents.register(event => {
  // Replace lava with water
  event.restrict(
    'minecraft:lava',
    r => r.replaceWithFluid('water').if(player => player.cannot('skills:stage', 2))
  );

  // Make water finite when not in an ocean or river biome in the overworld
  event.restrict("water", (is) => {
    is.finite().inDimension("overworld").notInBiome("#is_ocean").notInBiome("river"); // Remember: No `if`/`unless` conditions will work with this
  });

  // Make it impossible to bucket lava when in the overworld
  event.restrict("lava", (is) => {
    is.unbucketable().inDimension("overworld").if(player => player.cannot('skills:stage', 2));
  });

  // Make it impossible to create lava using recipes (e.g. Create)
  event.restrict(
    'minecraft:lava',
    r => r.unproducible().if(player => player.cannot('skills:stage', 3))
  );
});
```

### Caveats

1. Adding a replacement restriction on common fluids (i.e. turning water in the Overworld into lava or similarly with
   lava in the Nether) can cause a lot of lag in Forge on skills change which could kill single player instances without
   enough memory.
2. We can only prevent _picking up_ a fluid with `unbucketable` because we are not guaranteed access to know what's in
   the bucket and whether it contains anything without sacrificing working with bucket-like items from mods. To prevent
   _placing_ a fluid, use Item Skills restrictions on the bucket item.
3. To see _replacements_ in world, Block Skills must be installed.
4. JEI integration does not remove recipes related to the `unconsumable` flag. It does hide the recipes from
   right-clicking on the ingredient. However, it does not remove the recipe itself -- only `unproducible` does that.
   That is, a crafty player could view a recipe that produces the item, then right click on the produced item to see
   what recipes with which it can be consumed.

## Developers

Add the following to your `build.gradle`. I depend
on [Architectury API](https://github.com/architectury/architectury-api), [KubeJS](https://github.com/KubeJS-Mods/KubeJS),
and [PlayerSkills](https://github.com/impleri/player-skills), so you'll need those as well.

```groovy
dependencies {
    // Common should always be included 
    modImplementation "net.impleri:fluid-skills-${minecraft_version}:${fluidskills_version}"
    // Plus forge
    modApi "net.impleri:fluid-skills-${minecraft_version}-forge:${fluidskills_version}"
    // Or fabric
    modApi "net.impleri:fluid-skills-${minecraft_version}-fabric:${fluidskills_version}"
}
repositories {
    maven {
        url = "https://maven.impleri.org/minecraft"
        name = "Impleri Mods"
        content {
            includeGroup "net.impleri"
        }
    }
}
```

## Modpacks

Want to use this in a modpack? Great! This was designed with modpack developers in mind. No need to ask.
