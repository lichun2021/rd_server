package com.hawk.activity.type.impl.playerteamback.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.os.HawkException;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_playerteam_back")
public class PlayerTeamBackEntity extends AchieveActivityEntity implements IActivityDataEntity {

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
	
	/**
	 * 跨天更新时间
	 */
    @IndexProp(id = 4)
	@Column(name = "refreshTime", nullable = false)
	private long refreshTime;
	
	/** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	/** 账号鉴定的鉴定星数量  */
    @IndexProp(id = 6)
	@Column(name = "starNum", nullable = false)
	private int starNum;
	
	/** 组队ID */
    @IndexProp(id = 7)
	@Column(name = "teamId", nullable = false)
	private int teamId;
	
	/** H5页面传回的奖励情况  */
    @IndexProp(id = 8)
	@Column(name = "rewardInfos", nullable = false)
	private String rewardInfos;
	
	/** H5页面传回的战队成员信息  */
    @IndexProp(id = 9)
	@Column(name = "teamMemberInfos", nullable = false)
	private String teamMemberInfos;

    @IndexProp(id = 10)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 11)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 12)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private List<MemberInfo> memberList = new CopyOnWriteArrayList<MemberInfo>();
	
	@Transient
	private List<Integer> rewardList = new CopyOnWriteArrayList<Integer>();

	public PlayerTeamBackEntity() {
	}

	public PlayerTeamBackEntity(String playerId, int termId) {
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

	public String getAchieveItems() {
		return achieveItems;
	}
	
	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public void addItem(AchieveItem item) {
		this.itemList.add(item);
	}

	@Override
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}
	
	public int getStarNum() {
		return starNum;
	}

	public void setStarNum(int starNum) {
		this.starNum = starNum;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}
	
	public List<MemberInfo> getMemberList() {
		return memberList;
	}

	public List<Integer> getRewardList() {
		return rewardList;
	}

	@Override
	public void beforeWrite() {
		try{
			this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);			
			this.teamMemberInfos = SerializeHelper.collectionToString(this.memberList, SerializeHelper.ELEMENT_DELIMITER, SerializeHelper.BETWEEN_ITEMS);
			this.rewardInfos = SerializeHelper.collectionToString(this.rewardList, SerializeHelper.ELEMENT_DELIMITER);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}

	@Override
	public void afterRead() {
		try{
			this.itemList.clear();
			this.rewardList.clear();
			this.memberList.clear();
			SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
			SerializeHelper.stringToList(MemberInfo.class, this.teamMemberInfos, SerializeHelper.ELEMENT_SPLIT, SerializeHelper.BETWEEN_ITEMS, this.memberList);
			SerializeHelper.stringToList(Integer.class, this.rewardInfos, this.rewardList);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}

}
