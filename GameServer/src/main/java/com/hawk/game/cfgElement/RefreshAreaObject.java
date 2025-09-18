package com.hawk.game.cfgElement;

/**
 * 世界地图刷新资源使用第三类封装对象
 * @author julia
 *
 */
public class RefreshAreaObject {
	int key;	// 等级
	int weight;	// 权重

	public RefreshAreaObject(int key, int weight) {
		this.key = key;
		this.weight = weight;
	}

	public int getKey() {
		return key;
	}

	public int getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return key + "_" + weight;
	}
}