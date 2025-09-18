package com.hawk.game.service.starwars.callback;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsSearchPlayerResp;

public class SearchPlayerCallBack extends CsRpcCallback {
	Player player;
	public SearchPlayerCallBack(Player player) {
		this.player = player;
	}
	@Override
	public int invoke(Object arg) {		
		HawkProtocol hawkProtocol = (HawkProtocol)arg;
		StarWarsSearchPlayerResp resp = hawkProtocol.parseProtocol(StarWarsSearchPlayerResp.getDefaultInstance());
		//说明查到了.
		Player.logger.info("search player call back playerId:{}, result:{}", player.getId(), resp.getPlayer() != null);
		
		player.responseSuccess(HP.code.STAR_WARS_SEARCH_PLAYER_REQ_VALUE);
		player.sendProtocol(hawkProtocol);
		
		return 0;
	}
	
	public void onTimeout(Object arg) {
		player.sendError(HP.code.STAR_WARS_SEARCH_PLAYER_REQ_VALUE, Status.Error.STAR_WARS_SEARCH_TIME_OUT_VALUE, 0);
	}
}
