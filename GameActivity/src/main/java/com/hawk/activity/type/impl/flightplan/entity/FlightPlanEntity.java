package com.hawk.activity.type.impl.flightplan.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_flight_plan")
public class FlightPlanEntity  extends AchieveActivityEntity implements IActivityDataEntity,IExchangeTipEntity {

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
	/**
	 * 停留的格子ID
	 */
    @IndexProp(id = 4)
	@Column(name = "cellId", nullable = false)
	private int cellId;
	
	/** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

	/**
	 * 商品兑换次数
	 */
    @IndexProp(id = 6)
	@Column(name = "goodsExchange", nullable = false)
	private String goodsExchange;

    @IndexProp(id = 7)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
    @IndexProp(id = 10)
	@Column(name = "tips", nullable = false)
	private String tips;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	@Transient
	private Map<Integer, Integer> goodsExchangeMap = new HashMap<Integer, Integer>();

	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	public FlightPlanEntity() {
	}
	
	public FlightPlanEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
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
	
	public int getCellId() {
		return cellId;
	}
	public void setCellId(int cellId) {
		this.cellId = cellId;
	}
	
	public String getAchieveItems() {
		return achieveItems;
	}
	
	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public Map<Integer, Integer> getGoodsExchangeMap() {
		return goodsExchangeMap;
	}
	
	public int getExchangeTimes(int goodsId) {
		return goodsExchangeMap.getOrDefault(goodsId, 0);
	}

	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.goodsExchange = SerializeHelper.mapToString(goodsExchangeMap);
		this.tips = SerializeHelper.collectionToString(this.tipSet,SerializeHelper.ATTRIBUTE_SPLIT);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.goodsExchangeMap = SerializeHelper.stringToMap(goodsExchange);
		this.tipSet.clear();
		SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
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
	public Set<Integer> getTipSet() {
		return tipSet;
	}

	@Override
	public void setTipSet(Set<Integer> tipSet) {
		this.tipSet = tipSet;
	}

}
