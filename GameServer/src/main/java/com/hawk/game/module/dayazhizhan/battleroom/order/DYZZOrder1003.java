package com.hawk.game.module.dayazhizhan.battleroom.order;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 购买后8兵种各获得N士兵
 * @author lwt
 * @date 2022年4月8日
 */
public class DYZZOrder1003 extends DYZZOrder {

	public DYZZOrder1003(DYZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public DYZZOrder startOrder() {
		super.startOrder();
		DYZZCAMP camp = getParent().getCamp();
		List<IDYZZPlayer> campPlayers = getParent().getParent().getCampPlayers(camp);
		List<Integer> armyIdList = Arrays.asList(100107, 100207, 100307, 100407, 100507, 100607, 100707, 100807);
		int free = NumberUtils.toInt(getConfig().getP1());
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (IDYZZPlayer player : campPlayers) {
			List<ArmyEntity> armyList = player.getData().getArmyEntities();
			for (ArmyEntity armyEntity : armyList) {
				if (armyIdList.contains(armyEntity.getArmyId())) {
					armyEntity.addFree(free);
					map.put(armyEntity.getArmyId(), free);
				}
			}

//			player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_COLLECT, armyIdList.toArray(new Integer[0]));
			// 同步兵种数量变化信息
	     	player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_COLLECT, map);
	     	player.refreshPowerElectric(PowerChangeReason.AWARD_SOLDIER);

		}
		return this;
	}
}
