package com.hawk.game.module.schedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Schedule.HPScheduleInfoSync;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.service.guildTeam.ipml.TBLYGuildTeamManager;
import com.hawk.game.service.guildTeam.ipml.XQHXGuildTeamManager;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 待办事项管理器
 * 
 * @author lating
 */
public class ScheduleService {
	
	public static final int SYSTEM_SCHEDULE = 0; //系统待办
	public static final int GUILD_SCHEDULE  = 1; //联盟定制
	
	public static final int SCHEDULE_CREATE = 0; //新建注册待办
	public static final int SCHEDULE_UPDATE = 1; //编辑更新待办
	public static final int SCHEDULE_DELETE = 2; //删除待办
	
	public static final long SCHEDULE_REMOVE_COND = HawkTime.HOUR_MILLI_SECONDS * 4; //自动删除待办的时间条件
	

	/** 系统待办事项 */
	private Map<String, ScheduleInfo> systemSchedules = new ConcurrentHashMap<>();
	
	/** 联盟定制待办事项 */
	private Map<String, Map<String, ScheduleInfo>> guildSchedules = new ConcurrentHashMap<>();
	
	private static ScheduleService instance = new ScheduleService();
	
	public static ScheduleService getInstance() {
		return instance;
	}
	
	public void init() {
		if (systemSchedules.isEmpty()) {
			List<ScheduleInfo> globalList = RedisProxy.getInstance().getAllSchedule(null);
			for (ScheduleInfo info : globalList) {
				systemSchedules.put(info.getUuid(), info);
			}
		}

		List<String> guildIds = GuildService.getInstance().getGuildIds();
		for (String guildId : guildIds) {
			List<ScheduleInfo> guildList = RedisProxy.getInstance().getAllSchedule(guildId);
			if (guildList.isEmpty()) {
				continue;
			}
			Map<String, ScheduleInfo> schedules = new ConcurrentHashMap<>();
			for (ScheduleInfo info : guildList) {
				schedules.put(info.getUuid(), info);
			}
			guildSchedules.put(guildId, schedules);
		}
	}
	
	/**
	 * 添加系统待办事项
	 * @param schedule
	 */
	public void addSystemSchedule(ScheduleInfo schedule) {
		try {
			if (systemSchedules.isEmpty() && !GsApp.getInstance().isInitOK()) {
				List<ScheduleInfo> globalList = RedisProxy.getInstance().getAllSchedule(null);
				for (ScheduleInfo info : globalList) {
					systemSchedules.put(info.getUuid(), info);
				}
			}
			
			int type = schedule.getType();
			String guildId = schedule.getGuildId();
			String teamId = schedule.getTeamId();
			//先找出同类型的
			List<ScheduleInfo> list = systemSchedules.values().stream().filter(
					e -> e.getType() == type && e.getGuildId().equals(guildId) && e.getTeamId().equals(teamId)).collect(Collectors.toList());
			if (list != null && !list.isEmpty()) {
				ScheduleInfo oldSchedule = list.get(0);
				oldSchedule.setStartTime(schedule.getStartTime());
				oldSchedule.setContinueTime(schedule.getContinueTime());
				oldSchedule.setPosX(schedule.getPosX());
				oldSchedule.setPosY(schedule.getPosY());
				schedule = oldSchedule;
			}
			
			systemSchedules.put(schedule.getUuid(), schedule);
			RedisProxy.getInstance().addSchedule(schedule, SYSTEM_SCHEDULE);
			logSystemSchedule(schedule);
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				if (HawkOSOperator.isEmptyString(guildId) || guildId.equals(player.getGuildId())) {
					syncScheduleInfo(player);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			try {
				HawkLog.logPrintln("schedule info: {}", JSONObject.toJSONString(schedule));
				for (ScheduleInfo info : systemSchedules.values()) {
					HawkLog.logPrintln("system schedule info: {}", JSONObject.toJSONString(info));
				}
			} catch (Exception e1){
				HawkException.catchException(e1);
			}
		}
	}
	

	/**
	 * 添加联盟待办事项
	 * @param schedule
	 */
	public void addGuildSchedule(Player player, ScheduleInfo schedule, int operType) {
		String guildId = schedule.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			throw new RuntimeException("guildId empty");
		}
		Map<String, ScheduleInfo> map = guildSchedules.get(guildId);
		if (map == null) {
			guildSchedules.putIfAbsent(guildId, new ConcurrentHashMap<>());
			map = guildSchedules.get(guildId);
		}
		map.put(schedule.getUuid(), schedule);
		RedisProxy.getInstance().addSchedule(schedule, GUILD_SCHEDULE);
		logGuildSchedule(player, schedule, operType);
	}
	
