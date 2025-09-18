package com.hawk.activity.type.impl.plantFortress.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import com.hawk.game.protocol.Activity.PlantFortressRewardType;
import com.hawk.serialize.string.SerializeHelper;
/**
 * 时空豪礼数据实体
 * @author che
 *
 */
@Entity
@Table(name = "activity_plant_fortress")
public class PlantFortressEntity  extends HawkDBEntity implements IActivityDataEntity {

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
	@Column(name = "stageId", nullable = false)
	private int stageId;
	
    @IndexProp(id = 5)
	@Column(name = "rewardShow", nullable = false)
	private int rewardShow;
	
    @IndexProp(id = 6)
	@Column(name = "bigRewardId", nullable = false)
	private int bigRewardId;
	
    @IndexProp(id = 7)
	@Column(name = "tickets", nullable = false)
	private String tickets;
	
    @IndexProp(id = 8)
	@Column(name = "bigAwardTimes", nullable = false)
	private int bigAwardTimes;
	
	
    @IndexProp(id = 9)
	@Column(name = "openCount", nullable = false)
	private int openCount;
	
	
    @IndexProp(id = 10)
	@Column(name = "rewardChoose", nullable = false)
	private String rewardChoose;
	
    @IndexProp(id = 11)
	@Column(name = "bigRewardCount", nullable = false)
	private String bigRewardCount;
	
    @IndexProp(id = 12)
	@Column(name = "buyCount", nullable = false)
	private int buyCount;
	
    @IndexProp(id = 13)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 14)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 15)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	/** 时空之门列表*/
	@Transient
	private List<PlantFortressTicket> ticketList = 
		new CopyOnWriteArrayList<PlantFortressTicket>();
	
	/** 成就任务列表*/
	@Transient
	private Map<Integer, Integer> rewardChooseMap = new ConcurrentHashMap<Integer, Integer>();
	
	/** 大奖获取次数列表*/
	@Transient
	private Map<Integer, Integer> bigRewardCountMap = new ConcurrentHashMap<Integer, Integer>();
	
	
	public PlantFortressEntity() {
		
	}
	
	public PlantFortressEntity(String playerId, int termId) {
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

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	
	public int getBigRewardId() {
		return bigRewardId;
	}

	public void setBigRewardId(int bigRewardId) {
		this.bigRewardId = bigRewardId;
	}

	public String getTickets() {
		return tickets;
	}

	public void setTickets(String tickets) {
		this.tickets = tickets;
	}

	public int getBigAwardTimes() {
		return bigAwardTimes;
	}

	public void setBigAwardTimes(int bigAwardTimes) {
		this.bigAwardTimes = bigAwardTimes;
	}
	

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}

	public String getRewardChoose() {
		return rewardChoose;
	}

	public void setRewardChoose(String rewardChoose) {
		this.rewardChoose = rewardChoose;
	}

	public List<PlantFortressTicket> getTicketList() {
		return ticketList;
	}

	public void setTicketList(List<PlantFortressTicket> ticketList) {
		this.ticketList = ticketList;
	}

	
	public Map<Integer, Integer> getRewardChooseMap() {
		return rewardChooseMap;
	}

	public void setRewardChooseMap(Map<Integer, Integer> rewardChooseMap) {
		this.rewardChooseMap = rewardChooseMap;
	}

	
	
	public int getRewardShow() {
		return rewardShow;
	}

	public void setRewardShow(int rewardShow) {
		this.rewardShow = rewardShow;
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
		this.tickets = SerializeHelper.collectionToString(this.ticketList, SerializeHelper.ELEMENT_DELIMITER);
		this.rewardChoose = SerializeHelper.mapToString(this.rewardChooseMap);
		this.bigRewardCount = SerializeHelper.mapToString(this.bigRewardCountMap);
	}
	
	@Override
	public void afterRead() {
		this.ticketList.clear();
		SerializeHelper.stringToList(PlantFortressTicket.class, this.tickets, this.ticketList);
		SerializeHelper.stringToMap(this.rewardChoose, Integer.class, Integer.class, this.rewardChooseMap);
		SerializeHelper.stringToMap(this.bigRewardCount, Integer.class, Integer.class, this.bigRewardCountMap);
	}

	
	
	public boolean isBigReward(){
		for(PlantFortressTicket ticket : this.ticketList){
			if(ticket.getRewardType() == 
					PlantFortressRewardType.PLANT_FORTRESS_BIG_REWEARD_VALUE){
				return true;
			}
		}
		return false;
	}
	
	public PlantFortressTicket getTicket(int id){
		for(PlantFortressTicket ticket : this.ticketList){
			if(ticket.getTicketId() == id){
				return ticket;
			}
		}
		return null;
	}
	
	
	public void addPlantFortressTicket(PlantFortressTicket ticket){
		this.ticketList.add(ticket);
	}
	
	
	public int getDefaultChose(int pool){
		if(this.rewardChooseMap.containsKey(pool)){
			return this.rewardChooseMap.get(pool);
		}
		return 0;
	}
	
	public void updateDeDefaultChose(int pool,int rewardId){
		this.rewardChooseMap.put(pool, rewardId);
	}
	
	public void removeDeDefaultChose(int pool){
		this.rewardChooseMap.remove(pool);
	}
	
	public int getBigRewardCount(int id){
		if(this.bigRewardCountMap.containsKey(id)){
			return this.bigRewardCountMap.get(id);
		}
		return 0;
	}
	
	
	public Map<Integer, Integer> getBigRewardCountMap() {
		return bigRewardCountMap;
	}

	

	public void addBigRewardCount(int id,int num){
		int count = this.getBigRewardCount(id);
		count += num;
		this.bigRewardCountMap.put(id, count);
	}
	
	public Map<Integer,Integer> getCommRewardMap(){
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		for(PlantFortressTicket ticket : this.ticketList){
			if(ticket.getRewardType() != PlantFortressRewardType.PLANT_FORTRESS_COMM_REWEARD_VALUE){
				continue;
			}
			int rewardId = ticket.getRewardId();
			if(map.containsKey(rewardId)){
				int value = map.get(rewardId);
				map.put(rewardId, value + 1);
				continue;
			}
			map.put(rewardId,1);
		}
		return map;
	}
	
	public void addOpenCount(int add){
		this.openCount += add;
	}

	public int getBuyCount() {
		return buyCount;
	}

	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}
	
	
}
