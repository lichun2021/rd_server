package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Objects;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.RandomUtil;

public class BattleSoldier_7 extends ICannonSoldier {
	boolean trigerSkill5;
	boolean triger1508;
	private int buf1540CENCnt; // 1540层数
	private int buf1540ATKCnt; // 连续攻击3回合 倒数
	private boolean buf1540ATK3; // 连续攻击3回合
	private boolean buf1540ZiHui; // 触发自毁伤害
	private boolean buf1540JisnShe; // 建设伤害
	private boolean triggerSkill744;
	/**点援护值*/
	private int effect12331AddEscortPoint;
	/**火力值*/
	private int effect12335AddFirePoint;
	boolean effect12331Round;
	boolean effect12335Round;
	public int eff12339Cnt;
	public double eff12339Power;
	public int buff74601;
	int atkCnt;
	/**【12673】能级护阵：战斗开始时，自身攻城车受到的伤害降低80.00%（作用号12673），该减伤效果每次生效效果衰减10.00%（effect12673BaseVaule）  */
	int effect12673Vaule;
	boolean effect12674AtkThreshold = false;
	
	@Override
	public SoldierType getType() {
		return SoldierType.CANNON_SOLDIER_7;
	}

	@Override
	public void beforeWarfare() {
		super.beforeWarfare();
		effect12673Vaule = getEffVal(EffType.HERO_12673) + getEffVal(EffType.HERO_12692);
	}

	@Override
	public void roundStart() {
		super.roundStart();
		trigerSkill5 = false;
		triger1508 = false;
		{
			buf1540ATK3 = false;
			if (buf1540ATKCnt < 0 && buf1540CENCnt >= ConstProperty.getInstance().getEffect1541Maxinum()) {
				buf1540ATKCnt = 3;
				getTroop().getBattle().addDebugLog("### MBWS兵种:{} 1541 持续3回合攻击触发", getSoldierId());
			}
			if (buf1540ATKCnt > 0) {
				buf1540ATK3 = true;
			}
			buf1540ATKCnt--;
			if (buf1540ATK3 && buf1540ATKCnt == 0) {
				buf1540ZiHui = true;
			}
		}
		
		if (getEffVal(EffType.HERO_12671) > 0 && getBattleRound() % 2 == 1) {
			int maxp = addEffect12335FirePoint(ConstProperty.getInstance().effect12671AddFirePoint);
			addDebugLog("12671 自身每次进入奇数回合时立即增加火力值 , {} 上限 {}", effect12335AddFirePoint, maxp);
		}
		if (getEffVal(EffType.HERO_12672) > 0 && getBattleRound() % 2 == 0) {
			int maxp = incEffect12331EscortPoint(ConstProperty.getInstance().effect12672AddEscortPoint);
			addDebugLog("12672 自身每次进入偶数回合时立即增加援护值, {} 上限 {}", effect12331AddEscortPoint, maxp);
		}
		
		effect12331Round = getEffVal(EffType.HERO_12331) > 0 && getBattleRound()%2 == 1;
		effect12335Round = getEffVal(EffType.HERO_12335) > 0 && getBattleRound()%2 == 0;
		if(effect12331Round){
			check12333();
		}
		if(effect12335Round){
			check12335();
		}
		eff12674Atk();
	}
	
