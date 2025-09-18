package com.hawk.game.module.autologic;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.ResTreasureCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.invoker.AutoPutModuleInvoker;
import com.hawk.game.invoker.GenerateResTreasureMsgInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.AutoMonsterMarchParam;
import com.hawk.game.module.autologic.PlayerAutoResourceParam.AutoMarchInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.HP.code;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.Status.AutoGatherErr;
import com.hawk.game.protocol.Talent.TalentType;
import com.hawk.game.protocol.World.*;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.MapBlock;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * 玩家成就
 *
 * @author golden
 */
public class PlayerAutoModule extends PlayerModule {
    public static final int AUTO_PUT_FAIL = 1;
    public static final int AUTO_PUT_ALREADY = 2;
    public static final int AUTO_PUT_SET_FIRST = 3;
    //小锅道具ID
    private final int normalResourceId = 1140001;
    //大锅道具ID
    private final int superResourceId = 1140002;
    //超大锅ID
    private final int hugeResourceId = 1140003;
    
    
    private long putPosEmptyTimer = -1;
    private boolean firstPut = true;
    private long logoutTime = 0;
    /**
     * 自动拉锅参数
     */
    private PlayerAutoResourceParam autoResouceParam;
    private long autoResourceTickTime;
    private volatile boolean cityMoving = false;

    public PlayerAutoModule(Player player) {
        super(player);
        this.autoResouceParam = new PlayerAutoResourceParam();
    }

    @Override
    protected boolean onPlayerLogin() {
        long timer = HawkTime.getMillisecond();
        if(this.logoutTime!=0 && this.logoutTime + 30000 < timer){
            this.autoResouceParam.tmpDebugLog("autoresource player: {} onPlayerLogin this.logoutTime + 30000 < timer stop auto",
                    this.player.getId());
            stopAutoPutAndMarch();
            this.logoutTime = 0;
        }

        boolean putStatus = this.autoResouceParam == null ? false :
                this.autoResouceParam.isAutoStationPut();

        boolean gatherStatus = this.autoResouceParam == null ?
                false : this.autoResouceParam.isAutoStationMarch();
        sendAutoPutStatus(putStatus, gatherStatus);

        this.autoResouceParam.tmpDebugLog("autoresource player: {} onPlayerLogin put: {} march {} time {}",
                this.player.getId(), putStatus, gatherStatus, timer);

        return true;
    }

