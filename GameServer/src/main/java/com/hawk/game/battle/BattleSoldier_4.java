package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkWeightFactor;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.RandomUtil;
import com.hawk.serialize.string.SerializeHelper;

/**
 * * <data id="40102" des="战术飞行器（弓骑兵)高超音速引擎(奔袭)1级无需配置数值,极大的提高行军速度" />
 * 
 * @author lwt
 * @date 2017年12月30日
 */
public class BattleSoldier_4 extends IPlanSoldier {
	boolean trigerSkill_3;
	boolean trigerSkill_3_1532;
	boolean atkFootSoldier;
	int skill3Count; // 累计空袭次数
	final int ONCE = 300;
	int eff1452Debuf;
	Map<Integer, Integer> eff1553Debuf;
	private int debufSkill224Val;
	boolean trigereff12062;
	private int eff1532Power;
	private int eff1532Max;
	private List<Integer> eff1532Weight = new ArrayList<>(6);

	private int liao1645Cnt;
	private int liao1645Kill;
	int debuff1656round;
	int attackedCnt;
	int debuff12462Round;
	int effect12465FirePoint;

	@Override
	public void init(Player player, BattleSoldierCfg soldierCfg, int count, int shadowCnt) {
		super.init(player, soldierCfg, count, shadowCnt);
		BattleSoldierSkillCfg skill3 = getSkill(PBSoldierSkill.PLANE_SOLDIER_4_SKILL_3);
		if (Objects.nonNull(skill3)) {
			eff1532Max = NumberUtils.toInt(skill3.getP4(), 35);
			Splitter.on(",").split(skill3.getP5()).forEach(str -> eff1532Weight.add(NumberUtils.toInt(str)));
		}
	}

	@Override
	public void roundStart2() {
		super.roundStart2();
		hero12465();
		hero12461();
	}

