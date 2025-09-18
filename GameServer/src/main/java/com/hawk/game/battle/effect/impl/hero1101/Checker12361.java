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
 * - 【万分比】【12361】定点猎杀: 自身出征数量最多的狙击兵在发起攻击时，有 XX.XX% 的概率越过防御坦克，选择敌方最脆弱的单位（生命值越低的单位，被选中的概率越高）进行攻击（伤害率: XX.XX%）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 自身出征数量最多的狙击兵
    - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的狙击兵（兵种类型 = 6）生效（若存在多个狙击兵数量一样且最高，取等级高的）
      - 在战斗开始前进行判定，选中目标后战中不再变化
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队无效！！
  - 有 XX.XX% 的概率越过防御坦克，选择敌方最脆弱的单位（生命值越低的单位，被选中的概率越高）
    - XX.XX%的概率数值为固定参数，读取const表，字段effect12361BasePro
      - 配置格式：万分比
    - 选择敌方最脆弱的目标，就是配置各兵种被选中的权重，读取const表，字段effect12361TargetWeight
      - 配置格式：兵种类型id1_权重1，兵种类型id2_权重2......兵种类型id7_权重7
    - 若触发时，敌方可选取目标只有防御坦克，则攻击防御坦克单位；若无可选取目标，则此次攻击无效
  - （伤害率: XX.XX%）
    - 另外此伤害对于各兵种有单独修正系数（玩家不可见），读取const表，字段effect12361DamageAdjust
      - 配置格式：兵种1修正系数_......_兵种8修正系数
    - 即实际伤害 = 伤害率 * 修正系数 *基础伤害 *（1 + 各类加成）
  - 注：此作用号与娜塔莉专属作用号【1617】、艾薇儿专属兵种技能【60501】无法共存
    - 具体实现方式为：【12361】>>【1617】>>【60501】；即存在靠前的作用号时，靠后作用号则无效
  - 注：此作用号与埃托利亚专属作用号【1673】可以共存
    - 当两者同时存在时，实际概率 = 【12361】+【1673】
 * @author lwt
 * @date 2024年7月8日
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12361)
public class Checker12361 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.FOOT_SOLDIER_6)) {
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
