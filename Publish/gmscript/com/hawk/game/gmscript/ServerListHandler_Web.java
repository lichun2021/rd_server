package com.hawk.game.gmscript;

import java.util.List;
import java.util.Map;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.ServerInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;

/**
 * 提供gm平台 当前大区下的所有小区的列表
 *
 */
public class ServerListHandler_Web extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		JSONObject retJson = new JSONObject();
		retJson.put("ret", 0);
		retJson.put("msg", "");

		JSONArray infoArray = new JSONArray();
		List<ServerInfo> serverList = RedisProxy.getInstance().getServerList();
		if (serverList != null) {
			for (ServerInfo serverInfo : serverList) {
				if (!GlobalData.getInstance().isMainServer(serverInfo.getId())) {
					continue;
				}
				ZoneBean bean = makeJsonBean(serverInfo);
				infoArray.add(bean);
			}
		}
		retJson.put("data", infoArray);
		return retJson.toJSONString();
	}
	

	public ZoneBean makeJsonBean(ServerInfo serverInfo) {
		ZoneBean bean = new ZoneBean();

		bean.setZoneID(serverInfo.getId());
		bean.setZoneName(serverInfo.getName());
		bean.setHost(serverInfo.getHost());
		bean.setZoneType(String.valueOf(serverInfo.getServerType()));
		bean.setNowState(String.valueOf(serverInfo.getStatusLabel()));
		bean.setHuidu(serverInfo.getGrayState()==1);
		bean.setOpen(serverInfo.getViewStatus()==1);
		bean.setNewZone(serverInfo.getNewStatus()==1);
		bean.setWelcome(serverInfo.getSuggestStatus()==1);
		bean.setCreateTime(HawkTime.parseTime(serverInfo.getOpenTime()) / 1000);
		bean.setAgentHost(serverInfo.getAgentHost());
		bean.setWebHost(serverInfo.getWebHost());

		return bean;
	}
	
	public class ZoneBean {
		private String agentHost;
		private String webHost;
		
		private String zoneID;
		private String zoneName;
		private String host;
		private String zoneType;
		private String nowState;

		private long createTime;
		private boolean huidu;
		private boolean open;
		private boolean newZone;
		private boolean welcome;
		
		
		public String getWebHost() {
			return webHost;
		}
		public void setWebHost(String webHost) {
			this.webHost = webHost;
		}
		public String getAgentHost() {
			return agentHost;
		}
		public void setAgentHost(String agentHost) {
			this.agentHost = agentHost;
		}
		public String getZoneID() {
			return zoneID;
		}
		public void setZoneID(String zoneID) {
			this.zoneID = zoneID;
		}
		public String getZoneName() {
			return zoneName;
		}
		public void setZoneName(String zoneName) {
			this.zoneName = zoneName;
		}
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public String getZoneType() {
			return zoneType;
		}
		public void setZoneType(String zoneType) {
			this.zoneType = zoneType;
		}
		public String getNowState() {
			return nowState;
		}
		public void setNowState(String nowState) {
			this.nowState = nowState;
		}
		public long getCreateTime() {
			return createTime;
		}
		public void setCreateTime(long createTime) {
			this.createTime = createTime;
		}
		public boolean isHuidu() {
			return huidu;
		}
		public void setHuidu(boolean huidu) {
			this.huidu = huidu;
		}
		public boolean isOpen() {
			return open;
		}
		public void setOpen(boolean open) {
			this.open = open;
		}
		public boolean isNewZone() {
			return newZone;
		}
		public void setNewZone(boolean newZone) {
			this.newZone = newZone;
		}
		public boolean isWelcome() {
			return welcome;
		}
		public void setWelcome(boolean welcome) {
			this.welcome = welcome;
		}
	}
}
