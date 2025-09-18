package com.hawk.activity.type.impl.lotteryDraw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LotteryDrawEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.lotteryDraw.cfg.LotteryDrawAchieveCfg;
import com.hawk.activity.type.impl.lotteryDraw.cfg.LotteryDrawActivityKVCfg;
import com.hawk.activity.type.impl.lotteryDraw.cfg.LotteryDrawCellCfg;
import com.hawk.activity.type.impl.lotteryDraw.cfg.LotteryDrawFirstTenCfg;
import com.hawk.activity.type.impl.lotteryDraw.cfg.LotteryDrawMultiStageCfg;
import com.hawk.activity.type.impl.lotteryDraw.cfg.LotteryDrawMultiWeightCfg;
import com.hawk.activity.type.impl.lotteryDraw.entity.LotteryDrawEntity;
import com.hawk.game.protocol.Activity.DoLotteryDrawResp;
import com.hawk.game.protocol.Activity.DrawResult;
import com.hawk.game.protocol.Activity.LotteryDrawPageInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LotteryDrawType;

/**
 * 十连抽活动
 * 
 * @author admin
 */
public class LotteryDrawActivity extends ActivityBase implements AchieveProvider {

	public LotteryDrawActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.LOTTERY_DRAW_ACTIVITY;
	}

	@Override
	public Action takeRewardAction() {
		return Action.ACTIVITY_REWARD_LOTTERY_DRAW;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LotteryDrawActivity activity = new LotteryDrawActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<LotteryDrawEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		LotteryDrawEntity dataEntity = opDataEntity.get();
		LotteryDrawPageInfo.Builder builder = genPageInfo(dataEntity);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.LOTTERY_DRAW_PAGE_INFO_SYNC_S_VALUE, builder));
	}

	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<LotteryDrawEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		LotteryDrawEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<LotteryDrawAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(LotteryDrawAchieveCfg.class);
		while (configIterator.hasNext()) {
			LotteryDrawAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<LotteryDrawEntity> queryList = HawkDBManager.getInstance()
				.query("from LotteryDrawEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			LotteryDrawEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		LotteryDrawCellCfg cfg = HawkConfigManager.getInstance().getConfigByIndex(LotteryDrawCellCfg.class, 0);
		LotteryDrawEntity entity = new LotteryDrawEntity(playerId, termId, cfg.getId());
		return entity;
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_LOTTERY_DRAW, () -> {
				initAchieveInfo(playerId);
			});
		}
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

	/**
	 * 跨天时推送界面信息
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		syncActivityDataInfo(event.getPlayerId());
	}

	public void onGetPageInfo(String playerId) {
		Optional<LotteryDrawEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.DO_LOTTERY_DRAW_C_VALUE,
					Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			return;
		}
		LotteryDrawEntity dataEntity = opDataEntity.get();
		genPageInfo(dataEntity);
		PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.GET_LOTTERY_DRAW_PAGE_INFO_S_VALUE,
				Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
	}

	private LotteryDrawPageInfo.Builder genPageInfo(LotteryDrawEntity dataEntity) {
		LotteryDrawPageInfo.Builder builder = LotteryDrawPageInfo.newBuilder();
		builder.setCellId(dataEntity.getLastCellId());
		builder.setCanFree(!HawkTime.isSameDay(dataEntity.getLastFreeDrawTime(), HawkTime.getMillisecond()));
		builder.setNextFreeTime(HawkTime.getNextAM0Date());
		builder.setNextEnsureTimes(10 - dataEntity.getEnsureTimes());

		LotteryDrawMultiStageCfg stageCfg = getMultiStageCfg(dataEntity);
		builder.setMultiStage(stageCfg.getId());
		builder.setMultiPer(stageCfg.getRate());
		int stageSize = HawkConfigManager.getInstance().getConfigIterator(LotteryDrawMultiStageCfg.class).size();
		builder.setTotalStage(stageSize);
		builder.setIsMulti(dataEntity.isMulti());
		builder.setTotalCnt(dataEntity.getTotalTimes());
		return builder;
	}

	/**
	 * 抽奖
	 * 
	 * @param playerId
	 * @param isTenTimes
	 */
	public void onDoLotteryDraw(String playerId, boolean isTenTimes) {
		DoLotteryDrawResp.Builder builder = DoLotteryDrawResp.newBuilder();
		LotteryDrawActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LotteryDrawActivityKVCfg.class);
		Optional<LotteryDrawEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.DO_LOTTERY_DRAW_C_VALUE,
					Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			return;
		}
		LotteryDrawEntity dataEntity = opDataEntity.get();
		int drawType = 0;
		boolean isMulti = dataEntity.isMulti();
		boolean isFree = (!isTenTimes)
				&& !HawkTime.isSameDay(dataEntity.getLastFreeDrawTime(), HawkTime.getMillisecond());
		if (!isFree) {
			RewardItem.Builder cost = null;
			if (isTenTimes) {
				cost = kvCfg.getTenConsume();
				drawType = LotteryDrawType.LOTTERY_DRAW_TYPE_TEN.intVal();
			} else {
				cost = kvCfg.getSingleConsume();
				drawType = LotteryDrawType.LOTTERY_DRAW_TYPE_SINGLE.intVal();
			}
			int itemCnt = getDataGeter().getItemNum(playerId, cost.getItemId());
			if (itemCnt < 0) {
				return;
			}
			List<RewardItem.Builder> consumeList = new ArrayList<>();
			if (itemCnt >= cost.getItemCount()) {
				consumeList.add(cost);
			} else {
				int needBuyCnt = (int) (cost.getItemCount() - itemCnt);
				RewardItem.Builder price = kvCfg.getItemConsume();
				price.setItemCount(price.getItemCount() * needBuyCnt);
				consumeList.add(price);
				if (itemCnt > 0) {
					cost.setItemCount(itemCnt);
					consumeList.add(cost);
				}
			}

			boolean consumeResult = getDataGeter().consumeItems(playerId, consumeList, HP.code.DO_LOTTERY_DRAW_S_VALUE,
					Action.ACTIVITY_REWARD_LOTTERY_DRAW);
			if (consumeResult == false) {
				return;
			}
		} else {
			dataEntity.setLastFreeDrawTime(HawkTime.getMillisecond());
			drawType = LotteryDrawType.LOTTERY_DRAW_TYPE_FREE.intVal();
		}
		// 是不是首次进行十连抽
		boolean isFirstTen = isTenTimes && dataEntity.getTenDrawTimes() == 0;
		Map<Integer, LotteryDrawFirstTenCfg> firstMap = new HashMap<>();
		if (isFirstTen) {
			firstMap = getFirstTenDrawCfg();
			// 首次十连抽如果有单独配置,则按配置抽奖.若无单独配置,则按正常抽奖流程抽奖
			isFirstTen = firstMap.size() > 0;
		}

		// 保底奖励id
		Map<LotteryDrawCellCfg, Integer> cellWeightMap = getCellWeightMap(isTenTimes);
		List<HawkTuple2<Integer, Integer>> resultList = new ArrayList<>();

		// 是否触发保底,
		boolean isEnsure = false;
		if (!isTenTimes) {
			if (dataEntity.getEnsureTimes() + 1 >= 10) {
				dataEntity.setEnsureTimes(0);
				isEnsure = true;
			} else {
				dataEntity.setEnsureTimes(dataEntity.getEnsureTimes() + 1);
			}
			int cellId = randCell(cellWeightMap, isEnsure);
			int multi = 1;
			if (dataEntity.isMulti()) {
				multi = randMultiNum();
			}
			resultList.add(new HawkTuple2<Integer, Integer>(cellId, multi));
			refreshMultiData(dataEntity);
			dataEntity.setTotalTimes(dataEntity.getTotalTimes() + 1);
		} else {
			boolean hasChoose = false;
			List<Integer> noEnsureIds = kvCfg.getNoEnsureIdList();
			for (int i = 1; i <= 10; i++) {
				// 判定之前的抽取有未抽中保底奖励
				for (HawkTuple2<Integer, Integer> tuple : resultList) {
					if (noEnsureIds.contains(tuple.first)) {
						hasChoose = true;
						break;
					}
				}
				// 非首次十连抽,且前九次未抽中保底奖励,则会进行保底
				isEnsure = !hasChoose && i == 10 && !isFirstTen;
				if (isFirstTen && firstMap.containsKey(i)) {
					LotteryDrawFirstTenCfg cfg = firstMap.get(i);
					int cellId = cfg.getCellId();
					int multi = cfg.getMulti();
					if (multi > 1) {
						dataEntity.setMultiLucky(1);
					} else {
						dataEntity.setMultiLucky(dataEntity.getMultiLucky() + 1);
					}
					resultList.add(new HawkTuple2<Integer, Integer>(cellId, multi));
					refreshMultiData(dataEntity);
				} else {
					int cellId = randCell(cellWeightMap, isEnsure);
					int multi = 1;
					if (dataEntity.isMulti()) {
						multi = randMultiNum();
					}
					resultList.add(new HawkTuple2<Integer, Integer>(cellId, multi));
					// 刷新多倍数据
					refreshMultiData(dataEntity);
				}
			}
			dataEntity.setTenDrawTimes(dataEntity.getTenDrawTimes() + 1);
			dataEntity.setTotalTimes(dataEntity.getTotalTimes() + 10);
		}
		builder.setPageInfo(genPageInfo(dataEntity));
		ActivityManager.getInstance().postEvent(new LotteryDrawEvent(playerId, resultList.size()), true);

		List<RewardItem.Builder> totalList = new ArrayList<>();
		StringBuilder resultBuilder = new StringBuilder();
		StringBuilder mulitBuilder = new StringBuilder();
		for (HawkTuple2<Integer, Integer> tuple : resultList) {
			DrawResult.Builder result = DrawResult.newBuilder();
			result.setCellId(tuple.first);
			result.setMulti(tuple.second);
			builder.addResult(result);
			LotteryDrawCellCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LotteryDrawCellCfg.class,
					tuple.first);
			List<RewardItem.Builder> rewards = cfg.getRewardList();
			for (RewardItem.Builder reward : rewards) {
				reward.setItemCount(reward.getItemCount() * tuple.second);
				totalList.add(reward);
			}
			resultBuilder.append(tuple.first).append(",");
			mulitBuilder.append(tuple.second).append(",");
		}
		dataEntity.setLastCellId(resultList.get(resultList.size() - 1).first);

		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.DO_LOTTERY_DRAW_S_VALUE, builder));

		// 添加额外奖励
		int extCnt = isTenTimes ? 9 : 1;
		List<RewardItem.Builder> extRewards = kvCfg.getExtRewardItems();
		if (!extRewards.isEmpty()) {
			for (RewardItem.Builder extReward : extRewards) {
				extReward.setItemCount(extReward.getItemCount() * extCnt);
				totalList.add(extReward);
			}
		}

		ActivityReward reward = new ActivityReward(totalList, Action.ACTIVITY_REWARD_LOTTERY_DRAW);
		reward.setAlert(false);
		postReward(playerId, reward);
		resultBuilder.deleteCharAt(resultBuilder.length() - 1);
		mulitBuilder.deleteCharAt(mulitBuilder.length() - 1);
		getDataGeter().lotteryDrawRecord(playerId, drawType, dataEntity.getMultiLucky(), isMulti, dataEntity.isMulti(),
				resultBuilder.toString(), mulitBuilder.toString());
	}

	/**
	 * 刷新多倍数据
	 * 
	 * @param dataEntity
	 */
	private void refreshMultiData(LotteryDrawEntity dataEntity) {
		boolean isMulti = randMulti(dataEntity);
		if (isMulti) {
			dataEntity.setMultiLucky(1);
		} else {
			dataEntity.setMultiLucky(dataEntity.getMultiLucky() + 1);
		}
		dataEntity.setMulti(isMulti);
	}

	/**
	 * 随机是否触发多倍buff
	 * 
	 * @param dataEntity
	 * @return
	 */
	private boolean randMulti(LotteryDrawEntity dataEntity) {
		LotteryDrawMultiStageCfg stageCfg = getMultiStageCfg(dataEntity);
		int rate = stageCfg.getRate();
		return HawkRand.randInt(10000) <= rate;
	}

	/**
	 * 随机多倍倍率
	 * 
	 * @return
	 */
	private int randMultiNum() {
		Map<LotteryDrawMultiWeightCfg, Integer> multiWeightMap = getMultiWeightMap();
		LotteryDrawMultiWeightCfg cfg = HawkRand.randomWeightObject(multiWeightMap);
		return cfg.getMulti();
	}

	/**
	 * 随机奖励格子
	 * 
	 * @param weightMap
	 * @param isEnsure
	 *            是否触发奖励
	 * @return
	 */
	public int randCell(Map<LotteryDrawCellCfg, Integer> weightMap, boolean isEnsure) {
		if (isEnsure) {
			return HawkConfigManager.getInstance().getKVInstance(LotteryDrawActivityKVCfg.class).getEnsureCellId();
		}
		LotteryDrawCellCfg cfg = HawkRand.randomWeightObject(weightMap);
		return cfg.getId();
	}

	/**
	 * 获取抽奖权重map
	 * 
	 * @param isTenDraw
	 * @return
	 */
	private Map<LotteryDrawCellCfg, Integer> getCellWeightMap(boolean isTenTimes) {
		Map<LotteryDrawCellCfg, Integer> cellWeightMap = new HashMap<>();
		ConfigIterator<LotteryDrawCellCfg> its = HawkConfigManager.getInstance()
				.getConfigIterator(LotteryDrawCellCfg.class);
		for (LotteryDrawCellCfg cfg : its) {
			if (isTenTimes) {
				cellWeightMap.put(cfg, cfg.getTenWeight());
			} else {
				cellWeightMap.put(cfg, cfg.getSingleWeight());
			}
		}
		return cellWeightMap;
	}

	/**
	 * 获取首次十连抽配置
	 * 
	 * @return
	 */
	private Map<Integer, LotteryDrawFirstTenCfg> getFirstTenDrawCfg() {
		Map<Integer, LotteryDrawFirstTenCfg> map = new HashMap<>();
		ConfigIterator<LotteryDrawFirstTenCfg> its = HawkConfigManager.getInstance()
				.getConfigIterator(LotteryDrawFirstTenCfg.class);
		for (LotteryDrawFirstTenCfg cfg : its) {
			map.put(cfg.getTimes(), cfg);
		}
		return map;
	}

	/**
	 * 获取当前翻倍阶段配置
	 * 
	 * @param entity
	 * @return
	 */
	private LotteryDrawMultiStageCfg getMultiStageCfg(LotteryDrawEntity entity) {
		int lucky = entity.getMultiLucky();
		ConfigIterator<LotteryDrawMultiStageCfg> its = HawkConfigManager.getInstance()
				.getConfigIterator(LotteryDrawMultiStageCfg.class);
		for (LotteryDrawMultiStageCfg cfg : its) {
			if (lucky >= cfg.getMin() && lucky <= cfg.getMax()) {
				return cfg;
			}
		}
		return null;
	}

	/**
	 * 获取多倍奖励权重map
	 * 
	 * @return
	 */
	private Map<LotteryDrawMultiWeightCfg, Integer> getMultiWeightMap() {
		Map<LotteryDrawMultiWeightCfg, Integer> map = new HashMap<>();
		ConfigIterator<LotteryDrawMultiWeightCfg> its = HawkConfigManager.getInstance()
				.getConfigIterator(LotteryDrawMultiWeightCfg.class);
		for (LotteryDrawMultiWeightCfg cfg : its) {
			map.put(cfg, cfg.getWeight());
		}
		return map;
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
		Optional<LotteryDrawEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		LotteryDrawEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(LotteryDrawAchieveCfg.class, achieveId);
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(),
				achieveId);
		return Result.success();
	}
}
