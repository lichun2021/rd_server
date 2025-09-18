package com.hawk.activity.type.impl.equipCraftsman.entity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.equipCraftsman.item.EquipCarftsmanItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 装备工匠
 * 
 * @author Golden
 *
 */
@Entity
@Table(name = "activity_equip_carftsman")
public class EquipCarftsmanEntity extends HawkDBEntity implements IActivityDataEntity {

	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
	/**
	 * 玩家id
	 */
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId = null;
	
	/**
	 * 活动期数
	 */
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
	/**
	 * 属性库
	 */
    @IndexProp(id = 4)
	@Column(name = "attrBox", nullable = false)
	private String attrBox;
	
	/**
	 * 界面 0为主界面 1为次级界面
	 */
    @IndexProp(id = 5)
	@Column(name = "page", nullable = false)
	private int page;
	
	/**
	 * 抽取次数
	 */
    @IndexProp(id = 6)
	@Column(name = "gachaTimes", nullable = false)
	private int gachaTimes;
	
    @IndexProp(id = 7)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	/**
	 * 词条库
	 */
	@Transient
	private Map<String, EquipCarftsmanItem> attrBoxMap = new HashMap<>();
	
	
	public EquipCarftsmanEntity() {
		
	}
	
	public EquipCarftsmanEntity(String playerId, int termId) {
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

	public String getAttrBox() {
		return attrBox;
	}

	public void setAttrBox(String attrBox) {
		this.attrBox = attrBox;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getGachaTimes() {
		return gachaTimes;
	}

	public void setGachaTimes(int gachaTimes) {
		this.gachaTimes = gachaTimes;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

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

	/**
	 * 获取词条库信息
	 */
	public Map<String, EquipCarftsmanItem> getAttrBoxMap() {
		return attrBoxMap;
	}

	/**
	 * 删除词条
	 */
	public void removeAttr(String uuid) {
		attrBoxMap.remove(uuid);
		notifyUpdate();
	}
	
	/**
	 * 添加词条
	 */
	public void addAttr(String uuid, EquipCarftsmanItem item) {
		attrBoxMap.put(uuid, item);
		notifyUpdate();
	}
	
	@Override
	public void beforeWrite() {
		attrBox = SerializeHelper.mapToString(attrBoxMap, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.SEMICOLON_ITEMS, SerializeHelper.COLON_ITEMS);
	}

	@Override
	public void afterRead() {
		if (!HawkOSOperator.isEmptyString(attrBox)) {
			attrBoxMap = SerializeHelper.stringToMap(attrBox, String.class, EquipCarftsmanItem.class, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.SEMICOLON_ITEMS, SerializeHelper.COLON_ITEMS, new HashMap<>());
		}
	}
	
}
