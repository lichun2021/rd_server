package com.hawk.game.activity.impl.yurirevenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.YuriRevengeRewardEvent;
import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.yurirevenge.YuriRevengeActivity;
import com.hawk.activity.type.impl.yurirevenge.YuriRevengeTimeController;
import com.hawk.activity.type.impl.yurirevenge.cfg.YuriRevengeActivityKVCfg;
import com.hawk.activity.type.impl.yurirevenge.cfg.YuriRevengeRankReward;
import com.hawk.activity.type.impl.yurirevenge.cfg.YuriRevengeScoreReward;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.msg.MigrateOutPlayerMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ActivityState;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.Error;
import com.hawk.game.protocol.Status.SysError;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.YuriRevenge.GetYuriRankInfoResp;
import com.hawk.game.protocol.YuriRevenge.State;
import com.hawk.game.protocol.YuriRevenge.YuriRankInfo;
import com.hawk.game.protocol.YuriRevenge.YuriRankType;
import com.hawk.game.protocol.YuriRevenge.YuriRevengePageInfoResp;
import com.hawk.game.protocol.YuriRevenge.YuriTurnInfo;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.GuildYuriFactory;
import com.hawk.game.world.object.YuriFactoryPoint;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.log.LogConst.ActivityClickType;

import redis.clients.jedis.Tuple;


/**
 * 尤里复仇活动管理类
 * 
 * @author admin
 *
 */
public class YuriRevengeService extends HawkAppObj {
	static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 单例对象
	 */
	private static YuriRevengeService instance = null;
	/**
	 * 已完成本期活动的联盟列表
	 */
	private List<String> finishedGuildList;
	
	/**
	 * 已发放本期奖励的玩家列表
	 */
	private List<String> rewardedPlayerList;
	
	/**
	 * 个人尤里复仇积分缓存(只记录起服后新增数据)
	 */
	private Map<String, Long> personScoreMap;
	/**
	 * 联盟尤里复仇积分(只记录起服后新增数据)
	 */
	private Map<String, Long> guildScoreMap;
	
	/**
	 * 玩家防守失败数量(只记录起服后新增数据)
	 */
	private Map<String,Integer> failMap;
	
