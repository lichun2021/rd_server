package com.hawk.game.guild.guildrank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.game.config.ControlProperty;
import com.hawk.game.config.GuildRankCfg;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.guild.guildrank.data.GuildMailCount;
import com.hawk.game.guild.guildrank.data.GuildRankSvInfo;
import com.hawk.game.guild.guildrank.impl.GuildTotalContriRank;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.GuildManager.DonateRankType;
import com.hawk.game.protocol.GuildManager.GuildDonateRankInfo;
import com.hawk.game.protocol.GuildManager.GuildGetRankResp;
import com.hawk.game.protocol.GuildManager.GuildRankType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;


/**
 * 联盟排行榜管理器
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月14日 下午6:36:11
 */
public class GuildRankMgr {

	static Logger logger = LoggerFactory.getLogger("Server");

	private static final GuildRankMgr INSTANCE = new GuildRankMgr();

	private GuildRankSvInfo lastUpdateInfo;

	/**
	 * tick 开关 在
	 */
	private volatile boolean tickable = true;
	private Map<String, HawkTuple2<Long, GuildGetRankResp.Builder>> respCacheMap = new ConcurrentHashMap<String, HawkTuple2<Long, GuildGetRankResp.Builder>>();

	public static GuildRankMgr getInstance() {
		return INSTANCE;
	}

	/**
	 * 待发邮件的统计数据
	 */
	private Map<String, GuildMailCount> guildRankCountMap = new HashMap<>();

	/**
	 * @Desc 排行榜统计接口
	 * @param guildId
	 * @param newResp
	 */
	public void putGuildRankCount(String guildId, GuildGetRankResp.Builder newResp) {
		GuildMailCount gcount = guildRankCountMap.get(guildId);
		if (null == gcount) {
			gcount = new GuildMailCount(guildId);
			guildRankCountMap.put(guildId, gcount);
		}
		gcount.putRankCount(newResp.getRankType(), newResp.build());
		logger.info("guildrank_log countinfo guildId={}", guildId);
	}

	/**
	 * @Desc 全服发邮件
	 */
	final public void sendPassDayMails() {
		int delayTime = 0;
		for (Map.Entry<String, GuildMailCount> entry : guildRankCountMap.entrySet()) {
			delayTime += 1000;
			addRankTask(new HawkDelayTask(delayTime, delayTime, 1) {
				@Override
				public Object run() {
					logger.info("guildrank_log sendmail guildId={}", entry.getKey());
					entry.getValue().sendMemberRankMail();
					return null;
				}
			});
		}
	}

	/**
	 * 跨天处理
	 * 
	 * @Desc
	 */
	final public void onPassDay() {
		for (GRankType rankType : GRankType.values()) {
			GuildBaseRank rank = rankType.getRank();
			if (null != rank) {
				int delayTime = (rank.getRankType().ordinal() + 1) * 1000;
				addRankTask(new HawkDelayTask(delayTime, delayTime, 1) {
					@Override
					public Object run() {
						rank.onPassDay();
						logger.info("guildrank_log on pass day sv top 3 rank: {}", rank.getRankType().name());
						return null;
					}
				});
			}
		}
	}

	/**
	 * @Desc 往线程插入一个任务
	 * @param task
	 */
	public void addRankTask(HawkDelayTask task) {
		HawkThreadPool taskPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (null != taskPool) {
			task.setTypeName("GuildRankPassDay");
			taskPool.addTask(task, 0, false);
		}
	}

	/**
	 * @Desc tick 函数 每分钟tick 一次
	 */
	public void onTick() {

		if (ControlProperty.getInstance().getGuildrankSwitch() != 1) {
			return;
		}

		if (!tickable) {
			return;
		}
		long curTime = HawkTime.getMillisecond();
		if (curTime > HawkTime.MINUTE_MILLI_SECONDS + lastUpdateInfo.getLastUpdateTime()) {
			if (!HawkTime.isSameDay(lastUpdateInfo.getLastUpdateTime(), curTime)) {
				tickable = false;
				onPassDay();

				addRankTask(new HawkDelayTask(1, HawkTime.MINUTE_MILLI_SECONDS, 1) {
					@Override
					public Object run() {
						sendPassDayMails();
						logger.info("guildrank_log on pass day send mails ok!");
						return null;
					}
				});

				addRankTask(new HawkDelayTask(1, HawkTime.MINUTE_MILLI_SECONDS * 2, 1) {
					@Override
					public Object run() {
						checkOpen();
						checkClose();
						guildRankCountMap.clear();
						logger.info("guildrank_log on pass day clear cached top 3 rank");
						tickable = true;
						return null;
					}
				});
			}
			lastUpdateInfo.setLastUpdateTime(curTime);
			LocalRedis.getInstance().updateGuildRankSvInfo(lastUpdateInfo);
		}
	}

