package com.hawk.game.lianmengxzq.worldpoint.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.os.HawkOSOperator;
import org.hawk.serializer.HawkSerializer;

import com.google.protobuf.ByteString;
import com.hawk.game.GsConfig;
import com.hawk.game.config.XZQPointCfg;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.protocol.WorldPoint.PBSerializeData;

/***
 * 点的报名 占领 刻字等信息
 * @author lwt
 * @date 2021年9月8日
 */
public class ZXQPointInfoData {
	/** 阶段数据*/
	private String stateStr;
	/** 阶段编号*/
	private int stateOrdinal;
	/** 是否有npc*/
	private boolean hasNpc;
	/**当前所属盟队长信息 */
	private XZQCommander commander;
	/** 前一次占领联盟ID*/
	private String lastControlGuild;
	/** 报名工会列表*/
	private List<String> signupGuillds = new CopyOnWriteArrayList<>();
	/** 攻占的历史联盟 */
	private Set<String> occupyHistorySet = new ConcurrentHashSet<>();
	/** 刻字列表*/
	private List<XZQBuildRecord> records = new CopyOnWriteArrayList<>(); 


	
	
	public void loadXZQBuildRecord(XZQPointCfg cfg){
		if(cfg == null){
			return;
		}
		int pointId = cfg.getId();
		List<String> serverIds = new ArrayList<>();
		String serverId = GsConfig.getInstance().getServerId();
		List<String> mlist = AssembleDataManager.getInstance().getMergedServerList(serverId);
		if(mlist == null){
			serverIds.add(serverId);
		}else{
			serverIds.addAll(mlist);
		}
		for(String sid : serverIds){
			XZQBuildRecord record = XZQBuildRecord.load(sid,pointId);
			if(record == null){
				continue;
			}
			this.records.add(record);
		}
	}
	
	public void clearXZQBuildRecord(XZQPointCfg cfg){
		if(cfg == null){
			return;
		}
		int pointId = cfg.getId();
		List<String> serverIds = new ArrayList<>();
		String serverId = GsConfig.getInstance().getServerId();
		List<String> mlist = AssembleDataManager.getInstance().getMergedServerList(serverId);
		if(mlist == null){
			serverIds.add(serverId);
		}else{
			serverIds.addAll(mlist);
		}
		for(String sid : serverIds){
			XZQBuildRecord.clear(sid,pointId);
		}
		this.records.clear();
	}
	
	public PBSerializeData serializ() {
		PBSerializeData.Builder obj = PBSerializeData.newBuilder();
		if (Objects.nonNull(stateStr)) {
			obj.setParam1(serialize(stateStr));
		}
		obj.setParam2(serialize(stateOrdinal));
		obj.setParam3(serialize(hasNpc));
		if (!occupyHistorySet.isEmpty()) {
			obj.setParam4(serialize(occupyHistorySet));
		}
		if (Objects.nonNull(commander)) {
			obj.setParam5(serialize(commander));
		}
		if (!signupGuillds.isEmpty()) {
			obj.setParam6(serialize(signupGuillds));
		}
		if (!HawkOSOperator.isEmptyString(lastControlGuild)) {
			obj.setParam7(serialize(lastControlGuild));
		}
		return obj.build();
	}

	@SuppressWarnings("unchecked")
	public void mergeFrom(PBSerializeData data) {
		if (data.hasParam1()) {
			this.stateStr = deserialize(data.getParam1(), String.class);
		}
		this.stateOrdinal = deserialize(data.getParam2(), int.class);
		this.hasNpc = deserialize(data.getParam3(), boolean.class);
		if (data.hasParam4()) {
			this.occupyHistorySet = deserialize(data.getParam4(), ConcurrentHashSet.class);
		}
		if (data.hasParam5()) {
			commander = deserialize(data.getParam5(), XZQCommander.class);
		}
		if(data.hasParam6()){
			this.signupGuillds = deserialize(data.getParam6(), CopyOnWriteArrayList.class);
		}
		if(data.hasParam7()){
			this.lastControlGuild = deserialize(data.getParam7(), String.class);
		}
	}

	private <T> ByteString serialize(T value) {
		return ByteString.copyFrom(HawkSerializer.serialize(value));
	}

	private <T> T deserialize(ByteString bytes, Class<T> type) {
		return HawkSerializer.deserialize(bytes.toByteArray(), type);
	}

	public String getStateStr() {
		return stateStr;
	}

	public void setStateStr(String stateStr) {
		this.stateStr = stateStr;
	}

	public int getStateOrdinal() {
		return stateOrdinal;
	}

	public void setStateOrdinal(int stateOrdinal) {
		this.stateOrdinal = stateOrdinal;
	}

	public void setHasNpc(boolean b) {
		hasNpc = b;
	}

	public boolean isHasNpc() {
		return hasNpc;
	}

	public Set<String> getOccupyHistorySet() {
		return occupyHistorySet;
	}

	public void setOccupyHistorySet(HashSet<String> occupyHistorySet) {
		this.occupyHistorySet = occupyHistorySet;
	}

	public XZQCommander getCommander() {
		return commander;
	}

	public void setCommander(XZQCommander commander) {
		this.commander = commander;
	}

	public List<String> getSignupGuillds() {
		return signupGuillds;
	}

	public void setSignupGuillds(List<String> signupGuillds) {
		this.signupGuillds = signupGuillds;
	}

	public String getGuildControl() {
		if(this.commander == null){
			return null;
		}
		return this.commander.getPlayerGuildId();
	}

	public XZQBuildRecord getRecord(String serverId){
		if(this.records == null){
			return null;
		}
		for(XZQBuildRecord re : this.records){
			if(re.getServerId().equals(serverId)){
				return re;
			}
		}
		return null;
	}
	public void addRecord(XZQBuildRecord record) {
		this.records.add(record);
	}

	public List<XZQBuildRecord> getXZQBuildRecords(){
		return this.records;
	}
	
	public String getLastControlGuild() {
		return lastControlGuild;
	}

	public void setLastControlGuild(String lastControlGuild) {
		this.lastControlGuild = lastControlGuild;
	}


	
	
}
