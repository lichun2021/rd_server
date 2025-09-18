package com.hawk.game.battle.effect.impl.hero1110;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 *- 【万分比】【12571~12572】集结战斗开始前，若自身出征采矿车数量超过自身出征部队总数 50%(effect12571SelfNumLimit)且不低于集结部队总数 5%(effect12571AllNumLimit)，自身出征数量最多的采矿车于本场战斗中获取如下效果（多个莱万同时存在时，至多有 2 (effect12571Maxinum)个采矿车单位生效）:
  - 声波震慑：莱万布设声波发射器，使战斗中敌方全体近战单位受到声波震慑影响，每回合结束时受到一次震慑伤害（伤害率：XX.XX%（作用号12571）），且有【固定值(effect12571BaseVaule)+XX.XX%（作用号12572）*受声波震慑影响回合数】 的概率增加 1(effect12571AddFirePoint)  点震慑值；在每回合开始时，当前震慑值不低于 10点 (effect12571AtkThresholdValue)的敌方单位会进入【损坏状态】，持续 X（effect12571ContinueRound） 回合，并且清空自身震慑值（不同采矿车单位施加的震慑值可叠加，【损坏状态】将会触发杰西卡对敌方施加的负面效果） 
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 集结战斗开始前，若自身出征采矿车数量超过自身出征部队总数50%
    - 数量1 = 某玩家出征携带的采矿车（兵种类型 = 8）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12571SelfNumLimit
        - 配置格式：万分比
  - 且不低于集结部队总数 5%
    - 数量1 = 某玩家出征携带的采矿车（兵种类型 = 8）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩、参谋技 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12571AllNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的采矿车在本场战斗中获得如下效果
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的采矿车（兵种类型 = 8）生效（若存在多个采矿车数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对自身幻影部队不生效
  - 声波震慑：莱万布设声波发射器，使战斗中敌方全体近战单位受到声波震慑影响，每回合结束时受到一次震慑伤害（伤害率：XX.XX%（12571））
    - 本次战斗全程生效
    - 近战部队包含：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
    - 回合结束时 即 该回合内所有单位行动完成，且回合真实结束前触发
      - 如有多个 回合结束时 触发的效果，则按作用号从小到大顺序依次触发
    - 伤害率 即 实际伤害 = 伤害率 * 基础伤害 *（1 + 各类加成）
  - 且有【固定值(effect12571BaseVaule)+XX.XX%（12572）*受声波震慑影响回合数】 的概率增加 1(effect12571AddFirePoint)  点震慑值；
    - 固定值读取const表，字段effect12571BaseVaule
      - 配置格式：万分比
    - 附加概率值读取作用号【12572】值
      - 配置格式：万分比
    - 受声波震慑影响回合数 为敌方单位受到 作用号12571效果 的回合数
    - 每个敌方单位独立进行随机，震慑值记录在敌方单位上
      - 增加震慑值读取const表，字段effect12571AddFirePoint
        - 配置格式：绝对值
  - 在每回合开始时，当前震慑值不低于 10点 (effect12571AtkThresholdValue)的敌方单位会进入【损坏状态】
    - 数量1 = 敌方震慑值
    - 数量2 = 触发震慑值
      - 数量1/数量2 >= 指定数量时生效以下效果
        - 指定数值读取const表，字段effect12571AtkThresholdValue
    - 【损坏状态】为一种负面状态标识，记录在敌方单位身上
      - 【损坏状态】原为杰西卡专属状态，此前需要配合杰拉尼触发，此处为了新英雄替换杰拉尼，所以给了相同状态
  - 持续 X（effect12571ContinueRound） 回合，并且清空自身震慑值
    - 回合开始：进入【损坏状态】状态，当前回合算作第1回合
    - 回合结束：当前回合结束时进行判定，如果 当前持续回合=理应持续回合，则清除【损坏状态】状态
    - 持续回合数读取const表，字段effect12571ContinueRound
      - 配置格式：绝对值
  - （不同采矿车单位施加的震慑值可叠加，
    - 集结中存在多个此作用号时，可以同时生效（受2个采矿车生效限制），叠加逻辑如下
      - 声波震慑伤害：每个采矿车分别造成一次伤害
      - 敌方震慑值 = 采矿车A造成震慑值+采矿车B造成震慑值
  - 【损坏状态】将会触发杰西卡对敌方施加的负面效果）
    - 处于【损坏状态】的敌方单位，会触发杰西卡作用号【12164~12171】效果
      - 【240222】【SSS】【军事】【采矿车】【杰西卡】 【1095】  
      - 原本作用号【12164~12171】绑定【12163】触发，此处需要由莱万施加的【损坏状态】触发
[图片]
[图片]
      - 注：如果由莱万施加的【损坏状态】触发，则作用号【12164~12171】持续回合数修改为读取const表，字段effect12571ContinueRound
        - 此前作用号【12164~12171】持续回合数，原本单独配置6个const表字段：effect12164ContinueRound~effect12171ContinueRound
  - 【损坏状态】将会触发卡尔技能中【损坏状态】对敌方施加的负面效果）
    - 处于【损坏状态】的敌方单位，会触发卡尔作用号【12204~12207】效果
    - 【240425】【SSS】【军事】【突击】【卡尔】 【1098】 
    - 此前判断【损坏状态】的逻辑是
      - 目标单位作用号【12164~12171】任意一个大于0，即为【损坏状态】
        - 此处如果杰西卡的作用号【12164~12171】逻辑不变，则此处不需要变动
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12572)
public class Checker12572 implements IChecker {
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
