package com.hawk.activity.type.impl.radiationWar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.result.Result;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.radiationWar.cfg.RadiationWarAchieveCfg;
import com.hawk.activity.type.impl.radiationWar.entity.RadiationWarEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.RadiationWarPageInfo;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * @Desc:184 新版辐射战争
 * @author:Winder
 * @date:2020年5月9日
 */
public class RadiationWarActivity extends ActivityBase implements AchieveProvider {

	public RadiationWarActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.RADIATION_WAR_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_RADIATION_WAR_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RadiationWarActivity activity = new RadiationWarActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RadiationWarEntity> queryList = HawkDBManager.getInstance()
				.query("from RadiationWarEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RadiationWarEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RadiationWarEntity entity = new RadiationWarEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_RADIATION_WAR, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<RadiationWarEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		RadiationWarEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<RadiationWarAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(RadiationWarAchieveCfg.class);
		while (configIterator.hasNext()) {
			RadiationWarAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		entity.notifyUpdate();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}
	

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<RadiationWarEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		RadiationWarEntity playerDataEntity = opPlayerDataEntity.get();
		if(playerDataEntity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(RadiationWarAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(RadiationWarAchieveCfg.class, achieveId);
		}
		return config;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	@Override
	public void onTakeRewardSuccess(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<RadiationWarEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		RadiationWarEntity playerDataEntity = opPlayerDataEntity.get();
		List<AchieveItem> items = new ArrayList<>();
		ConfigIterator<RadiationWarAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(RadiationWarAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			RadiationWarAchieveCfg cfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			items.add(item);
		}
		playerDataEntity.resetItemList(items);
		//重置击杀叛军次数
		playerDataEntity.setKillNum(0);
		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, playerDataEntity.getItemList());
		//同步
		syncActivityDataInfo(playerId);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<RadiationWarEntity> opRadiationWarEntity = getPlayerDataEntity(playerId);
		if (!opRadiationWarEntity.isPresent()) {
			return;
		}
		RadiationWarEntity radiationWarEntity = opRadiationWarEntity.get();
		int killNum = radiationWarEntity.getKillNum();
		RadiationWarPageInfo.Builder builder = RadiationWarPageInfo.newBuilder();
		builder.setKillNum(killNum);
		builder.setGuildKillNum(0);
		pushToPlayer(playerId, HP.code.RADITION_WAR_PAGE_INFO_RESP_VALUE, builder);
	}
	
	
	@Subscribe
	public void onMonsterKillEvent(MonsterAttackEvent event) {
		String playerId = event.getPlayerId();
		Optional<RadiationWarEntity> opRadiationWarEntity = getPlayerDataEntity(playerId);
		if (!opRadiationWarEntity.isPresent()) {
			return;
		}
		if (!isOpening(playerId)) {
			return;
		}
		if (!event.isKill()) {
			return;
		}
		//幽灵叛军类型野怪
		if (event.getMosterType() == MonsterType.TYPE_2_VALUE) {
			RadiationWarEntity raEntity = opRadiationWarEntity.get();
			raEntity.setKillNum(raEntity.getKillNum() + event.getAtkTimes());
			raEntity.notifyUpdate();
			//同步信息
			syncActivityDataInfo(playerId);
		}
	}
	@Override
	public void onPlayerMigrate(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub
		
	}
}