    public void stopAutoPutAndMarch() {
        try{
            this.autoResouceParam.setAutoPutResourceEffect(false);
            this.autoResouceParam.setAutoStationPut(false);
            this.autoResouceParam.setAutoStationMarch(false);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    @Override
    protected boolean onPlayerLogout() {
        this.logoutTime = HawkTime.getMillisecond();
        this.autoResouceParam.tmpDebugLog("autoresource player: {} onPlayerLogout time {}",
                this.player.getId(), this.logoutTime);
        return true;
    }

    @Override
    public boolean onTick() {

        long curTime = HawkApp.getInstance().getCurrentTime();
        if (curTime < (this.autoResourceTickTime + 1000)) {
            return true;
        }
        this.autoResourceTickTime = curTime;

        //没有月卡
        if(!isHaveAutoResource()){
            return true;
        }

        if(this.isCityMoving()){
            this.setCityMoving(false);

            if(this.autoResouceParam.isAutoStationPut() || this.autoResouceParam.isAutoStationMarch()){
                stopAutoPutAndMarch();
                this.player.sendError(code.AUTO_GATHER_PARA_SETUP_REQ_VALUE,
                        AutoGatherErr.AUTO_TRANS_CITY_GATHER_STOP_VALUE, 0);
                sendAutoPutStatus(autoResouceParam.isAutoStationPut(), autoResouceParam.isAutoStationMarch());
            }
        }

        this.autoMarchResourceStation();
        this.autoPutResourceStation(curTime);
        this.checkPutPosEmpty(curTime);
        return true;
    }

    public boolean isAutoPut() {
        return this.autoResouceParam.isAutoStationPut() ||
                this.autoResouceParam.isAutoStationMarch();
    }

    private boolean isHaveAutoResource(){
        //获取作用号 是否已经开启自动拉锅
        int value = this.player.getData().getEffVal(EffType.AUTO_PUT_RESOURCE);
        if (value > 0) {
            this.autoResouceParam.setAutoPutResourceEffect(true);
        }
        //说明是从有到无，发邮件
        if (value <= 0) {
            sendStopAutoMail();
            return false;
        }
        return true;
    }

    private void sendStopAutoMail() {
        if (this.autoResouceParam.isAutoPutResourceEffect()) {
            SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                    .setPlayerId(player.getId())
                    .setMailId(MailId.AUTO_PUT_RES_EXPIRE)
                    .build());
            stopAutoPutAndMarch();
            sendAutoPutStatus(false, false);
        }
    }


    /**
     * 放锅子
     */
    private void autoPutResourceStation(long curTime) {
        //自动打野开启中不行
        if (isAutoKillMonster()){
            this.autoResouceParam.tmpDebugLog("autoresource player: {} autoPutResourceStation isAutoKillMonster",
                    player.getId());
            return;
        }

        //没开自动放锅，不处理
        if (null == this.autoResouceParam || !this.autoResouceParam.isAutoStationPut()) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} autoPutResourceStation isAutoStationPut",
                            player.getId());
            return;
        }
        //放锅逻辑
        //取玩家选择的坐标
        int[] posArr = GameUtil.splitXAndY(this.autoResouceParam.getStationPutPos());
        int posX = posArr[0];
        int posY = posArr[1];
        //从世界取玩家选择的坐标点，要求是空地needFree==true
        Point point = WorldPointService.getInstance().getAreaPoint(posX, posY, true);
        //坐标点非法或不是空地，不能放锅
        if (point == null) {
            WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(
                    autoResouceParam.getStationPutPos());
            if (worldPoint != null && worldPoint.getPointType() != WorldPointType.RESOURC_TRESURE_VALUE) {
                //当前自动放锅点有非锅的障碍，停止自动放锅，发送错误邮件
                this.autoResouceParam.
                        tmpDebugLog("autoresource player: {} autoPutResourceStation RESOURC_TRESURE_VALUE stopPutAndSendMail",
                                player.getId());
                stopPutAndSendMail();
            }
            return;
        }

        if (!point.canPlayerSeat()) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} autoPutResourceStation canPlayerSeat stopPutAndSendMail",
                            player.getId());
            stopPutAndSendMail();
            return;
        }

        ConstProperty cfg = HawkConfigManager.getInstance().getKVInstance(ConstProperty.class);

        Map<Integer, Integer> consumMap = cfg.getSpacePropLKB();


        int consumeId = 0;
        //先查看大锅，有的话优先放,
        if (this.autoResouceParam.isHugeResource() &&
                this.player.getData().getItemNumByItemId(this.hugeResourceId) > 0) {
            consumeId = this.hugeResourceId;
        }else if (this.autoResouceParam.isSuperResource() &&
                this.player.getData().getItemNumByItemId(this.superResourceId) > 0) {
            consumeId = this.superResourceId;
        } else if (this.autoResouceParam.isCommonResoure() &&
                this.player.getData().getItemNumByItemId(this.normalResourceId) > 0) {
            consumeId = this.normalResourceId;
        } else {
            //大小锅都没有，返回
            SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                    .setPlayerId(player.getId())
                    .setMailId(MailId.AUTO_PUT_RES_NO_ITEM)
                    .build());
            this.autoResouceParam.setAutoStationPut(false);
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} autoPutResourceStation no res to auto put",
                            player.getId());
            return;
        }
        int costCount = consumMap.get(consumeId);
        if (costCount <= 0) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} autoPutResourceStation costCount <= 0",
                            player.getId());
            return;
        }
        // 道具不足，不能放锅
        ConsumeItems consumeItems = ConsumeItems.valueOf();
        consumeItems.addItemConsume(consumeId, costCount);
        if (!consumeItems.checkConsume(player)) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} autoPutResourceStation !consumeItems.checkConsume",
                            player.getId());
            return;
        }

        // 道具
        ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, consumeId);
        //宝库id
        int restreId = itemCfg.getResTreasure();
        // 宝库配置
        ResTreasureCfg resTreCfg = HawkConfigManager.getInstance().getConfigByKey(ResTreasureCfg.class, restreId);
        // 宝库配置为空
        if (resTreCfg == null) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {}, ResTreasureCfg null, itemId:{}, cfgId:{}", player.getId(), consumeId, restreId);
            return;
        }
        // 目标点
        int pointId = GameUtil.combineXAndY(posX, posY);
        // 超出地图范围
        int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
        int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
        if (posX >= worldMaxX || posX <= 0 || posY >= worldMaxY || posY <= 0) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {}, out or range, x:{}, y:{}", player.getId(), posX, posY);
            return;
        }
        // 请求点为阻挡点
        if (MapBlock.getInstance().isStopPoint(pointId)) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {}, is stop point, x:{}, y:{}", player.getId(), posX, posY);
            return;
        }

        // 请求点再国王领地内
        if (WorldPointService.getInstance().isInCapitalArea(pointId)) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {}, is int capital area, x:{}, y:{}", player.getId(), posX, posY);
            return;
        }

        // 投递世界线程执行
        WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.WORLD_MONSTER_POINT_GENERATE) {
            @Override
            public boolean onInvoke() {
                // 中心点不是空闲点
                Point centerPoint = WorldPointService.getInstance().getAreaPoint(posX, posY, true);
                if (centerPoint == null) {
                    HawkLog.errPrintln("autoresource player: {}, centerPoint point not free, posX:{}, posY:{}",
                            player.getId(), posX, posY);
                    return false;
                }

                // 资源宝库占用半径
                int resRadius = resTreCfg.getRadius();

                // 获取周围点
                List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, resRadius);
                if (aroundPoints.size() != 2 * resRadius * (resRadius - 1)) {
                    HawkLog.errPrintln("autoresource player: {}, arround points has been occupy, posX:{}, posY:{}",
                            player.getId(), posX, posY);
                    player.dealMsg(MsgId.AUTO_PUT_MODULE, new AutoPutModuleInvoker(player, AUTO_PUT_FAIL));
                    return false;
                }

                // 投递回玩家线程：消耗道具
                player.dealMsg(MsgId.GEN_RES_TREASURE, new GenerateResTreasureMsgInvoker(player, consumeItems));

                // 中心点所在区域
                AreaObject areaObj = WorldPointService.getInstance().getArea(posX, posY);
                // 中心点所在资源带
                int zoneId = WorldUtil.getPointResourceZone(posX, posY);

                // 生成资源宝库点
                WorldPoint worldPoint = new WorldPoint(posX, posY, areaObj.getId(), zoneId, WorldPointType.RESOURC_TRESURE_VALUE);
                worldPoint.setResourceId(restreId);
                worldPoint.setLifeStartTime(HawkTime.getMillisecond());
                worldPoint.setProtectedEndTime(HawkTime.getMillisecond() + resTreCfg.getLifeTime() * 1000);
                worldPoint.setGuildId(player.getGuildId());
                worldPoint.setPlayerName(player.getName());

                // 创建玩家使用的世界点信息
                if (!WorldPointService.getInstance().createWorldPoint(worldPoint)) {
                    HawkLog.errPrintln("autoresource player: {}, createWorldPoint failed, posX:{}, posY:{}", player.getId(), posX, posY);
                    return false;
                }
                player.sendProtocol(HawkProtocol.valueOf(HP.code.GEN_RES_TRESSRUE_SUCCES_VALUE, PBGenResTreasureSuccess.newBuilder().setX(posX).setY(posY).setCfgId(restreId)));
                //把自动放锅空置检测timer赋初值
                autoResourceTickTime = -1;
                ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_ALLIANCE).setGuildId(player.getGuildId())
                        .setKey(Const.NoticeCfgId.RES_TREASURE_PUT).addParms(player.getName()).addParms(posX).addParms(posY).build();
                ChatService.getInstance().addWorldBroadcastMsg(parames);
                // 行为日志
                WorldPointService.logger.info("Gen RESOURC_TRESURE point playerId={} posX={} posY={}", player.getId(), posX, posY, restreId);
                player.dealMsg(MsgId.AUTO_PUT_MODULE, new AutoPutModuleInvoker(player, AUTO_PUT_ALREADY));
                return true;
            }
        });
    }

    public void stopPutAndSendMail() {
        SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                .setPlayerId(player.getId())
                .setMailId(MailId.AUTO_PUT_RES_POS_OCCUPY)
                .build());

        autoResouceParam.setAutoStationPut(false);
        autoResouceParam.setAutoStationMarch(false);
        sendAutoPutStatus(autoResouceParam.isAutoStationPut(), autoResouceParam.isAutoStationMarch());
    }

    public void stopPutAndSendErrCode() {
        this.player.sendError(code.AUTO_GATHER_PARA_SETUP_REQ_VALUE,
                AutoGatherErr.AUTO_PUT_POS_ERR_VALUE, 0);

        autoResouceParam.setAutoStationPut(false);
        autoResouceParam.setAutoStationMarch(false);
        sendAutoPutStatus(autoResouceParam.isAutoStationPut(), autoResouceParam.isAutoStationMarch());
    }

    /**
     * 拉锅子
     */
    private void autoMarchResourceStation() {
        //自动打野开启中不行
        if (isAutoKillMonster()) {
            autoResouceParam.tmpDebugLog("autoresource player: {} autoMarchResourceStation isAutoKillMonster",
                    player.getId());
            return;
        }
        //没有自动拉锅，不处理
        if (null == this.autoResouceParam || !this.autoResouceParam.isAutoStationMarch()) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} autoMarchResourceStation !this.autoResouceParam.isAutoStationMarch()",
                            player.getId());
            return;
        }

        WorldPoint point = WorldPointService.getInstance().getWorldPoint(this.autoResouceParam.getStationMarchPos());
        if (point == null) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} autoMarchResourceStation point == null",
                            player.getId());
            return;
        }
        //判断世界点类型是否为超时空要塞
        if (point.getPointType() != WorldPointType.RESOURC_TRESURE_VALUE) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} autoMarchResourceStation point.getPointType() != WorldPointType.RESOURC_TRESURE_VALUE",
                            player.getId());
            return;
        }

        BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(this.player.getId());
        Set<Integer> outSidemarchs = new HashSet<>();
        for (IWorldMarch march : marchs) {
            int idx = march.getMarchEntity().getAutoResourceIdentify();
            if (idx > 0) {
                //这里取得是在外面拉锅的队伍
                outSidemarchs.add(idx);
            }
        }
        this.autoResouceParam.
                tmpDebugLog("autoresource player: {} autoMarchResourceStation outSidemarchs size: {} MarchInfos size: {}",
                        player.getId(), outSidemarchs.size(), this.autoResouceParam.getMarchInfos().size());
        int marchCount = 0;
        int outCount = outSidemarchs.size();
        int outLimit = this.autoResouceParam.getMarchInfos().size();
        //出征
        for (AutoMarchInfo autoMarch : this.autoResouceParam.getMarchInfos()) {
            if (outSidemarchs.contains(autoMarch.getIndex())) {
                this.autoResouceParam.
                        tmpDebugLog("autoresource player: {} autoMarchResourceStation outSidemarchs.contains continue index: {}",
                                player.getId(), autoMarch.getIndex());
                continue;
            }

            String targetId = String.valueOf(point.getResourceId());
            // 检查资源配置
            ResTreasureCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ResTreasureCfg.class,
                    point.getResourceId());
            if (cfg == null) {
                this.autoResouceParam.
                        tmpDebugLog("autoresource player: {} autoMarchResourceStation cfg == null ResourceId: {}",
                                player.getId(), point.getResourceId());
                continue;
            }

            // 带兵出征通用检查
            EffectParams param = this.checkMarchReq(autoMarch, point);
            if (param == null) {
                continue;
            }
            // 扣兵
            if (!ArmyService.getInstance().checkArmyAndMarch(player, param.getArmys(), autoMarch.getHeroIds(), autoMarch.getSuperSoldierId())) {
                this.autoResouceParam.
                        tmpDebugLog("autoresource player: {} autoMarchResourceStation checkArmyAndMarch",
                                player.getId());
                continue;
            }
            IWorldMarch march = WorldMarchService.getInstance().startMarch(
                    player, WorldMarchType.COLLECT_RES_TREASURE_VALUE, point.getId(), targetId, null, 0, 0, autoMarch.getIndex(),0, param);
            // 行为日志
            BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_COLLECT_RESOURCE,
                    Params.valueOf("marchData", march));

            WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
            builder.setSuccess(true);
            this.player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_COLL_TREASURE_S, builder));
            marchCount++;
            if(marchCount + outCount >= outLimit){
                this.autoResouceParam.
                        tmpDebugLog("autoresource player: {} marchCount + outCount >= outLimit break {} {} {}",
                                player.getId(), marchCount, outCount, outLimit);
                break;
            }
        }
    }

    private boolean isAutoKillMonster() {
        AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
        if (autoMarchParam != null) {
            return true;
        }
        return false;
    }

    public EffectParams checkMarchReq(AutoMarchInfo autoMarch, WorldPoint worldPoint) {

        List<ArmyInfo> armyList = autoMarch.getArmy();
        List<Integer> heros = autoMarch.getHeroIds();
        int armourSuit = autoMarch.getArmourSuitType();
        int superSoldierId = autoMarch.getSuperSoldierId();
        int lab = autoMarch.getSuperLab();
        int talent = autoMarch.getTalent();
        if (WorldUtil.isOwnPoint(player.getId(), worldPoint)) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} checkMarchReq isOwnPoint",
                            player.getId());
            return null;
        }
        if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} checkMarchReq isHasFreeMarch",
                            player.getId());
            return null;
        }
        if ((armyList == null || armyList.size() == 0)) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} checkMarchReq armyList == null",
                            player.getId());
            return null;
        }
        for (ArmyInfo marchArmy : armyList) {
            if (marchArmy.getTotalCount() <= 0) { // 防止刷兵
                this.autoResouceParam.
                        tmpDebugLog("autoresource player: {} checkMarchReq getTotalCount",
                                player.getId());
                return null;
            }
        }
        if (heros.size() > 2) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} checkMarchReq heros.size() > 2",
                            player.getId());
            return null;
        }

        // 检查英雄出征
		if (!ArmyService.getInstance().heroCanMarch(player, heros)) {
			this.autoResouceParam.tmpDebugLog("autoresource player: {} checkMarchReq heroCanMarch",
					player.getId());
			return null;
		}
		
		if (!ArmyService.getInstance().superSoldierCsnMarch(player, superSoldierId)) {
			this.autoResouceParam.
			tmpDebugLog("autoresource player: {} checkMarchReq SUPER_SOLDIER_STATE_FREE",
					player.getId());
			return null;
		}

        if (lab != 0 && !player.isSuperLabActive(lab)) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} checkMarchReq isSuperLabActive",
                            player.getId());
            return null;
        }

        // 士兵总数
        int totalCnt = 0;
        for (ArmyInfo marchArmy : armyList) {
            totalCnt += marchArmy.getTotalCount();
        }

        EffectParams effParams = new EffectParams();
        effParams.setArmys(armyList);
        effParams.setHeroIds(heros);
        effParams.setSuperSoliderId(superSoldierId);
        effParams.setArmourSuit(ArmourSuitType.valueOf(armourSuit));
        effParams.setMechacoreSuit(MechaCoreSuitType.valueOf(autoMarch.getMechacoreSuit()));
        effParams.setSuperSoliderId(superSoldierId);
        TalentType talentType = TalentType.valueOf(talent);
        if (talentType != null) {
            effParams.setTalent(talent);
        } else {
            effParams.setTalent(TalentType.TALENT_TYPE_DEFAULT_VALUE);
        }

        int maxMarchSoldierNum = player.getMaxMarchSoldierNum(effParams);
        if (totalCnt > maxMarchSoldierNum) {
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} checkMarchReq totalCnt > maxMarchSoldierNum totalCnt: {} maxMarchSoldierNum: {}",
                            player.getId(), totalCnt, maxMarchSoldierNum);
            return null;
        }
        
		int troop = autoMarch.getAutoMarchPB().getTroops();
		if (troop != 0) {
			WorldMarchReq.Builder marchReq = WorldMarchReq.newBuilder();
			marchReq.setFormation(autoMarch.getAutoMarchPB().getTroops());
			marchReq.setPosX(0);
			marchReq.setPosY(0);
			effParams.setWorldmarchReq(marchReq.build());
		}
        return effParams;
    }


    @ProtocolHandler(code = code.AUTO_GATHER_PARA_SETUP_REQ_VALUE)
    private boolean AutoGatherParaSetupReq(HawkProtocol protocol) {
        AutoGatherParaSetupReq req = protocol.parseProtocol(AutoGatherParaSetupReq.getDefaultInstance());
        this.putPosEmptyTimer = -1;
        this.autoResouceParam.
                tmpDebugLog("autoresource player: {} AutoGatherParaSetupReq putPosEmptyTimer set -1",
                        player.getId());
        if (isAutoKillMonster()){
            this.autoResouceParam.tmpDebugLog("autoresource player: {} AutoGatherParaSetupReq isAutoKillMonster",
                    player.getId());
            this.player.sendError(code.AUTO_GATHER_PARA_SETUP_REQ_VALUE,
                    AutoGatherErr.AUTO_KILL_MONSTER_VALUE, 0);
            return true;
        }
        setFirstPut(true);
        //开启自动放锅
        startAutoPut(protocol, req);
        //开启自动拉锅
        startAutoMarch(protocol, req);
        //给客户端同步状态
        sendAutoPutStatus(this.autoResouceParam.isAutoStationPut(),
                this.autoResouceParam.isAutoStationMarch());

        this.player.responseSuccess(protocol.getType());
        return true;
    }

    private void startAutoMarch(HawkProtocol protocol, AutoGatherParaSetupReq req) {
        if (req.getGatherX() == -1 && req.getGatherY() == -1) {
            return;
        }
        int rlt = this.autoResouceParam.checkAutoMarch(player, req);
        if (rlt != 0) {
            this.player.sendError(protocol.getType(), rlt, 0);
            return;
        }
        this.autoResouceParam.setAutoStationMarch(true);
    }

    private boolean startAutoPut(HawkProtocol protocol, AutoGatherParaSetupReq req) {
        if (req.getPutY() == -1 && req.getPutX() == -1) {
            return false;
        }

        if( !req.getNormalResSel() && !req.getSuperResSel() && !req.getHugeResSel()){
            return false;
        }

        int rlt = this.autoResouceParam.checkAutoPut(player, req);
        if (rlt != 0) {
            this.player.sendError(protocol.getType(), rlt, 0);
            //障碍是锅，不影响开启自动放锅
            if(rlt != AutoGatherErr.AUTO_PUT_ALREADY_EXIST_VALUE){
                return false;
            }
        }

        this.autoResouceParam.setAutoStationPut(true);

        return true;
    }

    private void sendAutoPutStatus(boolean statusPut, boolean statusGather) {
        AutoGatherStatusNotice.Builder builder = AutoGatherStatusNotice.newBuilder();
        builder.setAutoPut(statusPut);
        builder.setAutoGather(statusGather);

        this.player.sendProtocol(HawkProtocol.valueOf(code.AUTO_GATHER_STATUS_NOTICE_VALUE, builder));
    }

    @ProtocolHandler(code = code.AUTO_GATHER_STOP_REQ_VALUE)
    private boolean AutoGatherStopReq(HawkProtocol protocol) {
        AutoGatherStopReq req = protocol.parseProtocol(AutoGatherStopReq.getDefaultInstance());

        if (null == this.autoResouceParam) {
            return true;
        }
        this.autoResouceParam.setAutoStationMarch(req.getStopAutoGather());
        this.autoResouceParam.setAutoStationPut(req.getStopAutoPut());
        this.putPosEmptyTimer = -1;
        this.autoResouceParam.
                tmpDebugLog("autoresource player: {} AutoGatherStopReq putPosEmptyTimer set -1",
                        player.getId());
        this.sendAutoPutStatus(this.autoResouceParam.isAutoStationPut(), this.autoResouceParam.isAutoStationMarch());
        int rlt = this.autoResouceParam.checkStart();
        if (rlt == 0) {
            this.player.responseSuccess(protocol.getType());
            return true;
        }
        this.player.sendError(protocol.getType(), rlt, 0);
        return true;
    }

    /**
     * 检查放锅地点空置时间，超过规定时长，停止自动放锅
     */
    private void checkPutPosEmpty(long curTime) {
        //非自动拉锅状态，不需要检测
        if (!this.autoResouceParam.isAutoStationMarch()) {
            return;
        }
        WorldPoint point = WorldPointService.getInstance().getWorldPoint(
                this.autoResouceParam.getStationMarchPos());
        //当前自动拉锅点上是锅，不继续处理
        if (point != null && point.getPointType() == WorldPointType.RESOURC_TRESURE_VALUE) {
            this.putPosEmptyTimer = -1;
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} point != null end putPosEmptyTimer",
                            player.getId());
            return;
        }
        //初值
        if (this.putPosEmptyTimer == -1) {
            this.putPosEmptyTimer = curTime;
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} start putPosEmptyTimer",
                            player.getId());
            return;
        }
        //开始空置计时
        long timer = ConstProperty.getInstance().getSpacePropVacant();
        //空置开始时间+空置允许时间，停止自动放锅
        if (timer + this.putPosEmptyTimer <= curTime) {
            this.autoResouceParam.setAutoStationMarch(false);
            this.putPosEmptyTimer = -1;
            this.autoResouceParam.
                    tmpDebugLog("autoresource player: {} checkPutPosEmpty AUTO_PUT_RES_INTERUPT",
                            player.getId());
            //发送邮件
            SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                    .setPlayerId(player.getId())
                    .setMailId(MailId.AUTO_PUT_RES_INTERUPT)
                    .build());
            sendAutoPutStatus(this.autoResouceParam.isAutoStationPut(), this.autoResouceParam.isAutoStationMarch());
        }
    }

    public boolean isFirstPut() {
        return firstPut;
    }

    public void setFirstPut(boolean firstPut) {
        this.firstPut = firstPut;
    }

    public boolean isCityMoving() {
        return cityMoving;
    }

    public void setCityMoving(boolean cityMoving) {
        this.cityMoving = cityMoving;
    }
}
