package com.hawk.activity.type.impl.redEnvelope.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawk.activity.type.impl.redEnvelope.callback.RecieveCallBack;
import com.hawk.game.protocol.Activity.RedEnvelopeState;

/***
 * 红包基类
 * @author yang.rao
 *
 */
public abstract class BaseRedEnvelope {
	
	/** 红包id **/
	private String id;
	
	/** 红包数量 **/
	private int count;
	
	/** 已经领取的玩家集合 **/
	private Set<String> players;
	
	/** 初始化拆分好的红包 **/
	private ArrayList<OnceRedEnvelope> spiltList = new ArrayList<>();
	
	public BaseRedEnvelope(){
		players = new HashSet<>();
	}
	
	public BaseRedEnvelope(String id, int count){
		this.id = id;
		players = new HashSet<>();
		this.count = count;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public ArrayList<OnceRedEnvelope> getSpiltList() {
		return spiltList;
	}

	public void setSpiltList(ArrayList<OnceRedEnvelope> spiltList) {
		this.spiltList = spiltList;
	}
	
	/****
	 * 添加一次抢红包记录
	 * @param redEnvelope
	 */
	public void addOnceRedEnvelope(OnceRedEnvelope redEnvelope){
		this.spiltList.add(redEnvelope);
	}

	/****
	 * 	是否领取过红包
	 * @return
	 */
	public boolean hasRecieved(String playerId){
		return players.contains(playerId);
	}
	
	public Set<String> getPlayers() {
		return players;
	}

	public void setPlayers(Set<String> players) {
		this.players = players;
	}

	/***
	 * 是否已经派完
	 * @return
	 */
	public boolean hasRecievedOver(){
		return players.size() == count;
	}
	
	public void addReciever(String playerId){
		players.add(playerId);
	}
	
	/***
	 * 领取红包
	 * @param playerId 抢红包的人
	 * @param callback 历史记录回调
	 * @return
	 */
	public abstract void recieve(String playerId, RecieveCallBack callback);
	
	/***
	 * 拆分红包(初始化的时候，将红包拆分)
	 * @param count
	 */
	public abstract void splitBag();
	
	/***
	 * 获取该红包对应的玩家状态
	 * @param playerId
	 * @return
	 */
	public abstract RedEnvelopeState getPlayerState(String playerId);
	
	/***
	 * 获取自己领取该红包详情
	 * @return
	 */
	public abstract OnceRedEnvelope getMyRecieveDetail(String playerId);
	
	/***
	 * 获取该红包领取详情
	 * @return
	 */
	public abstract List<OnceRedEnvelope> getRecieveDetails();
	
	/***
	 * 保存红包到redis
	 * @param key
	 */
	public abstract void save2Redis(String key);
}
