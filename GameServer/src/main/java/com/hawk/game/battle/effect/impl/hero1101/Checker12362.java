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
 * 【12362】
- 【万分比】【12362】精准打击: 被定点猎杀命中的敌方单位，有 XX.XX% 的概率因武器遭受打击而使其丧失 XX.XX% 的伤害效率（该效果无法叠加，持续 2 回合）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制绑定在【12361】上，只对【12361】作用号命中的敌方单位生效
  - 触发概率读取const表，字段effect12362BasePro
    - 配置格式：万分比
  - 因武器遭受打击而使其丧失 XX.XX% 的伤害效率
    - 该效果为debuff效果，降低敌方攻击时造成的伤害值
    - 该效果计算时与其他作用号累乘计算，即
      - 实际伤害 = 基础伤害 * （1 + 各类加成）*（1 - 【本作用值】）
    - 另外此效果对于各兵种有单独修正系数（玩家不可见），读取const表，字段effect12362Adjust
      - 配置格式：兵种类型id1_修正系数1，兵种类型id2_修正系数2......兵种类型id7_修正系数7
      - 即实际效果数值 = 【12362】* 修正系数 
  - （该效果无法叠加，持续 2 回合）
    - 即某单位挂上该作用号后，后续再生效此作用号时，数值不变且持续回合数不变
    - 持续回合数读取const表，字段effect12362ContinueRound
      - 配置格式：绝对值
      - 注：从被赋予作用号开始到本回合结束后，算作1回合
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12362)
public class Checker12362 implements IChecker {

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
