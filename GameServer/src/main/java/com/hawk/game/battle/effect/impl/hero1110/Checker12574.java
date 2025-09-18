package com.hawk.game.battle.effect.impl.hero1110;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 *【12574~12575】
- 【万分比】【12574~12575】聚焦增幅：每 X（effect12574AtkRound） 回合开始时，为自身身后随机 2（effect12574InitChooseNum） 个非采矿车近战单位施加聚焦效果，持续 5（effect12574ContinueRound） 回合（优先选择未处于聚焦效果下的单位，状态不可叠加），获得聚焦效果的单位获得以下效果。->针对近战兵种留个内置随机权重系数（effect12574RoundWeight）
  - 攻击、防御、血量增加固定值（effect12574BaseVaule）+XX.XX%（12574）*采矿车自身三维属性加成，超能攻击增加 固定值（effect12575BaseVaule）+ XX.XX%（12575）* 采矿车自身超能属性加成
- 每第 X 回合开始时
  - 战斗中每第 X 的倍数的回合开始后，自身开始普攻前，进行判定
  - 指定回合数读取const表，字段effect12574AtkRound
    - 配置格式：绝对值
- 为自身身后随机 2 个非采矿车近战单位 ->针对近战兵种留个内置随机权重系数
  - 随机规则
    - 作用号生效后，随机在符合兵种需求的单位中随机选择2个目标（优先选择无聚焦效果的单位）
      - 自身身后最近 2 个非采矿车近战单位包括：主战坦克（兵种类型 = 2）、轰炸机（兵种类型=3）
      - 近战单位中选择各兵种类型随机权重读取const表，字段effect12574RoundWeight
        - 配置格式：类型1_权重1,类型2_权重2
          - 权重系数具体配置为万分比
  - 基础可选取上限数量读取const表，字段effect12574InitChooseNum
    - 配置格式：绝对值
  - 随机规则
    - 作用号生效后，随机在符合兵种需求的单位中随机选择2个目标
      - 近战部队类型包含有：防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、主战坦克（兵种类型 = 2）、轰炸机（兵种类型 = 3）
      - 近战单位中选择各兵种类型随机权重读取const表，字段effect12574RoundWeight
        - 配置格式：类型1_权重1,类型2_权重2,类型3_权重3,类型4_权重4
- 施加聚焦效果，持续 5 回合
  - 回合开始：进入聚焦效果状态，当前回合算作第1回合
  - 回合结束：当前回合结束时进行判定，如果 当前持续回合=理应持续回合，则清除聚焦效果状态
  - 持续回合数读取const表，字段effect12574ContinueRound
    - 配置格式：绝对值
- （优先选择未处于聚焦效果下的单位，状态不可叠加）
  - 选择自身身后随机 2 个非采矿车近战单位时，优先选择无聚焦效果的单位
    - 多个莱万存在时，可能出现可选单位均有聚焦效果，则随机在有聚焦效果的单位中选择
      - 状态不可叠加，二者取作用值高的保留
- 获得聚焦效果的单位获得以下效果：
- 攻击、防御、血量增加固定值+XX.XX%*采矿车自身三维属性加成
  - 增加属性为外围属性增加效果；
    - 即 敌方部队实际属性 = 基础属性*（1 + 属性加成 + 本作用号属性）
  - 攻击、防御、血量增加固定数值读取const表，字段effect12574BaseVaule
    - 配置格式：万分比
  - 攻击、防御、血量增加作用值读取作用号【12574】
    - 配置格式：万分比
  - 采矿车自身三维属性加成同作用号【12014】
    - 【230608】【SSS】【战车双将】【杰拉尼】 【1086】
    - 自身属性加成 = （自身攻击加成 + 自身防御加成 + 自身生命加成）/3
    - 这里自身攻击/防御/生命加成取的是作用号提供方的采矿车（兵种类型 = 8）兵种的属性
    - 这里为属性加成做下说明
      - 单兵攻击/防御/生命 = （基础数值 + 基础加成）*（1 + 己方各类基础作用号加成 - 敌方各类基础作用号减少）*（1 + 己方各类外围作用号加成 - 敌方各类外围作用号减少）
        - 基础数值：由兵种等级和星级决定，直接配置在兵种表内
        - 基础加成：泰能战士进阶养成特殊效果
        - 己方各类基础作用号加成：各类战场特殊增益效果（如：泰伯战令）
        - 敌方各类基础作用号减少：各类战场特殊减益效果（如：军团模拟战）
        - 己方各类外围作用号加成：常规属性加成（如：科技、装备等）
        - 敌方各类外围作用号减少：各类减益效果（如：英雄亚瑟的减少生命加成的效果）
    - 此处自身攻击/防御/生命加成 = 己方各类外围作用号加成 - 敌方各类外围作用号减少
      - 注：这里取自身属性加成的时机在集结战斗开始时（即各种其他光环、集结增益作用号都算完之后）
- 超能攻击增加 固定值+ XX.XX%* 采矿车自身超能属性加成
  - 增加属性为超能攻击属性增加效果；
    - 即 敌方部队超能攻击属性 = 其他超能攻击属性加成 + 本作用号属性
  - 超能攻击固定数值读取const表，字段effect12575BaseVaule
    - 配置格式：万分比
  - 攻击、防御、血量增加作用值读取作用号【12575】
    - 配置格式：万分比
  - 采矿车自身超能攻击属性加成参考作用号【12014】，逻辑完全同上
    - 此处自身超能攻击加成 = 己方各类外围作用号加成 - 敌方各类外围作用号减少
      - 注：这里取自身属性加成的时机在集结战斗开始时（即各种其他光环、集结增益作用号都算完之后）
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12574)
public class Checker12574 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.EFF_12571) <= 0) {
			return CheckerKVResult.DefaultVal;
		}
		int atkper = parames.solider.tupleValue(BattleTupleType.Type.ATK, SoldierType.XXXXXXXXXXXMAN).first;
		int defper = parames.solider.tupleValue(BattleTupleType.Type.DEF, SoldierType.XXXXXXXXXXXMAN).first;
		int hpper = parames.solider.tupleValue(BattleTupleType.Type.HP, SoldierType.XXXXXXXXXXXMAN).first;
		int avg = (atkper + defper + hpper) / 3;
		
		int effPer = (int) (parames.unity.getEffVal(effType()) * GsConst.EFF_PER * avg + ConstProperty.getInstance().effect12574BaseVaule);

		return new CheckerKVResult(effPer, 0);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
