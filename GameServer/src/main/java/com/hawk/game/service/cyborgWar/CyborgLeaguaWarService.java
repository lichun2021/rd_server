package com.hawk.game.service.cyborgWar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CWGradeEvent;
import com.hawk.game.protocol.Shop;
import com.hawk.game.service.shop.ShopService;
import org.apache.commons.collections4.CollectionUtils;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.config.CyborgSeasonDivisionCfg;
import com.hawk.game.config.CyborgSeasonRankAwardCfg;
import com.hawk.game.config.CyborgSeasonTimeCfg;
import com.hawk.game.config.CyborgShopRefreshTimeCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.CyborgWar.CSWScoreInfo;
import com.hawk.game.protocol.CyborgWar.CSWState;
import com.hawk.game.protocol.CyborgWar.CSWStateInfo;
import com.hawk.game.protocol.CyborgWar.CWTeamInfo;
import com.hawk.game.protocol.CyborgWar.CWTeamRank;
import com.hawk.game.protocol.CyborgWar.GetCWTeamRankResp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.cyborgWar.CWConst.CLWActivityState;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;

import redis.clients.jedis.Tuple;


/**
 * 赛博之战赛季
 * @author Jesse
 */
public class CyborgLeaguaWarService extends HawkAppObj {
	
	/**
	 * 全局实例对象
	 */
	private static CyborgLeaguaWarService instance = null;
	
	/**
	 * 活动时间信息数据
	 */
	public static CLWActivityData activityInfo = new CLWActivityData();
	
	/**
	 * 本服所有联盟对应战队信息(仅限本服遍历使用)
	 */
	public static Map<String, List<String>> guildTeams = new ConcurrentHashMap<>();
	/**
	 * 匹配状态信息
	 */
	public static CWMatchState matchServerInfo = new CWMatchState();
	
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static CyborgLeaguaWarService getInstance() {
		return instance;
	}

