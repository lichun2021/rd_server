package com.hawk.game.crossactivity.impl;

import com.hawk.activity.event.impl.OccupyPylonFinishEvent;
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
public class OccupyPylonTargetParser implements CrossTargetParser<OccupyPylonFinishEvent> {
	
	@Override
	public CrossTargetType getTargetType() {
		return CrossTargetType.OCCUPY_PYLON;
	}
	

	@Override
	public boolean calcScore(int termId, CrossIntegralCfg circularCfg, OccupyPylonFinishEvent event) {
		String playerId = event.getPlayerId();
		Double configParam = circularCfg.getIndexScoreList().get(0);
		if (configParam == null) {
			return false;
		}

		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		String serverId = player.getMainServerId();
		double addScore = configParam.doubleValue();

		// 跨服行为积分翻倍
		if (!GlobalData.getInstance().isLocalServer(serverId)) {
			addScore = addScore * circularCfg.getCrossScoreMag() / GsConst.RANDOM_MYRIABIT_BASE;
		}
		addScore = addScore * (1 + player.getEffect().getEffVal(EffType.CROSS_TECH_EFF_3016) * GsConst.EFF_PER);
		CrossActivityService.getInstance().addScore(player, getTargetType(), (long)addScore);
		return true;
	}
}
