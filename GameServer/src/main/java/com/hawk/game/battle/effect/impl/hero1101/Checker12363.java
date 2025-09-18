package com.hawk.game.battle.effect.impl.hero1101;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
/**
 * - 【万分比】【12363】爆裂弹片: 定点猎杀命中敌方单位后，额外对其附近随机 2 个敌方单位产生 1 次爆裂伤害（伤害率: XX.XX%）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制绑定在【12361】上，在【12361】命中敌方后才能生效
  - 额外对其附近随机 2 个敌方单位
    - 目标不包含被【12361】命中的单位
    - 附近随机实际上就是配置各兵种被选中的权重，读取const表，字段effect12363TargetWeight
      - 配置格式：兵种类型id1_权重1，兵种类型id2_权重2......兵种类型id7_权重7
    - 可选取敌方单位数量读取const表，字段effect12363TargetNum
      - 配置格式：绝对值
    - 若敌方剩余目标数不足则有多少取多少，若为0则此作用号在本次攻击时无效
  - （伤害率: XX.XX%）
    - 即实际伤害 = 伤害率 * 基础伤害 *（1 + 各类加成）
  - 注：此爆裂伤害命中敌方目标后，可同时触发埃托利亚的专属作用号【1670】的持续点燃伤害效果
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12363)
public class Checker12363 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12361) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

	
}
