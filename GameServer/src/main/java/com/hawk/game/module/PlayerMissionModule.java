package com.hawk.game.module;

import java.util.List;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.msg.MissionMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mission.MissionBonusReq;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.MissionContext;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.type.IMission;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 任务模块
 * @author luke
 */
public class PlayerMissionModule extends PlayerModule {
	
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 构造函数
	 * @param player
	 */
	public PlayerMissionModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		// 任务登陆检测, 同步任务信息
		MissionService.getInstance().missionLoginCheck(player);
		player.getPush().syncMissionList();
		// 登陆任务刷新
		MissionService.getInstance().missionRefresh(player, MissionFunType.FUN_LOGIN, 0, 1);
		return true;
	}

	@Override
	protected boolean onPlayerLogout() {
		MissionService.getInstance().removeMissionTag(player);
		MissionService.getInstance().unloadOverlayMissionData(player.getId());
		return true;
	}

	@ProtocolHandler(code = HP.code.MISSION_LIST_C_VALUE)
	private boolean missionList(HawkProtocol protocol) {
		player.getPush().syncMissionList();
		return true;
	}

	@ProtocolHandler(code = HP.code.MISSION_BONUS_C_VALUE)
	private boolean missionBonus(HawkProtocol protocol) {
		MissionBonusReq req = protocol.parseProtocol(MissionBonusReq.getDefaultInstance());
		MissionService.getInstance().bonusMission(player, req.getMissionId());
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 刷新任务
	 * @param msg
	 */
	@MessageHandler
	private void onRefreshMission(MissionMsg msg) {
		MissionEvent event = msg.getEvent();		
		// 事件触发任务列表
		List<MissionType> touchMissions = event.touchGeneralMissions();
		if (touchMissions == null || touchMissions.isEmpty()) {
			return;
		}
		
		for(MissionType type : touchMissions) {
			IMission mission = MissionContext.getInstance().getMissions(type);
			mission.refreshGeneralMission(player, event);
		}
	}
}
