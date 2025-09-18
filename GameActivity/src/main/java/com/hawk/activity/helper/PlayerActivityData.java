package com.hawk.activity.helper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.annotation.SerializeField;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityPlayerEntity;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.IActivityDataEntity;

public class PlayerActivityData {
	/**
	 * 玩家id
	 */
	protected String playerId;
	/**
	 * 活动DB实体Map
	 */
	@SerializeField
	protected Map<Integer, HawkDBEntity> dataMap;
	/**
	 * 所有活动期次数据
	 */
	protected Map<Integer, ActivityPlayerEntity> playerActivityEntityMap;

	public PlayerActivityData() {
		dataMap = new ConcurrentHashMap<Integer, HawkDBEntity>();
		playerActivityEntityMap = new ConcurrentHashMap<Integer, ActivityPlayerEntity>();
	}
	
	public PlayerActivityData(String playerId) {
		this();
		this.playerId = playerId;
	}

	public String getPlayerId() {
		return playerId;
	}
	
	public Map<Integer, HawkDBEntity> getDataMap() {
		return dataMap;
	}
	
	public HawkDBEntity getActivityDataEntity(ActivityType activityType) {
		return dataMap.get(activityType.intValue());
	}

	public HawkDBEntity putActivityDataEntity(int activityType, HawkDBEntity entity) {
		HawkDBEntity result = dataMap.putIfAbsent(activityType, entity);
		
		// 若dataMap已存在该key的映射,且新数据的termId大于内存中数据,则进行替换
		if (result != null) {
			if (((IActivityDataEntity) entity).getTermId() > ((IActivityDataEntity) result).getTermId()) {
				dataMap.put(activityType, entity);
			}
		}
		
		return dataMap.get(activityType);
	}
	
	public Map<Integer, ActivityPlayerEntity> getPlayerActivityEntityMap() {
		return playerActivityEntityMap;
	}
	
	public ActivityPlayerEntity getPlayerActivityEntity(int activityId) {
		
		ActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCfg.class, activityId);
		if (cfg == null) {
			return null;
		}
		// 跨服玩家,且该活动跨服开关为关闭,返回空数据
		if (ActivityManager.getInstance().getDataGeter().isCrossPlayer(playerId) && !cfg.isCrossOpen()) {
			return null;
		}
		
		ActivityPlayerEntity entity = playerActivityEntityMap.get(activityId);
		if (entity != null) {
			return entity;
		}
		
		entity = loadFromDB(playerId, activityId);
		if (entity != null) {
			playerActivityEntityMap.putIfAbsent(activityId, entity);
			return playerActivityEntityMap.get(activityId);
		}

		// 创建新对象
		entity = createDataEntity(playerId, activityId);
		if (entity != null) {
			playerActivityEntityMap.putIfAbsent(activityId, entity);
			return playerActivityEntityMap.get(activityId);
		}
		
		return null;
	}
	
	/**
	 * 创建玩家活动期数数据(注册开启类活动)
	 * @param playerId
	 * @param activityId
	 * @return
	 */
	private ActivityPlayerEntity createDataEntity(String playerId, int activityId) {
		ActivityPlayerEntity entity = new ActivityPlayerEntity();
		entity.setPlayerId(playerId);
		entity.setActivityId(activityId);
		entity.setTermId(0);
		entity.setState(ActivityState.HIDDEN.intValue());
		HawkDBManager.getInstance().create(entity);
		return entity;
	}
	
	/**
	 * 从db加载玩家活动期数数据(注册开启类活动)
	 * 
	 * @param playerId
	 * @param activityId
	 * @return
	 */
	private ActivityPlayerEntity loadFromDB(String playerId, int activityId) {
		ActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCfg.class, activityId);
		if (cfg == null) {
			return null;
		}
		// 跨服玩家,且该活动跨服开关为关闭,返回空数据
		if (ActivityManager.getInstance().getDataGeter().isCrossPlayer(playerId) && !cfg.isCrossOpen()) {
			return null;
		}
		
		List<ActivityPlayerEntity> queryList = HawkDBManager.getInstance().query("from ActivityPlayerEntity where playerId = ? and activityId = ? and invalid = 0", playerId,
				activityId);

		if (queryList != null && queryList.size() > 0) {
			ActivityPlayerEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}
}
