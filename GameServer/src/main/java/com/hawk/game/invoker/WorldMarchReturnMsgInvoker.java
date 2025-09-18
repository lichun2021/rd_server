package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.service.ArmyService;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.submarch.BasedMarch;

public class WorldMarchReturnMsgInvoker extends HawkMsgInvoker {

	private Player player;
	private BasedMarch basedMarch;
	
	public WorldMarchReturnMsgInvoker(Player player, BasedMarch basedMarch) {
		this.player = player;
		this.basedMarch = basedMarch;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		WorldMarch march = basedMarch.getMarchEntity();
		//此处为什么不放在投递消息之前判断, 是因为, 假如在判断完成并且将mask设定之后, 一旦玩家消息没有处理或者消息异常
		//则此过程不再重复执行, 就会导致卡队列返回的问题.
		//放在此处判断，是因为，即使系统处理比较慢导致多次投递了消息，也没有关系，因为玩家消息队列执行，执行完成之后会将mask设定，则下次不会再次执行
		//这样，即使在丢消息以及异常情况下，因为时间到了，每次心跳都会尝试再次执行，直到成功为止
		
		//判断是否已经在处理return了
		if((march.getMarchProcMask() & GsConst.MarchProcMask.RETURN_PROC) > 0) {
			WorldMarchService.logger.error("marchReturn failed, march is already processing, marchData: {}", basedMarch.getMarchEntity());
			return false;
		}
		// 各自处理自己的返回
		if((march.getMarchProcMask() & GsConst.MarchProcMask.AWARD_PROC) == 0){
			basedMarch.onWorldMarchReturn(player);
			march.addWorldMarchProcMask(GsConst.MarchProcMask.AWARD_PROC);
		}
		// 部队回城
		ArmyService.getInstance().onArmyBack(player, basedMarch.getMarchEntity().getArmys(), basedMarch.getMarchEntity().getHeroIdList(),basedMarch.getMarchEntity().getSuperSoldierId(), basedMarch);
		march.addWorldMarchProcMask(GsConst.MarchProcMask.RETURN_PROC);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public BasedMarch getBasedMarch() {
		return basedMarch;
	}
	
}
