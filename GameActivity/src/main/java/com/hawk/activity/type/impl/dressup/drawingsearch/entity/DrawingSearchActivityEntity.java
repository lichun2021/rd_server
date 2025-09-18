package com.hawk.activity.type.impl.dressup.drawingsearch.entity;

import com.hawk.activity.type.IActivityDataEntity;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;

/**
 * 装扮投放系列活动一:搜寻图纸(copy 61号活动)
 * @author hf
 */
@Entity
@Table(name="activity_dress_drawing_search")
public class DrawingSearchActivityEntity extends HawkDBEntity implements IActivityDataEntity{

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

	/**最后的操作时间用来做数据的清理*/
    @IndexProp(id = 4)
	@Column(name="lastOperTime", nullable = false)
	private long lastOperTime;

	/**上次余下的时间*/
    @IndexProp(id = 5)
	@Column(name="collectRemainTime", nullable = false)
	private int collectRemainTime;

	/**野外采集上次累计的时间*/
    @IndexProp(id = 6)
	@Column(name="wolrdCollectRemainTime", nullable = false)
	private int wolrdCollectRemainTime;

	/**击败尤里的次数*/
    @IndexProp(id = 7)
	@Column(name="beatYuriTimes", nullable = false)
	private int beatYuriTimes;

	/**许愿次数*/
    @IndexProp(id = 8)
	@Column(name="wishTimes", nullable = false)
	private int wishTimes;

	/**
	 * 世界资源收集的次数
	 */
    @IndexProp(id = 9)
	@Column(name="wolrdCollectTimes", nullable = false)
	private int wolrdCollectTimes;

	/**
	 * 活动期间总掉落数量,新规则有总数限制
	 */
    @IndexProp(id = 10)
	@Column(name="totalDropNum", nullable = false)
	private int totalDropNum;


	/**创建时间*/
    @IndexProp(id = 11)
	@Column(name="createTime", nullable = false)
	private long createTime;

	/**更新时间*/
    @IndexProp(id = 12)
	@Column(name="updateTime", nullable = false)
	private long updateTime;

	/**记录是否有效*/
    @IndexProp(id = 13)
	@Column(name="invalid", nullable = false)
	private boolean invalid;


	public DrawingSearchActivityEntity() {
	}
	
	public DrawingSearchActivityEntity(String playerId, int termId) {
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

	public long getLastOperTime() {
		return lastOperTime;
	}

	public void setLastOperTime(long lastOperTime) {
		this.lastOperTime = lastOperTime;
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

	public int getWolrdCollectTimes() {
		return wolrdCollectTimes;
	}

	public void setWolrdCollectTimes(int wolrdCollectTimes) {
		this.wolrdCollectTimes = wolrdCollectTimes;
	}

	public int getTotalDropNum() {
		return totalDropNum;
	}

	public void setTotalDropNum(int totalDropNum) {
		this.totalDropNum = totalDropNum;
	}

	public void addTotalDropNum(int value) {
		this.totalDropNum = totalDropNum + value;
		this.notifyUpdate();
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
