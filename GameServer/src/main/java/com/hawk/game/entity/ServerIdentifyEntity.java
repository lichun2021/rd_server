package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

/**
 * 服务器标示实体
 * @author golden
 *
 */
@Entity
@Table(name = "server_identify")
public class ServerIdentifyEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "serverIdentify", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String serverIdentify = "";
	
	@Column(name = "serverOpenTime", nullable = false)
    @IndexProp(id = 2)
	private long serverOpenTime = 0;
	
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 3)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 4)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 5)
	protected boolean invalid;

	public String getServerIdentify() {
		return serverIdentify;
	}

	public void setServerIdentify(String serverIdentify) {
		this.serverIdentify = serverIdentify;
	}
	
	public long getServerOpenTime() {
		return serverOpenTime;
	}

	public void setServerOpenTime(long serverOpenTime) {
		this.serverOpenTime = serverOpenTime;
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
		return serverIdentify;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		serverIdentify = primaryKey;
	}
}
