package com.hawk.game.entity; 

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import org.hibernate.annotations.GenericGenerator;

/**
*	
*	auto generate do not modified
*/
@Entity
@Table(name="player_relation")
public class PlayerRelationEntity extends HawkDBEntity{

	/**主键*/
	@Id
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

	/**玩家ID*/
	@Column(name="playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId; 

	/**关联玩家ID*/
	@Column(name="targetPlayerId", nullable = false)
    @IndexProp(id = 3)
	private String targetPlayerId;

	/**类型, 1为好友,2为黑名单*/
	@Column(name="type", nullable = false)
    @IndexProp(id = 4)
	private int type;

	/***/
	@Column(name="love", nullable = false)
    @IndexProp(id = 5)
	private int love;
	
	@Column(name="remark", nullable=false)
    @IndexProp(id = 6)
	private String remark;
	
	/**创建时间*/
	@Column(name="createTime", nullable = false)
    @IndexProp(id = 7)
	private long createTime;

	/**更新时间*/
	@Column(name="updateTime", nullable = false)
    @IndexProp(id = 8)
	private long updateTime;

	/**是否有效*/
	@Column(name="invalid", nullable = false)
    @IndexProp(id = 9)
	private boolean invalid;
	
	/**
	 * 是否是守护关系, 之所以不存在redis上面是担心单个key玩家数据量太多了.按单个服一起存的话,合服又比较麻烦.
	 */
	@Column(name="guard", nullable = false)
	@IndexProp(id = 10)
	private boolean guard;
	/**
	 * 守护值
	 */
	@Column(name="guardValue", nullable = false)
	@IndexProp(id = 11)
	private int guardValue;
	/**
	 * 升级守护时间.
	 */
	@Column(name="operationTime", nullable = false)
	@IndexProp(id = 12)
	private long operationTime;
	
	
	@Column(name="dressId", nullable = false)
	@IndexProp(id = 13)
	private int dressId; 
	
	public String getId(){
		return this.id; 
	}

	public void setId(String id){
		this.id = id;
	}

	public String getPlayerId(){
		return this.playerId; 
	}

	public void setPlayerId(String playerId){
		this.playerId = playerId;
	}

	public String getTargetPlayerId(){
		return this.targetPlayerId; 
	}

	public void setTargetPlayerId(String targetPlayerId){
		this.targetPlayerId = targetPlayerId;
	}

	public int getType(){
		return this.type; 
	}

	public void setType(int type){
		this.type = type;
	}

	public int getLove(){
		return this.love; 
	}

	public void setLove(int love){
		this.love = love;
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

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {		
		this.id = primaryKey;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public boolean isGuard() {
		return guard;
	}

	public void setGuard(boolean guard) {
		this.guard = guard;
	}

	public int getGuardValue() {
		return guardValue;
	}

	public void setGuardValue(int guardValue) {
		this.guardValue = guardValue;
	}

	public long getOperationTime() {
		return operationTime;
	}

	public void setOperationTime(long operationTime) {
		this.operationTime = operationTime;
	}

	public int getDressId() {
		return dressId;
	}

	public void setDressId(int dressId) {
		this.dressId = dressId;
	}
}
