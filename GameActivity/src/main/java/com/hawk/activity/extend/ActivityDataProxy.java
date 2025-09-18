package com.hawk.activity.extend;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Rank;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.redis.HawkRedisSession;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Table;
import com.hawk.activity.config.MergeServerTimeCfg;
import com.hawk.activity.entity.ActivityAccountRoleInfo;
import com.hawk.activity.entity.PlayerData4Activity;
import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyRankScoreCfg;
import com.hawk.activity.type.impl.commandAcademySimplify.cfg.CommandAcademySimplifyRankScoreCfg;
import com.hawk.activity.type.impl.guildDragonAttack.entity.GuildDragonTrapData;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireRankCfg;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireTargetCfg;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeRankCfg;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeTargetCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoRankCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoTargetCfg;
import com.hawk.activity.type.impl.inherit.BackPlayerInfo;
import com.hawk.activity.type.impl.inheritNew.BackNewPlayerInfo;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.activity.type.impl.redEnvelope.callback.RecieveCallBack;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.ServerInfo;
import com.hawk.game.protocol.Activity.GuardRecordPB;
import com.hawk.game.protocol.Activity.MachineAwakeState;
import com.hawk.game.protocol.Activity.PBDamageRank;
import com.hawk.game.protocol.Activity.PBEmptyModel10Info;
import com.hawk.game.protocol.Activity.PBEmptyModel8Info;
import com.hawk.game.protocol.Activity.PBEmptyModel9Info;
import com.hawk.game.protocol.Activity.PBSpreadBindRoleInfo;
import com.hawk.game.protocol.Activity.SpaceMachineGuardActivityInfoPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOfficerStruct;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfoResp;
import com.hawk.game.protocol.World.PBCakeShare;
import com.hawk.game.protocol.World.PBDragonBoat;
import com.hawk.gamelib.player.PowerData;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.LogConst.PowerChangeReason;
import redis.clients.jedis.Tuple;

/**
 * 数据获取器
 * <pre>
 * 用于获取活动业务所需的一些游戏内数据信息
 * 游戏业务逻辑中请实现该接口的数据获取方法
 * </pre>
 * @author PhilChen
 *
 */
public interface ActivityDataProxy {
	/**
	 * 获取服务器id
	 * @return
	 */
	String getServerId();
	
	/**
	 * 获取服务器类型
	 * @return
	 */
	int getServerType();
	
	/**
	 * 获取本地redis标识
	 * @return
	 */
	String getLocalIdentify();
	
	/**
	 * 获取GM控制状态,是否被GM强制关闭活动
	 * @param activityType
	 * @return
	 */
	boolean isGmClose(ActivityType activityType);
	
	/**
	 * 系统是否初始化完毕
	 * @return
	 */
	boolean isGsInitFinish();
	
	/**
	 * 判断此成就活动是否是酒馆活动（每日任务）
	 * @param achieveId
	 * @return
	 */
	boolean isTavernActivity(int achieveId);
	
	/**
	 * 获取跨天判定时间点
	 * @return
	 */
	int getCrossDayHour();
	
	/**
	 * 获取玩家的建筑工厂等级
	 * @param playerId
	 * @return
	 */
	int getConstructionFactoryLevel(String playerId);

	int getConstructionFactoryCfgId(String playerId);
	
	/**
	 * 获取建筑等级
	 * @param playerId
	 * @param buildType
	 * @return
	 */
	int getBuildMaxLevel(String playerId, int buildType);
	
	/**
	 * 获取玩家客户端版本号
	 * @param playerId
	 * @return
	 */
	String getPlayerVersion(String playerId);

	/**
	 * 获取玩家的角色创建时间
	 * @param playerId
	 * @return
	 */
	long getPlayerCreateTime(String playerId);
	
	/**
	 * 获取玩家的角色创建当天零点的时间
	 * @param playerId
	 * @return
	 */
	long getPlayerCreateAM0Date(String playerId);

	/**
	 * 获取在线玩家列表
	 * @return
	 */
	Set<String> getOnlinePlayers();

	/**
	 * 判断玩家是否在线
	 * @return
	 */
	boolean isOnlinePlayer(String playerId);
	/**
	 * 扣除玩家物品
	 * @param playerId
	 * @param itemList
	 * @param protocolType
	 * @param behaviorAction
	 * @return					是否扣除成功
	 */
	boolean consumeItems(String playerId, List<RewardItem.Builder> itemList, int protocolType, Action action);
	/**消耗道具不足部分道具补充*/
	boolean consumeItemsIsGold(String playerId, List<RewardItem.Builder> itemList, int protocolType, Action action);
	
	/**
	 * 消耗金币
	 */
	boolean consumeGold(String playerId, int goldCount, int protocolType, Action action);
	
	/**
	 * 获取玩家渠道id
	 * @param playerId
	 * @return
	 */
	String getPlayerChannelId(String playerId);
	
	/**
	 * 获取玩家渠道信息
	 * @param playerId
	 * @return
	 */
	String getPlayerChannel(String playerId);

	/**
	 * 获取服务器大区信息
	 * @return
	 */
	String getAreaId();
	/**
	 * 获取玩家登录天数
	 * @param playerId
	 * @return
	 */
	int getLoginDays(String playerId);
	
	/**
	 * 获取玩家指挥官等级
	 * @param playerId
	 * @return
	 */
	int getPlayerLevel(String playerId);

	/**
	 * 获取玩家vip等级
	 * @param playerId
	 * @return
	 */
	int getVipLevel(String playerId);
	
	/**
	 * 获取玩家指定类型的建筑数量
	 * @param playerId
	 * @param buildType
	 * @return
	 */
	int getBuildingNum(String playerId, int buildType);

	/**
	 * 获取玩家资源产出率
	 * @param playerId
	 * @param resType
	 * @return
	 */
	long getResourceOutputRate(String playerId, int resType);
	
	/**
	 * 获取玩家士兵数量
	 * @param playerId
	 * @param armyId
	 * @return
	 */
	int getSoldierHaveNum(String playerId, int armyId);

	/**
	 * 获取开服时间
	 * @return
	 */
	long getServerOpenDate();
	
	/**
	 * 获取开服当天零点时间
	 * @return
	 */
	long getServerOpenAM0Date();
	
	String getItemAward(int awardId); 
	/**
	 * 
	 * @param playerId
	 * @param rewardId
	 * @param num
	 * @param action
	 * @param isPop 是否需要弹窗
	 */
	void takeReward(String playerId, int rewardId, int num, Action action, boolean isPop);

	/**
	 *
	 * @param playerId
	 * @param rewardId
	 * @param num
	 * @param action
	 * @param rewardType
	 * @param isPop
	 */
	void takeReward(String playerId, int rewardId, int num, Action action,
					RewardOrginType rewardType, boolean isPop);

	/**
	 * 固定奖励为物品三段式，固定物品奖励堆叠，如果num参数大于1，
	 * 进行多次随机奖励时，每次随机的重复物品不堆叠
	 * @param playerId
	 * @param fixItem 固定物品奖励三段式，此物品堆叠
	 * @param rewardId 如果num参数大于1，进行多次随机奖励时，每次随机的重复物品不堆叠
	 * @param num
	 * @param action
	 * @param rewardType
	 * @param isPop
	 */
	void takeRewardWithFixItem(String playerId, String fixItem, int rewardId, int num, Action action,
					RewardOrginType rewardType, boolean isPop);
	/**
	 *
	 * @param playerId
	 * @param rewardId
	 * @param num
	 * @param action
	 * @param isPop 是否需要弹窗
	 */
	List<RewardItem.Builder> takeRewardReturnItemlist(String playerId, int rewardId, int num,
													  Action action, boolean isPop);

	/**
	 * 非通用,给在残卷活动中 各种操作中会掉落物品 通过邮件发送
	 * @param playerId
	 * @param rewardId
	 * @param num
	 * @param action
	 * @param cfg
	 */
	void takeReward(String playerId, int rewardId, int num, Action action, int mailid, String content, String activityName);
	
	void takeReward(String playerId, int rewardId, int num, Action action, int mailid, Object[] title, Object[] subTitle, Object[] content);
	
	/**
	 * 非通用.
	 * @param playerId
	 * @param rewardId
	 * @param num
	 * @param action
	 * @param mailid
	 * @param content
	 * @param activityName
	 * @param getStatus
	 */
	void takeReward(String playerId, int rewardId, int num, Action action, int mailid, String content, String activityName, boolean isGet, Map<Integer, Integer> map);
	/**
	 * 发奖
	 * @param playerId
	 * @param itemList
	 * @param action
	 * @param isPop
	 */
	void takeReward(String playerId, List<RewardItem.Builder> itemList, Action action, boolean isPop);

	void takeRewardAuto(String playerId, RewardItem.Builder itemInfo, int multi,
					Action action, boolean isPop, RewardOrginType... originType);
	
	int getItemNum(String playerId, int itemId);
	/**
	 * 扣除物品
	 * @param playerId
	 * @param itemList
	 * @param action
	 * @param isGold
	 * @param 倍数
	 */
	boolean cost(String playerId, List<RewardItem.Builder> itemList, int multi, Action action, boolean isGold);
	boolean cost(String playerId, List<RewardItem.Builder> itemList,  Action action);
	/**
	 * 发奖的时候奖励翻倍
	 * @param playerId
	 * @param itemList
	 * @param multi
	 * @param activityAction
	 * @param isPop
	 */
	void takeReward(String playerId, List<com.hawk.game.protocol.Reward.RewardItem.Builder> itemList, int multi,
			Action activityAction, boolean isPop, RewardOrginType... orginType);
	
	/**
	 * 获取玩家名字
	 * @param playerId
	 * @return
	 */
	String getPlayerName(String playerId);
	
	/**
	 * 获取玩家的个保法开关信息
	 * @param playerId
	 * @return
	 */
	List<Integer> getPersonalProtectVals(String playerId);

	/**
	 * 获取玩家联盟名字
	 * @param playerId
	 * @return
	 */
	String getGuildNameByByPlayerId(String playerId);
	
	/**
	 * guildTag
	 * @param guildId
	 * @return
	 */
	String getGuildTagByPlayerId(String playerId);
	
	/**
	 * 获取联盟名称
	 * @param guildId
	 * @return
	 */
	String getGuildName(String guildId);

	/**
	 * 获取ids
	 */
	List<String> getGuildIds();

	/**
	 * 获取联盟简称
	 * @param guildId
	 * @return
	 */
	String getGuildTag(String guildId);
	
	/**
	 * 获取联盟盟主名字
	 * 
	 * @param guildId
	 * @return
	 */
	String getGuildLeaderName(String guildId);
	
	/**
	 * 获取联盟盟主的id
	 * @param guildId
	 * @return
	 */
	String getGuildLeaderId(String guildId);
	
	/**
	 * 获取联盟旗帜
	 * @param guildId
	 * @return
	 */
	int getGuildFlag(String guildId);
	
	/**
	 * 获取playerId联盟ID
	 */
	String getGuildId (String playerId);
	
	/**
	 * 获取联盟成员id
	 * @param guildId
	 * @return
	 */
	Collection<String> getGuildMemberIds(String guildId);
	
	
	/**获取入盟时间
	 * @param playerId
	 * @return
	 */
	long getJoinGuildTime(String playerId);
	/**
	 * 获取在线联盟成员id
	 * @param guildId
	 * @return
	 */
	List<String> getOnlineGuildMemberIds(String guildId);
	
