package com.hawk.activity.type.impl.bountyHunter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.bountyHunter.config.BountyHunterActivityKVCfg;
import com.hawk.activity.type.impl.bountyHunter.config.BountyHunterHitCfg;
import com.hawk.activity.type.impl.bountyHunter.config.BountyHunterRewardPoolCfg;
import com.hawk.activity.type.impl.bountyHunter.entity.BountyHunterEntity;
import com.hawk.game.protocol.Activity.PBHunterInfoSync;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;

public class BountyHunterActivity extends ActivityBase {
	public static final int BOSSAHP = 10;

	public BountyHunterActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.BOUNTY_HUNTER;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BountyHunterActivity activity = new BountyHunterActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		sync(playerId);
	}
	
	

	@Override
	public void onOpen() {
		BountyHunterActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BountyHunterActivityKVCfg.class);
		String key = "bounty_hunter_all_glod_" + getActivityTermId();
		boolean exitKey = ActivityGlobalRedis.getInstance().getRedisSession().exists(key);
		if (!exitKey) {
			ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(key, kvCfg.getInitGold(), (int) TimeUnit.DAYS.toSeconds(7));
		}

		super.onOpen();
	}

	/**
	 * 跨天事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		sync(playerId);
	}

	public void sync(String playerId) {
		Optional<BountyHunterEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		BountyHunterEntity ent = opEntity.get();
		BountyHunterActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BountyHunterActivityKVCfg.class);
		Reward.RewardItem.Builder openCost = RewardHelper.toRewardItem(kvCfg.getItemHitPrice());
		openCost.setItemCount(openCost.getItemCount() * ent.getCostMutil());

		PBHunterInfoSync.Builder resp = PBHunterInfoSync.newBuilder();
		resp.setBossState(ent.getPool());
		resp.setBossHp(ent.getBossHp());
		resp.setLefState(ent.getLefState());
		resp.setFree(ent.getCostMutil() == 0 ? 1 : 0);
		resp.setMutil(ent.getRewardMutil());
		resp.setBatter(ent.getBatter());
		resp.setNextCost(RewardHelper.toItemString(openCost.build()));
		resp.setFreeGet(ent.getFreeItemDay() != HawkTime.getYearDay());

		pushToPlayer(playerId, HP.code.BOUNTER_HUNTER_SYNC_S_VALUE, resp);

	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<BountyHunterEntity> queryList = HawkDBManager.getInstance()
				.query("from BountyHunterEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			BountyHunterEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		BountyHunterActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BountyHunterActivityKVCfg.class);
		BountyHunterHitCfg hitCfg = HawkConfigManager.getInstance().getConfigByKey(BountyHunterHitCfg.class, kvCfg.getInitHitId());
		List<BountyHunterRewardPoolCfg> pooAList = HawkConfigManager.getInstance().getConfigIterator(BountyHunterRewardPoolCfg.class).stream()
				.filter(c -> c.getPool() == 1)
				.collect(Collectors.toList());
		BountyHunterRewardPoolCfg poolAcfg = HawkRand.randomWeightObject(pooAList);

		BountyHunterEntity entity = new BountyHunterEntity(playerId, termId);
		entity.setMutilCount(hitCfg.getCostMutil());
		entity.setCostMutil(hitCfg.getCostMutil());
		entity.setRewardMutil(hitCfg.getRewardMutil());
		entity.setPoolARount(poolAcfg.getRound());
		entity.setBossHp(BOSSAHP);
		entity.setPool(poolAcfg.getPool());
		entity.setHitType(hitCfg.getType());
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
	}

	@Override
	public void onTick() {
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

}
