package com.hawk.game.module.lianmengyqzz.march.service.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZSeasonServer;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.protocol.Rank;
import com.hawk.game.util.LogUtil;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.profiler.HawkJVMMonitor;
import org.hawk.profiler.HawkSysProfiler;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.TeamStrengthWeightCfg;
import com.hawk.game.crossactivity.rank.MatchStrengthRank;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZWarConstCfg;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinGuild;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinServer;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZRecordData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityJoinState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.model.President;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

import redis.clients.jedis.Tuple;


public class YQZZState100StartShow  extends IYQZZServiceState {
	
	
	public YQZZState100StartShow(YQZZMatchService parent) {
		super(parent);
	}
	
	@Override
	public void init() {
		YQZZActivityStateData data = this.calcInfo();
		//清除一下数据
		this.getDataManager().clearData();
		//重置一下状态
		this.getDataManager().getStateData().setTermId(data.getTermId());
		this.getDataManager().getStateData().setState(YQZZActivityState.START_SHOW);
		//是否可以参加本次赛事
		boolean canJoin = this.getParent().canJoinActivity();
		YQZZActivityJoinState joinState = canJoin?YQZZActivityJoinState.JOIN:YQZZActivityJoinState.OUT;
		this.getDataManager().getStateData().setJoinGame(joinState);
		this.getDataManager().getStateData().setSaveServerInfo(0);
		this.getDataManager().getStateData().saveRedis();
		if(GsConfig.getInstance().isDebug()){
			gmAddServer();
		}
	}
	
	
	@Override
	public void tick() {
		YQZZActivityStateData data = this.calcInfo();
		//如果不在当前状态，则往下个状态推进
		YQZZActivityStateData curData = this.getDataManager().getStateData();
		if(curData.getTermId() != data.getTermId()
				|| curData.getState() != data.getState()){
			this.getParent().updateState(YQZZActivityState.MATCH);
			return;
		}
		//如果不参与战斗
		YQZZActivityJoinState joinState = this.getDataManager()
				.getStateData().getJoinGame();
		if(joinState == YQZZActivityJoinState.OUT){
			return;
		}
		//如果参战，则写入当前服参战信息,距离匹配阶段10分钟前
		long curTime = HawkTime.getMillisecond();
		YQZZTimeCfg cfg = this.getTimeCfg();
		long matchTime = cfg.getMatchTimeValue();
		if(curTime >(matchTime - HawkTime.MINUTE_MILLI_SECONDS * 20)){
			this.saveYQZZJoinServer();
		}
	}

	@Override
	public void gmOp() {
		gmAddServer();
		//如果不参与战斗
		YQZZActivityJoinState joinState = this.getDataManager()
				.getStateData().getJoinGame();
		if(joinState == YQZZActivityJoinState.OUT){
			return;
		}
		this.saveYQZZJoinServer();
	}