	/**
	 * 获取联盟的去兵战力
	 * @param playerId
	 * @return
	 */
	long getGuildNoArmyPower(String guildId);

	/**
	 * 邀请目标玩家入盟
	 * @param playerId
	 * @param targetPlayerId
	 * @return
	 */
	int invitePlayerJoinGuild(String playerId, String targetPlayerId);
	/**
	 * 获取玩家的战力数据
	 * @param playerId
	 * @return
	 */
	PowerData getPowerData(String playerId);
	
	
	/**获取冠军霸主信息
	 * @return
	 */
	StarWarsOfficerStruct getWorldKing();

	/**
	 * 判断item id是否存在
	 * @return
	 */
	public boolean isExistItemId(int id);
	
	/**
	 * 判断awardId是否存在
	 * @return
	 */
	public boolean isExistAwardId(int id);
	
	/**
	 * 给玩家推送协议
	 */
	public boolean sendProtocol(String playerId, HawkProtocol protocol);
	
	/**
	 * 给玩家发送邮件
	 * @param playerId
	 * @param mailId
	 * @param title
	 * @param subTitle
	 * @param content
	 * @param items
	 * @param isGetReward
	 * @return
	 */
	public boolean sendMail(String playerId, MailId mailId, Object[] title, Object[] subTitle, Object[] content, List<RewardItem.Builder> items, boolean isGetReward);
	
	public void addWorldBroadcastMsg(ChatType chatType, Const.NoticeCfgId key, String playerId, Object... parms);
	
	public void addWorldBroadcastMsg(ChatType chatType, String guildId, NoticeCfgId key, String playerId, Object... parms);
	
	/**
	 * 记录点击领取活动奖励事件
	 * 
	 * @param playerId
	 * @param btn          点击按钮类型
	 * @param activityType 活动类型
	 * @param cellItemId   子活动Cell按钮Id
	 */
	public void recordActivityRewardClick(String playerId, ActivityBtns btn, ActivityType activityType, int cellItemId);
	
	/**
	 * 记录月卡购买事件
	 * 
	 * @param playerId
	 * @param cardId 月卡Id
	 * @param renew 是否是续费购买
	 */
	public void buyMonthCardRecord(String playerId, int cardId, boolean renew, long validEndTime);
	
	/**
	 * 激活月卡条件判断
	 * 
	 * @param playerId
	 * @param cardType
	 * @return
	 */
	public boolean monthCardFrontBuildCheck(String playerId, int cardType);
	
	/**
	 * 特权卡半价检测
	 * @param playerId
	 * @param cardType
	 * @return
	 */
	public int monthCardGoldPrivilegeCheck(String playerId, int cardType, String payGiftId, boolean paySuccess);
	
	/**
	 * 记录基金购买事件
	 * @param playerId
	 * @param type 活动类型
	 */
	public void buyFundRecord(String playerId, ActivityType type);
	
	/**
	 * 记录最强指挥官积分变动事件
	 * @param playerId
	 * @param rankType	排行类型(阶段/总排行)
	 * @param stageId	阶段id
	 * @param score		积分
	 */
	public void strongestLeaderScoreRecord(String playerId, ActivityRankType rankType, int stageId, long score);
	
	
	/**
	 * 记录王者联盟个人积分变动事件
	 * @param playerId
	 * @param rankType	排行类型(0/1  阶段/总排行)
	 * @param termId	期数
	 * @param stageId	阶段id
	 * @param score		积分
	 */
	public void strongestGuildScoreRecord(String playerId, int rankType, int termId, int stageId, long score);
	
	/**
	 * 记录盟军宝藏抽奖事件
	 * @param playerId
	 * @param type
	 * @param lucky
	 * @param isMulti
	 * @param causeMulti
	 * @param results
	 * @param multis
	 */
	public void lotteryDrawRecord(String playerId, int type, int lucky, boolean isMulti, boolean causeMulti, String results, String multis);
	
	/**
	 * 是否已经首充过
	 * @param playerId
	 * @return
	 */
	public boolean hasAlreadyFirstRecharge(String playerId);
	/**
	 * 添加buff
	 * @param playerId
	 * @param buffId
	 * @param endTime
	 * 
	 */
	public void addBuff(String playerId, int buffId, long endTime);
	/**
	 * 获取hellfireTarget组装后的数据
	 * @return
	 */
	public Table<Integer, Integer, List<ActivityHellFireTargetCfg>> getHellFireTargetCfgTable();	
	/**
	 *地狱火 2
	 * @return
	 */
	public Table<Integer, Integer, List<ActivityHellFireTwoTargetCfg>> getHellTwoTargetCfgTable();
	/**
	 *地狱火 2
	 * @return
	 */
	public Table<Integer, Integer, List<ActivityHellFireThreeTargetCfg>> getHellThreeTargetCfgTable();
	
	/**
	 * 获取rank list
	 * @return
	 */
	public Map<Integer, List<ActivityHellFireRankCfg>> getHellFireRankMap();
	/**
	 * 地狱火 2
	 * @return
	 */
	public Map<Integer, List<ActivityHellFireTwoRankCfg>> getHellFireTwoRankMap();
	/**
	 * 地狱火三
	 * @return
	 */
	public Map<Integer, List<ActivityHellFireThreeRankCfg>> getHellFireThreeRankMap();
	/**
	 * 从GameServer工程组装数据给Activity工程用
	 * @param playerId
	 * @return
	 */
	public PlayerData4Activity getPlayerData4Activity(String playerId);
	
	/**
	 * 刷新战斗力
	 * 
	 * @param playerId
	 */
	public void refreshPower(String playerId, PowerChangeReason reason);
	
	/**
	 * 获取满足条件的英雄数量
	 * @param playerId
	 * @param level
	 * @param quality
	 * @param star
	 */
	public int getHeroNumByCondition(String playerId, int level, int quality, int star);
	
	/**
	 * 获取玩家拥有并分享过的英雄数量
	 * @param playerId
	 * @param level
	 * @param quality
	 * @param star
	 */
	public int getSharedHeroNum(String playerId);
	
	/**
	 * 获取满足条件的装备数量
	 * @param playerId
	 * @param level
	 * @param quality
	 */
	public int getEquipNumByCondition(String playerId, int level, int quality);
	
	/**
	 * 获取兵种等级
	 * @param soldierId
	 * @return
	 */
	int getSoldierLevel(Integer soldierId);
	
	/**
	 * 获取兵种战力
	 * @return
	 */
	Map<Integer, Float> getArmyPowerMap();
	
	/***
	 * 获取直充金额
	 * @return
	 */
	int getRechargeCount(String playerId);

	PBHeroInfo getHeroInfo(String playerId, int heroId);
	/**
	 * 判断玩家是否在创建过程中.
	 * @return
	 */
	public boolean isNewly(String playerId);
	/**
	 * 记录潘多拉抽奖
	 * @param playerId
	 * @param num 消耗的钥匙数量,0代表免费抽奖.
	 */
	public void logPandoraLottery(String playerId, int num);
	/**
	 * 潘多拉抽奖
	 * @param playerId
	 * @param cfgId 配置ID
	 * @param num 个数
	 */
	public void logPandoraExchange(String playerId, int cfgId, int num);
	
	public int getGiftBuyCnt(String giftId);
	
	/***
	 * 记录幸运星抽奖次数
	 * @param playerId
	 * @param num
	 * @return
	 */
	public void logLuckyStarLottery(String playerId, int num);
	
	/****
	 * 获取玩家平台信息
	 * @param playerId
	 * @return
	 */
	public String getPlatform(String playerId);
	
	public int getPlatId(String playerId);
	
	/**
	 * 获取机甲觉醒/年兽界面信息
	 * @return
	 */
	public HawkTuple2<MachineAwakeState, Long> getMachineAwakeInfo(ActivityType activityType);
	
	/**
	 * 通过award.xml表给奖励
	 * @param awardId
	 * @param count
	 * @param playerId
	 * @param isPop
	 * @param action
	 * @param originType
	 */
	public String sendAwardFromAwardCfg(int awardId, int count, String playerId, boolean isPop, Action action, RewardOrginType... originType);

	
	/**获取award.xml表的随机奖励
	 * @param awardId
	 * @return
	 */
	public List<String> getAwardFromAwardCfg(int awardId);

	/**
	 * 发送走马灯消息
	 * 
	 * @param key
	 * @param playerId
	 * @param parms
	 */
	public void sendBroadcat(Const.NoticeCfgId key, String playerId, Object... parms); 
	
	/***
	 * 获取联盟旗帜
	 * @param guildId
	 * @return
	 */
	public int getGuildFlat(String guildId);
	
	/**
	 * 不管是跨服过来的,还是跨服出去的.
	 * @param playerId
	 * @return
	 */
	public boolean isCrossPlayer(String playerId);
	
	public boolean isPlayerCrossIngorePlayerObj(String playerId);

	/**
	 * 玩家是否存在
	 * @param playerId
	 * @return
	 */
	public boolean isPlayerExist(String playerId);
	
	/**
	 * 玩家是否处于副本状态
	 * @param playerId
	 * @return
	 */
	public boolean isInDungeonState(String playerId);
	
	/**
	 * 是否是NPC玩家
	 * @param playerId
	 * @return
	 */
	public boolean isNpcPlayer(String playerId);
	
	/****
	 * 判断联盟是否存在
	 * @param guildId
	 * @return
	 */
	public boolean isGuildExist(String guildId);

	/****
	 * 判断联盟是否在本服存在
	 * @param guildId
	 * @return
	 */
	public boolean isGuildLocalExist(String guildId);
	
	/****
	 * 发系统红包
	 * @param playerId
	 * @param awardId
	 * @param callback
	 */
	public void sendSystemRedEnvelope(String playerId, int awardId, RecieveCallBack callback);
	
	/****
	 * 获取玩家头像
	 * @param playerId
	 * @return
	 */
	public String getPfIcon(String playerId);
	
	/***
	 * 获取ICON 
	 * @param playerId
	 * @return
	 */
	public int getIcon(String playerId);
	
	/**
	 * 获取获取AccountRoleInfo列表的redis操作key
	 * @param openId
	 * @return
	 */
	public String getAccountRoleInfoKey(String openId);
	/**
	 * 获取账号角色信息
	 * @param serverId
	 * @param platform
	 * @param openId
	 * @return
	 */
	public AccountRoleInfo getAccountRole(String serverId, String platform, String openId);
	
	public List<AccountRoleInfo> getAccountRoleList(String openId);
	
	/**
	 * 设置玩家 spreadInfo
	 */
	public void setPlayerSpreadInfo(String playerId, String openId, String serverId);
	
	/**
	 * 根据获取玩家的id获取 spreadInfo
	 */
	public PBSpreadBindRoleInfo getPlayerSpreadInfo(String playerId);
	
	/**
	 * 设置openid 绑定 推广码
	 * @param openId
	 */
	public void setSpreadOpenidBindFlag(String openId);
	
	/**
	 * 获取openid 是否绑定过推广码
	 * @param openId
	 * @return
	 */
	public boolean getIsSpreadOpenidBindFlag(String openId);
	
	/***
	 * 获取openId
	 * @param playerId
	 * @return
	 */
	public String getOpenId(String playerId);
	
	/***
	 * 领取回归大礼 打点
	 * @param playerId
	 */
	public void logRecieveComeBackGreatReward(String playerId);
	
