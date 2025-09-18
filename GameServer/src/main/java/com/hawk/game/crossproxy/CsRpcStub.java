package com.hawk.game.crossproxy;

import org.hawk.os.HawkTime;

/**
 * 跨服rpc通信存更
 * 
 * @author hawk
 *
 */
public class CsRpcStub {
	/**
	 * 代理头信息
	 */
	private ProxyHeader header;
	/**
	 * 存根时间
	 */
	private long stubTime;
	/**
	 * 线程索引
	 */
	private int threadIdx;
	/**
	 * 回调接口
	 */
	private CsRpcCallback callback;
	
	public CsRpcStub(ProxyHeader header, int threadIdx, CsRpcCallback callback) {
		this.header = header;
		this.threadIdx = threadIdx;
		this.callback = callback;
		this.stubTime = HawkTime.getMillisecond();
	}

	public ProxyHeader getHeader() {
		return header;
	}
	
	public long getStubTime() {
		return stubTime;
	}

	public void setStubTime(long stubTime) {
		this.stubTime = stubTime;
	}

	public int getThreadIdx() {
		return threadIdx;
	}

	public CsRpcCallback getCallback() {
		return callback;
	}
}