	/**
	 * 获取榜单返回给客户端的结构体
	 * 
	 * @Desc
	 * @param playerId
	 * @param guildId
	 * @param rType
	 * @return
	 */
	public GuildGetRankResp.Builder getRankResp(String playerId, String guildId, GuildRankType rType) {
		GRankType rankType = GuildRankTypedef.GRankType.findRankType(rType);
		if (null != rankType) {
			Long curMs = HawkTime.getMillisecond();
			GuildBaseRank rank = rankType.getRank();
			if (null != rank && rank.isOpen()) {
				String key = guildId + rType.name();
				HawkTuple2<Long, GuildGetRankResp.Builder> retPair = respCacheMap.get(key);
				if (null != retPair) {
					// 1分钟内有请求过的直接返回不另外生成
					if (curMs < HawkTime.MINUTE_MILLI_SECONDS + retPair.first) {
						return retPair.second;
					}
				}
				GuildGetRankResp.Builder retResp = rank.prepareRankResp(guildId);
				if (null != retResp) {
					respCacheMap.put(key, new HawkTuple2<Long, GuildGetRankResp.Builder>(HawkTime.getMillisecond(), retResp));
					logger.info("guildrank_log player:{} guild:{} getresp:{}" ,playerId, guildId,JsonFormat.printToString(retResp.build()));
					return retResp;
				}
			}
		}

		GuildGetRankResp.Builder retResp = GuildGetRankResp.newBuilder();
		retResp.setRankType(rType);
		return retResp;
	}

	/**
	 * @Desc 检测加载配置关闭排行榜
	 */
	public void checkClose() {
		long serverOpenDays = GameUtil.getServerOpenDays();
		ConfigIterator<GuildRankCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(GuildRankCfg.class);
		GuildRankCfg cfg = cfgIter.next();
		while (null != cfg) {
			GuildBaseRank rank = GRankType.findRank(GuildRankType.valueOf(cfg.getId()));
			if (null != rank) {
				// 0 或者1 常开放
				if (0 != cfg.getCloseDay() && cfg.getCloseDay() < serverOpenDays) {
					rank.close();
				}
			}
			cfg = cfgIter.next();
		}
	}

	/**
	 * @Desc 加载配置表开启排行榜
	 */
	public void checkOpen() {
		long serverOpenDays = GameUtil.getServerOpenDays();
		ConfigIterator<GuildRankCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(GuildRankCfg.class);
		GuildRankCfg cfg = cfgIter.next();
		while (null != cfg) {
			GuildBaseRank rank = GRankType.findRank(GuildRankType.valueOf(cfg.getId()));
			if (null != rank) {
				// 在开放区间内
				if ((0 == cfg.getOpenDay() || serverOpenDays >= cfg.getOpenDay())
						&& (cfg.getCloseDay() == 0 || serverOpenDays <= cfg.getCloseDay())) {
					if (!rank.isOpen()) {
						rank.open();
					}
				}
			}
			cfg = cfgIter.next();
		}
	}

	public boolean init() {
		// 从redis 加载 上次存储的数据
		lastUpdateInfo = LocalRedis.getInstance().getGuildRankSvInfo();
		if (null == lastUpdateInfo) {
			lastUpdateInfo = new GuildRankSvInfo();
		}
		checkOpen();
		checkClose();
		// 如果没有加载历史数据 这里加载历史数据
		if (!lastUpdateInfo.isLoadHistory()) {
			List<String> guildIds = GuildService.getInstance().getGuildIds();

			lastUpdateInfo.setLoadHistory(true);

			LocalRedis.getInstance().updateGuildRankSvInfo(lastUpdateInfo);

			for (String guildId : guildIds) {
				// 加载 玩家入盟时间
				Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
				for (String memberId : memberIds) {
					long joinTm = GuildService.getInstance().getJoinGuildTime(memberId);
					if (joinTm > 0) {
						GRankType.GUILD_JOIN_TM.getRank().addRankVal(guildId, memberId, (int) (joinTm / 1000));
					}
				}
				// 加载捐献总榜
				List<GuildDonateRankInfo> donateInfos = GuildService.getInstance()
						.buildContributionRankInfo(DonateRankType.TOTAL_RANK, guildId);
				for (GuildDonateRankInfo info : donateInfos) {
					if (info.getDonate() > 0)
						((GuildTotalContriRank) (GRankType.GUILD_CONTRI.getRank())).setRankVal(guildId,
								info.getPlayerId(), info.getDonate());
				}
			}
		}
		return true;
	}

