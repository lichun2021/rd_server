package com.hawk.game.tsssdk.invoker;

import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Player.SensitiveWordCheckResp;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.SENSITIVE_WORD_CHECK)
public class SensitiveWordCheckInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String word, int protocol, String callback) {
		SensitiveWordCheckResp.Builder resp = SensitiveWordCheckResp.newBuilder();
		if (result != 0) {
			resp.setSensitive(true);
			resp.setErrorCode(Status.NameError.CONTAINS_FILTER_WORD_VALUE);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SENSITIVE_WORD_CHECK_S, resp));
			return 0;
		}
		
		resp.setSensitive(false);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SENSITIVE_WORD_CHECK_S, resp));
		LogUtil.logSecTalkFlow(player, null, LogMsgType.OTHER, "", word);
		
		return 0;
	}

}
