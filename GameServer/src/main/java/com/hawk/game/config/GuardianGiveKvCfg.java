package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 守护特效赠送.
 * 
 * @author codej
 *
 */
@HawkConfigManager.KVResource(file = "xml/guardian_give.xml")
public class GuardianGiveKvCfg extends HawkConfigBase {
	
	private final int giveLogNum;
	
	private final int getLogNum;
	
	private final String needItem;// 30000_1150078_1;
	
	private final int getCD;
	
	private long getCDMs;
	
	private final String needItem2; //30000_1150097_1
	
	private final String needItem3; //30000_1150097_1
	
	private final int dressSendMsgLen;
	
	private List<ItemInfo> itemList;
	
	private List<ItemInfo> itemList2;
	
	private List<ItemInfo> itemList3;
	public GuardianGiveKvCfg(){
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
	public String getNeedItem() {
		return needItem;
	}
	public int getGetCD() {
		return getCD;
	}
	
	public List<ItemInfo> getItemList() {
		return itemList;
	}
	
	public List<ItemInfo> getItemList2() {
		return itemList2;
	}

	public List<ItemInfo> getItemList3() {
		return itemList3;
	}
	
	public long getGetCDMs() {
		return getCDMs;
	}
	
	@Override
	protected boolean assemble() {
		getCDMs = getCD * 1000;
		itemList = ItemInfo.valueListOf(needItem);
		itemList2 = ItemInfo.valueListOf(needItem2);
		itemList3 = ItemInfo.valueListOf(needItem3);
		return super.assemble();
	}
	public int getDressSendMsgLen() {
		return dressSendMsgLen;
	}
}
