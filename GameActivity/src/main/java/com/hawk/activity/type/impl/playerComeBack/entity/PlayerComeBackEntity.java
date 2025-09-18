package com.hawk.activity.type.impl.playerComeBack.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Activity.ComeBackPlayerRewardInfo;
import com.hawk.serialize.string.SerializeHelper;

/***
 * 老玩家回归
 * @author yang.rao
 *
 */

@Entity
@Table(name="activity_player_comeback")
public class PlayerComeBackEntity extends HawkDBEntity implements IActivityDataEntity {

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
	
	/** 该标识，用于判定回归的老玩家是否初始化 **/
    @IndexProp(id = 4)
	@Column(name = "init", nullable = false)
	private boolean init;
	
	/** 活动开启的时间 **/
    @IndexProp(id = 5)
	@Column(name = "startTime", nullable = false)
	private long startTime;
	
    @IndexProp(id = 6)
	@Column(name = "accountLogoutTime", nullable = false)
	private long accountLogoutTime;
	
	/** 回归豪礼领取到的奖励id **/
    @IndexProp(id = 7)
	@Column(name = "rewardInfos", nullable = true)
	private String rewardInfos;
	
	/** 发展冲刺成就任务信息 **/
    @IndexProp(id = 8)
	@Column(name = "achieveInfos", nullable = true)
	private String achieveInfos;
	
	/** 低折回馈购买信息 **/
    @IndexProp(id = 9)
	@Column(name = "buyInfos", nullable = true)
	private String buyInfos;
	
	/** 发展军资换购信息 **/
    @IndexProp(id = 10)
	@Column(name = "exchangeInfos", nullable = true)
	private String exchangeInfos;
	
    @IndexProp(id = 11)
	@Column(name = "loginDay", nullable = false)
	private int loginDay;

    @IndexProp(id = 12)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 13)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	
    @IndexProp(id = 14)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<Integer> rewardList = new ArrayList<Integer>();
	
	@Transient
	private Map<Integer, Integer> exchangeNumMap = new HashMap<>();
	
	/** 已经购买的信息 **/
	@Transient
	private Map<Integer, Integer> buyMsg = new HashMap<Integer, Integer>();
	
	/** 成就信息 **/
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	public PlayerComeBackEntity(){}
	
	public PlayerComeBackEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
	}
	
	/***
	 * 初始化老玩家回归活动数据
	 */
	public void init(){
		long time = HawkTime.getMillisecond();
		this.startTime = time;
		this.rewardInfos = "";
		this.achieveInfos = "";
		this.buyInfos = "";
		this.exchangeInfos = "";
		this.loginDay = 1;
		this.init = true;
	}
	
	@Override
	public void beforeWrite() {
		rewardInfos = SerializeHelper.collectionToString(rewardList, SerializeHelper.ATTRIBUTE_SPLIT);
	    achieveInfos = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	    buyInfos = SerializeHelper.mapToString(buyMsg);
	    exchangeInfos = SerializeHelper.mapToString(exchangeNumMap);
	}

	@Override
	public void afterRead() {
		rewardList = SerializeHelper.cfgStr2List(rewardInfos);
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveInfos, this.itemList);
		buyMsg = SerializeHelper.stringToMap(buyInfos, Integer.class, Integer.class);
		exchangeNumMap = SerializeHelper.stringToMap(exchangeInfos, Integer.class, Integer.class);
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

	public boolean isInit() {
		return init;
	}
	
	public Map<Integer, Integer> getExchangeNumMap() {
		return exchangeNumMap;
	}

	public Map<Integer, Integer> getBuyMsg() {
		return buyMsg;
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/***
	 * 判断是否有回归大礼奖励
	 * @return
	 */
	public boolean checkHasGreatReward(){
		if(!init){ //从未初始化过，不用再判断了。
			return false;
		}
		long time = HawkTime.getMillisecond();
		if(startTime <= time && rewardList.size() == 0){
			return true;
		}
		return false;
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
	
	public void onPlayerBuy(int chestId, int count){
		if(count <= 0){
			return;
		}
		Integer buyObj = buyMsg.get(chestId);
		int already = buyObj == null ? 0 : buyObj.intValue();
		int nowBuy = already + count;
		buyMsg.put(chestId, nowBuy);
	}
	
	public void onPlayerTakeReward(int rewardId){
		rewardList.add(rewardId);
		notifyUpdate();
	}
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public void buildRewardBuilder(ComeBackPlayerRewardInfo.Builder build){
		for(Integer rewardId : rewardList){
			build.addRecieveIds(rewardId);
		}
		long time = HawkTime.getMillisecond();
		int day = (int)((time - accountLogoutTime) / HawkTime.DAY_MILLI_SECONDS);
		if(day < 0){
			HawkLog.errPrintln("PlayerComeBack lost time error, curTime:{}, logoutTime:{}", time, accountLogoutTime);
			day = 0;
		}
		build.setLostDay(day);
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

	public String getRewardInfos() {
		return rewardInfos;
	}

	public void setRewardInfos(String rewardInfos) {
		this.rewardInfos = rewardInfos;
	}

	public String getAchieveInfos() {
		return achieveInfos;
	}

	public void setAchieveInfos(String achieveInfos) {
		this.achieveInfos = achieveInfos;
	}

	public String getBuyInfos() {
		return buyInfos;
	}

	public void setBuyInfos(String buyInfos) {
		this.buyInfos = buyInfos;
	}

	public String getExchangeInfos() {
		return exchangeInfos;
	}

	public void setExchangeInfos(String exchangeInfos) {
		this.exchangeInfos = exchangeInfos;
	}

	public List<Integer> getRewardList() {
		return rewardList;
	}

	public void setRewardList(List<Integer> rewardList) {
		this.rewardList = rewardList;
	}

	public void setInit(boolean init) {
		this.init = init;
	}

	public void setExchangeNumMap(Map<Integer, Integer> exchangeNumMap) {
		this.exchangeNumMap = exchangeNumMap;
	}

	public void setBuyMsg(Map<Integer, Integer> buyMsg) {
		this.buyMsg = buyMsg;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}

	public int getLoginDay() {
		return loginDay;
	}

	public void setLoginDay(int loginDay) {
		this.loginDay = loginDay;
	}

	public long getAccountLogoutTime() {
		return accountLogoutTime;
	}

	public void setAccountLogoutTime(long accountLogoutTime) {
		this.accountLogoutTime = accountLogoutTime;
	}
	
	public boolean isStartDay(){
		if(startTime == 0){
			return true;
		}
		long time = HawkTime.getMillisecond();
		if(HawkTime.isSameDay(startTime, time)){
			return true;
		}
		return false;
	}
}
