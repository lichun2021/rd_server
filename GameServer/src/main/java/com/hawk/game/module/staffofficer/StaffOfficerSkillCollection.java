package com.hawk.game.module.staffofficer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.HeroCfg;
import com.hawk.game.config.HeroOfficeCfg;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.staffofficer.config.StaffOfficerSkillCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBStaffOfficeSync;
import com.hawk.game.util.GameUtil;

public class StaffOfficerSkillCollection {
	private Map<Integer, StaffOfficerSkill> skills = Collections.emptyMap();
	private int staffVal;
	private String playerId;
	
	public final static StaffOfficerSkillCollection defaultInst = new StaffOfficerSkillCollection();
		

	public static StaffOfficerSkillCollection create(PlayerData playerData) {
		StaffOfficerSkillCollection result = new StaffOfficerSkillCollection();
		result.playerId = playerData.getPlayerId();
		result.refresh(playerData);
		return result;
	}

	/**
	 * 
	 * @param effType
	 * @param great 我方参谋值大于对方
	 * @return
	 */
	public int getEffVal(EffType effType, boolean great) {
		int result = 0;
		for (StaffOfficerSkill skill : skills.values()) {
			if (skill.getType() == StaffOfficerType.SOType4) {
				continue;
			}
			if (great && skill.getType() == StaffOfficerType.SOType3) {
				result += skill.getEffVal(effType);
			}
			if (skill.getType() == StaffOfficerType.SOType2 || skill.getType() == StaffOfficerType.SOType1) {
				result += skill.getEffVal(effType);
			}
		}
		return result;
	}

	public int getEffValSOType4(EffType effType) {
		int result = 0;
		for (StaffOfficerSkill skill : skills.values()) {
			if (skill.getType() == StaffOfficerType.SOType4) {
				result += skill.getEffVal(effType);
			}
		}
		return result;
	}

	public List<StaffOfficerSkill> getSkillList() {
		return new ArrayList<>(skills.values());
	}

	public void refresh(PlayerData playerData) {
		try {
			int staffVal = staffVal(playerData);
			ConfigIterator<StaffOfficerSkillCfg> it = HawkConfigManager.getInstance().getConfigIterator(StaffOfficerSkillCfg.class);
			Map<Integer, StaffOfficerSkillCfg> map = new HashMap<>();

			for (StaffOfficerSkillCfg cfg : it) {
				StaffOfficerSkillCfg val = map.get(cfg.getSkillId());
				if (val == null && staffVal >= cfg.getStaffPoint()) {
					map.put(cfg.getSkillId(), cfg);
				}
				if (val != null && cfg.getSkillLevel() > val.getSkillLevel() && staffVal >= cfg.getStaffPoint()) {
					map.put(cfg.getSkillId(), cfg);
				}
			}

			Map<Integer, StaffOfficerSkill> skills = new HashMap<>();
			for (StaffOfficerSkillCfg cfg : map.values()) {
				StaffOfficerSkill skill = new StaffOfficerSkill();
				if (cfg.getStaffPoint() <= staffVal) {
					skill.setUnLock(true);
				}
				skill.setCfgId(cfg.getId());
				skills.put(cfg.getSkillId(), skill);
			}

			this.skills = ImmutableMap.copyOf(skills);
			this.staffVal = staffVal;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private int staffVal(PlayerData playerData) {
		if (GameUtil.isNpcPlayer(playerId)) {
			return 0;
		}
		int staffVal = 0;
		try {
			for (HeroEntity entity : playerData.getHeroEntityList()) {
				PlayerHero hero = entity.getHeroObj();
				staffVal += hero.staffVal();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return staffVal;
	}

	public int getStaffVal() {
		return staffVal;
	}

	@SuppressWarnings("unused")
	private void setStaffVal(int staffVal) {
		this.staffVal = staffVal;
	}

	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(playerId);
	}

	public PBStaffOfficeSync buildSyncPB() {
		PBStaffOfficeSync.Builder builder = PBStaffOfficeSync.newBuilder();
		builder.setStaffOfficePoint(staffVal);
		for (StaffOfficerSkill skill : skills.values()) {
			builder.addSkills(skill.toPBObj());
		}
		return builder.build();
	}

}
