package com.hawk.game.module.dayazhizhan.battleroom.order;

import org.apache.commons.lang.math.NumberUtils;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZFuelBankSmall;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.DYZZInTower;
import com.hawk.game.util.GameUtil;

/**
 * 1008		p1	在我方区域随机刷新N个陨晶矿（小）																																																			
 */
public class DYZZOrder1008 extends DYZZOrder {

	public DYZZOrder1008(DYZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public DYZZOrder startOrder() {
		super.startOrder();

		int num = NumberUtils.toInt(getConfig().getP1());

		DYZZInTower target = getParent().getParent().getDYZZBuildingByClass(DYZZInTower.class).stream()
				.filter(base -> base.getBornCamp() == getParent().getCamp()).findAny()
				.get();
		int[] pos = GameUtil.splitXAndY(target.getPointId());

		for (int i = 0; i < num; i++) {
			DYZZFuelBankSmall icd = DYZZFuelBankSmall.create(getParent().getParent());
			getParent().getParent().randomFreePoint(pos, icd.getPointType(), icd.getRedis());
			getParent().getParent().addViewPoint(icd);
		}
		return this;
	}
}
