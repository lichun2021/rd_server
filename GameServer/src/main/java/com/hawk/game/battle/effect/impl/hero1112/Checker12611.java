package com.hawk.game.battle.effect.impl.hero1112;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * - 【万分比】【12611】爆裂雷弹：自身出征数量最多的突击步兵攻击时，有XX.XX%(effect12611BaseVaule)的概率触发爆裂雷弹，对敌方随机近战单位及其距离最近2（effect12611AtkNums）个近战单位造成1（effect12611AtkTimes）次额外攻击（伤害率+XX.XX%（12611），如无近战单位则选择远程单位），所有受击目标进入【雷感状态】持续5（effect12611ContinueRound）回合（该效果不可叠加，集结时每个突击步兵单位造成的【雷感状态】，都仅对自身部队生效，每个单位最多同时受到 X （effect12611Maxinum）次【雷感状态】）->针对敌方兵种留个内置随机权重
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 自身出征数量最多的突击步兵攻击时，有XX.XX%的概率触发爆裂雷弹
    - 仅自身出征数量最多的突击步兵攻击时触发
    - 概率读取const表，字段effect12611BaseVaule
      - 配置格式：万分比
  - 对敌方随机近战单位
    - 随机规则
      - 作用号生效后，优先在敌方近战单位中随机选择1个目标，若敌方无可选单位，则在敌方所有单位中随机选择1个目标
        - 近战部队类型包含有：防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、主战坦克（兵种类型 = 2）、轰炸机（兵种类型 = 3）
        - 近战单位中选择各兵种类型随机权重读取const表，字段effect12611RoundWeight
          - 配置格式：类型1_权重1,类型2_权重2……类型8_权重8
  - 及其距离最近2个近战单位造成1次额外攻击
    - 此处机制类似作用号【12412】【241024】【SSS】【军事】【轰炸】【尤利娅】【Yulia】【1104】
    - 按照随机目标计算最近距离，且在每次触发爆裂雷弹时独立计算
    - 注：这里只能选择随机目标以外的其他近战单位
      -  主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
    - 所谓距离最近的 2 个敌方近战单位
      - 战斗单位排序在原目标前面且最近的 1 个（若原目标前面没有近战单位，则往后取）
      - 战斗单位排序在原目标后面且最近的 1 个（若原目标后面没有近战单位，则往前取）
        - 若敌方其他战斗单位不足，则有多少取多少
        - 不能选择原受到攻击的单位作为目标
        - 注：若目标数量为奇数，同样距离下，优先取靠后的敌方单位
    - 选取目标单位数量读取const表，字段effect12611AtkNums
      - 配置格式：绝对值
    - 对目标单位的攻击次数读取const表，字段effect12611AtkTimes
      - 配置格式：绝对值
  - （伤害率: XX.XX%，如无近战单位则选择远程单位）
    - 即 实际伤害 = 伤害率 * 基础伤害 *（1 + 各类加成）
      - 伤害率=作用号值/10000
      - 配置格式：万分比
  - 所有受击目标进入【雷感状态】持续5回合
    - 【雷感状态】为施加于敌方身上的负面状态
    - 回合开始：进入状态的回合，当前回合算作第1回合
    - 回合结束：当前回合结束时进行判定，如果 当前持续回合=理应持续回合，则清除状态
    - 持续回合数读取const表，字段effect12611ContinueRound
      - 配置格式：绝对值
  - （该效果不可叠加，
    - 单位拥有雷感状态时再被施加，会重置持续回合数，而非生效2次
  - 集结时每个突击步兵单位造成的【雷感状态】，都仅对自身部队生效
    - 多个玩家的雷感状态独立，不会交互触发，自己触发自身技能数值
  - 每个单位最多同时受到 X 次【雷感状态】）
    - 注：最多受到X个突击步兵部队施加的效果，本质上控制集结中突击步兵数量
      - 如果多于X个状态，则后施加的无法成功施加
    - 层数上限读取const表，字段effect12611Maxinum
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12611)
public class Checker12611 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.FOOT_SOLDIER_5)) {
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
