package com.hawk.game.crossactivity.impl;

import com.hawk.activity.event.impl.OccupyStrongpointFinishEvent;
import com.hawk.game.config.CrossIntegralCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.CrossTargetParser;
import com.hawk.game.crossactivity.CrossTargetType;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

/**
 * 占领幽灵兵营
 * @author Jesse
 *
 */
public class OccupyStrongpointTargetParser implements CrossTargetParser<OccupyStrongpointFinishEvent> {
	
	@Override
	public CrossTargetType getTargetType() {
		return CrossTargetType.OCCUPY_STRONGPOINT;
	}
	

	@Override
	public boolean calcScore(int termId, CrossIntegralCfg circularCfg, OccupyStrongpointFinishEvent event) {
		String playerId = event.getPlayerId();
		int pointLvl = event.getPointLvl();
		Double coefficient = circularCfg.getIndexScoreList().get(pointLvl - 1);
		if (coefficient == null) {
			return false;
		}

		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		String serverId = player.getMainServerId();
		double addScore = coefficient * event.getOccupyTime() / 60;

		// 跨服行为积分翻倍
		if (!GlobalData.getInstance().isLocalServer(serverId)) {
			addScore = addScore * circularCfg.getCrossScoreMag() / GsConst.RANDOM_MYRIABIT_BASE;
		}
		
		int addEff = player.getData().getEffVal(EffType.CROSS_STRONGPOINT_SCORE_ADD);
		addScore = Math.floor(GsConst.EFF_PER * addScore * (GsConst.RANDOM_MYRIABIT_BASE + addEff));

		CrossActivityService.getInstance().addScore(player, getTargetType(), (long)addScore);
		return true;
	}
}
