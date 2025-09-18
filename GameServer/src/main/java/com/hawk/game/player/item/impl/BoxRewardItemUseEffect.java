package com.hawk.game.player.item.impl;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class BoxRewardItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.BOX_CHOOSE_REWARD_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		int index = Integer.parseInt(targetId);
		index = Math.max(0, index - 1);
		if (itemCfg.getChooseAward(index) == null) {
			player.sendError(protoType, Status.SysError.PARAMS_INVALID_VALUE, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		int index = Integer.parseInt(targetId);
		index = Math.max(0, index - 1);
		ItemInfo itemInfo = itemCfg.getChooseAward(index);
		itemInfo.setCount(itemInfo.getCount() * itemCount);
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(itemInfo);
		awardItem.rewardTakeAffectAndPush(player, Action.USE_CHOOSE_AWARD_ITEM, true);
		return true;
	}

}
