package com.hawk.activity.type.impl.heavenBlessing.cfg;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/heaven_blessing/heaven_blessing_level_cfg.xml")
public class HeavenBlessingLevelCfg extends HawkConfigBase {

    /**
     * 唯一ID
     */
    @Id
    private final int id;
    /**
     * 安卓礼包id
     */
    private final int androidPayId;
    /**
     * ios礼包id
     */
    private final int iosPayId;
    /**
     * 自定义奖励配置
     */
    private final String customAward;
    /**
     * 自定义奖励列表，选择部分
     */
    private List<Reward.RewardItem.Builder> customAwards;
    /**
     * 自定义选择列表，固定部分
     */
    private List<Reward.RewardItem.Builder> constantAwards;
    /**
     * 随机奖励成就id配置
     */
    private final String randomAward;
    /**
     * 随机奖励成就id列表
     */
    private List<Integer> randomAwards;
    /**
     * 折扣
     */
    private final float discount;

    public HeavenBlessingLevelCfg(){
        id = 0;
        androidPayId = 0;
        iosPayId = 0;
        customAward = "";
        randomAward = "";
        discount = 0f;
    }

    @Override
    protected boolean assemble() {
        //分割随机奖励成就配置
        this.randomAwards = ImmutableList.copyOf(SerializeHelper.cfgStr2List(randomAward, SerializeHelper.BETWEEN_ITEMS));
        //分割自定义奖励可选和固定部分
        String [] arr = customAward.split(";");
        if(arr.length != 2){
            return false;
        }
        //把字符串转换成奖励
        this.customAwards = RewardHelper.toRewardItemImmutableList(arr[0]);
        this.constantAwards = RewardHelper.toRewardItemImmutableList(arr[1]);
        return true;
    }

    public int getAndroidPayId() {
        return androidPayId;
    }

    public int getIosPayId() {
        return iosPayId;
    }

    public List<Reward.RewardItem.Builder> getCustomAwards() {
        return customAwards;
    }

    public List<Reward.RewardItem.Builder> getConstantAwards() {
        return constantAwards;
    }

    public List<Integer> getRandomAwards() {
        return randomAwards;
    }
}
