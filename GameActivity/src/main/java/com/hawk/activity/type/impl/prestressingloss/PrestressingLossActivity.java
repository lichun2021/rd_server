package com.hawk.activity.type.impl.prestressingloss;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayPrestressLossEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.prestressingloss.cfg.PrestressingLossAchieveCfg;
import com.hawk.activity.type.impl.prestressingloss.cfg.PrestressingLossKVCfg;
import com.hawk.activity.type.impl.prestressingloss.entity.PrestressingLossEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.PrestressingLossActivityInfo;
import com.hawk.game.protocol.Activity.ActivityPB.Builder;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 预流失（干预）活动
 * 
 * @author lating
 *
 */
public class PrestressingLossActivity extends ActivityBase implements AchieveProvider {

	public PrestressingLossActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.PRESTRESSING_LOSS_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.PRESTRESSING_LOSS_ACTIVITY_REWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PrestressingLossActivity activity = new PrestressingLossActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PrestressingLossEntity> queryList = HawkDBManager.getInstance()
				.query("from PrestressingLossEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PrestressingLossEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PrestressingLossEntity entity = new PrestressingLossEntity(playerId, termId);
		return entity;
	}
	
	public boolean isActivityClose(String playerId) {
		return !checkActivityOpen(playerId);
	}
	
