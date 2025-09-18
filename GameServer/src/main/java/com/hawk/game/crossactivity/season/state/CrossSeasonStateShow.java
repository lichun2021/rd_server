package com.hawk.game.crossactivity.season.state;

import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.CrossSeasonTimeCfg;
import com.hawk.game.config.CrossTimeCfg;
import com.hawk.game.crossactivity.season.CrossActivitySeasonService;
import com.hawk.game.crossactivity.season.CrossSeasonLog;
import com.hawk.game.crossactivity.season.CrossSeasonServerData;
import com.hawk.game.crossactivity.season.CrossSeasonStateEnum;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonState;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

public class CrossSeasonStateShow extends ICrossSeasonState{

	@Override
	public void start() {
		//初始化赛季积分数据
		this.initSeasonScoreData();
	}

	@Override
	public void update() {
		long curTime = HawkTime.getMillisecond();
		CrossSeasonTimeCfg timeCfg = this.getTimeCfg();
		if(curTime > timeCfg.getStartTimeValue()){
			//改变状态
			this.getStateData().changeState(CrossSeasonStateEnum.OPEN);
			return;
		}
	}

	@Override
	public CrossSeasonStateEnum getStateEnum() {
		return CrossSeasonStateEnum.SHOW;
	}

	@Override
	public CrossActivitySeasonState getCrossActivitySeasonState() {
		return CrossActivitySeasonState.C_SEASON_SHOW;
	}
	
	/**
	 * 初始化积分数据
	 */
	public void initSeasonScoreData(){
		String serverId = GsConfig.getInstance().getServerId();
		long curTime = HawkTime.getMillisecond();
		Long mergeServerTime = AssembleDataManager.getInstance().getServerMergeTime(serverId);
		long mergeServerTimeValue = Objects.isNull(mergeServerTime)?0:mergeServerTime.longValue();
		int initScore = this.getInitScore();
		CrossSeasonServerData data = new CrossSeasonServerData();
		int season = this.getStateData().getSeason();
		data.setSeason(season);
		data.setServerId(serverId);
		data.setScore(initScore);
		data.setInitTime(curTime);
		data.setMergeTime(mergeServerTimeValue);
		data.saveData();
		//排行榜数据也重置一下
		CrossActivitySeasonService.getInstance().getRank().initSeason(season);
		//发邮件
		if(initScore == CrossConstCfg.getInstance().getSeasonInitScore()){
			long currTime = HawkTime.getMillisecond();
			long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 5;
			SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
					.setMailId(MailConst.MailId.CROSS_ACTIVITY_SEASON_OPEN_MAIL)
					.build(), currTime, currTime + experiTime);
		}
		//积分日志
		CrossSeasonLog.logCrossSeasonScore(serverId, "", 0, initScore, 0, initScore, season, 0, 0, 1);
		HawkLog.logPrintln("CrossActivitySeasonService-initSeasonScoreData:{},{}",season,data.getScore());
	}
	
	/**
	 * 计算初始化积分
	 * @return
	 */
	public int getInitScore(){
		long curTime = HawkTime.getMillisecond();
		CrossConstCfg constCfg = CrossConstCfg.getInstance();
		CrossSeasonTimeCfg timeCfg = this.getTimeCfg();
		int starTerm = timeCfg.getStartTerm();
				
		//取可以开启的最近一期
		int crossTerm = 0;
		ConfigIterator<CrossTimeCfg> crossTimeIte = HawkConfigManager.getInstance().getConfigIterator(CrossTimeCfg.class);
		for(CrossTimeCfg cfg : crossTimeIte){
			if(cfg.getTermId() < starTerm){
				continue;
			}
			//这一期的匹配时间,如果当前时间已经大于匹配时间了  就不取了
			long matchTime = cfg.getShowTimeValue() - constCfg.getMatchTime();
			if(curTime > matchTime){
				continue;
			}
			if(crossTerm == 0 || cfg.getTermId() < crossTerm){
				crossTerm = cfg.getTermId();
			}
		}
		//初始积分 - 相差期数*衰减量
		int score = constCfg.getSeasonInitScore() - (crossTerm - starTerm) * constCfg.getSeasonInitScoreDecay();
		score = Math.max(score, 0);
		score = Math.min(score, constCfg.getSeasonInitScore());
		return score;
	}
}
