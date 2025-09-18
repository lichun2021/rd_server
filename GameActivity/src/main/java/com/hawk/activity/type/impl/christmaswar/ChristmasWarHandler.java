package com.hawk.activity.type.impl.christmaswar;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.ChristmasWarRankInfoReq;
import com.hawk.game.protocol.Activity.ChristmasWarReceiveReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class ChristmasWarHandler extends ActivityProtocolHandler {
	@ProtocolHandler(code = HP.code.CHRISTMAS_WAR_PAGE_INFO_REQ_VALUE)
	public void onPageInfoReq(HawkProtocol hawkProtocol, String playerId) {
		ChristmasWarActivity activity = this.getActivity(ActivityType.CHRISTMAS_WAR_ACTIVITY);
		activity.synPageInfo(playerId);
	}

	@ProtocolHandler(code = HP.code.CHRISTMAS_WAR_RECEIVE_REQ_VALUE)
	public void onReceiveReq(HawkProtocol hawkProtocol, String playerId) {
		ChristmasWarReceiveReq cparam = hawkProtocol.parseProtocol(ChristmasWarReceiveReq.getDefaultInstance());
		ChristmasWarActivity activity = this.getActivity(ActivityType.CHRISTMAS_WAR_ACTIVITY);
		int rlt = activity.onReceiveReq(playerId, cparam.getId());
		if (rlt == Status.SysError.SUCCESS_OK_VALUE) {
			this.responseSuccess(playerId, hawkProtocol.getType());
		} else {			
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), rlt);
		}
	}

	@ProtocolHandler(code = HP.code.CHRISTMAS_WAR_RANK_INFO_REQ_VALUE)
	public void onRankInfoReq(HawkProtocol hawkProtocol, String playerId) {
		ChristmasWarRankInfoReq cparam = hawkProtocol.parseProtocol(ChristmasWarRankInfoReq.getDefaultInstance());
		ChristmasWarActivity activity = this.getActivity(ActivityType.CHRISTMAS_WAR_ACTIVITY);
		activity.synRankInfo(playerId, cparam.getRankType());
	}
}
