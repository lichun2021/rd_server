package com.hawk.game.util;

/**
 * int 的bit操作
 */
public class BitIntUtil {
	public static int setBitValue(int flag, int position, int value){
		int tmp =  (1 <<(position -1));
		if(value == 1){
			flag |= tmp;
		}else if(value == 0){
			tmp = tmp ^ Integer.MAX_VALUE;
			flag &= tmp;
		}

		return flag;
	}


	public static int getBitValue(int flag, int position) {
		return ((1 << (position - 1)) & flag)  > 0 ? 1 :0;
	}

	/**
	 * 计算bit中有多少1
	 * @param flag
	 * @return
	 */
	public static int bitCount(int flag) {
		return Integer.bitCount(flag);
	}
}
