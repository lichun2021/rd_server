package com.hawk.activity.type.impl.guildbanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AddBannerEvent;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.activity.event.impl.JoinGuildEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.guildbanner.rank.GuildBannerRank;
import com.hawk.activity.type.impl.guildbanner.rank.GuildBannerRankProvider;
import com.hawk.activity.type.impl.guildbanner.cfg.ActivityGuildBannerKVCfg;
import com.hawk.activity.type.impl.guildbanner.cfg.ActivityGuildBannerRankCfg;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.game.protocol.Activity.GuildBannerPageInfoResp;
import com.hawk.game.protocol.Activity.GuildBannerRankMsg;
import com.hawk.game.protocol.Activity.GuildBannerRankResp;
import com.hawk.game.protocol.HP;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.game.protocol.MailConst.MailId;

/**
 * 插旗（联盟排行）活动
 * 
 * @author lating
 *
 */
public class GuildBannerActivity extends ActivityBase {
	
	public GuildBannerActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GUILD_BANNER_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new GuildBannerActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		return null;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return null;
	}

	@Override
	public boolean isActivityClose(String playerId) {
		if (!HawkOSOperator.isEmptyString(playerId)) {
			String guildId = getDataGeter().getGuildId(playerId);
//			if (HawkOSOperator.isEmptyString(guildId)) {
//				return true;
//			}
		}
		
		return false;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		// do nothing;
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// do nothing;
	}

	@Override
	public void onPlayerLogin(String playerId) {
//		if (isShow(playerId)) {}
	}

	@Override
	public void onTick() {

	}
	
	@Override
	public void onEnd() {
//		int termId = this.getActivityTermId();
//		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
//			@Override
//			public Object run() {
//				handlerRankReward(termId);
//				return null;
//			}
//		});
	}
	
	@Override
	public void onHidden() {
		GuildBannerRankProvider rankProvider = (GuildBannerRankProvider) ActivityRankContext
				.getRankProvider(ActivityRankType.GUILD_BANNER_RANK, GuildBannerRank.class);
		String redisKey = rankProvider.getRedisKey(getKeySuffix());
		// 这里是否要设过期时间, 不让历史数据永久占用空间
		ActivityLocalRedis.getInstance().getRedisSession().expire(redisKey, 86400 * 3);
		rankProvider.cleanShowList();
	}

	/**
	 * 获取redis key
	 * 
	 * @return
	 */
	public String getKeySuffix() {
		return String.valueOf(this.getActivityTermId());
	}
	
	/**
	 * 活动结算时发送排名奖励
	 * 
	 * @param termId
	 */
	private void handlerRankReward(int termId) {
		String keySuffix = String.valueOf(termId);
		List<GuildBannerRank> rankList = getRankListAndExpire(keySuffix);
		for (GuildBannerRank bannerRank : rankList) {
			ActivityGuildBannerRankCfg rankCfg = this.getRankCfg(bannerRank.getRank());
			if (rankCfg == null) {
				logger.info("guildBannerRank reward not found, guildId: {}, rank: {}", bannerRank.getId(), bannerRank.getRank());
			} else {
				logger.info("guildBannerRank reward, guildId: {}, rank: {}", bannerRank.getId(), bannerRank.getRank());
				
				Collection<String> members = getDataGeter().getGuildMemberIds(bannerRank.getId());
				for(String playerId : members){
					this.getDataGeter().sendMail(playerId, MailId.GUILD_BANNER_RANK_AWARD, // 邮件
							new Object[] { this.getActivityCfg().getActivityName() },
							new Object[] { this.getActivityCfg().getActivityName() }, 
							new Object[] { bannerRank.getRank() },
							rankCfg.getRewardList(), false);  
				}
			}
		}
	}
	
	/**
	 * 取出排序后的，并且设置键过期
	 * 
	 * @param keySuffix
	 * @return
	 */
	public List<GuildBannerRank> getRankListAndExpire(String keySuffix) {
		GuildBannerRankProvider rankProvider = (GuildBannerRankProvider) ActivityRankContext
				.getRankProvider(ActivityRankType.GUILD_BANNER_RANK, GuildBannerRank.class);
		
		int rankSize = ActivityGuildBannerKVCfg.getInstance().getRankSize();
		String redisKey = rankProvider.getRedisKey(keySuffix);
		
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(redisKey, 0, Math.max((rankSize - 1), 0));		
		List<GuildBannerRank> newRankList = new ArrayList<GuildBannerRank>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			GuildBannerRank bannerRank = new GuildBannerRank();
			bannerRank.setId(rank.getElement());
			bannerRank.setRank(index);
			long score = RankScoreHelper.getRealScore((long) rank.getScore());
			bannerRank.setScore(score);
			newRankList.add(bannerRank);
			index++;
		}

		return newRankList;
	}
	
	/**
	 * 获取排名奖励配置
	 * 
	 * @param rankRewardCfgList
	 * @param rank
	 * @return
	 */
	private ActivityGuildBannerRankCfg getRankCfg(int rank) {
		ConfigIterator<ActivityGuildBannerRankCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ActivityGuildBannerRankCfg.class);
		while (iterator.hasNext()) {
			ActivityGuildBannerRankCfg rankCfg = iterator.next();
			if (rank >= rankCfg.getRankUpper() && rank <= rankCfg.getRankLower()) {
				return rankCfg;
			}
		}

		return null;
	}
	
	/**
	 * 获取联盟排行
	 * 
	 * @param guildId
	 * @return
	 */
	public int getGuildRank(String guildId) {
		ActivityRankProvider<GuildBannerRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.GUILD_BANNER_RANK, GuildBannerRank.class);
		GuildBannerRank bannerRank = rankProvider.getRank(guildId);
		if (bannerRank != null) {
			return bannerRank.getRank();
		}
		
		return 0;
	}
	
	@Subscribe
	public void onEvent(JoinGuildEvent event){
		ActivityState state = this.getActivityEntity().getActivityState();
		if (state == ActivityState.NONE || state == ActivityState.HIDDEN) {
			return;
		}
		
		String playerId = event.getPlayerId();
		String guildId = getDataGeter().getGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			PlayerPushHelper.getInstance().syncActivityStateInfo(playerId, this);
		}
	}
	
	@Subscribe
	public void onEvent(GuildDismissEvent event) {
		// 判断活动是否开启
		ActivityState state = this.getActivityEntity().getActivityState();
		if (state == ActivityState.NONE || state == ActivityState.HIDDEN) {
			return;
		}
		
		String guildId = event.getGuildId();
		ActivityRankProvider<GuildBannerRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.GUILD_BANNER_RANK, GuildBannerRank.class);
		rankProvider.remMember(guildId);
		rankProvider.doRankSort();
	}
	
	/**
	 * 监听新增旗帜（包括从别的盟夺过来的旗帜）事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onAddBannerEvent(AddBannerEvent event) {
		// 判断活动是否开启
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		
		String guildId = event.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			HawkLog.logPrintln("guildBannerActivity add banner event handle failed, guildId is null");
			return;
		}
		
		changeScore(guildId, event.getBannerCount());
	}
	
	/**
	 * 分数变化
	 * 
	 * @param playerId
	 */
	private void changeScore(String guildId, int bannerCount) {
		ActivityRankProvider<GuildBannerRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.GUILD_BANNER_RANK, GuildBannerRank.class);
		GuildBannerRank bannerRank = rankProvider.getRank(guildId);
		long oldCount = bannerRank.getScore();
		bannerRank.setScore(bannerCount);
		rankProvider.insertIntoRank(bannerRank);
		
		HawkLog.logPrintln("guildBannerActivity change score, guildId: {}, oldScore: {}, newScore: {}", guildId, oldCount, bannerCount);
	}

	public String getRedisKey(String suffix) {
		 return ActivityRedisKey.GUILD_BANNER_RANK + ":" + suffix;
	}
	
	/**
	 * 同步排行榜信息
	 * 
	 * @param playerId
	 */
	public void pushRankInfo(String playerId) {
		ActivityRankProvider<GuildBannerRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.GUILD_BANNER_RANK, GuildBannerRank.class);
		List<GuildBannerRank> rankList = rankProvider.getRankList();
		
		// 同步排名信息
		GuildBannerRankResp.Builder sbuilder = GuildBannerRankResp.newBuilder(); 
		for (GuildBannerRank rank : rankList) {
			GuildBannerRankMsg rankMsg = buildGuildBannerRank(rank);
			if (rankMsg != null) {
				sbuilder.addRankInfo(rankMsg);
			}
		}
		
		String guildId = getDataGeter().getGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			GuildBannerRank myRank = rankProvider.getRank(guildId);
			sbuilder.setGuildTag(getDataGeter().getGuildTag(guildId));
			sbuilder.setMyGuildName(getDataGeter().getGuildName(guildId));
			sbuilder.setMyGuildRank(myRank.getRank());
			sbuilder.setMyGuildScore(myRank.getScore());
			sbuilder.setMyGuildFlag(getDataGeter().getGuildFlag(guildId));
			sbuilder.setMyGuildLeader(getDataGeter().getGuildLeaderName(guildId));
		} else {
			sbuilder.setGuildTag("");
			sbuilder.setMyGuildName("");
			sbuilder.setMyGuildRank(0);
			sbuilder.setMyGuildScore(0);
			sbuilder.setMyGuildFlag(0);
			sbuilder.setMyGuildLeader("");
		}
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.GUILD_BANNER_RANK_RESP, sbuilder);
		this.getDataGeter().sendProtocol(playerId, protocol);
	}
	
	/**
	 * 构建排行榜信息PB
	 * 
	 * @param rank
	 * @return
	 */
	public GuildBannerRankMsg buildGuildBannerRank(GuildBannerRank rank) {
		String guildId = rank.getId();
		String guildName = getDataGeter().getGuildName(guildId);
		if (!HawkOSOperator.isEmptyString(guildName)) {
			GuildBannerRankMsg.Builder builder = GuildBannerRankMsg.newBuilder();
			builder.setGuildName(guildName);
			builder.setGuildTag(getDataGeter().getGuildTag(guildId));
			builder.setGuildFlag(getDataGeter().getGuildFlag(guildId));
			builder.setGuildLeader(getDataGeter().getGuildLeaderName(guildId));
			builder.setRank(rank.getRank());
			builder.setScore(rank.getScore());
			return builder.build();
		} 
		
		return null;
	}
	
	/**
	 * 同步活动页面信息
	 * @param playerId
	 */
	public void pushActivityPageInfo(String playerId) {
		GuildBannerPageInfoResp.Builder builder = GuildBannerPageInfoResp.newBuilder();
		String guildId = this.getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			builder.setCreateBannerCount(0);
			builder.setLoseBannerCount(0);
			builder.setOccupyBannerCount(0);
			this.getDataGeter().sendProtocol(playerId, HawkProtocol.valueOf(HP.code.GUILD_BANNER_PAGE_INFO_RESP, builder));
			return;
		}
		
		builder.setMaxCreateBanner(this.getDataGeter().getMaxCreateWarFlagCount(guildId));
		List<Integer> createPoint = this.getDataGeter().getCreatedWarFlagPoints(guildId);
		builder.setCreateBannerCount(this.getDataGeter().getOwnerFlagCount(guildId));
		builder.addAllCreateBannerPoint(createPoint.stream().filter(e -> e > 0).collect(Collectors.toList()));
		
		List<Integer> coccupyPoint = this.getDataGeter().getOccupyWarFlagPoints(guildId);
		builder.setOccupyBannerCount(coccupyPoint.size());
		builder.addAllOccupyBannerPoint(coccupyPoint.stream().filter(e -> e > 0).collect(Collectors.toList()));
		
		List<Integer> losePoint = this.getDataGeter().getLoseWarFlagPoints(guildId);
		builder.setLoseBannerCount(losePoint.size());
		builder.addAllLoseBannerPoint(losePoint.stream().filter(e -> e > 0).collect(Collectors.toList()));
		
		List<Integer> canFightCenterFlags = this.getDataGeter().getCanFightCenterFlags(guildId);
		builder.addAllCenterBannerPoint(canFightCenterFlags);
		
		builder.setCenterMaxCount(this.getDataGeter().getCenterFlagCount(guildId));
		
		builder.setCenterCurrCount(this.getDataGeter().getCenterFlagPlaceCount(guildId));
		
		this.getDataGeter().sendProtocol(playerId, HawkProtocol.valueOf(HP.code.GUILD_BANNER_PAGE_INFO_RESP, builder));
	}
	
	/**
	 * 刷新排行榜数据
	 */
	public void refreshRankData() {
		String redisKey = getRedisKey(getKeySuffix());
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(redisKey, 0, -1);		
		int count = 0;
		try (Jedis jedis = ActivityLocalRedis.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
			
			for (Tuple rank : rankSet) {
				try {
					String guildId = rank.getElement();
					long score = getDataGeter().getGuildWarFlagCount(guildId);
					long rankScore = RankScoreHelper.calcSpecialRankScore(score);
					pip.zadd(redisKey, rankScore, guildId);
					count++;
				} catch (Exception ex) {
					HawkException.catchException(ex);
				}
			}

			pip.sync();

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		HawkLog.logPrintln("refresh GuildBannerActivity rank data, count: {}", count);
		
	}
	
}