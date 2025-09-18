package com.hawk.activity.type.impl.midAutumn.entity; 

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_mid_autumn")
public class MidAutumnEntity extends HawkDBEntity implements IActivityDataEntity{

	/***/
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

	/***/
    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false, length=50)
	private String playerId;

	/***/
    @IndexProp(id = 3)
	@Column(name="termId", nullable = false, length=10)
	private int termId;
	
    @IndexProp(id = 4)
	@Column(name = "loginDays", nullable = false)
	private int loginDays;
	
	/**
	 * 跨天天数更新时间
	 */
    @IndexProp(id = 5)
	@Column(name = "refreshTime", nullable = false)
	private long refreshTime;
	
	/***/
    @IndexProp(id = 6)
	@Column(name="beatYuriTimes", nullable = false, length=10)
	private int beatYuriTimes;

	/***/
    @IndexProp(id = 7)
	@Column(name="wishTimes", nullable = false, length=10)
	private int wishTimes;
	
	/**兑换的商品复合结构{exchangeId,num}*/
    @IndexProp(id = 8)
	@Column(name="exchangeNum", nullable = false)
	private String exchangeNum;
	
	/**购买的商品复合结构{giftId_rewardId_rewardId2}, index = 按序*/ 
    @IndexProp(id = 9)
	@Column(name="buyGiftNum", nullable = false)
	private String buyGiftNum;
	
	/** 关注的兑换id列表 **/
    @IndexProp(id = 10)
	@Column(name = "playerPoint", nullable = false)
	private String playerPoint;

	@Transient
	private List<Integer> playerPoints = new ArrayList<Integer>();
	/***/
    @IndexProp(id = 11)
	@Column(name="createTime", nullable = false, length=19)
	private long createTime;

	/***/
    @IndexProp(id = 12)
	@Column(name="updateTime", nullable = false, length=19)
	private long updateTime;

	/***/
    @IndexProp(id = 13)
	@Column(name="invalid", nullable = false, length=0)
	private boolean invalid;
	
    @IndexProp(id = 14)
	@Column(name="achieveItems", nullable = false, length=1024)
	private String achieveItems;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private Map<Integer, Integer> exchangeNumMap;
	
	@Transient
	private List<String> buyGiftList = new ArrayList<>();  //  giftId_id  
	
	public MidAutumnEntity() {
	}
	
	public MidAutumnEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.loginDays = 1;
		this.refreshTime = HawkTime.getMillisecond();
		this.achieveItems = "";
	}

	public String getId() {
		return this.id; 
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return this.termId; 
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}

	public int getBeatYuriTimes() {
		return this.beatYuriTimes; 
	}

	public void setBeatYuriTimes(int beatYuriTimes) {
		this.beatYuriTimes = beatYuriTimes;
	}

	public int getWishTimes() {
		return this.wishTimes; 
	}

	public void setWishTimes(int wishTimes) {
		this.wishTimes = wishTimes;
	}

	public String getExchangeNum() {
		return exchangeNum;
	}

	public void setExchangeNum(String exchangeNum) {
		this.exchangeNum = exchangeNum;
	}
	
	@Override
	public long getCreateTime() {
		return this.createTime; 
	}
	@Override
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	@Override
	public long getUpdateTime() {
		return this.updateTime; 
	}
	@Override
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	@Override
	public boolean isInvalid() {
		return this.invalid; 
	}
	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
	public String getAchieveItems() {
		return achieveItems;
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public Map<Integer, Integer> getExchangeNumMap() {
		return exchangeNumMap;
	}

	public void setExchangeNumMap(Map<Integer, Integer> exchangeNumMap) {
		this.exchangeNumMap = exchangeNumMap;
	}
	
	public String getBuyGiftNum() {
		return buyGiftNum;
	}

	public void setBuyGiftNum(String buyGiftNum) {
		this.buyGiftNum = buyGiftNum;
	}

	public List<String> getBuyGiftList() {
		return buyGiftList;
	}

	public void setBuyGiftList(List<String> buyGiftList) {
		this.buyGiftList = buyGiftList;
	}

	public void addBuyGiftList(String buyGiftStr){
		if (buyGiftList == null) {
			buyGiftList = new ArrayList<>();
		}
		this.buyGiftList.add(buyGiftStr);
		this.notifyUpdate();
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.exchangeNum = SerializeHelper.mapToString(exchangeNumMap);
		this.buyGiftNum =SerializeHelper.collectionToString(this.buyGiftList, SerializeHelper.ELEMENT_DELIMITER);
		if(playerPoint == null){
			playerPoint = "";
		}
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.exchangeNumMap = SerializeHelper.stringToMap(exchangeNum, Integer.class, Integer.class);
		this.buyGiftList.clear();
		SerializeHelper.stringToList(String.class, buyGiftNum, this.buyGiftList);
		playerPoints = SerializeHelper.cfgStr2List(playerPoint, SerializeHelper.ATTRIBUTE_SPLIT);
	}
	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}

	public void resetItemList(List<AchieveItem> retainList) {
		this.itemList = retainList;
		this.notifyUpdate();
	}

	public String getPlayerPoint() {
		return playerPoint;
	}

	public void setPlayerPoint(String playerPoint) {
		this.playerPoint = playerPoint;
	}

	public List<Integer> getPlayerPoints() {
		return playerPoints;
	}

	public void setPlayerPoints(List<Integer> playerPoints) {
		this.playerPoints = playerPoints;
	}
	
	public void addTips(int id){
		if(!playerPoints.contains(id)){
			playerPoints.add(id);
			setPlayerPoint(SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT));
		}
	}

	public void initTips(List<Integer> ids){
		playerPoints.addAll(ids);
		this.playerPoint = SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT);
	}
	
	public void removeTips(int id){
		playerPoints.remove(new Integer(id));
		setPlayerPoint(SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT));
	}
	
}
