package com.hawk.activity.type.impl.seasonActivity.cfg;

import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.task.OrderTaskType;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 赛事活动任务表
 */
@HawkConfigManager.XmlResource(file = "activity/season/season_achieve.xml")
public class SeasonAchieveCfg extends HawkConfigBase implements IOrderTaskCfg {
    //成就id
    @Id
    private final int id;

    //比赛类型
    private final int match;

    //任务类型
    private final int orderType;

    //条件值
    private final int conditionVal;

    //增加经验
    private final int exp;

    //轮数
    private final int repeatVal;

    //任务类型
    private OrderTaskType taskType;

    //条件列表
    private List<Integer> conditionList;

    public SeasonAchieveCfg() {
        id = 0;
        match = 0;
        orderType = 0;
        conditionVal = 0;
        exp = 0;
        repeatVal = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getRepeatVal() {
        return repeatVal;
    }

    @Override
    public List<Integer> getConditionList() {
        return conditionList;
    }

    @Override
    public int getConditionValue() {
        switch (orderType){
            case 1047:
            case 1048:{
                return conditionVal;
            }
            default:{
                return 1;
            }
        }
    }

    @Override
    public int getExp() {
        return exp;
    }

    @Override
    public OrderTaskType getTaskType() {
        return taskType;
    }

    @Override
    protected boolean assemble() {
        taskType = OrderTaskType.getType(orderType);
        if (taskType == null) {
            HawkLog.errPrintln("taskType type not found! type: {}", orderType);
            return false;
        }
        List<Integer> conditionValueList = new ArrayList<>();
        conditionValueList.add(conditionVal);
        conditionList = conditionValueList;
        return true;
    }
}