	/**
	 * 退出联盟
	 * 
	 * @Desc
	 * @param playerId
	 * @param guildId
	 */
	public void onPlayerExitGuild(String playerId, String guildId) {
		if (!guildId.isEmpty() && !playerId.isEmpty()) {
			String[] historyIds = RedisProxy.getInstance().getPlayerHistoryGuildIds(playerId);
			List<String> retArrayList = new ArrayList<>();
			for (int i = 0; i < historyIds.length; i++) {
				if (!historyIds[i].equals(guildId)) {
					retArrayList.add(historyIds[i]);
				}
			}
			// 本次在最后加上
			retArrayList.add(guildId);
			// 最多只有三个联盟的记录
			while (retArrayList.size() > 3) {
				String removeGuildId = retArrayList.get(0);
				retArrayList.remove(0);
				// 删除联盟排行榜关于这个榜单的记录
				for (GRankType rankType : GRankType.values()) {
					if (rankType.getRank().getIsGuildRank()) {
						rankType.getRank().delRankKey(removeGuildId, playerId);
					}
				}
			}

			RedisProxy.getInstance().updatePlayerHistoryGuildIds(playerId, retArrayList.toArray(new String[0]));
		}
	}

	/**
	 * 击杀敌军
	 * 
	 * @Desc
	 * @param playerId
	 * @param num
	 */
	public void onKillSoldier(String playerId, long num) {
		if (num > 0) {
			GRankType.DAY_KILL.getRank().addRankVal(null, playerId, num);
			GRankType.KILL.getRank().addRankVal(null, playerId, num);
		} else {
			logger.info("guildrank_log onKillSoldier add val less 0");
		}

	}

	/**
	 * 掠夺 个人数据
	 * 
	 * @Desc
	 * @param playerId
	 * @param num
	 */
	public void onRobRes(String playerId, long num) {
		if (num > 0) {
			GRankType.DAY_RES_ROB.getRank().addRankVal(null, playerId, num);
			GRankType.RES_ROB.getRank().addRankVal(null, playerId, num);
		} else {
			logger.info("guildrank_log onRobRes add val less 0");
		}

	}

	/**
	 * 战损
	 * 
	 * @Desc
	 * @param playerId
	 * @param num
	 */
	public void onKilledSoldier(String playerId, long num) {
		if (num > 0) {
			GRankType.DAY_KILLED.getRank().addRankVal(null, playerId, num);
			GRankType.KILLED.getRank().addRankVal(null, playerId, num);
		} else {
			logger.info("guildrank_log onKilledSoldier add val less 0");
		}
	}

	/**
	 * 被掠夺
	 * 
	 * @Desc
	 * @param playerId
	 * @param num
	 */
	public void onRobbedRes(String playerId, long num) {
		if (num > 0) {
			GRankType.DAY_RES_LOST.getRank().addRankVal(null, playerId, num);
			GRankType.RES_LOST.getRank().addRankVal(null, playerId, num);
		} else {
			logger.info("guildrank_log onRobbedRes add val less 0");
		}
	}

	/**
	 * 在线时长 统计时机，下线
	 * 
	 * @Desc
	 * @param playerId
	 * @param guildId
	 * @param num
	 */
	public void onPlayerOnlineTMChange(String playerId, String guildId, long num) {
		if (num > 0) {
			GRankType.DAY_ONLINE_TM.getRank().addRankVal(guildId, playerId, num);
			GRankType.AVG_ONLINE.getRank().addRankVal(guildId, playerId, num);
			logger.info("guildrank_log on player {} add {}", playerId, num);
		} else {
			logger.info("guildrank_log onPlayerOnlineTMChange add val less 0");
		}
	}

	/**
	 * 入盟时间
	 * 
	 * @Desc
	 * @param playerId
	 * @param guildId
	 */
	public void onPlayerJoinGuild(String playerId, String guildId) {
		int num = HawkTime.getSeconds();
		GRankType.GUILD_JOIN_TM.getRank().addRankVal(guildId, playerId, num);
	}

