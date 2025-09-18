package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 泰伯利亚之战时间配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tiberium_war_time.xml")
public class TiberiumTimeCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int termId;
	
	/** 报名开启时间 */
	private final String signStartTime;

	/** 报名结束时间 */
	private final String signEndTime;
	
	/** 匹配结束时间 */
	private final String matchEndTime;

	/** 战斗阶段开启时间 */
	private final String warStartTime;

	/** 战斗阶段结束时间 */
	private final String warEndTime;
	
	/** 开放服务器列表 */
	private final String limitServer;
	
	/** 关闭服务器列表 */
	private final String forbidServer;


	private long signStartTimeValue;

	private long signEndTimeValue;

	private long matchEndTimeValue;

	private long warStartTimeValue;

	private long warEndTimeValue;

	private List<String> limitServerList;
	
	private List<String> forbidServerList;

	public TiberiumTimeCfg() {
		termId = 0;
		signStartTime = "";
		signEndTime = "";
		matchEndTime = "";
		warStartTime = "";
		warEndTime = "";
		limitServer = "";
		forbidServer = "";
	}

	public int getTermId() {
		return termId;
	}

	public final long getSignStartTimeValue() {
		return signStartTimeValue;
	}

	public final void setSignStartTimeValue(long signStartTimeValue) {
		this.signStartTimeValue = signStartTimeValue;
	}

	public final long getSignEndTimeValue() {
		return signEndTimeValue;
	}

	public final void setSignEndTimeValue(long signEndTimeValue) {
		this.signEndTimeValue = signEndTimeValue;
	}

	public final long getMatchEndTimeValue() {
		return matchEndTimeValue;
	}

	public final void setMatchEndTimeValue(long matchEndTimeValue) {
		this.matchEndTimeValue = matchEndTimeValue;
	}

	public final long getWarStartTimeValue() {
		return warStartTimeValue;
	}

	public final void setWarStartTimeValue(long warStartTimeValue) {
		this.warStartTimeValue = warStartTimeValue;
	}

	public final long getWarEndTimeValue() {
		return warEndTimeValue;
	}

	public final void setWarEndTimeValue(long warEndTimeValue) {
		this.warEndTimeValue = warEndTimeValue;
	}

	public final String getSignStartTime() {
		return signStartTime;
	}

	public final String getSignEndTime() {
		return signEndTime;
	}

	public final String getMatchEndTime() {
		return matchEndTime;
	}

	public final String getWarStartTime() {
		return warStartTime;
	}

	public final String getWarEndTime() {
		return warEndTime;
	}

	/**
	 * 获取可开启本期泰伯利亚之战区服列表
	 * 
	 * @return
	 */
	public List<String> getLimitServerList() {
		List<String> copy = new ArrayList<>();
		for (String serverId : limitServerList) {
			copy.add(serverId);
		}
		return copy;
	}
	
	/**
	 * 获取禁止开启本期泰伯利亚之战区服列表
	 * 
	 * @return
	 */
	public List<String> getForbidServerList() {
		List<String> copy = new ArrayList<>();
		for (String serverId : forbidServerList) {
			copy.add(serverId);
		}
		return copy;
	}
	
	
	protected boolean assemble() {
		signStartTimeValue = HawkTime.parseTime(signStartTime);
		signEndTimeValue = HawkTime.parseTime(signEndTime);
		matchEndTimeValue = HawkTime.parseTime(matchEndTime);
		warStartTimeValue = HawkTime.parseTime(warStartTime);
		warEndTimeValue = HawkTime.parseTime(warEndTime);
		limitServerList = new ArrayList<>();
		forbidServerList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(limitServer)) {
			for (String serverId : limitServer.split(",")) {
				limitServerList.add(serverId);
			}
		}
		if (!HawkOSOperator.isEmptyString(forbidServer)) {
			for (String serverId : forbidServer.split(",")) {
				forbidServerList.add(serverId);
			}
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		ConfigIterator<TiberiumTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(TiberiumTimeCfg.class);
		long baseTermId = 0;
		for (TiberiumTimeCfg timeCfg : it) {
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" CrossTimeCfg check valid failed, term order error:termId: {}", termId);
				return false;
			}
			baseTermId = termId;
			long signStartTime = timeCfg.getSignStartTimeValue();
			long signEndTime = timeCfg.getSignEndTimeValue();
			long matchEndTime = timeCfg.getMatchEndTimeValue();
			long warStartTime = timeCfg.getWarStartTimeValue();
			long warEndTime = timeCfg.getWarEndTimeValue();

			if (signEndTime < signStartTime || matchEndTime < signEndTime || warStartTime < signEndTime || warEndTime < matchEndTime || warEndTime < warStartTime) {
				HawkLog.errPrintln(" CrossTimeCfg check valid failed, termId: {}", termId);
				return false;
			}
		}
		return true;
	}
}
