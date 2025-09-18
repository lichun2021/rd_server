package com.hawk.activity.type.impl.destinyRevolver.entity;

import java.util.ArrayList;
import java.util.List;

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

/**
 * 命运左轮
 * @author golden
 *
 */
@Entity
@Table(name = "activity_destiny_revolver")
public class DestinyRevolverEntity extends AchieveActivityEntity implements IActivityDataEntity {
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
	 * 点击开始活动
	 */
    @IndexProp(id = 4)
	@Column(name = "firstKick", nullable = false)
	private boolean firstKick;
	
	/**
	 * 是否在塔罗牌界面
	 */
    @IndexProp(id = 5)
	@Column(name = "inTarot", nullable = false)
	private boolean inTarot;
	
	/**
	 * 9个格子的列表
	 */
    @IndexProp(id = 6)
	@Column(name = "gridStr", nullable = false)
	private String gridStr;

	/**
	 * 9个格子的翻牌倒计时
	 */
    @IndexProp(id = 7)
	@Column(name = "nineEndTime", nullable = false)
	private long nineEndTime;
	
	/**
	 * 翻倍
	 */
    @IndexProp(id = 8)
	@Column(name = "multiple", nullable = false)
	private int multiple;
	
	/**
	 * 活动成就项数据
	 */
    @IndexProp(id = 9)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    @IndexProp(id = 10)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 11)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 12)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private List<Integer> gridist;

	@Transient
	private List<AchieveItem> itemList;
	
	public DestinyRevolverEntity() {
		gridist = new ArrayList<>(10);
		itemList = new ArrayList<>();
	}

	public DestinyRevolverEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		gridist = new ArrayList<>(10);
		itemList = new ArrayList<>();
	}

	public void resetItems() {
		gridist = new ArrayList<>(10);
		for (int i = 0; i < 10; i++) {
			gridist.add(0);
		}
		nineEndTime = 0L;
		inTarot = false;
		notifyUpdate();
	}

	@Override
	public void beforeWrite() {
		this.gridStr = SerializeHelper.collectionToString(this.gridist, SerializeHelper.ELEMENT_DELIMITER);
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public void afterRead() {
		this.gridist.clear();
		SerializeHelper.stringToList(Integer.class, this.gridStr, this.gridist);
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
	}

	public boolean hasFirstKick() {
		return firstKick;
	}

	public void setFirstKick(boolean hasFirstKick) {
		this.firstKick = hasFirstKick;
	}

	public boolean isInTarot() {
		return inTarot;
	}

	public void setInTarot(boolean inTarot) {
		this.inTarot = inTarot;
	}

	public String getGridStr() {
		return gridStr;
	}

	public void setGridStr(String gridStr) {
		this.gridStr = gridStr;
	}

	public List<Integer> getGridist() {
		return gridist;
	}

	public void setGridist(List<Integer> gridist) {
		this.gridist = gridist;
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

	public int getMultiple() {
		return multiple;
	}

	public void setMultiple(int multiple) {
		this.multiple = multiple;
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
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}

	public long getNineEndTime() {
		return nineEndTime;
	}

	public void setNineEndTime(long nineEndTime) {
		this.nineEndTime = nineEndTime;
	}
}
