package com.mursaat.dungeongenerator;

import java.util.concurrent.ThreadLocalRandom;

public class DungeonParams {

  private int minRadius = 8;

  private int maxRadius = 16;

  private int minSpaceBetweenRooms = 1;

  /** The minimum number of room in the dungeon */
  private int minRoomCount = 30;

  /** The maximum number of room in the dungeon */
  private int maxRoomCount = 60;

  /** The width of the hallways (must be odd) */
  private int hallwaysWidth = 3;

  public int getMinSpaceBetweenRooms() {
    return minSpaceBetweenRooms;
  }

  public DungeonParams setMinSpaceBetweenRooms(int minSpaceBetweenRooms) {
    this.minSpaceBetweenRooms = minSpaceBetweenRooms;
    return this;
  }

  public int getMinRoomCount() {
    return minRoomCount;
  }

  public DungeonParams setMinRoomCount(int minRoomCount) {
    this.minRoomCount = Math.min(minRoomCount, this.maxRoomCount);
    return this;
  }

  public int getHallwaysWidth() {
    return hallwaysWidth;
  }

  public DungeonParams setHallwaysWidth(int hallwaysWidth) {
    this.hallwaysWidth = hallwaysWidth;
    return this;
  }

  public int getMaxRoomCount() {
    return maxRoomCount;
  }

  public DungeonParams setMaxRoomCount(int maxRoomCount) {
    this.maxRoomCount = Math.max(maxRoomCount, this.minRoomCount);
    return this;
  }

  public int getMinRadius() {
    return minRadius;
  }

  public DungeonParams setMinRadius(int minRadius) {
    this.minRadius = Math.min(minRadius, this.minRadius);
    return this;
  }

  public int getMaxRadius() {
    return maxRadius;
  }

  public DungeonParams setMaxRadius(int maxRadius) {
    this.maxRadius = Math.max(maxRadius, this.minRadius);
    return this;
  }

  public int getRandomRoomCount() {
    return ThreadLocalRandom.current().nextInt(minRoomCount, maxRoomCount + 1);
  }

  public int getRandomRadius() {
    return ThreadLocalRandom.current().nextInt(minRadius, maxRadius + 1);
  }
}