	/**
	 * 获取全局待办事项
	 * @param uuid
	 * @return
	 */
	public ScheduleInfo getSystemSchedule(String uuid) {
		return systemSchedules.get(uuid);
	}
	
	/**
	 * 获取联盟待办事项
	 * @param guildId
	 * @param uuid
	 * @return
	 */
	public ScheduleInfo getGuildSchedule(String guildId, String uuid) {
		Map<String, ScheduleInfo> map = guildSchedules.get(guildId);
		if (map != null) {
			return map.get(uuid);
		}
		return null;
	}
	
	public Collection<ScheduleInfo> getGuildSchedule(String guildId) {
		Map<String, ScheduleInfo> map = guildSchedules.get(guildId);
		if (map == null) {
			return Collections.emptyList();
		}
		return map.values();
	}
	
	
	/**
	 * 移除全局待办事项
	 * @param uuid
	 */
	public void removeSystemSchedule(String uuid) {
		systemSchedules.remove(uuid);
		RedisProxy.getInstance().removeSchedule(uuid, null);
	}
	
	/**
	 * 移除联盟待办事项
	 * @param guildId
	 * @param uuid
	 */
	public void removeGuildSchedule(Player player, String guildId, String uuid) {
		Map<String, ScheduleInfo> map = guildSchedules.get(guildId);
		if (map != null) {
			ScheduleInfo schedule = map.remove(uuid);
			RedisProxy.getInstance().removeSchedule(uuid, guildId);
			if (schedule != null && player != null) {
				logGuildSchedule(player, schedule, SCHEDULE_DELETE);
			}
		}
	}
	
