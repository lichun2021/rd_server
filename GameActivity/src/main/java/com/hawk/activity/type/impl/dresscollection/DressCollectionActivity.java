package com.hawk.activity.type.impl.dresscollection;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DressActiveEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.dresscollection.cfg.DressCollectionAchieveCfg;
import com.hawk.activity.type.impl.dresscollection.cfg.DressCollectionKVCfg;
import com.hawk.activity.type.impl.dresscollection.entity.DressCollectionEntity;
import com.hawk.game.protocol.Activity.DressCollectionInfoPB;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.game.protocol.HP;

/**
 * 周年庆称号活动（集齐所有周年装扮，激活永久5周年称号）
 * 
 * @author lating
 *
 */
public class DressCollectionActivity extends ActivityBase implements AchieveProvider {

	public DressCollectionActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DRESS_COLLECTION_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DressCollectionActivity activity = new DressCollectionActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DressCollectionEntity> queryList = HawkDBManager.getInstance()
				.query("from DressCollectionEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DressCollectionEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DressCollectionEntity entity = new DressCollectionEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.DRESS_COLLECTION, () -> {
				this.syncActivityDataInfo(playerId);
			});
		}
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		
		syncActivityInfo(playerId);
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		// 检测处于激活状态，但还未记上的装扮
		DressCollectionKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DressCollectionKVCfg.class);
		for (int dressId : cfg.getDressIdSet()) {
			dressInActiveStateCheck(event.getPlayerId(), dressId);
		}
		for (int dressId : cfg.getEffectIdSet()) {
			dressInActiveStateCheck(event.getPlayerId(), dressId);
		}
		
		syncActivityInfo(event.getPlayerId());
	}
	
	/**
	 * 检测装扮激活状态
	 * @param playerId
	 * @param dressId
	 */
	private void dressInActiveStateCheck(String playerId, int dressId) {
		Optional<DressCollectionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		DressCollectionEntity entity = opEntity.get();
		if (entity.getDressTypeList().contains(dressId)) {
			return;
		}
		
		try {
			boolean yes = this.getDataGeter().dressInActiveState(playerId, dressId);
			if (yes) {
				ActivityManager.getInstance().postEvent(new DressActiveEvent(playerId, dressId));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 同步活动数据
	 * @param playerId
	 * @param entity
	 */
	private void syncActivityInfo(String playerId) {
		Optional<DressCollectionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		DressCollectionEntity entity = opEntity.get();
        if (entity.getItemList().isEmpty()) {
            this.initAchieve(playerId);
        }
        
        DressCollectionInfoPB.Builder builder = DressCollectionInfoPB.newBuilder();
		builder.addAllCollectedId(entity.getDressTypeList());
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.DRESS_COLLECTION_INFO_RESP_VALUE, builder));
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
		 //获得玩家活动数据
        Optional<DressCollectionEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据实体
        DressCollectionEntity entity = opEntity.get();
        //如果成就数据为空就初始化成就数据
        if (entity.getItemList().isEmpty()) {
            //初始化成就数据
            this.initAchieve(playerId);
        }
        //返回当前成就数据
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(DressCollectionAchieveCfg.class, achieveId);
        return config;
	}

	@Override
	public Action takeRewardAction() {
		return Action.DRESS_COLLECTION_ACHIEVE;
	}
	
	 /**
     * 初始化成就数据
     * @param playerId 玩家id
     */
    private void initAchieve(String playerId) {
        Optional<DressCollectionEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        DressCollectionEntity entity = opEntity.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<DressCollectionAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DressCollectionAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        while (iterator.hasNext()) {
        	DressCollectionAchieveCfg cfg = iterator.next();
        	AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
        	list.add(item);
        }
        entity.setItemList(list);
    }
	
    /**
     * 装扮收集验证
     * 
     * @param playerId
     * @param dressId
     * @return
     */
    public boolean dressCollectCheck(DressActiveEvent event) {
    	String playerId =  event.getPlayerId();
    	int dressId = event.getDressId();
    	DressCollectionKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DressCollectionKVCfg.class);
    	if (!cfg.isValidDress(dressId)) {
    		return false;
    	}
    	
    	if (!this.isOpening(playerId)) {
    		return false;
    	}
    	Optional<DressCollectionEntity> opEntity = getPlayerDataEntity(playerId);
		DressCollectionEntity entity = opEntity.get();
		if (!entity.getDressTypeList().contains(dressId)) {
			entity.getDressTypeList().add(dressId);
			entity.notifyUpdate();
			try {
				syncActivityDataInfo(playerId);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		return true;
    }
}
