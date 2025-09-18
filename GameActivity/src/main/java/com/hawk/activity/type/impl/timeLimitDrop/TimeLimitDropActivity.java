package com.hawk.activity.type.impl.timeLimitDrop;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.CityResourceCollectEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.TimeLimitDropGetItemEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.event.impl.WishingEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.activity.type.impl.timeLimitDrop.cfg.TimeLImitDropRankAwardCfg;
import com.hawk.activity.type.impl.timeLimitDrop.cfg.TimeLimitDroAwardCfg;
import com.hawk.activity.type.impl.timeLimitDrop.cfg.TimeLimitDropCfg;
import com.hawk.activity.type.impl.timeLimitDrop.cfg.TimeLimitDropDataCfg;
import com.hawk.activity.type.impl.timeLimitDrop.entity.TimeLimitDropEntity;
import com.hawk.activity.type.impl.timeLimitDrop.rank.TimeLimitDropRank;
import com.hawk.activity.type.impl.timeLimitDrop.rank.TimeLimitDropRankProvider;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.TimeLimitDropRankMsg;
import com.hawk.game.protocol.Activity.TimeLimitDropRankResp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

public class TimeLimitDropActivity extends ActivityBase implements AchieveProvider {

	public TimeLimitDropActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isAllowOprate(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return true;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public void onOpen() {		
		Set<String> idSet = this.getDataGeter().getOnlinePlayers();
		for (String id : idSet) {
			this.callBack(id, GameConst.MsgId.TIME_LIMIT_DROP_INIT_ACHIEVE, ()->{
				this.pushAchieveItems(id);
			});
		}
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!this.isAllowOprate(event.getPlayerId())) {
			return;
		}
		if (event.isCrossDay()) {
			this.cleanData(event.getPlayerId());					
		}
	}
	/**
	 * 数据重置
	 * @param AllyBeatBack
	 */
	private void cleanData(String playerId) {
		HawkLog.logPrintln("TimeLimitDropResetData playerId:{}", playerId);
		Optional<TimeLimitDropEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		TimeLimitDropEntity entity = opEntity.get();		
		entity.setCollectRemainTime(0);
		entity.setBeatYuriTimes(0);
		entity.setWishTimes(0);
		entity.setWolrdCollectRemainTime(0);
		entity.setWolrdCollectTimes(0);
	}
	
	
	private void pushAchieveItems(String playerId) {
		Optional<AchieveItems> achieveItems = this.getAchieveItems(playerId);
		if (achieveItems.isPresent()) {
			AchievePushHelper.pushAchieveUpdate(playerId, achieveItems.get().getItems());
		}
		
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<TimeLimitDropEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		TimeLimitDropEntity playerDataEntity = opPlayerDataEntity.get();
		if (playerDataEntity.getItemList().isEmpty()) {
			ConfigIterator<TimeLimitDroAwardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(TimeLimitDroAwardCfg.class);
			List<AchieveItem> itemList = new ArrayList<>();
			while (configIterator.hasNext()) {
				TimeLimitDroAwardCfg cfg = configIterator.next();				
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());				
				itemList.add(item);
			}
			playerDataEntity.setItemList(itemList);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(TimeLimitDroAwardCfg.class, achieveId);
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
		
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public Action takeRewardAction() {
		return Action.TIME_LIMIT_DROP_ACHIEVE_REWARD;
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.TIME_LIMIT_DROP;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		TimeLimitDropActivity activity = new TimeLimitDropActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<TimeLimitDropEntity> queryList = HawkDBManager.getInstance()
				.query("from TimeLimitDropEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		TimeLimitDropEntity dropEntity = new TimeLimitDropEntity();
		dropEntity.setPlayerId(playerId);
		dropEntity.setTermId(termId);
		
		return dropEntity;
	}
	
	@Subscribe
	public void worldCollectEvent(ResourceCollectEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (event.getCollectTime() <= 0) {
			return;
		}

		Optional<TimeLimitDropEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		
		TimeLimitDropEntity entity = opEntity.get();
		int collectTime = event.getCollectTime() + entity.getWolrdCollectRemainTime();
		TimeLimitDropDataCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitDropDataCfg.class,
				TimeLimitDropConst.WORLD_COLLECT);
		//配置不存在说明策划不想触发
		if (cfg == null) {
			return;
		}
		
		if (collectTime >= cfg.getDropParam()) {
			int num = collectTime / cfg.getDropParam();
			int remain = collectTime % cfg.getDropParam();
			if (cfg.getDropLimit() > 0) {
				num = num > cfg.getDropLimit()  - entity.getWolrdCollectTimes() ? cfg.getDropLimit()  - entity.getWolrdCollectTimes() : num;
			}
			if (num > 0) {
				takeReward(event.getPlayerId(), cfg.getDropId(), num,
						Action.TIME_LIMIT_DROP_TASK_REWARD, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
				entity.setWolrdCollectTimes(num + entity.getWolrdCollectTimes());
				entity.setWolrdCollectRemainTime(remain);
			}			
		} else {
			entity.setWolrdCollectRemainTime(collectTime);
		}
	}

	@Subscribe
	public void wishingEvent(WishingEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<TimeLimitDropEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		TimeLimitDropEntity entity = opEntity.get();
		entity.setWishTimes(entity.getWishTimes() + 1);

		TimeLimitDropDataCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitDropDataCfg.class,
				TimeLimitDropConst.WISH);
		if (cfg == null) {
			return;
		}
		
		if (entity.getWishTimes() >= cfg.getDropParam()) {
			takeReward(event.getPlayerId(), cfg.getDropId(), 1,
					Action.TIME_LIMIT_DROP_TASK_REWARD, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setWishTimes(0);
		}
	
	}

	@Subscribe
	public void resourceCollectEvent(CityResourceCollectEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		// 处理使用技能的情况
		if (event.getCollectTime().isEmpty()) {
			return;
		}

		Optional<TimeLimitDropEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		TimeLimitDropEntity entity = opEntity.get();
		int remainTime = entity.getCollectRemainTime();

		TimeLimitDropDataCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitDropDataCfg.class,
				TimeLimitDropConst.RESOURCE_COLLECT);
		if (cfg == null) {
			return;
		}
		
		int num = 0;
		int totalTime = 0;
		for (Integer timeLong : event.getCollectTime()) {
			totalTime = timeLong + remainTime;
			totalTime = totalTime > cfg.getDropLimit() ? cfg.getDropLimit() : totalTime;
			if (totalTime > cfg.getDropParam()) {
				num = totalTime / cfg.getDropParam() + num;
				remainTime = totalTime % cfg.getDropParam();
			}
		}

		if (num > 0) {
			takeReward(event.getPlayerId(), cfg.getDropId(), num,
					Action.TIME_LIMIT_DROP_TASK_REWARD, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
		}
		entity.setCollectRemainTime(remainTime);
		
	}
	
	@Subscribe
	public void beatYuriEvent(MonsterAttackEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		int monsterType = event.getMosterType();
		switch(monsterType) {
		case MonsterType.TYPE_1_VALUE:
		case MonsterType.TYPE_2_VALUE:
			if (!event.isKill()) {
				return;
			}
			break;
		default:
			return;
		}
		
		int atkTimes = event.getAtkTimes();
		Optional<TimeLimitDropEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		TimeLimitDropEntity entity = opEntity.get();
		entity.setBeatYuriTimes(entity.getBeatYuriTimes() + atkTimes);
		TimeLimitDropDataCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitDropDataCfg.class,
				TimeLimitDropConst.BEAT_YURI);
		if (cfg == null) {
			return;
		}
		
		if (atkTimes >= cfg.getDropParam()) {
			takeReward(event.getPlayerId(), cfg.getDropId(), atkTimes,
					Action.TIME_LIMIT_DROP_TASK_REWARD, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setBeatYuriTimes(0);
		}
	}	

	private void takeReward(String playerId, int dropId, int num, Action action, int mailId,
			String name, String activityName) {		
		Map<Integer, Integer> map = new HashMap<>();		
		this.getDataGeter().takeReward(playerId, dropId, num, action, mailId, name, activityName, false, map);
		HawkLog.logPrintln("playerId:{} time limit drop activity reward map:{}", playerId, map);
		ActivityManager.getInstance().postEvent(new TimeLimitDropGetItemEvent(playerId, map));				
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}
	
	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
	@Subscribe
	public void onGetItemEvent(TimeLimitDropGetItemEvent getItemEvent) {
		if (!isOpening(getItemEvent.getPlayerId())) {
			return;
		}
		
		TimeLimitDropCfg cfg = TimeLimitDropCfg.getInstance();
		Integer num = getItemEvent.getItemCount().get(cfg.getItemId());
		if (num == null) {
			return;
		}
		
		ActivityRankProvider<TimeLimitDropRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.TIME_LIMIT_DROP_RANK, TimeLimitDropRank.class);
		TimeLimitDropRank timeLimitDropRank = new TimeLimitDropRank();
		timeLimitDropRank.setId(getItemEvent.getPlayerId());
		timeLimitDropRank.setScore(num);
		rankProvider.insertIntoRank(timeLimitDropRank);
		
		this.getDataGeter().logTimieLimit(getItemEvent.getPlayerId(), num);
		
		HawkLog.logPrintln("playerId:{} time limit drop activity addScore:{}", getItemEvent.getPlayerId(), num);
	}
	
	@Override
	public void onEnd() {
		int termId = this.getActivityTermId();
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			
			@Override
			public Object run() {
				sendReward(termId);			
				
				return null;
			}
		});
	}
	
	private void sendReward(int termId) {
		HawkLog.logPrintln("TimeLimitDropLimtActivityEnd");
		List<TimeLimitDropRank> rankList = TimeLimitDropActivity.this.getRankList(termId);
		for (TimeLimitDropRank rank : rankList) {
			TimeLImitDropRankAwardCfg rewardCfg = TimeLimitDropActivity.this.getRankRewardCfg(rank.getRank());
			if (rewardCfg == null) {
				HawkLog.errPrintln("TimeLimitDropLimtActivityEnd rank rewardCfg can not find playerId:{}, rank:{}", rank.getId(), rank.getRank());
			} else {
				TimeLimitDropActivity.this.getDataGeter().sendMail(rank.getId(), MailId.TIME_LIMIT_DROP_RANK_REWARD, null, null, new Object[]{rank.getRank()}, rewardCfg.getGainItemList(), false);
				HawkLog.logPrintln("TimeLimitDropLimtActivityEnd reward playerId:{}, rank:{}", rank.getId(), rank.getRank());
			}									
		}		
	}
	
	public List<TimeLimitDropRank> getRankList(int termId) {
		int rankSize = TimeLimitDropCfg.getInstance().getRankSize();
		TimeLimitDropRankProvider rankProvider = (TimeLimitDropRankProvider)(ActivityRankContext.getRankProvider(ActivityRankType.TIME_LIMIT_DROP_RANK, TimeLimitDropRank.class));
		String redisKey = rankProvider.getRedisKey(termId);		
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(redisKey, 0, Math.max((rankSize - 1), 0));		
		List<TimeLimitDropRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			TimeLimitDropRank timeDropRank = new TimeLimitDropRank();
			timeDropRank.setId(rank.getElement());
			timeDropRank.setRank(index);
			long score = RankScoreHelper.getRealScore((long) rank.getScore());
			timeDropRank.setScore(score);
			newRankList.add(timeDropRank);
			index++;
		}
		//设置过期
		ActivityLocalRedis.getInstance().getRedisSession().expire(redisKey, 86400 * 3);
		return newRankList;
	}

	private TimeLImitDropRankAwardCfg getRankRewardCfg(int rank) {
		ConfigIterator<TimeLImitDropRankAwardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(TimeLImitDropRankAwardCfg.class);
		while(configIterator.hasNext()) {
			TimeLImitDropRankAwardCfg config = configIterator.next();
			if (config.getRankHigh() <= rank && config.getRankLow() >= rank) {
				return config;
			}
		}
		
		return null;
	}
	
	
	public void onRankInfoReq(String playerId) {
		ActivityRankProvider<TimeLimitDropRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.TIME_LIMIT_DROP_RANK, TimeLimitDropRank.class);
		List<TimeLimitDropRank> rankList = rankProvider.getRankList();
		TimeLimitDropRankResp.Builder sbuilder = TimeLimitDropRankResp.newBuilder();
		for (TimeLimitDropRank tldr : rankList) {
			try {
				sbuilder.addRankList(buildTimeLimitDropRank(tldr));
			} catch (Exception e) {
			}
		}
		
		TimeLimitDropRank myRank = rankProvider.getRank(playerId);
		long myScore = 0;
		int myRankNO = 0;
		if (myRank != null) {
			myScore = myRank.getScore();
			myRankNO = myRank.getRank();
		}		
		sbuilder.setMyRank(myRankNO);
		sbuilder.setMyScore(myScore);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.TIME_LIMIT_DROP_RANK_RESP_VALUE, sbuilder);
		this.getDataGeter().sendProtocol(playerId, protocol);		
	}
	
	
	private TimeLimitDropRankMsg.Builder buildTimeLimitDropRank(TimeLimitDropRank rank) {
		ActivityDataProxy dataGeter = getDataGeter();
		TimeLimitDropRankMsg.Builder rankPBBuilder = TimeLimitDropRankMsg.newBuilder();
		String playerName = dataGeter.getPlayerName(rank.getId());
		String guildName = dataGeter.getGuildTagByPlayerId(rank.getId());
		if (HawkOSOperator.isEmptyString(guildName) == false) {
			rankPBBuilder.setGuildName(guildName);
		}
		rankPBBuilder.setPlayerName(playerName);
		rankPBBuilder.setPlayerId(rank.getId());
		rankPBBuilder.addAllPersonalProtectSwitch(dataGeter.getPersonalProtectVals(rank.getId()));
		rankPBBuilder.setRank(rank.getRank());
		rankPBBuilder.setScore(rank.getScore());
		
		return rankPBBuilder;
	}
	
	@Override
	public boolean handleForMergeServer() {
		if (this.getActivityEntity().getActivityState() == ActivityState.HIDDEN) {
			return true;
		}
		
		this.sendReward(this.getActivityTermId());
		
		return true;
	} 
	
	/**
	 * 删除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return;
		}
		ActivityRankProvider<TimeLimitDropRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.TIME_LIMIT_DROP_RANK, TimeLimitDropRank.class);
		rankProvider.remMember(playerId);
		rankProvider.doRankSort();
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}
