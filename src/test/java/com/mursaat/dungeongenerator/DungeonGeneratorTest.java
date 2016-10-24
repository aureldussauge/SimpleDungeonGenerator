package com.mursaat.dungeongenerator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DungeonGeneratorTest {
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void generateDungeon() throws Exception {
		DungeonParams dungeonParams = new DungeonParams();
		dungeonParams.setHallwaysWidth(3);
		dungeonParams.setMinSpaceBetweenRooms(1);
		dungeonParams.setMinRoomCount(20);
		dungeonParams.setMaxRoomCount(30);

		RoomParams roomParams = new RoomParams();
		roomParams.setMinWidth(10);
		roomParams.setMaxWidth(20);

		DungeonGenerator myGenerator = new DungeonGenerator(dungeonParams, roomParams);
		Dungeon dungeon = new DungeonGenerator().generateDungeon();
		System.out.println(dungeon);
	}
}
