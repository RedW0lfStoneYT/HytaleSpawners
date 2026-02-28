# Hytale Spawners
An advanced spawner mod for hytale  

![UpdateChecker Compatible](https://i.imgur.com/CpkvDty.png)
## Features
- Easy give command built in
- Customize spawner properties like radius, spawn rate, spawn count
- Supports all mob types
- Render the spawner entity in the middle of the spawner block

## Installation
1. Download the mod from the releases section or from [curse forge](https://curseforge.com/hytale/mods/mob-spawners).
2. Place the downloaded file into your Hytale mods folder.
3. Launce the server
4. Enjoy!

## API Usage
Using the API is super simple; All you need is to add the spawner mod as a maven dependency as shown below:

```groovy
repositories {
    maven {
        name "selenaDevelopmentReleases"
        url "https://repo.selenadevelopment.com/releases"
    }
}

dependencies {
    compileOnly "dev.selena:Spawners:VERSION"
}
```
Replace `VERSION` with the latest version of the mod.
### Modifying an existing spawner
Modifying a spawner is super simple

```java
// First get the spawner block entity
// If in a system use the CommandBuffer<ChunkStore> or otherwise use the Store<ChunkStore>
SpawnerBlock spawner = commandBuffer.getComponent(ref, SpawnerMain.get().getSpawnerBlockComponentType());
// Now we can easily modify the spawner properties 
// Be sure to use com.hypixel.hytale.protocol.Range for ranges
spawner.setSpawnIntervalTicks(new Range(1000, 8000)); // Set spawn interval between 1000 and 8000 ticks
```

### Creating a new spawner
Creating a new spawner is just as easy

```java
// First create a new spawner block state
SpawnerBlock spawner = new SpawnerBlock(spawnerType);
// Now set the desired properties
// Again make sure to use hypixel.hytale.protocol 
spawner.setSpawnRadius(new Size(5, 10)); // Set spawn radius to 5 wide and 10 tall

// Now get the ItemStack
ItemStack item = spawner.getItemStack();
```

### Events
The spawner mod has 4 events you can listen to
- SpawnerSpawnEvent.Pre - Called before a mob is spawned, can be canceled  
- SpawnerSpawnEvent.Post - Called after a mob is spawned
- SpawnerBlockPlaceEvent.Pre - Called before a spawner block is placed, can be canceled
- SpawnerBlockPlaceEvent.Post - Called after a spawner block is placed

## Commands 
*   `/spawner <mob_type> <target>` - Gives a spawner of the specified mob type to the target player - `spawner.give`  
    **NOTE: There are other options to the command that you can view with `/help spawner`**
*   `/sadmin debug-spawner <relativeLocation>` - Displays a preview of spawn radius (green) and nearby check radius (red), Will only show spawn radius if they are the same - `spawner.admin.debug`
*   `/sadmin reload` - Reloads the config and lang file - `spawner.admin.reload`

## Details on the give command options
- `--amount` - The amount of spawners to give (default: 1) - `--amount=10`
- `--spawnAmount` - The range of mobs that will spawn (default depends on config) - `--spawnAmount 5 10`
- `--spawnInterval` - The range of ticks between spawn attempts (default depends on config) - `--spawnInterval 1000 8000`
- `--spawnRadius` - The width (first argument) and height (second) radius that entities will spawn around the spawner (default depends on config) - `--spawnRadius 5 10`

## Config options

*   `spawnRange` - The range of entities to spawn
*   `spawnRadius` - The radius that entities will spawn around the spawner
*   `spawnTicksRange` - The min and max ticks between spawn attempts
*   `maxSpawnAttempts` - The max amount of failed spawn attempts before waiting for the next spawn attempt
*   `renderMobModel` - Should the preview mob spawn in the middle of the spawner
*   `maxNearbyEntities` - The max nearby entities
*   `checkNearbyEntities` - Should the spawner check for nearby entities before spawning
*   `nearbyEntitiesCheckRadius` - The radius around the spawner to check for nearby entities
*   `useWorldTimeTicks` - Should spawn attempts use the World time ticks or System time ticks (if you have `IsGameTimePaused` set to true and still want the spawners to spawn set this value to false)
*   `rotatePreviewEntity` - Should the preview entity rotate? (Experimental)
*   `previewEntityRotationDegreesPerTick` - The amount of degrees to rotate per tick
*   `nerfSpawnerMobs` - If set to true the spawned entity will be given the `Spawner_Role` (edit the max health on that to change the nerfed entity health) where it has no pathing or targeting (drops will still work)
*   `dropSpawnerWhenMined` - Should the spawner be dropped when mined?

### Want to buy or even upgrade your hytale account? Use code `RWS` at checkout for 5% off!