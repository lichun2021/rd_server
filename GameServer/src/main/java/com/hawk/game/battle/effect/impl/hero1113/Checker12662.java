package com.hawk.game.battle.effect.impl.hero1113;

import java.util.Objects;
import java.util.stream.Collectors;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;

/**
 *【12662】
- 【万分比】【12662】任命在战备参谋部时，集结战斗中己方任意空军命中1次敌方后，己方所有单位的攻击增加【+XX.XX%（12662）+固定值（effect12662BaseVaule）*战备参谋部中任命乔希的友方成员数量】（该效果可叠加，至多 10(effect12662Maxinum)  层，友方乔希计数时至多取 7（effect12662CountMaxinum） 个）->针对己方兵种留个内置系数
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗中随时变化，达到层数上限后不再变化
  - 集结战斗中己方任意空军命中1次敌方后
    - 己方空军：若有盟友部队，盟友部队的攻击也算在内
      - 空军单位类型包含有：轰炸机（兵种类型 = 3）、直升机（兵种类型 = 4）
    - 若为范围攻击，则按攻击命中的敌方单位实际个数进行计算
      - 敌方单位包含有：兵种（1~8）
  - 自身全体部队生命增加 【+XX.XX%+固定值*战备参谋部中任命乔希的友方成员数量】
    - 作用号固定数值读取const表，字段effect12662BaseVaule
      - 配置格式：万分比
    - 该作用号为常规外围属性加成效果，与其他作用号累加计算
      - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】 * 自身兵种修正系数）
  - （该效果可叠加，至多 10 层）
    - 层数上限读取const表，字段effect12662Maxinum
      - 配置格式：绝对值
  - （己方乔希计数时至多取 7  个）
    - 取集结己方委任英雄中携带乔希（ID1113）的玩家数量
    - 该计数有最高值限制，读取const表，字段effect12662CountMaxinum
      - 配置格式：绝对值
  - ->针对兵种留个内置系数
    - 实际针对自身各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect12662SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
    - 配置格式：万分比
 */
@BattleTupleType(tuple = { Type.SOLDIER_SKILL })
@EffectChecker(effType = EffType.EFF_12662)
public class Checker12662 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(effType()) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		// 战备参谋部中任命艾拉的友方成员数量
		int effect12553CountMaxinum;
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			effect12553CountMaxinum = (Integer) object;
		} else {
			effect12553CountMaxinum = parames.unityList.stream().filter(unity -> unity.getEffVal(effType()) > 0).map(unity -> unity.getPlayerId()).collect(Collectors.toSet())
					.size();
			effect12553CountMaxinum = Math.min(effect12553CountMaxinum, ConstProperty.getInstance().effect12553CountMaxinum);
			parames.putLeaderExtryParam(getSimpleName(), effect12553CountMaxinum);
		}

		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type) && BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			effPer = parames.unity.getEffVal(effType()) + ConstProperty.getInstance().effect12662BaseVaule * effect12553CountMaxinum;
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
