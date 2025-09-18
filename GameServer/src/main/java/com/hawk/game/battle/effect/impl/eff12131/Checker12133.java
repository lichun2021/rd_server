package com.hawk.game.battle.effect.impl.eff12131;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 【12133】
- 【万分比】【12133】磁暴干扰：触发荣耀凯恩的【磁暴聚能】效果后，额外向敌方随机 1 个空军部队追加 1 次攻击（伤害率XX.XX%，优先选择轰炸机），
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 触发荣耀凯恩的【磁暴聚能】效果后
    - 该作用号绑定英雄荣耀凯恩的专属芯片【磁暴聚能】
    - 荣耀凯恩 = 1078
    - 【磁暴聚能】对应作用号【1654~1657】
[图片]
  - 空军部队类型包含有：直升机（兵种类型 = 4）和轰炸机（兵种类型 = 3）
  - 该作用号效果为主战坦克在普通攻击后，额外发起的1次追击效果
    - 即需要主战坦克本体有普通攻击行为后，才能触发
    - 注：【芯片】主战坦克连击的效果不会触发此作用号
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
  - 伤害率（XX.XX%）
    - 即 实际伤害 = 本次伤害率 * 基础伤害
    - 注：除伤害率单独计算外，其他诸如是否暴击、暴击伤害等所有属性效果均生效
  - 随机规则
    - 作用号生效后，优先在敌方轰炸机中随机1个目标，若敌方无轰炸机，则在敌方直升机中随机1个目标，若敌方也无直升机，则判定失去攻击目标，此次攻击失效
 * @author lwt
 * @date 2023年12月4日
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12133)
public class Checker12133 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
