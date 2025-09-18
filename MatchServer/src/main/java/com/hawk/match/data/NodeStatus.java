package com.hawk.match.data;

import org.hawk.os.HawkException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 节点状态
 * 
 * @author hawk
 *
 */
public class NodeStatus {
	/**
	 * 节点状态信息
	 */
	private JSONObject statusJson;
	
	/**
	 * 获取状态的json对象
	 * 
	 * @return
	 */
	public JSONObject getStatusJson() {
		return statusJson;
	}
	
	/**
	 * 更新状态信息
	 * 
	 * @param statusInfo
	 * @return
	 */
	public boolean update(String statusInfo) {
		try {
			statusJson = JSON.parseObject(statusInfo);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
}
