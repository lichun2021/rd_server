package com.hawk.activity.type.impl.tiberiumGuess;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.TblyGuessSendRewardEvent;
import com.hawk.activity.extend.TiberiumSeasonTimeAbstract;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.tiberiumGuess.cfg.TblyGuessActiviytKVCfg;
import com.hawk.activity.type.impl.tiberiumGuess.cfg.TblyGuessRewardCfg;
import com.hawk.activity.type.impl.tiberiumGuess.entity.TiberiumGuessEntity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.TblyGuessInfo;
import com.hawk.game.protocol.Activity.TblyGuessResp;
import com.hawk.game.protocol.Activity.TblyGuildBaseInfo;
import com.hawk.game.protocol.Activity.TblyMatchGuessPageResp;
import com.hawk.game.protocol.Activity.TblyMatchInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGuildBaseInfo;
import com.hawk.log.Action;

/** 泰伯利亚竞猜活动
 * @author Winder
 *
 */
public class TiberiumGuessActivity extends ActivityBase {
	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public TiberiumGuessActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	/**
	 * 赛季标识偏移
	 */
	public static final int SEASON_OFFSET = 10000;
	
	/**
	 * 联赛相关redis存储有效期(s)g
	 */
	public static final int TLW_EXPIRE_SECONDS = 24 * 3600 * 90;
	

