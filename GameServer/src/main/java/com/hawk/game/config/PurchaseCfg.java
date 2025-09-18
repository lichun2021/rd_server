package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;

/**
 * 购买钻石附带功能配置
 * 
 * @author
 *
 */
@HawkConfigManager.XmlResource(file = "xml/purchase.xml")
public class PurchaseCfg extends HawkConfigBase {
	/** 购买次数 */
	@Id
	protected final int id;
	/** 对应体力购买次数所需消耗 */
	protected final String buyEnergyDiamonds;
	
	/** 购买精英关卡次数消耗 */
	protected final String buyDungeonTimes;

	/** 重置联盟捐献次数消耗 */
	protected final String refreshcost;
	
	/** 购买体力消耗*/
	protected ItemInfo consumeItem;
	
	/** 购买精英关卡次数消耗*/
	protected ItemInfo dungeonTimesConsume;
	
	/** 重置联盟捐献次数*/
	protected ItemInfo donateRefreshConsume;
	
	private static int maxTime;

	public PurchaseCfg() {
		id = 0;
		buyEnergyDiamonds = "";
		buyDungeonTimes = "";
		refreshcost = "";
	}

	public int getId() {
		return id;
	}
	
	public String getBuyEnergyDiamonds() {
		return buyEnergyDiamonds;
	}
	
	public String getBuyDungeonTimes() {
		return buyDungeonTimes;
	}

	public ItemInfo getConsumeItem() {
		if (consumeItem != null) {
			return consumeItem.clone();
		}
		
		return null;
	}
	
	public ItemInfo getDungeonTimesConsume() {
		if (dungeonTimesConsume != null) {
			return dungeonTimesConsume.clone();
		}
		
		return null;
	}

	public String getRefreshcost() {
		return refreshcost;
	}

	@Override
	protected boolean assemble() {
		if (id > maxTime) {
			maxTime = id;
		}
		
		if(!HawkOSOperator.isEmptyString(buyEnergyDiamonds)) {
			consumeItem = new ItemInfo();
			if (!consumeItem.init(buyEnergyDiamonds)) {
				throw new RuntimeException("purchase cfg error " + id);
			}
		}
		
		if(!HawkOSOperator.isEmptyString(buyDungeonTimes)) {
			dungeonTimesConsume = new ItemInfo();
			if (!dungeonTimesConsume.init(buyDungeonTimes)) {
				throw new RuntimeException("purchase cfg error " + id);
			}
		}
		
		if(!HawkOSOperator.isEmptyString(refreshcost)) {
			donateRefreshConsume = new ItemInfo();
			if (!donateRefreshConsume.init(refreshcost)) {
				throw new RuntimeException("purchase cfg error " + id);
			}
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		return true;
	}
	
	public static int getMaxTime() {
		return maxTime;
	}
}
