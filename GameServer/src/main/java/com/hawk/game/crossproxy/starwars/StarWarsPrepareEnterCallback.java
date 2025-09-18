package com.hawk.game.crossproxy.starwars;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Cross.StarWarsCrossMsg;
import com.hawk.game.service.starwars.StarWarsActivityService;

public class StarWarsPrepareEnterCallback extends CsRpcCallback {
	Player player;
	StarWarsCrossMsg crossMsg;
	public StarWarsPrepareEnterCallback(Player player, StarWarsCrossMsg crossMsg) {
		this.player = player;
		this.crossMsg = crossMsg;	
	}
	
	@Override
	public int invoke(Object obj) {			
		StarWarsActivityService.getInstance().tryDeincreseLoginingPlayer();
		HawkProtocol hawkProtocol = (HawkProtocol)obj; 		
		StarWarsCallbackOperationService.getInstance().onPrepareCrossBack(player, hawkProtocol, crossMsg);
		
		return 0;
	}
	
	@Override
	public void onTimeout(Object obj) {
		Player.logger.info("starwars prepare ent timeout playerId:{}", player.getId());
		StarWarsActivityService.getInstance().tryDeincreseLoginingPlayer();				
		StarWarsCallbackOperationService.getInstance().onPrepareCrossFail(player);
	}
}
