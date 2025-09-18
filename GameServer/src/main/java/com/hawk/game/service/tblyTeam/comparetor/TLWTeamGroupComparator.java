package com.hawk.game.service.tblyTeam.comparetor;

import com.hawk.game.service.tblyTeam.model.TBLYSeasonData;

import java.util.Comparator;

public class TLWTeamGroupComparator implements Comparator<TBLYSeasonData> {
    @Override
    public int compare(TBLYSeasonData o1, TBLYSeasonData o2) {
        if (o1.winCnt != o2.winCnt) {
            return o2.winCnt - o1.winCnt;
        } else if (o1.score != o2.score) {
            return o1.score > o2.score ? -1 : 1;
        } else if (o1.initPower != o2.initPower) {
            return o1.initPower > o2.initPower ? -1 : 1;
        }  else if (o1.createTime != o2.createTime) {
            return o1.createTime > o2.createTime ? -1 : 1;
        } else {
            if(o1.isSeed){
                return -1;
            }
            if(o2.isSeed){
                return 1;
            }
            return 0;
        }
    }
}
