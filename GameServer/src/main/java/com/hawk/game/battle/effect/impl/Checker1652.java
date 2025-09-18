package com.hawk.game.battle.effect.impl;

import java.util.Objects;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 技能描述：

•出征或驻防时，部队防御 +xx.xx%

•出征或驻防时，部队生命 +xx.xx%

•自身携带最多的士兵，每5回合随机对 2 个小于自己士兵数量的我方坦克或战车目标，释放铁幕保护， 被保护的部队，受到伤害降低 xx.xx%，持续2回合。

•若符合条件的我方目标不足，剩余的铁幕能量将随机投放至敌方步兵单位，造成其当前士兵数量 xx.xx% 直接阵亡。

•受铁幕力场相互之间影响，同个回合多个昆娜领导的士兵最多可投放 6 个铁幕便携装置，且无法对同一个目标重复释放铁幕。

作用号 1652

每5回合的回合开始时，我方最多的部队，对小于此部队士兵数量，且士兵类型=1，2，7，8的2个不同目标，施加1个免伤buff

部队最终受到伤害 = 基础伤害 * （1-【1652】）* 其他受伤比率 【1652】是万分比数值，3200 即32%免伤比率）

当有多个玩家携带【1652】作用号，一个回合最多生效x个buff, x由const表1652effectNum控制，

填6即，最多可给6个目标buff，

buff目标将优先选择没有被buff的符合条件的目标，

若已方符合条件的目标，全被buff，将执行 【1653】

【1653】 将【1652】剩余的可执行的buff 数量，转换为随机伤害1个敌方步兵目标，

士兵类型=5，6 造成目标士兵数量百分比的阵亡

士兵阵亡数 = 当前目标本回合士兵数量 * 【1653】 杀死的部队，记录给【1652】开头我方最多的部队目标 多个玩家携带【1653】。

在选择步兵目标时，同一个回合，受到过【1653】伤害的步兵目标的需要去重

 

案例1，我方集结，2个人携带此英雄，每个人出兵30W，但是集结中只有战车，坦克 2排低于30W，那么我方第一个人，会buff这两排目标，剩余1个buff会寻找敌方随机1排步兵，第二个人，会选择除了刚才那个被选中的其余3排步兵目标，如果只有两个，那么就只打两个，另外一个浪费了

 

战报显示

我方最多的部队 兵种信息“！”

显示文本 ：

本次战斗，铁幕保护我方 x 目标 （x为，全部回合buff我方单位的排数量，非士兵数量）

本次战斗，铁幕消灭敌方 y 名士兵 （y为，1653杀伤的士兵数）
 * @author lwt
 * @date 2022年8月30日
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1652)
public class Checker1652 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.tarType)) {
			String playerId = parames.unity.getPlayer().getId();
			BattleUnity maxArmy = null;
			for (SoldierType type : SoldierType.values()) {
				if (!isSoldier(type)) {
					continue;
				}
				BattleUnity temp = parames.getPlayerMaxFreeArmy(playerId, type);
				if(Objects.isNull(temp)){
					continue;
				}
				if (Objects.isNull(maxArmy) || temp.getFreeCnt() > maxArmy.getFreeCnt()) {
					maxArmy = temp;
				}
			}
			if (maxArmy == parames.unity) {
				effPer = parames.unity.getEffVal(effType());
			}

		}
		return new CheckerKVResult(effPer, effNum);
	}
}
