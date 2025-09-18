package com.hawk.activity.type.impl.christmaswar;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ChristmasWarAttackEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.christmaswar.cfg.ChristmasWarKVCfg;
import com.hawk.activity.type.impl.christmaswar.cfg.ChristmasWarRankRewardCfg;
import com.hawk.activity.type.impl.christmaswar.cfg.ChristmasWarTaskCfg;
import com.hawk.activity.type.impl.christmaswar.entity.ActivityChristmasWarEntity;
import com.hawk.activity.type.impl.christmaswar.entity.ChristmasWarRedisData;
import com.hawk.activity.type.impl.christmaswar.rank.ChristmasWarRankObject;
import com.hawk.game.protocol.Activity.ChristmasWarPageInfoResp;
import com.hawk.game.protocol.Activity.ChristmasWarRankInfoResp;
import com.hawk.game.protocol.Activity.ChristmasWarRankType;
import com.hawk.game.protocol.Activity.ChristmasWarReceiveResp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

public class ChristmasWarActivity extends ActivityBase {

	/** 上次排行刷新时间 */
	private long lastCheckTime = 0;
	/**
	 * 活动的一些数据.
	 */
	private ChristmasWarRedisData data;

	/** 个人伤害排行 */
	private ChristmasWarRankObject selfRank = new ChristmasWarRankObject(ChristmasWarRankType.PERSONAL_DAMAGE_RANK);

	/** 联盟伤害排行 */
	private ChristmasWarRankObject guildRank = new ChristmasWarRankObject(ChristmasWarRankType.ALLIANCE_DAMAGE_RANK);
	/**
	 * 个人击杀排行榜。
	 */
	private ChristmasWarRankObject killRank = new ChristmasWarRankObject(ChristmasWarRankType.PERSONAL_KILL_RANK);
	
	/**
	 * 上一次注水的时间.
	 */
	private long lastInjectWaterTime; 

	public ChristmasWarActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.CHRISTMAS_WAR_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ChristmasWarActivity activity = new ChristmasWarActivity(config.getActivityId(), activityEntity);

		return activity;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		this.synPageInfo(playerId);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityChristmasWarEntity> entityList = HawkDBManager.getInstance().query(
				"from ActivityChristmasWarEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (entityList != null && !entityList.isEmpty()) {
			return entityList.get(0);
		}

		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityChristmasWarEntity christmasWarEntity = new ActivityChristmasWarEntity(playerId, termId);

