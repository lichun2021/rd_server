package com.hawk.game.queryentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 查询ID集合辅助类
 * 
 * @author Nannan.Gao
 * @date 2016-10-4 18:20:49
 */
@Entity
public class GMId {

	@Id
	@Column(name = "gmId")
	private String gmId;

	public GMId() {

	}

	public GMId(String gmId) {
		this.gmId = gmId;
	}

	public String getGmId() {
		return gmId;
	}

	@Override
	public String toString() {
		return this.gmId;
	}
}