package com.hawk.activity.type.impl.alliancecelebrate;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.GuildQuiteEvent;
import com.hawk.activity.event.impl.JoinGuildEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.alliancecelebrate.cfg.AllianceCelebrateKVCfg;
import com.hawk.activity.type.impl.alliancecelebrate.cfg.AllianceCelebrateLevelCfg;
import com.hawk.activity.type.impl.alliancecelebrate.entity.AllianceCelebrateEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.AllianceCelebrateInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @desc 双十一-联盟欢庆
 * 以联盟为单位,玩家用物品进行捐献,
 * 增加经验提升等级,领取奖励(对玩家的贡献度有一定的限制),
 * 排行榜为全服联盟的榜
 * @author hf
 */
public class AllianceCelebrateActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	public AllianceCelebrateActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	/** 上次排行刷新时间*/
	private long lastCheckTime = 0;

	/** 第一列的普通奖励*/
	private static final int COMMON_REWARD_INDEX = 1;
	/** 第二列的高级奖励*/
	private static final int GREAT_REWARD_INDEX = 2;

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ALLIANCE_CELEBRATE_ACTIVITY;
	}


	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_ALLIANCE_CELEBRATE, ()-> {
				//初始化,联盟经验都是0
				Optional<AllianceCelebrateEntity> opDataEntity = getPlayerDataEntity(playerId);
				if (opDataEntity.isPresent()) {
					this.syncActivityInfo(playerId, 0,opDataEntity.get());
				}
			});
		}
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<AllianceCelebrateEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			long guildExp = this.getGuildExp(playerId);
			String guildId= this.getDataGeter().getGuildId(playerId);
			//同步的时候检测,如果没有联盟,清掉玩家个人积分
			AllianceCelebrateEntity entity = opDataEntity.get();
			if (StringUtils.isEmpty(guildId)){
				entity.setDonate(0);
				entity.notifyUpdate();
			}
			syncActivityInfo(playerId,guildExp, entity);
		}
	}


	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		AllianceCelebrateActivity activity = new AllianceCelebrateActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<AllianceCelebrateEntity> queryList = HawkDBManager.getInstance()
				.query("from AllianceCelebrateEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			AllianceCelebrateEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		AllianceCelebrateEntity entity = new AllianceCelebrateEntity(playerId, termId);
		return entity;
	}
	
	/**
	 * 玩家贡献
	 * @param playerId
	 * @param num 捐献个数
	 * @param protocolType
	 */
	public void donationAllianceExpReq(String playerId,int num, int protocolType){
		Optional<AllianceCelebrateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		String guildId = this.getDataGeter().getGuildId(playerId);
		if (StringUtils.isEmpty(guildId)){
			sendErrorAndBreak(playerId, protocolType, Status.Error.GUILD_NOT_EXIST_VALUE);
			logger.info("AllianceCelebrateActivity,donationExp fail guild null playerId:{}, num:{}",playerId, num);
			return;
		}
		AllianceCelebrateEntity entity = opEntity.get();

		AllianceCelebrateKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceCelebrateKVCfg.class);
		//捐献消耗物品
		List<RewardItem.Builder> makeCost = RewardHelper.toRewardItemImmutableList(cfg.getDonateItem());
		boolean cost = this.getDataGeter().cost(playerId, makeCost, num, Action.ALLIANCE_CELEBRATE_DONATION_COST, true);
		if (!cost) {
			sendErrorAndBreak(playerId, protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			logger.info("AllianceCelebrateActivity,donationExp fail cost null playerId:{}, num:{}",playerId, num);
			return;
		}
		int addExp = cfg.getDonateExp() * num;
		//加经验
		entity.addDonate(addExp);
		entity.notifyUpdate();
		long guildExp = 0;
		if(!HawkOSOperator.isEmptyString(guildId)){
			//加经验
			guildExp = this.addGuildExp(guildId, addExp);
			//添加联盟积分
			//guildRank.addRankScore(guildId, getActivityTermId(), addExp);
		}
		if(guildId == null){
			guildId = "";
		}
		//同步信息
		this.syncActivityInfo(playerId, guildExp,entity);
		//日志记录
		int termId = this.getActivityTermId();
		this.getDataGeter().logAllianceCelebrateScore(playerId, guildId, termId, addExp, entity.getDonate(), (int)guildExp);
		logger.info("AllianceCelebrateActivity,donate success guildId:{},playerId:{},num:{},addExp:{}, playerExp:{}, guildExp:{}",guildId, playerId, num, addExp,entity.getDonate(), guildExp);
	}

	/**
	 * 领取奖励
	 * @param playerId
	 * @param req
	 * @return
	 */
	public Result<?> receiveAllianceCelebrateReward(String playerId, Activity.AllianceCelebrateRewardReq req){
		String info = req.getGetRewardInfo();
		String[] infoArr= info.split("_");
		/**参数错误*/
		if (infoArr.length < 2){
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		/**奖励对应的等级*/
		int level = NumberUtils.toInt(infoArr[0]);
		int allianceLv = getAllianceCelebrateLv(playerId);
		if (level > allianceLv){
			return Result.fail(Status.Error.ALLIANCE_CELEBRATE_REWARD_LV_LIMIT_VALUE);
		}
		/**第几个奖励*/
		int index = NumberUtils.toInt(infoArr[1]);
		Optional<AllianceCelebrateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		AllianceCelebrateEntity entity = opEntity.get();
		/**已经领过奖的数据*/
		List<String> hasRewardList = entity.getRewardInfoList();
		/**已领过此奖励*/
		if (hasRewardList.contains(info)){
			return Result.fail(Status.Error.ALLIANCE_CELEBRATE_REWARD_RECEIVED_VALUE);
		}
		AllianceCelebrateLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCelebrateLevelCfg.class, level);
		/**个人贡献的条件值*/
		int personCondition = getPersonCondition(index, levelCfg);
		int donateNum = entity.getDonate();
		if (donateNum < personCondition){
			return Result.fail(Status.Error.ALLIANCE_CELEBRATE_REWARD_DONATE_LIMIT_VALUE);
		}
		/**修改entity数据*/
		entity.addRewardInfo(info);
		/**发奖*/
		List<Reward.RewardItem.Builder> rewardList = getRewardAlliance(index, levelCfg);
		this.getDataGeter().takeReward(playerId, rewardList, Action.ALLIANCE_CELEBRATE_DONATION_REWARD, true);
		/**同步信息*/
		syncActivityDataInfo(playerId);
		this.getDataGeter().logAllianceCelebrateReward(playerId, getActivityTermId(), level, index);
		logger.info("AllianceCelebrateActivity, reward success ,playerId:{},info:{},donateNum:{},allianceLv:{}", playerId, info,donateNum,allianceLv);
		return Result.success();
	}


	/**
	 * 联盟退出
	 * @param event
	 */
	@Subscribe
	public void onGuildQuite(GuildQuiteEvent event){
		String playerId = event.getPlayerId();
		Optional<AllianceCelebrateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		AllianceCelebrateEntity entity = opEntity.get();
		entity.setDonate(0);
		entity.notifyUpdate();
		syncActivityDataInfo(event.getPlayerId());
	}

	/**
	 * 联盟加入
	 * @param event
	 */
	@Subscribe
	public void onGuildQuite(JoinGuildEvent event){
		String playerId = event.getPlayerId();
		Optional<AllianceCelebrateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		syncActivityDataInfo(event.getPlayerId());
	}

	/**
	 * 同步信息
	 * @param playerId
	 * @param guildExp
	 * @param entity
	 */
	public void syncActivityInfo(String playerId, long guildExp, AllianceCelebrateEntity entity){
		HawkTuple2<Integer, Integer> level = this.guildLevelAndExp(guildExp);
		AllianceCelebrateInfoResp.Builder builder = AllianceCelebrateInfoResp.newBuilder();
		builder.setLevel(level.first);
		builder.setExp((int) guildExp);
		builder.setPersonExp(entity.getDonate());
		List<String> rewardList = entity.getRewardInfoList();
		for(String info : rewardList){
			builder.addHasRewardInfo(info);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.ALLIANCE_CELEBRATE_INFO_SYNC, builder));
	}

	/**
	 * 联盟欢庆的当前等级
	 * @param playerId
	 * @return
	 */
	public int getAllianceCelebrateLv(String playerId){
		long guildExp = this.getGuildExp(playerId);
		HawkTuple2<Integer, Integer> level = this.guildLevelAndExp(guildExp);
		return level.first;
	}
	/**
	 * 获取Exp
	 * @param playerId
	 * @return
	 */
	public long getGuildExp(String playerId){
		String guildId = this.getDataGeter().getGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			return 0;
		}
		String key = this.getGuildExpKey();
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(key, guildId);
		if (index != null) {
			long exp = index.getScore().longValue();
			return exp;
		}
		return 0;
	}

	/**
	 * 添加Exp
	 * @param guildId
	 * @return
	 */
	public long addGuildExp(String guildId,int exp){
		String key = this.getGuildExpKey();
		Double add = ActivityGlobalRedis.getInstance().getRedisSession().
				zIncrby(key, guildId, exp,(int)TimeUnit.DAYS.toSeconds(30));
		return add.longValue();
	}
	
	/**
	 * 联盟经验key
	 * @return
	 */
	public String getGuildExpKey(){
		int termId = this.getActivityTermId();
		return ActivityRedisKey.ALLIANCE_CELEBRATE_EXP+":"+termId;
	}
	
	/**
	 * 根据联盟总经验算出等级和当前的经验
	 * @param guildExp
	 */
	public HawkTuple2<Integer, Integer> guildLevelAndExp(long guildExp){
		List<AllianceCelebrateLevelCfg> configList = HawkConfigManager.getInstance()
				.getConfigIterator(AllianceCelebrateLevelCfg.class).toList();
		int level = 0;
		int exp = (int)guildExp;
		for(AllianceCelebrateLevelCfg cfg : configList){
			if(guildExp >= cfg.getAllianceExp() && cfg.getLv() > level){
				level = cfg.getLv();
				exp = (int) (guildExp - cfg.getAllianceExp());
			}
		}
		return new HawkTuple2<>(level,exp);
	}

	/**
	 * 根据奖励的第几列获取个人需要满足的条件
	 * @param index
	 * @param cfg
	 * @return
	 */
	public int getPersonCondition(int index, AllianceCelebrateLevelCfg cfg) {
		if (index == COMMON_REWARD_INDEX){
			return cfg.getPersonExpPt1();
		}else if(index == GREAT_REWARD_INDEX){
			return cfg.getPersonExpPt2();
		}
		return 0;
	}

	/**
	 * 获取奖励
	 * @param index
	 * @param cfg
	 * @return
	 */
	public List<RewardItem.Builder> getRewardAlliance(int index, AllianceCelebrateLevelCfg cfg) {
		if (index == COMMON_REWARD_INDEX){
			return cfg.getAwardItem1();
		}else if(index == GREAT_REWARD_INDEX){
			return cfg.getAwardItem2();
		}
		return null;
	}
}