	private void hero12465() {
		try {
			if (effect12465FirePoint<=0 || effect12465FirePoint < ConstProperty.getInstance().getEffect12465AtkThresholdValue()) {
				return;
			}

			if (getBattleRound() % ConstProperty.getInstance().getEffect12465AtkRound() != 0) {
				return;
			}
			effect12465FirePoint = 0;

			List<BattleSoldier> tarList = getTroop().getSoldierList().stream().filter(BattleSoldier::canBeAttack).filter(s -> s.skill12465BuffVal() == 0)
					.sorted(Comparator.comparingInt(BattleSoldier::getxPos)).collect(Collectors.toList());
			final int size = tarList.size();
			int qian = tarList.indexOf(this);
			int hou = qian;
			int max = ConstProperty.getInstance().getEffect12465InitChooseNum();
			List<BattleSoldier> atktarList = new ArrayList<>();
			atktarList.add(this);
			for (int i = 0; i < size; i++) {
				qian--;
				hou++;
				if (hou >= 0 && hou < size && max > 0) {
					BattleSoldier tar = tarList.get(hou);
					boolean ishoumian = tar.getType() == SoldierType.FOOT_SOLDIER_5||tar.getType() == SoldierType.FOOT_SOLDIER_6 || tar.getType() == SoldierType.CANNON_SOLDIER_7;
					if (ishoumian) {
						atktarList.add(tar);
						max--;
					}
				}
				if (qian >= 0 && qian < size && max > 0) {
					BattleSoldier tar = tarList.get(qian);
					boolean ishoumian = tar.getType() == SoldierType.FOOT_SOLDIER_5||tar.getType() == SoldierType.FOOT_SOLDIER_6 || tar.getType() == SoldierType.CANNON_SOLDIER_7;
					if (ishoumian) {
						atktarList.add(tar);
						max--;
					}
				}
			}
			int yuanCnt = (int) getTroop().getSoldierList().stream().filter(BattleSoldier::isYuanCheng).filter(BattleSoldier::isAlive).count();
			int taryuanCnt = (int) getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::isPlan).filter(BattleSoldier::isAlive).count();
			taryuanCnt = Math.min(taryuanCnt, ConstProperty.getInstance().getEffect12465AirForceCountMaxinum());
			int buffVal = ConstProperty.getInstance().getEffect12465BaseVaule() + getEffVal(EffType.HERO_12465) * yuanCnt
					+ ConstProperty.getInstance().getEffect12465GrowCof() * taryuanCnt;

			HawkTuple2<Integer, Integer> skill12465Buff = HawkTuples.tuple(getBattleRound() + ConstProperty.getInstance().getEffect12465ContinueRound() - 1, buffVal);
			for (BattleSoldier tar : atktarList) {
				tar.setSkill12465Buff(skill12465Buff);
				addDebugLog("{} 则为自身及身后 2个非直升机单位提供空域护航效果，使其 {} 受到伤害减少 {}", getUUID(), tar.getUUID(), buffVal);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void hero12461() {
		try {
			if (getEffVal(EffType.HERO_12461) <= 0) {
				return;
			}
			if (getBattleRound() % ConstProperty.getInstance().getEffect12461AtkRound() != 0) {
				return;
			}

			List<BattleSoldier> footList = new ArrayList<>();
			int effect12461ChooseNum = ConstProperty.getInstance().getEffect12461InitChooseNum();
			if (getEffVal(EffType.HERO_12481) > 0) {
				int yuanCnt = (int) getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::isPlan).filter(BattleSoldier::isAlive).count();
				int eff12481Num = ConstProperty.getInstance().getEffect12481BaseVaule() + getEffVal(EffType.HERO_12481) * yuanCnt;
				effect12461ChooseNum = effect12461ChooseNum + eff12481Num / GsConst.RANDOM_MYRIABIT_BASE;
				if (HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < eff12481Num % GsConst.RANDOM_MYRIABIT_BASE) {
					effect12461ChooseNum = effect12461ChooseNum + 1;
				}
			}
			{
				List<BattleSoldier> planList = getTroop().getEnemyTroop().getSoldierList().stream()
						.filter(BattleSoldier::isPlan)
						.filter(s -> s.getType() == SoldierType.PLANE_SOLDIER_3)
						.filter(s -> s.skill12461DeBuffVal() <= 0)
						.filter(BattleSoldier::canBeAttack)
						.collect(Collectors.toCollection(ArrayList::new));
				planList = RandomUtil.randomWeightObject(planList, effect12461ChooseNum);
				footList.addAll(planList);
			}
			if (footList.size() < effect12461ChooseNum) {
				List<BattleSoldier> planList = getTroop().getEnemyTroop().getSoldierList().stream()
						.filter(BattleSoldier::isPlan)
						.filter(s -> s.getType() == SoldierType.PLANE_SOLDIER_4)
						.filter(s -> s.skill12461DeBuffVal() <= 0)
						.filter(BattleSoldier::canBeAttack)
						.collect(Collectors.toCollection(ArrayList::new));
				planList = RandomUtil.randomWeightObject(planList, effect12461ChooseNum - footList.size());
				footList.addAll(planList);
			}

			for (BattleSoldier tar : footList) {
				debuff12462Round = getBattleRound() + ConstProperty.getInstance().getEffect12461ContinueRound() - 1;
				tar.setSkill12461DeBuff(HawkTuples.tuple(debuff12462Round, getEffVal(EffType.HERO_12461), this));
				addDebugLog("{} 选中其中 {} 进入缠斗状态", getUUID(), tar.getUUID());
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	@Override
	public SoldierType getType() {
		return SoldierType.PLANE_SOLDIER_4;
	}

	@Override
	public void attackOnce(BattleSoldier defSoldier, int atkTimes, double hurtPer, int atkDis) {
		if (atkTimes == 0) {
			BattleSoldierSkillCfg skill3 = getSkill(PBSoldierSkill.PLANE_SOLDIER_4_SKILL_3);
			int triger = skill3.getTrigger()
					+ getEffVal(EffType.SOLDIER_SKILL_TRIGER_403)
					+ getEffVal(EffType.EFF_1315) * Math.min(getTroop().getBattle().getBattleRound(), ConstProperty.getInstance().getEffect1315Maxinum())
					- debuff1553Val() - eff12143DebuffVal();
			trigerSkill_3 = trigerSkill(skill3, triger);
			atkFootSoldier = trigerSkill_3 ? skill3.getP1IntVal() >= RandomUtils.nextInt(GsConst.RANDOM_MYRIABIT_BASE) : false;
		}
		if (trigerSkill_3) {
			skill403Atk(defSoldier, atkTimes, hurtPer);
			if (Math.random() < getEffVal(EffType.S403_1532) * GsConst.EFF_PER) {
				getTroop().getBattle().addDebugLog("### 触发1532 ");
				skill403Atk(defSoldier, atkTimes, hurtPer);
			}
		} else {
			super.attackOnce(defSoldier, atkTimes, hurtPer, atkDis);
		}
	}

	private void skill403Atk(BattleSoldier defSoldier, int atkTimes, double hurtPer) {
		skill3Count++;
		BattleSoldier assaultSoldier = getAssaultSoldier(defSoldier);
		int eff1554 = assaultSoldier.getEffVal(EffType.HERO_1554);
		boolean lanjie = eff1554 >= HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE);
		if (lanjie) {
			getTroop().getBattle().addDebugLog("### 1554  原目标 : {} 拦截成功攻击: {} ", assaultSoldier.getSoldierId(), defSoldier.getSoldierId());
			assaultSoldier = defSoldier;
		}

		super.attackOnce(assaultSoldier, atkTimes, hurtPer, Integer.MAX_VALUE, false);
		trigerSkill_3_1532 = false;
		if (Math.random() < getEffVal(EffType.EFF_1456) * GsConst.EFF_PER) {
			super.attackOnce(assaultSoldier, atkTimes, hurtPer, Integer.MAX_VALUE, false);
		}
		if (getTroop().getLast1645round() != getTroop().getBattle().getBattleRound() && Math.random() < getEffVal(EffType.HERO_1645) * GsConst.EFF_PER) {
			getTroop().setLast1645round(getTroop().getBattle().getBattleRound());
			List<BattleSoldier> s3List = getTroop().getSoldierList().stream()
					.filter(sol -> sol instanceof BattleSoldier_3)
					.filter(sol -> sol.getFreeCnt() <= getFreeCnt() * 2)
					.sorted(Comparator.comparingInt(BattleSoldier::getFreeCnt).reversed())
					.limit(ConstProperty.getInstance().getEffect1645Maxinum())
					.collect(Collectors.toList());
			// 俯冲轰炸
			for (BattleSoldier sol : s3List) {
				BattleSoldier_3 sol3 = (BattleSoldier_3) sol;
				hurtPer = 1 - (ConstProperty.getInstance().getEffect1645Power() + getEffVal(EffType.HERO_1646)) * GsConst.EFF_PER;
				sol3.eff1645fuchonghongzha(this, defSoldier, QIAN_PAI_MAX, hurtPer, Integer.MAX_VALUE);
				liao1645Cnt++;
			}
		}

		eff12062Atk(defSoldier);

	}

	private void eff12062Atk(BattleSoldier defSoldier) {
		try {
			if (getEffVal(EffType.EFF_12062) > 0) {
				trigereff12062 = true;
				Map<Integer, Integer> weightmapCfg = SerializeHelper.stringToMap(ConstProperty.getInstance().getEffect12062RoundWeight(), Integer.class, Integer.class, "_", ",");
				for (int j = 0; j < ConstProperty.getInstance().getEffect12062AtkTimes(); j++) {
					List<BattleSoldier> soldierList = new ArrayList<>(defSoldier.getTroop().getSoldierList());
					for (int i = 0; i < ConstProperty.getInstance().getEffect12062AtkNum(); i++) {
						BattleSoldier tar12062 = eff12062Tar(weightmapCfg, soldierList);
						addDebugLog("空军双将 12062 额外向敌方远程随机 2 个单位进行 1 轮散射攻击");
						put12062Debuff(tar12062);
						soldierList.remove(tar12062);
					}
					if (getEffVal(EffType.HERO_12468) >= HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE)) {
						weightmapCfg = SerializeHelper.stringToMap(ConstProperty.getInstance().getEffect12468RoundWeight(), Integer.class, Integer.class, "_", ",");
						for (int i = 0; i < ConstProperty.getInstance().getEffect12468AtkNum(); i++) {
							BattleSoldier tar12062 = eff12062Tar(weightmapCfg, soldierList);
							addDebugLog("- 【12468】并有 XX% 的概率选中 2 个敌方近战单位作为额外攻击目标");
							put12062Debuff(tar12062);
							soldierList.remove(tar12062);
						}
					}
				}
				trigereff12062 = false;
			}
		} catch (Exception e) {
			trigereff12062 = false;
			HawkException.catchException(e);
		}
	}

	private void put12062Debuff(BattleSoldier tar12062) {
		if (Objects.nonNull(tar12062)) {
			super.attackOnce(tar12062, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false);
			int val12063 = (int) (getEffVal(EffType.EFF_12063) * (1 + GsConst.EFF_PER * getEffVal(EffType.HERO_12466)));
			int max12063 = (int) (ConstProperty.getInstance().getEffect12063MaxValue() * (1 + GsConst.EFF_PER * getEffVal(EffType.HERO_12467)));
			tar12062.addDebuff12063Atk(val12063, max12063);

			int val12064 = (int) (getEffVal(EffType.EFF_12064) * (1 + GsConst.EFF_PER * getEffVal(EffType.HERO_12466)));
			int max12064 = (int) (ConstProperty.getInstance().getEffect12064MaxValue() * (1 + GsConst.EFF_PER * getEffVal(EffType.HERO_12467)));
			tar12062.addDebuff12064Def(val12064, max12064);

			int val12065 = (int) (getEffVal(EffType.EFF_12065) * (1 + GsConst.EFF_PER * getEffVal(EffType.HERO_12466)));
			int max12065 = (int) (ConstProperty.getInstance().getEffect12065MaxValue() * (1 + GsConst.EFF_PER * getEffVal(EffType.HERO_12467)));
			tar12062.addDebuff12065HP(val12065, max12065);

			int val12272 = (int) (getEffVal(EffType.EFF_12272) * (1 + GsConst.EFF_PER * getEffVal(EffType.HERO_12466)));
			int max12272 = (int) (ConstProperty.getInstance().getEffect12272MaxValue() * (1 + GsConst.EFF_PER * getEffVal(EffType.HERO_12467)));
			tar12062.addDebuff12272FAtk(val12272, max12272);
		}
	}

	private BattleSoldier eff12062Tar(Map<Integer, Integer> weightmapCfg, List<BattleSoldier> soldierList) {
		Map<SoldierType, Integer> weightmap = new HashMap<>(4);
		Map<SoldierType, BattleSoldier> map = new HashMap<>(4);
		// 【狙击兵 = 6，攻城车 = 7，突击步兵 = 5，直升机 = 4】
		Collections.shuffle(soldierList);
		for (BattleSoldier tar : soldierList) {
			if (map.containsKey(tar.getType())) {
				continue;
			}
			int weight = weightmapCfg.getOrDefault(tar.getType().getNumber(), 0);
			if (weight == 0) {
				continue;
			}
			if (tar.isAlive()) {
				map.put(tar.getType(), tar);
				weightmap.put(tar.getType(), weight);
			}
		}
		if (weightmap.isEmpty()) {
			return null;
		}

		SoldierType type = HawkRand.randomWeightObject(weightmap);
		return map.get(type);
	}

	@Override
	protected double getHurtVal(BattleSoldier defSoldier, double reducePer) {
		double result = super.getHurtVal(defSoldier, reducePer);
		if (trigereff12062) {
			result = result * GsConst.EFF_PER * getEffVal(EffType.EFF_12062);
		}
		if (debuff12462Round >= getBattleRound()) {
			result = result * (1 - GsConst.EFF_PER * getEffVal(EffType.HERO_12462));
			addDebugLog("{} 【12462】缠斗方造成伤害效率降低 ", getUUID());
		}

		return result;
	}

	@Override
	public int skillHurtExactly(BattleSoldier defSoldier) {
		int result = super.skillHurtExactly(defSoldier);
		if (trigerSkill_3) {
			BattleSoldierSkillCfg skill3 = getSkill(PBSoldierSkill.PLANE_SOLDIER_4_SKILL_3);
			result = result
					+ NumberUtils.toInt(skill3.getP3())
					+ getEffVal(EffType.SOLDIER_SKILL_403_303)
					+ getEffVal(EffType.SOLDIER_1418)
					+ getEffVal(EffType.S403_1534)
					+ getEffVal(EffType.HERO_12192);
		}
		if (trigerSkill_3_1532) {
			result = result + getEffVal(EffType.S403_1533);
		}

		BattleSoldierSkillCfg skill_14 = getSkill(PBSoldierSkill.SOLDIER_SKILL_414);
		if (trigerSkill(skill_14) && skill_14.getP2().contains(defSoldier.getType().getNumber() + "")) {
			result += skill_14.getP1IntVal();
		}

		return result;
	}

	@Override
	protected void attackOver(BattleSoldier defSoldier, int killCnt, double hurtVal) {
		super.attackOver(defSoldier, killCnt, hurtVal);
		add1595Debuff(defSoldier);
		if (trigerSkill_3 && getEffVal(EffType.S403_1532) > 0) {
			eff1532Power = eff1532Power + getAtkDis() + defSoldier.getAtkDis();
			getTroop().getBattle().addDebugLog("### 403释放完成 冲能数 {} " + eff1532Power);
		}
		BattleSoldierSkillCfg skill424 = getSkill(PBSoldierSkill.SOLDIER_SKILL_424);
		if (defSoldier.isFoot() && trigerSkill(skill424)) {
			defSoldier.incrDebufSkill424Val(NumberUtils.toInt(skill424.getP3()), NumberUtils.toInt(skill424.getP2()), skill424.getP1IntVal());
		}
		if (getEffVal(EffType.HERO_12465) > 0) {
			effect12465FirePoint += ConstProperty.getInstance().getEffect12465AddFirePoint();
			addDebugLog("每攻击命中 1(effect12465AddFirePoint) 次敌方单位后，增加自身 10(effect12465AddFirePoint) 点磁能 {}", 12465);
		}
	}

	@Override
	protected void attacked(BattleSoldier attacker, int deadCnt) {
		super.attacked(attacker, deadCnt);
		attackedCnt++;
	}

	@Override
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		hurtVal = super.reduceHurtValPct(atkSoldier, hurtVal);

		if (getEffVal(EffType.HERO_12193) > 0) {
			int effVal = getEffVal(EffType.HERO_12193) * Math.min(attackedCnt, ConstProperty.getInstance().getEffect12193Maxinum());
			hurtVal *= (1 - effVal * GsConst.EFF_PER);
		}
		if (debuff12462Round >= getBattleRound() && atkSoldier.isPlan()) {
			hurtVal = hurtVal * (1 - getEffVal(EffType.HERO_12463) * GsConst.EFF_PER);
			if (getEffVal(EffType.HERO_12482) > 0) {
				int yuanCnt = (int) getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::isPlan).filter(BattleSoldier::isAlive).count();
				yuanCnt = Math.min(yuanCnt, ConstProperty.getInstance().getEffect12482CountMaxinum());
				hurtVal = hurtVal * (1 - (ConstProperty.getInstance().getEffect12482BaseVaule() + getEffVal(EffType.HERO_12482) * yuanCnt) * GsConst.EFF_PER);
			}
			addDebugLog("【12463】缠斗方受到被缠斗方伤害时，使本次伤害减少");
		}
		if (getEffVal(EffType.HERO_12469) > 0) {
			int effVal = Math.min(skill3Count, ConstProperty.getInstance().getEffect12469Maxinum()) * getEffVal(EffType.HERO_12469);
			hurtVal *= (1 - effVal * GsConst.EFF_PER * ConstProperty.getInstance().getEffect12469AdjustMap().getOrDefault(atkSoldier.getType().getNumber(), 0) * GsConst.EFF_PER);
			addDebugLog("【12469】个人战时，自身出征数量最多的直升机每触发 1 次黑鹰轰炸技能后，自身受到攻击时伤害减少 {}", effVal);
		}

		return hurtVal;
	}

