package com.hawk.game.battle.effect.impl.hero1104;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 *  - 万分比】【12414】轰炸机在释放轮番轰炸技能时，额外附加如下效果:潜能爆发: 个人战时，轮番轰炸发动轮次额外 +XX 且每发动 1 次轮番轰炸后，自身受到攻击时伤害减少 +XX.XX%（该效果可叠加，至多 X 层）（受敌方兵种类型修正）
  - 注：此作用号依旧绑定轮番轰炸作用号【12051】
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 该作用号仅在个人战时生效（包含个人进攻和个人防守）
  - 轮番轰炸发动轮次额外 +XX
    - 这里仅在个人战时生效，轮番轰炸可攻击轮次受各种作用号影响，这里列下具体的
    - 【个人战时】实际轮次 = 基础轮次 + 【伊娜莉莎专属技能】+ 【伊娜莉莎专属技能】 + 【本作用值】
      - 基础轮次：作用号【12051】的基础参数，个人战斗时（包含个人进攻和个人防守，只有自身1个玩家即为生效）读取const表，字段effect12051AtkTimesForPerson
        - 配置格式：绝对值
      - 【伊娜莉莎专属技能】：作用号【12066】的基础参数；额外可攻击轮次读取const表，字段effect12066AtkTimes
        - 配置格式：绝对值
      - 【伊娜莉莎专属军魂】：作用号【12271】的基础参数；个人战斗时读取const表，字段effect12271AtkTimesForPerson
        - 配置格式：绝对值
      - 【本作用值】：不随作用号数值变化（作用值>0，则生效此基础参数）；读取const表，字段effect12414AtkTimesForPerson
  - 每发动 1 次轮番轰炸后，自身受到攻击时伤害减少 +XX.XX%（该效果可叠加，至多 XX%）
    - 这里仅在个人战时生效
    - 每发动 1 次：以轮番轰炸发动时实际攻击的次数进行计算（也就是会受轮番轰炸单轮可攻击次数的影响）
    - 自身受到伤害：不同兵种id独立计算，这里仅对发起轮番轰炸的轰炸机生效
    - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
  - （受敌方兵种类型修正）
    - 展示给玩家的面板数值为：【作用值】* 叠加层数
    - 实际生效的数值随敌方兵种类型变化
      - 即实际数值 = 【作用值】*叠加层数 * 【兵种修正系数】
        - 各兵种修正系数读取const表，字段effect12414Adjust
          - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
            - 修正系数具体配置为万分比
  - （该效果可叠加，至多 X 层）
    - 该作用号可叠加，受叠加次数上限限制
      - 层数上限读取const表，字段effect12414Maxinum
        - 配置格式：绝对值
 *
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12414)
public class Checker12414 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.EFF_12051) <= 0 || BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		if (parames.tarStatic.getPlayerArmyCountMap().size() > 1) {
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
