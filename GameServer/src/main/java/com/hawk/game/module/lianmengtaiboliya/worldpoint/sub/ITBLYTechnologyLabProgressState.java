package com.hawk.game.module.lianmengtaiboliya.worldpoint.sub;

import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYTechnologyLab;

public abstract class ITBLYTechnologyLabProgressState {
	private final TBLYTechnologyLab parent;

	public ITBLYTechnologyLabProgressState(TBLYTechnologyLab parent) {
		this.parent = parent;
	}

	public abstract void onTick();

	public TBLYTechnologyLab getParent() {
		return parent;
	}

}
