package com.hawk.activity.type.impl.plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.entity.MsPlayerRankInfo;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.plan.cfg.PlanActivityKVCfg;
import com.hawk.activity.type.impl.plan.cfg.PlanIntegralCfg;
import com.hawk.activity.type.impl.plan.cfg.PlanIntegralCritCfg;
import com.hawk.activity.type.impl.plan.cfg.PlanRankRewardCfg;
import com.hawk.activity.type.impl.plan.cfg.PlanRewardCfg;
import com.hawk.activity.type.impl.plan.entity.PlanActivityRankInfo;
import com.hawk.activity.type.impl.plan.entity.PlanEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.HPPlanInfoNtf;
import com.hawk.game.protocol.Activity.HPPlanRankResp;
import com.hawk.game.protocol.Activity.HPPlanResp;
import com.hawk.game.protocol.Activity.RankPB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

public class PlanActivity extends ActivityBase {
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<PlanEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	static CopyOnWriteArrayList<PlanActivityRankInfo> cacheRank = new CopyOnWriteArrayList<PlanActivityRankInfo>();
	static long termId = 0;
	static long lastUpdateRankTime = 0;

	public PlanActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PLAN_ACTIVITY;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		try {
			if (getActivityEntity().getState() == Activity.ActivityState.OPEN_VALUE
					|| getActivityEntity().getState() == Activity.ActivityState.END_VALUE) {

				Optional<PlanEntity> opDataEntity = this.getPlayerDataEntity(playerId);
				if (!opDataEntity.isPresent()) {
					return;
				}
				this.syncActivityInfo(playerId, opDataEntity.get());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public void onOpen() {
		// 新的一轮 清空缓存
		cacheRank.clear();
		termId = getActivityTermId();

		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, GameConst.MsgId.PLAN_ACTIVITY_INIT, () -> {
				Optional<PlanEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error("on PlanActivity open init PlanEntity error, no entity created:" + playerId);
				}

				this.syncActivityInfo(playerId, opEntity.get());
			});
		}

	}

	private void syncActivityInfo(String playerId, PlanEntity entity) {
		HPPlanInfoNtf.Builder builder = HPPlanInfoNtf.newBuilder();
		builder.setScore(entity.getScore());
		if (null != cacheRank && cacheRank.size() > 0) {
			for (PlanActivityRankInfo rk : cacheRank) {
				if (rk.pb.getRank() > 3) {
					break;
				}
				builder.addRankTop3(rk.pb);
			}
		}

		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.PLAN_INFO_SYN_S_VALUE, builder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {

		PlanActivity activity = new PlanActivity(config.getActivityId(), activityEntity);

		if (0 != activityEntity.getTermId()) {
			loadRankToRedis(activityEntity.getTermId());
		}

		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PlanEntity> queryList = HawkDBManager.getInstance()
				.query("from PlanEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PlanEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PlanEntity entity = new PlanEntity(playerId, termId);
		entity.setScore(0);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	private List<PlanRewardCfg> lottery(int type, int times) {
		List<PlanRewardCfg> ret = new ArrayList<PlanRewardCfg>();
		for (int i = 0; i < times; i++) {
			int totalWeight = PlanRewardCfg.getTotalWeightByType(type);
			List<PlanRewardCfg> cfgAll = PlanRewardCfg.getLotteryCfgAllByType(type);
			if (0 == totalWeight || cfgAll.isEmpty()) {
				break;
			}

			int curWeight = HawkRand.randInt(1, totalWeight);
			for (PlanRewardCfg cfg : cfgAll) {
				if (cfg.getWeight() < curWeight) {
					curWeight -= cfg.getWeight();
					continue;
				}
				ret.add(cfg);
				break;
			}
		}

		return ret;
	}

	Result<?> planActivityLottery(String playerId, int type) {
		try {
			int times = 1;
			if ((type != 1 && type != 2)) {
				return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			}

			if (2 == type) {
				times = 10;
			}

			Optional<PlanEntity> opDataEntity = getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			}
			PlanEntity entity = opDataEntity.get();

			// 如果活动不在开放期不让抽奖
			if (getActivityEntity().getState() != Activity.ActivityState.OPEN_VALUE) {
				return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			}

			List<RewardItem.Builder> cost = HawkConfigManager.getInstance().getKVInstance(PlanActivityKVCfg.class)
					.getPlanLotteryCost(type);

			if (null != cost && cost.size() > 0
					&& this.getDataGeter().getItemNum(playerId, cost.get(0).getItemId()) < cost.get(0).getItemCount()) {
				cost = HawkConfigManager.getInstance().getKVInstance(PlanActivityKVCfg.class).getPlanLotteryPrice(type);
			}

			List<RewardItem.Builder> reward = HawkConfigManager.getInstance().getKVInstance(PlanActivityKVCfg.class)
					.getPlanLotteryReward(type);

			if (null == cost || null == reward) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}

			List<PlanRewardCfg> cfgList = lottery(type, times);
			if (null == cfgList) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
			// 计算分数
			PlanIntegralCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(PlanIntegralCfg.class, type);
			if (null == scoreCfg) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}

			int score = HawkRand.randInt(scoreCfg.getMin(), scoreCfg.getMin());

			PlanIntegralCritCfg mutiCfg = PlanIntegralCritCfg.randomCfg();

			if (null == mutiCfg) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}

			// 扣道具
			boolean flag = this.getDataGeter().cost(playerId, cost, Action.PLAN_ACTIVITY_LOTTERY_COST);
			if (!flag) {
				return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
			}

			HPPlanResp.Builder builder = HPPlanResp.newBuilder();
			score *= mutiCfg.getMultiple();
			entity.setScore(entity.getScore() + score);
			entity.notifyUpdate();
			builder.addAllLotteryIds(cfgList.stream().map(it -> it.getId()).collect(Collectors.toList()));
			builder.setLotteryMulti(mutiCfg.getMultiple());
			builder.setLotteryScore(score);
			builder.setLotteryType(type);

			this.getDataGeter().takeReward(playerId, reward, 1, Action.PLAN_ACTIVITY_LOTTERY_REWARD, false,
					RewardOrginType.PLAN_REWARD);
			List<RewardItem.Builder> rewards = new ArrayList<RewardItem.Builder>();
			// rewards.addAll(reward);
			for (PlanRewardCfg cfg : cfgList) {
				rewards.addAll(cfg.getRewardList());
			}

			this.getDataGeter().takeReward(playerId, rewards, 1, Action.PLAN_ACTIVITY_LOTTERY_REWARD, true,
					RewardOrginType.PLAN_REWARD);

			this.syncActivityInfo(playerId, entity);
			PlayerPushHelper.getInstance().pushToPlayer(playerId,
					HawkProtocol.valueOf(HP.code.PLAN_LOTTERY_RESP_S, builder));

			onPlayRankScoreAdd(playerId, score);

			// 抽奖打点
			getDataGeter().logPlanActivityLottery(playerId, entity.getTermId(), type, score);

		} catch (Exception e) {
			HawkException.catchException(e);
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}

		return Result.success();
	}

	private void onPlayRankScoreAdd(String playerId, long addSocre) {
		if(addSocre <= 0){
			return;
		}
		PlanActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlanActivityKVCfg.class);
		if (null != kvCfg) {
			String key = getRedisKey(getActivityTermId());
			ActivityLocalRedis.getInstance().zIncrbyWithExpire(key, playerId, (double) addSocre,
					(int) HawkTime.DAY_MILLI_SECONDS * 31);
		}
	}

	public void sendPlanRankToPlayer(String playerId) {
		PlanActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlanActivityKVCfg.class);
		if (null != kvCfg) {
			HPPlanRankResp.Builder resp = HPPlanRankResp.newBuilder();
			for (PlanActivityRankInfo rk : cacheRank) {
				resp.addRank(rk.pb.build());
				if (rk.pb.getRank() >= kvCfg.getRankerMax()) {
					break;
				}
			}

			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PLAN_RANK_RESP_S, resp));
		}
	}

	private String getRedisKey(int termId) {
		String key = String.format("%s:%d", ActivityRedisKey.PLAN_ACTIVITY_RANK, termId);
		return key;
	}

	// mysql数据库排行榜加入到redis排行榜
	private void loadRankToRedis(int termId) {
		PlanActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlanActivityKVCfg.class);
		if (null != kvCfg) {
			// 获取全服大本等级分布
			String sql = String.format(
					"select playerId, score from `activity_plan` where invalid = 0 and termId = %d and score > 0 order by score desc limit %d",
					termId, kvCfg.getRankerMax());
			List<MsPlayerRankInfo> rankList = HawkDBManager.getInstance().executeQuery(sql, MsPlayerRankInfo.class);
			if (null != rankList && !rankList.isEmpty()) {
				String key = getRedisKey(termId);
				// 删除redis 字段
				ActivityLocalRedis.getInstance().del(key);
				for (MsPlayerRankInfo info : rankList) {
					ActivityLocalRedis.getInstance().zIncrbyWithExpire(key, info.getPlayerId(),
							(double) info.getScore(), (int) HawkTime.DAY_MILLI_SECONDS * 31);
				}
			}
		}
	}

	// 更新排行榜
	private void updateCacheRank() {
		PlanActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlanActivityKVCfg.class);
		if (null != kvCfg) {
			String key = getRedisKey(getActivityTermId());
			Set<Tuple> ret = ActivityLocalRedis.getInstance().zrevrange(key, 0, kvCfg.getRankerMax() - 1);
			if (null != ret) {
				cacheRank.clear();
				// 清空内存中缓存的排行榜
				int rank = 0;
				Iterator<Tuple> redisRankIter = ret.iterator();

				ConfigIterator<PlanRankRewardCfg> rewardIter = HawkConfigManager.getInstance()
						.getConfigIterator(PlanRankRewardCfg.class);

				PlanRankRewardCfg rankRewardCfg = null;
				while (redisRankIter.hasNext()) {
					++rank;
					if (rewardIter.hasNext()) {
						rankRewardCfg = rewardIter.next();
					}
					if (null == rankRewardCfg) {
						break;
					}

					Tuple tp = redisRankIter.next();
					// 0 分不上榜
					if(tp.getScore() <= 0){
						break;
					}
					while (tp.getScore() < rankRewardCfg.getRankMinScore()) {
						++rank;
						if (rewardIter.hasNext()) {
							rankRewardCfg = rewardIter.next();
						} else {
							break;
						}
					}

					try {
						String playerName = this.getDataGeter().getPlayerName(tp.getElement());
						String guildName = this.getDataGeter().getGuildTagByPlayerId(tp.getElement());
						// 这里加入到本地缓存的排行榜
						RankPB.Builder builder = RankPB.newBuilder();
						builder.setRank(rank);
						if (!HawkOSOperator.isEmptyString(playerName)) {
							builder.setPlayerName(playerName);
							builder.setPlayerId(tp.getElement());
							builder.addAllPersonalProtectSwitch(this.getDataGeter().getPersonalProtectVals(tp.getElement()));
						}
						if (!HawkOSOperator.isEmptyString(guildName)) {
							builder.setGuildName(guildName);
						}
						builder.setScore((long) tp.getScore());
						LoggerFactory.getLogger("Server").info( "play_activity_upaterank: player:{},name:{},rank:{},score:{}", tp.getElement(), playerName, rank, tp.getScore()); 
						cacheRank.add(PlanActivityRankInfo.valueOf(tp.getElement(), builder));
					} catch (Exception e) {
						HawkException.catchException(e, "PlanActivity set player: score: rank:", tp.getElement(),
								tp.getScore(), rank);
					}

					if (rank >= kvCfg.getRankerMax()) {
						break;
					}
				}
			}
		}

	}

	@Override
	public void onTick() {
		try {
			if (getActivityEntity().getState() == Activity.ActivityState.OPEN_VALUE) {
				if (lastUpdateRankTime + HawkTime.MINUTE_MILLI_SECONDS * 2 < HawkTime.getMillisecond()) {
					lastUpdateRankTime = HawkTime.getMillisecond();
					updateCacheRank();
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		super.onTick();
	}

	@Override
	public void onEnd() {
		// 结算
		// 结算的时候updateRank
		updateCacheRank();
		MailId mailId = MailId.PLAN_ACTIVITY_RANK_AWARD;
		Object[] title = new Object[0];
		Object[] subTitle = new Object[0];
		for (PlanActivityRankInfo rk : cacheRank) {
			PlanRankRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlanRankRewardCfg.class,
					rk.pb.getRank());
			if (null == cfg) {
				break;
			}
			// 邮件发送奖励
			Object[] content;
			content = new Object[1];
			content[0] = rk.pb.getRank();

			try {
				sendMailToPlayer(rk.playerId, mailId, title, subTitle, content, cfg.getRewardList());
				HawkLog.logPrintln("PlanActivity send self rankReward succ, playerId: {}, rank: {}, cfgId: {}",
						rk.playerId, rk.pb.getRank(), cfg.getId());
			} catch (Exception e) {
				HawkException.catchException(e);
				HawkLog.logPrintln("PlanActivity send self rankReward error, playerId: {}, rank: {}, cfgId: {}",
						rk.playerId, rk.pb.getRank(), cfg.getId());
			}
		}

		super.onEnd();
	}
}
