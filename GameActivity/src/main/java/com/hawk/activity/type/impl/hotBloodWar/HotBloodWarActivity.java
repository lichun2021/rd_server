package com.hawk.activity.type.impl.hotBloodWar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ArmyHurtDeathEvent;
import com.hawk.activity.event.impl.HotBloodWarArmyCureEvent;
import com.hawk.activity.event.impl.HotBloodWarScoreEvent;
import com.hawk.activity.event.impl.PvpBattleEvent;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.hotBloodWar.cfg.HotBloodWarAchieveCfg;
import com.hawk.activity.type.impl.hotBloodWar.cfg.HotBloodWarKVCfg;
import com.hawk.activity.type.impl.hotBloodWar.cfg.HotBloodWarPointCfg;
import com.hawk.activity.type.impl.hotBloodWar.cfg.HotBloodWarTimeCfg;
import com.hawk.activity.type.impl.hotBloodWar.entity.CureArmyData;
import com.hawk.activity.type.impl.hotBloodWar.entity.HotBloodWarEntity;
import com.hawk.game.protocol.Activity.PBHotBloodWarArmy;
import com.hawk.game.protocol.Activity.PBHotBloodWarDataInfo;
import com.hawk.game.protocol.Activity.PBRecoverSpeedItem;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;
/**
 * 荣耀英雄降临-荣耀凯恩
 * @author che
 *
 */
public class HotBloodWarActivity extends ActivityBase  implements AchieveProvider {

	private long tickTime;

	public HotBloodWarActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HOT_BLOOD_WAR_378;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HotBloodWarActivity activity = new HotBloodWarActivity(
				config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HotBloodWarEntity> queryList = HawkDBManager.getInstance()
				.query("from HotBloodWarEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HotBloodWarEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HotBloodWarEntity entity = new HotBloodWarEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<HotBloodWarEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		HotBloodWarEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			return Optional.empty();
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity, true, getActivityId(), entity.getTermId());
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		HotBloodWarAchieveCfg cfg =  HawkConfigManager.getInstance().
				getConfigByKey(HotBloodWarAchieveCfg.class, achieveId);
		return cfg;
	}
	
	@Override
	public List<Builder> getRewardList(String playerId, AchieveConfig achieveConfig) {
		return AchieveProvider.super.getRewardList(playerId, achieveConfig);
	}