	/**
	 * 每天凌晨4点清除过期的待办事项
	 */
	public void removeScheduleDaily() {
		long now = HawkTime.getMillisecond();
		List<String> removeList = new ArrayList<>();
		for(ScheduleInfo info : systemSchedules.values()) {
			if (now - info.getEndTime() >= SCHEDULE_REMOVE_COND) {
				removeList.add(info.getUuid());
			}
		}
		removeList.forEach(e -> removeSystemSchedule(e));
		
		for (Entry<String, Map<String, ScheduleInfo>> entry : guildSchedules.entrySet()) {
			removeList.clear();
			String guildId = entry.getKey();
			for(ScheduleInfo info : entry.getValue().values()) {
				if (now - info.getEndTime() >= SCHEDULE_REMOVE_COND) {
					removeList.add(info.getUuid());
				}
			}
			removeList.forEach(e -> removeGuildSchedule(null, guildId, e));
		}
		
		for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
			syncScheduleInfo(player);
		}
	}
	
	/**
	 * 给玩家同步待办事项信息
	 * @param player
	 */
	public void syncScheduleInfo(Player player) {
		String guildId = player.getGuildId();
		HPScheduleInfoSync.Builder syncBuilder = HPScheduleInfoSync.newBuilder();
		if (HawkOSOperator.isEmptyString(guildId)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.SCHEDULE_INFO_SYNC_VALUE, syncBuilder));
			return;
		}
		
		Set<String> pointSet = new HashSet<>();
		for (ScheduleInfo info : systemSchedules.values()) {
			try {
				if (HawkOSOperator.isEmptyString(info.getTeamId())) {
					if (HawkOSOperator.isEmptyString(info.getGuildId()) || info.getGuildId().equals(guildId)) {
						syncBuilder.addScheduleInfo(info.toBuilder());
					}
				} else if (info.getType() == ScheduleType.SCHEDULE_TYPE_7_VALUE) { //战区特殊处理
					IWeapon signWeapon = SuperWeaponService.getInstance().getWeapon(Integer.parseInt(info.getTeamId()));
					if (signWeapon != null && signWeapon.checkSignUp(guildId) && !pointSet.contains(info.getTeamId())) {
						pointSet.add(info.getTeamId());
						syncBuilder.addScheduleInfo(info.toBuilder());
					}
				} else {
					String teamId = getPlayerTeamId(guildId, player.getId(), info.getType());
					if (info.getTeamId().equals(teamId) && info.getGuildId().equals(guildId)) {
						syncBuilder.addScheduleInfo(info.toBuilder());
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		Map<String, ScheduleInfo> map = guildSchedules.get(guildId);
		if (map != null) {
			for (ScheduleInfo info : map.values()) {
				if (info.getPlayerIds().isEmpty() || info.getPlayerIds().contains(player.getId())) {
					syncBuilder.addScheduleInfo(info.toBuilder());
				}
			}
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SCHEDULE_INFO_SYNC_VALUE, syncBuilder));
	}
	
	/**
	 * 给联盟成员同步待办事项信息
	 * @param guildId
	 */
	public void notifyGuildMember(Player player, String guildId) {
		List<String> onlinePlayerIds = GuildService.getInstance().getOnlineMembers(guildId);
		for (String playerId : onlinePlayerIds) {
			if (playerId.equals(player.getId())) {
				continue;
			}
			Player mplayer = GlobalData.getInstance().getActivePlayer(playerId);
			if (mplayer != null) {
				syncScheduleInfo(mplayer);
			}
		}
	}
	
	public String getPlayerTeamId(String guildId,String playerId,int type){
		switch (type) {
		case ScheduleType.SCHEDULE_TYPE_1_VALUE:
			//泰伯
			return TBLYGuildTeamManager.getInstance().getSelfTeamId(playerId);
		case ScheduleType.SCHEDULE_TYPE_2_VALUE:
			//先驱
			return XQHXGuildTeamManager.getInstance().getSelfTeamId(playerId);
		case ScheduleType.SCHEDULE_TYPE_3_VALUE:
			//星海
			return XHJZWarService.getInstance().getSelfTeamId(playerId);
		case ScheduleType.SCHEDULE_TYPE_5_VALUE:
			//赛博
			return CyborgWarService.cyborgWarCacheData.getPlayerTeam(guildId, playerId);
		default:
			return "";
		}
	}
	
	/**
	 * 系统待办事项记录
	 */
	public void logSystemSchedule(ScheduleInfo schedule) {
		try {
			Map<String, Object> param = new HashMap<>();
	        param.put("guildId", schedule.getGuildId());         //联盟id
	        param.put("teamId", schedule.getTeamId());           //队伍id
	        param.put("scheduleId", schedule.getUuid());         //待办事项唯一id
	        param.put("scheduleType", schedule.getType());       //待办事项类型
	        param.put("startTime", schedule.getStartTime());     //待办事项开启时间
	        param.put("continuesTime", schedule.getContinueTime()); //待办事项持续时长
	        param.put("posX", schedule.getPosX());                  //跳转地点坐标x
	        param.put("posY", schedule.getPosY());                  //跳转地点坐标y
	        LogUtil.logActivityCommon(LogInfoType.schedule_system, param);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 联盟定制待办事项记录
	 * @param operType 操作类型：0注册新增，1-编辑修改，2-删除
	 */
	public void logGuildSchedule(Player player, ScheduleInfo schedule, int operType) {
		try {
			Map<String, Object> param = new HashMap<>();
	        param.put("guildId", schedule.getGuildId());         //联盟id
	        param.put("operType", operType);                     //操作类型：0注册新增，1-编辑修改，2-删除
	        param.put("scheduleId", schedule.getUuid());         //待办事项唯一id
	        param.put("scheduleType", schedule.getType());       //待办事项类型
	        param.put("title", schedule.getTitle());             //待办事项标题
	        param.put("startTime", schedule.getStartTime());     //待办事项开启时间
	        param.put("continuesTime", schedule.getContinueTime()); //待办事项持续时长
	        param.put("posX", schedule.getPosX());                  //跳转地点坐标x
	        param.put("posY", schedule.getPosY());                  //跳转地点坐标y
	        LogUtil.logActivityCommon(player, LogInfoType.schedule_guild, param);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}
