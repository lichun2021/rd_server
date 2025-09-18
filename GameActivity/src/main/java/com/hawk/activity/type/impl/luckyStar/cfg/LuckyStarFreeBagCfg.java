package com.hawk.activity.type.impl.luckyStar.cfg;

import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/lucky_star/lucky_star_gift.xml")
public class LuckyStarFreeBagCfg extends HawkConfigBase {
	
	@Id
	private final String id;
	
	/** 奖励id，多个用逗号分割 **/
	private final String award;
	
	private List<RewardItem.Builder> rewardList;
	
	public LuckyStarFreeBagCfg(){
		this.id = "";
		this.award = "";
	}
	
	@Override
	protected boolean assemble() {
		rewardList = RewardHelper.toRewardItemImmutableList(award);
		return super.assemble();
	}

	public String getId() {
		return id;
	}
	
	public String getAward() {
		return award;
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
	
	@Override
	protected boolean checkValid() {
		//需要验证奖励是否合法
		return ConfigChecker.getDefaultChecker().checkAwardsValid(award);
	}
	
	
}
