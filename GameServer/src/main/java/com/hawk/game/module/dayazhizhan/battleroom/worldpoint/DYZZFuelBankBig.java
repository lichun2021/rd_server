package com.hawk.game.module.dayazhizhan.battleroom.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZFuelBankBigCfg;

public class DYZZFuelBankBig extends IDYZZFuelBank {

	public DYZZFuelBankBig(DYZZBattleRoom parent) {
		super(parent);
	}

	public static DYZZFuelBankBigCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(DYZZFuelBankBigCfg.class);
	}

	public static DYZZFuelBankBig create(DYZZBattleRoom parent) {
		DYZZFuelBankBigCfg buildcfg = getCfg();
		DYZZFuelBankBig icd = new DYZZFuelBankBig(parent);
		icd.setTotalResNum(buildcfg.getTotalRes());
		icd.setRemainResNum(buildcfg.getTotalRes());
		icd.setMinRes(buildcfg.getMinRes());
		icd.setResourceId(buildcfg.getResourceId());
		icd.setRedis(buildcfg.getRedis());
		icd.setCollectSpeed(buildcfg.getCollectSpeedList());
		icd.setLevel(2);
		return icd;
	}
	@Override
	public void removeWorldPoint() {
		getParent().getFubankBigrefresh().onFubankRemove(this);
		super.removeWorldPoint();
	}
}
