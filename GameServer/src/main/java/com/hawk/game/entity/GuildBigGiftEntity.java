package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.guild.GuildBigGift;

/**
 * 联盟大礼包. 联盟唯一
 */
@Entity
@Table(name = "guild_big_gift")
public class GuildBigGiftEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "guildId")
    @IndexProp(id = 2)
	private String guildId;

	@Column(name = "bigGiftLevelExp")
    @IndexProp(id = 3)
	private int bigGiftLevelExp; // 礼包经验, 计算等级

	@Column(name = "bigGiftId")
    @IndexProp(id = 4)
	private int bigGiftId; //

	@Column(name = "bigGiftExp")
    @IndexProp(id = 5)
	private int bigGiftExp;// 成员领取小礼包计数. 达到指定数量后发放bigGiftId下的award给玩家礼包

	@Column(name = "giftSerialized")
    @IndexProp(id = 6)
	private String giftSerialized; // 小礼包列表. 只保持限定时间内

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 7)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 8)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 9)
	protected boolean invalid;

	@Transient
	private GuildBigGift giftObj;

	public String getGuildId() {
		return guildId;
	}

	@Override
	public void beforeWrite() {
		giftSerialized = giftObj.smailGiftListSerialize();
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		giftObj = GuildBigGift.create(this);
		super.afterRead();
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public int getBigGiftLevelExp() {
		return bigGiftLevelExp;
	}

	public void setBigGiftLevelExp(int bigGiftLevelExp) {
		this.bigGiftLevelExp = bigGiftLevelExp;
	}

	public int getBigGiftId() {
		return bigGiftId;
	}

	public void setBigGiftId(int bigGiftId) {
		this.bigGiftId = bigGiftId;
	}

	public int getBigGiftExp() {
		return bigGiftExp;
	}

	public void setBigGiftExp(int bigGiftExp) {
		this.bigGiftExp = bigGiftExp;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public GuildBigGift getGiftObj() {
		return giftObj;
	}

	public void setGiftObj(GuildBigGift giftObj) {
		this.giftObj = giftObj;
	}

	public String getGiftSerialized() {
		return giftSerialized;
	}

	public void setGiftSerialized(String giftSerialized) {
		this.giftSerialized = giftSerialized;
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
