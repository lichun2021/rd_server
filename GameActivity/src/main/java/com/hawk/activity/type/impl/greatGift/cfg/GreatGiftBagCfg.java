package com.hawk.activity.type.impl.greatGift.cfg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.gamelib.activity.ConfigChecker;

/***
 * 超值好礼 礼包配置
 * @author yang.rao
 *
 */
@HawkConfigManager.XmlResource(file = "activity/super_gift/super_gift_bag.xml")
public class GreatGiftBagCfg extends HawkConfigBase {
	
	@Id
	private final String giftId;
	
	/** 宝箱的阶数 **/
	private final int giftStage;
	
	/** 平台 **/
	private final String channelType;
	
	private static List<GreatGiftBagCfg> androidSortedStageGiftList = new ArrayList<GreatGiftBagCfg>();
	private static List<GreatGiftBagCfg> iosSortedStageGiftList = new ArrayList<GreatGiftBagCfg>();
	
	public GreatGiftBagCfg(){
		giftId = "";
		giftStage = 0;
		channelType = "";
	}

	public String getGiftId() {
		return giftId;
	}

	public int getGiftStage() {
		return giftStage;
	}

	public String getChannelType() {
		return channelType.trim();
	}
	
	protected boolean assemble() {
		if ("android".equals(channelType.trim())) {
			androidSortedStageGiftList.add(this);
		} else {
			iosSortedStageGiftList.add(this);
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		androidSortedStageGiftList.sort(new Comparator<GreatGiftBagCfg>() {
            @Override
            public int compare(GreatGiftBagCfg cfg1, GreatGiftBagCfg cfg2) {
            	return cfg1.getGiftStage() - cfg2.getGiftStage();
            }
        });
		
		iosSortedStageGiftList.sort(new Comparator<GreatGiftBagCfg>() {
            @Override
            public int compare(GreatGiftBagCfg cfg1, GreatGiftBagCfg cfg2) {
            	return cfg1.getGiftStage() - cfg2.getGiftStage();
            }
        });
		
		return ConfigChecker.getDefaultChecker().checkPayGiftValid(giftId, channelType);
	}
	
	public static List<GreatGiftBagCfg> getSortedCfgList(String channelType) {
		if ("android".equals(channelType.trim())) {
			return androidSortedStageGiftList;
		}
		
		return iosSortedStageGiftList;
	}
}
