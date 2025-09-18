package com.hawk.game.activity;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.EquipmentCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.gamelib.activity.ConfigChecker;

public class ActivityConfigChecker extends ConfigChecker{

	@Override
	public boolean checkAwardsValid(String awardsStr) {
		List<ItemInfo> list = ItemInfo.valueListOf(awardsStr);
		for(ItemInfo itemInfo : list){
			ItemType itemType = itemInfo.getItemType();
			int itemId = itemInfo.getItemId();
			if(itemType == null){
				return false;
			}
			switch (itemType) {
			case PLAYER_ATTR:
				PlayerAttr attr = PlayerAttr.valueOf(itemId);
				if (attr == null) {
					return false;
				}
				break;
			case TOOL:
				if (!ItemCfg.isExistItemId(itemId)) {
					return false;
				}
				break;
			case EQUIP:
				if (!EquipmentCfg.isCfgExist(itemId)) {
					return false;
				}
				break;
			case SOLDIER:
				if (!BattleSoldierCfg.isCfgExist(itemId)) {
					return false;
				}
				break;

			default:
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkPayGiftValid(String giftId, String channelType) {
		PayGiftCfg config = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, giftId);
		if(config != null && config.getChannelType().equalsIgnoreCase(channelType)){
			return true;
		}
		return false;
	}

	@Override
	public boolean chectAwardIdValid(int awardId) {
		AwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
		if(cfg == null){
			return false;
		}
		return true;
	}

}
