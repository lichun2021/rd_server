package com.hawk.game.battle.effect.impl.hero1098;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 12202~12203】
- 【万分比】【12202~12203】蓄能炮：出征或驻防时，自身出征数量最多的突击步兵每第 5 回合开始时，若当前蓄能值不低于 100 点，则额外向敌方全体近战单位进行 3 轮攻击（伤害率：【XX.XX% + 蓄能值*XX.XX%】），并清空自身蓄能值（若当前敌方无近战单位，则攻击敌方全体远程单位）
  - 注：因有两个数值参数，这里用两个作用号开发
    - 【12202】为主作用号，控制各类机制和基础伤害率
    - 【12203】为副作用号，绑定【12202】生效，仅用于计算伤害率系数
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 自身出征数量最多的突击步兵
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的突击步兵（兵种类型 = 5）生效（若存在多个突击步兵数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
  - 每第 5 回合开始时，若当前蓄能值不低于 100 点
    - 每第 5 回合开始时进行判定，回合数读取const表，字段effect12202AtkRound
      - 配置格式：绝对值
    - 每第 5 回合开始时即释放，不是等待该兵种出手时释放
      - 注：这里与杰拉尼和杰西卡的释放逻辑一致，目前在杰拉尼和杰西卡后面
    - 蓄能值 >= 100点时发起额外攻击，阈值读取const表，字段effect12202AtkThresholdValue
      - 配置格式：绝对值
  - 则额外向敌方全体近战单位进行 3 轮攻击（伤害率：【XX.XX% + 蓄能值*XX.XX%】）
    - 攻击目标：
      - 敌方全体近战部队，具体包含有：主战坦克 = 2、防御坦克 = 1、轰炸机 = 3和采矿车 = 8
      - 若当前敌方无近战部队，则攻击敌方全体远程部队，具体包含有：直升机 = 4、狙击兵 = 6、突击步兵 = 5和攻城车 = 7
        - 每轮攻击独立判定目标
    - 攻击轮次读取const表，字段effect12202AtkTimes
      - 配置格式：绝对值
    - 实际伤害率 = 基础伤害率 * 各兵种修正系数
      - 基础伤害率 = 【12202】 + 蓄能值*【12203】
      - 各兵种修正系数读取const表，字段effect12202DamageAdjust
        - 配置格式：兵种1修正系数_兵种2修正系数_......_兵种8修正系数 
          - 各修正系数配置为万分比
  - 另外在触发【12202】的范围技能时，不计算蓄能值；且在释放完成后，将当前蓄能值清空至0点
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12203)
public class Checker12203 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12201) == 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = parames.unity.getEffVal(effType());
		int effNum = 0;

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
