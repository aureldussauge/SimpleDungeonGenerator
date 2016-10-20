package com.mursaat.dungeongenerator.graphs;

import com.mursaat.dungeongenerator.util.MathUtils;

/**
 * This class is used by {@link Graph} to create graphs. It allows to represent the link between two
 * {@link Node}
 */
public class Edge {
  private Node[] nodes;

  private double dist = -1;
  private double dist2 = -1;

  public Edge(Node firstNode, Node secondNode) {
    nodes = new Node[2];
    nodes[0] = firstNode;
    nodes[1] = secondNode;
  }

  /** @return the Euclidian distance of the edge */
  public Double getEulidianDist() {
    if (dist == -1) {
      dist =
          MathUtils.euclidianDist(
              nodes[0].getRoom().getPosition(), nodes[1].getRoom().getPosition());
    }
    return dist;
  }

  /**
   * Compute the squared distance of the edge. This is faster than the real distance ({@link
   * Edge#getEulidianDist()})
   *
   * @return the squared distance of the edge
   */
  public Double getEulidianDist2() {
    if (dist2 == -1) {
      dist2 =
          MathUtils.euclidianDist2(
              nodes[0].getRoom().getPosition(), nodes[1].getRoom().getPosition());
    }
    return dist2;
  }

  /**
   * @return a node of this edge (not the same as the one returned by {@link Edge#getSecondNode()})
   */
  public Node getFirstNode() {
    return nodes[0];
  }

  /**
   * @return a node of this edge (not the same as the one returned by {@link Edge#getFirstNode()})
   */
  public Node getSecondNode() {
    return nodes[1];
  }

  /** @return the two nodes of this edge in an array (size = 2) */
  public Node[] getNodes() {
    return nodes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Edge edge = (Edge) o;
    Node[] otherNodeEdges = edge.getNodes();

    return (otherNodeEdges[0] == nodes[0] && otherNodeEdges[1] == nodes[1])
        || (otherNodeEdges[0] == nodes[1] && otherNodeEdges[1] == nodes[0]);
  }

  @Override
  public int hashCode() {
    return nodes[0].hashCode() + nodes[1].hashCode();
  }
}
