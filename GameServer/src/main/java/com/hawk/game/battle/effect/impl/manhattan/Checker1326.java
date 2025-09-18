package com.hawk.game.battle.effect.impl.manhattan;

import java.util.HashMap;
import java.util.Map;

import com.hawk.game.battle.BattleUnity;
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
 * 【1325~1326】
【绝对值】【1325~1326】在战斗中，自身所有单位受到伤害减少20%（作用号1325），此效果会在每回合匀速衰减，在【30（作用号1326）+己方队伍中核心科技品阶总和*25.00%（effect1325RankRatio）】回合时衰减至0.00%（品阶总和上限为80（effect1325Maxinum）
，回合数不为整时向上取整）->针对自身兵种留个内置系数（effect1325SoldierAdjust） 
- 自身所有单位受到伤害减少 XX.XX%
  - 初始减伤值，直接读取作用号【1325】配置值
    - 配置格式：万分比
  - 该作用号为伤害减少效果，作用号与其他作用号累乘计算，即 
    - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【当前回合减伤值】* 自身兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect1325SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
      - 当前回合减伤值由下方计算公式得出
- 此效果会在每回合匀速衰减，在【      】回合时衰减至0.00%
  - 每回合衰减值 = 初始减伤值 / 回合数
  - 当前回合减伤值 = 初始减伤值 - 每回合衰减值 * （当前回合数-1）
- 【作用号1326+己方队伍中核心科技品阶总和*25.00%（effect1325RankRatio）】
  - 作用号值，直接读取作用号【1326】配置值
    - 配置格式：绝对值
  - 己方队伍中核心科技品阶总和
    - 为己方参与本次战斗的玩家，各自核心科技品阶的总和
      - 科技品阶读取mecha_core_technology_rank表coreRankLevel字段（此处是配置品阶值，具体应读取玩家自身品阶数值）
  - 系数读取const表，字段effect1325RankRatio
    - 配置格式：万分比
- （品阶总和上限为80（effect1325Maxinum），回合数不为整时向上取整）
  - 此处品阶总和有上限，上限读取const表，字段effect1325Maxinum
    - 配置格式：绝对值
  - 回合数在计算后向上取整
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_1326)
public class Checker1326 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.EFF_1325) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());

			Map<String, Integer> map = new HashMap<>();
			for (BattleUnity unity : parames.unityList) {
				if (map.containsKey(unity.getPlayerId())) {
					continue;
				}
				map.put(unity.getPlayerId(), unity.getPlayer().getMechaCoreRankLevel());
			}
			int allr = map.values().stream().mapToInt(Integer::intValue).sum();
			allr = Math.min(allr, ConstProperty.getInstance().effect1325Maxinum);
			allr = (int) Math.ceil(allr * GsConst.EFF_PER * ConstProperty.getInstance().effect1325RankRatio);

			effPer = effPer + allr;
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
