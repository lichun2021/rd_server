package com.hawk.activity.type.impl.alliesWishing.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.alliesWishing.cfg.AllianceWishKVCfg;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_alliance_wish")
public class AllianceWishEntity extends HawkDBEntity implements IActivityDataEntity {

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
	@Column(name = "numbers", nullable = false)
	private String numbers;
	
    @IndexProp(id = 5)
	@Column(name = "signDays", nullable = false)
	private int signDays;
	
    @IndexProp(id = 6)
	@Column(name = "supplySignDays", nullable = false)
	private int supplySignDays;
	
    @IndexProp(id = 7)
	@Column(name = "lastSignTime", nullable = false)
	private long lastSignTime;
	
    @IndexProp(id = 8)
	@Column(name = "sendGuildCount", nullable = false)
	private String sendGuildCount;
	
    @IndexProp(id = 9)
	@Column(name = "wishMembers", nullable = false)
	private String wishMembers;
	
    @IndexProp(id = 10)
	@Column(name = "wishCount", nullable = false)
	private int wishCount;
	
    @IndexProp(id = 11)
	@Column(name = "luxuryWishCount", nullable = false)
	private int luxuryWishCount;
	
    @IndexProp(id = 12)
	@Column(name = "achiveWish", nullable = false)
	private long achiveWish;
	
    @IndexProp(id = 13)
	@Column(name = "buyGift", nullable = false)
	private int buyGift;
	
    @IndexProp(id = 14)
	@Column(name = "exchangeMsg", nullable = false)
	private String exchangeMsg;
	
    @IndexProp(id = 15)
	@Column(name = "resetCount", nullable = false)
	private int resetCount;

    @IndexProp(id = 16)
	@Column(name = "careIgnore", nullable = false)
	private String careIgnore;
	
    @IndexProp(id = 17)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 18)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 19)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	@Transient
	private Map<Integer,Integer> numberMap = new ConcurrentHashMap<>();
	
	@Transient
	private List<WishMember> wishMemberList = new CopyOnWriteArrayList<WishMember>();
	
	@Transient
	private Map<Integer, Integer> exchangeNumMap = new ConcurrentHashMap<>();
	
	@Transient
	private Set<Integer> careIgnoreList = new ConcurrentHashSet<Integer>();
	
	public AllianceWishEntity(){}
	
	public AllianceWishEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.sendGuildCount = "";
		this.careIgnore = "";
		this.exchangeMsg = "";
		this.numbers = "";
		
	}
	
	
	@Override
	public void beforeWrite() {
		this.exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
		this.numbers = SerializeHelper.mapToString(this.numberMap);
		this.wishMembers = WishMember.serializList(this.wishMemberList);
		this.careIgnore = SerializeHelper.collectionToString(this.careIgnoreList,SerializeHelper.ATTRIBUTE_SPLIT);
		
	}

	@Override
	public void afterRead() {
		SerializeHelper.stringToMap(this.exchangeMsg, Integer.class, Integer.class,this.exchangeNumMap);
		SerializeHelper.stringToMap(this.numbers,Integer.class, Integer.class,this.numberMap);
		this.wishMemberList = WishMember.mergeFromList(this.wishMembers);
		SerializeHelper.stringToSet(Integer.class, this.careIgnore, SerializeHelper.ATTRIBUTE_SPLIT,null,this.careIgnoreList);
		
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
	
	public int getSignDays() {
		return signDays;
	}
	
	public void setSignDays(int signDays) {
		this.signDays = signDays;
	}
	
	
	public int getSupplySignDays() {
		return supplySignDays;
	}
	
	public void setSupplySignDays(int supplySignDays) {
		this.supplySignDays = supplySignDays;
	}
	
	public long getLastSignTime() {
		return lastSignTime;
	}
	
	public void setLastSignTime(long lastSignTime) {
		this.lastSignTime = lastSignTime;
	}
	
	public int getWishCount() {
		return wishCount;
	}
	
	
	public void setWishCount(int wishCount) {
		this.wishCount = wishCount;
	}
	
	public int getLuxuryWishCount() {
		return luxuryWishCount;
	}
	
	
	public void setLuxuryWishCount(int luxuryWishCount) {
		this.luxuryWishCount = luxuryWishCount;
	}
	
	
	public void setBuyGift(int buyGift) {
		this.buyGift = buyGift;
	}
	
	public int getBuyGift() {
		return buyGift;
	}
	
	public long getAchiveWish() {
		return achiveWish;
	}
	
	public void setAchiveWish(long achiveWish) {
		this.achiveWish = achiveWish;
	}
	
	
	public int getResetCount() {
		return resetCount;
	}

	public void setResetCount(int resetCount) {
		this.resetCount = resetCount;
	}
	
	
	public List<WishMember> getWishMemberList() {
		return wishMemberList;
	}

	
	public Map<Integer, Integer> getExchangeNumMap() {
		return exchangeNumMap;
	}
	
	/**
	 * 获取联盟帮助刷新时间
	 * 
	 * 不想给线上数据添加字段。
	 * 直接用sendGuildCount 这个字段存发送联盟帮助的时间戳
	 * @return
	 */
	public long getGuildSendTime(){
		if(HawkOSOperator.isEmptyString(this.sendGuildCount)){
			return 0;
		}
		return Long.parseLong(this.sendGuildCount);
	}
	
	/**
	 * 设置联盟帮助刷新时间
	 * 
	 * 不想给线上数据添加字段。
	 * 直接用sendGuildCount 这个字段存发送联盟帮助的时间戳
	 */
	public void setSendGuildTime(long time){
		this.sendGuildCount = String.valueOf(time);
	}
	
	
	
	public void addGuildWishMember(WishMember member){
		this.wishMemberList.add(member);
		this.notifyUpdate();
	}
	
	public int getExchangeCount(int exchangeId){
		return this.exchangeNumMap.getOrDefault(exchangeId,0);
	}
	
	
	public void addExchangeCount(int eid,int count){
		if(count <=0){
			return;
		}
		count += this.getExchangeCount(eid);
		this.exchangeNumMap.put(eid, count);
		this.notifyUpdate();
	}
	
	public void resetExchange(){
		this.exchangeNumMap.clear();
		this.notifyUpdate();
	}
	
	
	public Map<Integer, Integer> getNumberMap() {
		return numberMap;
	}
	
	public int getNumByPos(int pos){
		return numberMap.getOrDefault(pos, 0);
	}
	
	public void updateNumByPos(int pos,int num){
		numberMap.put(pos, num);
		this.notifyUpdate();
	}
	
	
	public List<Integer> getNumList(){
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		int numCount = cfg.getNumCount();
		List<Integer> numList = new ArrayList<>(numCount);
		for(int i=1;i<=numCount;i++){
			int num = this.getNumByPos(i);
			numList.add(num);
		}
		return numList;
	}
	
	public int getTotalSignDays(){
		return this.signDays + this.supplySignDays;
	}
	
	public boolean allMax(){
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		int numCount = cfg.getNumCount();
		for(int i=1;i<=numCount;i++){
			int num = this.getNumByPos(i);
			if(num < 9){
				return false;
			}
		}
		return true;
	}
	
	public Set<Integer> getCareIgnoreList() {
		return careIgnoreList;
	}
	
	
	public void addCareIgnore(int id){
		if(!careIgnoreList.contains(id)){
			careIgnoreList.add(id);
		}
		this.notifyUpdate();
	}
	
	public void removeCareIgnore(int id){
		careIgnoreList.remove(id);
		this.notifyUpdate();
	}
	
}
