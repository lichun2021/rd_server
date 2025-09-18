package com.hawk.game.world.thread.tasks;

import org.hawk.os.HawkException;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;

public class ExitCrossWorldTask extends WorldTask {
	private Player player;
	public ExitCrossWorldTask(Player player, int worldTaskType) {		
		super(worldTaskType);
		this.player = player;
	}

	@Override
	public boolean onInvoke() {
		try {
			// 收回行军
			WorldMarchService.getInstance().mantualMoveCityProcessMarch(player);
			// 移除城点
			WorldPlayerService.getInstance().removeCity(player.getId(), true);
			
			WorldPointService.getInstance().removePlayerSignature(player.getId());
			
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		player.setCrossStatus(GsConst.PlayerCrossStatus.EXIT_CROSS_MARCH_FINAL);
		return true;
	}

}
