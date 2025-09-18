package com.hawk.game.battle.effect.impl.eff12101;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 【12101】
- 【万分比】【12101】任命在战备参谋部时，部队伤害增加  +XX.XX%
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 该作用号为额外伤害加成，在计算时与其他伤害加成累加计算
    - 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用值】）
  - 注：这里对轰炸机（兵种类型 = 3）单独处理，仅在其发动兵种技能【俯冲轰炸】【id = 303】时生效
【12102】
- 【万分比】【12102】任命在战备参谋部时，部队伤害增加  +XX.XX%*己方出战兵种类型数
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 该作用号为额外伤害加成，在计算时与其他伤害加成累加计算
    - 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用值】）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 己方出战兵种类型数：己方各兵种类型（1~8）出战的去重计数（即取值【1~8】之间）
    - 单人时取单人所有部队
    - 集结时取集结己方所有玩家的所有部队
  - 注：这里对轰炸机（兵种类型 = 3）单独处理，仅在其发动兵种技能【俯冲轰炸】【id = 303】时生效

【12103】
- 【万分比】【12103】任命在战备参谋部时，步兵受到攻击时，伤害减少 +XX.XX%
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 该作用号为伤害减少效果，与其他作用号累乘计算
    - 即 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】）
  - 步兵类型：狙击兵（兵种类型 = 6）和突击步兵（兵种类型 = 5）

【12104】
- 【万分比】【12104】任命在战备参谋部时，步兵受到攻击时，伤害减少 +XX.XX%*敌方空军单位数
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 该作用号为伤害减少效果，与其他作用号累乘计算，但是与【12103】累加计算
    - 即 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【12103】-【本作用值】）
  - 步兵类型：狙击兵（兵种类型 = 6）和突击步兵（兵种类型 = 5）
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 敌方空军单位数：敌方战报中的单排空军计数
    - 不同玩家不同等级的空军均视为独立单位
    - 空军：直升机（兵种类型 = 4）和轰炸机（兵种类型 = 3）
    - 单人时取敌方单人所有空军部队
    - 集结时取集结敌方所有玩家的所有空军部队
    - 另外该计算有最高值限制，读取const表，字段effect12104MaxNum
      - 配置格式：绝对值
 * @author lwt
 * @date 2023年9月27日
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12103)
public class Checker12103 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_5 || parames.type == SoldierType.FOOT_SOLDIER_6) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
