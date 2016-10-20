package com.mursaat.dungeongenerator;

import java.util.ArrayList;
import java.util.List;

public class DungeonHallway implements DungeonStructure {
  private DungeonRoom roomFrom;
  private DungeonRoom roomTo;

  public List<Position> path;

  public DungeonHallway(DungeonRoom roomFrom, DungeonRoom roomTo) {
    this.roomFrom = roomFrom;
    this.roomTo = roomTo;
    path = new ArrayList<>();
  }

  public DungeonRoom getRoomFrom() {
    return roomFrom;
  }

  public DungeonRoom getRoomTo() {
    return roomTo;
  }

  public List<Position> getPath() {
    return path;
  }

  public void setPath(List<Position> path) {
    this.path = path;
  }

  public void addPosition(Position pos) {
    path.add(pos);
  }

  private void initPath() {
    // Generate the path between the two rooms (1 to 3 positions)

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

      // check if the hallway is empty
      if (leftRoom.getX() + leftRoom.getWidth() == rightRoom.getX()) {
        int doorX = rightRoom.getX();
        path = new ArrayList<>(1);
        path.add(new Position(doorX, doorY));
      }
      // else, create the tunnel
      else {
        path = new ArrayList<>(2);
        path.add(new Position(leftRoom.getX() + leftRoom.getWidth(), doorY));
        path.add(new Position(rightRoom.getX() - 1, doorY));
      }
    }

    // Test if we can create a simple vertical tunnel
    else if (roomXSpaceDifference > 0 && roomXSpaceDifference <= leftRoom.getWidth()) {
      int doorX = rightRoom.getX() + roomXSpaceDifference / 2;

      // check if the hallway is empty
      if (topRoom.getY() + topRoom.getHeight() == bottomRoom.getY()) {
        int doorY = bottomRoom.getY();
        path = new ArrayList<>(1);
        path.add(new Position(doorX, doorY));
      }
      // else, create the tunnel
      else {
        path = new ArrayList<>(2);
        path.add(new Position(doorX, topRoom.getY() + topRoom.getHeight()));
        path.add(new Position(doorX, bottomRoom.getY() - 1));
      }
    }

    // else, we have to create a L tunnel
    else {
      int firstXPos;
      int firstYPos;
      int secondXPos;
      int secondYPos;

      firstXPos = leftRoom.getX() + leftRoom.getWidth();
      firstYPos = leftRoom.getY() + leftRoom.getHeight() / 2;
      secondXPos = rightRoom.getX() + rightRoom.getWidth() / 2;

      if (leftRoom == bottomRoom) {
        secondYPos = rightRoom.getY() + rightRoom.getHeight();
      } else {
        secondYPos = rightRoom.getY() - 1;
      }

      path = new ArrayList<>(3);
      path.add(new Position(firstXPos, firstYPos));
      path.add(new Position(secondXPos, firstYPos));
      path.add(new Position(secondXPos, secondYPos));
    }
  }
}
