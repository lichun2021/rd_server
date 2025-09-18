package com.hawk.game.battle.effect.impl.hero1104;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * - 【万分比】【12411】轰炸机在释放轮番轰炸技能时，额外附加如下效果:领域突破: 若当前敌方无远程单位，轮番轰炸有 XX.XX% 的概率将目标调整至敌方近战单位
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 以下为轮番轰炸作用号【12051】的原先的选取目标逻辑
[图片]
  - 此作用号绑定轮番轰炸作用号【12051】，在释放【12051】时进行判定（不同等级兵、不同回合独立判定）；具体如下
    - 若释放时敌方存在远程兵种，则此作用号无效，走原先的【12051】正常逻辑
    - 若释放时敌方不存在远程兵种，则按概率进行此次判定
      - 若判定成功：则走下述流程（同原先【12051】的目标选取规则，只是都换成近战单位类型）
        - 敌方近战随机 1 个单位
          - 近战部队类型包含有：防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、主战坦克（兵种类型 = 2）、轰炸机（兵种类型 = 3）
          - 随机机制如下
            - 大轮；取敌方现有所有兵种类型，排序如下【防御坦克 = 1，采矿车 = 2，主战坦克 = 3，轰炸机 = 4】
            - 按大轮顺序依次从对应兵种类型中随机1个攻击目标
              - 若对应兵种类型下所有敌方目标均阵亡，则索寻下个兵种类型下的敌方目标
              - 成功完成攻击后，可攻击轮次 -1
            - 完成一大轮攻击后，重新按当前敌方近战兵种类型进行下一大轮
            - 直至完成所有攻击轮次 或 敌方近战兵种全部阵亡 后结束
      - 若判定失败：则【12051】作用号无法发动
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12411)
public class Checker12411 implements IChecker {

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
