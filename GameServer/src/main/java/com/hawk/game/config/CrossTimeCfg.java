package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankService;


/**
 * 跨服活动时间配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_time.xml")
public class CrossTimeCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int termId;

	/** 预览时间 */
	private final String showTime;

	/** 开启时间 */
	private final String startTime;

	/** 结束时间 */
	private final String endTime;

	/** 排行奖励发放时间 */
	private final String rankAwardTime;
	
	/** 可开放本期活动分组限制*/
	private final String limitGroup;

	/** 消失时间 */
	private final String hiddenTime;

	private long showTimeValue;

	private long startTimeValue;

	private long endTimeValue;

	private long awardTimeValue;

	private long hiddenTimeValue;
	
	private List<Integer> limitGroupList;

	public CrossTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		rankAwardTime = "";
		hiddenTime = "";
		limitGroup = "";
	}
	
	public int getTermId() {
		return termId;
	}

	public String getShowTime() {
		return showTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getHiddenTime() {
		return hiddenTime;
	}

	public long getShowTimeValue() {
		return showTimeValue;
	}

	public long getStartTimeValue() {
		return startTimeValue;
	}

	public long getEndTimeValue() {
		return endTimeValue;
	}

	public long getAwardTimeValue() {
		return awardTimeValue;
	}

	public long getHiddenTimeValue() {
		return hiddenTimeValue;
	}
	
	/**
	 * 获取限制可开启本期跨服区组id列表
	 * @return
	 */
	public List<Integer> getLimitGroupList() {
		List<Integer> copy = new ArrayList<>();
		for(Integer groupId : limitGroupList){
			copy.add(groupId);
		}
		return copy;
	}

	protected boolean assemble() {
		showTimeValue = HawkTime.parseTime(showTime);
		startTimeValue = HawkTime.parseTime(startTime);
		endTimeValue = HawkTime.parseTime(endTime);
		awardTimeValue = HawkTime.parseTime(rankAwardTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		limitGroupList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(limitGroup)) {
			for(String groupIdStr :  limitGroup.split(",")){
				limitGroupList.add(Integer.valueOf(groupIdStr));
			}
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		ConfigIterator<CrossTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(CrossTimeCfg.class);
		long baseTime = 0;
		long baseTermId = 0;
		for (CrossTimeCfg timeCfg : it) {
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" CrossTimeCfg check valid failed, term order error:termId: {}", termId);
				return false;
			}
			baseTermId = termId;
			long showTime = timeCfg.getShowTimeValue();
			long startTime = timeCfg.getStartTimeValue();
			long endTime = timeCfg.getEndTimeValue();
			long hiddenTime = timeCfg.getHiddenTimeValue();
			long awardTime = timeCfg.getAwardTimeValue();
			
			if (showTime < baseTime || startTime < showTime || endTime < startTime || awardTime < endTime
					|| hiddenTime < endTime) {
				HawkLog.errPrintln(" CrossTimeCfg check valid failed, termId: {}", termId);
				return false;
			}
			baseTime = hiddenTime;
		}
		return true;
	}
}
