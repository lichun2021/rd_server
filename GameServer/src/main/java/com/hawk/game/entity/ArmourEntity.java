package com.hawk.game.entity;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.ItemInfoCollection;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 铠甲
 * @author golden
 *
 */
@Entity
@Table(name = "armour")
public class ArmourEntity extends HawkDBEntity {

	/**
	 * 唯一id，uuid
	 */
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;
	
	/**
	 * 玩家id
	 */
	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId;
	
	/**
	 * 配置id
	 */
	@Column(name = "armourId", nullable = false)
    @IndexProp(id = 3)
	private int armourId;
	
	/**
	 * 等级
	 */
	@Column(name = "level", nullable = false)
    @IndexProp(id = 4)
	private int level;
	
	/**
	 * 品质
	 */
	@Column(name = "quality", nullable = false)
    @IndexProp(id = 5)
	private int quality;
	
	/**
	 * 装备套装
	 */
	@Column(name = "suit", nullable = false)
    @IndexProp(id = 6)
	private String suit;
	
	/**
	 * 额外属性
	 */
	@Column(name = "extraAttr", nullable = false)
    @IndexProp(id = 7)
	private String extraAttr;
	
	/**
	 * 特技属性
	 */
	@Column(name = "skillAttr", nullable = false)
    @IndexProp(id = 8)
	private String skillAttr;
	
	/**
	 * 锁定
	 */
	@Column(name = "locked")
    @IndexProp(id = 9)
	protected boolean locked;
	
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 10)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 11)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 12)
	protected boolean invalid;

	/**
	 * 结束时间
	 */
	@Column(name = "endTime")
    @IndexProp(id = 13)
	protected long endTime;

	/**
	 * 是否是神器
	 */
	@Column(name = "isSuper")
    @IndexProp(id = 14)
	protected boolean isSuper;
	
	/**
	 * 星级
	 */
	@Column(name = "star")
    @IndexProp(id = 15)
	protected int star;
	
	/**
	 * 星级属性
	 */
	@Column(name = "starAttr", nullable = false)
    @IndexProp(id = 16)
	private String starAttr;
	
	/**
	 * 星级属性充能消耗(分解的时候返还)
	 */
	@Column(name = "starAttrConsume", nullable = false)
    @IndexProp(id = 17)
	private String starAttrConsume;


	/**
	 * 量子槽位
	 */
	@Column(name = "quantum")
	@IndexProp(id = 18)
	protected int quantum;
	
	/**
	 * 额外属性
	 */
	@Transient
	private List<ArmourEffObject> extraAttrEff = new CopyOnWriteArrayList<>();
	
	/**
	 * 特技属性
	 */
	@Transient
	private List<ArmourEffObject> skillEff = new CopyOnWriteArrayList<>();
	
	/**
	 * 星级属性
	 */
	@Transient
	private List<ArmourEffObject> starEff = new CopyOnWriteArrayList<>();
	
	/**
	 * 穿戴套装
	 */
	@Transient
	private Set<Integer> suitSet = new ConcurrentHashSet<>();
	
	/**
	 * 总消耗道具
	 */
	@Transient
	private ItemInfoCollection consumeItems = new ItemInfoCollection();
	
	@Override
	public void beforeWrite() {
		extraAttr = SerializeHelper.collectionToString(extraAttrEff, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT);
		skillAttr = SerializeHelper.collectionToString(skillEff, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT);
		starAttr = SerializeHelper.collectionToString(starEff, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT);
		suit = SerializeHelper.collectionToString(suitSet, SerializeHelper.BETWEEN_ITEMS);
		starAttrConsume = consumeItems.toString();
	}

	@Override
	public void afterRead() {
		extraAttrEff = SerializeHelper.stringToList(ArmourEffObject.class, extraAttr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new CopyOnWriteArrayList<>());
		skillEff = SerializeHelper.stringToList(ArmourEffObject.class, skillAttr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new CopyOnWriteArrayList<>());
		starEff = SerializeHelper.stringToList(ArmourEffObject.class, starAttr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new CopyOnWriteArrayList<>());
		if (!HawkOSOperator.isEmptyString(suit)) {
			suitSet = SerializeHelper.stringToSet(Integer.class, suit, SerializeHelper.BETWEEN_ITEMS, null, new ConcurrentHashSet<>());
		}
		consumeItems = ItemInfoCollection.valueOf(starAttrConsume);
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

	public int getArmourId() {
		return armourId;
	}

	public void setArmourId(int cfgId) {
		this.armourId = cfgId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public String getExtraAttr() {
		return extraAttr;
	}

	public void setExtraAttr(String extraAttr) {
		this.extraAttr = extraAttr;
	}

	public String getSkillAttr() {
		return skillAttr;
	}

	public void setSkillAttr(String skillAttr) {
		this.skillAttr = skillAttr;
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

	public void addExtraAttrEff(ArmourEffObject effect) {
		extraAttrEff.add(effect);
		notifyUpdate();
	}
	
	public void addSkillAttrEff(ArmourEffObject effect) {
		skillEff.add(effect);
		notifyUpdate();
	}
	
	public List<ArmourEffObject> getExtraAttrEff() {
		return extraAttrEff;
	}

	public List<ArmourEffObject> getSkillEff() {
		return skillEff;
	}

	public Set<Integer> getSuitSet() {
		return suitSet;
	}

	public void addSuit(int suitId) {
		suitSet.add(suitId);
		notifyUpdate();
	}
	
	public void removeSuit(int suitId) {
		suitSet.remove(suitId);
		notifyUpdate();
	}
	
	public void clearSuit(boolean needUpdate) {
		suitSet.clear();
		if (needUpdate) {
			notifyUpdate();	
		}
	}
	
	public boolean isLock() {
		return locked;
	}

	public void setLock(boolean lock) {
		this.locked = lock;
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isSuper() {
		return isSuper;
	}

	public void setSuper(boolean isSuper) {
		this.isSuper = isSuper;
	}

	public int getStar() {
		return star;
	}

	public void addStar() {
		this.star++;
		notifyUpdate();
	}

	public List<ArmourEffObject> getStarEff() {
		return starEff;
	}
	
	public void addStarEff(ArmourEffObject starAttr) {
		starEff.add(starAttr);
		notifyUpdate();
	}
	
	public void addConsume(List<ItemInfo> itemInfos) {
		consumeItems.add(itemInfos);
		notifyUpdate();
	}

	public ItemInfoCollection getConsumeItems() {
		return consumeItems;
	}

	public int getQuantum() {
		return quantum;
	}

	public void addQuantum() {
		this.quantum++;
		notifyUpdate();
	}
}
