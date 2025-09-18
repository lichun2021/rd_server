package com.hawk.game.service.mssion.funtype;

import java.util.Set;

import com.hawk.game.player.PlayerData;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 解锁地块任务
 * 
 * @author lating
 *
 */
public class UnlockAreaMission implements IFunMission {

	@Override
	public long genMissionNum(PlayerData playerData, int funId) {
		int count = 0;
		Set<Integer> unlockedAreas = playerData.getPlayerBaseEntity().getUnlockedAreaSet();
		for (Integer areaId : unlockedAreas) {
			if (GameUtil.isCityOutsideAreaBlock(areaId)) {
				count++;
			}
		}
		
		return count;
	}

	@Override
	public MissionFunType getFunType() {
		return MissionFunType.FUN_UNLOCK_AREA;
	}

}
