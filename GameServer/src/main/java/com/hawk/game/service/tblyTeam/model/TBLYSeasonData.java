package com.hawk.game.service.tblyTeam.model;

import com.alibaba.fastjson.JSON;
import com.hawk.game.service.tiberium.TiberiumConst;
import org.hawk.os.HawkOSOperator;

public class TBLYSeasonData {
    public String teamId;
    public String serverId;
    public int serverType;
    public int teamType;
    public int groupType;
    public int eGroupType;
    public int groupId;
    public boolean isSeed;
    public long score;
    public int winCnt;
    public int loseCnt;
    public int kickOutlose;
    public int kickOutTerm = 100;
    public int kickOutgroupType;
    public long initPower;
    public long lastestPower;
    public int initPowerRank;
    public long createTime;
    public int finalRank;

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static TBLYSeasonData unSerialize(String json) {
        if(HawkOSOperator.isEmptyString(json)){
            return null;
        }
        return JSON.parseObject(json, TBLYSeasonData.class);
    }

    public int getRankOrder(){
        if(kickOutgroupType > 0){
            return TiberiumConst.TLWGroupType.getType(kickOutgroupType).getRankOrder();
        }else {
            return TiberiumConst.TLWGroupType.getType(groupType).getRankOrder();
        }
    }
}
