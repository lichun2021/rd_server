package com.hawk.activity.type.impl.machineLab.entity;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_machine_lab")
public class MachineLabEntity extends HawkDBEntity implements IActivityDataEntity {

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
	@Column(name = "initTime", nullable = false)
	private long initTime;
	
    @IndexProp(id = 5)
	@Column(name = "playerServer", nullable = false)
	private String playerServer;
	
    @IndexProp(id = 6)
	@Column(name = "playerExp", nullable = false)
	private int playerExp;
	
    @IndexProp(id = 7)
	@Column(name = "stormingPointTotal", nullable = false)
	private int stormingPointTotal;
	
	
    @IndexProp(id = 8)
	@Column(name = "buyGift", nullable = false)
	private int buyGift;
	
    @IndexProp(id = 9)
	@Column(name = "serverRewardLevel", nullable = false)
	private int serverRewardLevel;
	
    @IndexProp(id = 10)
	@Column(name = "playerRewardLevel", nullable = false)
	private int playerRewardLevel;
	
	
    @IndexProp(id = 11)
	@Column(name = "playerAdvRewardLevel", nullable = false)
	private int playerAdvRewardLevel;
	
	
    @IndexProp(id = 12)
	@Column(name = "dropMsg", nullable = false)
	private String dropMsg;
	
    @IndexProp(id = 13)
	@Column(name = "exchangeMsg", nullable = false)
	private String exchangeMsg;

    @IndexProp(id = 14)
	@Column(name = "careIgnore", nullable = false)
	private String careIgnore;
	
    @IndexProp(id = 15)
	@Column(name = "supplementTime", nullable = false)
	private long supplementTime;
	
    @IndexProp(id = 16)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 17)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 18)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	@Transient
	private Map<Integer, Integer> exchangeNumMap = new ConcurrentHashMap<>();
	
	@Transient
	private Map<Integer, Integer> dropNumMap = new ConcurrentHashMap<>();
	
	@Transient
	private Set<Integer> careIgnoreList = new ConcurrentHashSet<Integer>();
	
	public MachineLabEntity(){}
	
	public MachineLabEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.careIgnore = "";
		this.exchangeMsg = "";
		this.dropMsg = "";
		this.playerServer = "";
	}
	
	
	@Override
	public void beforeWrite() {
		this.exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
		this.careIgnore = SerializeHelper.collectionToString(this.careIgnoreList,SerializeHelper.ATTRIBUTE_SPLIT);
		this.dropMsg = SerializeHelper.mapToString(dropNumMap);
	}

	@Override
	public void afterRead() {
		SerializeHelper.stringToMap(this.exchangeMsg, Integer.class, Integer.class,this.exchangeNumMap);
		SerializeHelper.stringToSet(Integer.class, this.careIgnore, SerializeHelper.ATTRIBUTE_SPLIT,null,this.careIgnoreList);
		SerializeHelper.stringToMap(this.dropMsg, Integer.class, Integer.class,this.dropNumMap);
		
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public String getPlayerId() {
		return playerId;
	}
	
	
	
	public long getInitTime() {
		return initTime;
	}
	
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}
	
	public String getPlayerServer() {
		return playerServer;
	}
	
	public void setPlayerServer(String playerServer) {
		this.playerServer = playerServer;
	}
	
	public int getPlayerExp() {
		return playerExp;
	}
	
	public int getPlayerRewardLevel() {
		return playerRewardLevel;
	}
	
	public void setPlayerRewardLevel(int playerRewardLevel) {
		this.playerRewardLevel = playerRewardLevel;
	}
	
	
	public int getPlayerAdvRewardLevel() {
		return playerAdvRewardLevel;
	}
	
	public void setPlayerAdvRewardLevel(int playerAdvRewardLevel) {
		this.playerAdvRewardLevel = playerAdvRewardLevel;
	}
	
	public int getServerRewardLevel() {
		return serverRewardLevel;
	}
	
	public void setServerRewardLevel(int serverRewardLevel) {
		this.serverRewardLevel = serverRewardLevel;
	}
	
	public int getStormingPointTotal() {
		return stormingPointTotal;
	}
	
	public void setStormingPointTotal(int stormingPointTotal) {
		this.stormingPointTotal = stormingPointTotal;
	}
	
	public void setBuyGift(int buyGift) {
		this.buyGift = buyGift;
	}
	
	public int getBuyGift() {
		return buyGift;
	}
	
	
	public Map<Integer, Integer> getExchangeNumMap() {
		return exchangeNumMap;
	}
	
	
	public int getExchangeCount(int exchangeId){
		return this.exchangeNumMap.getOrDefault(exchangeId,0);
	}
	
	
	public void addExchangeCount(int eid,int count){
		if(count <=0){
			return;
		}
		count += this.getExchangeCount(eid);
		this.exchangeNumMap.put(eid, count);
		this.notifyUpdate();
	}
	
	public void resetExchange(){
		this.exchangeNumMap.clear();
		this.notifyUpdate();
	}
	
	
	public Set<Integer> getCareIgnoreList() {
		return careIgnoreList;
	}
	
	
	public void addCareIgnore(int id){
		if(!careIgnoreList.contains(id)){
			careIgnoreList.add(id);
		}
		this.notifyUpdate();
	}
	
	public void removeCareIgnore(int id){
		careIgnoreList.remove(id);
		this.notifyUpdate();
	}
	
	
	public void addPlayerExp(int add){
		this.playerExp += add;
		this.notifyUpdate();
	}
	
	
	public Map<Integer, Integer> getDropNumMap() {
		return dropNumMap;
	}
	
	public int getDropCount(int dropId){
		return this.dropNumMap.getOrDefault(dropId,0);
	}
	
	
	public void addDropCount(int dropId,int count){
		if(count <=0){
			return;
		}
		count += this.getDropCount(dropId);
		this.dropNumMap.put(dropId, count);
		this.notifyUpdate();
	}
	
	public long getSupplementTime() {
		return supplementTime;
	}
	
	public void setSupplementTime(long supplementTime) {
		this.supplementTime = supplementTime;
	}
}