	/***
	 * 发展冲刺成就完成 打点
	 * @param playerId
	 * @param achieveId
	 */
	public void logComeBackAchieve(String playerId, int achieveId);
	
	/***
	 * 老玩家回归 兑换 打点
	 * @param playerId
	 * @param id
	 * @param num
	 */
	public void logComeBackExchange(String playerId, int id, int num);
	
	/***
	 * 老玩家回归，低折回馈 打点
	 * @param playerId
	 * @param id
	 * @param num
	 */
	public void logComeBackBuy(String playerId, int id, int num);
	/**
	 * 获取玩家的平台好友
	 * @param playerId
	 * @return
	 */
	public JSONObject fetchPlatformFriendList(String playerId);
	/**
	 * 获取玩家的注册ID
	 * @param playerId
	 * @return
	 */
	public String getPlayerServerId(String playerId);
	public String getPlayerMainServerId(String playerId);

	public long getServerOpenTime(String playerId);
	/**
	 * 转接http
	 * @param serverId
	 * @param script
	 * @param formatArgs
	 * @param timeout
	 * @return
	 */
	HawkTuple2<Integer, String> proxyCall(String serverId, String script, String formatArgs, int timeout);
	
	
	public String doHttpRequest(String url, int timeout);
	
	/***
	 * 保存老玩家回归的传承标识
	 * @param info
	 */
	public void saveRoleInfoAfterOldServerComeBack(ActivityAccountRoleInfo info);
	
	/***
	 * 获取传承标识
	 * @param openId
	 * @return
	 */
	public ActivityAccountRoleInfo getRoleInfoInheritIdentifyAndRemove(String openId);
	
	/**
	 * 获取军魂承接回归信息
	 * @param openId
	 * @return
	 */
	public BackPlayerInfo getBackPlayerInfoById(String openId);
	
	/**
	 * 更新军魂承接回归信息
	 * @param info
	 */
	public void updateBackPlayerInfo(BackPlayerInfo info);
	
	/**
	 * 获取适合军魂承接的角色信息
	 * @param playerId
	 * @return
	 */
	public AccountRoleInfo getSuitInheritAccount(String playerId);
	
	/**
	 * 获取指定帐号的充值额度和vip经验
	 * @param roleInfo
	 * @return
	 */
	public int getAccountRechargeNumAndExp(AccountRoleInfo roleInfo);
	
	/**
	 * 获取vip经验最大值
	 * @return
	 */
	public int getVipMaxExp();
	
	/**
	 * 添加角色被承接记录
	 * @param info
	 */
	public void addInheritedInfo(AccountRoleInfo info, String playerId, int rebetGold);
	
	/**
	 * 获取军魂承接回归信息
	 * @param openId
	 * @return
	 */
	public BackNewPlayerInfo getBackPlayerInfoByIdNew(String openId);
	
	/**
	 * 更新军魂承接回归信息
	 * @param info
	 */
	public void updateBackPlayerInfoNew(String playerId, BackNewPlayerInfo info);

	/**
	 * 收集传承数据
	 * @param playerId
	 * @param inheritedInfo
	 * @param notInheritedInfo
	 */
	public void inheritDataCollect(String playerId, List<AccountRoleInfo> inheritedInfo, List<HawkTuple2<AccountRoleInfo, Integer>> notInheritedInfo);
	
	/**
	 * 获取所有区服帐号列表
	 * @param openId
	 * @return
	 */
	public List<AccountRoleInfo> getPlayerAccountInfosNew(String playerId);
	
	/**
	 * 获取适合军魂承接的角色信息
	 * @param playerId
	 * @return
	 */
	public AccountRoleInfo getSuitInheritAccountNew(String playerId);
	
	/**
	 * 获取指定帐号的充值额度和vip经验
	 * @param roleInfo
	 * @return
	 */
	public int getAccountRechargeNumAndExpNew(AccountRoleInfo roleInfo);
	
	/**
	 * 获取当前区服的合服时间.
	 * @return
	 */
	Long getServerMergeTime();
	
	/**
	 * 获取以建造的联盟旗帜坐标
	 * 
	 * @param guildId
	 * @return
	 */
	public List<Integer> getCreatedWarFlagPoints(String guildId);
	/**
	 * 获取已占领别的联盟的旗帜坐标
	 * @param guildId
	 * @return
	 */
	public List<Integer> getOccupyWarFlagPoints(String guildId);
	/**
	 * 获取已丢失的联盟旗帜坐标
	 * @param guildId
	 * @return
	 */
	public List<Integer> getLoseWarFlagPoints(String guildId);
	/**
	 * 获取可建造旗帜数量上限
	 * @param guildId
	 * @return
	 */
	public int getMaxCreateWarFlagCount(String guildId);
	/**
	 * 获取联盟插旗数量（包括被夺走的）
	 * @param guildId
	 * @return
	 */
	public int getOwnerFlagCount(String guildId);
	
	/**
	 *  
	 */
	void yuriRevengeSendRewardByMergeServer(int termId);
	
	/**
	 * 获取联盟拥有的旗帜数量
	 * 
	 * @param guildId
	 * @return
	 */
	public int getGuildWarFlagCount(String guildId);
	
	/**
	 * 记录红警战令经验购买情况
	 * @param playerId
	 * @param termId
	 * @param cycle
	 * @param id
	 * @param exp
	 */
	void logBuyOrderExp(String playerId,int termId, int cycle, int id, int exp);
	
	/**
	 * 记录红警战令进阶购买情况
	 * @param playerId
	 * @param termId
	 * @param cycle
	 * @param authId
	 */
	void logBuyOrderAuth(String playerId, int termId, int cycle, int authId);
	
	/**
	 * 记录红警战令经验等级流水
	 * @param playerId
	 * @param termId
	 * @param cycle
	 * @param expAdd
	 * @param exp
	 * @param level
	 * @param reason
	 * @param reasonId
	 */
	 
	void logOrderExpChange(String playerId, int termId, int cycle, int expAdd, int exp, int level, int reason, int reasonId);
	
	/**
	 * 记录红警战令任务完成情况
	 * @param playerId
	 * @param termId
	 * @param cycle
	 * @param orderId
	 * @param finishTimes
	 */
	void logOrderFinishId(String playerId, int termId, int cycle, int orderId, int addTimes, int finishTimes);
	
	/**
	 * 获取玩家的头像
	 * @param puid
	 * @return
	 */
	String getPfIconFromRedis(String openId, String platform); 
	
	void logBountyHunterHit(String playerId,int termId, String boss, int bossHp, String costStr, String rewStr, boolean free, int rewardMutil, int lefState, String bigGift);
	
	boolean isServerPlayer(String playerId);
	
	
	/**获取玩家每日积分总分
	 * @param playerId
	 * @return
	 */
	int getPlayerTavernBoxScore(String playerId);
	/**
	 * 源计划 抽奖打点 
	 * @param playerId
	 * @param termId
	 * @param lotteryType 1 超级充能 :2磁暴充能
	 * @param score 获取积分
	 */
	void logPlanActivityLottery(String playerId, int termId, int lotteryType, int score);
	
	/**
	 * 记录月签数据 
	 * @param playerId
	 * @param typea 1 签到,2 补签,3领取累记签奖励 
	 * @param termId
	 * @param dayIndex
	 * @param cost
	 */
	public void dailySignRewardRecord(String playerId, int type, int termId, int dayIndex, String cost);
	
	/**
	 * 推广员活动玩家打点
	 * @param playerId
	 * @param openid 绑定的openid
	 * @param code  绑定的code
	 * @param charge 玩家充值数量
	 * @param exchange 字段为0 表示是登录数据，其他表示兑换的id
	 * @param count 兑换的数量
	 */
	public void logPlayerSpreadLogin(String playerId, String openid, String code, int charge, int exchange, int count);
	
	/**
	 * 计算时间金币
	 */
	public int caculateTimeGold(long time, SpeedUpTimeWeightType type);
	
	/**
	 * 获取合服列表
	 */
	public List<String> getMergeServerList();
	/**
	 * 获取从服列表
	 * @return
	 */
	public List<String> getSlaveServerList();
	/**
	 * 投资理财打点日志
	 * @param addCustomer
	 * @param investCancel
	 */
	public void logInvest(String playerId, int productId, int investAmount, boolean addCustomer, boolean investCancel);
	
	/**
	 * 幸运折扣活动刷新奖池打点
	 * @param playerId
	 * @param refreshType 1 免费次数刷新, 2 使用道具刷新
	 * @param poolId 刷新到的奖池id
	 * @param discount 刷新到的折扣
	 */
	public void logLuckyDiscountDraw(String playerId, int refreshType, int poolId, String discount);
	
	/**
	 * 幸运折扣活动购买商品打点
	 * @param playerId
	 * @param goods 购买商品
	 * @param price 购买的单价
	 * @param num  购买的数量
	 * @param discount 购买的折扣
	 * @param goodsId 购买商品的id
	 */
	public void logLuckyDiscountBuy(String playerId, String goods, String price, int num, String discount, int goodsId);
	
	/**
	 * 活动模板8 腾讯url活动分享
	 * @param isShared 今日是否已经分享过
	 * @param isReward 今日是否已经领取
	 */
	public void setPlayerUrlModelEightActivityInfo(String playerId, boolean isShared, boolean isReward);
	
	/**
	 * 活动模板8 腾讯url活动分享 
	 * @return
	 */
	public PBEmptyModel8Info getPlayerUrlModelEightActivityInfo(String playerId);
	
	/**
	 * 活动模板9 腾讯url活动分享
	 * @param isShared 今日是否已经分享过
	 * @param isReward 今日是否已经领取
	 */
	public void setPlayerUrlModelNineActivityInfo(String playerId, boolean isShared, boolean isReward);
	
	/**
	 * 活动模板9 腾讯url活动分享 
	 * @return
	 */
	public PBEmptyModel9Info getPlayerUrlModelNineActivityInfo(String playerId);
	
	/**
	 * 活动模板10 腾讯url活动分享
	 * @param isShared 今日是否已经分享过
	 * @param isReward 今日是否已经领取
	 */
	public void setPlayerUrlModelTenActivityInfo(String playerId, boolean isShared, boolean isReward);
	
	/**
	 * 活动模板10 腾讯url活动分享 
	 * @return
	 */
	public PBEmptyModel10Info getPlayerUrlModelTenActivityInfo(String playerId);
	
	/**
	 * 触发英雄试炼
	 */
	public boolean touchHeroTrial(String playerId, List<Integer> heroIds, List<Integer> condition);
	
	public void logHeroTrialReceive(String playerId, int missionId);
	
	public void logHeroTrialRefreshMission(String playerId, int missionId);
	
	public void logHeroTrialComplete(String playerId, int missionId);
	
	public void logHeroTrialCostRefresh(String playerId, String cost);
	
	public void logHeroTrialCostSpeed(String playerId, int cost);

	public void logHeroBackExchange(String playerId, int activityId, int exchangeId, String costItem, String gainItem, int count);

	public void logHeroBackBuyChest(String playerId, int activityId, int chestId, int count, String costItem, String gainItem);
	
	/**
	 * 黑科技刷新奖池
	 * @param playerId
	 * @param cost
	 * @param buffId
	 */
	public void logBlackTechDraw(String playerId, long cost, int buffId);
	
	/**
	 * 黑科技激活buff
	 * @param playerId
	 * @param buffId
	 */
	public void logBlackTechActive(String playerId, int buffId);
	
