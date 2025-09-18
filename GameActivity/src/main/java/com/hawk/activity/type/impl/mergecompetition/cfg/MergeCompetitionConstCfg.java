package com.hawk.activity.type.impl.mergecompetition.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/merge_competition/%s/merge_competition_cfg.xml", autoLoad=false, loadParams="368")
public class MergeCompetitionConstCfg extends HawkConfigBase {

	/** 服务器开服延时开启活动时间 单位:s */
	private final int serverDelay;
	
	/** 个人去兵战力榜最大名次 */
	private final int rankType1ShowMax;
	
	/** 联盟去兵战力榜最大名次 */
	private final int rankType2ShowMax;
	
	/** 个人体力消耗榜最大名次 */
	private final int rankType3ShowMax;
	
	/** 全军嘉奖积分榜最大名次 */
	private final int rankType4ShowMax;
	
	/** 区服积分榜最大名次 */
	private final int rankType5ShowMax;
	
	/** 个人去兵战力榜-本服前X给国家积分 */
	private final int rankType1LocalNum;
	
	/** 联盟去兵战力榜-本服前X给国家积分 */
	private final int rankType2LocalNum;
	
	/** 个人体力消耗榜-本服前X给国家积分 */
	private final int rankType3LocalNum;
	
	
	private static MergeCompetitionConstCfg instance;
	
	public static MergeCompetitionConstCfg getInstance() {
		return instance;
	}
	
	public MergeCompetitionConstCfg() {
		serverDelay = 0;
		rankType1ShowMax = 0;
		rankType2ShowMax = 0;
		rankType3ShowMax = 0;
		rankType4ShowMax = 0;
		rankType5ShowMax = 0;
		rankType1LocalNum = 0;
		rankType2LocalNum = 0;
		rankType3LocalNum = 0;
	}

	@Override
	protected boolean assemble() {
		instance = this;
		return true;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getRankType1ShowMax() {
		return rankType1ShowMax;
	}

	public int getRankType2ShowMax() {
		return rankType2ShowMax;
	}

	public int getRankType3ShowMax() {
		return rankType3ShowMax;
	}

	public int getRankType4ShowMax() {
		return rankType4ShowMax;
	}

	public int getRankType5ShowMax() {
		return rankType5ShowMax;
	}

	public int getRankType1LocalNum() {
		return rankType1LocalNum;
	}

	public int getRankType2LocalNum() {
		return rankType2LocalNum;
	}

	public int getRankType3LocalNum() {
		return rankType3LocalNum;
	}

}
