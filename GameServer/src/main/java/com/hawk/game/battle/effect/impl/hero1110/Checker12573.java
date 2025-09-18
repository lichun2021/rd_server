package com.hawk.game.battle.effect.impl.hero1110;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 *12573】
- 【万分比】【12573】损坏追惩：当敌方【损坏状态】结束后，在接下来 3 回合中，回合结束时会受到一次附加伤害，伤害值为该单位本回合所受伤害的【固定值(effect12571BaseVaule)+XX.XX%（作用号12573）*进入损坏状态次数】。->针对近战兵种留个内置伤害系数（effect12573SoldierAdjust）
- 当敌方【损坏状态】结束后，在接下来 3 回合中
  - 在【损坏状态】结束回合的下一回合开始时，才开始触发
  - 持续回合数读取const表，字段effect12573ContinueRound
    - 配置格式：绝对值
- 回合结束时会受到一次附加伤害
  - 回合结束时 即 该回合内所有单位行动完成，且回合真实结束前触发
    - 如有多有 回合结束时 触发的效果，则按作用号从小到大顺序依次触发
- 伤害值为该单位本回合所受伤害的【固定值+XX.XX%*进入损坏状 态次数】。
  - 附加伤害 = 该单位本回合受到总伤害数值 *（固定值+【本作用值】*  进入【损坏状态】次数）* 敌方兵种修正系数/10000）
    - 附加伤害：不再计算其防御、增减伤和其他作用号影响，直接对单位造成该值的伤害
    - 固定值读取const表，字段effect12573BaseVaule
      - 配置格式：万分比
    - 作用值读取作用号【12573】值
      - 配置格式：万分比
    - 进入【损坏状态】次数 为 敌方单位被莱万施加【损坏状态】的次数，施加时计次逻辑如下
      - 如果该单位未处于【损坏状态】，则成功施加【损坏状态】，记录次数 +1
      - 如果该单位已处于【损坏状态】，则刷新【损坏状态】持续回合数，记录次数 +1
  - ->针对近战兵种留个内置伤害系数
    - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12573SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id4_修正系数4
        - 修正系数具体配置为万分比
    - 配置格式：万分比
  - 叠加逻辑
    - 多个采矿车单位，单独结算伤害
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12573)
public class Checker12573 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.EFF_12571) <= 0) {
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
