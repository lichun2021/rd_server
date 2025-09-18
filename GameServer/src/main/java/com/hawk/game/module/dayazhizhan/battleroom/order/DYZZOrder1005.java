package com.hawk.game.module.dayazhizhan.battleroom.order;

import org.apache.commons.lang.math.NumberUtils;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZBase;

/**
 * 购买后一定时间内我方地基获得一个N生命值的护盾
 */
public class DYZZOrder1005 extends DYZZOrder {

	public DYZZOrder1005(DYZZOrderCollection parent) {
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
		target.setOrder1005Val(num);
		target.setOrder1005End(end);

		return this;
	}
}
