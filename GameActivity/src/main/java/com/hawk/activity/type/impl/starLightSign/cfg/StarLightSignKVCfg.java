package com.hawk.activity.type.impl.starLightSign.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

@HawkConfigManager.KVResource(file = "activity/starlight_sign/starlight_cfg.xml")
public class StarLightSignKVCfg extends HawkConfigBase {
    private final long serverDelay;

    private final String buymission;

    private final String buysign;

    private final String buyIncremental;

    private final String Maximummagnification;

    private final int smailOdds;

    private List<Reward.RewardItem.Builder> buymissionNeed;

    private List<Reward.RewardItem.Builder> buysignNeed;

    private List<Reward.RewardItem.Builder> buyIncrementalNeed;

    private Map<Integer, Integer> MaximummagnificationMap;

    public StarLightSignKVCfg(){
        serverDelay = 0;
        buymission = "";
        buysign = "";
        buyIncremental = "";
        Maximummagnification = "";
        smailOdds = 20000;
    }

    @Override
    protected boolean assemble() {
        try {
            buymissionNeed = RewardHelper.toRewardItemImmutableList(buymission);
            buysignNeed = RewardHelper.toRewardItemImmutableList(buysign);
            buyIncrementalNeed = RewardHelper.toRewardItemImmutableList(buyIncremental);
            MaximummagnificationMap = SerializeHelper.stringToMap(Maximummagnification,
                    Integer.class, Integer.class,SerializeHelper.ATTRIBUTE_SPLIT,SerializeHelper.BETWEEN_ITEMS);
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    @Override
    protected final boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(buymission);
        valid = valid && ConfigChecker.getDefaultChecker().checkAwardsValid(buysign);
        valid = valid && ConfigChecker.getDefaultChecker().checkAwardsValid(buyIncremental);
        if (!valid) {
            throw new InvalidParameterException(String.format("SeasonKingAwardCfg reward error"));
        }
        return super.checkValid();
    }

    public long getServerDelay() {
        return serverDelay * 1000L;
    }

    public int getSmailOdds() {
        return smailOdds;
    }

    public List<Reward.RewardItem.Builder> getBuymissionNeed() {
        return buymissionNeed;
    }

    public List<Reward.RewardItem.Builder> getBuysignNeed() {
        return buysignNeed;
    }

    public List<Reward.RewardItem.Builder> getBuyIncrementalNeed() {
        return buyIncrementalNeed;
    }

    public Map<Integer, Integer> getMaximummagnificationMap() {
        return MaximummagnificationMap;
    }
}
