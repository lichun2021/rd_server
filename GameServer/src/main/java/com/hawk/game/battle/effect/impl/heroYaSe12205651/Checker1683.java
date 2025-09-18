package com.hawk.game.battle.effect.impl.heroYaSe12205651;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 * #12205651 【0525版本】【新英雄】【战车兵营委任】亚瑟 https://meego.feishu.cn/ccredalert/story/detail/12205651
 * 
 【1681】
- 【万分比】【1681】集结战斗开始前，若自身出征携带不低于集结部队总数 5% 的采矿车，自身出征数量最多的采矿车在攻击命中敌方后，降低其 +XX.XX% 的生命加成。
满足上述条件下，若自身出征携带超过自身出征数量 50% 的泰能采矿车，此效果 翻倍。（多个亚瑟同时存在时，该效果可叠加，至多 XX%）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 该作用号仅对集结战斗生效（包含集结进攻和集结防守）
  - 战斗集结开始前，若自身出征携带不低于集结部队总数 5% 的采矿车
    - 数量1 = 某玩家出征携带的采矿车（兵种类型 = 8）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 在战斗开始前判定，满足条件后本次战斗全程生效
      - 指定数值读取const表，字段effect1681AllNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的采矿车在攻击命中敌方后，降低其 +XX.XX% 的生命加成
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的采矿车（兵种类型 = 8）生效（若存在多个采矿车数量一样且最高，取等级高的）
      - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
    - 降低敌方生命加成为外围属性减少效果；即 敌方部队实际生命 = 基础生命*（1 + 敌方各类生命加成 - 本作用号生命加成降低）
    - 该效果由被攻击附加后，持续至本场战斗结束
  - 满足上述条件下，若自身出征携带超过自身出征数量 50% 的泰能采矿车，此效果 翻倍
    - 数量1 = 某玩家出征携带的泰能采矿车（兵种类型 = 8 且 plantSoldier = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 在战斗开始前判定，满足条件后本次战斗全程生效
      - 指定数值读取const表，字段effect1681SelfNumLimit
        - 配置格式：万分比
      - 效果倍率读取const表，字段effect1681RateValue
        - 配置格式：万分比
      - 即 实际降低数值 = 作用值 * 效果倍率/10000
        - 若配置在英雄上，则 作用值 = 英雄后勤值 * 配置系数/10000
  - （多个亚瑟同时存在时，该效果可叠加，至多 XX%）
    - 集结中存在多个此作用号时，可以叠加
    - 效果记录在敌方部队身上，数值直接叠加；限制该作用号数值上限
      - 注：这里是该作用号的最终数值上限，泰能采矿车的倍率效果也受其限制
      - 数值上限读取const表，字段effect1681MaxValue
        - 配置格式：万分比

【1682~1684】
- 【万分比】【1682】集结战斗开始前，自身攻城车攻击 +XX.XX%。满足上述条件下，若自身出征携带超过自身出征数量 50% 的泰能攻城车，此效果 翻倍。
- 【万分比】【1683】集结战斗开始前，自身攻城车防御 +XX.XX%。满足上述条件下，若自身出征携带超过自身出征数量 50% 的泰能攻城车，此效果 翻倍。
- 【万分比】【1684】集结战斗开始前，自身攻城车生命 +XX.XX%。满足上述条件下，若自身出征携带超过自身出征数量 50% 的泰能攻城车，此效果 翻倍。
  - 战报相关
    - 上述3个作用号均于战报中展示
    - 上述3个作用号均合并至精简战报的攻城车攻击、防御、生命属性加成进行展示
  - 上述3个作用号机制完全一致，加的属性类别不一样，分3个作用号开发；攻城车兵种类型 = 7
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 自身属性加成为外围属性加成效果；即 实际攻击/防御/生命 = 基础攻击/防御/生命*（1 + 各类加成 + 本作用号加成）
  - 满足上述条件下，若自身出征携带超过自身出征数量 50% 的泰能攻城车，此效果 翻倍。
    - 数量1 = 某玩家出征携带的泰能攻城车（兵种类型 = 7 且 plantSoldier = 1）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 在战斗开始前判定，满足条件后本次战斗全程生效
      - 指定数值读取const表，字段effect1682SelfNumLimit、effect1683SelfNumLimit、effect1684SelfNumLimit
        - 配置格式：万分比
      - 效果倍率读取const表，字段effect1682RateValue、effect1683RateValue、effect1684RateValue
        - 配置格式：万分比
      - 即 实际加成数值 = 作用值 * 效果倍率/10000
        - 若配置在英雄上，则 作用值 = 英雄后勤值 * 配置系数/10000
 */
@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.HERO_1683)
public class Checker1683 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.CANNON_SOLDIER_7
				|| parames.unity.getEffVal(effType()) == 0
				|| !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}

		String playerId = parames.unity.getPlayer().getId();
		int effPer = parames.unity.getEffVal(effType());
		int effNum = 0;
		// 泰能采矿车
		int march8Plantcnt = parames.unitStatic.getPlantUnityStatistics().getPlayerSoldierCountMarch().get(playerId, SoldierType.CANNON_SOLDIER_7);
		// 若自身出征携带超过自身出征数量 50% 的泰能采矿车
		if (march8Plantcnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect1683SelfNumLimit()* GsConst.EFF_PER) {
			effPer = (int) (effPer * GsConst.EFF_PER * ConstProperty.getInstance().getEffect1683RateValue());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
