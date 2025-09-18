package com.hawk.game.data;

/**
 * 玩家的赏金追加信息
 * 
 * @Desc 用来记录玩家的追加 和被追加的赏金
 * @author RickMei
 * @Date 2018年12月7日 上午3:23:24
 */
public class PlayerAddBountyInfo {
	/**
	 * 追加给别人的上金属
	 */
	private int addBounty = 0;
	/**
	 * 被人追加的赏金数
	 */
	private int beAddBounty = 0;

	public int getAddBounty() {
		return addBounty;
	}

	public int getBeAddBounty() {
		return beAddBounty;
	}

	public void setAddBounty(int addBounty) {
		this.addBounty = addBounty;
	}

	public void setBeAddBounty(int beAddBounty) {
		this.beAddBounty = beAddBounty;
	}
	public void IncAddBounty(int val) {
		addBounty += val;
	}

	public void IncBeAddBounty(int val) {
		beAddBounty += val;
	}

	public boolean Effect(){
		return addBounty != 0 || beAddBounty != 0;
	}
	static public PlayerAddBountyInfo valueOf(int addBounty, int beAddBounty) {
		PlayerAddBountyInfo retInfo = new PlayerAddBountyInfo();
		retInfo.addBounty = addBounty;
		retInfo.beAddBounty = beAddBounty;
		return retInfo;
	}
}
