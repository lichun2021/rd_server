package com.hawk.activity.type.impl.senceShare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BarracksShareSucessEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PhysicalPowerShareSucessEvent;
import com.hawk.activity.event.impl.ShareEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.senceShare.cfg.SceneShareAchieveCfg;
import com.hawk.activity.type.impl.senceShare.cfg.SceneShareKVCfg;
import com.hawk.activity.type.impl.senceShare.entity.SceneShareEntity;
import com.hawk.game.protocol.Activity.SceneShareInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.DailyShareType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 场景分享活动
 * @author che
 *
 */
public class SceneShareActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public SceneShareActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SCENE_SHARE_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<SceneShareEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			// 刷成就
			if (opDataEntity.get().getItemList().isEmpty()) {
				initAchieveInfo(playerId);
			}
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACTIVITY_SCENE_SHARE_INIT, () -> {
				initAchieveInfo(playerId);
				this.syncActivityDataInfo(playerId);
			});
		}
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<SceneShareEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	// 同步数据消息给玩家
	public void syncActivityInfo(String playerId, SceneShareEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}
		SceneShareInfoResp.Builder sBuilder =  SceneShareInfoResp.newBuilder();
		List<Integer> slist = entity.getSceneList();
		if(slist.size() > 0){
			for(int share : slist){
				sBuilder.addSceneShares(share);
			}
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.SCENE_SHARE_INFO_SYNC_RESP_VALUE, sBuilder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SceneShareActivity activity = new SceneShareActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SceneShareEntity> queryList = HawkDBManager.getInstance()
				.query("from SceneShareEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SceneShareEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SceneShareEntity entity = new SceneShareEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		SceneShareKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SceneShareKVCfg.class);
		String playerId = event.getPlayerId();
		Optional<SceneShareEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		SceneShareEntity entity = opPlayerDataEntity.get();
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		if (kvCfg.isReset()) {
			List<AchieveItem> addList = new ArrayList<AchieveItem>();
			ConfigIterator<SceneShareAchieveCfg> scoreAchieveIt = HawkConfigManager.getInstance()
					.getConfigIterator(SceneShareAchieveCfg.class);
			while (scoreAchieveIt.hasNext()) {
				SceneShareAchieveCfg achieveCfg = scoreAchieveIt.next();
				AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
				addList.add(item);
			}
			entity.resetItemList(addList);
			// 初始化成就数据
			ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, addList), true);
		}
		//重置分享记录
		entity.clearSceneList();
		//推送新数据
		syncActivityInfo(playerId, entity);
		logger.info("SceneShareActivity onContinueLogin resetShare:"+playerId);
	}





	
	
	
	@Subscribe
	public void playerShare(ShareEvent event) {
		DailyShareType stype = event.getShareType();
		String playerId = event.getPlayerId();
		if (HawkOSOperator.isEmptyString(playerId)) {
			return;
		}
		ActivityEvent aEvent = null;
		switch (stype) {
		case SHARE_TANK_FACTORY:
		case SHARE_CHARIOT_FACTORY:
		case SHARE_PLANE_FACTORY:
		case SHARE_SOLDIER_FACTORY:
			aEvent = BarracksShareSucessEvent.valueOf(playerId);
			break;
		case SHARE_PHYSICAL_POWER:
			aEvent = PhysicalPowerShareSucessEvent.valueOf(playerId);
			break;
		default:
			return;
		}
		Optional<SceneShareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SceneShareEntity entity = opEntity.get();
		if(entity.sceneShared(stype.getNumber())){
			logger.info("SceneShareActivity playerShare already "+playerId+":"+stype.getNumber());
			return;
		}
		entity.addSceneShare(stype.getNumber());
		this.syncActivityInfo(playerId, entity);
		ActivityManager.getInstance().postEvent(aEvent);
		logger.info("SceneShareActivity playerShare sucess "+playerId+":"+stype.getNumber());
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
		Optional<SceneShareEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		SceneShareEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public Action takeRewardAction() {
		return Action.SCENE_SHARE_AWARD;
	}

	@Override
	public SceneShareAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(SceneShareAchieveCfg.class, achieveId);
	}

	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<SceneShareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SceneShareEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		List<AchieveItem> addList = new ArrayList<AchieveItem>();
		ConfigIterator<SceneShareAchieveCfg> scoreAchieveIt = HawkConfigManager.getInstance()
				.getConfigIterator(SceneShareAchieveCfg.class);
		while (scoreAchieveIt.hasNext()) {
			SceneShareAchieveCfg achieveCfg = scoreAchieveIt.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			addList.add(item);
		}
		entity.resetItemList(addList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

}
