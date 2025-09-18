package com.hawk.game.crossactivity.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.game.config.CrossIntegralCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.CrossTargetParser;
import com.hawk.game.crossactivity.CrossTargetType;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

public class CollectResourceTargetParser implements CrossTargetParser<ResourceCollectEvent> {
	
	@Override
	public CrossTargetType getTargetType() {
		return CrossTargetType.COLLECT_RESOURCE;
	}
	
	
	@Override
	public boolean calcScore(int termId, CrossIntegralCfg circularCfg, ResourceCollectEvent event) {
		String playerId = event.getPlayerId();
		Map<Integer, Double> collectMap = event.getCollectMap();
		Map<Integer, Double> indexScoreMap = circularCfg.getIndexScoreMap();
		double addScore = 0;
		for (Entry<Integer, Double> entry : collectMap.entrySet()) {
			Double coefficient = indexScoreMap.get(entry.getKey());
			if (coefficient == null) {
				continue;
			}
			addScore += coefficient * entry.getValue();
		}
		
		if (addScore <= 0) {
			return false;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		String serverId = player.getMainServerId();

		// 跨服行为积分翻倍
		if (!GlobalData.getInstance().isLocalServer(serverId)) {
			addScore = addScore * circularCfg.getCrossScoreMag() / GsConst.RANDOM_MYRIABIT_BASE;
		}

		int addEff = player.getData().getEffVal(EffType.CROSS_RES_COLLECT_SCORE_ADD);
		addScore = Math.floor(GsConst.EFF_PER * addScore * (GsConst.RANDOM_MYRIABIT_BASE + addEff));
		
		CrossActivityService.getInstance().addScore(player, getTargetType(), (long)addScore);
		return true;
	}
}
