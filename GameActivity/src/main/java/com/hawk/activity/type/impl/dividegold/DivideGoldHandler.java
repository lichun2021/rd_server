package com.hawk.activity.type.impl.dividegold;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.AskForFuZiReq;
import com.hawk.game.protocol.Activity.GiveFuZiReq;
import com.hawk.game.protocol.Activity.OpenBoxResponse;
import com.hawk.game.protocol.Const.ChatType;

/**瓜分金币活动消息处理
 * @author Winder
 */
public class DivideGoldHandler extends ActivityProtocolHandler {
	//活动信息
	@ProtocolHandler(code = HP.code.DIVIDE_GOLD_INFO_REQ_VALUE)
	public void divideGoldInfoSync(HawkProtocol protocol, String playerId){
		DivideGoldActivity activity = getActivity(ActivityType.DIVIDE_GOLD_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
	}
	//开宝箱
	@ProtocolHandler(code = HP.code.DIVIDE_GOLD_OPEN_TREASURE_BOX_REQ_VALUE)
	public void openTreasureBox(HawkProtocol protocol, String playerId){
		DivideGoldActivity activity = getActivity(ActivityType.DIVIDE_GOLD_ACTIVITY);
		Result<?> result = activity.openTreasureBox(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		OpenBoxResponse.Builder builder = (OpenBoxResponse.Builder) result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.DIVIDE_GOLD_OPEN_TREASURE_BOX_RESP_VALUE, builder));
	}
	//合成红包
	@ProtocolHandler(code = HP.code.DIVIDE_GOLD_COMPOUND_RED_ENVELOPE_REQ_VALUE)
	public void compoundRedEnvelope(HawkProtocol protocol, String playerId){
		DivideGoldActivity activity = getActivity(ActivityType.DIVIDE_GOLD_ACTIVITY);
		Result<Void> result = activity.compoundRedEnvelope(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		responseSuccess(playerId, protocol.getType());
	}
	//开红包
	@ProtocolHandler(code = HP.code.DIVIDE_GOLD_OPEN_RED_ENVELOPE_REQ_VALUE)
	public void openRedEnvelope(HawkProtocol protocol, String playerId){
		DivideGoldActivity activity = getActivity(ActivityType.DIVIDE_GOLD_ACTIVITY);
		Result<Void> result = activity.openRedEnvelope(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		responseSuccess(playerId, protocol.getType());
	}
	//索要福字
	@ProtocolHandler(code = HP.code.DIVIDE_GOLD_ASK_FOR_FUZI_REQ_VALUE)
	public void askForFuZiItem(HawkProtocol protocol, String playerId){
		DivideGoldActivity activity = getActivity(ActivityType.DIVIDE_GOLD_ACTIVITY);
		AskForFuZiReq req = protocol.parseProtocol(AskForFuZiReq.getDefaultInstance());
		String itemStr = req.getAskForItem();
		int chatType = req.getChatType();
		Result<Void> result = activity.askForFuZi(playerId, itemStr, ChatType.valueOf(chatType));
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		responseSuccess(playerId, protocol.getType());
	}
	//赠送福字
	@ProtocolHandler(code = HP.code.DIVIDE_GOLD_GIVE_FUZI_REQ_VALUE)
	public void giveFuZi(HawkProtocol protocol, String playerId){
		DivideGoldActivity activity = getActivity(ActivityType.DIVIDE_GOLD_ACTIVITY);
		GiveFuZiReq req = protocol.parseProtocol(GiveFuZiReq.getDefaultInstance());
		Result<Void> result = activity.giveFuZi(playerId, req.getToPlayerId(), req.getGiveItem(), req.getUuid());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		responseSuccess(playerId, protocol.getType());
	}
}
