package com.hawk.activity.type.impl.supplyStationTwo.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_supply_station_two")
public class SupplyStationTwoEntity extends HawkDBEntity implements IActivityDataEntity{

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


	/** 购买信息 **/
    @IndexProp(id = 7)
	@Column(name = "buyInfo", nullable = false)
	private String buyInfo;

	/** 关注的兑换id列表 **/
    @IndexProp(id = 8)
	@Column(name = "playerPoint", nullable = false)
	private String playerPoint;

    @IndexProp(id = 9)
	@Column(name = "exchangeMsg", nullable = false)
	private String exchangeMsg;

	/** 已经购买的信息 **/
	@Transient
	private Map<Integer, Integer> buyMsg = new HashMap<Integer, Integer>();

	@Transient
	private List<Integer> playerPoints = new ArrayList<Integer>();

	@Transient
	private Map<Integer, Integer> exchangeNumMap = new HashMap<>();
	
	public SupplyStationTwoEntity(){}
	
	public SupplyStationTwoEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		if(buyInfo == null){
			buyInfo = "";
		}
		buyInfo = SerializeHelper.mapToString(buyMsg);
		if(playerPoint == null){
			playerPoint = "";
		}
		this.playerPoint = SerializeHelper.collectionToString(this.playerPoints,SerializeHelper.ATTRIBUTE_SPLIT);
		if(exchangeMsg == null){
			exchangeMsg = "";
		}
		exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
	}

	@Override
	public void afterRead() {
		buyMsg = SerializeHelper.stringToMap(buyInfo, Integer.class, Integer.class);
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
	
	public String getBuyInfo() {
		return buyInfo;
	}

	public void setBuyInfo(String buyInfo) {
		this.buyInfo = buyInfo;
	}

	/***
	 * 判断是否可以购买
	 * @param chestId
	 * @param count
	 * @param maxCount
	 * @return
	 */
	public boolean canBuy(int chestId, int count, int maxCount){
		if(count > maxCount || count <= 0){
			return false;
		}
		Integer buyObj = buyMsg.get(chestId);
		int already = buyObj == null ? 0 : buyObj.intValue();
		if(already + count > maxCount){
			return false;
		}
		return true;
	}
	
	/***
	 * 计算可以购买数量
	 * @param chestId
	 * @return
	 */
	public int getBuyCnt(int chestId){
		Integer buyObj = buyMsg.get(chestId);
		int already = buyObj == null ? 0 : buyObj.intValue();
		return already;
	}
	
	public void onPlayerBuy(int chestId, int count){
		if(count <= 0){
			return;
		}
		Integer buyObj = buyMsg.get(chestId);
		int already = buyObj == null ? 0 : buyObj.intValue();
		int nowBuy = already + count;
		buyMsg.put(chestId, nowBuy);
	}
	
	public void crossDay(){
		buyMsg.clear();
		buyInfo = "";
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

	public void setPlayerPoints(List<Integer> playerPoints) {
		this.playerPoints = playerPoints;
	}

	public void addTips(int id){
		if(!playerPoints.contains(id)){
			playerPoints.add(id);
			setPlayerPoint(SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT));
		}
	}

	public void removeTips(int id){
		playerPoints.remove(new Integer(id));
		setPlayerPoint(SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT));
	}

	@Override
	public String toString() {
		return "SupplyStationTwoEntity [id=" + id + ", playerId=" + playerId + ", termId=" + termId + ", buyInfo="
				+ buyInfo + ", createTime=" + createTime + ", updateTime=" + updateTime + ", invalid=" + invalid
				+ ", buyMsg=" + buyMsg + "]";
	}
}
