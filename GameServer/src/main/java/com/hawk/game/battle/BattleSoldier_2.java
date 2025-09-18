package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkWeightFactor;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.RandomContent;
import com.hawk.game.util.RandomUtil;

public class BattleSoldier_2 extends BattleSoldier {
	private boolean skill2Tried;
	private boolean roundEvery3;
	private final String KEY1312 = "KEY1312";
	private boolean sssKaiEn;
	private boolean sssKaiEn1656;
	private int sssKaiEn1656Cnt;

	private int effect1656per;
	private int effect1657p5;
	private int effect1657p6;
	private int effect1657p7;

	private Set<String> help1635tar = new HashSet<>();// 帮助几个目标
	private int help1635Count; // 帮了多少次
	private boolean skill224Zhuiji;
	/** 结束回合 - 值*/
	private HawkTuple2<Integer, Integer> skill624Debuff = HawkTuples.tuple(0, 0);
	private HawkTuple2<Integer, Integer> skill544Debuff = HawkTuples.tuple(0, 0);
	private int skill_44HP;
	private boolean sss12131Atk;
	private boolean sss12133Atk;

	private int buff12152Val;
	private int buff12293Val;
	private int eff12402Cnt;
	private int eff12404Cnt;
	private int skill24501Num;
	private int effect12511Point;
	private HawkTuple2<Integer, Integer> skill12511Buff = HawkTuples.tuple(0, 0);
	private int effect12513Rate,effect12514Rate,effect12518Val;
	private Map<BattleSoldier,String> eff12515Map = new HashMap<>(); // 重力缠绕 obj: crId
	private int eff12512Cnt;
	@Override
	public void roundStart() {
		super.roundStart();
		roundEvery3 = getTroop().getBattle().getBattleRound() % 3 == 0;
		sssKaiEn = getTroop().getBattle().getBattleRound() % (3 - effect1657p7) == 0; // TODO 开技能后 - 1
		effect12511Check();
	}

	private void effect12511Check() {
		try {
		if (effect12511Point < ConstProperty.getInstance().effect12511AtkThresholdValue || getBattleRound() % ConstProperty.getInstance().effect12511AtkRound != 0) {
			return;
		}
		// - 激能迸发：自身主战坦克暴击伤害增加 effect12511BaseVaule + XX.XX%【12511】*敌方空军单位数 （敌方空军单位计数时至多取 effect12511CountMaxinum 个）
		int yuanCnt = (int) getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::isPlan).filter(BattleSoldier::isAlive).count();
		yuanCnt = Math.min(yuanCnt, ConstProperty.getInstance().effect12511CountMaxinum);
		int eff12511Val = ConstProperty.getInstance().effect12511BaseVaule + yuanCnt * getEffVal(EffType.HERO_12511);
		HawkTuple2<Integer, Integer> buff = HawkTuples.tuple(getBattleRound() + ConstProperty.getInstance().effect12511ContinueRound - 1, eff12511Val);
		setSkill12511Buff(buff);
		effect12511Point = 0;
		
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public SoldierType getType() {
		return SoldierType.TANK_SOLDIER_2;
	}
	
	@Override
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		double result = super.reduceHurtValPct(atkSoldier, hurtVal);
		result = result * GsConst.EFF_PER * (GsConst.EFF_RATE - eff12404Cnt * getEffVal(EffType.HERO_12404));
		
		int cen = eff12512Cnt / ConstProperty.getInstance().effect12512CritTimes;
		if (cen >0 && getEffVal(EffType.HERO_12512) > 0) {
			cen = Math.min(cen, ConstProperty.getInstance().effect12512Maxinum);
			double eff12512Val = cen * getEffVal(EffType.HERO_12512) * ConstProperty.getInstance().effect12512SoldierAdjustMap.getOrDefault(atkSoldier.getType(), 0) * GsConst.EFF_PER;
			final double oldVal = result;
			result = result * GsConst.EFF_PER * (GsConst.EFF_RATE - eff12512Val);
			addDebugLog("【12512】洪流战幕：自身主战坦克受到伤害减少 12512:{}  hurtVal{} -> {}", eff12512Val,oldVal , hurtVal);
		}
		if (effect12518Val > 0) {
			result = result * (1 - effect12518Val * GsConst.EFF_PER * ConstProperty.getInstance().effect12518SoldierAdjustMap.getOrDefault(atkSoldier.getType(), 10000) * GsConst.EFF_PER);
		}
		return result;
	}

	@Override
	public int skillReduceHurtPer(BattleSoldier atkSoldier) {
		int result = super.skillReduceHurtPer(atkSoldier);
		return result;
	}
	
	@Override
	public int skillFireAtkExactly(BattleSoldier defSoldier) {
		int result = super.skillFireAtkExactly(defSoldier);
		if(sssKaiEn1656){
			result += getEffVal(EffType.EFF_12232);
			addDebugLog("###{}【12232】触发磁暴聚能时，主战坦克超能攻击额外 +{}% ", getUUID(), getEffVal(EffType.EFF_12232));
		}
		result += eff12402Cnt * getEffVal(EffType.HERO_12402);
		return result;
	}

	@Override
	public int skillAtkExactly(BattleSoldier defSoldier) {
		int result = super.skillAtkExactly(defSoldier);
		if (roundEvery3) {
			result += getEffVal(EffType.TANK_1490);
		}
		if (sssKaiEn && getEffVal(EffType.HERO_1654) > 0) {
			result += getEffVal(EffType.HERO_1654);
			getTroop().getBattle().addDebugLog("### 1654 atk {}", getEffVal(EffType.HERO_1654));
		}
		HawkTuple3<Integer, Integer, Integer> eff12163Debuff = eff12163DebuffVal();
		if (eff12163Debuff.second > 0) {
			result -= eff12163Debuff.second;
			addDebugLog("###{}主战坦克处于【损坏状态】时，其攻击加成减少 +{} ", getUUID(), eff12163Debuff.second);
		}
		result += eff12402Cnt * getEffVal(EffType.HERO_12402);
		return result;
	}
	
