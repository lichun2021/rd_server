package com.hawk.activity.type.impl.newbietrain.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import java.security.InvalidParameterException;

/**
 * 新兵作训-礼包配置
 */
@HawkConfigManager.XmlResource(file = "activity/newbie_train/%s/newbie_train_gift.xml", autoLoad=false, loadParams="334")
public class NewbieTrainGiftCfg extends HawkConfigBase {
    @Id
    private final int id;
    /**
     * 专服标识（0=非专服,1=专服）
     */
    private final int service;
    /**
     * 1-英雄作训礼包；2-装备作训礼包
     */
    private final int type;
    
    private final String rewards;
    
    private final String price;
    
    private boolean freeGift;

    public NewbieTrainGiftCfg(){
        this.id = 0;
        this.service = 0;
        this.type = 0;
        this.rewards = "";
        this.price = "";
    }

    public int getId() {
        return id;
    }

	public int getType() {
		return type;
	}
	
	public int getService() {
		return service;
	}

	public String getRewards() {
		return rewards;
	}

	public String getPrice() {
		return price;
	}
	
	public boolean isFree() {
		return freeGift;
	}

	@Override
    protected boolean assemble() {
		RewardItem.Builder builder = RewardHelper.toRewardItem(price);
		freeGift = builder.getItemCount() == 0;
		return true;
    }

    @Override
    protected boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
        if (!valid) {
            throw new InvalidParameterException(String.format("NewbieTrainGiftCfg reward error, id: %s , rewards: %s", id, rewards));
        }
        return super.checkValid();
    }
    
}
