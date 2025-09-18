package com.hawk.game.config;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.hawk.game.item.ItemInfo;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import java.util.ArrayList;
import java.util.List;

@HawkConfigManager.XmlResource(file = "xml/xhjz_league_rank_reward.xml")
public class XHJZSeasonRankRewrdCfg extends HawkConfigBase {
    /** 活动期数*/
    @Id
    private final int id;
    private final int division;
    private final int rankMin;
    private final int rankMax;
    private final String reward;
    private final int honorPoint;

    /**
     * 任务奖励列表
     */
    private List<ItemInfo> rewardItems;

    private static RangeMap<Integer, XHJZSeasonRankRewrdCfg> rangeMap1 = TreeRangeMap.create();
    private static RangeMap<Integer, XHJZSeasonRankRewrdCfg> rangeMap2 = TreeRangeMap.create();

    public XHJZSeasonRankRewrdCfg(){
        id = 0;
        division = 0;
        rankMin = 0;
        rankMax = 0;
        reward = "";
        honorPoint = 0;

    }

    public int getId() {
        return id;
    }

    public int getDivision() {
        return division;
    }

    public int getRankMin() {
        return rankMin;
    }

    public int getRankMax() {
        return rankMax;
    }

    public List<ItemInfo> getRewardItem() {
        List<ItemInfo> copy = new ArrayList<>();
        for (ItemInfo item : rewardItems) {
            copy.add(item.clone());
        }
        return copy;
    }

    protected boolean assemble() {
        this.rewardItems = ItemInfo.valueListOf(this.reward);
        return true;
    }

    public static boolean doAssemble() {
        RangeMap<Integer, XHJZSeasonRankRewrdCfg> tmpRangeMap1 = TreeRangeMap.create();
        RangeMap<Integer, XHJZSeasonRankRewrdCfg> tmpRangeMap2 = TreeRangeMap.create();
        ConfigIterator<XHJZSeasonRankRewrdCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(XHJZSeasonRankRewrdCfg.class);
        for(XHJZSeasonRankRewrdCfg cfg : iterator){
            if(cfg.getDivision() == 1){
                tmpRangeMap1.put(Range.closed(cfg.getRankMin(), cfg.getRankMax()), cfg);
            }
            if(cfg.getDivision() == 2){
                tmpRangeMap2.put(Range.closed(cfg.getRankMin(), cfg.getRankMax()), cfg);
            }
        }
        rangeMap1 = tmpRangeMap1;
        rangeMap2 = tmpRangeMap2;
        return true;
    }

    public static XHJZSeasonRankRewrdCfg getRankCfg(int division, int rank){
        if(division == 1){
            return rangeMap1.get(rank);
        }else {
            return rangeMap2.get(rank);
        }
    }
}
