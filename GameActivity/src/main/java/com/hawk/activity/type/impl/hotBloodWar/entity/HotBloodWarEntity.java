package com.hawk.activity.type.impl.hotBloodWar.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.JSONArray;
import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * @author LENOVO
 *
 */
/**
 * @author LENOVO
 *
 */
@Entity
@Table(name="activity_hot_blood_war")
public class HotBloodWarEntity extends ActivityDataEntity{

	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId = null;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
    @IndexProp(id = 4)
  	@Column(name = "cureArmyInfos", nullable = false)
  	private String cureArmyInfos;
    
    @IndexProp(id = 5)
  	@Column(name = "cureFirstType", nullable = false)
  	private int cureFirstType;
    
    @IndexProp(id = 6)
	@Column(name = "cureArmyId", nullable = false)
	private int cureArmyId;
	
    @IndexProp(id = 7)
	@Column(name = "cureArmyStartTime", nullable = false)
	private long cureArmyStartTime;
    
    @IndexProp(id = 8)
  	@Column(name = "cureArmySpeedTime", nullable = false)
    private long cureArmySpeedTime;
    
    @IndexProp(id = 9)
  	@Column(name = "cureArmyCalTime", nullable = false)
    private long cureArmyCalTime;
    
    
    @IndexProp(id = 10)
    @Column(name = "loginDays", nullable = false)
    private String loginDays;
    
	/** 活动成就项数据 */
    @IndexProp(id = 11)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
    
    
    @IndexProp(id = 12)
   	@Column(name = "selfHurtInfo", nullable = false)
    private String selfHurtInfo;
    
    @IndexProp(id =13)
   	@Column(name = "enemyKillInfo", nullable = false)
    private String enemyKillInfo;
    
    @IndexProp(id = 14)
   	@Column(name = "enemyKillScore", nullable = false)
    private long enemyKillScore;
    
    @IndexProp(id = 15)
   	@Column(name = "selfHurtScore", nullable = false)
    private long selfHurtScore;
    
    @IndexProp(id = 16)
   	@Column(name = "finishCheck", nullable = false)
    private int finishCheck;
    
    @IndexProp(id = 17)
   	@Column(name = "initTime", nullable = false)
    private long initTime;
	
    @IndexProp(id = 18)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 19)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 20)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();
	
	@Transient
	private Map<Integer,CureArmyData> cureArmyMap = new ConcurrentHashMap<Integer,CureArmyData>();
	
	@Transient
	private Map<Integer,Long> enemyKillMap = new ConcurrentHashMap<>();
	
	@Transient
	private Map<Integer,Long> selfHurtMap = new ConcurrentHashMap<>();
	
	
	public HotBloodWarEntity() {
		
	}
	
	public HotBloodWarEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.cureArmyInfos = "";
		this.achieveItems = "";
		this.enemyKillInfo = "";
		this.selfHurtInfo = "";
		this.loginDays = "";
	}
	
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList);
		this.cureArmyInfos = this.serializeArmyData();
		this.enemyKillInfo = SerializeHelper.mapToString(this.enemyKillMap);
		this.selfHurtInfo =  SerializeHelper.mapToString(this.selfHurtMap);
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.cureArmyMap = this.unSerializeArmyData();
		this.enemyKillMap = SerializeHelper.stringToMap(this.enemyKillInfo, Integer.class, Long.class);
		this.selfHurtMap = SerializeHelper.stringToMap(this.selfHurtInfo, Integer.class, Long.class);
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	protected void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
	public String getPlayerId() {
		return playerId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public long getInitTime() {
		return initTime;
	}
	
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}
	
	
	public Map<Integer, CureArmyData> getCureArmyMap() {
		return cureArmyMap;
	}

	public void setCureFirstType(int cureFirstType) {
		this.cureFirstType = cureFirstType;
	}
	
	public int getCureFirstType() {
		return cureFirstType;
	}
	
	
	public int getCureArmyId() {
		return cureArmyId;
	}
	
	public void setCureArmyId(int cureArmyId) {
		this.cureArmyId = cureArmyId;
	}
	
	public long getCureArmyStartTime() {
		return cureArmyStartTime;
	}
	
	public void setCureArmyStartTime(long cureArmyStartTime) {
		this.cureArmyStartTime = cureArmyStartTime;
	}
	
	
	public long getCureArmyCalTime() {
		return cureArmyCalTime;
	}
	
	public void setCureArmyCalTime(long cureArmyCalTime) {
		this.cureArmyCalTime = cureArmyCalTime;
	}
	
	
	public void setCureArmySpeedTime(long cureArmySpeedTime) {
		this.cureArmySpeedTime = cureArmySpeedTime;
	}
	
	
	public long getCureArmySpeedTime() {
		return cureArmySpeedTime;
	}
	
		
	public Map<Integer, Long> getEnemyKillMap() {
		return enemyKillMap;
	}
	
	public Map<Integer, Long> getSelfHurtMap() {
		return selfHurtMap;
	}

	public long getEnemyKillScore() {
		return enemyKillScore;
	}
	
	public void setEnemyKillScore(long enemyKillScore) {
		this.enemyKillScore = enemyKillScore;
	}
	
	public long getSelfHurtScore() {
		return selfHurtScore;
	}
	
	
	public void setSelfHurtScore(long selfHurtScore) {
		this.selfHurtScore = selfHurtScore;
	}
	
	
	
	public int getFinishCheck() {
		return finishCheck;
	}
	
	public void setFinishCheck(int finishCheck) {
		this.finishCheck = finishCheck;
	}
	
	@Override
	public void setLoginDaysStr(String loginDays) {
        this.loginDays = loginDays;
    }
    
    @Override
	public String getLoginDaysStr() {
		return this.loginDays;
	}

	
	public String serializeArmyData(){
		JSONArray arr = new JSONArray();
		for(CureArmyData data : cureArmyMap.values()){
			arr.add(data.serializeData());
		}
		return arr.toJSONString();
	}
	
	public Map<Integer,CureArmyData> unSerializeArmyData(){
		Map<Integer,CureArmyData> map = new ConcurrentHashMap<Integer,CureArmyData>();
		if(HawkOSOperator.isEmptyString(this.cureArmyInfos)){
			return map;
		}
		JSONArray arr = JSONArray.parseArray(this.cureArmyInfos);
		for(int i=0;i<arr.size();i++){
			String data = arr.getString(i);
			CureArmyData army = new CureArmyData();
			army.unSerializeData(data);
			map.put(army.getArmyId(), army);
		}
		return map;
	}
	
	
	public void addEnemyKillCount(int armyId,int armyCnt){
		if(armyCnt <= 0){
			return;
		}
		long cnt = this.enemyKillMap.getOrDefault(armyId, 0l) + armyCnt;
		this.enemyKillMap.put(armyId, cnt);
		this.notifyUpdate();
	}
	
	
	public void addSelfHurtCount(int armyId,int armyCnt){
		if(armyCnt <= 0){
			return;
		}
		long cnt = this.selfHurtMap.getOrDefault(armyId, 0l) + armyCnt;
		this.selfHurtMap.put(armyId, cnt);
		this.notifyUpdate();
	}
	
	
	public boolean hasCureArmy(){
		for(CureArmyData data : this.cureArmyMap.values()){
			if(data.getCureCount() > 0){
				return true;
			}
		}
		return false;
	}

}