	/**
	 * 黑科技购买加持礼包
	 * @param playerId
	 * @param packageId
	 */
	public void logBlackTechBuy(String playerId, int packageId);
	
	/**
	 * 全副武装探测打点
	 * @param playerId
	 * @param searchId
	 */
	public void logFullArmedSearch(String playerId, int searchId );
	
	/**
	 * 全副武装购买打点
	 * @param playerId
	 * @param cfgId
	 * @param count
	 */
	public void logFullArmedBuy(String playerId, int cfgId, int count);
	
	/**
	 * 先锋豪礼购买打点记录
	 * @param playerId
	 * @param termId
	 * @param type
	 * @param giftId
	 */
	public void logPioneerGiftBuy(String playerId, int termId, int type, int giftId);
	
	
	/**
	 * 时空轮盘抽奖
	 * @param playerId
	 * @param termId
	 * @param count    1 抽一次  10 十连抽
	 * @param buyCount    购买多少个
	 * @param itemSet 当前的四个设置
	 */
	public void logRouletteActivityLottery(String playerId, int termId, int count, int buyCount, String itemSet);
	
	/**
	 * 领取是空轮盘箱子奖励
	 * @param playerId
	 * @param termId
	 */
	public void logRouletteActivityRewardBox(String playerId, int termId);
	
	
	/**
	 * 皮肤计划
	 * @param playerId
	 * @param addScore 本次加分
	 * @param allScore 总分
	 */
	public void logSkinPlan(String playerId, int addScore, int afterScore, int beforeScore);
	
	/**
	 * 今日累充数据打点
	 * @param playerId
	 * @param id
	 * @param buyGift
	 */
	public void logDailyRecharge(String playerId, int id, boolean buyGift);
	
	/**
	 * 今日累充新版购买礼包数据打点 
	 * @param playerId
	 * @param giftId	礼包ID
	 * @param rewardIds 礼包选择奖励ID集合
	 */
	public void logDailyRechargeNew(String playerId, int giftId, String rewardIds);
	
	/**机甲觉醒个人总伤害数据打点
	 * 
	 * @param playerId
	 * @param activityId
	 * @param termId
	 * @param addScore
	 * @param totalScore
	 */
	public void logMachineAwakePersonDamage(String playerId, int activityId, int termId, int addScore, long totalScore);
	
	/**中秋庆典购买礼包
	 * @param playerId
	 * @param giftId
	 * @param items
	 */
	public void logMidAutumnGift(String playerId, int giftId, String items);

	
	/**
	 * 特惠商人助力庆典活动开始，刷新商店内容
	 * @param playerId
	 */
	public void travelShopAssistRefresh(String playerId, boolean clear);

	
	/**
	 * 检测权限
	 */
	public boolean checkGuildAuthority(String playerId, AuthId authId);
	
	/**
	 * 联盟总动员打点入驻日志
	 */
	public void logACMissionReceive(String playerId, int achieveId, int guildMemberCount);
	public void logACMissionFinish(String playerId, int addExp, int guildMemberCount);
	public void logACMissionAbandon(String playerId, int achieveId, boolean outData);
	public void logACMBuyTimes(String playerId);
	
	/***
	 * 勋章宝藏抽奖次数
	 */
	public void logMedalTreasureLottery(String playerId, int num);
	
	
	/**
	 * 记录限时掉落
	 * @param playerId
	 */
	void logTimieLimit(String playerId, int dropNum);
	
	/**
	 * 火险征召,全军动员
	 * @param playerId
	 * @param type
	 * @param score
	 */
	void logHellFire(String playerId, int type, int score);
	
	/**
	 * 庆典特惠 成就任务完成
	 * @param player
	 * @param achieveId 成就ID
	 */
	void logTravelShopAssistAchieveFinish(String playerId,int achieveId);
	
	/**
	 * 玩家许愿消耗金币和获取的许愿点
	 * @param player
	* @param awardId     锦鲤大奖ID
	 * @param cost        金币消耗
	 * @param wishPoint   许愿点数
	 * @param turnId      开奖期数(也是 开奖时间)
	 */
	public  void redkoiPlayerCost(String playerId, int awardId, int costType,long cost, 
			int wishPoint, String termId,String turnId);
	
	/**
	 * 玩家被选中锦鲤
	 * @param player
	 * @param awardId   锦鲤大奖ID
	 * @param turnId    开奖期数(也是 开奖时间)
	 */
	public  void redkoiAward(String playerId,int awardId,String termId,String turnId);
	
	public void logDivideGoldOpenRedEnvelope(String playerId, int goldNum);
	

	
	
	/**
	 * 是否是当前服
	 * @param serverId 玩家注册服
	 */
	public boolean isLocalServer(String serverId);

	/**
	 * 英雄进化奖池兑换
	 * 
	 * @param playerId		玩家角色ID
	 * @param level			奖池等级
	 * @param exchangeId	兑换ID
	 */
	public void logEvolutionExchange(String playerId, int level, int exchangeId);
	
	/**
	 * 英雄进化积分变动
	 * @param playerId
	 * @param exp		积分变化数		
	 * @param add		true增加false减少
	 * @param resourceId 增加或减少的产生来源  taskId或exchangeId
	 */
	public void logEvolutionExpChange(String playerId, int exp, boolean add, int resourceId);
	
	/**
	 * 英雄进化任务完成情况
	 * 
	 * @param playerId
	 * @param taskId	任务ID
	 * @param times		任务完成轮次（第几轮）
	 */
	public void logEvolutionTask(String playerId, int taskId, int times);
	
	/**
	 * 威龙商城商品兑换记录
	 * 
	 * @param player
	 * @param cfgId	 	兑换商品的配置ID
	 * @param itemId	兑换商品的物品ID
	 * @param itemNum	兑换商品的数量
	 * @param costNum   兑换消耗碎片的数量
	 * @param exchangeTimes 该商品已兑换次数 
	 */
	public void logFlightPlanExchange(String playerId, int cfgId, int itemId, int itemNum, int costNum, int exchangeTimes);

	
	/**泰伯利亚时间配置
	 * @return
	 */
	public TiberiumSeasonTimeAbstract getTiberiumSeasonTimeCfg();
	
	/**泰伯利亚时间配置
	 * @return
	 */
	public TiberiumSeasonTimeAbstract getTiberiumSeasonTimeCfgByTermId(int season, int termId);	

	/**获取泰伯利亚决赛的对战信息
	 * @param termId
	 * @return
	 */
	public TLWGetMatchInfoResp.Builder getTblyMatchInfo(String playerId, int termId);
	
	
	/**获取联盟的参战成员数量和总战力
	 * @param guildId
	 * @param termId
	 * @return
	 */
	public HawkTuple3<Long, Integer, String> getTWGuildInfo(String guildId);
	
	/**判断两个联盟是否是同组对手
	 * @param guildIdA
	 * @param guildIdB
	 * @return
	 */
	public boolean isTblySameRoom(String guildIdA, String guildIdB);

	/**
	 * 活动赛事对应开始和结束时间
	 * @param matchType 活动类型
	 * @param termID 活动期数
	 * @return 赛事开始和结束时间 first是开始时间，second是结束时间
	 */
	public HawkTuple2<Long, Long> getSeasonActivityMatchInfo(Activity.SeasonMatchType matchType, int termID);

	/**
	 * 获取玩家能量源数量
	 * @param playerId
	 * @return
	 */
	public int getEnergyCount(String playerId);
	
	/**
	 * 获取指挥官学院排名分数配置
	 * @param rank
	 * @return
	 */
	public List<CommandAcademyRankScoreCfg> getCommandAcademyRankScoreCfg(int rank);
	public List<CommandAcademySimplifyRankScoreCfg> getCommandAcademySimplifyRankScoreCfg(int rank);

	/**
	 * 处理基地飞升活动建筑升级
	 * @param playerId
	 */
	public boolean dealWithBaseBuild(String playerId);

	public boolean dealWithBackToNewFlyBuild(String playerId);

	
	/**
	 * 指挥官学院礼包购买
	 * @param playerId  
	 * @param termId   活动期数
	 * @param stageId  活动阶段ID
	 * @param giftId   助力礼包ID
	 */
	public  void logCommandAcademyGiftBuy(String playerId, int termId, int stageId,int giftId);
	
	/**
	 * 指挥官 学院排名
	 * @param playerId
	 * @param termId    活动期数
	 * @param stageId   活动阶段ID
	 * @param rankIndex 排行名次
	 */
	public  void logCommandAcademyRank(String playerId, int termId, int stageId,int rankIndex);
	
	/**
	 * 指挥官学院团购人数
	 * @param termId    活动期数
	 * @param stageId   活动阶段ID
	 * @param buyCount  礼包真实购买人数
	 * @param assistCount 注水数量
	 */
	public  void logCommandAcademyBuyCount(int termId,int stageId,int buyCount,int assistCount);
	
	/**
	 * 指挥官学院礼包购买
	 * @param playerId  
	 * @param termId   活动期数
	 * @param stageId  活动阶段ID
	 * @param giftId   助力礼包ID
	 */
	public  void logCommandAcademySimplifyGiftBuy(String playerId, int termId, int stageId,int giftId);
	
	/**
	 * 指挥官 学院排名
	 * @param playerId
	 * @param termId    活动期数
	 * @param stageId   活动阶段ID
	 * @param rankIndex 排行名次
	 */
	public  void logCommandAcademySimplifyRank(String playerId, int termId, int stageId,int rankIndex);
	
	/**
	 * 指挥官学院团购人数
	 * @param termId    活动期数
	 * @param stageId   活动阶段ID
	 * @param buyCount  礼包真实购买人数
	 * @param assistCount 注水数量
	 */
	public  void logCommandAcademySimplifyBuyCount(int termId,int stageId,int buyCount,int assistCount);
	
	/**
	 * 发放联盟礼包
	 * @param playerId
	 * @param allianceGift  联盟礼包ID
	 */
	public void sendAllianceGift(String playerId,int allianceGift);
	
	/**
	 * 玩家在同一个盟的判断
	 */
	public boolean isInTheSameGuild(String... playerIds);
	
	/**
	 * 黑市装备精炼
	 * @param playerId
	 * @param termId 活动期数
	 * @param refineId  精炼公式ID
	 * @param count 精炼次数
	 */
	public void logEquipBlackMarketRefine(String playerId,int termId,int refineId,int count);
	
	/**
	 * 获取圣诞boss刷新限制.
	 * @return
	 */
	int getChristmasBossRefreshLimit();
	
	/**
	 * 记录任务完成的次数.
	 * @param termId
	 * @param taskId
	 * @param num
	 */
	void logChristmasTask(int termId, int taskId, int num);
	/**
	 * 记录圣诞领取记录.
	 * @param playerId
	 * @param termId
	 * @param taskId
	 */
	void logChristmasTaskReceive(String playerId, int termId, int taskId);
	
	/**
	 * 获取boss数量。
	 * @return
	 */
	int getChristmasBossNum();

	
	
	/**
	 * 是否今日未登录过的离线玩家
	 * @param playerId
	 * @return
	 */
	boolean isDailyOffLine(String playerId);
	
	/**
	 * 是否今日首次登陆
	 * @param playerId
	 * @return
	 */
	boolean isDailyFirstLogin(String playerId);
	
	/**
	 * 获取玩家最近一次的登出时间
	 * @param playerId
	 * @return
	 */
	long getPlayerLogoutTime(String playerId);

