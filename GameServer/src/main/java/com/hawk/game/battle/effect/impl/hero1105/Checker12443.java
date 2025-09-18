package com.hawk.game.battle.effect.impl.hero1105;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 *12443】
- 【万分比】【12443】任命在战备参谋部时，自身出征数量最多的轰炸机单位在触发【智能护盾】后，自身攻击、超能攻击 +XX.XX%（该效果不可叠加，持续 3 回合）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 自身出征数量最多的轰炸机单位在触发【智能护盾】后
    - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的轰炸机（兵种类型 = 3）生效（若存在多个轰炸机数量一样且最高，取等级高的）
    - 注：此作用号绑定轰炸机兵种（兵种类型 = 3）的专属兵种技能智能护盾（id=302），触发技能后生效
  - 自身攻击，超能攻击 +XX.XX%
    - 自身：这里仅对触发【智能护盾】的轰炸机生效
    - 该作用号为常规外围属性加成效果，与其他作用号累加计算）
      - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - （该效果不可叠加，持续 3 回合）
    - 不可叠加：再次赋予时数值不变，且持续回合数不变
    - 持续 X 回合（本回合被附加到下回合开始算 1 回合）
      - 持续回合数读取const表，字段effect12443ContinueRound
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.SOLDIER_SKILL })
@EffectChecker(effType = EffType.HERO_12443)
public class Checker12443 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);

	}

}
