package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;

/**
 * 调查问卷信息实体
 *
 * @author Jesse
 */
@Entity
@Table(name = "questionnaire")
public class QuestionnaireEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId;

	// 主界面问卷信息
	@Column(name = "pageSurveys")
    @IndexProp(id = 2)
	private String pageSurveys;
	
	// 邮件问卷ids
	@Column(name = "mailSurveys")
    @IndexProp(id = 3)
	private String mailSurveys;
	
	// 过期问卷ids
	@Column(name = "overdueSurveys")
    @IndexProp(id = 4)
	private String overdueSurveys;
	
	// 已完成问卷ids
	@Column(name = "finishedSurveys")
    @IndexProp(id = 5)
	private String finishedSurveys;
	
	// 上次问卷推送检测时间
	@Column(name = "lastCheckTime", nullable = false)
    @IndexProp(id = 6)
	protected long lastCheckTime = 0;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 7)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 8)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 9)
	protected boolean invalid;

	public QuestionnaireEntity() {
	}
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPageSurveys() {
		return pageSurveys;
	}

	public void setPageSurveys(String pageSurveys) {
		this.pageSurveys = pageSurveys;
	}

	public String getMailSurveys() {
		return mailSurveys;
	}

	public void setMailSurveys(String mailSurveys) {
		this.mailSurveys = mailSurveys;
	}

	public String getOverdueSurveys() {
		return overdueSurveys;
	}

	public void setOverdueSurveys(String overdueSurveys) {
		this.overdueSurveys = overdueSurveys;
	}

	public String getFinishedSurveys() {
		return finishedSurveys;
	}

	public void setFinishedSurveys(String finishedSurveys) {
		this.finishedSurveys = finishedSurveys;
	}

	public long getLastCheckTime() {
		return lastCheckTime;
	}

	public void setLastCheckTime(long lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
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
		return this.playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
