package com.hawk.activity.type.impl.greatGift.cfg;

import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/***
 * 超值好礼 宝箱配置
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "activity/super_gift/super_gift_chest.xml")
public class GreatGiftChestCfg extends HawkConfigBase {
	
	@Id
	private final int stageId;
	
	private final String name;
	
	private final int integralOpen;
	
	private final String award;
	
	private List<RewardItem.Builder> rewardList;
	
	public GreatGiftChestCfg(){
		stageId = 0;
		name = "";
		integralOpen = 0;
		award = "";
	}

	@Override
	protected boolean assemble() {
		rewardList = RewardHelper.toRewardItemImmutableList(award);
		return super.assemble();
	}
	
	/***
	 * 获取宝箱奖励
	 * @return
	 */
	public List<RewardItem.Builder> getRewardList(){
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

	public int getStageId() {
		return stageId;
	}

	public String getName() {
		return name;
	}

	public int getIntegralOpen() {
		return integralOpen;
	}

	public String getAward() {
		return award;
	}
}
