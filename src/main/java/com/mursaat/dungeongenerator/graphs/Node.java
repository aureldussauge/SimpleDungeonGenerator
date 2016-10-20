package com.mursaat.dungeongenerator.graphs;

import com.mursaat.dungeongenerator.DungeonRoom;

/**
 * This class is used by {@link Graph} to create graphs. It allows to represent a node (which is a
 * room)
 */
public class Node {
  private DungeonRoom room;

  public Node(DungeonRoom room) {
    this.room = room;
  }

  public DungeonRoom getRoom() {
    return room;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Node node = (Node) o;

    return room != null ? room.equals(node.room) : node.room == null;
  }

  @Override
  public int hashCode() {
    return room != null ? room.hashCode() : 0;
  }
}