	/**
	 * 当前活动期数
	 */
	private int currtTermId;
	

	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static YuriRevengeService getInstance() {
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	public YuriRevengeService(HawkXID xid) {
		super(xid);
		// 设置实例
		instance = this;
	}

	public boolean init() {
		currtTermId = 0;
		if(isActivityOpen()){
			Optional<YuriRevengeActivity> opActivity = getActivity();
			if(opActivity.isPresent()){
				currtTermId = opActivity.get().getActivityEntity().getTermId();
			}
		}
		finishedGuildList = LocalRedis.getInstance().getYuriRevengeFinishGuilds(currtTermId);
		personScoreMap = new ConcurrentHashMap<>();
		guildScoreMap = new ConcurrentHashMap<>();
		failMap = new HashMap<>();
		rewardedPlayerList = LocalRedis.getInstance().getYuriRevengeRewardedPlayers(currtTermId);
		return true;
	}
	
	private Optional<YuriRevengeActivity> getActivity(){
		return ActivityManager.getInstance().getGameActivityByType(ActivityType.YURI_REVENGE_ACTIVITY.intValue());
	}
	
	/**
	 * 获取尤里复仇活动界面信息
	 * @param resp
	 * @param player
	 * @return
	 */
	public int onGetPageInfo(YuriRevengePageInfoResp.Builder builder, Player player) {
		int activityState = getActivityState();
		// 活动未开启
		if (!isActivityOpen()) {
			return Status.Error.YURI_REVENGE_ACTIVITY_NOT_OPEN_VALUE;
		}
		Optional<YuriRevengeActivity> opActivity = getActivity();
		if (!opActivity.isPresent()) {
			return Status.Error.YURI_REVENGE_ACTIVITY_NOT_OPEN_VALUE;
		}
		YuriRevengeActivity activity = opActivity.get();
		String playerId = player.getId();
		String guildId = player.getGuildId();
		State state = getCurrentState(guildId);
		YuriRevengeTimeController timeController = activity.getTimeControl();
		int selfScore = 0;
		long guildScore = 0;
		long stateEndTime = 0;
		switch (state) {
		case CLOSE:
			if(activityState == ActivityState.SHOW_VALUE){
				// 本期活动开始时间
				stateEndTime = timeController.getStartTimeByTermId(currtTermId);
			}
			else{
				// 下期活动开始时间
				stateEndTime = timeController.getNextStartTimeByTermId(currtTermId);
			}
			break;
		case OPEN:
			// 若在可开启阶段,显示可开启倒计时,否则显示可开启阶段倒计时
			if(isInCanOpenDuration()){
				long currStartTime = timeController.getStartTimeByTermId(currtTermId);
				YuriRevengeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(YuriRevengeActivityKVCfg.class);
				long openTime = cfg.getOpentime();
				stateEndTime = currStartTime + openTime;
			}
			else{
				stateEndTime = timeController.getEndTimeByTermId(currtTermId);
			}
			break;
		case OPEN_FIGHTING:
			YuriFactoryPoint point = WorldFoggyFortressService.getInstance().getGuildYuriPoint(guildId);
			GuildYuriFactory factory = point.getGuildYuriFactory(guildId);
			stateEndTime = factory.getNextPushTime();
			// 如果是最后一波,状态显示为OPEN_FINISHED
			if(stateEndTime == Long.MAX_VALUE){
				stateEndTime = timeController.getEndTimeByTermId(currtTermId);
				state = State.OPEN_FINISHED;
			}else{
				WorldPoint worldPoint = point.getWorldPoint();
				// 波次信息
				YuriTurnInfo.Builder turnInfo = YuriTurnInfo.newBuilder();
				turnInfo.setTurn(factory.getRound());
				turnInfo.setPosX(worldPoint.getX());
				turnInfo.setPosY(worldPoint.getY());
				builder.setTurnInfo(turnInfo);
			}
			selfScore = (int) LocalRedis.getInstance().getYuriRevengeRankScore(currtTermId, YuriRankType.SELF_RANK, playerId);
			guildScore = LocalRedis.getInstance().getYuriRevengeRankScore(currtTermId, YuriRankType.GUILD_RANK, guildId);
			break;
		case OPEN_FINISHED:
			selfScore = (int) LocalRedis.getInstance().getYuriRevengeRankScore(currtTermId, YuriRankType.SELF_RANK, playerId);
			if (!HawkOSOperator.isEmptyString(guildId)) {
				guildScore = LocalRedis.getInstance().getYuriRevengeRankScore(currtTermId, YuriRankType.GUILD_RANK, guildId);
			}
			// 奖励发放时间
			stateEndTime = timeController.getEndTimeByTermId(currtTermId);
			break;
		}
		builder.setState(state);
		builder.setSelfScore(selfScore);
		builder.setGuildScore(guildScore);
		builder.setStateEndTime(stateEndTime);
		return SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 开启尤里复仇活动
	 * @param player
	 * @return
	 */
	public int onOpenActivity(Player player) {
		String guildId = player.getGuildId();
		// 没有联盟
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Error.GUILD_NO_JOIN_VALUE;
		}
		;
		// 开启权限不足
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.YURIREVENGE)) {
			return Error.GUILD_LOW_AUTHORITY_VALUE;
		}
		int result = canOpenActivity(guildId);
		if (result != SysError.SUCCESS_OK_VALUE) {
			return result;
		}
		// 开启进攻
		result = WorldFoggyFortressService.getInstance().openYuriRevenge(guildId);
		if (result != SysError.SUCCESS_OK_VALUE) {
			return result;
		}
		
