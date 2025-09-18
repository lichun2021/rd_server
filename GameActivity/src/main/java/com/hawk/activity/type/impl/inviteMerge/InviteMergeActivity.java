package com.hawk.activity.type.impl.inviteMerge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.inviteMerge.cfg.InviteMergeKVCfg;
import com.hawk.activity.type.impl.inviteMerge.cfg.InviteMergeRuleCfg;
import com.hawk.activity.type.impl.inviteMerge.cfg.InviteMergeServerRankCfg;
import com.hawk.activity.type.impl.inviteMerge.cfg.InviteMergeTimeCfg;
import com.hawk.activity.type.impl.inviteMerge.tmp.InviteMergeLeaderTmp;
import com.hawk.game.protocol.ActivityInviteMerge.InviteMergeInfo;
import com.hawk.game.protocol.ActivityInviteMerge.InviteMergeLeaderInfo;
import com.hawk.game.protocol.ActivityInviteMerge.InviteMergePageInfo;
import com.hawk.game.protocol.ActivityInviteMerge.InviteMergeProgress;
import com.hawk.game.protocol.ActivityInviteMerge.InviteMergeProgressCell;
import com.hawk.game.protocol.ActivityInviteMerge.InviteMergeRankCellInfo;
import com.hawk.game.protocol.ActivityInviteMerge.InviteMergeTimeInfo;
import com.hawk.game.protocol.ActivityInviteMerge.InviteMergeTurn;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.game.protocol.Rank;
import com.hawk.game.protocol.Status;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;


/**
 * 邀请合服活动
 * @author Golden
 *
 */
public class InviteMergeActivity extends ActivityBase {

	/**
	 * REDIS key
	 */
	static enum InviteMergeRedisKey {
		/**
		 *  队长信息
		 */
		ACTIVITY_INVITE_MERGE_LEADER,
		/**
		 *  投票权限
		 */
		ACTIVITY_INVITE_VOTE_PREMISSION,
		/**
		 * 合服邀请
		 */
		ACTIVITY_INVITE_MERGE_INVITE,
		/**
		 *  合服被邀请
		 */
		ACTIVITY_INVITE_MERGE_BE_INVITE,
		/**
		 * 邀请合服建联
		 */
		ACTIVITY_INVITE_MERGE_CONNECT,
		/**
		 *  投票赞成数量
		 */
		ACTIVITY_INVITE_MERGE_APPROVE,
		/**
		 *  投票反对数量
		 */
		ACTIVITY_INVITE_MERGE_OPPOSE,
		/**
		 *  邀请合服通过
		 */
		ACTIVITY_INVITE_MERGE_DEAL,
		/** 
		 * 玩家投票状态 -1 无权限 0 未投票 1 赞成 2反对
		 */
		ACTIVITY_INVITE_MERGE_PLAYER_STATUS,
		/**
		 *  重置标识,开启第二轮的时候设置
		 */
		ACTIVITY_INVITE_MERGE_RESET_MARK,
		/**
		 *  玩家投票状态重置标识
		 */
		ACTIVITY_INVITE_MERGE_PLAYER_STATUS_MARK,
		;
	}
	
	/**
	 * 队长信息
	 */
	private static Map<String, InviteMergeLeaderTmp> leaders = new HashMap<>();
	/**
	 * 投票权限列表
	 */
	private static Set<String> votePremission = new HashSet<>();
	/**
	 * 本服投票的目标区服
	 */
	private String voteTargetServer;
	
	private boolean turn3Ticked = false;
	private boolean turn6Ticked = false;
	private long lastLeaderTickTime = 0L;
	
