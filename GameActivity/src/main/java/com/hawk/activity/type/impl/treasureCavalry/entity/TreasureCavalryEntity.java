package com.hawk.activity.type.impl.treasureCavalry.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkRand;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.treasureCavalry.cfg.TreasureCavalryRewardPoolCfg;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_treasure_cavalry")
public class TreasureCavalryEntity extends HawkDBEntity implements IActivityDataEntity {
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
	@Column(name = "pool", nullable = false)
	private int pool;

    @IndexProp(id = 5)
	@Column(name = "itemStr", nullable = false)
	private String itemStr;

    @IndexProp(id = 6)
	@Column(name = "multiple", nullable = false)
	private int multiple;
	// 刷新次数
    @IndexProp(id = 7)
	@Column(name = "refreshTimes", nullable = false)
	private int refreshTimes;

    @IndexProp(id = 8)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 9)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 10)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private List<Integer> itemList;;

	public TreasureCavalryEntity() {
		itemList = new ArrayList<>(10);
	}

	public TreasureCavalryEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		itemList = new ArrayList<>(10);
	}

	public void resetItems() {
		ConfigIterator<TreasureCavalryRewardPoolCfg> it = HawkConfigManager.getInstance().getConfigIterator(TreasureCavalryRewardPoolCfg.class);
		TreasureCavalryRewardPoolCfg cfg = HawkRand.randomWeightObject(it.toList());
		this.setPool(cfg.getId());
		itemList = new ArrayList<>(10);
		for (int i = 0; i < 10; i++) {
			itemList.add(0);
		}
	}

	@Override
	public void beforeWrite() {
		this.itemStr = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(Integer.class, this.itemStr, this.itemList);
	}

	public int getPool() {
		return pool;
	}

	public void setPool(int pool) {
		this.pool = pool;
	}

	public String getItemStr() {
		return itemStr;
	}

	public void setItemStr(String itemStr) {
		this.itemStr = itemStr;
	}

	public List<Integer> getItemList() {
		return itemList;
	}

	public void setItemList(List<Integer> itemList) {
		this.itemList = itemList;
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

	public int getRefreshTimes() {
		return refreshTimes;
	}

	public void setRefreshTimes(int refreshTimes) {
		this.refreshTimes = refreshTimes;
	}

	// public int getRefreshCount() {
	// return refreshCount;
	// }
	//
	// public void setRefreshCount(int refreshCount) {
	// this.refreshCount = refreshCount;
	// }

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

}
