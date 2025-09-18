package com.hawk.game.service.tblyTeam.comparetor;

import com.hawk.game.service.tblyTeam.model.TBLYSeasonData;

import java.util.Comparator;

public class TLWTeamTotalRankComparator implements Comparator<TBLYSeasonData> {
    @Override
    public int compare(TBLYSeasonData arg0, TBLYSeasonData arg1) {
        int gap = 0;
        if (arg0.getRankOrder() != arg1.getRankOrder()) {
            // 按淘汰赛组别由高到低
            gap = arg0.getRankOrder() - arg1.getRankOrder();
        } else if (arg0.kickOutTerm != arg1.kickOutTerm) {
            // 按淘汰轮次由高到低
            gap = arg0.kickOutTerm - arg1.kickOutTerm;
        } else if (arg0.score != arg1.score) {
            // 按总积分由高到低
            gap = arg0.score > arg1.score ? 1 : -1;
        } else if (arg0.lastestPower != arg1.lastestPower) {
            // 按被淘汰时的出战战力由低到高
            gap = arg0.lastestPower > arg1.lastestPower ? -1 : 1;
        } else if (arg0.initPower != arg1.initPower) {
            // 按入围时出战战力由高到低
            gap = arg0.initPower > arg1.initPower ? 1 : -1;
        } else if (arg0.createTime != arg1.createTime) {
            // 按入联盟创建时间
            gap = arg0.createTime > arg1.createTime ? 1 : -1;
        } else {
            return 0;
        }
        return -gap;
    }
}
