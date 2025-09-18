package com.hawk.game.module;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.controler.SystemControler;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.TavernService;
import com.hawk.game.util.GsConst.ControlerModule;

/**
 * 酒馆
 * 
 * @author PhilChen
 *
 */
public class PlayerTavernModule extends PlayerModule {
	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerTavernModule(Player player) {
		super(player);
	}
	
	@Override
	protected boolean onPlayerAssemble() {
		TavernService.getInstance().refreshTavernInfo(player, true);
		return true;
	}



	@Override
	protected boolean onPlayerLogin() {
		TavernService.getInstance().syncTavernInfo(player, player.getData().getTavernEntity());
		return true;
	}
	
	/**
	 * 打开界面获取酒馆信息
	 * 
	 * @param resourceType
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TAVERN_INFO_SYNC_C_VALUE)
	public boolean onTavernRefreshSync(HawkProtocol protocol) {
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.DAILY_TASK)) {
			logger.error("daily task system closed");
			sendError(protocol.getType(), Status.SysError.DAILY_TASK_SYSTEM_CLOSED);
			return false;
		}
		
		TavernService.getInstance().refreshTavernInfo(player, false);
		return true;
	}

}
