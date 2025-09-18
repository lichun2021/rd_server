package com.hawk.activity.type.impl.timeLimitLogin.entity; 

import org.hawk.db.HawkDBEntity;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_time_limit_login")
public class TimeLimitLoginEntity extends HawkDBEntity implements IActivityDataEntity{

	/***/
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

	/***/
    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false, length=64)
	private String playerId;

	/***/
    @IndexProp(id = 3)
	@Column(name="termId", nullable = false, length=10)
	private int termId;

	/**限时登录信息 id_status*/  
    @IndexProp(id = 4)
	@Column(name="loginData", nullable = false, length=10)
	private String loginData;  

	/***/
    @IndexProp(id = 5)
	@Column(name="createTime", nullable = false, length=19)
	private long createTime;

	/***/
    @IndexProp(id = 6)
	@Column(name="updateTime", nullable = false, length=19)
	private long updateTime;

	/***/
    @IndexProp(id = 7)
	@Column(name="invalid", nullable = false, length=3)
	private boolean invalid;
	
	@Transient
	private List<Integer> loginDataList = new ArrayList<>();
		
	public TimeLimitLoginEntity(){
		
	}
	
	public TimeLimitLoginEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
		this.loginData = "";
	}
	
	public String getId() {
		return this.id; 
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return this.termId; 
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	@Override
	public long getCreateTime() {
		return this.createTime; 
	}
	@Override
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	@Override
	public long getUpdateTime() {
		return this.updateTime; 
	}
	@Override
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
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
	public boolean isInvalid() {
		return false;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
	@Override
	public void beforeWrite() {
		loginData = SerializeHelper.collectionToString(loginDataList, SerializeHelper.ATTRIBUTE_SPLIT);
	}

	@Override
	public void afterRead() {
		loginDataList = SerializeHelper.cfgStr2List(loginData);
	}
	
	
	public void addStatus(int id){
		if(loginDataList.contains(id)){
			return;
		}
		loginDataList.add(id);
		this.notifyUpdate();
	}

	public String getLoginData() {
		return loginData;
	}

	public void setLoginData(String loginData) {
		this.loginData = loginData;
	}

	public boolean containStatus(int id){
		return this.loginDataList.contains(id);
	}
	

}
