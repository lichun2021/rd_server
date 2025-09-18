package com.hawk.activity.type.impl.honourHeroReturn.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * @author richard
 */
@HawkConfigManager.XmlResource(file = "activity/honour_hero_return/honour_hero_return_reward.xml")
public class HonourHeroReturnRewardCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final String awardFixItem;
    private final String awardId;
    public HonourHeroReturnRewardCfg() {
        this.id = 0;
        awardFixItem = "";
        this.awardId = "";
    }

    public int getId() {
        return id;
    }
    public String getAwardId(){
        return awardId;
    }
    public String getAwardFixItem(){
        return awardFixItem;
    }
    @Override
    protected boolean assemble() {
        return super.assemble();
    }
}
