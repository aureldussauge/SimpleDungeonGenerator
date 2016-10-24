package com.mursaat.dungeongenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * A dungeon containing rooms and hallways
 */
public class Dungeon {

	/**
	 * The rooms
	 */
	List<DungeonRoom> rooms;

	/**
	 * The hallways
	 */
	List<DungeonHallway> hallways;

	/**
	 * The rooms and hallways in an array
	 * Each cell refers to a room, an hallway or null=wall
	 */
	DungeonStructure[][] tiles;

	/**
	 * The width of the dungeon
	 */
	int width;

	/**
	 * The height of the dungeon
	 */
	int height;

	Dungeon() {
		this.rooms = new ArrayList<>();
		this.hallways = new ArrayList<>();
	}

	void addRoom(DungeonRoom room) {
		rooms.add(room);
	}

	public List<DungeonRoom> getRooms() {
		return rooms;
	}

	public List<DungeonHallway> getHallways() {
		return hallways;
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

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (tiles[y][x] instanceof DungeonRoom)
					stringBuilder.append('X');
				else if (tiles[y][x] instanceof DungeonHallway)
					stringBuilder.append('O');
				else
					stringBuilder.append(' ');
			}
			stringBuilder.append('\n');
		}
		return stringBuilder.toString();
	}
}
