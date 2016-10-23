# SimpleDungeonGenerator

This library allows to generator simple dungeons map.<br>
It uses the algorithm described here : http://www.gamasutra.com/blogs/AAdonaac/20150903/252889/Procedural_Dungeon_Generation_Algorithm.php and here https://www.reddit.com/r/gamedev/comments/1dlwc4/procedural_dungeon_generation_algorithm_explained/<br>

Latest release
---------------

The most recent release is v0.0.1, released October 23, 2016

You can get the .jar here : https://github.com/Mursaat/SimpleDungeonGenerator/releases/tag/v0.0.1

To add a dependency using Gradle:
```
repositories {
	jcenter()
	maven { url "https://jitpack.io" }
}
dependencies {
	compile 'com.github.mursaat:simpledungeongenerator:v0.0.1'
}
```

Getting started
---------------

Create a DungeonGenerator, which will allow to create multiple dungeon
```java
DungeonGenerator myGenerator = new DungeonGenerator();
Dungeon dungeon = myGenerator.generateDungeon();
System.out.println(dungeon);
```

Where **toString()** is called on a dungeon object, it will display the map in the console.<br>
'X' = A room tile<br>
'O' = An hallway tile<br>
Spaces are Walls<br>

For example : 
```
                                    XXXXXXXXXXX               XXXXXXXXXX                 
                                    XXXXXXXXXXX               XXXXXXXXXX      XXXXXXXXXX 
                                    XXXXXXXXXXX               XXXXXXXXXX      XXXXXXXXXX 
                                    XXXXXXXXXXX               XXXXXXXXXX      XXXXXXXXXX 
                                    XXXXXXXXXXX               XXXXXXXXXX      XXXXXXXXXX 
                                    XXXXXXXXXXX               XXXXXXXXXX  XXX XXXXXXXXXX 
                                    XXXXXXXXXXX XXXXXXXXXXXXX XXXXXXXXXXOOXXXOXXXXXXXXXX 
                         XXXXXXXXXX XXXXXXXXXXX XXXXXXXXXXXXX XXXXXXXXXXOOXXXOXXXXXXXXXX 
                         XXXXXXXXXX XXXXXXXXXXXOXXXXXXXXXXXXX XXXXXXXXXXOOXXXOXXXXXXXXXX 
                         XXXXXXXXXXOXXXXXXXXXXXOXXXXXXXXXXXXXOXXXXXXXXXX  XXX XXXXXXXXXX 
                         XXXXXXXXXXOXXXXXXXXXXXOXXXXXXXXXXXXXOXXXXXXXXXX  XXX XXXXXXXXXX 
                         XXXXXXXXXXOXXXXXXXXXXX XXXXXXXXXXXXXOXXXXXXXXXX  XXX XXXXXXXXXX 
                         XXXXXXXXXX XXXXXXXXXXX XXXXXXXXXXXXX XXXXXXXXXX  XXX XXXXXXXXXX 
                         XXXXXXXXXX             XXXXXXXXXXXXXOXXXXXXXXXX      XXXXXXXXXX 
                         XXXXXXXXXX             XXXXXXXXXXXXXOOOO   OOO       XXXXXXXXXX 
                         XXXXXXXXXX                          OXXXXXXXXXXXXXXX XXXXXXXXXX 
                         XXXXXXXXXX                           XXXXXXXXXXXXXXX            
                         XXXXXXXXXX                           XXXXXXXXXXXXXXX            
                                 OOO                          XXXXXXXXXXXXXXX            
                                 OXXXXX                       XXXXXXXXXXXXXXX            
                                 OXXXXX                       XXXXXXXXXXXXXXX            
                                 OXXXXX                       XXXXXXXXXXXXXXX            
                                 OXXXXX                               OOO                
                                 OXXXXX                          XXXXXXXXXXXXX           
                                 OXXXXX                          XXXXXXXXXXXXX           
                                 OXXXXX                          XXXXXXXXXXXXX           
                                 OXXXXX      XXXXXXXXXXXXXXX     XXXXXXXXXXXXX           
                                 OXXXXX      XXXXXXXXXXXXXXX     XXXXXXXXXXXXX           
                                 OXXXXX      XXXXXXXXXXXXXXX     XXXXXXXXXXXXX XXXXXXXXX 
                                 OOO         XXXXXXXXXXXXXXX     XXXXXXXXXXXXX XXXXXXXXX 
                     XXXXX       XXXXXXXXXXXOXXXXXXXXXXXXXXX     XXXXXXXXXXXXX XXXXXXXXX 
                     XXXXX       XXXXXXXXXXXOXXXXXXXXXXXXXXXOOOOOXXXXXXXXXXXXX XXXXXXXXX 
                     XXXXX       XXXXXXXXXXXOXXXXXXXXXXXXXXXOOOOOXXXXXXXXXXXXXOXXXXXXXXX 
                     XXXXXOOOOOOOXXXXXXXXXXX                OOOOOXXXXXXXXXXXXXOXXXXXXXXX 
                     XXXXXOOOOOOOXXXXXXXXXXX  XXXXXXXXXXXXXXX    XXXXXXXXXXXXXOXXXXXXXXX 
                     XXXXXOOOOOOOXXXXXXXXXXX  XXXXXXXXXXXXXXXOOOOXXXXXXXXXXXXX XXXXXXXXX 
					 XXXXX       XXXXXXXXXXX  XXXXXXXXXXXXXXXOOOOXXXXXXXXXXXXX XXXXXXXXX 
                     XXXXX               OOO  XXXXXXXXXXXXXXXOOOOXXXXXXXXXXXXX XXXXXXXXX 
                     XXXXX               OOO  XXXXXXXXXXXXXXX          OOO     XXXXXXXXX 
                     XXXXX               OOO             OOO      XXXXXXXXXXXX           
                       OOO               XXXXXXX XXXX XXXXXXXXXXX XXXXXXXXXXXX           
                       OOO               XXXXXXX XXXX XXXXXXXXXXX XXXXXXXXXXXX           
                       OOO               XXXXXXX XXXX XXXXXXXXXXX XXXXXXXXXXXX           
XXXXXXXXXXXXXX         OOO               XXXXXXX XXXX XXXXXXXXXXXOXXXXXXXXXXXX           
XXXXXXXXXXXXXX XXXXXXXXXXOXXXXXXXXOXXXXXOXXXXXXX XXXX XXXXXXXXXXXOXXXXXXXXXXXX           
XXXXXXXXXXXXXX XXXXXXXXXXOXXXXXXXXOXXXXXOXXXXXXX XXXX XXXXXXXXXXXOXXXXXXXXXXXX           
XXXXXXXXXXXXXX XXXXXXXXXXOXXXXXXXXOXXXXXOXXXXXXXOXXXXOXXXXXXXXXXX XXXXXXXXXXXX           
XXXXXXXXXXXXXX XXXXXXXXXX XXXXXXXX XXXXXOXXXXXXXOXXXXOXXXXXXXXXXX XXXXXXXXXXXX           
XXXXXXXXXXXXXX XXXXXXXXXX XXXXXXXX XXXXXOXXXXXXXOXXXXOO   OOO                            
XXXXXXXXXXXXXX XXXXXXXXXX XXXXXXXX XXXXXOXXXXXXX XXXX XXXXXXX                            
XXXXXXXXXXXXXXOXXXXXXXXXX XXXXXXXX XXXXXOXXXXXXX XXXX XXXXXXX                            
XXXXXXXXXXXXXXOXXXXXXXXXX XXXXXXXX XXXXXOXXXXXXX XXXX XXXXXXX                            
XXXXXXXXXXXXXXOXXXXXXXXXX          XXXXXOXXXXXXX XXXX XXXXXXX                            
XXXXXXXXXXXXXX XXXXXXXXXX          XXXXXOXXXXXXX      XXXXXXX                            
XXXXXXXXXXXXXX XXXXXXXXXX          XXXXXOXXXXXXX      XXXXXXX                            
XXXXXXXXXXXXXX XXXXXXXXXX          XXXXXOO   OOO      XXXXXXX                            
XXXXXXXXXXXXXX                     XXXXXOO   OOO      XXXXXXX                            
XXXXXXXXXXXXXX                     XXXXXOO   OOO      XXXXXXX XXXXXXXXX                  
                                       OOO   OOO      XXXXXXX XXXXXXXXX                  
                                     XXXXXXXXXXXXXX   XXXXXXXOXXXXXXXXX                  
                                     XXXXXXXXXXXXXX   XXXXXXXOXXXXXXXXX                  
                                     XXXXXXXXXXXXXX   XXXXXXXOXXXXXXXXX  XXXXXXXXXXXXXX  
                                     XXXXXXXXXXXXXX   XXXXXXX XXXXXXXXX  XXXXXXXXXXXXXX  
                                     XXXXXXXXXXXXXX           XXXXXXXXX  XXXXXXXXXXXXXX  
                                     XXXXXXXXXXXXXX           XXXXXXXXXOOXXXXXXXXXXXXXX  
                                     XXXXXXXXXXXXXX           XXXXXXXXXOOXXXXXXXXXXXXXX  
                                     XXXXXXXXXXXXXX           XXXXXXXXXOOXXXXXXXXXXXXXX  
                                     XXXXXXXXXXXXXX           XXXXXXXXX  XXXXXXXXXXXXXX  
                                                              XXXXXXXXX  XXXXXXXXXXXXXX  
                                                              XXXXXXXXX  XXXXXXXXXXXXXX  
                                                                         XXXXXXXXXXXXXX  
                                                                         XXXXXXXXXXXXXX  
                                                                         XXXXXXXXXXXXXX  
                                                                         XXXXXXXXXXXXXX  
```

