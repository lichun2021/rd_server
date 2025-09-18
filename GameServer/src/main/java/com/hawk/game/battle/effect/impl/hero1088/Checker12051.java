package com.hawk.game.battle.effect.impl.hero1088;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 - 【万分比】【12051~12052】若自身出征轰炸机（兵种类型 = 3）数量超过自身出征部队总数 50%，自身出征数量最多的轰炸机单位在本场战斗中获得如下效果: 【迂回轰炸】每第 5 回合，额外向敌方远程随机 1 个单位进行 18 轮攻击（伤害率: XX.XX%+轮次数*YY.YY%；每轮攻击独立选择目标，选取目标优先遍历所有远程兵种类型）
  - 注：这里由于数值不一致，分2个作用号开发
    - 主体作用号为【12051】，控制整体机制和基础伤害率XX.XX%
    - 附带作用号为【12052】，在【12051】生效时，按轮次给予其额外伤害率加成
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
      - 指定数值读取const表，字段effect12051SelfNumLimit
        - 配置格式：万分比
  - `自身出征数量最多的轰炸机单位在本场战斗中获得如下效果
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的轰炸机（兵种类型 = 3）生效（若存在多个轰炸机数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
  - 【迂回轰炸】每第 5 回合，额外向敌方远程随机 1 个单位进行 18 轮攻击（伤害率: XX.XX%+轮次数*YY.YY%；每轮攻击独立选择目标，选取目标优先遍历所有远程兵种类型）
    - 每第 5 回合
      - 战斗中每第 5 的倍数的回合开始后，自身开始普攻前，额外进行1次攻击（可以理解为释放1次技能效果）
      - 指定回合数读取const表，字段effect12051AtkRound
        - 配置格式：绝对值
    - 敌方远程随机 1 个单位
      - 远程部队类型包含有：直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
      - 随机机制如下
        - 大轮；取敌方现有所有兵种类型，排序如下【狙击兵 = 6，攻城车 = 7，突击步兵 = 5，直升机 = 4】
        - 按大轮顺序依次从对应兵种类型中随机1个攻击目标
          - 若对应兵种类型下所有敌方目标均阵亡，则索寻下个兵种类型下的敌方目标
          - 成功完成攻击后，可攻击轮次 -1
        - 完成一大轮攻击后，重新按当前敌方远程兵种类型进行下一大轮
        - 直至完成所有攻击轮次 或 敌方远程兵种全部阵亡 后结束
    - 基础攻击轮次随战斗类型有所区别
      - 集结战斗时（包含集结进攻和集结防守，存在友军玩家即为生效）读取const表，字段effect12051AtkTimesForMass
        - 配置格式：绝对值
      - 个人战斗时（包含个人进攻和个人防守，只有自身1个玩家即为生效）读取const表，字段effect12051AtkTimesForPerson
        - 配置格式：绝对值
    - 伤害率（XX.XX%）+轮次数*YY.YY%
      - 即 实际伤害 = 本次伤害率 * 基础伤害
        - 本次伤害率 = 基础伤害率XX.XX% + 当前攻击轮次*YY.YY%
          - 基础伤害率XX.XX%由作用号【12051】系数决定
          - 伤害增长率YY.YY%由作用号【12052】系数决定
          - 当前攻击轮次在当前回合发动大轮攻击开始时，重置为1
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12051)
public class Checker12051 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!check(parames,ConstProperty.getInstance().getEffect12051SelfNumLimit())) {
			return CheckerKVResult.DefaultVal;
		}

		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}

	public static boolean check(CheckerParames parames,int selfNumLimit) {
		if (parames.type != SoldierType.PLANE_SOLDIER_3) {
			return false;
		}
		String playerId = parames.unity.getPlayerId();
		BattleUnity maxUnity = parames.getPlayerMaxFreeArmy(playerId, SoldierType.PLANE_SOLDIER_3);
		if (maxUnity != parames.unity) {
			return false;
		}

		int march3cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.PLANE_SOLDIER_3);
		// 若自身出征轰炸机（兵种类型 = 3）数量超过自身出征部队总数 50%，自身出征数量最多的轰炸机单位在本场战斗中获得如下效果: 【迂回轰炸】每第 5 回合，额外向敌方远程随机 1 个单位进行 18 轮攻击（伤害率: XX.XX%+轮次数*YY.YY%；每轮攻击独立选择目标，选取目标优先遍历所有远程兵种类型）
		if (march3cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) <= selfNumLimit * GsConst.EFF_PER) {
			return false;
		}
		return true;
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
