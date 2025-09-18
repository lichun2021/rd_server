package com.hawk.activity.type.impl.inviteMerge;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.ActivityInviteMerge.InviteMergeOper;

public class InviteMergeHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求界面信息
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_INVITE_MERGE_PAGE_REQ_VALUE)
	public void pageInfo(HawkProtocol hawkProtocol, String playerId) {
		InviteMergeActivity activity = getActivity(ActivityType.INVITE_MERGE);
		activity.inviteMergePageInfo(playerId);
	}
	
	/**
	 * 合服邀请信息(实力排行,邀请目标,接受邀请)
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_INVITE_MERGE_INFO_REQ_VALUE)
	public void rankInfo(HawkProtocol hawkProtocol, String playerId) {
		InviteMergeActivity activity = getActivity(ActivityType.INVITE_MERGE);
		activity.inviteMergeRankInfo(playerId);
	}
	
	/**
	 * 合服邀请进度
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_INVITE_MERGE_PROGRESS_REQ_VALUE)
	public void progress(HawkProtocol hawkProtocol, String playerId) {
		InviteMergeActivity activity = getActivity(ActivityType.INVITE_MERGE);
		activity.getProgress(playerId);
	}
	
	/**
	 * 合服邀请
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_INVITE_MERGE_INVITE_VALUE)
	public void invite(HawkProtocol hawkProtocol, String playerId) {
		InviteMergeActivity activity = getActivity(ActivityType.INVITE_MERGE);
		
		// 邀请
		InviteMergeOper req = hawkProtocol.parseProtocol(InviteMergeOper.getDefaultInstance());
		activity.invite(playerId, req.getServerId(), false);
		
		// 同步信息
		activity.inviteMergeRankInfo(playerId);
	}
	
	/**
	 * 撤回合服邀请
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_INVITE_MERGE_REVOKE_VALUE)
	public void delInvite(HawkProtocol hawkProtocol, String playerId) {
		InviteMergeActivity activity = getActivity(ActivityType.INVITE_MERGE);
		
		// 邀请
		InviteMergeOper req = hawkProtocol.parseProtocol(InviteMergeOper.getDefaultInstance());
		activity.invite(playerId, req.getServerId(), true);
		
		// 同步信息
		activity.inviteMergeRankInfo(playerId);
	}
	
	/**
	 * 接受合服邀请
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_INVITE_MERGE_ACCEPT_VALUE)
	public void accept(HawkProtocol hawkProtocol, String playerId) {
		InviteMergeActivity activity = getActivity(ActivityType.INVITE_MERGE);
		
		// 邀请
		InviteMergeOper req = hawkProtocol.parseProtocol(InviteMergeOper.getDefaultInstance());
		boolean accept = activity.acceptInvite(playerId, req.getServerId());
		
		// 同步信息
		if (accept) {
			activity.inviteMergePageInfo(playerId);
		} else {
			activity.inviteMergeRankInfo(playerId);
		}
	}
	
	/**
	 * 拒绝合服邀请
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_INVITE_MERGE_REFUSE_VALUE)
	public void refuse(HawkProtocol hawkProtocol, String playerId) {
		InviteMergeActivity activity = getActivity(ActivityType.INVITE_MERGE);
		
		// 邀请
		InviteMergeOper req = hawkProtocol.parseProtocol(InviteMergeOper.getDefaultInstance());
		activity.refuseInvite(playerId, req.getServerId());
		
		// 同步信息
		activity.inviteMergeRankInfo(playerId);
	}
	
	/**
	 * 投票
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_INVITE_MERGE_TICKET_VALUE)
	public void ticket(HawkProtocol hawkProtocol, String playerId) {
		InviteMergeActivity activity = getActivity(ActivityType.INVITE_MERGE);
		
		// 邀请
		InviteMergeOper req = hawkProtocol.parseProtocol(InviteMergeOper.getDefaultInstance());
		activity.ticket(playerId, req.getTicket());
		
		// 同步信息
		activity.inviteMergePageInfo(playerId);
	}
}