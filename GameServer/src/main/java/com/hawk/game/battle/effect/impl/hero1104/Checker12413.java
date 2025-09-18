package com.hawk.game.battle.effect.impl.hero1104;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 *  - 【万分比】【12413】轰炸机在释放轮番轰炸技能时，额外附加如下效果:返航掩护: 轮番轰炸结束后，该轰炸机单位受到非主战坦克单位攻击时，智能护盾发动概率额外 +XX.XX%（该效果不可叠加，持续 X 回合）
  - 注：此作用号依旧绑定轮番轰炸作用号【12051】，在【12051】释放结束后，开始生效
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 该轰炸机单位受到非主战坦克单位攻击时
    - 注：只对发起轮番轰炸的那个轰炸机单位生效
    - 注：在受到所有兵种类型（1~8）都生效，主战坦克 = 2 除外
  - 智能护盾发动概率额外 +XX.XX%
    - 智能护盾为轰炸机兵种（兵种类型 = 3）的专属兵种技能（id = 302）
    - 其发动有概率限制，基础概率读取battle_soldier_skill表，字段trigger
      - 配置格式：绝对值（目前为18%）
    - 此处加成计算方式为
      - 最终概率 = 基础概率*（1 + 本作用值）- 【12053】+ 其他作用值（若有）
        - 此为概率判定数值，最终数值合法区间【0,100%】
  - （该效果不可叠加，持续 X 回合）
    - 不可叠加：再次赋予时数值不变，且持续回合数不变
    - 持续 X 回合（本回合被附加到下回合开始算 1 回合）
      - 持续回合数读取const表，字段effect12413ContinueRound
        - 配置格式：绝对值
 *
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12413)
public class Checker12413 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.EFF_12051) <= 0) {
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
