# Hytale Spawners
An advanced spawner mod for hytale
## Features
- Easy give command built in
- Customize spawner properties like radius, spawn rate, spawn count
- Supports all mob types
- Render the spawner entity in the middle of the spawner block

## Installation
1. Download the mod from the releases section or from [curse forge](https://curseforge.com/hytale/mods/hytale-spawners).
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
There is only one command built in for giving spawners
- `/spawner <mob_type> <target>` - Gives a spawner of the specified mob type to the target player - `spawner.give`  
There are other options to the command that you can view with `/help spawner`


