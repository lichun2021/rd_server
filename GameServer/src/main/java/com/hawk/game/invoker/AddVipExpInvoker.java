package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.log.Action;

public class AddVipExpInvoker extends HawkMsgInvoker {
	private Player player;
	private int vipExp;
	
	public AddVipExpInvoker(Player player, int vipExp) {
		this.player = player;
		this.vipExp = vipExp;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIP_POINT_VALUE, vipExp);
		awardItems.rewardTakeAffectAndPush(player, Action.PLAYER_BUILDING_UPGRADE);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getVipExp() {
		return vipExp;
	}

	public void setVipExp(int vipExp) {
		this.vipExp = vipExp;
	}
	
}
