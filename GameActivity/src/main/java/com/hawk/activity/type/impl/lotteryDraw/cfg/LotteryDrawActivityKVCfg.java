package com.hawk.activity.type.impl.lotteryDraw.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 十连抽活动全局K-V配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/lottery_draw/lottery_draw_activity_cfg.xml")
public class LotteryDrawActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	/** 单抽消耗*/
	private final String singlePrice;
	
	/** 十连抽消耗*/
	private final String tenPrice;
	
	/** 单个道具价格*/
	private final String itemPrice;
	
	/** 保底奖励格*/
	private final int ensureCellId;
	
	/** 十连抽抽中下列格子时不走保底*/
	private final String noEnsureIds;
	
	/** 购买1次获得固定奖励*/
	private final String extReward;
	
	private RewardItem.Builder singleConsume;
	
	private RewardItem.Builder tenConsume;
	
	private RewardItem.Builder itemConsume;
	
	private List<RewardItem.Builder> extRewardItems;
	
	private List<Integer> noEnsureIdList;
	
	public LotteryDrawActivityKVCfg() {
		serverDelay = 0;
		singlePrice = "";
		tenPrice = "";
		itemPrice = "";
		ensureCellId = 0;
		noEnsureIds = "";
		extReward = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getSinglePrice() {
		return singlePrice;
	}

	public String getTenPrice() {
		return tenPrice;
	}
	
	public String getItemPrice() {
		return itemPrice;
	}

	public int getEnsureCellId() {
		return ensureCellId;
	}
	
	public RewardItem.Builder getSingleConsume() {
		return singleConsume.clone();
	}

	public RewardItem.Builder getTenConsume() {
		return tenConsume.clone();
	}

	public RewardItem.Builder getItemConsume() {
		return itemConsume.clone();
	}
	
	public List<Integer> getNoEnsureIdList() {
		return noEnsureIdList;
	}
	

	public List<RewardItem.Builder> getExtRewardItems() {
		List<RewardItem.Builder> copy = new ArrayList<>();
		for(RewardItem.Builder builder : extRewardItems){
			copy.add(builder.clone());
		}
		return copy;
	}

	@Override
	protected boolean assemble() {
		singleConsume = RewardHelper.toRewardItem(singlePrice);
		tenConsume = RewardHelper.toRewardItem(tenPrice);
		itemConsume = RewardHelper.toRewardItem(itemPrice);
		extRewardItems = RewardHelper.toRewardItemImmutableList(extReward);
		if (singleConsume == null || tenConsume == null || itemConsume == null) {
			return false;
		}
		noEnsureIdList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(noEnsureIds)) {
			for (String id : noEnsureIds.split(",")) {
				noEnsureIdList.add(Integer.valueOf(id));
			}
		}
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		LotteryDrawCellCfg cellCfg = HawkConfigManager.getInstance().getConfigByKey(LotteryDrawCellCfg.class, ensureCellId);
		if(cellCfg == null){
			return false;
		}
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(singlePrice);
		if (!valid) {
			throw new InvalidParameterException(String.format("LotteryDrawActivityKVCfg reward error, singlePrice: %s", singlePrice));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(tenPrice);
		if (!valid) {
			throw new InvalidParameterException(String.format("LotteryDrawActivityKVCfg reward error, tenPrice: %s", tenPrice));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(itemPrice);
		if (!valid) {
			throw new InvalidParameterException(String.format("LotteryDrawActivityKVCfg reward error, itemPrice: %s", itemPrice));
		}
		return super.checkValid();
	}
	
}