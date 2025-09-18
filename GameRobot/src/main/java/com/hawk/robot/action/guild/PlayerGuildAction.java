package com.hawk.robot.action.guild;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.GuildManager.ChangeGuildLevelReq;
import com.hawk.game.protocol.GuildManager.DimiseLeaderReq;
import com.hawk.game.protocol.GuildManager.GetGuildMemeberInfoReq;
import com.hawk.game.protocol.GuildManager.GuildMemeberInfo;
import com.hawk.game.protocol.GuildManager.HPGuildInfoSync;
import com.hawk.game.protocol.GuildManager.HPGuildShopBuyReq;
import com.hawk.game.protocol.GuildManager.HPGuildShopItem;
import com.hawk.game.protocol.GuildManager.HPInviteAddShopItem;
import com.hawk.game.protocol.GuildManager.KickMemberReq;
import com.hawk.game.protocol.GuildScience.DonateType;
import com.hawk.game.protocol.GuildScience.GuildScienceDonateReq;
import com.hawk.game.protocol.GuildScience.GuildScienceInfo;
import com.hawk.game.protocol.GuildScience.GuildScienceResearchReq;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.config.GuildScienceLevelCfg;
import com.hawk.robot.util.GuildUtil;

/**
 * 联盟相关操作
 * @author zhenyu.shang
 */
@RobotAction(valid = true)
public class PlayerGuildAction extends HawkRobotAction {
	/**
	 * 首领随机行为Map
	 */
	private Map<LeaderActionType, Integer> leaderActMap = new HashMap<LeaderActionType, Integer>();
	
	/**
	 * 成员随机行为Map
	 */
	private Map<NorActionType, Integer> norActMap = new HashMap<NorActionType, Integer>();
	
	/**
	 * 无联盟随机行为Map
	 */
	private Map<NoGuildActionType, Integer> noGuildActMap = new HashMap<NoGuildActionType, Integer>();
	
	public PlayerGuildAction() {
		//存在问题,暂时不用解散联盟
//		leaderActMap.put(LeaderActionType.DISS, LeaderActionType.DISS.getRand());
		leaderActMap.put(LeaderActionType.MAKEOVER, LeaderActionType.MAKEOVER.getRand());
		leaderActMap.put(LeaderActionType.KICK, LeaderActionType.KICK.getRand());
		leaderActMap.put(LeaderActionType.ASSIGNEMENT, LeaderActionType.ASSIGNEMENT.getRand());
		leaderActMap.put(LeaderActionType.BUY, LeaderActionType.BUY.getRand());
		leaderActMap.put(LeaderActionType.SUPPLY, LeaderActionType.SUPPLY.getRand());
		leaderActMap.put(LeaderActionType.DONATE, LeaderActionType.DONATE.getRand());
		leaderActMap.put(LeaderActionType.RESEARCH, LeaderActionType.RESEARCH.getRand());
		leaderActMap.put(LeaderActionType.NONE, LeaderActionType.NONE.getRand());
		
		norActMap.put(NorActionType.QUIT, NorActionType.QUIT.getRand());
		norActMap.put(NorActionType.BUY, NorActionType.BUY.getRand());
		norActMap.put(NorActionType.DONATE, NorActionType.DONATE.getRand());
		norActMap.put(NorActionType.NONE, NorActionType.NONE.getRand());
		
		noGuildActMap.put(NoGuildActionType.CREATE, NoGuildActionType.CREATE.getRand());
		noGuildActMap.put(NoGuildActionType.SEARCH_OR_JOIN, NoGuildActionType.SEARCH_OR_JOIN.getRand());
		noGuildActMap.put(NoGuildActionType.NONE, NoGuildActionType.NONE.getRand());
	}
	
	private enum LeaderActionType{
//		DISS(2),  //解散
		MAKEOVER(3),//转让
		KICK(10),//踢人
		ASSIGNEMENT(10),//变权限
		BUY(10),//买东西
		SUPPLY(10),//商店补货
		DONATE(10),//联盟科技捐献
		RESEARCH(10),//联盟科技研究
		NONE(35);//什么都不做
		
