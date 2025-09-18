package com.hawk.activity.type.impl.sendFlower;

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
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.SendFlowerEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.msg.ActivityStateChangeMsg;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.sendFlower.cfg.SendFlowerAchieve;
import com.hawk.activity.type.impl.sendFlower.cfg.SendFlowerActivityKVCfg;
import com.hawk.activity.type.impl.sendFlower.cfg.SendFlowerRankRewardCfg;
import com.hawk.activity.type.impl.sendFlower.entity.SendFlowerEntity;
import com.hawk.activity.type.impl.sendFlower.rank.SendFlowerRankObject;
import com.hawk.game.protocol.Activity.ActivityState;
import com.hawk.game.protocol.Activity.PBSendFlowerRankInfoResp;
import com.hawk.game.protocol.Activity.PBSendFlowerRecordInfo;
import com.hawk.game.protocol.Activity.PBSendFlowerRecordResp;
import com.hawk.game.protocol.Activity.PBSendFlowerType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

public class SendFlowerActivity extends ActivityBase implements AchieveProvider {
	/** 个人红包历史记录，过期时间 **/
	private final int RECORD_EXPIRE = (86400 * 30);

	/** 上次排行刷新时间 */
	private long lastCheckTime = 0;

	/** 送花排行 */
	private SendFlowerRankObject songHuaRank = new SendFlowerRankObject(PBSendFlowerType.SONG_HUA_TYPE);

	/** 收花排行 */
	private SendFlowerRankObject shouHuaRank = new SendFlowerRankObject(PBSendFlowerType.SHOU_HUA_TYPE);

