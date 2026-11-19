# Realistic Animals

A Fabric mod for **Minecraft 26.3** that makes passive mobs, especially farm animals, act like real animals.

It runs entirely on the server, so it works in singleplayer and on servers. Players joining a server don't need to install it.

## What animals do now

| Behaviour | What you'll see |
| --- | --- |
| **Herd alarm** | Hit one cow and the whole herd bolts from the attacker, and one of them usually calls out. Adult llamas and trader llamas stand their ground and spit at the attacker instead. |
| **Predator avoidance** | Chickens and rabbits run from foxes, cats, ocelots and wild wolves. Cows, sheep, pigs, horses and llamas run from wild wolves and polar bears. Every animal runs from hostile monsters that come close, and from any mob that is targeting it. Tamed pets don't scare them. |
| **Wary of people** | Sprinting at an animal startles it, and babies spook from farther away. Sneaking or holding the animal's food lets you walk up calmly. Leashed and tamed animals never flee from you. |
| **Memory** | An animal remembers whoever hurt it or its herd for about 2 minutes and runs from that person on sight. |
| **Trust** | Hand-feed an animal its food (green sparkles appear) and it learns to trust you, so it stays calm even when you sprint past. Trust is saved with the animal. |
| **Foraging** | Animals walk over to their food lying on the ground (wheat, seeds, carrots and so on) and eat it, healing themselves. Babies also grow faster. Each animal eats one item, then waits about 30 seconds before eating again. |
| **Grazing** | Cows, mooshrooms, horses, donkeys, mules and llamas stop to graze. They eat short grass and ferns (when `mobGriefing` is on) or nibble the pasture, and heal a little as they do. Pastures never turn to dirt. |
| **Herd cohesion** | Adults that wander away from their herd drift back toward the group. |
| **Night rest** | After dark, animals settle down and stay put instead of wandering all night. They still react to danger and to food. |
| **Storm shelter** | During thunderstorms, animals look for cover such as trees, overhangs or barns, and wait it out there. |
| **Injuries** | Badly hurt animals limp (move 30% slower). Animals that are left alone for 30 seconds slowly heal. |
| **Milking cooldown** | A cow or mooshroom can be milked (or give stew) once every 5 minutes. |

Affected animals: cow, mooshroom, sheep, pig, chicken, rabbit, horse, donkey, mule, llama and trader llama.
(Goats, camels and armadillos use Mojang's newer "brain" AI and are left untouched.)

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) 0.20.1 or newer for Minecraft 26.3.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) and `realistic-animals-1.0.0.jar` into your `mods` folder.
3. Use Java 25 or newer.

`realistic-animals-1.0.0+26.3.zip` in this repository contains the ready-to-use jar (`mod/`) and the full source project (`source/`).

## Configuration

`config/realisticanimals.json` is created on first launch. Every behaviour can be switched off, and every distance, duration and speed can be tuned. Times are in ticks (20 ticks = 1 second).

## Data pack customisation

Which animals get each behaviour is controlled by entity type tags under `data/realisticanimals/tags/entity_type/`:
`realistic_behavior`, `herd_animals`, `grazers`, `small_prey`, `livestock`, `hunts_small_prey`, `hunts_livestock` and `defends_herd`.
You can override them with a data pack. Only goal-based mobs belong in `realistic_behavior`.

## Building

```
./gradlew build distZip
```

The jar is written to `build/libs/` and the shareable zip to `build/dist/`. GitHub Actions (`.github/workflows/build.yml`) runs the same build on every push and uploads both files.
