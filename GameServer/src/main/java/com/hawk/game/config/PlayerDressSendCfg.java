package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 装扮赠送
 * 
 * @author RickMei
 *
 */
@HawkConfigManager.KVResource(file = "xml/skin_give.xml")
public class PlayerDressSendCfg extends HawkConfigBase {
	
	private final int giveLogNum;
	
	private final int getLogNum;
	
	private final String needItem;// 30000_1150078_1;
	
	private final int getCD;
	
	private long getCDMs;
	
	private final String needItem2; //30000_1150097_1
	
	private final String needItem3; //30000_1150097_1
	
	private final int dressSendMsgLen;
	
	private List<RewardItem.Builder> itemList;
	
	private List<RewardItem.Builder> itemList2;
	
	private List<RewardItem.Builder> itemList3;
	public PlayerDressSendCfg(){
		giveLogNum = 10;
		getLogNum = 10;
		needItem = "30000_1150078_1";
		needItem2 = "30000_1150097_1";
		needItem3 = "30000_1150097_1";
		getCD = 60;
		getCDMs = 0;
		dressSendMsgLen = 60;
	}
	public int getGiveLogNum() {
		return giveLogNum;
	}
	public int getGetLogNum() {
		return getLogNum;
	}
	public List<ItemInfo> getNeedItem() {
		return ItemInfo.valueListOf(needItem);
	}
	public List<ItemInfo> getNeedItem2() {
		return ItemInfo.valueListOf(needItem2);
	}
	public List<ItemInfo> getNeedItem3() {
		return ItemInfo.valueListOf(needItem3);
	}
	public int getGetCD() {
		return getCD;
	}
	
	public List<RewardItem.Builder> getItemList() {
		return itemList;
	}
	
	public List<RewardItem.Builder> getItemList2() {
		return itemList2;
	}

	public List<RewardItem.Builder> getItemList3() {
		return itemList3;
	}
	
	public long getGetCDMs() {
		return getCDMs;
	}
	
	@Override
	protected boolean assemble() {
		getCDMs = getCD * 1000;
		itemList = RewardHelper.toRewardItemImmutableList(needItem);
		itemList2 = RewardHelper.toRewardItemImmutableList(needItem2);
		itemList3 = RewardHelper.toRewardItemImmutableList(needItem3);
		return super.assemble();
	}
	public int getDressSendMsgLen() {
		return dressSendMsgLen;
	}
	
}
