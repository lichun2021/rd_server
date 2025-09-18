package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.net.http.HawkHttpServer;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.hawk.game.battle.effect.impl.hero1112.Buff12616;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.RandomContent;
import com.hawk.game.util.RandomUtil;

public class BattleSoldier_5 extends IFootSoldier {
	// <data id="50101" des="轻步兵（弓兵)强化剂(连击)1级无需配置数值,加快步兵的攻击速度" />
	boolean trigerSkill5;
	private int atkPct;
	private int attackCount;
	boolean triger1512;
	int dis1512;

	int skill514Cen;
	int skill514CenMax;
	int skill514CenEff;

	int atkcount1562;
	boolean atk1562;
	List<HawkTuple2<Integer, Integer>> effect1562DamageParamList;
	int effect1562DamageParam;

	Map<Integer, Integer> eff1595Debuf;

	int buff1598Cen;
	int debufSkill424ValHP;
	int debufSkill424ValDEF;
	int JIJIA_1321;
	int sss1669Cnt;
	int sss1669CntMax;
	HawkTuple2<Integer, Integer> sss12254Buff = HawkTuples.tuple(0, 0);

	/**蓄能*/
	int sss12201Xuneng;
	/**蓄能炮*/
	boolean sss12202Triger;
	Buff12616 buff12616;
	int skill46Cnt;
	@Override
	public SoldierType getType() {
		return SoldierType.FOOT_SOLDIER_5;
	}

	@Override
	public void roundStart3() {
		hero12202();
	}

