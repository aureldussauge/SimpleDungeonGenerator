package com.mursaat.dungeongenerator;

import com.mursaat.dungeongenerator.graphs.*;

import java.util.*;

public class DungeonGenerator {

  DungeonParams dungeonParams;
  RoomParams roomParams;
  RoomGenerator roomGenerator;

  public DungeonGenerator() {
    this.dungeonParams = new DungeonParams();
    this.roomParams = new RoomParams();
    roomGenerator = new RoomGenerator(roomParams);
  }

  public DungeonGenerator(DungeonParams dungeonParams, RoomParams roomParams) {
    this.dungeonParams = dungeonParams;
    this.roomParams = roomParams;
    roomGenerator = new RoomGenerator(roomParams);
  }

  /**
   * Create a dungeon, according to {@link DungeonGenerator#dungeonParams} and {@link
   * DungeonGenerator#roomParams}
   *
   * @return
   */
  public Dungeon generateDungeon() {
    // 1 - Initialize the main dungeon parameters
    int roomCount = dungeonParams.getRandomRoomCount();
    int radius = dungeonParams.getRandomRadius();

    // 2 - Generate all the rooms we need
    ArrayList<DungeonRoom> rooms = new ArrayList<>();
    for (int roomNumber = 0; roomNumber < roomCount; roomNumber++) {
      DungeonRoom room = roomGenerator.generateRoom(radius);
      rooms.add(room);
    }

    // 3 - Remove all collision between the rooms
    handleRoomsCollision(rooms);

    // 4 - Ajust all rooms positions (remove negatives)
    // Find min and max positions
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;

    for (DungeonRoom room : rooms) {
      if (room.getX() < minX) minX = room.getX();
      if (room.getY() < minY) minY = room.getY();
    }
    for (DungeonRoom room : rooms) {
      Position roomPos = room.getPosition();
      roomPos.x -= minX;
      roomPos.y -= minY;
    }

    // 5 - Select the main rooms
    List<DungeonRoom> mainRooms = getMainRooms(rooms, rooms.size() / 2);

    // 6 - Use Delaunay triangulation
    Graph triangulationGraph = Graph.triangulate(mainRooms);

    // 7 - Get Minimal Spanning Tree (with 10% additional edges)
    Graph mstGraph = triangulationGraph.getMinimumSpanningTree(0.20f);

    // 8 - Get hallways rooms
    List<DungeonRoom> hallwayRooms = new ArrayList<>(rooms.size() - mainRooms.size());
    for (DungeonRoom room : rooms) {
      if (room.getType() == DungeonRoom.RoomType.HALLWAY_ROOM) {
        hallwayRooms.add(room);
      }
    }

    // 9 - Create the dungeon from the graph
    return new Dungeon(mstGraph, hallwayRooms, dungeonParams.getHallwaysWidth());
  }

  /**
   * Select and return the N biggest rooms
   *
   * @param rooms
   * @param n number of rooms selected
   * @return A list containing the N biggest rooms
   */
  private static List<DungeonRoom> getMainRooms(List<DungeonRoom> rooms, int n) {
    if (rooms.size() < n) {
      return rooms;
    }

    TreeSet<DungeonRoom> sortedList =
        new TreeSet<>(
            (r1, r2) -> r1.getWidth() * r1.getHeight() < r2.getWidth() * r2.getHeight() ? 1 : -1);
    sortedList.addAll(rooms);
    Iterator<DungeonRoom> itr = sortedList.iterator();

    List<DungeonRoom> mainRooms = new ArrayList<>();
    for (int roomCount = 0; roomCount < n; roomCount++) {
      DungeonRoom currMainRoom = itr.next();
      currMainRoom.setType(DungeonRoom.RoomType.MAIN_ROOM);
      mainRooms.add(currMainRoom);
    }

    return mainRooms;
  }

  /**
   * Detect if there is a collision between all the rooms. If true, it makes all the rooms deviate
   * from each other When it finish, there are no other collisions remaining
   *
   * @param rooms All the rooms we want to test
   */
  private void handleRoomsCollision(List<DungeonRoom> rooms) {
    boolean collide = true;
    while (collide) {
      collide = false;
      for (DungeonRoom r1 : rooms) {
        for (DungeonRoom r2 : rooms) {
          if (r2 != r1) {
            collide = handleRoomCollision(r1, r2) | collide;
          }
        }
      }
    }
  }

  /**
   * Detect if there is a collision between two rooms. If true, it makes the two rooms move away
   * from each other (1 unit only)
   *
   * @param r1 The first room
   * @param r2 The second room
   * @return true if there is a collision, false otherwise
   */
  private boolean handleRoomCollision(DungeonRoom r1, DungeonRoom r2) {

    final int space = dungeonParams.getMinSpaceBetweenRooms();
    if (r1.getX() - space < r2.getX() + r2.getWidth()
        && r1.getX() + r1.getWidth() + space > r2.getX()
        && r1.getY() - space < r2.getY() + r2.getHeight()
        && r1.getY() + r1.getHeight() + space > r2.getY()) {

      Direction bestDir = Direction.TOP;
      int bestDirSize = Integer.MAX_VALUE;

      if (r1.getX() + r1.getWidth() + space >= r2.getX()) {
        int newSize = r1.getX() + r1.getWidth() - r2.getX();
        if (newSize < bestDirSize) {
          bestDirSize = newSize;
          bestDir = Direction.RIGHT;
        }
      }
      if (r2.getX() + r2.getWidth() >= r1.getX() - space) {
        int newSize = r2.getX() + r2.getWidth() - r1.getX();
        if (newSize < bestDirSize) {
          bestDirSize = newSize;
          bestDir = Direction.LEFT;
        }
      }
      if (r1.getY() + r1.getHeight() + space >= r2.getY()) {
        int newSize = r1.getY() + r1.getHeight() - r2.getY();
        if (newSize < bestDirSize) {
          bestDirSize = newSize;
          bestDir = Direction.BOTTOM;
        }
      }
      if (r2.getY() + r2.getHeight() >= r1.getY() - space) {
        int newSize = r2.getY() + r2.getHeight() - r1.getY();
        if (newSize < bestDirSize) {
          bestDir = Direction.TOP;
        }
      }

      switch (bestDir) {
        case BOTTOM:
          r2.setY(r2.getY() + 1);
          r1.setY(r1.getY() - 1);
          break;
        case LEFT:
          r2.setX(r2.getX() - 1);
          r1.setX(r1.getX() + 1);
          break;
        case RIGHT:
          r2.setX(r2.getX() + 1);
          r1.setX(r1.getX() - 1);
          break;
        case TOP:
          r2.setY(r2.getY() - 1);
          r1.setY(r1.getY() + 1);
          break;
      }
      return true;
    }
    return false;
  }
}
