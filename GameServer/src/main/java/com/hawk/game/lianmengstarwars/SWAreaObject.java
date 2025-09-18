package com.hawk.game.lianmengstarwars;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;

public class SWAreaObject extends AreaObject {

	public SWAreaObject(int areaId, int beginX, int beginY, int endX, int endY) {
		super(areaId, beginX, beginY, endX, endY);
	}

	@Override
	public boolean initAreaPoints() {
		throw new UnsupportedOperationException();
	}

	public boolean initAreaPoints(SWBattleRoom battleroom) {
		for (int i = beginX; i <= endX; i++) {
			for (int j = beginY; j <= endY; j++) {
				Point point = new Point(i, j, this.id, 0);
				allPoints.put(point.getId(), point);
				freePoints.put(point.getId(), point);
			}
		}
		return true;
	}

	@Override
	public List<Point> getValidPoints(int worldPointType, MonsterType monsterType) {
		throw new UnsupportedOperationException();
	}

	public List<Point> getValidPoints(int redis) {
		Predicate<? super Point> predicate = p -> (p.getX() + p.getY()) % 2 != redis % 2;
		return allPoints.values().stream().filter(predicate).collect(Collectors.toList());
	}
}
