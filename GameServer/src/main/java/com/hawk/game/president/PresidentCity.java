package com.hawk.game.president;

import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.annotation.SerializeField;
import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;

import com.alibaba.fastjson.JSON;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.OccupyPresidentEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.entity.OfficerEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.module.obelisk.service.mission.ObeliskMissionType;
import com.hawk.game.module.schedule.ScheduleInfo;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.msg.PlayerLockImageMsg;
import com.hawk.game.msg.PlayerLockImageMsg.LockParam;
import com.hawk.game.msg.PlayerLockImageMsg.LockType;
import com.hawk.game.msg.PlayerUnlockImageMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg.PLAYERSTAT_PARAM;
import com.hawk.game.msg.PlayerUnlockImageMsg.UnlockType;
import com.hawk.game.nation.NationService;
import com.hawk.game.player.Player;
import com.hawk.game.president.model.President;
import com.hawk.game.president.model.PresidentCrossAccumulateInfo;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.President.OfficerType;
import com.hawk.game.protocol.President.PresidentInfo;
import com.hawk.game.protocol.President.PresidentInfoSync;
import com.hawk.game.protocol.President.PresidentPeriod;
import com.hawk.game.protocol.President.PresidentTowerStatus;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventControlPresident;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.PresidentTowerPointId;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 王城主类
 * @author zhenyu.shang
 * @since 2017年12月4日
 * 新增属性如果不是基础类型,需要注意,参考president的用法.
 */
public class PresidentCity {
	
	/** 王城状态 */
	@SerializeField
	private int status;
	
	/** 国王 */
	@SerializeField
	private President president;
	
	/** 当前占领联盟 */
	@SerializeField
	private String guildId;
	
	/** 加时阶段占领联盟 */
	@SerializeField
	private String overTimeGuildId;
	
	/** 国王战开始时间 */
	@SerializeField
	private long startTime;
	
	/** 国王战结束时间 */
	@SerializeField
	private long endTime;
	
	/** 国王战期数 */
	@SerializeField
	private int turnCount;
	
	/** 占领时间 */
	@SerializeField
	private long occupyTime;
	
	/** 国家名字 */
	@SerializeField
	private String countryName;
	
	/** 国家名字修改次数 */
	@SerializeField
	private int countryModifyTimes;
	
	@SerializeField
	private long lastTickTime;
	/**
	 * 兼容老数据.
	 */
	@SerializeField
	private int version = 1;
	
	/**
	 * 宣言
	 */
	@SerializeField
	private String manifesto;
	
	/**
	 *更新宣言的次数.
	 */
	@SerializeField
	private int updateManifestoTimes;	
	
	/** 王城箭塔 */
	private List<PresidentTower> presidentTowers;
	
	/** 国王战开始公告提示步骤 */
	private int warStartNoticeStep = 0;
	
	/** 国王战结束公告提示步骤 */
	private int warEndNoticeStep = 0;
	/**
	 * 同步国王的时间.
	 */
	private int lastPushTime;
	
	/**
	 * 跨服的国王信息
	 */
	private CrossPlayerStruct crossKingInfo;
	
