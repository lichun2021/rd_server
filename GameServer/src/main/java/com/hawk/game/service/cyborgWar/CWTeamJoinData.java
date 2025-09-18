package com.hawk.game.service.cyborgWar;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.annotation.JSONField;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.CyborgWar.CWTeamInfo;
import com.hawk.game.protocol.Mail.PBCyborgContributePlayer;
import com.hawk.game.protocol.Mail.PBCyborgContributionMail;

/**
 * 战队出战信息
 * 
 * @author Jesse
 *
 */
public class CWTeamJoinData implements Comparable<CWTeamJoinData> {
	public String id;

	public String guildId;

	public String serverId;

	public int serverOpenDays;
	
	public String name;

	public String tag;

	public int flag;

	public String roomId;

	public int memberCnt;

	public long totalPower;

	public long matchPower;

	public long score;

	/** 积分占比奖励*/
	public int scorePer;
	public int cyborgItemTotal;
	
	/** 击杀最多*/
	public String killMaxPlayer;
	public String killMaxPlayerInfo;
	
	/** 承受伤害最多*/
	public String damagedMaxPlayer;
	public String damagedMaxPlayerInfo;
	
	/** 打怪最多*/
	public String monsterMaxPlayer;
	public String monsterMaxPlayerInfo;
	
	
	/** 报名时段位 */
	public int starBef;

	/** 比赛结束时段位 */
	public int starAft;

	/** 报名场次角标 */
	public int timeIndex;

	/** 是否匹配失败 */
	public boolean matchFailed;

	/** 本组战队列表 */
	public List<String> roomTeams;

	/** 比赛排行 */
	public int rank;

	/** 是否已发奖 */
	public boolean isAwarded;

	/** 是否正常完成结算 */
	public boolean isComplete;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public int getMemberCnt() {
		return memberCnt;
	}

	public void setMemberCnt(int memberCnt) {
		this.memberCnt = memberCnt;
	}

	public long getTotalPower() {
		return totalPower;
	}

	public void setTotalPower(long totalPower) {
		this.totalPower = totalPower;
	}

	public long getMatchPower() {
		return matchPower;
	}

	public void setMatchPower(long matchPower) {
		this.matchPower = matchPower;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}
	
	
	

	public int getScorePer() {
		return scorePer;
	}

	public void setScorePer(int scorePer) {
		this.scorePer = scorePer;
	}


	public int getCyborgItemTotal() {
		return cyborgItemTotal;
	}

	public void setCyborgItemTotal(int cyborgItemTotal) {
		this.cyborgItemTotal = cyborgItemTotal;
	}

	public String getKillMaxPlayer() {
		return killMaxPlayer;
	}

	public void setKillMaxPlayer(String killMaxPlayer) {
		this.killMaxPlayer = killMaxPlayer;
	}

	public String getKillMaxPlayerInfo() {
		return killMaxPlayerInfo;
	}

	public void setKillMaxPlayerInfo(String killMaxPlayerInfo) {
		this.killMaxPlayerInfo = killMaxPlayerInfo;
	}

	public String getDamagedMaxPlayer() {
		return damagedMaxPlayer;
	}

	public void setDamagedMaxPlayer(String damagedMaxPlayer) {
		this.damagedMaxPlayer = damagedMaxPlayer;
	}

	public String getDamagedMaxPlayerInfo() {
		return damagedMaxPlayerInfo;
	}

	public void setDamagedMaxPlayerInfo(String damagedMaxPlayerInfo) {
		this.damagedMaxPlayerInfo = damagedMaxPlayerInfo;
	}

	public String getMonsterMaxPlayer() {
		return monsterMaxPlayer;
	}

	public void setMonsterMaxPlayer(String monsterMaxPlayer) {
		this.monsterMaxPlayer = monsterMaxPlayer;
	}

	public String getMonsterMaxPlayerInfo() {
		return monsterMaxPlayerInfo;
	}

	public void setMonsterMaxPlayerInfo(String monsterMaxPlayerInfo) {
		this.monsterMaxPlayerInfo = monsterMaxPlayerInfo;
	}

	public int getTimeIndex() {
		return timeIndex;
	}

	public void setTimeIndex(int timeIndex) {
		this.timeIndex = timeIndex;
	}

	public boolean isMatchFailed() {
		return matchFailed;
	}

	public void setMatchFailed(boolean matchFailed) {
		this.matchFailed = matchFailed;
	}

	public List<String> getRoomTeams() {
		return roomTeams;
	}

