package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkWeightFactor;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.impl.Checker1524;
import com.hawk.game.battle.effect.impl.Checker1525;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.item.RandomItem;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.RandomContent;
import com.hawk.game.util.RandomUtil;
import com.sun.org.apache.xml.internal.security.c14n.CanonicalizerSpi;

public class BattleSoldier_6 extends IFootSoldier {
	// <data id="60101" des="重型步兵（弩兵)弹药强化(强击)1级无需配置数值,大幅度提升重型步兵的攻击力" />
	// <data id="60102" des="重型步兵（弩兵)枪管强化(瞄准)1级无需配置数值,较大地提高射程" />

	// 技能释放回合
	int trigerSkill5Round;
	boolean isSkill5Round;
	Set<Integer> eff1313DodgeRound = new HashSet<>();
	Map<Integer, Integer> eff1595Debuf;
	int eff1617Cen = 0;
	int debufSkill424ValHP;
	int debufSkill424ValDEF;
	List<HawkTuple3<Integer, BattleSoldier, Double>> sss1670Records;
	int skill1083p7 = 2;
	boolean eff12361Atk;
	boolean eff12363Atk;
	private int skill46Cnt;
	private List<BattleSoldier> skill46Targes = new ArrayList<>();
	@Override
	public SoldierType getType() {
		return SoldierType.FOOT_SOLDIER_6;
	}

	@Override
	public void roundStart() {
		super.roundStart();
		isSkill5Round = trigerSkill5Round == getTroop().getBattle().getBattleRound();
		isSkill5Round = isSkill5Round || eff1617Cen >= ConstProperty.getInstance().getEffect1617TimesLimit();
		sss1670Debuf();
		skill64601Atk();
	}

	@Override
	public void attackOnce(BattleSoldier defSoldier, int atkTimes, double hurtPer, int atkDis) {
		if (getEffVal(EffType.HERO_12361) == 0) {
			if (getEffVal(EffType.HERO_1616) <= 0) {
				BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.FOOT_SOLDIER_6_SKILL_5);
				boolean hasHero = getHeros().contains(NumberUtils.toInt(skill.getP2()));
				if (hasHero && !isSkill5Round && atkTimes < 1 && trigerSkill(skill, EffType.EFF_1424)) {
					getTroop().getBattle().addDebugLog("触发技能本回合不攻击 {}", skill);
					trigerSkill5Round = getTroop().getBattle().getBattleRound() + 1;
					return;
				}
			}

			if ((isSkill5Round && atkTimes < 1) || HawkRand.randInt(10000) < getEffVal(EffType.HERO_1673)) { // 如果是技能回合首次攻击
				defSoldier = getAssaultSoldier(defSoldier);
			}
		}
		
		if (!eff12361Atk(defSoldier)) {
			super.attackOnce(defSoldier, atkTimes, hurtPer, atkDis);
		}

