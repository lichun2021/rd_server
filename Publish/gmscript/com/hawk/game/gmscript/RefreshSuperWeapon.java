package com.hawk.game.gmscript;

import java.util.Map;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.config.SuperWeaponConstCfg;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.superweapon.SuperWeaponService;

/**
 * 刷新战区争夺开启时间
 * @author golden
 * 
 */
public class RefreshSuperWeapon extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
		long nexWarStartTime = HawkTime.getNextTimeDayOfWeek(HawkTime.getMillisecond(), constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute());
		
		SuperWeaponService.getInstance().setStartTime(nexWarStartTime);
		SuperWeaponService.getInstance().setTurnCount(1);
		SuperWeaponService.getInstance().setStatus(SuperWeaponPeriod.SIGNUP_VALUE);
		
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.SUPERWEAPON_SIGNUP_STARTED, null);
		SuperWeaponService.getInstance().broadcastSuperWeaponInfo(null);
		return HawkScript.successResponse(null);
	}
}