package com.hawk.game.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hawk.game.config.CrMissionRankNPCCfg;
import com.hawk.game.config.CrMissionRankRewardCfg;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.PlotBattle.CRRankPlayerInfo;
import com.hawk.game.protocol.PlotBattle.CRRankResp;
import com.hawk.game.protocol.PlotBattle.RankElement;
import com.hawk.game.service.mail.MailParames;
import com.hawk.gamelib.rank.RankScoreHelper;

import redis.clients.jedis.Tuple;

/***
 * 英雄试炼
 * @author yang.rao
 *
 */
public class PlotBattleService {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	private static PlotBattleService Instance = new PlotBattleService();
	
	/** tick周期 **/
	private static final long tickPeriod = HawkTime.MINUTE_MILLI_SECONDS;
	
	/** 英雄试炼排行榜过期时间 **/
	private static final int cr_guild_rank_expire = (int)(5 * (HawkTime.DAY_MILLI_SECONDS / 1000) );
	
	/** 下一次排行榜tick的时间  **/
	private long nextRankTickTime = 0l;
	
	private long sendRankMailRealTime = 0l; //发送邮件的真实时间
	
	private static final int RANK_SIZE = 20;
	
	/** 缓存英雄试炼盟友排行榜 **/
	private Map<String, CRRankResp.Builder> rankMap = new ConcurrentHashMap<>();
	
	private PlotBattleService(){
		nextRankTickTime = HawkTime.getNextAM0Date();
	}
	
	public static PlotBattleService getInstance(){
		return Instance;
	}
	
	public long getTickPeriod(){
		return tickPeriod;
	}
	
	public void onTick(){
		long curTime = HawkTime.getMillisecond();
		if(curTime >= nextRankTickTime){
			try {
				sendRankMail();
			} catch (Exception e) {
				HawkException.catchException(e);
			}finally {
				nextRankTickTime = HawkTime.getNextAM0Date();
				sendRankMailRealTime = curTime;
				//设置执行的真实时间
				LocalRedis.getInstance().setPlotBattleSendRankMailRealTime(sendRankMailRealTime);
			}
		}
		
		/** 存在这样一种情况，服务器维护时间跨天了，这个时候启动服务器，需要给昨天的排行榜发送邮件 **/
		if(sendRankMailRealTime == 0l){
			sendRankMailRealTime = LocalRedis.getInstance().getPlotBattleSendRankMailRealTime();
			if(sendRankMailRealTime == 0){ //今天第一次开这个需求，redis是没有数据的
				sendRankMailRealTime = curTime;
			}
			if(!HawkTime.isSameDay(curTime, sendRankMailRealTime)){
				try {
					sendRankMail();
				} catch (Exception e) {
					HawkException.catchException(e);
				}finally {
					sendRankMailRealTime = curTime;
				}
			}
		}
	}
	
