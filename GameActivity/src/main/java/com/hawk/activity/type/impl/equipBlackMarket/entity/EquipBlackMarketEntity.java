package com.hawk.activity.type.impl.equipBlackMarket.entity;

import java.util.HashSet;
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

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 充值双倍活动数据存储
 * @author PhilChen
 *
 */
@Entity
@Table(name = "activity_equip_black_market")
public class EquipBlackMarketEntity extends HawkDBEntity implements IActivityDataEntity {
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
	
	/** 购买礼包ID列表 */
    @IndexProp(id = 4)
	@Column(name = "buyPackageIds", nullable = false)
	private String buyPackageIds;
	
	/** 上一次购买的礼包ID*/
    @IndexProp(id = 5)
	@Column(name = "lastBuyPackage", nullable = false)
	private String lastBuyPackage;
	
	/** 炼化次数 */
    @IndexProp(id = 6)
	@Column(name = "refines", nullable = false)
	private String refines;
	
    @IndexProp(id = 7)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private Set<String> buyPackageSet = new HashSet<>();
	
	@Transient
	private Map<Integer,Integer> refineMap = new ConcurrentHashMap<Integer,Integer>();
	
	
	
	public EquipBlackMarketEntity() {
		
	}
	
	public EquipBlackMarketEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.refines = "";
		this.buyPackageIds = "";
		this.lastBuyPackage = "";
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	
	public String getBuyPackageIds() {
		return buyPackageIds;
	}
	
	
	public Set<String> getBuyPackageSet() {
		return buyPackageSet;
	}
	
	

	public Map<Integer, Integer> getRefineMap() {
		return refineMap;
	}

	public String getLastBuyPackage() {
		return lastBuyPackage;
	}

	public void setLastBuyPackage(String lastBuyPackage) {
		this.lastBuyPackage = lastBuyPackage;
		this.notifyUpdate();
	}

	@Override
	public void beforeWrite() {
		this.buyPackageIds = SerializeHelper.collectionToString(this.buyPackageSet, 
				SerializeHelper.ATTRIBUTE_SPLIT);
		this.refines = SerializeHelper.mapToString(this.refineMap);
		
	}

	@Override
	public void afterRead() {
		this.buyPackageSet = SerializeHelper.stringToSet(String.class, 
				this.buyPackageIds, SerializeHelper.ATTRIBUTE_SPLIT);
		this.refineMap = SerializeHelper.stringToMap(this.refines, Integer.class, 
				Integer.class, this.refineMap);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	
	public void resetBuyPackages(){
		this.buyPackageSet = new HashSet<String>();
		this.notifyUpdate();
	}
	
	
	

	public void addGoodsId(String goodsId){
		this.buyPackageSet.add(goodsId);
		this.lastBuyPackage = goodsId;
		this.notifyUpdate();
	}
	
	
	public boolean isBuyGift(String gId){
		return this.buyPackageSet.contains(gId);
	}
	
	public int getRefineCount(int id){
		if(this.refineMap.containsKey(id)){
			return this.refineMap.get(id);
		}
		return 0;
	}
	
	public void addRefineCount(int id,int count){
		if(this.refineMap.containsKey(id)){
			count += this.refineMap.get(id);
		}
		this.refineMap.put(id, count);
		this.notifyUpdate();
	}
}
