package com.hawk.game.battle;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;

import com.google.common.base.Splitter;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

public class BattleSoldier_1 extends BattleSoldier {
	// <data id="10102" des="装甲坦克（盾兵）拉姆护盾系统(铁甲)1级无需配置数值,极大地提高防御" />
	// <data id="10103" des="装甲坦克（盾兵）双重装甲(体魄)1级无需配置数值,极大地提高生命" />
	boolean skill1451;
	/**114技能层数*/
	int SOLDIER_SKILL_114_ceng;
	int deadPct;
	int eff1320StartRound;
	private boolean bakeProtect; // 触发正义防卫中
	private BattleSoldier bake1631Protected; // 保护谁

	private int eff1635Cen;

	/** 结束回合 - 值*/
	private HawkTuple2<Integer, Integer> skill624Debuff;
	private HawkTuple2<Integer, Integer> skill544Debuff;

	private int eff19092Round;
	private boolean triggerSkill144;
	private int eff12401Cnt;
	private int eff12403Cnt;
	
	@Override
	public void roundStart() {
		super.roundStart();
		skill1451 = false;
		hundun12085();
	}

	private void hundun12085() {
		try {

			if (getEffVal(EffType.EFF_12085) <= 0) {
				return;
			}
			int battleRound = getTroop().getBattle().getBattleRound();
			boolean five = battleRound % ConstProperty.getInstance().getEffect12085AtkRound() == 0;
			if (!five) {
				return;
			}
			List<BattleSoldier> yuanList = getTroop().getSoldierList().stream()
					.filter(BattleSoldier::isYuanCheng).filter(BattleSoldier::isAlive)
					.filter(s -> !Objects.equals(s.getPlayerId(), getPlayerId())).collect(Collectors.toList());
			int count = 0;
			Collections.shuffle(yuanList);
			int endRound = getBattleRound() + ConstProperty.getInstance().getEffect12085ContinueRound() - 1;
			int buffRound = getBattleRound() + eff19092Round - 1;
			// 即护盾值 = min（该防御坦克数量，100万）*该防御坦克生命值*XX.XX%
			int val = (int) (Math.min(get12085Cnt(), ConstProperty.getInstance().getEffect12085NumLimit()) * GsConst.EFF_PER * getHpVal() * getEffVal(EffType.EFF_12085));
			for (BattleSoldier yuan : yuanList) {
				if (yuan.addBuff12085(this, endRound, val, buffRound, getEffVal(EffType.EFF_12092))) {
					addDebugLog("远程庇护 {} +12085->  {}", getUUID(), yuan.getUUID());
					count++;
					if (getEffVal(EffType.HERO_12403) > 0) {
						eff12403Cnt = Math.min(eff12403Cnt + 1, ConstProperty.getInstance().getEffect12403Maxinum());
						addDebugLog("### 12403 {}  层数 {}", getUUID(), eff12403Cnt);
					}
				}
				if (count >= ConstProperty.getInstance().getEffect12085EffectNum()) {
					break;
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	protected int get12085Cnt() {
		return getFreeCnt();
	}

	@Override
	public void init(Player player, BattleSoldierCfg soldierCfg, int count, int shadowCnt) {
		super.init(player, soldierCfg, count, shadowCnt);
		if (!hasSkill(PBSoldierSkill.SOLDIER_SKILL_114)) {
			SOLDIER_SKILL_114_ceng = -1000000;
		}
	}

	@Override
	public SoldierType getType() {
		return SoldierType.TANK_SOLDIER_1;
	}

	@Override
	public void attackOnce(BattleSoldier defSoldier, int atkTimes, double hurtPer, int atkDis) {
		if (getEffVal(EffType.EFF_1451) > 0 && Math.random() < 0.25) {
			// 防御坦克（兵种type=1）有一定概率（25%，写死或填const表）本回合不进行攻击，防御大幅提升。提升比例=作用号/10000.
			skill1451 = true;
			return;
		}

		super.attackOnce(defSoldier, atkTimes, hurtPer, atkDis);
	}

	@Override
	public int skillReduceHurtPer(BattleSoldier atkSoldier) {
		int result = super.skillReduceHurtPer(atkSoldier);
		int M5_1320 = getEffVal(EffType.M5_1320);
		if (M5_1320 > 0 && eff1320StartRound > 0 && eff1320StartRound + 1 >= getTroop().getBattle().getBattleRound()) {
			result += M5_1320;
			getTroop().getBattle().addDebugLog("### M5 1320 额外减伤 {} ", M5_1320);
		}
		if (bakeProtect) {
			result += getEffVal(EffType.HERO_1632);
		}
		return result;
	}

	@Override
	public int skillHPExactly() {
		int result = super.skillHPExactly();
		if (SOLDIER_SKILL_114_ceng > 0) {
			BattleSoldierSkillCfg skill_14 = getSkill(PBSoldierSkill.SOLDIER_SKILL_114);
			result += Math.min(SOLDIER_SKILL_114_ceng, NumberUtils.toInt(skill_14.getP3())) * NumberUtils.toInt(skill_14.getP2());
		}
		result += getTroop().getSkill144Val().values().stream().mapToInt(Integer::intValue).sum();
		HawkTuple3<Integer, Integer, Integer> eff12163Debuff = eff12163DebuffVal();
		if (eff12163Debuff.second > 0) {
			result -= eff12163Debuff.second;
			addDebugLog("###{}防御坦克处于【损坏状态】时，其防御、生命加成减少 +{} ", getUUID(), eff12163Debuff.second);
		}
		result += eff12401Cnt * getEffVal(EffType.HERO_12401);
		result += eff12403Cnt * getEffVal(EffType.HERO_12403);
		return result;
	}
	
	@Override
	public int skillHPExactly(BattleSoldier atkSoldier) {
		int result = super.skillHPExactly(atkSoldier);
		if (atkSoldier.isJinZhan()) {//防坦受到近战部队攻击时，生命+XX%
			BattleSoldierSkillCfg skill_141 = getSkill(PBSoldierSkill.SOLDIER_SKILL_14101);
			result += skill_141.getP1IntVal();
		}
		return result;
	}

	@Override
	public int skillDefExactly(BattleSoldier atkSoldier) {
		int result = super.skillDefExactly(atkSoldier);
		if (skill1451) {
			result += getEffVal(EffType.EFF_1451);
		}

		if (SOLDIER_SKILL_114_ceng > 0) {
			BattleSoldierSkillCfg skill_14 = getSkill(PBSoldierSkill.SOLDIER_SKILL_114);
			result += Math.min(SOLDIER_SKILL_114_ceng, NumberUtils.toInt(skill_14.getP3())) * skill_14.getP1IntVal();
		}
		result += getTroop().getSkill144Val().values().stream().mapToInt(Integer::intValue).sum();
		
		HawkTuple3<Integer, Integer, Integer> eff12163Debuff = eff12163DebuffVal();
		if (eff12163Debuff.second > 0) {
			result -= eff12163Debuff.second;
			addDebugLog("###{}防御坦克处于【损坏状态】时，其防御、生命加成减少 +{} ", getUUID(), eff12163Debuff.second);
		}
		result += eff12401Cnt * getEffVal(EffType.HERO_12401);
		
		return result;
	}

	@Override
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		hurtVal = super.reduceHurtValPct(atkSoldier, hurtVal);
		SoldierType tarType = atkSoldier.getType();
		hurtVal *= (1 - skill3Value(tarType) * GsConst.EFF_PER);
		hurtVal *= (1 - skill4Value(tarType) * GsConst.EFF_PER);
		BattleSoldierSkillCfg skill_24 = getSkill(PBSoldierSkill.SOLDIER_SKILL_124);
		if (trigerSkill(skill_24) && atkSoldier.isJinZhan()) {
			hurtVal *= (1 - skill_24.getP1IntVal() * GsConst.EFF_PER);
		}
		hurtVal *= (1 - eff11004() * GsConst.EFF_PER);
		
		HawkTuple3<Integer, Integer, Integer> eff12163Debuff = eff12163DebuffVal();
		if (eff12163Debuff.third > 0) {
			hurtVal *= (1 + eff12163DebuffVal().third * GsConst.EFF_PER);
			hurtVal = Math.max(hurtVal, 0);
			addDebugLog("###{}防御坦克处于【损坏状态】时，其在受到攻击时，伤害额外+{} ", getUUID(), eff12163Debuff.third);
		}
		return hurtVal;
	}

	@Override
	public int skillHurtExactly(BattleSoldier defSoldier) {
		int damage = super.skillHurtExactly(defSoldier);
		BattleSoldierSkillCfg skill_5 = getSkill(PBSoldierSkill.TANK_SOLDIER_1_SKILL_5); // 爆击
		int trigger5 = skill_5.getTrigger() + getEffVal(EffType.SOLDIER_SKILL_203_105) - skill624DebuffVal() - getDebuff12206Val();
		if (trigerSkill(skill_5, trigger5)) {
			damage = damage + skill_5.getP1IntVal();
		}

		return damage;

	}

	private int skill3Value(SoldierType tarType) {
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.TANK_SOLDIER_1_SKILL_3);
		if (Objects.isNull(skill)) {
			return 0;
		}
		skillTrigerLog(skill, GsConst.RANDOM_MYRIABIT_BASE);

		int resut = skill.getP1IntVal();
		if (StringUtils.isNotEmpty(skill.getP2()) && Splitter.on(",").splitToList(skill.getP2()).contains(tarType.getNumber() + "")) {
			resut = resut + NumberUtils.toInt(skill.getP3());
		}

		resut = Math.min(5000, resut);
		return resut;
	}

	private int skill4Value(SoldierType tarType) {
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.TANK_SOLDIER_1_SKILL_4);
		if (trigerSkill(skill)) {
			return skill.getP1IntVal() + getEffVal(EffType.SOLDIER_SKILL_104);
		}
		return 0;
	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.TANK_SOLDIER_1_SKILL_66;
	}

	@Override
	protected void attacked(BattleSoldier attacker, int deadCnt) {
		super.attacked(attacker, deadCnt);
		SOLDIER_SKILL_114_ceng++;
		bakeProtect = false;
		// 死亡百分比
		int dpct = (int) (getDeadCnt() * 10D / getOriCnt());
		if (dpct != deadPct) {
			deadPct = dpct;
			if (getEffVal(EffType.M5_1320) > 0) {
				eff1320StartRound = getTroop().getBattle().getBattleRound();
				getTroop().getBattle().addDebugLog("### M5 1320 {} 受伤 {}/10 ", attacker.getSoldierId(), deadPct);
			}
		}

		if (attacker.canBeAttack()) {
			int effVal1432 = getEffVal(EffType.EFF_1432);
			if (effVal1432 > 0 && deadCnt > 1) {
				double hurtVal = deadCnt * getHpVal() * GsConst.EFF_PER * effVal1432;
				hurtVal = attacker.forceField(this, hurtVal);
				int curCnt = attacker.getFreeCnt();
				int maxKillCnt = (int) Math.ceil(3.0f * hurtVal / attacker.getHpVal());
				maxKillCnt = Math.max(1, maxKillCnt);
				int killCnt = Math.min(maxKillCnt, curCnt);

				attacker.addDeadCnt(killCnt);
				addKillCnt(attacker, killCnt);
				getTroop().getBattle().addDebugLog("### 触发1432 击杀 {} count{} ", attacker.getSoldierId(), killCnt);
			}
		}

		if (triggerSkill144) {
			BattleSoldierSkillCfg skill144 = getSkill(PBSoldierSkill.TANK_SOLDIER_1_SKILL_44);
			int val = getTroop().getSkill144Val().getOrDefault("skill144", 0);
			val = Math.min(val + skill144.getP1IntVal(), skill144.getP1IntVal() * NumberUtils.toInt(skill144.getP2()));
			getTroop().getSkill144Val().put("skill144", val);
			getTroop().getBattle().addDebugLog("### skill144 {} val {} ", getUUID(), val);
		}
		if (attacker.isYuanCheng() && getEffVal(EffType.HERO_12401)>0) {
			eff12401Cnt = Math.min(eff12401Cnt + 1, ConstProperty.getInstance().getEffect12401Maxinum());
			addDebugLog("### 12401 {}  层数 {}", getUUID(), eff12401Cnt);
		}
	}

	public BattleSoldier getBake1631Protected() {
		return bake1631Protected;
	}

	public void setBake1631Protected(BattleSoldier bake1631Protected) {
		this.bake1631Protected = bake1631Protected;
	}

	public boolean isBakeProtect() {
		return bakeProtect;
	}

	public void setBakeProtect(boolean bakeProtect) {
		this.bakeProtect = bakeProtect;
	}

	public void incEff1635Cen() {
		eff1635Cen += 1;
		getTroop().getBattle().addDebugLog("### Save1634 {}  层数 {}", getUUID(), eff1635Cen);
	}

	public int hero1634EffVal() {
		return getEffVal(EffType.HERO_1634) + Math.min(eff1635Cen, ConstProperty.getInstance().getEffect1634Maxinum()) * getEffVal(EffType.HERO_1635);
	}

	@Override
	public ArmyInfo calcArmyInfo(double selfCoverRete) {
		ArmyInfo result = super.calcArmyInfo(selfCoverRete);
		int deadCnt = result.getDeadCount();
		int save1634 = (int) (deadCnt * GsConst.EFF_PER * hero1634EffVal());
		result.setDeadCount(deadCnt - save1634);
		result.setSave1634(save1634);
		if (save1634 > 0) {
			HawkLog.logPrintln("Save1634 soldierUID:{} effectVal:{} olddead:{} savecount:{}", getUUID(), hero1634EffVal(), deadCnt, save1634);
		}
		return result;
	}

	public int getEff1635Cen() {
		return eff1635Cen;
	}

	@Override
	public boolean isTank() {
		return true;
	}

	@Override
	public void setSkill624Debuff(HawkTuple2<Integer, Integer> debuff) {
		skill624Debuff = debuff;
	}

	int skill624DebuffVal() {
		if (Objects.isNull(skill624Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill624Debuff.first) {
			return 0;
		}
		getTroop().getBattle().addDebugLog("### skill624 降低 105技能 暴击几率 {} 结束回合 {}", skill624Debuff.second, skill624Debuff.first);
		return skill624Debuff.second;
	}
	
	@Override
	public void setSkill544Debuff(HawkTuple2<Integer, Integer> debuff) {
		skill544Debuff = debuff;
	}
	
	public HawkTuple2<Integer, Integer> getSkill544Debuff() {
		return skill544Debuff;
	}

	private int eff11004() {
		int result = 0;
		if (getEffVal(EffType.ARMOUR_11004) > 0) {
			int atkCnt = Math.min(getAttackedCnt(), ConstProperty.getInstance().getEffect11004TimesLimit());
			result = atkCnt * getEffVal(EffType.ARMOUR_11004);
			getTroop().getBattle().addDebugLog("### ARMOUR_11004 {} 命中数 {} 11004 {}", getUUID(), atkCnt, result);
		}
		return result;
	}

	@Override
	protected PBSoldierSkill honor10SkillId() {
		return PBSoldierSkill.TANK_SOLDIER_1_SKILL_34;
	}

	@Override
	public Map<EffType, Integer> getEffMapClientShow() {
		Map<EffType, Integer> result = super.getEffMapClientShow();
		mergeClientShow(result, EffType.HERO_12004, EffType.FIRE_2001);
		mergeClientShow(result, EffType.EFF_12033, EffType.TANK_A_DEF_PER, EffType.TANK_A_HP_PER);
		mergeClientShow(result, EffType.PLANT_SOLDIER_SKILL_844, EffType.TANK_A_DEF_PER);
		return result;
	}

	public int getEff19092Round() {
		return eff19092Round;
	}

	public void setEff19092Round(int eff19092Round) {
		this.eff19092Round = eff19092Round;
	}

	public boolean isTriggerSkill144() {
		return triggerSkill144;
	}

	public void setTriggerSkill144(boolean triggerSkill144) {
		this.triggerSkill144 = triggerSkill144;
	}

}
