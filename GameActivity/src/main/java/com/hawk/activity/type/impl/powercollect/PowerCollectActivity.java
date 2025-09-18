package com.hawk.activity.type.impl.powercollect;

import java.util.ArrayList;
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
import org.hawk.os.HawkOSOperator;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.activity.event.impl.PowerLabItemDropAchieveEvent;
import com.hawk.activity.event.impl.PowerLabItemDropEvent;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.powercollect.cache.GuildScore;
import com.hawk.activity.type.impl.powercollect.cache.PersonScore;
import com.hawk.activity.type.impl.powercollect.cfg.PowerCollectAchieveAwardCfg;
import com.hawk.activity.type.impl.powercollect.cfg.PowerCollectGuildRankAwardCfg;
import com.hawk.activity.type.impl.powercollect.cfg.PowerCollectKVCfg;
import com.hawk.activity.type.impl.powercollect.cfg.PowerCollectPersonRankAwardCfg;
import com.hawk.activity.type.impl.powercollect.entity.PowerCollectEntity;
import com.hawk.activity.type.impl.powercollect.rank.PowerCollectRank;
import com.hawk.activity.type.impl.powercollect.rank.impl.GuildRank;
import com.hawk.activity.type.impl.powercollect.rank.impl.PersonRank;
import com.hawk.game.protocol.Activity.PowerCollectGuildRank;
import com.hawk.game.protocol.Activity.PowerCollectInfo;
import com.hawk.game.protocol.Activity.PowerCollectPersonRank;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

/***
 * 能量收集活动
 * @author yang.rao
 *
 */
public class PowerCollectActivity extends ActivityBase implements AchieveProvider{
	
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	public List<PowerCollectRank<?>> rankList = new ArrayList<>();
	
	public static final int expireTime = 7 * 86400; //7天的秒数