		private final int rand;
		
		private LeaderActionType(int rand){
			this.rand = rand;
		}

		public int getRand() {
			return rand;
		}
	}
	
	private enum NorActionType{
		QUIT(10),//退出
		BUY(10),//购买
		DONATE(20),//联盟科技捐献
		NONE(60);//什么都不做
		
		private final int rand;
		
		private NorActionType(int rand){
			this.rand = rand;
		}

		public int getRand() {
			return rand;
		}
	}
	
	private enum NoGuildActionType {
		CREATE(10),//创建联盟
		SEARCH_OR_JOIN(100),//搜索/加入联盟
		NONE(200);//什么都不做

		private final int rand;

		private NoGuildActionType(int rand) {
			this.rand = rand;
		}

		public int getRand() {
			return rand;
		}
	}

	/**
	 * 策略:
	 * 首先判断是否已经有联盟
	 * 无联盟者:
	 * 1. 20%的robot执行创建联盟操作
	 * 2. 80%的执行加入联盟操作
	 * 有联盟者:
	 * 随机执行行为,
	 * 1.如果是盟主或R4
	 *  解散联盟 5%
	 *  转让盟主 5%
	 *  踢出成员 10%
	 *  将非R4成员提成R4 15%
	 *  购买商店物品 10%
	 *  无操作 55%
	 * 2.其他成员
	 *  退出公会 10%
	 *  购买商店物品 10%
	 *  无操作 80%
	 */
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		String guildId = gameRobotEntity.getGuildData().getGuildId();
		if(!HawkOSOperator.isEmptyString(guildId)){
			hasGuildAction(gameRobotEntity, guildId);
		} else {
			notGuildAction(gameRobotEntity);
		}
	}
	
	/**
	 * 有联盟操作
	 * @param gameRobotEntity
	 */
	private void hasGuildAction(GameRobotEntity gameRobotEntity, String guildId){
		HPGuildInfoSync syncInfo = gameRobotEntity.getGuildData().getGuildInfoSync();
		if(syncInfo == null){
			return;
		}
		int power = syncInfo.getGuildAuthority();
		if(power >= Const.GuildAuthority.L4_VALUE){ //R4及盟主操作
			LeaderActionType type = HawkRand.randomWeightObject(leaderActMap);
			switch (type) {
			case ASSIGNEMENT:
				this.changeLevel(gameRobotEntity);
				break;
			case BUY:
				this.buyItem(gameRobotEntity);
				break;
			case SUPPLY:
				this.shopSupply(gameRobotEntity);
				break;
			case DONATE:
				this.scienceDonate(gameRobotEntity);
				break;
			case RESEARCH:
				this.scienceResearch(gameRobotEntity);
				break;
//			case DISS:
//				this.dismiss(gameRobotEntity);
//				break;
			case KICK:
				this.kickMember(gameRobotEntity);
				break;
			case MAKEOVER:
				this.demiseLeader(gameRobotEntity);
				break;
			case NONE:
				break;
			default:
				break;
			}
			RobotLog.guildPrintln("Robot {} is leader, this period Do {} Action", gameRobotEntity.getPuid(), type);
		} else { //普通成员操作
			NorActionType type = HawkRand.randomWeightObject(norActMap);
			switch (type) {
			case BUY:
				this.buyItem(gameRobotEntity);
				break;
			case DONATE:
				this.scienceDonate(gameRobotEntity);
				break;
			case QUIT:
				this.quitGuild(gameRobotEntity);
				break;
			case NONE:
				break;
			default:
				break;
			}
			RobotLog.guildPrintln("Robot {} is normal, this period Do {} Action", gameRobotEntity.getPuid(), type);
		}
	}
	
	/**
	 * 无联盟操作
	 * @param gameRobotEntity
	 */
	private void notGuildAction(GameRobotEntity gameRobotEntity){
		NoGuildActionType type = HawkRand.randomWeightObject(noGuildActMap);
		switch (type) {
		case CREATE:
			if(!WorldDataManager.getInstance().isGuildNumLimit()){
				GuildUtil.createGuild(gameRobotEntity);
			} else {
				GuildUtil.searchOrJoinGuild(gameRobotEntity);
			}
			break;
		case SEARCH_OR_JOIN:
			GuildUtil.searchOrJoinGuild(gameRobotEntity);
			break;
		case NONE:
			break;
		default:
			break;
		}
		RobotLog.guildPrintln("Robot {} is no guild, this period Do {} Action", gameRobotEntity.getPuid(), type);
	}
	
