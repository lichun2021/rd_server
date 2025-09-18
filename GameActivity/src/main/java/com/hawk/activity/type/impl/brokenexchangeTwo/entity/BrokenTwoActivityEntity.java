package com.hawk.activity.type.impl.brokenexchangeTwo.entity; 

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

/**
*	残卷兑换活动
*	auto generate do not modified
*/
@Entity
@Table(name="activity_broken_exchange_two")
public class BrokenTwoActivityEntity extends HawkDBEntity implements IActivityDataEntity{

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

	/**上次余下的时间*/
    @IndexProp(id = 4)
	@Column(name="collectRemainTime", nullable = false)
	private int collectRemainTime;

	/**野外采集上次累计的时间*/
    @IndexProp(id = 5)
	@Column(name="wolrdCollectRemainTime", nullable = false)
	private int wolrdCollectRemainTime;

	/**击败尤里的次数*/
    @IndexProp(id = 6)
	@Column(name="beatYuriTimes", nullable = false)
	private int beatYuriTimes;

	/**许愿次数*/
    @IndexProp(id = 7)
	@Column(name="wishTimes", nullable = false)
	private int wishTimes;

	/**礼包消耗钻石*/
    @IndexProp(id = 8)
	@Column(name="giftCostDiamond", nullable = false)
	private int giftCostDiamond;

	/**兑换的商品复合结构{exchangeId,num}*/
    @IndexProp(id = 9)
	@Column(name="exchangeNum", nullable = false)
	private String exchangeNum;

	/**最后的操作时间用来做数据的清理*/
    @IndexProp(id = 10)
	@Column(name="lastOperTime", nullable = false)
	private long lastOperTime;
	/**
	 * 世界资源收集的次数
	 */
    @IndexProp(id = 11)
	@Column(name="wolrdCollectTimes", nullable = false)
	private int wolrdCollectTimes;

	/**创建时间*/
    @IndexProp(id = 12)
	@Column(name="createTime", nullable = false)
	private long createTime;

	/**更新时间*/
    @IndexProp(id = 13)
	@Column(name="updateTime", nullable = false)
	private long updateTime;

	/**记录是否有效*/
    @IndexProp(id = 14)
	@Column(name="invalid", nullable = false)
	private boolean invalid;

	/** complex type @exchangeNum*/
	@Transient
	private Map<Integer, Integer> exchangeNumMap;

	
	
	public BrokenTwoActivityEntity() {
	}
	
	public BrokenTwoActivityEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}



	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId(){
		return this.playerId; 
	}

	public void setPlayerId(String playerId){
		this.playerId = playerId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getCollectRemainTime(){
		return this.collectRemainTime; 
	}

	public void setCollectRemainTime(int collectRemainTime){
		this.collectRemainTime = collectRemainTime;
	}

	public int getWolrdCollectRemainTime(){
		return this.wolrdCollectRemainTime; 
	}

	public void setWolrdCollectRemainTime(int wolrdCollectRemainTime){
		this.wolrdCollectRemainTime = wolrdCollectRemainTime;
	}

	public int getBeatYuriTimes(){
		return this.beatYuriTimes; 
	}

	public void setBeatYuriTimes(int beatYuriTimes){
		this.beatYuriTimes = beatYuriTimes;
	}

	public int getWishTimes(){
		return this.wishTimes; 
	}

	public void setWishTimes(int wishTimes){
		this.wishTimes = wishTimes;
	}

	public int getGiftCostDiamond(){
		return this.giftCostDiamond; 
	}

	public void setGiftCostDiamond(int giftCostDiamond){
		this.giftCostDiamond = giftCostDiamond;
	}

	public String getExchangeNum(){
		return this.exchangeNum; 
	}

	public void setExchangeNum(String exchangeNum){
		this.exchangeNum = exchangeNum;
	}

	public long getLastOperTime(){
		return this.lastOperTime; 
	}

	public void setLastOperTime(long lastOperTime){
		this.lastOperTime = lastOperTime;
	}

	public long getCreateTime(){
		return this.createTime; 
	}

	public void setCreateTime(long createTime){
		this.createTime = createTime;
	}

	public long getUpdateTime(){
		return this.updateTime; 
	}

	public void setUpdateTime(long updateTime){
		this.updateTime = updateTime;
	}

	public boolean isInvalid(){
		return this.invalid; 
	}

	public void setInvalid(boolean invalid){
		this.invalid = invalid;
	}

	public Map<Integer, Integer> getExchangeNumMap(){
		return this.exchangeNumMap; 
	}

	public void setExchangeNumMap(Map<Integer, Integer> exchangeNumMap){
		this.exchangeNumMap = exchangeNumMap;
	}
	
	@Override
	public void afterRead(){		
		this.exchangeNumMap = SerializeHelper.stringToMap(exchangeNum, Integer.class, Integer.class);

	}
	
	@Override
	public void beforeWrite(){		
		this.exchangeNum = SerializeHelper.mapToString(exchangeNumMap);
	}

	public int getWolrdCollectTimes() {
		return wolrdCollectTimes;
	}

	public void setWolrdCollectTimes(int wolrdCollectTimes) {
		this.wolrdCollectTimes = wolrdCollectTimes;
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
