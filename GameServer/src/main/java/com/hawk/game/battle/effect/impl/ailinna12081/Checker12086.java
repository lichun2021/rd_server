package com.hawk.game.battle.effect.impl.ailinna12081;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12086】
- 【万分比】【12086】勠力同心：携带有装甲防护的单位，在遭受损失时，因急救防护效果有 XX.XX%*该防御坦克属性加成/攻击方属性加成 
（该比率数值最低50%，最高200%；属性加成为自身攻击加成、防御加成和生命加成均值）的部队直接恢复，并将剩余 25% 的部队损失转移至己方所有近战部队
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 此作用号绑定作用号【12085】的装甲防护的接受单位
  - 在战斗中受到伤害后，结算扣除部队前，将一定比率的部队直接恢复，此结算效果在作用号【1658】之后
    - 先结算【1658】效果；实际损失【1658】 = 所受伤害/单兵生命 * （1 - 【1658】/10000）
    - 后结算【12086】效果
      - 恢复比率 = 实际损失【1658】* XX.XX% * 该防御坦克属性加成/攻击方属性加成
        - 向下取整
      - 实际损失【12086】 = 实际损失【1658】 - 恢复比率
    - 【该防御坦克属性加成/攻击方属性加成】该比率数值有上下限限制
      - 下限读取const表，字段effect12086RateMin
        - 配置格式：万分比
      - 上限读取const表，字段effect12086RateMax
        - 配置格式：万分比
  - 这里为属性加成做下说明
    - 单兵攻击/防御/生命 = （基础数值 + 基础加成）*（1 + 己方各类基础作用号加成 - 敌方各类基础作用号减少）*（1 + 己方各类外围作用号加成 - 敌方各类外围作用号减少）
      - 基础数值：由兵种等级和星级决定，直接配置在兵种表内
      - 基础加成：泰能战士进阶养成特殊效果
      - 己方各类基础作用号加成：各类战场特殊增益效果（如：泰伯战令）
      - 敌方各类基础作用号减少：各类战场特殊减益效果（如：军团模拟战）
      - 己方各类外围作用号加成：常规属性加成（如：科技、装备等）
      - 敌方各类外围作用号减少：各类减益效果（如：英雄亚瑟的减少生命加成的效果）
  - 此处自身攻击/防御/生命加成 = 己方各类外围作用号加成 - 敌方各类外围作用号减少
    - 注：这里取自身属性加成的时机在遭受损失时；即随战中实时变化
  - 并将剩余 25% 的部队损失将转移至己方所有近战部队（近战部队当前存活数量越多，分摊比例越高）
    - 折算完直接恢复率后的损失部队，有50%比率转移至己方所有近战部队，剩余比率则为该单位实际损失
    - 转移损失 = 实际损失【12086】*转移比率（向下取整）
      - 各近战部队实际损失 = 转移损失/当前近战单位数量（四舍五入）（按战斗单位数量，平均分摊）
      - 转移比率读取const表，字段effect12086TransferRate
      - 注：为避免反复嵌套死锁，转移损失的部队为直接扣除对应部队数量（不再参与各类伤害计算和战中急救等效果）
    - 实际损失 = 实际损失【12086】 - 转移损失
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12086)
public class Checker12086 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.EFF_12085) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
