package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.GuildGiftEvent;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.VipCfg;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.entity.PlayerGuildGiftEntity;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.GuildSmailGift;
import com.hawk.game.invoker.GuildSmailGiftGetAwardMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.msg.GuildSmailGiftAddMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildBigGift.PBDeleteGiftRequest;
import com.hawk.game.protocol.GuildBigGift.PBGetGiftAwardRequest;
import com.hawk.game.protocol.GuildBigGift.PBGetGiftLog;
import com.hawk.game.protocol.GuildBigGift.PBGuildGiftLog;
import com.hawk.game.protocol.GuildBigGift.PBPlayerGuildGiftResp;
import com.hawk.game.protocol.GuildBigGift.PBPlayerGuildGiftUpdate;
import com.hawk.game.protocol.GuildBigGift.PBSyncPlayerGiftSize;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.SysFunctionModuleId;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 联盟大礼包
 * 
 * @author lwt
 * @date 2018年3月21日
 */
public class PlayerGuildBigGiftModule extends PlayerModule {
	private int lastRedCount;

	public PlayerGuildBigGiftModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		if(player.isCsPlayer()){
			return true;
		}
		long now = HawkTime.getMillisecond();
		int value = (int) getPlayerData().getGuildGiftEntity().stream()
				.filter(gift -> gift.getGiftOverTime() > now)
				.filter(gift -> gift.getState() == 0)
				.count();
		if (value != lastRedCount) {
			lastRedCount = value;
			PBSyncPlayerGiftSize.Builder resp = PBSyncPlayerGiftSize.newBuilder();
			resp.setSize(lastRedCount);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_SMAIL_GIFT_SIZE, resp));
		}
		return super.onTick();
	}

	@Override
	protected boolean onPlayerLogin() {
		// 跨服玩家不处理
		if(player.isCsPlayer()){
			return true;
		}
		
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					loginProcess();
					return null;
				}
			};
			task.setPriority(1);
			task.setTypeName("GuildBigGiftModuleLogin");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
		} else {
			loginProcess();
		}
		
		return super.onPlayerLogin();
	}
	
	/**
	 * 登录处理
	 */
	private void loginProcess() {
		if (player.hasGuild()) {
			lastRedCount = 0;
			// 清理过期
			clearOverTimeGift();

			// 检查未领取
			refrashGiftList();
			GuildService.getInstance().bigGift(player.getGuildId()).syncInfo(player);
		} else {
			getPlayerData().getGuildGiftEntity().forEach(PlayerGuildGiftEntity::delete);
			getPlayerData().getGuildGiftEntity().clear();
		}
	}

	/** 清理过期 */
	private void clearOverTimeGift() {
		long now = HawkTime.getMillisecond();
		List<PlayerGuildGiftEntity> list = getPlayerData().getGuildGiftEntity().stream().filter(gift -> gift.getGiftOverTime() < now).collect(Collectors.toList());
		getPlayerData().getGuildGiftEntity().removeAll(list);
		list.forEach(PlayerGuildGiftEntity::delete);
	}

	/**
	 * 有新的礼物
	 */
	@MessageHandler
	private boolean onGuildSmailGiftAddMsg(GuildSmailGiftAddMsg msg) {
		if (player.getData().getGuildGiftEntity().size() >= GuildConstProperty.getInstance().getAllianceGiftUpLimit()) {
			// 总数达到上限 清理过期
			clearOverTimeGift();
		}

		GuildSmailGift gift = msg.getGift();
		PlayerGuildGiftEntity entity = addSmailGift(gift);
		if (Objects.nonNull(entity)) {
			notifyUpdate(Arrays.asList(entity), false);
		}

		return true;
	}

	/**
	 * 加入联盟
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onGuildJoinMsg(GuildJoinMsg msg) {
		String guildId = msg.getGuildId();
		GuildService.getInstance().bigGift(guildId).syncInfo(player);
		return true;
	}

	/**
	 * 退出联盟
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onGuildQuitMsg(GuildQuitMsg msg) {
		getPlayerData().getGuildGiftEntity().forEach(e -> e.delete(true));
		getPlayerData().getGuildGiftEntity().clear();
		return true;
	}

	private List<PlayerGuildGiftEntity> refrashGiftList() {
		long joinTime = GuildService.getInstance().getGuildMemberObject(player.getId()).getJoinGuildTime();
		long lastRefrash = GuildService.getInstance().getGuildMemberObject(player.getId()).getLastRefrashBigGift();
		lastRefrash = Math.max(joinTime, lastRefrash);
		List<GuildSmailGift> giftList = GuildService.getInstance().bigGift(player.getGuildId()).unResiveGifts(lastRefrash);

		List<PlayerGuildGiftEntity> addList = new ArrayList<>();
		for (GuildSmailGift gift : giftList) {
			PlayerGuildGiftEntity entity = addSmailGift(gift);
			if (Objects.nonNull(entity)) {
				addList.add(entity);
			}
		}
		return addList;
	}

	private PlayerGuildGiftEntity addSmailGift(GuildSmailGift gift) {
		// 记录礼物时间.
		GuildMemberObject guildMemberObject = GuildService.getInstance().getGuildMemberObject(player.getId());
		if(Objects.isNull(guildMemberObject)){
			return null;
		}
		guildMemberObject.updateLastRefrashBigGift(HawkTime.getMillisecond());
		if (player.getData().getGuildGiftEntity().size() >= GuildConstProperty.getInstance().getAllianceGiftUpLimit()) {
			// 总数达到上限,无法领取
			return null;
		}
		PlayerGuildGiftEntity dbEntity = new PlayerGuildGiftEntity();
		dbEntity.setPlayerId(player.getId());
		dbEntity.setItemId(gift.getItemId());
		dbEntity.setAwardGet(gift.getReward());
		dbEntity.setGiftCreateTime(gift.getCreateTime());
		dbEntity.setGiftOverTime(gift.getOverTime());

		HawkDBManager.getInstance().create(dbEntity);

		player.getData().getGuildGiftEntity().add(dbEntity);
		return dbEntity;
	}

	/**
	 * 广播变化
	 * 
	 * @param list
	 * @param update
	 *            是更新
	 */
	private void notifyUpdate(List<PlayerGuildGiftEntity> list, boolean update) {
		if (list.isEmpty()) {
			return;
		}
		PBPlayerGuildGiftUpdate.Builder respBul = PBPlayerGuildGiftUpdate.newBuilder();
		for (PlayerGuildGiftEntity gift : list) {
			respBul.addGiftList(gift.toPbObj());
		}
		respBul.setUpdate(update);

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_SMAIL_GIFT_UPDATE, respBul));
	}

	/**
	 * 领取礼物
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_SMAIL_GIFT_GET_REWARD_C_VALUE)
	private void onGetReward(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			return;
		}
		boolean isSysFunctionNotOpen = !GameUtil.checkSysFunctionOpen(player, SysFunctionModuleId.GUILDAWARD);
		if(isSysFunctionNotOpen){
			return;
		}
		PBGetGiftAwardRequest req = protocol.parseProtocol(PBGetGiftAwardRequest.getDefaultInstance());
		List<String> giftIdlist = req.getIdsList();
		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, player.getVipLevel());
		boolean isNotVipDrawAllGift = Objects.isNull(vipCfg) || vipCfg.getDrawAllGift() == 0;
		if (giftIdlist.size() > 1 && isNotVipDrawAllGift) {
			HawkLog.errPrintln("BigGift get error isNotVipDrawAllGift={} , isSysFunctionNotOpen={}", isNotVipDrawAllGift, isSysFunctionNotOpen);
			return;
		}
		long now = HawkTime.getMillisecond();
		List<PlayerGuildGiftEntity> giftList = player.getData().getGuildGiftEntity().stream()
				.filter(gift -> giftIdlist.contains(gift.getId()))
				.filter(gift -> gift.getState() == 0)
				.filter(gift -> gift.getGiftOverTime() > now)
				.collect(Collectors.toList());
		boolean isDouble = player.getEffect().getEffVal(EffType.GUILD_GIFT_REWARD_DOUBLE) > 0;
		AwardItems awardItem = AwardItems.valueOf();
		List<byte[]> logInfos = new ArrayList<>();
		// 没有指定奖励的 取award 有的直接用
		for (PlayerGuildGiftEntity gift : giftList) {
			if (StringUtils.isEmpty(gift.getAwardGet())) {
				ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, gift.getItemId());
				AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, itemCfg.getRewardId());
				AwardItems awardItems = awardCfg.getRandomAward();
				List<ItemInfo> awardList = awardItems.getAwardItems();
				String reward = ItemInfo.toString(awardList);
				gift.setAwardGet(reward);
			}
			List<ItemInfo> awardList = ItemInfo.valueListOf(gift.getAwardGet());
			if (isDouble) {
				for (ItemInfo itemInfo : awardList) {
					itemInfo.setCount(itemInfo.getCount() * 2);
				}
			}
			String reward = ItemInfo.toString(awardList);
			gift.setAwardGet(reward);
			awardItem.addItemInfos(awardList);
			gift.setState(1);

			// 领取记录
			PBGuildGiftLog.Builder logInfo = PBGuildGiftLog.newBuilder();
			logInfo.setId(gift.getId());
			logInfo.setItemId(gift.getItemId());
			logInfo.setAwardGet(gift.getAwardGet());
			logInfo.setGetTime(now);
			logInfos.add(logInfo.build().toByteArray());
		}

		LocalRedis.getInstance().addGuildGiftLog(player.getId(), logInfos);

		// 批量领取礼包时，前端要有不同的表现方式
		if (giftIdlist.size() > 1) {
			awardItem.rewardTakeAffectAndPush(player, Action.PLAYER_SMAIL_GIFT_GET_REWARD, true, RewardOrginType.SHOPPING_GIFT);
		} else {
			awardItem.rewardTakeAffectAndPush(player, Action.PLAYER_SMAIL_GIFT_GET_REWARD, true);
		}

		player.responseSuccess(protocol.getType());
		
		ActivityManager.getInstance().postEvent(new GuildGiftEvent(player.getId(), giftIdlist.size()));
		// 删除已领取的礼包
		getPlayerData().getGuildGiftEntity().removeAll(giftList);
		giftList.forEach(e -> e.delete(true));

		// 联盟线程 更新大礼包经验 message
		GuildSmailGiftGetAwardMsgInvoker invoker = new GuildSmailGiftGetAwardMsgInvoker(player.getGuildId(), giftList);
		GuildService.getInstance().dealMsg(MsgId.PLAYER_SMAIL_GIFT_GET_REWARD, invoker);
	}

	/**
	 * 获取礼包领取记录
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_SMAIL_GIFT_GET_LOG_C_VALUE)
	private void onGetGiftLog(HawkProtocol protocol) {
		List<byte[]> logInfos = LocalRedis.getInstance().getGuildGiftLog(player.getId());
		PBGetGiftLog.Builder builder = PBGetGiftLog.newBuilder();
		try {
			for (byte[] logInfo : logInfos) {
				PBGuildGiftLog.Builder logBuilder = PBGuildGiftLog.newBuilder().mergeFrom(logInfo);
				builder.addLogInfo(logBuilder);
			}
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_SMAIL_GIFT_GET_LOG_S, builder));

	}

	/**
	 * 删除礼物
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_SMAIL_GIFT_DEL_REWARD_C_VALUE)
	private void onDelReward(HawkProtocol protocol) {
		PBDeleteGiftRequest req = protocol.parseProtocol(PBDeleteGiftRequest.getDefaultInstance());
		List<String> giftIdlist = req.getIdsList();
		List<PlayerGuildGiftEntity> giftList = player.getData().getGuildGiftEntity().stream().filter(gift -> giftIdlist.contains(gift.getId()))
				.collect(Collectors.toList());
		getPlayerData().getGuildGiftEntity().removeAll(giftList);
		giftList.forEach(PlayerGuildGiftEntity::delete);

		player.responseSuccess(protocol.getType());

	}

	/**
	 * 礼物列表
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_SMAIL_GIFT_LIST_C_VALUE)
	private void onListGift(HawkProtocol protocol) {
		long now = HawkTime.getMillisecond();
		PBPlayerGuildGiftResp.Builder resp = PBPlayerGuildGiftResp.newBuilder();
		for (PlayerGuildGiftEntity gift : getPlayerData().getGuildGiftEntity()) {
			if (gift.getGiftOverTime() > now) {
				resp.addGiftList(gift.toPbObj());
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_SMAIL_GIFT_LIST_S, resp));
	}

}
