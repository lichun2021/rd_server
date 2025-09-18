package com.hawk.game.tsssdk.invoker;

import org.hawk.app.HawkApp;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.PLAYER_SIGNATURE)
public class PlayerSignatureInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String signature, int protocol, String callback) {
		if (result == 0 && GameUtil.canSignatureUse(signature)) {
			WorldPointService.getInstance().updatePlayerSignature(player.getId(), signature);
			RedisProxy.getInstance().updateChangeContentTime(player.getId(), ChangeContentType.CHANGE_SIGNATURE, HawkApp.getInstance().getCurrentTime());
			int checkResult = Integer.valueOf(callback);
			if (checkResult > 0) {
				RedisProxy.getInstance().removeChangeContentCDTime(player.getId(), ChangeContentType.CHANGE_SIGNATURE);
			}
			
			GameUtil.notifyDressShow(player.getId());
			LogUtil.logSecTalkFlow(player, null, LogMsgType.SIGNATURE, "", signature);
			player.responseSuccess(protocol);
		} else {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
		}
		
		return 0;
	}

}
