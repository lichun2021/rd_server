package com.hawk.game.module.dayazhizhan.battleroom.order;

import java.util.List;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.player.rogue.DYZZRogueType;

/**
 * 获得rogue机会
 * @author lwt
 * @date 2022年4月8日
 */
public class DYZZOrder1004 extends DYZZOrder {

	public DYZZOrder1004(DYZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public DYZZOrder startOrder() {
		super.startOrder();
		DYZZCAMP camp = getParent().getCamp();
		List<IDYZZPlayer> campPlayers = getParent().getParent().getCampPlayers(camp);
		int param = getBuyCnt();
		campPlayers.forEach(p -> p.getRogueCollec().rogueOnce(DYZZRogueType.BUY, param));
		return this;
	}
}
