package com.hawk.game.player.supersoldier.skill;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple5;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.config.SuperSoldierSkillCfg;
import com.hawk.game.config.SuperSoldierSkillLevelCfg;
import com.hawk.game.config.SuperSoldierStarLevelCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.player.skill.ISkill;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.SuperSoldierSkillSlot;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierEffect;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierSkillInfo;

/**
 * 
 * @author lwt
 * @date 2017年7月26日
 */
public abstract class ISuperSoldierSkill implements ISkill, SerializJsonStrAble {
	private long coolDown;
	private int exp;
	private SuperSoldierSkillSlot parent;

	public ISuperSoldierSkill() {
	}

	/**
	 * 配置ID
	 */
	public int skillID() {
		return getClass().getAnnotation(SuperSoldierSkill.class).skillID();
	}

	public abstract void setSkillID(int skillId);

	public int getLevel() {
		SuperSoldier spsoldier = getParent().getParent();
		SuperSoldierStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, spsoldier.getCfgId(), spsoldier.getStar(),
				spsoldier.getStep());
		HawkAssert.notNull(starLevelCfg, "Miss SuperSoldierStarLevelCfg level =" + spsoldier.getLevel() + " color = " + spsoldier.getConfig().getQualityColor());
		int maxLevel = starLevelCfg.getMaxSkillLevel();

		ConfigIterator<SuperSoldierSkillLevelCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierSkillLevelCfg.class);
		long level = configIterator.stream()
				.filter(cfg -> cfg.getSkillQuality() == getCfg().getSkillQuality())
				.filter(cfg -> cfg.getSkillExp() <= exp).count() + 1;
		return (int) Math.min(maxLevel, level);
	}

	public boolean isMaxLevel() {
		SuperSoldier spsoldier = getParent().getParent();
		SuperSoldierStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, spsoldier.getCfgId(), spsoldier.getStar(),
				spsoldier.getStep());
		HawkAssert.notNull(starLevelCfg, "Miss SuperSoldierStarLevelCfg level =" + spsoldier.getLevel() + " color = " + spsoldier.getConfig().getQualityColor());
		int maxLevel = starLevelCfg.getMaxSkillLevel();
		return getLevel() >= maxLevel;
	}

	/**
	 * 技能提供作用号值
	 * 
	 * @return
	 */
	public List<PBSuperSoldierEffect> effectVal() {
		SuperSoldierSkillCfg cfg = getCfg();
		HawkAssert.notNull(cfg, "Cfg error SuperSoldierSkillCfg is null skillID = " + skillID());
		List<PBSuperSoldierEffect> result = new ArrayList<>();
		SuperSoldier spsoldier = getParent().getParent();
		SuperSoldierStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, spsoldier.getCfgId(), spsoldier.getStar(),
				spsoldier.getStep());
		Map<Integer, Integer> effMap = new LinkedHashMap<>();
		// 属性值
		final int officeId = getParent().getParent().getOffice();
		for (HawkTuple5<Integer, Double, Double, Integer, Integer> eic : cfg.getBuff()) {
			boolean skillOfficeNonNull = eic.fourth > 0 || eic.fifth > 0;
			boolean officeNotOK = officeId != eic.fourth && eic.fifth != officeId;
			if (skillOfficeNonNull && officeNotOK) {
				continue;
			}

			int effVal = (int) Math.ceil((eic.second + eic.third * getLevel()));
			effMap.put(eic.first, effVal);
		}
		starLevelCfg.starEffectAddMap(skillID()).forEach((eff, val) -> effMap.merge(eff, val, (v1, v2) -> v1 + v2));

		for (Entry<Integer, Integer> ent : effMap.entrySet()) {
			PBSuperSoldierEffect ef = PBSuperSoldierEffect.newBuilder()
					.setEffectId(ent.getKey())
					.setValue(ent.getValue()).build();
			result.add(ef);
		}

		return result;
	}

	public int addExp(int xp) {
		this.exp = this.exp + xp;
		getParent().getParent().notifyChange();
		return this.exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	/**
	 * 技能cd
	 * 
	 * @return
	 */
	public final long cd() {
		return 0;
	}

	/**
	 * 序列化保存
	 */
	@Override
	public final String serializ() {
		// 考虑到未来维护可能需要兼容. 这里扔使用array
		JSONArray array = new JSONArray();
		array.add(exp);
		return array.toJSONString();
	}

	/**
	 * 反序列化
	 * 
	 * @param serialiedStr
	 */
	@Override
	public final void mergeFrom(String serialiedStr) {
		JSONArray array = JSONArray.parseArray(serialiedStr);
		exp = array.getIntValue(0);
	}

	/**
	 * 返回pb对象
	 * 
	 * @return
	 */
	public PBSuperSoldierSkillInfo toPBobj() {
		PBSuperSoldierSkillInfo.Builder bul = PBSuperSoldierSkillInfo.newBuilder();
		bul.setSuperSoldierId(parent.getParent().getCfgId())
				.setSkillId(skillID())
				.setTotalExp(exp)
				.setCoolDown(coolDown)
				.setLevel(getLevel())
				.addAllEffect(effectVal());
		return bul.build();
	}

	/**
	 * 技能是否在冷却中
	 * 
	 * @return
	 */
	public boolean isCooling() {
		return HawkTime.getMillisecond() > coolDown;
	}

	/**
	 * 释放技能
	 */
	public final void cast() {
		if (beforeCast()) {
			casting();
			afterCast();
		}
	}

	protected boolean beforeCast() {
		return true;
	}

	protected abstract void casting();

	protected void afterCast() {
		long coolDown = HawkTime.getMillisecond() + cd();
		setCoolDown(coolDown);
	}

	public SuperSoldierSkillCfg getCfg() {
		SuperSoldierSkillCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierSkillCfg.class, skillID());
		HawkAssert.notNull(cfg, "SuperSoldierSkillCfg is null for id : " + skillID());
		return cfg;
	}

	/** 冷却时间 */
	public long getCoolDown() {
		return coolDown;
	}

	public void setCoolDown(long coolDown) {
		this.coolDown = coolDown;
	}

	public SuperSoldierSkillSlot getParent() {
		return parent;
	}

	public void setParent(SuperSoldierSkillSlot parent) {
		this.parent = parent;
	}

	public int getExp() {
		return exp;
	}

}
