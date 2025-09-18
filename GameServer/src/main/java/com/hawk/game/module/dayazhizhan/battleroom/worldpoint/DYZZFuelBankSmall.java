package com.hawk.game.module.dayazhizhan.battleroom.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZFuelBankSmallCfg;

public class DYZZFuelBankSmall extends IDYZZFuelBank {

	public DYZZFuelBankSmall(DYZZBattleRoom parent) {
		super(parent);
	}

	public static DYZZFuelBankSmallCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(DYZZFuelBankSmallCfg.class);
	}

	public static DYZZFuelBankSmall create(DYZZBattleRoom parent) {
		DYZZFuelBankSmallCfg buildcfg = getCfg();
		DYZZFuelBankSmall icd = new DYZZFuelBankSmall(parent);
		icd.setTotalResNum(buildcfg.getTotalRes());
		icd.setRemainResNum(buildcfg.getTotalRes());
		icd.setMinRes(buildcfg.getMinRes());
		icd.setResourceId(buildcfg.getResourceId());
		icd.setRedis(buildcfg.getRedis());
		icd.setCollectSpeed(buildcfg.getCollectSpeedList());
		icd.setLevel(1);
		return icd;
	}

	@Override
	public void removeWorldPoint() {
		getParent().getFubankSmallrefresh().onFubankRemove(this);
		super.removeWorldPoint();
	}
}
