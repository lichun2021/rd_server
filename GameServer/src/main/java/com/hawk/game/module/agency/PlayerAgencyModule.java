package com.hawk.game.module.agency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.hawk.activity.event.impl.AgencyRewardEvent;
import com.hawk.game.config.*;
import com.hawk.game.entity.*;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.AgencyFinishEvent;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerMarchModule;
import com.hawk.game.msg.AgencyFinishMsg;
import com.hawk.game.msg.BuildingLevelUpMsg;
import com.hawk.game.msg.MissionMsg;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.msg.WorldMoveCityMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Agency.AgencyBoxReq;
import com.hawk.game.protocol.Agency.AgencyEvent;
import com.hawk.game.protocol.Agency.AgencyEventCommon;
import com.hawk.game.protocol.Agency.AgencyEventState;
import com.hawk.game.protocol.Agency.AgencyEventType;
import com.hawk.game.protocol.Agency.AgencyPageInfo;
import com.hawk.game.protocol.Agency.AgencySearchResp;
import com.hawk.game.protocol.Agency.GenAgencyEventReq;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SuperSoldier.SupersoldierTaskType;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchResp;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventAgency;
import com.hawk.game.service.mssion.event.EventAgencyMissionFinishInit;
import com.hawk.game.service.mssion.event.EventMonsterAttack;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ModuleType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;

/**
 * 情报交易中心
 * 
 * @author Golden
 *
 */
public class PlayerAgencyModule extends PlayerModule {