	/**
	 * 生成机器人名字和id
	 */
	String genRobotName();
	
	/**
	 * 资源保卫战建造
	 * @param player
	 * @param targetId 目标玩家id
	 */
	public void logResourceDefenseBuild(String playerId, int resType);
	
	/**
	 * 资源保卫战偷取
	 * @param player
	 * @param targetId 目标玩家id
	 */
	public void logResourceDefenseSteal(String playerId, String targetId);
	
	/**
	 * 资源保卫战获取经验
	 * @param player
	 * @param addExp 增加经验
	 * @param afterExp 增加过后玩家总经验
	 * @param afterLevel 增加经验过后玩家等级
	 */
	public void logResourceDefenseExp(String playerId, int addExp, int afterExp, int afterLevel);

		/***
	 * 时空豪礼 成就任务完成
	 * @param player
	 * @param taskId 成就ID
	 */
	public void logChronoGiftTaskFinish(String playerId,int termId,int taskId);
	
	/***
	 * 时空豪礼 时空之门解锁
	 * @param player
	 * @param giftId 时空之门ID
	 */
	public void logChronoGiftUnlock(String playerId,int termId,int giftId);
	
	
	/***
	 * 时空豪礼 免费礼品获取
	 * @param player
	 * @param @param giftId 时空之门ID
	 */
	public void logChronoGiftFreeAwardAchieve(String playerId,int termId,int giftId);
	
	
	
	
	
	/**
	 * 充值基金投资流水
	 * @param playerId
	 * @param termId
	 * @param giftId
	 */
	public void logRechargeFundInvest(String playerId, int termId, int giftId);
	
	/**
	 * 充值基金充值解锁信息
	 * @param playerId
	 * @param termId
	 * @param rechargeGold
	 * @param rechargeBef
	 * @param rechargeAft
	 * @param unlockCnt
	 */
	public void logRechargeFundRecharge(String playerId, int termId, int rechargeGold, int rechargeBef, int rechargeAft, int unlockCnt);
	
	/**
	 * 充值基金领奖
	 * @param playerId
	 * @param termId
	 * @param giftId
	 * @param rewardId
	 */
	public void logRechargeFundReward(String playerId, int termId, int giftId, int rewardId);

	boolean checkMergeServerTimeWithCrossTime(MergeServerTimeCfg timeCfg);
	
	
	/**
	 * 获取老玩家回流
	 * @param playerId
	 * @return
	 */
	public BackFlowPlayer getBackFlowPlayer(String playerId);
	

	
	/**
	 * 发送聊天消息
	 * @param sender
	 * @param receivers
	 * @param nid
	 * @param params
	 */
	public void sendChatRoomMessage(String sender,List<String> receivers,NoticeCfgId nid,Object... params);
	
	/**
	 * 获取登录时间
	 * @param playerId
	 * @return
	 */
	public long getAccountLoginTime(String playerId);
	
	
	/**
	 * 回归有礼抽奖
	 * @param playerId
	 * @param termId
	 * @param backCount
	 * @param isFree
	 * @param lotteryCount
	 */
	public void logBackGiftLottery(String playerId, int termId, int backCount, int isFree,int lotteryCount);

	/**
	 * 体力赠送，消息数量
	 * @param playerId
	 * @param termId
	 * @param backCount
	 * @param messageCount
	 * @param messageTotalCount
	 */
	public void logPowerSendMessageCount(String playerId, int termId, int backCount, int messageCount,int messageTotalCount);

	/**
	 * 免费兑换装扮
	 * @param player
	 * @param termId
	 * @param addExp
	 * @param afterLevel
	 * @param afterExp
	 */
	public void logExchangeDecorateLevel(String playerId, int termId, int addExp,int afterLevel,int afterExp);
	
	/**
	 * 免费兑换装扮 任务完成
	 * @param player
	 * @param termId
	 * @param addExp
	 * @param afterLevel
	 * @param afterExp
	 */
	public void logExchangeDecorateMission(String playerId, int termId, int missionId);	
	
	/**
	 * 能源滚滚个人积分流水
	 * @param playerId
	 * @param termId
	 * @param addScore
	 * @param addType
	 * @param afterScore
	 */
	public  void logEnergiesSelfScore(String playerId, int termId, int addScore, int addType, int afterScore);
	
	/**
	 * 能源滚滚联盟积分流水
	 * @param guildId
	 * @param termId
	 * @param addScore
	 * @param addType
	 * @param afterScore
	 */
	public void logEnergiesGuildScore(String guildId, int termId, int addScore, int addType, long afterScore);
	
	/**
	 * 能源滚滚排行记录
	 * @param termId
	 * @param rankType
	 * @param rankerId
	 * @param rank
	 * @param score
	 */
	public void logEnergiesRank(int termId, int rankType, String rankerId, int rank, long score);
	
	
	/**幽灵秘宝记录翻牌结果
	 * @param playerId
	 * @param drewValue  翻牌结果
	 */
	public void logGhostSecretDrewResult(String playerId, int termId, int drewValue);
	
	/**幽灵秘宝重置时记录本轮玩家已翻牌的次数
	 * @param playerId
	 * @param drewedTimes 已经翻牌次数
	 */
	public void logGhostSecretResetInfo(String playerId, int termId, int drewedTimes);
	
	/**记录三连奖励触发次数
	 * @param playerId
	 * @param rewardId
	 */
	public void logGhostSecretRewardInfo(String playerId,int termId, int rewardId);

	
	/**
	 * 获取龙船信息
	 * @return
	 */
	public PBDragonBoat.Builder getDragonBoatInfo();
	
	/**
	 * 获取龙船位置信息
	 * @return
	 */
	public HawkTuple2<Integer, Integer> getDragonBoatPos();
	
	
	/**
	 * 端午-联盟庆典升级发奖
	 * @param termId
	 * @param guildId
	 * @param level
	 * @param players
	 */
	public void logDragonBoatCelebrateionLevelReward(int termId, 
			String guildId,int level,String players);
	
	/**
	 * 端午-联盟庆典贡献经验
	 * @param playerId
	 * @param termId
	 * @param guildId
	 * @param donateType
	 * @param donateExp
	 */
	public void logDragonBoatCelebrationDonate(String playerId, int termId,
			String guildId, int donateType, int donateExp,int totalExp);
	
	
	/**
	 * 端午-道具兑换
	 * @param player
	 * @param termId
	 * @param exchangeId
	 * @param exchangeCount
	 */
	public void logDragonBoatExchange(String playerId, int termId, 
			int exchangeId, int exchangeCount);
	
	/**
	 * 端午-龙船送礼
	 * @param player
	 * @param termId
	 * @param type 1前往    2到达领奖
	 * @param boatId
	 */
	public void logDragonBoatGiftAchieve(String playerId, int termId, int type, long boatId);
	
	/**
	 * 端午-打开福袋
	 * @param player
	 * @param termId
	 * @param openCount
	 */
	public void logDragonBoatLuckyBagOpen(String playerId, int termId, int openCount);
	
	
	/**
	 * 端午-连续充值天数
	 * @param player
	 * @param termId
	 * @param days
	 */
	public void logDragonBoatRechargeDays(String playerId, int termId, int days);
	
	
	/** 记录 虚拟实验室翻卡结果
	 * @param playerId
	 * @param termId
	 * @param cardIndex
	 * @param cardValue
	 */
	public void logVirtualLaboratoryOpenCard(String playerId,int termId, int cardIndex, int cardIndexTwo, int cardValue);
	
	/**
	 * 检查赛博条件
	 * @param playerId
	 * @return
	 */
	public boolean checkCyborgWar(String playerId);

	
	/**
	 * @param playerId
	 * @param termId 活动期数
	 * @param buyId	购买基金的Id
	 * @param scoreInfo 积分任务数据
	 */
	public void logMedalFundRewardScoreInfo(String playerId,int termId, int buyId, String scoreInfo, int type);

	/**
	 * 沙场点兵，翻雕像
	 * @param playerId
	 * @param termId
	 * @param stage 阶段ID
	 * @param quality 雕像品质ID
	 */
	public void logArmiesMassOpenSculpture(String playerId,int termId,int stage,int quality);
	
	
	/**机甲投资领奖记录
	 * @param playerId
	 * @param termId 活动期数
	 * @param buyId	购买基金的Id
	 * @param scoreInfo 积分任务数据
	 */
	public void logSupersoldierInvestRewardScoreInfo(String playerId, int termId, int buyId, String scoreInfo);

	
	/**能量源投资领奖记录
	 * @param playerId
	 * @param termId 活动期数
	 * @param buyId	购买基金的Id
	 * @param scoreInfo 积分任务数据
	 */
	public void logEnergyInvestRewardScoreInfo(String playerId, int termId, int buyId, String scoreInfo);

	
	/**霸主膜拜膜拜记录
	 * @param playerId
	 * @param termId
	 * @param blessRealNum  真实膜拜次数
	 * @param blessWaterNum 注水后的膜拜次数
	 */
	public void logOverlordBlessingInfo(String playerId, int termId, long blessRealNum, long blessWaterNum);

	
	
	/**
	 * 新服战令-战令经验购买
	 * @param player
	 * @param termId
	 * @param expId
	 * @param exp
	 */
	public void logNewBuyOrderExp(String playerId, int termId, int expId, int exp);
	
	/**
	 * 新服战令-进阶直购
	 * @param player
	 * @param termId
	 * @param authId
	 */
	public void logNewBuyOrderAuth(String playerId, int termId, int authId);
	
	/**
	 * 新服战令-经验流水
	 * @param player
	 * @param termId
	 * @param expAdd
	 * @param totalExp
	 * @param exp
	 * @param level
	 * @param reason	变更来源:0-初始化,1-完成任务,2-进阶,3-购买经验
	 * @param reasonId	来源id: 任务id/进阶礼包id/经验id
	 */
	public void logNewOrderExpChange(String playerId, int termId, int expAdd, int totalExp,int exp, int level, int reason, int reasonId);
	
	/**
	 * 新服战令-任务完成情况
	 * @param player
	 * @param termId
	 * @param orderId
	 * @param finishTimes
	 */
	public void logNewOrderFinishId(String playerId, int termId, int orderId, int addTimes, int finishTimes);


	public void logSeasonBuyOrderAuth(String playerId, int termId, int authId);

	/**
	 * 新服战令-经验流水
	 * @param player
	 * @param termId
	 * @param expAdd
	 * @param oldLevel
	 * @param exp
	 * @param level
	 * @param reason	变更来源:0-初始化,1-完成任务,2-进阶,3-购买经验
	 * @param reasonId	来源id: 任务id/进阶礼包id/经验id
	 */
	public void logSeasonOrderExpChange(String playerId, int termId, int expAdd,int exp, int oldLevel, int level, int reason, int reasonId);

	/**
	 * 新服战令-任务完成情况
	 * @param player
	 * @param termId
	 * @param orderId
	 * @param finishTimes
	 */
	public void logSeasonOrderFinishId(String playerId, int termId, int orderId, int addTimes, int finishTimes);

	public void logStarLightSignAward(String playerId, List<Reward.RewardItem.Builder> awardList, int reason);
	public void logStarLightSignScore(String playerId, int before, int after, int add);
	public void logStarLightSignChoose(String playerId, int type, int rechargeType, int choose);
	public List<Integer> getCanFightCenterFlags(String guildId);
	
	public int getCenterFlagCount(String guildId);
	
	public int getCenterFlagPlaceCount(String guildId);

