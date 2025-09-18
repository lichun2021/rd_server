package com.hawk.activity.type.impl.developFastOld.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/develop_fast_old/develop_fast_old_task.xml")
public class DevelopFastOldAchieveCfg extends AchieveConfig {
    /** 成就id*/
    @Id
    private final int achieveId;
    /** 成就分类*/
    private final int category;
    /** 等级阶段 */
    private final int chapter;
    /** 显示顺序 */
    private final int showOrder;


    /** 条件类型*/
    private final int conditionType;
    /** 条件值*/
    private final String conditionValue;
    /** 奖励列表*/
    private final String rewards;

    /** 成就类型 */
    private AchieveType achieveType;

    /** 成就奖励 */
    private List<Reward.RewardItem.Builder> rewardList;

    /** 成就值 */
    private List<Integer> conditionValueList;

    public DevelopFastOldAchieveCfg(){
        achieveId = 0;
        category = 0;
        chapter = 0;
        showOrder = 0;
        conditionType = 0;
        conditionValue = "";
        rewards = "";
    }

    /**
     * 解析配置
     * @return 是否解析成功
     */
    @Override
    protected boolean assemble() {
        try {
            //type 检验
            achieveType = AchieveType.getType(conditionType);
            if (achieveType == null) {
                HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
                return false;
            }
            //道具解析
            rewardList = RewardHelper.toRewardItemImmutableList(rewards);
            //条件解析
            conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    /**
     * 成就id
     * @return 获得成绩id
     */
    @Override
    public int getAchieveId() {
        return achieveId;
    }

    /**
     * 获得成就类型
     * @return 获得成就类型
     */
    @Override
    public AchieveType getAchieveType() {
        return achieveType;
    }

    /**
     * 获得条件值
     * @return 条件值
     */
    @Override
    public List<Integer> getConditionValues() {
        return conditionValueList;
    }

    /**
     * 获得奖励
     * @return 奖励
     */
    @Override
    public List<Reward.RewardItem.Builder> getRewardList() {
        return RewardHelper.toRewardItemImmutableList(rewards);
    }

    /**
     * 获得奖励字段
     * @return 奖励字段
     */
    @Override
    public String getReward() {
        return rewards;
    }

    /**
     * 获得类型
     * @return 类型
     */
    public int getCategory() {
        return category;
    }

    /**
     * 获得阶段等级
     * @return 阶段等级
     */
    public int getChapter() {
        return chapter;
    }

    /**
     * 获得成就类型
     * @return 成就类型
     */
    public int getConditionType() {
        return conditionType;
    }

    /**
     * 获得显示顺序
     * @return 显示顺序
     */
    public int getShowOrder() {
        return showOrder;
    }
}
