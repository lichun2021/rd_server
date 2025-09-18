package com.hawk.activity.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * mysql 活动限制排行榜  
 *
 * @author RickMei 
 *
 */
@Entity
public class MsPlayerRankInfo {
	@Id
	@Column(name = "playerId")
	protected String playerId;
	
	@Column(name = "score")
	protected long score;
	
	public String getPlayerId(){
		return this.playerId;
	}
	
	public long getScore(){
		return this.score;
	}
}
