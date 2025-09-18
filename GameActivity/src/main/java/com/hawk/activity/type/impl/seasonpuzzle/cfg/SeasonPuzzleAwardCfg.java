package com.hawk.activity.type.impl.seasonpuzzle.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@HawkConfigManager.XmlResource(file = "activity/season_picture_puzzle/season_picture_puzzle_rewards.xml")
public class SeasonPuzzleAwardCfg extends HawkConfigBase {
    @Id
    private final int id;
    /**
     * 拼图期数
     */
    private final int periods;
    /**
     * 放入奖励
     */
    private final String rewards;
    /**
     * 赠送奖励
     */
    private final String giveRewards;
    /**
     * 道具
     */
    private final String item;
    /**
     * 对应拼图格子
     */
    private final int slotMark;

    private int itemId;
    private static Set<Integer> puzzleItemIds = new HashSet<>();
    private static Map<Integer, List<SeasonPuzzleAwardCfg>> periodConfigMap = new HashMap<>();
    
    public SeasonPuzzleAwardCfg(){
        id = 0;
        periods = 0;
        rewards = "";
        giveRewards = "";
        item = "";
        slotMark = 0;
    }

    /**
     * 解析
     * @return 解析结果
     */
    @Override
    protected boolean assemble() {
    	RewardItem.Builder rewardItem = RewardHelper.toRewardItem(item);
    	itemId = rewardItem.getItemId();
    	puzzleItemIds.add(rewardItem.getItemId());
    	List<SeasonPuzzleAwardCfg> list = periodConfigMap.get(periods);
    	if (list == null) {
    		list = new ArrayList<>();
    		periodConfigMap.put(periods, list);
    	}
    	list.add(this);
        return true;
    }

    /**
     * 检查道具合法性
     * @return 检查结果
     */
    @Override
    protected final boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
        if (!valid) {
            throw new InvalidParameterException(String.format("season_picture_puzzle_rewards.xml reward error, id: %s , reward: %s", id, rewards));
        }
        
        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(giveRewards);
        if (!valid) {
            throw new InvalidParameterException(String.format("season_picture_puzzle_rewards.xml reward error, id: %s , giveRewards: %s", id, giveRewards));
        }
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

	public String getGiveRewards() {
		return giveRewards;
	}

	public int getItemId() {
		return itemId;
	}
	
	public String getItem() {
		return item;
	}

	public int getSlotMark() {
		return slotMark;
	}

	public static List<SeasonPuzzleAwardCfg> getConfigList(int periods) {
		return periodConfigMap.get(periods);
	}
	
	public static Set<Integer> getPuzzleItemIds() {
		return puzzleItemIds;
	}
	
}
