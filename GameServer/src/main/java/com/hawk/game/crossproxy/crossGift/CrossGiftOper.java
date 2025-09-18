package com.hawk.game.crossproxy.crossGift;

/**
 * 跨服礼包实现
 * @author Golden
 *
 */
public interface CrossGiftOper {

	/**
	 * 检测是否可以发奖
	 * @return
	 */
	public boolean doCheck(String playerId);
	
	/**
	 * 获取实现类
	 * @return
	 */
	public static CrossGiftOper imp(int type) {
		
		switch (type) {
		case 1:
			return new CrossGiftImp1();

		case 2:
			return new CrossGiftImp2();
			
		case 3:
			return new CrossGiftImp3();
			
		case 4:
			return new CrossGiftImp4();
			
		default:
			return null;
		}
	}
}
