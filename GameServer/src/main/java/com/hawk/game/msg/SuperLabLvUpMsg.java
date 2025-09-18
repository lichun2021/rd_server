package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 超能实验室升级部件消息
 * 
 * @author lating
 *
 */
public class SuperLabLvUpMsg extends HawkMsg {

	/**
	 * 升级次数
	 */
	private int levelUpTimes;

	/**
	 * 下次升级需要的能量源物品数量
	 */
	private int nextNeedItemNum;
	
	public int getLevelUpTimes() {
		return levelUpTimes;
	}

	public void setLevelUpTimes(int levelUpTimes) {
		this.levelUpTimes = levelUpTimes;
	}

	public int getNextNeedItemNum() {
		return nextNeedItemNum;
	}

	public void setNextNeedItemNum(int nextNeedItemNum) {
		this.nextNeedItemNum = nextNeedItemNum;
	}

	public SuperLabLvUpMsg() {
		super(MsgId.SUPER_LAB_LEVELUP);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static SuperLabLvUpMsg valueOf(int levelUpTimes, int nextNeedItemNum) {
		SuperLabLvUpMsg msg = new SuperLabLvUpMsg();
		msg.setLevelUpTimes(levelUpTimes);
		msg.setNextNeedItemNum(nextNeedItemNum);
		return msg;
	}

}
