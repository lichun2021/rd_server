package com.hawk.game.battle.effect.impl.hero1104;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * - - 【万分比】【12412】轰炸机在释放轮番轰炸技能时，额外附加如下效果:温压爆弹: 轮番轰炸每次命中敌方单位后，额外向其所在区域投放 1 枚温压弹，对其距离最近的 2 个敌方单位造成 1 次轰爆伤害（伤害率: XX.XX%）
  - 注：此作用号依旧绑定轮番轰炸作用号【12051】，在【12051】释放过程中，每次命中敌方单位时都生效
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 对其距离最近的 2 个敌方单位造成 1 次轰爆伤害
    - 注：这里只按照距离轮番轰炸的敌方单位进行选择（不再判定是否属于近战或远程），且在轮番轰炸每次攻击时按实际命中目标独立计算
    - 注：这里只能选择受到轮番轰炸的敌方以外的其他单位（兵种类型 = 1~8）
    - 所谓距离最近的 2 个敌方单位
      - 战斗单位排序在原目标前面且最近的 1 个（若原目标前面没有单位，则往后取）
      - 战斗单位排序在原目标后面且最近的 1 个（若原目标后面没有单位，则往前取）
        - 注：若敌方其他战斗单位不足，则有多少取多少（但不能选择原受到轮番轰炸的单位作为目标）
        - 注：若目标数量为奇数，同样距离下，优先取靠后的敌方单位
    - 选取目标单位数量读取const表，字段effect12412AtkNums
      - 配置格式：绝对值
    - 对目标单位的攻击次数读取const表，字段effect12412AtkTimes
      - 配置格式：绝对值
  - （伤害率: XX.XX%）
    - 即 实际伤害 = 伤害率 * 基础伤害 *（1 + 各类加成）
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12412)
public class Checker12412 implements IChecker {

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
