package com.hawk.game.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.app.HawkAppCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.ByteString;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ShareProsperityEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.MailExpireCfg;
import com.hawk.game.config.MailsysCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.MailStatus;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.Mail.CommonMail;
import com.hawk.game.protocol.Mail.CureMail;
import com.hawk.game.protocol.Mail.DetectMail;
import com.hawk.game.protocol.Mail.FightMail;
import com.hawk.game.protocol.Mail.GuildInviteMail;
import com.hawk.game.protocol.Mail.HPCheckMailRes;
import com.hawk.game.protocol.Mail.HPNewMailRes;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.Mail.MonsterMail;
import com.hawk.game.protocol.Mail.MoveCityInviteMail;
import com.hawk.game.protocol.Mail.PBCYBORGNuclearHitContent;
import com.hawk.game.protocol.Mail.PBCyborgContributionMail;
import com.hawk.game.protocol.Mail.PBGuildDragonAttackDamageMail;
import com.hawk.game.protocol.Mail.PBGuildHospiceMail;
import com.hawk.game.protocol.Mail.PBGuildRankContent;
import com.hawk.game.protocol.Mail.PBGundamStartUp;
import com.hawk.game.protocol.Mail.PBPlayerDressAskMailContent;
import com.hawk.game.protocol.Mail.PBPlayerGuardDressMailContent;
import com.hawk.game.protocol.Mail.PBTBLYNuclearHitContent;
import com.hawk.game.protocol.Mail.PBYQZZNuclearHitContent;
import com.hawk.game.protocol.Mail.PveFightMail;
import com.hawk.game.protocol.Mail.ResAssistanceMail;
import com.hawk.game.protocol.Mail.SoilderAssistanceMail;
import com.hawk.game.protocol.Mail.YuriRevengeFightMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MailConst.SysMsgType;
import com.hawk.game.protocol.RedisMail.ChatData;
import com.hawk.game.protocol.RedisMail.ChatRoomData;
import com.hawk.game.protocol.RedisMail.ChatType;
import com.hawk.game.protocol.RedisMail.MailEntityContent;
import com.hawk.game.protocol.RedisMail.MemberData;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.MailManager;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.PersonalMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.GsConst.DiamondPresentReason;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.log.LogConst.SnsType;
import com.hawk.log.Source;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 邮件服务类
 * 
 * @author Nannan.Gao
 * @date 2016-11-30 18:23:18
 */
public class MailService {

	private static final int MAX_RESEND = 5;
	private static Logger logger = LoggerFactory.getLogger("Server");

	private static MailService instance = new MailService();
	// 收藏夹邮件类型
	public static final int SAVE_MAIL_TYPE = 996;
	
	public static final int REWARD_MAIL_KEEP = 31;
	public static final String REWARD_MAIL_DOT = "_RED";
//	// 收藏夹邮件后缀
//	public final String SAVE_MAIL_DOT = "_X";
	
	public static MailService getInstance() {
		return instance;
	}

	/** 邮件体 */
	static final String MAIL_CONTENT = "mailentity_content:";
	/** 邮件体 */
	static final String MAIL_ENTITY = "mailentity_entity:";
	/** 邮件体顺序 */
	static final String MAIL_SORT = "mailentity_sort:";
	/** 邮件体未读 */
	static final String MAIL_UNREAD = "mailentity_unread:";

	/**
	 * 邮件类型
	 */
	public int getMailType(MailId mailId) {
		MailsysCfg msyscfg = HawkConfigManager.getInstance().getConfigByKey(MailsysCfg.class, mailId.getNumber());
		return msyscfg == null ? 4 : msyscfg.getNewPageType();
	}
	
