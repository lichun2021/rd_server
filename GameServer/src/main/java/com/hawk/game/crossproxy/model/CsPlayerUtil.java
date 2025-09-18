package com.hawk.game.crossproxy.model;

import com.hawk.game.protocol.Login.HPLogin;

public class CsPlayerUtil {
	/**
	 * 是否是第一次跨服登录.
	 * @return
	 */
	public static boolean isFirstCrossLogin(HPLogin hpLogin) {		
		if (hpLogin == null) {
			return false;
		}
		
		return hpLogin.hasInnerEnterCrossMsg();			
	}
	
	public static boolean isFirstCrossLogin(HPLogin hpLogin, int crossType) {
		if (hpLogin == null) {
			return false;
		}
		
		
		if (hpLogin.hasInnerEnterCrossMsg()) {
			return hpLogin.getInnerEnterCrossMsg().getCrossType() == crossType;
		} else {
			return false;
		}
	}
}
