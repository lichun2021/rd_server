package com.hawk.game.player.item.impl;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.VipCfg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.log.Action;

public class TalentSwitchItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.TALENT_SWITCH_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, player.getVipLevel());
		if(vipCfg == null || vipCfg.getFreeToExchangeTalent() != 1) {
			HawkLog.errPrintln("use item, switch item use condition, playerId: {}, itemId: {}, vipLevel: {}, protocol: {}", player.getId(), itemId, player.getVipLevel(), protoType);
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addGold(itemCfg.getSellPrice() * itemCount);
		awardItem.rewardTakeAffectAndPush(player, Action.TALENT_SWITCH_ITEM_COMPENSATION, true);
		return true;
	}

}
