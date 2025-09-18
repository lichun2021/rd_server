package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

/**
 * 锦标赛基础配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "xml/championship_const.xml")
public class ChampionshipConstCfg extends HawkConfigBase {

	/**
	 * 实例
	 */
	private static ChampionshipConstCfg instance = null;

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static ChampionshipConstCfg getInstance() {
		return instance;
	}

	/**
	 * 总开关
	 */
	private final int isSystemClose;

	/**
	 * 开服时长不足的服务器不参与泰伯利亚之战(单位:秒)
	 */
	private final int serverDelay;

	/**
	 * 参与匹配最低人数限制
	 */
	private final int warMemberMinCnt;
	
	/**
	 * 参战最低大本等级限制
	 */
	private final int cityLvlLimit;

	/**
	 * 各段位出战人数限制
	 */
	private final String gradeMemberCnt;

	/**
	 * 匹配竞争锁有效期(单位:秒)
	 */
	private final int matchLockExpire;

	/**
	 * 匹配准备时间(单位:秒)
	 */
	private final int matchPrepareTime;

	/**
	 * 各等级部队积分
	 */
	private final String scoreCof;

	/**
	 * 各段位出战人数限制
	 */
	private Map<Integer, Integer> gradeMemberLimitMap;

	private Map<Integer, Integer> scoreMap;

	/**
	 * 构造
	 */
	public ChampionshipConstCfg() {
		instance = this;
		matchPrepareTime = 120;
		isSystemClose = 0;
		serverDelay = 0;
		warMemberMinCnt = 0;
		cityLvlLimit = 12;
		matchLockExpire = 120;
		gradeMemberCnt = "";
		scoreCof = "";
	}

	public boolean isSystemClose() {
		return isSystemClose == 1;
	}

	public final long getServerDelay() {
		return serverDelay * 1000l;
	}

	public final int getWarMemberMinCnt() {
		return warMemberMinCnt;
	}

	public static final void setInstance(ChampionshipConstCfg instance) {
		ChampionshipConstCfg.instance = instance;
	}

	public int getMatchLockExpire() {
		return matchLockExpire;
	}

	public long getMatchPrepareTime() {
		return matchPrepareTime * 1000l;
	}
	
	public int getScore(int lvl){
		return scoreMap.getOrDefault(lvl, 0);
	}
	
	/**
	 * 获取指定段位出战人数限制
	 * 
	 * @param grade
	 * @return
	 */
	public int getGradeMemberLimit(int grade) {
		if (gradeMemberLimitMap.containsKey(grade)) {
			return gradeMemberLimitMap.get(grade);
		}
		return 1;
	}
	
	public int getCityLvlLimit() {
		return cityLvlLimit;
	}

	@Override
	protected boolean assemble() {
		try {
			Map<Integer, Integer> memberLimitMap = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(gradeMemberCnt)) {
				String[] timeStrs = gradeMemberCnt.split(",");
				for (String timeStr : timeStrs) {
					String[] strs = timeStr.split("_");
					int grade = Integer.valueOf(strs[0]);
					int cnt = Integer.valueOf(strs[1]);
					memberLimitMap.put(grade, cnt);
				}
			}
			gradeMemberLimitMap = memberLimitMap;
			Map<Integer, Integer> scoreLvlMap = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(scoreCof)) {
				String[] scoreStrs = scoreCof.split("_");
				for (int i = 0; i < scoreStrs.length; i++) {
					int score = Integer.valueOf(scoreStrs[i]);
					scoreLvlMap.put(i + 1, score);
				}
			}
			this.scoreMap = scoreLvlMap;
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return super.assemble();
	}

}
