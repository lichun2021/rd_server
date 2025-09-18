package com.hawk.game.nation.releation;

import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.protocol.National.NationbuildingType;

/**
 * 国家外交中心
 * @author zhenyu.shang
 * @since 2022年4月12日
 */
public class NationReleationCenter extends NationalBuilding {

	public NationReleationCenter(NationConstructionEntity entity, NationbuildingType buildType) {
		super(entity, buildType);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void levelupOver() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void levelupStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkStateCanBuild() {
		return true;
	}

}
