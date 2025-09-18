package com.hawk.game.crossactivity.season;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossSeasonRankRewardCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonScoreRank;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonScoreRankResp;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

public class CrossSeasonScoreRank {

	private int season;
	//排行榜缓存
	private List<CrossActivitySeasonScoreRank.Builder> ranks = new ArrayList<>();
	
	
	/**
	 * 初始化
	 * @param season
	 */
	public void initSeason(int season){
		this.season = season;
		this.ranks =  new ArrayList<>();
	}
	
	/**
	 * 轮询
	 */
	public void onTick(){
		if(this.season <= 0){
			return;
		}
		this.doRank();
	}
	
	
	/**
	 * 排行榜数据
	 */
	public void doRank(){
		List<CrossActivitySeasonScoreRank.Builder> finalRank = this.getFinalRank();
		if(Objects.nonNull(finalRank)){
			this.ranks = finalRank;
			return;
		}
		
		List<CrossActivitySeasonScoreRank.Builder> tempRank = this.sortRank();
		if(Objects.nonNull(tempRank)){
			this.ranks = tempRank;
			return;
		}
	}
	
	
	/**
	 * 现有数据排行
	 * @return
	 */
	public List<CrossActivitySeasonScoreRank.Builder> sortRank(){
		List<CrossSeasonServerData> slist = this.sortScoreData();
		if(Objects.isNull(slist)){
			return null;
		}
		if(slist.isEmpty()){
			return null;
		}
		List<CrossActivitySeasonScoreRank.Builder> ranks = new ArrayList<>();
		int rank=1;
		int showRank = 1;
		int scoreTemp = 0;
		for(CrossSeasonServerData data : slist){
			if(!GlobalData.getInstance().isMainServer(data.getServerId())){
				continue;
			}
			CrossActivitySeasonScoreRank.Builder builder = CrossActivitySeasonScoreRank.newBuilder();
			builder.setServerId(data.getServerId());
			builder.setScore(data.getScore());
			if(data.getScore() == scoreTemp){
				builder.setRank(showRank);
			}else{
				builder.setRank(rank);
				showRank = rank;
				scoreTemp = data.getScore();
			}
			ranks.add(builder);
			rank ++;
		}
		return ranks;
	}
	

	
	/**
	 * 最终数据排行
	 * @return
	 */
	public List<CrossActivitySeasonScoreRank.Builder> getFinalRank(){
		List<CrossSeasonServerData> dlist = this.getFinalScoreData(); 
		if(Objects.isNull(dlist)){
			return null;
		}
		if(dlist.isEmpty()){
			return null;
		}
		List<CrossActivitySeasonScoreRank.Builder> ranks = new ArrayList<>();
		int index = dlist.size() -1;
		for(int i=index;i >=0; i--){
			CrossSeasonServerData data = dlist.get(index);
			CrossActivitySeasonScoreRank.Builder builder = CrossActivitySeasonScoreRank.newBuilder();
			builder.setServerId(data.getServerId());
			builder.setScore(data.getScore());
			builder.setRank(data.getAwardRank());
			ranks.add(builder);
		}
		return ranks;
	}
	
	
	/**
	 * 获取最终排行记录
	 * @return
	 */
	public List<CrossSeasonServerData> getFinalScoreData(){
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_FINAL_RANK , season);
		List<String> dlist = RedisProxy.getInstance().getRedisSession().lRange(key, 0, -1, 0);
		if(Objects.isNull(dlist)){
			return null;
		}
		if(dlist.isEmpty()){
			return null;
		}
		List<CrossSeasonServerData> list = new ArrayList<>();
		for(String str : dlist){
			CrossSeasonServerData data = new CrossSeasonServerData();
			data.unSerialize(str);
			list.add(data);
		}
		return list;
	}
	
	
	/**
	 * 现有数据排序
	 * @return
	 */
	public List<CrossSeasonServerData> sortScoreData(){
		Map<String,CrossSeasonServerData> seasonMap = CrossSeasonServerData.loadAllData(this.season);
		if(seasonMap.isEmpty()){
			return null;
		}
		List<CrossSeasonServerData> slist = new ArrayList<>();
		slist.addAll(seasonMap.values());
		Collections.sort(slist,new Comparator<CrossSeasonServerData>() {
			@Override
			public int compare(CrossSeasonServerData o1, CrossSeasonServerData o2) {
				if(o1.getScore() != o2.getScore()){
					return o1.getScore() - o2.getScore() > 0 ? -1 : 1;
				}else {
					return o1.getServerId().compareTo(o2.getServerId());
				}
			}
		});
		return slist;
	}
	
	
	
	
	/**
	 * 确定最终排行榜
	 */
	public void makeSureFinalRank(){
		List<CrossSeasonServerData> slist = this.sortScoreData();
		if(Objects.isNull(slist)){
			return;
		}
		if(slist.isEmpty()){
			return;
		}
		int rank=1;
		int showRank = 1;
		int scoreTemp = 0;
		List<String> dataList = new ArrayList<>();
		for(CrossSeasonServerData data : slist){
			if(!GlobalData.getInstance().isMainServer(data.getServerId())){
				continue;
			}
			if(data.getScore() == scoreTemp){
				data.setAwardRank(showRank);
			}else{
				data.setAwardRank(rank);
				showRank = rank;
				scoreTemp = data.getScore();
			}
			rank ++;
			dataList.add(data.serialize());
			//日志
			CrossSeasonLog.logCrossSeasonFinalRank(data.getServerId(), data.getScore(), data.getAwardRank(), data.getSeason());
		}
		//保存数据
		CrossSeasonServerData.saveAllData(slist, this.season);
		//定榜
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_FINAL_RANK , season);
		RedisProxy.getInstance().getRedisSession().lPush(key, 0, dataList.toArray(new String[0]));
	}
	
	
	/**
	 * 发放最终奖励
	 */
	public void sendFinalRankReward(){
		HawkLog.logPrintln("CrossActivitySeasonService-sendFinalRankReward-start:{},{}",
				this.season,GsConfig.getInstance().getServerId());
		List<CrossSeasonServerData> list = this.getFinalScoreData();
		if(Objects.isNull(list)){
			return;
		}
		if(list.isEmpty()){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		for(CrossSeasonServerData data : list){
			if(!serverId.equals(data.getServerId())){
				continue;
			}
			boolean hasSend = this.hasFinalRewardSendRecord();
			if(hasSend){
				return;
			}
			int score = data.getScore();
			int awardRank = data.getAwardRank();
			//添加记录
			this.addFinalRewardSendRecord(String.valueOf(awardRank));
			//发放全服邮件
			CrossSeasonRankRewardCfg cfg = this.getCrossSeasonRankRewardCfg(awardRank);
			if(Objects.nonNull(cfg)){
				long curTime = HawkTime.getMillisecond();
				long experiTime = curTime + HawkTime.DAY_MILLI_SECONDS * 15;
				SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
						.setMailId(MailConst.MailId.CROSS_ACTIVITY_SEASON_FINAL_RANK_REWARD)
						.addContents(score,awardRank)
						.setRewards(cfg.getRewardList())
						.setAwardStatus(Const.MailRewardStatus.NOT_GET)
						.build(), curTime, experiTime);
				//日志
				CrossSeasonLog.logCrossSeasonFinalReward(serverId, data.getScore(), data.getAwardRank(), cfg.getId(), data.getSeason());
				HawkLog.logPrintln("CrossActivitySeasonService-sendFinalRankReward-send:{},{},{}",
						this.season,awardRank,GsConfig.getInstance().getServerId());
			}
		}
		HawkLog.logPrintln("CrossActivitySeasonService-sendFinalRankReward-end:{},{}",
				this.season,GsConfig.getInstance().getServerId());
	}
	
	
	public CrossSeasonRankRewardCfg getCrossSeasonRankRewardCfg(int rank){
		ConfigIterator<CrossSeasonRankRewardCfg> ite = HawkConfigManager.getInstance().getConfigIterator(CrossSeasonRankRewardCfg.class);
		for(CrossSeasonRankRewardCfg cfg : ite){
			if(cfg.getRankUpper() <= rank && rank <= cfg.getRankLower()){
				return cfg;
			}
		}
		return null;
	}
	
	
	public boolean hasFinalRewardSendRecord(){
		String serverId = GsConfig.getInstance().getServerId();
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_FINAL_REWARD , season);
		String rlt = RedisProxy.getInstance().getRedisSession().hGet(key, serverId);
		if(HawkOSOperator.isEmptyString(rlt)){
			return false;
		}
		return true;
	}
	
	public void addFinalRewardSendRecord(String record){
		String serverId = GsConfig.getInstance().getServerId();
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_FINAL_REWARD , season);
		RedisProxy.getInstance().getRedisSession().hSet(key, serverId, record);
	}
	
	
	public CrossActivitySeasonScoreRankResp.Builder buildRank(String serverId){
		CrossActivitySeasonScoreRankResp.Builder builder = CrossActivitySeasonScoreRankResp.newBuilder();
		CrossActivitySeasonScoreRank.Builder self = null;
		for(CrossActivitySeasonScoreRank.Builder rank : ranks){
			builder.addRanks(rank.clone());
			if(rank.getServerId().equals(serverId)){
				self = rank.clone();
			}
		}
		if(Objects.nonNull(self)){
			builder.setSelfRank(self);
		}else{
			self = CrossActivitySeasonScoreRank.newBuilder();
			self.setRank(0);
			self.setServerId(serverId);
			self.setScore(0);
			builder.setSelfRank(self);
		}
		return builder;
	}
}
