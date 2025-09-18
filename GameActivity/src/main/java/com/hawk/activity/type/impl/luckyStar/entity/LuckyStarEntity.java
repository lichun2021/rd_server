package com.hawk.activity.type.impl.luckyStar.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_Lucky_star")
public class LuckyStarEntity extends HawkDBEntity implements IActivityDataEntity {

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
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 5)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 6)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	/** 当日礼包剩余购买次数 **/
    @IndexProp(id = 7)
	@Column(name = "leftGiftCnt", nullable = false)
	private String leftGiftCnt;
	
	/** 已经抽奖的次数 **/
    @IndexProp(id = 8)
	@Column(name = "lotCnt", nullable = false)
	private int lotCnt;
	
    @IndexProp(id = 9)
	@Column(name = "lastBuyGiftId", nullable = false)
	private String lastBuyGiftId;
	
	/** 今日领取的免费宝箱id **/
    @IndexProp(id = 10)
	@Column(name = "todayRecieveBag", nullable = false)
	private String todayRecieveBag;
    
    /** 成就数据 **/
    @IndexProp(id = 11)
	@Column(name="achieveItems", nullable = false)
	private String achieveItems = "";
    
    //当天首次进入活动的时间
    @IndexProp(id = 12)
	@Column(name="dayTime", nullable = false)
	private long dayTime;
	
	/** 当日礼包购买信息 key-礼包id value-该礼包当日购买次数 **/
	@Transient
	private Map<String, Integer> giftBuyCntMap = new HashMap<String, Integer>();
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	public LuckyStarEntity(){}
	
	public LuckyStarEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		leftGiftCnt = SerializeHelper.mapToString(giftBuyCntMap);
		if(lastBuyGiftId == null){
			lastBuyGiftId = "";
		}
		if(todayRecieveBag == null){
			todayRecieveBag = "";
		}
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		giftBuyCntMap = SerializeHelper.stringToMap(leftGiftCnt, String.class, Integer.class);
	}
	
	/***
	 * 购买礼包成功回调
	 * @param giftId
	 */
	public void onPlayerBuyGift(String giftId){
		if(giftBuyCntMap.containsKey(giftId)){
			int count = giftBuyCntMap.get(giftId);
			giftBuyCntMap.put(giftId, count + 1);
		}else{
			giftBuyCntMap.put(giftId, 1);
		}
		lastBuyGiftId = giftId;
		setLeftGiftCnt(SerializeHelper.mapToString(giftBuyCntMap));
	}
	
	public void crossDay(){
		leftGiftCnt = "";
		giftBuyCntMap.clear();
		todayRecieveBag = "";
	}
	
	/***
	 * 获取改礼包本日购买次数
	 * @param giftId
	 * @return
	 */
	public int getGiftBuyCnt(String giftId){
		if(!giftBuyCntMap.containsKey(giftId)){
			return 0;
		}else{
			return giftBuyCntMap.get(giftId);
		}
	}

	public Map<String, Integer> getGiftBuyCntMap() {
		return giftBuyCntMap;
	}
	
	/***
	 * 是否有免费的宝箱可以领取
	 * @return
	 */
	public boolean hasFreeBag(){
		if(todayRecieveBag != null && !todayRecieveBag.trim().equals("")){
			return false;
		}
		return true;
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

	public int getLotCnt() {
		return lotCnt;
	}

	public void setLotCnt(int lotCnt) {
		this.lotCnt = lotCnt;
	}
	
	public void lottery(int cnt){
		this.lotCnt += cnt;
	}

	public String getLeftGiftCnt() {
		return leftGiftCnt;
	}

	public void setLeftGiftCnt(String leftGiftCnt) {
		this.leftGiftCnt = leftGiftCnt;
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

	public String getLastBuyGiftId() {
		return lastBuyGiftId;
	}

	public void setLastBuyGiftId(String lastBuyGiftId) {
		this.lastBuyGiftId = lastBuyGiftId;
	}

	public String getTodayRecieveBag() {
		return todayRecieveBag;
	}

	public void setTodayRecieveBag(String todayRecieveBag) {
		this.todayRecieveBag = todayRecieveBag;
	}

	@Override
	public String toString() {
		return "LuckyStarEntity [id=" + id + ", playerId=" + playerId + ", termId=" + termId + ", createTime="
				+ createTime + ", updateTime=" + updateTime + ", invalid=" + invalid + ", leftGiftCnt=" + leftGiftCnt
				+ ", lotCnt=" + lotCnt + ", lastBuyGiftId=" + lastBuyGiftId + ", todayRecieveBag=" + todayRecieveBag
				+ ", giftBuyCntMap=" + giftBuyCntMap + "]";
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public void resetItemList(List<AchieveItem> newAchieves) {
		this.itemList.clear();
		this.itemList.addAll(newAchieves);
		this.notifyUpdate();
	}

	public long getDayTime() {
		return dayTime;
	}

	public void setDayTime(long dayTime) {
		this.dayTime = dayTime;
	}
	
}
