package com.hawk.game.nation.hospital;

import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.protocol.National.NationbuildingType;

/**
 * 国家医院
 * @author zhenyu.shang
 * @since 2022年4月12日
 */
public class NationalHospital extends NationalBuilding {

	public NationalHospital(NationConstructionEntity entity, NationbuildingType buildType) {
		super(entity, buildType);
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
