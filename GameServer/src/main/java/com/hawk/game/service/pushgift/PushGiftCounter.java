package com.hawk.game.service.pushgift;

import java.util.Map;

/**
 * 计数器
 * @author Golden
 *
 */
public class PushGiftCounter {

	/**
	 * 计数map
	 */
	private Map<String, Integer> counterMap;
	
	/**
	 * 私有化默认构造
	 */
	private PushGiftCounter() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static PushGiftCounter getInstance() {
		return Inner.instance;
	}
	
	/**
	 * 内部类
	 * @author Golden
	 *
	 */
	private static class Inner {  
        private static final PushGiftCounter instance = new PushGiftCounter();  
    }
	
}


