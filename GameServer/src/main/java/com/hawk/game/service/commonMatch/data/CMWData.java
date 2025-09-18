package com.hawk.game.service.commonMatch.data;

import com.alibaba.fastjson.JSON;
import org.hawk.os.HawkOSOperator;

public class CMWData {
    public String teamId;
    public long score;
    public long rankingScore;
    public long totalScore;
    public int winCnt;
    public int loseCnt;
    public int rankingWinCnt;
    public int rankingLoseCnt;
    public int qualifierRank;
    public int rank;
    public int group;
    public boolean isNew;


    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static CMWData unSerialize(String json) {
        if (HawkOSOperator.isEmptyString(json)) {
            return null;
        }
        return JSON.parseObject(json, CMWData.class);
    }
}
