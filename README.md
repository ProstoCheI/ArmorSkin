# ArmorSkin

Turn armor into parts of any player's skin. Rename it in an anvil using their username: helmets become heads, chestplates become torsos with arms, and leggings and boots become legs. Wear a complete skin or mix parts from different players.

The custom models appear on players and armor stands, as well as in your inventory, in your hand, in item frames, and when dropped on the ground.

## How to use

1. Place a piece of armor in the first slot of an anvil.
2. Enter a player's username, such as `Notch`.
3. To customize its appearance, open the tag menu using the button with four squares. You can also type tags directly into the name.
4. Take the item and equip it on yourself or an armor stand.

Use a Minecraft Java Edition account username. An internet connection is required to load skins for the first time. Armor may keep its usual appearance while the texture loads.

| Item | Skin part |
| --- | --- |
| Helmet | Head, including the outer skin layer |
| Chestplate | Torso and arms, including the outer skin layer |
| Leggings | Upper legs |
| Boots | Lower legs |

By default, armor looks like a costume slightly larger than the player's body. The corresponding parts of the original model are hidden so they don't show through the custom skin.

## Tags

Add tags after the username, separated by spaces. Each tag consists of a hyphen followed by a single lowercase Latin letter.

| Tag | Applies to | Effect |
| --- | --- | --- |
| `-r` | Any armor piece | Uses the normal body size instead of the larger costume model |
| `-s` | Chestplate | Uses slim arms. Arms are wide by default |
| `-c` | Chestplate | Displays the named player's cape |
| `-e` | Chestplate | Displays standard elytra |
| `-c -e` | Chestplate | Displays elytra with the named player's cape texture, without a separate cape |

For example, naming a chestplate `Notch -r -s` gives it Notch's skin, slim arms, and a model that matches the normal body size.

Capes and elytra use Minecraft's normal animations. If the named player has no cape, `-c` adds nothing and `-c -e` displays standard elytra. These elytra are cosmetic and do not let you fly.

Tags are only visible when renaming an item in an anvil. Everywhere else, the item's name is displayed without them.

## Reloading skins

If a player has changed their skin or a texture failed to load, run this command in chat:

```text
/armorskin reload
```

This clears the mod's cache and starts reloading all previously requested skins. Operator permissions are not required.

## Installation and multiplayer

The current version supports **Minecraft Java Edition 1.21.1** with **Fabric Loader 0.16.14 or later**. **Fabric API** for 1.21.1 is also required.

Install Fabric, then place the ArmorSkin and Fabric API JAR files in your `mods` folder.

ArmorSkin runs entirely on the client and does not need to be installed on the server. Other players can see your custom armor if they also have the mod installed. Armor protection, durability, and other gameplay properties remain unchanged.
