package com.hawk.game.battle.effect.impl.hero1116;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkWeightFactor;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleSoldier_6;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.RandomContent;
import com.hawk.game.util.RandomUtil;

public class Hero1116Param {
	final BattleSoldier_6 parent;
	List<BattleSoldier> skill12722Targes = new ArrayList<>();
	public int eff12725Val, eff12723Val;
	boolean eff12723TypeChange;
	public boolean eff12721;
	int eff12723Type; // 1紧凑形态 , 2 狙击形态
	int eff12727;
	int eff12728;
	boolean eff12721Atk;
	public Hero1116Param(BattleSoldier_6 parent) {
		this.parent = parent;
	}

	public void roundStart() {
		this.eff12721 = Hero1116Rules.is12721Active(parent.getEffVal(EffType.HERO_12721));
		if (!eff12721) {
			return;
		}
		this.eff12727 = parent.getEffVal(EffType.HERO_12727) + parent.getEffVal(EffType.HERO_12744);
		this.eff12723Val = 0;
		this.eff12725Val = 0;
		this.eff12723TypeChange = false;
		this.eff12728 = 0;
		skill12722Targes.clear();
		if (!Hero1116Rules.is12722Round(parent.getEffVal(EffType.HERO_12722), parent.getBattleRound(), ConstProperty.getInstance().effect12722AtkRound)) {
			return;
		}
		List<RandomContent<BattleSoldier>> objList = parent.getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::canBeAttack)
				.map(s -> RandomContent.create(s, ConstProperty.getInstance().effect12722RoundWeightMap.getOrDefault(s.getType(), 100))).filter(r -> r.getWeight() > 0)
				.collect(Collectors.toList());
		if (objList.isEmpty()) {
			return;
		}
		objList = RandomUtil.randomWeightObject(objList, ConstProperty.getInstance().effect12722AtkNums);
		List<BattleSoldier> targets = objList.stream().map(RandomContent::getObj).collect(Collectors.toList());
		skill12722Targes.addAll(targets);

		int eff12723cnt = 0;
		for (BattleSoldier tar : skill12722Targes) {
			parent.addDebugLog("【12722】光轨锁定 : {}", tar.getUUID());
			if (tar.getType() != SoldierType.FOOT_SOLDIER_5 && tar.getType() != SoldierType.FOOT_SOLDIER_6) {
				eff12723cnt++;
			}
		}
		int newtype = 0;
		if (eff12723cnt >= ConstProperty.getInstance().effect12723NumLimit) {
			// - 紧凑形态：自身超能攻击增加+XX.XX%【12725】，同步解析多个光反射角度，攻击时多道光束聚焦一点，造成穿透性灼伤效果，造成一次超能攻击伤害，伤害率+XX.XX%【12726】。(->针对敌方兵种留个内置系数effect12726SoldierAdjust)
			this.eff12725Val = parent.getEffVal(EffType.HERO_12725) + parent.getEffVal(EffType.HERO_12743);
			newtype = 1;
			parent.addDebugLog("紧凑形态 自身超能攻击增加 12725 : {}", this.eff12725Val);
		} else {
			// 狙击形态：视野转为冷色调红外成像，标记敌人热能轮廓，攻击能精准命中敌方弱点部位使其晕眩，自身攻击增加+XX.XX【12723】%，且受到攻击的敌方部队中XX.XX%数量的部队下一回合无法进行攻击(->针对敌方兵种留个内置系数effect12724SoldierAdjust)
			this.eff12723Val = parent.getEffVal(EffType.HERO_12723) + parent.getEffVal(EffType.HERO_12742);
			newtype = 2;
			parent.addDebugLog("狙击形态 自身攻击增加 12723 : {} ", this.eff12723Val);
		}
		if (this.eff12723Type != newtype) {
			this.eff12723TypeChange = true;
			this.eff12728 = parent.getEffVal(EffType.HERO_12728) + parent.getEffVal(EffType.HERO_12745);
			parent.addDebugLog("形态切换 自身狙击兵受到伤害再减少 12728 : {} ", this.eff12728);
		}
		this.eff12723Type = newtype;

	}

