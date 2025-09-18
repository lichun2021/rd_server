package com.hawk.game.tsssdk.invoker;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Player.PlayerCheckNameResp;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GameUtil;

@Category(scene = GameMsgCategory.PLAYER_CHECK_NAME)
public class PlayerCheckNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int resultCode, String name, int protocol, String callback) {
		PlayerCheckNameResp.Builder resp = PlayerCheckNameResp.newBuilder();
		if (resultCode != 0) {
			resp.setResult(false);
			resp.setErrorCode(Status.NameError.CONTAINS_FILTER_WORD_VALUE);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_CHECK_NAME_S_VALUE, resp));
			return 0;
		}
		
		int errorCode = GameUtil.tryOccupyPlayerName("", "", name);
		if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
			resp.setResult(false);
			resp.setErrorCode(errorCode);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_CHECK_NAME_S_VALUE, resp));
			return 0;
		}
		
		resp.setResult(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_CHECK_NAME_S_VALUE, resp));
		
		return 0;
	}
	
}