	public void setRoomTeams(List<String> roomTeams) {
		this.roomTeams = roomTeams;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public boolean isAwarded() {
		return isAwarded;
	}

	public void setAwarded(boolean isAwarded) {
		this.isAwarded = isAwarded;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	@Override
	public int compareTo(CWTeamJoinData arg0) {
		long gap = this.getTotalPower() - arg0.getTotalPower();
		if (gap > 0) {
			return -1;
		} else if (gap < 0) {
			return 1;
		}
		gap = this.memberCnt - arg0.memberCnt;
		if (gap > 0) {
			return -1;
		} else if (gap < 0) {
			return 1;
		}
		return this.id.compareTo(arg0.id);
	}

	public int getStarBef() {
		return starBef;
	}

	public void setStarBef(int starBef) {
		this.starBef = starBef;
	}

	public int getStarAft() {
		return starAft;
	}

	public void setStarAft(int starAft) {
		this.starAft = starAft;
	}
	
	public int getServerOpenDays() {
		return serverOpenDays;
	}
	
	public void setServerOpenDays(int serverOpenDays) {
		this.serverOpenDays = serverOpenDays;
	}
	
	
	@JSONField(serialize = false)
	public List<ItemInfo> getScorePerRewards(){
		List<ItemInfo> list = new ArrayList<>();
		if(this.cyborgItemTotal > 0){
			ItemInfo item = new ItemInfo(ItemType.PLAYER_ATTR_VALUE, 
					PlayerAttr.CYBORG_SCORE_VALUE, this.cyborgItemTotal);
			list.add(item);
		}
		return list;
	}
	
	public int contributePlayer(String playerId){
		if(playerId.equals(this.killMaxPlayer)){
			return 1;
		}
		if(playerId.equals(this.damagedMaxPlayer)){
			return 1;
		}
		if(playerId.equals(this.monsterMaxPlayer)){
			return 1;
		}
		return 0;
	}
	
	
	@JSONField(serialize = false)
	public List<ItemInfo> getContributeRewards(String playerId){
		List<ItemInfo> list = new ArrayList<>();
		if(HawkOSOperator.isEmptyString(playerId)){
			return list;
		}
		if(playerId.equals(this.killMaxPlayer)){
			list.addAll(CyborgConstCfg.getInstance().getKillMaxRewards());
		}
		if(playerId.equals(this.damagedMaxPlayer)){
			list.addAll(CyborgConstCfg.getInstance().getDamagedMaxRewards());
		}
		if(playerId.equals(this.monsterMaxPlayer)){
			list.addAll(CyborgConstCfg.getInstance().getMonsterMaxRewards());
		}
		return list;
	}

	
	@JSONField(serialize = false)
	public CWTeamInfo.Builder build() {
		CWTeamInfo.Builder builder = CWTeamInfo.newBuilder();
		builder.setId(this.id);
		builder.setGuildId(this.guildId);
		builder.setGuildTag(this.tag);
		builder.setGuildFlag(flag);
		builder.setName(this.name);
		builder.setServerId(this.serverId);
		builder.setBattlePoint(this.totalPower);
		builder.setMemberCnt(this.memberCnt);
		builder.setRank(this.rank);
		return builder;
	}

	/**
	 * @return
	 */
	@JSONField(serialize = false)
	public PBCyborgContributionMail.Builder buildContributeMail(){
		if(HawkOSOperator.isEmptyString(this.killMaxPlayer) &&
				HawkOSOperator.isEmptyString(this.damagedMaxPlayer) &&
				HawkOSOperator.isEmptyString(this.monsterMaxPlayer)){
			return null;
		}
		PBCyborgContributionMail.Builder builder = PBCyborgContributionMail.newBuilder();
		if(!HawkOSOperator.isEmptyString(this.killMaxPlayer)){
			try {
				PBCyborgContributePlayer.Builder killMaxBuilder = PBCyborgContributePlayer.newBuilder();
				JsonFormat.merge(this.killMaxPlayerInfo, killMaxBuilder);
				builder.setKillMax(killMaxBuilder);
			} catch (ParseException e) {
				HawkException.catchException(e);
			}
			
		}
		if(!HawkOSOperator.isEmptyString(this.damagedMaxPlayer)){
			try {
				PBCyborgContributePlayer.Builder damagedMaxBuilder = PBCyborgContributePlayer.newBuilder();
				JsonFormat.merge(this.damagedMaxPlayerInfo, damagedMaxBuilder);
				builder.setDamagedMax(damagedMaxBuilder);
			} catch (ParseException e) {
				HawkException.catchException(e);
			}
		}
		if(!HawkOSOperator.isEmptyString(this.monsterMaxPlayer)){
			try {
				PBCyborgContributePlayer.Builder monsterMaxBuilder = PBCyborgContributePlayer.newBuilder();
				JsonFormat.merge(this.monsterMaxPlayerInfo, monsterMaxBuilder);
				builder.setMonsterMax(monsterMaxBuilder);
			} catch (ParseException e) {
				HawkException.catchException(e);
			}
		}
		return builder;
	}
}