	public CyborgLeaguaWarService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		try {
			// 读取活动阶段数据
			activityInfo = CyborgWarRedis.getInstance().getCLWActivityInfo();

			// 进行阶段检测
			checkStateChange(true);
			// 阶段轮询
			addTickable(new HawkPeriodTickable(1000) {
				@Override
				public void onPeriodTick() {
					// 活动阶段轮询
					stateTick();
				}
			});

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 阶段轮询
	 */
	public void stateTick() {
		if (!GsApp.getInstance().isInitOK()) {
			return;
		}
		try {
			checkStateChange(false);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取期数信息
	 * @return
	 */
	public int getSeason() {
		return activityInfo.getSeason();
	}
	
	/**
	 * 获取活动信息
	 * @return
	 */
	public CLWActivityData getActivityData(){
		return activityInfo;
	}
	
	
	/**
	 * 当前阶段状态计算,仅供状态检测调用
	 * 
	 * @return
	 */
	private CLWActivityData calcInfo() {
		CLWActivityData info = new CLWActivityData();
		if (CyborgConstCfg.getInstance().isSystemClose()) {
			info.setState(CLWActivityState.CLOSE);
			return info;
		}
		ConfigIterator<CyborgSeasonTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgSeasonTimeCfg.class);
		long now = HawkTime.getMillisecond();

		CyborgSeasonTimeCfg cfg = null;
		// 游客服不开启该活动
		if (GsConfig.getInstance().getServerType() == ServerType.GUEST) {
			return info;
		}
		for (CyborgSeasonTimeCfg timeCfg : its) {
			if (now > timeCfg.getShowTimeValue()) {
				cfg = timeCfg;
			}
		}
		// 没有可供开启的配置
		if (cfg == null) {
			return info;
		}


		int season = 0;
		CLWActivityState state = CLWActivityState.NOT_OPEN;
		if (cfg != null) {
			season = cfg.getSeason();
			long showTime = cfg.getShowTimeValue();
			long openTime = cfg.getOpenTimeValue();
			long endShowTime = cfg.getEndShowTimeValue();
			long endTime = cfg.getEndTimeValue();
			if (now < showTime) {
				state = CLWActivityState.NOT_OPEN;
			} else if (now >= showTime && now < openTime) {
				state = CLWActivityState.SHOW;
			} else if (now >= openTime && now < endShowTime) {
				state = CLWActivityState.OPEN;
			} else if (now >= endShowTime && now < endTime) {
				state = CLWActivityState.END;
			} else if (now >= endTime) {
				state = CLWActivityState.NOT_OPEN;
			}
		}

		info.setSeason(season);
		info.setState(state);
		return info;
	}
	
	private void checkStateChange( boolean isInit) {
		CLWActivityData newInfo = calcInfo();
		int old_season = activityInfo.getSeason();
		int new_season = newInfo.getSeason();

		// 如果当前期数和当前实际期数不一致,且当前活动强制关闭,则推送活动状态,且刷新状态信息
		if (old_season != new_season && new_season == 0) {
			activityInfo = newInfo;
			if (!isInit) {
				// 在线玩家推送活动状态
				for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
					CyborgWarService.getInstance().syncPageInfo(player);
				}
			}
			CyborgWarRedis.getInstance().updateCLWActivityInfo(activityInfo);
		}
		CLWActivityState old_state = activityInfo.getState();
		CLWActivityState new_state = newInfo.getState();
		boolean needUpdate = false;
		// 期数不一致,则重置活动状态,从未开启阶段开始轮询
		if (new_season != old_season) {
			old_state = CLWActivityState.NOT_OPEN;
			activityInfo.setSeason(new_season);
			activityInfo.setDivisionRewardFinish(false);
			activityInfo.setRankRewardFinish(false);
			needUpdate = true;
		}

		for (int i = 0; i < 8; i++) {
			if (old_state == new_state) {
				break;
			}
			needUpdate = true;
			if (old_state == CLWActivityState.NOT_OPEN) {
				old_state = CLWActivityState.SHOW;
				activityInfo.setState(old_state);
				onShow();
			} else if (old_state == CLWActivityState.SHOW) {
				old_state = CLWActivityState.OPEN;
				activityInfo.setState(old_state);
				onOpen();
			} else if (old_state == CLWActivityState.OPEN) {
				old_state = CLWActivityState.END;
				activityInfo.setState(old_state);
				onEnd();
			} else if (old_state == CLWActivityState.END) {
				old_state = CLWActivityState.NOT_OPEN;
				activityInfo.setState(old_state);
				onHidden();
			}
		}

		if (needUpdate) {
			activityInfo = newInfo;
			if (!isInit) {
				// 在线玩家推送活动状态
				for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
					CyborgWarService.getInstance().syncPageInfo(player);
				}
			}
			CyborgWarRedis.getInstance().updateCLWActivityInfo(activityInfo);
			HawkLog.logPrintln("CyborgLeaguaWarService state change, oldSeason: {}, oldState: {} ,newSeason: {}, newState: {}", old_season, old_state, activityInfo.getSeason(),
					activityInfo.getState());
		}

	}
	
	/**
	 * 赛季预热开启
	 */
	private void onShow() {
		
	}
	
	/**
	 * 赛季开启
	 */
	private void onOpen() {
		ShopService.getInstance().forceRefresh(Shop.ShopType.CYBORG_SHOP, activityInfo.getTimeCfg().getOpenTimeValue());
	}
	
	/**
	 * 赛季结束展示
	 */
	private void onEnd() {
		sendDivisionReward();
		sendStarRankReward();
	}
	
	/**
	 * 赛季结束
	 */
	private void onHidden() {
		
	}
	
