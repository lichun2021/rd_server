package com.hawk.activity.type.impl.ghostSecret;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.DrewTreasureReq;
import com.hawk.game.protocol.Activity.DrewTreasureResp;
import com.hawk.game.protocol.Activity.DrewTreasureResp.Builder;

/**幽灵秘宝活动消息处理
 * @author Winder
 */
public class GhostSecretHandler extends ActivityProtocolHandler {
	//挖幽灵秘宝
	@ProtocolHandler(code = HP.code.GHOST_SECRET_DREW_CARD_REQ_VALUE)
	public void drewGhostSecret(HawkProtocol protocol, String playerId){
		DrewTreasureReq req = protocol.parseProtocol(DrewTreasureReq.getDefaultInstance());
		GhostSecretActivity activity = getActivity(ActivityType.GHOST_SECRET_ACTIVITY);
		Result<?> result = activity.drewGhostSecret(playerId, req.getDigIndex(), req.getDrewType(), HP.code.GHOST_SECRET_DREW_CARD_REQ_VALUE);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		DrewTreasureResp.Builder builder = (Builder) result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.GHOST_SECRET_DREW_CARD_RESP_VALUE, builder));
		//activity.syncActivityDataInfo(playerId);
	}
	//重置次数
	@ProtocolHandler(code = HP.code.GHOST_SECRET_RESET_REQ_VALUE)
	public void resetDrewTimes(HawkProtocol protocol, String playerId){
		GhostSecretActivity activity = getActivity(ActivityType.GHOST_SECRET_ACTIVITY);
		activity.resetGhostSecret(playerId, HP.code.GHOST_SECRET_RESET_REQ_VALUE);
	}
}
