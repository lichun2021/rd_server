package com.hawk.game.battle.effect.impl.hero1088;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;

/**
【12053】
- 【万分比】【12053】若自身出征轰炸机（兵种类型 = 3）数量超过自身出征部队总数 50%，自身出征数量最多的轰炸机单位在本场战斗中获得如下效果: 【轻装简行】降低自身【智能护盾】 发动概率 +XX.XX%
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 若自身出征轰炸机数量超过自身出征部队总数 50%
    - 数量1 = 某玩家出征携带的轰炸机（兵种类型 = 3）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12053SelfNumLimit
        - 配置格式：万分比
  - `自身出征数量最多的轰炸机单位在本场战斗中获得如下效果
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的轰炸机（兵种类型 = 3）生效（若存在多个轰炸机数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
  - 【智能护盾】【id = 302】为轰炸机兵种基础技能，此处降低其触发概率
    - 即实际概率 = 基础概率 + 其他概率加成 - 【本作用值】
      - 基础概率读取battle_soldier_skill表，字段trigger
      - 该数值为概率判定；最终取值介于【0,100%】
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12053)
public class Checker12053 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!Checker12051.check(parames,ConstProperty.getInstance().getEffect12053SelfNumLimit())) {
			return CheckerKVResult.DefaultVal;
		}

		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
