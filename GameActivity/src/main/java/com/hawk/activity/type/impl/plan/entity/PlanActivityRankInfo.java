package com.hawk.activity.type.impl.plan.entity;

import com.hawk.game.protocol.Activity.RankPB;

public class PlanActivityRankInfo {
	
	public String playerId;
	public RankPB.Builder pb;
	
	private PlanActivityRankInfo(String playerId, RankPB.Builder pb){
		this.playerId = playerId;
		this.pb = pb;
	}
	
	public static PlanActivityRankInfo valueOf(String playerId, RankPB.Builder pb){
		return new PlanActivityRankInfo(playerId, pb);
	}
}
