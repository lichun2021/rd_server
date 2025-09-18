package com.hawk.activity.type.impl.hiddenTreasure.cfg;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**勋章宝藏kv配置表
 * @author Winder
 *
 */
@HawkConfigManager.KVResource(file = "activity/hidden_treasure/hidden_treasure_cfg.xml")
public class HiddenTreasureActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	// # 免费刷新CD（s）
	private final int freeRefreshTimes;// = 14400

	// # 刷新次数-每日
	private final int refreshTimes;// = 100

	// # 刷新消耗道具
	private final String refreshCost;// = 30000_2980001_30

	// # 单个宝箱开启花费道具
	private final String openTreasureCost;// = 30000_2980001_40

	// # 道具购买花费
	private final String purchaseItemCost;// = 10000_1000_10

	// # 道具可购买次数
	private final int purchaseItemTimes;// = 30

	// # 道具ID
	private final String accelerateItemId;// = 30000_2980001_1

	// # 首次抽取宝箱ID
	private final String firstExtraction;// = 1,1,2,2,2,3,4,5,6

	// # 称号id. 额外得奖励
	private final int modelType;// = 7788

	// # 滚动条物品. >= 全服滚动播报
	private final String noticeItem;// = 30000_2980001_100

	private ImmutableList<Integer> firstExtractionList;
	private RewardItem noticeItemPB;

	public HiddenTreasureActivityKVCfg() {
		serverDelay = 0;
		freeRefreshTimes = 14400;

		refreshTimes = 100;

		refreshCost = "30000_2980001_30";

		openTreasureCost = "30000_2980001_40";

		purchaseItemCost = "10000_1000_10";

		purchaseItemTimes = 30;

		accelerateItemId = "30000_2980001_1";

		firstExtraction = "1,1,2,2,2,3,4,5,6";
		modelType = 7788;
		noticeItem = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	@Override
	protected boolean assemble() {
		noticeItemPB = RewardHelper.toRewardItem(noticeItem).build();
		firstExtractionList = ImmutableList.copyOf(Splitter.on(",").omitEmptyStrings().splitToList(firstExtraction).stream().map(Integer::valueOf).collect(Collectors.toList()));
		return true;
	}

	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(accelerateItemId);
		if (!valid) {
			throw new InvalidParameterException(String.format("HiddenTreasureActivityKVCfg reward error, accelerateItemId: %s", accelerateItemId));
		}
		return super.checkValid();
	}

	public ImmutableList<Integer> getFirstExtractionList() {
		return firstExtractionList;
	}

	public void setFirstExtractionList(List<Integer> firstExtractionList) {
		// this.firstExtractionList = firstExtractionList;
	}

	public int getFreeRefreshTimes() {
		return freeRefreshTimes;
	}

	public int getRefreshTimes() {
		return refreshTimes;
	}

	public String getRefreshCost() {
		return refreshCost;
	}

	public String getOpenTreasureCost() {
		return openTreasureCost;
	}

	public String getPurchaseItemCost() {
		return purchaseItemCost;
	}

	public int getPurchaseItemTimes() {
		return purchaseItemTimes;
	}

	public String getAccelerateItemId() {
		return accelerateItemId;
	}

	public String getFirstExtraction() {
		return firstExtraction;
	}

	public void setFirstExtractionList(ImmutableList<Integer> firstExtractionList) {
		this.firstExtractionList = firstExtractionList;
	}

	public int getModelType() {
		return modelType;
	}

	public String getNoticeItem() {
		return noticeItem;
	}

	public RewardItem getNoticeItemPB() {
		return noticeItemPB;
	}

	public void setNoticeItemPB(RewardItem noticeItemPB) {
		this.noticeItemPB = noticeItemPB;
	}

}