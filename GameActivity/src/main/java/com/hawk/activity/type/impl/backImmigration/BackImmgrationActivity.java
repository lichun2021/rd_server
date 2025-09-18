package com.hawk.activity.type.impl.backImmigration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.config.MergeServerTimeCfg;
import com.hawk.activity.entity.ActivityAccountRoleInfo;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.backImmigration.cfg.BackImmgrationKVCfg;
import com.hawk.activity.type.impl.backSoldierExchange.cfg.BackSoldierExchangeKVCfg;
import com.hawk.activity.type.impl.immgration.cfg.ImmgrationActivityTimeCfg;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.game.protocol.Rank;

public class BackImmgrationActivity extends ActivityBase {
	
	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	public static final String CUSTOM_KEY = "BackImmgrationActivityDataRecord";
	
	public List<BackImmgrationServer> serverList = new ArrayList<>();
	public long tickTime;
	
	public BackImmgrationActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.BACK_IMMGRATION;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BackImmgrationActivity activity = new BackImmgrationActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return null;
	}
	
	@Override
	public void onTick() {
		long curTime = HawkTime.getMillisecond();
		if(curTime - this.tickTime <= 60 * 1000){
			return;
		}
		this.tickTime = curTime;
		// 开服时间不足
		BackImmgrationKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackImmgrationKVCfg.class);
		long openTime = this.getDataGeter().getServerOpenDate();
		if (HawkTime.getMillisecond() - openTime < kvCfg.getServerDelay()) {
			return;
		}
		//写入排行榜数据
		this.updateSelfBackImmgrationServerData();
		//更新缓存
		this.upodateBackImmgrationServerDatas();
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		int termId = this.getActivityTermId();
		if(termId <= 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		Long serverMergeTime = this.getDataGeter().getServerMergeTime();
		long serverSeparateTime = this.getServerSeparateTime();
		if(Objects.isNull(serverMergeTime)){
			serverMergeTime = 0l;
		}
		//要合服了  就不触发了
		if(serverMergeTime > curTime){
			return;
		}
		//要拆服了 就不触发了
		if(serverSeparateTime > curTime){
			return;
		}
		BackImmgrationKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackImmgrationKVCfg.class);
		BackImmgrationData data = this.getPlayerBackImmgrationData(playerId);
		//已经在触发开启中
		if(curTime < HawkTime.getAM0Date(new Date(data.getBackTime())).getTime() 
				+ kvCfg.getContinueDays() * HawkTime.DAY_MILLI_SECONDS){
			 logger.info("BackImmgrationActivity onPlayerLogin in activityTime playerId:{}, backTime:{}, logoutTime:{},power:{}", 
					 playerId, data.getBackTime(),data.getLogoutTime(), data.getPower());
			return;
		}
		//限定时间不触发
		if(curTime < HawkTime.getAM0Date(new Date(data.getBackTime())).getTime() + 
				(kvCfg.getIntervalDays() + kvCfg.getContinueDays())  * HawkTime.DAY_MILLI_SECONDS){
			logger.info("BackImmgrationActivity onPlayerLogin in stopTime playerId:{}, backTime:{}, logoutTime:{},power:{}",
					playerId, data.getBackTime(),data.getLogoutTime(), data.getPower());
			return;
		}
		//检查是否符合回流
		long lastLogoutTime = this.getDataGeter().getPlayerLogoutTime(playerId);
		Date lossBegin = new Date(lastLogoutTime);
		Date lossOver = new Date(curTime);
		if((HawkTime.calcBetweenDays(lossBegin, lossOver) - 1) < kvCfg.getTriggerLossDays()){
			logger.info("BackImmgrationActivity onPlayerLogin lossDays less playerId:{}, backTime:{}, logoutTime:{}", 
					playerId, curTime,lastLogoutTime);
			return;
		}
		//检查主城等级
		int cityLevel = this.getDataGeter().getBuildMaxLevel(playerId, BuildingType.CONSTRUCTION_FACTORY_VALUE);
		if(cityLevel < kvCfg.getTriggerCityLevel()){
			logger.info("BackImmgrationActivity onPlayerLogin cityLevel less playerId:{}, backTime:{}, logoutTime:{},cityLevel:{}", 
					playerId, curTime,lastLogoutTime,cityLevel);
			return;
		}
		//VIP等级
		int vipLevel = this.getDataGeter().getVipLevel(playerId);
		if(vipLevel < kvCfg.getVipLevel()){
			logger.info("BackImmgrationActivity onPlayerLogin vipLevel less playerId:{}, backTime:{}, logoutTime:{},cityLevel:{},vipLevel:{}", 
					playerId, curTime,lastLogoutTime,cityLevel,vipLevel);
			return;
		}
		//角色创建天数
		long createTime = this.getDataGeter().getPlayerCreateTime(playerId);
		int createDays = HawkTime.calcBetweenDays(new Date(createTime), new Date(curTime));
		if(createDays < kvCfg.getIntregisterDay()){
			logger.info("BackImmgrationActivity onPlayerLogin vipLevel less playerId:{}, backTime:{}, logoutTime:{},cityLevel:{},vipLevel:{},createTime:{}", 
					playerId, curTime,lastLogoutTime,cityLevel,vipLevel,createTime);
			return;
		}
		List<String> targetServers = this.getPlayerImmgrationServer(playerId);
		if(targetServers.size() <= 0){
			logger.info("BackImmgrationActivity onPlayerLogin targetServers less playerId:{}, backTime:{}, logoutTime:{},cityLevel:{},vipLevel:{},createTime:{}", 
					playerId, curTime,lastLogoutTime,cityLevel,vipLevel,createTime);
			return;
		}
		//初始化活动数据
		int backCount = data.getBackCount();
		long power = this.getDataGeter().getPlayerNoArmyPower(playerId);
		data.setTermId(termId);
		data.setBackTime(curTime);
		data.setLogoutTime(lastLogoutTime);
		data.setPower(power);
		data.setBackCount(backCount + 1);
		data.setTargetServer("");
		data.setImmgrationTime(0);
		this.getDataGeter().updateBackImmgrationData(playerId, data.serializ());
		this.logBackImmgrationStart(termId, playerId, backCount, power);
		logger.info("BackImmgrationActivity onPlayerLogin back sucess playerId:{}, backTime:{}, logoutTime:{},cityLevel:{},vipLevel:{},createTime:{}", 
				playerId, curTime,lastLogoutTime,cityLevel,vipLevel,createTime);
	}

	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		BackImmgrationData data = this.getPlayerBackImmgrationData(event.getPlayerId());
		long loginTime = this.getDataGeter().getAccountLoginTime(event.getPlayerId());
		BackSoldierExchangeKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackSoldierExchangeKVCfg.class);
		long outTime = HawkTime.getAM0Date(new Date(data.getBackTime())).getTime() + kvCfg.getContinueDays() * HawkTime.DAY_MILLI_SECONDS;
		if(loginTime > outTime){
			return;
		}
		this.checkActivityClose(event.getPlayerId());
	}
	
	
	
	
	@Override
	public boolean isActivityClose(String playerId) {
		long curTime = HawkTime.getMillisecond();
		BackImmgrationKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackImmgrationKVCfg.class);
		BackImmgrationData data = this.getPlayerBackImmgrationData(playerId);
		Long serverMergeTime = this.getDataGeter().getServerMergeTime();
		long serverSeparateTime = this.getServerSeparateTime();
		if(Objects.isNull(serverMergeTime)){
			serverMergeTime = 0l;
		}
		//和293活动不同时开放
		if(this.inActivity293(curTime)){
			return true;
		}
		//没有触发回流不开放
		if(data.getBackTime() <= 0){
			return true;
		}
		//如果是合服之前的回流，就关闭了
		if(data.getBackTime() < serverMergeTime){
			return true;
		}
		//拆服之后关闭
		if(data.getBackTime() < serverSeparateTime){
			return true;
		}
		//是否在活动期限内
		long outTime = data.getBackTime() + kvCfg.getContinueDays() * HawkTime.DAY_MILLI_SECONDS;
		if(curTime >= outTime){
			return true;
		}
		//是否已经完成移民
		if(data.getImmgrationTime() > 0){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 获取玩家可移民的服务器列表
	 */
	public List<String> getPlayerImmgrationServer(String playerId){
		BackImmgrationKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackImmgrationKVCfg.class);
		String openId = this.getDataGeter().getOpenId(playerId);
		List<ActivityAccountRoleInfo> roleList = ActivityGlobalRedis.getInstance().getActivityAccountRoleList(openId);
		Set<String> serverRoles = new HashSet<>();
		roleList.forEach(r->serverRoles.add(r.getServerId()));
		List<String> slist = new ArrayList<>();
		long serverOpenTime = this.getDataGeter().getServerOpenDate();
		long power = this.getDataGeter().getPlayerNoArmyPower(playerId);
		String serverId = this.getDataGeter().getServerId();
		for(BackImmgrationServer server : this.serverList){
			if(server.getServerId().equals(serverId)){
				continue;
			}
			if(serverRoles.contains(server.getServerId())){
				continue;
			}
			// 开服时间不足
			if (HawkTime.getMillisecond() - server.getOpenTime() < kvCfg.getServerDelay()) {
				continue;
			}
			// 开服时间需要大于当前服
			if(server.getOpenTime() <=  serverOpenTime){
				continue;
			}
			// 开服间隔天数
			if (HawkTime.calcBetweenDays(new Date(serverOpenTime), new Date(server.getOpenTime())) < kvCfg.getMigrantDay()) {
				continue;
			}
			if(server.getPowerMin() <= power && power <= server.getPowerMax()){
				slist.add(server.getServerId());
			}
			if(slist.size() >= kvCfg.getRecommendServerCount()){
				break;
			}
		}
		return slist;
	}
	
	
	public void onPlayerImmgrationAction(String playerId,String targetServer){
		long curTime = HawkTime.getMillisecond();
		BackImmgrationData data = this.getPlayerBackImmgrationData(playerId);
		data.setTargetServer(targetServer);
		data.setImmgrationTime(curTime);
		this.savePlayerBackImmgrationData(data);
	}
	
	
	public BackImmgrationData getPlayerBackImmgrationData(String playerId){
		String str = this.getDataGeter().getBackImmgrationData(playerId);
		BackImmgrationData data = new BackImmgrationData();
		data.setPlayerId(playerId);
		data.mergeFrom(str);
		return data;
	}
	
	
	public void savePlayerBackImmgrationData(BackImmgrationData data){
		this.getDataGeter().updateBackImmgrationData(data.getPlayerId(), data.serializ());
	}
	
	
	public void updateSelfBackImmgrationServerData(){
		String serverId = this.getDataGeter().getServerId();
		if(serverId.startsWith("1999") || serverId.startsWith("2999") ){
		    return;
		}
		long curTime = HawkTime.getMillisecond();
		int termId = this.getActivityTermId();
		BackImmgrationKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BackImmgrationKVCfg.class);
		long openTime = this.getDataGeter().getServerOpenDate();
		long rankPowerFrom = 0;
		long rankPowerTo = 0;
		Map<String, HawkTuple2<Integer, Long>> rankMap = this.getDataGeter().getRankDataMapCache(Rank.RankType.PLAYER_NOARMY_POWER_RANK);
		if(Objects.nonNull(rankMap)){
			for(HawkTuple2<Integer, Long> tuple : rankMap.values()){
				int rank = tuple.first;
				long power = tuple.second;
				if(rank == cfg.getPowerRankFrom()){
					rankPowerTo = power;
				}
				if(rank == cfg.getPowerRankEnd()){
					rankPowerFrom = power;
				}
			}
		}
		if(rankPowerFrom >= rankPowerTo){
			return;
		}
		BackImmgrationServer serverData = new BackImmgrationServer();
		serverData.setServerId(serverId);
		serverData.setPowerMin(rankPowerFrom);
		serverData.setPowerMax(rankPowerTo);
		serverData.setOpenTime(openTime);
		serverData.setUpdateTime(curTime);
		this.saveServerPowerInfo(termId, serverId, serverData);
	}
	
	
	
	public void upodateBackImmgrationServerDatas(){
		int termId = this.getActivityTermId();
		long curTime = HawkTime.getMillisecond();
		List<BackImmgrationServer> servers = this.getBackImmgrationServerDatas(termId);
		List<BackImmgrationServer> list = new ArrayList<>();
		for(BackImmgrationServer data : servers){
			//3分钟没有更新，不要了
			if(curTime - data.getUpdateTime() > 3 * 60 * 1000){
				continue;
			}
			list.add(data);
		}
		Collections.sort(list, new Comparator<BackImmgrationServer>() {
			@Override
			public int compare(BackImmgrationServer o1, BackImmgrationServer o2) {
				if(o1.getOpenTime() != o2.getOpenTime()){
					if(o1.getOpenTime()  > o2.getOpenTime()){
						return -1;
					}else{
						return 1;
					}
				}
				return o2.getServerId().compareTo(o1.getServerId());
			}
		});
		this.serverList = list;
	}
	
	
	
	public List<BackImmgrationServer> getBackImmgrationServerDatas(int termId){
		List<BackImmgrationServer> list = new ArrayList<>();
		String key = ActivityRedisKey.BACK_IMMGRATION_POW_RANGE +":"+ termId;
		Map<String,String> dataMap =ActivityGlobalRedis.getInstance().getRedisSession().hGetAll(key);
		for(String str : dataMap.values()){
			BackImmgrationServer server = new BackImmgrationServer();
			server.mergeFrom(str);
			list.add(server);
		}
		
		return list;
	}
	
	public void saveServerPowerInfo(int termId,String serverId,BackImmgrationServer serverData){
		String key = ActivityRedisKey.BACK_IMMGRATION_POW_RANGE +":"+ termId;
		ActivityGlobalRedis.getInstance().getRedisSession().hSet(key, serverId, serverData.serializ());
	}
	
	
	public long getServerSeparateTime(){
		String serverId = this.getDataGeter().getServerId();
		List<MergeServerTimeCfg> list = HawkConfigManager.getInstance()
				.getConfigIterator(MergeServerTimeCfg.class).toList();
		for(MergeServerTimeCfg mergeCfg : list){
			if(mergeCfg.getMergeType() ==0){
				continue;
			}
			if(!mergeCfg.getMergeServerList().contains(serverId)){
				continue;
			}
			return mergeCfg.getMergeTimeValue();
		}
		return 0;
	}
	
	/**
	 * 是否在293活动时间内
	 * @param time
	 * @return
	 */
	public boolean inActivity293(long time){
		//是否和318的活动冲突，如果有则不开
		ConfigIterator<ImmgrationActivityTimeCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ImmgrationActivityTimeCfg.class);
		while (configIterator.hasNext()) {
			ImmgrationActivityTimeCfg next = configIterator.next();
			if(next.getShowTimeValue() <= time && time <= next.getEndTimeValue() ){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 回流移民触发
	 * @param termId
	 * @param playerId
	 * @param backCount
	 * @param power
	 */
    private void logBackImmgrationStart(int termId,String playerId,int backCount ,long power){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("backCount", backCount); //邀请ID
        param.put("power", power); //受邀玩家ID
        getDataGeter().logActivityCommon(playerId, LogInfoType.back_immgration_start, param);
    }
	
}
