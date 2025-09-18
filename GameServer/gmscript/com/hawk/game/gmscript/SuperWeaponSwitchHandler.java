package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.superweapon.SuperWeaponService;

/**
 * 超级武器开启关闭服务
 * @author zhenyu.shang
 * @since 2018年5月24日
 */
public class SuperWeaponSwitchHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		
		try {
			String act = params.get("act");
			if(act == null){
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "");
			}
			if(act.equals("open")){
				SuperWeaponService.getInstance().openSuperWeaponService();
			} else if(act.equals("close")){
				SuperWeaponService.getInstance().closeSuperWeaponService();
			} else {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "");
			}
			
			SuperWeaponService.logger.info("super weapon servie do {} success ~~!", act);
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, e.getMessage());
		}
		return HawkScript.successResponse(null);
	}

}
