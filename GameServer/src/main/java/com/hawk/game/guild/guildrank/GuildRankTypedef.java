package com.hawk.game.guild.guildrank;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.game.config.GuildRankCfg;
import com.hawk.game.guild.guildrank.impl.GuildDailyRank;
import com.hawk.game.guild.guildrank.impl.GuildJoinGuildRank;
import com.hawk.game.guild.guildrank.impl.GuildPersonal3DaysOLRank;
import com.hawk.game.guild.guildrank.impl.GuildPersonalDailyOLRank;
import com.hawk.game.guild.guildrank.impl.GuildPersonalDailyRank;
import com.hawk.game.guild.guildrank.impl.GuildPersonalTotalRank;
import com.hawk.game.guild.guildrank.impl.GuildTotalContriRank;
import com.hawk.game.guild.guildrank.impl.GuildTotalRank;
import com.hawk.game.guild.guildrank.impl.GuildWeekRank;
import com.hawk.game.protocol.GuildManager.GuildRankType;

/**
 * 联盟排行榜 定义
 * 
 * @Desc 联盟排行榜 数据存 global redis 联盟数据存在 联盟各自排行榜 数据 结构采用 sorted_set 个人榜单 今日数据
 *       、三日榜单 和周榜 存 local redis ，总榜存 全局redis
 * @author RickMei
 * @Date 2018年11月16日 下午7:03:25
 */

public class GuildRankTypedef {

	static final private int SECS_OF_30_DAYS = 2592000;

	static final public String GR_GUID_FMT = "grguild:%s:%s";

	static final public String GR_GUID_DAILY_FMT = "grguild:%s:%s:%s";

	// 今日战争
	/* rank : kill */
	static final public String GR_D_KILL = "grdkill";

	/* rank : killed */
	static final public String GR_D_KILLED = "grdkilled"; //

	/* rank : res rob */
	static final public String GR_D_RES_ROB = "grdresrob";

	/* rank : res lost */
	static final public String GR_D_RES_LOST = "grdreslost";

	// 时间排行
	/* rank : today online time 今日在线无意义 */
	static final public String GR_DAY_ONLINE_TM = "grdayonline";

	/* rank : average online time for past three days */
	static final public String GR_AVG_ONLINE = "grol";

	/* rank : join guild */
	static final public String GR_GUILD_JOIN_TM = "grjointm";

	// 科技捐献
	/* rank : day contribution */
	static final public String GR_GUILD_D_CONTRI = "grdcontri";

	/* rank : week contribution */
	static final public String GR_GUILD_W_CONTRI = "grwcontri";

	/* rank : contribution */
	static final public String GR_GUILD_CONTRI = "grcontri";

	// 悬赏反击
	/* rank : bounty */
	static final public String GR_GUILD_BOUNTY = "grbounty";

	/* rank : award bounty */
	static final public String GR_GUILD_AWD_BOUNTY = "grawdbounty";

	/* rank : counter-atk */
	static final public String GR_GUILD_COUNTER_ATK = "grcounteratk";

	// 战争总榜
	/* rank : day kill */
	static final public String GR_KILL = "grkill";

	/* rank : day killed */
	static final String GR_KILLED = "grkilled";

	/* rank : day res rob */
	static final public String GR_RES_ROB = "grrob";

	/* rank : day res lost */
	static final public String GR_RES_LOST = "grlost";

	// 联盟新星
	/* rank : help */
	static final public String GR_HELP = "grhelp";

	/* rank : mass */
	static final public String GR_MASS = "grmass";

	/* rank : treasure */
	static final public String GR_TREASURE = "grtreasure";

	/* rank : gather */
	static final public String GR_GATHER = "grgather";

	/* rank : kill monster */
	static final public String GR_KILLMONSTER = "grkillmonster";

	// 统计方式
	public enum GRankCountType {
		ALWAYS, // 永久
		DAILY, // 日
		PAST3DAYS, // 过去三天
		WEEKLY; // 周
	}

	// 统计方式
	public enum GRankDataSVType {
		PERSONAL, // 数据跟玩家
		GUILD, // 数据跟联盟
	}

	// rank status
	public enum GRankStatus {
		NOTOPEN, // 尚未开放
		OPEN, // 正在开放
		CLOSE, // 已经关闭
	}

