package com.hawk.activity.type.impl.plantweapon.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import org.hawk.config.HawkConfigManager;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.plantweapon.cfg.PlantWeaponKVCfg;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_plant_weapon")
public class PlantWeaponEntity extends HawkDBEntity implements IActivityDataEntity{
	
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
    
    /** 第几轮抽奖 */
    @IndexProp(id = 4)
	@Column(name="turnCount", nullable = false)
	private int turnCount;
    
    /** 当天首次进入活动的时间 */
    @IndexProp(id = 5)
	@Column(name="dayTime", nullable = false)
	private long dayTime;
    
    /** 连续抽奖次数 */
    @IndexProp(id = 6)
	@Column(name="continueDraws", nullable = false)
	private int continueDraws;
    
    /** 连续放弃次数 */
    @IndexProp(id = 7)
   	@Column(name="continueGiveups", nullable = false)
   	private int continueGiveups;
    /**
     * 冷却起始时间
     */
    @IndexProp(id = 8)
   	@Column(name="cooldownTime", nullable = false)
   	private long cooldownTime;
    
	/** 灵感进度 */
    @IndexProp(id = 9)
   	@Column(name="inspireProgress", nullable = false)
   	private int inspireProgress;
    
    /** 累计消耗数量 */
    @IndexProp(id = 10)
   	@Column(name="consumeItemCount", nullable = false)
   	private int consumeItemCount;
    
    /** 当下已随机出的奖励  */
    @IndexProp(id = 11)
	@Column(name="awardItems", nullable = false)
	private String awardItems = "";
    
	/** 下次研究消耗的折扣 */
    @IndexProp(id = 12)
   	@Column(name="disCount", nullable = false)
   	private int disCount;
    
    /** 商店已购买次数信息 */
    @IndexProp(id = 13)
   	@Column(name = "shopItems", nullable = false)
   	private String shopItems = "";
    
    /** 成就数据 **/
    @IndexProp(id = 14)
	@Column(name="achieveItems", nullable = false)
	private String achieveItems;

    @IndexProp(id = 15)
	@Column(name="createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 16)
	@Column(name="updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 17)
	@Column(name="invalid", nullable = false)
	private boolean invalid;
    
    /** 后续开放活动玩家可以将【研究灵感】进度满值送的超武激活道具自选成为往期 的道具 */
    @IndexProp(id = 18)
	@Column(name="choosePlantWeapon", nullable = false)
	private int choosePlantWeapon;
    
    /**
     * 每日免费奖励领取时间
     */
    @IndexProp(id = 19)
   	@Column(name="dailyRecieveTime", nullable = false)
   	private long dailyRecieveTime;
    
    /**
     * 已触发保底次数
     */
    @IndexProp(id = 20)
   	@Column(name="touchCount", nullable = false)
   	private int touchCount;
    
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

	/** 商店商品购买数量  */
	@Transient
	private Map<Integer, Integer> shopItemMap = new HashMap<>();
	
	@Transient
	private List<String> awardList = new ArrayList<String>();
	
	
	public PlantWeaponEntity(){}
	
	public PlantWeaponEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
		this.turnCount = 1;
		PlantWeaponKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponKVCfg.class);
		this.choosePlantWeapon = kvCfg.getPlantWeapon();
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.awardItems = SerializeHelper.collectionToString(this.awardList, SerializeHelper.BETWEEN_ITEMS);
		this.shopItems = SerializeHelper.mapToString(this.shopItemMap);
	}

	@Override
	public void afterRead() {
		if (!itemList.isEmpty()) {
			this.itemList.clear();
		}
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.awardList = SerializeHelper.stringToList(String.class, this.awardItems, SerializeHelper.BETWEEN_ITEMS);
		this.shopItemMap = SerializeHelper.stringToMap(this.shopItems, Integer.class, Integer.class);
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
	
	public long getDayTime() {
		return dayTime;
	}

	public void setDayTime(long dayTime) {
		this.dayTime = dayTime;
	}

	public void resetItemList(List<AchieveItem> newAchieves) {
		this.itemList.clear();
		this.itemList.addAll(newAchieves);
		this.notifyUpdate();
	}

	public int getTurnCount() {
		return turnCount;
	}

	public void setTurnCount(int turnCount) {
		this.turnCount = turnCount;
	}
	
	public void turnCountAdd(int add) {
		this.turnCount += add;
		this.notifyUpdate();
	}

	public int getContinueDraws() {
		return continueDraws;
	}

	public void setContinueDraws(int continueDraws) {
		this.continueDraws = continueDraws;
	}
	
	public void continueDrawsAdd(int add) {
		this.continueDraws += add;
		this.notifyUpdate();
	}

	public int getContinueGiveups() {
		return continueGiveups;
	}

	public void setContinueGiveups(int continueGiveups) {
		this.continueGiveups = continueGiveups;
	}
	
	public void continueGiveupsAdd(int add) {
		this.continueGiveups += add;
		this.notifyUpdate();
	}

	public int getInspireProgress() {
		return inspireProgress;
	}

	public void setInspireProgress(int inspireProgress) {
		this.inspireProgress = inspireProgress;
	}
	
	public void inspireProgressAdd(int add) {
		this.inspireProgress += add;
		this.notifyUpdate();
	}

	public int getConsumeItemCount() {
		return consumeItemCount;
	}

	public void setConsumeItemCount(int consumeItemCount) {
		this.consumeItemCount = consumeItemCount;
	}
	
	public void consumeAdd(int count) {
		this.consumeItemCount += count;
		this.notifyUpdate();
	}

	public int getDisCount() {
		return disCount;
	}

	public void setDisCount(int disCount) {
		this.disCount = disCount;
	}
	
	 public String getAwardItems() {
		return awardItems;
	}

	public void setAwardItems(String awardItems) {
		this.awardItems = awardItems;
	}
	 
	public List<String> getAwardList() {
		return this.awardList;
	}
	
	public long getCooldownTime() {
		return cooldownTime;
	}

	public void setCooldownTime(long cooldownTime) {
		this.cooldownTime = cooldownTime;
	}
	
	public int getChoosePlantWeapon() {
		return choosePlantWeapon;
	}

	public void setChoosePlantWeapon(int choosePlantWeapon) {
		this.choosePlantWeapon = choosePlantWeapon;
	}
	
	public long getDailyRecieveTime() {
		return dailyRecieveTime;
	}

	public void setDailyRecieveTime(long dailyRecieveTime) {
		this.dailyRecieveTime = dailyRecieveTime;
	}
	
	public int getTouchCount() {
		return touchCount;
	}

	public void setTouchCount(int touchCount) {
		this.touchCount = touchCount;
	}
	
	public void touchCountAdd(int add) {
		setTouchCount(touchCount + 1);
	}
	
}
