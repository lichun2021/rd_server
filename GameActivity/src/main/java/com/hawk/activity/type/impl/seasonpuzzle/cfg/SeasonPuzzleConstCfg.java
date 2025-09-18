package com.hawk.activity.type.impl.seasonpuzzle.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/season_picture_puzzle/season_picture_puzzle_const.xml")
public class SeasonPuzzleConstCfg extends HawkConfigBase {
	
	private final int serverDelay;
	
	/** 每人每日可赠送次数 */
	private final int givePuzzleTimesLimit;
	
	/** 每人每日可获得碎片次数 */
	private final int achieveGetPuzzleTimesLimit;
	
	/** 每日清空拼图棋盘  */
	private final int resetTray;
	
	/** 每日清空碎片 */
	private final int resetPuzzle;
	
	/** 注水参数 */
	private final String addValue;
	
	/** 求助冷却时间 */
	private final int helpCD;

	
	private List<int[]> puzzleAddValList = new ArrayList<>();
	
	private static SeasonPuzzleConstCfg instance;
	
	public static SeasonPuzzleConstCfg getInstance() {
		return instance;
	}
	
	public SeasonPuzzleConstCfg() {
		this.serverDelay = 0;
		this.givePuzzleTimesLimit = 0;
		this.achieveGetPuzzleTimesLimit = 0;
		this.resetTray = 0;
		this.resetPuzzle = 0;
		this.addValue = "";
		this.helpCD = 0;
	}
	
	@Override
	protected boolean assemble() {
		puzzleAddValList = SerializeHelper.str2intList(this.addValue);
		instance = this;
		return true;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getGivePuzzleTimesLimit() {
		return givePuzzleTimesLimit;
	}

	public int getAchieveGetPuzzleTimesLimit() {
		return achieveGetPuzzleTimesLimit;
	}

	public int getResetTray() {
		return resetTray;
	}

	public int getResetPuzzle() {
		return resetPuzzle;
	}

	public List<int[]> getPuzzleAddValList() {
		return puzzleAddValList;
	}

	public long getHelpCD() {
		return helpCD * 1000L;
	}
}