You can also control multiple parameters about the generation. Just use the second constructor of DungeonGenerator.
Here is an example :

```java
DungeonParams dungeonParams = new DungeonParams();
dungeonParams.setHallwaysWidth(3);
dungeonParams.setMinSpaceBetweenRooms(1);
dungeonParams.setMinRoomCount(20);
dungeonParams.setMaxRoomCount(30);

RoomParams roomParams = new RoomParams();
roomParams.setMinWidth(10);
roomParams.setMaxWidth(20);

DungeonGenerator myGenerator = new DungeonGenerator(dungeonParams, roomParams);
Dungeon dungeon = new DungeonGenerator().generateDungeon();
```

Params you can control on DungeonParams :
* **minSpaceBetweenRooms** : The minimum space which separate all rooms
* **minRoomCount** : The minimum number of rooms generated
* **maxRoomCount** : The maximum number of rooms generated
*In this version, we apply a filter to select only certain rooms, so we can have less rooms than the minRoomCount*
* **hallwaysWidth** : The hallway width, must be an odd number (ex : 1,3,...)
*For next version, it would be possible to have hallway using pair width*

**Params you can control on RoomParams :**
* **minHeight** : The minimal height for a room
* **maxHeight** : The maximal height for a room
* **minWidth** : The minimal width for a room
* **maxWidth** : The maximal width for a room

**How to get dungeon datas ?**
On a given dungeon, you can get the tile array by using **getTiles()**. Each value of the returned 2D array is a null (wall) or a Reference on a room or hallway.
If you want the list of rooms or hallways, you can also use **getRooms()** and **getHallways()**.
