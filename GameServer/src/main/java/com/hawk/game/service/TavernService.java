package com.hawk.game.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.config.*;
import com.hawk.game.protocol.Const;
import com.hawk.game.util.GsConst;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.AddTavernScoreEvent;
import com.hawk.activity.event.impl.TavernScoreBoxFinishEvent;
import com.hawk.activity.extend.KeyValue;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.ordnanceFortress.OrdnanceFortressActivity;
import com.hawk.activity.type.impl.plantFortress.PlantFortressActivity;
import com.hawk.activity.type.impl.plantsecret.PlantSecretActivity;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.entity.TavernEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Tavern.FinishCountPB;
import com.hawk.game.protocol.Tavern.TavernInfoSync;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventDailyMissionBoxInit;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.util.TimeUtil;

/**
 * 酒馆逻辑 (每日任务)
 * @author PhilChen
 *
 */
public class TavernService implements AchieveProvider {

	static final Logger logger = LoggerFactory.getLogger("Server");

	private static TavernService service;
	
	

	private TavernService() {
	}

	public static TavernService getInstance() {
		if (service == null) {
			service = new TavernService();
			AchieveContext.registeProvider(service);
		}
		return service;
	}
	
	@Override
	public int providerActivityId() {
		return 0;
	}

	/**
	 * 同步酒馆信息
	 * @param player
	 */
	public void syncTavernInfo(Player player, TavernEntity tavernEntity) {
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.DAILY_TASK)) {
			return;
		}
		
		TavernInfoSync.Builder builder = TavernInfoSync.newBuilder();
		long refreshTime = getNextRefreshTime(player);
		builder.setRefreshTime(refreshTime);
		Map<Integer, Integer> achieveFinishMap = tavernEntity.getAchieveFinishMap();
		for (Entry<Integer, Integer> entry : achieveFinishMap.entrySet()) {
			FinishCountPB.Builder countPB = FinishCountPB.newBuilder();
			countPB.setAchieveId(entry.getKey());
			countPB.setFinishCount(entry.getValue());
			builder.addCount(countPB);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TAVERN_INFO_SYNC_S_VALUE, builder));
	}
	
	/**
	 * 获取酒馆剩余刷新时间
	 * @param player
	 * @return
	 */
	private long getNextRefreshTime(Player player) {
		int tavernRefreshTime = ConstProperty.getInstance().getTavernRefreshTime();
		Calendar calendar = HawkTime.getCalendar(true);
		calendar.set(Calendar.HOUR_OF_DAY, tavernRefreshTime);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long resetTime = calendar.getTimeInMillis();
		long now = HawkTime.getMillisecond();
		if (now >= resetTime) {
			// 今天的重置时间已过，下一次重置时间是明天
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			resetTime = calendar.getTimeInMillis();
		}
		return resetTime;
		
	}

	/**
	 * 尝试刷新酒馆
	 * @param player
	 */
	public void refreshTavernInfo(Player player, boolean isLogin) {
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.DAILY_TASK)) {
			return;
		}
		
		boolean update = false;
		TavernEntity tavernEntity = player.getData().getTavernEntity();
		List<AchieveItem> achieveItemList = tavernEntity.getAchieveItemList();
		if (isLogin) {
			// 登录时检测是否存在成就项的配置更新
			ConfigIterator<TavernAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(TavernAchieveCfg.class);
			Map<Object, TavernAchieveCfg> map = configIterator.toMap();
			
			update = checkAchieveConfig(player.getId(), achieveItemList, map);
			if (update) {
				tavernEntity.notifyUpdate();
			}
		}
		long now = HawkTime.getMillisecond();
		// 检查是否到了刷新时间
		if (TimeUtil.isNeedReset(ConstProperty.getInstance().getTavernRefreshTime(), now, tavernEntity.getLastRefreshTime()) == false) {
			return;
		}
		
		// 重置积分成就
		for (AchieveItem achieveItem : tavernEntity.getScoreItemList()) {
			achieveItem.reset();
		}
		// 刷新酒馆，重置酒馆成就数据
		for (AchieveItem achieveItem : achieveItemList) {
			achieveItem.reset();
		}
		tavernEntity.getAchieveFinishMap().clear();
		tavernEntity.setLastRefreshTime(now);
		// 向玩家同步数据
		syncTavernInfo(player, tavernEntity);
		
		List<AchieveItem> pushList = new ArrayList<>();
		pushList.addAll(achieveItemList);
		pushList.addAll(tavernEntity.getScoreItemList());
		AchievePushHelper.pushAchieveUpdate(player.getId(), pushList);
		
		logger.info("refreshTavernInfo, playerId:{}, isLogin:{}", player.getId(), isLogin);
	}

	

	@Override
	public boolean isProviderActive(String playerId) {
		return !SystemControler.getInstance().isSystemItemsClosed(ControlerModule.DAILY_TASK);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !SystemControler.getInstance().isSystemItemsClosed(ControlerModule.DAILY_TASK);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		TavernEntity tavernEntity = player.getData().getTavernEntity();
		List<AchieveItem> list = new ArrayList<AchieveItem>(tavernEntity.getAchieveItemList());
		list.addAll(tavernEntity.getScoreItemList());
		AchieveItems items = new AchieveItems(list, tavernEntity);
		return Optional.ofNullable(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(TavernAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(TavernScoreCfg.class, achieveId);

		}
		return config;
	}
	
	@Override
	public List<Builder> getRewardList(String playerId, AchieveConfig achieveConfig) {
		if (achieveConfig instanceof TavernScoreCfg) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			int factoryLevel = player.getCityLevel();
			TavernScoreCfg config = (TavernScoreCfg) achieveConfig;
			TavernRewardBoxCfg boxConfig = HawkConfigManager.getInstance().getCombineConfig(TavernRewardBoxCfg.class, config.getBoxId(), factoryLevel);
			if (boxConfig == null) {
				logger.error("[tavern] box reward id not exist. playerId:{} boxId:{} factoryLevel:{}", playerId, config.getBoxId(), factoryLevel);
				return Collections.emptyList();
			}
			List<RewardItem.Builder> list = new ArrayList<>();
			String rewards = boxConfig.getRewards();
			List<RewardItem.Builder> boxRewardList = RewardHelper.toRewardItemImmutableList(rewards);
			int eff639 = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_639);
			double eff639Per = eff639 * GsConst.EFF_PER + 1;
			boxRewardList.forEach(item->{
				item.setItemCount((long)(item.getItemCount() * eff639Per));
			});

			list.addAll(boxRewardList);
			list.addAll(getExtRewardList(playerId, achieveConfig.getAchieveId()));
			return list;
		}
		return Collections.emptyList();
	}
	
	public  List<RewardItem.Builder> getExtRewardList(String playerId,int achieveId){
		List<RewardItem.Builder> list = new ArrayList<>();
		Optional<ActivityBase> opActivity  = ActivityManager.getInstance().
				getActivity(ActivityType.ORDNANCE_FORTRESS_VALUE);
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		//终生卡额外奖励
		int eff639 = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_639);
		double eff639Per = eff639 * GsConst.EFF_PER + 1;
		if (opActivity.isPresent()) {
			ActivityBase activity = opActivity.get();
			if (activity.isOpening(playerId)) {
				OrdnanceFortressActivity act = (OrdnanceFortressActivity) activity;
				List<RewardItem.Builder> ordnanceFortressList = act.getDailyBoxExtRewards(achieveId);
				ordnanceFortressList.forEach(item ->{
					item.setItemCount((long)(item.getItemCount() * eff639Per));
				});
				if(ordnanceFortressList!= null && !ordnanceFortressList.isEmpty()){
					list.addAll(ordnanceFortressList);
				}
			}
		}
		
		Optional<ActivityBase> opPlantActivity  = ActivityManager.getInstance().
				getActivity(ActivityType.PLANT_FORTRESS_VALUE);
		if (opPlantActivity.isPresent()) {
			ActivityBase activity = opPlantActivity.get();
			if (activity.isOpening(playerId)) {
				PlantFortressActivity act = (PlantFortressActivity) activity;
				List<RewardItem.Builder> plantFortressList = act.getDailyBoxExtRewards(achieveId);
				if(plantFortressList!= null && !plantFortressList.isEmpty()){
					list.addAll(plantFortressList);
				}
			}
		}
		
		Optional<ActivityBase> plantSecretOption  = ActivityManager.getInstance().
				getActivity(ActivityType.PLANT_SECRET_VALUE);
		if (plantSecretOption.isPresent()) {
			ActivityBase activity = plantSecretOption.get();
			if (activity.isOpening(playerId)) {
				PlantSecretActivity act = (PlantSecretActivity) activity;
				List<RewardItem.Builder> rewardList = act.getDailyBoxExtRewards(achieveId);
				rewardList.forEach(item ->{
					item.setItemCount((long)(item.getItemCount() * eff639Per));
				});
				if(rewardList!= null && !rewardList.isEmpty()){
					list.addAll(rewardList);
				}
			}
		}
		
		return list;
	}

	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.DAILY_TASK)) {
			return Result.fail(Status.SysError.DAILY_TASK_SYSTEM_CLOSED_VALUE);
		}
		
		TavernAchieveCfg config = HawkConfigManager.getInstance().getConfigByKey(TavernAchieveCfg.class, achieveItem.getAchieveId());
		if (config == null) {
			// 积分类成就不做处理
			TavernScoreCfg scoreconfig = HawkConfigManager.getInstance().getConfigByKey(TavernScoreCfg.class, achieveItem.getAchieveId());
			if(scoreconfig != null){
				ActivityManager.getInstance().postEvent(new TavernScoreBoxFinishEvent(playerId, scoreconfig.getAchieveId(), scoreconfig.getConditionValue(0)));
			}
			return Result.success();
		}
		// 增加完成次数
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		TavernEntity tavernEntity = player.getData().getTavernEntity();
		int finishCount = tavernEntity.getFinishCount(config.getAchieveId());
		if (finishCount >= config.getCount()) {
			return Result.success();
		}
		int addCount = 1;
		int remain = -1;
		if (config.getAchieveType() == AchieveType.TAVERN_MAKE_TRAP_COUNT 
				|| config.getAchieveType() == AchieveType.TAVERN_RESOURCE_COLLECT 
				|| config.getAchieveType() == AchieveType.TAVERN_USE_RESOURCE_TOOL_TIMES 
				|| config.getAchieveType() == AchieveType.TAVERN_TRAIN_TANK_TYPE_NUMBER
				|| config.getAchieveType() == AchieveType.USE_HERO_EXPITEM
				|| config.getAchieveType() == AchieveType.TREAT_ARMY
				|| config.getAchieveType() == AchieveType.GUILD_HELP
				|| config.getAchieveType() == AchieveType.MATERIAL_COMPOSITION_TIMES
				|| config.getAchieveType() == AchieveType.MONSTER_KILL_COUNT
				|| config.getAchieveType() == AchieveType.GACHA) {
			int configShowValue = config.getAchieveType().getAchieveData().getConfigShowValue(config);
			int showValue = config.getAchieveType().getAchieveData().getShowValue(achieveItem);
			addCount = Math.max(1, showValue / configShowValue);
			remain = showValue % configShowValue;
			if (finishCount + addCount > config.getCount()) {
				addCount = config.getCount() - finishCount;
			}
		}
		
		int beforeScore = getTotalScore(tavernEntity);
		
		int count = tavernEntity.addFinishCount(config.getAchieveId(), addCount);
		if (count < config.getCount()) {
			// 重置成就数据
			achieveItem.reset();
			if (remain > 0) {
				achieveItem.setValue(0, remain);
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("[tavern] playerId:{} finish tavern achieve achieveId:{} count:{}", playerId, achieveItem.getAchieveId(), count);
		}
		
		// 增加积分
		int addScore = config.getScore() * addCount;
		
		int afterScore = getTotalScore(tavernEntity);
		
		ActivityManager.getInstance().postEvent(new AddTavernScoreEvent(playerId, addScore, afterScore));
		
		// 同步次数信息
		FinishCountPB.Builder countPB = FinishCountPB.newBuilder();
		countPB.setAchieveId(config.getAchieveId());
		countPB.setFinishCount(count);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TAVERN_COUNT_SYNC_S_VALUE, countPB));
		
		
		LogUtil.logDailyActiveScoreChange(player, beforeScore, afterScore, addScore, config.getAchieveType().getValue());
		
		return Result.success();
	}
	
	/**
	 * 获取每日任务总积分
	 * 
	 * @return
	 */
	public int getTotalScore(TavernEntity tavernEntity) {
		Map<Integer, Integer> achieveFinishMap = tavernEntity.getAchieveFinishMap();
		int totalScore = 0;
		for (Entry<Integer, Integer> entry : achieveFinishMap.entrySet()) {
			TavernAchieveCfg config = HawkConfigManager.getInstance().getConfigByKey(TavernAchieveCfg.class, entry.getKey());
			if (config == null) {
				continue;
			}
			
			totalScore += config.getScore() * entry.getValue();
		}
		
		return totalScore;
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Action takeRewardAction() {
		return Action.DAILY_MISSION_BONUS;
	}

	/**
	 * 获取酒馆当前积分
	 * @param playerId
	 * @return
	 */
	public int getTavernBoxScore(String playerId) {
		try {
			//其它模块调用时,检测酒馆当日是否刷新过,没刷新过直接0
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			TavernEntity tavernEntity = player.getData().getTavernEntity();
			long lastFreshTime = tavernEntity.getLastRefreshTime();
			long nowTime = HawkTime.getMillisecond();
			boolean isSameDay = HawkTime.isSameDay(lastFreshTime, nowTime);
			if (!isSameDay) {
				return 0;
			}
			int configSize = HawkConfigManager.getInstance().getConfigSize(TavernScoreCfg.class);
			TavernScoreCfg config = HawkConfigManager.getInstance().getConfigByIndex(TavernScoreCfg.class, configSize - 1);
			int achieveId = config.getAchieveId();
			
			KeyValue<AchieveConfig, AchieveProvider> achieve = AchieveManager.getInstance().getAchieveConfigAndProvider(achieveId);
			if (achieve == null) {
				return 0;
			}
			
			KeyValue<AchieveItem, AchieveItems> keyValue = AchieveManager.getInstance().getActiveAchieveItem(playerId, achieveId);
			if (keyValue == null) {
				return 0;
			}
			
			AchieveItem achieveItem = keyValue.getKey();
			return achieveItem.getValue(0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return 0;
	}
	
	public void onTakeRewardSuccess(String playerId, int conditionType) {
		if (conditionType == AchieveType.TAVERN_SCORE.getValue()) {
			RedisProxy.getInstance().dailyMissionBox(playerId);
			MissionManager.getInstance().postMsg(playerId, new EventDailyMissionBoxInit());
		}
	}
	
	/**
	 * 获取总的活跃积分
	 * @param player
	 * @return
	 */
	public int getTotalScore(Player player) {
		long now = HawkTime.getMillisecond();
		TavernEntity tavernEntity = player.getData().getTavernEntity();
		int score = 0;
		if (HawkTime.isSameDay(tavernEntity.getLastRefreshTime(), now)) {
			score = TavernService.getInstance().getTotalScore(tavernEntity);
		}
		
		return score;
	}
	
}
