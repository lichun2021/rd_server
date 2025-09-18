package com.hawk.activity.type.impl.battlefield.entity;

import java.util.List;
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
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_battlefield_treasure")
public class BattleFieldEntity  extends AchieveActivityEntity implements IActivityDataEntity {

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
	
	/** 停留的格子ID */
    @IndexProp(id = 4)
	@Column(name = "cellId", nullable = false)
	private int cellId;
	
	/** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	/** 购买通行证礼包的时间  */
    @IndexProp(id = 6)
	@Column(name = "buyTime", nullable = false)
	private long buyTime;
	
	/** 累计登录天数  */
    @IndexProp(id = 7)
	@Column(name = "loginDays", nullable = false)
	private int loginDays;
	
	/** 已领取奖励的天数  */
    @IndexProp(id = 8)
	@Column(name = "receiveAwardDays", nullable = false)
	private String receiveAwardDays;
	
	/** 固定点数骰子投掷次数  */
    @IndexProp(id = 9)
	@Column(name = "fixedRollTimes", nullable = false)
	private int fixedRollTimes;
	
	/** 随机点数骰子投掷次数 */
    @IndexProp(id = 10)
	@Column(name = "randomRollTimes", nullable = false)
	private int randomRollTimes;
	
	/** 奖池奖励  */
    @IndexProp(id = 11)
	@Column(name = "poolAwards", nullable = false)
	private String poolAwards;
	
	/** 本轮已走过的格子  */
    @IndexProp(id = 12)
	@Column(name = "passedCells", nullable = false)
	private String passedCells;
	
    @IndexProp(id = 13)
	@Column(name = "buyOrdinary", nullable = false)
	private int buyOrdinary;
	
    @IndexProp(id = 14)
	@Column(name = "buyControl", nullable = false)
	private int buyControl;
	
    @IndexProp(id = 15)
	@Column(name = "yijianpaotu", nullable = false)
	private int yijianpaotu;
	
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
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	@Transient
	private List<Integer> receiveDayList = new CopyOnWriteArrayList<Integer>();
	@Transient
	private List<Integer> poolAwardList = new CopyOnWriteArrayList<Integer>();
	@Transient
	private List<Integer> passedCellList = new CopyOnWriteArrayList<Integer>();

	public BattleFieldEntity() {
	}
	
	public BattleFieldEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
		this.receiveAwardDays = "";
		this.poolAwards = "";
		this.passedCells = "";
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.receiveAwardDays = SerializeHelper.collectionToString(this.receiveDayList, SerializeHelper.ELEMENT_DELIMITER);
		this.poolAwards = SerializeHelper.collectionToString(this.poolAwardList, SerializeHelper.ELEMENT_DELIMITER);
		this.passedCells = SerializeHelper.collectionToString(this.passedCellList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		this.receiveDayList.clear();
		this.poolAwardList.clear();
		this.passedCellList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToList(Integer.class, this.receiveAwardDays, this.receiveDayList);
		SerializeHelper.stringToList(Integer.class, this.poolAwards, this.poolAwardList);
		SerializeHelper.stringToList(Integer.class, this.passedCells, this.passedCellList);
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
		this.itemList.clear();
		this.itemList.addAll(itemList);
		this.notifyUpdate();
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public List<Integer> getReceiveDayList() {
		return receiveDayList;
	}
	
	public void addReceiveDay(int day) {
		if (!receiveDayList.contains(day)) {
			receiveDayList.add(day);
			this.notifyUpdate();
		}
	}

	public void resetPoolAwardList(List<Integer> poolAwardList) {
		this.poolAwardList.clear();
		this.poolAwardList.addAll(poolAwardList);
		this.notifyUpdate();
	}
	
	public List<Integer> getPoolAwardList() {
		return poolAwardList;
	}
	
	public int getCellAwardCount() {
		return poolAwardList.size();
	}
	
	public int getAwardId(int cellIndex) {
		return poolAwardList.get(cellIndex);
	}
	
	public long getBuyTime() {
		return buyTime;
	}

	public void setBuyTime(long buyTime) {
		this.buyTime = buyTime;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	public String getReceiveAwardDays() {
		return receiveAwardDays;
	}

	public void setReceiveAwardDays(String receiveAwardDays) {
		this.receiveAwardDays = receiveAwardDays;
	}

	public int getFixedRollTimes() {
		return fixedRollTimes;
	}

	public void setFixedRollTimes(int fixedRollTimes) {
		this.fixedRollTimes = fixedRollTimes;
	}

	public int getRandomRollTimes() {
		return randomRollTimes;
	}

	public void setRandomRollTimes(int randomRollTimes) {
		this.randomRollTimes = randomRollTimes;
	}

	public String getCellAward() {
		return poolAwards;
	}

	public void setCellAward(String cellAward) {
		this.poolAwards = cellAward;
	}
	
	public List<Integer> getPassedCellList() {
		return passedCellList;
	}
	
	public void addPassedCell(int cellId) {
		if (!passedCellList.contains(cellId)) {
			passedCellList.add(cellId);
			this.notifyUpdate();
		}
	}
	
	public void clearPassedCells() {
		passedCellList.clear();
		this.notifyUpdate();
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	public int getBuyOrdinary() {
		return buyOrdinary;
	}

	public void setBuyOrdinary(int buyOrdinary) {
		this.buyOrdinary = buyOrdinary;
	}

	public int getBuyControl() {
		return buyControl;
	}

	public void setBuyControl(int buyControl) {
		this.buyControl = buyControl;
	}

	public int getYijianpaotu() {
		return yijianpaotu;
	}

	public void setYijianpaotu(int yijianpaotu) {
		this.yijianpaotu = yijianpaotu;
	}

}
