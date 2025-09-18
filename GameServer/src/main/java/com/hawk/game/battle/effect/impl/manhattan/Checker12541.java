package com.hawk.game.battle.effect.impl.manhattan;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12541】
- 【万分比】【12541】战斗效果：进攻其他指挥官基地战斗开始时，随机选中敌方某部队（数量越多的部队被选中概率越高），使其在整场战斗中，每 X(effect12541AtkRound)  回合开始时，对其友方随机部队进行 1（effect12541AtkNum） 次额外攻击，伤害率为 XX%【12541】 ->需要配置消灭数量修正系数XX%（effect12541LoseAdjust）
  - 战报相关
    - 于战报中显示（需客户端配合@李有鹏）
[图片]
      - 参考吉迪恩技能黑洞吞噬作用号【1535】【1536】，【1536】造成的损失
      - 将敌方被控制单位造成的战损，显示在进攻方 消灭后方的问号处
        - 如果同时有多个技能在 消灭问号 处有战报信息显示，则将本字段显示在同一Tips中，不同行显示
          - 排序规则：
            - 如果字段关联芯片，则芯片id越大的越靠后
            - 如果关联作用号，则作用号越大的越靠后
        - 本次战斗中，敌方{0}的{1}星{2}部队被心灵控制，造成敌方损伤{3}（key =@PvpMail15421）
        - 参数{0}：被选中的玩家名称
        - 参数{1}：被选中的部队，其兵种星级（读取battle_soldier表，id对应的level字段）
        - 参数{2}：被选中的部队，其兵种类型（读取battle_soldier表，id对应的name字段）
        - 参数{3}：本场战斗中，被控制的敌方单位造成的实际消灭数量
    - 不合并至精简战报中
  - 该作用号仅在进攻战斗时生效（包含个人进攻和集结进攻）
    - 集结时，进攻方均可生效此作用号，相互独立
  - 该作用号仅在进攻其他玩家基地时生效
    - 注：真实玩家基地才生效
- 随机选中敌方某部队（敌方数量越多的部队被选中概率越高）
  - 以敌方各部队数量为权重，进行去重随机
    - 集结进攻时，多个玩家的心灵控制器不会选中同一单位
  - 选择部队在战斗开始前判定，满足条件后本场战斗不会改变
- 使其在整场战斗中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
- 每 X 回合开始时
  - 战斗中每第 X 的倍数的回合开始后，双方开始普攻前，进行判定
  - 指定回合数读取const表，字段effect12541AtkRound
    - 配置格式：绝对值
- 对其友方随机部队进行 1 次额外攻击
  - 其友方部队表示其被控制前的友方单位，包括其自身其他单位和集结时的友方单位
  - 读取const表，字段effect12541AtkNum
    - 配置格式：绝对值
  - 注：如果没有其他部队，则不进行额外攻击
- 伤害率为 XX%
  - 即 实际伤害 = 伤害率 * 基础伤害 *（1 + 各类加成）
- 被控制单位 消灭数量修正系数XX%
  - 被控制单位 攻击的回合进行检测，攻击后进行如下检测，并修正本次攻击消灭数量
    - 需满足：被控制单位累计消灭总数<= 进攻方累计消灭总数 * XX%
      - 如不满足，则被控制单位 本回合消灭数降低，直至满足上述条件
      - 即 被控制单位 本回合消灭数 = 进攻方累计消灭总数 * XX% — 控制单位此次攻击前累计消灭数 
  - 注：造成的消灭数量不记在被选中单位上，单独记在控制该单位的玩家战报中
  - 读取const表，字段effect12541LoseAdjust
    - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12541)
public class Checker12541 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.CITY_ATK.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		String pid = parames.unity.getPlayerId();
		BattleUnity max = parames.getPlayeMaxMarchArmy(pid);

		if (parames.unity != max) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}


	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
