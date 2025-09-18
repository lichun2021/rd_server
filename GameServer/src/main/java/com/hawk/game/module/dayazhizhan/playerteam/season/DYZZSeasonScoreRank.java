package com.hawk.game.module.dayazhizhan.playerteam.season;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple4;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonScoreRank;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonScoreRankMember;

import redis.clients.jedis.Tuple;

public class DYZZSeasonScoreRank  implements SerializJsonStrAble{
	
	/**
	 * 期数 
	 */
	private int termId;
	
	/**
	 * 排行榜成员 周更
	 */
	private List<DYZZSeasonScoreRankMember> rankMembers;

	/**
	 * 设定榜时间
	 */
	private long rankSaveCheckTime;
	
	/**
	 * 重载榜时间
	 */
	private long rankLoadCheckTime;
	
	
	/**
	 * 构造
	 */
	public DYZZSeasonScoreRank(int termId) {
		this.termId = termId;
		this.rankMembers = new ArrayList<>();
	}
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("termId", termId);
		
		JSONArray rankMemberArr = new JSONArray();
		for(DYZZSeasonScoreRankMember member : this.rankMembers){
			rankMemberArr.add(member.serializ());
		}
		obj.put("rankMembers", rankMemberArr.toJSONString());
		return obj.toJSONString();
	}


	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSON.parseObject(serialiedStr);
		this.termId = obj.getIntValue("termId");
		String rankMembersStr = obj.getString("rankMembers");
		JSONArray rankMemberArr = JSONArray.parseArray(rankMembersStr);
		for(int i=0;i<rankMemberArr.size();i++){
			String memberStr = rankMemberArr.getString(i);
			DYZZSeasonScoreRankMember member = new DYZZSeasonScoreRankMember();
			member.mergeFrom(memberStr);
			this.rankMembers.add(member);
		}
	}

	/**
	 * 检查更新
	 */
	public void checkUpdate(){
		if(this.termId == 0){
			return;
		}
		this.checkRankSave();
		this.checkRankLoad();
	}
	
	/**
	 * 刷新排名到redis
	 */
	private void checkRankSave(){
		long curTime = HawkTime.getMillisecond();
		if(curTime == 0){
			this.rankSaveCheckTime = curTime;
			return;
		}
		DYZZSeasonCfg seasonCfg = HawkConfigManager.getInstance().getKVInstance(DYZZSeasonCfg.class);
		int timeInterval = seasonCfg.getScoreRankRefresh();
		if(curTime - this.rankSaveCheckTime < timeInterval * 1000){
			return;
		}
		
		this.rankSaveCheckTime = curTime;
		String serverId = GsConfig.getInstance().getServerId();
		DYZZSeasonRedisData.getInstance()
				.achiveDYZZSeasonScoreRankRefreshLock(termId, serverId, timeInterval * 2);
		String lockServer = DYZZSeasonRedisData.getInstance().getDYZZSeasonScoreRankRefreshLock(termId);
		HawkLog.logPrintln("DYZZSeasonService scoreRank refreshServer,serverId:{},termId:{}",lockServer,termId);
		if(!serverId.equals(lockServer)){
			return; 
		}
		//搞到最新的排行数据
		List<DYZZSeasonScoreRankMember> members = this.loadRankMembersData();
		DYZZSeasonScoreRank newData = new DYZZSeasonScoreRank(this.termId);
		newData.setRankMembers(members);
		//更新到redis
		DYZZSeasonRedisData.getInstance().updateDYZZSeasonScoreRankShowData(newData);
		HawkLog.logPrintln("DYZZSeasonService scoreRank refreshServer,serverId:{},termId:{},rankData:{}"
				,lockServer,termId,newData.serializ());
		
	}
	
	
	/**
	 * 刷新榜
	 */
	private void checkRankLoad(){
		long curTime = HawkTime.getMillisecond();
		if(curTime == 0){
			this.rankLoadCheckTime = curTime;
			return;
		}
		if(curTime - this.rankLoadCheckTime < HawkTime.MINUTE_MILLI_SECONDS){
			return;
		}
		this.rankLoadCheckTime = curTime;
		DYZZSeasonScoreRank newData = DYZZSeasonRedisData.getInstance()
				.getDYZZSeasonScoreRankShowData(termId);
		this.reloadRankData(newData);
		
	}
	
	
	
	
	/**
	 * 重置榜内数据
	 * @param newData
	 */
	private void reloadRankData(DYZZSeasonScoreRank newData){
		this.termId = newData.getTermId();
		this.rankMembers = newData.getRankMembers();
	}
	
	
	
	/**
	 * 加载成员
	 * @return
	 */
	private List<DYZZSeasonScoreRankMember> loadRankMembersData(){
		DYZZSeasonCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZSeasonCfg.class);
		List<DYZZSeasonScoreRankMember> list = new ArrayList<>();
		int size = cfg.getScoreRankSize() -1;
		size = Math.max(size, 0);
		Set<Tuple> set = DYZZSeasonRedisData.getInstance().getDYZZSeasonScoreRank(this.termId, size);
		int rank = 0;
		for(Tuple tuple : set){
			String playerString = tuple.getElement();
			int score = (int) tuple.getScore();
			HawkTuple4<String, String, String, String> tupe = this.parseRankPlayerString(playerString);
			AccountRoleInfo roleInfo = RedisProxy.getInstance().getAccountRole(tupe.first, tupe.second, tupe.third);
			if(roleInfo == null ){
				continue;
			}
			if(!roleInfo.getPlayerId().equals(tupe.fourth)){
				continue;
			}
			rank ++;
			DYZZSeasonScoreRankMember member = new DYZZSeasonScoreRankMember();
			member.setPlayerId(roleInfo.getPlayerId());
			member.setServerId(roleInfo.getServerId());
			member.setPlayerName(roleInfo.getPlayerName());
			member.setIcon(roleInfo.getIcon());
			member.setPficon(roleInfo.getPfIcon());
			member.setRank(rank);
			member.setScore(score);
			list.add(member);
		}
		return list;
	}

	/**
	 * 把玩家数据放进榜
	 * @param score
	 * @param serverId
	 * @param platForm
	 * @param openId
	 * @param playerId
	 */
	public void updatePlayerScore(int score,String serverId,String platForm,String openId,String playerId){
		double rankScore = HawkTime.getMillisecond() * 0.0000000000001;
		rankScore = 1d - rankScore;
		rankScore += score;
		// 同分数, 先达到的一方在前
		DYZZSeasonRedisData.getInstance().updateDYZZSeasonScoreRank(termId, rankScore,
				this.getRankPlayerString(serverId, platForm, openId,playerId));
	}

	/**
	 * 序列化玩家榜ID数据
	 * @param serverId
	 * @param platForm
	 * @param openId
	 * @param playerId
	 * @return
	 */
	public String getRankPlayerString(String serverId,String platForm,String openId,String playerId){
		JSONObject obj = new JSONObject();
		obj.put("serverId", serverId);
		obj.put("platForm", platForm);
		obj.put("openId", openId);
		obj.put("playerId", playerId);
		return obj.toString();
	}
	
	/**
	 * 反序列化玩家榜ID数据
	 * @param str
	 * @return
	 */
	public HawkTuple4<String, String, String, String> parseRankPlayerString(String str){
		JSONObject obj = JSON.parseObject(str);
		String serverId = obj.getString("serverId");
		String platForm = obj.getString("platForm");
		String openId = obj.getString("openId");
		String playerId = obj.getString("playerId");
		return new HawkTuple4<String, String, String,String>(serverId, platForm, openId,playerId);
	}
	
	
	/**
	 * 重置期数
	 * @param termId
	 */
	public void resetTerm(int termId){
		this.termId = termId;
		this.rankMembers = new ArrayList<>();
		HawkLog.logPrintln("DYZZSeasonService scoreRank resetTerm,termId:{}",termId);
	}
	
	
	



	public int getTermId() {
		return termId;
	}



	public void setTermId(int termId) {
		this.termId = termId;
	}


	/**
	 * 获取排行榜成员
	 * @return
	 */
	public List<DYZZSeasonScoreRankMember> getRankMembers() {
		return rankMembers;
	}


	/**
	 * 设置排行榜数据
	 * @param rankMembers
	 */
	private void setRankMembers(List<DYZZSeasonScoreRankMember> rankMembers) {
		this.rankMembers = rankMembers;
	}
	
	
	
	/**
	 * 获取名人堂
	 * @return
	 */
	public List<DYZZSeasonScoreRankMember> getFameHall() {
		List<DYZZSeasonScoreRankMember> members = new ArrayList<>();
		DYZZSeasonCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZSeasonCfg.class);
		int size = cfg.getFameHallSize();
		for(int i=0;i<size;i++){
			if(this.rankMembers.size() > i){
				members.add(rankMembers.get(i));
			}
		}
		return members;
	}
	
	
	/**
	 * 构建PB数据
	 * @return
	 */
	public PBDYZZSeasonScoreRank.Builder createPBDYZZSeasonScoreRank(){
		PBDYZZSeasonScoreRank.Builder builder = PBDYZZSeasonScoreRank.newBuilder();
		this.rankMembers.forEach(member->builder.addMembers(member.createPBDYZZSeasonScoreRankMember()));
		return builder;
	}
	
	
	public PBDYZZSeasonScoreRankMember.Builder createPlayerScoreMember(Player player,int socre){
		int rank  = this.getRank(player.getId());
		PBDYZZSeasonScoreRankMember.Builder builder = PBDYZZSeasonScoreRankMember.newBuilder();
		builder.setPlayerId(player.getId());
		builder.setServerId(player.getServerId());
		builder.setPlayerName(player.getName());
		builder.setIcon(player.getIcon());
		if(!HawkOSOperator.isEmptyString(player.getPfIcon())){
			builder.setPfIcon(player.getPfIcon());
		}
		builder.setRank(rank);
		builder.setScore(socre);
		return builder;
	}
	
	private int getRank(String playerId){
		for(DYZZSeasonScoreRankMember member : this.rankMembers){
			if(member.getPlayerId().equals(playerId)){
				return member.getRank();
			}
		}
		return 0;
	}
}
