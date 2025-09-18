package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.service.ArmyService;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.Action;

public class MarchBeforeRemoveMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	private IWorldMarch worldMarch;
	
	public MarchBeforeRemoveMsgInvoker(Player player, IWorldMarch worldMarch) {
		this.player = player;
		this.worldMarch = worldMarch;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {		
		if(!worldMarch.beforeImmediatelyRemoveMarchProcess(player)){
			return false;
		}
		// 若是有援助资源，则加回,援助资源到达目的地之后给对方，援助资源置为null
		WorldMarch marchEntity = worldMarch.getMarchEntity();
		if (marchEntity.getAssistantStr() != null) {
			AwardItems award = AwardItems.valueOf(marchEntity.getAssistantStr());
			award.rewardTakeAffectAndPush(player, Action.ASSISTANT_RES);
		}
		// 立即回兵
		ArmyService.getInstance().onArmyBack(player, marchEntity.getArmys(), marchEntity.getHeroIdList(),marchEntity.getSuperSoldierId(), worldMarch);
		return true;
	}
	
	public Player getPlayer() {
		return player;
	}

	public IWorldMarch getWorldMarch() {
		return worldMarch;
	}
	
	
}
