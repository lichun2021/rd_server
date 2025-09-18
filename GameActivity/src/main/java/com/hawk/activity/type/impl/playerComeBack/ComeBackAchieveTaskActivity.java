package com.hawk.activity.type.impl.playerComeBack;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityAccountRoleInfo;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ComeBackLoginDayEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.playerComeBack.cfg.achieve.PlayerComeBackAchieveTaskConfig;
import com.hawk.activity.type.impl.playerComeBack.entity.PlayerComeBackEntity;
import com.hawk.activity.type.impl.playerComeBack.helper.ComeBackPlayerHelper;
import com.hawk.log.Action;

/***
 * 回归成就活动（发展冲刺）
 * @author yang.rao
 *
 */
public class ComeBackAchieveTaskActivity extends ActivityBase implements AchieveProvider {

	public ComeBackAchieveTaskActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		/***
		 * 流程：1.判定entity有没有init 2.如果没有，则判定是否符合老玩家回归 3.符合则初始化entity
		 * 4.如果有init，则判定是否所有的活动都结束了，如果都结束了，则清理数据，重新判定是否符合老玩家回归
		 */
		Optional<PlayerComeBackEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		PlayerComeBackEntity entity = optional.get();
		String openId = ActivityManager.getInstance().getDataGeter().getOpenId(playerId);
		if (!entity.isInit()){
			ActivityAccountRoleInfo info = ComeBackPlayerHelper.isComeBackPlayer(openId);
			if(info != null){
				entity.init();
				entity.setAccountLogoutTime(info.getLogoutTime());
				entity.notifyUpdate();
				//初始化成就
				initAchieve(playerId, entity);
			}
		}
		//策划需求，开一期活动，本期之内，玩家只可能开启一期回归活动
//		else{
//			if(entity.allActivityEnd()){
//				if(ComeBackPlayerHelper.isComeBackPlayer(openId)){
//					entity.reset(); //清理上一期的数据
//					entity.init(); //重新开一期
//				}
//			}
//		}
	}
	
	private void initAchieve(String playerId, PlayerComeBackEntity entity){
		Iterator<PlayerComeBackAchieveTaskConfig> ite = HawkConfigManager.getInstance().getConfigIterator(PlayerComeBackAchieveTaskConfig.class);
		while(ite.hasNext()){
			PlayerComeBackAchieveTaskConfig config = ite.next();
			AchieveItem item = AchieveItem.valueOf(config.getAchieveId());
			entity.addItem(item);
		}
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new ComeBackLoginDayEvent(playerId, entity.getLoginDay()), true); //登录第一天
	}
	
	@Subscribe
	public void onContinutLoginEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<PlayerComeBackEntity> opEntity = getPlayerDataEntity(playerId);
		PlayerComeBackEntity entity = opEntity.get();
		if (event.isCrossDay() && !entity.isStartDay()) {
			entity.setLoginDay(entity.getLoginDay() + 1);
			ActivityManager.getInstance().postEvent(new ComeBackLoginDayEvent(playerId, entity.getLoginDay()), true);
		}
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId) && !isHidden(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isOpening(playerId) && !isHidden(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<PlayerComeBackEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		PlayerComeBackEntity entity = opEntity.get();
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		PlayerComeBackAchieveTaskConfig config = HawkConfigManager.getInstance().getConfigByKey(PlayerComeBackAchieveTaskConfig.class, achieveId);
		return config;
	}

	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		//打点任务
		this.getDataGeter().logComeBackAchieve(playerId, achieveItem.getAchieveId());
		return AchieveProvider.super.onAchieveFinished(playerId, achieveItem);
	}

	@Override
	public Action takeRewardAction() {
		return Action.COME_BACK_ACHIEVE_REWARD;
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PLAYER_COME_BACK_ACHIEVE;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ComeBackAchieveTaskActivity activity = new ComeBackAchieveTaskActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PlayerComeBackEntity> queryList = HawkDBManager.getInstance()
				.query("from PlayerComeBackEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PlayerComeBackEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PlayerComeBackEntity entity = new PlayerComeBackEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

	@Override
	public boolean isHidden(String playerId) {
		Optional<PlayerComeBackEntity> optional = getPlayerDataEntity(playerId);
		if(!optional.isPresent()){
			return true;
		}
		PlayerComeBackEntity entity = optional.get();
		if(!entity.isInit()){ //都没有初始化
			return true;
		}
		return super.isHidden(playerId);
	}
}