	@Override
	public void onOpen() {
		
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.TBLY_GUESS;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		TiberiumGuessActivity activity = new TiberiumGuessActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<TiberiumGuessEntity> queryList = HawkDBManager.getInstance().query("from TiberiumGuessEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			TiberiumGuessEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		TiberiumGuessEntity entity = new TiberiumGuessEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		TblyMatchGuessPageResp.Builder builder = genTblyMatchGuessPageInfo(playerId);
		if (builder != null) {
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.TBLY_GUESS_MATCH_PAGE_RESP_VALUE, builder));
		}
	}
	
	/**活动界面信息
	 * @param playerId
	 * @return
	 */
	public TblyMatchGuessPageResp.Builder genTblyMatchGuessPageInfo(String playerId){
		TiberiumSeasonTimeAbstract tiberiumSeasonTimeAbstract = getDataGeter().getTiberiumSeasonTimeCfg();
		//拿不到配置,返回null不给client推消息
		if (tiberiumSeasonTimeAbstract == null) {
			return null;
		}
		int termId = tiberiumSeasonTimeAbstract.getTermId();
		TblyMatchGuessPageResp.Builder builder = TblyMatchGuessPageResp.newBuilder();
		
		TblyGuessActiviytKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TblyGuessActiviytKVCfg.class);
		long guessStartTime = getDataGeter().getTiberiumSeasonTimeCfgByTermId(tiberiumSeasonTimeAbstract.getSeason(), kvCfg.getStartRound()).getMatchEndTimeValue();
		if (termId < kvCfg.getStartRound()) {
			builder.setStartTime(guessStartTime);
			return builder;
		}
		builder.setStartTime(guessStartTime);
		int season = tiberiumSeasonTimeAbstract.getSeason();
		int tblyTermId = tiberiumSeasonTimeAbstract.getTermId();
		String guildsStr = ActivityGlobalRedis.getInstance().hget(getTblyGuessInfoKey(season, tblyTermId), playerId);
		Set<String> guildsSet = new HashSet<>();
		if (!StringUtils.isEmpty(guildsStr)) {
			guildsSet = SerializeHelper.stringToSet(String.class, guildsStr, SerializeHelper.ATTRIBUTE_SPLIT);
		}
		//泰伯利亚功能取到的数据
		TLWGetMatchInfoResp.Builder tblyMainInfo = getDataGeter().getTblyMatchInfo(playerId, termId);
		for (TLWGetMatchInfo.Builder tBuilder : tblyMainInfo.getMatchInfoBuilderList()) {
			TblyMatchInfo.Builder inBuilder = TblyMatchInfo.newBuilder();
			TblyGuildBaseInfo.Builder baBuilderA = TblyGuildBaseInfo.newBuilder();
			TblyGuildBaseInfo.Builder baBuilderB = TblyGuildBaseInfo.newBuilder();
			//A队
			TLWGuildBaseInfo baseInfoA = tBuilder.getGuildA();
			baBuilderA.setId(baseInfoA.getId());
			baBuilderA.setName(baseInfoA.getName());
			baBuilderA.setTag(baseInfoA.getTag());
			baBuilderA.setGuildFlag(baseInfoA.getGuildFlag());
			baBuilderA.setServerId(baseInfoA.getServerId());
			//A队参战人数
			HawkTuple2<Long, Integer> guildA = getDataGeter().getTWGuildInfo(baseInfoA.getId());
			if (guildA != null) {
				baBuilderA.setBattlePoint(guildA.first);
				baBuilderA.setMemberCnt(guildA.second);
			}
			inBuilder.setGuildA(baBuilderA);
			//A队投票信息
			builder.addTblyGuessInfo(genTblyGuessInfo(guildsSet, baseInfoA.getId(), season, tblyTermId));
			//B队
			TLWGuildBaseInfo baseInfoB = tBuilder.getGuildB();
			baBuilderB.setId(baseInfoB.getId());
			baBuilderB.setName(baseInfoB.getName());
			baBuilderB.setTag(baseInfoB.getTag());
			baBuilderB.setGuildFlag(baseInfoB.getGuildFlag());
			baBuilderB.setServerId(baseInfoB.getServerId());
			//B队参战人数
			HawkTuple2<Long, Integer> guildB = getDataGeter().getTWGuildInfo(baseInfoB.getId());
			if (guildB != null) {
				baBuilderB.setBattlePoint(guildB.first);
				baBuilderB.setMemberCnt(guildB.second);
			}
			inBuilder.setGuildB(baBuilderB);
			//B队投票信息
			builder.addTblyGuessInfo(genTblyGuessInfo(guildsSet, baseInfoB.getId(), season, tblyTermId));
			//AB的基础信息
			builder.addTblyMatchInfo(inBuilder);
			
		}
		return builder;
	}
	
	
	/**联盟战队投票信息
	 * @param guildsSet
	 * @param guildId
	 * @param season
	 * @param tblyTermId
	 * @return
	 */
	public TblyGuessInfo.Builder genTblyGuessInfo(Set<String> guildsSet, String guildId, int season, int tblyTermId){
		TblyGuessInfo.Builder guessBuilder = TblyGuessInfo.newBuilder();
		guessBuilder.setId(guildId);
		long voteNum = NumberUtils.toLong(ActivityGlobalRedis.getInstance().get(getTblyGuessTotalKey(season, tblyTermId, guildId)));
		guessBuilder.setVoteNum(voteNum);
		boolean isVote = guildsSet.contains(guildId)? true : false;
		guessBuilder.setIsVote(isVote);
		return guessBuilder;
	}
	
	@Subscribe
	public void onEvent(TblyGuessSendRewardEvent event){
		Set<String> winGuilds = event.getWinGuilds();
		TiberiumSeasonTimeAbstract tiberiumSeasonTimeAbstract = getDataGeter().getTiberiumSeasonTimeCfg();
		int termId = tiberiumSeasonTimeAbstract.getTermId();
		TblyGuessActiviytKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TblyGuessActiviytKVCfg.class);
		if (termId < kvCfg.getStartRound()) {
			return ;
		}
		int season = tiberiumSeasonTimeAbstract.getSeason();
		//发奖
		sendTblyGuessReward(season,termId, winGuilds);
		logger.info(" onEvent TblyGuessSendRewardEvent  season:{}, termId:{}, winGuilds:{}", season, termId, winGuilds.toString());
	}
	
	/**
	 * 竞猜发奖
	 */
	public void sendTblyGuessReward(int season, int termId,  Set<String> winGuilds){
		//本服所有竞猜的玩家
		Map<String, String> votePlayerInfo = ActivityGlobalRedis.getInstance().hgetAll(getTblyGuessInfoKey(season, termId));
		//遍历所有竞猜玩家
		for (Entry<String, String> entry : votePlayerInfo.entrySet()) {
			String playerId = entry.getKey();
			String guildsStr = entry.getValue();
			//玩家竞猜的联盟数据
			Set<String> guildsSet = SerializeHelper.stringToSet(String.class, guildsStr, SerializeHelper.ATTRIBUTE_SPLIT);
			//遍历每个玩家竞猜的联盟ID
			for (String guessGuildId : guildsSet) {
				try {
					boolean isWin = winGuilds.contains(guessGuildId);
					String guildName = getDataGeter().getTWGuildInfo(guessGuildId).third;
					Object[] content = new Object[] { guildName };
					//根据对错获取奖励
					List<RewardItem.Builder> items = getRewardByResult(termId, isWin);
					Object[] subTitle = new Object[] {};
					if (isWin) {
						//发猜对的奖励
						sendMailToPlayer(playerId, MailConst.MailId.TBLY_GUESS_ACT_CORRECT, subTitle, content, items);
						logger.info("sendTblyGuessReward Winner success  playerId:{}, guildId:{}, termId:{}", playerId, guessGuildId, termId);
					}else{
						//发猜错的奖励
						sendMailToPlayer(playerId, MailConst.MailId.TBLY_GUESS_ACT_WRONG, subTitle, content, items);
						logger.info("sendTblyGuessReward Losser success  playerId:{}, guildId:{}, termId:{}", playerId, guessGuildId, termId);
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}

	
	/**
	 * @param tblyTermId
	 * @param isWin
	 * @return
	 */
	public List<RewardItem.Builder> getRewardByResult(int tblyTermId, boolean isWin){
		ConfigIterator<TblyGuessRewardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(TblyGuessRewardCfg.class);
		while (iterator.hasNext()) {
			TblyGuessRewardCfg cfg = iterator.next();
			if (cfg.getRound() == tblyTermId && cfg.isWin() == isWin) {
				return cfg.getRewardItems();
			}
		}
		return null;
		
	}
	
	/**竞猜投票
	 * @param playerId
	 * @param id 竞猜Id
	 * @return
	 */
	public Result<?> guessTblyWinner(String playerId, String id) {
		//跨服不能操作
		if (getDataGeter().isCrossPlayer(playerId)) {
			return Result.fail(Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE);
		}
		TblyGuessResp.Builder builder = TblyGuessResp.newBuilder();
		TblyGuessInfo.Builder gBuilder = TblyGuessInfo.newBuilder();
		
		TiberiumSeasonTimeAbstract tiberiumSeasonTimeAbstract = getDataGeter().getTiberiumSeasonTimeCfg();
		int season = tiberiumSeasonTimeAbstract.getSeason();
		int tblyTermId = tiberiumSeasonTimeAbstract.getTermId();
		
		String guildsStr = ActivityGlobalRedis.getInstance().hget(getTblyGuessInfoKey(season, tblyTermId), playerId);
		Set<String> guildsSet = new HashSet<>();
		if (!StringUtils.isEmpty(guildsStr)) {
			guildsSet = SerializeHelper.stringToSet(String.class, guildsStr, SerializeHelper.ATTRIBUTE_SPLIT);
		}
		if (guildsSet.contains(id)) {
			return Result.fail(Status.Error.TBLY_GUESS_AREADY_VOTE_GUILD_VALUE);
		}
		for (String voteGuildId : guildsSet) {
			boolean isSameRoom = getDataGeter().isTblySameRoom(id, voteGuildId);
			if (isSameRoom) {
				return Result.fail(Status.Error.TBLY_GUESS_AREADY_VOTE_SAME_ROOM_VALUE);
			}
		}
		TblyGuessActiviytKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TblyGuessActiviytKVCfg.class);
		List<RewardItem.Builder> consumList = kvCfg.getBetConsume();
		//消耗
		boolean flag = this.getDataGeter().cost(playerId, consumList, Action.TBLY_GUESS_VOTE_COST);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//投票总数
		long voteNum = ActivityGlobalRedis.getInstance().increase(getTblyGuessTotalKey(season, tblyTermId, id));
		//添加投票信息
		guildsSet.add(id);
		
		String lastGuildsStr = SerializeHelper.collectionToString(guildsSet, SerializeHelper.ATTRIBUTE_SPLIT);
		//玩家投票信息存储
		ActivityGlobalRedis.getInstance().hset(getTblyGuessInfoKey(season, tblyTermId), playerId, lastGuildsStr, TLW_EXPIRE_SECONDS);
		
		logger.info("guessTblyWinner success  playerId:{}, guildId:{}, voteNum:{}", playerId, id, voteNum);
		gBuilder.setId(id);
		gBuilder.setVoteNum(voteNum);
		gBuilder.setIsVote(true);
		builder.setTblyGuessInfo(gBuilder);
		return Result.success(builder);
		
	}
	
	/** 工会竞猜总票数key
	 * @param season
	 * @param tblyTermId
	 * @param guildId
	 * @return
	 */
	public String getTblyGuessTotalKey(int season, int tblyTermId, String guildId){
		return "tbly_guess_total:" + getMarkId(season, tblyTermId) + ":" + guildId;
	}
	
	/**玩家投票key
	 * @param season
	 * @param tblyTermId
	 * @return
	 */
	public String getTblyGuessInfoKey(int season, int tblyTermId){
		return "tbly_guess_info:" + getDataGeter().getServerId() + ":" + getMarkId(season, tblyTermId);
	}
	
	/**
	 * 获取当前轮次标识
	 * @param season
	 * @param termId
	 * @return
	 */
	public int getMarkId(int season, int tblyTermId) {
		return season * SEASON_OFFSET + tblyTermId;
	}
	
}