	@Override
	public double addHurtValPct(BattleSoldier defSoldier, double hurtVal) {
		hurtVal = super.addHurtValPct(defSoldier, hurtVal);
		HawkTuple3<Integer, Integer, Integer> eff12163Debuff = eff12163DebuffVal();
		if (eff12163Debuff.third > 0) {
			hurtVal *= (1 - eff12163DebuffVal().third * GsConst.EFF_PER);
			hurtVal = Math.max(hurtVal, 0);
			addDebugLog("###{}主战坦克处于【损坏状态】时，其在发起攻击时，伤害降低{} ", getUUID(), eff12163Debuff.third);
		}
		return hurtVal;
	}

	@Override
	public void attackOnce(BattleSoldier defSoldier, int atkTimes, double hurtPer, int atkDis) {
		skill2Atk(defSoldier, atkTimes, atkDis);

		if (HawkRand.randInt(10000) < getEffVal(EffType.SOLDIER_2_ATK_ONCE)) {
			super.attackOnce(defSoldier, QIAN_PAI_MAX, 0, atkDis); // 不触发千排
		}

		// 是否触发224技能追击效果
		BattleSoldierSkillCfg skill_24 = getSkill(PBSoldierSkill.SOLDIER_SKILL_224);
		boolean skill224zuiji = trigerSkill(skill_24) && HawkRand.randInt(10000) < NumberUtils.toInt(skill_24.getP3());
		super.attackOnce(defSoldier, atkTimes, hurtPer, atkDis, !skill224zuiji); // 正常攻击一次
		sss12131Atk(defSoldier);
		if (skill224zuiji) {
			skill224Zhuiji(defSoldier, atkTimes, hurtPer, atkDis);
		}
		sssKaiEn1656Atk(defSoldier, atkTimes, atkDis);

		Integer ct1312 = (Integer) defSoldier.getTroop().getExtryParam(KEY1312).orElse(0) + 1;
		defSoldier.getTroop().putExtryParam(KEY1312, ct1312);
	}

