package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.protocol.GuildBigGift.PBPlayerGuildGift;

/**
 * 联盟礼包. 玩家个人拥有
 */
@Entity
@Table(name = "guild_smail_gift")
public class PlayerGuildGiftEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "playerId")
    @IndexProp(id = 2)
	private String playerId;

	@Column(name = "itemId")
    @IndexProp(id = 3)
	private int itemId; // 礼物item 如果awardGet为空, 则从award中取得奖励

	@Column(name = "awardGet")
    @IndexProp(id = 4)
	private String awardGet;

	@Column(name = "state")
    @IndexProp(id = 5)
	private int state; // 0 未领取, 1已领取

	@Column(name = "giftCreateTime")
    @IndexProp(id = 6)
	private long giftCreateTime; // 礼物创建时间

	@Column(name = "giftOverTime")
    @IndexProp(id = 7)
	private long giftOverTime; // 过期时间

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 8)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 9)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 10)
	protected boolean invalid;

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
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

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getAwardGet() {
		return awardGet;
	}

	public void setAwardGet(String awardGet) {
		this.awardGet = awardGet;
	}

	public int getState() {
		return state;
	}

	/**
	 * 0 未领取, 1已领取
	 * 
	 * @param state
	 */
	public void setState(int state) {
		this.state = state;
	}

	public long getGiftCreateTime() {
		return giftCreateTime;
	}

	public void setGiftCreateTime(long giftCreateTime) {
		this.giftCreateTime = giftCreateTime;
	}

	public long getGiftOverTime() {
		return giftOverTime;
	}

	public void setGiftOverTime(long giftOverTime) {
		this.giftOverTime = giftOverTime;
	}

	public PBPlayerGuildGift toPbObj() {
		PBPlayerGuildGift.Builder builder = PBPlayerGuildGift.newBuilder();
		builder.setId(id)
				.setItemId(itemId)
				.setAwardGet(awardGet)
				.setOverTime(giftOverTime)
				.setState(state);

		return builder.build();
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
}
