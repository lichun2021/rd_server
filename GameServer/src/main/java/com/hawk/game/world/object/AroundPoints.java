package com.hawk.game.world.object;

import java.util.ArrayList;
import java.util.List;

/**
 * 周围占用点封装
 * @author golden
 *
 */
public class AroundPoints extends ThreadLocal<AroundPoints> {

	private static final AroundPoints instance = new AroundPoints();
	
	/**
	 * 周围占用点
	 */
	private final List<Point> aroundPoints = new ArrayList<Point>();
	
	@Override
	public AroundPoints initialValue() {
		AroundPoints instance = new AroundPoints();
		return instance;
	}
	
	public static List<Point> getAroundPoints() {
		AroundPoints ap = instance.get();
		ap.aroundPoints.clear();
		return ap.aroundPoints;
	}
}
