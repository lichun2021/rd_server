package com.hawk.activity.type.impl.developFast.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.game.protocol.Reward;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/develop_fast/develop_fast_level.xml")
public class DevelopFastLevelCfg extends HawkConfigBase {
    /** id*/
    @Id
    private final int id;
    /** 类型*/
    private final int type;
    /** 等级阶段*/
    private final int chapter;
    /** 条件值*/
    private final int conditionValue;
    /** 分数*/
    private final int score;
    /** 奖励*/
    private final String rewards;
    /** 解析奖励*/
    private List<Reward.RewardItem.Builder> rewardList;

    /**
     * 构造函数
     */
    public DevelopFastLevelCfg(){
        id = 0;
        type = 0;
        chapter = 0;
        conditionValue = 0;
        score = 0;
        rewards = "";
    }

    /**
     * 解析配置
     * @return 是否解析成功
     */
    @Override
    protected boolean assemble() {
        try {
            //解析奖励
            rewardList = RewardHelper.toRewardItemImmutableList(rewards);
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    /**
     * 获得id
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * 获得类型
     * @return 类型
     */
    public int getType() {
        return type;
    }

    /**
     * 获得章节等级
     * @return 获得章节等级
     */
    public int getChapter() {
        return chapter;
    }

    /**
     * 获得条件值
     * @return 条件值
     */
    public int getConditionValue() {
        return conditionValue;
    }

    /**
     * 获得奖励
     * @return 奖励
     */
    public List<Reward.RewardItem.Builder> getRewardList() {
        return RewardHelper.toRewardItemImmutableList(rewards);
    }

    /**
     * 获得分数
     * @return 分数
     */
    public int getScore() {
        return score;
    }
}