	/**
	 * 发放赛季结算段位排行奖励
	 */
	private void sendStarRankReward() {
		CLWActivityData activityData = getActivityData();
		int season = getSeason();
		int rankLimit = 100;
		Set<Tuple> tuples = CyborgWarRedis.getInstance().getCLWTeamStarRanks(0, rankLimit - 1, season);
		List<String> teamIds = tuples.stream().map(t -> t.getElement()).collect(Collectors.toList());
		Map<String, CWTeamData> teamDatas = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
		String serverId = GsConfig.getInstance().getServerId();
		int rank = 0;
		for (Tuple tuple : tuples) {
			rank++;
			String teamId = tuple.getElement();
			CWTeamData teamData = teamDatas.get(teamId);
			if (teamData == null) {
				continue;
			}
			// 非本服战队
			if (!serverId.equals(teamData.getServerId())) {
				HawkLog.logPrintln("CyborgLeaguaWarService sendStarRankReward ignore, teamId:{}, teamServer:{}, guildId:{}", teamData.getId(), teamData.getServerId(),
						teamData.getGuildId());
				continue;
			}
			CyborgSeasonRankAwardCfg cfg = getRewardCfgByRank(rank);
			List<ItemInfo> reward = cfg.getRewardList();
			Set<String> idList = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
			for (String playerId : idList) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.CLW_DIVISION_RANK_REWARD).addContents(rank)
						.setAwardStatus(MailRewardStatus.NOT_GET).addRewards(reward).build());
				HawkLog.logPrintln("CyborgLeaguaWarService sendStarRankReward success, playerId:{}, teamId:{}, guildId:{}, rank:{}", playerId, teamData.getId(),
						teamData.getGuildId(), rank);
				LogUtil.logCyborgSeasonRankAward(teamData.getGuildId(), teamId, season,serverId,teamData.getStar(),rank, playerId);
			}
		}
		
		activityData.setRankRewardFinish(true);
		CyborgWarRedis.getInstance().updateCLWActivityInfo(activityData);

	}
	
	/**
	 * 发放赛季段位结算奖励
	 */
	private void sendDivisionReward() {
		int season = getSeason();
		List<CWTeamData> teamList = CyborgWarService.getInstance().getServerTeam();
		List<CWTeamData> updateList = new ArrayList<>();
		String serverId = GsConfig.getInstance().getServerId();
		for (CWTeamData teamData : teamList) {
			// 未参与本期排位
			if (teamData.getInitSeason() != season) {
				HawkLog.logPrintln("CyborgLeaguaWarService sendDivisionReward ignore, season:{}, initSeason:{}, teamId:{}, guildId:{}", season, teamData.getInitSeason(),
						teamData.getId(), teamData.getGuildId());
				continue;
			}
			// 本赛季结算奖励已发放
			if (teamData.getRewardedSeason() == season) {
				HawkLog.logPrintln("CyborgLeaguaWarService sendDivisionReward failed, alreasySent, season:{}, initSeason:{}, teamId:{}, guildId:{}", season,
						teamData.getInitSeason(), teamData.getId(), teamData.getGuildId());
				continue;
			}
			String teamId = teamData.getId();
			int star = teamData.getStar();
			CyborgSeasonDivisionCfg cfg = getDivisionCfgByStar(star);
			if (cfg == null) {
				HawkLog.logPrintln("CyborgLeaguaWarService sendDivisionReward failed, cfgNotExist, season:{}, initSeason:{}, teamId:{}, guildId:{}, star:{}", season,
						teamData.getInitSeason(), teamData.getId(), teamData.getGuildId(), star);
				continue;
			}
			List<ItemInfo> reward = cfg.getDivisionAwardList();
			Set<String> idList = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
			for (String playerId : idList) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.CLW_DIVISION_FINAL_REWARD).addContents(star)
						.setAwardStatus(MailRewardStatus.NOT_GET).addRewards(reward).build());
				//Tlog
				LogUtil.logCyborgSeasonStarAward(teamData.getGuildId(), teamId, season, serverId, star, cfg.getId(), playerId);
				HawkLog.logPrintln("CyborgLeaguaWarService sendDivisionReward success, playerId:{}, season:{}, initSeason:{}, teamId:{}, guildId:{}, star:{}", playerId, season,
						teamData.getInitSeason(), teamData.getId(), teamData.getGuildId	(), star);
				ActivityManager.getInstance().postEvent(new CWGradeEvent(playerId, cfg.getId()));
			}
			teamData.setRewardedSeason(season);
			updateList.add(teamData);
		}
		CLWActivityData activityData = getActivityData();
		activityData.setDivisionRewardFinish(true);
		CyborgWarRedis.getInstance().updateCLWActivityInfo(activityData);
		if (!updateList.isEmpty()) {
			CyborgWarRedis.getInstance().updateCWTeamData(updateList);
		}
	}
	
	/**
	 * 构建活动状态信息
	 * @return
	 */
	public CSWStateInfo.Builder genCSWStateInfo(){
		CSWStateInfo.Builder builder = CSWStateInfo.newBuilder();
		CLWActivityData activityData = getActivityData();
		CLWActivityState state = activityData.getState();
		CyborgSeasonTimeCfg cfg = activityInfo.getTimeCfg();
		if (cfg != null) {
			builder.setSeason(activityData.getSeason());
			builder.setOpenTime(cfg.getShowTimeValue());
			builder.setStartTime(cfg.getOpenTimeValue());
			builder.setEndShowTime(cfg.getEndShowTimeValue());
			builder.setEndTime(cfg.getEndTimeValue());
		}
		switch (state) {
		case CLOSE:
			builder.setState(CSWState.CSW_NOT_OPEN);
			break;
		case NOT_OPEN:
			builder.setState(CSWState.CSW_NOT_OPEN);
			break;
		case SHOW:
			builder.setState(CSWState.CSW_OPEN_SHOW);
			break;
		case OPEN:
			builder.setState(CSWState.CSW_OPEN);
			break;
		case END:
			builder.setState(CSWState.CSW_END_SHOW);
			break;
		default:
			break;
		}
		return builder;
	}
	
	/**
	 * 是否在赛季期间(预览-结束展示)
	 * @return
	 */
	public boolean isInSeason() {
		if (getSeason() == 0) {
			return false;
		}
		CLWActivityState state = getActivityData().getState();
		if (state == CLWActivityState.CLOSE || state == CLWActivityState.NOT_OPEN) {
			return false;
		}
		return true;
	}
	
	/**
	 * 是否在赛季排位期间(仅排位)
	 * @return
	 */
	public boolean isSeasonOpen() {
		if (getSeason() == 0) {
			return false;
		}
		return getActivityData().getState() == CLWActivityState.OPEN;
	}
	
	/**
	 * 是否在赛季排位期间(排位-结束展示)
	 * @return
	 */
	public boolean isSeasonAfterOpen() {
		if (getSeason() == 0) {
			return false;
		}
		CLWActivityState state = getActivityData().getState();
		return state == CLWActivityState.OPEN || state == CLWActivityState.END;
	}
	
	/**
	 * 根据星级获取段位配置
	 * @param star
	 * @return
	 */
	public CyborgSeasonDivisionCfg getDivisionCfgByStar(int star) {
		ConfigIterator<CyborgSeasonDivisionCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgSeasonDivisionCfg.class);
		for (CyborgSeasonDivisionCfg cfg : its) {
			HawkTuple2<Integer, Integer> tuple = cfg.getStarRangeTuple();
			if (star >= tuple.first && star <= tuple.second) {
				return cfg;
			}
		}
		return null;
	}
	
	/**
	 * 根据段位排名获取排名奖励配置
	 * @param rank
	 * @return
	 */
	public CyborgSeasonRankAwardCfg getRewardCfgByRank(int rank) {
		ConfigIterator<CyborgSeasonRankAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgSeasonRankAwardCfg.class);
		for (CyborgSeasonRankAwardCfg cfg : its) {
			HawkTuple2<Integer, Integer> tuple = cfg.getRankRange();
			if (rank >= tuple.first && rank <= tuple.second) {
				return cfg;
			}
		}
		return null;
	}

	/**
	 * 根据星级和排行计算星级变化量
	 * @param star
	 * @param rank
	 * @return
	 */
	public int getStarChange(int star, int rank) {
		CyborgSeasonDivisionCfg cfg = getDivisionCfgByStar(star);
		if (cfg == null) {
			return 0;
		}
		return cfg.getStarChangeByRank(rank);
	}
	
	/**
	 * 获取当前最近一期的商店刷新时间配置
	 * @return
	 */
	public CyborgShopRefreshTimeCfg getNearbyCfg() {
		ConfigIterator<CyborgShopRefreshTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgShopRefreshTimeCfg.class);
		long now = HawkTime.getMillisecond();
		CyborgShopRefreshTimeCfg cfg = null;
		for (CyborgShopRefreshTimeCfg timeCfg : its) {
			if (now > timeCfg.getRefreshTimeValue()) {
				cfg = timeCfg;
			}
		}
		return cfg;
	}
	
	
	/**
	 * 获取下一次商店兑换重置时间
	 * @return
	 */
	public CyborgShopRefreshTimeCfg getNextCfg(){
		ConfigIterator<CyborgShopRefreshTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgShopRefreshTimeCfg.class);
		long now = HawkTime.getMillisecond();
		for (CyborgShopRefreshTimeCfg timeCfg : its) {
			if (now < timeCfg.getRefreshTimeValue()) {
				return timeCfg;
			}
		}
		return null;
	}
	
	/**
	 * 拉取赛博赛季段位排行
	 * @param player
	 * @return
	 */
	public GetCWTeamRankResp.Builder getSeasonStarRank(Player player) {
		GetCWTeamRankResp.Builder builder = GetCWTeamRankResp.newBuilder();
		CLWActivityData activityData = getActivityData();
		CLWActivityState state = activityData.getState();
		int season = activityData.getSeason();
		// 仅排位阶段及结束展示阶段显示排行
		if (state != CLWActivityState.OPEN && state != CLWActivityState.END) {
			return builder;
		}
		int rankLimit = 100;
		String selfGuildId = player.getGuildId();
		String selfTeamId = CyborgWarService.getInstance().getSelfTeamId(player);
		Set<Tuple> tuples = CyborgWarRedis.getInstance().getCLWTeamStarRanks(0, rankLimit - 1, season);
		List<String> teamIds = tuples.stream().map(t -> t.getElement()).collect(Collectors.toList());
		Map<String, CWTeamData> teamDatas = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
		CWTeamRank.Builder selfRank = null;
		int rank = 1;
		for (Tuple tuple : tuples) {
			String teamId = tuple.getElement();
			CWTeamData teamData = teamDatas.get(teamId);
			if (teamData == null) {
				continue;
			}
			CWTeamRank.Builder rankInfo = CWTeamRank.newBuilder();
			CWTeamInfo.Builder teamBuilder = CWTeamInfo.newBuilder();
			teamBuilder.setId(teamData.getId());
			teamBuilder.setGuildId(teamData.getGuildId());
			teamBuilder.setGuildName(teamData.getGuildName());
			teamBuilder.setGuildTag(teamData.getGuildTag());
			teamBuilder.setGuildFlag(teamData.getGuildFlag());
			teamBuilder.setName(teamData.getName());
			teamBuilder.setSeasonStar(teamData.getStar());
			teamBuilder.setSeasonScore(teamData.getSeasonScore());
			teamBuilder.setServerId(teamData.getServerId());
			rankInfo.setTeamInfo(teamBuilder);
			rankInfo.setRank(rank);
			builder.addRankInfo(rankInfo);
			if (teamId.equals(selfTeamId)) {
				selfRank = rankInfo;
			}
			rank++;
		}
		if (!HawkOSOperator.isEmptyString(selfTeamId)) {
			if (selfRank == null) {
				selfRank = CWTeamRank.newBuilder();
				CWTeamData teamData = CyborgWarRedis.getInstance().getCWTeamData(selfTeamId);
				CWTeamInfo.Builder teamBuilder = CWTeamInfo.newBuilder();
				teamBuilder.setId(teamData.getId());
				teamBuilder.setGuildId(selfGuildId);
				teamBuilder.setGuildName(teamData.getGuildName());
				teamBuilder.setGuildTag(teamData.getGuildTag());
				teamBuilder.setGuildFlag(teamData.getGuildFlag());
				teamBuilder.setName(teamData.getName());
				if (teamData.getInitSeason() == season) {
					teamBuilder.setSeasonStar(teamData.getStar());
					teamBuilder.setSeasonScore(teamData.getSeasonScore());
				} else {
					teamBuilder.setSeasonStar(0);
					teamBuilder.setSeasonScore(0);
				}
				teamBuilder.setServerId(teamData.getServerId());
				selfRank.setTeamInfo(teamBuilder);
				selfRank.setRank(-1);
			}
			builder.setSelfRank(selfRank);
		}
		return builder;
	}
	
	/***
	 * 获取联盟积分信息
	 * @param player
	 * @return
	 */
	public CSWScoreInfo.Builder genGuildSeasonScoreInfo(Player player) {
		CSWScoreInfo.Builder builder = CSWScoreInfo.newBuilder();
		String guildId = player.getGuildId();
		long score = 0;
		if(HawkOSOperator.isEmptyString(guildId)){
			builder.setScore(0);
			return builder;
		}
		CLWActivityData activityData = getActivityData();
		int season = activityData.getSeason();
		if(!isInSeason()){
			builder.setScore(0);
			return builder;
		}
		score = CyborgWarRedis.getInstance().getCLWGuildScore(guildId, season);
		builder.setScore(score);
		List<Integer> rewardedList = CyborgWarRedis.getInstance().getCLWRewardedList(player.getId(), season);
		if(!CollectionUtils.isEmpty(rewardedList)){
			builder.addAllRewardedId(rewardedList);
		}
		return builder;
	}
	
	/**
	 * 获取赛博商店redisKey的赛季参数
	 * @return
	 */
	public String getCyborgShopKeyInfo() {
		String seasonInfo = "";
		CyborgShopRefreshTimeCfg cfg = getNearbyCfg();
		if (cfg != null) {
			seasonInfo = cfg.getId() + ":";
		}
		return seasonInfo;
	}
}
