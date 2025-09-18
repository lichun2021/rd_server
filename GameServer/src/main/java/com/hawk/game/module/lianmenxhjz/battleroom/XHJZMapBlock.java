package com.hawk.game.module.lianmenxhjz.battleroom;

import java.util.Set;
import java.util.TreeSet;

/**
 * 地图阻挡信息
 * @author julia
 */
public class XHJZMapBlock {
	/**
	 * 阻挡点id列表
	 */
	private Set<Integer> stops = new TreeSet<Integer>();

	private static XHJZMapBlock instance = new XHJZMapBlock();

	public static XHJZMapBlock getInstance() {
		return instance;
	}

	public boolean init() {
		return loadMapData();// && initMapRoundBlock();
	}

	/**
	 * 加载预置数据
	 * @return
	 */
	private boolean loadMapData() {
		return false;
	}

	/**
	 * 是否是阻挡点
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isStopPoint(int pointId) {
		return stops.contains(pointId);
	}

}
