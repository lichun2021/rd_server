package com.hawk.game.battle.effect.impl.hero1108;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
【12513】
- 【万分比】【12513】自身出征数量最多的主战坦克在触发坦克对决时，本次攻击额外附加如下效果：
  - 歼灭突袭：额外向敌方随机 1（effect12513AtkNum） 个空军单位（优先选择轰炸机作为目标）追加 1 （effect12513AtkTimes）次攻击，伤害率 固定值（effect12513BaseVaule） + XX.XX%*敌方空军单位数（敌方空军单位计数时至多取 10 个（effect12513CountMaxinum））
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 自身出征数量最多的主战坦克在触发坦克对决时
    - 该作用号效果仅对玩家出征时数量最多的主战坦克（兵种类型 = 2）生效（若存在多个主战坦克数量一样且最高，取等级高的）
      - 注：若存在荣耀所罗门的幻影部队，此作用号对自身幻影部队不生效
    - 此作用号绑定坦克对决作用号【12131】，触发坦克对决时，额外触发效果
      - 注：坦克对决的追加攻击失败时，该作用号也能触发
      - 坦克对决效果见【231221】【SSS】【军事主战】【米卡】 【1093】 
  - 额外向敌方随机 1 个空军单位（优先选择轰炸机作为目标）追加 1 次攻击
    - 空军部队类型包含有：轰炸机（兵种类型 = 3）和直升机（兵种类型 = 4）
    - 该作用号效果为坦克对决效果后，额外发起的追加攻击效果
    - 随机规则
      - 作用号生效后，优先在敌方轰炸机随机选择1个目标，若敌方无可选单位，则在敌方直升机单位中随机1个目标，若敌方也无可选单位则判定失去攻击目标，此次攻击失效
    - 选取单位数值读取const表，字段effect12513AtkNum
      - 配置格式：绝对值
    - 追加攻击次数读取const表，字段effect12513AtkTimes
      - 配置格式：绝对值
  - 伤害率 固定值 + XX.XX%*敌方空军单位数
    - 该作用号固定数值读取const表，字段effect12513BaseVaule
      - 配置格式：万分比
    - 伤害率
      - 即 实际伤害 =基础伤害 *（1 + 各类加成）* 伤害率
  - （敌方空军单位计数时至多取 10 个）
    - 该计数有最高值限制，读取const表，字段effect12513CountMaxinum
      - 配置格式：绝对值
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12513)
public class Checker12513 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12131) <= 0|| parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.TANK_SOLDIER_2)) {
			return CheckerKVResult.DefaultVal;
		}
		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}