		// 推送活动界面信息
		YuriRevengePageInfoResp.Builder pageInfo = YuriRevengePageInfoResp.newBuilder();
		onGetPageInfo(pageInfo, player);
		GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.PUSH_YURI_REVENGE_PAGE_INFO, pageInfo));
		
		//发送邮件通知
		GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
				.setMailId(MailId.YURI_REVENGE_FIGHT_OPEN)
				.addContents(player.getName(), HawkTime.getMillisecond()));
		return SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 获取活动排行信息  
	 * @param rankType
	 * @param resp 
	 * @param player
	 * @return
	 */
	public int onGetRankInfo(YuriRankType rankType, GetYuriRankInfoResp.Builder resp, Player player) {
		// 榜单信息
		List<YuriRankInfo> rankInfoList = loadRankInfo(rankType);
		resp.addAllRankInfo(rankInfoList);
		resp.setRankType(rankType);

		// 个人排行信息
		YuriRankInfo.Builder selfRankInfo = YuriRankInfo.newBuilder();
		GuildInfoObject guild = null;
		if (player.hasGuild()) {
			guild = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
			selfRankInfo.setFlagId(guild.getFlagId());
			selfRankInfo.setGuildTag(guild.getTag());
		}

		String rankId = "";
		String name = "";
		List<Integer> personalProtectVal = new ArrayList<Integer>();

		if (rankType == YuriRankType.SELF_RANK) {
			name = player.getName();
			rankId = player.getId();
			personalProtectVal.addAll(player.getData().getPersonalProtectListVals());
		} else if (rankType == YuriRankType.GUILD_RANK && player.hasGuild()) {
			name = guild.getName();
			rankId = guild.getId();
		} else {
			return SysError.SUCCESS_OK_VALUE;
		}

		int selfRank = (int) LocalRedis.getInstance().getYuriRevengeRank(currtTermId, rankType, rankId);

		if (selfRank != -1) {
			long score = LocalRedis.getInstance().getYuriRevengeRankScore(currtTermId, rankType, rankId);
			selfRankInfo.setName(name);
			selfRankInfo.setRank(selfRank);
			selfRankInfo.setScore(score);
			selfRankInfo.addAllPersonalProtectSwitch(personalProtectVal);
			resp.setSelfRank(selfRankInfo);
		}
		return SysError.SUCCESS_OK_VALUE;
	}
	
	
	
	/**
	 * 联盟尤里复仇战斗结束
	 * @param guildId
	 */
	public void onGuildActivityFinish(String guildId){
		logger.debug("guild yuriRevenge finish, guildId: {}, termId: {}", guildId, currtTermId);
		finishedGuildList.add(guildId);
		LocalRedis.getInstance().addYuriRevengeFinishGuild(currtTermId, guildId);
		long guildScore = getCacheScore(YuriRankType.GUILD_RANK, guildId);
		// 发放奖励
		for(String memberId : GuildService.getInstance().getGuildMembers(guildId)){
			if(rewardedPlayerList.contains(memberId)){
				logger.error("yuriRevenge finish reward block, alread send, playerId: {}, termId: {}",memberId, currtTermId);
				continue;
			}
			long selfScore = getCacheScore(YuriRankType.SELF_RANK, memberId);
			ConfigIterator<YuriRevengeScoreReward> configIterator =HawkConfigManager.getInstance().getConfigIterator(YuriRevengeScoreReward.class);
			YuriRevengeScoreReward rewardCfg = null;
			for (YuriRevengeScoreReward cfg : configIterator) {
				if (selfScore >= cfg.getPersonIntegral() && guildScore >= cfg.getAllianceIntegral()) {
					if (rewardCfg == null) {
						rewardCfg = cfg;
					} else if (cfg.getId() > rewardCfg.getId()) {
						rewardCfg = cfg;
					}
				}
			}
			if(rewardCfg == null){
				continue;
			}
			String reward = rewardCfg.getItem();
			AwardItems award = AwardItems.valueOf();
			award.init(reward);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
	                .setPlayerId(memberId)
	                .setMailId(MailId.YURI_REVENGE_SCORE_REWARD)
	                .addContents(rewardCfg.getItem())
	                .setRewards(award.getAwardItems())
	                .setAwardStatus(MailRewardStatus.NOT_GET)
	                .build());
			ActivityManager.getInstance().postEvent(new YuriRevengeRewardEvent(memberId, rewardCfg.getId()));
			rewardedPlayerList.add(memberId);
			LocalRedis.getInstance().addYuriRevengeRewardedPlayer(currtTermId, memberId);
			
			Player player = GlobalData.getInstance().makesurePlayer(memberId);
			if(player != null){
				LogUtil.logActivityClickFlow(player, ActivityClickType.REWARD_CLICK, String.valueOf(ActivityBtns.ActivityChildCellBtn_VALUE), 
						String.valueOf(Activity.ActivityType.YURI_REVENGE_VALUE), String.valueOf(MailId.YURI_REVENGE_SCORE_REWARD_VALUE));
			}
		}
	}
	
	/**
	 * 添加尤里复仇积分
	 * @param guildId
	 */
	public void onAddScore(String guildId, long guildScore, Map<String, Long> personScores) {
		long guildScoreBef = getCacheScore(YuriRankType.GUILD_RANK, guildId);
		long guildScoreAft = guildScore + guildScoreBef;
		logger.info("yuriRevenge add personScore, guildId: {}, addScore: {}, scoreBef: {}, scoreAft: {}", guildId, guildScore, guildScoreBef, guildScoreAft);
		updateCacheScore(YuriRankType.GUILD_RANK, guildId, guildScoreAft);
		LocalRedis.getInstance().updateYuriRevengeRankScore(currtTermId, YuriRankType.GUILD_RANK, guildId, guildScoreAft);
		for (Entry<String, Long> entry : personScores.entrySet()) {
			String playerId = entry.getKey();
			long scoreBef = getCacheScore(YuriRankType.SELF_RANK, entry.getKey());
			long scoreAft = scoreBef + entry.getValue();
			updateCacheScore(YuriRankType.SELF_RANK, playerId, scoreAft);
			LocalRedis.getInstance().updateYuriRevengeRankScore(currtTermId, YuriRankType.SELF_RANK, playerId, scoreAft);
			logger.info("yuriRevenge add personScore, playerId: {}, addScore: {}, scoreBef: {}, scoreAft: {}", playerId, entry.getValue(), scoreBef, scoreAft);
		}
	}
	
	/**
	 * 添加玩家失败次数
	 * @param playerId
	 */
	public void addLoseCnt(String playerId) {
		if (failMap.containsKey(playerId)) {
			failMap.put(playerId, failMap.get(playerId) + 1);
		} else {
			failMap.put(playerId, 1);
		}
	}
	
	/**
	 * 获取玩家失败次数
	 * @param playerId
	 * @return
	 */
	public int getLoseCnt(String playerId){
		if(failMap.containsKey(playerId)){
			return failMap.get(playerId);
		}
		return 0;
	}
	
	/**
	 * 玩家离开联盟
	 */
	public void onQuitGuild(String playerId, String guildId){
		// 移除该玩家有尤里行军
		WorldFoggyFortressService.getInstance().removePlayerMonsterMarch(guildId, playerId, true);
		LocalRedis.getInstance().removeYuriRevengeRankScore(currtTermId, YuriRankType.SELF_RANK, playerId);
		failMap.remove(playerId);
		personScoreMap.remove(playerId);
	}
	
	/**
	 * 解散联盟
	 * @param guildId
	 */
	public void onDismissGuild(String guildId){
		// 关闭该联盟的尤里行军
		WorldFoggyFortressService.getInstance().closeYuriRevenge(guildId, true);
		LocalRedis.getInstance().removeYuriRevengeRankScore(currtTermId, YuriRankType.GUILD_RANK, guildId);
		guildScoreMap.remove(guildId);
		finishedGuildList.remove(guildId);
	}
	
	/**
	 * 获取排行奖励上榜数量
	 * @return
	 */
	private int getMaxRankCount(){
		return HawkConfigManager.getInstance().getConfigIterator(YuriRevengeRankReward.class).size();
	}
	

	/**
	 * 获取内存中玩家/联盟积分
	 * @param rankType
	 * @param id
	 * @return
	 */
	private long getCacheScore(YuriRankType rankType, String id){
		switch (rankType) {
		case SELF_RANK:
			if(personScoreMap.containsKey(id)){
				return personScoreMap.get(id);
			}
			break;
		case GUILD_RANK:
			if(guildScoreMap.containsKey(id)){
				return guildScoreMap.get(id);
			}
			break;
		}
		return 0;
	}

	/**
	 * 刷新缓存中玩家/联盟积分
	 * @param rankType
	 * @param id
	 * @param score
	 */
	private void updateCacheScore(YuriRankType rankType, String id, long score) {
		switch (rankType) {
		case SELF_RANK:
			personScoreMap.put(id, score);
			break;
		case GUILD_RANK:
			guildScoreMap.put(id, score);
			break;
		}
	}


	/**
	 * 加载排行信息
	 * @param rankType
	 * @return
	 */
	private List<YuriRankInfo> loadRankInfo(YuriRankType rankType) {
		List<YuriRankInfo> rankInfoList = new ArrayList<>();
		int rankCount = getMaxRankCount();
		Set<Tuple> set = LocalRedis.getInstance().getYuriScoreRankScore(currtTermId, rankType, rankCount - 1);
		int rank = 1;
		for (Tuple tuple : set) {
			YuriRankInfo.Builder rankInfo = YuriRankInfo.newBuilder();
			String id = tuple.getElement();
			long score = (long) tuple.getScore();
			rankInfo.setRank(rank);
			rankInfo.setScore(score);
			if (rankType == YuriRankType.GUILD_RANK) {
				GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(id);
				if (guild == null) {
					continue;
				}
				rankInfo.setName(guild.getName());
				rankInfo.setGuildTag(guild.getTag());
				rankInfo.setFlagId(guild.getFlagId());
			} else {
				Player snaoshot = GlobalData.getInstance().makesurePlayer(id);
				if (snaoshot == null) {
					continue;
				}
				GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(snaoshot.getGuildId());
				if(guild == null){
					continue;
				}
				rankInfo.setName(snaoshot.getName());
				rankInfo.setFlagId(guild.getFlagId());
				rankInfo.setGuildTag(guild.getTag());
				rankInfo.setPlayerId(snaoshot.getId());
				rankInfo.addAllPersonalProtectSwitch(snaoshot.getData().getPersonalProtectListVals());
			}
			rankInfoList.add(rankInfo.build());
			rank++;
		}
		return rankInfoList;
	}

	/**
	 * 判断是否满足活动开启条件
	 * @return
	 */
	private int canOpenActivity(String guildId){
		// 活动未开启
		if(getActivityState() != ActivityState.OPEN_VALUE){
			return Error.YURI_REVENGE_ACTIVITY_NOT_OPEN_VALUE;
		}
		// 该联盟已完成尤里复仇活动
		if(finishedGuildList.contains(guildId)){
			return Error.YURI_REVENGE_FINISHED_VALUE;
		}
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 获取当前活动状态
	 * @return
	 */
	private int getActivityState() {
		Optional<YuriRevengeActivity> opActivity = getActivity();
		if (!opActivity.isPresent()) {
			return ActivityState.HIDDEN_VALUE;
		}
		ActivityEntity entity = opActivity.get().getActivityEntity();
		if (entity == null) {
			return ActivityState.HIDDEN_VALUE;
		}
		return entity.getState();
	}
	
	
	/**
	 * 获取指定联盟当前的活动状态
	 * @return
	 */
	private State getCurrentState(String guildId) {
		int activityState = getActivityState();
		// 活动为非开启状态时
		switch (activityState) {
		// 根据参与情况与可开启时间段判定具体开启状态
		case ActivityState.OPEN_VALUE:
			boolean canOpen = isInCanOpenDuration();
			// 玩家未加入联盟
			if (HawkOSOperator.isEmptyString(guildId)) {
				if (canOpen) {
					return State.OPEN;
				} else {
					return State.OPEN_FINISHED;
				}
			}
			YuriFactoryPoint point = WorldFoggyFortressService.getInstance().getGuildYuriPoint(guildId);
			// 联盟正在进行尤里复仇
			if (point != null && point.getGuildYuriFactory(guildId) != null) {
				return State.OPEN_FIGHTING;
			}
			// 联盟已结束尤里复仇
			if (finishedGuildList.contains(guildId)) {
				return State.OPEN_FINISHED;
			}
			// 不在可开启时间段
			if (!canOpen) {
				return State.OPEN_FINISHED;
			}
			return State.OPEN;
		// 活动结束状态对应下次
		case ActivityState.END_VALUE:
			return State.CLOSE;
		// 活动展示期,对应本次开启时间	
		case ActivityState.SHOW_VALUE:
		return State.CLOSE;
		default:
			return State.OPEN;
		}
	}
	
	/**
	 * 尤里复仇活动是否开启
	 * @return
	 */
	private boolean isActivityOpen(){
		int activityState = getActivityState();
		return activityState != ActivityState.HIDDEN_VALUE;
	}
	
	/**
	 * 判断是否在可开启尤里进攻的时间段
	 * @return
	 */
	private boolean isInCanOpenDuration() {
		Optional<YuriRevengeActivity> opActivity = getActivity();
		if (!opActivity.isPresent()) {
			return false;
		}
		YuriRevengeActivity activity = opActivity.get();
		ITimeController timeController = activity.getTimeControl();
		long now = HawkTime.getMillisecond();
		long currStartTime = timeController.getStartTimeByTermId(currtTermId);
		YuriRevengeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(YuriRevengeActivityKVCfg.class);
		long openTime = cfg.getOpentime();
		return now >= currStartTime && now <= currStartTime + openTime;
	}
	
	
	/**
	 * 活动开启
	 */
	public void onActivityShow(int termId) {
		if(currtTermId < termId){
			currtTermId = termId;
			finishedGuildList.clear();
			rewardedPlayerList.clear();
			guildScoreMap.clear();
			personScoreMap.clear();
			failMap.clear();
		}
	}
	
	/**
	 * 活动结束 发奖
	 */
	public void onActivityEnd(int termId) {
		int maxRankCount = getMaxRankCount();
		//个人排行奖励
		Set<Tuple> selfRanks = LocalRedis.getInstance().getYuriScoreRankScore(termId, YuriRankType.SELF_RANK, maxRankCount);
		if(selfRanks != null){
			int rank = 1;
			for(Tuple rankInfo : selfRanks){
				String playerId = rankInfo.getElement();
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				if(player == null){
					continue;
				}
				
				YuriRevengeRankReward cfg = HawkConfigManager.getInstance().getConfigByKey(YuriRevengeRankReward.class, rank);
				if(cfg == null){
					continue;
				}
				
				AwardItems award = AwardItems.valueOf(cfg.getPersonAward());
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		                .setPlayerId(playerId)
		                .setMailId(MailId.YURI_REVENGE_SELF_RANK_REWARD)
		                .addContents(player.getName(), rank)
		                .setRewards(award.getAwardItems())
		                .setAwardStatus(MailRewardStatus.NOT_GET)
		                .build());
				rank++;
				LogUtil.logActivityClickFlow(player, ActivityClickType.REWARD_CLICK, String.valueOf(ActivityBtns.ActivityChildCellBtn_VALUE), 
						String.valueOf(Activity.ActivityType.YURI_REVENGE_VALUE), String.valueOf(MailId.YURI_REVENGE_SELF_RANK_REWARD));
			}
		}
		// 联盟排行奖励
		Set<Tuple> guildRanks = LocalRedis.getInstance().getYuriScoreRankScore(termId, YuriRankType.GUILD_RANK, maxRankCount);
		if (guildRanks != null) {
			int rank = 1;
			for (Tuple rankInfo : guildRanks) {
				String guildId = rankInfo.getElement();
				GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guild == null) {
					continue;
				}
				YuriRevengeRankReward cfg = HawkConfigManager.getInstance().getConfigByKey(YuriRevengeRankReward.class, rank);
				if (cfg == null) {
					continue;
				}
				AwardItems award = AwardItems.valueOf(cfg.getAllianceAward());
				GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
						.setMailId(MailId.YURI_REVENGE_GUILD_RANK_REWARD)
						.addContents(guild.getName(), rank)
						.setRewards(award.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET));
				rank++;
			}
		}
		
	}
	
	/**
	 * 玩家数据迁移时,移除其尤里复仇活动相关信息
	 * @param playerId
	 */
	@MessageHandler
	private void migratePlayer(MigrateOutPlayerMsg msg){
		String playerId = msg.getPlayer().getId();
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		// 移除该玩家有尤里行军
		WorldFoggyFortressService.getInstance().removePlayerMonsterMarch(guildId, playerId, true);
		removeRank(playerId);
	}
	
	/**
	 * 移除排行榜信息
	 * @param playerId
	 */
	public void removeRank(String playerId) {
		try {
			LocalRedis.getInstance().removeYuriRevengeRankScore(currtTermId, YuriRankType.SELF_RANK, playerId);
			failMap.remove(playerId);
			personScoreMap.remove(playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: yurirevenge", playerId);
	}
}
