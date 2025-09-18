package com.hawk.game.module.hospice;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;
import org.hawk.xid.HawkXID;

import com.google.common.base.Splitter;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.config.AllianceCareCfg;
import com.hawk.game.entity.GuildHospiceEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.GuildHelpInfo;
import com.hawk.game.guild.manor.building.GuildManorWarehouse;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Chat.HPPushChat;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.Counterattack.PBGuildHospiceMarch;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.PBGuildHospiceMail;
import com.hawk.game.protocol.Mail.PBHospiceHelper;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.AttackMarchReportPB;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.log.Action;
import com.hawk.log.Source;

/** 行军价段 */
public class HospiceStateMARCH implements IHospiceState {

	Map<String, Long> marchMap = new HashMap<>();

	@Override
	public String name() {
		return IHospiceState.State.MARCH.name();
	}

	@Override
	public void playerLogin(Player player, HospiceObj hospiceObj) {
		syncInfo(player, hospiceObj);
	}

	@Override
	public void syncInfo(Player player, HospiceObj hospiceObj) {
		GuildHospiceEntity dbEntity = hospiceObj.getGuildHospiceEntity();
		long now = HawkTime.getMillisecond();
		if (dbEntity.getMatchEndTime() < now) {
			return;
		}
		List<String> helpers = Splitter.on("|").omitEmptyStrings().splitToList(dbEntity.getHelpers());
		PBGuildHospiceMarch.Builder build = PBGuildHospiceMarch.newBuilder();
		// 生成默认的一条
		{
			int pointId = 0;
			GuildManorWarehouse wareHouse = GuildManorService.getInstance().getBuildingByTypeAndIdx(player.getGuildId(), 1, TerritoryType.GUILD_STOREHOUSE);
			if (Objects.nonNull(wareHouse) && wareHouse.getPositionId() != 0) {
				pointId = wareHouse.getPositionId();
			}
			if (pointId == 0) {
				Integer bas = GuildManorService.getInstance().getGuildManorPointId(player.getGuildId());
				if (Objects.nonNull(bas)) {
					pointId = bas.intValue();
				}
			}
			if (pointId == 0) {
				String owerId = GuildService.getInstance().getGuildLeaderId(player.getGuildId());
				if (Objects.nonNull(owerId) && !Objects.equals(owerId, player.getId())) {
					pointId = WorldPlayerService.getInstance().getPlayerPos(owerId);
				}
			}
			if (pointId == 0) {
				List<Integer> superWeaponPoints = SuperWeaponService.getInstance().getSuperWeaponPoints();
				if (!superWeaponPoints.isEmpty()) {
					pointId = superWeaponPoints.get(0);
				}
			}

			NpcPlayer helper = new NpcPlayer(HawkXID.nullXid());
			helper.setPlayerId("");
			helper.setPlayerPos(pointId);

			HawkTuple2<WorldMarchPB, AttackMarchReportPB> result = abcdefg(player, helper, dbEntity.getMatchStartTime(), dbEntity.getMatchEndTime());

			build.addXingjun(result.first);
			build.addLeida(result.second);
		}

		// 一人一条
		for (String hid : helpers) {
			Player help = GlobalData.getInstance().makesurePlayer(hid);
			if (Objects.nonNull(help) && marchMap.containsKey(hid) && GuildService.getInstance().isInTheSameGuild(player.getId(), help.getId())) {
				HawkTuple2<WorldMarchPB, AttackMarchReportPB> result = abcdefg(player, help, marchMap.get(hid), dbEntity.getMatchEndTime());
				build.addXingjun(result.first);
				build.addLeida(result.second);
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_HOSPICE_SYNC, build));

	}

	private HawkTuple2<WorldMarchPB, AttackMarchReportPB> abcdefg(Player player, Player helper, long startTime, long endTime) {
		TemporaryMarch asmarch = new TemporaryMarch();
		asmarch.setOrigionId(helper.getPlayerPos());
		asmarch.setTerminalId(player.getPlayerPos());
		asmarch.setPlayer(helper);
		asmarch.setStartTime(startTime);
		asmarch.setEndTime(endTime);
		asmarch.setMarchId(helper.getId());
		asmarch.setMarchType(WorldMarchType.GUILD_HOSPICE);

		WorldMarchPB.Builder march = asmarch.toBuilder();

		AttackMarchReportPB.Builder attReportBuilder = asmarch.assembleEnemyMarchInfo(player, Collections.emptySet());
		attReportBuilder.setMarchStartTime(startTime);
		attReportBuilder.setArrivalTime(endTime);
		attReportBuilder.setOriginalX(asmarch.getOrigionX());
		attReportBuilder.setOriginalY(asmarch.getOrigionY());
		HawkTuple2<WorldMarchPB, AttackMarchReportPB> result = HawkTuples.tuple(march.build(), attReportBuilder.build());
		return result;
	}

