package com.hawk.activity.type.impl.growUpBoost.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.Action;

/**
 *  中部养成计划
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/grow_up_boost/grow_up_boost_round.xml")
public class GrowUpBoostScoreCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	// 加分道具1
	private final String addScoreItem1;
	// 加分道具2
	private final String addScoreItem2;
	// 加分道具3
	private final String addScoreItem3;
	// 加分道具4
	private final String addScoreItem4;
	// 加分道具5
	private final String addScoreItem5;
	// 道具1每个加分
	private final int itemScoreValue1;
	// 道具2每个加分
	private final int itemScoreValue2;
	// 道具3每个加分
	private final int itemScoreValue3;
	// 道具4每个加分
	private final int itemScoreValue4;
	// 道具5每个加分
	private final int itemScoreValue5;																				
	// 1层兑换额外礼品
	private final String exchangeGiftPage1;
	// 2层兑换额外礼品
	private final String exchangeGiftPage2;
	// 3层兑换额外礼品
	private final String exchangeGiftPage3;
	// 4层兑换额外礼品
	private final String exchangeGiftPage4;
	// 5层兑换额外礼品
	private final String exchangeGiftPage5;
	
	private Map<Integer,Integer> itemScoreMap;
	
	
	private static Set<Integer> itemSet = new HashSet<Integer>();
	private static Set<Integer> actions = new HashSet<Integer>();
	public GrowUpBoostScoreCfg() {
		id = 0;
		addScoreItem1 = "";
		addScoreItem2 = "";
		addScoreItem3 = "";
		addScoreItem4 = "";
		addScoreItem5 = "";
		itemScoreValue1 = 0;
		itemScoreValue2 = 0;
		itemScoreValue3 = 0;
		itemScoreValue4 = 0;
		itemScoreValue5 = 0;																				
		exchangeGiftPage1 = "";
		exchangeGiftPage2 = "";
		exchangeGiftPage3 = "";
		exchangeGiftPage4 = "";
		exchangeGiftPage5 = "";
		
	}
	
	@Override
	protected boolean assemble() {
		Map<Integer,Integer> itemScoreMapTemp = new HashMap<>();
		RewardItem.Builder addScoreItembuilder1 = RewardHelper.toRewardItem(addScoreItem1);
		RewardItem.Builder addScoreItembuilder2 = RewardHelper.toRewardItem(addScoreItem2);
		RewardItem.Builder addScoreItembuilder3 = RewardHelper.toRewardItem(addScoreItem3);
		RewardItem.Builder addScoreItembuilder4 = RewardHelper.toRewardItem(addScoreItem4);
		RewardItem.Builder addScoreItembuilder5 = RewardHelper.toRewardItem(addScoreItem5);
		if(Objects.nonNull(addScoreItembuilder1)){
			itemScoreMapTemp.put(addScoreItembuilder1.getItemId(), itemScoreValue1);
			itemSet.add(addScoreItembuilder1.getItemId());
		}
		if(Objects.nonNull(addScoreItembuilder2)){
			itemScoreMapTemp.put(addScoreItembuilder2.getItemId(), itemScoreValue2);
			itemSet.add(addScoreItembuilder2.getItemId());
		}
		if(Objects.nonNull(addScoreItembuilder3)){
			itemScoreMapTemp.put(addScoreItembuilder3.getItemId(), itemScoreValue3);
			itemSet.add(addScoreItembuilder3.getItemId());
		}
		if(Objects.nonNull(addScoreItembuilder4)){
			itemScoreMapTemp.put(addScoreItembuilder4.getItemId(), itemScoreValue4);
			itemSet.add(addScoreItembuilder4.getItemId());
		}
		if(Objects.nonNull(addScoreItembuilder5)){
			itemScoreMapTemp.put(addScoreItembuilder5.getItemId(), itemScoreValue5);
			itemSet.add(addScoreItembuilder5.getItemId());
		}
		this.itemScoreMap = itemScoreMapTemp;
		
		actions.add(Action.PLANT_TECH_UPGRADE.intItemVal());
		actions.add(Action.PLANT_TECH_CHIP_UPGRADE.intItemVal());
		actions.add(Action.PLANT_INSTRUMENT_CHIP_UPGRADE.intItemVal());
		actions.add(Action.PLANT_SOLDIER_CRACK_UPGRADE.intItemVal());
		actions.add(Action.PLANT_SOLDIER_CRACK_UPGRADE_CHIP.intItemVal());
		actions.add(Action.PLANT_CRYSTALANALYSIS_CHIP_UPGRADE.intItemVal());
		actions.add(Action.PLANT_SOLDIERSTRENGTHEN_TECH_UPGRADE.intItemVal());
		actions.add(Action.PLANT_SCIENCE_LEVEL_UP.intItemVal());
		actions.add(Action.PLANTSOLDIER_MILITARY_UPGRADE.intItemVal());
		actions.add(Action.GROW_UP_BOOST_BUY_COST.intItemVal());
		actions.add(Action.ARMOUR_QUANTUM_UP.intItemVal());
		actions.add(Action.EQUIP_RESEARCH_LEVEL_UP.intItemVal());
		
		actions.add(Action.ARMOUR_STAR_ATTR_CHARGE.intItemVal());
		actions.add(Action.ARMOUR_STAR_UP.intItemVal());
		//机甲赋能
		actions.add(Action.SUPER_SOLDIER_ENERGY_LEVEL_UP.intItemVal());
		//建筑升级
		actions.add(Action.PLAYER_BUILDING_UPGRADE.intItemVal());
		//性能探索 星能跃迁
		actions.add(Action.ARMOUR_STAR_EXPLORE_UP_ONCE.intItemVal());
		actions.add(Action.ARMOUR_STAR_EXPLORE_JUMP.intItemVal());
		//超武
		actions.add(Action.MANHATTAN_BASE_STAGE_UP.intItemVal());
		actions.add(Action.MANHATTAN_BASE_LEVEL_UP.intItemVal());
		actions.add(Action.MANHATTAN_SW_UNLOCK.intItemVal());
		actions.add(Action.MANHATTAN_SW_STAGE_UP.intItemVal());
		actions.add(Action.MANHATTAN_SW_LEVEL_UP.intItemVal());
		//泰能强化
		//actions.add(Action.PLANTSOLDIER_MILITARY_UPGRADE.intItemVal());
		
		//actions.add(Action.TECHNOLOGY_LEVEL_UP.intItemVal());
		//actions.add(Action.PLANT_SOLDIER_ADVANCE.intItemVal());
		return true;
	}

	public int getId() {
		return id;
	}


	public String getExchangeGiftPage1() {
		return exchangeGiftPage1;
	}

	public String getExchangeGiftPage2() {
		return exchangeGiftPage2;
	}

	public String getExchangeGiftPage3() {
		return exchangeGiftPage3;
	}

	public String getExchangeGiftPage4() {
		return exchangeGiftPage4;
	}

	public String getExchangeGiftPage5() {
		return exchangeGiftPage5;
	}
	
	public int getItemAddScore(int itemId) {
		return itemScoreMap.getOrDefault(itemId, 0);
	}
	
	
	public List<RewardItem.Builder> getExchangeGainItem(int group) {
		switch (group) {
		case 1:return RewardHelper.toRewardItemImmutableList(this.exchangeGiftPage1);
		case 2:return RewardHelper.toRewardItemImmutableList(this.exchangeGiftPage2);
		case 3:return RewardHelper.toRewardItemImmutableList(this.exchangeGiftPage3);
		case 4:return RewardHelper.toRewardItemImmutableList(this.exchangeGiftPage4);
		case 5:return RewardHelper.toRewardItemImmutableList(this.exchangeGiftPage5);
		default:return RewardHelper.toRewardItemImmutableList("");
		}
		
	} 
	
	
	public static boolean useAddScore(Action action,int itemId) {
		if(!actions.contains(action.intItemVal())){
			return false;
		}
		if (!itemSet.contains(itemId)) {
			return false;
		}
		return true;
	}
	


	

}
