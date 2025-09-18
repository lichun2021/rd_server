package com.hawk.game.module;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonOpenTimeCfg;
import com.hawk.game.GsConfig;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.XQHXConstCfg;
import com.hawk.game.config.XQHXTalentCfg;
import com.hawk.game.config.XQHXTalentLevelCfg;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.crossproxy.xqhx.XQHXCallbackOperationService;
import com.hawk.game.crossproxy.xqhx.XQHXPrepareEnterCallback;
import com.hawk.game.entity.PlayerXQHXEntity;
import com.hawk.game.entity.PlayerXQHXTalentEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengXianquhx.msg.XQHXQuitReason;
import com.hawk.game.module.lianmengXianquhx.msg.XQHXQuitRoomMsg;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.msg.xqhx.*;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.*;
import com.hawk.game.protocol.Talent.*;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.guildTeam.model.GuildTeamRoomData;
import com.hawk.game.service.xqhxWar.XQHXWarService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.log.LogConst;
import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 先驱回响赛制模块
 */
public class PlayerXQHXModule extends PlayerModule {
    static Logger logger = LoggerFactory.getLogger("Server");

    /**
     * 构造函数
     *
     * @param player
     */
    public PlayerXQHXModule(Player player) {
        super(player);
    }

    /**
     * 玩家登陆
     * @return 执行结果
     */
    @Override
    protected boolean onPlayerLogin() {
        XQHXWarService.getInstance().syncPageInfo(player);
        checkTalent(player);
        return true;
    }

    /**
     * 请求页面信息
     * @param hawkProtocol 前端参数
     */
    @ProtocolHandler(code = HP.code2.XQHX_PAGE_INFO_REQ_VALUE)
    public void pageInfo(HawkProtocol hawkProtocol) {
        XQHXWarService.getInstance().syncPageInfo(player);
    }

    /**
     * 小队战力排行
     * @param hawkProtocol 前端参数
     */
    @ProtocolHandler(code = HP.code2.XQHX_TEAM_RANK_REQ_VALUE)
    public void teamRank(HawkProtocol hawkProtocol) {
        XQHXWarService.getInstance().teamRank(player);
    }

    /**
     * 对战历史
     * @param hawkProtocol 前端参数
     */
    @ProtocolHandler(code = HP.code2.XQHX_HISTORY_REQ_VALUE)
    public void history(HawkProtocol hawkProtocol) {
        XQHXWarService.getInstance().history(player);
    }

    public void checkTalent(Player player){
        PlayerXQHXEntity playerXQHXEntity = player.getData().getPlayerXQHXEntity();
        int seasonTermId = 0;
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
        if (opActivity.isPresent()) {
            seasonTermId = opActivity.get().getActivityTermId();
        }
        if(playerXQHXEntity.getSeason() != seasonTermId){
        	playerXQHXEntity.setSeason(seasonTermId);
            for(PlayerXQHXTalentEntity entity : player.getData().getXQHXTalentEntityList()){
                entity.setLevel(0);
            }
            playerXQHXEntity.setUsedPoint(0);
            XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
            int curCount = player.getData().getItemNumByItemId(constCfg.getTalentItemId());
            ConsumeItems consume = ConsumeItems.valueOf();
            consume.addItemConsume(constCfg.getTalentItemId(), curCount, false);
            if (!consume.checkConsume(player)) {
                return;
            }
            consume.consumeAndPush(player, Action.XQHX_SEASON_CLEAR);
            
        }

    }

    public int getTalentCount(Player player){
        XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
        int curCount = player.getData().getItemNumByItemId(constCfg.getTalentItemId());
        return Math.min(constCfg.getTalentMax(), curCount);
    }