	@Override
	public int skillAtkExactly(BattleSoldier defSoldier) {
		int result = 0;
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.PLANE_SOLDIER_4_SKILL_2);
		if (BattleConst.WarEff.RES.check(getTroop().getWarEff()) && trigerSkill(skill)) {
			result = result + skill.getP1IntVal() + getEffVal(EffType.SOLDIER_SKILL_402);
		}
		if (trigerSkill_3) {
			result = result + Math.min(skill3Count * ONCE, getEffVal(EffType.SUPER_SOLDIER_1414));
		}
		result -= eff1452Debuf;
		eff1452Debuf = 0;

		result -= debufSkill224Val;
		int buff1644 = Math.min(ConstProperty.getInstance().getEffect1644Maxinum(), skill3Count * getEffVal(EffType.HERO_1644));
		result += buff1644;
		if (buff1644 > 0) {
			getTroop().getBattle().addDebugLog("### 1644 atk增加 {}", buff1644);
		}
		return result;
	}

	@Override
	public int skillIgnoreTargetHP(BattleSoldier target) {
		int result = super.skillIgnoreTargetHP(target);
		if (target.isYuanCheng()) {// 直升机攻击远程部队时，减少目标生命加成+XX%
			BattleSoldierSkillCfg skill_241 = getSkill(PBSoldierSkill.SOLDIER_SKILL_44101);
			result += skill_241.getP1IntVal();
		}
		return result;
	}

	/**
	 * 获取冲锋目标
	 */
	public BattleSoldier getAssaultSoldier(BattleSoldier defSoldier) {
		if (eff1532Power > eff1532Max) {
			eff1532Power = eff1532Power - eff1532Max;
			trigerSkill_3_1532 = true;
			getTroop().getBattle().addDebugLog("### 403 消耗冲能 层数: {}", eff1532Power);
		}

		if (trigerSkill_3_1532) { // 改变攻击逻辑
			List<BattleSoldier> defList = defSoldier.getTroop().getSoldierList().stream()
					.filter(BattleSoldier::canBeAttack)
					.filter(BattleSoldier::isAlive)
					.filter(ar1 -> ar1.getLastAtkRoundMaxHurt() > 0)
					.sorted((ar1, ar2) -> ar2.getLastAtkRoundMaxHurt() - getLastAtkRoundMaxHurt())
					.limit(eff1532Weight.size())
					.collect(Collectors.toList());
			Map<BattleSoldier, Integer> map = Maps.newHashMapWithExpectedSize(defList.size());
			for (int i = 0; i < defList.size(); i++) {
				map.put(defList.get(i), eff1532Weight.get(i));
			}
			if (!map.isEmpty()) {
				for (Entry<BattleSoldier, Integer> tarEnt : map.entrySet()) {
					BattleSoldier temp = tarEnt.getKey();
					getTroop().getBattle().addDebugLog("### Soldier:{} hurt:{} weight:{}", temp.getSoldierId(), temp.getLastAtkRoundMaxHurt(), tarEnt.getValue());
				}
				BattleSoldier result = HawkRand.randomWeightObject(map);
				getTroop().getBattle().addDebugLog("### 403 改变攻击攻击目标:{}", result.getSoldierId());
				return result;
			}

		}

		List<BattleSoldier> footList = defSoldier.getTroop().getSoldierList().stream()
				.filter(BattleSoldier::isAlive)
				.filter(ds -> ds.getType() == SoldierType.FOOT_SOLDIER_5
						|| ds.getType() == SoldierType.FOOT_SOLDIER_6
						|| ds.getType() == SoldierType.CANNON_SOLDIER_7
						|| ds.getType() == SoldierType.PLANE_SOLDIER_4)
				.collect(Collectors.toList());

		List<BattleSoldier> atklist;
		if (!footList.isEmpty() && atkFootSoldier) {
			atklist = footList;
		} else {
			atklist = defSoldier.getTroop().getSoldierList().stream()
					.filter(BattleSoldier::canBeAttack)
					.filter(BattleSoldier::isAlive)
					.collect(Collectors.toList());
		}
		if (atklist.isEmpty()) {
			return defSoldier;
		}
		HawkWeightFactor<BattleSoldier> hf = new HawkWeightFactor<>();
		for (BattleSoldier so : atklist) {
			int weight = (int) Math.pow(so.getFreeCnt(), BattleConst.Const.POW.getNumber() * 0.01);
			hf.addWeightObj(weight, so);
		}

		return hf.randomObj();

	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.PLANE_SOLDIER_4_SKILL_66;
	}

	public int getEff1452Debuf() {
		return eff1452Debuf;
	}

	public void setEff1452Debuf(int eff1452Debuf) {
		this.eff1452Debuf = eff1452Debuf;
		getTroop().getBattle().addDebugLog("### Add 1452 debuf {}", eff1452Debuf);
	}

	public void add1553Debuff(int debufval) {
		if (Objects.isNull(eff1553Debuf)) {
			eff1553Debuf = new HashMap<>();
		}
		int round = getTroop().getBattle().getBattleRound();
		eff1553Debuf.merge(round, debufval, (v1, v2) -> Math.max(v1, v2));
		getTroop().getBattle().addDebugLog("### Add 1553 debuf {}", eff1553Debuf.get(round));
	}

	public int debuff1553Val() {
		if (Objects.isNull(eff1553Debuf)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		return eff1553Debuf.getOrDefault(round - 1, 0) + eff1553Debuf.getOrDefault(round, 0);
	}

	private void add1595Debuff(BattleSoldier defSoldier) {
		int effVal1595 = getEffVal(EffType.ARMOUR_1595);
		if (effVal1595 > 0) {
			if (defSoldier.getType() == SoldierType.FOOT_SOLDIER_5) {
				BattleSoldier_5 plan = (BattleSoldier_5) defSoldier;
				plan.add1595Debuff(effVal1595);
			}
			if (defSoldier.getType() == SoldierType.FOOT_SOLDIER_6) {
				BattleSoldier_6 plan = (BattleSoldier_6) defSoldier;
				plan.add1595Debuff(effVal1595);
			}
		}

	}

	@Override
	public boolean isPlan() {
		return true;
	}

	@Override
	public void incrDebufSkill224Val(int val, int maxCen) {
		debufSkill224Val = Math.min(debufSkill224Val + val, val * maxCen);
		getTroop().getBattle().addDebugLog("### {} Add skill 224 debuf {}", getUUID(), debufSkill224Val);
	}

	public int getLiao1645Cnt() {
		return liao1645Cnt;
	}

	public void setLiao1645Cnt(int liao1645Cnt) {
		this.liao1645Cnt = liao1645Cnt;
	}

	public int getLiao1645Kill() {
		return liao1645Kill;
	}

	public void setLiao1645Kill(int liao1645Kill) {
		this.liao1645Kill = liao1645Kill;
	}

	@Override
	public ArmyInfo calcArmyInfo(double selfCoverRete) {
		ArmyInfo result = super.calcArmyInfo(selfCoverRete);
		result.setLiao1645Cnt(liao1645Cnt);
		result.setLiao1645Kill(liao1645Kill);
		if (liao1645Cnt > 0) {
			HawkLog.logPrintln("{} 1645 trigercnt {} kill:{}", getUUID(), liao1645Cnt, liao1645Kill);
		}
		return result;
	}

	@Override
	public double getPhysicsDefVal(BattleSoldier atkSoldier) {
		if (debuff1656round >= getTroop().getBattle().getBattleRound()) {
			return 0;
		}
		return super.getPhysicsDefVal(atkSoldier);
	}

	public void addDebuff1656() {
		debuff1656round = getTroop().getBattle().getBattleRound();
		HawkLog.logPrintln("{} debuff1656 直升机基础防御值，变更为0", getUUID());
	}

	@Override
	protected PBSoldierSkill honor10SkillId() {
		return PBSoldierSkill.PLANE_SOLDIER_4_SKILL_34;
	}

	@Override
	public Map<EffType, Integer> getEffMapClientShow() {
		Map<EffType, Integer> result = super.getEffMapClientShow();
		mergeClientShow(result, EffType.HERO_12014, EffType.FIRE_2004);
		return result;
	}

	public double chanDou(BattleSoldier atkSoldier, double hurtVal) {
		int yuanCnt = (int) getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::isPlan).filter(BattleSoldier::isAlive).count();
		yuanCnt = Math.min(yuanCnt, ConstProperty.getInstance().getEffect12461AdjustCountMaxinum());
		int effVal12461 = ConstProperty.getInstance().getEffect12461ShareBaseVaule() + getEffVal(EffType.HERO_12461) * yuanCnt;
		final double soulHurt = hurtVal * GsConst.EFF_PER * effVal12461;

		double adjustCof = ConstProperty.getInstance().getEffect12461AdjustCof() - ConstProperty.getInstance().getEffect12461GrowCof() * yuanCnt;
		double soulHurtreal = soulLink(atkSoldier, soulHurt) * GsConst.EFF_PER * adjustCof;

		soulHurtreal = this.forceField(atkSoldier, soulHurtreal);
		int curCnt = getFreeCnt();
		int maxKillCnt = (int) Math.ceil(4.0f * soulHurtreal / getHpVal());
		maxKillCnt = Math.max(1, maxKillCnt);
		int killCnt = Math.min(maxKillCnt, curCnt);

		addDeadCnt(killCnt);
		atkSoldier.addKillCnt(this, killCnt);

		// 被攻击
		attacked(atkSoldier, killCnt);

		addDebugLog("###  {} - 缠斗状态 下，受到己方直升机干扰，{} 敌方该空军单位在造成伤害时，有 {} 的伤害将强制分摊至己方该直升机上  死{}", getUUID(), atkSoldier.getUUID(), soulHurt, killCnt);

		// 缠斗
		return hurtVal - soulHurt;
	}

}
