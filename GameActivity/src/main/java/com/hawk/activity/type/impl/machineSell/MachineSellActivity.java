package com.hawk.activity.type.impl.machineSell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.MachineSellEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.machineSell.cfg.MachineSellAchieveCfg;
import com.hawk.activity.type.impl.machineSell.cfg.MachineSellKVCfg;
import com.hawk.activity.type.impl.machineSell.cfg.MachineSellRandomCellCfg;
import com.hawk.activity.type.impl.machineSell.entity.MachineSellEntity;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.game.protocol.Activity.HPMachineSellLotteryInfoNtf;
import com.hawk.game.protocol.Activity.HPMachineSellLotteryReq;
import com.hawk.game.protocol.Activity.HPMachineSellLotteryResp;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class MachineSellActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public MachineSellActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.MACHINE_SELL_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<MachineSellEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			//成就未空初始化成就
			if (opDataEntity.get().getItemList().isEmpty()) {
				initAchieveInfo(playerId);
			}
		}
	}

	Result<?> onLottery(String playerId, HPMachineSellLotteryReq msg) {
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		if (msg.getLotteryType() != 1 && msg.getLotteryType() != 2) {
			logger.error("machinesell_log on onLottery reqParam error, require 1/2 got :{} player: {}",
					msg.getLotteryType(), playerId);
			return null;
		}
		
		Optional<MachineSellEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		MachineSellEntity entity = opEntity.get();

		MachineSellKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MachineSellKVCfg.class);
		if (null == cfg) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		
		int lotteryTimes = 0;
		Action action = Action.MACHINE_SELL_LOTTERY_ONCE_COST;
		List<RewardItem.Builder> costItems = new ArrayList<RewardItem.Builder>();
		List<RewardItem.Builder> rewardList = new ArrayList<RewardItem.Builder>();
		
		if (msg.getLotteryType() == 1) {
			lotteryTimes = 1;
			if (cfg.getFree() > 0) {
				if (entity.getSingleTimes() >= cfg.getFree()) {
					costItems.addAll(cfg.getItemOncePriceItems());
				}
			}
			rewardList.addAll( cfg.getExtRewardItems() );
		} else if (msg.getLotteryType() == 2) {
			lotteryTimes = cfg.getFiveTimes();
			action = Action.MACHINE_SELL_LOTTERY_FIVE_COST;
			costItems.addAll(cfg.getItem5TimesPriceItems());
			rewardList.addAll( cfg.getExt5TimesRewardItems());
		}
		
		if(!costItems.isEmpty()){
			boolean flag = this.getDataGeter().cost(playerId, costItems, action);
			if (!flag) {
				//道具不足
				return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
			}		
		}
		
		HPMachineSellLotteryResp.Builder respB = HPMachineSellLotteryResp.newBuilder();
		respB.setLotteryType(msg.getLotteryType());
		List<Integer> lotteryIds = new ArrayList<Integer>();
		lottery( lotteryTimes, rewardList, lotteryIds);
		respB.addAllLotteryIds(lotteryIds);
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.MACHINE_SELL_LOTTERY_RESP, respB));
		
		//发物品给玩家
		this.getDataGeter().takeReward(playerId, rewardList, 1, Action.MACHINE_SELL_LOTTERY_REWARD, false, RewardOrginType.MACHINE_SELL_LOTTERY);
		//成就逻辑
		ActivityManager.getInstance().postEvent( MachineSellEvent.valueOf(playerId,lotteryTimes) );
		
		//增加次数 
		if(msg.getLotteryType() == 1){
			entity.incSingleTimes(lotteryTimes);
		}
		
		entity.incLotteryTimes(lotteryTimes);
		entity.notifyUpdate();
		
		return null;
	}
	
	private void lottery(int times, List<RewardItem.Builder> list, List<Integer> lotteryIds ){
		int totalWeight = MachineSellRandomCellCfg.getTotalWeight();
		for( int i = 0; i < times; i++ ){
			int curWeight = HawkRand.randInt(1, totalWeight);
			ConfigIterator<MachineSellRandomCellCfg> iter = HawkConfigManager.getInstance().getConfigIterator(MachineSellRandomCellCfg.class );
			while(iter.hasNext()){
				 MachineSellRandomCellCfg cfg =  iter.next();
				 if(cfg.getRate() < curWeight){
					curWeight -= cfg.getRate();
					continue;
				 }
				 list.addAll(cfg.getRewardList());
				 lotteryIds.add(cfg.getId());
				 break; 
			}
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, GameConst.MsgId.QUESTION_SHARE_INIT, () -> {
				Optional<MachineSellEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error(
							"machinesell_log on MachineSellActivity open init MachineSellEntity error, no entity created:"
									+ playerId);
				}
				callBack(playerId, MsgId.ACHIEVE_INIT_ACCUMULATE_RECHARGE, () -> {
					initAchieveInfo(playerId);
					this.syncActivityInfo(playerId, opEntity.get());
				});
			});
		}
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<MachineSellEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	public void syncActivityInfo(String playerId, MachineSellEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}

		HPMachineSellLotteryInfoNtf.Builder builder = HPMachineSellLotteryInfoNtf.newBuilder();

		builder.setTimes(entity.getLotteryTimes());
		builder.setSingleTimes(entity.getSingleTimes());

		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.MACHINE_SELL_LOTTERY_NTF, builder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MachineSellActivity activity = new MachineSellActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<MachineSellEntity> queryList = HawkDBManager.getInstance()
				.query("from MachineSellEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			MachineSellEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		MachineSellEntity entity = new MachineSellEntity(playerId, termId);
		// 刷新question
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
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<MachineSellEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		MachineSellEntity entity = opEntity.get();
		if (event.isCrossDay()) {
			MachineSellKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MachineSellKVCfg.class);
			if (null != cfg && cfg.isReset() == true) {
				// 清空单次抽奖次数
				entity.setSingleTimes(0);
				entity.notifyUpdate();
			}
			this.syncActivityDataInfo(playerId);
		}
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
		Optional<MachineSellEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		MachineSellEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public Action takeRewardAction() {
		return Action.MACHINE_SELL_BOX_REWARD;
	}

	@Override
	public MachineSellAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(MachineSellAchieveCfg.class, achieveId);
	}

	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<MachineSellEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		MachineSellEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<MachineSellAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(MachineSellAchieveCfg.class);
		while (configIterator.hasNext()) {
			MachineSellAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

}