	public PowerCollectActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SUPER_POWER_LAB;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			initAchieveItems(playerId);
		}
	}
	
	/****
	 * 同步排行榜信息
	 * @param playerId
	 */
	public void syncRankInfo(String playerId){
		Optional<PowerCollectEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		PowerCollectInfo.Builder info = PowerCollectInfo.newBuilder();
		buildPersonTotalRank(info);
		buildGuildRank(info);
		buildMyRank(playerId, info);
		String guildId = getDataGeter().getGuildId(playerId);
		if(!HawkOSOperator.isEmptyString(guildId)){
			buildMyGuildRank(guildId, info);
		}
		PowerCollectEntity entity = opEntity.get();
		info.setCollectCnt(entity.getCollectCnt());
		pushToPlayer(playerId, HP.code.POWER_COLLECT_INFO_S_VALUE, info);
	}
	
	/***
	 * 构建个人榜
	 * @param rankBuilder
	 */
	@SuppressWarnings("unchecked")
	private void buildPersonTotalRank(PowerCollectInfo.Builder builder){
		List<PersonScore> personList = null;
		for(PowerCollectRank<?> rank : rankList){
			if(rank instanceof PersonRank){
				personList = (List<PersonScore>) rank.getRankList();
			}
		}
		if(personList == null){
			return;
		}
		int rank = 0;
		for(PersonScore data : personList){
			try {
				rank ++;
				PowerCollectPersonRank.Builder personBuilder = PowerCollectPersonRank.newBuilder();
				String playerName = getDataGeter().getPlayerName(data.getElement());
				String guildName = getDataGeter().getGuildNameByByPlayerId(data.getElement());
				String guildTag = getDataGeter().getGuildTagByPlayerId(data.getElement());
				if(!HawkOSOperator.isEmptyString(playerName)){
					personBuilder.setPlayerName(playerName);
					personBuilder.setPlayerId(data.getElement());
					personBuilder.addAllPersonalProtectSwitch(getDataGeter().getPersonalProtectVals(data.getElement()));
				}
				if(!HawkOSOperator.isEmptyString(guildName)){
					personBuilder.setGuildName(guildName);
				}
				if(!HawkOSOperator.isEmptyString(guildTag)){
					personBuilder.setGuildTag(guildTag);
				}
				personBuilder.setScore((long)data.getScore());
				personBuilder.setRank(rank);
				builder.addPersonRank(personBuilder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}

		}
	}
	
	/***
	 * 构建联盟榜
	 * @param rankBuilder
	 */
	@SuppressWarnings("unchecked")
	private void buildGuildRank(PowerCollectInfo.Builder builder) {
		List<GuildScore> guildList = null;
		for (PowerCollectRank<?> rank : rankList) {
			if (rank instanceof GuildRank) {
				guildList = (List<GuildScore>) rank.getRankList();
			}
		}
		if (guildList == null) {
			return;
		}
		int rank = 1;
		for (GuildScore guildScore : guildList) {
			PowerCollectGuildRank.Builder guildBuilder = PowerCollectGuildRank.newBuilder();
			String guildId = guildScore.getElement();
			guildBuilder.setRank(rank);
			guildBuilder.setScore(guildScore.getScore());
			String guildName = getDataGeter().getGuildName(guildId);
			String guildTag = getDataGeter().getGuildTag(guildId);
			if (HawkOSOperator.isEmptyString(guildName)) {
				continue;
			}
			guildBuilder.setGuildName(guildName);
			if (!HawkOSOperator.isEmptyString(guildTag)) {
				guildBuilder.setGuildTag(guildTag);
			}
			guildBuilder.setGuildFlag(getDataGeter().getGuildFlat(guildId));
			builder.addGuildRank(guildBuilder);
			rank++;
		}
	}
	
	/***
	 * 构建我的个人排名
	 * @param playerId
	 */
	private void buildMyRank(String playerId, PowerCollectInfo.Builder builder){
		String key = String.format(ActivityRedisKey.POWER_COLLECT_PERSON_RANK, getActivityTermId());
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(key, playerId);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			//score = RankScoreHelper.getRealScore(index.getScore().longValue());
			score = index.getScore().longValue();
		}
		PowerCollectPersonRank.Builder myBuilder = PowerCollectPersonRank.newBuilder();
		myBuilder.setRank(rank);
		myBuilder.setScore(score);
		builder.setMyRank(myBuilder);
	}
	
	/***
	 * 构建我的联盟排行
	 * @param guildId
	 */
	private void buildMyGuildRank(String guildId, PowerCollectInfo.Builder builder){
		String key = String.format(ActivityRedisKey.POWER_COLLECT_GUILD_RANK, getActivityTermId());
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(key, guildId);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			//score = RankScoreHelper.getRealScore(index.getScore().longValue());
			score = index.getScore().longValue();
		}
		PowerCollectGuildRank.Builder guildBuilder = PowerCollectGuildRank.newBuilder();
		guildBuilder.setRank(rank);
		guildBuilder.setScore(score);
		guildBuilder.setGuildName(getDataGeter().getGuildName(guildId));
		guildBuilder.setGuildTag(getDataGeter().getGuildTag(guildId));
		guildBuilder.setGuildFlag(getDataGeter().getGuildFlat(guildId));
		builder.setMyGuildRank(guildBuilder);
	}

	@Override
	public void onOpen() {
		initRankList();
		Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
		for(String id : onlinePlayers){
			callBack(id, GameConst.MsgId.INIT_POWER_COLLECT_ACHIEVE, ()->{
				initAchieveItems(id);
			});
		}
	}
	
	@Override
	public void onTick() {
		for(PowerCollectRank<?> rank : rankList){
			rank.doRankSort(); //排序
		}
	}

	private void initRankList(){
		rankList.clear();
		PowerCollectRank<?> personRank = new PersonRank();
		PowerCollectRank<?> guildRank = new GuildRank();
		rankList.add(personRank);
		rankList.add(guildRank);
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<PowerCollectEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		String playerId = event.getPlayerId();
		logger.info("PowerCollectActivity ContinueLoginEvent, playerId:{},entity:{}", playerId, opEntity.get());
		PowerCollectEntity entity = opEntity.get();
		entity.resetAchieve();
		initAchieveItems(playerId);
		entity.notifyUpdate();
	}
	
	@Subscribe
	public void collectItemEvent(PowerLabItemDropEvent event){
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		int itemId = event.getItemId();
		PowerCollectKVCfg config = HawkConfigManager.getInstance().getKVInstance(PowerCollectKVCfg.class);
		if(itemId != config.getItemId()){
			logger.error("PowerCollectActivity PowerLabItemDropEvent recieve error itemID:{},playerId:{}", event.getItemId(), event.getPlayerId());
			return;
		}
		
		if(event.getCount() <= 0){
			logger.error("PowerCollectActivity PowerLabItemDropEvent recieve error item count:{}", event.getCount());
			return;
		}
		Optional<PowerCollectEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		PowerCollectEntity entity = opEntity.get();
		entity.addCollectCnt(event.getCount());
		entity.notifyUpdate();
		ActivityManager.getInstance().postEvent(new PowerLabItemDropAchieveEvent(playerId, itemId, event.getCount()));
		logger.info("PowerCollectActivity player:{},increase item count:{}", playerId, event.getCount());
		for(PowerCollectRank<?> rank : rankList){
			if(rank instanceof PersonRank){
				rank.addScore(event.getCount(), playerId);
			}
			if(rank instanceof GuildRank){
				String guildId = getDataGeter().getGuildId(playerId);
				if(!HawkOSOperator.isEmptyString(guildId)){
					rank.addScore(event.getCount(), guildId);
				}
			}
		}
	}
	
	/**
	 * 联盟解散,移除联盟排行数据
	 * @param event
	 */
	@Subscribe
	public void onGuildDismiss(GuildDismissEvent event) {
		String guildId = event.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		if (isHidden(event.getPlayerId())) {
			return;
		}
		for (PowerCollectRank<?> rank : rankList) {
			if (rank instanceof GuildRank) {
				rank.remove(guildId);
			}
		}
	}
	
	
	/***
	 * 初始化成就
	 * @param playerId
	 * @return
	 */
	public void initAchieveItems(String playerId) {
		Optional<PowerCollectEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		PowerCollectEntity playerDataEntity = opPlayerDataEntity.get();
		if (playerDataEntity.getItemList().isEmpty()) {
			ConfigIterator<PowerCollectAchieveAwardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PowerCollectAchieveAwardCfg.class);
			List<AchieveItem> itemList = new ArrayList<>();
			while (configIterator.hasNext()) {
				PowerCollectAchieveAwardCfg cfg = configIterator.next();				
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());				
				itemList.add(item);
			}
			playerDataEntity.setItemList(itemList);
			ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, playerDataEntity.getItemList()), true);
		}
	}

	@Override
	public void onEnd() {
		int termId = getActivityTermId(); //这个时候获取termid，还是能获取到正确的值
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			
			@Override
			public Object run() {
				sendReward(termId);
				return null;
			}
		});
		for(PowerCollectRank<?> rank : rankList){
			rank.clear();
		}
	}
	
	/**
	 * 活动结束后发奖.
	 * @param termId
	 */
	private void sendReward(int termId) {
		try {
			sendPersonRewardMail(MailId.POWER_LAB_COLLECT_PERSON_RANK, new Object[0], new Object[0], termId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		sendGuildRewardMail(MailId.POWER_LAB_COLLECT_GUILD_RANK, new Object[0], new Object[0], termId);
	}
	
	/***
	 * 发送个人排名奖励
	 * @param stageCfg
	 * @param rankId
	 * @param mailId
	 * @param title
	 * @param subTitle
	 * @param rank
	 */
	@SuppressWarnings("unchecked")
	private void sendPersonRewardMail(MailId mailId, Object[] title, Object[] subTitle, int termId){
		List<PowerCollectPersonRankAwardCfg> rankListReward = getPeronRankRewardConfig();
		int high = getPersonRankHasRewardMaxRank();
		if(high == 0){
			logger.error("PowerCollectActivity sendPersonRewardMail rankReward error, config error");
			return;
		}
		PowerCollectRank<?> rank = null;
		for(PowerCollectRank<?> pwrank : rankList){
			if(pwrank instanceof PersonRank){
				rank = pwrank;
			}
		}
		List<PersonScore> rankPlayers = (List<PersonScore>) rank.getHasRewardRankList(termId, high);
		int maxRank = 0;
		for (PersonScore s : rankPlayers) {
			maxRank++;
			for (PowerCollectPersonRankAwardCfg rewardCfg : rankListReward) {
				if (rewardCfg.getRankLow() >= maxRank && rewardCfg.getRankHigh() <= maxRank) {
					try {
						Object[] content;
						content = new Object[2];
						content[0] = getActivityCfg().getActivityName();
						content[1] = maxRank;
						sendMailToPlayer(s.getElement(), mailId, title, subTitle, content, rewardCfg.getRewardList());
						logger.info("PowerCollectActivity send rankReward, playerId:{}, rank:{}, cfgId:{}", s.getElement(), maxRank,
								rewardCfg.getId());
					} catch (Exception e) {
						HawkException.catchException(e);
					}

				}
			}
		}
	}
	
	/***
	 * 发送联盟排行奖励
	 * @param stageCfg
	 * @param rankId
	 * @param mailId
	 * @param title
	 * @param subTitle
	 * @param rank
	 */
	@SuppressWarnings("unchecked")
	private void sendGuildRewardMail(MailId mailId, Object[] title, Object[] subTitle, int termId){
		List<PowerCollectGuildRankAwardCfg> rankListReward = getGuildRankRewardConfig();
		int high = getGuildRankHasRewardMaxRank();
		if(high == 0){
			logger.error("PowerCollectActivity sendGuildRewardMail rankReward error, config error");
			return;
		}
		PowerCollectRank<?> rank = null;
		for(PowerCollectRank<?> pwrank : rankList){
			if(pwrank instanceof GuildRank){
				rank = pwrank;
			}
		}
		List<GuildScore> rankGuild = (List<GuildScore>) rank.getHasRewardRankList(termId, high);
		int maxRank = 0;
		for(GuildScore s : rankGuild){
			maxRank ++;
			for (PowerCollectGuildRankAwardCfg rewardCfg : rankListReward) {
				if (rewardCfg.getRankLow() >= maxRank && rewardCfg.getRankHigh() <= maxRank) {
					// 给联盟所有的人都要发奖励邮件
					Collection<String> members = getDataGeter().getGuildMemberIds(s.getElement());
					for (String playerId : members) {
						Object[] content = new Object[2];
						content[0] = getActivityCfg().getActivityName();
						content[1] = maxRank;
						sendMailToPlayer(playerId, mailId, title, subTitle, content, rewardCfg.getRewardList());
						logger.info("PowerCollectActivity send guild rank mail rankReward, playerId:{}, rank:{}, cfgId:{}, rank",
								playerId, maxRank, rewardCfg.getId(), rank);
					}
				}
			}
		}
	}
	
	private List<PowerCollectPersonRankAwardCfg> getPeronRankRewardConfig(){
		List<PowerCollectPersonRankAwardCfg> configList = new ArrayList<>();
		ConfigIterator<PowerCollectPersonRankAwardCfg> ite = HawkConfigManager.getInstance().getConfigIterator(PowerCollectPersonRankAwardCfg.class);
		while(ite.hasNext()){
			PowerCollectPersonRankAwardCfg config = ite.next();
			configList.add(config);
		}
		return configList;
	}

	/***
	 * 获取个人榜最次的上榜排名
	 * @return
	 */
	private int getPersonRankHasRewardMaxRank(){
		int max = 0;
		ConfigIterator<PowerCollectPersonRankAwardCfg> ite = HawkConfigManager.getInstance().getConfigIterator(PowerCollectPersonRankAwardCfg.class);
		while(ite.hasNext()){
			PowerCollectPersonRankAwardCfg config = ite.next();
			if(config.getRankLow() > max){
				max = config.getRankLow();
			}
		}
		return max;
	}
	
	private List<PowerCollectGuildRankAwardCfg> getGuildRankRewardConfig(){
		List<PowerCollectGuildRankAwardCfg> configList = new ArrayList<>();
		ConfigIterator<PowerCollectGuildRankAwardCfg> ite = HawkConfigManager.getInstance().getConfigIterator(PowerCollectGuildRankAwardCfg.class);
		while(ite.hasNext()){
			PowerCollectGuildRankAwardCfg config = ite.next();
			configList.add(config);
		}
		return configList;
	}
	
	/***
	 * 获取联盟榜最次的上榜排名
	 * @return
	 */
	private int getGuildRankHasRewardMaxRank(){
		int max = 0;
		ConfigIterator<PowerCollectGuildRankAwardCfg> ite = HawkConfigManager.getInstance().getConfigIterator(PowerCollectGuildRankAwardCfg.class);
		while(ite.hasNext()){
			PowerCollectGuildRankAwardCfg config = ite.next();
			if(config.getRankLow() > max){
				max = config.getRankLow();
			}
		}
		return max;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PowerCollectActivity activity = new PowerCollectActivity(config.getActivityId(), activityEntity);
		activity.initRankList();
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PowerCollectEntity> queryList = HawkDBManager.getInstance()
				.query("from PowerCollectEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PowerCollectEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PowerCollectEntity entity = new PowerCollectEntity(playerId, termId);
		return entity;
	}

	
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
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
		Optional<PowerCollectEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		PowerCollectEntity entity = opEntity.get();
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(PowerCollectAchieveAwardCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.POWER_COLLECT_ACHIEVE_REWARD;
	}
	
	@Override
	public boolean handleForMergeServer() {
		if (this.getActivityEntity().getActivityState() == ActivityState.HIDDEN) {
			return true;
		}
		
		int termId = this.getActivityTermId();
		sendReward(termId);
		
		return true;
	}
	
	public void removePlayerRank(String playerId) {
		if (isActivityClose(null)) {
			return;
		}
		try {			
			for(PowerCollectRank<?> rank : rankList){
				if(rank instanceof PersonRank){
					rank.remove(playerId);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}
