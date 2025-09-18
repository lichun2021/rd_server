package com.hawk.activity.type.impl.mergecompetition;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.MergeCompeteGiftRewardReq;
import com.hawk.game.protocol.Activity.MergeCompeteRankDataReq;
import com.hawk.game.protocol.HP;

/**
 * 新服合服比拼活动
 * 
 * @author lating
 *
 */
public class MergeCompetitionActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取活动界面信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MERGE_COMPETE_ACTIVITY_INFO_C_VALUE)
	public boolean onActivityInfoReq(HawkProtocol protocol, String playerId) {
		MergeCompetitionActivity activity = getActivity(ActivityType.MERGE_COMPETITION);
		activity.syncActivityDataInfo(playerId);
		return true;
	}
	
	/**
	 * 请求榜单数据
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MERGE_COMPETE_SINGLE_RANK_C_VALUE)
	public boolean onActivityRankInfoReq(HawkProtocol protocol, String playerId) {
		MergeCompetitionActivity activity = getActivity(ActivityType.MERGE_COMPETITION);
		MergeCompeteRankDataReq req = protocol.parseProtocol(MergeCompeteRankDataReq.getDefaultInstance());
		activity.syncActivityRankInfo(playerId, req.getRankType(), protocol.getType());
		return true;
	}
	
	/**
	 * 请求领取嘉奖礼包
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MERGE_COMPETE_GIFT_AWARD_C_VALUE)
	public boolean onGiftAwardReq(HawkProtocol protocol, String playerId) {
		MergeCompetitionActivity activity = getActivity(ActivityType.MERGE_COMPETITION);
		MergeCompeteGiftRewardReq req = protocol.parseProtocol(MergeCompeteGiftRewardReq.getDefaultInstance());
		activity.recieveGiftAward(playerId, req.getAwardId(), protocol.getType());
		return true;
	}
	
}
