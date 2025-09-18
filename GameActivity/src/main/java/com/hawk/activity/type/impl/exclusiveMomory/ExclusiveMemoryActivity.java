package com.hawk.activity.type.impl.exclusiveMomory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ExclusiveMemoryShareCountEvent;
import com.hawk.activity.event.impl.ExclusiveMemoryShareEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.exclusiveMomory.cfg.ExclusiveMemoryAchieveCfg;
import com.hawk.activity.type.impl.exclusiveMomory.entity.ExclusiveMemoryEntity;
import com.hawk.game.protocol.Activity.PBExclusiveMemoryResp;
import com.hawk.game.protocol.HP;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 独家记忆
 * @author che
 *
 */
public class ExclusiveMemoryActivity extends ActivityBase  implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public ExclusiveMemoryActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.EXCLUSIVE_MEMORY_ACTIVITY;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ExclusiveMemoryActivity activity = new ExclusiveMemoryActivity(
				config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ExclusiveMemoryEntity> queryList = HawkDBManager.getInstance()
				.query("from ExclusiveMemoryEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ExclusiveMemoryEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ExclusiveMemoryEntity entity = new ExclusiveMemoryEntity(playerId, termId);
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
		Optional<ExclusiveMemoryEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		ExclusiveMemoryEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId,entity);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity, true, getActivityId(), entity.getTermId());
		return Optional.of(items);
	}
	
	
	//初始化成就
	private void initAchieve(String playerId,ExclusiveMemoryEntity entity){
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<ExclusiveMemoryAchieveCfg> configIterator = HawkConfigManager.getInstance().
				getConfigIterator(ExclusiveMemoryAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			ExclusiveMemoryAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
	}
		
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.EXCLUSIVE_MEMORY_INIT, () -> {
				Optional<ExclusiveMemoryEntity>  optional = this.getPlayerDataEntity(playerId);
				if (!optional.isPresent()) {
					return;
				}
				ExclusiveMemoryEntity entity = optional.get();
				this.initAchieve(playerId,entity);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	/**
	 * 打开独家记忆
	 * @param playerId
	 */
	public void openExclusiveMemory(String playerId){
		Optional<ExclusiveMemoryEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        ExclusiveMemoryEntity entity = opEntity.get();
        entity.setOpenState(1);
        this.syncActivityInfo(playerId, entity);
	}

	@Subscribe
	public void onEvent(ExclusiveMemoryShareEvent event) {
		String playerId = event.getPlayerId();
        if (!isOpening(playerId)) {
            return;
        }
        //活动数据不存在
        Optional<ExclusiveMemoryEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        ExclusiveMemoryEntity entity =  opEntity.get();
        entity.recordLoginDay();
        ActivityManager.getInstance().postEvent(new ExclusiveMemoryShareCountEvent(playerId, entity.getLoginDaysCount()), true);
        logger.info("ExclusiveMemoryActivity ExclusiveMemoryShareCountEvent, playerId: {},count:{}",playerId,entity.getLoginDaysCount());
	}
	
	
		
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<ExclusiveMemoryEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	
	
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId,ExclusiveMemoryEntity entity){
		PBExclusiveMemoryResp.Builder builder = PBExclusiveMemoryResp.newBuilder();
		builder.setOpenState(entity.getOpenState());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.EXCLUSIVE_MEMORY_INFO_RESP,builder));
	}
	
	
	

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg =  HawkConfigManager.getInstance().
				getConfigByKey(ExclusiveMemoryAchieveCfg.class, achieveId);
		return cfg;
	}

	@Override
	public Action takeRewardAction() {
		return Action.EXCLUSIVE_MEMORY_TASK_REWARD;
	}
	
	
}