	public InviteMergeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.INVITE_MERGE;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		InviteMergeActivity activity = new InviteMergeActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return null;
	}
	
	public void onShow() {
		leaders.clear();
		votePremission.clear();
		HawkLog.logPrintln("invite merge activity clear history votePremission onShow");
		super.onShow();
	}
	
	public void onHidden() {
		leaders.clear();
		votePremission.clear();
		HawkLog.logPrintln("invite merge activity clear history votePremission onHidden");
		super.onHidden();
	}
	
	@Override
	public void onQuickTick() {
		if (!isOpening(null)) {
			turn3Ticked = false;
			turn6Ticked = false;
			
			//选队长：在活动open前5分钟内确定
			chooseLeader();
			//投票权限：在活动open前5分钟内确定投票权限，此时活动状态还是show状态，还没真正开启
			onVotePermission();
			return;
		}
		
		// 阶段3tick
		turn3Tick();
		// 阶段4tick
		turn4Tick();
		// 阶段6tick
		turn6Tick();
		// 队长tick 检测红点
		leaderTickSync();
	}
	
	/**
	 * 选投票权限
	 */
	public void onVotePermission() {
		try {
			ActivityState state = getIActivityEntity(null).getActivityState();
			if (state != ActivityState.SHOW) {
				return;
			}
			long currentTime = HawkApp.getInstance().getCurrentTime();
			InviteMergeTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(InviteMergeTimeCfg.class, getActivityTermId());
			InviteMergeKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(InviteMergeKVCfg.class);
			if (currentTime > timeCfg.getStartTimeValue() || currentTime < timeCfg.getStartTimeValue() - kvCfg.getChooseLeaderTimeLimit()) {
				return;
			}
			
			String serverId = getDataGeter().getServerId();
			if (!votePremission.isEmpty()) {
				HawkLog.logPrintln("invite merge activity onVotePermission break, votePremission players not empty");
				return;
			}
			
			// 从redis拿
			int termId = getActivityTermId();
			String key = String.format("%s:%d:%s", InviteMergeRedisKey.ACTIVITY_INVITE_VOTE_PREMISSION.toString(), termId, serverId);
			Set<String> infos = getRedis().hKeys(key, 0);
			if (infos != null && !infos.isEmpty()) {
				votePremission = infos;
				HawkLog.logPrintln("invite merge activity onVotePermission done from redis, player count: {}", infos.size());
				return;
			}
			
			InviteMergeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InviteMergeKVCfg.class);
			// 取排行榜信息,写入redis
			Map<String, String> info = new HashMap<>();
			Set<Tuple> rankSet = getDataGeter().getRankList(Rank.RankType.PLAYER_NOARMY_POWER_RANK, cfg.getVotingPlayerNum());
	        for (Tuple tuple : rankSet) {
	            String playerId = tuple.getElement();
	            votePremission.add(playerId);
	            info.put(playerId, HawkTime.formatNowTime());
	            try {
		            // 发邮件
		    		Object[] subTitle = new Object[] {};
		    		Object[] content = new Object[] { getLeaderInfo(serverId).getPlayerName(), cfg.getVotingPlayerNum()};
		    		sendMailToPlayer(playerId, MailId.MERGE_INVITE_PERMISSION, subTitle, content, new ArrayList<>());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
	        }
	        // 存到redis
	        getRedis().hmSet(key, info, 0);
	        HawkLog.logPrintln("invite merge activity onVotePermission done, player count: {}", rankSet.size());
	        
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 队长tick 检测红点
	 */
	private void leaderTickSync() {
		try {
			//  5s一次
			long currentTime = HawkTime.getMillisecond();
			if (currentTime - lastLeaderTickTime < 5 * 1000L) {
				return;
			}
			lastLeaderTickTime = currentTime;
			
			// 获取队长信息
			String serverId = getDataGeter().getServerId();
			InviteMergeLeaderTmp leaderInfo = getLeaderInfo(serverId);
			if (leaderInfo == null) {
				return;
			}
			
			// 不在线不发
			String playerId = leaderInfo.getPlayerId();
			if (!getDataGeter().isOnlinePlayer(playerId)) {
				return;
			}
			
			inviteMergeRankInfo(playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		// 配置里没有,证明本服没资格参与活动
		String serverId = getDataGeter().getServerId();
		InviteMergeServerRankCfg rankCfg = HawkConfigManager.getInstance().getConfigByKey(InviteMergeServerRankCfg.class, serverId);
		if (rankCfg == null) {
			return true;
		}
		return false;
	}
	
	/**
	 * 选择队长(谁可以邀请合服)
	 */
	private void chooseLeader() {
		try {
			ActivityState state = getIActivityEntity(null).getActivityState();
			if (state != ActivityState.SHOW) {
				return;
			}
			long currentTime = HawkApp.getInstance().getCurrentTime();
			InviteMergeTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(InviteMergeTimeCfg.class, getActivityTermId());
			InviteMergeKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(InviteMergeKVCfg.class);
			if (currentTime > timeCfg.getStartTimeValue() || currentTime < timeCfg.getStartTimeValue() - kvCfg.getChooseLeaderTimeLimit()) {
				return;
			}
			String playerId = getDataGeter().chooseInviteMergeLeader();
			if (HawkOSOperator.isEmptyString(playerId)) {
				HawkLog.logPrintln("invite merge activity choose leader failed, leaderId: {}", playerId);
				return;
			}
			
			String serverId = getDataGeter().getServerId();
			InviteMergeLeaderTmp leader = getLeaderInfo(serverId);
			if (leader != null && leader.getPlayerId().equals(playerId)) {
				HawkLog.logPrintln("invite merge activity choose leader from cache, leaderId: {}", playerId);
				return;
			}
			
			String playerName = getDataGeter().getPlayerName(playerId);
			String guildTag = getDataGeter().getGuildTagByPlayerId(playerId);
			InviteMergeLeaderTmp info = new InviteMergeLeaderTmp();
			info.setPlayerId(playerId);
			info.setPlayerName(playerName);
			if (!HawkOSOperator.isEmptyString(guildTag)) {
				info.setGuildTag(guildTag);
			}
			updateLeaderInfo(info);
			HawkLog.logPrintln("invite merge activity choose leader, leaderId: {}, leaderName: {}", playerId, playerName);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 投票结果展示1阶段
	 */
	private void turn3Tick() {
		// 阶段拦截
		if (getInviteMergeTurn() != InviteMergeTurn.INVITE_MERGE_TURN_3) {
			return;
		}
		// 只tick一次
		if (turn3Ticked) {
			return;
		}
		turn3Ticked = checkDeal();
	}
	
	/**
	 * 投票结果展示2阶段
	 */
	private void turn6Tick() {
		// 阶段拦截
		if (getInviteMergeTurn() != InviteMergeTurn.INVITE_MERGE_TURN_6 && getInviteMergeTurn() != InviteMergeTurn.INVITE_MERGE_TURN_7) {
			return;
		}
		// 只tick一次
		if (turn6Ticked) {
			return;
		}
		turn6Ticked = checkDeal();
	}
	
	/**
	 * 检测投票
	 */
	private boolean checkDeal() {
		int step = 0;
		try {
			voteTargetServer = null; //投票阶段结束了，本地缓存的目标区服要重置
			String serverId = getDataGeter().getServerId();
			step = 1;
			String targetServerId = this.hGet(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_CONNECT), serverId, 0);
			if (HawkOSOperator.isEmptyString(targetServerId)) {
				return true;
			}
			
			step = 2;
			// 对方赞成数量
			int bApproveCnt = getTickCnt(true, targetServerId);
			// 对方反对数量
			int bOpposeCnt = getTickCnt(false, targetServerId);
			// 我方赞成数量
			int aApproveCnt = getTickCnt(true, serverId);
			// 我方反对数量
			int aOpposeCnt = getTickCnt(false, serverId);
			
			// deal
			int succ = 0;
			if (bApproveCnt >= bOpposeCnt && aApproveCnt >= aOpposeCnt) {
				succ = 1;
				getRedis().hSet(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_DEAL), serverId, targetServerId);
				step = 3;
				getRedis().hSet(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_DEAL), targetServerId, serverId);
			}
			
			step = 0;
			Map<String, Object> param = new HashMap<>();
			param.put("termId", getActivityTermId());
	        param.put("serverA", serverId);        //区服1
	        param.put("approveCntA", aApproveCnt); //区服1支持票数量
	        param.put("opposeCntA", aOpposeCnt);   //区服1反对票数量
	        param.put("serverB", targetServerId);  //区服2
	        param.put("approveCntB", bApproveCnt); //区服2支持票数量
	        param.put("opposeCntB", bOpposeCnt);   //区服2反对票数量
	        param.put("voteResult", succ);         //投票结果: 1成功0失败
	        param.put("voteStage", getInviteMergeTurn() == InviteMergeTurn.INVITE_MERGE_TURN_3 ? 1 : 2); //投票阶段
	        getDataGeter().logActivityCommon(LogInfoType.merge_vote_result, param);
		} catch (Exception e) {
			HawkException.catchException(e);
			//执行到中间失败了，还原之前的设置
			if (step == 3) {
				try {
					getRedis().hDel(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_DEAL), getDataGeter().getServerId());
				} catch (Exception e1) {
					HawkException.catchException(e1);
				}
			}
		}
		return step == 0;
	}
	
	/**
	 * 第二轮开启tick,清除之前的信息
	 */
	private void turn4Tick() {
		int step = 0;
		try {
			// 阶段拦截
			if (getInviteMergeTurn().getNumber() < InviteMergeTurn.INVITE_MERGE_TURN_4_VALUE) {
				return;
			}
			
			// 重复执行检测
			String serverId = getDataGeter().getServerId();
			String key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_RESET_MARK);
			String str = this.hGet(key, serverId, 0);
			if (!HawkOSOperator.isEmptyString(str)) {
				return;
			}
			getRedis().hSet(key, serverId, HawkTime.formatNowTime());
			
			step = 1;
			// 证明已经匹配成功了
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_DEAL);
			String deal = this.hGet(key, serverId, 0);
			if (!HawkOSOperator.isEmptyString(deal)) {
				return;
			}
			
			step = 2;
			// 合服邀请
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_INVITE, serverId);
			getRedis().del(key);
			
			// 合服被邀请
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_BE_INVITE, serverId);
			getRedis().del(key);
			
			// 邀请合服建联
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_CONNECT);
			getRedis().hDel(key, serverId);
			
			// 投票赞成数量
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_APPROVE);
			getRedis().hDel(key, serverId);
			
			// 投票反对数量
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_OPPOSE);
			getRedis().hDel(key, serverId);
		} catch (Exception e) {
			HawkException.catchException(e);
			//执行到中间就失败了，需要还原之前的设置
			if (step == 1) {
				try {
					getRedis().hDel(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_RESET_MARK), getDataGeter().getServerId());
				} catch (Exception e1) {
					HawkException.catchException(e1);
				}
			}
		}
	}
	
	/**
	 * 获取redisSession
	 * @return
	 */
	private HawkRedisSession getRedis() {
		return ActivityGlobalRedis.getInstance().getRedisSession();
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(null)) {
			return;
		}
		inviteMergePageInfo(playerId);
	}
	
	/**
	 * 获取队长信息(谁可以邀请合服)
	 * @return
	 */
	private InviteMergeLeaderTmp getLeaderInfo(String serverId) {
		int termId = getActivityTermId();
		// 从缓存拿
		InviteMergeLeaderTmp leader = leaders.get(serverId);
		if (leader != null) {
			return leader;
		}
		
		// 从redis拿
		String key = InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_LEADER + ":" + termId;
		String str = getRedis().hGet(key, serverId);
		if (!HawkOSOperator.isEmptyString(str)) {
			leader = InviteMergeLeaderTmp.deSerializ(str);
			leaders.put(serverId, leader);
		}
		
		if (leader == null) {
			String playerId = getDataGeter().chooseInviteMergeLeader();
			if (!HawkOSOperator.isEmptyString(playerId)) {
				String playerName = getDataGeter().getPlayerName(playerId);
				String guildTag = getDataGeter().getGuildTagByPlayerId(playerId);
				InviteMergeLeaderTmp info = new InviteMergeLeaderTmp();
				info.setPlayerId(playerId);
				info.setPlayerName(playerName);
				if (!HawkOSOperator.isEmptyString(guildTag)) {
					info.setGuildTag(guildTag);
				}
				updateLeaderInfo(info);
				HawkLog.logPrintln("invite merge activity choose leader when getLeaderInfo, leaderId: {}, leaderName: {}", playerId, playerName);
			}
		}
		
		return leader;
	}
	
	/**
	 * 获取队长信息PB
	 * @param serverId
	 * @return
	 */
	private InviteMergeLeaderInfo.Builder getLeaderInfoPB(String serverId) {
		InviteMergeLeaderTmp leaderInfo = getLeaderInfo(serverId);
		if (leaderInfo == null) {
			return null;
		}
		InviteMergeLeaderInfo.Builder builder = InviteMergeLeaderInfo.newBuilder();
		builder.setLeaderId(leaderInfo.getPlayerId());
		builder.setLeaderName(leaderInfo.getPlayerName());
		String guildTag = leaderInfo.getGuildTag();
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			builder.setLeaderGuildTag(guildTag);
		}
		return builder;
		
	}
	
	/**
	 * 更新对账信息
	 * @param builder
	 */
	private void updateLeaderInfo(InviteMergeLeaderTmp info) {
		// 存储到缓存
		int termId = getActivityTermId();
		String serverId = getDataGeter().getServerId();
		leaders.put(serverId, info);
		
		// 存到redis
		String key = InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_LEADER + ":" + termId;
		getRedis().hSet(key, serverId, info.serializ());
	}
	
	/**
	 * 同步界面信息
	 */
	public void inviteMergePageInfo(String playerId) {
		
		// 检测玩家投票状态
		checkPlayerStatusReset(playerId);
		
		String serverId = getDataGeter().getServerId();
		
		InviteMergePageInfo.Builder builder = InviteMergePageInfo.newBuilder();
		// 当前阶段
		builder.setInviteMergeTurn(getInviteMergeTurn());
		// 队长
		InviteMergeLeaderInfo.Builder leaderBuilder = getLeaderInfoPB(serverId);
		if (leaderBuilder != null) {
			builder.setLeader(leaderBuilder);
		}
		// 目标区服ID
		String targetServerId = getRedis().hGet(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_CONNECT), serverId);
		if (!HawkOSOperator.isEmptyString(targetServerId)) {
			builder.setTargetServerId(targetServerId);
			// 对方赞成数量
			builder.setBApproveCnt(getTickCnt(true, targetServerId));
			// 对方反对数量
			builder.setBOpposeCnt(getTickCnt(false, targetServerId));
			// 我方赞成数量
			builder.setAApproveCnt(getTickCnt(true, serverId));
			// 我方反对数量
			builder.setAOpposeCnt(getTickCnt(false, serverId));
			// 投票是否通过
			String deal = getRedis().hGet(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_DEAL), serverId);
			if (!HawkOSOperator.isEmptyString(deal)) {
				builder.setDeal(true);
			} else {
				builder.setDeal(false);
			}
			// 我的投票状态
			if (!votePremission.contains(playerId)) {
				builder.setMySatus(-1);
			} else {
				String mySatus = getRedis().getString(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_PLAYER_STATUS, playerId));
				if (!HawkOSOperator.isEmptyString(mySatus)) {
					builder.setMySatus(Integer.parseInt(mySatus));
				} else {
					builder.setMySatus(0);
				}
			}
			InviteMergeServerRankCfg cfg = HawkConfigManager.getInstance().getConfigByKey(InviteMergeServerRankCfg.class, targetServerId);
			InviteMergeRankCellInfo.Builder cell = InviteMergeRankCellInfo.newBuilder();
			cell.setRank(cfg.getRank());
			cell.setServerId(cfg.getServerId());
			cell.setPower(cfg.getPower());
			cell.setIsSuper(false);
			cell.setType(cfg.getType());
			cell.setInvited(true);
			cell.setBeInvited(true);
			cell.setConnected(true);
			cell.setDealed(true);
			InviteMergeLeaderTmp leaderInfo = getLeaderInfo(cfg.getServerId());
			if (leaderInfo != null) {
				cell.setLeaderName(leaderInfo.getPlayerName());
				String guildTag = leaderInfo.getGuildTag();
				if (!HawkOSOperator.isEmptyString(guildTag)) {
					cell.setLeaderGuildTag(guildTag);
				}
			}
			builder.setTargetServceInfo(cell);
		}
		
		InviteMergeTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(InviteMergeTimeCfg.class, getActivityTermId());
		InviteMergeTimeInfo.Builder timeInfo = InviteMergeTimeInfo.newBuilder();
		timeInfo.setShowTime(timeCfg.getShowTimeValue());
		timeInfo.setStartTime(timeCfg.getStartTimeValue());
		timeInfo.setInviteEndTime1(timeCfg.getInviteEndTime1Value());
		timeInfo.setVoteEntTime1(timeCfg.getVoteEntTime1Value());
		timeInfo.setShowEndTime1(timeCfg.getShowEndTime1Value());
		timeInfo.setInviteEndTime2(timeCfg.getInviteEndTime2Value());
		timeInfo.setVoteEntTime2(timeCfg.getVoteEntTime2Value());
		timeInfo.setShowEndTime2(timeCfg.getShowEndTime2Value());
		timeInfo.setEndTime(timeCfg.getEndTimeValue());
		timeInfo.setHiddenTime(timeCfg.getHiddenTimeValue());
		builder.setTimeInfo(timeInfo);
		
		// 同步协议
		pushToPlayer(playerId, HP.code2.ACTIVITY_INVITE_MERGE_PAGE_RESP_VALUE, builder);
	}
	
	/**
	 * 排行界面
	 * @param playerId
	 */
	public void inviteMergeRankInfo(String playerId) {
		String serverId = getDataGeter().getServerId();
		// 邀请列表
		Set<String> inviteSet = getRedis().hKeys(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_INVITE, serverId), 0);
		// 被邀请列表
		Set<String> beInviteSet = getRedis().hKeys(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_BE_INVITE, serverId), 0);
		// 已经建联
		Set<String> connects = getRedis().hKeys(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_CONNECT), 0);
		// 已经确认
		Set<String> deals = getRedis().hKeys(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_DEAL), 0);
		
		InviteMergeInfo.Builder builder = InviteMergeInfo.newBuilder();
		ConfigIterator<InviteMergeServerRankCfg> iter = HawkConfigManager.getInstance().getConfigIterator(InviteMergeServerRankCfg.class);
		while(iter.hasNext()) {
			InviteMergeServerRankCfg cfg = iter.next();
			if (!cfg.getAreaId().equals(getDataGeter().getAreaId())) {
				continue;
			}
			InviteMergeRankCellInfo.Builder cell = InviteMergeRankCellInfo.newBuilder();
			cell.setRank(cfg.getRank());
			cell.setServerId(cfg.getServerId());
			cell.setPower(cfg.getPower());
			cell.setIsSuper(false);
			cell.setType(cfg.getType());
			if (inviteSet != null && inviteSet.contains(cfg.getServerId())) {
				cell.setInvited(true);
			} else {
				cell.setInvited(false);
			}
			if (beInviteSet != null && beInviteSet.contains(cfg.getServerId())) {
				cell.setBeInvited(true);
			} else {
				cell.setBeInvited(false);
			}
			if (connects != null && connects.contains(cfg.getServerId())) {
				cell.setConnected(true);
			} else {
				cell.setConnected(false);
			}
			if (deals != null && deals.contains(cfg.getServerId())) {
				cell.setDealed(true);
			} else {
				cell.setDealed(false);
			}
			InviteMergeLeaderTmp leaderInfo = getLeaderInfo(cfg.getServerId());
			if (leaderInfo != null) {
				cell.setLeaderName(leaderInfo.getPlayerName());
				String guildTag = leaderInfo.getGuildTag();
				if (!HawkOSOperator.isEmptyString(guildTag)) {
					cell.setLeaderGuildTag(guildTag);
				}
			}
			builder.addInfo(cell);
			
			if (cfg.getServerId().equals(getDataGeter().getServerId())) {
				builder.setMyInfo(cell);
			}
		}
		// 同步协议
		pushToPlayer(playerId, HP.code2.ACTIVITY_INVITE_MERGE_INFO_RESP_VALUE, builder);
	}
	
	/**
	 * 获取进度
	 */
	public void getProgress(String playerId) {
		InviteMergeProgress.Builder builder = InviteMergeProgress.newBuilder();
		
		// 建联信息
		Map<String, String> connectMap = getRedis().hGetAll(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_CONNECT));  
		// 通过信息
		Map<String, String> dealmap = getRedis().hGetAll(getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_DEAL));
		
		// 没有建联信息,直接返回
		if (connectMap == null) {
			pushToPlayer(playerId, HP.code2.ACTIVITY_INVITE_MERGE_PROGRESS_RESP_VALUE, builder);
			return;
		}
		// 建联都是双向的,这里去重下
		Set<String> connectKeys = new HashSet<>();
		for (Entry<String, String> entry : connectMap.entrySet()) {
			if (connectKeys.contains(entry.getKey()) || connectKeys.contains(entry.getValue()) ) {
				continue;
			}
			connectKeys.add(entry.getKey());
		}
		
		for (String aServerId : connectKeys) {
			InviteMergeProgressCell.Builder cell = InviteMergeProgressCell.newBuilder();
			InviteMergeServerRankCfg aCfg = HawkConfigManager.getInstance().getConfigByKey(InviteMergeServerRankCfg.class, aServerId);
			cell.setAServerId(aCfg.getServerId());
			cell.setAIsSuper(false);
			cell.setARank(aCfg.getRank());
			cell.setAPower(aCfg.getPower());
			cell.setAType(aCfg.getType());
			
			String bServerId = connectMap.get(aServerId);
			InviteMergeServerRankCfg bCfg = HawkConfigManager.getInstance().getConfigByKey(InviteMergeServerRankCfg.class, bServerId);
			cell.setBServerId(bCfg.getServerId());
			cell.setBIsSuper(false);
			cell.setBRank(bCfg.getRank());
			cell.setBPower(bCfg.getPower());
			cell.setBType(bCfg.getType());
			
			if (dealmap != null && dealmap.containsKey(aServerId)) {
				cell.setDeal(true);
			} else {
				cell.setDeal(false);
			}
			builder.addCell(cell);
		}
		pushToPlayer(playerId, HP.code2.ACTIVITY_INVITE_MERGE_PROGRESS_RESP_VALUE, builder);
	}
	/**
	 * 获取合服邀请活动阶段
	 * 
	 *  INVITE_MERGE_TURN_0 = 0; // 预览阶段
	 * 	INVITE_MERGE_TURN_1 = 1; // 司令选择合服目标
		INVITE_MERGE_TURN_2 = 2; // 玩家投票
		INVITE_MERGE_TURN_3 = 3; // 本轮次投票结果展示
		INVITE_MERGE_TURN_4 = 4; // 司令选择合服目标
		INVITE_MERGE_TURN_5 = 5; // 玩家投票
		INVITE_MERGE_TURN_6 = 6; // 本轮次投票结果展示
		INVITE_MERGE_TURN_7 = 7; // 展示最终结果
	 */
	public InviteMergeTurn getInviteMergeTurn() {
		// 阶段
		InviteMergeTurn turn = InviteMergeTurn.INVITE_MERGE_TURN_0;
		long currentTime = HawkApp.getInstance().getCurrentTime();
		InviteMergeTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(InviteMergeTimeCfg.class, getActivityTermId());
		if (currentTime < cfg.getStartTimeValue()) {
			return InviteMergeTurn.INVITE_MERGE_TURN_0;
		}
		if (currentTime >= cfg.getStartTimeValue() && currentTime < cfg.getInviteEndTime1Value()) {
			return InviteMergeTurn.INVITE_MERGE_TURN_1;
		}
		if (currentTime >= cfg.getInviteEndTime1Value() && currentTime < cfg.getVoteEntTime1Value()) {
			return InviteMergeTurn.INVITE_MERGE_TURN_2;
		}
		if (currentTime >= cfg.getVoteEntTime1Value() && currentTime < cfg.getShowEndTime1Value()) {
			return InviteMergeTurn.INVITE_MERGE_TURN_3;
		}
		if (currentTime >= cfg.getShowEndTime1Value() && currentTime < cfg.getInviteEndTime2Value()) {
			return InviteMergeTurn.INVITE_MERGE_TURN_4;
		}
		if (currentTime >= cfg.getInviteEndTime2Value() && currentTime < cfg.getVoteEntTime2Value()) {
			return InviteMergeTurn.INVITE_MERGE_TURN_5;
		}
		if (currentTime >= cfg.getVoteEntTime2Value() && currentTime < cfg.getShowEndTime2Value()) {
			return InviteMergeTurn.INVITE_MERGE_TURN_6;
		}
		if (currentTime >= cfg.getShowEndTime2Value()) {
			return InviteMergeTurn.INVITE_MERGE_TURN_7;
		}
		return turn;
	}
	
	/**
	 * 获取投票数量
	 * @param mark true 赞成  false 反对
	 * @param serverId
	 * @return
	 */
	public int getTickCnt(boolean mark, String serverId) {
		// 对方赞成数量
		String key;
		if (mark) {
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_APPROVE);
		} else {
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_OPPOSE);
		}
		String str = this.hGet(key, serverId, 0);
		if (HawkOSOperator.isEmptyString(str)) {
			return 0;
		} else {
			return Integer.parseInt(str);
		}
	}
	
	/**
	 * 检测重置玩家投票状态
	 */
	public void checkPlayerStatusReset(String playerId) {
		if (getInviteMergeTurn().getNumber() <= InviteMergeTurn.INVITE_MERGE_TURN_3_VALUE) {
			return;
		}
		
		// 重复执行检测
		try {
			String key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_PLAYER_STATUS_MARK, playerId);
			String info = this.getString(key, 0);
			if (!HawkOSOperator.isEmptyString(info)) {
				return;
			}
			getRedis().setString(key, HawkTime.formatNowTime());
			
			// 删除状态key
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_PLAYER_STATUS, playerId);
			getRedis().del(key);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public String getKey(InviteMergeRedisKey key) {
		return key.toString() + ":" + getActivityTermId();
	}
	
	public String getKey(InviteMergeRedisKey key, String key1) {
		return key.toString() + ":" + getActivityTermId() + ":" + key1;
	}
	
	/**
	 * 邀请
	 * @param playerId
	 * @param targetServerId
	 * @param delete (删除邀请)
	 */
	public void invite(String playerId, String targetServerId, boolean delete) {
		String serverId = getDataGeter().getServerId();
		InviteMergeServerRankCfg rankCfgA = HawkConfigManager.getInstance().getConfigByKey(InviteMergeServerRankCfg.class, serverId);
		InviteMergeServerRankCfg rankCfgB = HawkConfigManager.getInstance().getConfigByKey(InviteMergeServerRankCfg.class, targetServerId);
		if (rankCfgA == null || rankCfgB == null) {
			HawkLog.errPrintln("invite merge failed, config not exist, playerId: {}, serverId: {}, targetServer: {}", playerId, serverId, targetServerId);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.ACTIVITY_INVITE_MERGE_INVITE_VALUE, Status.SysError.CONFIG_ERROR_VALUE);
			return;
		}
		if (!InviteMergeRuleCfg.canMerge(rankCfgA.getType(), rankCfgB.getType())) {
			HawkLog.errPrintln("invite merge failed, rule not permit, playerId: {}, serverId: {}, targetServer: {}", playerId, serverId, targetServerId);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.ACTIVITY_INVITE_MERGE_INVITE_VALUE, Status.Error.INVITE_MERGE_NOT_PERMIT_VALUE);
			return;
		}
		
		// 权限判断
		InviteMergeLeaderTmp leaderInfo = getLeaderInfo(serverId);
		if (leaderInfo == null) {
			HawkLog.errPrintln("invite failed leaderInfo null, playerId: {}, targetServerId: {}, delete: {}", playerId, targetServerId, delete);
			return;
		}
		
		if (!leaderInfo.getPlayerId().equals(playerId)) {
			return;
		}
		
		String key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_INVITE, serverId);
		if (delete) {
			getRedis().hDel(key, targetServerId);
		} else {
			getRedis().hSet(key, targetServerId, HawkTime.formatNowTime());
		}
		
		// 添加到对方的被邀请列表
		key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_BE_INVITE, targetServerId);
		if (delete) {
			getRedis().hDel(key, serverId);
		} else {
			getRedis().hSet(key, serverId, HawkTime.formatNowTime());
		}
		
		Map<String, Object> param = new HashMap<>();
		param.put("termId", getActivityTermId());
        param.put("targetServerId", targetServerId); //目标区服
        param.put("inviteOper", delete ? 0 : 1);         //操作类型：0撤回邀请，1发出邀请，2接受邀请，3拒绝邀请
        getDataGeter().logActivityCommon(playerId, LogInfoType.merge_invite_oper, param);
	}
	
	/**
	 * 接受邀请
	 * @param playerId
	 * @param targetServerId
	 */
	public boolean acceptInvite(String playerId, String targetServerId) {
		String serverId = getDataGeter().getServerId();
		// 权限判断
		InviteMergeLeaderTmp leaderInfo = getLeaderInfo(serverId);
		if (leaderInfo == null) {
			HawkLog.errPrintln("acceptInvite failed leaderInfo null, playerId: {}, targetServerId: {}", playerId, targetServerId);
			return false;
		}
		if (!leaderInfo.getPlayerId().equals(playerId)) {
			return false;
		}

		// 先检查下目标区服是否匹配成功了
		String key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_CONNECT);
		String targetConnect = this.hGet(key, targetServerId, 0);
		if (!HawkOSOperator.isEmptyString(targetConnect)) {
			// 对方区服已经匹配成功了,我方这边删除掉
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_BE_INVITE, serverId);		
			getRedis().hDel(key, targetServerId);
			return false;
		}
		
		// 我方区服已经建联了
		String ownConnect = this.hGet(key, serverId, 0);
		if (!HawkOSOperator.isEmptyString(ownConnect)) {
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_INVITE, targetServerId);
			getRedis().hDel(key, serverId);
			return false;
		}
		
		// 邀请列表删除
		key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_INVITE, serverId);  //我方邀请了谁
		getRedis().del(key);
		key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_INVITE, targetServerId); //对方邀请了谁
		getRedis().del(key);
		
		// 被邀请列表删除
		key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_BE_INVITE, serverId); //我方被谁邀请了  TODO	
		Map<String, String> inviteMeMap = getRedis().hGetAll(key);
		getRedis().del(key);
		for (Entry<String, String> entry : inviteMeMap.entrySet()) {
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_INVITE, entry.getKey());
			getRedis().hDel(key, serverId);
		}
		
		key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_BE_INVITE, targetServerId); //对方被谁邀请了  TODO	
		Map<String, String> inviteTaMap = getRedis().hGetAll(key);
		getRedis().del(key);
		for (Entry<String, String> entry : inviteTaMap.entrySet()) {
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_INVITE, entry.getKey());
			getRedis().hDel(key, targetServerId);
		}
		
		// 建联系
		key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_CONNECT);
		getRedis().hSet(key, targetServerId, serverId);
		getRedis().hSet(key, serverId, targetServerId);
		
		Map<String, Object> param = new HashMap<>();
		param.put("termId", getActivityTermId());
        param.put("targetServerId", targetServerId); //目标区服
        param.put("inviteOper", 2);         //操作类型：0撤回邀请，1发出邀请，2接受邀请，3拒绝邀请
        getDataGeter().logActivityCommon(playerId, LogInfoType.merge_invite_oper, param);
		return true;
	}
	
	/**
	 * 拒绝邀请
	 * @param playerId
	 * @param targetServerId
	 */
	public boolean refuseInvite(String playerId, String targetServerId) {
		String serverId = getDataGeter().getServerId();
		
		// 权限判断
		InviteMergeLeaderTmp leaderInfo = getLeaderInfo(serverId);
		if (leaderInfo == null) {
			HawkLog.errPrintln("refuseInvite failed leaderInfo null, playerId: {}, targetServerId: {}", playerId, targetServerId);
			return false;
		}
		if (!leaderInfo.getPlayerId().equals(playerId)) {
			return false;
		}

		// 对方邀请列表删除
		String key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_INVITE, targetServerId);
		getRedis().hDel(key, serverId);

		// 我方被邀请列表删除
		key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_BE_INVITE, serverId);		
		getRedis().hDel(key, targetServerId);
		
		Map<String, Object> param = new HashMap<>();
		param.put("termId", getActivityTermId());
        param.put("targetServerId", targetServerId); //目标区服
        param.put("inviteOper", 3);         //操作类型：0撤回邀请，1发出邀请，2接受邀请，3拒绝邀请
        getDataGeter().logActivityCommon(playerId, LogInfoType.merge_invite_oper, param);
		return true;
	}
	
	/**
	 * 投票
	 */
	public void ticket(String playerId, boolean ticket) {
		if (!votePremission.contains(playerId)) {
			return;
		}
		
		String key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_PLAYER_STATUS, playerId);
		String statusStr = getRedis().getString(key);
		if (!HawkOSOperator.isEmptyString(statusStr)) {
			return;
		}
		
		String serverId = getDataGeter().getServerId();
		
		getRedis().setString(key, ticket ? "1" : "2");
		
		// 投票 1赞成 2反对
		if (ticket) {
			getRedis().setString(key, String.valueOf(1));
			
			// 投票赞成数量
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_APPROVE);
			getRedis().hIncrBy(key, serverId, 1);
		} else {
			getRedis().setString(key, String.valueOf(2));
			
			// 投票反对数量
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_OPPOSE);
			getRedis().hIncrBy(key, serverId, 1);
		}
		
		if (HawkOSOperator.isEmptyString(voteTargetServer)) {
			key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_CONNECT);
			String str = getRedis().hGet(key, serverId);
			voteTargetServer = str == null ? "" : str;
		}
		
		Map<String, Object> param = new HashMap<>();
		param.put("termId", getActivityTermId());
        param.put("targetServerId", voteTargetServer); //目标区服
        param.put("voteCode", ticket ? 1 : 0);       //操作类型：0投票反对，1投票支持
        getDataGeter().logActivityCommon(playerId, LogInfoType.merge_invite_vote, param);
	}
	
	/**
	 * 匹配状态回退
	 * @param server1 区服1
	 * @param server2 区服2
	 */
	public void inviteMergeStateBack(String server1, String server2) {
		//反向建立联系
		String key = getKey(InviteMergeRedisKey.ACTIVITY_INVITE_MERGE_CONNECT);
		getRedis().hDel(key, server1, server2);
	}
	
	/**
	 * redis访问内部接口，用于容错
	 * @return
	 */
	private String hGet(String key, String field, int expireSeconds) {
		try (Jedis jedis = getRedis().getJedis(); Pipeline pipeline = jedis.pipelined()) {
			Response<String> resp = pipeline.hget(key, field);
			if (expireSeconds > 0) {
				pipeline.expire(key, expireSeconds);
			}
			pipeline.sync();
			return resp.get();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		throw new RuntimeException(key + " - " + field);
	}
	
	/**
	 * redis访问内部接口，用于容错
	 * @return
	 */
	private String getString(String key, int expireSeconds) {
		try (Jedis jedis = getRedis().getJedis(); Pipeline pipeline = jedis.pipelined()) {
			Response<String> resp = pipeline.get(key);
			if (expireSeconds > 0) {
				pipeline.expire(key, expireSeconds);
			}
			pipeline.sync();
			return resp.get();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		throw new RuntimeException(key);
	}
	
}
