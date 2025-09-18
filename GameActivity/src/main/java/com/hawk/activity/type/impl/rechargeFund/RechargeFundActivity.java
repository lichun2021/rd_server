package com.hawk.activity.type.impl.rechargeFund;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.rechargeFund.cfg.RechargeFundKVCfg;
import com.hawk.activity.type.impl.rechargeFund.cfg.RechargeFundLevelCfg;
import com.hawk.activity.type.impl.rechargeFund.cfg.RechargeFundRewardCfg;
import com.hawk.activity.type.impl.rechargeFund.entity.RechargeFundEntity;
import com.hawk.game.protocol.Activity.RFAwardInfo;
import com.hawk.game.protocol.Activity.RFAwardState;
import com.hawk.game.protocol.Activity.RFInvestInfo;
import com.hawk.game.protocol.Activity.RFInvestState;
import com.hawk.game.protocol.Activity.RFPageInfo;
import com.hawk.game.protocol.Activity.RFSelectAwardReq;
import com.hawk.game.protocol.Activity.RFStage;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class RechargeFundActivity extends ActivityBase {

	public RechargeFundActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	private RFStage stage = RFStage.RF_INVEST;

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RECHARGE_FUND;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		syncActivityDataInfo(playerId);
	}

	@Override
	public void onOpen() {
		stage = RFStage.RF_INVEST;
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.RECHARGE_FUND_INIT, () -> {
				this.syncActivityDataInfo(playerId);
			});
		}
	}

	@Override
	public void onEnd() {
		String serverId = this.getDataGeter().getServerId();
		String recordKey = getInvestPlayerKeys(serverId);
		Set<String> playerIds = ActivityGlobalRedis.getInstance().sMembers(recordKey);
		long startTime = getTimeControl().getStartTimeByTermId(getActivityTermId());
		long endTime = getTimeControl().getEndTimeByTermId(getActivityTermId());
		Long serverMergeTime = getDataGeter().getServerMergeTime();
		if (serverMergeTime == null) {
			serverMergeTime = 0l;
		}
		List<String> slaveServerList = getDataGeter().getSlaveServerList();
		// 本期活动开启之后合并的服务器,需要加载从服的投资人员列表
		if (!slaveServerList.isEmpty() && serverMergeTime >= startTime && serverMergeTime <= endTime) {
			for (String followServerId : slaveServerList) {
				String followRecordKey = getInvestPlayerKeys(followServerId);
				Set<String> followPlayerIds = ActivityGlobalRedis.getInstance().sMembers(followRecordKey);
				if (!followPlayerIds.isEmpty()) {
					playerIds.addAll(followPlayerIds);
				}
			}
		}
		if (playerIds.isEmpty()) {
			HawkLog.logPrintln("RechargeFundActivity noPlayerInvest, termId: {}", getActivityTermId());
			return;
		}
		Map<String, String> rewardDataMap = new HashMap<>();
		Map<String, RechargeFundData> dataMap = new HashMap<>();
		for (String playerId : playerIds) {
			Optional<RechargeFundEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				HawkLog.logPrintln("RechargeFundActivity endAwardError, entity is null, playerId: {}", playerId);
				continue;
			}
			RechargeFundEntity dataEntity = opEntity.get();
			RechargeFundData rewardData = calcRewardData(dataEntity);
			if (rewardData == null) {
				HawkLog.logPrintln("RechargeFundActivity totalTook, playerId: {}", playerId);
				continue;
			}
			rewardDataMap.put(rewardData.getPlayerId(), JSON.toJSONString(rewardData));
			dataMap.put(playerId, rewardData);
		}
		for (String playerId : playerIds) {
			final RechargeFundData rewardData = dataMap.get(playerId);
			if (rewardData == null) {
				continue;
			}
			callBack(playerId, MsgId.RECHARGE_FUND_INIT, () -> {
				sendReward(rewardData);
			});
		}

	}

	/**
	 * 补发未领取的奖励
	 * 
	 * @param rewardData
	 */
	private void sendReward(RechargeFundData rewardData) {
		String playerId = rewardData.getPlayerId();
		Map<Integer, List<String>> rewardMap = rewardData.getRewardMap();
		for (Entry<Integer, List<String>> entry : rewardMap.entrySet()) {
			int giftId = entry.getKey();
			List<String> rewardList = entry.getValue();
			if (rewardList == null || rewardList.isEmpty()) {
				continue;
			}
			List<RewardItem.Builder> itemList = new ArrayList<>();
			for (String rewardStr : rewardList) {
				HawkLog.logPrintln("sendReward reward:{}", rewardStr);
				List<RewardItem.Builder> rewardBuilders = RewardHelper.toRewardItemList(rewardStr);
				itemList.addAll(rewardBuilders);
			}
			MailId mailId = MailId.RECHARGE_FUND_REWARD;
			// 邮件发送奖励
			Object[] content;
			content = new Object[1];
			content[0] = giftId;
			sendMailToPlayer(playerId, mailId, new Object[0], new Object[0], content, itemList);
			HawkLog.logPrintln("RechargeFundActivity sendReward success, playerId: {}, termId: {}, giftId: {}, reward: {}", playerId, rewardData.getTermId(), giftId, rewardList);
		}

	}

	/**
	 * 计算待补发奖励信息
	 * 
	 * @param dataEntity
	 * @return
	 */
	private RechargeFundData calcRewardData(RechargeFundEntity dataEntity) {
		if (dataEntity == null) {
			return null;
		}
		try {
			Map<Integer, String> diyMap = dataEntity.getDiyMap();

			List<Integer> investList = dataEntity.getInvestList();

			List<Integer> rewardedList = dataEntity.getRewardedList();
			if (dataEntity.getInvestList().isEmpty()) {
				return null;
			}
			Map<Integer, List<String>> rewardMap = new HashMap<>();
			for (int giftId : investList) {
				List<String> rewards = new ArrayList<>();
				ConfigIterator<RechargeFundRewardCfg> its = HawkConfigManager.getInstance().getConfigIterator(RechargeFundRewardCfg.class);
				List<RechargeFundRewardCfg> cfgList = its.stream().filter(recfg -> recfg.getGiftId() == giftId).collect(Collectors.toList());
				for (RechargeFundRewardCfg cfg : cfgList) {
					int rewardId = cfg.getId();
					// 已领奖
					if (rewardedList.contains(rewardId)) {
						continue;
					}
					rewards.add(cfg.getGoldBar());
					rewards.add(cfg.getFixedRewards());
					rewards.add(diyMap.get(rewardId));
				}
				if (rewards.isEmpty()) {
					continue;
				}
				rewardMap.put(giftId, rewards);

			}
			if (rewardMap.isEmpty()) {
				return null;
			}
			RechargeFundData data = new RechargeFundData();
			data.setPlayerId(dataEntity.getPlayerId());
			data.setRewardMap(rewardMap);
			data.setTermId(dataEntity.getTermId());
			return data;
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("RechargeFundActivity calcRewardData error, playerId:{}, termId:{}, invest:{}, rewarded:{}", dataEntity.getPlayerId(), dataEntity.getTermId(),
					dataEntity.getInvestInfo(), dataEntity.getRewardedInfo());
		}
		return null;
	}

	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		// 在线跨天时同步消息
		if (!event.isLogin()) {
			String playerId = event.getPlayerId();
			syncActivityDataInfo(playerId);
		}
	}

	@Override
	public boolean isActivityClose(String playerId) {
		Optional<RechargeFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		RechargeFundEntity dataEntity = opEntity.get();
		RFStage stage = getRFStage();
		// 收益阶段,为投资的玩家,活动关闭显示
		if (stage == RFStage.RF_REWARD && dataEntity.getInvestList().isEmpty()) {
			return true;
		}
		return false;
	}

	@Subscribe
	public void onEvent(DiamondRechargeEvent event) {
		String playerId = event.getPlayerId();
		Optional<RechargeFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		// 非投资阶段充值不累计额度
		if (getRFStage() != RFStage.RF_INVEST) {
			return;
		}

		RechargeFundEntity dataEntity = opEntity.get();
		int rechargeBef = dataEntity.getRechargeNum();
		dataEntity.setRechargeNum(dataEntity.getRechargeNum() + event.getDiamondNum());
		ConfigIterator<RechargeFundLevelCfg> its = HawkConfigManager.getInstance().getConfigIterator(RechargeFundLevelCfg.class);
		int unlockNum = (int) its.stream().filter(cfg -> cfg.getUnlockRecharge() <= dataEntity.getRechargeNum()).count();
		getDataGeter().logRechargeFundRecharge(playerId, getActivityTermId(), event.getDiamondNum(), rechargeBef, dataEntity.getRechargeNum(), unlockNum);
		syncActivityDataInfo(playerId);
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RechargeFundActivity activity = new RechargeFundActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RechargeFundEntity> queryList = HawkDBManager.getInstance().query("from RechargeFundEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RechargeFundEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RechargeFundEntity entity = new RechargeFundEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	/**
	 * 同步活动内容数据
	 * 
	 * @param playerId
	 */
	public void syncActivityDataInfo(String playerId) {
		if (this.isOpening(playerId)) {
			RFPageInfo.Builder builder = genActivityInfo(playerId);
			if (builder != null) {
				PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RECHARGE_FUND_INFO_SYNC, builder));
			}
		}
	}

	/**
	 * 构建活动信息
	 * 
	 * @param playerId
	 * @return
	 */
	public RFPageInfo.Builder genActivityInfo(String playerId) {
		Optional<RechargeFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		int termId = getActivityTermId();
		long startTime = getTimeControl().getStartTimeByTermId(termId);
		long investTime = HawkConfigManager.getInstance().getKVInstance(RechargeFundKVCfg.class).getBuyTime();
		RFStage stage = getRFStage();
		RechargeFundEntity entity = opEntity.get();
		RFPageInfo.Builder builder = RFPageInfo.newBuilder();
		int rechargeNum = entity.getRechargeNum();
		builder.setRechargeNum(rechargeNum);
		builder.setRfStage(stage);
		builder.setInvestEndTime(startTime + investTime);
		int investedDays = 0;
		if (stage == RFStage.RF_REWARD) {
			investedDays = getInvestedDays();
		}
		builder.setCurrDay(investedDays);

		// 投资信息
		ConfigIterator<RechargeFundLevelCfg> its = HawkConfigManager.getInstance().getConfigIterator(RechargeFundLevelCfg.class);
		for (RechargeFundLevelCfg cfg : its) {
			RFInvestInfo.Builder investBuilder = genInvestInfo(entity, cfg);
			builder.addInvestInfo(investBuilder);
		}

		return builder;
	}

	/**
	 * 自选奖励内容
	 * 
	 * @param playerId
	 * @param req
	 * @return
	 */
	public int onDiySelect(String playerId, RFSelectAwardReq req) {
		int giftId = req.getGiftId();
		Optional<RechargeFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return -1;
		}
		RechargeFundEntity dataEntity = opEntity.get();
		RechargeFundLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RechargeFundLevelCfg.class, giftId);
		if (cfg == null) {
			return -1;
		}
		RFStage stage = getRFStage();
		// 不是投资阶段
		if (stage != RFStage.RF_INVEST) {
			return Status.Error.RF_STAGE_NOT_INVEST_VALUE;
		}
		// 已经投资,不可变更
		if (dataEntity.getInvestList().contains(giftId)) {
			return Status.Error.RF_STATE_INVESTED_VALUE;
		}
		List<RFAwardInfo> rewardList = req.getAwardInfoList();
		Map<Integer, String> selectMap = new HashMap<>();
		for (RFAwardInfo info : rewardList) {
			int awardId = info.getAwardId();
			String award = info.getItemStr(0);
			RechargeFundRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(RechargeFundRewardCfg.class, awardId);
			if (rewardCfg == null) {
				return Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE;
			}
			if (giftId != rewardCfg.getGiftId()) {
				return Status.Error.RF_GIFT_ID_ERROR_VALUE;
			}
			if (!rewardCfg.canSelect(award)) {
				return -1;
			}
			selectMap.put(awardId, award);
		}
		ConfigIterator<RechargeFundRewardCfg> its = HawkConfigManager.getInstance().getConfigIterator(RechargeFundRewardCfg.class);
		long cfgCnt = its.stream().filter(recfg -> recfg.getGiftId() == giftId).count();
		// 自选奖励数量不符
		if (selectMap.size() != cfgCnt) {
			return Status.Error.RF_DIY_NOT_ENOUGH_VALUE;
		}
		dataEntity.modifyDiyMap(selectMap);

		RFInvestInfo.Builder builder = genInvestInfo(dataEntity, cfg);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RECHARGE_FUND_SELECT_AWARD_RESP_VALUE, builder));
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 投资
	 * 
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public int doInvest(String playerId, int giftId) {
		Optional<RechargeFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return -1;
		}
		RechargeFundEntity dataEntity = opEntity.get();
		RechargeFundLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RechargeFundLevelCfg.class, giftId);
		if (cfg == null) {
			return Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE;
		}
		RFStage stage = getRFStage();
		// 不是投资阶段
		if (stage != RFStage.RF_INVEST) {
			return Status.Error.RF_STAGE_NOT_INVEST_VALUE;
		}
		// 未解锁
		if (dataEntity.getRechargeNum() < cfg.getUnlockRecharge()) {
			return Status.Error.RF_STATE_UNLOCK_VALUE;
		}
		// 已经投资
		if (dataEntity.getInvestList().contains(giftId)) {
			return Status.Error.RF_STATE_INVESTED_VALUE;
		}
		// 自选奖励不全
		ConfigIterator<RechargeFundRewardCfg> its = HawkConfigManager.getInstance().getConfigIterator(RechargeFundRewardCfg.class);
		for (RechargeFundRewardCfg rewardCfg : its) {
			if (rewardCfg.getGiftId() != giftId) {
				continue;
			}
			int rewardId = rewardCfg.getId();
			if (!dataEntity.getDiyMap().containsKey(rewardId)) {
				return Status.Error.RF_DIY_NOT_ENOUGH_VALUE;
			}
		}
		List<RewardItem.Builder> costList = cfg.getCostList();
		boolean consumeResult = getDataGeter().consumeItems(playerId, costList, HP.code.RECHARGE_FUND_INVEST_REQ_VALUE, Action.RECHARGE_FUND_INVEST);
		if (consumeResult == false) {
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}
		dataEntity.addInvestId(giftId);
		String serverId = this.getDataGeter().getServerId();
		String recordKey = getInvestPlayerKeys(serverId);
		// 记录投资人的id
		ActivityGlobalRedis.getInstance().sAdd(recordKey, getExpireSeconds(), playerId);

		getDataGeter().logRechargeFundInvest(playerId, getActivityTermId(), giftId);

		RFInvestInfo.Builder builder = genInvestInfo(dataEntity, cfg);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RECHARGE_FUND_INVEST_RESP_VALUE, builder));
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 领取奖励
	 * 
	 * @param playerId
	 * @param awardId
	 * @return
	 */
	public int getReward(String playerId, int awardId) {
		Optional<RechargeFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.RF_DATA_ERROR_VALUE;
		}
		RechargeFundEntity dataEntity = opEntity.get();
		RechargeFundRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(RechargeFundRewardCfg.class, awardId);
		if (rewardCfg == null) {
			return Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE;
		}
		RFStage stage = getRFStage();
		// 不是收益阶段
		if (stage != RFStage.RF_REWARD) {
			return Status.Error.RF_STAGE_NOT_REWARD_VALUE;
		}
		int giftId = rewardCfg.getGiftId();
		int investedDays = getInvestedDays();
		// 登录天数不足
		if (investedDays < rewardCfg.getLoginDay()) {
			return Status.Error.RF_DAYS_NOT_ENOUGH_VALUE;
		}
		// 没有投资
		if (!dataEntity.getInvestList().contains(giftId)) {
			return Status.Error.RF_STATE_UNINVEST_VALUE;
		}
		// 已领取
		if (dataEntity.getRewardedList().contains(awardId)) {
			return Status.Error.RF_REWARDED_VALUE;
		}

		RechargeFundLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RechargeFundLevelCfg.class, giftId);
		if (cfg == null) {
			return Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE;
		}
		dataEntity.addRewardedId(awardId);

		List<RewardItem.Builder> itemList = new ArrayList<>();
		RewardItem.Builder diyBuilder = RewardHelper.toRewardItem(dataEntity.getDiyMap().get(awardId));
		if (diyBuilder != null) {
			itemList.add(diyBuilder);
		}
		itemList.addAll(rewardCfg.getGoldBarList());
		itemList.addAll(rewardCfg.getFixedList());
		ActivityReward reward = new ActivityReward(itemList, Action.RECHARGE_FUND_REWARD);
		reward.setAlert(true);
		postReward(playerId, reward);

		getDataGeter().logRechargeFundReward(playerId, getActivityTermId(), giftId, awardId);

		RFInvestInfo.Builder builder = genInvestInfo(dataEntity, cfg);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RECHARGE_FUND_GET_AWARD_RESP_VALUE, builder));
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 构建投资信息
	 * 
	 * @param dataEntity
	 * @param cfg
	 * @return
	 */
	private RFInvestInfo.Builder genInvestInfo(RechargeFundEntity dataEntity, RechargeFundLevelCfg cfg) {
		List<Integer> investList = dataEntity.getInvestList();
		RFInvestInfo.Builder investBuilder = RFInvestInfo.newBuilder();
		int giftId = cfg.getGiftId();
		int limitNum = cfg.getUnlockRecharge();
		investBuilder.setGiftId(giftId);
		RFInvestState investState;
		if (investList.contains(giftId)) {
			investState = RFInvestState.RF_INVESTED;
		} else {
			if (dataEntity.getRechargeNum() >= limitNum) {
				investState = RFInvestState.RF_UNINVEST;
			} else {
				investState = RFInvestState.RF_UNLOCK;
			}
		}
		investBuilder.setInvestState(investState);
		ConfigIterator<RechargeFundRewardCfg> rewardIts = HawkConfigManager.getInstance().getConfigIterator(RechargeFundRewardCfg.class);
		for (RechargeFundRewardCfg rewardCfg : rewardIts) {
			if (rewardCfg.getGiftId() != giftId) {
				continue;
			}
			RFAwardInfo.Builder awardBuilder = genAwardInfo(dataEntity, rewardCfg);
			investBuilder.addAwardInfo(awardBuilder);
		}
		return investBuilder;
	}

	/**
	 * 构建奖励信息
	 * 
	 * @param dataEntity
	 * @param cfg
	 * @return
	 */
	private RFAwardInfo.Builder genAwardInfo(RechargeFundEntity dataEntity, RechargeFundRewardCfg cfg) {
		int investedDays = getInvestedDays();
		int awardId = cfg.getId();
		RFAwardInfo.Builder awardBuilder = RFAwardInfo.newBuilder();
		awardBuilder.setAwardId(awardId);
		if (dataEntity.getDiyMap().containsKey(awardId)) {
			awardBuilder.addItemStr(dataEntity.getDiyMap().get(awardId));
		}
		RFAwardState awardState;
		if (dataEntity.getInvestList().contains(cfg.getGiftId())) {
			if (dataEntity.getRewardedList().contains(awardId)) {
				awardState = RFAwardState.RF_TOOK;
			} else {
				if (investedDays >= cfg.getLoginDay()) {
					awardState = RFAwardState.RF_NOT_AWARD;
				} else {
					awardState = RFAwardState.RF_NOT_ACHIEVE;
				}
			}
		} else {
			awardState = RFAwardState.RF_NOT_ACHIEVE;
		}
		awardBuilder.setState(awardState);
		return awardBuilder;
	}

	/**
	 * 获取当前阶段
	 * 
	 * @return
	 */
	private RFStage getRFStage() {
		int termId = getActivityTermId();
		long currTime = HawkTime.getMillisecond();
		long startTime = getTimeControl().getStartTimeByTermId(termId);
		long investTime = HawkConfigManager.getInstance().getKVInstance(RechargeFundKVCfg.class).getBuyTime();
		if (currTime < startTime + investTime) {
			return RFStage.RF_INVEST;
		} else {
			return RFStage.RF_REWARD;

		}
	}

	/**
	 * 获取收益期已过天数
	 * 
	 * @return
	 */
	private int getInvestedDays() {
		int termId = getActivityTermId();
		long startTime = getTimeControl().getStartTimeByTermId(termId);
		long investTime = HawkConfigManager.getInstance().getKVInstance(RechargeFundKVCfg.class).getBuyTime();
		long currTime = HawkTime.getMillisecond();
		if (currTime < startTime + investTime) {
			return 0;
		}
		int crossHour = getDataGeter().getCrossDayHour();
		int betweenDays = HawkTime.getCrossDay(currTime, startTime + investTime, crossHour);
		return betweenDays + 1;
	}

	/**
	 * 参与投资玩家列表
	 * 
	 * @return
	 */
	private String getInvestPlayerKeys(String serverId) {
		int termId = getActivityTermId();
		return "activity_rf_invest:" + serverId + ":" + termId;
	}

	/**
	 * 记录有效期
	 * 
	 * @return
	 */
	private int getExpireSeconds() {
		int termId = getActivityTermId();
		ITimeController timeInfo = getTimeControl();
		long startTime = timeInfo.getStartTimeByTermId(termId);
		long hiddenTime = timeInfo.getHiddenTimeByTermId(termId);
		return (int) ((hiddenTime - startTime) / 1000 + 7 * 24 * 3600);
	}

	@Override
	public void onTick() {
		if (getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		RFStage newStage = getRFStage();
		if (stage != newStage) {
			stage = newStage;
			Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
			for (String playerId : onlinePlayerIds) {
				callBack(playerId, MsgId.RECHARGE_FUND_STAGE_CHANGE, () -> {
					// 同步活动状态,未投资的玩家活动不显示
					PlayerPushHelper.getInstance().syncActivityStateInfo(playerId, this);
				});
			}
		}
	}
	
}
