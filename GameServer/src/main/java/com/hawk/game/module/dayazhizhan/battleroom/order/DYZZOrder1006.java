package com.hawk.game.module.dayazhizhan.battleroom.order;

import org.apache.commons.lang.math.NumberUtils;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZBase;

/**
 * 1006	商品buff	p1	购买后一定时间内，对方主基地每次受到伤害，此伤害值＋1																	
 */
public class DYZZOrder1006 extends DYZZOrder {

	public DYZZOrder1006(DYZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public DYZZOrder startOrder() {
		super.startOrder();

		int num = NumberUtils.toInt(getConfig().getP1());
		long end = getEffectEndTime();

		DYZZBase target = getParent().getParent().getDYZZBuildingByClass(DYZZBase.class).stream()
				.filter(base -> base.getBornCamp() != getParent().getCamp()).findAny()
				.get();
		target.setOrder1006Val(num);
		target.setOrder1006End(end);

		return this;
	}
}
