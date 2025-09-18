package com.hawk.activity.type.impl.plantweaponback.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_plant_weapon_back")
public class PlantWeaponBackEntity extends HawkDBEntity implements IActivityDataEntity, IExchangeTipEntity{
	
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false)
	private String playerId;

    @IndexProp(id = 3)
	@Column(name="termId", nullable = false)
	private int termId;
    /**
     * 已抽奖次数
     */
    @IndexProp(id = 4)
   	@Column(name="drawTimes", nullable = false)
   	private int drawTimes;
    /**
     * 每日免费抽奖次数
     */
    @IndexProp(id = 5)
   	@Column(name="freeTimes", nullable = false)
   	private int freeTimes;
    
    /** 商店已购买次数信息 */
    @IndexProp(id = 6)
   	@Column(name = "shopItems", nullable = false)
   	private String shopItems = "";
    
    /** 成就数据 **/
    @IndexProp(id = 7)
	@Column(name="achieveItems", nullable = false)
	private String achieveItems;

    @IndexProp(id = 8)
    @Column(name="dayTime", nullable = false)
	private long dayTime;

    @IndexProp(id = 9)
   	@Column(name="buff", nullable = false)
   	private int buff;
    
    @IndexProp(id = 10)
    @Column(name="createTime", nullable = false)
   	private long createTime;

    @IndexProp(id = 11)
    @Column(name="updateTime", nullable = false)
  	private long updateTime;
    
    @IndexProp(id = 12)
	@Column(name="invalid", nullable = false)
	private boolean invalid;
    
    @IndexProp(id = 13)
	@Column(name = "tips", nullable = false)
	private String tips;
    
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

	/** 商店商品购买数量  */
	@Transient
	private Map<Integer, Integer> shopItemMap = new HashMap<>();
	
	@Transient
	private Set<Integer> tipSet = new HashSet<>();
	
	
	public PlantWeaponBackEntity(){}
	
	public PlantWeaponBackEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		this.shopItems = SerializeHelper.mapToString(this.shopItemMap);
		this.tips = SerializeHelper.collectionToString(this.tipSet, SerializeHelper.ATTRIBUTE_SPLIT);
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public void afterRead() {
		if (!itemList.isEmpty()) {
			this.itemList.clear();
		}
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.shopItemMap = SerializeHelper.stringToMap(this.shopItems, Integer.class, Integer.class);
		SerializeHelper.stringToSet(Integer.class, this.tips, SerializeHelper.ATTRIBUTE_SPLIT,null,this.tipSet);
	}

	@Override
	public String getPrimaryKey() {
		return id;
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

	public String getAchieveItems() {
		return achieveItems;
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}
	
	public Map<Integer, Integer> getShopItemMap() {
		return shopItemMap;
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
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList.clear();
		this.itemList.addAll(itemList);
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
	public void resetItemList(List<AchieveItem> newAchieves) {
		this.itemList.clear();
		this.itemList.addAll(newAchieves);
		this.notifyUpdate();
	}

	public int getFreeTimes() {
		return freeTimes;
	}

	public void setFreeTimes(int freeTimes) {
		this.freeTimes = freeTimes;
	}
	
	public void addFreeTimes(int addTimes) {
		this.freeTimes += addTimes;
		this.notifyUpdate();
	}

	public int getDrawTimes() {
		return drawTimes;
	}

	public void setDrawTimes(int drawTimes) {
		this.drawTimes = drawTimes;
	}
	
	public void addDrawTimes(int addTimes) {
		this.drawTimes += addTimes;
		this.notifyUpdate();
	}

	public long getDayTime() {
		return dayTime;
	}

	public void setDayTime(long dayTime) {
		this.dayTime = dayTime;
	}

	public int getBuff() {
		return buff;
	}

	public void setBuff(int buff) {
		this.buff = buff;
	}

	@Override
	public Set<Integer> getTipSet() {
		return this.tipSet;
	}

	@Override
	public void setTipSet(Set<Integer> tips) {
		this.tipSet = tips;
	}
	
}