	/**
	 * 日志
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");
	
	private long tickTime;
	
	
	public PlayerAgencyModule(Player player) {
		super(player);
	}
	
	@Override
	protected boolean onPlayerLogin() {
		if(player.isInDungeonMap()){
			return true;
		}
		// 推界面信息
		doCheck();
		pushPageInfo();
		logger.info("agency onPlayerLogin playerId:{},playerName:{}", player.getId(),player.getName());
		return true;
	}

	@Override
	public boolean onTick() {
		if(player.isInDungeonMap()){
			return true;
		}
		long curTime = HawkApp.getInstance().getCurrentTime();
		if(this.tickTime == 0){
			this.tickTime = curTime;
			return true;
		}
		if(curTime - this.tickTime < 3 * 1000){
			return true;
		}
		this.tickTime = curTime;
		boolean needPush = doCheck();
		if (needPush) {
			pushPageInfo();
			logger.info("agency onTick playerId:{},playerName:{}", player.getId(),player.getName());
		}
		return true;
	}
    public void addMission(){
        AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		//当前等级
        int currLevel = agencyEntity.getCurrLevel();
		if (currLevel == 0){
			return;
		}
		//当前等级任务数
		int count = getAgencyLevelCfg(currLevel).getEventCount();
		int eff640 = player.getEffect().getEffVal(EffType.LIFE_TIME_CARD_640);
		count += eff640;

		int nowCount = 0;
		for (AgencyEventEntity agency : agencyEntity.getAgencyEvents().values()) {
			// 特殊事件，道具事件，升级事件 不占用刷新数量
			AgencyEventCfg eventCfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agency.getEventId());
			if (eventCfg.getSpecialEvent() == 1 || eventCfg.getItemEvent() == 1) {
				continue;
			}
			nowCount ++;
		}
		//补充任务
		List<Integer> initEventsList = new ArrayList<>();
		int addCount = count - nowCount;
        for (int i = 0; i < addCount; i++) {
            initEventsList.add(randEvent(currLevel));
        }

        Map<Integer, List<Integer>> posMap = this.getRandPos(currLevel, initEventsList);
        for (int i = 0; i < addCount; i++) {
            Integer eventId = initEventsList.get(i);
            int pos = this.getEventPos(posMap, eventId);
            int[] posArr = GameUtil.splitXAndY(pos);
            AgencyEventEntity event = new AgencyEventEntity(eventId, posArr[0], posArr[1]);
            agencyEntity.addAgencyEvent(event);
        }
    }
	/**
	 * 推界面信息
	 */
	public void pushPageInfo() {
		// 建筑未解锁
		BuildingBaseEntity agencyBuild = player.getData().getBuildingEntityByType(BuildingType.RADAR);
		if (agencyBuild == null) {
			return;
		}
		// 没有达到开启等级
		int cityLevel = player.getCityLevel();
		int unlockLevel =  AgencyConstCfg.getInstance().getAgencyUnlockLevel();
		if(cityLevel < unlockLevel){
			return;
		}
		// 获取玩家实体
		AgencyEntity entity = player.getData().getAgencyEntity();
		if(entity.getCurrLevel() <= 0){
			return;
		}
		AgencyPageInfo.Builder builder = AgencyPageInfo.newBuilder();
		builder.setExp(entity.getExp());
		builder.setLevel(entity.getCurrLevel());
		builder.setNextRefreshTime(entity.getNextRefreshTime());
		builder.setHasKilledAgency(entity.getHasKilled() > 0);
		builder.setKillCount(entity.getKillCount());
		//战区控制数量
		int ControlSuperWeaponCount = SuperWeaponService.getInstance().getGuildControlSuperWeapon(player.getGuildId()).size();
		builder.setControlSuperWeapon(ControlSuperWeaponCount);
		// 所有正在进行中的任务的uuid的集合
		Map<String,Integer> marchAgency = new HashMap<>();
		List<IWorldMarch> playerMarch = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.AGENCY_MARCH_COASTER_VALUE);
		playerMarch.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.AGENCY_MARCH_RESCUR_VALUE));
		playerMarch.addAll(WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.AGENCY_MARCH_MONSTER_VALUE));
		for (IWorldMarch march : playerMarch) {
			if (march.isReturnBackMarch()) {
				continue;
			}
			marchAgency.put(march.getMarchEntity().getTargetId(), march.getTerminalId());
		}
		Map<String, AgencyEventEntity> agencyEvents = entity.getAgencyEvents();
		for (AgencyEventEntity event : agencyEvents.values()) {
			AgencyEvent.Builder eventBuilder = AgencyEvent.newBuilder();
			eventBuilder.setUuid(event.getUuid());
			eventBuilder.setEventId(event.getEventId());
			eventBuilder.setEndTime(event.getEventEndTime());
			eventBuilder.setState(AgencyEventState.valueOf(event.getEventState()));
			eventBuilder.setDoing(eventDoing(marchAgency,event));
			eventBuilder.setIsItemEvent(event.getIsItemEvent() == 1);
			eventBuilder.setIsSpecialEvent(event.getIsSpecialEvent() == 1);
			eventBuilder.setPosX(event.getPosX());
			eventBuilder.setPosY(event.getPosY());
			builder.addEvents(eventBuilder);
		}
		for (int boxId : entity.getBoxSet()) {
			builder.addBoxId(boxId);
		}
		builder.setPoolCount(entity.getAgencyEventsPool().size());
		builder.setBoxLevel(entity.getBoxExtLevel());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.AGENCY_PAGE_INFO_RESP, builder));
		logger.info("agency pushPageInfo,playerId:{}, info:{}",player.getId(),entity.toString());
	}
	
	
	private boolean eventDoing(Map<String,Integer> marchAgency,AgencyEventEntity event){
		if(marchAgency.containsKey(event.getUuid())){
			int pointId = marchAgency.get(event.getUuid());
			if(pointId == event.getPointId()){
				return true;
			}
		}
		return false;
	}
	/**
	 * 做检测
	 * @return
	 */
	private boolean doCheck() {
		// 建筑未解锁
		BuildingBaseEntity agencyBuild = player.getData().getBuildingEntityByType(BuildingType.RADAR);
		if (agencyBuild == null) {
			return false;
		}
		int cityLevel = player.getCityLevel();
		int unlockLevel =  AgencyConstCfg.getInstance().getAgencyUnlockLevel();
		if(cityLevel < unlockLevel){
			return false;
		}
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		// 初始化完成
		if (agencyEntity.getCurrLevel() <= 0) {
			initData();
			return true;
		}
		//检查事件过期
		boolean needPushCheck1 = this.checkEventTimeOut(agencyEntity);
		//检查事件池里面事件过期
		boolean needPushCheck2 = this.checkEventPoolTimeOut(agencyEntity);
		//检查事件刷新
		boolean needPushCheck3 = this.checkEventRefresh(agencyEntity);
		//检查特殊事件
		boolean needPushCheck4 = this.refreshSpecialEvent(agencyEntity);
		//检查补齐
		boolean needPushCheck5 = this.checkEventPoolOut(agencyEntity);
		//检查事件位置
		boolean needPushCheck6 = this.checkEventPos(agencyEntity);
		boolean needPush = needPushCheck1 || needPushCheck2 || needPushCheck3 || 
				needPushCheck4 || needPushCheck5 || needPushCheck6;
		return needPush;
	}
	
	
	private boolean checkEventTimeOut(AgencyEntity agencyEntity){
		// 检测界面上的任务过期
		boolean needPush = false;
		List<String> removeList = new ArrayList<>();
		for (AgencyEventEntity agencyEvent : agencyEntity.getAgencyEvents().values()) {
			AgencyEventCfg eventCfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agencyEvent.getEventId());
			long agencyEndTime = agencyEvent.getEventEndTime();
			if (eventCfg == null || HawkTime.getMillisecond() > agencyEndTime) {
				removeList.add(agencyEvent.getUuid());
				needPush = true;
			}
		}
		for (String removeUuid : removeList) {
			agencyEntity.removeAgencyEvent(removeUuid);
		}
		if(needPush){
			agencyEntity.notifyUpdate();
		}
		return needPush;
	}
	
	
	private boolean checkEventPoolTimeOut(AgencyEntity agencyEntity){
		// 检测池子里的任务过期
		List<AgencyEventEntity> dels = new ArrayList<>();
		for(AgencyEventEntity event : agencyEntity.getAgencyEventsPool()) {
			AgencyEventCfg eventCfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, event.getEventId());
			long agencyEndTime = event.getEventEndTime();
			if (eventCfg == null || HawkTime.getMillisecond() > agencyEndTime) {
				dels.add(event);
			}
		}
		boolean needPush = false;
		if(dels.size() > 0){
			needPush = true;
			for(AgencyEventEntity event : dels){
				agencyEntity.getAgencyEventsPool().remove(event);
			}
		}
		if(needPush){
			agencyEntity.notifyUpdate();
		}
		return needPush;
	}
	
	
	private boolean checkEventRefresh(AgencyEntity agencyEntity){
		// 刷新事件
		boolean needPush = false;
		long refreshTime = agencyEntity.getNextRefreshTime();
		if (refreshTime > 0L && refreshTime < HawkTime.getMillisecond()) {
			// 设置下次刷新事件
			long refreshCd = AgencyConstCfg.getInstance().getRefreshCd();
			long nextRefreshTime = HawkTime.getMillisecond() + (refreshCd - (HawkTime.getMillisecond() - refreshTime) % refreshCd);
			agencyEntity.setNextRefreshTime(nextRefreshTime);
			// 刷新事件
			long agecnyStartTime = nextRefreshTime - refreshCd;
			refreshEventsAuto(agencyEntity.getCurrLevel(), agecnyStartTime);
			//重置特殊事件刷新个数
			int yearDay = HawkTime.getYearDay();
			int finisSpecialDay = agencyEntity.getFinishSpecialDay();
			if(yearDay != finisSpecialDay){
				agencyEntity.setFinishSpecialCount(0);
				agencyEntity.setFinishSpecialDay(yearDay);
			}
			needPush = true;
		}
		//如果数据已经初始化过，但是特殊事件的天数为0,此判定为后面新添加
		if (refreshTime > 0L && agencyEntity.getFinishSpecialDay() <= 0){
			int yearDay = HawkTime.getYearDay();
			agencyEntity.setFinishSpecialCount(0);
			agencyEntity.setFinishSpecialDay(yearDay);
		}
		if(needPush){
			agencyEntity.notifyUpdate();
		}
		return needPush;
	}
	

	
	
	private boolean checkEventPoolOut(AgencyEntity agencyEntity){
		//补齐应为过期而删除的事件
		boolean needPush = false;
		int count = 0;
		for (AgencyEventEntity agency : agencyEntity.getAgencyEvents().values()) {
			// 特殊事件，道具事件，升级事件 不占用刷新数量
			AgencyEventCfg eventCfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agency.getEventId());
			if (eventCfg.getSpecialEvent() == 1 || eventCfg.getItemEvent() == 1) {
				continue;
			}
			count ++;
		}
		AgencyLevelCfg agencyLevelCfg = getAgencyLevelCfg(agencyEntity.getCurrLevel());
		int addCount = agencyLevelCfg.getEventCount() - count;
		int poolSize = agencyEntity.getAgencyEventsPool().size();
		if(addCount > 0 && poolSize > 0){
			for(int i=0;i<addCount;i++){
				// 池子里有的话,刷出新的事件
				int size = agencyEntity.getAgencyEventsPool().size();
				if(size > 0){
					needPush = true;
					AgencyEventEntity event = agencyEntity.getAgencyEventsPool().remove(size -1);
					agencyEntity.addAgencyEvent(event);
				}
			}
		}
		if(needPush){
			agencyEntity.notifyUpdate();
		}
		return needPush;
	}
	
	
	private boolean checkEventPos(AgencyEntity agencyEntity){
		// 判断玩家位置改变
		boolean needPush = false;
		int playerPos = WorldPlayerService.getInstance().getPlayerPos(player.getId());
		if (playerPos != 0 && playerPos != agencyEntity.getPlayerPos()) {
			agencyEntity.setPlayerPos(playerPos);
			List<Integer> elist = new ArrayList<>();
			for (AgencyEventEntity agencyEvent : agencyEntity.getAgencyEvents().values()) {
				elist.add(agencyEvent.getEventId());
			}
			for (AgencyEventEntity agencyEvent : agencyEntity.getAgencyEventsPool()) {
				elist.add(agencyEvent.getEventId());
			}
			//随机点列表
			Map<Integer,List<Integer>> posList = this.getRandPos(agencyEntity.getCurrLevel(), elist);
			for (AgencyEventEntity agencyEvent : agencyEntity.getAgencyEvents().values()) {
				int pos = this.getEventPos(posList, agencyEvent.getEventId());
				int[] posArr = GameUtil.splitXAndY(pos);
				agencyEvent.setPosX(posArr[0]);
				agencyEvent.setPosY(posArr[1]);
			}
			for (AgencyEventEntity agencyEvent : agencyEntity.getAgencyEventsPool()) {
				int pos = this.getEventPos(posList, agencyEvent.getEventId());
				int[] posArr = GameUtil.splitXAndY(pos);
				agencyEvent.setPosX(posArr[0]);
				agencyEvent.setPosY(posArr[1]);
			}
			needPush = true;
		}
		// 检测错误的点
		Map<String,Integer> errPoint = new HashMap<>();
		for (AgencyEventEntity agency : agencyEntity.getAgencyEvents().values()) {
			Point freePoint = WorldPointService.getInstance().getAreaPoint(agency.getPosX(), agency.getPosY(), true);
			if (freePoint != null) {
				continue;
			}
			errPoint.put(agency.getUuid(),agency.getEventId());
		}
		if(errPoint.size() > 0){
			needPush = true;
			List<Integer> events = new ArrayList<>(); 
			events.addAll(errPoint.values());
			//随机点列表
			Map<Integer,List<Integer>> posMap = this.getRandPos(agencyEntity.getCurrLevel(), events);
			for(String uuid : errPoint.keySet()){
				AgencyEventEntity event = agencyEntity.getAgencyEvent(uuid);
				if(event == null){
					continue;
				}
				int pos = this.getEventPos(posMap, event.getEventId());
				int[] posArr = GameUtil.splitXAndY(pos);
				event.setPosX(posArr[0]);
				event.setPosY(posArr[1]);
			}
		}
		if(needPush){
			agencyEntity.notifyUpdate();
		}
		return needPush;
	}
	
	
	
	/**
	 * 请求界面信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.AGENCY_PAGE_INFO_REQ_VALUE)
	protected void getPageInfo(HawkProtocol protocol) {
		// 建筑未解锁
		BuildingBaseEntity agencyBuild = player.getData().getBuildingEntityByType(BuildingType.RADAR);
		if (agencyBuild == null) {
			return;
		}
		int cityLevel = player.getCityLevel();
		int unlockLevel =  AgencyConstCfg.getInstance().getAgencyUnlockLevel();
		if(cityLevel < unlockLevel){
			return;
		}
		doCheck();
		pushPageInfo();
		logger.info("agency getPageInfo playerId:{},playerName:{}", player.getId(),player.getName());
	}
	
	/**
	 * 领取事件奖励
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.AGENCY_GET_EVENT_AWARD_VALUE)
	protected void getEventAward(HawkProtocol protocol) {
		AgencyEventCommon req = protocol.parseProtocol(AgencyEventCommon.getDefaultInstance());
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		AgencyEventEntity agencyEvent = agencyEntity.getAgencyEvent(req.getUuid());
		// 情报事件不存在
		if (agencyEvent == null) {
			sendError(protocol.getType(), Status.AgencyError.AGENCY_EVENT_NOT_EXIT_VALUE);
			return;
		}
		// 未完成
		if (agencyEvent.getEventState() != AgencyEventState.AGENCY_FINISHED_VALUE) {
			sendError(protocol.getType(), Status.AgencyError.AGENCY_EVENT_NOT_FINISH_VALUE);
			return;
		}
		// 发奖励
		AwardItems award = AwardItems.valueOf();
		AgencyEventCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agencyEvent.getEventId());
		award.addItemInfos(cfg.getEventReward());
		award.rewardTakeAffectAndPush(player, Action.AGENCY_REWARD, true, null);
		// 删除事件
		agencyEntity.removeAgencyEvent(req.getUuid());
		// 特殊事件
		if (agencyEvent.getIsSpecialEvent() == 1) {
			agencyEntity.setSpecialId(cfg.getSpecialId());
		// 道具事件
		} else if (agencyEvent.getIsItemEvent() == 1){
		// 普通事件
		} else {
			// 池子里有的话,刷出新的事件
			int size = agencyEntity.getAgencyEventsPool().size();
			if(size > 0){
				AgencyEventEntity event = agencyEntity.getAgencyEventsPool().remove(size -1);
				agencyEntity.addAgencyEvent(event);
			}
		}
		//添加经验,到达最等级，不积累经验
		if(!this.isMaxLevel(agencyEntity.getCurrLevel())){
			agencyEntity.addExp(AgencyConstCfg.getInstance().getEventExp());
		}
		// 自动升级逻辑
		do {
			// 当前等级
			int currLevel = agencyEntity.getCurrLevel();
			if (currLevel > AgencyConstCfg.getInstance().getAutoLevelUpLimit()) {
				break;
			}
			if(this.isMaxLevel(currLevel)){
				break;
			}
			// 当前等级配置
			AgencyLevelCfg agencyLevelCfg = getAgencyLevelCfg(currLevel);
			// 经验值不足
			if (agencyEntity.getExp() < agencyLevelCfg.getExp()) {
				break;
			}
			//检查其他限制条件
			AgencyLevelupLimit limitType =AgencyLevelupLimit.valueOf(agencyLevelCfg.getLevelUpLimitType());
			if(limitType != null && !limitType.checkLimit(player, agencyLevelCfg.getLevelUpLimitParamList())){
				break;
			}
			agencyEntity.setCurrLevel(currLevel + 1);
			agencyEntity.setExp(agencyEntity.getExp() - agencyLevelCfg.getExp());
			agencyEntity.setKillCount(0);
			if(this.isMaxLevel(agencyEntity.getCurrLevel())){
				agencyEntity.setExp(0);
			}
			// 刷新事件
			refreshEventsLevelUp(currLevel + 1);
			LogUtil.logPlayerAgencyLevelUp(player, currLevel + 1);
		} while(false);
		pushPageInfo();
		player.responseSuccess(protocol.getType());
		LogUtil.logPlayerAgencyAward(player, agencyEvent.getUuid(), agencyEvent.getEventId(), 
				cfg.getSpecialEvent(), cfg.getLevelUpEvent(), cfg.getItemEvent(), cfg.getDifficulty());
		logger.info("agency getEventAward playerId:{},playerName:{},agencyUuid:{}, agencyEventId:{}", 
				player.getId(),player.getName(),agencyEvent.getUuid(), agencyEvent.getEventId());
		ActivityManager.getInstance().postEvent(new AgencyRewardEvent(player.getId(), agencyEvent.getEventId()));
	}
	
	
	public void onPlayerCityMove(WorldMoveCityMsg msg){
		// 建筑未解锁
		BuildingBaseEntity agencyBuild = player.getData().getBuildingEntityByType(BuildingType.RADAR);
		if (agencyBuild == null) {
			return;
		}
		int cityLevel = player.getCityLevel();
		int unlockLevel =  AgencyConstCfg.getInstance().getAgencyUnlockLevel();
		if(cityLevel < unlockLevel){
			return;
		}
		boolean needPush = doCheck();
		if (needPush) {
			pushPageInfo();
			logger.info("agency onPlayerCityMove playerId:{},playerName:{}",player.getId(),player.getName());
		}
	}
	
	
	
	@MessageHandler
	public void onPlayerCityLevelUp(BuildingLevelUpMsg msg){
		// 建筑未解锁
		BuildingBaseEntity agencyBuild = player.getData().getBuildingEntityByType(BuildingType.RADAR);
		if (agencyBuild == null) {
			return;
		}
		
		BuildingType buildingType = BuildingType.valueOf(msg.getBuildingType());
		if(buildingType == null){
			return;
		}
		if(buildingType != BuildingType.CONSTRUCTION_FACTORY){
			return;
		}
		
		// 没有达到开启等级
		int cityLevel = player.getCityLevel();
		int unlockLevel =  AgencyConstCfg.getInstance().getAgencyUnlockLevel();
		if(cityLevel < unlockLevel){
			return;
		}
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		int currLevel = agencyEntity.getCurrLevel();
		if (currLevel > AgencyConstCfg.getInstance().getAutoLevelUpLimit()) {
			return;
		}
		if(this.isMaxLevel(currLevel)){
			return;
		}
		
		// 当前等级配置
		AgencyLevelCfg agencyLevelCfg = getAgencyLevelCfg(currLevel);
		// 经验值不足
		if (agencyLevelCfg == null || agencyEntity.getExp() < agencyLevelCfg.getExp()) {
			return;
		}
		//检查其他限制条件
		AgencyLevelupLimit limitType =AgencyLevelupLimit.valueOf(agencyLevelCfg.getLevelUpLimitType());
		if(limitType != null && !limitType.checkLimit(player, agencyLevelCfg.getLevelUpLimitParamList())){
			return;
		}
		agencyEntity.setCurrLevel(currLevel + 1);
		agencyEntity.setExp(agencyEntity.getExp() - agencyLevelCfg.getExp());
		agencyEntity.setKillCount(0);
		if(this.isMaxLevel(agencyEntity.getCurrLevel())){
			agencyEntity.setExp(0);
		}
		// 刷新事件
		refreshEventsLevelUp(currLevel + 1);
		LogUtil.logPlayerAgencyLevelUp(player, currLevel + 1);
		logger.info("agency onPlayerCityLevelUp playerId:{},playerName:{}",player.getId(),player.getName());
	}
	
	
	
	/**
	 * 生成情报任务
	 */
	@ProtocolHandler(code = HP.code.GEN_AGENCY_EVENT_VALUE)
	protected void genAgencyEvent(HawkProtocol protocol) {
		BuildingBaseEntity agencyBuild = player.getData().getBuildingEntityByType(BuildingType.RADAR);
		if (agencyBuild == null) {
			return;
		}
		int cityLevel = player.getCityLevel();
		int unlockLevel =  AgencyConstCfg.getInstance().getAgencyUnlockLevel();
		if(cityLevel < unlockLevel){
			return;
		}
		GenAgencyEventReq req = protocol.parseProtocol(GenAgencyEventReq.getDefaultInstance());
		int eventId = req.getEventId();
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		AgencyEventCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, eventId);
		if (cfg == null) {
			return;
		}
		if(cfg.getItemEvent() <= 0){
			return;
		}
		if (agencyEntity.getItemEventGenSet().contains(eventId)) {
			return;
		}
		
		List<Integer> events = new ArrayList<>(); 
		events.add(eventId);
		//随机点列表
		Map<Integer,List<Integer>> posMap = this.getRandPos(agencyEntity.getCurrLevel(), events);
		int pos = this.getEventPos(posMap, eventId);
		int[] posArr = GameUtil.splitXAndY(pos);
		AgencyEventEntity event = new AgencyEventEntity(eventId, posArr[0], posArr[1]);
		event.setIsItemEvent(1);
		agencyEntity.addAgencyEvent(event);
		agencyEntity.addItemEventGen(eventId);
		player.responseSuccess(protocol.getType());
		pushPageInfo();
		LogUtil.logPlayerAgencyRefresh(player, event.getUuid(), event.getEventId(), 
				cfg.getSpecialEvent(), cfg.getLevelUpEvent(), cfg.getItemEvent(), cfg.getDifficulty());
		logger.info("agency genAgencyEvent playerId:{},playerName:{}",player.getId(),player.getName());
	}
	
	/**
	 * 前往搜索
	 */
	@ProtocolHandler(code = HP.code.AGENCY_GOTO_SEARCH_REQ_VALUE)
	protected void gotoSearch(HawkProtocol protocol) {
		AgencyEventCommon req = protocol.parseProtocol(AgencyEventCommon.getDefaultInstance());
		// 事件不存在
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		AgencyEventEntity agencyEvent = agencyEntity.getAgencyEvent(req.getUuid());
		if (agencyEvent == null) {
			return;
		}
		// 已经完成了
		if (agencyEvent.getEventState() == AgencyEventState.AGENCY_FINISHED_VALUE) {
			return;
		}
		// 检测
		if (doCheck()) {
			pushPageInfo();
			logger.info("agency gotoSearch playerId:{},playerName:{}",player.getId(),player.getName());
		}
		AgencySearchResp.Builder builder = AgencySearchResp.newBuilder();
		builder.setPosX(agencyEvent.getPosX());
		builder.setPosY(agencyEvent.getPosY());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.AGENCY_GOTO_SEARCH_RESP, builder));
	}
	
	/**
	 * 点击升级
	 */
	@ProtocolHandler(code = HP.code.AGENCY_LEVEL_UP_REQ_VALUE)
	protected void clickLevelUp(HawkProtocol protocol) {
		BuildingBaseEntity agencyBuild = player.getData().getBuildingEntityByType(BuildingType.RADAR);
		if (agencyBuild == null) {
			return;
		}
		int cityLevel = player.getCityLevel();
		int unlockLevel =  AgencyConstCfg.getInstance().getAgencyUnlockLevel();
		if(cityLevel < unlockLevel){
			return;
		}
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		// 当前等级
		int currLevel = agencyEntity.getCurrLevel();
		//达到最大等级
		if(this.isMaxLevel(currLevel)){
			return;
		}
		// 当前等级配置
		AgencyLevelCfg agencyLevelCfg = getAgencyLevelCfg(currLevel);
		// 经验值不足
		if (agencyEntity.getExp() < agencyLevelCfg.getExp()) {
			return;
		}
		
		//检查其他限制条件
		AgencyLevelupLimit limitType =AgencyLevelupLimit.valueOf(agencyLevelCfg.getLevelUpLimitType());
		if(limitType != null && !limitType.checkLimit(player, agencyLevelCfg.getLevelUpLimitParamList())){
			return;
		}
		agencyEntity.setCurrLevel(currLevel + 1);
		agencyEntity.setExp(agencyEntity.getExp() - agencyLevelCfg.getExp());
		agencyEntity.setKillCount(0);
		//如果是最大等级 经验清空，不累积
		if(this.isMaxLevel(agencyEntity.getCurrLevel())){
			agencyEntity.setExp(0);
		}
		// 刷新事件
		refreshEventsLevelUp(currLevel + 1);
		// 推界面信息
		pushPageInfo();
		// 返回成功
		player.responseSuccess(protocol.getType());
		LogUtil.logPlayerAgencyLevelUp(player, currLevel + 1);
		logger.info("agency clickLevelUp playerId:{},playerName:{},beforeLevel:{}",player.getId(),player.getName(),currLevel);
	}
	
	
	/**
	 * 领取宝箱
	 */
	@ProtocolHandler(code = HP.code.AGENCY_BOX_REQ_VALUE)
	protected void box(HawkProtocol protocol) {
		AgencyBoxReq req = protocol.parseProtocol(AgencyBoxReq.getDefaultInstance());
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		if(req.hasBoxId()){
			int boxId = req.getBoxId();
			if (agencyEntity.getBoxSet().contains(boxId)) {
				return;
			}
			AgencyBoxCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyBoxCfg.class,boxId);
			if(cfg == null){
				return;
			}
			if (agencyEntity.getCurrLevel() < cfg.getUnlockLevel()) {
				return;
			}
			agencyEntity.addBox(req.getBoxId());
			// 发奖励
			AwardItems award = AwardItems.valueOf();
			award.addItemInfos(cfg.getReward());
			award.rewardTakeAffectAndPush(player, Action.AGENCY_BOX_REWARD, true, null);
			pushPageInfo();
			player.responseSuccess(protocol.getType());
			logger.info("agency box  hasBoxId playerId:{},playerName:{},boxId:{}",player.getId(),player.getName(), req.getBoxId());
			return;
		}
		
		if(req.hasLevel()){
			int level = req.getLevel();
			int size = HawkConfigManager.getInstance().getConfigSize(AgencyBoxCfg.class);
			AgencyBoxCfg cfg = HawkConfigManager.getInstance().getConfigByIndex(AgencyBoxCfg.class, size - 1);
			if(!agencyEntity.getBoxSet().contains(cfg.getBoxId())){
				return;
			}
			if(level > agencyEntity.getCurrLevel()){
				return;
			}
			int boxExtLevel = agencyEntity.getBoxExtLevel();
			if(boxExtLevel == 0){
				boxExtLevel = cfg.getUnlockLevel() + 1;
			}else{
				boxExtLevel += 1;
			}
			if(level != boxExtLevel){
				return;
			}
			agencyEntity.setBoxExtLevel(level);
			// 发奖励
			AwardItems award = AwardItems.valueOf();
			award.addItemInfos(cfg.getReward());
			award.rewardTakeAffectAndPush(player, Action.AGENCY_BOX_REWARD, true, null);
			pushPageInfo();
			player.responseSuccess(protocol.getType());
			LogUtil.logPlayerAgencyBox(player, cfg.getBoxId(), agencyEntity.getBoxExtLevel(), agencyEntity.getCurrLevel());
			logger.info("agency box  hasLevel playerId:{},playerName:{},boxId:{}",player.getId(),player.getName(),cfg.getBoxId());
		}
	}

	
	/**
	 * 发起行军
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.AGENCY_MARCH_REQ_VALUE)
	protected boolean onMarch(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		// 事件
		AgencyEventEntity agencyEvent = agencyEntity.getAgencyEvent(req.getAgencyUuid());
		if (agencyEvent == null) {
			return false;
		}
		// 没有空闲行军
		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}
		//同时只能有一个行军
		BlockingQueue<IWorldMarch> playerMarchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		for (IWorldMarch iWorldMarch : playerMarchs) {
			if(iWorldMarch.getMarchType() == WorldMarchType.AGENCY_MARCH_RESCUR ||
					iWorldMarch.getMarchType() == WorldMarchType.AGENCY_MARCH_MONSTER ||
					iWorldMarch.getMarchType() == WorldMarchType.AGENCY_MARCH_COASTER){
				if(iWorldMarch.isReturnBackMarch()){
					continue;
				}
				if(iWorldMarch.getTerminalId() == agencyEvent.getPointId()){
					sendError(protocol.getType(), Status.AgencyError.AGENCY_MARCH_LIMIT_VALUE);
					return false;
				}
			}
		}
		AgencyEventCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agencyEvent.getEventId());
		// 体力消耗
		int strengthConsume = AgencyConstCfg.getInstance().getStrengthConsume();
		//打野处理
		if (cfg.getType() == AgencyEventType.AGENCY_TYPE_MONSTER_VALUE) {
			int buff = player.getEffect().getEffVal(EffType.ATK_MONSTER_VIT_ADD, new EffectParams(req, new ArrayList<>()));
			strengthConsume = (int)(strengthConsume * (1 + buff * GsConst.EFF_PER));
			//体力减少
			int buffReduce =  player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_ATK_MONSTER_VIT_REDUCE);
			strengthConsume = (int) (strengthConsume * (1 - buffReduce * GsConst.EFF_PER));
			strengthConsume = Math.max(strengthConsume, 1);
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, strengthConsume);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}
		consumeItems.consumeAndPush(player, Action.AGENCY_MARCH_VIT);
		// 打怪行军，检查出征带的士兵
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (cfg.getType() == AgencyEventType.AGENCY_TYPE_MONSTER_VALUE) {
			WorldPoint worldPoint = new WorldPoint();
			worldPoint.setPointType(WorldPointType.EMPTY_VALUE);
			worldPoint.setOwnerId(player.getId());
			// 出兵检测
			PlayerMarchModule marchModule = player.getModule(ModuleType.WORLD_MARCH_MODULE);
			if (!marchModule.checkMarchReq(req, protocol.getType(), armyList, worldPoint)) {
				return false;
			}
			// 扣兵
			if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(),
					req.getSuperSoldierId())) {
				sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
				return false;
			}
		}
		switch (cfg.getType()) {
		// 救援
		case AgencyEventType.AGENCY_TYPE_RESCUR_VALUE:
			IWorldMarch rescurMarch = WorldMarchService.getInstance().startMarch(player, WorldMarchType.AGENCY_MARCH_RESCUR_VALUE, agencyEvent.getPointId(), agencyEvent.getUuid(), null, 0, new EffectParams(req, armyList));
			if (rescurMarch == null) {
				return false;
			}
			rescurMarch.getMarchEntity().setVitCost(strengthConsume);
			break;
		// 打怪
		case AgencyEventType.AGENCY_TYPE_MONSTER_VALUE:
			IWorldMarch monsterMarch = WorldMarchService.getInstance().startMarch(player, WorldMarchType.AGENCY_MARCH_MONSTER_VALUE, agencyEvent.getPointId(), agencyEvent.getUuid(), null, 0, new EffectParams(req, armyList));
			if (monsterMarch == null) {
				return false;
			}
			monsterMarch.getMarchEntity().setVitCost(strengthConsume);
			break;
		//搜索
		case AgencyEventType.AGENCY_TYPE_COASTER_VALUE:
			IWorldMarch coasterMarch = WorldMarchService.getInstance().startMarch(player, WorldMarchType.AGENCY_MARCH_COASTER_VALUE, agencyEvent.getPointId(), agencyEvent.getUuid(), null, 0, new EffectParams(req, armyList));
			if (coasterMarch == null) {
				return false;
			}
			coasterMarch.getMarchEntity().setVitCost(strengthConsume);
			break;
			
		default:
			break;
		}
		// 返回成功
		player.responseSuccess(protocol.getType());
		
		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.AGENCY_MARCH_RESP_VALUE, builder));

		return true;
	}
	
	/**
	 * 完成情报任务
	 */
	@MessageHandler
	protected void onAgency(AgencyFinishMsg msg) {
		String agencyUUid = msg.getAgencyUUid();
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		AgencyEventEntity agencyEvent = agencyEntity.getAgencyEvent(agencyUUid);
		if (agencyEvent != null && agencyEvent.getEventState() == AgencyEventState.AGENCY_FINISHED_VALUE) {
			return;
		}
		AgencyEventCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agencyEvent.getEventId());
		agencyEvent.setEventState(AgencyEventState.AGENCY_FINISHED_VALUE);
		agencyEntity.setHasKilled(1);
		agencyEntity.addFinishCount();
		//金色特殊事件
		if(cfg.getSpecialEvent() == 1){
			agencyEntity.addFinishSpecialCount(1);
		}
		pushPageInfo();
		MissionManager.getInstance().postMsg(player.getId(), new EventAgencyMissionFinishInit());
		MissionManager.getInstance().postMsg(player.getId(), new EventAgency(agencyEvent.getEventId()));
		MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.INTELLIGENCE_TASK, 1));
		ActivityManager.getInstance().postEvent(new AgencyFinishEvent(player.getId(), agencyEvent.getEventId()));
		
		
		LogUtil.logPlayerAgencyComplete(player, agencyEvent.getUuid(), agencyEvent.getEventId(), cfg.getSpecialEvent(),
				cfg.getLevelUpEvent(), cfg.getItemEvent(), cfg.getDifficulty());
		logger.info("agency onAgency  playerId:{},playerName:{},agencyUUID:{},agencyEventID:{}",
				player.getId(),player.getName(),agencyEvent.getUuid(), agencyEvent.getEventId());
	}
	
	/**
	 * 获取情报中心等级配置
	 * @param level
	 * @return
	 */
	private AgencyLevelCfg getAgencyLevelCfg(int level) {
		AgencyLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyLevelCfg.class, level);
		return cfg;
	}
	
	
	private boolean isMaxLevel(int level){
		AgencyLevelCfg cfg = this.getAgencyLevelCfg(level + 1);
		if(cfg == null){
			return true;
		}
		return false;
	}
	/**
	 * 升级刷新事件
	 */
	private void refreshEventsLevelUp(int nextLevel) {
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		// 配置了初始化事件,直接刷初始化事件。没有配置的话,就按照最大数量刷新。
		List<Integer> initEventsList = getAgencyLevelCfg(nextLevel).getInitEventsList();
		if (initEventsList.isEmpty()) {
			AgencyLevelCfg agencyLevelCfg = getAgencyLevelCfg(nextLevel);
			for (int i = 0; i < agencyLevelCfg.getEventCount(); i++) {
				initEventsList.add(randEvent(nextLevel));
			}
		}
		//随机点列表
		Map<Integer,List<Integer>> posMap = this.getRandPos(agencyEntity.getCurrLevel(), initEventsList);
		for (int i = 0; i < initEventsList.size(); i++) {
			Integer eventId = initEventsList.get(i);
			AgencyEventCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class,eventId);
			int pos = this.getEventPos(posMap, eventId);
			int[] posArr = GameUtil.splitXAndY(pos);
			AgencyEventEntity event = new AgencyEventEntity(eventId, posArr[0], posArr[1]);
			agencyEntity.addAgencyEvent(event);
			if(cfg.getItemEvent() > 0){
				event.setIsItemEvent(1);
				agencyEntity.addItemEventGen(eventId);
			}
			LogUtil.logPlayerAgencyRefresh(player, event.getUuid(), event.getEventId(), 
					cfg.getSpecialEvent(), cfg.getLevelUpEvent(), cfg.getItemEvent(), cfg.getDifficulty());
		}
	}

	/**
	 * 自动刷新事件
	 * @return
	 */
	private void refreshEventsAuto(int level, long agencyStartTime) {
		// 情报中心实体
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		List<AgencyEventEntity> moveToPool = new ArrayList<>();
		// 把主界面的任务都放到池子里
		for (AgencyEventEntity agency : agencyEntity.getAgencyEvents().values()) {
			if (agency.getEventState() != AgencyEventState.AGENCY_NOT_FINISH_VALUE) {
				continue;
			}
			// 特殊事件，道具事件，升级事件。不放入池子中
			AgencyEventCfg eventCfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agency.getEventId());
			if (eventCfg.getSpecialEvent() == 1 || eventCfg.getItemEvent() == 1) {
				continue;
			}
			moveToPool.add(agency);
		}
		// 清除主界面的任务,放入池子
		for (AgencyEventEntity event : moveToPool) {
			agencyEntity.removeAgencyEvent(event.getUuid());
			agencyEntity.addAgencyPoolEvent(event);
		}
		//排序
		if(moveToPool.size() > 0){
			agencyEntity.sortAgencyPoolEvent();
		}
		int count = 0;
		for (AgencyEventEntity agency : agencyEntity.getAgencyEvents().values()) {
			// 特殊事件，道具事件，升级事件 不占用刷新数量
			AgencyEventCfg eventCfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, agency.getEventId());
			if (eventCfg.getSpecialEvent() == 1 || eventCfg.getItemEvent() == 1) {
				continue;
			}
			count ++;
		}
		// 情报中心等级配置
		AgencyLevelCfg agencyLevelCfg = getAgencyLevelCfg(level);
		int newCount = agencyLevelCfg.getEventCount() - count;
		newCount = newCount + player.getEffect().getEffVal(EffType.LIFE_TIME_CARD_640);
		newCount = Math.max(newCount, 0);
		//随机点列表
		List<Integer> randEvents = new ArrayList<>();
		for (int i = 0; i < newCount; i++) {
			// 生成新的情报任务
			int randEvent = randEvent(level);
			randEvents.add(randEvent);
		}
		//随机点列表
		Map<Integer,List<Integer>> posMap = this.getRandPos(agencyEntity.getCurrLevel(), randEvents);
		for (int eventId : randEvents) {
			int pos = this.getEventPos(posMap, eventId);
			int[] posArr = GameUtil.splitXAndY(pos);
			AgencyEventEntity event = new AgencyEventEntity(eventId, posArr[0], posArr[1]);
			if (agencyStartTime > 0) {
				event.setStartTime(agencyStartTime);
			}
			agencyEntity.addAgencyEvent(event);
			AgencyEventCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, event.getEventId());
			LogUtil.logPlayerAgencyRefresh(player, event.getUuid(), event.getEventId(), 
					cfg.getSpecialEvent(), cfg.getLevelUpEvent(), cfg.getItemEvent(), cfg.getDifficulty());
		}
		
		
	}
	
	/**
	 * 随机普通任务
	 * @return
	 */
	private int randEvent(int level) {
		AgencyLevelCfg agencyLevelCfg = getAgencyLevelCfg(level);
		int randomGroup = agencyLevelCfg.getWeightGroup();
		List<AgencyWeightCfg> list = AssembleDataManager.getInstance().getAgencyWeightCfgs(randomGroup);
		AgencyWeightCfg weightCfg = HawkRand.randomWeightObject(list);
		List<AgencyEventCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(AgencyEventCfg.class).toList();
		//击杀野怪等级
		PlayerMonsterEntity monsterEntity = player.getData().getMonsterEntity();
		int killedLvl = monsterEntity.getMaxLevel();
		killedLvl = Math.max(1, killedLvl);
		//玩家大本等级
		int playerCityLevel = this.player.getCityLevel();
		
		for(AgencyEventCfg cfg : cfgs) {
			if (cfg.getType() != weightCfg.getType()) {
				continue;
			}
			if (cfg.getQuality() != weightCfg.getQuality()) {
				continue;
			}
			if (cfg.getSpecialEvent() == 1) {
				continue;
			}
			if (cfg.getItemEvent() == 1) {
				continue;
			}
			if (cfg.getLevelUpEvent() == 1) {
				continue;
			}
			if(cfg.getType() == AgencyEventType.AGENCY_TYPE_MONSTER_VALUE
					&& cfg.getMonsterLevel() != killedLvl){
				continue;
			}
			if(cfg.getType() == AgencyEventType.AGENCY_TYPE_COASTER_VALUE
					&& cfg.getCityLevel() != playerCityLevel){
				continue;
			}
			if(cfg.getType() == AgencyEventType.AGENCY_TYPE_RESCUR_VALUE
					&& cfg.getCityLevel() != playerCityLevel){
				continue;
			}
			return cfg.getId();
		}
		return 0;
	}
	
	
	
	/**
	 * 取坐标
	 * @param posMap
	 * @param eventId
	 * @return
	 */
	private int getEventPos(Map<Integer,List<Integer>> posMap,int eventId){
		AgencyEventCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, eventId);
		List<Integer> list = posMap.get(cfg.getType());
		if(list == null || list.isEmpty()){
			return 0;
		}
		return list.remove(0);
	}
	
	/**
	 * 随机点
	 * @param level
	 * @param eids
	 * @return
	 */
	private Map<Integer,List<Integer>> getRandPos(int level,List<Integer> eids){
		Map<Integer,List<Integer>> rlt= new HashMap<>();
		//已经占用的点
		Set<Integer> useList = getAllPointList();
		//玩家城点
		int playerPosX = player.getPosXY()[0];
		int playerPosY = player.getPosXY()[1];
		AgencyLevelCfg agencyLevelCfg = getAgencyLevelCfg(level);
		Map<Integer,Integer> evnetCounts = new HashMap<>();
		for(int eId : eids){
			AgencyEventCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AgencyEventCfg.class, eId);
			int count = evnetCounts.getOrDefault(cfg.getType(), 0) + 1;
			evnetCounts.put(cfg.getType(), count);
		}
		//随机城点
		for(Entry<Integer, Integer> entry : evnetCounts.entrySet()){
			int type = entry.getKey();
			int val = entry.getValue();
			int[] range = agencyLevelCfg.getRange(type);
			List<Integer> posRlt  = this.randPos(playerPosX, playerPosY, range, useList, val);
			useList.addAll(posRlt);
			rlt.put(type, posRlt);
		}
		return rlt;
	}
	
	/**
	 * 随机坐标
	 */
	private List<Integer> randPos(int playerPosX,int playerPosY,int[] randomRadius,Set<Integer> useList,int count) {
		List<Integer> posRlt = new ArrayList<>();
		if(count <= 0){
			return posRlt;
		}
		// 先找内圈
		List<Point> pointList = this.getFreePointsAgency(playerPosX, playerPosY, randomRadius[0], randomRadius[1]);
		Set<Integer> findList1 = findPoint(pointList,useList,count);
		posRlt.addAll(findList1);
		if (posRlt.size() >= count) {
			return posRlt;
		}
		useList.addAll(findList1);
		int last = count - posRlt.size();
		// 在找外圈
		List<Point> pointList2 = this.getFreePointsAgency(playerPosX, playerPosY, randomRadius[1], randomRadius[2]);
		Set<Integer> findList2 = findPoint(pointList2,useList,Math.max(0, last));
		posRlt.addAll(findList2);
		return posRlt;
	}
	
	private Set<Integer> findPoint(List<Point> pointList,Set<Integer> useList,int count) {
		Set<Integer> set = new HashSet<>();
		if(count <= 0){
			return set;
		}
		HawkRand.randomOrder(pointList);
		for (Point point : pointList) {
			if(set.size() >= count){
				break; 
			}
			if (!point.canRMSeat()) {
				continue;
			}
			if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
				continue;
			}
			if (WorldPointService.getInstance().getAreaPoint(point.getX(), point.getY(), true) == null) {
				continue;
			}
			if (useList.contains(point.getId())) {
				continue;
			}
			set.add(point.getId());
		}
		return set;
	}
	
	/**
	 * 击杀野怪
	 * @param msg
	 */
	@MessageHandler
	private void onKillMonster(MissionMsg msg) {
		if (!EventMonsterAttack.class.isInstance(msg.getEvent())) {
			return;
		}
		EventMonsterAttack event = (EventMonsterAttack) msg.getEvent();
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		// 当前情报中心等级
		int currLevel = agencyEntity.getCurrLevel();
		// 当前情报中心等级配置
		AgencyLevelCfg agencyLevelCfg = getAgencyLevelCfg(currLevel);
		// 经验条没满不能完成任务
		if (agencyLevelCfg == null || agencyEntity.getExp() < agencyLevelCfg.getExp()) {
			HawkLog.errPrintln("agency kill monster message reach, playerId: {}, currLevel: {}, entity exp: {}", player.getId(), currLevel, agencyEntity.getExp());
			return;
		}
		agencyEntity.addKillCount(event.getAtkTimes());
		pushPageInfo();
		logger.info("agency onKillMonster playerId: {}, playerName: {}", player.getId(), player.getName());
	}
	
	/**
	 * 初始化数据
	 */
	public void initData() {
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		if (agencyEntity.getCurrLevel() > 0) {
			return;
		}
		int yearDay = HawkTime.getYearDay();
		agencyEntity.setPlayerPos(WorldPlayerService.getInstance().getPlayerPos(player.getId()));
		agencyEntity.setCurrLevel(1);
		agencyEntity.setNextRefreshTime(calcFirstRefreshTime());
		agencyEntity.setFinishCount(0);
		agencyEntity.setFinishSpecialDay(yearDay);
		refreshEventsLevelUp(1);
		logger.info("agency initData  playerId:{},playerName:{}",
				player.getId(),player.getName());
	}

	/**
	 * 刷新特殊事件
	 * @param id
	 */
	private boolean refreshSpecialEvent(AgencyEntity agencyEntity) {
		if(agencyEntity.getCurrLevel() < AgencyConstCfg.getInstance().getStartSpecialLv()){
			return false;
		}
		if(agencyEntity.getFinishSpecialCount() >= AgencyConstCfg.getInstance()
				.getSpecialEventDailyRefreshLimit()){
			return false;
		}
		for(AgencyEventEntity event : agencyEntity.getAgencyEvents().values()){
			if(event.getIsSpecialEvent() > 0){
				return false;
			}
		}
		int specialId = agencyEntity.getSpecialId() + 1;
		AgencyEventCfg eventCfg = null;
		List<AgencyEventCfg> configs = HawkConfigManager.getInstance().getConfigIterator(AgencyEventCfg.class).toList();
		for (AgencyEventCfg cfg :configs) {
			if (cfg.getSpecialEvent() == 1 && cfg.getSpecialId() == specialId) {
				eventCfg = cfg;
				break;
			}
		}
		if(Objects.isNull(eventCfg)){
			return false;
		}
		if(eventCfg.getCityLevel() > player.getCityLevel()){
			return false;
		}
		//随机点列表
		List<Integer> eventList = new ArrayList<>();
		eventList.add(eventCfg.getId());
		Map<Integer,List<Integer>> posMap = this.getRandPos(agencyEntity.getCurrLevel(), eventList);
		int pos = this.getEventPos(posMap, eventCfg.getId());
		int[] posArr = GameUtil.splitXAndY(pos);
		AgencyEventEntity event = new AgencyEventEntity(eventCfg.getId(), posArr[0], posArr[1]);
		event.setIsSpecialEvent(1);
		agencyEntity.addAgencyEvent(event);
		LogUtil.logPlayerAgencyRefresh(player, event.getUuid(), event.getEventId(), 
				eventCfg.getSpecialEvent(), eventCfg.getLevelUpEvent(), eventCfg.getItemEvent(), eventCfg.getDifficulty());
		return true;
	}
	
	/**
	 * 计算下次刷新时间
	 * @param init 是否是初始化刷新
	 * @return
	 */
	private long calcFirstRefreshTime() {
		long baseRefreshTime = 0L;
		// 当前的小时
		int currHour = HawkTime.getHour();
		// 刷新的小时组
		int[] refreshHourArray = AgencyConstCfg.getInstance().getRefreshTimeArray();
		// 随机的时间范围(s)
		int randSecondTime = AgencyConstCfg.getInstance().getRefreshDelay();
		int minHour = refreshHourArray[0];
		int maxHour = refreshHourArray[refreshHourArray.length - 1];
		// 今天还没刷新
		if (currHour < maxHour) {
			int refreshHour = 0;
			for (int i = 0; i < refreshHourArray.length; i++) {
				if(refreshHourArray[i] > currHour){
					refreshHour = refreshHourArray[i];
					break;
				}
			}
			baseRefreshTime = HawkTime.getNextAM0Date() - GsConst.DAY_MILLI_SECONDS + refreshHour * GsConst.HOUR_MILLI_SECONDS;
		} else {
			baseRefreshTime = HawkTime.getNextAM0Date() + minHour * GsConst.HOUR_MILLI_SECONDS;
		}
		long refreshTime =  baseRefreshTime + HawkRand.randInt(1, randSecondTime) * 1000L;
		// 解锁该系统后，玩家确定的下次刷新时间不足30分钟的话，则刷新时间需要向后延迟30分钟
		if (refreshTime - HawkTime.getMillisecond() < 1800 * 1000L) {
			refreshTime += 1800 * 1000L;
		}
		// 解锁该系统后，玩家确定的下次刷新时间超过8小时，则设为八小时后刷新
		if (refreshTime - HawkTime.getMillisecond() > 8 * 3600 * 1000L) {
			refreshTime = HawkTime.getMillisecond() + 8 * 3600 * 1000L;
		}
		return refreshTime;
	}

	
	/**
	 * 获取所有事件占用的点
	 * @return
	 */
	private Set<Integer> getAllPointList() {
		Set<Integer> pointList = new HashSet<>();
		AgencyEntity agencyEntity = player.getData().getAgencyEntity();
		for (AgencyEventEntity agency : agencyEntity.getAgencyEvents().values()) {
			pointList.add(agency.getPointId());
		}
		for (AgencyEventEntity agency : agencyEntity.getAgencyEventsPool()) {
			pointList.add(agency.getPointId());
		}
		return pointList;
	}
	
	
	/**
	 * 获取周围空闲点(空心区域，菱形)
	 * @param centerX
	 * @param centerY
	 * @param rmin
	 * @param rmax
	 * @return
	 */
	public List<Point> getFreePointsAgency(int centerX, int centerY, int rmin, int rmax) {
		List<Point> aroundPoints = new ArrayList<>();
		if (rmin <= 0 || rmax <= 0 || rmin >= rmax) {
			return aroundPoints;
		}
		int maxX = centerX + rmax;
		int minX = Math.max(0, centerX - rmax);
		int maxY = centerY + rmax;
		int minY = Math.max(0, centerY - rmax);
		for(int x= minX;x<= maxX;x++){
			for(int y =minY;y<=maxY;y++ ){
				double dis = WorldUtil.distance(centerX, centerY, x, y);
				if (dis > rmax || dis < rmin) {
					continue;
				}
				Point p1 =WorldPointService.getInstance().getAreaPoint(x, y, true);
				if (p1 != null) {
					aroundPoints.add(p1);
				}
			}
		}
		return aroundPoints;
	}
}
