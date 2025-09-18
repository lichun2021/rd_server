package com.hawk.game.nation.construction.comm;

import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.construction.comm.impl.PreBuildLevelCondition;

/**
 * 建筑升级前置条件
 * @author zhenyu.shang
 * @since 2022年4月12日
 */
public abstract class BuildCondtion {
	
	public final static int PRE_BUILD_LEVEL_COND = 1;
	
	private String cond;

	public BuildCondtion(String cond) {
		this.cond = cond;
	}

	public String getCond() {
		return cond;
	}

	public void setCond(String cond) {
		this.cond = cond;
	}

	/**
	 * 是否满足前置条件
	 * @param building
	 * @return
	 */
	public abstract boolean isMeetConditions(NationalBuilding building);
	
	
	public static BuildCondtion newBuildCondition(int idx, String cond) {
		BuildCondtion buildCond = null;
		switch (idx) {
		case PRE_BUILD_LEVEL_COND:
			buildCond = new PreBuildLevelCondition(cond);
			break;

		default:
			break;
		}
		return buildCond;
	}
}
