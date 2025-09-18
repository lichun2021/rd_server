package com.hawk.game.tsssdk.invoker;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentGift;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.PRESIDENT_SEARCH_MEMBER)
public class PresidentSearchMemberInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}

		int type = Integer.parseInt(callback);
		PresidentGift.getInstance().searchMemeber(player, name, type, ConstProperty.getInstance().getSearchPrecise() > 0);
		LogUtil.logSecTalkFlow(player, null, LogMsgType.SEARCH_PLAYER, "", name);
		
		return 0;
	}

}
