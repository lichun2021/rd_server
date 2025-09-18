package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

/**
 * 星球大战时间配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/star_wars_time.xml")
public class StarWarsTimeCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int termId;
	
	/** 开启时间 */
	private final String signStartTime;

	/** 报名结束时间 */
	private final String signEndTime;
	
	/** 匹配结束时间 */
	private final String matchEndTime;
	
	/** 第一场数据锁定时间 */
	private final String mangeEndTimeOne;

	/** 第一场战斗开启时间 */
	private final String warStartTimeOne;

	/** 第一场战斗结束结束时间 */
	private final String warEndTimeOne;
	
	/** 第二场数据锁定时间 */
	private final String mangeEndTimeTwo;
	
	/** 第二场战斗开启时间 */
	private final String warStartTimeTwo;
	
	/** 第二场战斗结束时间 */
	private final String warEndTimeTwo;

	/** 第三场数据锁定时间 */
	private final String mangeEndTimeThree;
	
	/** 第三场战斗开启时间 */
	private final String warStartTimeThree;
	
	/** 第三场战斗结束时间 */
	private final String warEndTimeThree;
	
	/** 结束时间*/
	private final String endTime;
	
	
	private long signStartTimeValue;

	private long signEndTimeValue;

	private long matchEndTimeValue;
	
	private long mangeEndTimeOneValue;

	private long warStartTimeOneValue;

	private long warEndTimeOneValue;
	
	private long mangeEndTimeTwoValue;
	
	private long warStartTimeTwoValue;
	
	private long warEndTimeTwoValue;
	
	private long mangeEndTimeThreeValue;
	
	private long warStartTimeThreeValue;
	
	private long warEndTimeThreeValue;
	
	private long endTimeValue;

	public StarWarsTimeCfg() {
		termId = 0;
		signStartTime = "";
		signEndTime = "";
		matchEndTime = "";
		mangeEndTimeOne = "";
		warStartTimeOne = "";
		warEndTimeOne = "";
		mangeEndTimeTwo = "";
		warStartTimeTwo = "";
		warEndTimeTwo = "";
		mangeEndTimeThree = "";
		warStartTimeThree = "";
		warEndTimeThree = "";
		endTime = "";
	}

	public int getTermId() {
		return termId;
	}

	public String getSignStartTime() {
		return signStartTime;
	}

	public String getSignEndTime() {
		return signEndTime;
	}

	public String getMatchEndTime() {
		return matchEndTime;
	}

	public String getMangeEndTimeOne() {
		return mangeEndTimeOne;
	}

	public String getWarStartTimeOne() {
		return warStartTimeOne;
	}

	public String getWarEndTimeOne() {
		return warEndTimeOne;
	}

	public String getMangeEndTimeTwo() {
		return mangeEndTimeTwo;
	}

	public String getWarStartTimeTwo() {
		return warStartTimeTwo;
	}

	public String getWarEndTimeTwo() {
		return warEndTimeTwo;
	}

	public long getSignStartTimeValue() {
		return signStartTimeValue;
	}

	public long getSignEndTimeValue() {
		return signEndTimeValue;
	}

	public long getMatchEndTimeValue() {
		return matchEndTimeValue;
	}

	public long getMangeEndTimeOneValue() {
		return mangeEndTimeOneValue;
	}

	public long getWarStartTimeOneValue() {
		return warStartTimeOneValue;
	}

	public long getWarEndTimeOneValue() {
		return warEndTimeOneValue;
	}

	public long getMangeEndTimeTwoValue() {
		return mangeEndTimeTwoValue;
	}

	public long getWarStartTimeTwoValue() {
		return warStartTimeTwoValue;
	}

	public long getWarEndTimeTwoValue() {
		return warEndTimeTwoValue;
	}
	
	public long getMangeEndTimeThreeValue() {
		return mangeEndTimeThreeValue;
	}

	public long getWarStartTimeThreeValue() {
		return warStartTimeThreeValue;
	}

	public long getWarEndTimeThreeValue() {
		return warEndTimeThreeValue;
	}

	public String getEndTime() {
		return endTime;
	}

	public long getEndTimeValue() {
		return endTimeValue;
	}

	protected boolean assemble() {
		signStartTimeValue = HawkTime.parseTime(signStartTime);
		signEndTimeValue = HawkTime.parseTime(signEndTime);
		matchEndTimeValue = HawkTime.parseTime(matchEndTime);
		mangeEndTimeOneValue = HawkTime.parseTime(mangeEndTimeOne);
		warStartTimeOneValue = HawkTime.parseTime(warStartTimeOne);
		warEndTimeOneValue = HawkTime.parseTime(warEndTimeOne);
		mangeEndTimeTwoValue = HawkTime.parseTime(mangeEndTimeTwo);
		warStartTimeTwoValue = HawkTime.parseTime(warStartTimeTwo);
		warEndTimeTwoValue = HawkTime.parseTime(warEndTimeTwo);
		mangeEndTimeThreeValue = HawkTime.parseTime(mangeEndTimeThree);
		warStartTimeThreeValue = HawkTime.parseTime(warStartTimeThree);
		warEndTimeThreeValue = HawkTime.parseTime(warEndTimeThree);
		endTimeValue = HawkTime.parseTime(endTime);
		return true;
	}

	@Override
	protected boolean checkValid() {
		ConfigIterator<StarWarsTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(StarWarsTimeCfg.class);
		long baseTermId = 0;
		for (StarWarsTimeCfg timeCfg : it) {
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" StarWarsTimeCfg check valid failed, term order error:termId: {}", termId);
				return false;
			}
//			if (signEndTime < signStartTime || matchEndTime < signEndTime || warStartTime < signEndTime || warEndTime < matchEndTime || warEndTime < warStartTime) {
//				HawkLog.errPrintln(" CrossTimeCfg check valid failed, termId: {}", termId);
//				return false;
//			}
		}
		return true;
	}
}
