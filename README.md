# Malts

An open-source and free vaults plugin that incorporates a warehouse for mass item storage.

Beta builds can be found on Modrinth and Hangar:

- https://modrinth.com/plugin/malts
- https://hangar.papermc.io/BreweryTeam/Malts

## Quick overview of features if you're familiar with Vault plugins:

- A vault system similar to "PlayerVaults" vaults plugin.
- The ability for players to change the name, icon, and add "trusted" players to their vaults.
- The ability for players to share their vaults with other players of their choosing.
- A warehouse system for mass item storage.
- Guis for managing vaults and warehouse.
- The ability for admins to manage all vaults and warehouses.
- SQLite & MySQL support.
- A converter to migrate from "PlayerVaults/PlayerVaultsX".


## Some more info on Malts (for those who aren't familiar with vaults plugins):

- Malts is a plugin that allows players to use vaults and a warehouse to store items.


### Vaults
Vaults are virtual chests that players may use to store items of their choosing. They work very similarly to vaults
from the "PlayerVaults" plugin. Malts includes a GUI to show players their vaults in a nice and orderly fashion.
Player can customize, change the name, and even trust other players to their vaults to share them! The amount of
vaults a player can access is configurable and dynamically modifiable through commands!


### Warehouse
Every player get their own warehouse. Players can store materials in large quantities in their warehouse. The amount
of material they can store is based on their maximum stock which is configurable through commands and/or permissions.
The warehouse also has additional features such as "auto storing", "click to deposit", and "auto replenish".

### Importing
Malts supports importing vaults from "PlayerVaults" currently. More importers will be added closer to Malts' first
non-beta release.

### Quickbar
In the vaults gui,
a quickbar showing the most recently accessed warehouse compartments is available for player convenience.
This can be configured.

### QuickReturn
Players can right-click outside any Malts gui to return to "Your Vaults" gui. This can be configured.

Malts has other nuanced features too.
As stated before, this project is in **beta** and should not be used on production servers at the moment.
Malts only supports PaperMC-based servers.


Attached below are a showcase of some of the guis in Malts:

![yourvaults.png](images/yourvaults.png)



![quickbar.png](images/quickbar.png)



![warehouse.png](images/warehouse.png)



![warehouse2.png](images/warehouse2.png)



![warehouse3.png](images/warehouse3.png)
