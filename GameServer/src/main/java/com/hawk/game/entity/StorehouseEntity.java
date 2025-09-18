package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Storehouse.HPStore;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;

/**
 * 玩家宝藏
 * 
 * @author lwt
 *
 */
@Entity
@Table(name = "story_house")
public class StorehouseEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId;

	@Column(name = "storeId", nullable = false)
    @IndexProp(id = 3)
	private int storeId; // 宝藏Id

	@Column(name = "helpId")
    @IndexProp(id = 4)
	private String helpId; // 帮助人id

	@Column(name = "queryHelp")
    @IndexProp(id = 5)
	protected int queryHelp; // 求助次数

	@Column(name = "openTime", nullable = false)
    @IndexProp(id = 6)
	protected long openTime; // 开启时间

	@Column(name = "collect")
    @IndexProp(id = 7)
	protected boolean collect; // 已收取

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 8)
	protected long createTime;

	@Column(name = "updateTime")
    @IndexProp(id = 9)
	protected long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 10)
	protected boolean invalid;

	public StorehouseEntity() {

	}
	
	/** 生成数据传输对象 */
	public HPStore toHPMessage() {
		HPStore.Builder bul = HPStore.newBuilder()
				.setStoreUUID(id)
				.setStoreId(storeId)
				.setOpenTime(openTime)
				.setHelped(HawkOSOperator.isEmptyString(helpId) ? 0 : 1)
				.setQuieryHelp((queryHelp == 0 && HawkOSOperator.isEmptyString(helpId)) ? 1 : 0);

		if (!HawkOSOperator.isEmptyString(helpId)) {
			Player tarPlayer = GlobalData.getInstance().makesurePlayer(helpId);
			bul.setHelpPlayerId(tarPlayer.getId())
					.setHelpLevel(tarPlayer.getLevel())
					.setHelpName(tarPlayer.getName())
					.setHelpOffice(GameUtil.getOfficerId(tarPlayer.getId()))
					.setHelpVip(tarPlayer.getVipLevel())
					.setHelpVipActive(tarPlayer.getData().getVipActivated())
					.setHelpIcon(tarPlayer.getIcon())
					.setHelpPfIcon(tarPlayer.getPfIcon() == null ? "" : tarPlayer.getPfIcon())
					.setCommon(BuilderUtil.genPlayerCommonBuilder(tarPlayer));
		}

		return bul.build();
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

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	protected void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getStoreId() {
		return storeId;
	}

	public void setStoreId(int storeId) {
		this.storeId = storeId;
	}

	public String getHelpId() {
		return helpId;
	}

	public void setHelpId(String helpId) {
		this.helpId = helpId;
	}

	public int getQueryHelp() {
		return queryHelp;
	}

	public void setQueryHelp(int queryHelp) {
		this.queryHelp = queryHelp;
	}

	public long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}

	public boolean isCollect() {
		return collect;
	}

	public void setCollect(boolean collect) {
		this.collect = collect;
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
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;

	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
