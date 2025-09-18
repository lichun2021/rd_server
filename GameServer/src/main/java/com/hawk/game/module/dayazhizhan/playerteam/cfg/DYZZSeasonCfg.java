package com.hawk.game.module.dayazhizhan.playerteam.cfg;

import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.Set;

/**
 * 达雅赛季
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "xml/dyzz_season_cfg.xml")
public class DYZZSeasonCfg extends HawkConfigBase {

	/**
	 * 赛季初始话积分
	 */
	private final int seasonScoreInit; 
	
	
	/**
	 * 赛季积分增加计算参数
	 */
	private final double scoreParam1;
	private final double scoreParam2;
	private final double scoreParam3;
	
	/**
	 * 排行榜显示数量
	 */
	private final int scoreRankSize;
	/**
	 * 排行榜刷新时间 秒
	 */
	private final int scoreRankRefresh;

	/**
	 * 名人堂数量
	 */
	private final int fameHallSize;
	

	/**
	 * 每次匹配不上增加记分
	 */
	private final int matchAdd;
	
	/**
	 * 匹配间隔
	 */
	private final int matchTimeInterval;
	
	/**
	 * 赛季积分承接参数
	 */
	private final double contineScoreParam1;
	private final double contineScoreParam2;
	private final double contineScoreParam3;
	
	/**
	 * 战令进阶礼包ID
	 */
	private final int androidPayId;
	private final int iosPayId;
	private final int shareLimit;

	private final String itemSpecialCheck;
	
	
	public DYZZSeasonCfg() {
		seasonScoreInit = 0;
		scoreParam1 = 0;
		scoreParam2 = 0;
		scoreParam3 = 0;
		scoreRankSize= 0;
		scoreRankRefresh = 1800;
		fameHallSize = 0;
		matchAdd= 0;
		matchTimeInterval = 10000;
		contineScoreParam1= 0;
		contineScoreParam2= 0;
		contineScoreParam3= 0;
		androidPayId= 0;
		iosPayId= 0;
		shareLimit= 0;
		itemSpecialCheck = "3140001;3140002;3140003;3140004";
	}

	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}
	
	public int getSeasonScoreInit() {
		return seasonScoreInit;
	}
	
	public double getScoreParam1() {
		return scoreParam1;
	}
	
	public double getScoreParam2() {
		return scoreParam2;
	}
	
	public double getScoreParam3() {
		return scoreParam3;
	}
	
	public int getScoreRankSize() {
		return scoreRankSize;
	}
	
	public int getScoreRankRefresh() {
		return scoreRankRefresh;
	}
	

	
	
	public int getFameHallSize() {
		return fameHallSize;
	}
	
	public int getMatchInterval() {
		return matchTimeInterval;
	}
	
	public int getMatchScoreAdd() {
		return matchAdd;
	}
	
	public double getContineScoreParam1() {
		return contineScoreParam1;
	}
	
	public double getContineScoreParam2() {
		return contineScoreParam2;
	}

	public double getContineScoreParam3() {
		return contineScoreParam3;
	}

	public int getIosPayId() {
		return iosPayId;
	}
	
	public int getAndroidPayId() {
		return androidPayId;
	}
	
	public int getShareLimit() {
		return shareLimit;
	}

	public Set<Integer> getItemSpecialCheck() {
		return SerializeHelper.stringToSet(Integer.class, itemSpecialCheck, SerializeHelper.SEMICOLON_ITEMS);
	}
}
