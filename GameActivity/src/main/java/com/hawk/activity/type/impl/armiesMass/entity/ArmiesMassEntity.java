package com.hawk.activity.type.impl.armiesMass.entity;

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

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.armiesMass.ArmiesMassGift;
import com.hawk.activity.type.impl.armiesMass.ArmiesMassSculpture;
import com.hawk.serialize.string.SerializeHelper;
/**
 * 时空豪礼数据实体
 * @author che
 *
 */
@Entity
@Table(name = "activity_armies_mass")
public class ArmiesMassEntity  extends HawkDBEntity implements IActivityDataEntity {

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
	@Column(name = "stage", nullable = false)
	private int stage;
	
    @IndexProp(id = 5)
	@Column(name = "share", nullable = false)
	private int share;
	
    @IndexProp(id = 6)
	@Column(name = "sculptureOpenCount", nullable = false)
	private int sculptureOpenCount;
	
    @IndexProp(id = 7)
	@Column(name = "sculptures", nullable = false)
	private String sculptures;
	
    @IndexProp(id = 8)
	@Column(name = "freeAwards", nullable = false)
	private String freeAwards;
	
    @IndexProp(id = 9)
	@Column(name = "buyGifts", nullable = false)
	private String buyGifts;
	
	
	
	
    @IndexProp(id = 10)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 11)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 12)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	/** 雕像列表*/
	@Transient
	private List<ArmiesMassSculpture> sculptureList = new 
		CopyOnWriteArrayList<ArmiesMassSculpture>();


	/** 礼包购买情况*/
	@Transient
	private List<ArmiesMassGift> buyGiftList = new 
			CopyOnWriteArrayList<ArmiesMassGift>();
	
	
	@Transient
	private List<ArmiesMassGift> freeAwardList = new 
			CopyOnWriteArrayList<ArmiesMassGift>();
	
	
	
	
	public ArmiesMassEntity() {
		
	}
	
	public ArmiesMassEntity(String playerId, int termId) {
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

	
	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	

	public int getShare() {
		return share;
	}

	public void setShare(int share) {
		this.share = share;
	}



	public int getSculptureOpenCount() {
		return sculptureOpenCount;
	}

	public void setSculptureOpenCount(int sculptureOpenCount) {
		this.sculptureOpenCount = sculptureOpenCount;
	}

	public List<ArmiesMassSculpture> getSculptureList() {
		return sculptureList;
	}

	public void setSculptureList(List<ArmiesMassSculpture> sculptureList) {
		this.sculptureList = sculptureList;
	}
	
	public void reSetSculptureList(List<ArmiesMassSculpture> sculptureList) {
		this.sculptureList = sculptureList;
		this.notifyUpdate();
	}

	
	public ArmiesMassSculpture getSculptureByPosition(int position){
		for(ArmiesMassSculpture ele : this.sculptureList){
			if(ele.getPostion() == position){
				return ele;
			}
		}
		return null;
	}
	
	public int getSculptureCloseSize(){
		int size = 0;
		for(ArmiesMassSculpture ele : this.sculptureList){
			if(ele.getState()<1){
				size ++;
			}
		}
		return size;
	}
	
	
	
	public ArmiesMassSculpture getSculptureByQulity(int qulity){
		for(ArmiesMassSculpture ele : this.sculptureList){
			if(ele.getQuality() == qulity){
				return ele;
			}
		}
		return null;
	}
	
	


	public List<ArmiesMassGift> getBuyGiftList() {
		return buyGiftList;
	}

	public void setBuyGiftList(List<ArmiesMassGift> buyGiftList) {
		this.buyGiftList = buyGiftList;
	}

	public List<ArmiesMassGift> getFreeAwardList() {
		return freeAwardList;
	}

	public void setFreeAwardList(List<ArmiesMassGift> freeAwardList) {
		this.freeAwardList = freeAwardList;
	}

	public int getBuyGiftLevel(int type,int groupId){
		for(ArmiesMassGift gift : this.buyGiftList){
			if(gift.getType() == type && gift.getGroup() == groupId){
				return gift.getLevel();
			}
		}
		return 0;
	}
	
	
	public void buyGiftLevelUp(int type,int group,int level){
		ArmiesMassGift rlt = null;
		for(ArmiesMassGift gift : this.buyGiftList){
			if(gift.getType() == type && gift.getGroup() == group){
				rlt = gift;
				break;
			}
		}
		if(rlt != null){
			rlt.setLevel(level);
		}else{
			rlt = ArmiesMassGift.valueOf(type, group, level);
			this.buyGiftList.add(rlt);
		}
		this.notifyUpdate();
	}

	
	
	public int getFreeGiftLevel(int type,int groupId){
		for(ArmiesMassGift gift : this.freeAwardList){
			if(gift.getType() == type && gift.getGroup() == groupId){
				return gift.getLevel();
			}
		}
		return 0;
	}
	
	
	public void freeGiftLevelUp(int type,int group,int level){
		ArmiesMassGift rlt = null;
		for(ArmiesMassGift gift : this.freeAwardList){
			if(gift.getType() == type && gift.getGroup() == group){
				rlt = gift;
				break;
			}
		}
		if(rlt != null){
			rlt.setLevel(level);
			return;
		}
		rlt = ArmiesMassGift.valueOf(type, group, level);
		this.freeAwardList.add(rlt);
		this.notifyUpdate();
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
		this.sculptures = SerializeHelper.collectionToString(this.sculptureList, SerializeHelper.ELEMENT_DELIMITER);
		this.buyGifts = SerializeHelper.collectionToString(this.buyGiftList, SerializeHelper.ELEMENT_DELIMITER);
		this.freeAwards = SerializeHelper.collectionToString(this.freeAwardList, SerializeHelper.ELEMENT_DELIMITER);
		
	}
	
	@Override
	public void afterRead() {
		this.sculptureList.clear();
		this.buyGiftList.clear();
		this.freeAwardList.clear();
		SerializeHelper.stringToList(ArmiesMassSculpture.class, this.sculptures, this.sculptureList);
		SerializeHelper.stringToList(ArmiesMassGift.class, this.buyGifts, this.buyGiftList);
		SerializeHelper.stringToList(ArmiesMassGift.class, this.freeAwards, this.freeAwardList);
		
	}

	

	
	
}
