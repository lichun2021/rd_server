package com.hawk.game.guild.championship;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.guild.championship.GCConst.GCGuildGrade;
import com.hawk.game.guild.championship.GCConst.GCGuildStage;
import com.hawk.game.protocol.GuildChampionship.GCGuildInfo;

/**
 * @author Jesse
 *
 */
public class GCGuildData implements Comparable<GCGuildData> {
	public String id;

	/** 区服id,每次成员报名时检测serverId */
	public String serverId;

	public String name;

	public String tag;

	public int flag;

	/** 段位 */
	public GCGuildGrade grade = GCGuildGrade.GRADE_3;

	/** 上一期段位 */
	public GCGuildGrade lastGrade = GCGuildGrade.GRADE_3;
	
	/** 上一期战斗时所在的分组*/
	public GCGuildGrade lastBattleGrade = GCGuildGrade.GRADE_3;

	/** 晋级阶段 */
	public GCGuildStage stage;

	/** 最近一次出战期数 */
	public int termId;

	/** 分组id */
	public String groupId;
	
	/** 小组中的位置*/
	public int gPosIndex;

	/** 淘汰玩家数量 */
	public int kickCnt;
	
	/** 出战总战力*/
	public long totalPower;
	
	/** 小组排名*/
	public int groupRank;
	
	/** 出战人数*/
	public int memberCnt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public GCGuildGrade getGrade() {
		return grade;
	}

	public void setGrade(GCGuildGrade grade) {
		this.grade = grade;
	}

	public GCGuildGrade getLastGrade() {
		return lastGrade;
	}

	public void setLastGrade(GCGuildGrade lastGrade) {
		this.lastGrade = lastGrade;
	}

	public GCGuildGrade getLastBattleGrade() {
		return lastBattleGrade;
	}

	public void setLastBattleGrade(GCGuildGrade lastBattleGrade) {
		this.lastBattleGrade = lastBattleGrade;
	}

	public GCGuildStage getStage() {
		return stage;
	}

	public void setStage(GCGuildStage stage) {
		this.stage = stage;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getgPosIndex() {
		return gPosIndex;
	}

	public void setgPosIndex(int gPosIndex) {
		this.gPosIndex = gPosIndex;
	}

	public int getKickCnt() {
		return kickCnt;
	}

	public void setKickCnt(int kickCnt) {
		this.kickCnt = kickCnt;
	}

	public long getTotalPower() {
		return totalPower;
	}

	public void setTotalPower(long totalPower) {
		this.totalPower = totalPower;
	}
	
	public int getGroupRank() {
		return groupRank;
	}

	public void setGroupRank(int groupRank) {
		this.groupRank = groupRank;
	}

	public int getMemberCnt() {
		return memberCnt;
	}

	public void setMemberCnt(int memberCnt) {
		this.memberCnt = memberCnt;
	}

	@Override
	@JSONField(serialize = false)
	public int compareTo(GCGuildData arg0) {
		long gap = this.stage.getValue() - arg0.getStage().getValue();
		if (gap > 0) {
			return -1;
		} else if (gap < 0) {
			return 1;
		}
		gap = this.kickCnt - arg0.kickCnt;
		if (gap > 0) {
			return -1;
		} else if (gap < 0) {
			return 1;
		}
		gap = this.totalPower - arg0.totalPower;
		if (gap > 0) {
			return -1;
		} else if (gap < 0) {
			return 1;
		}
		return this.id.compareTo(arg0.id);
	}
	
	/**
	 * 构建联盟基础信息
	 * @return
	 */
	@JSONField(serialize = false)
	public GCGuildInfo.Builder build() {
		GCGuildInfo.Builder builder = GCGuildInfo.newBuilder();
		builder.setId(this.id);
		builder.setName(this.name);
		builder.setTag(this.tag);
		builder.setGuildFlag(flag);
		builder.setServerId(this.serverId);
		builder.setMemberCnt(this.memberCnt);
		return builder;
	}
}