		if (HawkRand.randInt(10000) < getEffVal(EffType.SOLDIER_1506)) {
			super.attackOnce(defSoldier, QIAN_PAI_MAX, 0, atkDis); // 不触发千排
		}
		
	}

	private boolean eff12361Atk(BattleSoldier defSoldier) {
		try {
			boolean trigger = getEffVal(EffType.HERO_12361) > 0
					&& HawkRand.randInt(10000) < ConstProperty.getInstance().getEffect12361BasePro() + getEffVal(EffType.HERO_1673) + getEffVal(EffType.HERO_12381);
			if (!trigger) {
				return false;
			}

			List<BattleSoldier> sols = defSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::canBeAttack)
					.collect(Collectors.toList());

			boolean has8 = defSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(s -> s.getType() == SoldierType.CANNON_SOLDIER_8).findAny().isPresent();
			int max12365 = !has8 ? 0
					: defSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(s -> s.getType() == SoldierType.FOOT_SOLDIER_6)
							.mapToInt(s -> s.getEffVal(EffType.HERO_12365)+ s.getEffVal(EffType.HERO_12384))
							.max().orElse(0);
			if (HawkRand.randInt(10000) < max12365) {
				sols = sols.stream().filter(s -> s.getType() != SoldierType.FOOT_SOLDIER_6).collect(Collectors.toList());
				addDebugLog("【12365】战车掩护: 若存在友军攻城车单位，狙击兵有 {} （友军出征攻城车数量在此处计算时最多取 100 万）的概率不被定点猎杀选中为目标", max12365);
			}
			
			if (sols.isEmpty()) {
				return false;
			}

			HawkWeightFactor<BattleSoldier> hf = new HawkWeightFactor<>();
			ImmutableMap<SoldierType, Integer> effect12361TargetWeightMap = ConstProperty.getInstance().getEffect12361TargetWeightMap();
			for (BattleSoldier so : sols) {
				int weight = effect12361TargetWeightMap.getOrDefault(so.getType(), 0);
				if (weight > 0) {
					hf.addWeightObj(weight, so);
				}
			}
			BattleSoldier result = null;
			try {
				result = hf.randomObj();
			} catch (Exception e) {
			}
			if (null == result) {
				result = HawkRand.randomObject(sols);
			}
			eff12361Atk = true;
			super.attackOnce(result, QIAN_PAI_MAX, 0, Integer.MAX_VALUE);
			hero12362Check(result);
			eff12361Atk = false;

			hero12363Check(result);
			return true;

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	@Override
	public int skillIgnoreTargetHP(BattleSoldier target) {
		int result = super.skillIgnoreTargetHP(target);
		result += getEffVal(EffType.ARMOUR_1597);
		return result;
	}

	@Override
	public int skillDefExactly(BattleSoldier atkSoldier) {
		int result = 0;
		if (atkSoldier.isPlan() && debufSkill424ValDEF > 0) {
			result -= debufSkill424ValDEF;
			getTroop().getBattle().addDebugLog("### 受到飞机攻击防御减少, skill424 debuff {}", debufSkill424ValDEF);
		}
		return result;
	}

	@Override
	public int skillHPExactly(BattleSoldier atkSoldier) {
		int result = 0;
		if (atkSoldier.isPlan() && debufSkill424ValHP > 0) {
			result -= debufSkill424ValHP;
			getTroop().getBattle().addDebugLog("### 受到飞机攻击血量减少, skill424 debuff {}", debufSkill424ValHP);
		}
		if (getEffVal(EffType.ARMOUR_11008) > 0 && !hasHouPai()) {
			result += getEffVal(EffType.ARMOUR_11008);
			getTroop().getBattle().addDebugLog("### {} ARMOUR_11008,没后排 {}", getUUID(), getEffVal(EffType.ARMOUR_11008));
		}
		return result;
	}

	@Override
	protected int skillDogeEffval(BattleSoldier target) {
		if (eff1313DodgeRound.remove(getTroop().getBattle().getBattleRound())) {
			getTroop().getBattle().addDebugLog("###effect 1313 百分百闪避攻击 Soldier={} , Attacker={} ",
					getUUID(), target.getUUID());
			return GsConst.RANDOM_MYRIABIT_BASE;
		}

		int first = super.skillDogeEffval(target);
		if (isSkill5Round && target.isPlan()) {
			first += getEffVal(EffType.HERO_1619);
			getTroop().getBattle().addDebugLog("###Soldier={} , Attacker={} [精准射击]状态闪避攻击效果增加 至 {}",
					getSoldierId(), target.getSoldierId(), first);
		}

		return first;
	}

	@Override
	protected void attackOver(BattleSoldier defSoldier, int killCnt, double hurtVal) {
		super.attackOver(defSoldier, killCnt, hurtVal);
		hero1313Check();
		hero1617Check();
		sss1670(defSoldier, killCnt, hurtVal);
		skill64601Check(defSoldier);
	}

	private void skill64601Check(BattleSoldier defSoldier) {
		BattleSoldierSkillCfg scfg = getSkill(PBSoldierSkill.SOLDIER_SKILL_64601);
		if (scfg.getP1().contains(defSoldier.getType().getNumber() + "")) {
			skill46Cnt++;
			if (skill46Cnt != 0 && skill46Cnt % scfg.getP3IntVal() == 0) {
				skill46Targes.add(defSoldier);
			}
		}
	}
	
	private void skill64601Atk() {
		BattleSoldierSkillCfg scfg = getSkill(PBSoldierSkill.SOLDIER_SKILL_64601);
		for (BattleSoldier defSoldier : skill46Targes) {
			additionalAtk(defSoldier, scfg.getP2IntVal(), true, true, "###【64601】 每 X 次攻击后目标兵种后，下次攻击标记目标，使目标下一回合开始时受到一次+XX%伤害");
		}
		skill46Targes.clear();
	}

	private void hero12363Check(BattleSoldier defSoldier) {
		List<RandomContent<BattleSoldier>> objList = defSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::canBeAttack).filter(s -> s != defSoldier)
				.map(s -> RandomContent.create(s, ConstProperty.getInstance().getEffect12363TargetWeightMap().getOrDefault(s.getType(), 0))).collect(Collectors.toList());
		int effect12363TargetNum = ConstProperty.getInstance().getEffect12363TargetNum();
		if(HawkRand.randInt(10000) < getEffVal(EffType.HERO_12383)){
			effect12363TargetNum +=  ConstProperty.getInstance().getEffect12383AddNum();
		}
		objList = RandomUtil.randomWeightObject(objList, effect12363TargetNum);
		for(RandomContent<BattleSoldier> sc: objList){
			addDebugLog("【12363】爆裂弹片: 定点猎杀命中敌方单位后，额外对其附近随机 2 个敌方单位产生 1 次爆裂伤害");
			eff12363Atk = true;
			super.attackOnce(sc.getObj(), QIAN_PAI_MAX, 0, Integer.MAX_VALUE);
			eff12363Atk = false;
		}
	}

	private void hero12362Check(BattleSoldier defSoldier) {
		int eff12362 = getEffVal(EffType.HERO_12362);
		if (eff12362 == 0|| !eff12361Atk) {
			return;
		}
		if( HawkRand.randInt(10000) > ConstProperty.getInstance().getEffect12362BasePro()){
			return;
		}
		double val = eff12362 * ConstProperty.getInstance().getEffect12362AdjustMap().getOrDefault(defSoldier.getType(), 10000) * GsConst.EFF_PER
				* (1 + getEffVal(EffType.HERO_12382) * GsConst.EFF_PER);
		HawkTuple2<Integer, Double> debuff = HawkTuples.tuple(getBattleRound() + ConstProperty.getInstance().getEffect12362ContinueRound()-1, val);
		defSoldier.setSkill12362Debuff(debuff);
	}

	/**
	 * 狙击步枪改造
	狙击步兵（兵种ID:6）攻击时使敌人受到持续伤害，每回合承受本次伤害的xx%（effectid:1670），持续2回合，对单一目标可以叠加。
	说明：在第一回合攻击的敌人，第2/3回合会造成持续伤害，伤害等于第一回合造成实际伤害的xx%（effectid:1670）
	 */
	private void sss1670(BattleSoldier defSoldier, int killCnt, double hurtVal) {
		int effVal1670 = getEffVal(EffType.HERO_1670);
		if (effVal1670 <= 0 || getTroop().getBattle().getBattleRound() < 2) {
			return;
		}
		if (Objects.isNull(sss1670Records)) {
			sss1670Records = new LinkedList<>();
		}

		HawkTuple3<Integer, BattleSoldier, Double> atk = HawkTuples.tuple(getTroop().getBattle().getBattleRound(), defSoldier, hurtVal);
		sss1670Records.add(atk);
	}

	private void sss1670Debuf() {
		try {
			if (sss1670Records == null || sss1670Records.isEmpty()) {
				return;
			}
			ListIterator<HawkTuple3<Integer, BattleSoldier, Double>> it = sss1670Records.listIterator();
			final int round = getTroop().getBattle().getBattleRound();
			while (it.hasNext()) {
				HawkTuple3<Integer, BattleSoldier, Double> atk = it.next();
				BattleSoldier defSoldier = atk.second;
				if (atk.first == round) {
					continue;
				}
				if (atk.first < round - skill1083p7 || !defSoldier.isAlive()) {
					it.remove();
					continue;
				}

				double hurtVal = atk.third * GsConst.EFF_PER * getEffVal(EffType.HERO_1670);
				hurtVal = hurtVal * (1 + getEffVal(EffType.EFF_12252) * GsConst.EFF_PER);
				hurtVal = hurtVal * (1 + getEffVal(EffType.HERO_12364) * GsConst.EFF_PER);
				hurtVal = defSoldier.forceField(this, hurtVal);
				int curCnt = defSoldier.getFreeCnt();
				int maxKillCnt = (int) Math.ceil(4.0f * hurtVal / defSoldier.getHpVal(this));
				maxKillCnt = Math.max(1, maxKillCnt);
				int killCnt = Math.min(maxKillCnt, curCnt);

				defSoldier.addDeadCnt(killCnt);
				addKillCnt(defSoldier, killCnt);
				getTroop().getBattle().addDebugLog("### 阿尔托莉雅 触发1670 击杀 {} count{} hurtVal {}", defSoldier.getUUID(), killCnt, hurtVal);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void hero1313Check() {
		boolean bfalse = getEffVal(EffType.SOLIDER_6_1313) >= HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE);
		if (bfalse) {
			getTroop().getBattle().addDebugLog("###effect 1313 Soldier={} , 下回合100%闪避一次攻击", getUUID());
			eff1313DodgeRound.add(getTroop().getBattle().getBattleRound() + 1);
		}
	}

	private void hero1617Check() {
		if (getEffVal(EffType.HERO_1617) > 0) {
			eff1617Cen++;
			if (eff1617Cen <= ConstProperty.getInstance().getEffect1617TimesLimit()) {
				getTroop().getBattle().addDebugLog("###Soldier={} [精准射击]印记 1617 = {}",
						getSoldierId(), eff1617Cen);
			}
			if (!isSkill5Round && eff1617Cen >= ConstProperty.getInstance().getEffect1617TimesLimit()) {
				isSkill5Round = true;
				getTroop().getBattle().addDebugLog("###Soldier={} [精准射击]印记 1617 开启", getSoldierId());
			}
			if (eff1617Cen >= 2 + ConstProperty.getInstance().getEffect1617TimesLimit()) {
				eff1617Cen = 0;
				isSkill5Round = false;
				getTroop().getBattle().addDebugLog("###Soldier={} [精准射击]印记 1617 结束", getSoldierId());
			}
		}
	}

	@Override
	public int skillHurtExactly(BattleSoldier defSoldier) {
		int result = super.skillHurtExactly(defSoldier);
		BattleSoldierSkillCfg skill_14 = getSkill(PBSoldierSkill.SOLDIER_SKILL_614);
		if (trigerSkill(skill_14)) {
			double minHP = defSoldier.getTroop().getSoldierList().stream()
					.filter(BattleSoldier::isAlive)
					.filter(BattleSoldier::canBeAttack)
					.mapToDouble(BattleSoldier::getCfgHpVal)
					.min()
					.orElse(defSoldier.getCfgHpVal());

			boolean isLowerHp = defSoldier.getCfgHpVal() <= minHP;
			if (isLowerHp) {
				result += skill_14.getP1IntVal();
			}
		}

		int e1525 = Checker1525.effNum(this, defSoldier);
		result += e1525;

		if (isSkill5Round) {
			BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.FOOT_SOLDIER_6_SKILL_5);
			int p1Add = getEffVal(EffType.HERO_1616) > 0 ? getEffVal(EffType.HERO_1616) : getEffVal(EffType.SOLDIER_SKILL_605_1090);
			result += skill.getP1IntVal() + p1Add;
		}

		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.FOOT_SOLDIER_6_SKILL_3);
		if (trigerSkill(skill, NumberUtils.toInt(skill.getP3()))) {
			result += NumberUtils.toInt(skill.getP4());
		}
		if (trigerSkill(skill, skill.getP1IntVal())) {
			result += NumberUtils.toInt(skill.getP2());
		}

		BattleSoldierSkillCfg skill24 = getSkill(PBSoldierSkill.SOLDIER_SKILL_624);
		if (trigerSkill(skill24)) {
			int killfootcnt = getKillTypeCnt(SoldierType.FOOT_SOLDIER_5) + getKillTypeCnt(SoldierType.FOOT_SOLDIER_6);
			int cen = Math.min(skill24.getP1IntVal(), killfootcnt / NumberUtils.toInt(skill24.getP3(), 1));
			int p2 = cen * NumberUtils.toInt(skill24.getP2());
			result += p2;
			getTroop().getBattle().addDebugLog("###skill624战斗中击杀敌方 {} 个步兵, 伤害加成 {},", killfootcnt, p2);
		}
		if (getEffVal(EffType.ARMOUR_11007) > 0 && hasHouPai()) {
			result += getEffVal(EffType.ARMOUR_11007);
			getTroop().getBattle().addDebugLog("### {} ARMOUR_11007 有后排, {}", getUUID(), getEffVal(EffType.ARMOUR_11007));
		}
		if (defSoldier.isYuanCheng()) {//狙击兵攻击远程部队时，伤害+XX%
			BattleSoldierSkillCfg skill_241 = getSkill(PBSoldierSkill.SOLDIER_SKILL_64101);
			result += skill_241.getP1IntVal();
		}
		return result;
	}
	
	@Override
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		hurtVal = super.reduceHurtValPct(atkSoldier, hurtVal);
		hurtVal *= (1 - getEffVal(EffType.HERO_12371) * GsConst.EFF_PER);
		return hurtVal;
	}

	@Override
	public double addHurtValPct(BattleSoldier defSoldier, double hurtVal) {
		hurtVal = super.addHurtValPct(defSoldier, hurtVal);
		if (eff12361Atk) {
			Integer xishu = ConstProperty.getInstance().getEffect12361DamageAdjustMap().getOrDefault(defSoldier.getType(), 10000);
			hurtVal = hurtVal * getEffVal(EffType.HERO_12361) * GsConst.EFF_PER
					* xishu * GsConst.EFF_PER;
			addDebugLog("【12361】定点猎杀 {} , {} ,{}", defSoldier.getUUID(), getEffVal(EffType.HERO_12361), xishu);
		}
		if (eff12363Atk) {
			hurtVal = hurtVal * getEffVal(EffType.HERO_12363) * GsConst.EFF_PER;
			addDebugLog("【12363】爆裂弹片: 定点猎杀命中敌方单位后，额外对其附近随机 2 个敌方单位产生 1 次爆裂伤害（伤害率: XX.XX%）", getEffVal(EffType.HERO_12363));
		}
		return hurtVal;
	}
	

	@Override
	public int getEffVal(EffType eff) {
		if (eff == EffType.HERO_1617 && super.getEffVal(EffType.HERO_12361) > 0) {
			return 0;
		}
		return super.getEffVal(eff);
	}

	@Override
	public int skillAtkExactly(BattleSoldier defSoldier) {
		int result = super.skillAtkExactly(defSoldier);
		result += Checker1524.effNum(this, defSoldier);
		// <data id="60103"
		// des="重型步兵（弩兵)部署攻击(守城)1级trigger=触发概率;damage=伤害参数;targetType=目标类型,在守城时有攻击加成"
		// trigger="10000" damage="2000" />
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.FOOT_SOLDIER_6_SKILL_2);
		if (BattleConst.WarEff.CITY_DEF.check(getTroop().getWarEff()) && trigerSkill(skill)) {
			result += skill.getP1IntVal();
		}

		result -= debuff1595Val();

		return result;
	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.FOOT_SOLDIER_6_SKILL_66;
	}

	Map<SoldierType, Integer> skill5P3;

	/** 获取冲锋目标 */
	public BattleSoldier getAssaultSoldier(BattleSoldier defSoldier) {
		// <data id="60501" des="狙击兵攻击" trigger="5800" p1="0" p2="1036" p3="2_10,3_10,4_10,5_20,6_16,7_10,8_10" />
		if (Objects.isNull(skill5P3)) {
			BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.FOOT_SOLDIER_6_SKILL_5);
			skill5P3 = new HashMap<>(7);
			String[] arr = skill.getP3().trim().split(",");
			for (String str : arr) {
				String[] tv = str.split("_");
				skill5P3.put(SoldierType.valueOf(Integer.valueOf(tv[0])), Integer.valueOf(tv[1]));
			}
		}

		List<BattleSoldier> footList = defSoldier.getTroop().getSoldierList().stream()
				.filter(BattleSoldier::isAlive)
				.filter(ds -> skill5P3.containsKey(ds.getType()))
				.filter(BattleSoldier::canBeAttack)
				.collect(Collectors.toList());

		if (footList.isEmpty()) {
			return defSoldier;
		}

		HawkWeightFactor<BattleSoldier> hf = new HawkWeightFactor<>();
		for (BattleSoldier so : footList) {
			int weight = skill5P3.getOrDefault(so.getType(),0);
			hf.addWeightObj(weight, so);
		}

		return hf.randomObj();

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

	public int getSkill1083p7() {
		return skill1083p7;
	}

	public void setSkill1083p7(int skill1083p7) {
		this.skill1083p7 = skill1083p7;
	}
	
	@Override
	protected PBSoldierSkill honor10SkillId() {
		return PBSoldierSkill.FOOT_SOLDIER_6_SKILL_34;
	}
	
	@Override
	public Map<EffType, Integer> getEffMapClientShow() {
		Map<EffType, Integer> result = super.getEffMapClientShow();
		mergeClientShow(result , EffType.HERO_12014, EffType.FIRE_2006);
		return result;
	}
}