//	/**
//	 * 解散联盟
//	 * @param gameRobotEntity
//	 */
//	private void dismiss(GameRobotEntity gameRobotEntity){
//		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_DISSMISEGUILD_C_VALUE));
//	}
	
	/**
	 * 退出联盟
	 * @param gameRobotEntity
	 */
	private void quitGuild(GameRobotEntity gameRobotEntity){
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_QUIT_C_VALUE));
	}
	
	/**
	 * 购买/通知补货
	 * @param gameRobotEntity
	 */
	private void buyItem(GameRobotEntity gameRobotEntity) {
		List<HPGuildShopItem> shopItems = WorldDataManager.getInstance().getGuildShopItem(gameRobotEntity.getGuildId());
		if (shopItems == null || shopItems.size() == 0) {
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_GET_SHOP_INFO_C_VALUE));
			return;
		}
		Random random = new Random();
		int rand_index = random.nextInt(shopItems.size());
		HPGuildShopItem item = shopItems.get(rand_index);
		
		int itemCount = item.getCount();
		// 存在则购买,否则通知补货
		if (itemCount > 0) {
			int buyCount = random.nextInt(itemCount) + 1;
			HPGuildShopBuyReq.Builder builder = HPGuildShopBuyReq.newBuilder();
			builder.setItemId(item.getItemId());
			builder.setCount(buyCount);
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SHOP_BUY_C_VALUE, builder));
		}
		else{
			HPInviteAddShopItem.Builder builder = HPInviteAddShopItem.newBuilder();
			builder.setItemName(String.valueOf(item.getItemId()));
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_INVITE_ADD_SHOP_ITEM_VALUE, builder));
			
		}
	}
	
	/**
	 * 联盟商店补货
	 * @param gameRobotEntity
	 */
	private void shopSupply(GameRobotEntity gameRobotEntity) {
		List<HPGuildShopItem> shopItems = WorldDataManager.getInstance().getGuildShopItem(gameRobotEntity.getGuildId());
		if (shopItems == null || shopItems.size() == 0) {
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_GET_SHOP_INFO_C_VALUE));
			return;
		}
		int itemId = shopItems.stream().parallel().filter(e -> e != null).findAny().get().getItemId();
		int buyCount = new Random().nextInt(5) + 1;
		HPGuildShopBuyReq.Builder builder = HPGuildShopBuyReq.newBuilder();
		builder.setItemId(itemId);
		builder.setCount(buyCount);
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_ADD_SHOP_ITEM_C_VALUE, builder));
	}
	
	/**
	 * 联盟科技捐献
	 * @param gameRobotEntity
	 */
	private void scienceDonate(GameRobotEntity gameRobotEntity) {
		List<GuildScienceInfo> scienceInfos = WorldDataManager.getInstance().getGuildScienceInfo(gameRobotEntity.getGuildId());
		if (scienceInfos == null || scienceInfos.size() == 0) {
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_GET_INFO_C_VALUE));
			return;
		}
		Optional<GuildScienceInfo> scienceInfo = scienceInfos.stream()
				.parallel()
				.filter(e -> e != null && !isFullDonate(e))
				.findAny();
		
		if(scienceInfo.isPresent()){
			GuildScienceDonateReq.Builder builder = GuildScienceDonateReq.newBuilder();
			builder.setScienceId(scienceInfo.get().getScienceId());
			// 随机捐献类型
			builder.setDonateType(DonateType.valueOf(new Random().nextInt(2)+1));
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_DONATE_C_VALUE, builder));
		}
	}
	
	/**
	 * 联盟科技研究
	 * @param gameRobotEntity
	 */
	private void scienceResearch(GameRobotEntity gameRobotEntity) {
		List<GuildScienceInfo> scienceInfos = WorldDataManager.getInstance().getGuildScienceInfo(gameRobotEntity.getGuildId());
		if (scienceInfos == null || scienceInfos.size() == 0) {
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_GET_INFO_C_VALUE));
			return;
		}
		Optional<GuildScienceInfo> scienceInfo = scienceInfos.stream()
				.filter(e -> e != null && isFullDonate(e) && e.getFinishTime() == 0)
				.findAny();
		if(scienceInfo.isPresent()){
			GuildScienceResearchReq.Builder req = GuildScienceResearchReq.newBuilder();
			req.setScienceId(scienceInfo.get().getScienceId());
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_RESEARCH_C_VALUE, req));
		}
	}
	
	/**
	 * 科技捐献值是否已满
	 * @param scienceInfo
	 * @return
	 */
	private boolean isFullDonate(GuildScienceInfo scienceInfo) {
		GuildScienceLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(GuildScienceLevelCfg.class,
				scienceInfo.getLevel() + 1, scienceInfo.getScienceId());
		return scienceInfo.getDonate() >= cfg.getFullDonate();
	}

	/**
	 * 改变成员等级
	 * @param gameRobotEntity
	 */
	private void changeLevel(GameRobotEntity gameRobotEntity){
		List<GuildMemeberInfo> members = WorldDataManager.getInstance().getGuildMemberInfo(gameRobotEntity.getGuildId());
		if(members == null || members.isEmpty() || members.size() == 1){
			GetGuildMemeberInfoReq.Builder req = GetGuildMemeberInfoReq.newBuilder();
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETMEMBERINFO_C_VALUE, req));
			return;
		}
		GuildMemeberInfo info = HawkRand.randomObject(members);
		if(!info.getPlayerId().equals(gameRobotEntity.getPlayerId())){
			ChangeGuildLevelReq.Builder builder = ChangeGuildLevelReq.newBuilder();
			builder.setPlayerId(info.getPlayerId());
			builder.setLevel(HawkRand.randInt(1, 5));
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_CAHNGELEVEL_C_VALUE, builder));
		}
	}
	
	/**
	 * 踢出成员
	 * @param gameRobotEntity
	 */
	private void kickMember(GameRobotEntity gameRobotEntity){
		List<GuildMemeberInfo> members = WorldDataManager.getInstance().getGuildMemberInfo(gameRobotEntity.getGuildId());
		if(members == null || members.isEmpty() || members.size() == 1){
			GetGuildMemeberInfoReq.Builder req = GetGuildMemeberInfoReq.newBuilder();
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETMEMBERINFO_C_VALUE, req));
			return;
		}
		GuildMemeberInfo info = HawkRand.randomObject(members);
		if(!info.getPlayerId().equals(gameRobotEntity.getPlayerId())){
			KickMemberReq.Builder builder = KickMemberReq.newBuilder();
			builder.setPlayerId(info.getPlayerId());
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_KICK_C_VALUE, builder));
		}
	}
	
	/**
	 * 转让盟主
	 * @param gameRobotEntity
	 */
	private void demiseLeader(GameRobotEntity gameRobotEntity){
		List<GuildMemeberInfo> members = WorldDataManager.getInstance().getGuildMemberInfo(gameRobotEntity.getGuildId());
		if(members == null || members.isEmpty() || members.size() == 1){
			GetGuildMemeberInfoReq.Builder req = GetGuildMemeberInfoReq.newBuilder();
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETMEMBERINFO_C_VALUE, req));
			return;
		}
		GuildMemeberInfo info = HawkRand.randomObject(members);
		if(!info.getPlayerId().equals(gameRobotEntity.getPlayerId())){
			DimiseLeaderReq.Builder builder = DimiseLeaderReq.newBuilder();
			builder.setPlayerId(info.getPlayerId());
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_DEMISELEADER_C_VALUE, builder));
		}
	}
}