	public enum GRankType {
		/**
		 * 联盟今日杀敌
		 */
		DAY_KILL(GuildRankType.GR_DAY_KILL, GR_D_KILL, false, GRankCountType.DAILY, GRankDataSVType.PERSONAL, GuildPersonalDailyRank.class),

		/**
		 * 联盟今日战损
		 */
		DAY_KILLED(GuildRankType.GR_DAY_KILLED, GR_D_KILLED, false, GRankCountType.DAILY, GRankDataSVType.PERSONAL, GuildPersonalDailyRank.class),

		/**
		 * 联盟今日资源掠夺
		 */
		DAY_RES_ROB(GuildRankType.GR_DAY_RES_ROB, GR_D_RES_ROB, false, GRankCountType.DAILY, GRankDataSVType.PERSONAL, GuildPersonalDailyRank.class),

		/**
		 * 联盟今日资源损失
		 */
		DAY_RES_LOST(GuildRankType.GR_DAY_RES_LOST, GR_D_RES_LOST, false, GRankCountType.DAILY, GRankDataSVType.PERSONAL, GuildPersonalDailyRank.class),

		/**
		 * 联盟今日在线时长
		 */
		DAY_ONLINE_TM(GuildRankType.GR_DAY_ONLINE_TM, GR_DAY_ONLINE_TM, false, GRankCountType.DAILY, GRankDataSVType.PERSONAL, GuildPersonalDailyOLRank.class),

		/**
		 * 入盟时间
		 */
		GUILD_JOIN_TM(GuildRankType.GR_GUILD_JOIN_TM, GR_GUILD_JOIN_TM, true, GRankCountType.ALWAYS, GRankDataSVType.GUILD, GuildJoinGuildRank.class),

		/**
		 * 联盟三日在线
		 */
		AVG_ONLINE(GuildRankType.GR_AVG_ONLINE, GR_AVG_ONLINE, false, GRankCountType.PAST3DAYS, GRankDataSVType.PERSONAL, GuildPersonal3DaysOLRank.class),

		/**
		 * 今日联盟捐献
		 */
		GUILD_D_CONTRI(GuildRankType.GR_GUILD_D_CONTRI, GR_GUILD_D_CONTRI, false, GRankCountType.DAILY, GRankDataSVType.GUILD, GuildDailyRank.class),

		/**
		 * 本周联盟捐献
		 */
		GUILD_W_CONTRI(GuildRankType.GR_GUILD_W_CONTRI, GR_GUILD_W_CONTRI, false, GRankCountType.WEEKLY, GRankDataSVType.GUILD, GuildWeekRank.class),

		/**
		 * 联盟捐献总榜
		 */
		GUILD_CONTRI(GuildRankType.GR_GUILD_CONTRI, GR_GUILD_CONTRI, false, GRankCountType.ALWAYS, GRankDataSVType.GUILD, GuildTotalContriRank.class),

		/**
		 * 联盟悬赏
		 */
		GUILD_BOUNTY(GuildRankType.GR_GUILD_BOUNTY, GR_GUILD_BOUNTY, false, GRankCountType.ALWAYS, GRankDataSVType.GUILD, GuildTotalRank.class),

		/**
		 * 联盟领赏
		 */
		GUILD_AWD_BOUNTY(GuildRankType.GR_GUILD_AWD_BOUNTY, GR_GUILD_AWD_BOUNTY, false, GRankCountType.ALWAYS, GRankDataSVType.GUILD, GuildTotalRank.class),

		/**
		 * 联盟联盟反击
		 */
		GUILD_COUNTER_ATK(GuildRankType.GR_GUILD_COUNTER_ATK, GR_GUILD_COUNTER_ATK, false, GRankCountType.ALWAYS, GRankDataSVType.GUILD, GuildTotalRank.class),

		/**
		 * 联盟总击杀榜
		 */
		KILL(GuildRankType.GR_KILL, GR_KILL, false, GRankCountType.ALWAYS, GRankDataSVType.PERSONAL, GuildPersonalTotalRank.class),

		/**
		 * 联盟战损总榜
		 */
		KILLED(GuildRankType.GR_KILLED, GR_KILLED, false, GRankCountType.ALWAYS, GRankDataSVType.PERSONAL, GuildPersonalTotalRank.class),

		/**
		 * 联盟掠夺资源总榜
		 */
		RES_ROB(GuildRankType.GR_RES_ROB, GR_RES_ROB, false, GRankCountType.ALWAYS, GRankDataSVType.PERSONAL, GuildPersonalTotalRank.class),

