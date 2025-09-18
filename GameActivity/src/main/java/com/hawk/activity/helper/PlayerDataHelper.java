package com.hawk.activity.helper;

import java.util.concurrent.TimeUnit;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.hawk.activity.config.ActivityConfig;
import com.hawk.activity.entity.ActivityPlayerEntity;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.ActivityType;

/**
 * <pre>
 * 玩家数据集合，以key-value的方式进行存放(@see PlayerKey)，包括玩家自身的数据和玩家的活动数据
 * 玩家自身数据需要在玩家登录时进行填充，同时当玩家相应数据发生变化时，需要以活动事件(ActivityEvent)方式通知活动模块进行数据变更
 * 约定由@see PlayerDataUpdater接收并处理各个数据变更事件
 * </pre>
 * @author PhilChen
 *
 */
public class PlayerDataHelper {
	/**
	 * 日志对象
	 */
	protected static final Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 数据获取代理
	 */
	private ActivityDataProxy dataGeter;
	/**
	 * 玩家数据缓存
	 */
	private LoadingCache<String, PlayerActivityData> playerDataCache;

	/**
	 * 实例对象
	 */
	private static PlayerDataHelper instance;
	
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static PlayerDataHelper getInstance() {
		if (instance == null) {
			instance = new PlayerDataHelper();
		}
		return instance;
	}
	
	/**
	 * 默认构造
	 */
	private PlayerDataHelper() {
		init();
	}
	
	/**
	 * 设置数据获取器
	 * 
	 * @param dataGeter
	 */
	public void setDataGeter(ActivityDataProxy dataGeter) {
		this.dataGeter = dataGeter;
	}
	
	public ActivityDataProxy getDataGeter() {
		return dataGeter;
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		playerDataCache = CacheBuilder.newBuilder().recordStats().maximumSize(ActivityConfig.getInstance().getCacheMaxSize())
				.initialCapacity(ActivityConfig.getInstance().getCacheInitSize())
				.expireAfterAccess(ActivityConfig.getInstance().getCacheExpireTime(), TimeUnit.MILLISECONDS)
				.removalListener(new RemovalListener<String, PlayerActivityData>() {
					@Override
					public void onRemoval(RemovalNotification<String, PlayerActivityData> notification) {
						PlayerActivityData playerData = notification.getValue();
						logger.info("cache remove player activity data, playerId: {}", playerData.getPlayerId());
					}
				}).build(new CacheLoader<String, PlayerActivityData>() {
					@Override
					public PlayerActivityData load(String playerId) {
						PlayerActivityData playerData = new PlayerActivityData(playerId);
						return playerData;
					}
				});
	}
	
	/**
	 * 获取玩家活动数据
	 * 
	 * @param playerId
	 * @return
	 */
	public PlayerActivityData getPlayerData(String playerId, boolean autoLoad) {
		PlayerActivityData playerActivityData = null;
		try {
			if (autoLoad) {
				playerActivityData = playerDataCache.get(playerId);
			} else {
				playerActivityData = playerDataCache.getIfPresent(playerId);
			}
			return playerActivityData;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return playerActivityData;
	}
	
	/**
	 * 获取玩家活动描述数据
	 * 
	 * @param playerId
	 * @return
	 */
	public PlayerActivityData getPlayerData(String playerId) {
		return getPlayerData(playerId, true);
	}

	/**
	 * 获取玩家活动详细数据
	 * 
	 * @param playerId
	 * @param activityType
	 * @return
	 */
	public HawkDBEntity getActivityDataEntity(String playerId, ActivityType activityType) {
		PlayerActivityData playerData = getPlayerData(playerId);
		return playerData.getActivityDataEntity(activityType);
	}

	/**
	 * 存储玩家活动详细数据
	 * 
	 * @param playerId
	 * @param activityType
	 * @param entity
	 * @return
	 */
	public HawkDBEntity putActivityDataEntity(String playerId, ActivityType activityType, HawkDBEntity entity) {
		PlayerActivityData playerData = getPlayerData(playerId);
		return playerData.putActivityDataEntity(activityType.intValue(), entity);
	}
	
	/**
	 * 获取玩家活动详细数据
	 * 
	 * @param playerId
	 * @param activityId
	 * @return
	 */
	public ActivityPlayerEntity getPlayerActivityEntity(String playerId, int activityId) {
		PlayerActivityData playerData = getPlayerData(playerId);
		ActivityPlayerEntity playerActivityEntity = playerData.getPlayerActivityEntity(activityId);
		return playerActivityEntity;
	}
}
