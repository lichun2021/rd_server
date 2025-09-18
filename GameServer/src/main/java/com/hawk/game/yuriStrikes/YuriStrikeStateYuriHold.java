package com.hawk.game.yuriStrikes;

import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.YuriStrike.YuriState;
import com.hawk.game.protocol.YuriStrike.YuriStrikeInfo;
import com.hawk.log.Action;

@YuriStrikeState(pbState = YuriState.YURI_HOLD)
public class YuriStrikeStateYuriHold implements IYuriStrikeState {

	@Override
	public YuriStrikeInfo.Builder toPBBuilder(Player player, YuriStrike obj) {
		YuriStrikeInfo.Builder result = YuriStrikeInfo.newBuilder()
				.setState(pbState())
				.setLockArea(obj.getDbEntity().getAreaIdLock())
				.setYuriCfgId(obj.getDbEntity().getCfgId());
		return result;
	}

	@Override
	public void attackWin(Player player, YuriStrike obj) {
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(ItemInfo.valueListOf(obj.getCfg().getRewards()));
		awardItem.rewardTakeAffectAndPush(player, Action.GET_YURISTRIKE_REWARD);
		
		obj.setState(player, IYuriStrikeState.valueOf(YuriState.PRE_CLEAN));
		obj.getDbEntity().setAreaIdLock(0);
	}

}
