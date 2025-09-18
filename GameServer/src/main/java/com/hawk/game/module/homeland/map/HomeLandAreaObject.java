package com.hawk.game.module.homeland.map;

import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HomeLandAreaObject extends AreaObject {

    private int theme;

    public HomeLandAreaObject(int theme, int areaId, int beginX, int beginY, int endX, int endY) {
        super(areaId, beginX, beginY, endX, endY);
        this.theme = theme;
    }

    @Override
    public boolean initAreaPoints() {
        for (int i = beginX; i <= endX; i++) {
            for (int j = beginY; j <= endY; j++) {
                HomeLandMapBlock mapBlock = HomeLandMapService.getInstance().getBlock(theme);
                if (mapBlock.isStopPoint(GameUtil.combineXAndY(i, j))) {
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

    public List<Point> getValidPoints(int radius) {
        Predicate<? super Point> predicate = p -> (p.getX() + p.getY()) % 2 != radius % 2;
        return allPoints.values().stream().filter(predicate).collect(Collectors.toList());
    }
}
