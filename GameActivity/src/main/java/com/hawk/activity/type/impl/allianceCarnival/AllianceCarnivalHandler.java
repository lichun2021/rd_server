package com.hawk.activity.type.impl.allianceCarnival;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.ACAbandonMission;
import com.hawk.game.protocol.Activity.ACReceiveMission;
import com.hawk.game.protocol.HP;

/**
 * 联盟总动员
 * @author golden
 *
 */
public class AllianceCarnivalHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求界面信息
	 */
	@ProtocolHandler(code = HP.code.ALLIANCE_CARNIVAL_PAGE_INFO_REQ_VALUE)
	public void pageInfo(HawkProtocol protocol, String playerId) {
		AllianceCarnivalActivity activity = this.getActivity(ActivityType.ALLIANCE_CARNIVAL);
		activity.sync(playerId);
	}
	
	/**
	 * 接受任务
	 */
	@ProtocolHandler(code = HP.code.ALLIANCE_CARNIVAL_RECEIVE_MISSION_REQ_VALUE)
	public void receive(HawkProtocol protocol, String playerId) {
		ACReceiveMission req = protocol.parseProtocol(ACReceiveMission.getDefaultInstance());
		AllianceCarnivalActivity activity = this.getActivity(ActivityType.ALLIANCE_CARNIVAL);
		activity.receive(playerId, req.getUuid());
	}
	
	/**
	 * 放弃任务
	 */
	@ProtocolHandler(code = HP.code.ALLIANCE_CARNIVAL_ABANDON_MISSION_REQ_VALUE)
	public void abandon(HawkProtocol protocol, String playerId) {
		ACAbandonMission req = protocol.parseProtocol(ACAbandonMission.getDefaultInstance());
		AllianceCarnivalActivity activity = this.getActivity(ActivityType.ALLIANCE_CARNIVAL);
		activity.abandon(playerId, req.getUuid());
	}
	
	/**
	 * 删除任务 
	 */
	@ProtocolHandler(code = HP.code.ALLIANCE_CARNIVAL_DELETE_MISSION_REQ_VALUE)
	public void delete(HawkProtocol protocol, String playerId) {
		ACAbandonMission req = protocol.parseProtocol(ACAbandonMission.getDefaultInstance());
		AllianceCarnivalActivity activity = this.getActivity(ActivityType.ALLIANCE_CARNIVAL);
		activity.delete(playerId, req.getUuid());
	}
	
	/**
	 * 排行榜
	 */
	@ProtocolHandler(code = HP.code.ALLIANCE_CARNIVAL_RANK_INFO_REQ_VALUE)
	public void rank(HawkProtocol protocol, String playerId) {
		AllianceCarnivalActivity activity = this.getActivity(ActivityType.ALLIANCE_CARNIVAL);
		activity.pushRank(playerId);
	}
	
	/**
	 * 购买次数
	 */
	@ProtocolHandler(code = HP.code.ALLIANCE_CARNIVAL_BUY_TIMES_VALUE)
	public void buyTimes(HawkProtocol protocol, String playerId) {
		AllianceCarnivalActivity activity = this.getActivity(ActivityType.ALLIANCE_CARNIVAL);
		activity.buyTimes(playerId);
	}
	
	@ProtocolHandler(code = HP.code.ALLIANCE_CARNIVAL_EXCHANGE_REQ_VALUE)
	public void exhcange(HawkProtocol protocol, String playerId) {
		AllianceCarnivalActivity activity = this.getActivity(ActivityType.ALLIANCE_CARNIVAL);
		activity.exchange(playerId);
	}	
	
}
