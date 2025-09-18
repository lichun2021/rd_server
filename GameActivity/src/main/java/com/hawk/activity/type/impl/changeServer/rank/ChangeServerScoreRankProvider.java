package com.hawk.activity.type.impl.changeServer.rank;

import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;

import java.util.List;

public class ChangeServerScoreRankProvider extends AbstractActivityRankProvider<ChangeServerScoreRank> {
    @Override
    protected boolean canInsertIntoRank(ChangeServerScoreRank rankInfo) {
        return false;
    }

    @Override
    protected boolean insertRank(ChangeServerScoreRank rankInfo) {
        return false;
    }

    @Override
    protected int getRankSize() {
        return 0;
    }

    @Override
    public ActivityRankType getRankType() {
        return null;
    }

    @Override
    public boolean isFixTimeRank() {
        return false;
    }

    @Override
    public void loadRank() {

    }

    @Override
    public void doRankSort() {

    }

    @Override
    public List<ChangeServerScoreRank> getRankList() {
        return null;
    }

    @Override
    public ChangeServerScoreRank getRank(String id) {
        return null;
    }

    @Override
    public List<ChangeServerScoreRank> getRanks(int start, int end) {
        return null;
    }

    @Override
    public void clean() {

    }

    @Override
    public void addScore(String id, int score) {

    }

    @Override
    public void remMember(String id) {

    }
}
