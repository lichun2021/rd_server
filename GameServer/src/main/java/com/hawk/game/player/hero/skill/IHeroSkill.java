package com.hawk.game.player.hero.skill;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkException;
import org.hawk.tuple.HawkTuple5;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.HeroSkillCfg;
import com.hawk.game.config.HeroSkillLevelCfg;
import com.hawk.game.config.HeroStarLevelCfg;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.player.hero.SkillSlot;
import com.hawk.game.player.skill.ISkill;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.protocol.Hero.PBSkillInfo;
import com.hawk.game.util.EffectParams;
import com.hawk.game.world.march.IWorldMarch;

/** @author lwt
 * @date 2017年7月26日 */
public abstract class IHeroSkill implements ISkill, SerializJsonStrAble {
	private int exp;
	private SkillSlot parent;

	public IHeroSkill() {
	}

	/** 配置ID */
	public abstract int skillID();
	
	/**
	 * 出征
	 */
	public void goMarch(IWorldMarch march){
	}

	public void tick(){}
	
	/**
	 * 单场战斗结束
	 */
	public void afterBattle(IBattleIncome income, BattleOutcome battleOutcome){
	}

	/**
	 * 出征归来
	 */
	public void backFromMarch(IWorldMarch march){
	}
	
	public boolean isProficiencySkill(){
		return false;
	}

	/**SSS 战记触发 行军显示*/
	public int getShowProficiencyEffect(){
		return 0;
	}
	
	/**SSS 战记触发*/
	public int getProficiencyEffect(EffType effType, EffectParams effParams){
		return 0;
	}
	
	public int getLevel() {
		int maxLevel = maxLevel();

		ConfigIterator<HeroSkillLevelCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(HeroSkillLevelCfg.class);
		long level = configIterator.stream()
				.filter(cfg -> cfg.getSkillQuality() == getCfg().getSkillQuality())
				.filter(cfg -> cfg.getSkillExp() <= exp).count() + 1;
		return (int) Math.min(maxLevel, level);
	}

	public int maxLevel() {
		PlayerHero hero = getParent().getParent();
		HeroStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(HeroStarLevelCfg.class, hero.getCfgId(), hero.getStar(), hero.getStep());
		HawkAssert.notNull(starLevelCfg, "Miss HeroStarLevelCfg level =" + hero.getLevel() + " color = " + hero.getConfig().getQualityColor());
		int maxLevel = starLevelCfg.getMaxSkillLevel();

		switch (getCfg().getSkillType()) {
		case 1:
			maxLevel += getParent().getParent().getParent().getEffect().getEffectTech(EffType.T3_1406.getNumber());
			break;
		case 2:
			maxLevel += getParent().getParent().getParent().getEffect().getEffectTech(EffType.T3_1405.getNumber());
			break;
		case 3:
			maxLevel += getParent().getParent().getParent().getEffect().getEffectTech(EffType.T3_1407.getNumber());
			break;

		default:
			break;
		}
		return maxLevel;
	}

	/** 技能提供作用号值
	 * 
	 * @return */
	public List<PBHeroEffect> effectVal() {
		HeroSkillCfg cfg = getCfg();
		HawkAssert.notNull(cfg, "Cfg error HeroSkillCfg is null skillID = " + skillID());
		List<PBHeroEffect> result = new ArrayList<>();
		// 属性值
		final int attrVal = getParent().getParent().attrs().get(cfg.getAttrId()).getNumber();
		final int officeId = getParent().getParent().getOffice();
		for (HawkTuple5<Integer, Double, Double, Integer, Integer> eic : cfg.getBuff()) {
			boolean skillOfficeNonNull = eic.fourth > 0 || eic.fifth > 0;
			boolean officeNotOK = officeId != eic.fourth && eic.fifth != officeId;
			if (skillOfficeNonNull && officeNotOK) {
				continue;
			}
			int effVal = (int) Math.ceil((eic.second + eic.third * getLevel()) * attrVal);
			if (eic.first == EffType.HERO_1635_VALUE) {
				effVal = ConstProperty.getInstance().getEffect1635BaseVal();
			}
			if (eic.first == EffType.HERO_1640_VALUE) {
				effVal = ConstProperty.getInstance().getEffect1640Parametric();
			}
			if (eic.first == EffType.EFF_12122_VALUE) {
				effVal = effVal + ConstProperty.getInstance().getEffect12122BaseVaule();
			}
			if (eic.first == EffType.EFF_12123_VALUE) {
				effVal = effVal + ConstProperty.getInstance().getEffect12123BaseVaule();
			}
			if (eic.first == EffType.HERO_12151_VALUE) {
				effVal = effVal + ConstProperty.getInstance().getEffect12151BaseVaule();
			}
			if (eic.first == EffType.HERO_12191_VALUE) {
				effVal = effVal + ConstProperty.getInstance().getEffect12191BaseVaule();
			}
			if (eic.first == EffType.HERO_12192_VALUE) {
				effVal = effVal + ConstProperty.getInstance().getEffect12192BaseVaule();
			}
			if (eic.first == EffType.HERO_12441_VALUE) {
				effVal = effVal + ConstProperty.getInstance().getEffect12441BaseVaule();
			}
			

			PBHeroEffect ef = PBHeroEffect.newBuilder()
					.setEffectId(eic.first)
					.setValue(effVal).build();
			result.add(ef);
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
	public String serializ() {
		// 考虑到未来维护可能需要兼容. 这里扔使用array
		JSONArray array = new JSONArray();
		array.add(exp);
		return array.toJSONString();
	}

	/** 反序列化
	 * 
	 * @param serialiedStr */
	@Override
	public void mergeFrom(String serialiedStr) {
		JSONArray array = JSONArray.parseArray(serialiedStr);
		exp = array.getIntValue(0);
	}

	/** 返回pb对象
	 * 
	 * @return */
	public PBSkillInfo toPBobj() {
		PBSkillInfo.Builder bul = PBSkillInfo.newBuilder();
		bul.setHeroId(parent.getParent().getCfgId())
				.setSkillId(skillID())
				.setTotalExp(exp)
				.setLevel(getLevel())
				.addAllEffect(effectVal());
		return bul.build();
	}


	/** 释放技能 */
	public final void cast(Object... args) {
		casting(args);
	}

	/** 技能是否在冷却中
	 * 
	 * @return */
	protected void casting(Object... args) {
	}

	public boolean isCooling() {
		return false;
	}
	
	public HeroSkillCfg getCfg() {
		HeroSkillCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroSkillCfg.class, skillID());
		HawkAssert.notNull(cfg, "HeroSkillCfg is null for id : " + skillID());
		return cfg;
	}

	public SkillSlot getParent() {
		return parent;
	}

	public void setParent(SkillSlot parent) {
		this.parent = parent;
	}

	public int getExp() {
		return exp;
	}
	
	protected int getSoulEffVal(EffType eff) {
		try {
			return getParent().getParent().getSoul().getEffVal(eff);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
}
