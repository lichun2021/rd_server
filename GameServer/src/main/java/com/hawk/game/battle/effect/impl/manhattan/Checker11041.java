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
 * 【11041】
- 【万分比】【11041】进攻战斗时，在战斗开始时，随机选中敌方某指挥官的全部部队进行 1 轮核弹轰炸（伤害率：XX%；此效果对步兵 翻倍）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始时判定，满足条件后本次战斗生效1次
    - 这里可理解为在战斗开始第1回合时，向敌方甩了个指挥技
  - 该作用号仅在进攻战斗时生效（包含个人进攻和集结进攻）
    - 集结时，进攻方均可生效此作用号，各甩各的，相互独立
  - 随机选中敌方某指挥官的全部部队
    - 按敌方参战玩家进行纯随机，选中后攻击目标为该玩家本次战斗的全部部队
      - 注：优先选中被此作用号攻击次数最少的敌方玩家
        - 即：每次选择目标时，筛选出被此作用号攻击次数最少的敌方玩家，从中随机1个作为目标
      - 注：若为NPC部队，则按NPC所属进行随机
  - （伤害率：XX%；此效果对步兵 翻倍）
    - 生效此作用号的进攻方，该次攻击绑定在其出征数量最多的部队身上（战报击杀数据也记录在其身上）
      - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
      - 若存在多个部队数量一样且最高，取等级高的；若存在多个部队数量一样且最高且等级最高，取兵种类型小的部队（最后有且仅有1个战斗单位有此效果）
    - 另外此伤害对于各兵种有单独修正系数（玩家不可见），读取const表，字段effect11041DamageAdjust
      - 配置格式：兵种类型id1_修正系数1,......兵种类型id8_修正系数8
    - 即实际伤害 = 伤害率 * 修正系数 *基础伤害 *（1 + 各类加成）
  - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队无效！！
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_11041)
public class Checker11041 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.ATK.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		String pid = parames.unity.getPlayerId();
		BattleUnity max = parames.getPlayeMaxMarchArmy(pid);

		if (parames.unity != max) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}


	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
