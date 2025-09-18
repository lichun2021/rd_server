package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 英雄档案馆配置
 *
 */
@HawkConfigManager.KVResource(file = "xml/hero_archives_const.xml")
public class HeroArchivesConstCfg extends HawkConfigBase {

	/**
	 * 实例
	 */
	private static HeroArchivesConstCfg instance = null;

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static HeroArchivesConstCfg getInstance() {
		return instance;
	}

	/**
	 * 第一个英雄解锁消耗
	 */
	private final String archivesFirstHeroCost;
	
	/**
	 * 解锁递增
	 */
	private final int archivesAccumulationCost;

	/**
	 * 解锁递增上限
	 */
	private final int maxCost;
	
	/**
	 * 构造
	 */
	public HeroArchivesConstCfg() {
		instance = this;
		archivesFirstHeroCost = "";
		archivesAccumulationCost = 0;
		maxCost = 0;
	}

	public List<ItemInfo> getArchivesFirstHeroCost() {
		return ItemInfo.valueListOf(archivesFirstHeroCost);
	}

	public int getArchivesAccumulationCost() {
		return archivesAccumulationCost;
	}

	public int getMaxCost() {
		return maxCost;
	}
}
