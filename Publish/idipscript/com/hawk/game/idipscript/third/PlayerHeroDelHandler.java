package com.hawk.game.idipscript.third;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 删除英雄
 *
 * localhost:8080/script/idip/4185
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4185")
public class PlayerHeroDelHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new DeleteHeroMsgInvoker(player, request));
		} else {
			deleteHero(player, request);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	public static class DeleteHeroMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public DeleteHeroMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			deleteHero(player, request);
			return true;
		}
	}
	
	/**
	 * 删除英雄
	 * @param player
	 * @param request
	 */
	private static void deleteHero(Player player, JSONObject request) {
		int heroId = request.getJSONObject("body").getIntValue("GenId");
		List<HeroEntity> heros = player.getData().getHeroEntityList();
		Iterator<HeroEntity> it = heros.iterator();
		int count = 0;
		while (it.hasNext()) {
			HeroEntity entity = it.next();
			if (entity.getHeroId() == heroId) {
				count++;
				it.remove();
				deleteHero(player, entity);
			}
		}
		
		LogUtil.logIdipSensitivity(player, request, 0, count);
		HawkLog.logPrintln("idip remove hero, playerId: {}, heroId: {}", player.getId(), heroId);
	}
	
	private static void deleteHero(Player player, HeroEntity entity) {
		entity.delete();
		PlayerHero hero = entity.getHeroObj();
		// 卸任官职
		hero.officeAppoint(0);
		// 返还还行军
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(entity.getPlayerId());
		if (marchs != null) {
			for (IWorldMarch march : marchs) {
				if (march.getMarchEntity().getHeroIdList().contains(hero.getCfgId())) {
					WorldMarchService.getInstance().onMarchReturnImmediately(march, march.getMarchEntity().getArmys());
				}
			}
		}
		
		if (player.isActiveOnline()) {
			player.kickout(Status.IdipMsgCode.IDIP_REMOVE_HERO_VALUE, true, null);
		}
		
	}
	
}
