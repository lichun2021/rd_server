package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.item.ItemInfo;

/**
 * 剧情战役 关卡表
 * 章节表包含关卡表
 * @author jm
 *
 */
@HawkConfigManager.XmlResource(file = "xml/plot_levels.xml")
public class PlotLevelCfg extends HawkConfigBase {
	/**
	 * 关卡ID
	 */
	@Id
	protected final int missionId;
	/**
	 * 关卡名字
	 */
	protected final String missionName;
	/**
	 * 章节ID
	 */
	protected final int chapterId;
	/**
	 * 序号
	 */
	protected final int sequeceNo;
	/**
	 * 第一次通关奖励 对应award表
	 */
	protected final String firstReward;
	/**
	 * 第一次通关之后的奖励
	 */
	//protected final String commonReward;
	/**
	 * 最少的通关时间
	 */
	protected final int minTime;
	/**
	 * 最大的通关时间
	 */
	protected final int maxTime;
	/**
	 * 剧情章节
	 */
	protected final int missionChapter;
	/**
	 *通用奖励格式
	 */
	private List<ItemInfo> firstRewardItem;
	/**
	 * 军衔
	 */
	private final int rankLevel;
	
	public List<ItemInfo> getFirstRewardItem() {
		return firstRewardItem;
	}

	public List<ItemInfo> getCommonRewardItem() {
		return commonRewardItem;
	}

	/**
	 * 通用奖励格式
	 */
	private List<ItemInfo> commonRewardItem;
	
	public PlotLevelCfg() {
		missionId = 0;
		missionName = "";
		chapterId = 0;
		sequeceNo = 0;
		firstReward = "";
		//commonReward = "";
		minTime = 0;
		maxTime = 0;
		missionChapter = 0;
		rankLevel = 0;
	}
	
	public int getMissionId() {
		return missionId;
	}
	public String getMissionName() {
		return missionName;
	}
	public int getChapterId() {
		return chapterId;
	}
	public int getSequeceNo() {
		return sequeceNo;
	}
	/*public String getFirstReward() {
		return firstReward;
	}*/
	/*public String getCommonReward() {
		return commonReward;
	}*/
	public int getMinTime() {
		return minTime;
	}
	public int getMaxTime() {
		return maxTime;
	}
	public int getMissionChapter() {
		return missionChapter;
	}
	
	@Override
	public boolean assemble() {
		try {
			firstRewardItem = new ArrayList<>();
			firstRewardItem = ItemInfo.valueListOf(firstReward);
			//commonRewardItem = ItemInfo.valueListOf(commonReward, ";");
			commonRewardItem = new ArrayList<>();
		} catch (Exception e) {
			HawkException.catchException(e);
			
			return false;
		}
		
		return true;
		
	}
	
	@Override
	public boolean checkValid() {
		firstRewardItem.stream().filter(item-> {
			RewardHelper.checkRewardItem(item.getType(),item.getItemId(), item.getCount());
			
			return true;
		});
		
		commonRewardItem.stream().filter(item-> {
			RewardHelper.checkRewardItem(item.getType(),item.getItemId(), item.getCount());
			
			return true;
		});
		
		boolean chapterIdCheckResult = false;
		chapterIdCheckResult = PlotChapterCfg.isExistChapterId(chapterId);
		
		return chapterIdCheckResult;
	}

	public int getRankLevel() {
		return rankLevel;
	}
}
