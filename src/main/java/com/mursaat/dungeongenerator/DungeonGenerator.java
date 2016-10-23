package com.mursaat.dungeongenerator;

import com.mursaat.dungeongenerator.graphs.*;
import com.mursaat.pathfinding.AStar;
import com.mursaat.pathfinding.AStarParams;
import com.mursaat.pathfinding.PathFinderMap;
import com.mursaat.pathfinding.PathNodePosition;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
	 * Select and return the N biggest rooms
	 *
	 * @param rooms
	 * @param n     number of rooms selected
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
		Dungeon dungeon = new Dungeon();

		List<Node> nodes = mstGraph.getNodes();
		List<Edge> edges = mstGraph.getEdges();

		// Find width and height
		dungeon.width = Integer.MIN_VALUE;
		dungeon.height = Integer.MIN_VALUE;

		for (Node node : nodes) {
			DungeonRoom room = node.getRoom();
			if (room.getX() + room.getWidth() > dungeon.width) dungeon.width = room.getX() + room.getWidth();
			if (room.getY() + room.getHeight() > dungeon.height) dungeon.height = room.getY() + room.getHeight();
		}
		for (DungeonRoom room : hallwayRooms) {
			if (room.getX() + room.getWidth() > dungeon.width) dungeon.width = room.getX() + room.getWidth();
			if (room.getY() + room.getHeight() > dungeon.height) dungeon.height = room.getY() + room.getHeight();
		}

		for (Node node : nodes) {
			DungeonRoom room = node.getRoom();
			dungeon.addRoom(room);
		}

		// Create the tiled map
		dungeon.tiles = new DungeonStructure[dungeon.height][dungeon.width];

		for (DungeonRoom room : rooms) {
			Position roomPosition = room.getPosition();

			int yRoomMax = roomPosition.y + room.getHeight();
			int xRoomMax = roomPosition.x + room.getWidth();
			for (int y = roomPosition.y; y < yRoomMax; y++) {
				for (int x = roomPosition.x; x < xRoomMax; x++) {
					dungeon.tiles[y][x] = room;
				}
			}
		}

		for (Edge edge : edges) {
			DungeonHallway hallway = createHallwayFromEdge(dungeon, edge);
			dungeon.hallways.add(hallway);

			if (!hallway.getPath().isEmpty()) {
				Position lastPosition = hallway.getPath().get(0);
				for (int posId = 1; posId < hallway.getPath().size(); posId++) {
					Position currPosition = hallway.getPath().get(posId);
					if (lastPosition.x != currPosition.x) {
						int lowestX = Math.min(currPosition.x, lastPosition.x);
						int greatestX = Math.max(currPosition.x, lastPosition.x);
						for (int x = lowestX; x <= greatestX; x++) {
							int lowestY = Math.max(currPosition.y - (dungeonParams.getHallwaysWidth() - 1) / 2, 0);
							int greatestY = Math.min(currPosition.y + (dungeonParams.getHallwaysWidth() - 1) / 2, dungeon.height - 1);
							for (int y = lowestY; y <= greatestY; y++) {
								dungeon.tiles[y][x] = dungeon.tiles[y][x] == null ? hallway : dungeon.tiles[y][x];
							}
						}
					}
					if (lastPosition.y != currPosition.y) {
						int lowestY = Math.min(currPosition.y, lastPosition.y);
						int greatestY = Math.max(currPosition.y, lastPosition.y);
						for (int y = lowestY; y <= greatestY; y++) {
							int lowestX = Math.max(currPosition.x - (dungeonParams.getHallwaysWidth() - 1) / 2, 0);
							int greatestX = Math.min(currPosition.x + (dungeonParams.getHallwaysWidth() - 1) / 2, dungeon.width - 1);
							for (int x = lowestX; x <= greatestX; x++) {
								dungeon.tiles[y][x] = dungeon.tiles[y][x] == null ? hallway : dungeon.tiles[y][x];
							}
						}
					}
					lastPosition = currPosition;
				}
			}
		}

		// Add HallwayRooms
		// 1 - Create another map just with these rooms
		DungeonRoom[][] hallwayRoomsTiles = new DungeonRoom[dungeon.height][dungeon.width];
		for (DungeonRoom room : hallwayRooms) {
			for (int y = room.getY(); y < room.getY() + room.getHeight(); y++) {
				for (int x = room.getX(); x < room.getX() + room.getWidth(); x++) {
					hallwayRoomsTiles[y][x] = room;
				}
			}
		}

		// 2 - Add hallwayRooms colliding with
		HashSet<DungeonRoom> usedHallwayRooms = new HashSet<>();
		for (int y = 0; y < dungeon.height; y++) {
			for (int x = 0; x < dungeon.width; x++) {
				if (hallwayRoomsTiles[y][x] != null && dungeon.tiles[y][x] instanceof DungeonHallway) {
					DungeonRoom roomToAdd = hallwayRoomsTiles[y][x];
					if (!usedHallwayRooms.contains(roomToAdd)) {
						for (int roomY = roomToAdd.getY();
							 roomY < roomToAdd.getY() + roomToAdd.getHeight();
							 roomY++) {
							for (int roomX = roomToAdd.getX();
								 roomX < roomToAdd.getX() + roomToAdd.getWidth();
								 roomX++) {
								dungeon.tiles[roomY][roomX] = roomToAdd;
							}
						}
						dungeon.addRoom(roomToAdd);
						usedHallwayRooms.add(roomToAdd);
					}
				}
			}
		}
		return dungeon;
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

	private DungeonHallway createHallwayFromEdge(Dungeon dungeon, Edge edge) {
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
				collide |= dungeon.tiles[doorY][x] != null;
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
				collide |= dungeon.tiles[y][doorX] != null;
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
						collide |= (dungeon.tiles[firstYPos][x] != null);
					}

					int lowerY = Math.min(firstYPos, secondYPos);
					int greaterY = Math.max(firstYPos, secondYPos);
					for (int y = lowerY; y <= greaterY; y++) {
						collide |= (dungeon.tiles[y][secondXPos] != null);
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
						collide |= (dungeon.tiles[y][firstXPos] != null);
					}

					int lowerX = Math.min(firstXPos, secondXPos);
					int greaterX = Math.max(firstXPos, secondXPos);
					for (int x = lowerX; x <= greaterX; x++) {
						collide |= (dungeon.tiles[secondYPos][x] != null);
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
}
