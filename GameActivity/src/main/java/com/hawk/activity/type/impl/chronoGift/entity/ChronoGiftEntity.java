package com.hawk.activity.type.impl.chronoGift.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.chronoGift.ChronoDoor;
import com.hawk.serialize.string.SerializeHelper;
/**
 * 时空豪礼数据实体
 * @author che
 *
 */
@Entity
@Table(name = "activity_chrono_gift")
public class ChronoGiftEntity  extends HawkDBEntity implements IActivityDataEntity {

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
	@Column(name = "buyNum", nullable = false)
	private int buyNum;
	
    @IndexProp(id = 5)
	@Column(name = "chronoDoors", nullable = false)
	private String chronoDoors;
	
    @IndexProp(id = 6)
	@Column(name = "achieves", nullable = false)
	private String achieves;
	
	
    @IndexProp(id = 7)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	/** 时空之门列表*/
	@Transient
	private List<ChronoDoor> chronoDoorList = new CopyOnWriteArrayList<ChronoDoor>();
	
	/** 成就任务列表*/
	@Transient
	private List<AchieveItem> achieveList = new CopyOnWriteArrayList<AchieveItem>();
	
	
	public ChronoGiftEntity() {
	}
	
	public ChronoGiftEntity(String playerId, int termId) {
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

	
	
	public int getBuyNum() {
		return buyNum;
	}

	public void setBuyNum(int buyNum) {
		this.buyNum = buyNum;
	}

	public List<ChronoDoor> getChronoDoorList() {
		return chronoDoorList;
	}

	public void setChronoDoorList(List<ChronoDoor> chronoDoorList) {
		this.chronoDoorList = chronoDoorList;
	}

	public List<AchieveItem> getAchieveList() {
		return achieveList;
	}

	public void setAchieveList(List<AchieveItem> achieveList) {
		this.achieveList = achieveList;
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
		this.chronoDoors = SerializeHelper.collectionToString(this.chronoDoorList, SerializeHelper.ELEMENT_DELIMITER);
		this.achieves = SerializeHelper.collectionToString(this.achieveList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.chronoDoorList.clear();
		this.achieveList.clear();
		SerializeHelper.stringToList(ChronoDoor.class, this.chronoDoors, this.chronoDoorList);
		SerializeHelper.stringToList(AchieveItem.class, this.achieves, this.achieveList);
	}

	


	public ChronoDoor getChronoDoor(int giftId) {
		for (ChronoDoor door : this.chronoDoorList) {
			if (door.getGiftId() == giftId) {
				return door;
			}
		}
		return null;
	} 
	
	
	
	
	public void resetItemList(List<AchieveItem> achieveList) {
		this.achieveList = achieveList;
		this.notifyUpdate();
	}
	
	
	public void addDoor(ChronoDoor door){
		this.chronoDoorList.add(door);
	}
	
	
	public void clearDoors(){
		this.chronoDoorList.clear();
	}
	
	
	public void addBuyKeyCount(int num){
		this.buyNum += num;
		this.notifyUpdate();
	}
	
}
