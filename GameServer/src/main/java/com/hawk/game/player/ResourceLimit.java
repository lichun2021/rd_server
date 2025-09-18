package com.hawk.game.player;

public class ResourceLimit {
	//玩家最大石油数量
	private long maxStoreOil;
	//玩家最大钢铁数量
	private long maxStoreSteel;
	//玩家最大合金数量
	private long maxStoreRare;
	//玩家最大矿石数量
	private long maxStoreOre;
	//玩家每小时产出石油
	private long oilOutputPerHour;
	//玩家每小时产出钢铁
	private long steelOutputPerHour;
	//玩家每小时产出合金
	private long rareOutputPerHour;
	//玩家每小时产出矿石
	private long oreOutputPerHour;
	
	public ResourceLimit() {
		
	}
	
	/**
	 * 
	 * @return 玩家石油最大存储量
	 */
	public long getMaxStoreOil() {
		return maxStoreOil;
	}

	/**
	 * 
	 * @param addStoreOil 玩家石油最大存储量
	 */
	public void addOilMaxStore(long addStoreOil) {
		this.maxStoreOil += addStoreOil;
	}

	/**
	 * 
	 * @return 玩家钢铁最大存储量
	 */
	public long getMaxStoreSteel() {
		return maxStoreSteel;
	}

	/**
	 * 
	 * @param addStoreSteel 玩家钢铁最大存储量
	 */
	public void addSteelMaxStore(long addStoreSteel) {
		this.maxStoreSteel += addStoreSteel;
	}

	/**
	 * 
	 * @return 玩家合金最大存储量
	 */
	public long getMaxStoreRare() {
		return maxStoreRare;
	}

	/**
	 * 
	 * @param addStoreRare 玩家合金最大存储量
	 */
	public void addRareMaxStore(long addStoreRare) {
		this.maxStoreRare += addStoreRare;
	}

	/**
	 * 
	 * @return 玩家矿石最大存储量
	 */
	public long getMaxStoreOre() {
		return maxStoreOre;
	}

	/**
	 * 
	 * @param addStoreOre 玩家矿石最大存储量
	 */
	public void addOreMaxStore(long addStoreOre) {
		this.maxStoreOre += addStoreOre;
	}

	/**
	 * 
	 * @return 玩家每小时石油产出量
	 */
	public long getOilOutputPerHour() {
		return oilOutputPerHour;
	}

	/**
	 * 
	 * @param addOilOutputPerHour 玩家每小时石油产出量
	 */
	public void addOilOutputPerHour(long addOilOutputPerHour) {
		this.oilOutputPerHour += addOilOutputPerHour;
	}

	/**
	 * 
	 * @return 玩家每小时钢铁产出量
	 */
	public long getSteelOutputPerHour() {
		return steelOutputPerHour;
	}

	/**
	 * 
	 * @param addSteelOutputPerHour 玩家每小时钢铁产出量
	 */
	public void addSteelOutputPerHour(long addSteelOutputPerHour) {
		this.steelOutputPerHour += addSteelOutputPerHour;
	}

	/**
	 * 玩家每小时合金产出量
	 * @return
	 */
	public long getRareOutputPerHour() {
		return rareOutputPerHour;
	}

	/**
	 * 
	 * @param addRareOutputPerHour 玩家每小时合金产出量
	 */
	public void addRareOutputPerHour(long addRareOutputPerHour) {
		this.rareOutputPerHour += addRareOutputPerHour;
	}

	/**
	 * 玩家每小时矿石产出量
	 * @return
	 */
	public long getOreOutputPerHour() {
		return oreOutputPerHour;
	}

	/**
	 * 
	 * @param addOreOutputPerHour 玩家每小时矿石产出量
	 */
	public void addOreOutputPerHour(long addOreOutputPerHour) {
		this.oreOutputPerHour += addOreOutputPerHour;
	}

	/**
	 * 初始化数据
	 */
	public void init() {
		maxStoreOil = 0;
		maxStoreSteel = 0;
		maxStoreRare = 0;
		maxStoreOre = 0;
		oilOutputPerHour = 0;
		steelOutputPerHour = 0;
		rareOutputPerHour = 0;
		oreOutputPerHour = 0;
	}

}
