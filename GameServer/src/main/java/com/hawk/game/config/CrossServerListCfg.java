package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CrossServerListCfg {	
	private final int id;
	/**
	 * 可以跨的区服
	 */
	private final String servers;
	/**
	 * 可以相互跨的区服.
	 */
	private List<String> serverList;
	
	public CrossServerListCfg(int id, String servers) {
		this.id = id;
		this.servers = servers;
	}
	public CrossServerListCfg() {
		this.id = 0;
		this.servers = "";
	}	

	public int getId() {
		return id;
	}

	public String getServers() {
		return servers;
	}

	public List<String> getServerList() {
		return serverList;
	} 
	
	
	public boolean assemble() {
		serverList = new ArrayList<>(Arrays.asList(servers.split("_"))); 		
			
		return true;
	}
	

	public boolean checkValid() {
		Set<String> serverSet = new HashSet<>(serverList);
		if (serverSet.size() != serverList.size()) {
			throw new InvalidParameterException("区服列表中有重复的数据");
		}
		
		return true;
	}
}