	/**
	 * 盟内捐献
	 * 
	 * @Desc
	 * @param playerId
	 * @param guildId
	 * @param num
	 */
	public void onPlayerContri(String playerId, String guildId, long num) {
		if (num > 0 && !guildId.isEmpty()) {
			GRankType.GUILD_CONTRI.getRank().addRankVal(guildId, playerId, num);
			GRankType.GUILD_W_CONTRI.getRank().addRankVal(guildId, playerId, num);
			GRankType.GUILD_D_CONTRI.getRank().addRankVal(guildId, playerId, num);
			logger.info("guildrank_log onPlayerContri playerId:{} val:{}", playerId, num);
		} else {
			logger.info("guildrank_log onPlayerContri add val less 0");
		}
	}

	/**
	 * 被悬赏
	 * 
	 * @Desc 策划明确了只要记录悬赏金币
	 * @param playerId
	 * @param num
	 */
	public void onPlayerBounty(String playerId, String itemInfo) {
		try {
			ItemInfo parseItem = new ItemInfo(itemInfo);
			if (parseItem.getCount() > 0) {
				if (parseItem.getItemId() == PlayerAttr.GOLD_VALUE) {
					String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
					GRankType.GUILD_BOUNTY.getRank().addRankVal(guildId, playerId, parseItem.getCount());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 领赏
	 * 
	 * @Desc 策划明确了只要记录悬赏金币
	 * @param playerId
	 * @param num
	 */
	public void onPlayerAwdBounty(String playerId, List<ItemInfo> itemsInfo) {
		try {
			// 反击达成奖励
			int moneyTotal = 0;
			for (ItemInfo itemInfo : itemsInfo) {
				if (itemInfo.getItemId() == PlayerAttr.GOLD_VALUE) {
					moneyTotal += itemInfo.getCount();
				}
			}
			String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
			if (moneyTotal > 0) {
				GRankType.GUILD_AWD_BOUNTY.getRank().addRankVal(guildId, playerId, moneyTotal);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 反击击杀
	 * 
	 * @Desc
	 * @param playerId
	 * @param num
	 */
	public void onPlayerCounter(String playerId, long num) {
		if (num > 0) {
			String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
			GRankType.GUILD_COUNTER_ATK.getRank().addRankVal(guildId, playerId, num);
		} else {
			logger.info("guildrank_log onPlayerCounter add val less 0");
		}
	}

	/**
	 * 帮助
	 * 
	 * @Desc
	 * @param playerId
	 * @param guildId
	 * @param num
	 */
	public void onPlayerHelp(String playerId, String guildId, long num) {
		if (num > 0) {
			GRankType.HELP.getRank().addRankVal(guildId, playerId, num);
		} else {
			logger.info("guildrank_log onPlayerHelp add val less 0");
		}
	}

	/**
	 * 联盟集结
	 * 
	 * @Desc
	 * @param playerId
	 * @param guildId
	 * @param num
	 */
	public void onPlayerMass(String playerId, String guildId, long num) {
		if (num > 0) {
			GRankType.MASS.getRank().addRankVal(guildId, playerId, num);
		} else {
			logger.info("guildrank_log onPlayerMass add val less 0");
		}
	}

	/**
	 * 联盟宝箱
	 * 
	 * @Desc
	 * @param playerId
	 * @param guildId
	 * @param num
	 */
	public void onPlayerTreasure(String playerId, String guildId, long num) {
		if (num > 0) {
			GRankType.TREASURE.getRank().addRankVal(guildId, playerId, num);
		} else {
			logger.info("guildrank_log onPlayerTreasure add val less 0");
		}
	}

	/**
	 * 资源采集
	 * 
	 * @Desc
	 * @param playerId
	 * @param guildId
	 * @param num
	 */
	public void onPlayerGather(String playerId, String guildId, long num) {
		if (num > 0) {
			GRankType.GATHER.getRank().addRankVal(guildId, playerId, num);
		} else {
			logger.info("guildrank_log onPlayerGather add val less 0");
		}
	}

	/**
	 * 击杀野怪
	 * 
	 * @Desc
	 * @param guildId
	 * @param num
	 */
	public void onPlayerKillMonster(String playerId, String guildId, long num) {
		if (num > 0) {
			GRankType.KILLMONSTER.getRank().addRankVal(guildId, playerId, num);
		} else {
			logger.info("guildrank_log onPlayerKillMonster add val less 0");
		}
	}
}