	/**
	 * {playerId, serverId 记录玩家是哪个服的跨服国王.
	 */
	private Map<String, String> crossServerKing = new HashMap<>();
	/**
	 * 上一次的公告时间.
	 */
	private int lastNoticeTime;
	
	
	public boolean init() {
		try {
			Field[] fields = getClass().getDeclaredFields();
			Field version = getClass().getDeclaredField("version");
			Object versionValue = LocalRedis.getInstance().getPresidentDataByKey("version", version.getType());
			if (versionValue == null) {
				for (Field field : fields) {
					field.setAccessible(true);
					SerializeField anno = field.getAnnotation(SerializeField.class);
					if(anno != null){
						String key = field.getName();
						Object obj = LocalRedis.getInstance().getPresidentDataByKey(key, field.getType());
						if(obj != null){
							field.set(this, obj);
						}
					}
				}
				//0版本的数据向1版本迁移, 有的服已经打了，有的服没有打.
				if (this.getPresident() != null) {
					String str = JSON.toJSONString(this.getPresident());
					LocalRedis.getInstance().updatePresidentDataByKey("president", str);
				}				
				LocalRedis.getInstance().updatePresidentDataByKey("version", this.version);
				
			} else if (((Integer)versionValue).intValue() == 1) {
				//1
				for (Field field : fields) {
					field.setAccessible(true);
					SerializeField anno = field.getAnnotation(SerializeField.class);
					if (anno != null) {
						String key = field.getName();
						Object obj;
						if (field.getType().isPrimitive() || field.getType() == String.class) {
							obj = LocalRedis.getInstance().getPresidentDataByKey(key, field.getType());
							if(obj != null){
								field.set(this, obj);
							}
						} else {
							obj = LocalRedis.getInstance().getPresidentDataByKey(key, String.class);
							if (obj != null) {
								field.set(this, JSON.parseObject((String)obj, field.getType()));
							}
						} 
					}
				}
			} 		
			
			//初始化箭塔
			presidentTowers = new ArrayList<PresidentTower>();
			for (PresidentTowerPointId pointId : PresidentTowerPointId.values()) {
				PresidentTower tower = new PresidentTower(); 
				tower.setIndex(pointId.getPointId());
				if(!tower.init()){
					return false;
				}
				presidentTowers.add(tower);
			}
			
			//初始化完之后，如果状态为空，则是第一次启服
			if(status == 0){
				setStatus(PresidentPeriod.INIT_VALUE);
				//设置开始时间
				long serverOpenTime = GameUtil.getServerOpenTime();
				if(serverOpenTime <= 0){
					throw new HawkException("server open time is zero...");
				}
				PresidentConstCfg constCfg = PresidentConstCfg.getInstance();
				
				// 计算基准时间
				long baseTime = serverOpenTime + (constCfg.getInitPeaceTime() * 1000L);
				long curTime = HawkTime.getMillisecond();
				if(baseTime > curTime){
					long startTime = HawkTime.getNextTimeDayOfWeek(baseTime, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute()); 
					setStartTime(startTime);
				}else{
					//下周四开  这种情况主要是拆服，拆完服后 当前时间大于基准时间，由于serverIdenty的变化导致记录为空需要初始化
					long weekTime = HawkTime.getFirstDayOfCurWeek().getTime();
					weekTime += HawkTime.DAY_MILLI_SECONDS * 7;
					setStatus(PresidentPeriod.PEACE_VALUE);
					long startTime = HawkTime.getNextTimeDayOfWeek(weekTime, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute());
					setStartTime(startTime);
				}
			} else if(status == PresidentPeriod.WARFARE_VALUE || status == PresidentPeriod.OVERTIME_VALUE){
				//一般情况下，是不会在战争状态和加时状态停服的。如果启服后, 当前是战争状态, 或者是加时状态, 重置开始时间和占领时间
				long offset = HawkTime.getMillisecond() - getLastTickTime();
				setStartTime(startTime + offset);
				if(occupyTime > 0){
					setOccupyTime(occupyTime + offset);
				}
			} else if(status == PresidentPeriod.INIT_VALUE){
				//如果是初始化状态，则可以在启服时重新加载配置，设置开始时间, 2018-3-14修改
				long serverOpenTime = GameUtil.getServerOpenTime();
				if(serverOpenTime <= 0){
					throw new HawkException("server open time is zero...");
				}
				PresidentConstCfg constCfg = PresidentConstCfg.getInstance();
				
				// 计算基准时间
				long baseTime = serverOpenTime + (constCfg.getInitPeaceTime() * 1000L);
				baseTime = Math.max(HawkTime.getMillisecond(), baseTime);
				long startTime = HawkTime.getNextTimeDayOfWeek(baseTime, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute());
				setStartTime(startTime);
			}
			
			PresidentFightService.logger.info("president init over, status : {} , next startTime : {}", getStatus(), new Date(getStartTime()));
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		//获取本区的国王.
		CrossPlayerStruct playerStruct = RedisProxy.getInstance().getServerKing(GsConfig.getInstance().getServerId());
		this.crossKingInfo = playerStruct;
		this.loadCrossServerKing();
		
		return true;
	}
	
	/**
	 * 添加待办事项提醒
	 * @param startTime
	 * @param posX
	 * @param posY
	 */
	private void addSchedule(long startTime, int posX, int posY) {
		ScheduleInfo schedule = ScheduleInfo.createNewSchedule(ScheduleType.SCHEDULE_TYPE_8_VALUE, "", startTime, posX, posY);
		ScheduleService.getInstance().addSystemSchedule(schedule);
	}
	
	public void loadCrossServerKing() {
		this.crossServerKing = RedisProxy.getInstance().getCrossServerKing();
	}

	/**
	 * 王城tick
	 */
	@SuppressWarnings("deprecation")
	public void tick(){
		long currTime = HawkTime.getMillisecond();
		PresidentConstCfg constCfg = PresidentConstCfg.getInstance();

		int centerX = WorldMapConstProperty.getInstance().getWorldCenterX();
		int centerY = WorldMapConstProperty.getInstance().getWorldCenterY();
		
		switch (getStatus()) {
		case PresidentPeriod.INIT_VALUE:
			//战争将要开始系统通知
			presidentWarWillStartNotice((int) ((getStartTime() - currTime) / 1000));
			
			// 过了初始和平期, 进入战争期
			if (currTime >= getStartTime()) {
				warStartNoticeStep = 0;
				
				// 国王战开始系统通知 
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
						Const.NoticeCfgId.PRESIDENT_WAR_STARTED, null, centerX + "," + centerY);
				
				setStatus(PresidentPeriod.WARFARE_VALUE);
				setTurnCount(getTurnCount() + 1);
				
				//设置箭塔有开战状态
				for (PresidentTower presidentTower : presidentTowers) {
					presidentTower.setTowerStatus(PresidentTowerStatus.TOWER_FIGHT_VALUE);
				}
				notifyPresidentPeriodChanged(PresidentPeriod.INIT_VALUE, PresidentPeriod.WARFARE_VALUE);
				
				//战争阶段清理掉所有的官员任命.
				PresidentOfficier.getInstance().onWarStart();
				//进入战斗阶段之后清理.
				resetManifestoInfo();
				//清理掉国王
				clearPresident();
			}
			break;
		case PresidentPeriod.PEACE_VALUE:
			//战争将要开始系统通知
			presidentWarWillStartNotice((int) ((getStartTime() - currTime) / 1000));

			// 和平时间过了就直接进入下一阶段的战争期
			if (currTime >= getStartTime()) {
				// 改变当前进入战争
				warStartNoticeStep = 0;
				// 国王战开始系统通知 
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
						Const.NoticeCfgId.PRESIDENT_WAR_STARTED, null, centerX + "," + centerY);

				setStartTime(currTime);
				setEndTime(0);
				setGuildId(null);
				setOverTimeGuildId(null);
				setStatus(PresidentPeriod.WARFARE_VALUE);
				setTurnCount(getTurnCount() + 1);
				// 清除之前的战争记录
				LocalRedis.getInstance().clearPresidentEvent();
				
				//设置箭塔有开战状态
				for (PresidentTower presidentTower : presidentTowers) {
					// 清除之前的战争记录
					LocalRedis.getInstance().clearPresidentTowerEvent(presidentTower.getIndex());
					
					presidentTower.setGuildId(null);
					presidentTower.setLeaderId(null);
					presidentTower.setLeaderName(null);
					presidentTower.setOccupyTime(0);
					presidentTower.setTowerStatus(PresidentTowerStatus.TOWER_FIGHT_VALUE);
				}
				
				notifyPresidentPeriodChanged(PresidentPeriod.PEACE_VALUE, PresidentPeriod.WARFARE_VALUE);
				
				PresidentOfficier.getInstance().onWarStart();
				//清理掉国王
				clearPresident();
				//清理掉宣言.
				resetManifestoInfo();
			} else {
				
				if (!HawkOSOperator.isEmptyString(this.getPresidentPlayerId()) && currTime - endTime > constCfg.getAppointTime()) {
					//超过规定的手动设置时间就自动设置成总统.
					if (PresidentOfficier.getInstance().isProvisionalPresident()) {
						this.onAutoSetPresident();
					}
				}
			}
			break;
		case PresidentPeriod.WARFARE_VALUE:
			warfareTick(currTime, constCfg);
			break;
		case PresidentPeriod.OVERTIME_VALUE:
			setLastTickTime(currTime);
			//先判断占领者是否变了,如果变了，则立即结束战斗
			if(!getGuildId().equals(getOverTimeGuildId())){
				noPresidentOver(currTime, constCfg, PresidentPeriod.OVERTIME_VALUE);
			} else {
//				boolean isCrossOpen = CrossActivityService.getInstance().isOpen();
//				if (isCrossOpen) {
//					PresidentCrossRateInfo crossRateInfo = CrossActivityService.getInstance().getCrossRateInfo();
//					if (crossRateInfo.isOccupySuccess()) {
//						presidentWinOver(currTime, constCfg);
//					}
//				} else {
					//没变则继续等待占领结束
					if (currTime - getOccupyTime() >= getOccupationTime() * 1000L) {
						presidentWinOver(currTime, constCfg);
					}
				//}
			}
			break;
		default:
			break;
		}
		
