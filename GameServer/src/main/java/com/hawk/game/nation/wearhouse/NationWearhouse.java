package com.hawk.game.nation.wearhouse;

import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.protocol.National.NationbuildingType;

/**
 * 国家仓库
 * @author zhenyu.shang
 * @since 2022年4月12日
 */
public class NationWearhouse extends NationalBuilding {

	public NationWearhouse(NationConstructionEntity entity, NationbuildingType buildType) {
		super(entity, buildType);
	}

	@Override
	public boolean init() {
		return false;
	}

	@Override
	public void levelupOver() {	
		// 当建设处初次建设完成时，将国家整体状态修改为完成状态
		if(this.entity.getLevel() == 1){
			// 同步国家整体状态
			NationService.getInstance().boardcastNationalStatus();
		}
	}

	@Override
	public void levelupStart() {
		
	}

	@Override
	public boolean checkStateCanBuild() {
		return true;
	}

	/**
	 * 获取资源保护比率
	 * @return
	 */
	public int getSafeResource() {
		return getCurrentLevelCfg().getSafeResource();
	}
}
