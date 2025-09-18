package com.hawk.activity.type.impl.seasonpuzzle.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.XmlResource(file = "activity/season_picture_puzzle/season_picture_puzzle_tray.xml")
public class SeasonPuzzleTrayCfg extends HawkConfigBase {
    @Id
    private final int id;
    /**
     * 拼图期数
     */
    private final int periods;
    /**
     * 拼图期数开始时间
     */
    private final String openTime;
    /**
     * 结束时间
     */
    private final String endTime;
    /**
     * 拼图完成奖励
     */
    private final String rewards;
    /**
     * 获得拼图池
     */
    private final String puzzleItem;

    private long openTimeValue;
	private long endTimeValue;
	
	private List<Integer> puzzleItemList = new ArrayList<>();
	
	private static Set<Integer> allPuzzleItemSet = new HashSet<>();

    public SeasonPuzzleTrayCfg(){
        id = 0;
        periods = 0;
        openTime = "";
        endTime = "";
        rewards = "";
        puzzleItem = "";
    }

    /**
     * 解析
     * @return 解析结果
     */
    @Override
    protected boolean assemble() {
    	openTimeValue = HawkTime.parseTime(openTime);
		endTimeValue = HawkTime.parseTime(endTime);
		List<RewardItem.Builder> itemList = RewardHelper.toRewardItemImmutableList(puzzleItem);
		itemList.forEach(e -> puzzleItemList.add(e.getItemId()));
		allPuzzleItemSet.addAll(puzzleItemList);
        return true;
    }

    /**
     * 检查道具合法性
     * @return 检查结果
     */
    @Override
    protected final boolean checkValid() {
        return super.checkValid();
    }
    
    public int getId() {
        return id;
    }

    public int getPeriods() {
		return periods;
	}

	public String getRewards() {
		return rewards;
	}

	public String getOpenTime() {
		return openTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getPuzzleItem() {
		return puzzleItem;
	}

	public long getOpenTimeValue() {
		return openTimeValue;
	}

	public void setOpenTimeValue(long openTimeValue) {
		this.openTimeValue = openTimeValue;
	}

	public long getEndTimeValue() {
		return endTimeValue;
	}

	public void setEndTimeValue(long endTimeValue) {
		this.endTimeValue = endTimeValue;
	}

	public List<Integer> getPuzzleItemList() {
		return puzzleItemList; 
	}
	
	public static Set<Integer> getAllPuzzleItemSet() {
		return allPuzzleItemSet;
	}
}
