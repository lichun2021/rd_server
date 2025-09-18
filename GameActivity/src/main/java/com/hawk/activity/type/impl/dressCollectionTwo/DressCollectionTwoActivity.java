package com.hawk.activity.type.impl.dressCollectionTwo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
import com.hawk.activity.type.impl.dressCollectionTwo.cfg.DressCollectionTwoAchieveCfg;
import com.hawk.activity.type.impl.dressCollectionTwo.cfg.DressCollectionTwoKVCfg;
import com.hawk.activity.type.impl.dressCollectionTwo.entity.DressCollectionTwoEntity;
import com.hawk.game.protocol.Activity.DressCollectionTwoInfo;
import com.hawk.game.protocol.HP;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 周年庆称号活动（集齐所有周年装扮，激活永久5周年称号）
 * 
 * @author lating
 *
 */
public class DressCollectionTwoActivity extends ActivityBase implements AchieveProvider {

	public DressCollectionTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DRESS_COLLECTION_ACTIVITY_TWO;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DressCollectionTwoActivity activity = new DressCollectionTwoActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DressCollectionTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from DressCollectionTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DressCollectionTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DressCollectionTwoEntity entity = new DressCollectionTwoEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.DRESS_COLLECTION_TWO, () -> {
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
		DressCollectionTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DressCollectionTwoKVCfg.class);
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
		Optional<DressCollectionTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		DressCollectionTwoEntity entity = opEntity.get();
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
		Optional<DressCollectionTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		DressCollectionTwoEntity entity = opEntity.get();
        if (entity.getItemList().isEmpty()) {
            this.initAchieve(playerId);
        }
        
        DressCollectionTwoInfo.Builder builder = DressCollectionTwoInfo.newBuilder();
		builder.addAllCollectedId(entity.getDressTypeList());
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.DRESS_COLLECTION_TWO_INFO_RESP_VALUE, builder));
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
        Optional<DressCollectionTwoEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据实体
        DressCollectionTwoEntity entity = opEntity.get();
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
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(DressCollectionTwoAchieveCfg.class, achieveId);
        return config;
	}

	@Override
	public Action takeRewardAction() {
		return Action.DRESS_COLLECTION_ACHIEVE_TWO;
	}
	
	 /**
     * 初始化成就数据
     * @param playerId 玩家id
     */
    private void initAchieve(String playerId) {
        Optional<DressCollectionTwoEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        DressCollectionTwoEntity entity = opEntity.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<DressCollectionTwoAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DressCollectionTwoAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        while (iterator.hasNext()) {
        	DressCollectionTwoAchieveCfg cfg = iterator.next();
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
    	DressCollectionTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DressCollectionTwoKVCfg.class);
    	if (!cfg.isValidDress(dressId)) {
    		return false;
    	}
    	
    	if (!this.isOpening(playerId)) {
    		return false;
    	}
    	Optional<DressCollectionTwoEntity> opEntity = getPlayerDataEntity(playerId);
    	DressCollectionTwoEntity entity = opEntity.get();
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
