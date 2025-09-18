package com.hawk.game.entity;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkRand;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationConstructionQuestCfg;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.nation.construction.model.NationalBuildQuestModel;
import com.hawk.game.protocol.National.NationBuildQuestType;
import com.hawk.game.protocol.National.NationbuildingType;

/**
 * 建设任务实体
 * @author zhenyu.shang
 * @since 2022年3月31日
 */
 @Entity
 @Table(name = "nation_build_quest")
public class NationBuildQuestEntity extends HawkDBEntity {
	
	@Id
	@Column(name = "playerId")
    @IndexProp(id = 1)
	private String playerId;
	
	@Column(name = "nationQuestType")
    @IndexProp(id = 2)
	private int nationQuestType;
	
	@Column(name = "refreshCount")
    @IndexProp(id = 3)
	private int refreshCount;
	
	@Column(name = "questTimes")
    @IndexProp(id = 4)
	private int questTimes;
	
	@Column(name = "questInfo")
    @IndexProp(id = 5)
	private String questInfo;
	
	@Column(name = "resetTime")
    @IndexProp(id = 6)
	private long resetTime;
	
	@Column(name = "createTime")
    @IndexProp(id = 7)
	private long createTime;

	@Column(name = "updateTime")
    @IndexProp(id = 8)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 9)
	private boolean invalid;
	
	
	/** 
	 * 所有任务，任务id, 任务实体
	 * (存在并发情况，所以这里需要用concurrent)
	 */
	@Transient
	private Map<String, NationalBuildQuestModel> questsMap = new ConcurrentHashMap<String, NationalBuildQuestModel>();
		
	
	@Override
	public void beforeWrite() {
		this.questInfo = JSONObject.toJSONString(this.questsMap);
	}
	
	@Override
	public void afterRead() {
		if(this.questInfo != null){
			this.questsMap = JSONObject.parseObject(this.questInfo, new TypeReference<Map<String, NationalBuildQuestModel>>(){});
		}
	}

	public String getPlayerId() {
		return playerId;
	}

	public Map<String, NationalBuildQuestModel> getQuestsMap() {
		return questsMap;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getNationQuestType() {
		return nationQuestType;
	}
	
	public NationBuildQuestType getQuestType(){
		return NationBuildQuestType.valueOf(nationQuestType);
	}

	public void setNationQuestType(int nationQuestType) {
		this.nationQuestType = nationQuestType;
	}

	public int getRefreshCount() {
		return refreshCount;
	}

	public void setRefreshCount(int refreshCount) {
		this.refreshCount = refreshCount;
	}

	public int getQuestTimes() {
		return questTimes;
	}

	public void setQuestTimes(int questTimes) {
		this.questTimes = questTimes;
	}

	public String getQuestInfo() {
		return questInfo;
	}

	public void setQuestInfo(String questInfo) {
		this.questInfo = questInfo;
	}

	public long getResetTime() {
		return resetTime;
	}

	public void setResetTime(long resetTime) {
		this.resetTime = resetTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public String getPrimaryKey() {
		return this.playerId;
	}
	
	@Override
	public String getOwnerKey() {
		return this.playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}
	
	public void incrRefreshCount(){
		this.refreshCount++;
		this.notifyUpdate();
	}
	
	/**
	 * 计算当前已经出征的队列数和剩余可做任务数
	 * @return
	 */
	public int[] calcAlreadyMarchCount(){
		int count = 0;
		int left = 0;
		for (NationalBuildQuestModel model : questsMap.values()) {
			if(model.getMarchId() != null) {
				count++;
			} else {
				left++;
			}
		}
		return new int[]{count, left};
	}
	
	
	public boolean checkRd(){
		int[] res = calcAlreadyMarchCount();
		int leftQuestTimes = getQuestTimes();
		// 当前剩余次数减去已经出征的队列数
		if(leftQuestTimes - res[0] > 0 && res[1] > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取可刷新任务的建筑id
	 * @return
	 */
	public List<Integer> getCanRefreshBuilding(){
		List<Integer> allBuildIds = new ArrayList<>();
		for (NationalBuilding building : NationService.getInstance().getAllNationalBuilding().values()) {
			// 只有开放的建筑可以刷出
			if(building.isOpen()){
				allBuildIds.add(building.getBuildType().getNumber());
			}
		}
		List<Integer> list = new ArrayList<Integer>();
		// 1. 先找出有任务的建筑
		for (NationalBuildQuestModel model : questsMap.values()) {
			if(model.getMarchId() != null){
				list.add(model.getBuildId());
			}
		}
		// 排除已经有正在做任务的
		allBuildIds.removeAll(list);
		return allBuildIds;
	}
	
	public void refreshQuest(int constructionLvl, List<Integer> allBuildIds, boolean needRand){
		// 1.随出对应建筑
		if(needRand) {
			Collections.shuffle(allBuildIds);
		}
		// 2.删除没有做的任务
		Iterator<Entry<String, NationalBuildQuestModel>> it = questsMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, NationalBuildQuestModel> entry = it.next();
			if(entry.getValue().getMarchId() == null) {
				it.remove();
			}
		}
		// 3.随机任务
		doRandomQuest(constructionLvl, allBuildIds);
	}

	private void doRandomQuest(int constructionLvl, List<Integer> allBuildIds) {
		// 3.根据建设处等级获取任务池
		ConfigIterator<NationConstructionQuestCfg> it = HawkConfigManager.getInstance().getConfigIterator(NationConstructionQuestCfg.class);
		List<Integer> questIds = new ArrayList<>();
		List<Integer> weights = new ArrayList<>();
		while (it.hasNext()) {
			NationConstructionQuestCfg cfg = it.next();
			if(cfg.getBuildingLvl() == constructionLvl) {
				questIds.add(cfg.getAchieveId());
				weights.add(cfg.getWeight());
			}
		}
		// 4. 再给每个建筑随机出任务
		int maxfreshQuest = NationConstCfg.getInstance().getRefreshTimes();
		BitSet flag = new BitSet();
		for (int i = 0; i < maxfreshQuest && i < allBuildIds.size(); i++) {
			Integer questCfgId = HawkRand.randomWeightObject(questIds, weights, flag);
			
			String questId = HawkUUIDGenerator.genUUID();
			NationalBuildQuestModel model = new NationalBuildQuestModel();
			model.setBuildId(allBuildIds.get(i));
			model.setPlayerId(playerId);
			model.setQuestCfgId(questCfgId);
			model.setQuestId(questId);
			// 存入全局
			questsMap.put(questId, model);
		}
		// 更新数据库
		this.notifyUpdate();
	}
	
	/**
	 * 随机任务并且存储
	 */
	public boolean initRandomQuest(int constructionLvl){
		// 初始化刷固定的建筑建设处，任务中心，国家医院
		List<Integer> allBuildIds = new ArrayList<Integer>();
		allBuildIds.add(NationbuildingType.NATION_BUILDING_CENTER_VALUE);
		allBuildIds.add(NationbuildingType.NATION_QUEST_CENTER_VALUE);
		allBuildIds.add(NationbuildingType.NATION_HOSPITAL_VALUE);
		this.refreshQuest(constructionLvl, allBuildIds, false);
		return true;
	}
	
	/**
	 * 清除任务信息（必须在玩家队列执行）
	 */
	public void removeQuest(String questId){
		this.questsMap.remove(questId);
		this.notifyUpdate();
	}
	
	/**
	 * 取消任务
	 * @param questId
	 */
	public void cancelQuest(String questId, int currentPercent){
		NationalBuildQuestModel model = this.questsMap.get(questId);
		model.setMarchId(null);
		model.addCurrentProcess(currentPercent);
		
		this.notifyUpdate();
	}
	
	/**
	 * 做完任务后新增一个任务
	 */
	public void replaceOverQuest(int constructionLvl){
		// 判断当前空闲任务是不是到达最大值
		int currIdleCount = currentIdleQuest();
		if(currIdleCount >= NationConstCfg.getInstance().getRefreshTimes()) {
			return;
		}
		// 拿到所有建筑id
		List<Integer> allBuildIds = new ArrayList<>();
		for (NationalBuilding building : NationService.getInstance().getAllNationalBuilding().values()) {
			// 只有开放的建筑可以刷出
			if(building.isOpen()){
				allBuildIds.add(building.getBuildType().getNumber());
			}
		}
		List<Integer> list = new ArrayList<Integer>();
		// 排除有任务的
		for (NationalBuildQuestModel model : questsMap.values()) {
			list.add(model.getBuildId());
		}
		// 排除已经有正在做任务的
		allBuildIds.removeAll(list);
		// 如果没有空建筑，直接返回
		if(allBuildIds.isEmpty()) {
			return;
		}
		// 随机一下
		Collections.shuffle(allBuildIds);
		// 只刷一个
		List<Integer> newBuildings = new ArrayList<>();
		newBuildings.add(allBuildIds.get(0));
		this.doRandomQuest(constructionLvl, newBuildings);
	}
	
	/**
	 * 获取当前闲置任务数量
	 * @return
	 */
	public int currentIdleQuest() {
		int count = 0;
		for (NationalBuildQuestModel model : questsMap.values()) {
			if(model.getMarchId() == null) {
				count++;
			}
		}
		return count;
	}
}