	private void eff12674Atk() {
		if (getEffVal(EffType.HERO_12674) <= 0) {
			return;
		}
		if (getBattleRound() % ConstProperty.getInstance().effect12674AtkRound != 0) {
			return;
		}
		
		int atkTimes = ConstProperty.getInstance().effect12674AtkTimes;
		int effect12674AtkThresholdValue = ConstProperty.getInstance().effect12674AtkThresholdValue;
		if (!effect12674AtkThreshold && effect12331AddEscortPoint >= effect12674AtkThresholdValue &&
				effect12335AddFirePoint >= effect12674AtkThresholdValue) {
			atkTimes += 1;
			effect12674AtkThreshold = true; //效果（一场战斗只能触发一次）
			addDebugLog("【协同攻击】时火力值和援护值均达到 {}，则在追加1轮  （一场战斗只能触发一次）",effect12674AtkThresholdValue);
		}
		addDebugLog("【协同攻击】时火力值: {} 援护值: {} effect12674AtkThresholdValue:{} atkTimes:{}",effect12335AddFirePoint,effect12331AddEscortPoint ,effect12674AtkThresholdValue,atkTimes);
		for (int i = 0; i < atkTimes; i++) {
			for (BattleSoldier defSoldier : getTroop().getEnemyTroop().getSoldierList()) {
				int hurtRate = (int) (getEffVal(EffType.HERO_12674) * GsConst.EFF_PER * ConstProperty.getInstance().effect12674SoldierAdjustMap.getOrDefault(defSoldier.getType(), 0));
				addDebugLog("###【12674】【协同攻击】（伤害率 {}（作用号12674）  ", hurtRate);
				additionalAttack(defSoldier, hurtRate, true, true, "");
				defSoldier.addAstiaya12674Debuff(this);
			}
		}
	}