	@Override
	public void tick(Player player, HospiceObj hospiceObj) {
		GuildHospiceEntity dbEntity = hospiceObj.getGuildHospiceEntity();
		long now = HawkTime.getMillisecond();
		if (StringUtils.isNotEmpty(dbEntity.getHelpQueue())) {
			GuildHelpInfo helpInfo = GuildService.getInstance().getGuildHelpInfo(player.getGuildId(), dbEntity.getHelpQueue());
			if (Objects.nonNull(helpInfo)) {
				// 获取玩家队列信息
				List<String> helpers = helpInfo.getHelpers();
				String helperStr = String.join("|", helpers);
				if (!Objects.equals(dbEntity.getHelpers(), helperStr)) {
					for (String helperId : helpers) {
						if (!marchMap.containsKey(helperId)) {
							marchMap.put(helperId, now);
							Player help = GlobalData.getInstance().makesurePlayer(helperId);
							HawkTuple2<WorldMarchPB, AttackMarchReportPB> result = abcdefg(player, help, now, dbEntity.getMatchEndTime());
							PBGuildHospiceMarch.Builder build = PBGuildHospiceMarch.newBuilder();
							build.addXingjun(result.first);
							build.addLeida(result.second);
							player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_HOSPICE_SYNC, build));
						}
					}
					dbEntity.setHelpers(helperStr);
				}
			}

		}

		if (dbEntity.getMatchEndTime() > now) {
			return;
		}
		List<String> helpers = Splitter.on("|").omitEmptyStrings().splitToList(dbEntity.getHelpers());
		AllianceCareCfg comCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCareCfg.class, player.getCityLevel());
		double per = 1 + Math.min(helpers.size() * comCfg.getAllianceMemberUpLimit(), comCfg.getMaxPercent());
		AwardItems awards = AwardItems.valueOf();
		if (StringUtils.isNotEmpty(dbEntity.getAwards())) {
			List<ItemInfo> awardList = ItemInfo.valueListOf(dbEntity.getAwards());
			awardList.stream().forEach(item -> item.setCount((int) (item.getCount() * per)));
			awards.addItemInfos(awardList);
		}
		// 额外道具给一波
		if (comCfg.nonRewardCheckCondition1Item(player)) {
			awards.addItemInfos(ItemInfo.valueListOf(comCfg.getCompensationItem1()));
		}

		if (comCfg.nonRewardCheckCondition2Item(player)) {
			awards.addItemInfos(ItemInfo.valueListOf(comCfg.getCompensationItem2()));
		}
		List<ItemInfo> toSend = awards.getAwardItems();

		PBGuildHospiceMail.Builder content = PBGuildHospiceMail.newBuilder()
				.setPfIcon(player.getPfIcon())
				.setIcon(player.getIcon())
				.setName(player.getName())
				.setGuildTag(player.getGuildTag())
				.setRewards(ItemInfo.toString(toSend))
				.setBattleTime(dbEntity.getMatchStartTime())
				.setOverwhelming(dbEntity.getOverwhelming());
		
		Player attacker = GlobalData.getInstance().makesurePlayer(dbEntity.getAttackerId());
		if (attacker != null) {
			content.setEpfIcon(attacker.getPfIcon())
					.setEicon(attacker.getIcon())
					.setEname(attacker.getName())
					.setEguildTag(attacker.getGuildTag());
		}

		for (String hid : helpers) {
			Player help = GlobalData.getInstance().makesurePlayer(hid);
			if (help != null) {
				PBHospiceHelper pbHelper = PBHospiceHelper.newBuilder()
						.setPfIcon(help.getPfIcon())
						.setIcon(help.getIcon())
						.setName(help.getName()).build();
				content.addHelpers(pbHelper);
			}
		}

		MailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.DEAD_SOLDIER_RESOURCE_BUCHANG)
				.addRewards(toSend)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.addContents(content)
				.build());

		dbEntity.setMaxPower(100);
		dbEntity.setLostPower(0);
		dbEntity.setOverwhelming(0);
		dbEntity.setHelpers("");
		dbEntity.setAwards("");
		hospiceObj.setState(player, IHospiceState.valueOf(State.CONGEST));

		ChatParames parames = ChatParames.newBuilder()
				.setChatType(Const.ChatType.SPECIAL_BROADCAST)
				.setKey(Const.NoticeCfgId.ALLIANCE_CARE)
				.addParms(player.getName())
				.build();
		HPPushChat.Builder builder = HPPushChat.newBuilder();
		builder.addChatMsg(parames.toPBMsg());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_CHAT_S_VALUE, builder));

		// 行为日志
		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.GUILD_HOSPICE_SEND,
				Params.valueOf("items", dbEntity.getAwards()),
				Params.valueOf("LostPower", dbEntity.getLostPower()),
				Params.valueOf("helpNum", helpers.size()),
				Params.valueOf("sendItem", ItemInfo.toString(toSend)));
	}

}
