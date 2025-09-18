package com.hawk.activity.type.impl.strongestGuild.history;

import java.util.ArrayList;
import java.util.List;

/***
 * 某一期历史榜单
 * @author yang.rao
 *
 */
public class HistoryRank {
	
	/** 期数 **/
	private int termId;
	
	private long startTime;
	
	private long endTime;
	
	private List<PersonRank> personRank = new ArrayList<>();
	
	private List<GuildRank> guildRank = new ArrayList<>();
	
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public HistoryRank(){}

	public HistoryRank(int termId, long startTime, long endTime){
		this.termId = termId;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public List<PersonRank> getPersonRank() {
		return personRank;
	}

	public void setPersonRank(List<PersonRank> personRank) {
		this.personRank = personRank;
	}

	public List<GuildRank> getGuildRank() {
		return guildRank;
	}

	public void setGuildRank(List<GuildRank> guildRank) {
		this.guildRank = guildRank;
	}
	
	public void addPersonRank(PersonRank rank){
		personRank.add(rank);
	}
	
	public void addGuildRank(GuildRank rank){
		guildRank.add(rank);
	}

	public static class PersonRank{
		private int rank;
		private String guildName;
		private String playerName;
		private long score;
		public int getRank() {
			return rank;
		}
		public void setRank(int rank) {
			this.rank = rank;
		}
		public String getGuildName() {
			return guildName;
		}
		public void setGuildName(String guildName) {
			this.guildName = guildName;
		}
		public String getPlayerName() {
			return playerName;
		}
		public void setPlayerName(String playerName) {
			this.playerName = playerName;
		}
		public long getScore() {
			return score;
		}
		public void setScore(long score) {
			this.score = score;
		}
	}
	
	public static class GuildRank{
		private int rank;
		private String guildName;
		private long score;
		private int guildFlag;
		private String guildTag;
		public int getRank() {
			return rank;
		}
		public void setRank(int rank) {
			this.rank = rank;
		}
		public String getGuildName() {
			return guildName;
		}
		public void setGuildName(String guildName) {
			this.guildName = guildName;
		}
		public long getScore() {
			return score;
		}
		public void setScore(long score) {
			this.score = score;
		}
		public int getGuildFlag() {
			return guildFlag;
		}
		public void setGuildFlag(int guildFlag) {
			this.guildFlag = guildFlag;
		}
		public String getGuildTag() {
			return guildTag;
		}
		public void setGuildTag(String guildTag) {
			this.guildTag = guildTag;
		}
	}
}
