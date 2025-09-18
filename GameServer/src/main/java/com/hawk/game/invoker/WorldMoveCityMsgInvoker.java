package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.entity.PlayerOtherEntity;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.util.WorldUtil;

/**
 * 世界迁城
 * @author jm
 *
 */
public class WorldMoveCityMsgInvoker extends HawkMsgInvoker {

	private int moveCityType;
	private Player player;
	private ConsumeItems consumeItems;
	
	public WorldMoveCityMsgInvoker(Player player, ConsumeItems consumeItems, int moveCityType){
		this.player = player;
		this.consumeItems = consumeItems;
		this.moveCityType = moveCityType;
	}
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		// 道具消耗
		Action action = Action.MANTUAL_MOVE_CITY;
		if(WorldUtil.isRandomMoveCity(moveCityType)){
			action = Action.RANDOM_MOVE_CITY;
		} else if(WorldUtil.isGuildMoveCity(moveCityType)){
			action = Action.GUILD_MOVE_CITY;
		}else if(WorldUtil.isGuildAutoMoveCity(moveCityType)){
			PlayerOtherEntity entity = player.getData().getPlayerOtherEntity();
			int cnt = entity.getAutoGuildCityMoveCnt() +1;
			entity.setAutoGuildCityMoveCnt(cnt);
		}
		return consumeItems.consumeAndPush(player, action).hasAwardItem();
	}
	public int getMoveCityType() {
		return moveCityType;
	}
	public Player getPlayer() {
		return player;
	}
	public ConsumeItems getConsumeItems() {
		return consumeItems;
	}
}
