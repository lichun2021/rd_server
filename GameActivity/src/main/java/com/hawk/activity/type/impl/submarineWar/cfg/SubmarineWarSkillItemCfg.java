package com.hawk.activity.type.impl.submarineWar.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 *  中部养成计划
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/submarine_war/submarine_war_item.xml")
public class SubmarineWarSkillItemCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	private final int effect;
	private final int value;
	private final int buyLimit;
	private final String price;
	private final int useLimit;
	private final int cd;
	
	public SubmarineWarSkillItemCfg() {
		id = 0;
		effect = 0;
		value = 0;
		buyLimit = 0;
		price = "";
		useLimit = 0;
		cd = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}


	
	
	public List<RewardItem.Builder> getBuyCostItem(int milt) {
		List<RewardItem.Builder> list = RewardHelper.toRewardItemImmutableList(this.price);
		for(RewardItem.Builder rb : list){
			long count = rb.getItemCount() * milt;
			rb.setItemCount(count);
		}
		return list;
	}
	
	
	public List<RewardItem.Builder> getGainItem(int milt) {
		List<RewardItem.Builder> list = new ArrayList<>();
		RewardItem.Builder costBuilder = RewardItem.newBuilder();
		costBuilder.setItemType(ItemType.TOOL_VALUE);
		costBuilder.setItemId(this.id);
		costBuilder.setItemCount(milt);
		list.add(costBuilder);
		return list;
	}
	
	public int getBuyLimit() {
		return buyLimit;
	}
	
	
	public int getUseLimit() {
		return useLimit;
	}
	
	public int getCd() {
		return cd;
	}
	
	public int getEffect() {
		return effect;
	}
	

	public int getValue() {
		return value;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	protected final boolean checkValid() {
		RewardItem item =  RewardHelper.toRewardItem(ItemType.TOOL_VALUE, this.id,1).build();
		String str = RewardHelper.toItemString(item);
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(str);
		if (!valid) {
			throw new InvalidParameterException(String.format("SubmarineWarSkillItemCfg id error not in item, id: %s", id));
		}
		return super.checkValid();
	}
	

}
