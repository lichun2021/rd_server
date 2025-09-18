package com.hawk.game.tsssdk.invoker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.CrossActivity.CrossGiftMemeberInfo;
import com.hawk.game.protocol.CrossActivity.CrossGiftMiniPlayerMsg;
import com.hawk.game.protocol.CrossActivity.CrossGiftSearchRes;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.SearchService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;

@Category(scene = GameMsgCategory.CROSS_GIFT_SEARCH_MEMBER)
public class CrossGiftSearchMemberInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}

		int type = Integer.parseInt(callback);
		searchMemeber(player, name, type);
		return 0;
	}

	public void searchMemeber(Player player, String name, int type) {
		Set<String> ignoreSet = RedisProxy.getInstance().getAllCrossGiftPlayer(type);				
		int maxCount = PresidentConstCfg.getInstance().getSearchMaxCount();
		List<String> idList = SearchService.getInstance().searchPlayerByNameIgnore(name, 0, 0, maxCount, new ArrayList<>(ignoreSet), ConstProperty.getInstance().getSearchPrecise() > 0);
		synMember(player, new HashSet<>(idList));		
	}
	
	private void synMember(Player player, Set<String> playerIds) {
		// 构建返回数据包
		CrossGiftSearchRes.Builder response = CrossGiftSearchRes.newBuilder();
		for (String playerId : playerIds) {
			Player searchPlayer = GlobalData.getInstance().makesurePlayer(playerId);
			if (null != searchPlayer) {
				CrossGiftMemeberInfo.Builder memeberInfo = CrossGiftMemeberInfo.newBuilder();
				CrossGiftMiniPlayerMsg.Builder miniPlayer = BuilderUtil.genCrossGiftMiniPlayer(playerId);
				memeberInfo.setMiniPlayer(miniPlayer);
				memeberInfo.setOfficer(GameUtil.getOfficerId(playerId));
				//String sendValue = LocalRedis.getInstance().getGiftSend(playerId); 这种写法有点过分.
				memeberInfo.setIsSendGift(true);
				memeberInfo.setBuildingLevel(player.getCityLevel());
				response.addMemeberInfo(memeberInfo);
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_GIFT_SEARCH_S, response));
	}
}
