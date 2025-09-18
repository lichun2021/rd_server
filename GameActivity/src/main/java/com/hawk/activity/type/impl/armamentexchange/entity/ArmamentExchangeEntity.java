package com.hawk.activity.type.impl.armamentexchange.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.game.protocol.Activity.ArmamentExchangeInfo;
/**
 * 周年商城
 * @author luke
 */
@Entity
@Table(name = "activity_armament_exchange")
public class ArmamentExchangeEntity  extends HawkDBEntity implements IActivityDataEntity {

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
	@Column(name = "exchange", nullable = false)
	private String exchange;
	
    @IndexProp(id = 5)
	@Column(name = "isOpen", nullable = false)
	private int isOpen;
	
    @IndexProp(id = 6)
	@Column(name = "isFirst", nullable = false)
	private int isFirst;
	
    @IndexProp(id = 7)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	/** 兑换列表*/
	@Transient
	private List<ArmamentExchangeInfo.Builder> exchangeList = new CopyOnWriteArrayList<ArmamentExchangeInfo.Builder>();
	
	public ArmamentExchangeEntity() {
	}
	
	public ArmamentExchangeEntity(String playerId, int termId) {
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

	@Override
	public void beforeWrite() {
		JSONArray jsonArry = new JSONArray();
		for(int i=0;i<exchangeList.size();i++){
			ArmamentExchangeInfo.Builder builder = exchangeList.get(i);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("idx", builder.getIdx());
			jsonObject.put("num", builder.getNum());
			jsonArry.add(jsonObject);
		}
		exchange = JSONArray.toJSONString(jsonArry);
	}
	
	@Override
	public void afterRead() {
		exchangeList = new CopyOnWriteArrayList<>();
		if(exchange!=null){			
			JSONArray ary = JSONArray.parseArray(exchange);
			for(int i=0;i<ary.size();i++){
				JSONObject jsonObject = ary.getJSONObject(i);
				ArmamentExchangeInfo.Builder builder = ArmamentExchangeInfo.newBuilder();
				builder.setIdx(jsonObject.getIntValue("idx"));
				builder.setNum(jsonObject.getIntValue("num"));
				exchangeList.add(builder);
			}
		}
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public List<ArmamentExchangeInfo.Builder> getExchangeList() {
		return exchangeList;
	}

	public void setExchangeList(List<ArmamentExchangeInfo.Builder> exchangeList) {
		this.exchangeList = exchangeList;
	}

	public int getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(int isOpen) {
		this.isOpen = isOpen;
	}

	public int getIsFirst() {
		return isFirst;
	}

	public void setIsFirst(int isFirst) {
		this.isFirst = isFirst;
	}

}