	/***
	 * 邮件过期秒
	 */
	public int getMailExpireSecond(int type) {
		int expireDay = 11;
		MailExpireCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MailExpireCfg.class, type);
		if (Objects.nonNull(cfg)) {
			expireDay = cfg.getExpireDay() + 1;
		}
		return GsConst.DAY_SECONDS * expireDay;
	}
	
	/***
	 * 邮件最大数量
	 */
	public int getMailMaxCount(int type) {
		int max = 2000;
//		MailExpireCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MailExpireCfg.class, type);
//		if (Objects.nonNull(cfg)) {
//			max = cfg.getMaxCount();
//		}
		return max;
	}

	/**
	 * 所有已知的邮件类型
	 * 
	 * @return
	 */
	private int[] allMailTypes() {
		return HawkConfigManager.getInstance().getConfigIterator(MailsysCfg.class).stream()
				.mapToInt(MailsysCfg::getNewPageType)
				.distinct()
				.sorted()
				.toArray();
	}

	/**
	 * 发送邮件入口
	 * 
	 * @param parames
	 * @return
	 */
	public boolean sendMail(MailParames parames) {
		// 判断模块关闭
		if (SystemControler.getInstance().isModuleClosed(ControlerModule.MAIL_SEND)) {
			logger.info("system control, mail send has closed, mailParames:{}", parames.toString());
			return false;
		}
		
		if (GlobalData.getInstance().getForbiddenMailIds().contains(parames.getMailId().getNumber())) {
			logger.info("system control, mail send fobidden, mailId: {}", parames.getMailId());
			return false;
		}

		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				doSendMail(parames);
				return null;
			}
		});
		return true;
	}

	/**
	 * 发送邮件入口
	 * 
	 * @param parames
	 * @return
	 */
	private void doSendMail(MailParames parames) {
		String playerId = parames.getPlayerId();
		final String mailUUID = parames.getUuid();
		final long createTime = HawkTime.getMillisecond();

		MailLiteInfo.Builder liteBuilder = MailLiteInfo.newBuilder();
		liteBuilder.setId(mailUUID);
		liteBuilder.setType(this.getMailType(parames.getMailId()));
		liteBuilder.setMailId(parames.getMailId().getNumber());
		liteBuilder.setCtime(createTime);
		liteBuilder.addIcon(parames.getIcon());
		liteBuilder.setPlayerId(playerId);
		liteBuilder.setGlobalMailId(parames.getGlobalMailId());
		liteBuilder.setTitle(parames.getTitle());
		liteBuilder.setMidTitle(parames.getMidTitle());
		liteBuilder.setSubTitle(parames.getSubTitle());
		liteBuilder.addAllTips(parames.getTips());
		liteBuilder.setAdditionalParam(parames.getAdditionalParam());
		liteBuilder.setHasReward(false);
		liteBuilder.setLock(0);
		liteBuilder.setStatus(0);
		liteBuilder.setIsLmjy(parames.getDuntype() == DungeonMailType.LMJY);
		liteBuilder.setIsTBLY(parames.getDuntype() == DungeonMailType.TBLY);
		liteBuilder.setIsSW(parames.getDuntype() == DungeonMailType.SW);
		liteBuilder.setDuntype(parames.getDuntype().intValue());
		liteBuilder.setSendServerId(GsConfig.getInstance().getServerId());

		String reward = parames.getReward();
		if (StringUtils.isNotEmpty(reward)) {
			liteBuilder.setReward(reward);
			if (parames.getAwardStatus() != MailRewardStatus.GET) {
				liteBuilder.setHasReward(true);
			}
		}

		if (HawkOSOperator.isEmptyString(parames.getOppPfIcon())) {
			liteBuilder.setPfIcon(parames.getOppPfIcon());
		}

		boolean sendSucc = this.addMailContent(liteBuilder.build(), parames.getContent());
		if (!sendSucc && parames.incAndGetReSendCount() <= MAX_RESEND) {
			MailManager.getInstance().inqueue(parames);
			return;
		}
		
		// 锦标赛邮件不进行后续操作
		if(playerId.startsWith("GcPlayer")){
			return;
		}
		
		// 增加邮件的行为日志
		BehaviorLogger.log4Player(playerId, Source.MAIL, Action.CREATE_MAIL,
				Params.valueOf("mailId", parames.getMailId()),
				Params.valueOf("mailIdUUID", mailUUID),
				Params.valueOf("mailReward", reward),
				Params.valueOf("mailType", liteBuilder.getType()));

		MailsysCfg msyscfg = HawkConfigManager.getInstance().getConfigByKey(MailsysCfg.class, parames.getMailId().getNumber());
		if (msyscfg != null && !HawkOSOperator.isEmptyString(msyscfg.getPushNews())) {
			PushService.getInstance().pushMsg(playerId, PushMsgType.NEW_MSG_VALUE, msyscfg.getPushNews());
		}

		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		if(parames.isKeepLog() && liteBuilder.getHasReward()){
			if(Objects.isNull(player)){
				player = GlobalData.getInstance().makesurePlayer(playerId);
			}
			if(Objects.nonNull(player)){
				LogUtil.logRewardMailSendFlow(player, liteBuilder.getId(), liteBuilder.getMailId(), LogInfoType.send_reward_mail, liteBuilder.getReward());
			}
		}
		if (player == null || !player.isActiveOnline()) {
			return;
		}

		// 在线即推送给客户端
		HPNewMailRes.Builder builder = HPNewMailRes.newBuilder().addMail(liteBuilder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_NEW_MAIL_S_VALUE, builder));
		return;
	}

	/**
	 * 清除多余邮件
	 * 
	 * @param playerId
	 * @param types
	 */
	public void clear(final String playerId) {
		int[] types = allMailTypes();
		for (int type : types) {
			delByType(playerId, type,true);
		}
		for (int type : types) {
			delByType(playerId, type,false);
		}
	}

	private void delByType(final String playerId, int type,boolean keepRewardMail) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			long now = HawkTime.getMillisecond();
			// 由于在线过程中不再清理,提前一小时清理. 防止出现列表显示混乱
			int mailMaxCount = getMailMaxCount(type);
			long mailExpireMillis = getMailExpireSecond(type) * 1000L - TimeUnit.DAYS.toMillis(1);
			long delTime = now - mailExpireMillis; // 在这之前的邮件都过期了
			if (!keepRewardMail) {// 不保留 过期一个月全删
				delTime = now - TimeUnit.DAYS.toMillis(REWARD_MAIL_KEEP) + TimeUnit.DAYS.toMillis(1);
			}
			String keySort = keySort(playerId, type);
			String oldestMailId = jedis.zrevrange(keySort, mailMaxCount, mailMaxCount).stream().findFirst().orElse(null); // 按创建时间从大到小第mailMaxCount封
			if (Objects.nonNull(oldestMailId)) {
				delTime = Math.max(delTime, jedis.zscore(keySort, oldestMailId).longValue());
			}

			Set<String> toDelMailids = jedis.zrangeByScore(keySort, 0, delTime);
			if (keepRewardMail) {
				toDelMailids = toDelMailids.stream().filter(mid -> !mid.endsWith(REWARD_MAIL_DOT)).collect(Collectors.toSet());
			}
			if (toDelMailids.isEmpty()) {
				return;
			}
			// 所有的邮件Id
			String[] mailIdArr = toDelMailids.toArray(new String[toDelMailids.size()]);
			String keyUnread = keyUnread(playerId, type);
			if (keepRewardMail) { // 保留奖励邮件要按id清除
				// 删未读
				jedis.zrem(keyUnread, toDelMailids.toArray(new String[0]));
				// 删排序
				jedis.zrem(keySort, toDelMailids.toArray(new String[0]));
			} else {
				jedis.zremrangeByScore(keyUnread, 0, delTime);
				jedis.zremrangeByScore(keySort, 0, delTime);
			}
