package com.hawk.game.entity;

import java.util.*;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;

/**
 * 联盟实体
 *
 * @author shadow
 *
 */
@Entity
@Table(name = "guild_info")
public class GuildInfoEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "name", unique = true, nullable = false)
    @IndexProp(id = 2)
	private String name;

	@Column(name = "tag", unique = true, nullable = false)
    @IndexProp(id = 3)
	private String tag;

	@Column(name = "flagId", nullable = false)
    @IndexProp(id = 4)
	private int flagId;

	@Column(name = "langId", nullable = false)
    @IndexProp(id = 5)
	private String langId;

	@Column(name = "level")
    @IndexProp(id = 6)
	private int level;

	@Column(name = "leaderId", nullable = false)
    @IndexProp(id = 7)
	private String leaderId;

	@Column(name = "leaderName", nullable = false)
    @IndexProp(id = 8)
	private String leaderName;

	@Column(name = "leaderOfflineTime")
    @IndexProp(id = 9)
	private long leaderOfflineTime = 0;

	@Column(name = "coleaderId")
    @IndexProp(id = 10)
	private String coleaderId;

	@Column(name = "coleaderName")
    @IndexProp(id = 11)
	private String coleaderName;

	@Column(name = "isNeedPermition", nullable = false)
    @IndexProp(id = 12)
	private boolean isNeedPermition;

	@Column(name = "needBuildingLevel")
    @IndexProp(id = 13)
	private int needBuildingLevel;

	@Column(name = "needPower")
    @IndexProp(id = 14)
	private int needPower;

	@Column(name = "needCommanderLevel")
    @IndexProp(id = 15)
	private int needCommanderLevel;

	@Column(name = "announcement")
    @IndexProp(id = 16)
	private String announcement;

	@Column(name = "notice")
    @IndexProp(id = 17)
	private String notice;

	@Column(name = "l1Name")
    @IndexProp(id = 18)
	private String l1Name;

	@Column(name = "l2Name")
    @IndexProp(id = 19)
	private String l2Name;

	@Column(name = "l3Name")
    @IndexProp(id = 20)
	private String l3Name;

	@Column(name = "l4Name")
    @IndexProp(id = 21)
	private String l4Name;

	@Column(name = "l5Name")
    @IndexProp(id = 22)
	private String l5Name;

	@Column(name = "needLang")
    @IndexProp(id = 23)
	private String needLang;

	@Column(name = "score")
    @IndexProp(id = 24)
	private long score;

	@Column(name = "lastDonateCheckTime")
    @IndexProp(id = 25)
	private long lastDonateCheckTime;
	
	@Column(name = "clearResNum")
    @IndexProp(id = 26)
	private int clearResNum;
	
	@Column(name = "hasChangeTag", nullable = false)
    @IndexProp(id = 27)
	private boolean hasChangeTag; 
	
	@Column(name = "guildBoundId")
    @IndexProp(id = 28)
	private String guildBoundId;
	
	@Column(name = "authInfo")
    @IndexProp(id = 29)
	private String authInfo;
	
	/** 上次联盟任务刷新事件*/
	@Column(name = "taskRefreshTime", nullable = false)
    @IndexProp(id = 30)
	protected long taskRefreshTime = 0;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 31)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 32)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 33)
	protected boolean invalid;
	
	// 联盟语言模式
	@Column(name = "chatRoomModel")
    @IndexProp(id = 34)
	protected int chatRoomModel = 0;
	
	@Column(name = "xzqTickets")
    @IndexProp(id = 35)
	public int xzqTickets;
	
	@Column(name = "spaceMechaInfo")
    @IndexProp(id = 36)
	public String spaceMechaInfo;
	
	/**
	 * 编队信息
	 */
	@Column(name = "formationInfo")
    @IndexProp(id = 37)
	public String formationInfo;

	/**
	 * 奖励旗帜信息
	 */
	@Column(name = "rewardFlag")
	@IndexProp(id = 38)
	public String rewardFlag;
	
	/**
	 * 盟主平台信息
	 */
	@Column(name = "leaderPlatform", nullable = false)
    @IndexProp(id = 39)
	private String leaderPlatform = "";
	
	
	/**
	 * 联盟权限信息
	 */
	@Transient
	private Map<Integer, List<Integer>> authMap = new HashMap<>();

	/**
	 * 编队
	 */
	@Transient
	private GuildFormationObj formation;

	@Transient
	private Set<Integer> rewardFlagSet = new HashSet<>();
	
	public GuildInfoEntity() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	protected void setTag(String tag) {
		this.tag = tag;
	}

	public int getFlagId() {
		return flagId;
	}

	protected void setFlagId(int flagId) {
		this.flagId = flagId;
	}

	public String getLangId() {
		return langId;
	}

	protected void setLangId(String langId) {
		this.langId = langId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getLeaderId() {
		if (HawkOSOperator.isEmptyString(leaderPlatform)) {
			try {
				Player player = GlobalData.getInstance().makesurePlayer(leaderId);
				if (player != null) {
					this.setLeaderPlatform(player.getPlatform());
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return leaderId;
	}

	protected void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public String getLeaderName() {
		return leaderName;
	}

	protected void setLeaderName(String leaderName) {
		this.leaderName = leaderName;
	}

	public String getColeaderId() {
		return coleaderId;
	}

	protected void setColeaderId(String coleaderId) {
		this.coleaderId = coleaderId;
	}

	public String getColeaderName() {
		return coleaderName;
	}

	protected void setColeaderName(String coleaderName) {
		this.coleaderName = coleaderName;
	}

	public boolean isNeedPermition() {
		return isNeedPermition;
	}

	protected void setNeedPermition(boolean isNeedPermition) {
		this.isNeedPermition = isNeedPermition;
	}

	public String getAnnouncement() {
		return announcement;
	}

	protected void setAnnouncement(String announcement) {
		this.announcement = announcement;
	}

	public String getNotice() {
		return notice;
	}

	protected void setNotice(String notice) {
		this.notice = notice;
	}

	public String getL1Name() {
		return l1Name;
	}

	protected void setL1Name(String l1Name) {
		this.l1Name = l1Name;
	}

	public String getL2Name() {
		return l2Name;
	}

	protected void setL2Name(String l2Name) {
		this.l2Name = l2Name;
	}

	public String getL3Name() {
		return l3Name;
	}

	protected void setL3Name(String l3Name) {
		this.l3Name = l3Name;
	}

	public String getL4Name() {
		return l4Name;
	}

	protected void setL4Name(String l4Name) {
		this.l4Name = l4Name;
	}

	public String getL5Name() {
		return l5Name;
	}

	protected void setL5Name(String l5Name) {
		this.l5Name = l5Name;
	}

	public int getNeedBuildingLevel() {
		return needBuildingLevel;
	}

	protected void setNeedBuildingLevel(int needBuildingLevel) {
		this.needBuildingLevel = needBuildingLevel;
	}

	public int getNeedCommanderLevel() {
		return needCommanderLevel;
	}

	protected void setNeedCommanderLevel(int needCommanderLevel) {
		this.needCommanderLevel = needCommanderLevel;
	}

	public int getNeedPower() {
		return needPower;
	}

	protected void setNeedPower(int needPower) {
		this.needPower = needPower;
	}

	public String getNeedLang() {
		return needLang;
	}

	protected void setNeedLanguage(String needLang) {
		this.needLang = needLang;
	}

	public long getLeaderOfflineTime() {
		return leaderOfflineTime;
	}

	protected void setLeaderOfflineTime(long leaderOfflineTime) {
		this.leaderOfflineTime = leaderOfflineTime;
	}

	public long getScore() {
		return score;
	}

	protected void setScore(long score) {
		this.score = score;
	}

	public long getLastDonateCheckTime() {
		return lastDonateCheckTime;
	}

	public void setLastDonateCheckTime(long lastDonateCheckTime) {
		this.lastDonateCheckTime = lastDonateCheckTime;
	}

	public int getClearResNum() {
		return clearResNum;
	}

	public void setClearResNum(int clearResNum) {
		this.clearResNum = clearResNum;
	}
	
	public void incrClearResNum(){
		this.clearResNum++;
	}
	
	public boolean isHasChangeTag() {
		return hasChangeTag;
	}

	public void setHasChangeTag(boolean hasChangeTag) {
		this.hasChangeTag = hasChangeTag;
	}
	
	public String getGuildBoundId() {
		return guildBoundId;
	}

	public void setGuildBoundId(String guildBoundId) {
		this.guildBoundId = guildBoundId;
	}
	
	public String getAuthInfo() {
		return authInfo;
	}

	public void setAuthInfo(String authInfo) {
		this.authInfo = authInfo;
	}

	public Map<Integer, List<Integer>> getAuthMap() {
		return authMap;
	}

	public void updateAuthMap(int authId, List<Integer> lvlList) {
		this.authMap.put(authId, lvlList);
		this.notifyUpdate();
	}

	@Override
	public void beforeWrite() {
		// 编队信息
		formationInfo = formation.serializ();
		this.rewardFlag = SerializeHelper.collectionToString(this.rewardFlagSet,SerializeHelper.ATTRIBUTE_SPLIT);
		if (authMap.isEmpty()) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, List<Integer>> entry : authMap.entrySet()) {
			int authId = entry.getKey();
			sb.append(",").append(authId);
			List<Integer> lvlList = entry.getValue();
			for (int i = 0; i < lvlList.size(); i++) {
				sb.append("_");
				sb.append(lvlList.get(i));
			}
		}
		if(sb.length() > 0){
			sb.replace(0, 1, "");
		}
		this.authInfo = sb.toString();

		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		authMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(this.authInfo)) {
			for (String info : this.authInfo.split(",")) {
				List<Integer> lvlList = new ArrayList<>();
				String[] infoArr = info.split("_");
				authMap.put(Integer.valueOf(infoArr[0]), lvlList);
				for (int i = 1; i < infoArr.length; i++) {
					lvlList.add(Integer.valueOf(infoArr[i]));
				}
			}
		}
		formation = GuildFormationObj.load(this, formationInfo);
		SerializeHelper.stringToSet(Integer.class, this.rewardFlag, SerializeHelper.ATTRIBUTE_SPLIT,null,this.rewardFlagSet);
		super.afterRead();
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

	public long getTaskRefreshTime() {
		return taskRefreshTime;
	}

	public void setTaskRefreshTime(long taskRefreshTime) {
		this.taskRefreshTime = taskRefreshTime;
	}

	public int getChatRoomModel() {
		return chatRoomModel;
	}

	public void setChatRoomModel(int chatRoomModel) {
		this.chatRoomModel = chatRoomModel;
	}
	
	public int getXzqTickets() {
		return xzqTickets;
	}

	public void setXzqTickets(int xzqTickets) {
		this.xzqTickets = xzqTickets;
	}

	public String getSpaceMechaInfo() {
		return spaceMechaInfo;
	}

	public void setSpaceMechaInfo(String spaceMechaInfo) {
		this.spaceMechaInfo = spaceMechaInfo;
	}

	public GuildFormationObj getFormation() {
		return formation;
	}

	public void setFormation(GuildFormationObj formation) {
		this.formation = formation;
	}

	public Set<Integer> getRewardFlagSet() {
		return rewardFlagSet;
	}
	
	public String getLeaderPlatform() {
		return leaderPlatform;
	}

	public void setLeaderPlatform(String leaderPlatform) {
		this.leaderPlatform = leaderPlatform;
	}
}
