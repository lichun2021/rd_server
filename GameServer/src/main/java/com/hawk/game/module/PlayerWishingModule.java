package com.hawk.game.module;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.controler.SystemControler;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Wishing.PlayerWishingReq;
import com.hawk.game.protocol.Wishing.PlayerWishingResp;
import com.hawk.game.service.WishingService;
import com.hawk.game.util.GsConst.ControlerModule;

/**
 * 许愿池
 * 
 * @author PhilChen
 *
 */
public class PlayerWishingModule extends PlayerModule {
	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerWishingModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		WishingService.getInstance().refreshWishingInfo(player);
		return true;
	}
	
	/**
	 * 打开界面获取许愿池信息
	 * 
	 * @param resourceType
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WISHING_INFO_SYNC_C_VALUE)
	public boolean onWishingSync(HawkProtocol protocol) {
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.ALLIED_DEPOT)) {
			logger.error("allied depot closed");
			sendError(protocol.getType(), Status.SysError.ALLIED_DEPOT_SYSTEM_CLOSED);
			return false;
		}
		
		WishingService.getInstance().refreshWishingInfo(player);
		return true;
	}

	/**
	 * 玩家许愿操作
	 * 
	 * @param resourceType
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WISHING_C_VALUE)
	public boolean onPlayerWishing(HawkProtocol protocol) {		
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.ALLIED_DEPOT)) {
			logger.error("allied depot closed");
			sendError(protocol.getType(), Status.SysError.ALLIED_DEPOT_SYSTEM_CLOSED);
			return false;
		}
		
		PlayerWishingReq req = protocol.parseProtocol(PlayerWishingReq.getDefaultInstance());
		Result<PlayerWishingResp.Builder> result = WishingService.getInstance().playerWishing(player, req.getResourceType());
		if (result.isOk()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WISHING_S_VALUE, result.getRetObj()));
			return true;
		} else {
			sendError(protocol.getType(), result.getStatus());
			return false;
		}
	}
	
}
