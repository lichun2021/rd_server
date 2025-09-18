package com.hawk.activity.type.impl.equipTech;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.equipTech.cfg.EquipTechAchieveCfg;
import com.hawk.activity.type.impl.equipTech.entity.EquipTechEntity;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class EquipTechActivity extends ActivityBase implements AchieveProvider{
	public final Logger logger = LoggerFactory.getLogger("Server");
	public EquipTechActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.EQUIP_TECH_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		EquipTechActivity activity = new EquipTechActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<EquipTechEntity> queryList = HawkDBManager.getInstance().query("from EquipTechEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0 ) {
			EquipTechEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_EQUIP_TECH, ()-> {
				initAchieve(playerId);
			});
		}
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		EquipTechEntity entity = new EquipTechEntity(playerId, termId);
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
		Optional<EquipTechEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent() || !isOpening(playerId)) {
			return Optional.empty();
		}
		EquipTechEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	/**初始化成就
	 * @param playerId
	 */
	private void initAchieve(String playerId) {
		Optional<EquipTechEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		EquipTechEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<EquipTechAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(EquipTechAchieveCfg.class);
		List<AchieveItem> list = new ArrayList<>();
		while (iterator.hasNext()) {
			EquipTechAchieveCfg cfg = iterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			list.add(item);
		}
		entity.setItemList(list);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		EquipTechAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipTechAchieveCfg.class, achieveId);
		return cfg;
	}

	@Override
	public Action takeRewardAction() {
		return Action.EQUIP_TECH_TASK_REWARD;
	}
	
	
}