	@Override
	public Action takeRewardAction() {
		return Action.HOT_BLOOD_WAR_ACHIEVE_REWARD;
	}

	
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.HOT_BLOOD_WAR_INIT, () -> {
				Optional<HotBloodWarEntity>  optional = this.getPlayerDataEntity(playerId);
				if (!optional.isPresent()) {
					return;
				}
				this.initAcivityData(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	@Override
	public void onHidden() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.HOT_BLOOD_WAR_END, () -> {
				this.checkSendFinish(playerId);
			});
		}
	}
	
	
	@Override
	public void onTick() {
		long curTime = HawkTime.getMillisecond();
		if(this.tickTime <=0){
			this.tickTime = curTime;
			return;
		}
		
		if(curTime < this.tickTime + HawkTime.MINUTE_MILLI_SECONDS * 5){
			return;
		}
		this.tickTime = curTime;
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.HOT_BLOOD_WAR_TICK, () -> {
				Optional<HotBloodWarEntity>  optional = this.getPlayerDataEntity(playerId);
				if (!optional.isPresent()) {
					return;
				}
				HotBloodWarEntity entity = optional.get();
				if(entity.getCureArmyCalTime() > 0){
					this.cureDeathArmy(entity, 0);
				}
			});
		}
	
	}
	
	
	@Override
	public void onPlayerLogin(String playerId) {
		this.initAcivityData(playerId);
		Optional<HotBloodWarEntity> opEntity = getPlayerDataEntity(playerId);
		if (opEntity.isPresent()) {
			HotBloodWarEntity entity = opEntity.get();
			this.cureDeathArmy(entity,0);
		}
		//检查是否已经发放完成
		this.checkSendFinish(playerId);
	}
	
	

	
	@Subscribe
	public void onDeathCure(HotBloodWarArmyCureEvent event) {
		if(!this.isOpening(event.getPlayerId())){
			return;
		}
		Optional<HotBloodWarEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        if (!opEntity.isPresent()) {
            return;
        }
        HotBloodWarEntity entity = opEntity.get();
        //先治愈一下
        this.cureDeathArmy(entity,0);
		//在添加
		this.addDeathArmy(entity, event.getDeaths());
		//设置治愈开始时间
		long curTime = HawkTime.getMillisecond();
		boolean hasCure = entity.hasCureArmy();
		if(hasCure && entity.getCureArmyStartTime() <= 0){
			entity.setCureArmyStartTime(curTime);
			entity.setCureArmyCalTime(curTime);
		}
		this.syncActivityDataInfo(event.getPlayerId());
		//日志
		this.logHotBloodWarArmyFlow(event.getPlayerId(), entity.getTermId(), 1, SerializeHelper.mapToString(event.getDeaths()));
		HawkLog.logPrintln("HotBloodWarActivity-onDeathCure-playerId-{},army:{}", event.getPlayerId(),SerializeHelper.mapToString(event.getDeaths()));
		
	}
	
	
	
	
	
	@Subscribe
	public void onEnemyKill(PvpBattleEvent event) {
		if(!this.isOpening(event.getPlayerId())){
			return;
		}
		Optional<HotBloodWarEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        if (!opEntity.isPresent()) {
            return;
        }
        HotBloodWarEntity entity = opEntity.get();
        Map<Integer, Integer> killMap = event.getArmyKillMap();
        Map<Integer, Integer> hurtMap = event.getArmyHurtMap();
        String befMap = SerializeHelper.mapToString(entity.getEnemyKillMap());
        long befkillScore = entity.getEnemyKillScore();
        String killstr = SerializeHelper.mapToString(killMap);
        String hurtstr = SerializeHelper.mapToString(hurtMap);
       
        //记录击杀击伤
        for(Map.Entry<Integer, Integer> entry : killMap.entrySet()){
        	entity.addEnemyKillCount(entry.getKey(), entry.getValue());
        }
        for(Map.Entry<Integer, Integer> entry : hurtMap.entrySet()){
        	entity.addEnemyKillCount(entry.getKey(), entry.getValue());
        }
        //计算积分
        this.calEnemyKillScore(entity);
        //抛事件
        ActivityManager.getInstance().postEvent(new HotBloodWarScoreEvent(event.getPlayerId(),entity.getEnemyKillScore(),entity.getSelfHurtScore(),
        		entity.getEnemyKillScore() + entity.getSelfHurtScore()), true);
        String aftMap = SerializeHelper.mapToString(entity.getEnemyKillMap());
        long aftkillScore = entity.getEnemyKillScore();
        HawkLog.logPrintln("HotBloodWarActivity-onEnemyKill-playerId-{},killArmy:{},hurtArmy:{},befMap:{},aftMap:{},scoreBef:{},scoreAft:{}",
        		event.getPlayerId(),killstr,hurtstr,befMap,aftMap,befkillScore,aftkillScore);
	}
	
	
	
	@Subscribe
	public void onSelfHurt(ArmyHurtDeathEvent event) {
		if(!this.isOpening(event.getPlayerId())){
			return;
		}
		Optional<HotBloodWarEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        if (!opEntity.isPresent()) {
            return;
        }
        HotBloodWarEntity entity = opEntity.get();
        Map<Integer, Integer> deahtMap = event.getDeaths();
        Map<Integer, Integer> hurtMap = event.getHurts();
        
        String befMap = SerializeHelper.mapToString(entity.getSelfHurtMap());
        long befkillScore = entity.getSelfHurtScore();
        String deathstr = SerializeHelper.mapToString(deahtMap);
        String hurtstr = SerializeHelper.mapToString(hurtMap);
        
        
        //记录击杀击伤
        for(Map.Entry<Integer, Integer> entry : deahtMap.entrySet()){
        	entity.addSelfHurtCount(entry.getKey(), entry.getValue());
        }
        for(Map.Entry<Integer, Integer> entry : hurtMap.entrySet()){
        	entity.addSelfHurtCount(entry.getKey(), entry.getValue());
        }
        //计算积分
        this.calSelfHurtScore(entity);
        
        ActivityManager.getInstance().postEvent(new HotBloodWarScoreEvent(event.getPlayerId(),entity.getEnemyKillScore(),entity.getSelfHurtScore(),
        		entity.getEnemyKillScore() + entity.getSelfHurtScore()), true);
        String aftMap = SerializeHelper.mapToString(entity.getSelfHurtMap());
        long aftkillScore = entity.getSelfHurtScore();
        HawkLog.logPrintln("HotBloodWarActivity-onSelfHurt-playerId-{},killArmy:{},hurtArmy:{},befMap:{},aftMap:{},scoreBef:{},scoreAft:{}",
        		event.getPlayerId(),deathstr,hurtstr,befMap,aftMap,befkillScore,aftkillScore);
	}
	
	
	
	/**
	 * 计算杀敌积分
	 * @param entity
	 */
	public void calSelfHurtScore(HotBloodWarEntity entity){
		Map<Integer, Long> killMap = entity.getSelfHurtMap();
		Map<Integer,Long> pointMap = new HashMap<>();
		for(Map.Entry<Integer, Long> entry : killMap.entrySet()){
			int configId = HotBloodWarPointCfg.getPointConfigId(2, entry.getKey());
			long count = pointMap.getOrDefault(configId, 0l) + entry.getValue();
			pointMap.put(configId, count);
		}
		
		long totalScore = 0l;
		for(Map.Entry<Integer, Long> entry : pointMap.entrySet()){
			int configId = entry.getKey();
			long count = entry.getValue();
			HotBloodWarPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HotBloodWarPointCfg.class, configId);
			if(Objects.isNull(cfg)){
				continue;
			}
			long score = count / cfg.getTarget() * cfg.getPoint();
			totalScore += score;
		}
		entity.setSelfHurtScore(totalScore);
	}
	
	
	/**
	 * 计算杀敌积分
	 * @param entity
	 */
	public void calEnemyKillScore(HotBloodWarEntity entity){
		Map<Integer, Long> killMap = entity.getEnemyKillMap();
		Map<Integer,Long> pointMap = new HashMap<>();
		for(Map.Entry<Integer, Long> entry : killMap.entrySet()){
			int configId = HotBloodWarPointCfg.getPointConfigId(1, entry.getKey());
			long count = pointMap.getOrDefault(configId, 0l) + entry.getValue();
			pointMap.put(configId, count);
		}
		
		long totalScore = 0l;
		for(Map.Entry<Integer, Long> entry : pointMap.entrySet()){
			int configId = entry.getKey();
			long count = entry.getValue();
			HotBloodWarPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HotBloodWarPointCfg.class, configId);
			if(Objects.isNull(cfg)){
				continue;
			}
			long score = count / cfg.getTarget() * cfg.getPoint();
			totalScore += score;
		}
		entity.setEnemyKillScore(totalScore);
	}
	
	
	
	
	
	//初始化成就
	private void initAcivityData(String playerId){
		int termId = this.getActivityTermId();
		if(termId <= 0){
			return;
		}
		if(!this.isOpening(playerId)){
			return;
		}
		Optional<HotBloodWarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		HotBloodWarEntity entity = opEntity.get();
		if(entity.getInitTime() > 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		//记录初始化时间
		entity.setInitTime(curTime);
		//记录当天日期
        entity.recordLoginDay();
        //更新成就任务
        this.updateAchieveData(entity);
        //回收道具
        HawkLog.logPrintln("HotBloodWarActivity-initActivityInfo,playerId-{}", playerId);
	}
	
	
	/**
	 * 成就数据
	 * @param entity
	 */
	public void updateAchieveData(HotBloodWarEntity entity){
		List<AchieveItem> itemList = new ArrayList<>();
		ConfigIterator<HotBloodWarAchieveCfg> itr = HawkConfigManager.getInstance().getConfigIterator(HotBloodWarAchieveCfg.class);
		for(HotBloodWarAchieveCfg cfg :itr){
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), itemList), true);
		 //回收道具
        HawkLog.logPrintln("HotBloodWarActivity-updateAchieveData-playerId-{}", entity.getPlayerId());
	}
	
	
	
	
	
	
	/**
	 * 添加死兵数据
	 * @param entity
	 * @param list
	 */
	public void addDeathArmy(HotBloodWarEntity entity,Map<Integer,Integer> armys){
		Map<Integer, CureArmyData> map = entity.getCureArmyMap();
		for(Entry<Integer,Integer> data : armys.entrySet()){
			int armyid = data.getKey();
			int count = data.getValue();
			if(count <=0){
				continue;
			}
			HawkTuple3<Integer, Integer, Integer> armyCfg = this.getDataGeter().getSoldierConfigData(armyid);
			if(Objects.isNull(armyCfg)){
				continue;
			}
			// 箭塔不参与结算
			if(armyCfg.first == SoldierType.BARTIZAN_100_VALUE){
				continue;
			}
			if(armyCfg.first == SoldierType.WEAPON_LANDMINE_101_VALUE){
				continue;
			}
			if(armyCfg.first == SoldierType.WEAPON_ACKACK_102_VALUE){
				continue;
			}
			if(armyCfg.first == SoldierType.WEAPON_ANTI_TANK_103_VALUE){
				continue;
			}
			
			CureArmyData cureing = map.get(armyid);
			if(Objects.isNull(cureing)){
				cureing = new CureArmyData();
				cureing.setArmyId(armyid);
				cureing.setCureCount(count);
				map.put(armyid, cureing);
			}else{
				int total = cureing.getCureCount() + count;
				cureing.setCureCount(total);
			}
		}
		entity.notifyUpdate();
	}
	
	
	
	/**
	 * 治愈死兵
	 */
	public void cureDeathArmy(HotBloodWarEntity entity,long speedTime){
		if(entity.getCureArmyStartTime() <= 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		long lastCalTime = entity.getCureArmyCalTime();
		long cureTime = curTime - lastCalTime + speedTime;
		HawkLog.logPrintln("HotBloodWarActivity-cureDeathArmy-start-playerId-{},cureTime:{},speed:{},bef:{}",entity.getPlayerId(),cureTime,speedTime,JSON.toJSONString(entity.getCureArmyMap()));
		//先治愈指定
		cureTime = this.cureFirstTarget(cureTime, entity);
		//循环治愈
		cureTime = this.cureNormal(cureTime, entity);
		//如果完成，设置时间
		this.checkCureFinish(cureTime, entity);
		//如果治愈没有完成，如果是加速则记录一下
		if(speedTime > 0 && entity.getCureArmyStartTime() > 0){
			long speedTotal = entity.getCureArmySpeedTime() + speedTime;
			entity.setCureArmySpeedTime(speedTotal);
		}
		
		HawkLog.logPrintln("HotBloodWarActivity-cureDeathArmy-end-playerId-{},cureTime:{},bef:{}",entity.getPlayerId(),cureTime,JSON.toJSONString(entity.getCureArmyMap()));
	}
	
	
	
	/**
	 * 先治愈指定
	 * @param cureTime
	 * @param cureMap
	 * @param cureFirstType
	 */
	public long cureFirstTarget(long cureTime,HotBloodWarEntity entity){
		if(entity.getCureFirstType()<= 0){
			return cureTime;
		}
		List<CureArmyData> cureList = this.getCureQueue(entity, true);
		if(cureList.isEmpty()){
			return cureTime;
		}
		long timeBef = cureTime;
		Map<Integer,Integer> cureMap = new HashMap<>();
		Collections.sort(cureList);
		for(CureArmyData data : cureList){
			int armyCureTime = data.getSigleArmyCureTime();
			int canCureCnt = (int) (cureTime / armyCureTime);
			int cureCnt = Math.min(canCureCnt, data.getCureCount());
			if(canCureCnt <= 0){
				return cureTime;
			}
			//增加治愈数量
			int healthCnt = data.getHealthCount() + cureCnt;
			data.setHealthCount(healthCnt);
			//减少伤兵数量
			int cureingCnt = data.getCureCount() - cureCnt;
			cureingCnt = Math.max(0, cureingCnt);
			data.setCureCount(cureingCnt);
			//减少时间
			cureTime -= armyCureTime * cureCnt;
			//治愈记录
			cureMap.put(data.getArmyId(), cureCnt);
		}
		entity.notifyUpdate();
		HawkLog.logPrintln("HotBloodWarActivity-cureFirstTarget-playerId-{},firstType:{},timeBef:{},timeAft:{},cureMap:{}",entity.getCureFirstType(),
				entity.getPlayerId(),timeBef,cureTime,SerializeHelper.mapToString(cureMap));
		return cureTime;
		
	}
	
	/**
	 * 获取治疗列表
	 * @param entity
	 * @param first
	 * @return
	 */
	public List<CureArmyData> getCureQueue(HotBloodWarEntity entity,boolean first){
		Map<Integer, CureArmyData> cureMap = entity.getCureArmyMap();
		List<CureArmyData> cureList = new ArrayList<>();
		for(CureArmyData data : cureMap.values()){
			HawkTuple3<Integer, Integer, Integer> armyConfig = data.getConfigData();
			if(Objects.isNull(armyConfig)){
				continue;
			}
			if(data.getCureCount() <= 0){
				continue;
			}
			if(!first){
				cureList.add(data);
			}
			if(first && armyConfig.first == entity.getCureFirstType()){
				cureList.add(data);
			}
		}
		return cureList;
	} 
	
	/**
	 * 正常循环治愈
	 * @param cureTime
	 * @param cureMap
	 * @param cureFirstType
	 */
	public long cureNormal(long cureTime,HotBloodWarEntity entity){
		List<CureArmyData> cureList = this.getCureQueue(entity, false);
		if(cureList.isEmpty()){
			return cureTime;
		}
		long timeBef = cureTime;
		Map<Integer,Integer> cureMap = new HashMap<>();
		int turn = cureList.size();
		for(int i=0;i < turn;i++){
			HawkTuple2<Long, Integer> tuple = this.calCureNormalTrun(cureList);
			if(tuple.first <= 0){
				break;
			}
			long turnTime = tuple.first;
			int minCnt = tuple.second;
			int cureCnt = (int) (cureTime / turnTime);
			if(cureCnt <= 0){
				break;
			}
			cureCnt = Math.min(minCnt, cureCnt);
			for(CureArmyData data : cureList){
				if(data.getCureCount() <=0){
					continue;
				}
				int cureArmyCnt = Math.min(cureCnt, data.getCureCount());
				//增加治愈数量
				int healthCnt = data.getHealthCount() + cureArmyCnt;
				data.setHealthCount(healthCnt);
				//减少伤兵数量
				int cureingCnt = data.getCureCount() - cureArmyCnt;
				cureingCnt = Math.max(0, cureingCnt);
				data.setCureCount(cureingCnt);
				//记录
				int rcount = cureMap.getOrDefault(data.getArmyId(), 0) + cureArmyCnt;
				cureMap.put(data.getArmyId(), rcount);
			}
			cureTime -= turnTime * cureCnt;
		}
		
		//单个治愈
		Collections.sort(cureList);
		if(entity.getCureArmyId() > 0){
			int rotate = -1;
			for(int i=0;i < cureList.size();i++){
				CureArmyData data = cureList.get(i);
				if(data.getArmyId() == entity.getCureArmyId()){
					rotate = i;
					break;
				}
			}
			if(rotate > 0){
				Collections.rotate(cureList, -rotate);
			}
		}
		int cureArmyId = 0;
		for(CureArmyData data : cureList){
			if(data.getCureCount() <=0){
				continue;
			}
			int armyCureTime = data.getSigleArmyCureTime();
			if(cureTime <  armyCureTime){
				cureArmyId = data.getArmyId();
				break;
			}
			//增加治愈数量
			int healthCnt = data.getHealthCount() + 1;
			data.setHealthCount(healthCnt);
			//减少伤兵数量
			int cureingCnt = data.getCureCount() - 1;
			cureingCnt = Math.max(0, cureingCnt);
			data.setCureCount(cureingCnt);
			//时间减少
			cureTime -= armyCureTime;
			//记录
			int rcount = cureMap.getOrDefault(data.getArmyId(), 0) + 1;
			cureMap.put(data.getArmyId(), rcount);
		}
		entity.setCureArmyId(cureArmyId);
		entity.notifyUpdate();
		HawkLog.logPrintln("HotBloodWarActivity-cureNormal-playerId-{},timeBef:{},timeAft:{},cureMap:{}",entity.getPlayerId(),timeBef,cureTime,SerializeHelper.mapToString(cureMap));
		return cureTime;
	}
	
	
	public void checkCureFinish(long cureTime,HotBloodWarEntity entity){
		boolean finish = true;
		Map<Integer, CureArmyData> cureMap = entity.getCureArmyMap();
		for(CureArmyData data : cureMap.values()){
			HawkTuple3<Integer, Integer, Integer> armyConfig = data.getConfigData();
			if(Objects.isNull(armyConfig)){
				continue;
			}
			if(data.getCureCount() <= 0){
				continue;
			}
			finish = false;
			break;
		}
		if(finish){
			entity.setCureArmyStartTime(0);
			entity.setCureArmyCalTime(0);
			entity.setCureArmySpeedTime(0);
			entity.setCureArmyId(0);
		}else{
			long startTime = HawkTime.getMillisecond() - cureTime;
			entity.setCureArmyCalTime(startTime);
		}
		
	}
	
	
	public HawkTuple2<Long, Integer> calCureNormalTrun(List<CureArmyData> cureList){
		long turnTime = 0;
		int minCnt = 0;
		for(CureArmyData data : cureList){
			if(data.getCureCount() <= 0){
				continue;
			}
			int armyCureTime = data.getSigleArmyCureTime();
			turnTime += armyCureTime;
			if(minCnt == 0){
				minCnt = data.getCureCount();
			}else{
				minCnt = Math.min(minCnt, data.getCureCount());
			}
		}
		return HawkTuples.tuple(turnTime, minCnt);
	}
	
	
	/**
	 * 检查是否回送兵
	 * @param playerId
	 */
	public void checkSendFinish(String playerId){
		if(this.getActivityTermId() != 0){
			return;
		}
		int lastTermId = this.getLastTermId();
		HotBloodWarEntity lastEntity = null;
		//先拿缓存中
		HawkDBEntity entity = PlayerDataHelper.getInstance().getActivityDataEntity(playerId, getActivityType());
		if(Objects.nonNull(entity)){
			IActivityDataEntity dataEntity = (IActivityDataEntity) entity;
			if (dataEntity.getTermId() == lastTermId) {
				lastEntity = (HotBloodWarEntity) entity; 
			}
		}
		//在数据库提
		if(Objects.isNull(lastEntity)){
			HawkDBEntity dbEntity = this.loadFromDB(playerId, lastTermId);
			if(Objects.nonNull(dbEntity)){
				lastEntity = (HotBloodWarEntity) dbEntity; 
			}
		}
		//如果没有
		if(Objects.isNull(lastEntity)){
			return;
		}
		//已经发过了
		if(lastEntity.getFinishCheck() > 0){
			return;
		}
		//组织奖励
		Map<Integer,Integer> sendMap = new HashMap<>();
		List<RewardItem.Builder> list = new ArrayList<>();
		for(CureArmyData cureData : lastEntity.getCureArmyMap().values()){
			if(cureData.getHealthCount() > 0 || cureData.getCureCount() > 0){
				int armyId = cureData.getArmyId();
				int healthCnt = cureData.getHealthCount();
				int cureCnt = cureData.getCureCount();
				int cnt = healthCnt + cureCnt;
				cureData.setHealthCount(0);
				cureData.setCureCount(0);
				Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(ItemType.SOLDIER_VALUE, armyId, cnt);
				list.add(reward);
				sendMap.put(armyId, cnt);
			}
		}
		lastEntity.setFinishCheck(1);
		lastEntity.notifyUpdate(false, 0);
		if(list.isEmpty()){
			return;
		}
		// 邮件
		this.getDataGeter().sendMail(playerId, MailId.HOT_BLOOD_WAR_END_BACK,
				new Object[] {},
				new Object[] {}, 
				new Object[] {},
				list, false);
		HawkLog.logPrintln("HotBloodWarActivity-checkSendFinish-playerId-{},army:{}", playerId,JSON.toJSONString(sendMap));
	}

	
	
	public int getLastTermId() {
        long curTime = HawkTime.getMillisecond();
        HotBloodWarTimeCfg lastCfg = null;
        long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
        List<HotBloodWarTimeCfg> list = HawkConfigManager.getInstance().getConfigIterator(HotBloodWarTimeCfg.class).toList();
        
        this.getDataGeter().getServerOpenDate();
        for(HotBloodWarTimeCfg cfg : list){
            if(cfg.getHiddenTimeValue() + serverOpenDate < curTime){
                if(lastCfg == null){
                    lastCfg = cfg;
                }
                if(cfg.getTermId() > lastCfg.getTermId()){
                    lastCfg = cfg;
                }
            }
        }
        if(lastCfg == null){
            return  0;
        }
        return lastCfg.getTermId();
    }
	
	
	
	/**
	 * 计算治愈结束时间
	 * @param startTime
	 * @param map
	 * @return
	 */
	public long calCureEndTime(long startTime, Map<Integer, CureArmyData> map){
		if(startTime <=0){
			return 0;
		}
		long cureTime = 0;
		for(CureArmyData data : map.values()){
			long armyCureTime = data.getSigleArmyCureTime();
			cureTime += armyCureTime * data.getCureCount();
		}
		return startTime + cureTime;
	}
	
	
	/**
	 * 获取信息
	 * @param playerId
	 */
	public void getActivityData(String playerId){
		Optional<HotBloodWarEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		HotBloodWarEntity entity = opDataEntity.get();
		this.cureDeathArmy(entity,0);
		this.syncActivityDataInfo(playerId);
	}
	
	
	/**
	 * 收健康兵
	 * @param playerId
	 */
	public void achieveCureArmy(String playerId){
		Optional<HotBloodWarEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		HotBloodWarEntity entity = opDataEntity.get();
		//先治愈一下
		this.cureDeathArmy(entity,0);
		//收取
		List<RewardItem.Builder> list = new ArrayList<>();
		Map<Integer,Integer> achieveMap = new HashMap<>();
		List<Integer> dels = new ArrayList<>();
		for(CureArmyData cureData : entity.getCureArmyMap().values()){
			if(cureData.getHealthCount() > 0){
				int armyId = cureData.getArmyId();
				int cnt = cureData.getHealthCount();
				cureData.setHealthCount(0);
				Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(ItemType.SOLDIER_VALUE, armyId, cnt);
				list.add(reward);
				achieveMap.put(armyId, cnt);
			}
			if(cureData.getHealthCount() <= 0 &&
					cureData.getCureCount() <= 0){
				dels.add(cureData.getArmyId());
			}
		}
		for(int del : dels){
			 entity.getCureArmyMap().remove(del);
		}
		entity.notifyUpdate();
		this.syncActivityDataInfo(playerId);
		this.getDataGeter().takeReward(playerId, list,
	                1, Action.HOT_BLOOD_WAR_ACHIEVE_ARMY, true,
	                Reward.RewardOrginType.ACTIVITY_REWARD);
		//日志
		this.logHotBloodWarArmyFlow(playerId, entity.getTermId(), 2, SerializeHelper.mapToString(achieveMap));
		HawkLog.logPrintln("HotBloodWarActivity-achieveCureArmy-playerId-{},army：{}",entity.getPlayerId(),SerializeHelper.mapToString(achieveMap));
	}
	
	
	/**
	 * 设置先制约的类型
	 * @param playerId
	 * @param cureFirstType
	 */
	public void setFirstCureType(String playerId,int cureFirstType){
		Optional<HotBloodWarEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		HotBloodWarEntity entity = opDataEntity.get();
		//先治愈一下
		this.cureDeathArmy(entity,0);
		entity.setCureFirstType(cureFirstType);
		this.syncActivityDataInfo(playerId);
	}
	
	/**
	 * 加速治疗
	 * @param playerId
	 * @param itemList
	 */
	public void itemSpeedUp(String playerId,List<PBRecoverSpeedItem> itemList){
		if(itemList.isEmpty()){
			return;
		}
		Optional<HotBloodWarEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		HotBloodWarEntity entity = opDataEntity.get();
		//先治愈一下
		this.cureDeathArmy(entity,0);
		//没有兵可以治愈
		if(entity.getCureArmyStartTime() <= 0){
			return;
		}
		HotBloodWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HotBloodWarKVCfg.class);
		List<RewardItem.Builder> costList = new ArrayList<>();
	    long speedTotal = 0;
		for (PBRecoverSpeedItem item : itemList) {
			int itemId = item.getItemUuid();
			if(!cfg.getSpeedItemList().contains(itemId)){
				continue;
			}
			int cnt = item.getItemCount();
			int speedTime = this.getDataGeter().getItemSpeedUpTime(itemId);
			if(speedTime <= 0){
				continue;
			}
			speedTotal += speedTime * 1000L * cnt;
			Reward.RewardItem.Builder cost = RewardHelper.toRewardItem(ItemType.TOOL_VALUE, itemId, cnt);
			costList.add(cost);
		}
		 //检查消耗
         boolean cost = this.getDataGeter().cost(playerId, costList, 1,
                Action.HOT_BLOOD_WAR_CURE_SPEED_COST, false);
         if(!cost){
        	 PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                     HP.code2.HOT_BLOOD_WAR_CURE_SPEED_C_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
        	 return;
         }
         this.cureDeathArmy(entity, speedTotal);
         this.syncActivityDataInfo(playerId);
	}

	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<HotBloodWarEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		HotBloodWarEntity entity = opDataEntity.get();
		PBHotBloodWarDataInfo.Builder builder = PBHotBloodWarDataInfo.newBuilder();
		Map<Integer, CureArmyData> cureMap = entity.getCureArmyMap();
		long endTime = this.calCureEndTime(entity.getCureArmyCalTime(), cureMap);
		for(CureArmyData cure : cureMap.values()){
			PBHotBloodWarArmy.Builder armyBuilder = PBHotBloodWarArmy.newBuilder();
			armyBuilder.setArmyId(cure.getArmyId());
			armyBuilder.setDeadCount(cure.getCureCount());
			armyBuilder.setRecieveCount(cure.getHealthCount());
			builder.addArmys(armyBuilder);
		}
		builder.setRecoverStartTime(entity.getCureArmyStartTime());
		builder.setRecoverEndTime(endTime);
		builder.setRecoverSpeedTime(entity.getCureArmySpeedTime());
		builder.setFirstRecoverArmy(entity.getCureFirstType());
		builder.setEnemyKillScore(entity.getEnemyKillScore());
		builder.setSelfHurtScore(entity.getSelfHurtScore());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.HOT_BLOOD_WAR_INFO_S_VALUE,builder));
	}
	


    /**
     * 死兵处理 
     * @param playerId
     * @param termId
     * @param actionType  1收入  2领取
     * @param armyData  士兵数据
     */
    private void logHotBloodWarArmyFlow(String playerId, int termId,int actionType,String armyData){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("actionType", actionType);
        param.put("armyData", armyData);
        getDataGeter().logActivityCommon(playerId, LogInfoType.hot_blood_war_army_flow, param);
    }

    
}
