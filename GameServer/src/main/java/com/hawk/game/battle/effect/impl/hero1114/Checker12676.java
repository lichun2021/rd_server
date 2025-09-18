package com.hawk.game.battle.effect.impl.hero1114;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 * 12676】
- 【万分比】【12676】坚毅同步：每偶数回合开始时，全体远程部队获得【坚毅同步】状态，【坚毅同步】状态下的单位防御和血量增加固定值（effect12676BaseVaule）+XX.XX%（作用号12676）*自身属性加成，持续 1 （effect12676ContinueRound）回合（至多叠加2（effect12676Maxinum）层）->针对己方兵种留个内置系数effect12676SoldierAdjust
- 注：需满足12671攻城车数量限制，作用号才生效
- 战报相关
- 于战报中隐藏
- 不合并至精简战报中
- 仅对集结战斗生效（包含集结进攻和集结防守）
- 在战斗开始前判定，满足条件后本次战斗全程生效
- 坚毅同步：每偶数回合开始时，全体远程部队获得【坚毅同步】状态，【坚毅同步】状态下的单位防御和血量增加固定值（effect12676BaseVaule）+XX.XX%（作用号12676）*自身属性加成->针对己方兵种留个内置系数effect12676SoldierAdjust
- 全体远程部队
  - 此为【光环效果】，集结战斗中所有己方的远程部队都有此效果加成
  - 远程部队类型包含有：直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
- 自身属性计算方式参考杰拉尼作用号120004【230608】【SSS】【战车双将】【杰拉尼】 【1086】
  - 自身属性加成 = （自身攻击加成 + 自身防御加成 + 自身生命加成）/3
- 该作用号为常规外围属性加成效果，与其他作用号累加计算
  - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】* 自身兵种修正系数/10000）
    - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect12676SoldierAdjust
      - 固定值读取const表，字段effect12676BaseVaule
        - 配置格式：万分比
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
- 持续 1 （effect12676ContinueRound）回合（至多叠加2（effect12676Maxinum）层）
  - 持续回合数指定数值读取const表，字段effect12676ContinueRound
    - 配置格式：绝对值
  - 叠加层数指定数值读取const表，字段effect12676Maxinum
    - 配置格式：绝对值
  - 叠加时如有超过层数的buff，优先取作用号【12676】数值较高的
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL4)
@EffectChecker(effType = EffType.HERO_12676)
public class Checker12676 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		if (!isYuanCheng(parames.type)) {
			return CheckerKVResult.DefaultVal;
		}

		int oldVal = 0;
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			oldVal = (Integer) object;
		} else {
			oldVal = calEffectVal(parames);
			parames.putLeaderExtryParam(getSimpleName(), oldVal);
		}
		int effPer = (int) (oldVal * GsConst.EFF_PER * ConstProperty.getInstance().effect12676SoldierAdjustMap.getOrDefault(parames.type, 0));
		int effNum = 0;

		return new CheckerKVResult(effPer, effNum);

	}

	/**数值*/
	private int calEffectVal(CheckerParames parames) {
		List<Integer> valueList = new ArrayList<>();
		for (BattleUnity unity : parames.unityList) {
			if (unity.getSolider().getEffVal(EffType.HERO_12671) == 0) {
				continue;
			}
			int effvalue = effvalue(unity, parames);
			valueList.add(effvalue);
		}

		int result = valueList.stream().sorted(Comparator.comparingInt(Integer::intValue).reversed()).limit(ConstProperty.getInstance().effect12676Maxinum)
				.mapToInt(Integer::intValue).sum();

		return result;
	}

	private int effvalue(BattleUnity unity, CheckerParames parames) {
		try {
			if (unity.getSolider().getEffVal(EffType.HERO_12671) == 0) {
				return 0;
			}
			int effPer = 0;

			BattleSoldier maxSoldier8 = unity.getSolider();
			int atkper = maxSoldier8.tupleValue(BattleTupleType.Type.ATK, SoldierType.XXXXXXXXXXXMAN).first;
			int defper = maxSoldier8.tupleValue(BattleTupleType.Type.DEF, SoldierType.XXXXXXXXXXXMAN).first;
			int hpper = maxSoldier8.tupleValue(BattleTupleType.Type.HP, SoldierType.XXXXXXXXXXXMAN).first;
			int avg = (atkper + defper + hpper) / 3;
			int eff12694 = unity.getSolider().getEffVal(EffType.HERO_12694);
			int effval = unity.getEffVal(effType()) + eff12694;
			effPer = (int) (avg * GsConst.EFF_PER * effval) + ConstProperty.getInstance().effect12676BaseVaule ;

			parames.addDebugLog("【12676】{}  【坚毅同步】状态下的单位防御和血量增加固定值 {} atk: {} def: {} hp: {} avg: {},eff12694:{}  最终 12676: {}",
					unity.getPlayerName(), maxSoldier8.getUUID(), atkper, defper, hpper, avg,eff12694, effPer);
			return effPer;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