	public void onTick(String playerId) {
		Optional<PrestressingLossEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		
		long now = HawkApp.getInstance().getCurrentTime();
		PrestressingLossEntity entity = opEntity.get();
		if (now - entity.getTickTime() < 10000) {
			return;
		}
		
		entity.resetTickTime(now);
		boolean activityOpen = entity.isActivityOpen();
		boolean newState = checkActivityOpen(playerId);
		if (activityOpen && !newState) { // 活动到期关闭
			entity.resetActivityOpen(false);
			Builder builder = PlayerPushHelper.getInstance().buildActivityHiddenPB(this);
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PUSH_ACTIVITY_INFO_VALUE, builder));
		} else if (!activityOpen && newState) { // 新开活动
			entity.resetActivityOpen(true);
			PlayerPushHelper.getInstance().syncActivityStateInfo(playerId, this);
			syncActivityDataInfo(playerId);
			
			List<AchieveItem> itemList = new ArrayList<>();
			Optional<AchieveItems> opAchieveItems = this.getAchieveItems(playerId);
			if(!opAchieveItems.isPresent()){
				return;
			}
			AchieveItems achieveItems = opAchieveItems.get();
			itemList.addAll(achieveItems.getItems());
			if(!itemList.isEmpty()){
				AchievePushHelper.pushAchieveInfo(playerId, itemList);
			}
			
			PrestressingLossKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PrestressingLossKVCfg.class);
			PrestressingLossActivityInfo.Builder builder = PrestressingLossActivityInfo.newBuilder();
			builder.setEndTime(entity.getOpenTime() + kvCfg.getCircleTime());
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.PRESTRESSING_LOSS_SYNC, builder));
			if (entity.getLoginDays() == 0) {
				entity.setLoginDays(1);
				entity.setLoginTime(now);
			}
		}
	}
	
	/**
	 * 判断活动是否开启
	 * @param playerId
	 * @return
	 */
	private boolean checkActivityOpen(String playerId) {
		PrestressingLossKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PrestressingLossKVCfg.class);
		long now = HawkTime.getMillisecond();
		long playerRegTime = getDataGeter().getPlayerCreateTime(playerId);
		if (now - playerRegTime < cfg.getRegisterTime()) {
			return false;
		}
		
		Optional<PrestressingLossEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return false;
		}
		PrestressingLossEntity entity = opEntity.get();
		if (entity.getOpenTime() == 0) {
			// 根据接口请求结果判断
			return checkActivityOpen(entity);
		}
		
		// 还在一期活动期限范围内
		if (now - entity.getOpenTime() < cfg.getCircleTime()) {
			return true;
		}
		
		if (entity.getCoolTimeVal() == 0) {
			int coolTimeSecond = cfg.getCoolTime() + HawkRand.randInt(0, cfg.getCoolingTimeFloat());
			entity.setCoolTimeVal(coolTimeSecond * 1000L);
		}
		
		// 冷却时间还没结束
		if (now - entity.getOpenTime() < entity.getCoolTimeVal()) {
			return false;
		}
		
		if (entity.getVacancyTimeVal() == 0) {
			int vacancyTimeSecond = cfg.getVacancyTime() + HawkRand.randInt(0, cfg.getVacancyPeriodFloat());
			entity.setVacancyTimeVal(vacancyTimeSecond * 1000L);
		}

		// 过了空置期
		if (now - entity.getOpenTime() > entity.getVacancyTimeVal()) {
			resetActivityData(entity);
			return true;
		}
		
		// 过了冷却期，没到空置期，根据接口请求结果判断
		return checkActivityOpen(entity);
	}
	
	/**
	 * 请求外部tx接口获取数据，根据数据判断这个玩家是否可以开启活动
	 * @param entity
	 * @return
	 */
	private boolean checkActivityOpen(PrestressingLossEntity entity) {
		String playerId = entity.getPlayerId();
		if (HawkApp.getInstance().getCurrentTime() - entity.getRequestDataTime() < 60000) {
			return false;
		}
		
		entity.resetRequestDataTime(HawkApp.getInstance().getCurrentTime());
		if (!getDataGeter().checkPrestressinLossActivityOpen(playerId)) {
			return false;
		}
		
		resetActivityData(entity);
		return true;
	}
	
	/**
	 * 重置活动相关数据
	 * 
	 * @param entity
	 */
	private void resetActivityData(PrestressingLossEntity entity) {
		entity.getItemList().clear();
		entity.setLoginTime(0);
		entity.setLoginDays(0);
		entity.setCoolTimeVal(0);
		entity.setVacancyTimeVal(0);
		entity.setOpenTime(HawkTime.getAM0Date().getTime());
		entity.setOpenTerm(entity.getOpenTerm() + 1);
		getDataGeter().logPrestressingLoss(entity.getPlayerId(), entity.getOpenTerm());
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<PrestressingLossEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		PrestressingLossEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		int vipLevel = getDataGeter().getVipLevel(playerId);
		int cityLevel = getDataGeter().getConstructionFactoryLevel(playerId);
		List<AchieveItem> items = new ArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<PrestressingLossAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PrestressingLossAchieveCfg.class);
		while (configIterator.hasNext()) {
			PrestressingLossAchieveCfg cfg = configIterator.next();
			if (cfg.getStartCityLv() > 0 || cfg.getEndCityLv() > 0) {
				if (cfg.getStartCityLv() > cityLevel || cfg.getEndCityLv() < cityLevel) {
					continue;
				}
			}
			
			if (cfg.getStartVipLevel() > 0 || cfg.getEndVipLevel() > 0) {
				if (cfg.getStartVipLevel() > vipLevel || cfg.getEndVipLevel() < vipLevel) {
					continue;
				}
			}
			
			if (cfg.getAchieveType() == AchieveType.LOGIN_DAYS_PRESTRESS_LOSS) {
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				items.add(item);
				continue;
			}
			
			if (cfg.getLossDay() != 1) {
				continue;
			}
			
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			items.add(item);
		}
		
		entity.resetItemList(items);
		entity.notifyUpdate();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<PrestressingLossEntity> opEntity = getPlayerDataEntity(playerId);
		PrestressingLossEntity entity = opEntity.get();
		
		PrestressingLossKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PrestressingLossKVCfg.class);
		PrestressingLossActivityInfo.Builder builder = PrestressingLossActivityInfo.newBuilder();
		builder.setEndTime(entity.getOpenTime() + kvCfg.getCircleTime());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.PRESTRESSING_LOSS_SYNC, builder));
		
		if (!event.isCrossDay() && !HawkTime.isCrossDay(
				HawkTime.getMillisecond(), entity.getLoginTime(), 0)) {
			return;
		}
		
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		
		entity.setLoginDays(entity.getLoginDays() + 1);
		entity.setLoginTime(HawkTime.getMillisecond());
		ActivityManager.getInstance().postEvent(new LoginDayPrestressLossEvent(playerId, entity.getLoginDays()), true);
		
		if (entity.getLoginDays() == 1) {
			return;
		}
		
		// 重置成就任务数据
		List<AchieveItem> newItems = new ArrayList<AchieveItem>();
		List<AchieveItem> deleteItems = new ArrayList<AchieveItem>();
		List<AchieveItem> addItems = new ArrayList<AchieveItem>();
		
		for (AchieveItem item : entity.getItemList()) {
			PrestressingLossAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(
					PrestressingLossAchieveCfg.class, item.getAchieveId());
			if (cfg.getAchieveType() == AchieveType.LOGIN_DAYS_PRESTRESS_LOSS) {
				newItems.add(item);
			} else {
				deleteItems.add(item);
			}
		}
		
		boolean empty = newItems.isEmpty();
		int vipLevel = getDataGeter().getVipLevel(playerId);
		int cityLevel = getDataGeter().getConstructionFactoryLevel(playerId);
		// 初始添加成就项
		ConfigIterator<PrestressingLossAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PrestressingLossAchieveCfg.class);
		while (configIterator.hasNext()) {
			PrestressingLossAchieveCfg cfg = configIterator.next();
			if (cfg.getAchieveType() == AchieveType.LOGIN_DAYS_PRESTRESS_LOSS) {
				if (!empty) {
					continue;
				}
			} else {
				if (cfg.getLossDay() != entity.getLoginDays()) {
					continue;
				}
			}
			
			
			if (cfg.getStartCityLv() > 0 || cfg.getEndCityLv() > 0) {
				if (cfg.getStartCityLv() > cityLevel || cfg.getEndCityLv() < cityLevel) {
					continue;
				}
			}
			
			if (cfg.getStartVipLevel() > 0 || cfg.getEndVipLevel() > 0) {
				if (cfg.getStartVipLevel() > vipLevel || cfg.getEndVipLevel() < vipLevel) {
					continue;
				}
			}
			
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			newItems.add(item);
			addItems.add(item);
		}
		
		if (newItems.isEmpty()) {
			return;
		}
		
		entity.resetItemList(newItems);
		entity.notifyUpdate();
		if (!deleteItems.isEmpty()) {
			AchievePushHelper.pushAchieveDelete(playerId, deleteItems);			
		}
		
		if (!addItems.isEmpty()) {
			AchievePushHelper.pushAchieveAdd(playerId, addItems);
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
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
		if (!isOpening(playerId)) {
			return Optional.empty();
		}
		
		Optional<PrestressingLossEntity> entityOp = getPlayerDataEntity(playerId);
		if(!entityOp.isPresent()){
			return Optional.empty();
		}
		PrestressingLossEntity entity = entityOp.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(PrestressingLossAchieveCfg.class, achieveId);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}
}
