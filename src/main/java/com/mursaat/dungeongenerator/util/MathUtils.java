package com.mursaat.dungeongenerator.util;

import com.mursaat.dungeongenerator.Position;

import java.util.concurrent.ThreadLocalRandom;

/** Some useful math functions */
public class MathUtils {

  /**
   * @param radius the radius of the circle
   * @return a random position, in a circle of given radius
   */
  public static Position getRandomPositionInCircle(int radius) {
    double t = 2 * Math.PI * ThreadLocalRandom.current().nextDouble();
    double u = Math.random() + Math.random();
    double r = (u > 1) ? (2 - u) : u;

    int x = (int) (radius * r * Math.cos(t));
    int y = (int) (radius * r * Math.sin(t));
    return new Position(x, y);
  }

  /**
   * Compute the distance between the two positions.
   *
   * @param p1 the first position
   * @param p2 the second position
   * @return the distance between the two positions
   */
  public static double euclidianDist(Position p1, Position p2) {
    return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
  }

  /**
   * Compute the squared distance between the two positions. This is faster than the real distance
   * ({@link MathUtils#euclidianDist(Position, Position)})
   *
   * @param p1 the first position
   * @param p2 the second position
   * @return the squared distance between the two positions
   */
  public static double euclidianDist2(Position p1, Position p2) {
    return Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2);
  }
}
