package com.hawk.game.gmscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.tuple.HawkTuple3;

import com.hawk.game.GsApp;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.tiberium.TiberiumPrepareMoveBackMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;


/**
 * 
 * @author jm
 * localhost:port/script/tiberiumMoveBack
 */
public class TiberiumMoveBackHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String playerId = params.get("playerId");
		int type = Integer.parseInt(params.getOrDefault("opt", "1"));
		String result = "ok";
		if (type == 1) {
			result = doForce(playerId);			
		}  else if(type == 2) {
			result = doClearImmigrationStats(playerId);
		}else if (type == 3) {
			result = doClearEmigrationStats(playerId);
		} else if (type == 4) {
			result = doClearImmigrationPlayer(playerId);
		}
		
		return HawkScript.successResponse(result);
	}
	
	private String doClearImmigrationPlayer(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return "player is null"; 
		}
		
		CrossService.getInstance().clearTiberiumImmigrationPlayer(playerId, false);
		
		return "ok";
	}

	private String doClearEmigrationStats(String playerId) {
		List<String> strList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(playerId)) {
			for (Entry<String, String> entry : CrossService.getInstance().getEmigrationPlayers().entrySet()) {
				CrossService.getInstance().clearTiberiumEmigrationPlayer(entry.getKey());
				strList.add(entry.getKey());
			}
		} else {
			CrossService.getInstance().clearTiberiumEmigrationPlayer(playerId);
			strList.add(playerId);
		}
		
		return strList.toString();
	}

	private String doClearImmigrationStats(String playerId) {
		List<String> strList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(playerId)) {
			for (Entry<String, String> entry : CrossService.getInstance().getImmigrationPlayers().entrySet()) {
				CrossService.getInstance().clearTiberiumImmigrationPlayer(entry.getKey());
				strList.add(entry.getKey());
			}
		} else {
			CrossService.getInstance().clearTiberiumImmigrationPlayer(playerId);
			strList.add(playerId);
		}  
		
		return strList.toString();
	}

	private String doForce(String playerId) {
		List<String> strList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(playerId)) {
			for (Entry<String, String> entry : CrossService.getInstance().getEmigrationPlayers().entrySet()) {
				HawkTuple3<String, Integer, String> tuple = moveBackPlayer(entry.getKey());
				strList.add(tuple.toString());
			}
		} else {
			HawkTuple3<String, Integer, String> tuple = moveBackPlayer(playerId);
			strList.add(tuple.toString());
		}
		
		return strList.toString();
	}

	public HawkTuple3<String, Integer, String> moveBackPlayer(String playerId) {
		int errorCode = ScriptError.SUCCESS_OK_VALUE;
		String msg = "ok";
		label: 
		{
			try {
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				if (player == null) {
					errorCode = ScriptError.ACCOUNT_NOT_EXIST_VALUE;
					msg = "player not exist";
	
					break label;
				} 
	
				// 不是本服,
				if (!GlobalData.getInstance().isLocalServer(player.getServerId())) {
					errorCode = ScriptError.ACCOUNT_NOT_EXIST_VALUE;
					msg = "server not the player register server";
	
					break label;
				}
	
				// 只有跨出去的玩家
				String toServer = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
				if (HawkOSOperator.isEmptyString(toServer)) {
					errorCode = ScriptError.NOT_IN_CROSS_VALUE;
					msg = "player not in cross";
	
					break label;
				}
				
				GsApp.getInstance().postMsg(player.getXid(), new TiberiumPrepareMoveBackMsg());
	
			} catch (Exception e) {
				HawkException.catchException(e);
	
				errorCode = ScriptError.EXCEPTION_VALUE;
				msg = "exception";
	
				break label;
			}
		}
		
		HawkLog.logPrintln("playerId:{} cross player move back errorCode:{}", playerId, errorCode);
		
		return new HawkTuple3<String, Integer, String>(playerId, errorCode, msg);
	}
}
