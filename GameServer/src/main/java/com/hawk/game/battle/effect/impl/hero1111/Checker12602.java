package com.hawk.game.battle.effect.impl.hero1111;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

/**
 *- - 【万分比】【12602】任命在战备参谋部时，集结战斗中己方全队坦克数量每损失10%(effect12602AllNumLimit)，自身全体部队生命增加【+XX.XX%（12602）+固定值（effect12602BaseVaule）*战备参谋部中任命索拉娜的友方成员数量】（该效果可叠加，至多 10(effect12602Maxinum) 层，友方索拉娜计数时至多取 7（effect12602CountMaxinum） 个）->针对兵种留个内置系数
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗每回合判定，满足条件后本轮战斗生效
  - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 集结战斗中己方坦克数量每损失10%
    - 坦克部队类型包含有：主战坦克 = 2、防御坦克 = 1
    - 部队损失比例计算可参考作用号【12391】【240822】【SSS】【参谋委任】【尤拉】【1102】 
    - 损失比读取const表，字段effect12602AllNumLimit
      - 配置格式：绝对值
  - 自身全体部队生命增加 【+XX.XX%（12602）+固定值*战备参谋部中任命索拉娜的友方成员数量】
    - 作用号固定数值读取const表，字段effect12602BaseVaule
      - 配置格式：万分比
    - 该作用号为常规外围属性加成效果，与其他作用号累加计算
      - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】 * 自身兵种修正系数）
  - （该效果可叠加，至多 10 层）
    - 层数上限读取const表，字段effect12602Maxinum
      - 配置格式：绝对值
  - （己方索拉娜计数时至多取 7  个）
    - 取集结己方委任英雄中携带艾拉（ID1111）的玩家数量
    - 该计数有最高值限制，读取const表，字段effect12602CountMaxinum
      - 配置格式：绝对值
  - ->针对兵种留个内置系数
    - 实际针对自身各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect12602SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
    - 配置格式：万分比
 */
@BattleTupleType(tuple = { Type.SOLDIER_SKILL })
@EffectChecker(effType = EffType.HERO_12602)
public class Checker12602 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType) || !isSoldier(parames.type)) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		if (parames.unity.getEffVal(effType()) > 0) {
			Object object = parames.getLeaderExtryParam(getSimpleName());
			int solaraCnt = 0;
			if (Objects.nonNull(object)) {
				solaraCnt = (Integer) object;
			} else {
				solaraCnt = solaraCnt(parames);
				parames.putLeaderExtryParam(getSimpleName(), solaraCnt);
			}
			
			effPer = parames.unity.getEffVal(effType()) + ConstProperty.getInstance().effect12602BaseVaule * solaraCnt;
			effPer = (int) (effPer* GsConst.EFF_PER* ConstProperty.getInstance().effect12602SoldierAdjustMap.getOrDefault(parames.type, 10000));
		}
		return new CheckerKVResult(effPer, 0);
	}

	/**数值最高的玩家*/
	private int solaraCnt(CheckerParames parames) {

		Map<String, Integer> valMap = new HashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayerName())) {
				continue;
			}
			if (unity.getEffVal(effType()) > 0) {
				int effvalue =  unity.getEffVal(effType());
				valMap.put(unity.getPlayerName(), effvalue);
			}
		}

		return Math.min(valMap.size(), ConstProperty.getInstance().effect12602CountMaxinum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
