package com.hawk.game.module.nationMilitary;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.nationMilitary.cfg.NationMilitaryCfg;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.module.nationMilitary.rank.MilitaryRank;
import com.hawk.game.module.nationMilitary.rank.NationMilitaryRankObj;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Rank.NationMilitaryRankResp;
import com.hawk.game.protocol.Reward.PBNationMilitaryLevelUpInfo;
import com.hawk.game.protocol.Reward.PBNationMilitaryLevelUpReward;
import com.hawk.game.protocol.Reward.PBNationMilitaryRankResetReward;
import com.hawk.game.protocol.Reward.PBNationMilitaryResetReward;
import com.hawk.game.protocol.Reward.RewardInfo;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class PlayerNationMilitaryModule extends PlayerModule {

	public PlayerNationMilitaryModule(Player player) {
		super(player);
	}

	private long lastCheckTime = 0;

	@Override
	protected boolean onPlayerLogin() {
		return super.onPlayerLogin();
	}

	@Override
	public boolean onTick() {
		check();
		return true;
	}

	private void check() {

		NationMilitaryEntity commanderEntity = player.getData().getNationMilitaryEntity();
		long currentTime = HawkApp.getInstance().getCurrentTime();
		if (currentTime - lastCheckTime < 30_000 && !commanderEntity.getNeedCheck().get()) {
			return;
		}
		lastCheckTime = currentTime;

		nativeCheck(commanderEntity);
		nomoCheck(commanderEntity);

		if (commanderEntity.getNeedCheck().get()) {
			player.getPush().syncPlayerInfo();
			commanderEntity.getNeedCheck().set(false);
		}

	}

	/**通用检查*/
	private void nomoCheck(NationMilitaryEntity commanderEntity) {
		if (commanderEntity.getNationMilitaryRewardDay() != HawkTime.getYearDay()) { // 如果跨天了
			commanderEntity.setNationMilitaryRewardDay(HawkTime.getYearDay());
			commanderEntity.setNationMilitaryReward(0);
		}
	}

	/** 只检查本地玩家*/
	private void nativeCheck(NationMilitaryEntity commanderEntity) {
		if (player.isCsPlayer() || !player.isActiveOnline()) {
			return;
		}  
		CrossActivityService service = CrossActivityService.getInstance();
		if (commanderEntity.getCrossTermId() != service.getTermId()) {
			commanderEntity.setNationMilitaryBattleExp(0);
			commanderEntity.setCrossTermId(service.getTermId());
		}
		if(commanderEntity.getNationMilitarLlevel() == 0){
			commanderEntity.setNationMilitarLlevel(calNationMilitaryLevel());
			commanderEntity.setNationMilitaryResetTerm(NationMilitaryRankObj.getInstance().getResetTerm());
		}
		// 检查重置
		if (NationMilitaryRankObj.getInstance().getResetTerm() > commanderEntity.getNationMilitaryResetTerm()) {
			// 3. 军功重置时，需要给玩家发送邮件， 告知玩家之前的军衔等级，重置后的军功值，军衔等级分别是多少。邮件ID如下：
			int oldExp  = commanderEntity.getNationMilitaryExp();
			int oldLevel = commanderEntity.getNationMilitarLlevel();

			NationMilitaryCfg curlevelCfg = HawkConfigManager.getInstance().getConfigByKey(NationMilitaryCfg.class, commanderEntity.getNationMilitarLlevel());
			String resetReward = curlevelCfg.getResetReward();
			commanderEntity.setNationMilitaryExp(curlevelCfg.getResetMerit());
			commanderEntity.setNationMilitaryResetTerm(NationMilitaryRankObj.getInstance().getResetTerm());
			commanderEntity.setNationMilitarLlevel(calNationMilitaryLevel());

			NationMilitaryRankObj.getInstance().zaddScore(player);
			int exp = commanderEntity.getNationMilitaryExp();
			int level = commanderEntity.getNationMilitarLlevel();

//			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
//					.setPlayerId(player.getId())
//					.setMailId(MailId.NATION_MILITARY_RESET)
//					.setAwardStatus(MailRewardStatus.GET)
//					.addContents(oldExp, oldLevel, exp, level)
//					.setRewards(resetReward)
//					.build());
			resetReward(player, resetReward, oldExp, oldLevel, exp, level);
		}
		// 检查等级
		int calNationMilitaryLevel = calNationMilitaryLevel();
		int oldlevel = commanderEntity.getNationMilitarLlevel();
		if (oldlevel != calNationMilitaryLevel) {
			commanderEntity.setNationMilitarLlevel(calNationMilitaryLevel);
		}
		NationMilitaryCfg preLevelExpCfg = HawkConfigManager.getInstance().getConfigByKey(NationMilitaryCfg.class, oldlevel);
		NationMilitaryCfg levelExpCfg = HawkConfigManager.getInstance().getConfigByKey(NationMilitaryCfg.class, calNationMilitaryLevel);
		
		
		if (commanderEntity.getNationMilitaryRankTerm() != NationMilitaryRankObj.getInstance().getDingBangTerm() && (preLevelExpCfg.getAdvanced() > 0 || levelExpCfg.getAdvanced() > 0)) {
			commanderEntity.setNationMilitaryRankTerm(NationMilitaryRankObj.getInstance().getDingBangTerm());
			rankResetReward(player, oldlevel, calNationMilitaryLevel, commanderEntity.getNationMilitaryExp());
		} else if (oldlevel < calNationMilitaryLevel) {
			levelUpReward(player, oldlevel, calNationMilitaryLevel);
		}

	}

	/**计算当前应处等级*/
	private int calNationMilitaryLevel() {
		NationMilitaryEntity commanderEntity = player.getData().getNationMilitaryEntity();
		Optional<MilitaryRank> rankOp = NationMilitaryRankObj.getInstance().getPlayerMilitaryRank(player.getId());
		if (commanderEntity.getNationMilitaryResetTerm() == NationMilitaryRankObj.getInstance().getResetTerm() && rankOp.isPresent()) {
			return rankOp.get().getCfgId();
		}

		int result = 0;
		ConfigIterator<NationMilitaryCfg> it = HawkConfigManager.getInstance().getConfigIterator(NationMilitaryCfg.class);
		for (NationMilitaryCfg cfg : it) {
			if (cfg.getAdvanced() > 0) {
				continue;
			}
			if (commanderEntity.getNationMilitaryExp() >= cfg.getMerit()) {
				result = Math.max(result, cfg.getLevel());
			}
		}

		return result;
	}

	/** 国家军功排行榜 */
	@ProtocolHandler(code = HP.code2.NATION_MILITARY_RANK_REQ_VALUE)
	private void onNationMilitaryRank(HawkProtocol protocol) {
		NationMilitaryRankResp.Builder resp = NationMilitaryRankResp.newBuilder();
		resp.addAllRankList(NationMilitaryRankObj.getInstance().getSortedRank());
		resp.setSelfRankInfo(NationMilitaryRankObj.getInstance().getRankCollection().buildRankInfo(player));

		resp.addAllRankListTwo(NationMilitaryRankObj.getInstance().getSortedRankTwo());
		resp.setSelfRankInfoTwo(NationMilitaryRankObj.getInstance().getRankCollectionTwo().buildRankInfo(player));

		resp.setNextResetTime(NationMilitaryRankObj.getInstance().getNextResetTime());
		resp.setNextDingBangTime(NationMilitaryRankObj.getInstance().getNextDingBangTime());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATION_MILITARY_RANK_RESP, resp));
	}

	/** 国家军功日奖励 */
	@ProtocolHandler(code = HP.code2.NATION_MILITARY_DAILY_RAWARD_VALUE)
	private void onGetDayReward(HawkProtocol protocol) {
		NationMilitaryEntity nationMilitaryEntity = player.getData().getNationMilitaryEntity();
		if (nationMilitaryEntity.getNationMilitaryReward() > 0) {
			player.sendError(protocol.getType(), Status.Error.CONTINUE_RECHARGE_AWARD_RECEIVED, 0);
			return;
		}

		NationMilitaryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationMilitaryCfg.class, nationMilitaryEntity.getNationMilitarLlevel());
		if (Objects.isNull(cfg) || StringUtils.isEmpty(cfg.getDailyReward())) {
			return;
		}
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(ItemInfo.valueListOf(cfg.getDailyReward()));
		awardItem.rewardTakeAffectAndPush(player, Action.NATION_MILITARY_REWARD,RewardOrginType.ITEM_BOX);

		nationMilitaryEntity.setNationMilitaryReward(cfg.getLevel());
		player.getPush().syncPlayerInfo();
		player.responseSuccess(protocol.getType());
	}

	public boolean resetReward(Player player, String resetReward, int oldExp, int oldLevel, int nationMilitary, int newlevel) {
		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(ItemInfo.valueListOf(resetReward));
		RewardInfo.Builder rewrdInfo = award.rewardTakeAffectAndPush(player, Action.NATION_MILI_RESET, true, RewardOrginType.COMMANDER_REWARD);

		PBNationMilitaryResetReward.Builder builder = PBNationMilitaryResetReward.newBuilder();
		builder.setBeforeNationMilitary(oldExp);
		builder.setBeforeLevel(oldLevel);
		builder.setRewardInfo(rewrdInfo == null ? RewardInfo.newBuilder() : rewrdInfo);
		builder.setLevel(newlevel);
		builder.setNationMilitary(nationMilitary);
		builder.setResetTerm(NationMilitaryRankObj.getInstance().getResetTerm());
		builder.setRankTerm(NationMilitaryRankObj.getInstance().getDingBangTerm());
		HawkLog.logPrintln("PlayerNationMilitaryResetRewardInvoker player : {} level: {}", player.getId(), newlevel);

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATION_MILITARY_RESET_RAWARD, builder));
		return true;
	}

	public boolean rankResetReward(Player player, int oldLevel, int newlevel, int nationMilitary) {

		PBNationMilitaryRankResetReward.Builder builder = PBNationMilitaryRankResetReward.newBuilder();
		builder.setBeforeLevel(oldLevel);
		builder.setLevel(newlevel);
		builder.setNationMilitary(nationMilitary);
		builder.setResetTerm(NationMilitaryRankObj.getInstance().getResetTerm());
		builder.setRankTerm(NationMilitaryRankObj.getInstance().getDingBangTerm());
		HawkLog.logPrintln("PlayerNationMilitaryRankResetRewardInvoker player : {} level: {}", player.getId(), newlevel);

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATION_MILITARY_RANK_RESET_RAWARD, builder));
		return true;
	}
	
	public boolean levelUpReward(Player player, int oldLevel, int newlevel) {

		PBNationMilitaryLevelUpInfo.Builder levelUpBuilder = PBNationMilitaryLevelUpInfo.newBuilder();

		PBNationMilitaryLevelUpReward.Builder builder = PBNationMilitaryLevelUpReward.newBuilder();
		builder.setBeforeLevel(oldLevel);
		builder.setLevel(newlevel);
		levelUpBuilder.addLevelUpInfo(builder);
		HawkLog.logPrintln("PlayerNationMilitaryLevelUp player : {} level: {}", player.getId(), newlevel);

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATION_MILITARY_LEVEL_UP_RAWARD, levelUpBuilder));
		return true;
	}
}
