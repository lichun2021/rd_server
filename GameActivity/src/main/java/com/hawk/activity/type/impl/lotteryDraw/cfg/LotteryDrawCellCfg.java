package com.hawk.activity.type.impl.lotteryDraw.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 十连抽活动格子配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/lottery_draw/lottery_draw_cell_cfg.xml")
public class LotteryDrawCellCfg extends HawkConfigBase {
	/** 格子id*/
	@Id
	private final int id;
	
	/** 格子奖励*/
	private final String rewards;
	
	/** 单抽权重*/
	private final int singleWeight;
	
	/** 十连抽权重*/
	private final int tenWeight;
	
	private List<RewardItem.Builder> rewardList;
	
	public LotteryDrawCellCfg() {
		id = 0;
		rewards = "";
		singleWeight = 0;
		tenWeight = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("LotteryDrawCellCfg reward error, id: %s , rewards: %s", id, rewards));
		}
		return super.checkValid();
	}
	
	public int getId() {
		return id;
	}

	public List<RewardItem.Builder> getRewardList() {
		List<RewardItem.Builder> list = new ArrayList<>();
		for(RewardItem.Builder builder : rewardList){
			RewardItem.Builder clone = RewardItem.newBuilder();
			clone.setItemId(builder.getItemId());
			clone.setItemType(builder.getItemType());
			clone.setItemCount(builder.getItemCount());
			list.add(clone);
		}
		return list;
	}

	public int getSingleWeight() {
		return singleWeight;
	}

	public int getTenWeight() {
		return tenWeight;
	}

}
