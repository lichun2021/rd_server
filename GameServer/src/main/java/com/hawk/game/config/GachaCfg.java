package com.hawk.game.config;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;

/**
 * 兵种强化
 * 
 * @author lwt
 *
 */
@HawkConfigManager.XmlResource(file = "xml/gacha.xml")
public class GachaCfg extends HawkConfigBase {
	@Id
	protected final int gachaType;
	/** 免费倒计时 */
	protected final int freeTime;// "300"
	protected final int freeTimesLimit;
	protected final String ticketExpend; // "30000_800109_1"
	protected final String ticketPrice;// "10000_1001_300"
	/** 高级掉落伦 */
	protected final int pseudoDropTimes;// ="100"
	protected final int firstTimeGachaPool;
	/** 免费池 */
	protected final int freeGachaPool;// ="10101"
	protected final int normalGachaPoolA;// ="10102"
	protected final int normalGachaPoolB;// ="11003";
	protected final int pseudoDropGachaPool;// ="10104" />
	protected final String buyItem;// =//"30000_1300005_1"
	protected final int countGachaPool;//机甲核心模块抽奖保底奖励池（已废弃）
	
	//活动相关
	protected final int normalGachaPoolAActivity;  //31005
	protected final int normalGachaPoolBActivity;  //31006
	protected final int pseudoDropGachaPoolActivity;  //31006
	protected final int firstTimeGachaPoolActivity;
	
	@Override
	protected boolean assemble() {
		return true;
	}

	@Override
	protected boolean checkValid() {
		if (StringUtils.isNotEmpty(buyItem)) {
			ItemCfg item = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, ItemInfo.valueOf(buyItem).getItemId());
			HawkAssert.notNull(item, "buyItem cant find : " + buyItem);
		}
		{
			if (!HawkOSOperator.isEmptyString(ticketExpend)) {
				ItemInfo item = ItemInfo.valueOf(ticketExpend);
				ItemCfg itemcfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
				HawkAssert.notNull(itemcfg, "ticketExpendItem cant find : " + buyItem);
			}
		}
		return super.checkValid();
	}

	public GachaCfg() {
		this.gachaType = 0;
		this.freeTime = 0;
		this.freeTimesLimit = 0;
		this.ticketExpend = "";
		this.ticketPrice = "";
		this.pseudoDropTimes = 0;
		this.firstTimeGachaPool = 0;
		this.freeGachaPool = 0;
		this.normalGachaPoolA = 0;
		this.normalGachaPoolB = 0;
		this.pseudoDropGachaPool = 0;
		this.countGachaPool = 0;
		this.buyItem = "";
		this.normalGachaPoolAActivity = 0;
		this.normalGachaPoolBActivity = 0;
		this.pseudoDropGachaPoolActivity = 0;
		this.firstTimeGachaPoolActivity = 0;
	}

	public int getGachaType() {
		return gachaType;
	}

	public int getFreeTime() {
		return freeTime;
	}

	public int getFreeTimesLimit() {
		return freeTimesLimit;
	}

	public String getTicketExpend() {
		return ticketExpend;
	}

	public int getPseudoDropTimes() {
		return pseudoDropTimes;
	}

	public int getFirstTimeGachaPool() {
		return firstTimeGachaPool;
	}

	public int getFreeGachaPool() {
		return freeGachaPool;
	}

	public int getNormalGachaPoolA() {
		return normalGachaPoolA;
	}

	public int getPseudoDropGachaPool() {
		return pseudoDropGachaPool;
	}

	public int getNormalGachaPoolB() {
		return normalGachaPoolB;
	}

	public String getBuyItem() {
		return buyItem;
	}

	public String getTicketPrice() {
		return ticketPrice;
	}
	
	public int getCountGachaPool() {
		return countGachaPool;
	}

	public int getNormalGachaPoolAActivity() {
		return normalGachaPoolAActivity;
	}

	public int getNormalGachaPoolBActivity() {
		return normalGachaPoolBActivity;
	}

	public int getPseudoDropGachaPoolActivity() {
		return pseudoDropGachaPoolActivity;
	}

	public int getFirstTimeGachaPoolActivity() {
		return firstTimeGachaPoolActivity;
	}

}
