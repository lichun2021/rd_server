package com.hawk.game.module.dayazhizhan.battleroom.order;

import org.apache.commons.lang.math.NumberUtils;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZBase;

/**
 * 1007	商品buff	p1	购买后一定时间内，我方主基地每次受到伤害，此伤害值-1																																		
 */
public class DYZZOrder1007 extends DYZZOrder {

	public DYZZOrder1007(DYZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public DYZZOrder startOrder() {
		super.startOrder();

		int num = NumberUtils.toInt(getConfig().getP1());
		long end = getEffectEndTime();

		DYZZBase target = getParent().getParent().getDYZZBuildingByClass(DYZZBase.class).stream()
				.filter(base -> base.getBornCamp() == getParent().getCamp()).findAny()
				.get();
		target.setOrder1007Val(num);
		target.setOrder1007End(end);

		return this;
	}
}