//			// 删entity
//			jedis.del(Arrays.stream(mailIdArr).map(this::keyEntity).toArray(byte[][]::new));
//			// 删content
//			jedis.del(Arrays.stream(mailIdArr).map(this::keyContent).toArray(byte[][]::new));

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void clearMailBaiId(String playerId, MailId... mids){
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) { // 删除活动邮件
			Set<Integer> typeSet = Arrays.stream(mids).map(mid -> getMailType(mid)).collect(Collectors.toSet());
			Set<Integer> midSet = Arrays.stream(mids).map(MailId::getNumber).collect(Collectors.toSet());
			for (int type : typeSet) {
				List<MailLiteInfo.Builder> list = MailService.getInstance().listMail(playerId, "", type, 256);
				Set<String> toDelMailids = new HashSet<>();
				for (MailLiteInfo.Builder mail : list) {
					if (midSet.contains(mail.getMailId())) {
						toDelMailids.add(mail.getId());
					}
				}
				if (!toDelMailids.isEmpty()) {
					// 所有的邮件Id
					String keyUnread = MailService.getInstance().keyUnread(playerId, type);
					String keySort = MailService.getInstance().keySort(playerId, type);
					// 删未读
					jedis.zrem(keyUnread, toDelMailids.toArray(new String[0]));
					// 删排序
					jedis.zrem(keySort, toDelMailids.toArray(new String[0]));
					// 所有的邮件Id
//					// 删entity
//					jedis.del(toDelMailids.stream().map(MailService.getInstance()::keyEntity).toArray(byte[][]::new));
//					// 删content
//					jedis.del(toDelMailids.stream().map(MailService.getInstance()::keyContent).toArray(byte[][]::new));

					DungeonRedisLog.log("ClearSPACE_MECHAMail", "playerId:{}, count:{}", playerId, toDelMailids.size());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 新健邮件
	 */
	private boolean addMailContent(final MailLiteInfo mail, final MailEntityContent content) {
		if(mail.getPlayerId().startsWith(BattleService.NPC_ID)){ // NPC 邮件不发送
			return true;
		}
		boolean result = true;
		long currTime = HawkTime.getMillisecond();
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();
				Pipeline pip = jedis.pipelined()) {
			int EXPIRE_SECOND = getMailExpireSecond(mail.getType());
			if (mail.getId().endsWith(REWARD_MAIL_DOT)) {
				EXPIRE_SECOND = (int) TimeUnit.DAYS.toSeconds(REWARD_MAIL_KEEP);
			}
			final String playerId = mail.getPlayerId();
			{
				// 存邮件体
				final byte[] key = keyContent(mail.getId());
				pip.set(key, content.toByteArray());
				pip.expire(key, EXPIRE_SECOND);
			}
			{
				// 存entity
				final byte[] key = keyEntity(mail.getId());
				pip.set(key, mail.toByteArray());
				pip.expire(key, EXPIRE_SECOND);
			}
			{
				// 存顺序
				final String key = keySort(playerId, mail.getType());
				pip.zadd(key, mail.getCtime(), mail.getId());
				pip.expire(key, (int) TimeUnit.DAYS.toSeconds(REWARD_MAIL_KEEP));
			}
			{
				// 存未读数
				final String key = keyUnread(playerId, mail.getType());
				pip.zadd(key, mail.getCtime(), mail.getId());
				pip.expire(key, (int) TimeUnit.DAYS.toSeconds(REWARD_MAIL_KEEP));
			}
			pip.sync();
		} catch (Exception e) {
			result = false;
			HawkException.catchException(e);
		}
		long costTimeMs = HawkTime.getMillisecond() - currTime;
		if (costTimeMs > HawkAppCfg.getInstance().getRedisOpTimeout()) {
			HawkLog.logPrintln("redis access costtime: {}, operation: {}", costTimeMs, "addMailContent");
		}
		return result;
	}
	
	/** 收藏邮件 **/
	public boolean addSaveMail(final MailLiteInfo mail) {
		boolean result = true;
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();
				Pipeline pip = jedis.pipelined()) {
			int EXPIRE_SECOND = GsConst.DAY_SECONDS * 30;
			final String playerId = mail.getPlayerId();
				// 存顺序
				final String key = keySort(playerId, SAVE_MAIL_TYPE);
				pip.zadd(key, mail.getCtime(), mail.getId());
				pip.expire(key, EXPIRE_SECOND);
			pip.sync();
		} catch (Exception e) {
			result = false;
			HawkException.catchException(e);
		}
		return result;
	}
	
	/** 取消收藏 */
	public void delSaveMail(final MailLiteInfo mail) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();
				Pipeline pip = jedis.pipelined()) {
			final String playerId = mail.getPlayerId();
			// 存顺序
			final String key = keySort(playerId, SAVE_MAIL_TYPE);
			pip.zrem(key, mail.getId());
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	/**
	 * 邮件体变更
	 */
	public void updateMailContent(final MailLiteInfo mail, final MailEntityContent content) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			long now = HawkTime.getMillisecond();
			int expireSeconds = getMailExpireSecond(mail.getType()) - (int) (now - mail.getCtime()) / 1000;
			if (mail.getId().endsWith(REWARD_MAIL_DOT)) {
				expireSeconds = (int) TimeUnit.DAYS.toSeconds(REWARD_MAIL_KEEP) - (int) (now - mail.getCtime()) / 1000;
			}
			// 存邮件体
			final byte[] key = keyContent(mail.getId());
			jedis.set(key, content.toByteArray());
			jedis.expire(key, expireSeconds);
		}catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	/**
	 * 邮件体
	 */
	public MailEntityContent getMailContent(final String mailId) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			final byte[] key = keyContent(mailId);
			byte[] contentbytes = jedis.get(key);
			if (contentbytes == null) {
				return MailEntityContent.newBuilder().build();
			}
			return MailEntityContent.newBuilder().mergeFrom(contentbytes).build();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return MailEntityContent.newBuilder().build();
	}

	/**
	 * 邮件entity
	 */
	public MailLiteInfo.Builder getMailEntity(final String mailId) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			final byte[] key = keyEntity(mailId);
			byte[] contentbytes = jedis.get(key);
			if (contentbytes == null) {
				return null;
			}
			return MailLiteInfo.newBuilder().mergeFrom(contentbytes);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 邮件entity
	 */
	public List<MailLiteInfo.Builder> listMailEntity(List<String> mailIds) {
		if (mailIds.isEmpty()) {
			return Collections.emptyList();
		}
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			List<MailLiteInfo.Builder> result = new ArrayList<>();
			List<byte[]> keys = mailIds.stream()
					.filter(StringUtils::isNotEmpty)
					.map(this::keyEntity).collect(Collectors.toList());
			List<byte[]> values = jedis.mget(keys.toArray(new byte[0][0]));
			for (byte[] value : values) {
				if (Objects.nonNull(value)) {
					result.add(MailLiteInfo.newBuilder().mergeFrom(value));
				}
			}
			return result;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Collections.emptyList();
	}

	/**
	 * 标记为已读
	 * 
	 * @param entity
	 */
	public void readMail(MailLiteInfo.Builder entity) {
		this.readMail(entity.getPlayerId(), entity.getType(), Arrays.asList(entity));
	}

	/**
	 * 标记为已读
	 * 
	 * @param entity
	 */
	public boolean readMail(String playerId, int type, List<MailLiteInfo.Builder> entityList) {
		if (entityList.isEmpty()) {
			return true;
		}
		entityList.forEach(entity -> entity.setStatus(MailStatus.READ_VALUE));
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();
				Pipeline pip = jedis.pipelined()) {
			// 所有的邮件Id
			String[] mailIdArr = entityList.stream().map(MailLiteInfo.Builder::getId).toArray(size -> new String[size]);
			// 删未读
			pip.zrem(keyUnread(playerId, type), mailIdArr);
			// 存entity
			long now = HawkTime.getMillisecond();
			for (MailLiteInfo.Builder entity : entityList) {
				byte[] key = keyEntity(entity.getId());
				byte[] val = entity.build().toByteArray();
				int expireSeconds = getMailExpireSecond(type) - (int) (now - entity.getCtime()) / 1000;
				if (entity.getId().endsWith(REWARD_MAIL_DOT)) {
					expireSeconds = (int) TimeUnit.DAYS.toSeconds(REWARD_MAIL_KEEP) - (int) (now - entity.getCtime()) / 1000;
				}
				pip.set(key, val);
				pip.expire(key, expireSeconds);
			}
			pip.sync();
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;

	}

	/**
	 * 删除
	 * 
	 * @param entity
	 */
	public void delMail(MailLiteInfo.Builder entity) {
		delMail(entity.getPlayerId(), entity.getType(), Arrays.asList(entity));
	}

	/**
	 * 批量删除
	 * 
	 * @param entity
	 */
	public void delMail(String playerId, int type, List<MailLiteInfo.Builder> entityList) {
		if (entityList.isEmpty()) {
			return;
		}
		entityList = entityList.stream().filter(mail -> !mail.getHasReward()).collect(Collectors.toList());

		long currTime = HawkTime.getMillisecond();
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();
				Pipeline pip = jedis.pipelined()) {
			// 所有的邮件Id
			String[] mailIdArr = entityList.stream().map(MailLiteInfo.Builder::getId).toArray(size -> new String[size]);
			// 删未读
			pip.zrem(keyUnread(playerId, type), mailIdArr);
			// 删排序
			pip.zrem(keySort(playerId, type), mailIdArr);
			
			// 战斗邮件等待正常过期(玩家正在tbly战斗中的有可能主播要看, 分享也有可能)
//			if (type != 5) {
//				// 删entity
//				pip.del(Arrays.stream(mailIdArr).map(this::keyEntity).toArray(byte[][]::new));
//				// 删content
//				pip.del(Arrays.stream(mailIdArr).map(this::keyContent).toArray(byte[][]::new));
//			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		long costTimeMs = HawkTime.getMillisecond() - currTime;
		if (costTimeMs > HawkAppCfg.getInstance().getRedisOpTimeout()) {
			HawkLog.logPrintln("redis access costtime: {}, operation: {}", costTimeMs, "delMail");
		}
	}

	/**
	 * 请求从 mailId 后一封开始的num封邮件
	 * 
	 * @param playerId
	 * @param mailId
	 * @param type
	 * @param num
	 * @return
	 */
	public List<MailLiteInfo.Builder> listMail(final String playerId, final String mailId, final int type, int num) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			long rank = 0;
			String keySort = keySort(playerId, type);
			if (StringUtils.isNotEmpty(mailId)) {
				Long zrevrank = jedis.zrevrank(keySort, mailId);
				if (Objects.isNull(zrevrank)) {
					return Collections.emptyList();
				}
				rank = zrevrank + 1;
			}
			Set<String> mailIds = jedis.zrevrange(keySort, rank, rank + num - 1);
			if (mailIds.isEmpty()) {
				return Collections.emptyList();
			}
			List<MailLiteInfo.Builder> listMail = listMailEntity(new ArrayList<>(mailIds));//
			return listMail;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Collections.emptyList();
	}

	/**
	 * 取得所有邮件.(不要乱用. 量非常大)
	 * 
	 * @param playerId
	 * @return
	 */
	public List<MailLiteInfo.Builder> listAllMail(String playerId) {
		int[] types = allMailTypes();
		List<MailLiteInfo.Builder> result = new ArrayList<>();
		for (int type : types) {
			result.addAll(this.listMail(playerId, null, type, Integer.MAX_VALUE));
		}

		return result;
	}

	/**
	 * 未读数量
	 * 
	 * @param playerId
	 * @param type
	 * @return
	 */
	public int unreadCount(final String playerId, int type) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			return jedis.zcount(keyUnread(playerId, type), 0, Long.MAX_VALUE).intValue();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	public String keyUnread(final String playerId, final int type) {
		return MAIL_UNREAD + playerId + ":" + type;
	}

	public String keySort(final String playerId, final int type) {
		return MAIL_SORT + playerId + ":" + type;
	}

	public byte[] keyEntity(final String uuid) {
		return (MAIL_ENTITY + uuid).getBytes();
	}

	public byte[] keyContent(final String uuid) {
		return (MAIL_CONTENT + uuid).getBytes();
	}

	/**
	 * String转换奖励列表
	 * 
	 * @param str
	 * @return
	 */
	private List<ItemInfo> stringToItemInfo(String str) {
		if (HawkOSOperator.isEmptyString(str)) {
			return null;
		}

		String[] rewards = str.split(",");
		List<ItemInfo> list = new ArrayList<ItemInfo>(rewards.length);
		for (String reward : rewards) {
			String[] arr = reward.split("_");
			if (arr.length == 3) {
				ItemInfo item = new ItemInfo();
				item.setType(Integer.valueOf(arr[0]));
				item.setItemId(Integer.valueOf(arr[1]));
				item.setCount(Integer.valueOf(arr[2]));
				list.add(item);
			}
		}
		return list;
	}

	/**
	 * 组织返回的邮件
	 * 
	 * @param mail
	 * @return
	 */
	public HPCheckMailRes.Builder createHPCheckMailResBuilder(String playerId, MailLiteInfo.Builder mail) {
		HPCheckMailRes.Builder builder = HPCheckMailRes.newBuilder();
		builder.setCreateTime(mail.getCtime());
		builder.setId(mail.getId());
		builder.setIsLmjy(mail.getIsLmjy());
		builder.setIsTBLY(mail.getIsTBLY());
		builder.setIsSW(mail.getIsSW());
		builder.setDuntype(mail.getDuntype());
		MailEntityContent content = MailService.getInstance().getMailContent(mail.getId());

		try {
			MailId mailId = MailId.valueOf(mail.getMailId());
			switch (mailId) {
			case INVITE_MOVE_CITY:
				if (content.getContentCount() > 0) {
					builder.setMoveCityInviteMail(MoveCityInviteMail.newBuilder().mergeFrom(content.getContent(0)));
				}
				break;
			case COLLECT_SUPERMINE_SUCC:
			case COLLECT_SUCC:
			case COLLECT_FAILED_TARGET_DISAPPEAR:
			case COLLECT_FAILED_ALLIED_OCCUPY:
			case COLLECT_FAILED_ENEMY_OCCUPY:
			case COLLECT_SUPERMIN_TARGET_CHANGED:
			case YURI_FACTORY_COLLECT:
			case STRONG_AWARD_REPORT:
			case TH_RESOURCE_REWARD:
			case COLL_RES_TREASURE_OK:
			case PYLON_COLLECT:
				// 采集资源邮件
				if (content.getContentCount() > 0) {
					builder.setCollectMail(CollectMail.newBuilder().mergeFrom(content.getContent(0)));
				}
				break;

			case DETECT_BASE_SUCC_TO_FROM:
			case DETECT_RES_SUCC_TO_FROM:
			case DETECT_CAMP_SUCC_TO_FROM:
			case DETECT_YURI_SUCC:
			case DETECT_GUILD_BASTION_SUCC_TO_FROM:
			case DETECT_CAPITAL_SUCC:
			case DETECT_TOWER_SUCC:
			case DETECT_STRONGPOINT_SUCCESS:
			case DETECT_FOGGY_SUCC:
			case DETECT_SUPER_WEAPON_SUCC:
			case DETECT_XZQ_SUCC:
			case DETECT_THRES_SUCCESS:
			case CHRISTMAS_BOX_SPY:
			case DETECT_PLYON_SUCCESS:
			case DETECT_XQHX_PLYON_SUCCESS:
			case SPY_WAR_FLAG_SUCESS:
			case SPY_CENTER_FLAG:
			case FORTRESS_SPY_SUCCUS:
			case TBLY_SPY_BUILD_SUCCESS:
			case TBLY_SPY_RES_SUCCESS:
			case SW_SPY_BUILD_SUCCESS:
			case SW_TERMINAL_SPY_BUILD_SUCCESS:
			case SW_ONE_SPY_BUILD_SUCCESS:
			case CYBORG_SPY_BUILD_SUCCESS:
			case DYZZ_SPY_BUILD_SUCCESS:
			case DYZZ_SPY_RES_SUCCESS:
			case YQZZ_SPY_BUILD_SUCCESS:
			case XQHX_SPY_BUILD_SUCCESS:
				// 侦查
				if (content.getContentCount() > 0) {
					builder.setDetectMail(DetectMail.newBuilder().mergeFrom(content.getContent(0)));
				}
				break;

			case WOUND_SOLDIER_DEAD:
			case PLANT_WOUND_SOLDIER_DEAD:
			case NATION_HOSPITAL_SOLDIER_DEAD:
				// 治疗伤兵
				builder.setCureMail(CureMail.newBuilder().mergeFrom(content.getContent(0)));
				break;

			case MONSTER_FAILED_TARGET_DISAPPEAR:
			case MONSTER_FAILED:
			case MONSTER_SUCC_KILLED:
			case MONSTER_SUCC:
				// 活动野怪
			case MASS_MONSTER_KILL_AWARD:
			case MASS_MONSTER_AWARD_WIN:
			case MASS_MONSTER_AWARD_FAIL:
				
			case TH_MONSTER_KILL_MEMBER:
			case TH_MONSTER_ATK_WIN:
			case TH_MONSTER_ATK_FAIL:
				
				// 新版野怪
			case WORLD_NEW_MONSTER_ATK_REPORT:
			case ATK_GUNDAM_WIN:
			case KILL_GUNDAM:
				
			case ATK_NIAN:
			case ONCE_KILL_NIAN:
			case KILL_NIAN:
			case GHOST_NIAN_2:
			case GHOST_NIAN_3:
			case GHOST_NIAN_4:
			case CHRISTMAS_PERSONAL_DEADLINESS:			
			case CHRISTMAS_PERSONAL_KILL:
			case CHRISTMAS_ATK:
				// 打怪
				if (content.getContentCount() > 0) {
					builder.setMonsterMail(MonsterMail.newBuilder().mergeFrom(content.getContent(0)));
				}
				break;
			// 尤里复仇战报
			case YURI_REVENGE_FIGHT_WIN:
			case YURI_REVENGE_FIGHT_FAILED:
			case YURI_REVENGE_ASSIST_FIGHT_WIN:
			case YURI_REVENGE_ASSIST_FIGHT_FAILED:
				builder.setYuriRevengeMail(YuriRevengeFightMail.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case ATTACK_BASE_SUCC_TO_FROM:
			case ATTACK_BASE_SUCC_TO_TARGET:
			case DUEL_ATTACK_BASE_SUCC_TO_FROM:
			case DUEL_ATTACK_BASE_SUCC_TO_TARGET:
			case ATTACK_CAMP_SUCC_TO_FROM:
			case ATTACK_CAMP_SUCC_TO_TARGET:
			case ATTACK_RES_SUCC_TO_FROM:
			case ATTACK_RES_SUCC_TO_TARGET:
			case ATTACK_CAPITAL_SUCC_TO_FROM:
			case ATTACK_CAPITAL_SUCC_TO_TARGET:
			case ATTACK_GUILD_BASTION_SUCC_TO_FROM:
			case ATTACK_GUILD_BASTION_SUCC_TO_TARGET:
			case ATTACK_GUILD_BASTION_FAILED_TO_FROM:
			case ATTACK_GUILD_BASTION_FAILED_TO_TARGET:
			case ATTACK_BASE_FAILED_TO_FROM:
			case ATTACK_BASE_FAILED_TO_TARGET:
			case DUEL_ATTACK_BASE_FAILED_TO_FROM:
			case DUEL_ATTACK_BASE_FAILED_TO_TARGET:
			case ATTACK_CAMP_FAILED_TO_FROM:
			case ATTACK_CAMP_FAILED_TO_TARGET:
			case ATTACK_RES_FAILED_TO_FROM:
			case ATTACK_RES_FAILED_TO_TARGET:
			case ATTACK_CAPITAL_FAILED_TO_FROM:
			case ATTACK_CAPITAL_FAILED_TO_TARGET:
			case ATTACK_YURI_SUCC:
			case ATTACK_YURI_FAILED:
			case ATTACK_CAPITAL_TOWER_FAILED_TO_FROM:
			case ATTACK_CAPITAL_TOWER_SUCC_TO_FROM:
			case ATTACK_CAPITAL_TOWER_FAILED_TO_TARGET:
			case ATTACK_CAPITAL_TOWER_SUCC_TO_TARGET:
			case ATTACK_STRONG_POINT_PVP_SUCC_TO_FROM:
			case ATTACK_STRONG_POINT_PVP_SUCC_TO_TARGET:
			case ATTACK_STRONG_POINT_PVP_FAILED_TO_FROM:
			case ATTACK_STRONG_POINT_PVP_FAILED_TO_TARGET:
			case ATTACK_SUPER_WEAPON_SUCC_TO_FROM:
			case ATTACK_SUPER_WEAPON_SUCC_TO_TARGET:
			case ATTACK_SUPER_WEAPON_FAILED_TO_FROM:
			case ATTACK_SUPER_WEAPON_FAILED_TO_TARGET:
			case ATTACK_XZQ_SUCC_TO_FROM:
			case ATTACK_XZQ_SUCC_TO_TARGET:
			case ATTACK_XZQ_FAILED_TO_FROM:
			case ATTACK_XZQ_FAILED_TO_TARGET:
			case ATTACK_TREASURE_HUNT_RES_SUCC_TO_FROM:
			case ATTACK_TREASURE_HUNT_RES_SUCC_TO_TARGET:
			case ATTACK_TREASURE_HUNT_RES_FAILED_TO_FROM:
			case ATTACK_TREASURE_HUNT_RES_FAILED_TO_TARGET:
			case ATTACK_WAR_FLAG_SUCC_TO_FROM:
			case ATTACK_WAR_FLAG_SUCC_TO_TARGET:
			case ATTACK_WAR_FLAG_FAILED_TO_FROM:
			case ATTACK_WAR_FLAG_FAILED_TO_TARGET:
			case ATTACK_FORTRESS_SUCC_TO_FROM:
			case ATTACK_FORTRESS_FAILED_TO_FROM:
			case ATTACK_FORTRESS_FAILED_TO_TARGET:
			case ATTACK_FORTRESS_SUCC_TO_TARGET:
			case ATTACK_TW_BUILD_SUCC_TO_FROM:
			case ATTACK_TW_BUILD_FAILED_TO_FROM:
			case ATTACK_TW_BUILD_FAILED_TO_TARGET:
			case ATTACK_TW_BUILD_SUCC_TO_TARGET:
			case ATTACK_TW_RES_SUCC_TO_FROM:
			case ATTACK_TW_RES_SUCC_TO_TARGET:
			case ATTACK_TW_RES_FAILED_TO_FROM:
			case ATTACK_TW_RES_FAILED_TO_TARGET:
				
			case ATTACK_DYZZ_BUILD_SUCC_TO_FROM:
			case ATTACK_DYZZ_BUILD_FAILED_TO_FROM:
			case ATTACK_DYZZ_BUILD_FAILED_TO_TARGET:
			case ATTACK_DYZZ_BUILD_SUCC_TO_TARGET:
			case ATTACK_DYZZ_RES_SUCC_TO_FROM:
			case ATTACK_DYZZ_RES_SUCC_TO_TARGET:
			case ATTACK_DYZZ_RES_FAILED_TO_FROM:
			case ATTACK_DYZZ_RES_FAILED_TO_TARGET:
				
			case ATTACK_YQZZ_BUILD_SUCC_TO_FROM:
			case ATTACK_YQZZ_BUILD_FAILED_TO_FROM:
			case ATTACK_YQZZ_BUILD_FAILED_TO_TARGET:
			case ATTACK_YQZZ_BUILD_SUCC_TO_TARGET:
				
			case ATTACK_XQHX_BUILD_SUCC_TO_FROM:
			case ATTACK_XQHX_BUILD_FAILED_TO_FROM:
			case ATTACK_XQHX_BUILD_FAILED_TO_TARGET:
			case ATTACK_XQHX_BUILD_SUCC_TO_TARGET:
				
			case ATTACK_XHJZ_BUILD_SUCC_TO_FROM:
			case ATTACK_XHJZ_BUILD_FAILED_TO_FROM:
			case ATTACK_XHJZ_BUILD_FAILED_TO_TARGET:
			case ATTACK_XHJZ_BUILD_SUCC_TO_TARGET:
				
			case CHAMPIONSHIP_ATK_WIN:
			case CHAMPIONSHIP_ATK_FAILED:
			case METERIAL_TRANSPORT_ATK_WIN:
			case METERIAL_TRANSPORT_ATK_FAILED:
				
			case ATTACK_SW_ONE_BUILD_SUCC_TO_FROM:
			case ATTACK_SW_ONE_BUILD_FAILED_TO_FROM:
			case ATTACK_SW_ONE_BUILD_FAILED_TO_TARGET:
			case ATTACK_SW_ONE_BUILD_SUCC_TO_TARGET:
			case ATTACK_SW_BUILD_SUCC_TO_FROM:
			case ATTACK_SW_BUILD_FAILED_TO_FROM:
			case ATTACK_SW_BUILD_FAILED_TO_TARGET:
			case ATTACK_SW_BUILD_SUCC_TO_TARGET:
			case ATTACK_SW_TERMINAL_BUILD_SUCC_TO_FROM:
			case ATTACK_SW_TERMINAL_BUILD_FAILED_TO_FROM:
			case ATTACK_SW_TERMINAL_BUILD_FAILED_TO_TARGET:
			case ATTACK_SW_TERMINAL_BUILD_SUCC_TO_TARGET:
			case ATK_PYLON_SUCCESS:
			case DEF_PYLON_FAIL:
			case ATK_PYLON_FAIL:
			case DEF_PYLON_SUCCESS:
			case ATK_XQHX_PYLON_SUCCESS:
			case DEF_XQHX_PYLON_FAIL:
			case ATK_XQHX_PYLON_FAIL:
			case DEF_XQHX_PYLON_SUCCESS:
			case ATTACK_CHRISTMAS_SUCC_TO_FROM:
			case ATTACK_CHRISTMAS_SUCC_TO_TARGET:
			case ATTACK_CHRISTMAS_FAILED_TO_FROM:
			case ATTACK_CHRISTMAS_FAILED_TO_TARGET:
			case CYBORG_ATTACK_BUILD_SUCC_TO_FROM:
			case CYBORG_ATTACK_BUILD_SUCC_TO_TARGET:
			case CYBORG_ATTACK_BUILD_FAILED_TO_FROM:
			case CYBORG_ATTACK_BUILD_FAILED_TO_TARGET:
			case ATTACK_MEDALF_SUCC_TO_FROM:// = 2024032701; // 驱赶胜利
			case ATTACK_MEDALF_SUCC_TO_TARGET:// = 2024032702; // 驱赶胜利对方
			case ATTACK_MEDALF_FAILED_TO_FROM:// = 2024032703; // 驱赶失败
			case ATTACK_MEDALF_FAILED_TO_TARGET://
				// 战斗
				if (content.getContentCount() > 0) {
					builder.setFightMail(FightMail.newBuilder().mergeFrom(content.getContent(0)));
				}
				break;

			case RES_ASSISTANCE_SUCC_TO_FROM:
			case RES_ASSISTANCE_SUCC_TO_TARGET:
				// 资源援助
				if (content.getContentCount() > 0) {
					builder.setResAssistanceMail(ResAssistanceMail.newBuilder().mergeFrom(content.getContent(0)));
				}
				break;

			case SOILDER_ASSISTANCE_SUCC_TO_FROM:
			case SOILDER_ASSISTANCE_SUCC_TO_TARGET:
			case CAMP_SUCC_MAIL:
				// 士兵援助邮件
				if (content.getContentCount() > 0) {
					builder.setAssistanceMail(SoilderAssistanceMail.newBuilder().mergeFrom(content.getContent(0)));
				}
				break;
			case GUILD_INVITE:
				// 联盟邀请函
				builder.setGuildInviteMail(GuildInviteMail.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case ATTACK_STRONG_POINT_PVE_SUCC_TO_FROM:
			case ATTACK_STRONG_POINT_PVE_FAILED_TO_FROM:
			case ATTACK_FOGGY_SUCC_TO_FROM:
			case ATTACK_FOGGY_FAILED_TO_FROM:
			case ATTACK_YURI_STRIKE_SUCC_TO_FROM:
			case ATTACK_YURI_STRIKE_FAILED_TO_FROM:
			case ATTACK_SUPER_WEAPON_PVE_SUCC_TO_FROM:
			case ATTACK_SUPER_WEAPON_PVE_FAILED_TO_FROM:
			case ATTACK_XZQ_PVE_SUCC_TO_FROM:
			case ATTACK_XZQ_PVE_FAILED_TO_FROM:
			case DEF_GHOST_MARCH_FIGHT_FAILED:
			case DEF_GHOST_MARCH_FIGHT_WIN:
			case ATTACK_FORTRESS_PVE_SUCC_TO_FROM:
			case ATTACK_FORTRESS_PVE_FAILED_TO_FROM:
			case ATTACK_YQZZ_BUILD_SUCC_TO_FROM_PVE:
			case ATTACK_FGYL_BUILD_SUCC_TO_FROM_PVE:
			case ATTACK_YQZZ_BUILD_FAILED_TO_FROM_PVE:
			case GHOST_TOWER_MONSTER_ATTACK_SUCC_TO_FROM:
			case GHOST_TOWER_MONSTER_ATTACK_FAILED_TO_FROM:
			case SPACE_MECHA_FIGHT_FAILED:
			case SPACE_MECHA_FIGHT_WIN:
			case SPACE_MECHA_STRONG_HOLD_FIGHT_WIN:
			case SPACE_MECHA_STRONG_HOLD_FIGHT_FAILED:
			case SPACE_MECHA_MAINSPACE_BROKEN:
			case SPACE_MECHA_SUBSPACE_BROKEN:
			case SPACE_MECHA_STRONGHOLD_BROKEN:
				// 据点PVE邮件
				builder.setPveFightMail(PveFightMail.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case DEAD_SOLDIER_RESOURCE_BUCHANG:
				builder.setGuildHospiceMail(PBGuildHospiceMail.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case GUILD_RANK_DAILY_PUSH_TOP3:
				// 联盟排行每日榜单
				builder.setGuildRankTop3Mail(PBGuildRankContent.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case PLAYER_DRESS_FRIEND_ASK:
			case PLAYER_DRESS_FRIEND_SEND:
			case PLAYER_DRESS_ALLY_ASK:
			case PLAYER_DRESS_ALLY_SEND:
				builder.setDressSend(PBPlayerDressAskMailContent.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case GUNDAM_START_UP:
				builder.setGundamStartup(PBGundamStartUp.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case TBLY_NUCLEAR_HIT:
				builder.setTblyNuclear(PBTBLYNuclearHitContent.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case CYBORG_NUCLEAR_HIT:
				builder.setCyborgNuclear(PBCYBORGNuclearHitContent.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case YQZZ_NUCLEAR_HIT:
				builder.setYqzzNuclear(PBYQZZNuclearHitContent.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case GUARD_FRIEND_BLAG:
			case GUARD_GUILD_BLAG:
			case GUARD_FRIEND_SEND:
			case GUARD_GUILD_SEND:
				builder.setGuardDress(PBPlayerGuardDressMailContent.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case CYBORG_CONTRIBUTE_NOTICE:
				builder.setCyborgContribution(PBCyborgContributionMail.newBuilder().mergeFrom(content.getContent(0)));
				break;
			case GUILD_DRAGON_ATTCK_PLAYER_FIGHT:
				builder.setDragonAtkMail(PBGuildDragonAttackDamageMail.newBuilder().mergeFrom(content.getContent(0)));
				break;
			default:
				JSONArray arr = new JSONArray();
				content.getContentList().stream().map(ByteString::toStringUtf8).forEach(arr::add);
				// 其它邮件
				CommonMail.Builder commonMail = CommonMail.newBuilder();
				commonMail.setMailMessage(arr.toJSONString());
				if (!HawkOSOperator.isEmptyString(mail.getReward())) {
					List<ItemInfo> items = stringToItemInfo(mail.getReward());
					for (ItemInfo info : items) {
						RewardItem.Builder reward = RewardItem.newBuilder();
						reward.setItemType(info.getType());
						reward.setItemId(info.getItemId());
						reward.setItemCount(info.getCount());
						commonMail.addRewards(reward);
					}
				}
				builder.setCommonMail(commonMail);
				break;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return builder;
	}

	public String createChatRoom(Player player, List<Player> members, String message, ChatType type) {
		return this.createChatRoom(player, members, message, type, null);
	}

	/**
	 * 创建聊天室
	 * 
	 * @param player
	 * @param members
	 * @param message
	 * @param type
	 * @return
	 */
	public String createChatRoom(Player player, List<Player> members, String message, ChatType type, String roomId) {
		long curTime = HawkTime.getMillisecond();
		ChatRoomData.Builder dataBuilder = ChatRoomData.newBuilder();
		dataBuilder.setCreaterId(player.getId());
		dataBuilder.setChatType(type);
		dataBuilder.setEffectTime(ConstProperty.getInstance().getMailEffectTime() * 1000 + curTime);
		// 聊天室ID
		if (StringUtils.isEmpty(roomId)) {
			roomId = HawkOSOperator.randomUUID();
		}
		for (Player member : members) {
			if (Objects.isNull(member)) {
				continue;
			}
			MemberData.Builder memberData = MemberData.newBuilder();
			memberData.setPlayerId(member.getId());
			memberData.setIsDelete(false);
			dataBuilder.addMembers(memberData);
			dataBuilder.addJoinMembers(member.getId());
		}
		if (!LocalRedis.getInstance().addChatRoomData(roomId, dataBuilder)) {
			return null;
		}

		// 添加聊天室聊天信息
		ChatData chatData;
		if (StringUtils.isEmpty(message) && ChatType.P2P != type) {
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(SysMsgType.ADD_MEMBER_VALUE + "_" + player.getName());
			for (Player member : members) {
				if (!Objects.equals(player.getName(), member.getName())) {
					strBuilder.append("_" + member.getName());
				}
			}
			chatData = addChatMessage(player, roomId, null, strBuilder.toString());
		} else {

			chatData = addChatMessage(player, roomId, player.getId(), message);
		}

		int recNum = 0;
		String toPlayerId = "";
		RelationService relationService = RelationService.getInstance();
		// 发送聊天室邮件
		for (Player member : members) {
			if (relationService.isBlacklist(member.getId(), player.getId())) {
				continue;
			}
			PersonalMailService.getInstance().sendChat(roomId, member.getId(), player.getId(), chatData, curTime);
			if (!player.getId().equals(member.getId())) {
				toPlayerId = member.getId();
				recNum++;
			}
		}

		if (recNum > 0) {
			LogUtil.logChatInfo(player, toPlayerId, SnsType.ROOM_CHAT, message, recNum);
			LogUtil.logSecTalkFlow(player, recNum == 1 ? toPlayerId : null, LogMsgType.MAIL, roomId, message);
		}

		// 添加交互信息（私聊）
		if (members.size() == 2) {
			LocalRedis.getInstance().addInteractivePlayer(members.get(0).getId(), members.get(1).getId(), curTime);
			LocalRedis.getInstance().addInteractivePlayer(members.get(1).getId(), members.get(0).getId(), curTime);
		}

		return roomId;
	}

	/**
	 * 添加聊天室聊天信息
	 * 
	 * @param roomId
	 * @param message
	 */
	public ChatData addChatMessage(Player player, String roomId, String playerId, String message) {

		ChatData.Builder chatData = ChatData.newBuilder();
		if (!HawkOSOperator.isEmptyString(playerId)) {
			chatData.setPlayerId(playerId);
		}

		chatData.setMessage(message);
		chatData.setGuildTag(player.getGuildTag());
		chatData.setOfficeId(GameUtil.getOfficerId(player.getId()));
		chatData.setMsgTime(HawkTime.getMillisecond());
		if (StringUtils.isNotEmpty(message)) {
			LocalRedis.getInstance().addChatMessage(roomId, chatData);
		}

		DressItem titleDress = WorldPointService.getInstance().getShowDress(playerId, DressType.TITLE_VALUE);
		if (titleDress != null) {
			chatData.setDressTitle(titleDress.getModelType());
		} else {
			chatData.setDressTitle(0);
		}

		chatData.setDressTitleType(WorldPointService.getInstance().getDressTitleType(playerId));
		
		return chatData.build();
	}

	/**
	 * 删除聊天室
	 * 
	 * @param mailIds
	 */
	public void deleteChatRooms(Player player, List<String> mailIds) {
		// 删除聊天室
		LocalRedis.getInstance().delPlayerChatRoom(player.getId(), mailIds.toArray(new String[mailIds.size()]));
		for (String roomId : mailIds) {
			// 清理数据
			ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
			if (dataBuilder == null) {
				continue;
			}

			List<MemberData.Builder> memberDatas = new ArrayList<MemberData.Builder>(dataBuilder.getMembersBuilderList());
			dataBuilder.clearMembers();

			int clearNumber = 0;
			for (MemberData.Builder memberData : memberDatas) {
				if (memberData.getPlayerId().equals(player.getId())) {
					memberData.setIsDelete(true);
				}
				if (memberData.getIsDelete()) {
					clearNumber++;
				}
				dataBuilder.addMembers(memberData);
			}
			if (clearNumber >= memberDatas.size()) {
				LocalRedis.getInstance().delChatRoom(roomId);
			} else {
				LocalRedis.getInstance().addChatRoomData(roomId, dataBuilder);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public int chatMailType() {
		return getMailType(MailId.CHAT_MAIL);
	}

	/**
	 * 提取邮件附件
	 * 
	 * @param entity
	 * @param award
	 * 
	 * @return 返回true表示取到了奖励，false表示未取到奖励
	 */
	public boolean getMailReward(Player player, MailLiteInfo.Builder entity, AwardItems award) {
		if (!entity.getHasReward() || HawkOSOperator.isEmptyString(entity.getReward())) {
			return false;
		}

		if (!Objects.equals(player.getId(), entity.getPlayerId())) {
			return false;
		}

		if (player.isZeroEarningState()) {
			player.sendIDIPZeroEarningMsg();
			return false;
		}

		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.MAIL_REWARD_RECV)) {
			return false;
		}
		
		if(entity.getMailId() == MailId.DEAD_SOLDIER_RESOURCE_BUCHANG_VALUE){
			// 战损补给不限制数量. 
			award.setCountCheck(false);
		}

		// 如果是好友赠送奖励邮件
		if (entity.getMailId() == MailId.PRESTENT_GIFT_VALUE) {
			DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
			// 当日已经领取的数量
			int alreadyGetTimes = dailyDataEntity.getFriendBoxTimes(MailId.PRESTENT_GIFT_VALUE);
			// 可以领取的最大数量
			int maxGetTimes = ConstProperty.getInstance().getFriendGift();
			if (alreadyGetTimes >= maxGetTimes) {
				return false;
			}

			dailyDataEntity.addFriendBoxTimes(MailId.PRESTENT_GIFT_VALUE, 1);
		}

		String[] rewards = entity.getReward().split(",");
		int diamonds = 0, newServerRebate = 0;
		boolean isHave = false;
		for (String reward : rewards) {
			String[] arr = reward.split("_");
			if (arr.length != 3) {
				continue;
			}

			isHave = true;
			int itemType = Integer.valueOf(arr[0]);
			int itemId = Integer.valueOf(arr[1]);
			int itemCount = Integer.valueOf(arr[2]);
			// 钻石奖励不走awardItems体系
			if (GameUtil.isDiamond(itemType, itemId)) {
				diamonds += itemCount;
				if (entity.getMailId() == MailId.SHARE_PROSP_OLDSVR_REWARD_VALUE) {
					newServerRebate += itemCount;
				}
				continue;
			}
			if (itemType / GsConst.ITEM_TYPE_BASE == Const.ItemType.TOOL_VALUE && HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId) == null) {
				continue;
			}
			// 作用号触发,航海远征积分排行邮件奖励双倍
			if (entity.getMailId() == MailId.CROSS_ACTIVITY_SERVER_RANK_VALUE && player.getEffect().getEffVal(EffType.CROSS_TECH_EFF_3015) > 0) {
				itemCount = itemCount * 2;
			}
			award.addMailItem(itemType, itemId, itemCount, player, Action.SYS_MAIL_AWARD, entity.getMailId() , entity.getId());
		}
		
		if (!isHave) {
			return false;
		}

		entity.setHasReward(false);

		// 只有大R代充和idip接口赠送的钻石才允许发放
		if (diamonds > 0) {
			if (StringUtils.isNotEmpty(entity.getAdditionalParam())) {
				// 赠送原因 recharge_activity
				player.increaseDiamond(diamonds, Action.XINYUE_HELP_RECHARGE, entity.getAdditionalParam(), DiamondPresentReason.RECHATGE);
			} else if (GameUtil.isRewardMail(entity.getMailId())) {
				// 赠送原因  customer_experience
				player.increaseDiamond(diamonds, Action.GET_MAIL_REWARD, entity.getAdditionalParam(), DiamondPresentReason.EXPERIENCE);
				if (newServerRebate > 0) {
					ActivityManager.getInstance().postEvent(new ShareProsperityEvent(player.getId(), newServerRebate));
				}
			} else {
				HawkLog.errPrintln("unsupport maile reward diamond, playerId: {}, mailId: {}, diamonds: {}", player.getId(), entity.getMailId(), diamonds);
			}

			player.getPush().syncPlayerDiamonds();
		}
		// 增加领取邮件奖励的行为日志
		BehaviorLogger.log4Player(player.getId(), Source.MAIL, Action.GET_MAIL_REWARD,
				Params.valueOf("ctime", entity.getCtime()),
				Params.valueOf("cServer", entity.getSendServerId()),
				Params.valueOf("mailIdUUID", entity.getId()),
				Params.valueOf("mailId", MailId.valueOf(entity.getMailId())),
				Params.valueOf("mailType", entity.getType()),
				Params.valueOf("reward", entity.getReward()),
				Params.valueOf("diamonds", diamonds));

		return true;
	}

	/**
	 * 清除玩家所有邮件
	 * @param playerId
	 */
	public void clearAll(String playerId) {
		for (int mailType : allMailTypes()) {
			final String key1 = keySort(playerId, mailType);
			RedisProxy.getInstance().getRedisSession().del(key1);
			
			final String key2 = keyUnread(playerId, mailType);
			RedisProxy.getInstance().getRedisSession().del(key2);
		}
	}
}
