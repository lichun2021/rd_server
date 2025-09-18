package com.hawk.activity.type.impl.resourceDefense;

import com.hawk.game.protocol.Activity;
import com.hawk.serialize.string.SerializeHelper;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.RDBuildStationReq;
import com.hawk.game.protocol.Activity.RDBuyExpReq;
import com.hawk.game.protocol.Activity.RDCharge;
import com.hawk.game.protocol.Activity.RDGetMission;
import com.hawk.game.protocol.Activity.RDOthersReq;
import com.hawk.game.protocol.Activity.RDStealReq;
import com.hawk.game.protocol.HP;

/**
 * 资源保卫战
 * @author golden
 *
 */
public class ResourceDefenseHandler extends ActivityProtocolHandler {

	/**
	 * 获取界面信息
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_PAGE_INFO_REQ_VALUE)
	public void getPageInfo(HawkProtocol protocol, String playerId){
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.syncPageInfo(playerId);
	}
	
	/**
	 * 开采
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_BUILD_STATION_REQ_VALUE)
	public void buildStation(HawkProtocol protocol, String playerId){
		RDBuildStationReq req = protocol.parseProtocol(RDBuildStationReq.getDefaultInstance());
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.checkStealTimes(playerId);
		activity.buildStation(playerId, req.getStationId());
	}
	
	/**
	 * 获取奖励
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_GET_MISSION_REQ_VALUE)
	public void getReward(HawkProtocol protocol, String playerId){
		RDGetMission req = protocol.parseProtocol(RDGetMission.getDefaultInstance());
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.checkStealTimes(playerId);
		activity.getReward(playerId, req.getCfgId());
	}
	
	/**
	 * 资源偷取界面
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_STEAL_PAGE_REQ_VALUE)
	public void stealPage(HawkProtocol protocol, String playerId) {
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.checkStealTimes(playerId);
		activity.stealPage(playerId);
	}
	
	/**
	 * 资源偷取
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_STEAL_VALUE)
	public void steal(HawkProtocol protocol, String playerId) {
		RDStealReq req = protocol.parseProtocol(RDStealReq.getDefaultInstance());
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.checkStealTimes(playerId);
		String[] playerIdArr = SerializeHelper.split(req.getPlayerId(), SerializeHelper.ATTRIBUTE_SPLIT);
		if (playerIdArr.length == 2 && playerIdArr[0].equals("robot")) {
			int robotId =  NumberUtils.toInt(playerIdArr[1]);
			activity.stealRobot(playerId,robotId);
		} else {
			activity.steal(playerId, req.getPlayerId(), req.getStationId());
		}
	}
	
	/**
	 * 购买经验
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_BUY_EXP_REQ_VALUE)
	public void buyExp(HawkProtocol protocol, String playerId) {
		RDBuyExpReq req = protocol.parseProtocol(RDBuyExpReq.getDefaultInstance());
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.checkStealTimes(playerId);
		activity.buyExp(playerId, req.getCfgId(), protocol.getType());
	}
	
	/**
	 * 收取
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_CHARGE_REQ_VALUE)
	public void doCharge(HawkProtocol protocol, String playerId) {
		RDCharge req = protocol.parseProtocol(RDCharge.getDefaultInstance());
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.checkStealTimes(playerId);
		activity.doCharge(playerId, req.getStationId());
	}
	
	/**
	 * 加速 屏蔽，不要了
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_SPEEDUP_STATION_REQ_VALUE)
	public void doSpeedUp(HawkProtocol protocol, String playerId) {
//		RDSpeedUpStationReq req = protocol.parseProtocol(RDSpeedUpStationReq.getDefaultInstance());
//		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
//		activity.doSpeedUp(playerId, req.getStationId());
	}
	
	/**
	 * 请求帮助
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_HELP_STATION_REQ_VALUE)
	public void requestHelp(HawkProtocol protocol, String playerId) {
//		RDHelpStationReq req = protocol.parseProtocol(RDHelpStationReq.getDefaultInstance());
//		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
//		activity.requestHelp(playerId, req.getStationId());
	}
	
	/**
	 * 帮助
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_HELP_REQ_VALUE)
	public void help(HawkProtocol protocol, String playerId) {
//		RDHelpReq req = protocol.parseProtocol(RDHelpReq.getDefaultInstance());
//		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
//		activity.help(playerId, req.getPlayerId(), req.getStationId());
	}
	
	/**
	 * 查看其他人
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_OTHERS_REQ_VALUE)
	public void others(HawkProtocol protocol, String playerId) {
		RDOthersReq req = protocol.parseProtocol(RDOthersReq.getDefaultInstance());
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.checkStealTimes(playerId);
		activity.others(playerId, req.getPlayerId());
	}

	/**
	 * 特工技能刷新技能
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_AGENT_SKILL_REFRESH_REQ_VALUE)
	public void agentSkillRefresh(HawkProtocol protocol, String playerId) {
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.onRefreshAgentSkill(playerId, protocol.getType());
	}

	/**
	 * 特工技能激活
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_AGENT_SKILL_ACTIVE_REQ_VALUE)
	public void agentSkillActive(HawkProtocol protocol, String playerId) {
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.onActiveAgentSkill(playerId, protocol.getType());
	}

	/**
	 * 高级机器人激活
	 */
	@ProtocolHandler(code = HP.code.RESOURCE_DEFENSE_ROBOT_ACTIVE_REQ_VALUE)
	public void robotActive(HawkProtocol protocol, String playerId) {
		Activity.RobotActiveReq req = protocol.parseProtocol(Activity.RobotActiveReq.getDefaultInstance());
		ResourceDefenseActivity activity = getActivity(ActivityType.RESOURCE_DEFENSE);
		activity.onActiveGreatRobot(playerId, req.getSkillId(),protocol.getType());
	}




}
