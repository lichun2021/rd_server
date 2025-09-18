package com.hawk.game.module.material.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportGroupCfg;
import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportKVCfg;
import com.hawk.activity.type.impl.materialTransport.entity.NumberType;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.material.MTConst.MTTruckState;
import com.hawk.game.module.material.MTConst.MTTruckType;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildChampionship.PBChampionPlayer;
import com.hawk.game.protocol.MaterialTransport.PBMTTruck;
import com.hawk.game.protocol.MaterialTransport.PBMTTruckBattleRecord;
import com.hawk.game.protocol.MaterialTransport.PBMTTruckGroup;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.AlgorithmUtil;

/**
 * 货车
 */
public class MTTruck {
	private String id;
	private int index;
	private MTTruckState state;
	private MTTruckType type;
	private String guildId = "";
	private String guildName = "";
	// 发车人
	private String playerId = "";
	private String serverId = "";
	private long startTime;
	private long endTime;
	private String trainReward = "";
	// 车长id 个人车== playerId, 联盟车可指定他人
	private MTMember leader;

	private int robCnt; // 被抢次数

	private int origionX;// = 16; // 起点坐标
	private int origionY; // = 17; // 起点坐标
	private int terminalX; // = 18; // 目标x
	private int terminalY; // = 19; // 目标y

	// 车厢
	private List<MTTruckGroup> compartments = new ArrayList<>();

	private List<MTTruckBattleRecord> battleRecord = new ArrayList<>();

	private PBMTTruck pbObj;

	public MTTruck() {
		id = HawkUUIDGenerator.genUUID();
		state = MTTruckState.PRE;
	}

	/** 参与运输*/
	public boolean isMember(String playerId) {
		if (leader.getPlayerId().equals(playerId)) {
			return true;
		}
		for (MTTruckGroup group : compartments) {
			if (group.isMember(playerId)) {
				return true;
			}
		}
		return false;
	}

	/**参与掠夺*/
	public boolean isRober(String playerId) {
		for (MTTruckBattleRecord bt : battleRecord) {
			if (bt.isAttacker(playerId)) {
				return true;
			}
		}
		return false;
	}

	public List<PBChampionPlayer> getBattleDataList() {
		if (type == MTTruckType.SINGLE) {
			return Arrays.asList(leader.getBattleData());
		}

		List<PBChampionPlayer> ids = new ArrayList<>();
		ids.add(leader.getBattleData());
		for (MTTruckGroup group : compartments) {
			for (MTMember mem : group.getMemberList()) {
				ids.add(mem.getBattleData());
			}
		}
		return ids;
	}

	public MTTruckGroup getCompartment(int cmIndex) {
		for (MTTruckGroup cm : compartments) {
			if (cm.getIndex() == cmIndex) {
				return cm;
			}
		}
		return compartments.get(0);
	}

