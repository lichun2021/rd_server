package com.hawk.activity.type.impl.celebrationShop.entity;

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

import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;
/**
 * 周年商城
 * @author luke
 */
@Entity
@Table(name = "activity_celebration_shop")
public class CelebrationShopEntity  extends HawkDBEntity implements IActivityDataEntity, IExchangeTipEntity {

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
	@Column(name = "exchange", nullable = false)
	private String exchange;
	
    @IndexProp(id = 5)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 6)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 7)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

    @IndexProp(id = 8)
	@Column(name = "tips", nullable = false)
	private String tips;
	
	/** 兑换列表*/
	@Transient
	private Map<Integer,Integer> exchangeMap = new ConcurrentHashMap<>();

	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	public CelebrationShopEntity() {
	}
	
	public CelebrationShopEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
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
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	@Override
	public void beforeWrite() {
		exchange = SerializeHelper.mapToString(exchangeMap);
		this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
	}
	
	@Override
	public void afterRead() {
		exchangeMap = SerializeHelper.stringToMap(exchange, Integer.class, Integer.class);
		SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public Map<Integer, Integer> getExchangeMap() {
		return exchangeMap;
	}

	public void setExchangeMap(Map<Integer, Integer> exchangeMap) {
		this.exchangeMap = exchangeMap;
	}
	
	public void putExchangeMap(int itemId,int itemNum){
		this.exchangeMap.put(itemId, itemNum);
	}

	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
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