	/***
	 * 发送试炼排行榜奖励邮件
	 */
	private void sendRankMail(){
		//所有的联盟
		List<String> guildList = GuildService.getInstance().getGuildIds();
		int delayTime = 0;
		for(String guildId : guildList){
			try {
				delayTime += 1000;
				addRankTask(new HawkDelayTask(delayTime, delayTime, 1) {
					@Override
					public Object run() {
						rankMap.remove(guildId); //清理缓存
						insertNPCScore2Redis(guildId);
						logger.info("CRRank sendmail guildId={}", guildId);
						sendGuildRankMail(guildId);
						return null;
					}
				});
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	private void sendGuildRankMail(String guildId){
		if(guildId == null){
			return;
		}
		//获取昨天的排行榜
		Set<Tuple> set = LocalRedis.getInstance().getCrRankInfo(guildId, RANK_SIZE -1  ,getBeforeDayStr());
		//记录排行榜
		int index = 0;
		for(Tuple t : set){
			index ++;
			String id = t.getElement();
			if(isNPC(id)){
				continue;
			}
			MailParames.Builder mail = MailParames.newBuilder()
					.setPlayerId(id)
					.setMailId(MailId.CR_MISSION_RANK_INFO)
					.setAwardStatus(MailRewardStatus.NOT_GET);
			int rank = index;
			String reward = getReward(rank);
			if(reward != null){
				//添加奖励
				AwardItems award = AwardItems.valueOf(reward);
				mail.addRewards(award.getAwardItems());
			}
			if(HawkOSOperator.isEmptyString(reward)){
				continue;
			}
			mail.addContents(rank);
			logger.info("send crrankmission mail, playerId:{}, rank:{}, reward:{}", id, rank, reward);
			MailService.getInstance().sendMail(mail.build());
			
		}
	}
	
	/***
	 * 获取奖励字符串
	 * @param rank 排名
	 * @return
	 */
	private String getReward(int rank){
		ConfigIterator<CrMissionRankRewardCfg> ite = HawkConfigManager.getInstance().getConfigIterator(CrMissionRankRewardCfg.class);
		while(ite.hasNext()){
			CrMissionRankRewardCfg cfg = ite.next();
			String [] src = cfg.getRank().split("_");
			if(src.length == 1){
				if(rank == Integer.valueOf(src[0])){
					return cfg.getReward();
				}
			}else if(src.length == 2){
				int min = Integer.valueOf(src[0]);
				int max = Integer.valueOf(src[1]);
				if(rank >= min && rank <= max){
					return cfg.getReward();
				}
			}
		}
		return null;
	}
	
	/****
	 * 构建英雄试炼排行榜
	 * @param guildId
	 * @return
	 */
	public CRRankResp.Builder getCrRankInfoFromCache(String guildId){
		CRRankResp.Builder build = rankMap.get(guildId);
		if(build != null){
			//在线玩家重新刷新头像和名字
			if(build.getRankInfoList() == null || build.getRankInfoList().size() == 0){
				insertNPCScore2Redis(guildId);
				logger.info("plotBattleService 执行插入NPC数据,guildId:{}", guildId);
				return buildCrRankInfoFromRedis(guildId);
			}
			rebuildCRRankResp(build);
			return build;
		}
		//从redis构建排行榜
		insertNPCScore2Redis(guildId); //如果没有数据，插入npc记录
		return buildCrRankInfoFromRedis(guildId);
	}
	
	public CRRankResp.Builder rebuildCRRankResp(CRRankResp.Builder build){
		for(CRRankPlayerInfo.Builder info : build.getRankInfoBuilderList()){
			RankElement ele = info.getElement();
			if(ele != null && ele ==RankElement.Player){
				String playerId = info.getPlayerId();
				Player player = GlobalData.getInstance().getActivePlayer(playerId);
				if(player != null){ //在线玩家实时更新头像和名字吧
					info.setPfIcon(player.getPfIcon());
					info.setPlayerName(player.getName());
					info.setAuthority(GuildService.getInstance().getPlayerGuildAuthority(playerId));
					GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
					if (null != member) {
						info.setOfficeId(member.getOfficeId());
					}
				}
			}
		}
		return build;
	}
	
	/***
	 * 从redis构建排行榜
	 * 此函数不涉及并发
	 * @param guildId
	 * @return
	 */
	public CRRankResp.Builder buildCrRankInfoFromRedis(String guildId){
		if(guildId == null){
			return null;
		}
		Set<Tuple> set = LocalRedis.getInstance().getCrRankInfo(guildId, RANK_SIZE - 1, getTodayStr());
		CRRankResp.Builder builder = buildList(set, guildId);
		logger.info("plotBattleService data insert cache,guildId:{}", guildId);
		rankMap.put(guildId, builder);
		return builder;
	}
	
	private void insertNPCScore2Redis(String guildId){
		ConfigIterator<CrMissionRankNPCCfg> ite = HawkConfigManager.getInstance().getConfigIterator(CrMissionRankNPCCfg.class);
		while(ite.hasNext()){
			CrMissionRankNPCCfg cfg = ite.next();
			String id = String.valueOf(cfg.getId());
			double score = cfg.getScore();
			long exchangeScore = RankScoreHelper.calcSpecialRankScore((long)score);
			logger.info("plotBattleService insert npc data,guildId:{}", guildId);
			LocalRedis.getInstance().updatePlayerPlotBattleScore(guildId, id, exchangeScore, getCrRankExpireTime());
		}
	}
	
	/***
	 * 通过排行list构建
	 * @param list
	 * @param guildId
	 * @return
	 */
	private CRRankResp.Builder buildList(Set<Tuple> set, String guildId){
		CRRankResp.Builder builder = CRRankResp.newBuilder();		
		for(Tuple ele : set){
			String id = ele.getElement();
			CRRankPlayerInfo.Builder build = CRRankPlayerInfo.newBuilder();
			if(!isNPC(id)){
				//判断玩家是否有联盟
				String playerId = id;
				if (GuildService.getInstance().isPlayerInGuild(guildId, playerId)){
					Player player = GlobalData.getInstance().makesurePlayer(playerId);
					if(player != null){
						build.setElement(RankElement.Player);
						build.setPlayerId(player.getId());
						build.setPlayerName(player.getName());
						build.setIcon(player.getIcon());
						build.setPfIcon(player.getPfIcon());
						build.setAuthority(GuildService.getInstance().getPlayerGuildAuthority(playerId));
						if(ele.getScore() >= RankScoreHelper.rankSpecialOffset){
							build.setScore(RankScoreHelper.getRealScore((long)ele.getScore()));
						}else{
							build.setScore((long)ele.getScore()); //这个if else为了兼容早上更新之前就有人打排行了
						}
						GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
						if (null != member) {
							build.setOfficeId(member.getOfficeId());
						}
						builder.addRankInfo(build);
					}
				}
			}else{
				String npcId = id; //npcid
				CrMissionRankNPCCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrMissionRankNPCCfg.class, Integer.parseInt(npcId));
				if(cfg == null){
					continue;
				}
				build.setElement(RankElement.NPC);
				build.setPlayerId(npcId);
				build.setPlayerName(cfg.getName());
				build.setPfIcon(cfg.getIcon());
				//build.setScore((long)ele.getScore());
				if(ele.getScore() >= RankScoreHelper.rankSpecialOffset){
					build.setScore(RankScoreHelper.getRealScore((long)ele.getScore()));
				}else{
					build.setScore((long)ele.getScore()); //做一下线上兼容
				}
				builder.addRankInfo(build);
			}
		}
		return builder;
	}
	
	private boolean isNPC(String id){
		try {
			int npcId = Integer.valueOf(id);
			CrMissionRankNPCCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrMissionRankNPCCfg.class, npcId);
			if(cfg != null){
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	/***
	 * 发送挑战成功邮件
	 * @param playerId
	 * @param challengeId 被挑战成功的玩家
	 */
	public void sendCrSusMail(String playerId, String challengeId){
		if(challengeId == null){
			return;
		}
		Player cplayer = GlobalData.getInstance().makesurePlayer(challengeId);
		if(cplayer == null){
			return;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player == null){
			return;
		}
		
		MailParames.Builder builder = MailParames.newBuilder()
				.setPlayerId(challengeId)
				.setMailId(MailId.CR_MISSION_CHALLENGE_SUS)
				.addContents(player.getName());
		//发送邮件
		MailService.getInstance().sendMail(builder.build());
	}
	
	public void playerLeaveGuild(String guildId, String playerId){
		logger.info("PlotBattleService player leave guild, playerId:{}, guildId:{}, guildName:{}", playerId, guildId, GuildService.getInstance().getGuildName(guildId));
		LocalRedis.getInstance().deletePlayerPlotBattleScoreLeaveGuild(guildId, playerId, cr_guild_rank_expire);
		//重构排行榜
		buildCrRankInfoFromRedis(guildId);
	}
	
	/***
	 * 获取昨天的时间字符串 yyyy-MM-dd
	 * @return
	 */
	public String getBeforeDayStr(){
		Calendar calendar = HawkTime.getCalendar(true);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		StringBuilder build = new StringBuilder();
		build.append(year).append("-")
		.append(month).append("-")
		.append(day);
		return build.toString();
	}
	
	/***
	 * 获取今天的日期字符串
	 * @return
	 */
	public String getTodayStr(){
		Calendar calendar = HawkTime.getCalendar(true);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		StringBuilder build = new StringBuilder();
		build.append(year).append("-")
		.append(month).append("-")
		.append(day);
		return build.toString();
	}
	
	public void addRankTask(HawkDelayTask task) {
		HawkThreadPool taskPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (null != taskPool) {
			task.setTypeName("CrMissionRank");
			taskPool.addTask(task, 0, false);
		}
	}
	/***
	 * 排行榜过期时间
	 * @return
	 */
	public int getCrRankExpireTime(){
		return cr_guild_rank_expire;
	}
}