	/**双享豪礼购买打点记录
	 * @param playerId
	 * @param termId
	 * @param giftId
	 * @param rewardId
	 */
	public void logDoubleGiftBuy(String playerId, int termId, int giftId, int rewardId);
	
	
	public void logGroupBuy(String playerId, int termId, int giftId, int rewardId, long realTimes, long waterTimes);
	
	/**
	 * 军械要塞进入下一层
	 * @param playerId
	 * @param termId  期数
	 * @param fromStage  操作前层数
	 * @param toStage  操作后层数
	 */
	public  void logOrdnanceFortressAdvance(String playerId,int termId,  int fromStage, int toStage) ;
	

	/**
	 * 军械要塞 开奖
	 * @param playerId
	 * @param termId 期数
	 * @param stage  层级
	 * @param openId 奖券ID
	 * @param count  当前层开奖次数
	 * @param cost   开奖消耗
	 * @param rewardType  奖品类型  1普通奖励  2大奖
	 * @param rewardId 奖品ID
	 */
	public  void logOrdnanceFortressOpen(String playerId, int termId, int stage, int openId, 
			int count, String cost, int rewardType, int rewardId);
	
	

	/**
	 * 获取蛋糕信息
	 * @return
	 */
	public PBCakeShare.Builder getCakeShareInfo();
	
	/**
	 * 获取蛋糕位置信息
	 * @return
	 */
	public HawkTuple2<Integer, Integer> getCakeSharePos();
	
	/**更新烟花效果
	 * @param playerId
	 * @param type
	 * @param duration
	 */
	public void updatePlayerFireWorks(String playerId, int type, long duration);
	
	

	/*
	 * 装备工匠词条变动
	 * @param playerId
	 * @param cfgId 装备词条配置id
	 * @param armourAddCfgId 装备属性配置id
	 * @param effectType 作用号类型
	 * @param effectValue 作用号值
	 * @param reason 变动原因 1 获取 2 放弃 3 被传承
	 * @param inheritArmourCfgId 传承的装备配置id
	 */
	public void logEquipCarftsmanAttr(String playerId, int cfgId, int armourAddCfgId, int effectType, int effectValue, int reason, int inheritArmourCfgId);
	/**
	 * 周年红包打开
	 * @param playerId
	 * @param termId 活动期数
	 * @param stage  红包ID
	 * @param score  红包积分
	 */
	public void logRedPackageOpen(String playerId, int termId, int stage, int score);
	
	/**
	 * 战地寻宝活动 
	 */
	public void logActivityAchieve(String playerId, int activityId, int termId, int achieveId, int achieveState, String achieveData);
	public void logBattleFieldBuyGift(String playerId, int termId);
	public void logBattleFieldDice(String playerId, int termId, boolean add, int diceType, int count, int afterCount);
	public void logBattleFieldDiceReward(String playerId, int termId, int awardType, int awardId, int cellId);


	/**
	 * 周年庆烟花盛典,点燃烟花激活buff
	 * @param playerId
	 * @param buffId
	 */
	public void logFireWorksForBuffActive(String playerId, int termId, int buffId);

	/**
	 * 周年庆庆典美食制作蛋糕
	 * @param playerId
	 * @param level  制作蛋糕的等级
	 */
	public void logCelebrationFoodMake(String playerId, int termId, int level);
	
	/**
	 * 军备兑换
	 * @param playerId
	 */
	public void logArmamentExchangeFirst(String playerId, int termId);



	
	/**
	 * 记录装备战令经验购买情况
	 * @param playerId
	 * @param termId
	 * @param cycle
	 * @param id
	 * @param exp
	 */
	void logBuyOrderEquipExp(String playerId,int termId, int cycle, int expId, int exp);
	
	/**
	 * 记录装备战令进阶购买情况
	 * @param playerId
	 * @param termId
	 * @param cycle
	 * @param authId
	 */
	void logBuyOrderEquipAuth(String playerId, int termId, int cycle, int authId);
	
	/**
	 * 装备战令经验流水
	 * @param playerId
	 * @param termId
	 * @param cycle
	 * @param expAdd
	 * @param exp
	 * @param level
	 * @param reason
	 * @param reasonId
	 */
	public void logOrderEquipExpChange(String playerId, int termId, int cycle, int expAdd, int exp, int level, int reason, int reasonId);
	
	
	/**
	 * 装备战令任务完成流水
	 * @param playerId
	 * @param termId
	 * @param cycle
	 * @param orderId
	 * @param addTimes
	 * @param finishTimes
	 */
	public void logOrderEquipFinishId(String playerId, int termId, int cycle, int orderId, int addTimes, int finishTimes);
	
	/**
	 * 回流拼图完成任务组
	 * @param playerId
	 * @param termId
	 */
	public void logReturnPuzzleScore(String playerId, int termId,int score);
	
	/**
	 * 检查特权礼包半价
	 * 
	 * @param playerId
	 * @return
	 */
	public int checkMonthCardPriceCut(String playerId);



	/**
	 * 获取代金券参数列表
	 * @param id
	 * @return
	 */
	public List<Integer> getVoucherItemUserParams(int id);
	
	/**
	 * 获取代金券限制
	 * @return
	 */
	public long getVoucherItemLimitPrice(int id,int type);
	
	/**
	 * 获取代金券过期时间
	 * @param id
	 * @return
	 */
	public long getVoucherEndTime(int id);
	
	/**
	 * 获取代金券价值
	 * @return
	 */
	public long getVoucherValue(int id);

	public String getDYZZBattleInfo(String playerId);

	public void delDYZZBattleInfo(String playerId);
	
	
	
	/**
	 * 超级折扣活动刷新奖池打点
	 * @param playerId
	 * @param refreshType 1 免费次数刷新, 2 使用道具刷新
	 * @param poolId 刷新到的奖池id
	 * @param discount 刷新到的折扣
	 */
	public void logSuperDiscountDraw(String playerId,int termId, int refreshType,  int cfgId,int poolId, String discount);
	
	/**
	 * 超级折扣活动购买商品打点
	 * @param playerId
	 * @param goods 购买商品
	 * @param price 购买的单价
	 * @param num  购买的数量
	 * @param discount 购买的折扣
	 * @param goodsId 购买商品的id
	 */
	public void logSuperDiscountBuy(String playerId,int termId, String goods, String price, int num, String discount, int goodsId,int voucherId);
	
	/**
	 * 全服签到活动玩家签到打点
	 * @param playerId
	 * @param termId
	 */
	public void logPlayerGlobalSign(String playerId, int termId);


	/**
	 * 双十一联盟欢庆活动联盟积分流水
	 * @param playerId
	 * @param guildId 联盟id
	 * @param termId  活动期数
	 * @param addScore 积分增量
	 * @param afterPlayerScore 增加后的个人积分
	 * @param afterGuildScore 增加后的联盟积分
	 */
	public void logAllianceCelebrateScore(String playerId, String guildId, int termId, int addScore, int afterPlayerScore,int afterGuildScore);


	/**
	 * 双十一联盟欢庆活动排行记录
	 * @param playerId
	 * @param termId
	 * @param level 领取奖励的等级
	 * @param index 领取奖励的第几列 1普通 和 2高级
	 */
	public void logAllianceCelebrateReward(String playerId, int termId, int level, int index);

	/**
	 * 资源保卫战 特工能力刷新/激活 记录
	 * @param playerId
	 * @param type 类型 1激活 , 2刷新
	 * @param skillInfo 技能信息
	 */
	public void logResourceDefenseSkillRefreshAndActive(String playerId, int termId, int type, String skillInfo);

	/**
	 * 资源保卫战 特工能力 生效
	 * @param playerId
	 * @param skillId 生效技能的Id
	 */
	public void logResourceDefenseAgentSkillEffect(String playerId, int termId, int skillId);


	/**
	 * 装扮投放系列活动三:重燃战火 领取宝箱每次一个
	 * @param playerId
	 */
	public void logFireReigniteReceiveBox(String playerId, int termId);

	
	/**
	 * 传承记录打点
	 * 
	 * @param playerId
	 * @param termId      期数 
	 * @param oldPlayerId 被传承角色ID
	 * @param oldServerId 被传承角色注册区服ID
	 * @param sumGold     被传承角色充值额度
	 * @param rebetGold   返利额度
	 * @param sumVipExp   被传承角色贵族经验值
	 * @param rebetExp    贵族经验返利值
	 */
	public void logAccountInherit(String playerId, int termId, String oldPlayerId, String oldServerId, long sumGold, long rebetGold, long sumVipExp, long rebetExp);

		
	
	/**
	 * 泰能宝库 进入下一层
	 * @param playerId
	 * @param termId  期数
	 * @param fromStage  操作前层数
	 * @param toStage  操作后层数
	 */
	public  void logPlantFortressAdvance(String playerId,int termId,  int fromStage, int toStage) ;
	


	/**
	 * 泰能宝库 开奖
	 * @param playerId
	 * @param termId 期数
	 * @param stage  层级
	 * @param openId 奖券ID
	 * @param count  当前层开奖次数
	 * @param cost   开奖消耗
	 * @param rewardType  奖品类型  1普通奖励  2大奖
	 * @param rewardId 奖品ID
	 */
	public  void logPlantFortressOpen(String playerId, int termId, int stage, int openId, 
			int count, String cost, int rewardType, int rewardId);
	/**
	 * 军事备战进阶奖励领取
	 * @param playerId
	 * @param termId  期数
	 * @param rewardId 成就任务ID
	 */
	public void logMilitaryPrepareAdvancedReward(String playerId, int termId, int  rewardId);
	/**
	 * 获取显示奖励(堆叠)
	 * @param itemStrs
	 * @return
	 */
	public List<RewardItem> getShowReward(List<String> itemStrs);
	
	public void logPeakHonourScore(String playerId, String guildId, int getType, long addScore, long afterScore, int matchId);
	public void logTimeLimitBuy(String playerId, int goodsId, int success);
	public void logTimeLimitBuyWater(int goodsId, int addCount);
	/**
	 * 小战区是否开放
	 * @return
	 */
	public boolean xzqOpen();
	/**
	 * 当前是否为测试环境
	 * @return
	 */
	public boolean isServerDebug();
	/**
	 * 是否是专服
	 */
	public boolean isProprietaryServer();

	/**
	 * 装圣诞节系列活动二:冬日装扮活动 每次领取宝箱
	 * @param boxId 宝箱Id
	 * @param playerId
	 */
	public void logFireReigniteReceiveBoxTwo(String playerId, int termId, int boxId);
	/**
	 * 圣诞累计充值钻石数
	 * @param playerId
	 * @param termId
	 * @param rechargeDiamond 活动充值的钻石数
	 * @param totalRechargeDiamond 活动总的充值钻石数
	 */
	public void logChristmasRechargeDiamond(String playerId, int termId, int rechargeDiamond, int totalRechargeDiamond);

	
	
	/**
	 * 获取当天生效作用号值
	 * @param playerId
	 * @param effId
	 * @return
	 */
	public int effectTodayUsedTimes(String playerId,EffType effId);
	
	/**
	 * 获取军事备战直购礼包名字信息
	 * @param giftId
	 * @return
	 */
	public String getMilitaryPrepareGiftName(String giftId);

	
	/**
	 * 雄心壮志活动抽取宝箱
	 * @param playerId
	 * @param termId
	 * @param boxCount
	 */
	public void logCoreplateBox(String playerId, int termId, int boxCount);


