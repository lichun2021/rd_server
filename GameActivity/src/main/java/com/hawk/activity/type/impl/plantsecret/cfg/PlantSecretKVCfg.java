package com.hawk.activity.type.impl.plantsecret.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.List;

/**
 * 泰能机密
 *
 * @author lating
 */
@HawkConfigManager.KVResource(file = "activity/taineng_secret/taineng_secret_cfg.xml")
public class PlantSecretKVCfg extends HawkConfigBase {
    /**
     * 服务器开服延时开启活动时间；单位:秒
     */
    private final int serverDelay;

    // 世界频道分享次数
    private final int worldshare;
    // 世界频道分享时间CD；单位秒
    private final int worldshareCD;

    // 联盟频道分享次数
    private final int allianceshare;
    // 联盟频道分享时间CD；单位秒
    private final int allianceshareCD;

    // 翻牌道具可购买次数
    private final int times;

    // 单个宝箱可开启次数
    private final int opentime;

    // 翻牌道具
    private final String propid;

    // 翻牌道具购买花费(金币)
    private final String price;

    // 活动结束将剩余的翻牌道具转换成此类道具发给玩家
    private final String items;

    //每日开箱上限
    private final int maxboxs;

    public PlantSecretKVCfg() {
        serverDelay = 0;
        worldshare = 0;
        worldshareCD = 0;
        allianceshare = 0;
        allianceshareCD = 0;
        times = 0;
        opentime = 0;
        propid = "";
        price = "";
        items = "";
        maxboxs = 0;
    }

    public String getPropid() {
		return propid;
	}

	public long getServerDelay() {
        return serverDelay * 1000L;
    }

    public String getItems() {
        return items;
    }

    public int getWorldshare() {
        return worldshare;
    }

    public int getWorldshareCD() {
        return worldshareCD;
    }

    public int getAllianceshare() {
        return allianceshare;
    }

    public int getAllianceshareCD() {
        return allianceshareCD;
    }

    public int getTimes() {
        return times;
    }

    public int getOpentime() {
        return opentime;
    }

    public String getPrice() {
        return price;
    }

    public List<RewardItem.Builder> getConsume() {
        return RewardHelper.toRewardItemImmutableList(this.propid);
    }

    public int getMaxboxs() {
        return maxboxs;
    }
}