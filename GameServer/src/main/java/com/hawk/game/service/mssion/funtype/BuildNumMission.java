package com.hawk.game.service.mssion.funtype;

import java.util.List;
import java.util.stream.Collectors;

import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 建筑数量任务
 * 
 * @author lating
 *
 */
public class BuildNumMission implements IFunMission {

	@Override
	public long genMissionNum(PlayerData playerData, int funId) {
		List<BuildingBaseEntity> list = playerData.getBuildingListByType(BuildingType.valueOf(funId/100))
				.stream().filter(e -> e.getBuildingCfgId() >= funId).collect(Collectors.toList());
		return list.size();
	}

	@Override
	public MissionFunType getFunType() {
		return MissionFunType.FUN_BUILD_NUMBER;
	}

}
