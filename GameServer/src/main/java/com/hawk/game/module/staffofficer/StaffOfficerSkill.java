package com.hawk.game.module.staffofficer;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.staffofficer.config.StaffOfficerSkillCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBStaffOfficeSkill;

public class StaffOfficerSkill {
	private int cfgId;
	private boolean unLock;
	private StaffOfficerSkillCfg cfg;

	public boolean isUnLock() {
		return unLock;
	}

	public StaffOfficerSkillCfg getCfg() {
		if (cfg != null) {
			return cfg;
		}
		cfg = HawkConfigManager.getInstance().getConfigByKey(StaffOfficerSkillCfg.class, cfgId);
		return cfg;
	}

	public void setUnLock(boolean unLock) {
		this.unLock = unLock;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public StaffOfficerType getType() {
		return getCfg().getType();
	}

	public int getEffVal(EffType effType) {
		return getCfg().getEffectVal(effType);
	}

	public PBStaffOfficeSkill toPBObj() {
		PBStaffOfficeSkill.Builder builder = PBStaffOfficeSkill.newBuilder();
		builder.setSkillId(cfgId);
		builder.setUnlock(unLock);
		return builder.build();
	}

}
