package com.hawk.activity.type.impl.lotteryDraw.entity;

import java.util.List;
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
@Table(name = "activity_lottery_draw")
public class LotteryDrawEntity  extends HawkDBEntity implements IActivityDataEntity{

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
	
	/** 上次免费抽奖时间*/
    @IndexProp(id = 4)
	@Column(name = "lastFreeDrawTime", unique = true, nullable = false)
	private long lastFreeDrawTime;
	
	/** 上次停留的格子id*/
    @IndexProp(id = 5)
	@Column(name = "lastCellId", unique = true, nullable = false)
	private int lastCellId;
	
	/** 进行十连抽的次数*/
    @IndexProp(id = 6)
	@Column(name = "tenDrawTimes", unique = true, nullable = false)
	private int tenDrawTimes;
	
	/** 总抽奖次数*/
    @IndexProp(id = 7)
	@Column(name = "totalTimes", unique = true, nullable = false)
	private int totalTimes;
	
	/** 保底累计次数*/
    @IndexProp(id = 8)
	@Column(name = "ensureTimes", nullable = false)
	private int ensureTimes;

	/** 是否触发多倍*/
    @IndexProp(id = 9)
	@Column(name = "multi", nullable = false)
	private boolean multi;
	
	/** 多倍奖励幸运值*/
    @IndexProp(id = 10)
	@Column(name = "multiLucky", nullable = false)
	private int multiLucky;
	
	/** 活动成就项数据 */
    @IndexProp(id = 11)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

    @IndexProp(id = 12)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 13)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 14)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();

	public LotteryDrawEntity() {
	}
	
	public LotteryDrawEntity(String playerId, int termId, int lastCellId) {
		this.playerId = playerId;
		this.termId = termId;
		this.lastCellId = lastCellId;
		this.lastFreeDrawTime = 0;
		this.achieveItems = "";
		this.tenDrawTimes = 0;
		this.totalTimes = 0;
		this.ensureTimes = 0;
		this.multi = false;
		this.multiLucky = 1;
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

	public long getLastFreeDrawTime() {
		return lastFreeDrawTime;
	}

	public void setLastFreeDrawTime(long lastFreeDrawTime) {
		this.lastFreeDrawTime = lastFreeDrawTime;
	}

	public int getLastCellId() {
		return lastCellId;
	}

	public void setLastCellId(int lastCellId) {
		this.lastCellId = lastCellId;
	}

	public int getTenDrawTimes() {
		return tenDrawTimes;
	}

	public void setTenDrawTimes(int tenDrawTimes) {
		this.tenDrawTimes = tenDrawTimes;
	}

	public int getTotalTimes() {
		return totalTimes;
	}

	public void setTotalTimes(int totalTimes) {
		this.totalTimes = totalTimes;
	}

	public int getEnsureTimes() {
		return ensureTimes;
	}

	public void setEnsureTimes(int ensureTimes) {
		this.ensureTimes = ensureTimes;
	}

	public boolean isMulti() {
		return multi;
	}

	public void setMulti(boolean multi) {
		this.multi = multi;
	}

	public int getMultiLucky() {
		return multiLucky;
	}

	public void setMultiLucky(int multiLucky) {
		this.multiLucky = multiLucky;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
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
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

}
