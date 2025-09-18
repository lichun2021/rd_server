package com.hawk.activity.type.impl.lotteryTicket;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistRecordReq;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistReq;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistSearchReq;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistSendReq;
import com.hawk.game.protocol.Activity.PBLotteryTicketUseReq;
import com.hawk.game.protocol.HP;

/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class LotteryTicketHandler extends ActivityProtocolHandler {
	
	/**
	 * 好友帮助信息
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.LOTTERY_TICKET_ASSIST_PAGE_REQ_VALUE)
	public void getPlayerFriendAssistPage(HawkProtocol hawkProtocol, String playerId){
		LotteryTicketActivity activity = this.getActivity(ActivityType.LOTTERY_TICKET);
		if(activity == null){
			return;
		}
		activity.getPlayerFriendAssistPage(playerId);
	}
	
	/**
	 * 搜索好友
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.LOTTERY_TICKET_ASSIST_SEARCH_REQ_VALUE)
	public void onPlayerAssistLotterySearch(HawkProtocol hawkProtocol, String playerId){
		LotteryTicketActivity activity = this.getActivity(ActivityType.LOTTERY_TICKET);
		if(activity == null){
			return;
		}
		PBLotteryTicketAssistSearchReq req = hawkProtocol.parseProtocol(PBLotteryTicketAssistSearchReq.getDefaultInstance());
		String name = req.getName();
		activity.onPlayerAssistLotterySearch(playerId, name);
	}
	
	
	/**
	 * 刮奖
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.LOTTERY_TICKET_USE_REQ_VALUE)
	public void onPlayerLottery(HawkProtocol hawkProtocol, String playerId){
		LotteryTicketActivity activity = this.getActivity(ActivityType.LOTTERY_TICKET);
		if(activity == null){
			return;
		}
		PBLotteryTicketUseReq req = hawkProtocol.parseProtocol(PBLotteryTicketUseReq.getDefaultInstance());
		int tIndex = req.getTicketIndex();
		activity.onPlayerLottery(playerId,tIndex);
	}
	
	
	/**
	 * 刮奖历史
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.LOTTERY_TICKET_USE_RECORD_REQ_VALUE)
	public void getPlayerLotteryRecord(HawkProtocol hawkProtocol, String playerId){
		LotteryTicketActivity activity = this.getActivity(ActivityType.LOTTERY_TICKET);
		if(activity == null){
			return;
		}
		activity.getPlayerLotteryRecord(playerId);
	}
	
	
	/**
	 * 邀请好友代刮
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.LOTTERY_TICKET_ASSIST_APPLY_REQ_VALUE)
	public void onPlayerAssistApply(HawkProtocol hawkProtocol, String playerId){
		LotteryTicketActivity activity = this.getActivity(ActivityType.LOTTERY_TICKET);
		if(activity == null){
			return;
		}
		PBLotteryTicketAssistSendReq req = hawkProtocol.parseProtocol(PBLotteryTicketAssistSendReq.getDefaultInstance());
		String assisId = req.getAssistId();
		int count = req.getCount();
		activity.onPlayerAssistApply(playerId, assisId, count);
	}
	
	/**
	 * 代刮
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.LOTTERY_TICKET_ASSIST_REQ_VALUE)
	public void onPlayerAssistLottery(HawkProtocol hawkProtocol, String playerId){
		LotteryTicketActivity activity = this.getActivity(ActivityType.LOTTERY_TICKET);
		if(activity == null){
			return;
		}
		PBLotteryTicketAssistReq req = hawkProtocol.parseProtocol(PBLotteryTicketAssistReq.getDefaultInstance());
		String assisId = req.getApplyId();
		int tIndex = req.getTicketIndex();
		activity.onPlayerAssistLottery(playerId, assisId,tIndex);
	}
	
	
	/**
	 * 代刮记录
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.LOTTERY_TICKET_ASSIST_RECORD_REQ_VALUE)
	public void onPlayerAssistRecord(HawkProtocol hawkProtocol, String playerId){
		LotteryTicketActivity activity = this.getActivity(ActivityType.LOTTERY_TICKET);
		if(activity == null){
			return;
		}
		PBLotteryTicketAssistRecordReq req = hawkProtocol.parseProtocol(PBLotteryTicketAssistRecordReq.getDefaultInstance());
		String assisId = req.getApplyId();
		activity.onPlayerAssistRecord(playerId, assisId);
	}
	
	
	
	/**
	 * 代刮拒绝
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.LOTTERY_TICKET_ASSIST_REFUSE_REQ_VALUE)
	public void onPlayerAssistRefuse(HawkProtocol hawkProtocol, String playerId){
		LotteryTicketActivity activity = this.getActivity(ActivityType.LOTTERY_TICKET);
		if(activity == null){
			return;
		}
		PBLotteryTicketAssistRecordReq req = hawkProtocol.parseProtocol(PBLotteryTicketAssistRecordReq.getDefaultInstance());
		String assistId = req.getApplyId();
		activity.onPlayerAssistRefuse(playerId, assistId);
	}
	
	
	/**
	 * 弹幕
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.LOTTERY_TICKET_BARRAGE_REQ_VALUE)
	public void onPlayerBarrage(HawkProtocol hawkProtocol, String playerId){
		LotteryTicketActivity activity = this.getActivity(ActivityType.LOTTERY_TICKET);
		if(activity == null){
			return;
		}
		activity.onPlayerBarrage(playerId);
	}
	
	
	
}