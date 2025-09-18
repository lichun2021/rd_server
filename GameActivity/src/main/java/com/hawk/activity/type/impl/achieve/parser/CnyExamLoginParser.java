package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.CnyExamLoginEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 新春试炼累计登录
 */
public class CnyExamLoginParser extends AchieveParser<CnyExamLoginEvent> {
    /**
     * 成就类型
     * @return 成就类型
     */
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.CNY_EXAM_LOGIN;
    }

    /**
     * 初始化逻辑
     * @param playerId 玩家id
     * @param achieveItem 成就数据
     * @param achieveConfig 成就配置
     * @return 初始化结果
     */
    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return true;
    }

    /**
     * 更新成就
     * @param achieveData 成就数据
     * @param achieveConfig 成就配置
     * @param event 累计登录事件
     * @return 更新结果
     */
    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, CnyExamLoginEvent event) {
        //累计登录天数
        int value = event.getLoginDays();
        //成就目标
        int configValue = achieveConfig.getConditionValue(0);
        //控制最大值
        if (value > configValue) {
            value = configValue;
        }
        //更新数据
        achieveData.setValue(0, value);
        //返回成功
        return true;
    }
}
