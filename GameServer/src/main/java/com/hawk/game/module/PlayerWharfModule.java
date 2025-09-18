package com.hawk.game.module;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.entity.WharfEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.service.WharfService;

/**
 * 码头
 * 
 * @author PhilChen
 *
 */
public class PlayerWharfModule extends PlayerModule {
	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerWharfModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		WharfEntity wharfEntity = player.getData().getWharfEntity();
		WharfService.getInstance().refreshWharfAward(player, wharfEntity);
		WharfService.getInstance().syncWharfInfo(player);
		return true;
	}
	
	@ProtocolHandler(code = HP.code.WHARF_PULL_DATA_C_VALUE)
	private boolean onPullData(HawkProtocol protocol) {
		WharfService.getInstance().syncWharfInfo(player);
		return true;
	}
	
	/**
	 * 领取码头奖励
	 * 
	 * @param resourceType
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WHARF_TAKE_AWARD_C_VALUE)
	private boolean onTakeAward(HawkProtocol protocol) {
		Result<?> result = WharfService.getInstance().takeAward(player);
		if (result.isOk()) {
			player.responseSuccess(protocol.getType());
			return true;
		} else {
			sendError(protocol.getType(), result.getStatus());
			return false;
		}
	}
}
