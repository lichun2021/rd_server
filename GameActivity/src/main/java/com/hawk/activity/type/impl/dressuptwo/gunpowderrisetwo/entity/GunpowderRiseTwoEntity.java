package com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo.cfg.GunpowderRiseTwoExchangeCfg;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 圣诞节系列活动三:冰雪商城活动
 * @author hf
 */
@Entity
@Table(name="activity_dress_gunpowder_rise_two")
public class GunpowderRiseTwoEntity extends HawkDBEntity implements IActivityDataEntity {
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
	
	/** 关注的兑换id列表 **/
    @IndexProp(id = 7)
	@Column(name = "playerPoint", nullable = false)
	private String playerPoint;
	
    @IndexProp(id = 8)
	@Column(name = "exchangeMsg", nullable = false)
	private String exchangeMsg;
	
	@Transient
	private List<Integer> playerPoints = new ArrayList<Integer>();
	
	@Transient
	private Map<Integer, Integer> exchangeNumMap = new HashMap<>();
	
	public GunpowderRiseTwoEntity(){}
	
	public GunpowderRiseTwoEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		initTips();
	}
	
	
	@Override
	public void beforeWrite() {
		if(playerPoint == null){
			playerPoint = "";
		}
		if(exchangeMsg == null){
			exchangeMsg = "";
		}
		exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
	}

	@Override
	public void afterRead() {
		playerPoints = SerializeHelper.cfgStr2List(playerPoint, SerializeHelper.ATTRIBUTE_SPLIT);
		exchangeNumMap = SerializeHelper.stringToMap(exchangeMsg, Integer.class, Integer.class);
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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

	public List<Integer> getPlayerPoints() {
		return playerPoints;
	}
	
	public void addTips(int id){
		if(!playerPoints.contains(id)){
			playerPoints.add(id);
			setPlayerPoint(SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT));
		}
	}

	public void initTips(){
		if (playerPoints.isEmpty()){
			ConfigIterator<GunpowderRiseTwoExchangeCfg> ite = HawkConfigManager.getInstance().getConfigIterator(GunpowderRiseTwoExchangeCfg.class);
			while(ite.hasNext()){
				GunpowderRiseTwoExchangeCfg config = ite.next();
				playerPoints.add(config.getId());
			}
		}
		this.playerPoint = SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT);
	}
	
	public void removeTips(int id){
		playerPoints.remove(new Integer(id));
		setPlayerPoint(SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT));
	}

}
