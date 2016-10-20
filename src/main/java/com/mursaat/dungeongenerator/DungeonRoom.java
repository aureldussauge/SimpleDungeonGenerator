package com.mursaat.dungeongenerator;

public class DungeonRoom implements DungeonStructure {

  public enum RoomType {
    MAIN_ROOM,
    HALLWAY_ROOM
  }

  private static int nextIdToGive = 1;

  private int id;
  private Position position;
  private int width;
  private int height;
  private RoomType type;

  public DungeonRoom() {
    id = nextIdToGive++;
    type = RoomType.HALLWAY_ROOM;
    position = new Position();
  }

  public Position getPosition() {
    return position;
  }

  public int getWidth() {
    return width;
  }

  public DungeonRoom setWidth(int width) {
    this.width = width;
    return this;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public RoomType getType() {
    return type;
  }

  public void setType(RoomType type) {
    this.type = type;
  }

  public int getHeight() {
    return height;
  }

  public DungeonRoom setHeight(int height) {
    this.height = height;
    return this;
  }

  public int getX() {
    return position.x;
  }

  public int getY() {
    return position.y;
  }

  public DungeonRoom setX(int x) {
    position.x = x;
    return this;
  }

  public DungeonRoom setY(int y) {
    position.y = y;
    return this;
  }

  public Position getCenterPosition() {
    return new Position(position.x + width / 2, position.y + height / 2);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DungeonRoom room = (DungeonRoom) o;

    return id == room.id;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
