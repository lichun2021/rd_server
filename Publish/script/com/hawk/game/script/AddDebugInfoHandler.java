package com.hawk.game.script;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;

import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.service.starwars.StarWarsConst.SWWarType;
import com.hawk.game.util.BuilderUtil;

/**
 * 调试指令
 * addDebugInfo
 * @author jm
 *
 */
public class AddDebugInfoHandler extends HawkScript {

	@Override
	public String action(Map<String, String> map, HawkScriptHttpInfo script) {
		String op = map.getOrDefault("op", "1");
		String rlt = "ok";
		if (op.equals("1")) {
			rlt = setStarWarsKing(map);
		} else if(op.equals("2")) {
			rlt = reloadStarWarsKing();
		} else if (op.equals("3")) {
			sendWorldKingMail();
		} else if (op.equals("4")) {
			getKingRecord(map.get("playerId"), Integer.parseInt(map.get("part")), Integer.parseInt(map.get("team")));
		} else  if (op.equals("5")) {
			sendPersonReward(map.get("playerId"), Integer.parseInt(map.get("part")), Integer.parseInt(map.get("team")));
		} else if(op.equals("6")) {
			StarWarsOfficerService.getInstance().onClearEvent();
		} else if (op.equals("7")) {
			rlt = posAllPlayerEnterInstance();
		} else {
			rlt = "op invalid";
		}
		
		return HawkScript.successResponse("");
	}
	
	public String posAllPlayerEnterInstance() {
		try {
			Set<Player> playerSet = GlobalData.getInstance().getOnlinePlayers();
			for (Player player : playerSet) {
				HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.STAR_WARS_ENTER_REQ_VALUE);
				hawkProtocol.bindSession(player.getSession());
				HawkTaskManager.getInstance().postProtocol(player.getXid(), hawkProtocol);
			}
			return "post protocol success";
		} catch (Exception e) {
			HawkException.catchException(e);
			return e.getMessage();
		}
				
	} 
	
	private void sendPersonReward(String playerId, int part, int team) {
		try {
			Method method = StarWarsOfficerService.class.getDeclaredMethod("sendSpecialAward", String.class, int.class, int.class);
			method.setAccessible(true);
			method.invoke(StarWarsOfficerService.getInstance(), playerId, part, team);
		} catch (Exception e) {
			HawkException.catchException(e);
		}			
		
	}

	private void getKingRecord(String playerId,int part, int teamId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);	
		StarWarsOfficerService.getInstance().synStarWarsKingRecord(player, part, teamId);
	}
	public void sendWorldKingMail() {
		try {
			Method method = StarWarsActivityService.class.getDeclaredMethod("sendKingMail", SWWarType.class);
			method.setAccessible(true);
			method.invoke(StarWarsActivityService.getInstance(), SWWarType.THIRD_WAR);
		} catch (Exception e) {
			HawkException.catchException(e);
		}			
	}
	/**
	 * 
	 * @return
	 */
	private String reloadStarWarsKing() {
		StarWarsOfficerService.getInstance().loadOrReloadOfficer();
		
		return "load data success    ";
	}

	private String setStarWarsKing(Map<String, String> map) {
		int part = Integer.parseInt(map.getOrDefault("part",  "2"));
		int team = Integer.parseInt(map.getOrDefault("team", "0"));
		Player player = GlobalData.getInstance().scriptMakesurePlayer(map);
		if (player == null) {
			return " can't make sure player";
		}
		int termId = 1;
		CrossPlayerStruct.Builder cps = BuilderUtil.buildCrossPlayer(player);
		RedisProxy.getInstance().updateCrossPlayerStruct(player.getId(), cps.build(), 86400);
		StarWarsOfficerService.getInstance().onFighterOver(player.getId(), termId, part, team);
		this.reloadStarWarsKing();
		return " set king success";
	}

}