	/**
	 * 记录登录基金购买事件
	 * @param playerId
	 * @param type 登录基金类型1加速基金,2英雄基金, 3装备基金
	 */
	public void buyLoginFundTwoRecord(String playerId, int termId, int type);


	/**
	 * 记录洪福礼包解锁事件
	 * @param playerId
	 * @param giftId 礼包id
	 */
	public void logHongFuGiftUnlock(String playerId, int termId, int giftId);

	/**
	 * 记录洪福礼包领奖
	 * @param playerId 玩家id
	 * @param termId 活动期数
	 * @param giftId 礼包id
	 * @param dayCount 领取累计了几天的
	 * @param chooseRewardId 自选奖励id
	 */
	public void logHongFuGiftRecReward(String playerId, int termId, int giftId, int dayCount, int chooseRewardId);
	
	/**
	 * 红蓝对决翻牌操作记录
	 * @param playerId
	 * @param termId
	 * @param operType
	 * @param pool
	 * @param ticketId
	 * @param rewardId
	 * @param refreshTimes
	 */
	public void logRedbludTicketFlow(String playerId, int termId, int operType, int pool, int ticketId, int rewardId, int refreshTimes);
	
	/**
	 * 精装夺宝摇色子
	 * @param player
	 * @param termId 期数
	 * @param randomFirst 色子1随机数
	 * @param randomSecond 色子2随机值
	 * @param awardNumStart 中奖范围起始
	 * @param awardNumEnd  中奖范围结束
	 * @param randomId  随机配置ID
	 * @param awardId  中奖ID
	 * @param scoreAdd 增加兑换记分
	 * @param cost  消耗
	 */
	public void logDressTreasureRandom(String playerId,int termId,int randomFirst,int randomSecond,
			int awardNumStart,int awardNumEnd,int randomId,int awardId,String cost) ;
	
	/**
	 * 精装夺宝重置
	 * @param player
	 * @param termId 期数
	 * @param randomId 随机配置ID
	 * @param awardNumStart 中奖范围起始
	 * @param awardNumEnd 中奖范围结束
	 */
	public void logDressTreasureRest(String playerId,int termId,int randomId, int awardNumStart, int awardNumEnd);

	
	/**
	 * 根据tx数据判断预流失（干预）活动是否开启
	 * @param playerId
	 * @return
	 */
	public boolean checkPrestressinLossActivityOpen(String playerId);

	/**
	 * 根据tx数据判断预天降洪福活动是否开启
	 * @param playerId
	 * @return
	 */
	public void checkHeavenBlessingActivityOpen(String playerId);
	
	/**
	 * 泰能机密打点
	 * @param playerId
	 * @param operType
	 * @param openBoxCount
	 * @param openCardTime
	 * @param buyItemCount
	 * @param openBoxTimes
	 * @param success
	 */
	public void logPlantSecret(String playerId, int termId, int operType, int openBoxCount, int openCardTime, int buyItemCount, int openBoxTimes, boolean success, int serverNum, int clientNum);


	/**
	 * 预流失活动激活打点
	 * @param playerId
	 * @param openTerm
	 */
	public void logPrestressingLoss(String playerId, int openTerm);
		
	/**
	 * 幸运转盘抽奖
	 * @param player
	 * @param termId
	 * @param randomCount  抽奖次数
	 * @param cellId  奖品格子ID
	 * @param rewardId 奖品ID
	 * @param canSelect 是否可以更换
	 * @param finish  是否是完结一抽
	 */
	public void logLuckyBoxRandom(String playerId,int termId,String group, int randomCount, int cellId, int rewardId,int canSelect,int finish);

	/**
	 * 移民活动check
	 * @param playerId
	 * @return
	 */
	public boolean immgrationBackFlowCheck(String playerId);
	
	
	
	/**
	 * 盟军祝福活动签到
	 * @param player
	 * @param termId 期数
	 * @param openPos 开放数字位置
	 * @param openNum 开放数字之
	 */
	public void logAllianceWishSign(String playerId,int termId, int signType,int openPos, int openNum);
	
	
	/**
	 * 盟军祝福活动帮助
	 * @param player
	 * @param termId 期数
	 * @param guildMember  联盟玩家ID
	 */
	public void logAllianceWishHelp(String playerId,int termId, String guildMember);

	/**
	 * 鸿福天降用户购买时统计
	 * @param playerId
	 * @param groupId
	 * @param level
	 * @param payCount
	 * @param choose
	 */
	public void logHeavenBlessingPay(String playerId,int groupId, int level, int payCount, int choose);

	/**
	 * 鸿福天降
	 * @param playerId
	 * @param groupId
	 */
	public void logHeavenBlessingActive(String playerId,int groupId);

	/**
	 * 鸿福天降
	 * @param playerId
	 * @param groupId
	 * @param level
	 * @param payCount
	 * @param choose
	 */
	public void logHeavenBlessingAward(String playerId,int groupId, int level, int payCount, int choose);

	/**
	 * 鸿福天降
	 * @param playerId
	 * @param gain
	 */
	public void logHeavenBlessingRandomAward(String playerId, Reward.RewardItem.Builder item);


	public void logHeavenBlessingOpen(String playerId);

	/**
	 * 活动通用记录打点
	 * @param playerId 玩家
	 * @param param 参数
	 */
	public void logActivityCommon(String playerId, LogInfoType logInfoType, Map<String, Object> param);
	/**
	 * 针对全服的非个人的
	 * @param logInfoTypeName
	 * @param param
	 */
	public void logActivityCommon(LogInfoType logInfoType, Map<String, Object> param);

	/**
	 * 感恩福利领奖打点
	 * @param playerId
	 * @param punchCount
	 * @param createDays
	 * @param help
	 * @param gold
	 */
	public void logGrateBenefitsAward(String playerId, int punchCount, int createDays, int help, int gold);



	/**荣耀返利 购买
	 * @param playerId
	 * @param termId
	 * @param num 购买次数
	 */
	public void logHonorRepayBuy(String playerId, int termId, int num);

	/**
	 * 陨晶成就达成
	 * @param playerId
	 * @param achieveId
	 */
	public void logDYZZAchieveReach(String playerId, int achieveId);

	/**
	 * 陨晶成就领取
	 * @param playerId
	 * @param achieveId
	 */
	public void logDYZZAchieveTake(String playerId, int achieveId);


	/**荣耀返利 领取返利奖励
	 * @param playerId
	 * @param termId
	 * @param buyTimes  返利购买次数
	 * @param type	类型 1-手动领取,2-系统补发
	 */
	public void logHonorRepayReceiveReward(String playerId, int termId, int buyTimes, int type);


	public Set<Tuple> getRankList(Rank.RankType rankType, int maxCount);

	public List<Rank.RankInfo> getRankCache(Rank.RankType rankType, int maxCount);

	public int returnBuildingLvUp(String playerId, int level);

	public int returnTechUp(String playerId, int level);

	public int returnRoleUp(String playerId, int level);

	public boolean returnUpgradeCheck(String playerId, Activity.ReturnUpgradeType type);

	public Map<Integer, Integer> getTechTypeMap(String playerId);

	public Map<Integer, Integer> getTechLevelTypeMap(int level);

	public long getTechLevelPower(int level);

	public Map<Integer, Integer> getTechTypeMaxMap();

	public Activity.ChangeServerActivityCondition.Builder getChangeServerActivityCondition(String playerId, Map<String, String> toServerIdMap, String tarServerId, Activity.ChangeServerActivityConditionType type);

	public void onChangeServerSearch(String playerId, int protoType, String name, int type);

	public String getRealChangeServerId(String playerId, String tarServerId, Map<String, String> toServerIdMap);
	public boolean onChangeServer(String playerId, String tarServerId, Map<String, String> toServerIdMap);

	/**荣耀返利 本次活动礼包次数
	 * @param playerId
	 * @param rechargeType
	 * @param iosId
	 * @param androidId
	 * @param startTime
	 * @return
	 */
	public int getRechargeTimesAfter(String playerId, int rechargeType, String iosId, String androidId, long startTime);

	/***
	 * 当前显示装扮
	 * @param  dressType
	 * @return modelType
	 */
	int getShowDress(String playerId, int dressType);
	
	/**
	 * 七夕相遇结局
	 * @param playerId
	 * @param termId  期数
	 * @param endingId 结局ID
	 */
	public void logLoverMeetEnding(String playerId,int termId, int endingId);
	
	/**
	 * 获取当天装备抽卡次数
	 * @param playerId
	 */
	public int getAmourGachaCount(String playerId);
	
	/**
	 * 是否完成这个章节任务
	 * @param playerId
	 * @param missionId
	 * @return
	 */
	public boolean hasFinishStoryMission(String playerId, int missionId);
	
	/**
	 * 盟军祝福礼包购买
	 * @param player
	 * @param termId  期数
	 * @param count  祝福值
	 * @param signCount  签到天数
	 */
	public void logAllianceWishGiftBuy(String playerId,int termId,int giftId, long count,int signCount);
	
	
	/**
	 * 盟军祝福礼包购买
	 * @param player
	 * @param termId  期数
	 * @param count  祝福值
	 * @param signCount  礼包ID
	 */
	public void logAllianceWishAchieve(String playerId,int termId, long count,int giftId);

	
	/**
	 * 英雄祈福选择祈福ID
	 * @param playerId
	 * @param termId
	 * @param choose
	 */
	public void logHeroWishChoose(String playerId,int termId, int choose);
	
	/**
	 * 联盟是否参与当前战斗
	 */
	boolean isJoinCurrWar(String guildId);

	/**
	 * 荣耀同享玩家捐献
	 * @param playerId
	 * @param termId
	 * @param itemId
	 * @param count
	 * @param guildId
	 * @param beforeEnergy 捐献前经验值
	 * @param afterEnergy 捐献后经验值
	 * @param curEnergyLevel 捐献后的A能量等级
	 */
	public void logShareGloryDonate(String playerId,int termId, int itemId,
									int count, String guildId, int beforeEnergy,
									int afterEnergy, int curEnergyLevel);

	/**
	 *  荣耀同享能量柱升级
	 * @param playerId
	 * @param termId
	 * @param guildId
	 * @param curLevel
	 */
	public void logShareGloryEnergyLevelup(String playerId,int termId, String guildId,
										   int curLevel, int itemId);

	/**
	 * 荣耀英雄降临抽奖
	 * @param playerId
	 * @param termId 期数
	 * @param lotteryTpye 抽奖类型 单抽  10抽
	 * @param lotteryRlt 抽奖结果
	 */
	public void logHonourHeroBefellLottery(String playerId,int termId, int lotteryTpye,String lotteryRlt);
	
	/**
	 * 双旦活动礼包的购买信息上报
	 */
	public void logLotteryGiftPay(String playerId, int lotteryType, int selectId, String randomReward);
	/**
	 * 双旦活动领取联盟进度奖励上报
	 */
	public void logLotteryTakeAchieveReward(String playerId, int lotteryType, int achieveId);
	/**
	 * 双旦活动抽奖结果上报 
	 */
	public void logLotteryInfo(String playerId, int lotteryType, String reward);
	
