package com.hawk.activity.type.impl.pointSprint.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_point_sprint")
public class PointSprintEntity extends HawkDBEntity implements IActivityDataEntity,IExchangeTipEntity {
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
	@Column(name = "createTime", nullable = false)
	private long createTime;

	@IndexProp(id = 5)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

	@IndexProp(id = 6)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@IndexProp(id = 7)
	@Column(name = "round", nullable = false)
	private int round = 1;

	@IndexProp(id = 8)
	@Column(name = "awardRound", nullable = false)
	private int awardRound;

	/** 积分 */
	@IndexProp(id = 9)
	@Column(name = "scoreInfo", nullable = false)
	private String scoreInfo = "";

	@IndexProp(id = 10)
	@Column(name = "awardedInfo", nullable = false)
	private String awardedInfo = "";

	/** 兑换数量信息 */
	@IndexProp(id = 11)
	@Column(name = "exchangeInfo", nullable = false)
	private String exchangeInfo = "";

	/** 兑换提醒信息 */
	@IndexProp(id = 12)
	@Column(name = "playerPoint", nullable = false)
	private String playerPoint = "";

	/** 兑换数量 */
	@Transient
	private Map<Integer, Integer> exchangeNumMap = new HashMap<>();

	@Transient
	private Map<Integer, Integer> scoreNumMap = new HashMap<>();

	/** 兑换提醒 */
	@Transient
	private List<Integer> playerPoints = new ArrayList<Integer>();

	/** 已领取奖励 */
	@Transient
	private List<Integer> awardedList = new ArrayList<Integer>();

	/**
	 * 构造函数
	 * 必须有空参数的，数据库模块解析需要
	 */
	public PointSprintEntity() {

	}

	/**
	 * 构造函数
	 * @param playerId 玩家id
	 * @param termId 活动期数
	 */
	public PointSprintEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	/**
	 * 存库前置操作
	 */
	@Override
	public void beforeWrite() {
		this.scoreInfo = SerializeHelper.mapToString(scoreNumMap);
		// 兑换数据转字符串
		this.exchangeInfo = SerializeHelper.mapToString(exchangeNumMap);
		// 提醒数据转字符串
		this.playerPoint = SerializeHelper.collectionToString(this.playerPoints, SerializeHelper.ATTRIBUTE_SPLIT);
		this.awardedInfo = SerializeHelper.collectionToString(this.awardedList, SerializeHelper.ATTRIBUTE_SPLIT);
	}

	/**
	 * 读库后置操作
	 */
	@Override
	public void afterRead() {
		scoreNumMap = SerializeHelper.stringToMap(scoreInfo, Integer.class, Integer.class);
		// 字符串转兑换数据
		exchangeNumMap = SerializeHelper.stringToMap(exchangeInfo, Integer.class, Integer.class);
		// 字符串转提醒数据
		playerPoints = SerializeHelper.cfgStr2List(playerPoint, SerializeHelper.ATTRIBUTE_SPLIT);
		awardedList = SerializeHelper.cfgStr2List(awardedInfo, SerializeHelper.ATTRIBUTE_SPLIT);
	}

	@Override
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getPlayerId() {
		return playerId;
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
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
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getAwardRound() {
		return awardRound;
	}

	public void setAwardRound(int awardRound) {
		this.awardRound = awardRound;
	}

	public String getExchangeInfo() {
		return exchangeInfo;
	}

	public void setExchangeInfo(String exchangeInfo) {
		this.exchangeInfo = exchangeInfo;
	}

	public String getPlayerPoint() {
		return playerPoint;
	}

	public void setPlayerPoint(String playerPoint) {
		this.playerPoint = playerPoint;
	}

	public Map<Integer, Integer> getExchangeNumMap() {
		return exchangeNumMap;
	}

	public void setExchangeNumMap(Map<Integer, Integer> exchangeNumMap) {
		this.exchangeNumMap = exchangeNumMap;
	}

	public List<Integer> getPlayerPoints() {
		return playerPoints;
	}

	public void setPlayerPoints(List<Integer> playerPoints) {
		this.playerPoints = playerPoints;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getScoreInfo() {
		return scoreInfo;
	}

	public void setScoreInfo(String scoreInfo) {
		this.scoreInfo = scoreInfo;
	}

	public Map<Integer, Integer> getScoreNumMap() {
		return scoreNumMap;
	}

	public void setScoreNumMap(Map<Integer, Integer> scoreNumMap) {
		this.scoreNumMap = scoreNumMap;
	}

	public String getAwardedInfo() {
		return awardedInfo;
	}

	public void setAwardedInfo(String awardedInfo) {
		this.awardedInfo = awardedInfo;
	}

	public List<Integer> getAwardedList() {
		return awardedList;
	}

	public void setAwardedList(List<Integer> awardedList) {
		this.awardedList = awardedList;
	}

	@Override
	public Set<Integer> getTipSet() {
		// TODO Auto-generated method stub
		return new HashSet<>(playerPoints);
	}

	@Override
	public void setTipSet(Set<Integer> tips) {
		playerPoints = new LinkedList<>(tips);
	}

}
