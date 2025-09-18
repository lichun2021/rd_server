package com.hawk.game.guild;

public class GuildDonateRank {
	private String playerId;
	
	// 捐献科技值
	private int donate;
	
	// 联盟贡献
	private int contribution;
	
	/**
	 * @param playerId
	 * @param scoreInfo 科技值_贡献
	 */
	public GuildDonateRank(String playerId, String scoreInfo){
		super();
		this.playerId = playerId;
		String[] scores = scoreInfo.split("_");
		this.donate = Integer.parseInt(scores[0]);
		this.contribution = Integer.parseInt(scores[1]);
	}
	
	public GuildDonateRank(String playerId, int donate, int contribution) {
		super();
		this.playerId = playerId;
		this.donate = donate;
		this.contribution = contribution;
	}
	
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public int getDonate() {
		return donate;
	}
	public void setDonate(int donate) {
		this.donate = donate;
	}
	public int getContribution() {
		return contribution;
	}
	public void setContribution(int contribution) {
		this.contribution = contribution;
	}
	
	/**
	 * 联盟捐献添加奖励
	 * @param addDonate
	 * @param addCont
	 */
	public void onDonate(int addDonate, int addCont) {
		this.donate += addDonate;
		this.contribution += addCont;
	}
	
	public String toString() {
		return this.donate + "_" + this.contribution;
	}
	
}
