package com.mursaat.dungeongenerator.graphs;

import com.mursaat.dungeongenerator.DungeonRoom;
import com.mursaat.dungeongenerator.Position;

import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/** This class can be used to represent a graph using nodes and edges. */
public class Graph {
  List<Edge> edges;
  List<Node> nodes;

  Map<Node, List<Edge>> edgesByNodes;
  Map<Node, List<Node>> neighbors;

  public Graph() {
    edges = new ArrayList<>();
    nodes = new ArrayList<>();
    edgesByNodes = new HashMap<>();
    neighbors = new HashMap<>();
  }

  /**
   * Add a node in the list in this graph
   *
   * @param node the node added
   */
  public void addNode(Node node) {
    nodes.add(node);
  }

  /** @return the total number of nodes */
  public int countNodes() {
    return nodes.size();
  }

  /** @return the list of edges. Must not be used to add edge. Use {@link #addEdge(Edge)} instead */
  public List<Edge> getEdges() {
    return Collections.unmodifiableList(edges);
  }

  /**
   * @return the list of nodes. Must not be used to add nodes. Use {@link #addNode(Node)} instead
   */
  public List<Node> getNodes() {
    return Collections.unmodifiableList(nodes);
  }

  /**
   * @param index the position of the node in the list
   * @return a node at a given position in our nodes list
   */
  public Node getNode(int index) {
    return nodes.get(index);
  }

  /**
   * Add an edge in a graph.
   *
   * @param edge the edge added
   */
  public void addEdge(Edge edge) {
    edges.add(edge);

    Node[] nodesInEdge = edge.getNodes();
    for (int i = 0; i < 2; i++) {
      Node node = nodesInEdge[i];
      Node otherNode = nodesInEdge[(i + 1) % 2];
      List<Edge> edgesForNode = edgesByNodes.get(node);
      if (edgesForNode == null) {
        edgesForNode = new ArrayList<>();
        edgesByNodes.put(node, edgesForNode);
      }
      edgesForNode.add(edge);

      List<Node> neighborsOfNode = neighbors.get(node);
      if (neighborsOfNode == null) {
        neighborsOfNode = new ArrayList<>();
        neighbors.put(node, neighborsOfNode);
      }
      neighborsOfNode.add(node);
      neighborsOfNode.add(otherNode);
    }
  }

  /**
   * @param node the node for which we want the neighbors
   * @return The list of neighbors nodes of the given node
   */
  public List<Node> getNeighbors(Node node) {
    return neighbors.get(node);
  }

  /**
   * @param node the node for which we want the nearest neighbor
   * @return the nearest neighbor node of the given node
   */
  public Node getNearestNeighbors(Node node) {
    List<Edge> edgesOfNode = edgesByNodes.get(node);
    if (edgesOfNode.isEmpty()) {
      return null;
    }

    Edge minEdge = edgesOfNode.get(0);
    for (int i = 1; i < edgesOfNode.size(); i++) {
      Edge currEdge = edgesOfNode.get(i);
      if (currEdge.getEulidianDist2() < minEdge.getEulidianDist2()) {
        minEdge = currEdge;
      }
    }

    return minEdge.getFirstNode() == node ? minEdge.getSecondNode() : minEdge.getFirstNode();
  }

  /**
   * @param rooms the rooms we want to triangulate
   * @return a graph resulting of the Delaunay triangulation of the given rooms
   */
  public static Graph triangulate(List<DungeonRoom> rooms) {

    // Create the initial graph, all nodes are linked
    Graph graph = new Graph();

    final int roomsCount = rooms.size();
    for (int firstNodeId = 0; firstNodeId < roomsCount; firstNodeId++) {
      graph.addNode(new Node(rooms.get(firstNodeId)));
      Node room = graph.getNode(firstNodeId);
      for (int secondNodeId = firstNodeId - 1; secondNodeId >= 0; secondNodeId--) {
        Node neighborRoom = graph.getNode(secondNodeId);
        graph.addEdge(new Edge(room, neighborRoom));
      }
    }

    // Triangulate in another graph
    Graph triangulatedGraph = new Graph();
    triangulatedGraph.nodes = graph.nodes;

    for (Node node : graph.nodes) {
      Node nearestNeighbor = graph.getNearestNeighbors(node);
      Edge nearestNeighborEdge = new Edge(node, nearestNeighbor);

      Edge lastEdge = nearestNeighborEdge;
      triangulatedGraph.addEdge(lastEdge);

      Node nextRightNeighbor;
      do {
        nextRightNeighbor = graph.getNextNeighborOnSide(Side.RIGHT, lastEdge);
        if (nextRightNeighbor != null) {
          lastEdge = new Edge(node, nextRightNeighbor);
          triangulatedGraph.addEdge(lastEdge);
        }
      } while (nextRightNeighbor != nearestNeighbor && nextRightNeighbor != null);

      lastEdge = nearestNeighborEdge;
      if (nextRightNeighbor != null) {
        Node nextLeftNeighbor;
        do {
          nextLeftNeighbor = graph.getNextNeighborOnSide(Side.RIGHT, lastEdge);
          if (nextLeftNeighbor != null) {
            lastEdge = new Edge(node, nextLeftNeighbor);
            triangulatedGraph.addEdge(lastEdge);
          }
        } while (nextLeftNeighbor != nearestNeighbor && nextLeftNeighbor != null);
      }
    }

    // Remove duplicates
    triangulatedGraph.edges =
        triangulatedGraph.edges.stream().distinct().collect(Collectors.toList());

    return triangulatedGraph;
  }

