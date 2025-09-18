package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 举报内容信息过滤完成消息
 * 
 * @author lating
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class ReportingInfoFilterFinishMsg extends HawkMsg {
	/**
	 * 过滤后的消息内容
	 */
	String msgContent;
	/**
	 * 被传回的数据
	 */
	String callbackData;

	public ReportingInfoFilterFinishMsg() {
		super(MsgId.REPORTING_INFO_FILTER);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static ReportingInfoFilterFinishMsg valueOf(String msgContent, String callbackData) {
		ReportingInfoFilterFinishMsg msg = HawkObjectPool.getInstance().borrowObject(ReportingInfoFilterFinishMsg.class);
		msg.msgContent = msgContent;
		msg.callbackData = callbackData;
		return msg;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public String getCallbackData() {
		return callbackData;
	}

	public void setCallbackData(String callbackData) {
		this.callbackData = callbackData;
	}
	
}
