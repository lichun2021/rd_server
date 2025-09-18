package com.hawk.game.module;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.config.AllianceBeatbackAdditionmoney;
import com.hawk.game.config.AllianceBeatbackMoneyRewardCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.data.PlayerAddBountyInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.Counterattack;
import com.hawk.game.guild.CounterattackBounty;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Counterattack.PBCounterattackDetailReq;
import com.hawk.game.protocol.Counterattack.PBCounterattackDetailResp.Builder;
import com.hawk.game.protocol.Counterattack.PBCounterattackListResp;
import com.hawk.game.protocol.Counterattack.PBCounterattackRemoveReq;
import com.hawk.game.protocol.Counterattack.PBGoAttackReq;
import com.hawk.game.protocol.Counterattack.PBGoAttackResp;
import com.hawk.game.protocol.Counterattack.PBIncBountyReq;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.Error;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.log.Action;

public class PlayerGuildCounterModule extends PlayerModule {

	public PlayerGuildCounterModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		return super.onTick();
	}

	@Override
	protected boolean onPlayerLogin() {

		return super.onPlayerLogin();
	}

	/**
	 * 反击列表
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.COUNTERATTACK_LIST_C_VALUE)
	private void onReqCounterattackList(HawkProtocol protocol) {

		List<Counterattack> counterList = GuildService.getInstance().guildCounterAttack(player.getGuildId());

		List<Counterattack> tmp = new ArrayList<>();
		for(Counterattack counterattack : counterList){
			Player playersnapshot = GlobalData.getInstance().makesurePlayer(counterattack.getDbObj().getPlayerId());
			Player attackersnapshot = GlobalData.getInstance().makesurePlayer(counterattack.getDbObj().getAtkerId());
			if(playersnapshot != null && attackersnapshot != null){
				tmp.add(counterattack);
			}
		}
		counterList = tmp;
		PBCounterattackListResp.Builder respbul = PBCounterattackListResp.newBuilder();
		counterList.stream()
				.filter(cou -> !cou.hasSendReward())
				.sorted(Comparator.comparingLong(Counterattack::getCreateTime).reversed())
				.limit(ConstProperty.getInstance().getAllianceCareHelpQuantityUpLimit())
				.map(Counterattack::toSummaryPbObj)
				.forEach(respbul::addAttackList);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.COUNTERATTACK_LIST_S, respbul));

	}

	/**
	 * 提升赏金
	 */
	@SuppressWarnings("deprecation")
	@ProtocolHandler(code = HP.code.COUNTERATTACK_INC_BOUNTY_VALUE)
	private void onIncBounty(HawkProtocol protocol) {
		PBIncBountyReq req = protocol.parseProtocol(PBIncBountyReq.getDefaultInstance());
		String uuid = req.getUuid();
		Counterattack counter = GuildService.getInstance().counterAttack(player.getGuildId(), uuid);
		if (Objects.isNull(counter)) {
			return;
		}
		int count = req.getCount();
//		int inced = counter.upBountyTimes(player.getId());// 已用次数
//		Optional<AllianceBeatbackMoneyRewardCfg> cfgOp = HawkConfigManager.getInstance().getConfigIterator(AllianceBeatbackMoneyRewardCfg.class).stream()
//				.filter(cfg -> cfg.getId() <= inced + 1)
//				.sorted(Comparator.comparingInt(AllianceBeatbackMoneyRewardCfg::getId).reversed())
//				.findFirst();
//		if (!cfgOp.isPresent()) {
//			return;
//		}
		AllianceBeatbackMoneyRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceBeatbackMoneyRewardCfg.class, 1);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(cfg.getCostMoney(), count), false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		
		Player attackersnapshot = GlobalData.getInstance().makesurePlayer(counter.getDbObj().getAtkerId());
		if(null == attackersnapshot ){
			return;
		}
		
		if (!checkAndUpdateBountyAddInfo(protocol, uuid, counter, cfg, count, attackersnapshot)) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.INC_GUILDCOUNTER_BOUNTY);
		counter.upBounty(player, ItemInfo.valueListOf(cfg.getAddMoney(), count));
		// 反回详情
		Builder detailPbobj = counter.toDetailPbobj(player.getId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.COUNTERATTACK_DETAIL_S, detailPbobj));

		// 发送联盟消息
		// 聊天悬赏令参数顺序 被悬赏人联盟简称，被悬赏人名字，赏金（三段式）， 被悬赏人头像和平台头像， 总赏金， 悬赏令uuid
		ChatService.getInstance().addWorldBroadcastMsg(ChatType.GUILD_HREF, Const.NoticeCfgId.COUNTERATTACK_UPBOUNTY, player,
				GuildService.getInstance().getGuildTag(attackersnapshot.getGuildId()),
				attackersnapshot.getName(),
				cfg.getAddMoney(),
				attackersnapshot.getIcon(),
				attackersnapshot.getPfIcon(),
				detailPbobj.getBounty(),
				counter.getDbObj().getId());
		
		
	}

	/**
	 *  ID57914246 
	 *  联盟赏金限制
	 */
	private boolean checkAndUpdateBountyAddInfo(HawkProtocol protocol, String uuid, Counterattack counter, AllianceBeatbackMoneyRewardCfg cfg,int multi, Player attackersnapshot) {
		try{
			Map<String, CounterattackBounty> playerBounty = new HashMap<>();
			if(StringUtils.isNotEmpty(counter.playerBountySer())){
				JSONArray arr = JSONArray.parseArray(counter.playerBountySer());
				for(Object str : arr){
					CounterattackBounty bounty = new CounterattackBounty(null);
					bounty.mergeFrom(str.toString());
					playerBounty.put(bounty.getPlayerId(), bounty);
				}
			}
			
			AwardItems allAward = AwardItems.valueOf();
			playerBounty.values().stream().map(CounterattackBounty::getBounty).forEach(bounty -> allAward.addItemInfos(bounty.getAwardItems()));
			List<ItemInfo> allItems = allAward.getAwardItems();
			int count = (int) allItems.stream().mapToLong(ItemInfo::getCount).sum();
			HawkLog.logPrintln("onIncBounty, playerId: {}, uuid: {}", player.getId(), uuid, count);
			if(count >= GuildConstProperty.getInstance().getBeatbackMaxBouns()){
				player.sendError(protocol.getType(), Error.GUILD_BOUNTYADD_ONE_LIMIT, 0);
				return false;
			}
			//我的赏金追加配置
			PlayerAddBountyInfo playerAddBountyCfg = AllianceBeatbackAdditionmoney.getAdditionMoneyByChargeAmount(player.getRechargeTotal()/10);
			//追加人的追加赏金配置
			PlayerAddBountyInfo  playerAddBountyInfo = RedisProxy.getInstance().getPlayerBountyAddInfo( player.getId() );
			if(playerAddBountyInfo.getAddBounty() >= playerAddBountyCfg.getAddBounty() ){
				player.sendError(HP.code.COUNTERATTACK_INC_BOUNTY_VALUE, Error.GUILD_BOUNTYADD_SELF_LIMIT_VALUE, 0);
				return false;
			}
			//被追加人赏金追加信息
			PlayerAddBountyInfo atkPlayerAddBountyCfg =  AllianceBeatbackAdditionmoney.getAdditionMoneyByChargeAmount(attackersnapshot.getRechargeTotal()/10);
			//被追加人的追加赏金信息
			PlayerAddBountyInfo atkPlayerAddBountyInfo = RedisProxy.getInstance().getPlayerBountyAddInfo((String)counter.getAtkerId());
			if( atkPlayerAddBountyInfo.getBeAddBounty() >=  atkPlayerAddBountyCfg.getBeAddBounty()){
				player.sendError(HP.code.COUNTERATTACK_INC_BOUNTY_VALUE, Error.GUILD_BOUNTYADD_ATK_LIMIT_VALUE, 0);
				return false;
			}		
			
			List<ItemInfo> bounty = ItemInfo.valueListOf(cfg.getAddMoney(), multi);
			
			for(ItemInfo item : bounty){
				if(item.getItemId() == PlayerAttr.GOLD_VALUE){
					playerAddBountyInfo.IncAddBounty((int)item.getCount() );
					atkPlayerAddBountyInfo.IncBeAddBounty((int)item.getCount());			
				}
			}
			
			RedisProxy.getInstance().updatePlayerBountyAddInfo(player.getId(), playerAddBountyInfo);
			RedisProxy.getInstance().updatePlayerBountyAddInfo(attackersnapshot.getId(), atkPlayerAddBountyInfo);
			
			//赏金增加
			GuildRankMgr.getInstance().onPlayerBounty(player.getId(), cfg.getAddMoney());
			return true;
		}catch(Exception e){
			HawkException.catchException(e);
			return false;
		}
	}

	@ProtocolHandler(code = HP.code.COUNTERATTACK_GO_ATTACK_C_VALUE)
	private void onCounterattackGoAttack(HawkProtocol protocol) {
		PBGoAttackReq req = protocol.parseProtocol(PBGoAttackReq.getDefaultInstance());
		Counterattack counter = GuildService.getInstance().counterAttack(player.getGuildId(), req.getUuid());

		PBGoAttackResp.Builder respBul = PBGoAttackResp.newBuilder();
		respBul.setTodayBounty(LocalRedis.getInstance().getDayGuildBountyAwd(player.getId()));
		int state = 1;
		if (Objects.isNull(counter)) {
			state = 3;
		} else {
			int attackerPointId = counter.getDbObj().getAttackerPointId();
			if (attackerPointId != WorldPlayerService.getInstance().getPlayerPos(counter.getDbObj().getAtkerId())) {
				state = 2;
			}
			int[] p = GameUtil.splitXAndY(attackerPointId);
			respBul.setX(p[0]).setY(p[1]);
		}

		Player attacker = GlobalData.getInstance().makesurePlayer(counter.getDbObj().getAtkerId());
		Integer gmaId = GuildManorService.getInstance().getGuildManorPointId(attacker.getGuildId());
		if (Objects.nonNull(gmaId)) {
			int[] gmap = GameUtil.splitXAndY(gmaId.intValue());
			respBul.setManorX(gmap[0]);
			respBul.setManorY(gmap[1]);
		}
		respBul.setState(state);

		player.sendProtocol(HawkProtocol.valueOf(HP.code.COUNTERATTACK_GO_ATTACK_S, respBul));

	}

	/**
	 * 官员称除
	 */
	@ProtocolHandler(code = HP.code.COUNTERATTACK_REMOVE_VALUE)
	private void onCounterattackRemove(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCEBEATBACKCLEARRECORD)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return;
		}

		PBCounterattackRemoveReq req = protocol.parseProtocol(PBCounterattackRemoveReq.getDefaultInstance());
		Counterattack counter = GuildService.getInstance().counterAttack(player.getGuildId(), req.getUuid());
		if (Objects.isNull(counter)) {
			player.responseSuccess(protocol.getType());
			return;
		}
		counter.sendBackAllBounty(MailId.COUNTERATTACK_REWARD_CHEXIAO_BACK);
		GuildService.getInstance().counterattackRemove(counter);

		player.responseSuccess(protocol.getType());
	}

	/**
	 * 退出联盟
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onGuildQuitMsg(GuildQuitMsg msg) {
		String guildId = msg.getGuildId();
		List<Counterattack> counters = GuildService.getInstance().guildCounterAttack(guildId);
		for (Counterattack counter : counters) {
			if (Objects.equals(counter.getDbObj().getPlayerId(), player.getId())) {
				counter.sendBackAllBounty(MailId.COUNTERATTACK_REWARD_QUITGUILD_BACK);
				GuildService.getInstance().counterattackRemove(counter);
			}
			// counter.sendBackOneBounty(player.getId(),MailId.COUNTERATTACK_REWARD_QUITGUILD_BACK);
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
		List<Counterattack> counters = GuildService.getInstance().guildCounterAttack(guildId);
		for (Counterattack counter : counters) {
			if (Objects.equals(counter.getDbObj().getAtkerId(), player.getId())) {
				counter.sendBackAllBounty(MailId.COUNTERATTACK_REWARD_CHEXIAO_BACK);
				GuildService.getInstance().counterattackRemove(counter);
			}
		}
		return true;
	}

	@ProtocolHandler(code = HP.code.COUNTERATTACK_DETAIL_C_VALUE)
	private void onCounterattackDetail(HawkProtocol protocol) {
		PBCounterattackDetailReq req = protocol.parseProtocol(PBCounterattackDetailReq.getDefaultInstance());
		Counterattack counter = GuildService.getInstance().counterAttack(player.getGuildId(), req.getUuid());
		if (Objects.isNull(counter)) {
			return;
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.COUNTERATTACK_DETAIL_S, counter.toDetailPbobj(player.getId())));
	}

}
