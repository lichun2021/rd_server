package com.hawk.activity.type.impl.mergecompetition.rank;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.hawk.collection.ConcurrentHashSet;

import com.hawk.activity.ActivityManager;

/**
 * 比拼组服信息
 */
public class CompeteServerInfo {
	
	private volatile AtomicLong mergeTime = new AtomicLong(0);
	
	private volatile Set<String> serverSet = new ConcurrentHashSet<>();

	private String serverGroup;
	
	public CompeteServerInfo() {
	}
	
	public String getOppServer() {
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		Optional<String> optional = this.serverSet.stream().filter(e -> !e.equals(serverId)).findFirst();
		return optional.isPresent() ? optional.get() : "";
	}
	
	public long getMergeTime() {
		return mergeTime.get();
	}

	public void setMergeTime(int mergeTime) {
		this.mergeTime.set(mergeTime * 1000L);
	}
	
	public void addServer(String serverId) {
		serverSet.add(serverId);
	}

	public Set<String> getServerList() {
		return serverSet;
	}

	public int getServerCount() {
		return serverSet.size();
	}
	
	public boolean isServerEmpty() {
		return serverSet.isEmpty();
	}
	
	public String getServerGroup() {
		return serverGroup;
	}

	public void setServerGroup(String serverGroup) {
		this.serverGroup = serverGroup;
	}
	
}