  private enum Side {
    LEFT,
    RIGHT
  }

  /**
   * @param side
   * @param edge
   * @return the next neighbor node, in left or right direction
   */
  private Node getNextNeighborOnSide(Side side, Edge edge) {
    Position p1 = edge.getFirstNode().getRoom().getPosition();
    Position p2 = edge.getSecondNode().getRoom().getPosition();

    double cosMin = 2;
    Node neighborMin = null;

    for (Node neighbor : nodes) {
      if (neighbor != edge.getFirstNode() && neighbor != edge.getSecondNode()) {
        Position neighborPos = neighbor.getRoom().getPosition();
        Position v1 = new Position(p2.x - p1.x, p2.y - p1.y);
        Position v2 = new Position(neighborPos.x - p1.x, neighborPos.y - p1.y);
        int det = v1.x * v2.y - v1.y * v2.x;

        if (det < 0 && side == Side.RIGHT || det > 0 && side == Side.LEFT) {
          double prodscal =
              (double) (p1.x - neighborPos.x) * (double) (p2.x - neighborPos.x)
                  + (double) (p1.y - neighborPos.y) * (double) (p2.y - neighborPos.y);

          double longki2 =
              Math.pow((double) (p1.x - neighborPos.x), 2)
                  + Math.pow((double) (p1.y - neighborPos.y), 2);

          double longkj2 =
              Math.pow((double) (p2.x - neighborPos.x), 2)
                  + Math.pow((double) (p2.y - neighborPos.y), 2);

          double longki = Math.sqrt(longki2);
          double longkj = Math.sqrt(longkj2);

          double coskij = prodscal / (longki * longkj);
          if (coskij < cosMin) {
            cosMin = coskij;
            neighborMin = neighbor;
          }
        }
      }
    }
    return neighborMin;
  }

  /**
   * @param additionalEdge The percentage of random added edges, taken in all the remaining edges
   *     which does not belong to the MST. (float between 0 [0%] and 1 [100%])
   * @return another graph, which is the minimum spanning tree of this graph
   */
  public Graph getMinimumSpanningTree(float additionalEdge) {
    Graph mstGraph = new Graph();
    mstGraph.nodes = nodes;

    // Sort edges by distance
    Collections.sort(edges, (o1, o2) -> o1.getEulidianDist2().compareTo(o2.getEulidianDist2()));

    // Kruskal : Edges added in ascending cost order
    for (Edge edge : edges) {
      // Search if there's already a path between the two node of the edges in our mst
      // If there isn't, we won't create a cycle, so we can add it to our mst
      if (!mstGraph.pathExists(edge.getFirstNode(), edge.getSecondNode())) {
        mstGraph.addEdge(edge);
      }
    }

    if (additionalEdge != 0) {
      // Unusued edges
      LinkedList<Edge> remainingEdges = new LinkedList<>(edges);
      remainingEdges.removeAll(mstGraph.edges);

      // The quantity of edges to add
      int additionalEdgesCount = Math.round(remainingEdges.size() * additionalEdge);

      for (int i = 0; i < additionalEdgesCount; i++) {
        int additionalEdgeArrayId = ThreadLocalRandom.current().nextInt(remainingEdges.size());
        mstGraph.addEdge(remainingEdges.get(additionalEdgeArrayId));
        remainingEdges.remove(additionalEdgeArrayId);
      }
    }

    return mstGraph;
  }

  /** @return another graph, which is the minimum spanning tree of this graph */
  public Graph getMinimumSpanningTree() {
    return getMinimumSpanningTree(0);
  }

  /**
   * @param n1 the first node of the path
   * @param n2 the last node of the path
   * @return a boolean which is true if a path exist in the graph between the two nodes given
   */
  public boolean pathExists(Node n1, Node n2) {
    Stack<Node> nodesToVisit = new Stack<>();
    HashSet<Node> alreadyVisitedNodes = new HashSet<>();
    nodesToVisit.add(n1);

    while (!nodesToVisit.isEmpty()) {
      Node currNode = nodesToVisit.pop();
      alreadyVisitedNodes.add(currNode);

      List<Node> neighbors = getNeighbors(currNode);
      if (neighbors != null) {
        for (Node neighbor : neighbors) {
          if (neighbor.equals(n2)) {
            return true;
          }
          if (!alreadyVisitedNodes.contains(neighbor)) {
            nodesToVisit.add(neighbor);
          }
        }
      }
    }
    return false;
  }
}