    public void syncAllTalentInfo(Player player){
        XQHXTalentInfoResp.Builder resp = XQHXTalentInfoResp.newBuilder();
        for(PlayerXQHXTalentEntity entity : player.getData().getXQHXTalentEntityList()){
            resp.addTalentInfos(entity.toPB());
        }
        int curCount = getTalentCount(player);
        resp.setTotalPoint(curCount);
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
        if (opActivity.isPresent()) {
            int seasonTermId = opActivity.get().getActivityTermId();
            SeasonOpenTimeCfg seasonOpenTimeCfg = HawkConfigManager.getInstance().getConfigByKey(SeasonOpenTimeCfg.class, seasonTermId);
            if(seasonOpenTimeCfg != null){
                resp.setEndTime(seasonOpenTimeCfg.getHiddenTimeValue());
            } else {
                resp.setEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(180));
            }
        } else {
            resp.setEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(180));
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_TALENT_INFO_RESP, resp));
    }

    public PlayerXQHXTalentEntity getTalentBycfgId(Player player, int talentId){
        for(PlayerXQHXTalentEntity entity : player.getData().getXQHXTalentEntityList()){
            if(entity.getTalentId() == talentId){
                return entity;
            }
        }
        return null;
    }

    @ProtocolHandler(code = HP.code2.XQHX_TALENT_INFO_REQ_VALUE)
    public void talentInfo(HawkProtocol hawkProtocol) {
        syncAllTalentInfo(player);
    }

    @ProtocolHandler(code = HP.code2.XQHX_TALENT_UPGRADE_REQ_VALUE)
    public void talentUpgrade(HawkProtocol hawkProtocol) {
        XQHXTalentUpgradeReq req = hawkProtocol.parseProtocol(XQHXTalentUpgradeReq.getDefaultInstance());
        PlayerXQHXTalentEntity entity = getTalentBycfgId(player, req.getTalentId());
        if(entity == null){
            entity = new PlayerXQHXTalentEntity(player.getId(), req.getTalentId());
            HawkDBManager.getInstance().create(entity);
            player.getData().getXQHXTalentEntityList().add(entity);
        }
        PlayerXQHXEntity playerXQHXEntity = player.getData().getPlayerXQHXEntity();
        int curCount = getTalentCount(player);
        int needCouont = 0;
        for(int i = entity.getLevel() + 1; i <= req.getTargetLevel(); i++){
            XQHXTalentLevelCfg cfg = XQHXTalentLevelCfg.getCfgByTalentIdAndLevel(entity.getTalentId(), i);
            if(cfg == null){
                continue;
            }
            needCouont += cfg.getPoint();
        }
        if(needCouont > curCount - playerXQHXEntity.getUsedPoint()){
            return;
        }
        //检查前置
        XQHXTalentCfg tcfg = HawkConfigManager.getInstance().getConfigByKey(XQHXTalentCfg.class, entity.getTalentId());
        if(Objects.nonNull(tcfg) && !tcfg.checkFront(player.getData().getXQHXTalentEntityList())){
        	return;
        }
        entity.setLevel(req.getTargetLevel());
        XQHXTalentUpgradeResp.Builder resp = XQHXTalentUpgradeResp.newBuilder();
        resp.setTalentInfo(entity.toPB());
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_TALENT_UPGRADE_RESP, resp));
        player.getData().getPlayerEffect().syncXqhxTalent(player);
    }

    @ProtocolHandler(code = HP.code2.XQHX_TALENT_CLEAR_REQ_VALUE)
    public void talentClear(HawkProtocol hawkProtocol) {
        for(PlayerXQHXTalentEntity entity : player.getData().getXQHXTalentEntityList()){
            entity.setLevel(0);
        }
        PlayerXQHXEntity playerXQHXEntity = player.getData().getPlayerXQHXEntity();
        playerXQHXEntity.setUsedPoint(0);
        syncAllTalentInfo(player);
        player.getData().getPlayerEffect().syncXqhxTalent(player);
    }







    /*******************************************************************************************************************
     * 以下逻辑为战场关联逻辑
     ******************************************************************************************************************/
    /**
     * 进入战场
     * @param hawkProtocol 前端参数
     */
    @ProtocolHandler(code = HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE)
    public void onEnterInstance(HawkProtocol hawkProtocol) {
        if(!XQHXWarService.getInstance().checkEnter(player)){
            return;
        }
        String serverId = getCrossToServerId();
        if (HawkOSOperator.isEmptyString(serverId)) {
            this.sendError(hawkProtocol.getType(), Status.XQHXError.XQHX_HAS_NO_MATCH_INFO);
            return;
        }
        int errorCode = sourceCheckEnterInstance(serverId);
        if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
            this.sendError(hawkProtocol.getType(), errorCode);
            return;
        }
        XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
        WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
        long shieldEndTime = HawkTime.getMillisecond()  + constCfg.getBattleTime() + TimeUnit.MINUTES.toMillis(20);
        if (Objects.nonNull(worldPoint) && worldPoint.getShowProtectedEndTime() < shieldEndTime) {
            StatusDataEntity entity = player.addStatusBuff(GameConst.CITY_SHIELD_BUFF_ID, shieldEndTime);
            if (entity != null) {
                player.getPush().syncPlayerStatusInfo(false, entity);
            }
        }

        if (isCrossToSelf(serverId)) {
            //如果是本服的则处理.
            simulateCross();
            DungeonRedisLog.log(player.getId(), "onEnterInstance local:{}", serverId);
        } else {
            boolean rlt = sourceDoLeaveForCross(serverId);
            if (!rlt) {
                player.sendError(hawkProtocol.getType(), Status.SysError.EXCEPTION_VALUE, 0);
            } else {
                player.responseSuccess(hawkProtocol.getType());
            }
            DungeonRedisLog.log(player.getId(), "onEnterInstance corss:{},rlt:{}", serverId,rlt);
        }

    }

    /**
     * 获得目标服Id
     * @return 目标服Id
     */
    private String getCrossToServerId() {
        GuildTeamRoomData roomData = XQHXWarService.getInstance().getRoomData(player);
        if(roomData == null){
            return null;
        }
        return roomData.roomServerId;
    }

    /**
     * 跨服前原服检查
     * @param serverId 联盟id
     * @return 检查结果
     */
    private int sourceCheckEnterInstance(String serverId) {
        // 有行军
        BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
        if (!marchs.isEmpty()) {
            return Status.XQHXError.XQHX_HAS_MARCH_VALUE;
        }

        // 战争狂热
        if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
            return Status.XQHXError.XQHX_IN_WAR_FEVER_VALUE;
        }

        // 城内有援助行军，不能进入泰伯利亚
        Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), World.WorldMarchType.ASSISTANCE_VALUE,
                World.WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
        if (!marchList.isEmpty()) {
            return Status.XQHXError.XQHX_HAS_ASSISTANCE_MARCH_VALUE;
        }

        // 有被动行军
        BlockingQueue<IWorldMarch> passiveMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(player.getId());
        if (!CollectionUtils.isEmpty(passiveMarchs)) {
            int playerPos = WorldPlayerService.getInstance().getPlayerPos(player.getId());
            for (IWorldMarch march : passiveMarchs) {
                if (march == null || march.getMarchEntity() == null || march.getMarchEntity().isInvalid()) {
                    continue;
                }
                if (march.getTerminalId() != playerPos) {
                    continue;
                }
                return Status.XQHXError.XQHX_HAS_PASSIVE_MARCH_VALUE;
            }
        }

        // 着火也不行
        if (player.getPlayerBaseEntity().getOnFireEndTime() > HawkTime.getMillisecond()) {
            return Status.XQHXError.XQHX_ON_FIRE_VALUE;
        }

        // 在联盟军演组队中不能.
        if (WarCollegeInstanceService.getInstance().isInTeam(player.getId())) {
            return Status.Error.LMJY_BAN_OP_VALUE;
        }

        // 已经在跨服中了.
        if (CrossService.getInstance().isCrossPlayer(player.getId())) {
            return Status.CrossServerError.CROSS_FIGHTERING_VALUE;
        }

        //已经在副本中了
        if (player.isInDungeonMap()) {
            Player.logger.error("playerId:{} has in instance:{}", player.getId(), player.getDungeonMap());
            return Status.Error.PLAYER_IN_INSTANCE_VALUE;
        }
        return Status.SysError.SUCCESS_OK_VALUE;
    }

    /**
     * 目标服是否为本服
     * @param crossToServerId 目标服
     * @return 是否为本服
     */
    private boolean isCrossToSelf(String crossToServerId) {
        return GsConfig.getInstance().getServerId().equals(crossToServerId);
    }

    /**
     * 如果目标服在本服，模拟进入战场
     */
    private void simulateCross() {
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_WAR_SIMULATE_CROSS_BEGIN_VALUE));
        player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
            @Override
            protected void doAction() {
                player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_WAR_SIMULATE_CROSS_FINISH_VALUE));
                boolean rlt = XQHXWarService.getInstance().joinRoom(player);
                Player.logger.info("playerId:{} enter local xqhx instance result:{}", player.getId(), rlt);
            }
        });
    }

    /**
     * 如果目标服在本服，模拟离开战场
     */
    private void simulateExitCross() {
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_WAR_SIMULATE_CROSS_BACK_BEGIN));
        player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
            @Override
            protected void doAction() {
                player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_WAR_SIMULATE_CROSS_BACK_FINISH_VALUE));
            }
        });
    }

    /**
     * 真实跨服操作
     * @param targetServerId 目标服
     * @return 跨服结果
     */
    private boolean sourceDoLeaveForCross(String targetServerId) {
        HawkSession session = player.getSession();
        SessionClosedMsg closeMsg = SessionClosedMsg.valueOf();
        closeMsg.setTarget(player.getXid());
        player.onMessage(closeMsg);

        //不入侵原来的逻辑只能再这里把Session加回来.
        player.setSession(session);
        //减掉在线.
        GlobalData.getInstance().changePfOnlineCnt(player, true);

        //移除当前服的在线信息.
        RedisProxy.getInstance().removeOnlineInfo(player.getOpenId());

        //做一些处理,然后发起请求.
        int tryCrossErrorCode = Status.SysError.EXCEPTION_VALUE;
        tryCross:
        {
            //把数据刷到redis里面.
            boolean flushToDb = PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, false);
            if (!flushToDb) {
                Player.logger.error("player enter xqhx flush reids error playerId:{}", player.getId());
                break tryCross;
            }

            //序列化工会的数据.
            try {
                GuildService.getInstance().serializeGuild4Cross(player.getGuildId());
                player.getData().serialData4Cross();
            } catch (Exception e) {
                HawkException.catchException(e);
                break tryCross;
            }

            boolean  setStatus = RedisProxy.getInstance().setPlayerCrossStatus(GsConfig.getInstance().getServerId(), player.getId(), GsConst.PlayerCrossStatus.PREPARE_CROSS);
            if (!setStatus) {
                Player.logger.error("player set cross status fail playerId:{}", player.getId());
                break tryCross;
            }

            tryCrossErrorCode = Status.SysError.SUCCESS_OK_VALUE;
        }

        Player.logger.info("xqhx condtion check  playerId:{}, errorCode:{}, serverId:{}", player.getId(), tryCrossErrorCode, targetServerId);

        if (tryCrossErrorCode == Status.SysError.SUCCESS_OK_VALUE) {
            Cross.XQHXCrossMsg.Builder msgBuilder = Cross.XQHXCrossMsg.newBuilder();
            msgBuilder.setServerId(targetServerId);
            Cross.InnerEnterCrossReq.Builder builder = Cross.InnerEnterCrossReq.newBuilder();
            builder.setCurTime(HawkTime.getSeconds());
            builder.setCrossType(Cross.CrossType.XQHX);
            player.setCrossStatus(GsConst.PlayerCrossStatus.PREPARE_CROSS);
            //发起一个远程RPC到远程去,
            HawkProtocol protocl = HawkProtocol.valueOf(CHP.code.INNER_ENTER_CROSS_REQ, builder);
            CrossProxy.getInstance().rpcRequest(protocl, new XQHXPrepareEnterCallback(player, msgBuilder.build()), targetServerId, player.getId(), "");
        } else {
            XQHXCallbackOperationService.getInstance().onPrepareCrossFail(player);
        }

        return true;
    }

    /**
     * 退出战场房间
     *
     * @return
     */
    @MessageHandler
    private boolean onQuitRoomMsg(XQHXQuitRoomMsg msg) {
        try {
            boolean isMidwayQuit = msg.getQuitReason() == XQHXQuitReason.LEAVE;
            XQHXWarService.getInstance().quitRoom(player, isMidwayQuit);
        } catch (Exception e) {
            HawkException.catchException(e);
        }

        try {
            CrossService.getInstance().addForceMoveBackXqhxPlayer(player.getId());
        } catch (Exception e) {
            HawkException.catchException(e);
        }

        return true;
    }


    /**
     * 迁回的预处理.
     * @author  jm
     */
    public void targetDoPrepareExitCrossInstance() {
        // 通知客户端跨服开始
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_WAR_CROSS_BACK_BEGIN));

        //检测到可以退出了那么先设置标志位
        player.setCrossStatus(GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);

        CrossService.getInstance().addExitXqhxPlayer(player.getId());

        //移除跨服玩家相关数据
        GuildService.getInstance().onCsPlayerOut(player);
        //移除跨服记录的装扮信息
        WorldPointService.getInstance().removeShowDress(player.getId());
        WorldPointService.getInstance().removePlayerSignature(player.getId());
        WorldPointService.getInstance().removeCollegeNameShow(player.getId());
        //cs player里面有个check exist 需要设置这个状态.假装行军都已经完成了.
        player.setCrossStatus(GsConst.PlayerCrossStatus.EXIT_CROSS_MARCH_FINAL);
        //清理守护信息.
        RelationService.getInstance().onPlayerExitCross(player.getId());
    }

    /**
     * A->B
     * 客户端触发A转发B处理
     *
     * @return
     */
    private void targetDoExitInstance() {
        Player.logger.info("target do exit instance playerId:{}", player.getId());
        //设置状态,
        player.setCrossStatus(GsConst.PlayerCrossStatus.EXIT_CROSS);

        //调用一个close结算玩家的状态.
        try {
            SessionClosedMsg closeMsg = SessionClosedMsg.valueOf();
            closeMsg.setTarget(player.getXid());
            player.onMessage(closeMsg);

            //删除保护罩
            CityManager.getInstance().removeCityShieldInfo(player.getId());
        } catch (Exception e) {
            //报错也要执行完.
            HawkException.catchException(e);
        }

        int errorCode = Status.SysError.SUCCESS_OK_VALUE;
        //这种用异常不好控制.
        operationCollection:
        {
            //刷新玩家的数据到redis, 失败退出.
            boolean flushToRedis = PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, true);
            if (!flushToRedis) {
                Player.logger.error("csplayer exit cross flush to redis fail ", player.getId());

                break operationCollection;
            }
        }

        Player.logger.info("xqhx playerId:{}, exit cross  errorCode:{}", player.getId(), errorCode);

        //把玩家在本服的痕迹清理掉
        String fromServerId = CrossService.getInstance().removeImmigrationPlayer(player.getId());
        GlobalData.getInstance().removeAccountInfoOnExitCross(player.getId());
        HawkApp.getInstance().removeObj(player.getXid());
        GlobalData.getInstance().invalidatePlayerData(player.getId());

        XQHXWar.PBXQHXWarInnerBackServerReq.Builder req = XQHXWar.PBXQHXWarInnerBackServerReq.newBuilder();
        req.setPlayerId(player.getId());
        //发送一个协会回原服.
        CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(HP.code2.XQHX_WAR_INNER_BACK_SERVER_REQ_VALUE, req), fromServerId, "");
        String mainServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
        //设置redis状态, 都已经到了这一步了，失败了就失败了，也不能怎么样了.
        boolean setCrossStatus = RedisProxy.getInstance().setPlayerCrossStatus(mainServerId, player.getId(), GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
        if (!setCrossStatus) {
            Player.logger.error("xqhx player exit cross set cross status fail playerId:{}", player.getId());
        }

    }

    /**
     * 预退出跨服
     * @param msg
     */
    @MessageHandler
    public void targetOnPrepareExitCrossMsg(XQHXPrepareExitCrossInstanceMsg msg) {
        HawkLog.logPrintln("xqhx prepare exit cross Instance from msg playerId:{}", player.getId());
        targetDoPrepareExitCrossInstance();
    }

    /**
     * 发出退出跨服信息.
     * @param msg
     */
    @MessageHandler
    public void targetOnExitInstanceMessage(XQHXExitCrossInstanceMsg msg) {
        HawkLog.logPrintln("xqhx exitInstance from msg playerId:{}", player.getId());
        targetDoExitInstance();
    }

    /**
     * 退出战场
     * @param hawkProtocol 前端参数
     */
    @ProtocolHandler(code = HP.code2.XQHX_WAR_EXIT_INSTANCE_REQ_VALUE)
    public void onExitInstance(HawkProtocol hawkProtocol) {
        Player.logger.info("playerId:{} exit xqhx war from protocol", player.getId());
        if (player.isCsPlayer()) {
            //远程操作.
            CsPlayer csPlayer = player.getCsPlayer();
            if (!csPlayer.isCrossType(Cross.CrossType.XQHX_VALUE)) {
                csPlayer.sendError(hawkProtocol.getType(), Status.XQHXError.XQHX_NOT_IN_INISTANCE, 0);

                return;
            }
            targetDoPrepareExitCrossInstance();
        } else {
            //本地操作.
            simulateExitCross();
        }
    }

    /**
     * 返回原服
     * @param msg
     */
    @MessageHandler
    public void sourceOnBackServer(XQHXBackServerMsg msg) {
        Player.logger.info("xqhx corss player back server plaeyrId:{}", player.getId());
        //在发起退出的时候强行把数据序列化到redis中，返回之后再读一次.
        PlayerDataSerializer.csSyncPlayerData(player.getId(), true);
        //修改顺序 先把数据反序列化回来再移除.
        String toServerId = CrossService.getInstance().removeEmigrationPlayer(player.getId());

        toServerId = toServerId == null ? "NULL" : toServerId;
        LogUtil.logPlayerCross(player, toServerId, LogConst.CrossStateType.CROSS_EXIT, Cross.CrossType.XQHX);

        //只有玩家在线的时候才走登录流程.
        if (player.getSession() != null && player.getSession().isActive()) {
            //模拟login协议需要的策数据.
            AccountInfo accoutnInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
            accoutnInfo.setLoginTime(HawkTime.getMillisecond());

            Login.HPLogin.Builder cloneHpLoginBuilder = player.getHpLogin().clone();
            cloneHpLoginBuilder.setFlag(1);
            HawkProtocol loginProtocol = HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, cloneHpLoginBuilder);
            player.getSession().setUserObject("account", accoutnInfo);
            loginProtocol.bindSession(player.getSession());
            player.onProtocol(loginProtocol);
            //在login的时候会加，所以这减掉
            GlobalData.getInstance().changePfOnlineCnt(player, false);
        } else {
            //回原服的时候更新一下activeServer;
            GameUtil.updateActiveServer(player.getId(), GsConfig.getInstance().getServerId());
        }

        // 通知客户端跨服返回完成
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_WAR_CROSS_BACK_FINISH));

        // 设置跨服返回时间
        player.setCrossBackTime(HawkTime.getMillisecond());

        RankService.getInstance().checkCityLvlRank(player);
        RankService.getInstance().checkPlayerLvlRank(player);
    }


    /**
     * 迁回。
     * @param msg
     */
    @MessageHandler
    public void targetOnMoveBack(XQHXMoveBackCrossPlayerMsg msg) {
        Player.logger.info("xqhx playerId:{} receive move back msg", player.getId());
        if (!player.isCsPlayer()) {
            Player.logger.error("xqhx player isn't csplayer can not receive this protocol playerId:{}", player.getId());

            return ;
        }

        //在线的话, 尝试踢下线.
        if (player.isActiveOnline()) {
            player.notifyPlayerKickout(Status.SysError.ADMIN_OPERATION_VALUE, null);
        }

        //加入到退出跨服
        targetDoPrepareExitCrossInstance();
    }

    /**
     * 从GM指令发过来一个签回玩家的指令.
     * @param msg
     */
    @MessageHandler
    public void sourceOnPrepareMoveBack(XQHXPrepareMoveBackMsg msg) {
        String toServerId = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
        if (HawkOSOperator.isEmptyString(toServerId)) {
            Player.logger.error("playerId:{} prepare force move back toServerId is null ", player.getId());

            return;
        }

        Player.logger.info("playerId:{} prepare move back toServerId:{}", player.getId(), toServerId);

        XQHXWar.PBXQHXWarMoveBackReq.Builder req = XQHXWar.PBXQHXWarMoveBackReq.newBuilder();
        req.setPlayerId(player.getId());

        HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code2.XQHX_WAR_MOVE_BACK_REQ_VALUE, req);
        CrossProxy.getInstance().sendNotify(hawkProtocol, toServerId, player.getId(), "");
    }
}
