package com.hawk.game.manager;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

/**
 * 
 * @author jm
 *
 */
public class IconManager {
	/**
	 * 单例
	 */
	private static IconManager instance = new IconManager();
	/**
	 * {crcString, pfIconString}
	 * 存储crc字符串到pfIcon的映射
	 */
	private Map<String, String> crcPfIconMap = new ConcurrentHashMap<>(5000);
	
	private IconManager() {
		
	}
	
	public static IconManager getInstance() {
		return instance;
	}
	
	/**
	 * 理论上来说
	 * 只有从redis里面加载PfIcon的时候才会调用该方法
	 * @param pfIcon
	 * @return
	 */
	public String getPficonCrc(String pfIcon) {
		if (HawkOSOperator.isEmptyString(pfIcon)) {
			return "";
		}
		
		String crc = calcPficonCrc(pfIcon);
		crcPfIconMap.put(crc, pfIcon);
		
		return crc;
		
	}
	/***
	 * 计算pfIconf的Crc
	 * @param pfIcon
	 * @return
	 */
	public String calcPficonCrc(String pfIcon) {
		try {
			return Integer.toHexString(HawkOSOperator.calcCrc(pfIcon.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			HawkException.catchException(e, "calc pfIcon crc");
		}
		
		return "";
	}
	
	/**
	 * 内部接口不做参数校验.
	 * @param crc
	 * @return
	 */
	public String getPficonByCrc(String crc) {
		return crcPfIconMap.get(crc);
	}
}