	private void sss12131Atk(BattleSoldier defSoldier) {
		if (getEffVal(EffType.HERO_12131) == 0) {
			return;
		}
		Set<BattleSoldier> eff12515targets = new HashSet<>();
		eff12515targets.add(defSoldier);
		try {
			int num = 1;
			if (HawkRand.randInt(10000) < getEffVal(EffType.EFF_12292)) {
				num = ConstProperty.getInstance().getEffect12292AtkNum();
			}
			List<BattleSoldier> enemyList = defSoldier.getTroop().getSoldierList();
			List<BattleSoldier> tankList = enemyList.stream().filter(BattleSoldier::isAlive).filter(s -> s.getType() == SoldierType.TANK_SOLDIER_2).collect(Collectors.toList());

			List<BattleSoldier> tarList = RandomUtil.randomWeightObject(tankList, num);
			if (tarList.size() < num) {
				tankList = enemyList.stream().filter(BattleSoldier::isAlive).filter(s -> s.getType() == SoldierType.TANK_SOLDIER_1).collect(Collectors.toList());
				tarList.addAll(RandomUtil.randomWeightObject(tankList, num - tarList.size()));
			}
			for (BattleSoldier tar : tarList) {
				addDebugLog(" {} sss12131 atk tar: {}", getUUID(), tar.getUUID());
				sss12131Atk = true;
				super.attackOnce(tar, QIAN_PAI_MAX, 0, Integer.MAX_VALUE,false); // 不触发千排
				sss12131Atk = false;
			}
			eff12515targets.addAll(tarList);
			if (tarList.isEmpty()) {
				List<BattleSoldier> tartets12514 = eff12514Atk(defSoldier);
				eff12515targets.addAll(tartets12514);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		try {//- 【12513】自身出征数量最多的主战坦克在触发坦克对决时，本次攻击额外附加如下效果：- 歼灭突袭：额外向敌方随机 1（effect12513AtkNum） 个空军单位（优先选择轰炸机作为目标）追加 1 （effect12513AtkTimes）次攻击，伤害率 固定值（effect12513BaseVaule） + XX.XX%*敌方空军单位数（敌方空军单位计数时至多取 10 个（effect12513CountMaxinum））
			if (getEffVal(EffType.HERO_12513) > 0) {
				List<BattleSoldier> plans = defSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isPlan).filter(BattleSoldier::isAlive)
						.collect(Collectors.toList());
				Collections.shuffle(plans);
				List<BattleSoldier> targets = new ArrayList<BattleSoldier>();
				//敌方空军单位计数
				int effect12513AtkNum = ConstProperty.getInstance().effect12513AtkNum;
				if (getEffVal(EffType.HERO_12532) > 0) {
					int eff12532Num = ConstProperty.getInstance().effect12532BaseVaule + getEffVal(EffType.HERO_12532) * Math.min(plans.size() , ConstProperty.getInstance().effect12532CountMaxinum);
					effect12513AtkNum = effect12513AtkNum + eff12532Num / GsConst.RANDOM_MYRIABIT_BASE;
					if (HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < eff12532Num % GsConst.RANDOM_MYRIABIT_BASE) {
						effect12513AtkNum = effect12513AtkNum + 1;
					}
				}
				
				for (BattleSoldier sol : plans) {
					if (sol.getType() == SoldierType.PLANE_SOLDIER_3 && targets.size() < effect12513AtkNum) {
						targets.add(sol);
					}
				}
				for (BattleSoldier sol : plans) {
					if (sol.getType() == SoldierType.PLANE_SOLDIER_4 && targets.size() < effect12513AtkNum) {
						targets.add(sol);
					}
				}
				addDebugLog("HERO_12513 num:{} effect12513AtkNum:{} ", targets.size(), effect12513AtkNum);
				effect12513Rate = ConstProperty.getInstance().effect12513BaseVaule + getEffVal(EffType.HERO_12513) * Math.min(plans.size() , ConstProperty.getInstance().effect12513CountMaxinum);
				for (BattleSoldier tar : targets) {
					for (int i = 0; i < ConstProperty.getInstance().effect12513AtkTimes; i++) {
						super.attackOnce(tar, QIAN_PAI_MAX, 0, Integer.MAX_VALUE,false); // 不触发千排
					}
				}
				effect12513Rate = 0;
				eff12515targets.addAll(targets);
				if(targets.isEmpty()){
					List<BattleSoldier> tartets12514 = eff12514Atk(defSoldier);
					eff12515targets.addAll(tartets12514);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		// 重力缠绕：本次攻击中，被攻击单位和所有被追加攻击的单位会进入同一重力缠绕状态
		if (getEffVal(EffType.HERO_12515) > 0) {
			String crId = HawkUUIDGenerator.genUUID();
			for (BattleSoldier tar : eff12515targets) {
				if(tar.effect12515Num < ConstProperty.getInstance().effect12515Maxinum){
					eff12515Map.put(tar, crId);
					tar.effect12515Num++;
					addDebugLog("【12515】重力缠绕：本次攻击中，被攻击单位和所有被追加攻击的单位会进入同一重力缠绕状态 {}", tar.getUUID());
					
					int eff12518Val = getEffVal(EffType.HERO_12518);
					if(eff12518Val>0){
						effect12518Val = Math.min(effect12518Val + eff12518Val, eff12518Val * ConstProperty.getInstance().effect12518Maxinum);
						addDebugLog("【【12518】 个人战时，每对敌方施加 1 次重力缠绕状态，自身主战坦克受到伤害减少 {} {}", getUUID() ,effect12518Val);
					}
				}
			}
		}
	}

	private List<BattleSoldier> eff12514Atk(BattleSoldier defSoldier) {
		// 【12514】自身出征数量最多的主战坦克在触发坦克对决时，本次攻击额外附加如下效果- 猎袭追击：当追加攻击没有可选取目标时，将会选取敌方随机单位进行攻击，优先选取近战单位作为目标，造成伤害为原本伤害的 XX.XX% 【12514】->针对敌方兵种留个内置修正系数(effect12514RoundWeight)
		if (getEffVal(EffType.HERO_12514) <= 0) {
			return Collections.emptyList();
		}
		List<RandomContent<BattleSoldier>> objList = defSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::isJinZhan)
				.map(s -> RandomContent.create(s, ConstProperty.getInstance().effect12514RoundWeightMap.getOrDefault(s.getType(), 100))).filter(r -> r.getWeight() > 0)
				.collect(Collectors.toList());
		if (objList.isEmpty()) {
			objList = defSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::isYuanCheng)
					.map(s -> RandomContent.create(s, ConstProperty.getInstance().effect12514RoundWeightMap.getOrDefault(s.getType(), 100))).filter(r -> r.getWeight() > 0)
					.collect(Collectors.toList());
		}
		objList = RandomUtil.randomWeightObject(objList, 1);
		List<BattleSoldier> targets = objList.stream().map(RandomContent::getObj).collect(Collectors.toList());
		for (BattleSoldier tar : targets) {
			effect12514Rate = getEffVal(EffType.HERO_12514) + getEffVal(EffType.HERO_12531);
			super.attackOnce(tar, QIAN_PAI_MAX, 0, Integer.MAX_VALUE,false);
			effect12514Rate = 0;
		}
		return targets;
	}

	private void skill2Atk(BattleSoldier defSoldier, int atkTimes, int atkDis) {
		if (atkTimes != 0) {
			return;
		}
		BattleSoldierSkillCfg skill_2 = getSkill(PBSoldierSkill.TANK_SOLDIER_2_SKILL_2);
		int trigger = skill_2 == null ? 0 : skill_2.getTrigger();
		skill2Tried = trigerSkill(skill_2, trigger + getEffVal(EffType.SOLDIER_SKILL_TRIGER_202) + getEffVal(EffType.SOLDIER_2_1109)); // trigerSkill(skill_2, EffType.SOLDIER_SKILL_TRIGER_202);
		if (skill2Tried) {
			BattleSoldier plan = randomTar(defSoldier.getTroop(), defSoldier);
			if (Objects.nonNull(plan)) {
				super.attackOnce(plan, QIAN_PAI_MAX, 0, atkDis); // 不触发千排
			}
		}
	}

	private void sssKaiEn1656Atk(BattleSoldier defSoldier, int atkTimes, int atkDis) {
		try {
			boolean sss1656 = sssKaiEn && getEffVal(EffType.HERO_1656) > 0;
			if (!sss1656 || atkTimes != 0) {
				return;
			}

			List<BattleSoldier> planList = defSoldier.getTroop().getSoldierList().stream()
					.filter(ds -> ds.getType() == SoldierType.PLANE_SOLDIER_3 || ds.getType() == SoldierType.PLANE_SOLDIER_4)
					.filter(ds -> ds.isAlive())
					.collect(Collectors.toList());
			Collections.shuffle(planList);
			int atkCnt = 1 + effect1657p6;
			if (HawkRand.randInt(10000) < getEffVal(EffType.EFF_12231)) {
				atkCnt = ConstProperty.getInstance().getEffect12231ArmyNum();
				addDebugLog("{}【12231】战技持续期间，有 XX.XX% 概率将空军目标数有 2 个增至 {} 个", getUUID(), atkCnt);
			}
			planList = planList.subList(0, Math.min(atkCnt, planList.size()));
			for (BattleSoldier plan : planList) {
				sssKaiEn1656 = true;
				super.attackOnce(plan, QIAN_PAI_MAX, 0, Integer.MAX_VALUE, false); // 不触发千排
				sssKaiEn1656 = false;

				double gailv = (effect1656per + effect1657p5) * Math.min(getFreeCnt() * 1D / plan.getFreeCnt(), 1);
				getTroop().getBattle().addDebugLog("### sssKaiEn1656 atk {} effect1656per:{} effect1657p5:{} gailv:{}", plan.getUUID(), effect1656per, effect1657p5, gailv);
				if (HawkRand.randInt(10000) < gailv) {
					sssKaiEn1656Cnt++;
					// 命中 兵种类型 = 3 的负面效果，负面状态持续期间，禁止该目标，释放兵种技能302 （智能护盾），并清除作用号【1544】，智能护盾吸收反击伤害累计的值，禁止【1544】继续累计值
					// 命中 兵种类型 = 4 的负面效果，负面状态持续期间，直升机基础防御值，变更为0
					if (plan instanceof BattleSoldier_3) {
						((BattleSoldier_3) plan).addDebuff1656();
					}
					if (plan instanceof BattleSoldier_4) {
						((BattleSoldier_4) plan).addDebuff1656();
					}
				}

				sss12133atk(defSoldier);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void sss12133atk(BattleSoldier defSoldier) {
		if (getEffVal(EffType.HERO_12133) == 0) {
			return;
		}
		List<BattleSoldier> enemyList = defSoldier.getTroop().getSoldierList();
		List<BattleSoldier> tankList = enemyList.stream().filter(s -> s.getType() == SoldierType.PLANE_SOLDIER_3).collect(Collectors.toList());
		BattleSoldier tar = HawkRand.randomObject(tankList);
		if (tar == null) {
			tankList = enemyList.stream().filter(s -> s.getType() == SoldierType.PLANE_SOLDIER_4).collect(Collectors.toList());
			tar = HawkRand.randomObject(tankList);
		}

		if (tar != null) {
			IPlanSoldier planSoldier = (IPlanSoldier) tar;
			addDebugLog(" {} 磁暴干扰：触发荣耀凯恩的【磁暴聚能】效果后，额外向敌方随机 1 个空军部队追加 1 次攻击 atk tar: {}", getUUID(), planSoldier.getUUID());
			sss12133Atk = true;
			super.attackOnce(planSoldier, QIAN_PAI_MAX, 0, Integer.MAX_VALUE); // 不触发千排
			sss12133Atk = false;

			if (getEffVal(EffType.HERO_12134) > 0 && planSoldier.getEff12134Debuff().first < getTroop().getBattle().getBattleRound()) {
				int round = getTroop().getBattle().getBattleRound() + ConstProperty.getInstance().getEffect12134ContinueRound() - 1;
				int val = getEffVal(EffType.HERO_12134);
				HawkTuple2<Integer, Integer> skil12134Debuff = HawkTuples.tuple(round, val);
				planSoldier.setEff12134Debuff(skil12134Debuff);
				getTroop().getBattle().addDebugLog("### {} 12134   {} hurt- {} 结束回合 {}", getUUID(), planSoldier.getUUID(), val, round);
			}

			if (getEffVal(EffType.HERO_12143) > 0 && planSoldier.getEff12143Debuff().first < getTroop().getBattle().getBattleRound()) {
				int round = getTroop().getBattle().getBattleRound() + getEffVal(EffType.SSS_SKILL_12143_P2) - 1;
				int val = getEffVal(EffType.HERO_12143);
				HawkTuple2<Integer, Integer> skil12143Debuff = HawkTuples.tuple(round, val);
				planSoldier.setEff12143Debuff(skil12143Debuff);
				getTroop().getBattle().addDebugLog("### {} 12143   {} 并额外降低目标 +XX.XX% 的轰炸概率 {} 结束回合 {}", getUUID(), planSoldier.getUUID(), val, round);
			}

		}

	}

	@Override
	protected void attacked(BattleSoldier attacker, int deadCnt) {
		super.attacked(attacker, deadCnt);
		buff12293Val += getEffVal(EffType.EFF_12293);
		buff12293Val = Math.min(buff12293Val, getEffVal(EffType.EFF_12293) * ConstProperty.getInstance().getEffect12293Maxinum());
		if (attacker.isYuanCheng() && getEffVal(EffType.HERO_12402) > 0) {
			eff12402Cnt = Math.min(eff12402Cnt + 1, ConstProperty.getInstance().getEffect12402Maxinum());
			addDebugLog("### 12402 {}  层数 {}", getUUID(), eff12402Cnt);
		}
		if (attacker.isYuanCheng()) {
			BattleSoldierSkillCfg skill_245 = getSkill(PBSoldierSkill.SOLDIER_SKILL_24501);
			skill24501Num = Math.min(skill24501Num + skill_245.getP2IntVal(), skill_245.getP2IntVal() * skill_245.getP3IntVal());
		}
	}

	private void skill224Zhuiji(BattleSoldier defSoldier, int atkTimes, double hurtPer, int atkDis) {
		getTroop().getBattle().addDebugLog("### kill 224 追击, 不叠加层数");
		skill224Zhuiji = true;
		super.attackOnce(defSoldier, QIAN_PAI_MAX, hurtPer, atkDis, false); // 追击一次
		skill224Zhuiji = false;
	}

	private void add1553Debuff(BattleSoldier defSoldier) {
		int effVal1553 = getEffVal(EffType.HERO_1553);
		if (effVal1553 > 0) {
			if (defSoldier.getType() == SoldierType.PLANE_SOLDIER_3) {
				BattleSoldier_3 plan = (BattleSoldier_3) defSoldier;
				plan.add1553Debuff(effVal1553);
			}
			if (defSoldier.getType() == SoldierType.PLANE_SOLDIER_4) {
				BattleSoldier_4 plan = (BattleSoldier_4) defSoldier;
				plan.add1553Debuff(effVal1553);
			}
		}

	}

	@Override
	public void attackOver(BattleSoldier defSoldier, int killCnt, double hurtVal) {
		super.attackOver(defSoldier, killCnt, hurtVal);
		add1452Debuff(defSoldier);
		if (skill2Tried) {
			add1553Debuff(defSoldier);
			add1635Buff(defSoldier);
		}
		if (roundEvery3) {
			int val1492 = getEffVal(EffType.TANK_1492);
			if (val1492 > 0) {
				int seldDead = (int) (val1492 * GsConst.EFF_PER * killCnt);
				addDeadCnt(seldDead);
				getTroop().getBattle().addDebugLog("### 1492 kill self {}", seldDead);
			}
		}
		BattleSoldierSkillCfg skill_24 = getSkill(PBSoldierSkill.SOLDIER_SKILL_224);
		if (defSoldier.isPlan() && !skill224Zhuiji && Objects.nonNull(skill_24)) {
			defSoldier.incrDebufSkill224Val(skill_24.getP1IntVal(), NumberUtils.toInt(skill_24.getP2()));
		}

		if (defSoldier.isPlan()) {
			BattleSoldierSkillCfg skill_44 = getSkill(PBSoldierSkill.TANK_SOLDIER_2_SKILL_44);
			skill_44HP = Math.min(skill_44HP + skill_44.getP1IntVal(), skill_44.getP1IntVal() * NumberUtils.toInt(skill_44.getP2()));
			int effVal12152 = getEffVal(EffType.HERO_12152);
			buff12152Val = Math.min(effVal12152 + buff12152Val, effVal12152* ConstProperty.getInstance().getEffect12152Maxinum());
		}
		hero12515(defSoldier, hurtVal);
		skill24601(defSoldier);
	}

	private void skill24601(BattleSoldier defSoldier) {
		BattleSoldierSkillCfg scfg = getSkill(PBSoldierSkill.SOLDIER_SKILL_24601);
		if (scfg.getP2IntVal() > 0 && scfg.getP1().contains(defSoldier.getType().getNumber() + "")) {
			defSoldier.debuffSkill24601 = Math.min(defSoldier.debuffSkill24601 + scfg.getP2IntVal(), scfg.getP2IntVal() * scfg.getP3IntVal());
			addDebugLog("###【24601】 每次攻击命中近战单位时，降低其防御 +XX%（该效果可叠加，至多 X 层） {} ", defSoldier.debuffSkill24601);
		}
	}

	private void hero12515(BattleSoldier defSoldier, double hurtVal) {
		try {
			if (!eff12515Map.containsKey(defSoldier)) { // 其中某一单位受到自身主战坦克伤害时，处于重力缠绕中的其他单位同时受到此次伤害的 XX.XX% 【12515】\
				return;
			}
			String crId = eff12515Map.get(defSoldier);
			List<BattleSoldier> eff12515targets = eff12515Map.entrySet().stream().filter(e -> Objects.equals(crId, e.getValue())).map(e -> e.getKey())
					.filter(BattleSoldier::canBeAttack).collect(Collectors.toList());
			for (BattleSoldier tar : eff12515targets) {
				eff12515Map.remove(tar);
				tar.effect12515Num--;
				if (tar != defSoldier) { // ###【12515】
					double crhurt = hurtVal * (GsConst.EFF_PER * (getEffVal(EffType.HERO_12515) + getEffVal(EffType.HERO_12533))
							* ConstProperty.getInstance().effect12515SoldierAdjustMap.getOrDefault(tar.getType(), 10000) * GsConst.EFF_PER);
					crhurt = tar.forceField(this, crhurt);
					int curCnt = tar.getFreeCnt();
					int maxKillCnt = (int) Math.ceil(4.0f * crhurt / tar.getHpVal());
					maxKillCnt = Math.max(1, maxKillCnt);
					int killCnt = Math.min(maxKillCnt, curCnt);
					tar.addDeadCnt(killCnt);
					addKillCnt(tar, killCnt);
					addDebugLog("###【12515】 解除重力缠绕状态 处于重力缠绕中的其他单位同时受到此次伤害 {} {} kill: {} ", tar.getUUID(), crhurt, killCnt);
				}

				if (getEffVal(EffType.HERO_12516) > 0) {
					additionalAtk(tar, getEffVal(EffType.HERO_12516), false, true, "### 12516 超能湮灭：自身主战坦克施加的重力缠绕解除时，会使所有原本处于该重力缠绕状态中单位受到一次超能攻击伤害");
				}
				int effVal12517 = getEffVal(EffType.HERO_12517);
				if (effVal12517 > 0) {
					int debuff12517Val = tar.debuff12517Val;
					debuff12517Val = Math.min(debuff12517Val + effVal12517, effVal12517 * ConstProperty.getInstance().effect12517Maxinum);
					tar.debuff12517Val = Math.max(tar.debuff12517Val, debuff12517Val);
					addDebugLog("### 12517 ，且受伤害的单位的伤害效率降低 {} {} ", tar.getUUID(), debuff12517Val);
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public int skillHPExactly() {
		int result = super.skillHPExactly() + skill_44HP + skill24501Num;
		result += getTroop().getSkill144Val().values().stream().mapToInt(Integer::intValue).sum();
		return result;
	}

	@Override
	public int skillDefExactly(BattleSoldier atkSoldier) {
		int result = super.skillDefExactly(atkSoldier);
		result += getTroop().getSkill144Val().values().stream().mapToInt(Integer::intValue).sum();
		return result;
	}

	/**
	 * 1635 为固定值，不受军事值（三值成长影响），效果值 由const.xml effect1635BaseVal 控制 填 10= 0.1%
	 * 1.自身主战坦克（兵种类型 =2）出征士兵数量大于目标防御坦克（兵种类型 =1） 的X% X由const.xml effect1635Maxinum控制 填5000 = 50% 
	 * 2.若战斗中对空军产生了暴击
	 * 3.则对所有满足的目标防御坦克（包含集结）且防御坦克归属玩家的身上携带【1634】增加一层复活
	 */
	private void add1635Buff(BattleSoldier defSoldier) {
		if (!defSoldier.isPlan() || getEffVal(EffType.HERO_1635) == 0) {
			return;
		}

		double marchNum = getOriCnt() - getShadowCnt(); // 出征数

		for (BattleSoldier sol : getTroop().getSoldierList()) {
			boolean can1635 = sol.getType() == SoldierType.TANK_SOLDIER_1 && sol.isAlive() && sol.getEffVal(EffType.HERO_1635) > 0;
			if (!can1635) {
				continue;
			}
			if (((BattleSoldier_1) sol).getEff1635Cen() >= ConstProperty.getInstance().getEffect1634Maxinum()) {
				continue;
			}
			double solMarchNum = sol.getOriCnt() - sol.getShadowCnt();
			if (marchNum / solMarchNum >= ConstProperty.getInstance().getEffect1635Maxinum() * GsConst.EFF_PER) {
				((BattleSoldier_1) sol).incEff1635Cen();
				help1635tar.add(sol.getUUID());
				help1635Count++;
				getTroop().getBattle().addDebugLog("### 1635 抢修目标 {}", sol.getUUID());
			}
		}

		getTroop().getBattle().addDebugLog("### 1635 抢修数 {}", help1635Count);
	}

	private void add1452Debuff(BattleSoldier defSoldier) {
		int effVal1452 = getEffVal(EffType.EFF_1452);
		if (effVal1452 > 0) {
			if (defSoldier.getType() == SoldierType.PLANE_SOLDIER_3) {
				BattleSoldier_3 plan = (BattleSoldier_3) defSoldier;
				int eff1452Debuf = Math.max(plan.getEff1452Debuf(), effVal1452);
				plan.setEff1452Debuf(eff1452Debuf);
			}
			if (defSoldier.getType() == SoldierType.PLANE_SOLDIER_4) {
				BattleSoldier_4 plan = (BattleSoldier_4) defSoldier;
				int eff1452Debuf = Math.max(plan.getEff1452Debuf(), effVal1452);
				plan.setEff1452Debuf(eff1452Debuf);
			}
		}
	}

	private BattleSoldier randomTar(BattleTroop enemyTroop, BattleSoldier defSoldier) {
		// 防守兵
		List<BattleSoldier> soldierList = enemyTroop.getSoldierList();

		List<BattleSoldier> planList = soldierList.stream()
				.filter(ds -> ds != defSoldier)
				.filter(ds -> ds.canBeAttack())
				.filter(ds -> ds.isAlive())
				.filter(ds -> withinAtkDis(ds))
				.collect(Collectors.toList());
		if (planList.isEmpty()) {
			return null;
		}

		if (HawkRand.randInt(10000) < getEffVal(EffType.ARMOUR_1593)) {
			Optional<BattleSoldier> plOP = planList.stream()
					.filter(ds -> ds.getType() == SoldierType.PLANE_SOLDIER_3 || ds.getType() == SoldierType.PLANE_SOLDIER_4)
					.findAny();
			if (plOP.isPresent()) {
				getTroop().getBattle().addDebugLog("### ARMOUR_1593 atk plan");
				return plOP.get();
			}
		}

		// 兵种数量0.3次方加权平分
		HawkWeightFactor<BattleSoldier> hf = new HawkWeightFactor<>();
		for (BattleSoldier so : planList) {
			int weight = (int) Math.pow(so.getFreeCnt(), BattleConst.Const.POW.getNumber() * 0.01);
			hf.addWeightObj(weight, so);
		}

		return hf.randomObj();
	}

	@Override
	public int skillHurtExactly(BattleSoldier defSoldier) {
		int damage = super.skillHurtExactly(defSoldier);
		SoldierType tarType = defSoldier.getType();
		boolean isfly = tarType == SoldierType.PLANE_SOLDIER_3 || tarType == SoldierType.PLANE_SOLDIER_4;
		if (isfly && skill2Tried) {
			BattleSoldierSkillCfg skill_2 = getSkill(PBSoldierSkill.TANK_SOLDIER_2_SKILL_2);
			damage += skill_2.getP1IntVal();
		}
		if (skill2Tried) {
			damage += getEffVal(EffType.SOLDIER_2_1108);
		}
		if (sss12131Atk) {
			damage += getEffVal(EffType.EFF_12291);
		}
		// <data id="20101"
		// des="主战坦克（枪兵）爱国者防空系统(长矛)1级trigger=触发概率;damage=伤害参数;targetType=目标类型,增加对于战斗飞行器的伤害"
		// trigger="10000" damage="2000" />

		BattleSoldierSkillCfg skill_1 = getSkill(PBSoldierSkill.TANK_SOLDIER_2_SKILL_1);
		if (isfly && trigerSkill(skill_1)) {
			damage = damage + skill_1.getP1IntVal() + getEffVal(EffType.SOLDIER_SKILL_201);
		}

		// <data id="20103"
		// des="主战坦克（枪兵）致命瞄准系统(致命一击)1级trigger=触发概率;damage=伤害参数;targetType=目标类型,有一定概率造成3倍伤害"
		// trigger="3000" damage="20000" />
		damage = hurtSkill3(defSoldier, damage);

		return damage;

	}

	private int hurtSkill3(BattleSoldier defSoldier, int damage) {
		// 爆击
		BattleSoldierSkillCfg skill_3 = getSkill(PBSoldierSkill.TANK_SOLDIER_2_SKILL_3);
		if (Objects.isNull(skill_3)) {
			return damage;
		}
		Integer ct1312 = (Integer) defSoldier.getTroop().getExtryParam(KEY1312).orElse(0);
		ct1312 = Math.min(ct1312, ConstProperty.getInstance().getEffect1312Maxinum());
		int effVal1070_1552 = Math.max(getEffVal(EffType.SOLDIER_SKILL_203_105), getEffVal(EffType.HERO_1552));
		int trigger = skill_3.getTrigger() + getEffVal(EffType.LABRORA_1201) + effVal1070_1552 + ct1312 * getEffVal(EffType.GANDA_1312) - skill624DebuffVal() + buff12152Val - getDebuff12206Val();
		if (sss12133Atk) {
			trigger += getEffVal(EffType.HERO_12142);
		}
		if (roundEvery3) {
			trigger += getEffVal(EffType.TANK_1493);
		}
		if (!trigerSkill(skill_3, trigger)) {
			return damage;
		}
		
		if(getEffVal(EffType.HERO_12404) > 0){
			eff12404Cnt = Math.min(eff12404Cnt + 1, ConstProperty.getInstance().getEffect12404Maxinum());
			addDebugLog("### 12404 {}  层数 {}", getUUID(), eff12404Cnt);
		}
		damage = damage + skill_3.getP1IntVal() + getEffVal(EffType.EFF_1421) + getEffVal(EffType.SOLDIER_1309) + eff11003() + buff12293Val;
		if (roundEvery3) {
			damage += getEffVal(EffType.TANK_1491);
		}
		if (sssKaiEn && getEffVal(EffType.HERO_1655) > 0) {
			damage += getEffVal(EffType.HERO_1655);
			getTroop().getBattle().addDebugLog("### 1655 暴击伤害+ {}", getEffVal(EffType.HERO_1655));
		}

		BattleSoldierSkillCfg skill_14 = getSkill(PBSoldierSkill.SOLDIER_SKILL_214);
		if (trigerSkill(skill_14) && skill_14.getP2().contains(defSoldier.getType().getNumber() + "")) {
			damage += skill_14.getP1IntVal();
		}
		final int effVal1651 = getEffVal(EffType.HERO_1651);
		if (effVal1651 > 0) {
			damage += effVal1651;
			getTroop().getBattle().addDebugLog("### {} 1651 暴击伤害增加 {}", getUUID(), effVal1651);
		}
		int[] arr12031 = ConstProperty.getInstance().getEffect12031EffectiveRoundArr();
		if (getBattleRound() >= arr12031[0] && getBattleRound() <= arr12031[1]) {
			damage += getEffVal(EffType.EFF_12031);
		}
		
		hero12153(defSoldier);
		if (defSoldier.isJinZhan()) {//主战攻击近战部队时，暴击伤害+XX%
			BattleSoldierSkillCfg skill_241 = getSkill(PBSoldierSkill.SOLDIER_SKILL_24101);
			damage += skill_241.getP1IntVal();
		}
		if (getEffVal(EffType.HERO_12511) > 0) {
			effect12511Point += ConstProperty.getInstance().effect12511AddFirePoint;
			if (defSoldier.getType() == SoldierType.TANK_SOLDIER_2) {
				effect12511Point += ConstProperty.getInstance().effect12511AddFirePointExtra;
			}
			addDebugLog("### 【12511】自身出征数量最多的主战坦克在触发暴击时，{}涌动值 {}", getUUID(), effect12511Point);
		}
		
		int skill12511BuffVal = skill12511BuffVal();
		if(skill12511BuffVal>0){//关联【12511】：能量激荡状态攻击时额外附加如下效果
			damage += skill12511BuffVal;
			eff12512Cnt ++;
		}
		if (getBuff12574Val(EffType.EFF_12577) > 0) {
			damage += getBuff12574Val(EffType.EFF_12577);
			addDebugLog("###增幅效果12577 主战坦克暴击伤害增加{} ", getBuff12574Val(EffType.EFF_12577));
		}
		return damage;
	}

	private void hero12153(BattleSoldier defSoldier) {
		if (defSoldier.isPlan()) {
			IPlanSoldier plan = (IPlanSoldier) defSoldier;
			if (getEffVal(EffType.HERO_12153) > 0 ) {
				int round = getTroop().getBattle().getBattleRound() + ConstProperty.getInstance().getEffect12153ContinueRound() - 1;
				int val = getEffVal(EffType.HERO_12153);
				HawkTuple2<Integer, Integer> skil12153Debuff = HawkTuples.tuple(round, val);
				plan.setEff12153Debuff(skil12153Debuff);
				getTroop().getBattle().addDebugLog("### {} 12153   {} 并额外降低目标 +XX.XX% 伤害效果 {} 结束回合 {}", getUUID(), plan.getUUID(), val, round);
			}
			if (getEffVal(EffType.HERO_12154) > 0 ) {
				int round = getTroop().getBattle().getBattleRound() + ConstProperty.getInstance().getEffect12154ContinueRound() - 1;
				int val = getEffVal(EffType.HERO_12154);
				HawkTuple2<Integer, Integer> skil12154Debuff = HawkTuples.tuple(round, val);
				plan.setEff12154Debuff(skil12154Debuff);
				getTroop().getBattle().addDebugLog("### {} 12154   {} 并额外降低目标 +XX.XX% 伤害效果 {} 结束回合 {}", getUUID(), plan.getUUID(), val, round);
			}
		}
	}

	@Override
	protected double getHurtVal(BattleSoldier defSoldier, double reducePer) {
		double hurt = super.getHurtVal(defSoldier, reducePer);
		if (sssKaiEn && sssKaiEn1656) {
			hurt = hurt * GsConst.EFF_PER * (getEffVal(EffType.HERO_1656) + getEffVal(EffType.HERO_1657));
			getTroop().getBattle().addDebugLog("### {} 1656 额外攻击 {} {} {}", getUUID(), getEffVal(EffType.HERO_1656), getEffVal(EffType.HERO_1657), hurt);
		}
		if (sss12131Atk) {
			hurt = hurt * GsConst.EFF_PER * getEffVal(EffType.HERO_12131);
			addDebugLog("### {} 12131 额外攻击 {} {}", getUUID(), getEffVal(EffType.HERO_12131), hurt);
		}
		if (sss12133Atk) {
			hurt = hurt * GsConst.EFF_PER * getEffVal(EffType.HERO_12133);
			addDebugLog("### {} 12133 额外攻击 {} {}", getUUID(), getEffVal(EffType.HERO_12133), hurt);
		}
		if(effect12513Rate>0){
			hurt = hurt * GsConst.EFF_PER * effect12513Rate;
			addDebugLog("###【12513】自身出征数量最多的主战坦克在触发坦克对决时，本次攻击额外附加如下效果：- 歼灭突袭：额外向敌方 {} 伤害率 {}", defSoldier.getUUID(), effect12513Rate);
		}
		if(effect12514Rate>0){
			hurt = hurt * GsConst.EFF_PER * effect12514Rate;
			addDebugLog("###- 12514】自身出征数量最多的主战坦克在触发坦克对决时，本次攻击额外附加如下效果 - 猎袭追击：当追加攻击没有可选取目标时敌方 {} 伤害率 {}", defSoldier.getUUID(), effect12514Rate);
		}
		return hurt;
	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.TANK_SOLDIER_2_SKILL_66;
	}

	@Override
	public ArmyInfo calcArmyInfo(double selfCoverRete) {
		// private Set<String> help1635tar = new HashSet<>();// 帮助几个目标
		// private int help1635Count; // 帮了多少次
		ArmyInfo result = super.calcArmyInfo(selfCoverRete);
		result.setHelp1635TarCount(help1635tar.size());
		result.setHelp1635Count(help1635Count);
		result.setSssKaiEn1656Cnt(sssKaiEn1656Cnt);
		return result;
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
		getTroop().getBattle().addDebugLog("### skill624 降低 203技能 暴击几率 {} 结束回合 {}", skill624Debuff.second, skill624Debuff.first);
		return skill624Debuff.second;
	}

	@Override
	public void setSkill544Debuff(HawkTuple2<Integer, Integer> debuff) {
		skill544Debuff = debuff;
	}

	public HawkTuple2<Integer, Integer> getSkill544Debuff() {
		return skill544Debuff;
	}

	private int eff11003() {
		int result = 0;
		if (getEffVal(EffType.ARMOUR_11003) > 0) {
			int atkCnt = Math.min(getKillDetail().size(), ConstProperty.getInstance().getEffect11003TimesLimit());
			result = atkCnt * getEffVal(EffType.ARMOUR_11003);
			getTroop().getBattle().addDebugLog("### ARMOUR_11003 暴击 命中目标数 {} 11003 {}", atkCnt, result);
		}
		return result;
	}

	public int getEffect1656per() {
		return effect1656per;
	}

	public void setEffect1656per(int effect1656per) {
		this.effect1656per = effect1656per;
	}

	public int getEffect1657p5() {
		return effect1657p5;
	}

	public void setEffect1657p5(int effect1657p5) {
		this.effect1657p5 = effect1657p5;
	}

	public int getEffect1657p6() {
		return effect1657p6;
	}

	public void setEffect1657p6(int effect1657p6) {
		this.effect1657p6 = effect1657p6;
	}

	public int getEffect1657p7() {
		return effect1657p7;
	}

	public void setEffect1657p7(int effect1657p7) {
		this.effect1657p7 = effect1657p7;
	}

	@Override
	protected PBSoldierSkill honor10SkillId() {
		return PBSoldierSkill.TANK_SOLDIER_2_SKILL_34;
	}

	@Override
	public Map<EffType, Integer> getEffMapClientShow() {
		Map<EffType, Integer> result = super.getEffMapClientShow();
		mergeClientShow(result, EffType.HERO_12004, EffType.FIRE_2002);
		mergeClientShow(result, EffType.EFF_12033, EffType.TANK_B_DEF_PER, EffType.TANK_B_HP_PER);
		mergeClientShow(result, EffType.PLANT_SOLDIER_SKILL_844, EffType.TANK_B_DEF_PER);
		return result;
	}
	
	public void setSkill12511Buff(HawkTuple2<Integer, Integer> skill12511Buff) {
		this.skill12511Buff = skill12511Buff;
	}

	int skill12511BuffVal() {
		if (Objects.isNull(skill12511Buff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill12511Buff.first) {
			return 0;
		}
		addDebugLog(" 12511 激能迸发：自身主战坦克暴击伤害增加", skill12511Buff.second);
		return skill12511Buff.second;
	}

}
