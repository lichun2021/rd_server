package com.hawk.activity.type.impl.submarineWar.rank;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author LENOVO
 *
 */
public class SubmarineWarRankGroup {

	private int groupId;
	
	private List<String> servers = new ArrayList<>();
	
	private long updateTime;
	
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public int getGroupId() {
		return groupId;
	}
	
	
	public void setServers(List<String> servers) {
		this.servers = servers;
	}
	
	public List<String> getServers() {
		return servers;
	}
	
	
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	public long getUpdateTime() {
		return updateTime;
	}
	
	
	public String serializ(){
		JSONObject obj = new JSONObject();
		obj.put("groupId", this.groupId);
		obj.put("updateTime", this.updateTime);
		if(servers.size() > 0){
			JSONArray arr = new JSONArray();
			for(String i : servers){
				arr.add(i);
			}
			obj.put("servers", arr.toJSONString());
		}
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr){
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.groupId = obj.getIntValue("groupId");
		this.updateTime = obj.getLongValue("updateTime");
		List<String> serversTemp = new ArrayList<>();
		if(obj.containsKey("servers")){
			String str = obj.getString("servers");
			JSONArray jarr = JSONArray.parseArray(str);
			for(int i=0;i<jarr.size();i++){
				String val = jarr.getString(i);
				serversTemp.add(val);
			}
		}
		this.servers = serversTemp;
	}
	
	
	
}
