package com.hawk.activity.type.impl.tiberiumGuess;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.TblyGuessReq;
import com.hawk.game.protocol.Activity.TblyGuessResp;
import com.hawk.game.protocol.Activity.TblyMatchGuessPageResp;

/**泰伯利亚竞猜活动
 * @author Winder
 *
 */
public class TiberiumGuessHandler extends ActivityProtocolHandler{
	//泰伯利亚竞猜活动信息请求
	@ProtocolHandler(code = HP.code.TBLY_GUESS_MATCH_PAGE_REQ_VALUE)
	public void tblyGuessInfoSync(HawkProtocol protocol, String playerId){
		TiberiumGuessActivity activity = getActivity(ActivityType.TBLY_GUESS);
		TblyMatchGuessPageResp.Builder builder = activity.genTblyMatchGuessPageInfo(playerId);
		if (builder != null) {
			sendProtocol(playerId, HawkProtocol.valueOf(HP.code.TBLY_GUESS_MATCH_PAGE_RESP_VALUE, builder));
		}
	}
	//竞猜
	@ProtocolHandler(code = HP.code.TBLY_GUESS_REQ_VALUE)
	public void tblyGuessWinner(HawkProtocol protocol, String playerId){
		TblyGuessReq req = protocol.parseProtocol(TblyGuessReq.getDefaultInstance());
		String id = req.getId();
		TiberiumGuessActivity activity = getActivity(ActivityType.TBLY_GUESS);
		Result<?> result = activity.guessTblyWinner(playerId, id);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		TblyGuessResp.Builder builder = (TblyGuessResp.Builder) result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.TBLY_GUESS_RESP, builder));
	}
}
