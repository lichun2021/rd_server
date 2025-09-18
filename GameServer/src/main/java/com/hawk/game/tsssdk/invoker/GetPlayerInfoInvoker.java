package com.hawk.game.tsssdk.invoker;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Player.GetPlayerBasicInfoResp;
import com.hawk.game.service.SearchService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.GET_PLAYER_INFO)
public class GetPlayerInfoInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		GetPlayerBasicInfoResp.Builder builder = SearchService.getInstance().searchNoGuildPlayerByName(name, player.getId(), ConstProperty.getInstance().getSearchPrecise() > 0);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_GETLOCALPLAYERINFOBYNAME_S_VALUE, builder));
		LogUtil.logSecTalkFlow(player, null, LogMsgType.SEARCH_PLAYER, "", name);
		
		return 0;
	}

}