	private void check12335() {
		String pid = getPlayerId();
		List<BattleSoldier> ylist = getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::isYuanCheng)
				.filter(b -> !Objects.equal(b.getPlayerId(), pid))
				.filter(bt -> bt.getBuff12337HpVal() == 0).collect(Collectors.toList());
		int effect12333Maxinum = ConstProperty.getInstance().getEffect12337Maxinum();
		if(HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < getEffVal( EffType.HERO_12356)){
			effect12333Maxinum += ConstProperty.getInstance().getEffect12356AddNum();
			addDebugLog("【12356】战技持续期间，开启聚能环护模式时，有 XX% 的概率将目标友军数由 2 个增至 3 个");
		}
		ylist = RandomUtil.randomWeightObject(ylist, effect12333Maxinum);
		for (BattleSoldier soldier : ylist) {
			int val = getEffVal(EffType.HERO_12337) + getEffVal(EffType.HERO_12338) * effect12331AddEscortPoint;
			soldier.setBuff12337(this, val + getEffVal(EffType.HERO_12354), val, getEffVal(EffType.HERO_12339));
		}

	}

	private void check12333() {
		String pid = getPlayerId();
		List<BattleSoldier> ylist = getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::isYuanCheng).filter(b-> !Objects.equal(b.getPlayerId(), pid))
				.filter(bt -> bt.getBuff12333Val(false) == 0).collect(Collectors.toList());
		int effect12333Maxinum = ConstProperty.getInstance().getEffect12333Maxinum();
		if(HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < getEffVal( EffType.HERO_12355)){
			effect12333Maxinum += ConstProperty.getInstance().getEffect12355AddNum();
			addDebugLog("【12355】战技持续期间，开启火力全开模式时，有 XX% 的概率将目标友军数由 2 个增至 3 个");
		}
		ylist = RandomUtil.randomWeightObject(ylist, effect12333Maxinum);
		for (BattleSoldier soldier : ylist) {
			int val = getEffVal(EffType.HERO_12333) + getEffVal(EffType.HERO_12334) * effect12335AddFirePoint;
			soldier.setBuff12333(val, val + getEffVal(EffType.HERO_12352));
		}
	}

	@Override
	public int atkTimes(int round) {
		if (debuff12015ContinueRound >= getBattleRound() && getBattleRound() % ConstProperty.getInstance().getEffect12015AtkRound() != 0) {
			return 0;
		}
		if (buf1540ATK3) {
			getTroop().getBattle().addDebugLog("### MBWS兵种:{} 1541 连续攻击 {}", getSoldierId(), buf1540ATKCnt + 1);
			return 1;
		}
		return super.atkTimes(round);
	}

	@Override
	public void attackOver(BattleSoldier defSoldier, int killCnt, double hurtVal) {
		super.attackOver(defSoldier, killCnt, hurtVal);
		atkCnt++;
		if(hasSkill(PBSoldierSkill.SOLDIER_SKILL_74501)){
			BattleSoldierSkillCfg skill425 = getSkill(PBSoldierSkill.SOLDIER_SKILL_74501);
			int skill74501Buff = getTroop().skill74501Buff ;
			skill74501Buff = skill74501Buff + skill425.getP3IntVal()/skill425.getP1IntVal();
			skill74501Buff = Math.min(skill74501Buff, skill425.getP3IntVal()* skill425.getP4IntVal());
			getTroop().skill74501Buff = skill74501Buff;
		}
		BattleSoldierSkillCfg skill424 = getSkill(PBSoldierSkill.SOLDIER_SKILL_724);
		if (defSoldier.isYuanCheng() && trigerSkill(skill424)) {
			defSoldier.incrDebufSkill724Val(skill424.getP1IntVal(), NumberUtils.toInt(skill424.getP2()));
		}
		defSoldier.addDebuff11012(getPlayerId(), getEffVal(EffType.ARMOUR_11012));
		if (getEffVal(EffType.EFF_12041) > 0 && defSoldier.isPlan()) {
			((IPlanSoldier) defSoldier).addDebuff12041(getEffVal(EffType.EFF_12041));
		}

		if (triggerSkill744) {
			BattleSoldierSkillCfg skill744 = getSkill(PBSoldierSkill.CANNON_SOLDIER_7_SKILL_44);
			int round = getTroop().getBattle().getBattleRound() + NumberUtils.toInt(skill744.getP2()) - 1;
			int val = skill744.getP1IntVal();
			HawkTuple2<Integer, Integer> skill744Debuff = HawkTuples.tuple(round, val);
			defSoldier.setSkill744Debuff(skill744Debuff);
			getTroop().getBattle().addDebugLog("### {} skill744   {} 降低其攻击、防御、生命 {} 结束回合 {}", getUUID(), defSoldier.getUUID(), val, round);
		}
		if (effect12331Round) {
			int maxp = incEffect12331EscortPoint(ConstProperty.getInstance().getEffect12331AddEscortPoint());
			addDebugLog(" 12331 - 且在每攻击命中 1 次敌方单位后，增加自身 1 点援护值（该数值可累计，至多 {}点） val= {}", maxp, effect12331AddEscortPoint);
		}
		if (getEffVal(EffType.EFF_12553) > 0 && defSoldier.isYuanCheng()) {
			int debuffEffect12553Value = defSoldier.debuffEffect12553Value + getEffVal(EffType.EFF_12553);
			debuffEffect12553Value = Math.min(debuffEffect12553Value, ConstProperty.getInstance().effect12553MaxValue);
			defSoldier.debuffEffect12553Value = debuffEffect12553Value;
			addDebugLog(" 12553 降低 {} 攻击、防御、生命  {}", getUUID(), debuffEffect12553Value);
		}
	}

	private int incEffect12331EscortPoint(int add) {
		effect12331AddEscortPoint += add;
		int maxp = 0;
		if(getEffVal(EffType.HERO_12331) > 0){
			maxp += ConstProperty.getInstance().getEffect12331MaxEscortPoint() + getEffVal(EffType.HERO_12351);
		}
		if(getEffVal(EffType.HERO_12672) > 0 && getEffVal(EffType.HERO_12331) > 0){
			maxp += ConstProperty.getInstance().effect12672PlusEscortPoint;
		}
		
		effect12331AddEscortPoint = Math.min(effect12331AddEscortPoint, maxp);
		return maxp;
	}

	@Override
	protected void attacked(BattleSoldier attacker, int deadCnt) {
		super.attacked(attacker, deadCnt);
		if (effect12335Round) {
			int maxp = addEffect12335FirePoint(ConstProperty.getInstance().getEffect12335AddFirePoint());
			addDebugLog(" 12335 - 且在每受到 1 次攻击后，增加自身 1 点火力值 {} 上限 {}", effect12335AddFirePoint, maxp);
		}
		skill74601Check();
		
	}

	private int addEffect12335FirePoint(int add) {
		effect12335AddFirePoint += add;
		int maxp = 0;
		if(getEffVal(EffType.HERO_12335) > 0){
			maxp += ConstProperty.getInstance().getEffect12335MaxFirePoint() + getEffVal(EffType.HERO_12353);
		}
		if(getEffVal(EffType.HERO_12671) > 0 && getEffVal(EffType.HERO_12331) > 0){
			maxp += ConstProperty.getInstance().effect12671PlusFirePoint;
		}
		
		effect12335AddFirePoint = Math.min(effect12335AddFirePoint, maxp);
		return maxp;
	}

	private void skill74601Check() {
		BattleSoldierSkillCfg scfg = getSkill(PBSoldierSkill.SOLDIER_SKILL_74601);
		if (scfg.getP1IntVal() > 0) {
			buff74601 = Math.min(buff74601 + scfg.getP1IntVal(), scfg.getP1IntVal() * scfg.getP2IntVal());
		}
	}

	@Override
	public void attackOnce(BattleSoldier defSoldier, int atkTimes, double hurtPer, int atkDis) {
		triger1508 = getEffVal(EffType.SOLDIER_1508) > 0 && HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < ConstProperty.getInstance().getEffect1508Prob();
		if (triger1508) { // 普攻变aoe
			List<BattleSoldier> soldierList = enemyList(defSoldier.getTroop());
			int n = Math.min(3, soldierList.size());
			Collections.shuffle(soldierList);
			for (int i = 0; i < n; i++) {
				getTroop().getBattle().addDebugLog("### 1508 触发:{} ", i);
				BattleSoldier sd = soldierList.get(i);
				super.attackOnce(sd, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
				try1540(sd);
			}
			getTroop().getBattle().addDebugLog("### 1508 触发结束 攻击{}次 ", n);
		} else {
			super.attackOnce(defSoldier, atkTimes, hurtPer, atkDis);
			try1540(defSoldier);
		}

		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.CANNON_SOLDIER_7_SKILL_5);
		if (skill != null && getHeros().contains(skill.getP1IntVal())) {
			List<BattleSoldier> soldierList = enemyList(defSoldier.getTroop());
			int rate = Math.min(NumberUtils.toInt(skill.getP2()) * soldierList.size(), skill.getTrigger());
			trigerSkill5 = trigerSkill(skill, rate);
			if (trigerSkill5) {
				int n = NumberUtils.toInt(skill.getP3());
				n = Math.min(n, soldierList.size());
				Collections.shuffle(soldierList);
				for (int i = 0; i < n; i++) {
					BattleSoldier sd = soldierList.get(i);
					super.attackOnce(sd, QIAN_PAI_MAX, 0, Integer.MAX_VALUE);
					// try1540(sd);
				}
			}
		}

		if (buf1540ZiHui) {
			buf1540ZiHui = false;
			buf1540CENCnt = 0;
			int seldDead = (int) (ConstProperty.getInstance().getEffect1541Power() * GsConst.EFF_PER * getFreeCnt());
			addDeadCnt(seldDead);
			getTroop().getBattle().addDebugLog("### MBWS兵种:{}  1541 自毁 {}", getSoldierId(), seldDead);
		}
	}

	@Override
	public void beforeAttack(BattleSoldier defSoldier) {
		super.beforeAttack(defSoldier);
		for (int i = 0; i < ConstProperty.getInstance().getEffect12015AtkTimes(); i++) {
			jelani12015(defSoldier);
		}
	}

	private boolean jelani12015AOE;
	/**发动此效果后 3 个回合内，无法释放以敌方为目标的攻击、技能等各类效果*/
	private int debuff12015ContinueRound;

	private void jelani12015(BattleSoldier defSoldier) {
		try {
			jelani12015AOE = false;
			if (getEffVal(EffType.HERO_12015) <= 0 || getBattleRound() % ConstProperty.getInstance().getEffect12015AtkRound() != 0) {
				return;
			}
			List<BattleSoldier> footList = defSoldier.getTroop().getSoldierList().stream()
					.filter(BattleSoldier::isAlive)
					.filter(BattleSoldier::canBeAttack)
					.collect(Collectors.toCollection(ArrayList::new));
			Collections.shuffle(footList);
			footList = footList.stream().limit(ConstProperty.getInstance().getEffect12015AtkNum()).collect(Collectors.toList());

			addDebugLog("-Jelani {} 每第 5 回合，额外向敌方随机 {}个单位进行 1 轮攻击（伤害率XX.XX%）", ConstProperty.getInstance().getEffect12015AtkNum(), getUUID());
			jelani12015AOE = true;
			for (BattleSoldier tar : footList) {
				super.attackOnce(tar, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
			}
			debuff12015ContinueRound = getBattleRound() + ConstProperty.getInstance().getEffect12015ContinueRound();
			addDebugLog("-Jelani {} 发动此效果后 {}个回合内，无法释放以敌方为目标的攻击、技能等各类效果 ", getUUID(), debuff12015ContinueRound);
			jelani12015AOE = false;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void try1540(BattleSoldier defSoldier) {
		final int eff1540 = getEffVal(EffType.MDWS_1540);
		if (eff1540 == 0) {
			return;
		}

		List<BattleSoldier> soldierList = enemyList(defSoldier.getTroop());
		int effect1540Prob1 = ConstProperty.getInstance().getEffect1540Prob1();
		int effect1540Prob2 = ConstProperty.getInstance().getEffect1540Prob2();
		int p = effect1540Prob1 + effect1540Prob2 * Math.min(6, soldierList.size());
		getTroop().getBattle().addDebugLog("### MBWS effect1540Prob1:{} effect1540Prob2{} 目标:{} 触发概率:{} ", effect1540Prob1, effect1540Prob2, soldierList.size(), p);
		if (p < HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE)) {
			return;
		}

		BattleSoldier t1 = null;
		BattleSoldier t2 = null;
		soldierList.remove(defSoldier);

		// 前排取两个
		for (BattleSoldier sd : soldierList) {
			if (t2 == null) {
				t2 = sd;
				continue;
			}
			if (t1 == null) {
				t1 = sd;
				continue;
			}
			if (sd.getxPos() >= defSoldier.getxPos()) {
				continue;
			}
			if (sd.getxPos() > t1.getxPos() || defSoldier.getxPos() - sd.getxPos() == 1) {
				t2 = t1;
				t1 = sd;
			}
		}
		// 后排取两个
		for (BattleSoldier sd : soldierList) {
			if (sd.getxPos() <= defSoldier.getxPos()) {
				continue;
			}
			if (sd.getxPos() < t2.getxPos() || sd.getxPos() - defSoldier.getxPos() == 1) {
				if (t1 == null) {
					t1 = t2;
				}
				t2 = sd;
			}
		}
		buf1540JisnShe = true;
		super.attackOnce(defSoldier, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
		buf1540CENCnt++;
		if (t1 == t2) {
			t2 = null;
		}
		if (t1 != null) {
			super.attackOnce(t1, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
			buf1540CENCnt++;
		}
		if (t2 != null) {
			super.attackOnce(t2, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
			buf1540CENCnt++;
		}
		buf1540JisnShe = false;

	}

	@Override
	public int skillHPExactly() {
		int result = super.skillHPExactly();
		if (getEffVal(EffType.ARMOUR_11013) > 0) {
			int atkCnt = Math.min(getAttackCnt(), ConstProperty.getInstance().getEffect11013TimesLimit());
			result += atkCnt * getEffVal(EffType.ARMOUR_11013);
			getTroop().getBattle().addDebugLog("### ARMOUR_11013 生命 {} 层 {}", atkCnt, result);
		}
		return result;
	}
	
	@Override
	public int skillHPExactly(BattleSoldier atkSoldier) {
		int result = super.skillHPExactly(atkSoldier);
		if (atkSoldier.isYuanCheng()) {//攻城车受到远程部队攻击时，生命+XX%
			BattleSoldierSkillCfg skill_141 = getSkill(PBSoldierSkill.SOLDIER_SKILL_74101);
			result += skill_141.getP1IntVal();
		}
		return result;
	}

	@Override
	protected double getHurtVal(BattleSoldier defSoldier, double reducePer) {
		double result = super.getHurtVal(defSoldier, reducePer);
		if (triger1508 && !trigerSkill5) { // AOE
			int effVal1508 = getEffVal(EffType.SOLDIER_1508);
			result = result * GsConst.EFF_PER * effVal1508 * (1 + getEffVal(EffType.SOLDIER_1509) * GsConst.EFF_PER);
			getTroop().getBattle().addDebugLog("### 1508 AOE hurtVal = {}", result);
		}
		int AILISHA_1510 = getEffVal(EffType.AILISHA_1510);
		if (AILISHA_1510 > 0 && defSoldier.getType() == SoldierType.TANK_SOLDIER_1) {
			result = result * (1 + AILISHA_1510 * GsConst.EFF_PER);
			getTroop().getBattle().addDebugLog("### 1510 buff hurtVal = {}", result);
		}
		int AILISHA_1511 = getEffVal(EffType.AILISHA_1511);
		if (AILISHA_1511 > 0 && defSoldier.getType() == SoldierType.CANNON_SOLDIER_8) {
			result = result * (1 + AILISHA_1511 * GsConst.EFF_PER);
			getTroop().getBattle().addDebugLog("### 1511 buff hurtVal = {}", result);
		}

		if (buf1540JisnShe) { // 溅射AOE
			double before = result;
			result = result * GsConst.EFF_PER * getEffVal(EffType.MDWS_1540) * (1 + getEffVal(EffType.MDWS_1542) * GsConst.EFF_PER);
			getTroop().getBattle().addDebugLog("### MBWS兵种:{} 原伤害{}  1540 溅射 hurtVal = {}", getSoldierId(), before, result);
		}

		if (jelani12015AOE) {
			result = result * GsConst.EFF_PER * getEffVal(EffType.HERO_12015);
			addDebugLog("-Jelani {} 12015 AOE（伤害率 {}）{}  hurt {} ", getUUID(), getEffVal(EffType.HERO_12015), defSoldier.getUUID(), result);
		}
		
		if(getEffVal(EffType.HERO_12671) > 0 && effect12335AddFirePoint > ConstProperty.getInstance().effect12671AtkThresholdValue){
			double xiuzheng = GsConst.EFF_PER * ConstProperty.getInstance().effect12671SoldierAdjustMap.getOrDefault(defSoldier.getType(), 0);
			int effVal = (int) ((getEffVal(EffType.HERO_12671) + getEffVal(EffType.HERO_12691))* xiuzheng);
			result = result * (1 + effVal * GsConst.EFF_PER);
			addDebugLog("-12671 自身攻城车造成伤害增加 {} hurtVal:{}",  effVal, result);
		}

		return result;
	}

	private List<BattleSoldier> enemyList(BattleTroop enemyTroop) {
		// 防守兵
		List<BattleSoldier> soldierList = enemyTroop.getSoldierList();

		List<BattleSoldier> planList = soldierList.stream()
				.filter(ds -> ds.canBeAttack())
				.filter(ds -> ds.isAlive())
				// .filter(ds -> withinAtkDis(ds))
				.collect(Collectors.toCollection(ArrayList::new));
		return planList;
	}

	@Override
	public int skillHurtExactly(BattleSoldier defSoldier) {
		int result = super.skillHurtExactly(defSoldier);
		if (getEffVal(EffType.EFF_1420) > 0) {// EFF_1420
			int pct = (int) (getDeadCnt() * 10D / getOriCnt()) + 1;
			pct = Math.min(pct, 5);
			result = result + pct * getEffVal(EffType.EFF_1420);
		}
		if (trigerSkill5) {
			BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.CANNON_SOLDIER_7_SKILL_5);
			result = result + (int) (NumberUtils.toInt(skill.getP4()) - GsConst.EFF_RATE) + getEffVal(EffType.SOLDIER_SKILL_505_1089) + getEffVal(EffType.EFF_1426);
		}
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.CANNON_SOLDIER_7_SKILL_1);
		if (trigerSkill(skill)) {
			result = result + skill.getP1IntVal() + getEffVal(EffType.SOLDIER_SKILL_701);
		}

		// AOE <data id="71401" des="触发范围攻击时，额外增加伤害【本次攻击生效】" trigger="10000" p1="2000" />
		if (triger1508 || trigerSkill5 || jelani12015AOE) {
			BattleSoldierSkillCfg skill_14 = getSkill(PBSoldierSkill.SOLDIER_SKILL_714);
			if (trigerSkill(skill_14)) {
				result = result + skill_14.getP1IntVal();
			}
		}
		if (jelani12015AOE) {
			boolean dianran = defSoldier.isDianran(); // 被点燃
			if (dianran) {
				result = result + getEffVal(EffType.HERO_12016);
				addDebugLog("-Jelani {} 12015 AOE（伤害率 {}）,{} 被点燃 {} 12016 {} ", getUUID(), getEffVal(EffType.HERO_12015), defSoldier.getUUID(), dianran,
						getEffVal(EffType.HERO_12016));
			}
		}
		
		return result;
	}
	
	@Override
	public int skillIgnoreTargetAtk(BattleSoldier battleSoldier) {
		int result = super.skillIgnoreTargetAtk(battleSoldier);
		if (getEffVal(EffType.HERO_12672) > 0 && effect12331AddEscortPoint > ConstProperty.getInstance().effect12672AtkThresholdValue && getBattleRound() % 2 == 0) {
			int effVal = (int) (effect12331AddEscortPoint * ConstProperty.getInstance().effect12672BaseVaule);
			result = result + effVal;
			addDebugLog("-12672 援护值{} 受击时无视目标部分攻击加成 {} ",effect12331AddEscortPoint, effVal);
		}
		return result;
	}

	@Override
	public int skillIgnoreTargetHP(BattleSoldier target) {
		int result = super.skillIgnoreTargetHP(target);
		if(getEffVal(EffType.HERO_12671) > 0 && effect12335AddFirePoint > ConstProperty.getInstance().effect12671AtkThresholdValue){
			double effVal = effect12335AddFirePoint * ConstProperty.getInstance().effect12671BaseVaule;
			result = (int) (result + effVal); 
			addDebugLog("-12671  火力值{} 并在攻击时无视目标部分生命加成 {} ", effect12335AddFirePoint, effVal);
		}
		return result;
	}

	@Override
	public int skillIgnoreTargetDef(BattleSoldier target) {
		return super.skillIgnoreTargetDef(target);
	}


	@Override
	public double addHurtValPct(BattleSoldier defSoldier, double hurtVal) {
		hurtVal = super.addHurtValPct(defSoldier, hurtVal);

		if (effect12331Round) {
			int effVal = getEffVal(EffType.HERO_12331) + getEffVal(EffType.HERO_12332)* effect12335AddFirePoint;
			hurtVal *= (1 + effVal * GsConst.EFF_PER);
			addDebugLog(" 12331 - 使自身攻击所造成的伤害增加 【{} + {}火力值* {}】 ", getEffVal(EffType.HERO_12331), effect12335AddFirePoint, getEffVal(EffType.HERO_12332));
		}
		return hurtVal;
	}

	@Override
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		hurtVal = super.reduceHurtValPct(atkSoldier, hurtVal);
		if (getEffVal(EffType.HERO_12335) > 0 && effect12335Round) {
			hurtVal *= (1 - getEffVal(EffType.HERO_12335) * GsConst.EFF_PER - effect12331AddEscortPoint * getEffVal(EffType.HERO_12336) * GsConst.EFF_PER);
			addDebugLog(" 12335 - 开启聚能环护模式: 使自身受到攻击所承受的伤害减少【{} + {}援护值* {}】 ", getEffVal(EffType.HERO_12335), effect12335AddFirePoint, getEffVal(EffType.HERO_12336));
		}
		
		if (getEffVal(EffType.HERO_12672) > 0 && effect12331AddEscortPoint > ConstProperty.getInstance().effect12672AtkThresholdValue) {
			int effVal = (int) (getEffVal(EffType.HERO_12672) * GsConst.EFF_PER * ConstProperty.getInstance().effect12672SoldierAdjustMap.getOrDefault(atkSoldier.getType(), 0));
			hurtVal = hurtVal * (1 - effVal * GsConst.EFF_PER);
			addDebugLog("-12672 自身攻城车受到伤害减少 {} ", effVal);
		}
		
		if (getEffVal(EffType.HERO_12681) > 0) {
			hurtVal *= (1 - getEffVal(EffType.HERO_12681) * GsConst.EFF_PER);
			addDebugLog(" 【12681】战技持续期间，自身出征数量最多的攻城车受到攻击时，伤害减少 +XX.XX%【12681】 {} ", getEffVal(EffType.HERO_12681));
		}
		
		hurtVal = effect12673ReduceHurt(atkSoldier, hurtVal);
		
		return hurtVal;
	}

	private double effect12673ReduceHurt(BattleSoldier atkSoldier, double hurtVal) {
		if (effect12673Vaule > 0) {
			if (atkSoldier instanceof BattleSoldier_3) {
				BattleSoldier_3 zhishengji = (BattleSoldier_3) atkSoldier;
				if (zhishengji.super303) {
					// - 注：额外隐藏效果，若伤害来源为门多萨作用号【1543】则减伤为XX.XX%（effect12673BaseVaule1543）、作用号【1544】则减伤为XX.XX%（effect12673BaseVaule1544），减伤不会衰减
					return hurtVal;
				}
			}
			hurtVal = hurtVal * (1 - effect12673Vaule * GsConst.EFF_PER);
			addDebugLog(" 12673 - 自身攻城车受到的伤害降低 {}（作用号12673），该减伤效果每次生效效果衰减10.00% ", effect12673Vaule);
			effect12673Vaule -= ConstProperty.getInstance().effect12673BaseVaule;
		}
		return hurtVal;
	}

	@Override
	public int skillAtkExactly(BattleSoldier defSoldier) {
		int result = getEffVal(EffType.CANNON_ATK_CITY_HERT);
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.CANNON_SOLDIER_7_SKILL_2);
		if (BattleConst.WarEff.CITY_ATK.check(getTroop().getWarEff()) && trigerSkill(skill)) {
			result = result + skill.getP1IntVal();
		}
		if (buf1540CENCnt > 0) {
			int cen1540 = Math.min(buf1540CENCnt, ConstProperty.getInstance().getEffect1541Maxinum());
			int atkAdd = cen1540 * getEffVal(EffType.MDWS_1541);
			result = result + atkAdd;
			getTroop().getBattle().addDebugLog("### MBWS兵种:{}  1541 buff 层数{} atkAdd = {}", getSoldierId(), cen1540, atkAdd);
		}

		return result;
	}

	@Override
	public int skillDefExactly(BattleSoldier atkSoldier) {
		int result = super.skillDefExactly(atkSoldier);
		if (getEffVal(EffType.EFF_1455) > 0) {// EFF_1420
			int pct = (int) (getDeadCnt() * 10D / getOriCnt()) + 1;
			pct = Math.min(pct, 5);
			result = pct * getEffVal(EffType.EFF_1455);
		}
		return result + buff74601;
	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.CANNON_SOLDIER_7_SKILL_66;
	}

	@Override
	protected PBSoldierSkill honor10SkillId() {
		return PBSoldierSkill.CANNON_SOLDIER_7_SKILL_34;
	}

	@Override
	public Map<EffType, Integer> getEffMapClientShow() {
		Map<EffType, Integer> result = super.getEffMapClientShow();
		mergeClientShow(result, EffType.HERO_12014, EffType.FIRE_2007);
		return result;
	}

	public boolean isTriggerSkill744() {
		return triggerSkill744;
	}

	public void setTriggerSkill744(boolean triggerSkill744) {
		this.triggerSkill744 = triggerSkill744;
	}

	@Override
	public ArmyInfo calcArmyInfo(double selfCoverRete) {
		ArmyInfo armyInfo = super.calcArmyInfo(selfCoverRete);
		armyInfo.setEff12339Cnt(eff12339Cnt);
		armyInfo.setEff12339Power(eff12339Power);
		return armyInfo;
	}

}
