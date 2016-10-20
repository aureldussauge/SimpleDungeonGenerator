package com.mursaat.dungeongenerator;

import java.util.concurrent.ThreadLocalRandom;

public class RoomParams {
  /** The minimum width of the generated room */
  private int minWidth = 3;

  /** The minimum height of the generated room */
  private int minHeight = 3;

  /** The maximum width of the generated room */
  private int maxWidth = 15;

  /** The maximum height of the generated room */
  private int maxHeight = 15;

  public RoomParams() {}

  public int getMinWidth() {
    return minWidth;
  }

  public RoomParams setMinWidth(int minWidth) {
    this.minWidth = minWidth;
    return this;
  }

  public int getMinHeight() {
    return minHeight;
  }

  public RoomParams setMinHeight(int minHeight) {
    this.minHeight = minHeight;
    return this;
  }

  public int getMaxWidth() {
    return maxWidth;
  }

  public RoomParams setMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
    return this;
  }

  public int getMaxHeight() {
    return maxHeight;
  }

  public RoomParams setMaxHeight(int maxHeight) {
    this.maxHeight = maxHeight;
    return this;
  }

  public int getRandomWidth() {
    return ThreadLocalRandom.current().nextInt(minWidth, maxWidth + 1);
  }

  public int getRandomHeight() {
    return ThreadLocalRandom.current().nextInt(minHeight, maxHeight + 1);
  }
}
