package com.hawk.activity.type.impl.luckyBox.entity;

import java.util.HashSet;
import java.util.List;
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
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.activity.type.impl.luckyBox.LuckyBoxCell;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_lucky_box")
public class LuckyBoxEntity extends HawkDBEntity implements IActivityDataEntity,IExchangeTipEntity {

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
	@Column(name = "cellMsg", nullable = false)
	private String cellMsg;
	
    @IndexProp(id = 5)
	@Column(name = "mustMsg", nullable = false)
	private String mustMsg;
	
    @IndexProp(id = 6)
	@Column(name = "exchangeMsg", nullable = false)
	private String exchangeMsg;
	
    @IndexProp(id = 7)
	@Column(name = "tipMsg", nullable = false)
	private String tipMsg;
	
    @IndexProp(id = 8)
	@Column(name = "buyNeedCount", nullable = false)
	private int buyNeedCount;

	/**
	 *处理必中逻辑使用，达到规定次数以后要必中
	 */
    @IndexProp(id = 9)
	@Column(name = "randomCount", nullable = false)
	private int randomCount;
	
    @IndexProp(id = 10)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 11)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 12)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
    @IndexProp(id = 13)
	@Column(name = "tips", nullable = false)
	private String tips;
	
	@Transient
	private Set<Integer> tipList = new ConcurrentHashSet<Integer>();
	
	@Transient
	private Map<Integer, Integer> exchangeNumMap = new ConcurrentHashMap<>();
	
	@Transient
	private Map<Integer, LuckyBoxCell> cellMap = new ConcurrentHashMap<>();
	/**
	 * 必中
	 */
	@Transient
	private Set<Integer> mustList = new ConcurrentHashSet<Integer>();
	
	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	public LuckyBoxEntity(){}
	
	public LuckyBoxEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}
	
	
	@Override
	public void beforeWrite() {
		this.exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
		this.tipMsg = SerializeHelper.collectionToString(this.tipList,SerializeHelper.BETWEEN_ITEMS);
		this.cellMsg = SerializeHelper.mapToString(cellMap);
		this.mustMsg = SerializeHelper.collectionToString(this.mustList,SerializeHelper.BETWEEN_ITEMS);
		this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
	}

	@Override
	public void afterRead() {
		SerializeHelper.stringToMap(this.exchangeMsg, Integer.class, Integer.class,this.exchangeNumMap);
		SerializeHelper.stringToSet(Integer.class, this.tipMsg, SerializeHelper.BETWEEN_ITEMS,null,this.tipList);
		SerializeHelper.stringToMap(this.cellMsg, Integer.class, LuckyBoxCell.class,this.cellMap);
		SerializeHelper.stringToSet(Integer.class, this.mustMsg, SerializeHelper.BETWEEN_ITEMS,null,this.mustList);
		SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
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
	
	public int getBuyNeedCount() {
		return buyNeedCount;
	}

	public void setBuyNeedCount(int buyNeedCount) {
		this.buyNeedCount = buyNeedCount;
	}
	
	
	
	public int getRandomCount() {
		return randomCount;
	}

	public void setRandomCount(int randomCount) {
		this.randomCount = randomCount;
	}

	public int getCellCount(){
		return this.cellMap.size();
	}
	
	public void addLuckyBoxCell(LuckyBoxCell cell){
		this.cellMap.put(cell.getCellId(),cell);
		this.notifyUpdate();
	}
	
	
	
	public LuckyBoxCell getLuckyBoxCell(int cellId){
		return this.cellMap.getOrDefault(cellId,null);
	}
	
	
	
	public Map<Integer, LuckyBoxCell> getCellMap() {
		return cellMap;
	}

	public void setCellMap(Map<Integer, LuckyBoxCell> cellMap) {
		this.cellMap = cellMap;
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
	
	public void addTip(int id){
		if(this.tipList.contains(id)){
			return;
		}
		this.tipList.add(id);
		this.notifyUpdate();
	}
	
	public void removeTip(int id){
		this.tipList.remove(Integer.valueOf(id));
		this.notifyUpdate();
	}

	public Set<Integer> getTipList() {
		return tipList;
	}

	public void addTipList(List<Integer> tipList) {
		this.tipList.addAll(tipList);
		this.notifyUpdate();
	}
	
	
	
	public void addMust(Set<Integer> addSet){
		this.mustList.addAll(addSet);
	}
	
	
	public Set<Integer> getMustList(){
		return this.mustList;
	}
	
	@Override
	public Set<Integer> getTipSet() {
		return tipSet;
	}

	@Override
	public void setTipSet(Set<Integer> tipSet) {
		this.tipSet = tipSet;
	}
}
