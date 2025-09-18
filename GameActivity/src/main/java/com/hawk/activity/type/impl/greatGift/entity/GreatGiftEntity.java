package com.hawk.activity.type.impl.greatGift.entity;

import java.util.ArrayList;
import java.util.List;
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
import com.hawk.game.protocol.Activity.greatGiftInfo;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_great_gift")
public class GreatGiftEntity extends HawkDBEntity implements IActivityDataEntity{

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
	
	/** 已经购买的礼包 **/
    @IndexProp(id = 4)
	@Column(name = "buyBag", nullable = false)
	private String buyBag;
	
	/** 已经领取的宝箱 **/
    @IndexProp(id = 5)
	@Column(name = "recieveChest", nullable = false)
	private String recieveChest;

    @IndexProp(id = 6)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 8)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
    
    /** 游戏外购买的礼包 **/
    @IndexProp(id = 9)
	@Column(name = "outBuyBag", nullable = false)
	private String outBuyBag;
    
    // 所有档位购买完成的时间
    @IndexProp(id = 10)
	@Column(name = "finishTime", nullable = false)
	private long finishTime;
    
	
	/** 购买礼包的集合 **/
	@Transient
	private List<String> bagList = new ArrayList<String>();
	
	/** 已领取宝箱的集合 **/
	@Transient
	private List<Integer> chestList = new ArrayList<>();
	
	//可以领取的宝箱奖励
	@Transient
	private List<Integer> canRecieveChestIds = null;
	
	/** 游戏外购买礼包的集合 **/
	@Transient
	private List<String> outBagList = new ArrayList<String>();
	
	
	public GreatGiftEntity(){}
	
	public GreatGiftEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
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

	public String getRecieveChest() {
		return recieveChest;
	}

	public void setRecieveChest(String recieveChest) {
		this.recieveChest = recieveChest;
	}

	public String getBuyBag() {
		return buyBag;
	}

	public void setBuyBag(String buyBag) {
		this.buyBag = buyBag;
	}
	
	public String getOutBuyBag() {
		return outBuyBag;
	}

	public void setOutBuyBag(String outBuyBag) {
		this.outBuyBag = outBuyBag;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	@Override
	public void afterRead() {
		chestList = SerializeHelper.cfgStr2List(recieveChest, SerializeHelper.ATTRIBUTE_SPLIT);
		if(buyBag == null || buyBag.equals("")){
			bagList = new ArrayList<>();
			return;
		}
		String src[] = buyBag.split(SerializeHelper.ATTRIBUTE_SPLIT);
		bagList = new ArrayList<>();
		for(String id : src){
			if(!id.equals("")){
				bagList.add(id);
			}
		}
		
		this.outBagList.clear();
		SerializeHelper.stringToList(String.class, this.outBuyBag, this.outBagList);
	}

	@Override
	public void beforeWrite() {
		if(buyBag == null){
			buyBag = "";
		}
		if(recieveChest == null){
			recieveChest = "";
		}
		
		this.outBuyBag = SerializeHelper.collectionToString(this.outBagList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	public List<String> getOutBagList() {
		return outBagList;
	}

	public void userBuyBag(String id){
		if(!bagList.contains(id)){
			bagList.add(id);
			refreshBuyBag();
		}else{
			throw new RuntimeException("Buy GreatGiftActivity treasure, but id repeated.");
		}
	}
	
	public void refreshBuyBag() {
		StringBuilder sb = new StringBuilder();
		for(String i : bagList){
			sb.append(i)
			.append(SerializeHelper.ATTRIBUTE_SPLIT);
		}
		setBuyBag(sb.toString());
	} 
	
	public boolean isBuy(String giftId){
		return bagList.contains(giftId);
	}
	
	public List<String> getBagList() {
		return bagList;
	}

	public boolean isRecieve(int chestId){
		return chestList.contains(chestId);
	}
	
	public void recieveChest(int id){
		if(!chestList.contains(id)){
			chestList.add(id);
			StringBuilder sb = new StringBuilder();
			for(Integer i : chestList){
				sb.append(i)
				.append(SerializeHelper.ATTRIBUTE_SPLIT);
			}
			canRecieveChestIds.remove(new Integer(id));
			setRecieveChest(sb.toString());
		}else{
			throw new RuntimeException("recieve GreatGiftActivity package, but id repeated.");
		}
	}
	
	public void buildResultInfo(greatGiftInfo.Builder build){
		for(String id : bagList){
			build.addBags(id);
		}
		for(Integer id : chestList){
			build.addChests(id);
		}
	}

	public List<Integer> getCanRecieveChestIds() {
		return canRecieveChestIds;
	}

	public void setCanRecieveChestIds(List<Integer> canRecieveChestIds) {
		this.canRecieveChestIds = canRecieveChestIds;
	}
	
	/***
	 * 玩家都购买完了，跨天重置
	 */
	public void crossDay(){
		buyBag = "";
		recieveChest = "";
		finishTime = 0;
		bagList.clear();
		outBagList.clear();
		chestList.clear();
		canRecieveChestIds = new ArrayList<>();
	}

	@Override
	public String toString() {
		return "GreatGiftEntity [id=" + id + ", playerId=" + playerId + ", termId=" + termId + ", buyBag=" + buyBag
				+ ", recieveChest=" + recieveChest + ", createTime=" + createTime + ", updateTime=" + updateTime
				+ ", invalid=" + invalid + "]";
	}
}
