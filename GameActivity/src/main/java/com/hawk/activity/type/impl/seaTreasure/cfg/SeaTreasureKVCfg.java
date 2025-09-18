package com.hawk.activity.type.impl.seaTreasure.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 秘海珍寻
 * @author Golden
 *
 */
@HawkConfigManager.KVResource(file = "activity/sea_treasure/sea_treasure_cfg.xml")
public class SeaTreasureKVCfg extends HawkConfigBase {

	/**
	 * 起服延迟开放时间
	 */
	private final int serverDelay;
	
	/**
	 * 单日寻宝次数
	 */
	private final int treasureNumber;
	
	/**
	 * 箱栏位数
	 */
	private final int boxNumeber;
	
	/**
	 * 加速道具可购买次数
	 */
	private final int accelerateNumber;
	
	/**
	 * 加速道具购买花费
	 */
	private final String cost;
	
	/**
	 * 个加速道具加速时间(秒)
	 */
	private final int accelerateTime;
	
	/**
	 * 加速道具物品ID
	 */
	private final String accelerateItemId;
	
	/**
	 * 单例
	 */
	private static SeaTreasureKVCfg instance;
	
	/**
	 * 获取单例
	 * @return
	 */
	public static SeaTreasureKVCfg getInstance(){
		return instance;
	}
	
	/**
	 * 构造
	 */
	public SeaTreasureKVCfg(){
		serverDelay = 0;
		treasureNumber = 0;
		boxNumeber = 0;
		accelerateNumber = 0;
		cost = "";
		accelerateTime = 0;
		accelerateItemId = "";
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getTreasureNumber() {
		return treasureNumber;
	}

	public int getBoxNumeber() {
		return boxNumeber;
	}

	public int getAccelerateNumber() {
		return accelerateNumber;
	}

	@SuppressWarnings("deprecation")
	public List<RewardItem.Builder> getCost() {
		return RewardHelper.toRewardItemList(cost);
	}

	public long getAccelerateTime() {
		return accelerateTime * 1000L;
	}

	@SuppressWarnings("deprecation")
	public List<RewardItem.Builder> getAccelerateItemId() {
		return RewardHelper.toRewardItemList(accelerateItemId);
	}
}
