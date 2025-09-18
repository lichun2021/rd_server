package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.GlobalData;

/**
 * 落地数据,支持落地单个玩家，
 * 也支持落地所有数据
 * 落地全局数据.
 * 三个type可自由组合.
 * 
 * @author jm
 *
 */
public class SaveDataHandler extends HawkScript {
	/**
	 * 三个类型可组合.
	 */
	static int TYPE_SINGLE_PLAYER = 1 << 0;
	static int TYEP_ALL_PLAYER = 1 << 1;
	static int TYPE_GLOBAL_DATA = 1 << 2;
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String playerId = params.get("playerId");
		String strType = params.get("type");
		if (HawkOSOperator.isEmptyString(strType)) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "not appoint type");
		}
		
		int type = Integer.parseInt(strType);
		boolean success = true;
		if ((type & TYPE_SINGLE_PLAYER)  > 0) {
			if (HawkOSOperator.isEmptyString(playerId)) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "type is single player but  playerId is empty"); 
			} else {
				success &= GlobalData.getInstance().savePlayerAllDbEntities(playerId);
			}
		}
		
		if ((type & TYEP_ALL_PLAYER) > 0) {
			success &= GlobalData.getInstance().saveAllPlayerDbEntities();
		}
		
		if ((type & TYPE_GLOBAL_DATA) > 0) {
			success &= GlobalData.getInstance().saveAllGlobalEntities();
		}

		if (success) {
			return HawkScript.successResponse("save data success");
		} else {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "save has error type=>"+type);
		}
	}
}