	public SendFlowerActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.SEND_FLOWER_HUA;
	}
	
	public Action takeRewardAction() {
		return Action.SEND_FLOWER_HUA_REWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SendFlowerActivity activity = new SendFlowerActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SendFlowerEntity> queryList = HawkDBManager.getInstance()
				.query("from SendFlowerEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SendFlowerEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SendFlowerEntity entity = new SendFlowerEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onOpen() {
		songHuaRank = new SendFlowerRankObject(PBSendFlowerType.SONG_HUA_TYPE);
		shouHuaRank = new SendFlowerRankObject(PBSendFlowerType.SHOU_HUA_TYPE);
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.SEND_FLOWER_INIT, () -> initAchieveInfo(playerId));
		}
	}

	/** 初始化成就信息
	 * 
	 * @param playerId */
	private void initAchieveInfo(String playerId) {
		Optional<SendFlowerEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SendFlowerEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<SendFlowerAchieve> configIterator = HawkConfigManager.getInstance().getConfigIterator(SendFlowerAchieve.class);
		while (configIterator.hasNext()) {
			SendFlowerAchieve next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		entity.notifyUpdate();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
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
		Optional<SendFlowerEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		SendFlowerEntity playerDataEntity = opPlayerDataEntity.get();
		if (playerDataEntity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(SendFlowerAchieve.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(SendFlowerAchieve.class, achieveId);
		}
		return config;
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	/** 攻击boss
	 * 
	 * @param event */
	@Subscribe
	public synchronized void onSongHua(SendFlowerEvent event) {
		int killCnt = event.getNum();
		if (killCnt <= 0) {
			return;
		}
		{
			String playerId = event.getPlayerId();
			Optional<SendFlowerEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
			if (!opPlayerDataEntity.isPresent()) {
				return;
			}
			SendFlowerEntity dataEntity = opPlayerDataEntity.get();
			dataEntity.setSongHua(dataEntity.getSongHua() + killCnt);
			int termId = getActivityTermId();
			songHuaRank.addRankScore(playerId, termId, killCnt);
			recordSongHua(event.getPlayerId(), event.getToPlayerId(), termId, killCnt);
			HawkLog.logPrintln("SendFlowerActivity send playerId: {},toId:{} add:{} ,score:{} ", playerId, event.getToPlayerId(), killCnt, dataEntity.getSongHua());
		}
		{
			String playerId = event.getToPlayerId();
			Optional<SendFlowerEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
			if (!opPlayerDataEntity.isPresent()) {
				return;
			}
			SendFlowerEntity dataEntity = opPlayerDataEntity.get();
			dataEntity.setShouHua(dataEntity.getShouHua() + killCnt);
			int termId = getActivityTermId();
			shouHuaRank.addRankScore(playerId, termId, killCnt);
			recordShouHua(event.getPlayerId(), event.getToPlayerId(), termId, killCnt);
			HawkLog.logPrintln("SendFlowerActivity shou playerId: {},fromId:{} add:{} ,score:{} ", playerId, event.getPlayerId(), killCnt, dataEntity.getShouHua());
		}
	}

	private void recordSongHua(String sId, String rId, int termId, int killCnt) {
		// 收花人信息
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String name = dataGeter.getPlayerName(rId);
		String guildTag = dataGeter.getGuildTagByPlayerId(rId);

		byte[] recordKey = getRecordKey(sId, termId, PBSendFlowerType.SONG_HUA_TYPE);
		PBSendFlowerRecordInfo.Builder value = PBSendFlowerRecordInfo.newBuilder()
				.setName(name)
				.setGuildTag(guildTag)
				.setScore(killCnt)
				.setTime(HawkTime.getMillisecond());
		ActivityLocalRedis.getInstance().lpush(recordKey, RECORD_EXPIRE, value.build().toByteArray());
	}

	private void recordShouHua(String sId, String rId, int termId, int killCnt) {
		// 送花人信息
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String name = dataGeter.getPlayerName(sId);
		String guildTag = dataGeter.getGuildTagByPlayerId(sId);

		byte[] recordKey = getRecordKey(rId, termId, PBSendFlowerType.SHOU_HUA_TYPE);
		PBSendFlowerRecordInfo.Builder value = PBSendFlowerRecordInfo.newBuilder()
				.setName(name)
				.setGuildTag(guildTag)
				.setScore(killCnt)
				.setTime(HawkTime.getMillisecond());
		ActivityLocalRedis.getInstance().lpush(recordKey, RECORD_EXPIRE, value.build().toByteArray());
	}

	byte[] getRecordKey(String playerId, int termId, PBSendFlowerType rankType) {
		String key = "";
		switch (rankType) {
		case SONG_HUA_TYPE:
			key = ActivityRedisKey.SONG_HUA_RANK;
			break;
		case SHOU_HUA_TYPE:
			key = ActivityRedisKey.SHOU_HUA_RANK;
			break;
		}
		return (key + playerId + ":" + termId).getBytes();
	}

	@Override
	public void onTick() {
		long currTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if (currTime >= endTime) {
			return;
		}
		SendFlowerActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SendFlowerActivityKVCfg.class);
		long gap = kvCfg.getRankPeriod();

		if (currTime - lastCheckTime > gap) {
			refreshRankInfo(termId);
			lastCheckTime = currTime;
		}
	}

	// 刷新榜单数据
	private void refreshRankInfo(int termId) {
		songHuaRank.refreshRank(termId);
		shouHuaRank.refreshRank(termId);

	}

	@Override
	public void onEnd() {
		int termId = getActivityTermId();
		// 通知GameServer活动结束
		HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_SERVER_ACTIVITY),
				ActivityStateChangeMsg.valueOf(getActivityType(), termId, ActivityState.END));
		sendReward(termId);
	}

	private void sendReward(int termId) {
		sendSongRankReward(termId);
		sendShouRankReward(termId);
	}

	/** 发送个人排行奖励
	 * 
	 * @param termId */
	private void sendSongRankReward(int termId) {
		try {
			songHuaRank.refreshRank(termId);
			MailId mailId = MailId.SEND_FLOWER_SONG_REWARD;
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = songHuaRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String playerId = tuple.getElement();
				SendFlowerRankRewardCfg rankCfg = getRankReward(rank, songHuaRank.rankType);
				if (rankCfg == null) {
					HawkLog.errPrintln("SendFlowerActivity self rank cfg error! playerId: {}, rank :{} , score:{}", playerId, rank, tuple.getScore());
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[2];
				content[0] = getActivityCfg().getActivityName();
				content[1] = rank;

				sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
				HawkLog.logPrintln("SendFlowerActivity send self rankReward, playerId: {}, rank: {}, cfgId: {}, score:{}", playerId, rank, rankCfg.getId(), tuple.getScore());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 发送个人排行奖励
	 * 
	 * @param termId */
	private void sendShouRankReward(int termId) {
		try {
			shouHuaRank.refreshRank(termId);
			MailId mailId = MailId.SEND_FLOWER_SHOU_REWARD;
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = shouHuaRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String playerId = tuple.getElement();
				SendFlowerRankRewardCfg rankCfg = getRankReward(rank, shouHuaRank.rankType);
				if (rankCfg == null) {
					HawkLog.errPrintln("SendFlowerActivity self rank cfg error! playerId: {}, rank :{}", playerId, rank);
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[2];
				content[0] = getActivityCfg().getActivityName();
				content[1] = rank;

				sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
				HawkLog.logPrintln("SendFlowerActivity send self rankReward, playerId: {}, rank: {}, cfgId: {}, score:{}", playerId, rank, rankCfg.getId(), tuple.getScore());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 获取排行奖励配置
	 * 
	 * @param rank
	 * @return */
	private SendFlowerRankRewardCfg getRankReward(int rank, PBSendFlowerType rankType) {
		SendFlowerRankRewardCfg rankCfg = null;
		ConfigIterator<SendFlowerRankRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SendFlowerRankRewardCfg.class);
		for (SendFlowerRankRewardCfg cfg : configIterator) {
			if (rankType.getNumber() == cfg.getRankType() && rank <= cfg.getRankLower() && rank >= cfg.getRankUpper()) {
				rankCfg = cfg;
				break;
			}
		}
		return rankCfg;
	}

	/** 拉取榜单信息
	 * 
	 * @param playerId
	 * @param rankType */
	public void pullRankInfo(String playerId, PBSendFlowerType rankType) {
		Optional<SendFlowerEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SendFlowerEntity entity = opEntity.get();
		PBSendFlowerRankInfoResp.Builder builder = null;
		switch (rankType) {
		case SONG_HUA_TYPE:
			builder = songHuaRank.buildRankInfoResp(entity);
			break;

		case SHOU_HUA_TYPE:
			builder = shouHuaRank.buildRankInfoResp(entity);
			break;
		}

		if (builder != null) {
			pushToPlayer(playerId, HP.code.SEND_FLOWER_RANK_S_VALUE, builder);
		}

	}

	public void pullRecordInfo(String playerId, PBSendFlowerType rankType) {
		int termId = getActivityTermId();
		byte[] key = getRecordKey(playerId, termId, rankType);
		List<byte[]> valueLst = ActivityLocalRedis.getInstance().lall(key);
		PBSendFlowerRecordResp.Builder resp = PBSendFlowerRecordResp.newBuilder();
		resp.setRankType(rankType);
		for (byte[] bys : valueLst) {
			try {
				PBSendFlowerRecordInfo.Builder bul = PBSendFlowerRecordInfo.newBuilder().mergeFrom(bys);
				resp.addRecordInfo(bul);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		pushToPlayer(playerId, HP.code.SEND_FLOWER_RECORD_S_VALUE, resp);
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

	@Override
	public boolean handleForMergeServer() {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return true;
		}

		int termId = this.getActivityTermId();
		sendReward(termId);

		return true;
	}
	
	/**
	 * 删除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return;
		}
		int termId = this.getActivityTermId();
		songHuaRank.removeRank(playerId, termId);
		songHuaRank.refreshRank(termId);
		shouHuaRank.removeRank(playerId, termId);
		shouHuaRank.refreshRank(termId);
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}
