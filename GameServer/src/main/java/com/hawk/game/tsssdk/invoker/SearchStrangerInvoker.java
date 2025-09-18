package com.hawk.game.tsssdk.invoker;

import java.util.Arrays;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Friend.SearchStrangerResp;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.SearchService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.SEARCH_STRANGER)
public class SearchStrangerInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		JSONObject json = JSONObject.parseObject(callback);
		int sex = json.getIntValue("sex");
		int location = json.getIntValue("location");
		int findMaxNum = ConstProperty.getInstance().getFriendFindLimit();
		List<String> strList = SearchService.getInstance().searchPlayerByNameIgnore(name, 
				sex, location, findMaxNum, Arrays.asList(player.getId()), ConstProperty.getInstance().getSearchPrecise() > 0);
		RelationService relationService = RelationService.getInstance();
		
		SearchStrangerResp.Builder sbuilder = SearchStrangerResp.newBuilder();
		for (String playerId : strList) {
			if (GlobalData.getInstance().isResetAccount(playerId)) {
				HawkLog.logPrintln("SEARCH_STRANGER filter, target player is removed player, playerId: {}, targetId: {}", player.getId(), playerId);
				continue;
			}
			sbuilder.addStrangers(relationService.buildStrangerMsg(player.getId(), playerId));
		}
		
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.SEARCH_STRANGER_RESP_VALUE, sbuilder);
		player.sendProtocol(respProtocol);
		LogUtil.logSecTalkFlow(player, null, LogMsgType.SEARCH_STRANGER, "", name);
		
		return 0;
	}

}
