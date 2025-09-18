package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/***
 * 解锁头像(头像框消息)
 * @author yang.rao
 *
 */
public class PlayerUnlockImageMsg extends HawkMsg {
	
	/** 类型 (暂时注释，头像和头像框解锁公用一条消息，这样省了一半的消息量) **/
	private ImageType type;
	/** 解锁类型 **/
	private UnlockType unlockType;
	/** 解锁参数 **/
	private Object unlockParam;
	
	public PlayerUnlockImageMsg(UnlockType unlockType, Object unlockParam){
		this.unlockType = unlockType;
		this.unlockParam = unlockParam;
	}

	public UnlockType getUnlockType() {
		return unlockType;
	}

	public Object getUnlockParam() {
		return unlockParam;
	}
	
	public ImageType getType() {
		return type;
	}

	public void setType(ImageType type) {
		this.type = type;
	}
	
	public static PlayerUnlockImageMsg valueOf(UnlockType unlockType, Object unlockParam){
		return new PlayerUnlockImageMsg(unlockType, unlockParam);
	}

	/***
	 * 解锁类型
	 * @author yang.rao
	 *
	 */
	public enum UnlockType{
		
		VIPLEVEL(1), //贵族等级
		PLAYERLEVEL(2), //玩家等级
		EFFECT(3), //使用道具
		PLAYERSTAT(4); //个人状态 （1:总统，2:盟主，3:QQ用户，4:QQSVIP，5:微信用户）
		
		int value;
		private UnlockType(int value) {
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	}
	
	/***
	 * 个人状态，参数
	 * @author yang.rao
	 *
	 */
	public enum PLAYERSTAT_PARAM{
	
		HUANGDI(1), 
		MENGZHU(2),
		QQ(3),
		QQSVIP(4),
		WEIXIN(5),
		ZHANQUSILING(6); //战区司令
		
		int value;
		private PLAYERSTAT_PARAM(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	}
	
	/***
	 * 头像类型
	 * @author yang.rao
	 *
	 */
	public enum ImageType{
		FRAME, //头像
		IMAGE; //头像框
	}
}
