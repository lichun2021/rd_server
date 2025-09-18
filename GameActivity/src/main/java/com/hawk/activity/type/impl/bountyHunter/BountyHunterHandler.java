package com.hawk.activity.type.impl.bountyHunter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.bountyHunter.config.BountyHunterActivityKVCfg;
import com.hawk.activity.type.impl.bountyHunter.config.BountyHunterHitCfg;
import com.hawk.activity.type.impl.bountyHunter.config.BountyHunterRewardCfg;
import com.hawk.activity.type.impl.bountyHunter.config.BountyHunterRewardPoolCfg;
import com.hawk.activity.type.impl.bountyHunter.entity.BountyHunterEntity;
import com.hawk.game.protocol.Activity.PBHunterInfo;
import com.hawk.game.protocol.Activity.PBHunterSelfRecord;
import com.hawk.game.protocol.Activity.PBHunterWorldBigGiftSync;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

/**
 * 机甲觉醒
 * 
 * @author Jesse
 */
public class BountyHunterHandler extends ActivityProtocolHandler {

	/**
	 * 同步
	 */
	@ProtocolHandler(code = HP.code.BOUNTER_HUNTER_SYNC_C_VALUE)
	public boolean onReqInfo(HawkProtocol protocol, String playerId) {
		BountyHunterActivity activity = getActivity(ActivityType.BOUNTY_HUNTER);
		activity.sync(playerId);
		return true;
	}

	@ProtocolHandler(code = HP.code.BOUNTER_HUNTER_FREE_ITEM_C_VALUE)
	public boolean onReqFree(HawkProtocol protocol, String playerId) {
		BountyHunterActivity activity = getActivity(ActivityType.BOUNTY_HUNTER);
		Optional<BountyHunterEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		BountyHunterEntity entity = opEntity.get();
		if (entity.getFreeItemDay() == HawkTime.getYearDay()) {
			return false;
		}

		BountyHunterActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BountyHunterActivityKVCfg.class);
		Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(kvCfg.getEveryDayFree());
		ActivityReward rewards = new ActivityReward(Arrays.asList(reward), Action.BOUNTY_HUNTER_HIT);
		rewards.setAlert(true);
		rewards.setOrginType(RewardOrginType.DEFAULT_REWARD, activity.getActivityId());// TODO 弹窑类型
		activity.postReward(playerId, rewards);

