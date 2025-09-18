package com.hawk.game.crossproxy.crossGift;

import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.CrossActivity.CrossRankInfo;

/**
 * 积分第一名分配礼包
 * @author Golden
 *
 */
public class CrossGiftImp1 implements CrossGiftOper {

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
		// 不是排名第一的服
		if (!player.getMainServerId().equals(serverRank.get(0).getServerId())) {
			return false;
		}
		// 不是战时总司令
		String crossFightPresident = RedisProxy.getInstance().getCrossFightPresident();
		if (HawkOSOperator.isEmptyString(crossFightPresident) || !crossFightPresident.equals(player.getId())) {
			return false;
		}
		return true;
	}
}