	private void hero12202() {
		if (sss12201Xuneng < ConstProperty.getInstance().getEffect12202AtkThresholdValue() || getBattleRound() % ConstProperty.getInstance().getEffect12202AtkRound() != 0) {
			return;
		}
		sss12202Triger = true;
		
		int effect12202AtkTimes = ConstProperty.getInstance().getEffect12202AtkTimes();
		if (getEffVal(EffType.EFF_12312) > HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE)) {
			effect12202AtkTimes = ConstProperty.getInstance().getEffect12312AtkNum();
			addDebugLog("【12312】战技持续期间，触发蓄能炮时，有 XX.XX% 的概率使攻击轮次由 3 轮增至 4 轮");
		}
		for (int i = 0; i < effect12202AtkTimes; i++) {
			List<BattleSoldier> jinzhanList = getTroop().getEnemyTroop().getSoldierList().stream().filter(s -> s.isAlive()).filter(s -> s.isJinZhan()).collect(Collectors.toList());
			if(jinzhanList.isEmpty()){
				jinzhanList = getTroop().getEnemyTroop().getSoldierList().stream().filter(s -> s.isAlive()).filter(s -> s.isYuanCheng()).collect(Collectors.toList());
			}
			for (BattleSoldier tar : jinzhanList) {
				super.attackOnce(tar, QIAN_PAI_MAX, 0, Integer.MAX_VALUE);
			}
		}
		sss12201Xuneng = 0;
		sss12202Triger = false;
	}

	@Override
	public void init(Player player, BattleSoldierCfg soldierCfg, int count, int shadowCnt) {
		super.init(player, soldierCfg, count, shadowCnt);
		if (hasSkill(PBSoldierSkill.SOLDIER_SKILL_514)) {
			BattleSoldierSkillCfg skill_14 = getSkill(PBSoldierSkill.SOLDIER_SKILL_514);
			skill514CenEff = skill_14.getP1IntVal();
			skill514CenMax = NumberUtils.toInt(skill_14.getP2());
		}

	}

	@Override
	public void attackOnce(BattleSoldier defSoldier, int atkTimes, double hurtPer, int atkDis) {
		boolean bfalse = ConstProperty.getInstance().getEffect1321Prob() >= HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE);
		JIJIA_1321 = bfalse ? getEffVal(EffType.JIJIA_1321) : 0;

		attackCount++;
		super.attackOnce(defSoldier, atkTimes, hurtPer, atkDis);
		sss1669Check(defSoldier);
		chuantouAttack(defSoldier);

		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.FOOT_SOLDIER_5_SKILL_5);
		if (skill != null && getHeros().contains(skill.getP1IntVal())) {
			List<BattleSoldier> soldierList = enemyList(defSoldier.getTroop());
			int rate = Math.min(NumberUtils.toInt(skill.getP2()) * soldierList.size(), skill.getTrigger());
			trigerSkill5 = trigerSkill(skill, rate);
			if (trigerSkill5) {
				super.attackOnce(defSoldier, QIAN_PAI_MAX, 0, getAtkDis());
				sss1669Check(defSoldier);
				attackCount++;
			}
		}

		mkxn1562Atk(defSoldier);
		if (getEffVal(EffType.HERO_12611) > 0 && ConstProperty.getInstance().effect12611BaseVaule > HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE)) {
			hero12611(defSoldier);
		}
	}

	private void hero12611(BattleSoldier defSoldier) {
		addDebugLog("###{} 触发爆裂雷弹", getUUID());
		List<RandomContent<BattleSoldier>> objList = defSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isJinZhan).filter(BattleSoldier::isAlive)
				.filter(BattleSoldier::canBeAttack).filter(s -> s != defSoldier)
				.map(s -> RandomContent.create(s, ConstProperty.getInstance().effect12611RoundWeightMap.getOrDefault(s.getType(), 1))).collect(Collectors.toList());
		if (objList.isEmpty()) {
			objList = defSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::canBeAttack).filter(s -> s != defSoldier)
					.map(s -> RandomContent.create(s, ConstProperty.getInstance().effect12611RoundWeightMap.getOrDefault(s.getType(), 1))).collect(Collectors.toList());
		}
		if (objList.isEmpty()) {
			return;
		}
		BattleSoldier randtar = RandomUtil.random(objList).getObj();

		List<BattleSoldier> tarList = defSoldier.getTroop().getSoldierList().stream().filter(s -> s.canBeAttack()).filter(s -> s != defSoldier).filter(s-> s.isJinZhan() == randtar.isJinZhan())
				.sorted(Comparator.comparingInt(BattleSoldier::getxPos)).collect(Collectors.toList());
		int index = tarList.indexOf(randtar);
		Set<BattleSoldier> atkTars = new HashSet<>();
		int effect12611AtkNums = ConstProperty.getInstance().effect12611AtkNums;
		
		for (int i = index; i < effect12611AtkNums + 1 + index; i++) {
			atkTars.add(tarList.get(i % tarList.size()));
		}
		
		for (BattleSoldier tar : atkTars) {
			double reduceHurtPer = 1 - getEffVal(EffType.HERO_12611) * GsConst.EFF_PER;
			addDebugLog("###{} 触发爆裂雷弹 -> {}", getUUID(), tar.getUUID());
			attackOnce(tar, QIAN_PAI_MAX, reduceHurtPer, Integer.MAX_VALUE, false);
		}
		addDebugLog("###{} 触发爆裂雷弹结束", getUUID());
	}
	
	private void mkxn1562Atk(BattleSoldier defSoldier) {
		// 【1562】【万分比】【专属芯片用】突击步兵攻击每命中1次目标后，攒1个豆（若命中目标为主战坦克，可额外攒1个豆），当豆攒至100个时，触发1次额外范围攻击效果；范围攻击效果：对所有【近战部队】造成1次XX%伤害的范围攻击（对坦克部队造成2倍XX%伤害）
		// 注：额外触发的范围攻击不攒豆，触发范围攻击后豆清空至0
		// 注：希伯特贯穿同时打3个目标时，算作命中了3次目标，攒3次豆
		// 注：额外触发的范围攻击在每次攻击结算后单独判定是否触发，若触发则在本次攻击结束后即释放范围攻击
		// 注：额外触发的范围攻击目标选择：若敌方当前存在【近战部队】则取敌方所有【近战部队】为攻击目标，若敌方当前不存在【近战部队】则选取敌方所有【远程部队】为攻击目标；【近战部队】：防御坦克、采矿车、主战坦克、轰炸机（兵种类型 = 1、2、3、8）；【远程部队】：直升机、突击步兵、狙击兵、攻城车（兵种类型 = 4、5、6、7）
		// 注：该作用号变化数值为范围攻击的基础伤害
		// 注：范围攻击的伤害随目标数量变化
		// 注：伤害随目标数量变化数值读取const表，字段effect1562DamageParam；配置格式：数量1_伤害1,数量2_伤害2_......（目标数量在配置1~2之间时取伤害1，在数量2之上时取伤害2，以此类推；伤害数值为万分比）
		// 注：攒豆数量达到XX时，触发范围攻击；该数值读取const表，字段effect1562TimesLimit，配置格式：上限数值（int）
		// 注：对坦克部队造成X倍伤害数值读取const表，字段effect1562DamageUpParam；配置格式：额外伤害倍数_兵种类型1_兵种类型2_......（额外伤害倍数为万分比数值）
		// 注：即范围攻击伤害 = 基础伤害 * 【1562】/10000 * 目标伤害参数值/10000 *（1 + if（坦克，额外伤害倍数/10000，0））
		//
		// 【1563】【万分比】【专属天赋用】触发作用号【1562】效果时，范围攻击的伤害额外增加XX%
		// 注：即范围攻击伤害 = 基础伤害 * 【1562】/10000 * 目标伤害参数值/10000*（1 + if（坦克，额外伤害倍数/10000，0）） * （1 + 【1563】/10000）
		// 注：基础伤害按照被攻击方防御数值分别计算基础伤害数值
		//
		// 【1564】【万分比】突击步兵受到空军（兵种类型 = 3、4）攻击后，受到伤害减少XX%
		// 注：该作用号计算时与【1121】累乘计算；即：受到伤害 = 基础伤害*（1 - 【1121】）*（1 - 【1564】）
		// 注：【1121】投放在突击步兵芯片【扭曲力场】上，概率触发
		if (atkcount1562 < ConstProperty.getInstance().getEffect1562TimesLimit()) {
			return;
		}
		List<BattleSoldier> soldierList = defSoldier.getTroop().getSoldierList().stream()
				.filter(BattleSoldier::canBeAttack)
				.filter(BattleSoldier::isAlive)
				.collect(Collectors.toList());
		List<BattleSoldier> jinzhan = soldierList.stream()
				.filter(BattleSoldier::isJinZhan)
				.collect(Collectors.toList());
		List<BattleSoldier> targetList = jinzhan.isEmpty() ? soldierList : jinzhan;

		getTroop().getBattle().addDebugLog("### 1562. AOE START");
		// 伤害加成计算
		effect1562DamageParam = getEffect1562DamageParam(targetList.size());
		atk1562 = true;
		for (BattleSoldier target : targetList) {
			super.attackOnce(target, QIAN_PAI_MAX, 0, Integer.MAX_VALUE);
			sss1669Check(defSoldier);
		}
		getTroop().getBattle().addDebugLog("### 1562. AOE END");
		atk1562 = false;
		atkcount1562 = 0;
	}

	@Override
	public int skillDefExactly(BattleSoldier atkSoldier) {
		int result = buff1598Cen * getEffVal(EffType.ARMOUR_1598);
		if (atkSoldier.isPlan() && debufSkill424ValDEF > 0) {
			result -= debufSkill424ValDEF;
			getTroop().getBattle().addDebugLog("### 受到飞机攻击防御减少, skill424 debuff {}", debufSkill424ValDEF);
		}
		
		result += buff12615();
		return result;
	}

	@Override
	public int skillHPExactly() {
		int result = buff1598Cen * getEffVal(EffType.ARMOUR_1598);
		result += buff12615();
		return result;
	}

	@Override
	public int skillHPExactly(BattleSoldier atkSoldier) {
		int result = 0;
		if (atkSoldier.isPlan() && debufSkill424ValHP > 0) {
			result -= debufSkill424ValHP;
			getTroop().getBattle().addDebugLog("### 受到飞机攻击血量减少, skill424 debuff {}", debufSkill424ValHP);
		}
		return result;
	}

	private int getEffect1562DamageParam(int soldierCount1562) {
		try {

			if (effect1562DamageParamList == null) {
				effect1562DamageParamList = new ArrayList<>();
				String str = ConstProperty.getInstance().getEffect1562DamageParam();
				List<String> splitToList = new ArrayList<>(Splitter.on(",").splitToList(str));
				Collections.reverse(splitToList);
				for (String nv : splitToList) {
					String[] arr = nv.split("_");
					effect1562DamageParamList.add(HawkTuples.tuple(NumberUtils.toInt(arr[0]), NumberUtils.toInt(arr[1])));
				}
			}

			for (HawkTuple2<Integer, Integer> nv : effect1562DamageParamList) {
				if (soldierCount1562 >= nv.first) {
					getTroop().getBattle().addDebugLog("### 1562. Target num {} damageParam {}", soldierCount1562, nv.second);
					return nv.second;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	@Override
	protected void attackOver(BattleSoldier defSoldier, int killCnt, double hurtVal) {
		buff1598Cen = Math.min(buff1598Cen + 1, 100);
		super.attackOver(defSoldier, killCnt, hurtVal);
		if (skill514CenMax > 0 && getRoundHurtTimes() > 1) {
			skill514Cen++;
			skill514Cen = Math.min(skill514Cen, skill514CenMax);
			getTroop().getBattle().addDebugLog("### 514 技能触发. 伤害层数 {}", skill514Cen);
		}
		if (getEffVal(EffType.HERO_1562) > 0 && !atk1562) {
			atkcount1562++;
			if (defSoldier.getType() == SoldierType.TANK_SOLDIER_2) {
				atkcount1562++;
			}
			atkcount1562 = Math.min(ConstProperty.getInstance().getEffect1562TimesLimit(), atkcount1562);
			getTroop().getBattle().addDebugLog("### 1562. 伤害层数 {}", atkcount1562);
		}

		if (defSoldier.isTank()) {
			BattleSoldierSkillCfg skill524 = getSkill(PBSoldierSkill.SOLDIER_SKILL_524);
			if (trigerSkill(skill524)) {
				int round = getTroop().getBattle().getBattleRound() + skill524.getP1IntVal() - 1;
				int val = NumberUtils.toInt(skill524.getP2());
				HawkTuple2<Integer, Integer> skill624Debuff = HawkTuples.tuple(round, val);
				defSoldier.setSkill624Debuff(skill624Debuff);
				getTroop().getBattle().addDebugLog("### {} skill624 降低 {} 暴击几率 {} 结束回合 {}", getUUID(), defSoldier.getUUID(), skill624Debuff.second, skill624Debuff.first);
			}
		}

		if (defSoldier.isTank() && hasSkill(PBSoldierSkill.FOOT_SOLDIER_5_SKILL_44)) {
			BattleSoldierSkillCfg skill544 = getSkill(PBSoldierSkill.FOOT_SOLDIER_5_SKILL_44);
			int round = getTroop().getBattle().getBattleRound() + NumberUtils.toInt(skill544.getP2()) - 1;
			int val = skill544.getP1IntVal();
			HawkTuple2<Integer, Integer> skill544Debuff = HawkTuples.tuple(round, val);
			defSoldier.setSkill544Debuff(skill544Debuff);
			getTroop().getBattle().addDebugLog("### {} skill544   {} 受到伤害+ {} 结束回合 {}", getUUID(), defSoldier.getUUID(), val, round);
		}
		if (defSoldier.getType() != SoldierType.TANK_SOLDIER_1 && getEffVal(EffType.HERO_12201) > 0 && !atk1562 && !sss12202Triger) {
			int add = ConstProperty.getInstance().getEffect12201BasePoint() + (defSoldier.isDianran() ? ConstProperty.getInstance().getEffect12201ExtraPoint() : 0);
			sss12201Xuneng += add;
			sss12201Xuneng = Math.min(sss12201Xuneng, ConstProperty.getInstance().getEffect12201MaxPoint());
			getTroop().getBattle().addDebugLog("### {} 12201 :  {}", defSoldier.getUUID(), sss12201Xuneng);
		}
		// 【12205】磁暴干扰：蓄能炮命中处于【损坏状态】的单位时，使其【损坏状态】下的减益效果额外 +XX.XX%
		if (getEffVal(EffType.HERO_12205) > 0 && defSoldier.eff12163DebuffVal().second > 0 && sss12202Triger) {
			defSoldier.setEff12163DebuffExtry(getEffVal(EffType.HERO_12205));
			addDebugLog("###{} 使其【损坏状态】12205 下的减益效果额外 +{} ", defSoldier.getUUID(), getEffVal(EffType.HERO_12205));
		}
		if (getEffVal(EffType.HERO_12206) > 0 && defSoldier.isTank() && sss12202Triger) {
			defSoldier.addDebuff12206Val(getEffVal(EffType.HERO_12206));
			addDebugLog("###{} 12206 降低其  {} 的暴击几率 ", defSoldier.getUUID(), defSoldier.getDebuff12206Val());
		}
		if (getEffVal(EffType.HERO_12611) > 0 ){
			defSoldier.addSorek12611Debuff(this);
		}
		
		skill54601Check(defSoldier);
	}

	private void skill54601Check(BattleSoldier defSoldier) {
		BattleSoldierSkillCfg scfg = getSkill(PBSoldierSkill.SOLDIER_SKILL_54601);
		if (scfg.getP1().contains(defSoldier.getType().getNumber() + "")) {
			if (skill46Cnt != 0 && skill46Cnt % scfg.getP3IntVal() == 0) {
				additionalAtk(defSoldier, scfg.getP2IntVal(), true, true, "###【54601】 每 X 次攻击后目标兵种后，下一次攻击额外造成+XX%伤害（多个突击单位触发，额外伤害为自身攻击伤害倍率");
			} 
			skill46Cnt++;
		}
	}

	/**
	 * 突击步兵（兵种ID:5）攻击时，有3x%/2x%/x%的概率对非当前目标再攻击一次（近战优先），每触发一次会降低一定概率x%
	effectid：1669， 每次攻击降低概率为 1/3*effectid：1669
	每次攻击时进行一次判定，且本次的额外攻击不会再进行能否连击的判定（即不会触发额外攻击）。
	优先攻击群组1：兵种ID:2 主战坦克。 兵种ID3:轰炸机。兵种ID8:采矿车。
	优先攻击群组2：兵种ID:4 直升机。 兵种ID5:突击步兵。兵种ID6:狙击兵。兵种ID:攻城车。
	对方有群组1的兵种时，在所有群组1内随机选择部队进行攻击。当没有群组1时，在群组2中随机选择部队进行攻击。当群组1和2都不存在时，攻击防御坦克 兵种ID：1
	 */
	private void sss1669Check(BattleSoldier defSoldier) {
		int effVal1669 = getEffVal(EffType.HERO_1669);
		if (effVal1669 <= 0 || sss1669Cnt >= sss1669CntMax) {
			return;
		}
		int trigger1669per = effVal1669;// - effVal1669 * sss1669Cnt / 3;
		if (trigger1669per < HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE)) {
			return;
		}

		BattleSoldier target = null;
		List<BattleSoldier> tarList = new ArrayList<>();
		for (BattleSoldier tar : defSoldier.getTroop().getSoldierList()) {
			if (tar != defSoldier && tar.canBeAttack() && tar.isAlive()) {
				tarList.add(tar);
			}
		}

		if (target == null) {
			for (BattleSoldier tar : tarList) {
				SoldierType tartype = tar.getType();
				if (tartype == SoldierType.TANK_SOLDIER_2 || tartype == SoldierType.PLANE_SOLDIER_3 || tartype == SoldierType.CANNON_SOLDIER_8) {
					target = tar;
					break;
				}
			}
		}
		if (target == null) {
			for (BattleSoldier tar : tarList) {
				SoldierType tartype = tar.getType();
				if (tartype == SoldierType.PLANE_SOLDIER_4 || tartype == SoldierType.FOOT_SOLDIER_5 || tartype == SoldierType.FOOT_SOLDIER_6
						|| tartype == SoldierType.CANNON_SOLDIER_7) {
					target = tar;
					break;
				}
			}
		}
		if (target == null) {
			for (BattleSoldier tar : tarList) {
				SoldierType tartype = tar.getType();
				if (tartype == SoldierType.TANK_SOLDIER_1) {
					target = tar;
					break;
				}
			}
		}
		if (target == null) {
			target = defSoldier;
		}

		if (Objects.nonNull(target)) {
			sss1669Cnt++;
			getTroop().getBattle().addDebugLog("### 阿尔托莉雅 {} 1669 连续攻击 目标 :{} 概率 {}", getUUID(), target.getUUID(), trigger1669per);
			super.attackOnce(target, QIAN_PAI_MAX, 0, Integer.MAX_VALUE);
			if (getEffVal(EffType.EFF_12254) > 0 && sss12254Buff.first < ConstProperty.getInstance().getEffect12254Maxinum()) {
				sss12254Buff = HawkTuples.tuple(sss12254Buff.first + 1, sss12254Buff.second + getEffVal(EffType.EFF_12254));
				addDebugLog("【12254】突击步兵越过防御坦克的额外攻击，每成功发动 {} 次，自身受到伤害减少 +{}%", sss12254Buff.first,sss12254Buff.second);
			}
		}
	}

	@Override
	protected double getHurtVal(BattleSoldier defSoldier, double reducePer) {
		double result = super.getHurtVal(defSoldier, reducePer);
		if (triger1512) {
			final int effect1512DisDecayCof = ConstProperty.getInstance().getEffect1512DisDecayCof();
			final int effect1512DisDecayMin = ConstProperty.getInstance().getEffect1512DisDecayMin();
			result = result * GsConst.EFF_PER * getEffVal(EffType.HERO_1512);
			result = result * GsConst.EFF_PER * Math.max(GsConst.EFF_RATE - dis1512 * effect1512DisDecayCof, effect1512DisDecayMin);
			result = result * (1 + getEffVal(EffType.HERO_1513) * GsConst.EFF_PER);
		}
		if (atk1562) {
			int effect1562Up = 0;
			if (defSoldier.getType() == SoldierType.TANK_SOLDIER_1 || defSoldier.getType() == SoldierType.TANK_SOLDIER_2) {
				effect1562Up = ConstProperty.getInstance().getEffect1562DamageUpParam();
			}
			result = result * (GsConst.EFF_PER * getEffVal(EffType.HERO_1562))
					* (GsConst.EFF_PER * effect1562DamageParam)
					* (1 + GsConst.EFF_PER * effect1562Up)
					* (1 + GsConst.EFF_PER * getEffVal(EffType.HERO_1563));
		}
		
		if (sss12202Triger) {
			result = result * GsConst.EFF_PER * ConstProperty.getInstance().getEffect12202DamageAdjustMap().getOrDefault(defSoldier.getType(), 0)
					* (getEffVal(EffType.HERO_12202) + sss12201Xuneng * getEffVal(EffType.HERO_12203)) * GsConst.EFF_PER;
			getTroop().getBattle().addDebugLog("### {} 12202 hurt :  {}", defSoldier.getUUID(), result);
		}
		if (defSoldier.getSorek12611DebuffVal(this) > 0) {
			int debuffVal = defSoldier.getSorek12611DebuffVal(this);
			if (defSoldier.isTank()) {
				debuffVal += ConstProperty.getInstance().effect12612BaseVaule;
			}
			debuffVal = (int) (debuffVal* GsConst.EFF_PER * ConstProperty.getInstance().effect12612SoldierAdjustMap.getOrDefault(defSoldier.getType(), 10000));
			result = result * (1 + debuffVal * GsConst.EFF_PER);
			addDebugLog("【12612】加深效应：【雷感状态】的单位，受到自身突击步兵的伤害增加 {}", debuffVal);
		}

		return result;
	}

	private void chuantouAttack(BattleSoldier defSoldier) {
		// 【1512】【万分比】突击步兵（兵种类型 = 5）攻击时，有概率造成贯穿效果，对攻击目标后排至多2个其他目标造成贯穿伤害XX%（贯穿伤害数值随目标站位距离衰减）
		// 注：对应概率读取const表，字段effect1512Prob【万分比】
		// 注：若攻击的目标的后排只有1个目标，则只对该目标造成贯穿伤害，若攻击的目标的后排没有其他目标，则没有贯穿伤害效果（不同玩家、不同等级、不同兵种均视为独立目标）
		// 注：记攻击的前排目标为A，站位格子为S1，其后排目标为B，站位格子为S2，则两个兵种的 站位距离 = abs（S1-S2）
		// 站位距离衰减 = max（1 - 站位距离*站位距离衰减系数，最低衰减值）
		// 站位距离衰减系数读取const表，字段effect1512DisDecayCof【万分比】
		// 最低衰减值读取const表，字段effect1512DisDecayMin【万分比】
		// 则对B造成的伤害 = 基础伤害（按照B的防御值计算）*【1512】* 站位距离衰减
		// 注：该作用号用于新英雄的专属芯片效果
		if (getEffVal(EffType.HERO_1512) > 0 && HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < ConstProperty.getInstance().getEffect1512Prob()) {
			triger1512 = true;
			// 防守兵
			List<BattleSoldier> soldierList = defSoldier.getTroop().getSoldierList();
			int atkcount = 0;
			for (BattleSoldier defsold : soldierList) {
				if (defSoldier == defsold || defsold.getxPos() < defSoldier.getxPos() || !defsold.canBeAttack() || !defsold.isAlive()) {
					continue;
				}
				dis1512 = defsold.getxPos() - defSoldier.getxPos();
				super.attackOnce(defsold, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
				sss1669Check(defSoldier);
				atkcount++;
				if (atkcount == 2) {
					break;
				}
			}

		}
		triger1512 = false;
	}

	@Override
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		hurtVal = super.reduceHurtValPct(atkSoldier, hurtVal);
		int eff1121 = getEffVal(EffType.DOLDIER_1121);
		if (eff1121 > 0) {
			int triger1121Add = 0;
			if (getEffVal(EffType.ARMOUR_11010) > 0) {// EFF_1420
				int pct = (int) (getDeadCnt() * 20D / getOriCnt());
				pct = Math.min(pct, ConstProperty.getInstance().getEffect11010TimesLimit());
				triger1121Add = pct * getEffVal(EffType.ARMOUR_11010);
				getTroop().getBattle().addDebugLog("### {} ARMOUR_11010 {}层 1121概率提升 ", getUUID(), pct);
			}
			if (HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < ConstProperty.getInstance().getEffect1121Prob() + triger1121Add) {
				eff1121 = Math.min(eff1121, 10000);
				hurtVal *= (1 - eff1121 * GsConst.EFF_PER);
			}
		}
		if(getEffVal(EffType.HERO_12207)>0 && atkSoldier.isPlan()){
			long dianrancont = atkSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(so -> so.eff12163DebuffVal().second > 0).count();
			dianrancont = Math.min(ConstProperty.getInstance().getEffect12208MaxNum(), dianrancont);
			hurtVal *= (1 - getEffVal(EffType.HERO_12207) * GsConst.EFF_PER - dianrancont * getEffVal(EffType.HERO_12208) * GsConst.EFF_PER);
			getTroop().getBattle().addDebugLog("### {} 12207受到空军单位攻击时，伤害减少  {} + {} * {} ", getUUID(), getEffVal(EffType.HERO_12207), dianrancont, getEffVal(EffType.HERO_12208));
		}
		hurtVal *= (1 - getEffVal(EffType.HERO_12211) * GsConst.EFF_PER);
		hurtVal *= (1 - sss12254Buff.second * GsConst.EFF_PER);
		hurtVal *= (1 - buff12616Val(atkSoldier) * GsConst.EFF_PER);
		hurtVal *= (1 - buff12617(atkSoldier) * GsConst.EFF_PER);
		return hurtVal;
	}

	@Override
	protected void attacked(BattleSoldier attacker, int deadCnt) {
		super.attacked(attacker, deadCnt);
		buff12616Attacked(attacker);
	}

	private List<BattleSoldier> enemyList(BattleTroop enemyTroop) {
		// 防守兵
		List<BattleSoldier> soldierList = enemyTroop.getSoldierList();

		List<BattleSoldier> planList = soldierList.stream()
				.filter(ds -> ds.canBeAttack())
				.filter(ds -> ds.isAlive())
				.filter(ds -> withinAtkDis(ds))
				.collect(Collectors.toList());
		return planList;
	}

	@Override
	public void roundStart() {
		super.roundStart();
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.FOOT_SOLDIER_5_SKILL_1);
		if (Objects.nonNull(skill)) {
			atkPct += skill.getP1IntVal();
		}
		atkPct += getEffVal(EffType.SOLDIER5_ATK_SPEED);

		trigerSkill5 = false;
		sss1669Cnt = 0;
		sss1669CntMax =3;
		if (getEffVal(EffType.EFF_12251) >= HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE)) {
			sss1669CntMax = ConstProperty.getInstance().getEffect12251AtkTimes();
			getTroop().getBattle().addDebugLog("### {} 【12251】突击步兵越过防御坦克的额外攻击，有 XX.XX% 概率将最大发动次数由 3 次增至 4 次 ", getUUID());
		}
		buff12616Check();
	}

	private void buff12616Check() {
		if (getEffVal(EffType.HERO_12616) == 0) {
			return;
		}
		if (buff12616 == null) {
			buff12616 = new Buff12616();
		}
		if (buff12616 != null) {
			buff12616.roundCnt.clear();
		}
		if (getBattleRound() % ConstProperty.getInstance().effect12616AtkRound == 0) {
			buff12616.round = ConstProperty.getInstance().effect12616ContinueRound - 1 + getBattleRound();
		}
	}
	
	private int buff12616Val(BattleSoldier atkSoldier){
		if(buff12616 == null ||buff12616.round < getBattleRound()){
			return 0;
		}
		int val =(int) (getEffVal(EffType.HERO_12616) + buff12616.defCnt.get(atkSoldier.getType()) * ConstProperty.getInstance().effect12616BaseVaule);
		return (int) (val * GsConst.EFF_PER * ConstProperty.getInstance().effect12616SoldierAdjustMap.getOrDefault(atkSoldier.getType(), 10000));
	}
	
	private void buff12616Attacked(BattleSoldier attacker) {
		if (buff12616 == null) {
			return;
		}
		if (buff12616.roundCnt.get(attacker.getType()) >= ConstProperty.getInstance().effect12616MaxTimes) {
			return;
		}
		if (buff12616.defCnt.get(attacker.getType()) < ConstProperty.getInstance().effect12616Maxinum) {
			long cen = buff12616.defCnt.incrementAndGet(attacker.getType());
			addDebugLog("###{} 【12616】感应防域 对伤害来源做出应对措施 {} 层", getUUID(), cen);
		}
	}

	@Override
	public int atkTimes(int round) {
		int atkTimes = super.atkTimes(round);
		int extrAtk = 0;
		while (atkPct > GsConst.RANDOM_MYRIABIT_BASE) {
			extrAtk += 1;
			atkPct -= GsConst.RANDOM_MYRIABIT_BASE;
			getTroop().getBattle().addDebugLog("### {} 1069 连续攻击 ", getUUID());
		}

		BattleSoldierSkillCfg skill4 = getSkill(PBSoldierSkill.FOOT_SOLDIER_5_SKILL_4);
		if (trigerSkill(skill4, EffType.SOLDIER_SKILL_TRIGER_504)) {
			getTroop().getBattle().addDebugLog("### {} FOOT_SOLDIER_5_SKILL_4 连续攻击 ", getUUID());
			extrAtk = extrAtk + 1;
		}
		if (getEffVal(EffType.HERO_1638) >= HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE)) {
			getTroop().getBattle().addDebugLog("### {} 1638 连续攻击 ", getUUID());
			extrAtk = extrAtk + 1;
		}
		incExtrAtktimes(extrAtk);
		return atkTimes + extrAtk;
	}

	@Override
	public int skillIgnoreTargetHP(BattleSoldier target) {
		int result = super.skillIgnoreTargetHP(target);
		result += JIJIA_1321;
		return result;
	}

	@Override
	public int skillIgnoreTargetDef(BattleSoldier target) {
		int val = 0;
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.FOOT_SOLDIER_5_SKILL_2);
		boolean isNotTank = !skill.getP2().contains(target.getSoldierCfg().getId() + "");
		if (isNotTank && trigerSkill(skill)) {
			val = skill.getP1IntVal();
		}
		val += getEffVal(EffType.EFF_1422);
		return super.skillIgnoreTargetDef(target) + val;
	}

	@Override
	public int skillHurtExactly(BattleSoldier defSoldier) {
		int result = super.skillHurtExactly(defSoldier);
		if (trigerSkill5) {
			BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.FOOT_SOLDIER_5_SKILL_5);

			result += ((NumberUtils.toInt(skill.getP4()) - GsConst.EFF_RATE) + getEffVal(EffType.SOLDIER_SKILL_505_1089));
		}

		// <data id="50103"
		// des="轻步兵（弓兵)精准射击(专注)1级trigger=触发概率;damage=伤害参数;targetType=目标类型,有一定几率造成额外伤害"
		// trigger="2000" damage="1000" />
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.FOOT_SOLDIER_5_SKILL_3);
		if (trigerSkill(skill, EffType.SOLDIER_SKILL_TRIGER_503)) {
			result = result + skill.getP1IntVal() + getEffVal(EffType.SOLDIER_SKILL_503);
		}
		BattleSoldierSkillCfg skill7 = getSkill(PBSoldierSkill.FOOT_SOLDIER_5_SKILL_7);
		if (trigerSkill(skill7)) {
			result = result + skill7.getP1IntVal() + getEffVal(EffType.SOLDIER_SKILL_507_P1);
		}
		result += skill514Cen * skill514CenEff;
		getTroop().getBattle().addDebugLog("### skill 514 伤害层数 {} 伤害提升 {}", skill514Cen, skill514Cen * skill514CenEff);

		if (getEffVal(EffType.ARMOUR_11009) > 0) {// EFF_1420
			int pct = (int) (getDeadCnt() * 20D / getOriCnt());
			pct = Math.min(pct, ConstProperty.getInstance().getEffect11009TimesLimit());
			result = result + pct * getEffVal(EffType.ARMOUR_11009);
			getTroop().getBattle().addDebugLog("### {} ARMOUR_11009 {} 层伤害提升 ", getUUID(), pct);
		}
		if (defSoldier.isJinZhan()) {// 突击步兵攻击近战部队时，伤害+XX%
			BattleSoldierSkillCfg skill_241 = getSkill(PBSoldierSkill.SOLDIER_SKILL_54101);
			result += skill_241.getP1IntVal();
		}
		
		if (sss12202Triger && getEffVal(EffType.HERO_12204) > 0 && defSoldier.eff12163DebuffVal().second > 0) {
			result += getEffVal(EffType.HERO_12204);
			addDebugLog("###{} 处于【损坏状态】时，12204 伤害额外 +{} ", defSoldier.getUUID(), getEffVal(EffType.HERO_12204));
		}
		if (getEffVal(EffType.EFF_12311) > 0) {
			int adjust = ConstProperty.getInstance().getEffect12311DamageAdjustMap().getOrDefault(defSoldier.getType(), 0);
			double add = getEffVal(EffType.EFF_12311) * GsConst.EFF_PER * adjust;
			result += add;
			addDebugLog("###{} 12311】出征或驻防时，突击步兵额外伤害 +XX.XX% +{} ", getUUID(), add);
		}
		return result;
	}

	@Override
	public int skillAtkExactly(BattleSoldier defSoldier) {
		int result = 0;
		if (getEffVal(EffType.EFF_1453) > 0) {
			result += getEffVal(EffType.EFF_1453) * Math.min(100, attackCount);
		}

		result -= debuff1595Val();
		result += buff12615();
		return result;
	}

	private int buff12615() {
		if(getEffVal(EffType.HERO_12615) == 0){
			return 0;
		}
		int hero12615Cen = Math.min(getBattleRound() / ConstProperty.getInstance().effect12615AtkRound, ConstProperty.getInstance().effect12615Maxinum);
		if(hero12615Cen == 0){
			return 0;
		}
		int result = (ConstProperty.getInstance().effect12615BaseVaule + getEffVal(EffType.HERO_12615) + getEffVal(EffType.HERO_12632)) * hero12615Cen;
		return result;
	}
	
	private int buff12617(BattleSoldier atk){
		if(getEffVal(EffType.HERO_12617) == 0){
			return 0;
		}
		int hero12617Cen = Math.min(getBattleRound() / ConstProperty.getInstance().effect12615AtkRound, ConstProperty.getInstance().effect12617Maxinum);
		int result = (int) (hero12617Cen * getEffVal(EffType.HERO_12617) * GsConst.EFF_PER * ConstProperty.getInstance().effect12617SoldierAdjustMap.getOrDefault(atk.getType(), 10000));
		addDebugLog("### 【12617】个人战时，每次触发磁流强化时，自身突击步兵受到的伤害减少 {}", result);
		return result;
	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.FOOT_SOLDIER_5_SKILL_66;
	}

	public void add1595Debuff(int debufval) {
		if (Objects.isNull(eff1595Debuf)) {
			eff1595Debuf = new HashMap<>();
		}
		int round = getTroop().getBattle().getBattleRound();
		eff1595Debuf.merge(round, debufval, (v1, v2) -> Math.max(v1, v2));

		getTroop().getBattle().addDebugLog("### Add 1595 debuf {}", eff1595Debuf.get(round));
	}

	public int debuff1595Val() {
		if (Objects.isNull(eff1595Debuf)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		return Math.max(eff1595Debuf.getOrDefault(round - 1, 0), eff1595Debuf.getOrDefault(round, 0));
	}

	@Override
	public boolean isFoot() {
		return true;
	}

	@Override
	public void incrDebufSkill424Val(int valhp, int valdef, int maxCen) {
		debufSkill424ValHP = Math.min(debufSkill424ValHP + valhp, valhp * maxCen);
		debufSkill424ValDEF = Math.min(debufSkill424ValDEF + valdef, valdef * maxCen);
		getTroop().getBattle().addDebugLog("### {} Add skill 424 debuf hp {} def {}", getUUID(), debufSkill424ValHP, debufSkill424ValDEF);
	}

	@Override
	protected PBSoldierSkill honor10SkillId() {
		return PBSoldierSkill.FOOT_SOLDIER_5_SKILL_34;
	}

	@Override
	public Map<EffType, Integer> getEffMapClientShow() {
		Map<EffType, Integer> result = super.getEffMapClientShow();
		mergeClientShow(result, EffType.HERO_12014, EffType.FIRE_2005);
		return result;
	}
}