	public List<ItemInfo> rob() {
		robCnt++;// 抢劫+1
		MaterialTransportKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MaterialTransportKVCfg.class);
		int maxRob = type == MTTruckType.SINGLE ? kvcfg.getTruckRobbedNumber() : kvcfg.getTrainRobbedNumber();
		List<ItemInfo> result = new ArrayList<>();
		for (MTTruckGroup group : compartments) {
			result.addAll(group.rob(maxRob >= robCnt));
		}
		return result;
	}

	public PBMTTruck toPBObj() {
		if (pbObj != null) {
			return pbObj;
		}

		PBMTTruck.Builder builder = PBMTTruck.newBuilder();
		builder.setId(id).setIndex(index).setState(state.getNumber()).setType(type.getNumber()).setGuildId(guildId).setPlayerId(playerId).setServerId(serverId)
				.setStartTime(startTime).setEndTime(endTime).setLeader(leader.toPBObj()).setRobCnt(robCnt).setOrigionX(origionX).setOrigionY(origionY).setTerminalX(terminalX)
				.setTerminalY(terminalY).setTrainReward(trainReward).setGuildName(guildName);

		for (MTTruckGroup group : compartments) {
			builder.addCompartments(group.toPBObj());
		}
		for (MTTruckBattleRecord rec : battleRecord) {
			builder.addBattleRecord(rec.toPBObj());
		}

		this.pbObj = builder.build();
		return this.pbObj;
	}

	public void mergeFrom(PBMTTruck obj) {
		this.id = obj.getId();
		this.index = obj.getIndex();
		this.state = MTTruckState.valueOf(obj.getState());
		this.type = MTTruckType.valueOf(obj.getType());
		this.guildId = obj.getGuildId();
		this.guildName = obj.getGuildName();
		this.playerId = obj.getPlayerId();
		this.serverId = obj.getServerId();
		this.startTime = obj.getStartTime();
		this.endTime = obj.getEndTime();
		this.robCnt = obj.getRobCnt();
		this.origionX = obj.getOrigionX();
		this.origionY = obj.getOrigionY();
		this.terminalX = obj.getTerminalX();
		this.terminalY = obj.getTerminalY();
		MTMember leader = new MTMember();
		leader.mergeFrom(obj.getLeader());
		this.leader = leader;
		this.trainReward = obj.getTrainReward();

		// 车厢
		List<MTTruckGroup> compartments = new ArrayList<>();
		for (PBMTTruckGroup com : obj.getCompartmentsList()) {
			MTTruckGroup group = new MTTruckGroup();
			group.mergeFrom(com);
			compartments.add(group);
		}
		this.compartments = compartments;

		List<MTTruckBattleRecord> battleRecord = new ArrayList<>();
		for (PBMTTruckBattleRecord com : obj.getBattleRecordList()) {
			MTTruckBattleRecord group = new MTTruckBattleRecord();
			group.mergeFrom(com);
			battleRecord.add(group);
		}
		this.battleRecord = battleRecord;

		this.pbObj = obj;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MTTruckType getType() {
		return type;
	}

	public void setType(MTTruckType type) {
		this.type = type;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getRobCnt() {
		return robCnt;
	}

	public void setRobCnt(int robCnt) {
		this.robCnt = robCnt;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<MTTruckGroup> getCompartments() {
		return compartments;
	}

	public void setCompartments(List<MTTruckGroup> compartments) {
		this.compartments = compartments;
	}

	public MTTruckState getState() {
		return state;
	}

	public void setState(MTTruckState state) {
		this.state = state;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public List<MTTruckBattleRecord> getBattleRecord() {
		return battleRecord;
	}

	public void setBattleRecord(List<MTTruckBattleRecord> battleRecord) {
		this.battleRecord = battleRecord;
	}

	public MTMember getLeader() {
		return leader;
	}

	public void setLeader(MTMember leader) {
		this.leader = leader;
	}

	public int getOrigionX() {
		return origionX;
	}

	public void setOrigionX(int origionX) {
		this.origionX = origionX;
	}

	public int getOrigionY() {
		return origionY;
	}

	public void setOrigionY(int origionY) {
		this.origionY = origionY;
	}

	public int getTerminalX() {
		return terminalX;
	}

	public void setTerminalX(int terminalX) {
		this.terminalX = terminalX;
	}

	public int getTerminalY() {
		return terminalY;
	}

	public void setTerminalY(int terminalY) {
		this.terminalY = terminalY;
	}

	public boolean isInview(final int x, final int y) {
		double dis = AlgorithmUtil.getDisPointToLine(x, y, getOrigionX(), getOrigionY(), getTerminalX(), getTerminalY());
		if (dis > 12) {
			return false;
		}
		return true;
	}

	public String getMarchId() {
		return getId();
	}

	public WorldMarchPB.Builder toBuilder(WorldMarchRelation relation) {
		WorldMarchPB.Builder builder = WorldMarchPB.newBuilder();
		builder.setPlayerId(leader.getPlayerId());

		builder.setPlayerName(leader.getName());
		builder.setEndTime(getEndTime());

		if (type == MTTruckType.SINGLE) {
			builder.setModelLvl(getQuality()); // 品质
		}

		builder.setMarchId(getMarchId());
		builder.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH);
		builder.setMarchType(getMarchType());

		builder.setOrigionX(getOrigionX());
		builder.setOrigionY(getOrigionY());
		builder.setRelation(relation);

		builder.setStartTime(getStartTime());
		builder.setTargetId(getMarchId());

		builder.setTerminalX(getTerminalX());
		builder.setTerminalY(getTerminalY());

		return builder;
	}

	private WorldMarchType getMarchType() {
		// MT_SINGLE = 297; //押镖玩法个人
		// MT_GUILD = 298; // 押镖玩法联盟
		// MT_GUILDBIG = 299; // 押镖玩法豪华联盟
		switch (type) {
		case SINGLE:
			return WorldMarchType.MT_SINGLE;

		case GUILD:
			return WorldMarchType.MT_GUILD;

		case GUILDBIG:
			return WorldMarchType.MT_GUILDBIG;

		default:
			break;
		}

		return null;
	}

	public int getQuality() {
		return HawkConfigManager.getInstance().getConfigByKey(MaterialTransportGroupCfg.class, getCompartment(1).getGroupId()).getQuality();
	}

	public PBMTTruck getPbObj() {
		return pbObj;
	}

	public void setPbObj(PBMTTruck pbObj) {
		this.pbObj = pbObj;
	}

	public String getTrainReward() {
		return trainReward;
	}

	public void setTrainReward(String trainReward) {
		this.trainReward = trainReward;
	}

	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}

	public WorldMarchRelation getRelation(Player player) {
		// // 自己的行军
		// if (Objects.equals(leader.getPlayerId(), player.getId())) {
		// return WorldMarchRelation.SELF;
		// }

		// // 同盟玩家行军
		// if (GuildService.getInstance().isInTheSameGuild(leader.getPlayerId(), player.getId())) {
		// return WorldMarchRelation.GUILD_FRIEND;
		// }
		return WorldMarchRelation.NONE;
	}

}
