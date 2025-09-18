package com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GuildDonateEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.cfg.DragonBoatCelebrationDropCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.cfg.DragonBoatCelebrationExchangeCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.cfg.DragonBoatCelebrationLevelCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.entity.DragonBoatCelebrationEntity;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.rank.DragonBoatCelebrationRank;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.rank.DragonBoatCelebrationRankMember;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.CeletrationRankMember;
import com.hawk.game.protocol.Activity.DragonBoatCeletrationInfoResp;
import com.hawk.game.protocol.Activity.DragonBoatCeletrationRankResp;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;
/**
 * 端午-联盟庆典
 * @author che
 *
 */
public class DragonBoatCelebrationActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	

	public DragonBoatCelebrationActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DRAGON_BOAT_CELERATION_ACTIVITY;
	}

	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.DRAGON_BOAT_CELEBRATION_INIT, ()-> {
				//初始化,联盟经验都是0
				Optional<DragonBoatCelebrationEntity> opDataEntity = getPlayerDataEntity(playerId);
				if (opDataEntity.isPresent()) {
					this.syncActivityInfo(playerId, 0,opDataEntity.get());
				}
			});
		}
	}
	

	
	/**
	 * 检查奖励发放
	 * @param score
	 */
	public HawkTuple2<Integer, Integer> guildLevelAndExp(long guildExp){
		List<DragonBoatCelebrationLevelCfg> configList = HawkConfigManager.getInstance()
				.getConfigIterator(DragonBoatCelebrationLevelCfg.class).toList();
		int level = 0;
		int exp = (int)guildExp;
		for(DragonBoatCelebrationLevelCfg cfg : configList){
			if(guildExp >= cfg.getExp() && cfg.getLv() > level){
				level = cfg.getLv();
				exp = (int) (guildExp - cfg.getExp());
			}
			
		}
		return new HawkTuple2<Integer, Integer>(level,exp);
	}
	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DragonBoatCelebrationActivity activity = new DragonBoatCelebrationActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DragonBoatCelebrationEntity> queryList = HawkDBManager.getInstance()
				.query("from DragonBoatCelebrationEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DragonBoatCelebrationEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DragonBoatCelebrationEntity entity = new DragonBoatCelebrationEntity(playerId, termId);
		return entity;
	}
	
	/**
	 * 制作礼物
	 */
	public void makeGift(String playerId,int type,int count,int protocolType){
		DragonBoatCelebrationExchangeCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(DragonBoatCelebrationExchangeCfg.class,type);
		if (cfg == null) {
			return;
		}
		
		Optional<DragonBoatCelebrationEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DragonBoatCelebrationEntity entity = opEntity.get();
		if(cfg.getExchangeLimit() > 0 &&
				entity.getExchangeCount(type) >= cfg.getExchangeLimit() ){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.DRAGON_BOAT_CELEBREATION_MAKE_GIFT_LIMIT_VALUE);
			return;
		}
		List<RewardItem.Builder> makeCost = RewardHelper.toRewardItemList(cfg.getPay());
		boolean cost = this.getDataGeter().cost(playerId,makeCost,count, Action.DRAGON_BOAT_CELEBRATION_MAKE_GIFT, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			logger.info("DragonBoatCelebration,makeGift fail cost null:{}",playerId);
			return;
		}
		List<RewardItem.Builder> makeAchieve = RewardHelper.toRewardItemList(cfg.getGain());
		//加次数
		entity.addExchangeCount(type, count);
		//发奖励
		this.getDataGeter().takeReward(playerId,makeAchieve, count, Action.DRAGON_BOAT_CELEBRATION_MAKE_GIFT, true);
		String guildId = this.getDataGeter().getGuildId(playerId);
		long guildExp = 0;
		int expAdd = 0;
		if(!HawkOSOperator.isEmptyString(guildId)){
			//加经验
			expAdd = cfg.getExpGain() * count;
			guildExp = this.addGuildExp(playerId,guildId, expAdd);
			//添加进工会榜
			this.addToGuildRank(playerId, guildId, expAdd);
			//检查经验是否够升级
			this.checkAwardSend(guildId,guildExp);
		}
		if(guildId == null){
			guildId = "";
		}
		//同步信息
		this.syncActivityInfo(playerId, guildExp,entity);
		//日志记录
		int termId = this.getActivityTermId();
		this.getDataGeter().logDragonBoatCelebrationDonate(playerId, termId, guildId, type, expAdd,(int)guildExp);
		logger.info("DragonBoatCelebration,makeGift sucess guildId:{},playerId:{},type:{},expAdd:{}",guildId,playerId,type,expAdd);
	}
	
	/**
	 * 检查发奖励
	 * @param exp
	 */
	public void checkAwardSend(String guildId,long curExp){
		int index = Math.abs(guildId.hashCode()) % HawkTaskManager.getInstance().getExtraThreadNum();  
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				HawkTuple2<Integer, Integer> curLevel = guildLevelAndExp(curExp);
				Map<String,String>  records = getGuildAwardASend(guildId);
				Collection<String>  members = getDataGeter().getGuildMemberIds(guildId);
				Map<String,Map<String,String>> memberAwardMap = getGuildMemberAwardMap(members);
				if(memberAwardMap == null){
					logger.info("DragonBoatCelebration,CheckAwardSend,memberAwardMap,err,guildId:{},level:{}",guildId,curLevel);
					return null;
				}
				logger.info("DragonBoatCelebration,CheckAwardSend,begin,guildId:{},level:{},records:{}",guildId,curLevel,records);
				for(int level=1;level<= curLevel.first;level++){
					DragonBoatCelebrationLevelCfg cfg = HawkConfigManager.
							getInstance().getConfigByKey(DragonBoatCelebrationLevelCfg.class, level);
					if(cfg == null){
						continue;
					}
					if(records.containsKey(String.valueOf(level))){
						continue;
					}
					//添加发奖纪录
					addGuildAwardASend(guildId, level, HawkTime.getMillisecond());
					logger.info("DragonBoatCelebration,CheckAwardSend,addGuildAwardASend,guildId:{},level:{}",guildId,level);
					//发放奖励邮件
					sendGuildLevelUpAward(guildId,members, level, cfg.getReward(),memberAwardMap);
				}
				return null;
			}
		},index);
	}
	
	
	public void sendGuildLevelUpAward(String guildId,Collection<String> members,int level,String award,Map<String,Map<String,String>> memberAwardMap){
		if(members == null){
			return;
		}
		if (members.isEmpty()) {
			return;
		}
		StringBuilder builder = new StringBuilder();
		String levelKey = String.valueOf(level);
		Map<String,Integer> sendMap = new HashMap<String,Integer>();
		for (String playerId : members) {
			Map<String,String> map =  memberAwardMap.get(playerId);
			if(map != null && map.containsKey(levelKey)){
				logger.info("DragonBoatCelebration,CheckAwardSend,sendGuildLevelUpAward,"
						+ "alreadySend,guildId:{},level:{},playerId:{}",guildId,level,playerId);
				continue;
			}
			builder.append(playerId).append("_");
			sendMap.put(playerId, level);
			List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(award);
			Object[] content =  new Object[]{level};
			this.getDataGeter().sendMail(playerId, MailId.DRAGON_BOAT_CELEBRATION_LEVEL_UP_REWARD, null, null, content,
					rewardList, false);
		}
		this.addGuildMemberAwardMap(sendMap);
		int termId = this.getActivityTermId();
		this.getDataGeter().logDragonBoatCelebrateionLevelReward(termId, guildId, level, builder.toString());
		logger.info("DragonBoatCelebration,CheckAwardSend,sendGuildLevelUpAward,"
				+ "guildId:{},level:{},players:{}",guildId,level,builder.toString());
		
	}
	
	
	public void syncActivityInfo(String playerId) {
		Optional<DragonBoatCelebrationEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			long guildExp = this.getGuildExp(playerId);
			syncActivityInfo(playerId,guildExp,opDataEntity.get());
		}
	}
	
	public void syncActivityInfo(String playerId,long guildExp,DragonBoatCelebrationEntity entity){
		HawkTuple2<Integer, Integer> level = this.guildLevelAndExp(guildExp);
		DragonBoatCeletrationInfoResp.Builder builder = DragonBoatCeletrationInfoResp.newBuilder();
		builder.setLevel(level.first);
		builder.setExp(level.second);
		Map<Integer,Integer> echanges = entity.getExchanges();
		for(Entry<Integer, Integer> entry : echanges.entrySet()){
			KeyValuePairInt.Builder kbuilder = KeyValuePairInt.newBuilder();
			kbuilder.setKey(entry.getKey());
			kbuilder.setVal(entry.getValue());
			builder.addEchangeList(kbuilder);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.DRAGON_BOAT_CELETRATION_INFO_RESP, builder));
	}
	
	/**
	 * 排行榜信息
	 */
	public void rankInfo(String playerId,int protocolType){
		String guildId = this.getDataGeter().getGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.AllianceCarnivalError.AC_HAVE_NO_ALLIANCE_VALUE);
			return;
		}
		int index = Math.abs(guildId.hashCode()) % HawkTaskManager.getInstance().getExtraThreadNum();  
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				List<DragonBoatCelebrationRankMember>  members = DragonBoatCelebrationRank.getRankMembers(guildId);
				DragonBoatCelebrationRankMember self = DragonBoatCelebrationRank.getRank(playerId, guildId);
				DragonBoatCeletrationRankResp.Builder builder = 
						DragonBoatCeletrationRankResp.newBuilder();
				if(members != null){
					for(DragonBoatCelebrationRankMember member : members){
						CeletrationRankMember.Builder mbuilder = CeletrationRankMember.newBuilder();
						mbuilder.setPlayerId(member.getPlayerId());
						String name = getDataGeter().getPlayerName(member.getPlayerId());
						mbuilder.setPlayerName(name);
						mbuilder.addAllPersonalProtectSwitch(getDataGeter().getPersonalProtectVals(member.getPlayerId()));
						mbuilder.setRankIndex(member.getRank());
						mbuilder.setScore((int)member.getScore());
						builder.addMembers(mbuilder);
					}
				}
				int selfRank = 0;
				int selfScore = 0;
				String selfName = getDataGeter().getPlayerName(playerId);
				if(self != null){
					selfRank = self.getRank();
					selfScore = (int)self.getScore();
				}
				CeletrationRankMember.Builder sbuilder = CeletrationRankMember.newBuilder();
				sbuilder.setPlayerId(playerId);
				sbuilder.setPlayerName(selfName);
				sbuilder.addAllPersonalProtectSwitch(getDataGeter().getPersonalProtectVals(playerId));
				sbuilder.setRankIndex(selfRank);
				sbuilder.setScore(selfScore);
				builder.setSelfRank(sbuilder);
				PlayerPushHelper.getInstance().pushToPlayer(playerId,
						HawkProtocol.valueOf(HP.code.DRAGON_BOAT_CELETRATION_RANK_RESP, builder));
				return null;
			}
		},index);
		
		
	}
	
	
	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		logger.info("onContinueLoginEvent,playerId:{},isCross:{}",event.getPlayerId(),event.isCrossDay());
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		Optional<DragonBoatCelebrationEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		DragonBoatCelebrationEntity entity = opEntity.get();
		entity.setWolrdCollectRemainTime(0);
		entity.setWolrdCollectTimes(0);
		entity.setBeatYuriTimes(0);
		entity.setBeatYuriTotalTimes(0);
		entity.setGuildDonateTimes(0);
		entity.setGuildDonateTotalTimes(0);
		entity.notifyUpdate();
		logger.info("onContinueLoginEvent,finish,playerId:{},isCross:{}",event.getPlayerId(),event.isCrossDay());
		
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
		Optional<DragonBoatCelebrationEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		DragonBoatCelebrationEntity entity = opEntity.get();
		DragonBoatCelebrationDropCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(DragonBoatCelebrationDropCfg.class,
							Activity.BrokenExchangeOper.BEAT_YURI_VALUE);
		if (cfg == null) {
			return;
		}
		int totalTimes = entity.getBeatYuriTotalTimes();
		if(totalTimes >= cfg.getDropLimit()){
			return;
		}
		entity.setBeatYuriTimes(entity.getBeatYuriTimes() + atkTimes);
		if (atkTimes >= cfg.getDropParam()) {
			this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), atkTimes,
					Action.ACTIVITY_EXCHANGE_BEAT_YURI, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setBeatYuriTimes(0);
			entity.setBeatYuriTotalTimes(totalTimes + 1);
		}
		logger.info("broken activity beatYuri playerId:{}, beatTimes:{}, totalBeatTimes", event.getPlayerId(),
				atkTimes, entity.getBeatYuriTimes());
	}

	
	
	
	@Subscribe
	public void worldCollectEvent(ResourceCollectEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (event.getCollectTime() <= 0) {
			return;
		}

		Optional<DragonBoatCelebrationEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		
		DragonBoatCelebrationEntity entity = opEntity.get();
		int collectTime = event.getCollectTime() + entity.getWolrdCollectRemainTime();
		DragonBoatCelebrationDropCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(DragonBoatCelebrationDropCfg.class,
							Activity.BrokenExchangeOper.WORLD_COLLECT_VALUE);
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
				this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), num,
						Action.ACTIVITY_EXCHANGE_WORLD_COLLECT, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
				entity.setWolrdCollectTimes(num + entity.getWolrdCollectTimes());
				entity.setWolrdCollectRemainTime(remain);
			}			
		} else {
			entity.setWolrdCollectRemainTime(collectTime);
		}
		logger.info("broken activity worldCollect playerId:{}, beforeCollecTime:{}, afterCollectTime:{}, addCollectTime:{}", event.getPlayerId(), (collectTime - event.getCollectTime()), entity.getWolrdCollectRemainTime(), event.getCollectTime());
	}

	
	
	@Subscribe
	public void wishingEvent(GuildDonateEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<DragonBoatCelebrationEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		DragonBoatCelebrationEntity entity = opEntity.get();

		DragonBoatCelebrationDropCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(DragonBoatCelebrationDropCfg.class,
							Activity.BrokenExchangeOper.GUILD_DONATE_VALUE);
		if (cfg == null) {
			return;
		}
		int totalTimes = entity.getGuildDonateTotalTimes();
		if(totalTimes >= cfg.getDropLimit()){
			return;
		}
		entity.setGuildDonateTimes(entity.getGuildDonateTimes() + 1);
		if (entity.getGuildDonateTimes() >= cfg.getDropParam()) {
			this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), 1,
					Action.ACTIVITY_EXCHANGE_GUILD_DONATE, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setGuildDonateTimes(0);
			entity.setGuildDonateTotalTimes(totalTimes + 1);
		}
		logger.info("broken activity wishing playerId:{}, wishTimes:{}", event.getPlayerId(), entity.getGuildDonateTimes());
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
	 * 获取exp
	 * @return
	 */
	public Map<String,Long> getGuildExp(){
		Map<String,Long> scoreMap = new HashMap<>();
		String key = this.getGuildExpKey();
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(key, 0, Integer.MAX_VALUE);		
		for(Tuple tuple : rankSet){
			String guildId = tuple.getElement();
			long score = (long) tuple.getScore();
			scoreMap.put(guildId, score);
		}
		return scoreMap;
	}
	
	
	/**
	 * 添加Exp
	 * @param guildId
	 * @param String
	 * @return
	 */
	public long addGuildExp(String playerId,String guildId,int exp){
		String key = this.getGuildExpKey();
		Double add = ActivityGlobalRedis.getInstance().getRedisSession().
				zIncrby(key, guildId, exp,(int)TimeUnit.DAYS.toSeconds(30));
		return add.longValue();
	}
	
	/**
	 * 添加发奖纪录
	 * @param guildId
	 * @param level
	 */
	public Map<String,String> getGuildAwardASend(String guildId){
		String key = this.getGuildAwardKey(guildId);
		Map<String,String> map = ActivityGlobalRedis.getInstance().hgetAll(key);
		return map;
	}
	
	/**
	 * 添加记录
	 * @param awards
	 */
	public void addGuildMemberAwardMap(Map<String,Integer> awards){
		if(awards == null){
			return;
		}
		if(awards.size() <= 0){
			return;
		}
		long time = HawkTime.getMillisecond();
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			for(Entry<String, Integer> entry : awards.entrySet()){
				String playerId = entry.getKey();
				int level =  entry.getValue();
				String key = this.getGuildMemberAwardKey(playerId);
				pip.hset(key, String.valueOf(level), String.valueOf(time));
				pip.expire(key, (int)TimeUnit.DAYS.toSeconds(30));
			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	/**
	 * 获取联盟成员发奖纪录
	 * @param playerIds
	 * @return
	 */
	public Map<String,Map<String,String>> getGuildMemberAwardMap(Collection<String> playerIds){
		Map<String,Map<String,String>> infoMap = new HashMap<String,Map<String,String>>();
		Map<String,Response<Map<String,String>>> piplineRes = new HashMap<String,Response<Map<String,String>>>();
		try(Jedis jedis = ActivityGlobalRedis.getInstance().getRedisSession().getJedis(); 
				Pipeline pip = jedis.pipelined()){
			for( String playerId : playerIds ){
				String key = this.getGuildMemberAwardKey(playerId);
				Response<Map<String,String>> onePiplineResp = pip.hgetAll(key);
				piplineRes.put(playerId,onePiplineResp );
			}
			pip.sync();
			if(piplineRes.size() == playerIds.size() ){
 	    		for(Entry<String,Response<Map<String,String>>> entry : piplineRes.entrySet()){
 	    			String playerId = entry.getKey();
 	    			Response<Map<String,String>> value = entry.getValue();
 	    			Map<String,String> map = value.get();
 	    			if(map != null){
 	    				infoMap.put(playerId, map);
 	    			}
 	    		}   		
			}else{
				return null;
			}
		}catch (Exception e) {
			HawkException.catchException(e);
		}
		return infoMap;
	}
	
	
	/**
	 * 
	 * @param guildId
	 * @param level
	 */
	public void addGuildAwardASend(String guildId,int level,long time){
		String key = this.getGuildAwardKey(guildId);
		ActivityGlobalRedis.getInstance().hset(key, String.valueOf(level),
				 String.valueOf(time),(int)TimeUnit.DAYS.toSeconds(30));
	}
	/**
	 * 添加荣耀值进榜
	 * @param playerId
	 * @param guildId
	 * @param honour
	 */
	public void addToGuildRank(String playerId,String guildId,int honour){
		DragonBoatCelebrationRank.addScore(playerId, guildId, honour);
	}
	
	
	/**
	 * 联盟经验key
	 * @param guildId
	 * @return
	 */
	public String getGuildExpKey(){
		int termId = this.getActivityTermId();
		return ActivityRedisKey.DRAGON_BOAT_CELETRATION_EXP+":"+termId;
	}
	

	/**
	 * 联盟排行榜key
	 * @param guildId
	 * @return
	 */
	public String getGuildRankKey(String guildId){
		int termId = this.getActivityTermId();
		return ActivityRedisKey.DRAGON_BOAT_CELETRATION_RANK+":"+guildId+":"+termId;
	}

	
	/**
	 * 联盟发奖记录key
	 * @param guildId
	 * @return
	 */
	public String getGuildAwardKey(String guildId){
		int termId = this.getActivityTermId();
		return ActivityRedisKey.DRAGON_BOAT_CELETRATION_GUILD_AWARD+":"+termId+":"+guildId;
	}
	
	
	/**
	 * 联盟成员发奖纪录
	 * @param playerId
	 * @return
	 */
	public String getGuildMemberAwardKey(String playerId){
		int termId = this.getActivityTermId();
		return ActivityRedisKey.DRAGON_BOAT_CELETRATION_MEMBER_AWARD+":"+termId+":"+playerId;
	}

	
}
