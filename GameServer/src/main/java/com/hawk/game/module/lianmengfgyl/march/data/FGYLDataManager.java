package com.hawk.game.module.lianmengfgyl.march.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.GsConfig;
import com.hawk.game.module.lianmengfgyl.march.entity.FGYLGuildEntity;

public class FGYLDataManager {
	
	/** 活动状态数据*/
	private FGYLActivityStateData stateData;
	/** 联盟活动数据*/
	private Map<String, FGYLGuildEntity> guildDatas = new ConcurrentHashMap<>();
	/** 联盟参加数据*/
	private Map<String, FGYLGuildJoinData> joinDatas = new ConcurrentHashMap<>();
	/** 活动期间榜*/
	private FGYLTermRank termRank = new FGYLTermRank();
	/** 历史荣耀榜*/
	private FGYLHonorRank honorRank = new FGYLHonorRank();
	

	public void init(){
		this.loadToCacheFGYLActivityStateData();
		this.loadToCacheFGYLGuildEntity();
		this.loadToCacheFGYLGuildJoinData();
		this.loadToCacheFGYLRankData();
	}
	

	
	

	/**
	 * 加载活动状态
	 * @return
	 */
	public FGYLActivityStateData loadToCacheFGYLActivityStateData(){
		String serverId = GsConfig.getInstance().getServerId();
		FGYLActivityStateData data = FGYLActivityStateData.loadData(serverId);
		if(data == null){
			data = new FGYLActivityStateData();
			data.setServerId(serverId);
			data.saveRedis();
		}
		this.stateData = data;
		return this.stateData;
	}

	/**
	 * 加载联盟记录
	 */
	public void loadToCacheFGYLGuildEntity(){
		List<FGYLGuildEntity> list = HawkDBManager.getInstance().query("from FGYLGuildEntity where invalid = 0");
		for (FGYLGuildEntity entity : list) {
			guildDatas.put(entity.getGuildId(), entity);
		}
	}
	
	
	/**
	 * 加载报名数据
	 */
	public void loadToCacheFGYLGuildJoinData(){
		String serverId = GsConfig.getInstance().getServerId();
		int termId = this.stateData.getTermId();
		this.joinDatas = FGYLGuildJoinData.loadAll(serverId, termId);
	}
	

	public void loadToCacheFGYLRankData(){
		int termId = this.stateData.getTermId();
		this.termRank.refreshRank(termId);
		List<String> reloads = new ArrayList<>();
		reloads.addAll(this.guildDatas.keySet());
		this.honorRank.checkReload(reloads);
		this.honorRank.refreshRank();
	}
	
	
	public FGYLHonorRank getHonorRank() {
		return honorRank;
	}
	
	public FGYLTermRank getTermRank() {
		return termRank;
	}
	
	
	public void clearGuildJoinData(){
		this.joinDatas = new ConcurrentHashMap<String, FGYLGuildJoinData>();
	}
	
	
	
	
	public FGYLGuildEntity getAndCreateFGYLGuildEntity(String guildId){
		FGYLGuildEntity entity = this.guildDatas.get(guildId);
		if(Objects.nonNull(entity)){
			return entity;
		}
		entity = new FGYLGuildEntity();
		entity.setGuildId(guildId);
		entity.create();
		this.guildDatas.put(guildId, entity);
		return entity;
	}
	
	
	
	public FGYLActivityStateData getStateData() {
		return stateData;
	}
	
	public FGYLGuildJoinData getFGYLGuildJoinData(String guildId){
		return this.joinDatas.get(guildId);
	}

	public Map<String, FGYLGuildJoinData> getAllFGYLGuildJoinData() {
		return joinDatas;
	}
	
	public FGYLGuildEntity getFGYLGuildEntity(String guild){
		return this.guildDatas.get(guild);
	}
	
	
	public void addFGYLGuildJoinData(FGYLGuildJoinData data){
		this.joinDatas.put(data.getGuildId(), data);
	}

	public void addFGYLGuildEntity(FGYLGuildEntity entity){
		this.guildDatas.put(entity.getGuildId(),entity);
	}
	
	public int getGuildPassLevel(String guildId){
		if(HawkOSOperator.isEmptyString(guildId)){
			return 0;
		}
		FGYLGuildEntity entity = this.guildDatas.get(guildId);
		if(Objects.isNull(entity)){
			return 0;
		}
		return entity.getPassLevel();
	}
}
