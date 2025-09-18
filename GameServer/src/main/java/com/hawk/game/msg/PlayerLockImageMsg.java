package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.msg.PlayerUnlockImageMsg.ImageType;

/***
 * 锁定头像消息（当某一些状态消失之后，玩家回到默认头像）
 * @author yang.rao
 *
 */
public class PlayerLockImageMsg extends HawkMsg {
	
	/** 类型 **/
	private ImageType type;	
	/** 锁定类型 **/
	private LockType lockType;	
	/** 锁定参数 **/
	private LockParam lockParam;
	
	public PlayerLockImageMsg(LockType lockType, LockParam lockParam){
		this.lockType = lockType;
		this.lockParam = lockParam;
	}
	
	public static PlayerLockImageMsg valueOf(LockType lockType, LockParam lockParam){
		return new PlayerLockImageMsg(lockType, lockParam);
	}

	public LockType getLockType() {
		return lockType;
	}

	public LockParam getLockParam() {
		return lockParam;
	}

	public ImageType getType() {
		return type;
	}

	public void setType(ImageType type) {
		this.type = type;
	}

	/***
	 * 锁定类型(目前只有个人状态发生变化事，头像或者头像框才会变为解锁状态)
	 * @author yang.rao
	 */
	public enum LockType{
		PLAYERSTAT(4); //个人状态
		
		int value;
		private LockType(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	}
	
	public enum LockParam{
		NO_HUANGDI(1), //不为皇帝
		NO_MENGZHU(2), //不为盟主
		NO_ZHANQUSILING(6); //不为战区司令
		
		int value;
		private LockParam(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
		
	}
}