//	public boolean eff12721Atk(BattleSoldier defSoldier) {
//		if(!eff12721){
//			return false;
//		}
//
//		try {
//			ImmutableMap<SoldierType, Integer> effect12361TargetWeightMap  = ConstProperty.getInstance().effect12721TargetWeightMap;
//
//			List<BattleSoldier> sols = defSoldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::canBeAttack)
//					.collect(Collectors.toList());
//
//
//			if (sols.isEmpty()) {
//				return false;
//			}
//
//			HawkWeightFactor<BattleSoldier> hf = new HawkWeightFactor<>();
//			for (BattleSoldier so : sols) {
//				int weight = effect12361TargetWeightMap.getOrDefault(so.getType(), 0);
//				if (weight > 0) {
//					hf.addWeightObj(weight, so);
//				}
//			}
//			BattleSoldier result = null;
//			try {
//				result = hf.randomObj();
//			} catch (Exception e) {
//			}
//			if (null == result) {
//				result = HawkRand.randomObject(sols);
//			}
//			eff12721Atk = true;
//			parent.doAttackOnce(result, BattleSoldier_6.QIAN_PAI_MAX, 0, Integer.MAX_VALUE,false);
//			eff12721Atk = false;
//
//			return true;
//
//		} catch (Exception e) {
//			HawkException.catchException(e);
//		}
//		return false;
//	}


	public void skill12722Atk(BattleSoldier defSoldier) {
		if (skill12722Targes.isEmpty()) {
			return;
		}
		if (this.eff12723Type == 1) {
			// - 紧凑形态：自身超能攻击增加+XX.XX%【12725】，同步解析多个光反射角度，攻击时多道光束聚焦一点，造成穿透性灼伤效果，造成一次超能攻击伤害，伤害率+XX.XX%【12726】。(->针对敌方兵种留个内置系数effect12726SoldierAdjust)
			int hurtRate = (int) (parent.getEffVal(EffType.HERO_12726) * GsConst.EFF_PER
					* ConstProperty.getInstance().effect12726SoldierAdjustMap.getOrDefault(defSoldier.getType(), 10000));
			parent.additionalAttack(defSoldier, hurtRate, false, true, "12726 additional super attack");
		} else {
			// 狙击形态：视野转为冷色调红外成像，标记敌人热能轮廓，攻击能精准命中敌方弱点部位使其晕眩，自身攻击增加+XX.XX【12723】%，且受到攻击的敌方部队中XX.XX%数量的部队下一回合无法进行攻击(->针对敌方兵种留个内置系数effect12724SoldierAdjust)
			Debuff12724 debuff = new Debuff12724();
			debuff.round = parent.getBattleRound() + 1;
			debuff.eff12724 = parent.getEffVal(EffType.HERO_12724);
			defSoldier.debuff12724.put(debuff.round, debuff);
			parent.addDebugLog("狙击形态 {}  下一回合 {} 无法进行攻击", defSoldier.getUUID(), debuff.eff12724);
		}
		for (BattleSoldier tar : skill12722Targes) {
			int hurtRate12722 = (int) ((parent.getEffVal(EffType.HERO_12722) + parent.getEffVal(EffType.HERO_12741)) * GsConst.EFF_PER
					* ConstProperty.getInstance().effect12722SoldierAdjustMap.getOrDefault(tar.getType(), 10000));
			parent.additionalAttack(tar, hurtRate12722, true, true, "【12722】光轨锁定 ");
		}
	}

	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		if (!eff12721) {
			return hurtVal;
		}

		if (this.eff12727 > 0) {
			int eff12727 = (int) (this.eff12727 * GsConst.EFF_PER * ConstProperty.getInstance().effect12727SoldierAdjustMap.getOrDefault(atkSoldier.getType(), 0));
			int eff12728 = (int) (this.eff12728 * GsConst.EFF_PER * ConstProperty.getInstance().effect12728SoldierAdjustMap.getOrDefault(atkSoldier.getType(), 0));
			hurtVal = hurtVal * GsConst.EFF_PER * (GsConst.EFF_RATE - eff12727);
			hurtVal = hurtVal * GsConst.EFF_PER * (GsConst.EFF_RATE - eff12728);
			parent.addDebugLog("【12727~12728】镜像残影自身狙击兵受到伤害减少 {} , {}", eff12727, eff12728);
		}
		if (Hero1116Rules.is12729Active(parent.getEffVal(EffType.HERO_12729), parent.getBattleRound(), ConstProperty.getInstance().effect12729AtkRound)) {
			int effVal12729 = (int) (parent.getEffVal(EffType.HERO_12729) * GsConst.EFF_PER
					* ConstProperty.getInstance().effect12729SoldierAdjustMap.getOrDefault(atkSoldier.getType(), 0));
			hurtVal = hurtVal * GsConst.EFF_PER * (GsConst.EFF_RATE - effVal12729);
			parent.addDebugLog("【12729】 受到伤害降低   {}", effVal12729);
		}

		if (parent.getEffVal(EffType.HERO_12731) > 0) {
			hurtVal = hurtVal * GsConst.EFF_PER * (GsConst.EFF_RATE - parent.getEffVal(EffType.HERO_12731));
			parent.addDebugLog("【12731】 受到伤害降低   {}", parent.getEffVal(EffType.HERO_12731));
		}

		return hurtVal;
	}

	public double addHurtValPct(BattleSoldier defSoldier, double hurtVal) {
		if (!eff12721) {
			return hurtVal;
		}

//		if (eff12721Atk) {
//			hurtVal = hurtVal * parent.getEffVal(EffType.HERO_12721) * GsConst.EFF_PER;
//			parent.addDebugLog("【12721】棱镜矩阵 {} hurtVal : {} ", defSoldier.getUUID(), hurtVal);
//		}

		if (Hero1116Rules.is12730Active(parent.getEffVal(EffType.HERO_12730), parent.getBattleRound(), ConstProperty.getInstance().effect12730AtkRound)) {
			int effVal12730 = (int) (parent.getEffVal(EffType.HERO_12730) * GsConst.EFF_PER
					* ConstProperty.getInstance().effect12730SoldierAdjustMap.getOrDefault(defSoldier.getType(), 0));
			hurtVal = hurtVal * GsConst.EFF_PER * (GsConst.EFF_RATE + effVal12730);
			parent.addDebugLog("【12730】 狙击兵造成伤害增加   {}", effVal12730);
		}
		return hurtVal;
	}

}
