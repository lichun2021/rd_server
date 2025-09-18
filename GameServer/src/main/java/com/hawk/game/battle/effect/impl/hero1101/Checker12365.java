package com.hawk.game.battle.effect.impl.hero1101;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 *- 【万分比】【12365】战车掩护: 若存在友军攻城车单位，狙击兵有 【友军出征攻城车数量/万 * XX.XX%】 （友军出征攻城车数量在此处计算时最多取 100 万）的概率不被定点猎杀选中为目标
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 注：此作用号绑定在作用号【12361】生效的目标单位上
  - 即在敌方【12361】判定触发时，我方狙击兵单位若满足作用号条件且同时判定成功，则在敌方可选择目标上屏蔽掉我方狙击兵
  - 具体流程为
    - 敌方发起攻击，【12361】判定成功，开始选取目标单位
    - 我方是否存在携带此作用号的狙击兵单位
      - 存在。继续
      - 不存在。结束
    - 我方当前是否存在友军攻城车单位
      - 存在。继续
      - 不存在。结束
    - 我方狙击兵进行概率判定
      - 判定成功。我方所有狙击兵单位于此次选取目标时，均不会被选中；敌方【12361】按处理后的权重进行目标选取
      - 判定失败。结束；敌方【12361】按正常权重进行目标选取
  - 友军出征攻城车数量
    - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 友军单位：即自身携带的不计入，只算友军（其他玩家）的单位
    - 攻城车兵种类型 = 8
  - （友军出征攻城车数量在此处计算时最多取 100 万）
    - 即在此概率计算时，此处参数数量有最高值限定。具体数值读取const表，字段effect12365MaxNum
      - 配置格式：绝对值
  - 【友军出征攻城车数量/万 * XX.XX%】
    - 实际概率 = 【友军出征攻城车数量/10000】（向下取整）*【本作用值】
    - 注：此为概率判定数值。最终数值合法范围为【0,100%】
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12365)
public class Checker12365 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12361) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6) {
			long f8cnt = parames.unitStatic.getArmyCountMapMarch().get(SoldierType.CANNON_SOLDIER_7)
					- parames.unitStatic.getPlayerSoldierCountMarch().get(parames.unity.getPlayerId(), SoldierType.CANNON_SOLDIER_7);
			int cen = (int) (Math.min(ConstProperty.getInstance().getEffect12365MaxNum(), f8cnt) / 10000);
			effPer = parames.unity.getEffVal(effType()) * cen;
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
