package com.hawk.game.battle.effect.impl.manhattan;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12561】
- 【万分比】【12561】基地防守战斗时，每 5（effect12561AtkRound） 回合开始时释放烟雾，使自身所有单位受到伤害减少 20%【12561】 ，并且使敌方所有单位攻击减少 XX.XX%（effect12561BaseVaule），持续 2（effect12561ContinueRound） 回合。（效果不可叠加，减伤效果在受到敌方坦克攻击时翻倍）->针对敌方兵种留个内置减伤修正系数（effect12561SoldierAdjust）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 该作用号仅在防守且防守自身基地或盟友基地时生效
    - 援助盟友时，援军方和被攻击的盟友均可生效，相互独立
  - 自身所有战斗单位是指所有参战的兵种（即兵种id = 1~8）
  - 此效果对战斗开始时（算作0回合？）造成的伤害也生效（数值算第1回合的）；如【11041】
[图片]
  - 每第 X 回合开始时
    - 战斗中每第 X 的倍数的回合开始后，自身开始普攻前，进行判定
    - 指定回合数读取const表，字段effect12561AtkRound
      - 配置格式：绝对值
  - 使自身所有单位受到伤害减少 XX.XX%【12561】
    - 该作用号为伤害减少效果，与其他作用号累乘计算，即 
      - 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】* 敌方兵种修正系数/10000 * 叠加数）
    - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12561SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
  - 并且使敌方所有单位攻击减少 XX.XX%
    - 降低敌方加成为外围属性减少效果；
      - 即 敌方部队实际攻击 = 基础攻击属性*（1 + 敌方攻击加成 - 本作用号攻击降低）
    - 固定数值读取const表，字段effect12561BaseVaule
      - 配置格式：万分比
  - 持续 2 回合
    - 持续回合数读取const表，字段effect12561ContinueRound
      - 配置格式：绝对值
  - （该效果不可叠加，减伤效果在受到敌方坦克攻击时翻倍）
    - 效果不可叠加
    - 减伤效果翻倍效果由策划自行配置 敌方兵种修正系数effect12561SoldierAdjust 
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12561)
public class Checker12561 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (BattleConst.WarEff.DEF_CITY.check(parames.troopEffType) && isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