		entity.setFreeItemDay(HawkTime.getYearDay());
		activity.sync(playerId);
		return true;
	}

	/**
	 * 打
	 */
	@ProtocolHandler(code = HP.code.BOUNTER_HUNTER_HIT_C_VALUE)
	public boolean onReqHit(HawkProtocol protocol, String playerId) {
		BountyHunterActivity activity = getActivity(ActivityType.BOUNTY_HUNTER);
		Optional<BountyHunterEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		BountyHunterEntity entity = opEntity.get();
		BountyHunterActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BountyHunterActivityKVCfg.class);
		LogPs lp = new LogPs();
		{
			// 扣费
			Reward.RewardItem.Builder openCost = RewardHelper.toRewardItem(kvCfg.getItemHitPrice());
			openCost.setItemCount(openCost.getItemCount() * entity.getCostMutil());
			// 拥有道具数
			// int ticketCount = activity.getDataGeter().getItemNum(playerId, openCost.getItemId());
			List<Reward.RewardItem.Builder> cost = new ArrayList<>();
			// if (ticketCount < openCost.getItemCount()) {
			// long left = openCost.getItemCount() - ticketCount;
			// Reward.RewardItem.Builder itemOnecePrice = RewardHelper.toRewardItem(kvCfg.getItemOnecePrice());
			// itemOnecePrice.setItemCount(itemOnecePrice.getItemCount() * left);
			// openCost.setItemCount(ticketCount);
			//
			// cost.add(openCost);
			// cost.add(itemOnecePrice);
			// } else {
			cost.add(openCost);
			// }

			boolean consumeResult = activity.getDataGeter().consumeItems(playerId, cost, protocol.getType(), Action.BOUNTY_HUNTER_HIT);
			if (consumeResult == false) {
				return false;
			}
			lp.costStr = RewardHelper.toItemString(cost.get(0).build());
			lp.free = entity.getCostMutil() == 0;
		}

		if (entity.getPool() == 1) {// A池
			int bossHp = entity.getBossHp() - 1;
			entity.setBossHp(bossHp);

			{// 发奖励
				Reward.RewardItem.Builder reward;
				if (bossHp > 0) {
					ConfigIterator<BountyHunterRewardCfg> it = HawkConfigManager.getInstance().getConfigIterator(BountyHunterRewardCfg.class);
					List<BountyHunterRewardCfg> rlist = it.stream().filter(r -> r.getPool() == entity.getPool()).collect(Collectors.toList());
					BountyHunterRewardCfg rewardCfg = HawkRand.randomWeightObject(rlist);
					entity.setLefState(0);
					reward = RewardHelper.toRewardItem(rewardCfg.getReward());
				} else { // 固定宝箱
					reward = RewardHelper.toRewardItem(kvCfg.getEveryTenItem());
					entity.setLefState(1);
				}
				reward.setItemCount(reward.getItemCount() * entity.getRewardMutil());
				ActivityReward rewards = new ActivityReward(Arrays.asList(reward), Action.BOUNTY_HUNTER_HIT);
				rewards.setAlert(true);
				rewards.setOrginType(RewardOrginType.BOUNTY_HUNTER_NOMAL_REWARD, activity.getActivityId());// TODO 弹窑类型
				activity.postReward(playerId, rewards);

				{
					// 1 功击雪怪 2 功击超级雪怪 3 杀雪怪 4 杀超级怪
					PBHunterSelfRecord pbR = PBHunterSelfRecord.newBuilder()
							.setType(bossHp == 0 ? 3 : 1)
							.setTime(HawkTime.getMillisecond())
							.setReward(RewardHelper.toItemString(reward.build()))
							.build();
					// 存redis
					ActivityGlobalRedis.getInstance().getRedisSession().lPush(personalRecordKey(playerId), (int) TimeUnit.DAYS.toSeconds(7), pbR.toByteArray());
				}

				lp.rewStr = RewardHelper.toItemString(reward.build());
				lp.boss = "A";
				lp.bossHp = bossHp;
				lp.rewardMutil = entity.getRewardMutil();

			}
			// 处理hit
			afterHit(entity);
			activity.sync(playerId);
			if (bossHp == 0) { // 处理A池轮回
				lp.lefState = 1;
				entity.setLefState(1);
				int poolARount = entity.getPoolARount() - 1;
				entity.setPoolARount(poolARount);
				if (poolARount == 0) { // 进入bossB模式
					List<BountyHunterRewardPoolCfg> pooAList = HawkConfigManager.getInstance().getConfigIterator(BountyHunterRewardPoolCfg.class).stream()
							.filter(c -> c.getPool() == 2)
							.collect(Collectors.toList());
					BountyHunterRewardPoolCfg poolBcfg = HawkRand.randomWeightObject(pooAList);
					entity.setBossBNotDie(poolBcfg.getBossBNotDie());
					entity.setBossBNotRun(poolBcfg.getBossBNotRun());
					entity.setPool(poolBcfg.getPool());
					entity.setLefState(0);
					activity.sync(playerId);
				} else {
					entity.setLefState(0);
					entity.setBossHp(BountyHunterActivity.BOSSAHP);
					activity.sync(playerId);
				}
			}

		} else { // B
			ConfigIterator<BountyHunterRewardCfg> it = HawkConfigManager.getInstance().getConfigIterator(BountyHunterRewardCfg.class);
			List<BountyHunterRewardCfg> rlist = it.stream().filter(r -> r.getPool() == entity.getPool()).collect(Collectors.toList());
			BountyHunterRewardCfg rewardCfg = HawkRand.randomWeightObject(rlist);
			{
				Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(rewardCfg.getReward());
				reward.setItemCount(reward.getItemCount() * entity.getRewardMutil());
				ActivityReward rewards = new ActivityReward(Arrays.asList(reward), Action.BOUNTY_HUNTER_HIT);
				rewards.setAlert(true);
				rewards.setOrginType(RewardOrginType.BOUNTY_HUNTER_NOMAL_REWARD, activity.getActivityId());
				// 给奖励
				activity.postReward(playerId, rewards);

				{
					// 1 功击雪怪 2 功击超级雪怪 3 杀雪怪 4 杀超级怪
					PBHunterSelfRecord pbR = PBHunterSelfRecord.newBuilder()
							.setType(2)
							.setTime(HawkTime.getMillisecond())
							.setReward(RewardHelper.toItemString(reward.build()))
							.build();
					// 存redis
					ActivityGlobalRedis.getInstance().getRedisSession().lPush(personalRecordKey(playerId), (int) TimeUnit.DAYS.toSeconds(7), pbR.toByteArray());
				}

				lp.rewStr = RewardHelper.toItemString(reward.build());
				lp.boss = "B";
				lp.rewardMutil = entity.getRewardMutil();
			}
			// 处理hit
			afterHit(entity);
			entity.setBossBHit(entity.getBossBHit() + 1);
			if (entity.getBossBHit() > entity.getBossBNotRun()) {// 回A池 boss 跑了
				lp.lefState = 2;
				entity.setLefState(2);
				entity.setBossBHit(0);
				activity.sync(playerId);

				// 再同步刷新bossA
				List<BountyHunterRewardPoolCfg> pooAList = HawkConfigManager.getInstance().getConfigIterator(BountyHunterRewardPoolCfg.class).stream()
						.filter(c -> c.getPool() == 1)
						.collect(Collectors.toList());
				BountyHunterRewardPoolCfg poolAcfg = HawkRand.randomWeightObject(pooAList);
				entity.setPoolARount(poolAcfg.getRound());
				entity.setBossHp(BountyHunterActivity.BOSSAHP);
				entity.setLefState(0);
				entity.setPool(poolAcfg.getPool());
			} else if (entity.getBossBHit() > entity.getBossBNotDie()) {// 拿全部
				int allGold = NumberUtils.toInt(ActivityGlobalRedis.getInstance().get(globalGoldKey()));
				boolean bfasle = Math.random() < kvCfg.getTakeAllRate(allGold) * 0.0001;
				if (bfasle) { // 中了大奖
					lp.lefState = 1;
					ActivityGlobalRedis.getInstance().del(globalGoldKey());
					ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(globalGoldKey(), kvCfg.getInitGold(), (int) TimeUnit.DAYS.toSeconds(7));

					Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GOLD_VALUE, allGold);
					ActivityReward bigrewards = new ActivityReward(Arrays.asList(reward), Action.BOUNTY_HUNTER_HIT);
					bigrewards.setAlert(true);
					bigrewards.setOrginType(RewardOrginType.BOUNTY_HUNTER_BIG_REWARD, activity.getActivityId());// TODO 弹窑类型
					activity.postReward(playerId, bigrewards);

					{
						// 1 功击雪怪 2 功击超级雪怪 3 杀雪怪 4 杀超级怪
						PBHunterSelfRecord pbR = PBHunterSelfRecord.newBuilder()
								.setType(4)
								.setTime(HawkTime.getMillisecond())
								.setReward(RewardHelper.toItemString(reward.build()))
								.build();
						// 存redis
						ActivityGlobalRedis.getInstance().getRedisSession().lPush(personalRecordKey(playerId), (int) TimeUnit.DAYS.toSeconds(7), pbR.toByteArray());
					}

					entity.setLefState(1);
					entity.setBossBHit(0);
					activity.sync(playerId);
					// 记录
					ActivityDataProxy dataGeter = activity.getDataGeter();
					String playerName = dataGeter.getPlayerName(playerId);
					PBHunterInfo.Builder recordBul = PBHunterInfo.newBuilder();
					recordBul.setName(playerName);
					recordBul.setPficon(dataGeter.getPfIcon(playerId));
					recordBul.setIcon(dataGeter.getIcon(playerId));
					recordBul.setServerId(dataGeter.getServerId());
					recordBul.setGuildTag(dataGeter.getGuildTagByPlayerId(playerId));
					recordBul.setTime(HawkTime.getMillisecond());
					recordBul.setReward("10000_1001_" + allGold);
					recordBul.setPlayerId(playerId);
					recordBul.addAllPersonalProtectSwitch(dataGeter.getPersonalProtectVals(playerId));
					lp.bigGift = "10000_1001_" + allGold;
					// 存redis
					ActivityGlobalRedis.getInstance().getRedisSession().lPush(globalGoldRecordKey(), (int) TimeUnit.DAYS.toSeconds(7), recordBul.build().toByteArray());
					// notice
					activity.sendBroadcast(NoticeCfgId.BOUNTY_HUNTER_OK, null, playerName, allGold);
					// 回奖池A
					List<BountyHunterRewardPoolCfg> pooAList = HawkConfigManager.getInstance().getConfigIterator(BountyHunterRewardPoolCfg.class).stream()
							.filter(c -> c.getPool() == 1)
							.collect(Collectors.toList());
					BountyHunterRewardPoolCfg poolAcfg = HawkRand.randomWeightObject(pooAList);
					entity.setPoolARount(poolAcfg.getRound());
					entity.setBossHp(BountyHunterActivity.BOSSAHP);
					entity.setPool(poolAcfg.getPool());
					entity.setLefState(0);
				}
			}
			activity.sync(playerId);

		}
		if (StringUtils.isNotEmpty(kvCfg.getExtReward())) {
			List<RewardItem.Builder> result = RewardHelper.toRewardItemList(kvCfg.getExtReward());
			ActivityReward reward = new ActivityReward(result, Action.BOUNTY_HUNTER_HIT);
			reward.setAlert(false);
			reward.setOrginType(RewardOrginType.BOUNTY_HUNTER_NOMAL_REWARD, activity.getActivityId());
			activity.postReward(playerId, reward);
		}

		activity.getDataGeter().logBountyHunterHit(playerId, activity.getActivityTermId(), lp.boss, lp.bossHp, lp.costStr, lp.rewStr, lp.free, lp.rewardMutil, lp.lefState,
				lp.bigGift);
		logger.info("BOUNTY_HUNTER_HIT playerId={}  boss = {} bossHp = {} cost = {} reward = {} free = {} rewardMutil = {} lefState = {} bigGift = {}",
				playerId, lp.boss, lp.bossHp, lp.costStr, lp.rewStr, lp.free, lp.rewardMutil, lp.lefState, lp.bigGift);
		return true;
	}

	@ProtocolHandler(code = HP.code.BOUNTER_HUNTER_WORLD_GIFT_SYNC_C_VALUE)
	public boolean onReqWorldRecord(HawkProtocol protocol, String playerId) {
		long allGold = NumberUtils.toInt(ActivityGlobalRedis.getInstance().get(globalGoldKey()));

		PBHunterWorldBigGiftSync.Builder resp = PBHunterWorldBigGiftSync.newBuilder();
		resp.setAllGold(allGold);

		{
			List<byte[]> records = ActivityGlobalRedis.getInstance().getRedisSession().lRange(globalGoldRecordKey(), 0, 100, 0);
			for (byte[] v : records) {
				try {
					resp.addPlayers(PBHunterInfo.newBuilder().mergeFrom(v));
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
			}
		}

		{
			List<byte[]> records = ActivityGlobalRedis.getInstance().getRedisSession().lRange(personalRecordKey(playerId), 0, 200, 0);
			for (byte[] v : records) {
				try {
					resp.addSeldRec(PBHunterSelfRecord.newBuilder().mergeFrom(v));
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
			}
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.BOUNTER_HUNTER_WORLD_GIFT_SYNC_S, resp));
		return true;
	}

	/**
	 * 金币池子
	 */
	private String globalGoldKey() {
		BountyHunterActivity activity = getActivity(ActivityType.BOUNTY_HUNTER);
		return "bounty_hunter_all_glod_" + activity.getActivityTermId();
	}

	/**
	 * 大奖记录
	 */
	private byte[] globalGoldRecordKey() {
		BountyHunterActivity activity = getActivity(ActivityType.BOUNTY_HUNTER);
		return ("bounty_hunter_record_" + activity.getActivityTermId()).getBytes();
	}

	/**
	 * 个人记录
	 */
	private byte[] personalRecordKey(String playerId) {
		BountyHunterActivity activity = getActivity(ActivityType.BOUNTY_HUNTER);
		return ("bounty_hunter_record_" + playerId + "_" + activity.getActivityTermId()).getBytes();
	}

	private void afterHit(BountyHunterEntity entity) {
		if (entity.getRewardMutil() != 1 || entity.getCostMutil() != 1) {
			entity.setBatter(entity.getBatter() + 1);
		} else {
			entity.setBatter(0);
		}

		int mutilCount = entity.getMutilCount() - 1;
		entity.setMutilCount(mutilCount);
		if (mutilCount < 1) {
			List<BountyHunterHitCfg> hitit = HawkConfigManager.getInstance().getConfigIterator(BountyHunterHitCfg.class).stream()
					.filter(cfg -> cfg.getType() != entity.getHitType())
					.collect(Collectors.toList());
			BountyHunterHitCfg hitCfg = HawkRand.randomWeightObject(hitit);
			entity.setMutilCount(hitCfg.getCout());
			entity.setCostMutil(hitCfg.getCostMutil());
			entity.setRewardMutil(hitCfg.getRewardMutil());
			entity.setHitType(hitCfg.getType());
		}

		BountyHunterActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BountyHunterActivityKVCfg.class);
		ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(globalGoldKey(), kvCfg.getEveryHitAdd(), (int) TimeUnit.DAYS.toSeconds(7));
	}

	class LogPs {
		public String bigGift = "";
		public int lefState;
		public int rewardMutil;
		public boolean free;
		public int bossHp;
		public String boss = "";
		public String rewStr = "";
		String costStr = "";
	}
}
