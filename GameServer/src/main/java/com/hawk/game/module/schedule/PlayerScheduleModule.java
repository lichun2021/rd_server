package com.hawk.game.module.schedule;

import java.util.List;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.msg.ScheduleCheckBackMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.Schedule.ScheduleCreateReq;
import com.hawk.game.protocol.Schedule.ScheduleDeleteReq;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.protocol.Schedule.ScheduleUpdateReq;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;

/**
 * 待办事项
 * @author lating
 * @date 2025年7月18日
 */
public class PlayerScheduleModule extends PlayerModule {
	
	public PlayerScheduleModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		return true;
	}
	
	@Override
	protected boolean onPlayerLogin() {
		ScheduleService.getInstance().syncScheduleInfo(player);
		return true;
	}
	
	/**
	 * 新建注册待办事项
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.SCHEDULE_CREATE_C_VALUE)
	private void onScheduleCreate(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return;
		}
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.SCHEDULE_EDIT_AUTH)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return;
		}
		ScheduleCreateReq req = protocol.parseProtocol(ScheduleCreateReq.getDefaultInstance());
		if (HawkOSOperator.isEmptyString(req.getTitle())) {
			HawkLog.errPrintln("schedule create param error, playerId: {}, title: {}", player.getId(), req.getTitle());
			sendError(protocol.getType(), Status.Error.SCHEDULE_TITLE_EMPTY_ERR_VALUE);
			return;
		}
		if (req.getStartTime() <= HawkTime.getMillisecond()) {
			HawkLog.errPrintln("schedule create param error, playerId: {}, startTime: {}", player.getId(), req.getStartTime());
			sendError(protocol.getType(), Status.Error.SCHEDULE_TIME_ERR_VALUE);
			return;
		}
		if (req.getContinues() < 60) {
			HawkLog.errPrintln("schedule create param error, playerId: {}, continues: {}", player.getId(), req.getContinues());
			sendError(protocol.getType(), Status.Error.SCHEDULE_CONTINUES_ERR_VALUE);
			return;
		}
		
		//x: 1-758, y: 1-1518, 当x+y是偶数时，可放行
		int x = req.getPosX(), y = req.getPosY();
		if (x < 1 || x > 758 ||  y < 1 || y > 1518 || (x+y)%2 != 0) {
			HawkLog.errPrintln("schedule create param error, playerId: {}, posX: {}, posY: {}", player.getId(), req.getPosX(), req.getPosY());
			sendError(protocol.getType(), Status.Error.SCHEDULE_POSXY_ERR_VALUE);
			return;
		}
		
		if (ScheduleType.valueOf(req.getType()) == null) {
			HawkLog.errPrintln("schedule create param error, playerId: {}, type: {}", player.getId(), req.getType());
			sendError(protocol.getType(), Status.Error.SCHEDULE_TYPE_ERR_VALUE);
			return;
		}
		
		if (req.getType() == ScheduleType.SCHEDULE_TYPE_9_VALUE && ScheduleService.getInstance().getGuildSchedule(player.getGuildId()).size() >= 2) {
			HawkLog.errPrintln("schedule create touch limit, playerId: {}, type: {}", player.getId(), req.getType());
			sendError(protocol.getType(), Status.Error.SCHEDULE_TYPE9_COUNT_ERR_VALUE);
			return;
		}
		
		JSONObject gameDataJson = new JSONObject();
		JSONObject json = new JSONObject();
		json.put("type", req.getType());
		json.put("startTime", req.getStartTime());
		json.put("continues", req.getContinues());
		json.put("posX", req.getPosX());
		json.put("posY", req.getPosY());
		GameTssService.getInstance().wordUicChatFilter(player, req.getTitle(), 
				GameMsgCategory.SCHEDULE_CHECK, GameMsgCategory.SCHEDULE_CHECK, 
				json.toJSONString(), gameDataJson, protocol.getType());
	}
	
	/**
	 * 编辑更新待办事项
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.SCHEDULE_UPDATE_C_VALUE)
	private void onScheduleUpdate(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return;
		}
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.SCHEDULE_EDIT_AUTH)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return;
		}
		
		ScheduleUpdateReq req = protocol.parseProtocol(ScheduleUpdateReq.getDefaultInstance());
		ScheduleInfo schedule = ScheduleService.getInstance().getGuildSchedule(player.getGuildId(), req.getUuid());
		if (schedule == null) {
			HawkLog.errPrintln("schedule update param error, playerId: {}, schedule not exist: {}", player.getId(), req.getUuid());
			sendError(protocol.getType(), Status.Error.SCHEDULE_NOT_EXIST_VALUE);
			return;
		}
		if (HawkOSOperator.isEmptyString(req.getTitle())) {
			HawkLog.errPrintln("schedule update param error, playerId: {}, title: {}", player.getId(), req.getTitle());
			sendError(protocol.getType(), Status.Error.SCHEDULE_TITLE_EMPTY_ERR_VALUE);
			return;
		}
		if (req.getStartTime() <= HawkTime.getMillisecond()) {
			HawkLog.errPrintln("schedule update param error, playerId: {}, startTime: {}", player.getId(), req.getStartTime());
			sendError(protocol.getType(), Status.Error.SCHEDULE_TIME_ERR_VALUE);
			return;
		}
		if (req.getContinues() < 60) {
			HawkLog.errPrintln("schedule update param error, playerId: {}, continues: {}", player.getId(), req.getContinues());
			sendError(protocol.getType(), Status.Error.SCHEDULE_CONTINUES_ERR_VALUE);
			return;
		}
		//x: 1-758, y: 1-1518, 当x+y是偶数时，可放行
		int x = req.getPosX(), y = req.getPosY();
		if (x < 1 || x > 758 ||  y < 1 || y > 1518 || (x+y)%2 != 0) {
			HawkLog.errPrintln("schedule update param error, playerId: {}, posX: {}, posY: {}", player.getId(), req.getPosX(), req.getPosY());
			sendError(protocol.getType(), Status.Error.SCHEDULE_POSXY_ERR_VALUE);
			return;
		}
		if (req.getType() != schedule.getType()) {
			HawkLog.errPrintln("schedule update param error, playerId: {}, req type: {}, type: {}", player.getId(), req.getType(), schedule.getType());
			sendError(protocol.getType(), Status.Error.SCHEDULE_TYPE_ERR_VALUE);
			return;
		}
		
		JSONObject gameDataJson = new JSONObject();
		JSONObject json = new JSONObject();
		json.put("uuid", req.getUuid());
		json.put("type", req.getType());
		json.put("startTime", req.getStartTime());
		json.put("continues", req.getContinues());
		json.put("posX", req.getPosX());
		json.put("posY", req.getPosY());
		GameTssService.getInstance().wordUicChatFilter(player, req.getTitle(), 
				GameMsgCategory.SCHEDULE_CHECK, GameMsgCategory.SCHEDULE_CHECK, 
				json.toJSONString(), gameDataJson, protocol.getType());
	}
	
	@MessageHandler
	private boolean onTitleFilterBack(ScheduleCheckBackMsg msg) {
		String uuid = msg.getUuid();
		if (HawkOSOperator.isEmptyString(uuid)) {
			ScheduleInfo schedule = ScheduleInfo.createGuildSchedule(msg.getType(), player.getGuildId(), msg.getTitle(), msg.getStartTime(), msg.getContinues(), msg.getPosX(), msg.getPosY());
			ScheduleService.getInstance().addGuildSchedule(player, schedule, ScheduleService.SCHEDULE_CREATE);
			player.responseSuccess(HP.code2.SCHEDULE_CREATE_C_VALUE);
			ScheduleService.getInstance().syncScheduleInfo(player);
			ScheduleService.getInstance().notifyGuildMember(player, player.getGuildId());
			HawkLog.logPrintln("schedule create succ, playerId: {}, title: {}, startTime: {}, continues: {}, pos: {},{}", 
					player.getId(), msg.getTitle(), msg.getStartTime(), msg.getContinues(), msg.getPosX(), msg.getPosY());
			return true;
		}
		
		ScheduleInfo schedule = ScheduleService.getInstance().getGuildSchedule(player.getGuildId(), uuid);
		schedule.setTitle(msg.getTitle());
		schedule.setStartTime(msg.getStartTime());
		schedule.setContinueTime(msg.getContinues());
		schedule.setPosX(msg.getPosX());
		schedule.setPosY(msg.getPosY());
		ScheduleService.getInstance().addGuildSchedule(player, schedule, ScheduleService.SCHEDULE_UPDATE);
		player.responseSuccess(HP.code2.SCHEDULE_UPDATE_C_VALUE);
		ScheduleService.getInstance().syncScheduleInfo(player);
		ScheduleService.getInstance().notifyGuildMember(player, player.getGuildId());
		HawkLog.logPrintln("schedule update succ, playerId: {}, uuid: {}, title: {}, startTime: {}, continues: {}, pos: {},{}", 
				player.getId(), uuid, msg.getTitle(), msg.getStartTime(), msg.getContinues(), msg.getPosX(), msg.getPosY());
		return true;
	}
	
	@MessageHandler
	private boolean onGuildJoinMsg(GuildJoinMsg msg) {
		ScheduleService.getInstance().syncScheduleInfo(player);
		return true;
	}
	
	@MessageHandler
	private boolean onGuildQuitMsg(GuildQuitMsg msg) {
		ScheduleService.getInstance().syncScheduleInfo(player);
		return true;
	}
	
	
	/**
	 * 删除待办事项
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.SCHEDULE_DELETE_C_VALUE)
	private void onScheduleDelete(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return;
		}
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.SCHEDULE_EDIT_AUTH)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return;
		}
		
		ScheduleDeleteReq req = protocol.parseProtocol(ScheduleDeleteReq.getDefaultInstance());
		List<String> uuids = req.getUuidList();
		if (uuids.isEmpty()) {
			HawkLog.errPrintln("schedule delete uuids empty, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		
		int count = 0;
		for (String uuid : uuids) {
			ScheduleInfo schedule = ScheduleService.getInstance().getGuildSchedule(player.getGuildId(), uuid);
			if (schedule != null) {
				ScheduleService.getInstance().removeGuildSchedule(player, player.getGuildId(), uuid);
				count++;
			}
		}
		
		if (count == 0) {
			HawkLog.errPrintln("schedule delete no object, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		player.responseSuccess(protocol.getType());
		ScheduleService.getInstance().syncScheduleInfo(player);
		ScheduleService.getInstance().notifyGuildMember(player, player.getGuildId());
		HawkLog.logPrintln("schedule delete succ, playerId: {}, uuids size: {}, delete count: {}", player.getId(), uuids.size(), count);
	}
	
}
