package com.hawk.game.config;

import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import java.util.*;

/**
 * 方尖碑配置表
 * @author hf
 */
@HawkConfigManager.XmlResource(file = "xml/obelisk.xml")
public class ObeliskCfg extends HawkConfigBase {
    /**
     * 配置id
     */
    @Id
    private final int id;
    /**
     * 类型 全服/联盟/个人
     */
    private final int type;
    /** 第一期. 合服开新的一期*/
    private final int termId;
    /**
     * 前置任务
     */
    private final int unlockTask;
    /**
     * 开启类型1跟随上一个任务结束触发,2根据开服时间
     */
    private final int openType;
    /**
     * 开启时间
     */
    private final int openTime;
    /**
     * 任务时限单位小时
     */
    private final int duration;
    /**
     * 任务子类型,具体的类型,比如全服杀怪个数
     */
    private final int taskType;
    /**
     * 任务参数1
     */
    private final int para1;
    /**
     * 任务参数2
     */
    private final int para2;
    /**
     * 目标数量
     */
    private final int count;
    /**
     * 记录类型,1任务开启后开始记录,2开服即记录
     */
    private final int recordType;
    /**
     * 是否存在进度,1进度条计数类,2无进度条点击查看按钮显示排名
     */
    private final int isProgress;
    /**
     * 任务结束类型,1到达目标数量时结束,2到达任务结束时间结束
     */
    private final int endType;
    /**
     * 奖励
     */
    private final String reward;

    /**
     * 排行榜奖励
     */
    private final String rankReward;

    /**
     * 是否可以领取多份
     */
    private final int isRepeatAccept;
    /**
     * 领奖要求主城等级
     */
    private final int rewardLevelLimit;

    /**
     * 奖励列表
     */
    private List<ItemInfo> rewardList;

    /**
     * 排行榜奖励列表
     */
    private Map<String, List<ItemInfo>> rankRewardMap = new HashMap<>();

    public ObeliskCfg() {
        this.id = 0;
        this.type = 0;
        this.unlockTask = 0;
        this.openType = 0;
        this.openTime = 0;
        this.termId = 0;
        this.duration = 0;
        this.taskType = 0;
        this.para1 = 0;
        this.para2 = 0;
        this.count = 0;
        this.recordType = 0;
        this.isProgress = 0;
        this.endType = 0;
        this.reward = "";
        this.rankReward = "";
        this.isRepeatAccept = 0;
        this.rewardLevelLimit = 0;

    }
    
    @Override
    protected boolean assemble() {
        if(!HawkOSOperator.isEmptyString(reward)) {
            rewardList = ItemInfo.valueListOf(reward);
        }
        if(!HawkOSOperator.isEmptyString(rankReward)) {
            Map<String, String> map = SerializeHelper.stringToMap(rankReward, String.class, String.class, SerializeHelper.SEMICOLON_ITEMS, SerializeHelper.ELEMENT_SPLIT);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                List<ItemInfo> list = ItemInfo.valueListOf(value);
                rankRewardMap.put(key, list);
            }
        }
        return true;
    }

    /**
     * 根据排名获取奖励
     * @param rank
     * @return
     */
    public List<ItemInfo> getRankReward(int rank){
        Set<String> rankSet = rankRewardMap.keySet();
		for (String rankStr : rankSet) {
			List<Integer> arr = SerializeHelper.stringToList(Integer.class, rankStr, "-");
			if (rank >= arr.get(0) && rank <= arr.get(arr.size() - 1)) {
				return rankRewardMap.get(rankStr);
			}
		}
        return null;
    }

    

    @Override
	protected boolean checkValid() {
		// TODO termId 不为0 , 前置任务不为null 等 
		return super.checkValid();
	}

	public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getUnlockTask() {
        return unlockTask;
    }

    public int getOpenType() {
        return openType;
    }

    public long getOpenTime() {
        return openTime * HawkTime.HOUR_MILLI_SECONDS;
    }

    public long getDuration() {
        return duration * HawkTime.HOUR_MILLI_SECONDS;
    }

    public int getTaskType() {
        return taskType;
    }

    public int getPara1() {
        return para1;
    }

    public int getPara2() {
        return para2;
    }

    public int getCount() {
        return count;
    }

    public int getRecordType() {
        return recordType;
    }

    public int getIsProgress() {
        return isProgress;
    }

    public int getEndType() {
        return endType;
    }

    public String getReward() {
        return reward;
    }

    public int getIsRepeatAccept() {
        return isRepeatAccept;
    }

    public int getRewardLevelLimit() {
        return rewardLevelLimit;
    }

    public List<ItemInfo> getRewardList() {
        return rewardList;
    }

	public int getTermId() {
		return termId;
	}
    
    
}