		kingTick();
	}

	/**
	 * 战争阶段tick
	 * @param currTime
	 * @param constCfg
	 */
	private void warfareTick(long currTime, PresidentConstCfg constCfg) {
		setLastTickTime(currTime);
		boolean isActivityOpen = CrossActivityService.getInstance().isOpen();
		if (isActivityOpen) {
			crossWarfareTick(currTime, constCfg);
		} else {
			commonWarfareTick(currTime, constCfg);
		}
	}

	/**
	 * 普通盟总战争状态tick
	 * @param currTime
	 * @param constCfg
	 */
	@SuppressWarnings("deprecation")
	private void commonWarfareTick(long currTime, PresidentConstCfg constCfg) {
		// 有攻击者盟主
		if (!HawkOSOperator.isEmptyString(guildId)) {
			// 箭塔心跳
			for (PresidentTower presidentTower : presidentTowers) {
				presidentTower.tick();
			}
			// 战争将要进入加时系统通知
			if(getStartTime() + getWarFareTime() * 1000L - occupyTime < getOccupationTime() * 1000L){
				presidentWarWillOverTime();
			}
			// 攻击者是否占领超过一定时间段, 担任国王进入和平时期
			if (currTime - getOccupyTime() >= getOccupationTime() * 1000L && !HawkOSOperator.isEmptyString(guildId)) {
				presidentWinOver(currTime, constCfg);
			} else if(currTime > getStartTime() + getWarFareTime() * 1000L){ //占领时间不够，但战争周期已经结束，进入加时状态
				//记录当前占领的联盟id
				setOverTimeGuildId(guildId);
				//改变状态
				setStatus(PresidentPeriod.OVERTIME_VALUE);
				// 获取获胜联盟信息
				String guildName = GuildService.getInstance().getGuildName(guildId);
				//发送加时公告
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.PRESIDENT_WAR_OVERTIME, null, guildName);
				notifyPresidentPeriodChanged(PresidentPeriod.WARFARE_VALUE, PresidentPeriod.OVERTIME_VALUE);
			}
		} else {//如果一直没人占领, 则到时间后自动结束
			if(currTime > getStartTime() + getWarFareTime() * 1000L){
				noPresidentOver(currTime, constCfg, PresidentPeriod.WARFARE_VALUE);
			}
		}
	}
	
	/**
	 * 跨服盟总战争状态tick
	 * @param currTime
	 * @param constCfg
	 */
	@SuppressWarnings("deprecation")
	private void crossWarfareTick(long currTime, PresidentConstCfg constCfg) {
		// 箭塔心跳
		for (PresidentTower presidentTower : presidentTowers) {
			presidentTower.tick();
		}
		long fightEnd = getStartTime() + getWarFareTime() * 1000L;
		// 攻击者是否占领超过一定时间段, 担任国王进入和平时期
		PresidentCrossAccumulateInfo info = CrossActivityService.getInstance().getAccumulateInfo();
		if (info.isOccupySuccess(fightEnd)) {
			presidentWinOver(currTime, constCfg);
		}else if(currTime > fightEnd + HawkTime.MINUTE_MILLI_SECONDS * 10){
			//超过限定时间都没有占领成功,说明有异常了
			noPresidentOver(currTime, constCfg, PresidentPeriod.WARFARE_VALUE);
		}
	}
	
	/**
	 * 半个小时tick 一次,处理国王相关
	 * 半个小时推送一次国王的信息,
	 * 半个小时拉取一次跨服国王的信息.
	 */
	private void kingTick() {
		int curTime = HawkTime.getSeconds();
		int perioTime = 1800;
		try {
			if (curTime > perioTime + lastPushTime) {
				lastPushTime = curTime;
				CrossPlayerStruct.Builder kingBuilder = null;
				String kingPlayerId = this.getPresidentPlayerId();
				if (HawkOSOperator.isEmptyString(kingPlayerId)) {
					kingBuilder = CrossPlayerStruct.newBuilder();
					kingBuilder.setServerId(GsConfig.getInstance().getServerId());
				} else {
					//本服的玩家写入信息, //非本服玩家,不做任何处理
					if (GlobalData.getInstance().isLocalPlayer(kingPlayerId)) {
						Player kingPlayer = GlobalData.getInstance().makesurePlayer(kingPlayerId);
						kingBuilder = BuilderUtil.buildCrossPlayer(kingPlayer);
						
						RedisProxy.getInstance().updateServerKing(kingBuilder.build());
					}					
				}
				
				//半个小时读取一次跨服国王的信息吧
				this.loadCrossServerKing();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
	}
	
	/**
	 * 跨服国王在生成国王的时候就写进入.
	 * @param kingBuilder
	 */
	public void pushKingInfo(CrossPlayerStruct kingBuilder) {
		this.crossKingInfo = kingBuilder;
		RedisProxy.getInstance().updateServerKing(kingBuilder);					
	}
	
	public void pushCrossKing(CrossPlayerStruct kingBuilder) {
		RedisProxy.getInstance().addCrossServerKing(GsConfig.getInstance().getServerId(), 
				kingBuilder.getPlayerId(), PresidentConstCfg.getInstance().getCrossKingExpireTime());
	}

	@SuppressWarnings("deprecation")
	private void noPresidentOver(long currTime, PresidentConstCfg constCfg, int status) {
		//设置本届国王为空
		String lastPresidentId = null;
		if (this.president != null) {
			lastPresidentId = this.president.getPlayerId();
		}
		this.president = null;
		LocalRedis.getInstance().updatePresidentDataByKey("president", null);
		//清理攻击状态
		setGuildId(null);
		setOverTimeGuildId(null);
		setOccupyTime(0);
		setStatus(PresidentPeriod.PEACE_VALUE);
		setEndTime(currTime);
		
		//设置箭塔有开战状态
		for (PresidentTower presidentTower : presidentTowers) {
			presidentTower.setGuildId(null);
			presidentTower.setLeaderId(null);
			presidentTower.setLeaderName(null);
			presidentTower.setOccupyTime(0);
			presidentTower.setTowerStatus(PresidentTowerStatus.TOWER_PEACE_VALUE);
		}
		
		//设置下次开启时间
		setStartTime(HawkTime.getNextTimeDayOfWeek(getEndTime() + GsConst.WEEK_MILLI_SECONDS, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute()));
				
		//通知国王战结束
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
				Const.NoticeCfgId.PRESIDENT_WAR_ENDED_NO_WIN, null);
		
		notifyPresidentPlayerChanged(lastPresidentId, "", "");
		
		notifyPresidentPeriodChanged(status, PresidentPeriod.PEACE_VALUE);
		
		RedisProxy.getInstance().setCrossPresidentFightOver();
		
		PresidentFightService.logger.info("president is over, there is no win in this Tenure, the TenureCount is {}", turnCount);
	}

	private void presidentWinOver(long currTime, PresidentConstCfg constCfg) {
		if (CrossActivityService.getInstance().isOpen()) {
			try {
				crossPresidentWinOver(currTime, constCfg);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		} else {
			localPresidentWinOver(currTime, constCfg);
		}
	}
	
	private void localPresidentWinOver(long currTime, PresidentConstCfg constCfg) {
		// 直接进入和平期
		warEndNoticeStep = 0;
		
		int guildFlag = GuildService.getInstance().getGuildFlag(guildId);
		String guildName = GuildService.getInstance().getGuildName(guildId);
		String guildTag = GuildService.getInstance().getGuildTag(guildId);
		String leaderId = GuildService.getInstance().getGuildLeaderId(guildId);
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
		String leaderName = leader.getName();
		int leaderIcon = leader.getIcon();
		String leaderPfIcon = leader.getPfIcon();
		String serverId = GsConfig.getInstance().getServerId();
		
		PresidentFightService.logger.info("start send nation notify, the targetServerId is {}, the local serverId is {}", serverId, GsConfig.getInstance().getServerId());
		
		//上届国王Id
		String lastPresidentId = null;
		if(president == null){
			president = new President();
			president.setTenure(currTime);
		} else {
			lastPresidentId = president.getPlayerId();
			if (!leaderId.equals(lastPresidentId)) {
				president.setTenure(currTime);
			}
		}
		
		president.setPlayerId(leaderId);
		president.setPlayerName(leaderName);
		president.setPlayerGuildId(guildId);
		president.setPlayerGuildName(guildName);
		president.setTenureCount(turnCount);
		president.setIcon(leaderIcon);
		president.setPfIcon(leaderPfIcon);
		president.setPlayerGuildTag(guildTag);
		president.setPlayerGuildFlag(guildFlag);
		president.setLastPresidentPlayerId(lastPresidentId);
		president.setServerId(serverId);
		
		// 国王信心存储到redis
		LocalRedis.getInstance().updatePresidentDataByKey("president", JSON.toJSONString(president));
		
		// 发公告
		ChatParames.Builder chatParames = ChatParames.newBuilder();
		chatParames.setChatType(Const.ChatType.SPECIAL_BROADCAST);
		chatParames.setKey(Const.NoticeCfgId.PRESIDENT_WAR_ENDED);
		chatParames.addParms(guildTag);
		chatParames.addParms(leaderName);
		ChatService.getInstance().addWorldBroadcastMsg(chatParames.build());

		// 进入和平状态
		enterPace();
		
		// 箭塔进入和平状态
		for (PresidentTower tower : presidentTowers) {
			tower.enterPace();
		}
		
		//设置下次开启时间
		setStartTime(HawkTime.getNextTimeDayOfWeek(getEndTime() + GsConst.WEEK_MILLI_SECONDS, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute()));
		// 通知国王改变
		notifyPresidentPlayerChanged(lastPresidentId, president.getPlayerId(), president.getPlayerGuildId());
		// 时期切换
		notifyPresidentPeriodChanged(PresidentPeriod.WARFARE_VALUE, PresidentPeriod.PEACE_VALUE);
		//发一封临时总统任命邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(leaderId)
				.setMailId(MailId.PRESIDENT_PROVISIONAL_APPOINT)
				.build());
		
		//新国王头像解锁，老国王头像上锁
		if(lastPresidentId == null){ //如果当前国王
			HawkTaskManager.getInstance().postMsg(leader.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.HUANGDI));
		}else{
			if(!lastPresidentId.equals(president.getPlayerId())){
				//新国王解锁新头像，老国王上锁头像
				if (GlobalData.getInstance().isLocalPlayer(lastPresidentId)) {
					Player lastPresident = GlobalData.getInstance().makesurePlayer(lastPresidentId);
					HawkTaskManager.getInstance().postMsg(lastPresident.getXid(), PlayerLockImageMsg.valueOf(LockType.PLAYERSTAT, LockParam.NO_HUANGDI));
				}
				Player curPresident = GlobalData.getInstance().makesurePlayer(president.getPlayerId());
				HawkTaskManager.getInstance().postMsg(curPresident.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.HUANGDI));
			}
		}
		
		// 任务
		if (GlobalData.getInstance().isLocalPlayer(president.getPlayerId())) {
			MissionManager.getInstance().postMsg(leader, new EventControlPresident());
			for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
				ActivityManager.getInstance().postEvent(new OccupyPresidentEvent(playerId));
			}
		}
		
		//产生国王的时候清理一下公告时间.
		this.clearLastNoticeTime();
		
		// 通知国家系统王战完成
		NationService.getInstance().checkAndUpatePresident3(president.getPlayerGuildId());
		
		if (leader.isActiveOnline()) {
			leader.getPush().syncPlayerInfo();
		}
		
		PresidentFightService.logger.info("president is over, the win guild is {}, the TenureCount is {}", president.getPlayerGuildId(), turnCount);
	}

	
	
	
	private void crossPresidentWinOver(long currTime, PresidentConstCfg constCfg) {
		// 直接进入和平期
		warEndNoticeStep = 0;
		PresidentCrossAccumulateInfo accumulateInfo = CrossActivityService.getInstance().getAccumulateInfo();
		CrossPlayerStruct winLeaderInfo = accumulateInfo.getWinner();
		String winServerId = winLeaderInfo.getServerId();
		String winLeaderId = winLeaderInfo.getPlayerId();
		String winGuildId = winLeaderInfo.getGuildID();
		String winGuildName = winLeaderInfo.getGuildName();
		String winGuildTag = winLeaderInfo.getGuildTag();
		int winGuildFlag = winLeaderInfo.getGuildFlag();
		String winLeaderName = winLeaderInfo.getName();
		int winLeaderIcon = winLeaderInfo.getIcon();
		String winLeaderPfIcon = winLeaderInfo.getPfIcon();
		
		//上届国王Id
		String lastPresidentId = null;
		// 上一届没有国王
		if(president == null){
			president = new President();
			president.setTenure(currTime);
		} else {
			// 老国王相关信息
			lastPresidentId = president.getPlayerId();
			if (!winLeaderId.equals(lastPresidentId)) {
				president.setTenure(currTime);
			}
		}
		president.setPlayerId(winLeaderId);
		president.setPlayerName(winLeaderName);
		president.setPlayerGuildId(winGuildId);
		president.setPlayerGuildName(winGuildName);
		president.setTenureCount(turnCount);
		president.setIcon(winLeaderIcon);
		president.setPfIcon(winLeaderPfIcon);
		president.setPlayerGuildTag(winGuildTag);
		president.setPlayerGuildFlag(winGuildFlag);
		president.setLastPresidentPlayerId(lastPresidentId);
		president.setServerId(winServerId);
		
		// 存储国王信息
		LocalRedis.getInstance().updatePresidentDataByKey("president", JSON.toJSONString(president));
		
		// 通知征服方占领成功
		boolean scoreRankWinServer = CrossActivityService.getInstance().isScoreRankWinServer(winServerId);
		if (scoreRankWinServer) {
			CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(HP.code2.NATIONAL_PRESIDENT_NOTIFY_VALUE), winServerId, null);
		}
		
		// 设置王战完成
		RedisProxy.getInstance().setCrossPresidentFightOver();
		
		// 设置胜利区服
		RedisProxy.getInstance().updateCrossWinServer(winServerId);
		
		// 发公告
		ChatParames.Builder chatParames = ChatParames.newBuilder();
		chatParames.setChatType(Const.ChatType.SPECIAL_BROADCAST);
		chatParames.setKey(Const.NoticeCfgId.CROSS_PRESIDENT_WAR_END);
		chatParames.addParms(GuildService.getInstance().getGuildTag(guildId));
		chatParames.addParms(winLeaderName);
		ChatService.getInstance().addWorldBroadcastMsg(chatParames.build());
		
		// 王城进入和平状态
		enterPace();
		
		// 箭塔切换成和平状态
		for (PresidentTower tower : presidentTowers) {
			tower.enterPace();
		}
		
		//设置下次开启时间
		setStartTime(HawkTime.getNextTimeDayOfWeek(getEndTime() + GsConst.WEEK_MILLI_SECONDS, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute()));
		
		// 通知国王改变
		notifyPresidentPlayerChanged(lastPresidentId, president.getPlayerId(), president.getPlayerGuildId());
		
		// 通知航海远征活动
		CrossActivityService.getInstance().presidentWin(winServerId, winLeaderName);
		
		// 时期切换
		notifyPresidentPeriodChanged(PresidentPeriod.WARFARE_VALUE, PresidentPeriod.PEACE_VALUE);
		
		// 跨服战令触发方尖碑任务
		int daySign = HawkTime.getYyyyMMddIntVal();
		String presidentServerId = president.getServerId();
		ObeliskService.getInstance().setServerObeliskMission(ObeliskMissionType.GUIlD_CROSS_PRESIDENT, presidentServerId, String.valueOf(daySign), GsConfig.getInstance().getServerId());
		
		// 清理一下公告时间
		clearLastNoticeTime();
		
		// 通知国家系统王战完成
		NationService.getInstance().checkAndUpatePresident3(president.getPlayerGuildId());
		
		// 同步国王信息
		Player player = GlobalData.getInstance().makesurePlayer(winLeaderId);
		if (player != null && player.isActiveOnline()) {
			player.getPush().syncPlayerInfo();
		}
	}
	
	/**
	 * 战争将要开始系统通知
	 * @param remainTime : 剩余时间（秒）
	 * */
	@SuppressWarnings("deprecation")
	private void presidentWarWillStartNotice(int remainTime) {
		try {
			if(remainTime < 0){
				return;
			}
			//距离战争开始的时间
			int centerX = WorldMapConstProperty.getInstance().getWorldCenterX();
			int centerY = WorldMapConstProperty.getInstance().getWorldCenterY();

			int[] times = PresidentConstCfg.getInstance().getMailBeforeTimeArray();
			if (times != null && times.length > 0) {
				for (int i = times.length - 1; i >= 0; i--) {
					if (remainTime <= times[i] && warStartNoticeStep < i + 1) {
						// 国王战开始系统通知 
						ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
								Const.NoticeCfgId.PRESIDENT_WAR_WILL_START, null, times[i] / 60, centerX + "," + centerY);
						warStartNoticeStep = i + 1;
						PresidentFightService.logger.info("president war will start... left Time : {}", times[i] / 60);
						break;
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 战争将要进入加时系统通知
	 * */
	@SuppressWarnings("deprecation")
	private void presidentWarWillOverTime() {
		try {
			long time = (getWarFareTime() - (HawkTime.getMillisecond() - occupyTime) / 1000);
			int[] timeArray = PresidentConstCfg.getInstance().getMailAfterTimeArray();
			if (timeArray == null || timeArray.length <= 0) {
				return;
			}
			for (int i = timeArray.length - 1; i >= 0; i--) {
				if (time <= timeArray[i] && warEndNoticeStep < i + 1) {
					String guildTag = GuildService.getInstance().getGuildTag(guildId);
					ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.PRESIDENT_WAR_WILL_TO_OVERTIME, null, guildTag,
							timeArray[i] / 60);
					warEndNoticeStep = i + 1;
					PresidentFightService.logger.info("president war will enter overTime... left Time : {}", timeArray[i] / 60);
					break;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 通知国王战时期改变
	 * 
	 * @param lastPeriod
	 * @param currPeriod
	 */
	protected void  notifyPresidentPeriodChanged(int lastPeriod, int currPeriod) {
		try {
			// 切换为和平时期所有驻军回家
			if (currPeriod == PresidentPeriod.PEACE_VALUE) {
				PresidentFightService.logger.info("change period to peace dissolve all marchs");
				WorldMarchService.getInstance().dissolveAllPresidentQuarteredMarchs();
				for (PresidentTower presidentTower : getTowers()) {
					WorldMarchService.getInstance().dissolveAllPresidentTowerQuarteredMarchs(presidentTower.getIndex());
				}
			}
			int centerX = WorldMapConstProperty.getInstance().getWorldCenterX();
			int centerY = WorldMapConstProperty.getInstance().getWorldCenterY();
			long currTime = HawkTime.getMillisecond();
			if (currPeriod == PresidentPeriod.WARFARE_VALUE) {
				// 发送邮件---国王战开启全服邮件
				Object[] content = new Object[] { centerX, centerY };
				SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
		                .setMailId(MailId.PRESIDENT_WAR_START)
		                .addContents(content)
		                .build()
		                ,currTime, currTime + getWarFareTime() * 1000L);
				
				//战争状态下所有的作用号失效
				PresidentOfficier.getInstance().synOfficerEffect();
			} else if (currPeriod == PresidentPeriod.PEACE_VALUE) {
				// 发送邮件---国王结束全服邮件
				String presidentId = getPresidentPlayerId();
				if (!HawkOSOperator.isEmptyString(presidentId)) {					
					String guildTag = this.getPresident().getPlayerGuildTag();
					String kingName = this.getPresident().getPlayerName();
					SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
					        .setMailId(MailId.PRESIDENT_WAR_END)
					        .addSubTitles(guildTag, kingName)
					        .addContents(guildTag, kingName)
					        .build()
					        ,currTime,
                            currTime + getWarFareTime() * 1000L);
				} else {
					SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
			                .setMailId(MailId.PRESIDENT_WAR_END_NO_WIN)
			                .build()
			                ,currTime, currTime + getWarFareTime() * 1000L);
				}
			}

			// 广播国王战状态信息
			broadcastPresidentInfo(null);
			
			//广播箭塔状态信息
			for (PresidentTower presidentTower : presidentTowers) {
				presidentTower.broadcastPresidentTowerInfo(null);
			}
			
			PresidentFightService.logger.info("president period changed: old status : {}, new status : {}, currentGuild : {}", lastPeriod, currPeriod, guildId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取国王ID
	 * @return
	 */
	public String getPresidentPlayerId(){
		if(president != null){
			return president.getPlayerId();
		}
		return null;
	}
	
	/**
	 * 通知国王改变(需要判断两个id是否一致, 连任的情况)
	 * 
	 * @param lastPresidentId
	 * @param currPresidentId
	 */
	protected void notifyPresidentPlayerChanged(String lastPresidentId, String currPresidentId, String guildId) {
		try {
			PresidentFightService.logger.info("president player changed: lastPresidentId：{} , currPresidentId：{}", lastPresidentId, currPresidentId);
			// 广播国王战状态信息
			broadcastPresidentInfo(null);
			// 修改redis存储状态
			LocalRedis.getInstance().clearTaxGuildInfo();
			// 通知国王战相关系统国王变更
			PresidentOfficier.getInstance().onPresidentChanged(lastPresidentId, currPresidentId);
			PresidentGift.getInstance().onPresidentChanged(lastPresidentId, currPresidentId);
			//只有跨服王战
			if (CrossActivityService.getInstance().isOpen()) {
				PresidentRecord.getInstance().onPresidentChanged(lastPresidentId, currPresidentId, guildId);
			}			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 重置宣言相关信息.
	 */
	public void resetManifestoInfo() {
		this.setManifesto("");
		this.setUpdateManifestoTimes(0);
	}
	
	/**
	 * 广播国王战信息
	 */
	public void broadcastPresidentInfo(Player player) {
		try {
			PresidentInfo.Builder infoBuilder = genPresidentInfoBuilder();
			PresidentInfoSync.Builder builder = PresidentInfoSync.newBuilder();
			builder.setServerName(GsConfig.getInstance().getServerId());
			builder.setInfo(infoBuilder);

			if (player != null) {
				player.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_INFO_SYNC, builder));
			} else {
				for (Player sendPlayer : GlobalData.getInstance().getOnlinePlayers()) {
					sendPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.PRESIDENT_INFO_SYNC, builder));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 广播国王战信息
	 */
	public void broadcastAllPresidentTowerInfo(Player player) {
		try {
			for (PresidentTower presidentTower : presidentTowers) {
				presidentTower.broadcastPresidentTowerInfo(player);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 生成国王战信息的协议对象
	 * 
	 * @return
	 */
	public PresidentInfo.Builder genPresidentInfoBuilder() {
		try {
			PresidentInfo.Builder infoBuilder = PresidentInfo.newBuilder();
			infoBuilder.setPeriodType(getStatus());
			infoBuilder.setTurnCount(getTurnCount());
			infoBuilder.setPeriodEndTime(getCurrentPeriodEndTime());
			infoBuilder.setWarStartTime(getStartTime());
			// 国家信息
			if (!HawkOSOperator.isEmptyString(getCountryName())) {
				infoBuilder.setCountryName(getCountryName());
			}
			infoBuilder.setCountryNameModifyTimes(getCountryModifyTimes());
			infoBuilder.setOccupyTime(getOccupyTime());

			//  国王信息
			if (getPresident() != null) {
				infoBuilder.setPresidentId(getPresident().getPlayerId());
				infoBuilder.setPresidentName(getPresident().getPlayerName());
				infoBuilder.setPresidentIcon(getPresident().getIcon());
				infoBuilder.setTenureTime(getPresident().getTenure());
				if (!HawkOSOperator.isEmptyString(getPresident().getPfIcon())) {
					infoBuilder.setPresidentPfIcon(getPresident().getPfIcon());
				}
				
				// 联盟
				infoBuilder.setPresidentGuildId(getPresident().getPlayerGuildId());
				infoBuilder.setPresidentGuildName(getPresident().getPlayerGuildName());
				infoBuilder.setPresidentGuildTag(getPresident().getPlayerGuildTag());
				infoBuilder.setPresidentGuildFlagId(getPresident().getPlayerGuildFlag());
				infoBuilder.setPresidentServerId(getPresident().getServerIdByAssemble());
				infoBuilder.setLeaderOfficerId(GameUtil.getOfficerId(getPresident().getPlayerId()));
			}
			

			String guildId = getGuildId();
			if (!HawkOSOperator.isEmptyString(guildId)) {
				
				try {
					if (CrossActivityService.getInstance().isOpen()) {
						String presidentLeaderMarchId = WorldMarchService.getInstance().getPresidentLeaderMarch();
						IWorldMarch presidentLeaderMarch = WorldMarchService.getInstance().getMarch(presidentLeaderMarchId);
						if (presidentLeaderMarch != null && !presidentLeaderMarch.getPlayer().getGuildId().equals(guildId)) {
							setGuildId(presidentLeaderMarch.getPlayer().getGuildId());
							guildId = getGuildId();
						}
						
						PresidentCrossAccumulateInfo accumulateInfo = CrossActivityService.getInstance().getAccumulateInfo();
						infoBuilder.setRate(0);
						infoBuilder.setStarOccupyTime(accumulateInfo.getStartOccupyTime());
						infoBuilder.setSpeed(0);
						infoBuilder.setIsAtk(accumulateInfo.isAtker());
						
						
						
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				infoBuilder.setLeaderGuildId(guildId);
				infoBuilder.setLeaderGuildTag(GuildService.getInstance().getGuildTag(guildId));
				infoBuilder.setLeaderGuildName(GuildService.getInstance().getGuildName(guildId));
				infoBuilder.setLeaderGuildFlag(GuildService.getInstance().getGuildFlag(guildId));
				
				Player leader = WorldMarchService.getInstance().getPresidentLeader();
				
				// 箭塔没有leader的时候， 取盟主的信息
				if (leader == null) {
					String guildLeaderId = GuildService.getInstance().getGuildLeaderId(guildId);
					if (HawkOSOperator.isEmptyString(guildLeaderId)) {
						CrossPlayerStruct leaderInfo = RedisProxy.getInstance().getCrossGuildLeaderInfo(guildId);
						infoBuilder.setLeaderId(leaderInfo.getPlayerId());
						infoBuilder.setLeaderName(leaderInfo.getName());
						infoBuilder.setLeaderIcon(leaderInfo.getIcon());
						infoBuilder.setLeaderPfIcon(leaderInfo.getPfIcon());
						infoBuilder.setLeaderServerId(GlobalData.getInstance().getMainServerId(leaderInfo.getServerId()));
					} else {						
						leader = GlobalData.getInstance().makesurePlayer(guildLeaderId);
						infoBuilder.setLeaderId(leader.getId());
						infoBuilder.setLeaderName(leader.getName());
						infoBuilder.setLeaderIcon(leader.getIcon());
						infoBuilder.setLeaderPfIcon(leader.getPfIcon());
						infoBuilder.setLeaderServerId(GsConfig.getInstance().getServerId());
					}
				} else {
					infoBuilder.setLeaderId(leader.getId());
					infoBuilder.setLeaderName(leader.getName());
					infoBuilder.setLeaderIcon(leader.getIcon());
					infoBuilder.setLeaderPfIcon(leader.getPfIcon());
					infoBuilder.setLeaderServerId(leader.getMainServerId());
				}
			}
			
			return infoBuilder;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 获取当前阶段的结束时间
	 * @return
	 */
	public long getCurrentPeriodEndTime(){
		if(getStatus() == PresidentPeriod.WARFARE_VALUE){//战争状态
			if(getGuildId() == null){
				return getStartTime() + getWarFareTime() * 1000L;
			} else {
				return getOccupyTime() + (getOccupationTime() * 1000L);
			}
		} else if(getStatus() == PresidentPeriod.OVERTIME_VALUE){//加时状态
			return getOccupyTime() + (getOccupationTime() * 1000L);
		}
		return getStartTime();
	}
	

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
		LocalRedis.getInstance().updatePresidentDataByKey("status", status);
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
		LocalRedis.getInstance().updatePresidentDataByKey("startTime", startTime);
		int centerX = WorldMapConstProperty.getInstance().getWorldCenterX();
		int centerY = WorldMapConstProperty.getInstance().getWorldCenterY();
		addSchedule(startTime, centerX, centerY);
		PresidentFightService.logger.info("president set StartTime, status : {} , next startTime : {}", getStatus(), new Date(startTime));
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
		LocalRedis.getInstance().updatePresidentDataByKey("endTime", endTime);
	}

	public int getTurnCount() {
		return turnCount;
	}

	public void setTurnCount(int turnCount) {
		this.turnCount = turnCount;
		LocalRedis.getInstance().updatePresidentDataByKey("turnCount", turnCount);
	}

	public long getOccupyTime() {
		return occupyTime;
	}

	public void setOccupyTime(long occupyTime) {
		this.occupyTime = occupyTime;
		LocalRedis.getInstance().updatePresidentDataByKey("occupyTime", occupyTime);
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
		LocalRedis.getInstance().updatePresidentDataByKey("guildId", guildId);
	}

	public String getServerId() {
		return GuildService.getInstance().getGuildServerId(guildId);
	}
	
	public String getOverTimeGuildId() {
		return overTimeGuildId;
	}

	public void setOverTimeGuildId(String overTimeGuildId) {
		this.overTimeGuildId = overTimeGuildId;
		LocalRedis.getInstance().updatePresidentDataByKey("overTimeGuildId", overTimeGuildId);
	}

	public int getCountryModifyTimes() {
		return countryModifyTimes;
	}

	public void setCountryModifyTimes(int countryModifyTimes) {
		this.countryModifyTimes = countryModifyTimes;
		LocalRedis.getInstance().updatePresidentDataByKey("countryModifyTimes", countryModifyTimes);
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
		LocalRedis.getInstance().updatePresidentDataByKey("countryName", countryName);
	}

	public long getLastTickTime() {
		return lastTickTime;
	}

	public void setLastTickTime(long lastTickTime) {
		this.lastTickTime = lastTickTime;
		LocalRedis.getInstance().updatePresidentDataByKey("lastTickTime", lastTickTime);
	}

	public President getPresident() {
		return president;
	}
	
	public PresidentTower getTower(int index){
		for (PresidentTower presidentTower : presidentTowers) {
			if (presidentTower.getIndex() == index) {
				return presidentTower;
			}
		}
		return null;
	}
	
	public List<PresidentTower> getTowers() {
		return presidentTowers;
	}
	
	/**
	 * 遣返行军
	 */
	public boolean repatriateMarch(Player player, String targetPlayerId, HawkProtocol protocol) {
		// 没有联盟或者不是本联盟占领
		if (!player.hasGuild()) {
			if (CrossActivityService.getInstance().isOpen()) {
				player.sendError(protocol.getType(), Status.CrossServerError.CROSS_MARCH_REPATRIATE_ERROR, 0);
			} else {
				player.responseSuccess(protocol.getType());
			}
			return false;
		}
		boolean sameGuild =player.getGuildId().equals(getGuildId());
		if (!sameGuild) {
			if (CrossActivityService.getInstance().isOpen()) {
				player.sendError(protocol.getType(), Status.CrossServerError.CROSS_MARCH_REPATRIATE_ERROR, 0);
			} else {
				player.responseSuccess(protocol.getType());
			}
			return false;
		}
		
		// 队长
		Player leader = WorldMarchService.getInstance().getPresidentLeader();
		boolean isLeader = player.getId().equals(leader.getId());
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		if (!guildAuthority && !isLeader) {
			if (CrossActivityService.getInstance().isOpen()) {
				player.sendError(protocol.getType(), Status.CrossServerError.CROSS_MARCH_REPATRIATE_ERROR, 0);
			} else {
				player.responseSuccess(protocol.getType());
			}
			return false;
		}

		// 跨服状态下判断是否和战时司令同盟
//		if (CrossActivityService.getInstance().isOpen()) {
//			String crossFightPresident = RedisProxy.getInstance().getCrossFightPresident(player.getMainServerId());
//			CrossPlayerStruct fightPresidentInfo = RedisProxy.getInstance().getFightPresidentInfo(crossFightPresident);
//			if (!player.getGuildId().equals(fightPresidentInfo.getGuildID())) {
//				player.sendError(protocol.getType(), Status.CrossServerError.CROSS_MARCH_REPATRIATE_ERROR, 0);
//				return false;
//			}
//		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REPATRIATE_MARCH) {
			@Override
			public boolean onInvoke() {
				List<IWorldMarch> marchs = WorldMarchService.getInstance().getPresidentQuarteredMarchs();
				for (IWorldMarch iWorldMarch : marchs) {
					if (iWorldMarch.isReturnBackMarch()) {
						continue;
					}
					if (!iWorldMarch.getPlayerId().equals(targetPlayerId)) {
						continue;
					}
					WorldMarchService.logger.info("marchRepatriate, playerId:{}, tarPlayerId:{}, marchId:{}", player.getId(), targetPlayerId, iWorldMarch.getMarchId());
					WorldMarchService.getInstance().onPlayerNoneAction(iWorldMarch, HawkApp.getInstance().getCurrentTime());
				}
				PresidentFightService.getInstance().sendPresidentQuarterInfo(player);
				broadcastAllPresidentTowerInfo(player);
				return true;
			}
		});

		return true;
	}
	
	/**
	 * 任命队长
	 * @param player
	 * @param targetPlayerId
	 * @return
	 */
	public boolean cheangeQuarterLeader(Player player, String targetPlayerId, HawkProtocol protocol) {
		// 没有联盟或者不是本联盟占领
		if (!player.hasGuild()) {
			if (CrossActivityService.getInstance().isOpen()) {
				player.sendError(protocol.getType(), Status.CrossServerError.CROSS_MARCH_CHANGE_LEADER_ERROR, 0);
			} else {
				player.responseSuccess(protocol.getType());
			}
			return false;
		}
		
		// 不是同阵营
		boolean sameGuild =player.getGuildId().equals(getGuildId());
		if (!sameGuild) {
			if (CrossActivityService.getInstance().isOpen()) {
				player.sendError(protocol.getType(), Status.CrossServerError.CROSS_MARCH_CHANGE_LEADER_ERROR, 0);
			} else {
				player.responseSuccess(protocol.getType());
			}
			return false;
		}
		
		// 队长
		Player leader = WorldMarchService.getInstance().getPresidentLeader();
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			if (CrossActivityService.getInstance().isOpen()) {
				player.sendError(protocol.getType(), Status.CrossServerError.CROSS_MARCH_CHANGE_LEADER_ERROR, 0);
			} else {
				player.responseSuccess(protocol.getType());
			}
			return false;
		}
		
		// 跨服状态下判断是否和战时司令同盟
//		if (CrossActivityService.getInstance().isOpen()) {
//			String crossFightPresident = RedisProxy.getInstance().getCrossFightPresident(player.getMainServerId());
//			CrossPlayerStruct fightPresidentInfo = RedisProxy.getInstance().getFightPresidentInfo(crossFightPresident);
//			if (!player.getGuildId().equals(fightPresidentInfo.getGuildID())) {
//				player.sendError(protocol.getType(), Status.CrossServerError.CROSS_MARCH_CHANGE_LEADER_ERROR, 0);
//				return false;
//			}
//		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHANGE_QUARTER_LEADER) {
			@Override
			public boolean onInvoke() {
				WorldMarchService.getInstance().changePresidentMarchLeader(targetPlayerId);
				PresidentFightService.getInstance().sendPresidentQuarterInfo(player);
				broadcastPresidentInfo(null);
				return true;
			}
		});
		
		return true;
	}
	
	/**
	 * 自动设置国王
	 * 
	 */
	private void onAutoSetPresident() {
		String presidentPlayerId = this.getPresidentPlayerId();
		PresidentFightService.logger.error("autoSetPresidente presidentId:{}", presidentPlayerId);
		try {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
	                .setPlayerId(presidentPlayerId)
	                .setMailId(MailId.PRESIDENT_OFFICAL_APPOINT_APPOINT)
	                .build());
			Player targetPlayer = GlobalData.getInstance().makesurePlayer(presidentPlayerId);
			if (targetPlayer != null) {
				PresidentRecord.getInstance().onPresidentChanged(this.getPresident().getLastPresidentPlayerId(), presidentPlayerId, targetPlayer.getGuildId());
			} else {
				PresidentFightService.logger.error("playerId:{} not found ", presidentPlayerId);
			}			
			OfficerEntity officerEntity = PresidentOfficier.getInstance().getEntityById(OfficerType.OFFICER_01_VALUE);
			
			if (officerEntity != null) {
				officerEntity.setEndTime(System.currentTimeMillis());
			}			
		} catch(Exception e) {
			PresidentFightService.logger.error("autoSetPresidenteRR");
		}		
	}
	
	public void clearPresident() {
		President president = getPresident();
		if (president == null) {
			return;
		}
		president.setPlayerId("");
		president.setPlayerName("");
		president.setPlayerGuildId("");
		president.setPlayerGuildName("");		
		president.setIcon(0);
		president.setPfIcon("");
		president.setPlayerGuildTag("");
		
		LocalRedis.getInstance().updatePresidentDataByKey("president", JSON.toJSONString(president));
		
		//清空国王的信息.
		this.crossKingInfo = null;
	}
	/**
	 * 国王更改
	 * @param targetPlayer
	 */
	public void chanagePresident(Player targetPlayer) {
		President president = getPresident();
		president.setPlayerId(targetPlayer.getId());
		president.setPlayerName(targetPlayer.getName());
		president.setPlayerGuildId(targetPlayer.getGuildId());
		president.setPlayerGuildName(targetPlayer.getGuildName());
		president.setTenureCount(getTurnCount());
		president.setIcon(targetPlayer.getIcon());
		president.setPfIcon(targetPlayer.getPfIcon());
		president.setPlayerGuildTag(targetPlayer.getGuildTag());
		president.setPlayerGuildFlag(targetPlayer.getGuildFlag());
		
		LocalRedis.getInstance().updatePresidentDataByKey("president", JSON.toJSONString(president)); 
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getWarStartNoticeStep() {
		return warStartNoticeStep;
	}

	public void setWarStartNoticeStep(int warStartNoticeStep) {
		this.warStartNoticeStep = warStartNoticeStep;
	}

	public int getWarEndNoticeStep() {
		return warEndNoticeStep;
	}

	public void setWarEndNoticeStep(int warEndNoticeStep) {
		this.warEndNoticeStep = warEndNoticeStep;
	}

	public String getManifesto() {
		return manifesto;
	}

	public void setManifesto(String manifesto) {
		this.manifesto = manifesto;
		LocalRedis.getInstance().updatePresidentDataByKey("manifesto", manifesto);
	}

	public int getUpdateManifestoTimes() {
		return updateManifestoTimes;
	}

	public void setUpdateManifestoTimes(int updateManifestoTimes) {
		this.updateManifestoTimes = updateManifestoTimes;
		LocalRedis.getInstance().updatePresidentDataByKey("updateManifestoTimes", updateManifestoTimes);
	}

	public CrossPlayerStruct getCrossKingInfo() {
		return crossKingInfo;
	}
	
	/**
	 * 获取战争周期时间
	 */
	public int getWarFareTime() {
		int warFareTime = PresidentConstCfg.getInstance().getWarfareTime();
		
		// 是跨服开启的王战，则走跨服的时间
		boolean isCrossActivityOpen = CrossActivityService.getInstance().isOpen();
		String presidentOpenServer = CrossActivityService.getInstance().getPresidentOpenServer();
		if (isCrossActivityOpen && !HawkOSOperator.isEmptyString(presidentOpenServer)
				&& presidentOpenServer.equals(GsConfig.getInstance().getServerId())) {
			warFareTime = CrossConstCfg.getInstance().getPresidentWarFareTime();
		}
		return warFareTime;
	}
	
	/**
	 * 获取占领时间
	 */
	public int getOccupationTime() {
		int occupyTime = PresidentConstCfg.getInstance().getOccupationTime();
		
		// 是跨服开启的王战，则走跨服的时间
		boolean isCrossActivityOpen = CrossActivityService.getInstance().isOpen();
		String presidentOpenServer = CrossActivityService.getInstance().getPresidentOpenServer();
		if (isCrossActivityOpen && !HawkOSOperator.isEmptyString(presidentOpenServer)
				&& presidentOpenServer.equals(GsConfig.getInstance().getServerId())) {
			occupyTime = CrossActivityService.getInstance().getNeedPresidentOccupyTime();
		}
		return occupyTime;
	}

	public void setPresident(President president) {
		this.president = president;
	}
	
	/**
	 * 判断是否已经打过一次国王战
	 * @return
	 */
	public boolean isOpenedPresident(){
		return this.status == PresidentPeriod.PEACE_VALUE || this.turnCount > 1 ;
	}
	
	/**
	 * 是否是跨服国王.
	 * @return
	 */
	public boolean isCrossServerKing(String playerId) {
		return this.crossServerKing.containsKey(playerId);
	}

	public int getLastNoticeTime() {
		return lastNoticeTime;
	}

	public void setLastNoticeTime(int lastNoticeTime) {
		this.lastNoticeTime = lastNoticeTime;
	}
	
	public void clearLastNoticeTime() {
		this.lastNoticeTime = 0;
	}
	
	
	/**
	 * 进入和平状态
	 */
	public void enterPace() {
		setGuildId(null);
		setOverTimeGuildId(null);
		setOccupyTime(0);
		setStatus(PresidentPeriod.PEACE_VALUE);
		setEndTime(HawkTime.getMillisecond());
	}
	
}
