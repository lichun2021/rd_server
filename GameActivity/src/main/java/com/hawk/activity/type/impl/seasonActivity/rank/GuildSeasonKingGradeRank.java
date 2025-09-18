package com.hawk.activity.type.impl.seasonActivity.rank;

import com.hawk.activity.type.impl.rank.ActivityRank;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

import java.util.List;

/**
 * 联盟王者段位排行数据
 */
public class GuildSeasonKingGradeRank extends ActivityRank implements SplitEntity {
    public GuildSeasonKingGradeRank(){

    }

    public GuildSeasonKingGradeRank(String guildId, long score, int rank) {
        this.setId(guildId);//联盟id
        this.setScore(score);//分数
        this.setRank(rank);//排名
    }

    public GuildSeasonKingGradeRank(String guildId, long score) {
        this.setId(guildId);//联盟id
        this.setScore(score);//分数
    }

    //实例化数据
    @Override
    public SplitEntity newInstance() {
        return new GuildSeasonKingGradeRank();
    }

    //序列号
    @Override
    public void serializeData(List<Object> dataList) {
        dataList.add(getId());
        dataList.add(getScore());
        dataList.add(getRank());
    }

    //解析
    @Override
    public void fullData(DataArray dataArray) {
        dataArray.setSize(3);
        setId(dataArray.getString());
        setScore(dataArray.getLong());
        setRank(dataArray.getInt());
    }
}
