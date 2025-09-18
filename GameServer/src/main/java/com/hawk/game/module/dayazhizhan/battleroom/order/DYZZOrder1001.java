package com.hawk.game.module.dayazhizhan.battleroom.order;

import java.util.List;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZShotType;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZBase;

/**
 * 购买后我方主基地对对方主基地发动一攻击，伤害为N
 * @author lwt
 * @date 2022年4月8日
 */
public class DYZZOrder1001 extends DYZZOrder {

	public DYZZOrder1001(DYZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public DYZZOrder startOrder() {
		super.startOrder();
		DYZZCAMP camp = getParent().getCamp();
		List<DYZZBase> baseList = getParent().getParent().getDYZZBuildingByClass(DYZZBase.class);
		for (DYZZBase base : baseList) {
			if (base.getBornCamp() == camp) {
				base.onNuclearShoot(base.getOrderAtkVal(), DYZZShotType.Order);
			}
		}
		return this;
	}
}
