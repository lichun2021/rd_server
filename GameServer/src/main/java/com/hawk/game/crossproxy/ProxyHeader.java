package com.hawk.game.crossproxy;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

public class ProxyHeader {
	// 原始数据
	private String ori;
	
	// 拆分数据
	private int type = -1;
	
	// 服务器往返
	private String from = "";
	private String to = "";
	
	// 收发对象往返
	private String source = "";
	private String target = "";
	
	private String rpcid = "";

	// 时戳
	private long timestamp = 0;
	
	// 广播集合
	private Set<String> broadcastIds;
	
	public String getOri() {
		return ori;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getRpcid() {
		return rpcid;
	}

	public void setRpcid(String rpcid) {
		this.rpcid = rpcid;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Set<String> getBroadcastIds() {
		if (broadcastIds == null) {
			broadcastIds = new HashSet<String>();
		}
		return broadcastIds;
	}

	public boolean unpack(String ori) {
		this.ori = ori;
		
		String[] items = ori.split("\\|", -1);
		if (items == null || items.length < 5) {
			return false;
		}

		type = Integer.valueOf(items[0]);
		
		from = items[1];
		to = items[2];
		
		source = items[3];
		target = items[4];
		
		if (items.length > 5) {
			rpcid = items[5];
		}		

		try {
			if (items.length > 6) {
				timestamp = Long.valueOf(items[6]);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return true;
	}

	public String pack() {
		ori = new StringJoiner("|")
				.add(type+"")
				.add(from)
				.add(to)
				.add(source == null ? "" : source)
				.add(target == null ? "" : target)
				.add(rpcid == null ? "" : rpcid)
				.add(timestamp+"")
				.toString();							
		
		return ori;
	}
	
	@Override
	public String toString() {
		return pack();
	}
	
	public boolean isValid() {
		return type >= 0 && !HawkOSOperator.isEmptyString(from) && !HawkOSOperator.isEmptyString(to);
	}
}
