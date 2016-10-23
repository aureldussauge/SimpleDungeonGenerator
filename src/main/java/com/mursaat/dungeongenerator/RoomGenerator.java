package com.mursaat.dungeongenerator;

import com.mursaat.dungeongenerator.util.MathUtils;

public class RoomGenerator {

	RoomParams params;

	public RoomGenerator() {
	}

	public RoomGenerator(RoomParams params) {
		this.params = params;
	}

	public DungeonRoom generateRoom(int radius) {
		int height = params.getRandomHeight();
		int width = params.getRandomWidth();

		Position position = MathUtils.getRandomPositionInCircle(radius);

		return new DungeonRoom().setWidth(width).setHeight(height).setX(position.x).setY(position.y);
	}
}
