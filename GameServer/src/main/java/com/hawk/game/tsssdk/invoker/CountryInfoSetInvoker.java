package com.hawk.game.tsssdk.invoker;

import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.SET_COUNTRY_INFO)
public class CountryInfoSetInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int resultCode, String countryName, int protocol, String callback) {
		if (resultCode != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}

		if (countryName.length() > PresidentConstCfg.getInstance().getNameLengthLimit()) {
			player.sendError(protocol, Status.Error.COUNTRY_NAME_TOO_LONG, 0);
			return 0;
		}

		// 通知王国信息修改
		PresidentFightService.getInstance().notifyCountryInfoChanged(countryName);
		LogUtil.logSecTalkFlow(player, null, LogMsgType.COUNTRY_NAME_CHANGE, "", countryName);
		
		return 0;
	}
	
}
