package com.hawk.game.trustee;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptManager;

import com.alibaba.fastjson.JSONObject;

public class TrusteeService {
	/**
	 * 是否开启
	 */
	private boolean enable = true;
	/**
	 * 托管玩家
	 */
	private Map<String, JSONObject> trusteePlayers;
	
	/**
	 * 单例对象
	 */
	private static TrusteeService instance = null;

	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static TrusteeService getInstance() {
		if (instance == null) {
			instance = new TrusteeService();
		}
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	public TrusteeService() {
		// 设置实例
		instance = this;
	}
	
	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		// 初始化策略玩家表信息
		trusteePlayers = new HashMap<String, JSONObject>();
		
		// 每分钟检测一次
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					HawkOSOperator.osSleep(60000);
					
					// 帧更新
					onTickUpdate();
				}
			};
		});
		thread.setName("Trustee");
		thread.setDaemon(true);
		thread.start();
		return true;
	}

	/**
	 * 是否允许开启托管
	 * 
	 * @param enable
	 */
	public void setTrusteeEnable(boolean enable) {
		this.enable = enable;
	}
	
	/**
	 * 帧更新
	 */
	private void onTickUpdate() {
		try {
			// 最高开关控制
			if (!enable) {
				return;
			}
			
			// ai脚本
			HawkScript script = HawkScriptManager.getInstance().getScript("trustee");
			if (script == null) {
				return;
			}
			
			for (Entry<String, JSONObject> trusteeEntry : trusteePlayers.entrySet()) {
				try {
					JSONObject trusteeArgs = trusteeEntry.getValue();
					// 执行策略
					script.doSomething(0, trusteeEntry.getKey(), trusteeArgs);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}				
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
