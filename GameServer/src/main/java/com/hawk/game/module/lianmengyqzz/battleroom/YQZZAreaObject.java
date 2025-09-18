package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;

public class YQZZAreaObject extends AreaObject {

	public YQZZAreaObject(int areaId, int beginX, int beginY, int endX, int endY) {
		super(areaId, beginX, beginY, endX, endY);
	}

	@Override
	public boolean initAreaPoints() {
		throw new UnsupportedOperationException();
	}

	public boolean initAreaPoints(YQZZBattleRoom battleroom) {
		for (int i = beginX; i <= endX; i++) {
			for (int j = beginY; j <= endY; j++) {
				if (YQZZMapBlock.getInstance().isStopPoint(GameUtil.combineXAndY(i, j))) {
					continue;
				}
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