	/**
	 * 获取星甲召唤的信息
	 * @param guildId
	 * @return
	 */
	public SpaceMachineGuardActivityInfoPB.Builder getSpaceMechaInfo(String guildId);
	/**
	 * 获取本期活动内联盟放置的所有舱体信息
	 * @param guildId
	 * @return
	 */
	public List<GuardRecordPB.Builder> getGuildSpaceRecord(String guildId);
	/**
	 * 判断玩家当天是否发起过联盟号召
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean spaceMechaGuildCall(String playerId);
	
	/**
	 * 机甲研究所 -攻坚芯片掉落
	 * @param playerId
	 * @param termId
	 * @param dropType
	 * @param dropCount
	 */
	public void logMachineLabDrop(String playerId,int termId,int dropType,int dropCount);
	
	/**
	 * 机甲研究所 -领取战令奖励
	 * @param playerId
	 * @param termId
	 * @param orderType
	 * @param levels
	 */
	public void logMachineLabOrderReward(String playerId,int termId,int orderType,String levels);
	
	/**
	 * 机甲研究所 -捐献
	 * @param playerId
	 * @param termId
	 * @param count
	 * @param giftMult
	 * @param donatMult
	 * @param serverExpAdd
	 * @param playerExpAdd
	 * @param stormingPointAdd
	 * @param stormingPointTotal
	 */
	public void logMachineLabContribute(String playerId,int termId, int count, int giftMult,
			int donatMult, int serverExpAdd, int playerExpAdd, int stormingPointAdd, int stormingPointTotal);
	
	/**
	 * 机甲研究所 -兑换
	 * @param playerId
	 * @param termId
	 * @param exchangeId
	 * @param exchangeCount
	 */
	public void logMachineLabExchange(String playerId,int termId,int exchangeId,int exchangeCount);
	
	/**
	 * 获得星币打点
	 * @param player
	 * @param termId
	 * @param taskId
	 * @param pointCount
	 * @param guildPointCount
	 * @param taskLimitGap
	 */
	public void logSpaceMechaPointGet(String playerId, int termId, int taskId, int pointCount, long guildPointCount, int taskLimitGap);
	/**
	 * 星甲召唤添加联盟星币
	 * @param termId
	 * @param guildId
	 * @param addPoint
	 */
	public void addSpaceMechaPoint(String guildId, int addPoint);
	/**
	 * 星甲召唤获取联盟星币
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public long getSpaceMechaPoint(String guildId);
	
	/**跨服事件回服务处理*/
	public boolean corssPostEvent(String playerId, ActivityEvent event);
	
	public void dungeonRedisLog(String anyId, String messagePattern, final Object... arguments);

	/**
	 * 判断本服是否是霸主赐福活动可以开启的服
	 * @return
	 */
	public boolean isOverlordBlessingOpenServer();
	
	public void	logCelebrationGiftBuy(String playerId, int fundLevel);
	
	public void logCelebrationScoreBuy(String playerId, int score, int cost);
	
	/**
	 * 获取玩家的金条数量
	 * @param playerId
	 * @return
	 */
	public int getDiamonds(String playerId);
	
	public void logGoldBabyFindReward(String playerId,ActivityType activityType, int poolId, int isLockTopGrade, int costCount, int rewardCount, int magnification);
	/**
	 * 新兵作训活动打点记录
	 * @param playerId
	 * @param times
	 * @param remainTimes
	 */
	public void logNewbieTrain(String playerId, int trainType, int times, int remainTimes, int gachaTimes, int gachaTimesTotal);

	/**
	 * 获取作用号值
	 * @param playerId
	 * @param effType
	 * @return
	 */
	public int getBuff(String playerId, EffType effType);
	/**
	 * 获取作用号生效期结束时间
	 * @param playerId
	 * @param effType
	 * @return
	 */
	public long getBuffEndTime(String playerId, EffType effType);

	public void syncEffect(String playerId, Collection<Integer> collection);
	
	/**
	 * 判断对应装扮是否处于激活状态
	 * @param playerId
	 * @param dressId
	 * @return
	 */
	public boolean dressInActiveState(String playerId, int dressId);
	
	/**
	 * 选举邀请合服活动队长
	 */
	public String chooseInviteMergeLeader();
	
	/**
	 * 这里取出来的官职ID包含星球大战, 国王战.
	 * 星球大战覆盖国王战.
	 * @param playerId
	 * @return
	 */
	public int getOfficerId(String playerId);
	/**
	 * 获取idip gm充值的redis存储key
	 * @return
	 */
	String getGmRechargeRedisKey();
	
	List<ServerInfo> getServerList();

	/**
	 * 星能探索活动刷矿点
	 */
	public List<Integer> planetExploreRefreshResPoint(int refreshCount);

	/**
	 * 获取星能矿点剩余储量
	 * @param posX
	 * @param posY
	 * @return
	 */
	public long getPlanetPointResRemain(int posX, int posY);

	/**
	 * 获取星能点的生命周期时长
	 * @param refreshTargetId
	 * @return
	 */
	public long getPlanetPointLifeTime(int refreshTargetId);

	/**装备科技解锁*/
	boolean isUnlockEquipResearch(String playerId,int type);

	
	/**
	 * 中部培养计划  任务完成获得积分
	 * @param player
	 * @param termId
	 * @param achieveId 任务ID
	 * @param scoreAdd  积分增加数量
	 * @param scoreBef  积分变化前
	 * @param scoreAft  积分变化后
	 */
	public void logGrowUpBoostAchieveScore(String playerId,int termId,int achieveId, int scoreAdd, int scoreBef, int scoreAft);
	
	
	/**
	 * 中部培养计划  道具消耗获得积分
	 * @param player
	 * @param termId
	 * @param itemId 消耗道具ID
	 * @param itemNum 消耗道具数量
	 * @param scoreAdd  积分增加数量
	 * @param scoreBef  积分变化前
	 * @param scoreAft  积分变化后
	 */
	public void logGrowUpBoostItemScore(String playerId,int termId,int itemId,int itemNum,int scoreAdd,int scoreBef,int scoreAft);
	
	/**
	 * 中部培养计划   领取积分任务奖励
	 * @param player
	 * @param termId
	 * @param achieveId 成就ID
	 * @param score 当前积分
	 */
	public void logGrowUpBoostScoreAchieveRewardTake(String playerId,int termId,int achieveId,int score);
	
	/**
	 * 中部培养计划  任务刷新记录 
	 * @param player
	 * @param termId
	 * @param refreshCount 刷新次数
	 * @param curPage  当前任务页
	 */
	public void logGrowUpBoostScoreAchievePageChange(String playerId,int termId,int refreshCount,int curPage) ;
	
	
	/**
	 * 中部培养计划  兑换道具解锁层数 
	 * @param player
	 * @param termId
	 * @param exchangeId  兑换ID
	 * @param exchangeGroup 兑换组
	 * @param unlockGroupMaxBef 兑换前解锁最高兑换组
	 * @param unlockGroupMaxAft 兑换后解锁最高兑换组
	 */
	public void logGrowUpBoostExchangeGroup(String playerId,int termId,int exchangeId,int exchangeGroup,int unlockGroupMaxBef,int unlockGroupMaxAft);
	
	/**
	 * 中部培养计划  道具礼包购买 
	 * @param player
	 * @param termId
	 * @param buyId 购买ID
	 */
	public void logGrowUpBoostBuyGift(String playerId,int termId,int buyId);
	
	/**
	 * 中部培养计划  道具回收
	 * @param playerId
	 * @param termId
	 * @param itemId
	 * @param itemCount
	 */
	public void logGrowUpBoostItemRecover(String playerId,int termId,int itemId,int itemCount);
	
	
	/**
	 * 是否为185活动的小怪
	 * @param monsterId
	 * @return
	 */
	public boolean activity185Monster(int monsterId);
	
	
	/**
	 * 是否为184活动的小怪
	 * @param monsterId
	 * @return
	 */
	public boolean activity184Monster(int monsterId);
	
	/**
	 * 记录成长激励需要记录装备分解获得的道具数据
	 * @param playerId
	 * @param items
	 */
	public void growUpBoostEquipDecomposeItemRecord(String playerId,Map<Integer,Long> items);
	
	/**
	 * 获取成长激励需要记录装备分解获得的道具数据
	 * @param playerId
	 * @return
	 */
	public Map<Integer,Long> getGrowUpBoostItemRecord(String playerId);
	
	
	/**
	 * 更新成长激励需要记录装备分解获得的道具数据
	 * @param playerId
	 * @return
	 */
	public void updateGrowUpBoostItemRecord(String playerId,Map<Integer,Long> rmap);
	
	
	/**
	 * 获取礼包里面的道具奖励个数
	 * @param serverAwardId
	 * @return
	 */
	public Map<Integer,Long> getGfitServerAwardItemsCount(String giftId);
	
	/**
	 * 获取玩家游戏好友列表
	 * @param playerId
	 * @return
	 */
	public List<String> getPlayerGameFriends(String playerId);
	
	/**
	 * 跟进名字获取玩家ID
	 * @param name
	 * @return
	 */
	public String getPlayerByName(String name);
	
	/**
	 * 获取去兵战力
	 */
	public long getPlayerNoArmyPower(String playerId);
	
	

	/**
	 * 获取记录
	 * @param playerId
	 * @return
	 */
	public String getBackImmgrationData(String playerId);
	
	/**
	 * 更新保持活动记录
	 * @param playerId
	 * @param rmap
	 */
	public void updateBackImmgrationData(String playerId,String str);
	
	/**
	 * 获取排行榜内最全成员
	 * @param rankType
	 * @return
	 */
	public Map<String, HawkTuple2<Integer, Long>> getRankDataMapCache(RankType rankType);
	
	/**
	 * 是否在跨服活动时间内
	 * @return
	 */
	public boolean inCrossActivityTime();

	/**
	 * 判断超武养成线功能是否解锁
	 * @param playerId
	 * @return
	 */
	public boolean isManhattanFuncUnlocked(String playerId);
	/**
	 * 判断超武是否已解锁
	 * @param playerId
	 * @param swId
	 * @return
	 */
	public boolean isPlantWeaponUnlocked(String playerId, int swId);
	/**
	 * 判断超武解锁道具是否充足
	 * @param playerId
	 * @param swId
	 * @return
	 */
	public boolean isPlantWeaponUnlockItemEnough(String playerId, int swId);
	
	/**
	 * 判断能否取到玩家数据
	 * @param playerId
	 * @return
	 */
	public boolean checkPlayerExist(String playerId);

	public void updateGuildEffect(int activityId, String guildId, Map<Integer, Integer> effMap);

	public void cleanGuildEffect(int activityId);
	/**
	 * 获取当前服务器注册人数
	 */
	public int getServerPlayerCount();
	
	
	public String getMainServer(String serverId);
	
	/**
	 * 获取玩家联盟巨龙陷阱的数据
	 * @param playerId
	 * @return
	 */
	public GuildDragonTrapData getGuildDragonTrapData(String playerId);
	
	/**
	 * 操作联盟巨龙陷阱
	 * @param playerId
	 * @param action
	 */
	public void guildDragonTrapOp(String playerId,int action,String... params);
	
	/**
	 * 获取伤害数据
	 * @param playerId
	 * @return
	 */
	public List<PBDamageRank> guildDragonAttackRank(String playerId);

	
	public void xqhxTalentCheck(String playerId);
	
	public boolean isSeasonHonorDataNew();
	
	public HawkRedisSession getOldRedisSession();
	
	
	
	public HawkTuple3<Integer, Integer, Integer> getSoldierConfigData(int soldierId);
	public int getSpecialSoldierTime(int soldierLevel);
	
	public int getItemSpeedUpTime(int itemId);
}
