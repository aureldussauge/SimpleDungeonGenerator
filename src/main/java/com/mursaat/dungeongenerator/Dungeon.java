package com.mursaat.dungeongenerator;

import com.mursaat.dungeongenerator.graphs.Edge;
import com.mursaat.dungeongenerator.graphs.Graph;
import com.mursaat.dungeongenerator.graphs.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** A dungeon containing rooms and hallways */
public class Dungeon {
  private List<DungeonRoom> rooms;
  private List<DungeonHallway> hallways;
  private DungeonStructure[][] tiles;
  private int width;
  private int height;

  Dungeon(Graph graph, List<DungeonRoom> hallwayRooms, final int hallwayWidth) {

    List<Node> nodes = graph.getNodes();
    List<Edge> edges = graph.getEdges();

    rooms = new ArrayList<>(nodes.size());
    hallways = new ArrayList<>(edges.size());

    // Find width and height
    width = Integer.MIN_VALUE;
    height = Integer.MIN_VALUE;

    for (Node node : graph.getNodes()) {
      DungeonRoom room = node.getRoom();
      if (room.getX() + room.getWidth() > width) width = room.getX() + room.getWidth();
      if (room.getY() + room.getHeight() > height) height = room.getY() + room.getHeight();
    }
    for (DungeonRoom room : hallwayRooms) {
      if (room.getX() + room.getWidth() > width) width = room.getX() + room.getWidth();
      if (room.getY() + room.getHeight() > height) height = room.getY() + room.getHeight();
    }

    for (Node node : graph.getNodes()) {
      DungeonRoom room = node.getRoom();
      addRoom(room);
    }

    // Create the tiled map
    tiles = new DungeonStructure[height][width];

    for (DungeonRoom room : rooms) {
      Position roomPosition = room.getPosition();

      int yRoomMax = roomPosition.y + room.getHeight();
      int xRoomMax = roomPosition.x + room.getWidth();
      for (int y = roomPosition.y; y < yRoomMax; y++) {
        for (int x = roomPosition.x; x < xRoomMax; x++) {
          tiles[y][x] = room;
        }
      }
    }

    for (Edge edge : edges) {
      DungeonHallway hallway = createHallwayFromEdge(edge);
      hallways.add(hallway);

      if (!hallway.getPath().isEmpty()) {
        Position lastPosition = hallway.getPath().get(0);
        for (int posId = 1; posId < hallway.getPath().size(); posId++) {
          Position currPosition = hallway.getPath().get(posId);
          if (lastPosition.x != currPosition.x) {
            int lowestX = Math.min(currPosition.x, lastPosition.x);
            int greatestX = Math.max(currPosition.x, lastPosition.x);
            for (int x = lowestX; x <= greatestX; x++) {
              int lowestY = Math.max(currPosition.y - (hallwayWidth - 1) / 2, 0);
              int greatestY = Math.min(currPosition.y + (hallwayWidth - 1) / 2, height - 1);
              for (int y = lowestY; y <= greatestY; y++) {
                tiles[y][x] = tiles[y][x] == null ? hallway : tiles[y][x];
              }
            }
          }
          if (lastPosition.y != currPosition.y) {
            int lowestY = Math.min(currPosition.y, lastPosition.y);
            int greatestY = Math.max(currPosition.y, lastPosition.y);
            for (int y = lowestY; y <= greatestY; y++) {
              int lowestX = Math.max(currPosition.x - (hallwayWidth - 1) / 2, 0);
              int greatestX = Math.min(currPosition.x + (hallwayWidth - 1) / 2, width - 1);
              for (int x = lowestX; x <= greatestX; x++) {
                tiles[y][x] = tiles[y][x] == null ? hallway : tiles[y][x];
              }
            }
          }
          lastPosition = currPosition;
        }
      }
    }

    // Add HallwayRooms
    // 1 - Create another map just with these rooms
    DungeonRoom[][] hallwayRoomsTiles = new DungeonRoom[height][width];
    for (DungeonRoom room : hallwayRooms) {
      for (int y = room.getY(); y < room.getY() + room.getHeight(); y++) {
        for (int x = room.getX(); x < room.getX() + room.getWidth(); x++) {
          hallwayRoomsTiles[y][x] = room;
        }
      }
    }

    // 2 - Add hallwayRooms colliding with
    HashSet<DungeonRoom> usedHallwayRooms = new HashSet<>();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (hallwayRoomsTiles[y][x] != null && tiles[y][x] instanceof DungeonHallway) {
          DungeonRoom roomToAdd = hallwayRoomsTiles[y][x];
          if (!usedHallwayRooms.contains(roomToAdd)) {
            for (int roomY = roomToAdd.getY();
                roomY < roomToAdd.getY() + roomToAdd.getHeight();
                roomY++) {
              for (int roomX = roomToAdd.getX();
                  roomX < roomToAdd.getX() + roomToAdd.getWidth();
                  roomX++) {
                tiles[roomY][roomX] = roomToAdd;
              }
            }
            addRoom(roomToAdd);
            usedHallwayRooms.add(roomToAdd);
          }
        }
      }
    }
  }

  private DungeonHallway createHallwayFromEdge(Edge edge) {
    DungeonRoom roomFrom = edge.getFirstNode().getRoom();
    DungeonRoom roomTo = edge.getSecondNode().getRoom();

    DungeonHallway hallway = new DungeonHallway(roomFrom, roomTo);

    // Order the room by relative position
    DungeonRoom leftRoom = roomFrom.getX() < roomTo.getX() ? roomFrom : roomTo;
    DungeonRoom rightRoom = leftRoom == roomFrom ? roomTo : roomFrom;

    DungeonRoom bottomRoom = roomFrom.getY() > roomTo.getY() ? roomFrom : roomTo;
    DungeonRoom topRoom = bottomRoom == roomFrom ? roomTo : roomFrom;

    int roomYSpaceDifference = topRoom.getY() + topRoom.getHeight() - bottomRoom.getY();
    int roomXSpaceDifference = leftRoom.getX() + leftRoom.getWidth() - rightRoom.getX();

    // Test if we can create a simple horizontal tunnel
    if (roomYSpaceDifference > 0 && roomYSpaceDifference <= topRoom.getHeight()) {
      int doorY = bottomRoom.getY() + roomYSpaceDifference / 2;

      boolean collide = false;
      for (int x = leftRoom.getX() + leftRoom.getWidth(); x < rightRoom.getX(); x++) {
        collide |= tiles[doorY][x] != null;
      }

      if (!collide) {
        hallway.addPosition(new Position(leftRoom.getX() + leftRoom.getWidth(), doorY));
        hallway.addPosition(new Position(rightRoom.getX(), doorY));
      }
    }

    // Test if we can create a simple vertical tunnel
    else if (roomXSpaceDifference > 0 && roomXSpaceDifference <= leftRoom.getWidth()) {
      int doorX = rightRoom.getX() + roomXSpaceDifference / 2;

      boolean collide = false;
      for (int y = topRoom.getY() + topRoom.getHeight(); y < bottomRoom.getY(); y++) {
        collide |= tiles[y][doorX] != null;
      }

      if (!collide) {
        hallway.addPosition(new Position(doorX, topRoom.getY() + topRoom.getHeight()));
        hallway.addPosition(new Position(doorX, bottomRoom.getY()));
      }
    }

    // else, we have to create a L tunnel
    else {
      int firstXPos = leftRoom.getX() + leftRoom.getWidth();
      int firstYPos;
      int secondXPos;
      int secondYPos;

      if (leftRoom == bottomRoom) {
        secondYPos = rightRoom.getY() + rightRoom.getHeight();
      } else {
        secondYPos = rightRoom.getY() - 1;
      }

      List<List<Position>> possiblePaths = new ArrayList<>();

      for (firstYPos = leftRoom.getY();
          firstYPos < leftRoom.getY() + leftRoom.getHeight();
          firstYPos++) {
        for (secondXPos = rightRoom.getX();
            secondXPos < rightRoom.getX() + rightRoom.getWidth();
            secondXPos++) {

          boolean collide = false;

          int lowerX = Math.min(firstXPos, secondXPos);
          int greaterX = Math.max(firstXPos, secondXPos);
          for (int x = lowerX; x <= greaterX; x++) {
            collide |= (tiles[firstYPos][x] != null);
          }

          int lowerY = Math.min(firstYPos, secondYPos);
          int greaterY = Math.max(firstYPos, secondYPos);
          for (int y = lowerY; y <= greaterY; y++) {
            collide |= (tiles[y][secondXPos] != null);
          }

          if (!collide) {
            List<Position> currentPath = new ArrayList<>(3);
            currentPath.add(new Position(firstXPos, firstYPos));
            currentPath.add(new Position(secondXPos, firstYPos));
            currentPath.add(new Position(secondXPos, secondYPos));
            possiblePaths.add(currentPath);
          }
        }
      }

      firstYPos = bottomRoom.getY() - 1;

      if (leftRoom == bottomRoom) {
        secondXPos = topRoom.getX() - 1;
      } else {
        secondXPos = topRoom.getX() + topRoom.getWidth();
      }

      for (firstXPos = bottomRoom.getX();
          firstXPos < bottomRoom.getX() + bottomRoom.getWidth();
          firstXPos++) {
        for (secondYPos = topRoom.getY();
            secondYPos < topRoom.getY() + topRoom.getHeight();
            secondYPos++) {

          boolean collide = false;

          int lowerY = Math.min(firstYPos, secondYPos);
          int greaterY = Math.max(firstYPos, secondYPos);
          for (int y = lowerY; y <= greaterY; y++) {
            collide |= (tiles[y][firstXPos] != null);
          }

          int lowerX = Math.min(firstXPos, secondXPos);
          int greaterX = Math.max(firstXPos, secondXPos);
          for (int x = lowerX; x <= greaterX; x++) {
            collide |= (tiles[secondYPos][x] != null);
          }

          if (!collide) {
            List<Position> currentPath = new ArrayList<>(3);
            currentPath.add(new Position(firstXPos, firstYPos));
            currentPath.add(new Position(firstXPos, secondYPos));
            currentPath.add(new Position(secondXPos, secondYPos));
            possiblePaths.add(currentPath);
          }
        }
      }

      if (!possiblePaths.isEmpty()) {
        int randIndex = ThreadLocalRandom.current().nextInt(0, possiblePaths.size());
        List<Position> path = possiblePaths.get(randIndex);
        hallway.path = path;
      }
    }
    return hallway;
  }

  protected void addRoom(DungeonRoom room) {
    rooms.add(room);
  }

  protected void setRooms(List<DungeonRoom> rooms) {
    this.rooms = rooms;
  }

  public List<DungeonRoom> getRooms() {
    return rooms;
  }

  public List<DungeonHallway> getHallways() {
    return hallways;
  }

  public void setHallways(List<DungeonHallway> hallways) {
    this.hallways = hallways;
  }

  public DungeonStructure[][] getTiles() {
    return tiles;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
