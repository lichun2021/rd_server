package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.log.Action;

/**
 * 新版野怪体力返还
 * @author golden
 *
 */
public class NewMonsterVitReturnInvoker extends HawkMsgInvoker {

	private Player player;
	
	/**
	 * 返还数量
	 */
	private int returnCount;
	
	
	public NewMonsterVitReturnInvoker(Player player, int returnCount) {
		this.player = player;
		this.returnCount = returnCount;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, returnCount);
		awardItems.rewardTakeAffectAndPush(player, Action.NEW_MONSTER_VIT_RETURN);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getCount() {
		return returnCount;
	}

	public void setCount(int count) {
		this.returnCount = count;
	}
}
