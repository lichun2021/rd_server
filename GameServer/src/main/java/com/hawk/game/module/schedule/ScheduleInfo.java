package com.hawk.game.module.schedule;

import java.util.HashSet;
import java.util.Set;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;
import com.hawk.game.module.schedule.cfg.ScheduleCfg;
import com.hawk.game.protocol.Schedule.PBScheduleInfo;

public class ScheduleInfo {

	private String uuid;
	
	private int type;
	
	private String guildId;
	
	private String title;
	
	private long startTime;
	
	private int continueTime;
	
	private int posX;
	
	private int posY;
	
	private Set<String> playerIds = new HashSet<>();
	
	private String teamId;
	
	private long createTime;


	public static ScheduleInfo createNewSchedule(int type, String guildId, long startTime, int posX, int posY) {
		ScheduleCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ScheduleCfg.class, type);
		ScheduleInfo schedule = new ScheduleInfo();
		schedule.uuid = HawkUUIDGenerator.genUUID();
		schedule.type = type;
		schedule.guildId = guildId == null ? "" : guildId;
		schedule.teamId = "";
		schedule.startTime = startTime;
		schedule.continueTime = cfg == null ? 0 : cfg.getLifeTime();
		schedule.posX = posX;
		schedule.posY = posY;
		schedule.createTime = HawkTime.getMillisecond();
		return schedule;
	}
	
	public static ScheduleInfo createGuildSchedule(int type, String guildId, String title, long startTime, int continueTime, int posX, int posY) {
		ScheduleInfo schedule = new ScheduleInfo();
		schedule.uuid = HawkUUIDGenerator.genUUID();
		schedule.type = type;
		schedule.guildId = guildId == null ? "" : guildId;
		schedule.teamId = "";
		schedule.title = title;
		schedule.startTime = startTime;
		schedule.continueTime = continueTime;
		schedule.posX = posX;
		schedule.posY = posY;
		schedule.createTime = HawkTime.getMillisecond();
		return schedule;
	}
	
	public static ScheduleInfo createNewSchedule(int type, String guildId, long startTime, int posX, int posY,String teamId) {
		ScheduleCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ScheduleCfg.class, type);
		ScheduleInfo schedule = new ScheduleInfo();
		schedule.uuid = HawkUUIDGenerator.genUUID();
		schedule.type = type;
		schedule.guildId = guildId == null ? "" : guildId;
		schedule.teamId = teamId == null ? "" : teamId;
		schedule.startTime = startTime;
		schedule.continueTime = cfg == null ? 0 : cfg.getLifeTime();
		schedule.posX = posX;
		schedule.posY = posY;
		schedule.createTime = HawkTime.getMillisecond();
		return schedule;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getContinueTime() {
		return continueTime;
	}

	public void setContinueTime(int continueTime) {
		this.continueTime = continueTime;
	}
	
	public long getEndTime() {
		return this.startTime + this.continueTime * 1000L;
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	
	public Set<String> getPlayerIds() {
		return playerIds;
	}

	public void setPlayerIds(Set<String> playerIds) {
		this.playerIds = playerIds;
	}
	
	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}
	
	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public PBScheduleInfo.Builder toBuilder() {
		PBScheduleInfo.Builder builder = PBScheduleInfo.newBuilder();
		builder.setUuid(uuid);
		builder.setType(type);
		if (!HawkOSOperator.isEmptyString(title)) {
			builder.setTitle(title);
		}
		builder.setStartTime(startTime);
		builder.setContinues(continueTime);
		builder.setPosX(posX);
		builder.setPosY(posY);
		return builder;
	}
	
}
