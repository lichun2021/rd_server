package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 安全sdk检测回调消息
 * 
 * @author lating
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class TsssdkInvokeMsg extends HawkMsg {
	/**
	 * 场景
	 */
	int category;
	/**
	 * 检测结果
	 */
	int resultCode;
	/**
	 * 协议
	 */
	int protocol;
	/**
	 * 过滤后的消息内容
	 */
	String msgContent;
	/**
	 * 透传回调数据
	 */
	String callbackData;

	public TsssdkInvokeMsg() {
		super(MsgId.TSSSDK_UIC_INVOKE);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static TsssdkInvokeMsg valueOf(int category, int resultCode, int protocol, String msgContent, String callbackData) {
		TsssdkInvokeMsg msg = HawkObjectPool.getInstance().borrowObject(TsssdkInvokeMsg.class);
		msg.category = category;
		msg.resultCode = resultCode;
		msg.protocol = protocol;
		msg.msgContent = msgContent;
		msg.callbackData = callbackData;
		return msg;
	}
	
	public int getCategory() {
		return category;
	}
	
	public int getResultCode() {
		return resultCode;
	}

	public String getMsgContent() {
		return msgContent;
	}
	
	public String getCallbackData() {
		return callbackData;
	}
	
	public int getProtocol() {
		return protocol;
	}
	
}
