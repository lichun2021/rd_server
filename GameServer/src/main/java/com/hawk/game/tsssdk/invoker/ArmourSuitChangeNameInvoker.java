package com.hawk.game.tsssdk.invoker;

import org.hawk.log.HawkLog;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GameUtil;

@Category(scene = GameMsgCategory.AMOUR_SUIT_CHANGE_NAME)
public class ArmourSuitChangeNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result == 0 && GameUtil.canArmourSuitNameUse(name)) {
			int suitType = Integer.valueOf(callback);
			RedisProxy.getInstance().setArmourSuitName(player.getId(), suitType, name);
			player.getPush().syncArmourSuitInfo();
			player.responseSuccess(protocol);
			HawkLog.logPrintln("armourChange, changeSuitName, playerId: {}, name: {}, suit: {}", player.getId(), name, suitType);
		} else {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
		}
		
		return 0;
	}

}
