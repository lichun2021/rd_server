package com.hawk.activity.type.impl.plantFortress.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 定制礼包活动配置
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/tiberium_fortress/tiberium_fortress_cfg.xml")
public class PlantFortressKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;
	
	private final String costItem;
	
	//购买道具
	private final String buyItem;
	//购买消耗
	private final String priceItem;

	private final int buyLimitCount;

	private RewardItem.Builder buyItemBuilder;
	private RewardItem.Builder priceItemBuilder;

	public PlantFortressKVCfg() {
		serverDelay = 0;
		costItem = "";
		buyItem = "";
		priceItem = "";
		buyLimitCount = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getCostItem() {
		return costItem;
	}
	
	
	public int getBuyLimitCount() {
		return buyLimitCount;
	}

	public RewardItem.Builder getBuyItemBuilder() {
		return this.buyItemBuilder.clone();
	}

	public RewardItem.Builder getPriceItemBuilder() {
		return this.priceItemBuilder.clone();
	}


	@Override
	protected boolean assemble() {
		try {
			if(HawkOSOperator.isEmptyString(buyItem)){
				return false;
			}
			if(HawkOSOperator.isEmptyString(priceItem)){
				return false;
			}
			buyItemBuilder = RewardHelper.toRewardItem(buyItem);
			priceItemBuilder = RewardHelper.toRewardItem(priceItem);
		} catch (Exception e) {
			HawkLog.errPrintln("assemble error! file: {}", this.getClass().getAnnotation(XmlResource.class).file());
			HawkException.catchException(e);
			return false;
		}
		return super.assemble();
	}
	
}
