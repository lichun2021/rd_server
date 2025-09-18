package com.hawk.game.crossproxy.crossGift;

import java.util.List;
import java.util.Objects;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.CrossActivity.CrossRankInfo;
import com.hawk.game.protocol.Player.CrossPlayerStruct;

/**
 * 防守成功礼包
 * @author Golden
 *
 */
public class CrossGiftImp4 implements CrossGiftOper {

	@Override
	public boolean doCheck(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player.isCsPlayer()) {
			return false;
		}
		List<CrossRankInfo> serverRank = CrossActivityService.getInstance().getServerRank();
		if (serverRank.size() <= 0) {
			return false;
		}
		// 防守方失败了
		if (RedisProxy.getInstance().isCrossWinServer(serverRank.get(0).getServerId())) {
			return false;
		}
		// 不是排名第三的服
		if (!player.getMainServerId().equals(serverRank.get(2).getServerId())) {
			return false;
		}
		// 不是跨服总司令
		CrossPlayerStruct president = CrossActivityService.getInstance().getCrossPresidentFromRedis();
		if(Objects.isNull(president)){
			return false;
		}
		if (HawkOSOperator.isEmptyString(president.getPlayerId()) || !president.getPlayerId().equals(player.getId())) {
			return false;
		}
		return true;
	}

}
