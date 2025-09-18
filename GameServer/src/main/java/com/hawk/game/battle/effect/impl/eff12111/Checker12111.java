package com.hawk.game.battle.effect.impl.eff12111;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

/**
 * 12111】
- 【万分比】【12111】军威：战斗开始前，使敌方 XX% 的部队军心动摇，无法参与战斗
  - 战报相关
    - 于战报中隐藏 
    - 不合并至精简战报中
  - 该判定于战斗开始前（早于所罗门的援军部队）。即
    - 注：集结战斗时，仅取队长的该作用号
    - 战斗开始时数量 = 出征数量 * （1 + 【所罗门援军效果】）
    - 无法参战数量 = 战斗开始时数量 * 【本作用值】（向下取整）
      - 仅对部队（1~8）兵种类型生效
      - 不同玩家不同类型不同等级兵种同比率单独处理
    - 实际参战数量 = 战斗开始时数量 - 无法参战数量
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12111)
public class Checker12111 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean bfalse = parames.unity.getPlayer().getLmjyState() == PState.GAMEING;
		if (!bfalse && isSoldier(parames.type) && parames.unity.getPlayer().getClass()!= NpcPlayer.class && !BattleConst.WarEff.MONSTER.check(parames.troopEffType)) {
			effPer = parames.unity.getEffVal(effType());
			if (parames.tarStatic.getLeaderUnity() != null) {
				int eff12111 = parames.tarStatic.getLeaderUnity().getEffVal(effType());
				BattleSoldier solider = parames.unity.getSolider();
				int eff12111Cnt = (int) (solider.getOriCnt() * GsConst.EFF_PER * eff12111);
				solider.setEff12111Cnt(eff12111Cnt);
			}
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