		return christmasWarEntity;
	}
	
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (event.isCrossDay()) {
			
			// 这里刷一次
			ChristmasWarRedisData localData = this.getData();
			if (localData.needReset()) {
				logger.info("christmas war reset data lastResetTime:{}, curTime:{}", localData.getLastResetTime(), HawkTime.getMillisecond());
				localData.reset();
			}
			
			logger.info("receive clear dataEvent playerId:{}", event.getPlayerId());
			resetPlayerData(event.getPlayerId());
		}
	}
	
	private void resetPlayerData(String playerId) {
		Optional<ActivityChristmasWarEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		opEntity.get().reset();
	}
	/**
	 * 攻击boss
	 * 
	 * @param event
	 */
	@Subscribe
	public void onAttackBoss(ChristmasWarAttackEvent event) {
		if (!this.isAllowOprate(event.getPlayerId())) {
			return;
		}
		
		String playerId = event.getPlayerId();
		String guildId = event.getGuildId();
		int killCnt = event.getKillCnt();
		if (killCnt > 0) {
			int termId = getActivityTermId();
			selfRank.addRankScore(playerId, termId, killCnt);
			guildRank.addRankScore(guildId, termId, killCnt);
		}			
	}

	@Subscribe
	public void onKillMonster(MonsterAttackEvent event) {
		//是否允许操作.
		if (!this.isAllowOprate(event.getPlayerId())) {
			return;
		}
		
		String playerId = event.getPlayerId();
		if ((event.getMosterType() != MonsterType.TYPE_1_VALUE && event.getMosterType() != MonsterType.TYPE_2_VALUE)
				|| !event.isKill()) {
			return;
		}

		int atkTimes = event.getAtkTimes();
		int termId = getActivityTermId();
		killRank.addRankScore(playerId, termId, atkTimes);
		
		this.addKillNum(atkTimes);		
	}
	
	/**
	 * 联盟解散
	 * 
	 * @param event
	 */
	@Subscribe
	public void onGuildDismiss(GuildDismissEvent event) {
		guildRank.removeRank(event.getGuildId(), getActivityTermId());
	}

	@Override
	public void onTick() {
		
		long curTime = HawkTime.getMillisecond();
		
		try {
			resetTick(curTime);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		try {
			//注水.
			injectWaterTick(curTime);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		try {
			rankTick(curTime);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	private void resetTick(long curTime) {
		ChristmasWarRedisData localData = this.getData();
		if (localData.needReset()) {
			logger.info("christmas war reset data lastResetTime:{}, curTime:{}", localData.getLastResetTime(), curTime);
			localData.reset();
		}
	}
	
	private void rankTick(long currTime) {
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if (currTime >= endTime) {
			return;
		}

		ChristmasWarKVCfg config = ChristmasWarKVCfg.getInstance();
		long rankPeriod = config.getRankPeriod();

		if (currTime - lastCheckTime > rankPeriod) {
			refreshRankInfo(termId);
			lastCheckTime = currTime;
		}
	}
	
	/**
	 * 注水检测.
	 * @param currTime
	 */
	private void injectWaterTick(long currTime) {
		List<int[]> injectWaterList = ChristmasWarKVCfg.getInstance().getInjectWaterList();
		long zeroClockTime = HawkTime.getAM0Date().getTime();
		if (currTime <= zeroClockTime) {
			return ;
		}
		
		int timeOffsetSecond = (int)((currTime -zeroClockTime) / 1000);
		
		for (int[] timeArray : injectWaterList) {
			if (timeArray[0] <= timeOffsetSecond && timeOffsetSecond < timeArray[1]) {
				if (currTime - lastInjectWaterTime > timeArray[2] * 1000) {
					lastInjectWaterTime = currTime;
					this.addKillNum(timeArray[3]);
				}
			}
		}
		
	}

	public void init() {
		data = ChristmasWarRedisData.load(this.getActivityTermId());
	}

	/**
	 * 刷新榜单.
	 * 
	 * @param termId
	 */
	private void refreshRankInfo(int termId) {
		selfRank.refreshRank(termId);
		guildRank.refreshRank(termId);
		killRank.refreshRank(termId);
	}

	public void synPageInfo(String playerId) {
		Optional<ActivityChristmasWarEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
						
		ChristmasWarRedisData localData = this.getData();
		int showKilllMonsterNum = Math.min(localData.getKillMonsterNum().get(), ChristmasWarKVCfg.getInstance().getShowMaxNum());
		
		ChristmasWarPageInfoResp.Builder respBuilder = ChristmasWarPageInfoResp.newBuilder();
		respBuilder.setKilledBoss(localData.getKillChristmasMonsterNum().get());
		respBuilder.setKillMonsterNum(showKilllMonsterNum);
		respBuilder.setSummonBoss(this.getSummonNum());
		respBuilder.addAllReceivedIds(opEntity.get().getReceivedIdsList());
		respBuilder.setRemainBossNum(this.getDataGeter().getChristmasBossNum());
		
		this.pushToPlayer(playerId, HP.code.CHRISTMAS_WAR_PAGE_INFO_RESP_VALUE, respBuilder);
	}

	public ChristmasWarRedisData getData() {
		if (data == null) {
			this.init();
		}

		return data;
	}

	@Override
	public void shutdown() {
		ChristmasWarRedisData localData = this.getData();
		if (localData == null) {
			logger.error("data not init");

			return;
		}

		localData.saveToRedis();
	}
	
	/**
	 * @param number
	 */
	private  void addKillNum(int number) {
		ChristmasWarRedisData localData = this.getData();
		if (localData == null) {
			logger.error("data not init");

			return ;
		}
		int beforeNum = localData.getKillMonsterNum().get();
		localData.addKillMonsterNum(number);
				
		int afterNum = localData.getKillMonsterNum().get();
		ChristmasWarTaskCfg beforeTaskCfg = this.getTaskCfg(beforeNum);
		int beforeId =  beforeTaskCfg == null ? 0 : beforeTaskCfg.getId();
		ChristmasWarTaskCfg afterTaskCfg = this.getTaskCfg(afterNum);
		int afterId = afterTaskCfg == null ? 0 : afterTaskCfg.getId();
		if (beforeId != afterId) {
			this.getDataGeter().logChristmasTask(this.getActivityTermId(), afterId, afterNum);
			pushTaskFinish();
		}
	}
	
	/**
	 * 全服任务完成之后需要全服推送.
	 */
	private void pushTaskFinish() {
		Set<String> onlinePlayerSet = this.getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerSet) {
			this.callBack(playerId, GameConst.MsgId.CHRISTMAS_WAR_TASK_FINISH, ()->{
				this.synPageInfo(playerId);
			});
		}		
	}
	
	private ChristmasWarTaskCfg getTaskCfg(int num) {
		ConfigIterator<ChristmasWarTaskCfg> taskIterator = HawkConfigManager.getInstance().getConfigIterator(ChristmasWarTaskCfg.class);
		ChristmasWarTaskCfg taskCfg = null;		
		while (taskIterator.hasNext()) {
			ChristmasWarTaskCfg tmpCfg = taskIterator.next();
			if (num >= tmpCfg.getTaskNumber()) {
				taskCfg = tmpCfg;
			}
		}
		
		return taskCfg;
	}
	
	public void addKillChristmasNum(int number) {
		
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		ChristmasWarRedisData localData = this.getData();
		if (localData == null) {
			logger.error("data not init");

			return;
		}

		localData.addKillChristmasNum(number);
	}
	
	public void addSummonedNum(int number) {
		ChristmasWarRedisData localData = this.getData();
		if (localData == null) {
			logger.error("data not init");

			return;
		}

		localData.addSummonedChristmasNum(number);
	}

	/**
	 * 获取可以召唤的boss数量.
	 * @return
	 */
	public int getSummonNum() {
		ChristmasWarRedisData localData = this.getData();								
		ChristmasWarTaskCfg config = this.getTaskCfg();
		if (config == null) {
			return 0;
		}
		
		// 如果当天已经召唤过boss,则可召唤数量为0
		if (localData.getSummonMonsterNum().get() > 0) {
			return 0;
		}
		
		int summonNum = Math.min((config.getBossNumber() - localData.getSummonMonsterNum().get()), this.getDataGeter().getChristmasBossRefreshLimit());
		
		return summonNum;
	}
	
	/**
	 * 召唤boss, 这里减少boss的数量。
	 * @param number
	 */
	public void summonBoss(int number) {
		ChristmasWarRedisData localData = this.getData();
		if (localData == null) {
			logger.error("data is null");
			
			return;
		}
		
		localData.addSummonedChristmasNum(number);
	}
	
	public ChristmasWarTaskCfg getTaskCfg() {
		ChristmasWarTaskCfg config = null;
		ChristmasWarRedisData localData = this.getData();		
		int killNum = localData.getKillMonsterNum().get();
		ConfigIterator<ChristmasWarTaskCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(ChristmasWarTaskCfg.class);
		while (cfgIterator.hasNext()) {
			ChristmasWarTaskCfg tmpCfg = cfgIterator.next();
			if (tmpCfg.getTaskNumber() <= killNum) {
				config = tmpCfg;
			}
		}
		
		return config;
	}
	
	
	/**
	 * 领奖.
	 * @param playerId
	 * @param recieveId
	 * @return
	 */
	public int onReceiveReq(String playerId, int receiveId) {
		logger.info("christmaswar receive playerId:{}, receiveId:{}", playerId, receiveId);
		ChristmasWarTaskCfg config = HawkConfigManager.getInstance().getConfigByKey(ChristmasWarTaskCfg.class, receiveId);
		ChristmasWarRedisData localData = this.getData();
		//杀死
		if (config.getTaskNumber() > localData.getKillMonsterNum().get()) {
			return Status.Error.CHRISTMAS_WAR_KILLED_MONSTER_NOT_ENOUGH_VALUE;
		}
		
		Optional<ActivityChristmasWarEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		ActivityChristmasWarEntity entity = opEntity.get();
		if (entity.getReceivedIdsList().contains(receiveId)) {
			return Status.Error.CHRISTMAS_WAR_ALREADY_RECEIVE_VALUE;
		}
		
		entity.addReceivedIds(receiveId);		
		//领奖.
		this.getDataGeter().takeReward(playerId, config.getAwardList(), Action.CHRISTMAS_AWARD, true);
		
		ChristmasWarReceiveResp.Builder respBuilder = ChristmasWarReceiveResp.newBuilder();
		respBuilder.addAllReceivedIds(entity.getReceivedIdsList());		
		this.pushToPlayer(playerId, HP.code.CHRISTMAS_WAR_RECEIVE_RESP_VALUE, respBuilder);
		
		//tlog
		this.getDataGeter().logChristmasTaskReceive(playerId, entity.getTermId(), receiveId);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	public void synRankInfo(String playerId, ChristmasWarRankType rankType) {
		ChristmasWarRankInfoResp.Builder  resp;
		Optional<ActivityChristmasWarEntity> opEntity = this.getPlayerDataEntity(playerId);
		ActivityChristmasWarEntity entity = opEntity.get();		
		switch(rankType) {
		case PERSONAL_DAMAGE_RANK:
			resp = selfRank.buildRankInfoResp(entity);
			break;
		case ALLIANCE_DAMAGE_RANK:
			resp = guildRank.buildRankInfoResp(entity);
			break;
		case PERSONAL_KILL_RANK:
			resp = killRank.buildRankInfoResp(entity);
			break;
		default:
			logger.info("error rank type:{} playerId:{}", rankType, playerId);
			
			return;				
		}
		
		this.pushToPlayer(playerId, HP.code.CHRISTMAS_WAR_RANK_INFO_RESP_VALUE, resp);
	}
	
	@Override
	public void onShow() {
		this.clear();
	}
	
	@Override
	public void onEnd() {
		sendReward();		
	}
	
	private void sendReward() {
		int termId = this.getActivityTermId();
		sendPersonalAttackRankReward(termId);
		sendGuildRankReward(termId);
		sendPersonalKillRankReward(termId);
	}
	
	@Override
	public boolean handleForMergeServer() {
		sendReward();
		
		return true;
	}
	
	/**
	 * 清理一下redis 数据.
	 */
	private void clear() {
		this.data = null;
	}
	
	private void sendPersonalKillRankReward(int termId) {
		try {
			killRank.refreshRank(termId);
			MailId mailId = MailId.CHRISTMAS_PERSONAL_KILL_RANK;
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = killRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String playerId = tuple.getElement();
				ChristmasWarRankRewardCfg rankCfg = getRankReward(rank, killRank.getRankType());
				if (rankCfg == null) {
					logger.info("personal kill rank cfg error! playerId: {}, rank :{}", playerId, rank);
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[1];
				//content[0] = getActivityCfg().getActivityName();
				content[0] = rank;

				sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
				logger.info(" send personal kill rankReward, playerId: {}, rank: {}, cfgId: {}", playerId, rank, rankCfg.getId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 发送个人排行奖励
	 * @param termId
	 */
	private void sendPersonalAttackRankReward(int termId) {
		try {
			selfRank.refreshRank(termId);
			MailId mailId = MailId.CHRISTMAS_PERSONAL_ATTACK_RANK;					
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = selfRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String playerId = tuple.getElement();
				ChristmasWarRankRewardCfg rankCfg = getRankReward(rank, selfRank.getRankType());
				if (rankCfg == null) {
					logger.info("personal attack rank cfg error! playerId: {}, rank :{}", playerId, rank);
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[1];
				//content[0] = getActivityCfg().getActivityName();
				content[0] = rank;

				sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
				logger.info(" send personal attack rankReward, playerId: {}, rank: {}, cfgId: {}", playerId, rank, rankCfg.getId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 发送联盟排行奖励
	 * @param termId
	 */
	private void sendGuildRankReward(int termId) {
		try {
			guildRank.refreshRank(termId);
			MailId mailId = MailId.CHRISTMAS_GUILD_ATTACK_RANK;
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = guildRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String guildId = tuple.getElement();
				ChristmasWarRankRewardCfg rankCfg = getRankReward(rank, guildRank.getRankType());
				if (rankCfg == null) {
					logger.info(" guild rank cfg error! guildId: {}, rank :{}", guildId, rank);
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[1];
				//content[0] = getActivityCfg().getActivityName();
				content[0] = rank;
				Collection<String> ids = getDataGeter().getGuildMemberIds(guildId);
				for(String playerId : ids){
					sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
					logger.info(" send guild attack rankReward, guildId: {}, playerId: {}, rank: {}, cfgId: {}", guildId, playerId, rank, rankCfg.getId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取排行奖励配置
	 * @param rank
	 * @return
	 */
	private ChristmasWarRankRewardCfg getRankReward(int rank, ChristmasWarRankType rankType) {
		ChristmasWarRankRewardCfg rankCfg = null;
		ConfigIterator<ChristmasWarRankRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ChristmasWarRankRewardCfg.class);
		for (ChristmasWarRankRewardCfg cfg : configIterator) {
			if (rankType.getNumber() == cfg.getRankType() && rank <= cfg.getRankLower() && rank >= cfg.getRankUpper()) {
				rankCfg = cfg;
				break;
			}
		}
		return rankCfg;
	}
	
	/**
	 * 删除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return;
		}
		int termId = this.getActivityTermId();
		selfRank.removeRank(playerId, termId);
		selfRank.refreshRank(termId);
		killRank.removeRank(playerId, termId);
		killRank.refreshRank(termId);
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}
