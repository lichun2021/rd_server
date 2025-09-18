package com.hawk.activity.type.impl.aftercompetition;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.GiftRecAmountReq;
import com.hawk.game.protocol.Activity.RecBigAwardReq;
import com.hawk.game.protocol.Activity.SendBigAwardReq;
import com.hawk.game.protocol.Activity.SendGiftReq;
import com.hawk.game.protocol.Activity.SetGiftRecieverReq;

/**
 * 赛后庆典371
 * 
 * @author lating
 */
public class AfterCompetitionHandler extends ActivityProtocolHandler{

	/**
	 * 请求活动信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY371_INFO_C_VALUE)
	public void onActivityInfoReq(HawkProtocol protocol, String playerId){
		AfterCompetitionActivity activity = getActivity(ActivityType.AFTER_COMPETITION);
		if(activity != null){
			activity.syncActivityDataInfo(playerId);
		}
	}
	
	/**
	 * 致敬
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY371_HOMAGE_C_VALUE)
	public void onHomage(HawkProtocol protocol, String playerId){
		AfterCompetitionActivity activity = getActivity(ActivityType.AFTER_COMPETITION);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		int result = activity.homage(playerId);
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 购买（赠送）礼物
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY371_SEND_GIFT_C_VALUE)
	public void onSendGift(HawkProtocol protocol, String playerId){
		AfterCompetitionActivity activity = getActivity(ActivityType.AFTER_COMPETITION);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		
		SendGiftReq req = protocol.parseProtocol(SendGiftReq.getDefaultInstance());
		int result = activity.buyGift(playerId, req.getGiftId(), req.getTargetPlayerId());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 发放全服大奖
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY371_BIGAWARD_SEND_C_VALUE)
	public void distGlobalBigAward(HawkProtocol protocol, String playerId){
		AfterCompetitionActivity activity = getActivity(ActivityType.AFTER_COMPETITION);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		SendBigAwardReq req = protocol.parseProtocol(SendBigAwardReq.getDefaultInstance());
		int result = activity.distGlobalBigAward(playerId, req.getGiftId());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 领取全服大奖
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY371_BIGAWARD_REC_C_VALUE)
	public void recieveGlobalGigAward(HawkProtocol protocol, String playerId){
		AfterCompetitionActivity activity = getActivity(ActivityType.AFTER_COMPETITION);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		RecBigAwardReq req = protocol.parseProtocol(RecBigAwardReq.getDefaultInstance());
		int result = activity.recieveGlobalBigAward(playerId, req.getGiftId(), req.getChannel(), req.getUuid());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 设置礼物默认赠送对象
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY371_SET_RECIEVER_C_VALUE)
	public void setGiftRecieverReq(HawkProtocol protocol, String playerId){
		AfterCompetitionActivity activity = getActivity(ActivityType.AFTER_COMPETITION);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		SetGiftRecieverReq req = protocol.parseProtocol(SetGiftRecieverReq.getDefaultInstance());
		int result = activity.setDefaultSend(playerId, req.getGiftId(), req.getTargetPlayerId());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 查看玩家的礼物接收次数
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY371_TARGET_GIFTREC_C_VALUE)
	public void queryGiftReceiveTimes(HawkProtocol protocol, String playerId){
		AfterCompetitionActivity activity = getActivity(ActivityType.AFTER_COMPETITION);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		GiftRecAmountReq req = protocol.parseProtocol(GiftRecAmountReq.getDefaultInstance());
		int result = activity.queryReceiveGiftCount(playerId, req.getTargetPlayerId());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 请求礼物发送记录
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY371_SEND_RECORD_C_VALUE)
	public void queryGiftSendRecord(HawkProtocol protocol, String playerId){
		AfterCompetitionActivity activity = getActivity(ActivityType.AFTER_COMPETITION);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		int result = activity.querySendGiftRecord(playerId);
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 请求礼物接收记录
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY371_RECIEVE_RECORD_C_VALUE)
	public void queryGiftRecieveRecord(HawkProtocol protocol, String playerId){
		AfterCompetitionActivity activity = getActivity(ActivityType.AFTER_COMPETITION);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		int result = activity.queryRecieveGiftRecord(playerId);
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
}
