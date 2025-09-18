package com.hawk.game.nation.construction.comm.impl;

import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.construction.comm.BuildCondtion;
import com.hawk.game.protocol.National.NationbuildingType;

/**
 * 前置建筑等级条件
 * @author zhenyu.shang
 * @since 2022年5月27日
 */
public class PreBuildLevelCondition extends BuildCondtion {

	public PreBuildLevelCondition(String cond) {
		super(cond);
	}

	@Override
	public boolean isMeetConditions(NationalBuilding building) {
		// 判断前置建筑是否存在，并且等级满足要求
		Integer buildId = Integer.parseInt(this.getCond());
		int buildType = buildId / 100;
		int level = buildId % 100;
		
		// 判断前置
		NationalBuilding prev = NationService.getInstance().getNationBuildingByType(NationbuildingType.valueOf(buildType));
		if(prev == null || prev.getEntity().getLevel() < level){
			return false;
		}
		return true;
	}

}
