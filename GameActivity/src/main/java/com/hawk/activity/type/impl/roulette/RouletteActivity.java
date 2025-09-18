package com.hawk.activity.type.impl.roulette;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DoRouletteEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.roulette.cfg.RouletteKVCfg;
import com.hawk.activity.type.impl.roulette.cfg.RouletteRewardBoxCfg;
import com.hawk.activity.type.impl.roulette.cfg.RouletteRewardRateCfg;
import com.hawk.activity.type.impl.roulette.entity.RouletteEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.HPRouletteActivityInfoSync;
import com.hawk.game.protocol.Activity.HPRouletteActivityItemSetReq;
import com.hawk.game.protocol.Activity.HPRouletteActivityLotteryReq;
import com.hawk.game.protocol.Activity.HPRouletteActivityLotteryResp;
import com.hawk.game.protocol.Activity.PBRouletteActivityItemInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

public class RouletteActivity extends ActivityBase {

	public RouletteActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ROULETTE_ACTIVITY;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<RouletteEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ROULETTE_INIT, () -> {
				this.syncActivityDataInfo(playerId);
			});
		}
	}

	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<RouletteEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		// 在线跨天
		if (event.isCrossDay()) {
			long startTime = getTimeControl().getStartTimeByTermId(getActivityTermId(), playerId);
			// 活动开启当天跨天,不重置免费次数
			if (HawkTime.isSameDay(startTime, HawkTime.getMillisecond())) {
				return;
			}
			opEntity.get().setFreeTimes(1);
			syncActivityInfo(playerId, opEntity.get());
			return;
		}
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RouletteActivity activity = new RouletteActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RouletteEntity> queryList = HawkDBManager.getInstance()
				.query("from RouletteEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RouletteEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RouletteEntity entity = new RouletteEntity(playerId, termId);
		entity.getItemSetMap().clear();
		entity.setScore(0);
		entity.setFreeTimes(1);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	private void syncActivityInfo(String playerId, RouletteEntity entity) {
		HPRouletteActivityInfoSync.Builder builder = HPRouletteActivityInfoSync.newBuilder();
		for (Map.Entry<Integer, String> entry : entity.getItemSetMap().entrySet()) {
			PBRouletteActivityItemInfo.Builder infoBuilder = PBRouletteActivityItemInfo.newBuilder();
			infoBuilder.setCfgId(entry.getKey());
			infoBuilder.setItemStr(entry.getValue());
			builder.addItemSet(infoBuilder);
		}
		builder.setScore(entity.getScore());

		HawkTuple2<Integer, Integer> curBoxReward =  entity.getCurBoxReward();
		
		builder.setCurBoxId(curBoxReward.first);
		builder.setCurBoxTimes(curBoxReward.second);
		builder.setFreeTimes(entity.getFreeTimes());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.HP_ROULETTE_INFO_SYNC_S, builder));
	}

	/**
	 * 同步活动内容数据
	 * 
	 * @param playerId
	 */
	public void syncActivityDataInfo(String playerId) {

		if (this.isOpening(playerId)) {
			Optional<RouletteEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			RouletteEntity entity = opEntity.get();
			this.syncActivityInfo(playerId, entity);
		}
	}

	public Result<?> onProtocolSetRewardItem(String playerId, HPRouletteActivityItemSetReq req) {
		try {
			if (req.getItemSetCount() <= 0 || req.getItemSetCount() > 4) {
				return Result.fail(Status.Error.ROULETTE_ACTIVITY_PARAM_VALUE);
			}
			// 验证参数
			for (PBRouletteActivityItemInfo iter : req.getItemSetList()) {
				RouletteRewardRateCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RouletteRewardRateCfg.class,
						iter.getCfgId());
				//配置表错误
				if (null == cfg || !cfg.isCanSelected()) {
					return Result.fail(Status.Error.ROULETTE_ACTIVITY_PARAM_VALUE);
				}
				//参数错误
				if( !cfg.isInRewardList(iter.getItemStr())){
					return Result.fail(Status.Error.ROULETTE_ACTIVITY_SET_VAL_ERR_VALUE);
				}
			}

			Optional<RouletteEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return Result.fail(Status.Error.ROULETTE_ACTIVITY_DATA_ERROR_VALUE);
			}
			RouletteEntity entity = opEntity.get();
			// 设置
			for (PBRouletteActivityItemInfo iter : req.getItemSetList()) {
				entity.setItemSetMap(iter.getCfgId(), iter.getItemStr());
			}
			entity.notifyUpdate();

			// 返回客户端
			this.syncActivityInfo(playerId, entity);

		} catch (Exception e) {
			HawkException.catchException(e);
			return Result.fail(Status.Error.ROULETTE_ACTIVITY_UNKNOW_VALUE);
		}
		return Result.success();
	}

	public Result<?> onProtocolRewardBox(String playerId) {
		try {
			Optional<RouletteEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return Result.fail(Status.Error.ROULETTE_ACTIVITY_DATA_ERROR_VALUE);
			}
			RouletteEntity entity = opEntity.get();
			
			HawkTuple2<Integer, Integer> curBoxReward =  entity.getCurBoxReward();
			// 配置表
			RouletteRewardBoxCfg boxCfg = HawkConfigManager.getInstance().getConfigByKey(RouletteRewardBoxCfg.class, curBoxReward.first);
			if (null == boxCfg) {
				return Result.fail(Status.Error.ROULETTE_ACTIVITY_CFG_ERR_VALUE);
			}
			// 达到积分
			if (entity.getScore() < boxCfg.getScore()) {
				return Result.fail(Status.Error.ROULETTE_ACTIVITY_BOX_SCORE_VALUE);
			}

			// 发奖励
			this.getDataGeter().takeReward(playerId, boxCfg.getRewardList(), 1, Action.ROULETTE_BOX_REWARD,
					true, RewardOrginType.ROULTTE_ACC_BOX);

			//扣积分
			entity.setScore(entity.getScore() - boxCfg.getScore());
			//加次数
			entity.setCurBoxTimes( curBoxReward.first, curBoxReward.second + 1 );
			if(boxCfg.getTimes() != 0){
				//该阶段是否领完
				if(boxCfg.getTimes() <= curBoxReward.second + 1){
					entity.setCurBoxTimes(curBoxReward.first + 1, 0);
				}
			}
			
			entity.notifyUpdate();

			// 返回客户端
			this.syncActivityInfo(playerId, entity);
			
			//打点
			this.getDataGeter().logRouletteActivityRewardBox(playerId, this.getActivityTermId());

		} catch (Exception e) {
			HawkException.catchException(e);
			return Result.fail(Status.Error.ROULETTE_ACTIVITY_UNKNOW_VALUE);
		}
		return Result.success();
	}

	public Result<?> onProtocolLottery(String playerId, HPRouletteActivityLotteryReq req) {
		try {
			//有一个没设置或者设置不正确的时候不能抽奖
			int count = 0;
			int score = 0;
			if (req.getLotteryType().equals(Activity.RouletteLotteryType.ONE)) {
				count = 1;
			} else if (req.getLotteryType().equals(Activity.RouletteLotteryType.TEN)) {
				count = 10;
			} else {
				return Result.fail(Status.Error.ROULETTE_ACTIVITY_PARAM_VALUE);
			}
			
			
			Optional<RouletteEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return Result.fail(Status.Error.ROULETTE_ACTIVITY_DATA_ERROR_VALUE);
			}
			RouletteEntity entity = opEntity.get();
			
			//获取设置
			if(entity.getItemSetMap().size() != RouletteRewardRateCfg.getCanSetMaxCount()){
				return Result.fail(Status.Error.ROULETTE_ACTIVITY_SET_EMPTY_VALUE);
			}
			//四个设置是否都匹配并且都是在范围内
			for(Map.Entry<Integer, String> entry : entity.getItemSetMap().entrySet()){
				RouletteRewardRateCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(RouletteRewardRateCfg.class, entry.getKey());
				if(null == rewardCfg ){
					return Result.fail(Status.Error.ROULETTE_ACTIVITY_SET_ID_ERR_VALUE);
				}
				//Integer selectItemId = entity.getItemSetMap().get(entry.getKey());
				
				if(!rewardCfg.isInRewardList(entry.getValue())){
					return Result.fail(Status.Error.ROULETTE_ACTIVITY_SET_VAL_ERR_VALUE);
				}
			}
			// 配置表
			RouletteKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(RouletteKVCfg.class);
			if (null == kvCfg) {
				return Result.fail(Status.Error.ROULETTE_ACTIVITY_CFG_ERR_VALUE);
			}
			score = count * kvCfg.getLuckyValue();
			// 判断消耗够不够
			//int costItemId = kvCfg.getCostItemId();
			int oneceCostItemNum = kvCfg.getCostOneNum();
			
			int needCount = count;
			//免费次数只在抽一次的时候消耗
			if(req.getLotteryType().equals(Activity.RouletteLotteryType.ONE)){
				if(entity.getFreeTimes() == 1){
					needCount -= 1;
				}
			}
			// 总共需要消耗
			int needItemNum = oneceCostItemNum * needCount;
			// 自己拥有的
			int haveItemNum = this.getDataGeter().getItemNum(playerId, kvCfg.getCostItemId());
			// 自己能消耗的
			int costItemNum = haveItemNum >= needItemNum ? needItemNum : haveItemNum;
			// 需要购买的
			int needBuyItemNum = needItemNum - costItemNum;

			List<RewardItem.Builder> costItemList = new ArrayList<RewardItem.Builder>();
			//自己已有的钥匙消耗
			if (costItemNum > 0) {
				RewardItem.Builder builder = RewardHelper.toRewardItem(kvCfg.getItemOnce());
				builder.setItemCount(costItemNum);
				costItemList.add(builder);
			}
			//购买消耗
			if (needBuyItemNum > 0) {
				RewardItem.Builder builder = RewardItem.newBuilder();
				builder.setItemId(kvCfg.getItemOnecePriceList().get(0).getItemId());
				builder.setItemType(kvCfg.getItemOnecePriceList().get(0).getItemType());
				builder.setItemCount(kvCfg.getItemOnecePriceList().get(0).getItemCount() * needBuyItemNum);
				costItemList.add(builder);
			}

			// 先随机
			List<RouletteRewardRateCfg> randomList = lottery(count);
			if(randomList.size() != count){
				return Result.fail(Status.Error.ROULETTE_ACTIVITY_RANDOM_ERR_VALUE);
			}
			// 奖励
			List<RewardItem.Builder> rewardList = new ArrayList<RewardItem.Builder>();
			// 检查随机到的奖励
			for(RouletteRewardRateCfg rewardCfg : randomList){
				if(rewardCfg.isCanSelected()){
					String itemStr = entity.getItemSetMap().get(rewardCfg.getId());
					
					RewardItem.Builder itemBuilder = rewardCfg.getRewardByStr(itemStr);
					
					if(null == itemBuilder){
						return Result.fail(Status.Error.ROULETTE_ACTIVITY_SET_VAL_ERR_VALUE);
					}
					rewardList.add(itemBuilder);
				}else{
					rewardList.addAll(rewardCfg.getRewardList());
				}
			}
	
			// 随机，判断道具
			if(null != costItemList && !costItemList.isEmpty()){
				boolean flag = this.getDataGeter().cost(playerId, costItemList, 1, Action.ROULETTE_LOTTERY_COST, false);
				if (!flag) {
					return Result.fail(Status.Error.ROULETTE_ACTIVITY_NO_KEY_VALUE);
				}
			}

			// 发固定
			this.getDataGeter().takeReward(playerId, kvCfg.getExtRewardList(), count, Action.ROULETTE_LOTTERY_REWARD, false);
			// 发奖励
			this.getDataGeter().takeReward(playerId, rewardList, 1, Action.ROULETTE_LOTTERY_REWARD,
					false, RewardOrginType.ACTIVITY_REWARD);
			
			// 给积分
			entity.setScore(score + entity.getScore());
			
			//免费次数只在抽一次的时候消耗
			if(req.getLotteryType().equals(Activity.RouletteLotteryType.ONE)){
				if(entity.getFreeTimes() > 0){
					entity.setFreeTimes(0);
				}			
			}

			entity.notifyUpdate();
			
			ActivityManager.getInstance().postEvent(new DoRouletteEvent(playerId, count));
			// 返回客户端
			
			HPRouletteActivityLotteryResp.Builder lotteryResp = HPRouletteActivityLotteryResp.newBuilder();
			lotteryResp.setLotteryType( req.getLotteryType() );
			lotteryResp.setCfgId(randomList.get(randomList.size() - 1).getId());
			for(RewardItem.Builder builder : rewardList){
				lotteryResp.addRewards(builder);
			}
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.HP_ROULETTE_LOTTERY_RESP_S, lotteryResp));
			this.syncActivityInfo(playerId, entity);
			String itemSet = SerializeHelper.mapToString(entity.getItemSetMap(),SerializeHelper.COLON_ITEMS,SerializeHelper.BETWEEN_ITEMS);
			//打日志
			this.getDataGeter().logRouletteActivityLottery(playerId, this.getActivityTermId(), count, needBuyItemNum,itemSet);

		} catch (Exception e) {
			HawkException.catchException(e);
			return Result.fail(Status.Error.ROULETTE_ACTIVITY_UNKNOW_VALUE);
		}
		return Result.success();
	}

	private List<RouletteRewardRateCfg> lottery(int times) {
		
		List<RouletteRewardRateCfg> ret = new ArrayList<RouletteRewardRateCfg>();
		try {
			for (int i = 0; i < times; i++) {
				RouletteRewardRateCfg cfg = RouletteRewardRateCfg.getRandomCfg();
				if (null == cfg) {
					break;
				}
				ret.add(cfg);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return null;
		}
		return ret;
	}
}
