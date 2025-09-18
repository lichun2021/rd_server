package com.hawk.game.module.lianmengyqzz.march.cfg;

import com.hawk.game.item.ItemInfo;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.List;

@HawkConfigManager.XmlResource(file = "xml/moon_war_season_guild_award.xml")
public class YQZZSeasonGuildAwardCfg  extends HawkConfigBase {
    @Id
    private final int id;
    private final int score;
    private final String award;
    private List<ItemInfo> rewardItems;

    public YQZZSeasonGuildAwardCfg() {
        id = 0;
        score = 0;
        award = "";
    }

    @Override
    protected boolean assemble() {
        this.rewardItems = ItemInfo.valueListOf(this.award);
        return true;
    }

    public int getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public List<ItemInfo> getRewardItems() {
        return rewardItems;
    }
}
