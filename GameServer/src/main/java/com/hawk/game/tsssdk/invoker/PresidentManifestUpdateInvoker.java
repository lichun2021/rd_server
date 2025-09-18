package com.hawk.game.tsssdk.invoker;

import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;

@Category(scene = GameMsgCategory.PRESIDENT_MANIFEST_UPDATE)
public class PresidentManifestUpdateInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int resultCode, String manifesto, int protocol, String callback) {
		if (resultCode != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		int result = PresidentOfficier.getInstance().onPresidentManifestoUpdate(player, manifesto);
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol);
		} else {
			player.sendError(protocol, result, 0);
		}
		
		return 0;
	}

}