		/**
		 * 联盟资源损失总榜
		 */
		RES_LOST(GuildRankType.GR_RES_LOST, GR_RES_LOST, false, GRankCountType.ALWAYS, GRankDataSVType.PERSONAL, GuildPersonalTotalRank.class),

		/**
		 * 联盟帮助榜
		 */
		HELP(GuildRankType.GR_HELP, GR_HELP, false, GRankCountType.PAST3DAYS, GRankDataSVType.PERSONAL, GuildPersonalDailyRank.class),

		/**
		 * 联盟集结榜
		 */
		MASS(GuildRankType.GR_MASS, GR_MASS, false, GRankCountType.PAST3DAYS, GRankDataSVType.PERSONAL, GuildPersonalDailyRank.class),

		/**
		 * 联盟开宝箱榜
		 */
		TREASURE(GuildRankType.GR_TREASURE, GR_TREASURE, false, GRankCountType.PAST3DAYS, GRankDataSVType.PERSONAL, GuildPersonalDailyRank.class),

		/**
		 * 联盟采集资源榜单
		 */
		GATHER(GuildRankType.GR_GATHER, GR_GATHER, false, GRankCountType.PAST3DAYS, GRankDataSVType.PERSONAL, GuildPersonalDailyRank.class),

		/**
		 * 联盟击杀野怪榜单
		 */
		KILLMONSTER(GuildRankType.GR_KILLMONSTER, GR_KILLMONSTER, false, GRankCountType.PAST3DAYS, GRankDataSVType.PERSONAL, GuildPersonalDailyRank.class);

		private GuildRankType typeId;
		private String typeName;
		private boolean isOrderSmallToLarge;
		private GRankCountType countType;
		private GRankDataSVType boardType;
		private int topN;
		private GuildBaseRank rank;

		public GuildRankType getTypeId() {
			return typeId;
		}

		public String getTypeName() {
			return typeName;
		}

		public boolean isOrderSmallToLarge() {
			return isOrderSmallToLarge;
		}

		public int getTopN() {
			return topN;
		}

		GRankType(GuildRankType typeId, String typeName, boolean iss2l, GRankCountType countType,
				GRankDataSVType boardType, Class<? extends GuildBaseRank> clazz) {
			this.typeId = typeId;
			this.typeName = typeName;
			this.isOrderSmallToLarge = iss2l;
			this.setCountType(countType);
			this.setBoardType(boardType);
			// 获取构造函数 构造之
			try {
				this.rank = clazz.getDeclaredConstructor(this.getClass()).newInstance(this);
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			// 获取配置表
			GuildRankCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildRankCfg.class,
					this.typeId.getNumber());
			if (null != cfg) {
				this.topN = cfg.getMailTopNumber();
			} else {
				this.topN = 0;
			}
		}

		public static String findRankName(GuildRankType typeId) {
			for (GRankType rt : GRankType.values()) {
				if (rt.typeId == typeId) {
					return rt.typeName;
				}
			}
			return "";
		}

		public static GRankType findRankType(GuildRankType typeId) {
			for (GRankType rt : GRankType.values()) {
				if (rt.typeId == typeId) {
					return rt;
				}
			}
			return null;
		}

		public static GuildBaseRank findRank(GuildRankType typeId) {
			for (GRankType rt : GRankType.values()) {
				if (rt.typeId == typeId) {
					return rt.getRank();
				}
			}
			return null;
		}

		public GRankCountType getCountType() {
			return countType;
		}

		public void setCountType(GRankCountType countType) {
			this.countType = countType;
		}

		public GRankDataSVType getBoardType() {
			return boardType;
		}

		/**
		 * 设置数据存储的方式 联盟|个人
		 * 
		 * @Desc
		 * @param boardType
		 */
		public void setBoardType(GRankDataSVType boardType) {
			this.boardType = boardType;
		}

		/**
		 * 获取存储数据的过期时间
		 * 
		 * @Desc
		 * @return
		 */
		public int GetOverdueTime() {
			// 获取配置表
			GuildRankCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildRankCfg.class,
					this.typeId.getNumber());
			if (null != cfg) {
				return cfg.getOverdueTime();
			}
			// default return 30 days secs
			return SECS_OF_30_DAYS;
		}

		public GuildBaseRank getRank() {
			return rank;
		}
	}
}