	public void gmAddServer(){
		boolean isSeasonFirst = false;
		int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
		YQZZTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		if(cfg.getSeason() > 0 && cfg.getType() == 2 && cfg.getTurn() == 1){
			isSeasonFirst = true;
		}
		// 读文件
		List<String> infos = new ArrayList<>();
		try {
			HawkOSOperator.readTextFileLines("tmp/yqzz_gm_server_info.txt", infos);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		for (String info : infos) {
			try {
				String serverId = info.split(",")[0];
				long power = Long.parseLong(info.split(",")[1]);
				YQZZJoinServer joinServer = new YQZZJoinServer();
				joinServer.setServerId(serverId);
				joinServer.setPower(power);
				joinServer.setTermId(termId);
				joinServer.setCpuUsage(10);
				joinServer.saveRedis();
				if(isSeasonFirst){
					YQZZSeasonServer seasonServer = new YQZZSeasonServer();
					seasonServer.setServerId(serverId);
					seasonServer.setSeason(cfg.getSeason());
					seasonServer.setPower(power);
					seasonServer.saveRedis();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	@SuppressWarnings("unused")
	private void sendStartEmail(){
		long currTime = HawkTime.getMillisecond();
		long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
				.setMailId(MailId.YQZZ_ACTIVITY_START)
				.build(), currTime, currTime + experiTime);
	}
	
	private void saveYQZZJoinServer(){
		if(this.getDataManager().getStateData()
				.getJoinGame() != YQZZActivityJoinState.JOIN){
			return;
		}
		if(this.getDataManager().getStateData().getSaveServerInfo() > 0){
			return;
		}
		List<RankInfo> rankList = RankService.getInstance().getRankCache(RankType.ALLIANCE_FIGHT_KEY);
		if(rankList.isEmpty()){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZWarConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		int joinGuildCount = cfg.getJoinGuildCount();
		joinGuildCount = Math.min(rankList.size(), joinGuildCount);
		//参赛联盟ID
		List<String> joinGuildIds = new ArrayList<>();
		Map<String, YQZZJoinGuild> joinGuilds = new HashMap<>();
		for (int i = 0; i < joinGuildCount; i++) {
			RankInfo rankInfo = rankList.get(i);
			String guildId = rankInfo.getId();
			GuildInfoObject obj = GuildService.getInstance().getGuildInfoObject(guildId);
			if(obj != null){
				long power = GuildService.getInstance().getGuildBattlePoint(guildId);
				YQZZJoinGuild joinGuild = new YQZZJoinGuild();
				joinGuild.setTermId(termId);
				joinGuild.setServerId(serverId);
				joinGuild.setGuildId(guildId);
				joinGuild.setGuildName(obj.getName());
				joinGuild.setGuildTag(obj.getTag());
				joinGuild.setGuildFlag(obj.getFlagId());
				joinGuild.setLeaderId(obj.getLeaderId());
				joinGuild.setLeaderName(obj.getLeaderName());
				joinGuild.setGuildRank(rankInfo.getRank());
				joinGuild.setPower(power);
				joinGuilds.put(joinGuild.getGuildId(), joinGuild);
				joinGuildIds.add(joinGuild.getGuildId());
			}
		}

		try {
			int logCount = Math.min(rankList.size(), 10);
			for (int i = 0; i < logCount; i++) {
				RankInfo rankInfo = rankList.get(i);
				String guildId = rankInfo.getId();
				GuildInfoObject obj = GuildService.getInstance().getGuildInfoObject(guildId);
				if (obj != null) {
					LogUtil.logYQZZMatchPower(termId, GsConfig.getInstance().getServerId(), obj.getId(), obj.getName(), i + 1, rankInfo.getRankInfoValue());
				}
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
		
		
		long power = this.getMatchPower(termId);
		int openDayW = this.getOpenDayW();
		// 司令
		President president = PresidentFightService.getInstance()
				.getPresidentCity().getPresident();
		
		YQZZJoinServer joinServer = new YQZZJoinServer();
		joinServer.setTermId(termId);
		joinServer.setServerId(serverId);
		joinServer.setPower(power);
		joinServer.setOpenDayW(openDayW);
		joinServer.setJoinGuilds(joinGuildIds);
		if(president != null && 
				!HawkOSOperator.isEmptyString(president.getPlayerId())){
			joinServer.setLeaderId(president.getPlayerId());
			joinServer.setLeaderName(president.getPlayerName());
			joinServer.setLeaderGuild(president.getPlayerGuildId());
			joinServer.setLeaderGuildName(president.getPlayerGuildName());
			joinServer.setLeaderGuildFlag(president.getPlayerGuildFlag());
			joinServer.setLeaderGuildTag(president.getPlayerGuildTag());
		}else {
			GuildInfoObject guildInfoObject = getFirstGuildInfoObject();
			if(guildInfoObject != null){
				joinServer.setLeaderId(guildInfoObject.getLeaderId());
				joinServer.setLeaderName(guildInfoObject.getLeaderName());
				joinServer.setLeaderGuild(guildInfoObject.getId());
				joinServer.setLeaderGuildName(guildInfoObject.getName());
				joinServer.setLeaderGuildFlag(guildInfoObject.getFlagId());
				joinServer.setLeaderGuildTag(guildInfoObject.getTag());
			}
		}
		try {
			joinServer.setUsedMem(HawkJVMMonitor.getInstance().getUsedMemoryMB());
			joinServer.setTotalMem(HawkJVMMonitor.getInstance().getTotalMemoryMB());
			joinServer.setCpuUsage(HawkJVMMonitor.getInstance().getCpuUsage());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		joinServer.saveRedis();
		YQZZJoinGuild.saveAllData(termId, joinGuilds);
		this.getDataManager().getStateData().setSaveServerInfo(1);
		this.getDataManager().getStateData().saveRedis();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		if(season > 0){
			YQZZSeasonServer seasonServer = YQZZSeasonServer.loadByServerId(season, serverId);
			if(seasonServer != null){
				long guildTotalPower = 0;
				for (int i = 0; i < joinGuildCount; i++) {
					RankInfo rankInfo = rankList.get(i);
					guildTotalPower += rankInfo.getRankInfoValue();
				}
				seasonServer.setPower(guildTotalPower);
				seasonServer.saveRedis();
			}
		}

	}


	private GuildInfoObject getFirstGuildInfoObject(){
		List<Rank.RankInfo> rankList = RankService.getInstance().getRankCache(Rank.RankType.ALLIANCE_FIGHT_KEY);
		if(rankList.isEmpty()){
			return null;
		}
		Rank.RankInfo rankInfo = rankList.get(0);
		String guildId = rankInfo.getId();
		GuildInfoObject obj = GuildService.getInstance().getGuildInfoObject(guildId);
		if(obj == null){
			return null;
		}
		return obj;
	}

	public int getOpenDayW(){
		try {
			YQZZWarConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
			return cfg.serverMatchOpenDayWeight();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	public long getMatchPower(int termId){
		try {
			YQZZWarConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
			//获取列表
			int count = cfg.getMoonMatchNumLimit() -1;
			count = Math.max(count, 0);
			Set<Tuple> rankList = MatchStrengthRank.getInstance().getStrengthList(count);
			//列表为空则不走写入逻辑.
			if (rankList == null || rankList.size() <= 0) {
				return 0;
			}
			double memberPower = 0;
			int rank = 0;
			for(Tuple info : rankList){
				rank ++;
				String playerId = info.getElement();
				long power = (long) info.getScore();
				double powerWeight = this.getPowerWeight(rank);
				double addPower =  (power * powerWeight);
				memberPower += addPower;
				//日志
				//LogUtil.logCrossActivityPlayerStrength(termId, playerId, rank, power, powerWeight, addPower);
				HawkLog.logPrintln("YQZZActivityService match power,termId:{},playerId:{},rank:{},power:{},powerWeight:{},memberPower:{},",
						termId,playerId,rank,power,powerWeight, addPower);
			}
			
			double teamParam = this.getTeamMatchParam(termId);
			double openServerParam = cfg.getMatchPowerParam();
			long matchPower = (long) (teamParam * memberPower * openServerParam);
			HawkLog.logPrintln("YQZZActivityService match power,termId:{},memberPower:{},teamParam:{},openServerParam:{},matchPower:{}", termId,memberPower,teamParam,openServerParam,matchPower);
			return matchPower;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	
	/**
	 * 队伍磨合参数
	 * @param teamId
	 * @return
	 */
	private double getTeamMatchParam(int termId){
		YQZZWarConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		int count = cfg.getMoonMatchTimesLimit() -1;
		count = Math.max(count, 0);
		List<Integer> terms = new ArrayList<>();
		for(int i=1;i<=count;i++){
			int termTemp = termId -i;
			if(termTemp >= 1){
				terms.add(termTemp);
			}
		}
		
		String serverId = GsConfig.getInstance().getServerId();
		Map<Integer,YQZZRecordData> logList = YQZZRecordData.loadAll(serverId, terms);
		double param = 0;
		for(YQZZRecordData record : logList.values()){
			//int historyTerm = record.getTermId();
			int rank = record.getRank();
			double rankParam = cfg.getMoonMatchBattleResultValue(rank);
			param += rankParam;
			//日志
			//LogUtil.logCrossActivityTeamParam(termId, historyTerm, rank, rankParam);
			HawkLog.logPrintln("YQZZActivityService match power, getTeamMatchParam,termId:{},rank:{},param:{}",
					record.getTermId(),rank,rankParam);
		}
		param = Math.min(param, cfg.getMoonMatchCofMaxValue());
		param = Math.max(param, cfg.getMoonMatchCofMinValue());
		HawkLog.logPrintln("YQZZActivityService match power, getTeamMatchParam, result,termId:{},param:{}",termId,param);
		return param + 1;
	}
	
	/**
	 * 战力排名权重
	 * @param rank
	 * @return
	 */
	private double getPowerWeight(int rank){
		List<TeamStrengthWeightCfg> cfgList = AssembleDataManager.getInstance().getTeamStrengthWeightCfgList(40);
		for(TeamStrengthWeightCfg cfg : cfgList){
			if(cfg.getRankUpper()<= rank && rank <= cfg.getRankLower()){
				return cfg.getWeightValue();
			}
		}
		return 0;
	}
		
	
}
