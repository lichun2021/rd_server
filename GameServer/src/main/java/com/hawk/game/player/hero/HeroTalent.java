package com.hawk.game.player.hero;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.tuple.HawkTuple5;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.config.HeroTalentCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.protocol.Hero.PBTalentInfo;

/**
 * @author lwt
 * @date 2017年7月26日
 */
public class HeroTalent implements SerializJsonStrAble {
	private int exp;
	private int skillID;
	private TalentSlot parent;

	public HeroTalent(int skillID) {
		this.skillID = skillID;
	}

	/** 配置ID */
	public int skillID() {
		return skillID;
	}

	/**
	 * 技能提供作用号值
	 * 
	 * @return
	 */
	public List<PBHeroEffect> effectVal() {
		HeroTalentCfg cfg = getCfg();
		HawkAssert.notNull(cfg, "Cfg error HeroSkillCfg is null skillID = " + skillID());
		List<PBHeroEffect> result = new ArrayList<>();
		// 属性值
		final int attrVal = Math.min(exp, cfg.getMaxPoint());
		final int officeId = getParent().getParent().getOffice();
		PBHeroEffect eff1440 = null;
		PBHeroEffect eff1441 = null;
		for (HawkTuple5<Integer, Double, Double, Integer, Integer> eic : cfg.getBuff()) {
			boolean skillOfficeNonNull = eic.fourth > 0 || eic.fifth > 0;
			boolean officeNotOK = officeId != eic.fourth && eic.fifth != officeId;
			if (skillOfficeNonNull && officeNotOK) {
				continue;
			}
			int effVal = (int) Math.ceil(eic.second + eic.third * attrVal);
			PBHeroEffect ef = PBHeroEffect.newBuilder()
					.setEffectId(eic.first)
					.setValue(effVal).build();
			if (eic.first == EffType.HERO_XLSL_1440.getNumber()) {
				eff1440 = ef;
				continue;
			}
			if (eic.first == EffType.HERO_XLSD_1441.getNumber()) {
				eff1441 = ef;
				continue;
			}
			result.add(ef);
		}

		if (result.isEmpty()) {
			if (eff1440 != null) {
				result.add(eff1440);
			} else if (eff1441 != null) {
				result.add(eff1441);
			}
		}

		return result;
	}

	public int addExp(int xp) {
		this.exp = this.exp + xp;
		return this.exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	/** 序列化保存 */
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
	public PBTalentInfo toPBobj() {
		PBTalentInfo.Builder bul = PBTalentInfo.newBuilder();
		bul.setHeroId(parent.getParent().getCfgId())
				.setSkillId(skillID())
				.setExp(exp)
				.addAllEffect(effectVal());
		return bul.build();
	}

	protected boolean beforeCast() {
		return true;
	}

	public HeroTalentCfg getCfg() {
		HeroTalentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroTalentCfg.class, skillID());
		HawkAssert.notNull(cfg, "HeroSkillCfg is null for id : " + skillID());
		return cfg;
	}

	public TalentSlot getParent() {
		return parent;
	}

	public void setParent(TalentSlot parent) {
		this.parent = parent;
	}

	public int getExp() {
		return exp;
	}

	public int getSkillID() {
		return skillID;
	}

	public void setSkillID(int skillID) {
		this.skillID = skillID;
	}

	/**
	 * 等级是否达到最大
	 * @return
	 */
	public boolean isExpMax() {
		return exp >= getCfg().getMaxPoint();
	}
}
