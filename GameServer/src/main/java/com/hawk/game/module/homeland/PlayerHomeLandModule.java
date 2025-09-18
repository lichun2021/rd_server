package com.hawk.game.module.homeland;

import com.hawk.activity.helper.PlayerAcrossDayLoginMsg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.homeland.cfg.*;
import com.hawk.game.module.homeland.entity.HomeLandBuilding;
import com.hawk.game.module.homeland.entity.HomeLandComponent;
import com.hawk.game.module.homeland.entity.PlayerHomeLandEntity;
import com.hawk.game.module.homeland.map.HomeLandMap;
import com.hawk.game.module.homeland.map.HomeLandMapComponent;
import com.hawk.game.module.homeland.rank.*;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.*;
import com.hawk.game.protocol.HomeLand.*;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * 家园模块
 *
 * @author zhy
 */
public class PlayerHomeLandModule extends PlayerModule {
    static final Logger logger = LoggerFactory.getLogger("Server");
    private final HomeLandMapComponent mapComponent = new HomeLandMapComponent();
    private final Map<HomeLandBuildingOperation, BiConsumer<HomeLandBuildingUpdateReq, Integer>> operationHandlers = new HashMap<>();
    /**
     * 功能是否已解锁
     */
    private boolean funcUnlocked = false;

    /**
     * 构造函数
     *
     * @param player
     */
    public PlayerHomeLandModule(Player player) {
        super(player);
        operationHandlers.put(HomeLandBuildingOperation.BUILDING_UPGRADE, this::upgradeMapBuild);
        operationHandlers.put(HomeLandBuildingOperation.BUILDING_PLACE, this::placeBuild);
        operationHandlers.put(HomeLandBuildingOperation.BUILDING_RECYCLE, this::recycleBuild);
        operationHandlers.put(HomeLandBuildingOperation.BUILDING_MOVE, this::moveBuild);
        operationHandlers.put(HomeLandBuildingOperation.BUILDING_UPGRADE_WARHOUSE, this::upgradeWareBuild);
        operationHandlers.put(HomeLandBuildingOperation.BUILDING_DISASSEMBLY, this::disassemblyBuild);
    }

    /**
     * 玩家登陆处理(数据同步)
     */
    @Override
    protected boolean onPlayerLogin() {
        if (player.isCsPlayer()) {
            return true;
        }
        checkFuncUnlock();
        if (funcUnlocked) {
            initHomeLand(player.getData().getHomeLandEntity());
            resetData();
        }
        player.getData().getHomeLandEntity().getComponent().pushBuildCollect();
        return true;
    }

    /**
     * 检测功能是否已解锁
     */
    private void checkFuncUnlock() {
        if (player.checkHomeLandFuncUnlock()) {
            funcUnlocked = true;
        }
    }

    /**
     * 玩家跨天消息事件
     *
     * @param msg
     * @return
     */
    @MessageHandler
    public boolean onCrossDayLogin(PlayerAcrossDayLoginMsg msg) {
        if (funcUnlocked) {
            resetData();
        }
        return true;
    }

    /**
     * 同步家园数据
     */
    public void syncHomeLandEntityInfo() {
        HomeLandComponent component = player.getData().getHomeLandEntity().getComponent();
        HawkProtocol protocol = HawkProtocol.valueOf(HP.code2.HOME_BUILDING_INFO_S_VALUE, component.toPB(true, false)
                .setPlayerId(player.getId())
                .setServerId(player.getMainServerId())
                .setGuildTag(player.getGuildTag())
                .setPlayerName(player.getName())
                .setIsLogin(true));
        sendProtocol(protocol);
    }

    @Override
    public boolean onTick() {
        HomeLandComponent component = player.getData().getHomeLandEntity().getComponent();
        component.onTick();
        return super.onTick();
    }

    //重置点赞
    private void resetData() {
        HomeLandComponent component = player.getData().getHomeLandEntity().getComponent();
        component.dailyReset();
    }

    private void initHomeLandView(PlayerHomeLandEntity entity) {
        for (HomeLandBuilding build : entity.getComponent().getMapBuildComp().getBuildingMap().values()) {
            mapComponent.addViewPoint(entity.getThemeId(), build);
        }
    }

    /**
     * 建筑放置,移动
     *
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code2.HOME_BUILDING_OPERATION_C_VALUE)
    public boolean onBuildingUpdate(HawkProtocol protocol) {
        if (!funcUnlocked) {
            sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_UNLOCK);
            return false;
        }
        HomeLandBuildingUpdateReq req = protocol.parseProtocol(HomeLandBuildingUpdateReq.getDefaultInstance());
        BiConsumer<HomeLandBuildingUpdateReq, Integer> handler = operationHandlers.get(req.getOperation());
        if (handler == null) {
            return false;
        }
        handler.accept(req, protocol.getType());
        return true;
    }

    /**
     * 回收
     */
    public void recycleBuild(HomeLandBuildingUpdateReq req, int protocolType) {
        String uuid = req.getUuid();
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        if (!component.containsBuild(uuid)) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_NOT_FIND);
            return;
        }
        HomeLandBuilding building = component.getHomeBuildById(uuid);
        //主建筑不能回收
        if (building.isMainBuild()) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_MAIN_BUILD_NOT_CYC);
            return;
        }
        HomeLandMap map = mapComponent.getMap(entity.getThemeId());
        if (map == null) {
            sendError(protocolType, Status.SysError.CONFIG_ERROR);
            return;
        }
        map.removeViewPoint(building);
        component.addWareHouse(building.getConfigId(), building.getBuildType());
        collectRecruit(building);
        component.removeMapBuild(uuid);
        logger.info("HomeLand player:{},recycleBuild:{},pox:{},poy:{}", player.getId(), uuid, building.getX(), building.getY());
        sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_UPDATE_S_VALUE, buildPushBuildingUpdate(req.getOperation(), building)));
        component.notifyChanged();
        player.responseSuccess(protocolType);
    }

    /**
     * 拆解建筑
     */
    public void disassemblyBuild(HomeLandBuildingUpdateReq req, int protocolType) {
        int buildCfgId = req.getBuildCfgId();
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        if (req.getDisassembly() <= 0) {
            sendError(protocolType, Status.SysError.PARAMS_INVALID);
            return;
        }
        if (!component.containsWareHouse(buildCfgId)) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_NOT_FIND);
            return;
        }
        if (!component.checkCanDisassembly(buildCfgId, req.getDisassembly())) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_DISASSEMBLY_NOT_ENOUGH);
            return;
        }
        HomeLandBuildingCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, buildCfgId);
        if (currCfg.getReclaimItems().isEmpty()) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_RECYCLE_INVALID);
            return;
        }
        List<ItemInfo> reclaim = currCfg.getReclaimItems();
        AwardItems awardItems = AwardItems.valueOf();
        for (int i = req.getDisassembly(); i > 0; i--) {
            awardItems.addItemInfos(reclaim);
        }
        awardItems.rewardTakeAffectAndPush(player, Action.HOME_LAND_RECYCLE, true);
        component.removeWareHouse(buildCfgId, req.getDisassembly());
        component.notifyWareHouseChanged();
        int left = component.containsWareHouse(buildCfgId) ? component.getWareHouseComp().getWareHouseMap().get(buildCfgId).getCount() : 0;
        logger.info("HomeLand player:{} DisassemblyBuild:{},disassembly:{},left:{}", player.getId(), buildCfgId, req.getDisassembly(), left);
        player.responseSuccess(protocolType);
    }

    /**
     * 升级背包中建筑
     */
    public void upgradeWareBuild(HomeLandBuildingUpdateReq req, int protocolType) {
        int buildCfgId = req.getBuildCfgId();
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        if (!component.containsWareHouse(buildCfgId)) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_NOT_FIND);
            return;
        }
//        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
//        Optional<HomeLandBuilding> mainBuild = component.getMainBuild(cfg.getMainBuildType());
//        if (!mainBuild.isPresent()) {
//            sendError(protocolType, Status.HOMELANDError.HOME_LAND_MAIN_UNLOCK);
//            return;
//        }
        HomeLandBuildingCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, buildCfgId);
        if (currCfg == null) {
            sendError(protocolType, Status.SysError.CONFIG_ERROR);
            return;
        }
        HomeLandBuildingCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, currCfg.getPostStage());
        if (nextLevelCfg == null) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_UPGRADE_MAX_LEVEL);
            return;
        }
        //其他建筑不能高于主建筑
//        if (nextLevelCfg.getBuildType() != cfg.getMainBuildType() && nextLevelCfg.getLevel() > mainBuild.get().getBuildCfg().getLevel()) {
//            logger.info("HomeLand upgradeWarBuild:{},mainBuildLevel:{},nextBuild:{}", buildCfgId, mainBuild.get().getBuildCfg().getLevel(), nextLevelCfg.getId());
//            sendError(protocolType, Status.HOMELANDError.HOME_LAND_UPGRADE_LEVEL);
//            return;
//        }
        //达到繁荣度
        if (component.checkProsperity(nextLevelCfg.getBuildProsperityLimit())) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_NOT_ENOUGH_PROSPERITY);
            return;
        }
        //下一等级的消耗
        List<ItemInfo> nextWareHouseCost = component.getBuildWareHouseCost(nextLevelCfg.getCostItem());
        if (!component.checkWareHouseCost(nextWareHouseCost, currCfg.getId())) {
            sendError(protocolType, Status.Error.ITEM_NOT_ENOUGH);
            return;
        }
        List<ItemInfo> nextCost = component.getItemCost(nextLevelCfg.getCostItem());
        if (!nextCost.isEmpty()) {
            ConsumeItems consume = ConsumeItems.valueOf();
            consume.addConsumeInfo(nextCost, false);
            if (!consume.checkConsume(player, protocolType)) {
                sendError(protocolType, Status.Error.ITEM_NOT_ENOUGH);
                return;
            }
            consume.consumeAndPush(player, Action.HOME_LAND_UPGRADE);
        }
        //消耗掉的
        component.costWareHouse(nextWareHouseCost);
        //删掉当前的
        component.removeWareHouse(currCfg.getId(), 1);
        //获得下一等级的
        component.addWareHouse(nextLevelCfg.getId(), nextLevelCfg.getBuildType());
        component.notifyWareHouseAndCollectChange(nextLevelCfg.getId());
        logHomeLandUpgradeBuild(player, 2, currCfg.getId(), nextLevelCfg.getId());
        player.responseSuccess(protocolType);
    }

    /**
     * 升级建筑
     */
    public void upgradeMapBuild(HomeLandBuildingUpdateReq req, int protocolType) {
        String uuid = req.getUuid();
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        if (!component.containsBuild(uuid)) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_NOT_FIND);
            return;
        }
//        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
//        Optional<HomeLandBuilding> mainBuild = component.getMainBuild(cfg.getMainBuildType());
//        if (!mainBuild.isPresent()) {
//            sendError(protocolType, Status.HOMELANDError.HOME_LAND_MAIN_UNLOCK);
//            return;
//        }
        HomeLandBuilding building = component.getHomeBuildById(uuid);
        HomeLandBuildingCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, building.getConfigId());
        if (currCfg == null) {
            sendError(protocolType, Status.SysError.CONFIG_ERROR);
            return;
        }
        HomeLandBuildingCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, currCfg.getPostStage());
        if (nextLevelCfg == null) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_UPGRADE_MAX_LEVEL);
            return;
        }
        //其他建筑不能高于主建筑
//        if (building.getBuildType() != cfg.getMainBuildType() && nextLevelCfg.getLevel() > mainBuild.get().getBuildCfg().getLevel()) {
//            logger.info("HomeLand upgradeMapBuild:{},mainBuildLevel:{},nextBuild:{}", uuid, mainBuild.get().getBuildCfg().getLevel(), nextLevelCfg.getId());
//            sendError(protocolType, Status.HOMELANDError.HOME_LAND_UPGRADE_LEVEL);
//            return;
//        }
        //达到繁荣度
        if (component.checkProsperity(nextLevelCfg.getBuildProsperityLimit())) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_NOT_ENOUGH_PROSPERITY);
            return;
        }
        //下一等级的消耗
        List<ItemInfo> nextWareHouseCost = component.getBuildWareHouseCost(nextLevelCfg.getCostItem());
        if (!component.checkMapBuildCost(nextWareHouseCost)) {
            sendError(protocolType, Status.Error.ITEM_NOT_ENOUGH);
            return;
        }
        List<ItemInfo> nextCost = component.getItemCost(nextLevelCfg.getCostItem());
        if (!nextCost.isEmpty()) {
            ConsumeItems consume = ConsumeItems.valueOf();
            consume.addConsumeInfo(nextCost, false);
            if (!consume.checkConsume(player, protocolType)) {
                sendError(protocolType, Status.Error.ITEM_NOT_ENOUGH);
                return;
            }
            consume.consumeAndPush(player, Action.HOME_LAND_UPGRADE);
        }
        building.setConfigId(nextLevelCfg.getId());
        component.costWareHouse(nextWareHouseCost);
        component.addCollect(nextLevelCfg.getBuildType());
        component.notifyCollectChanged();
        sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_UPDATE_S_VALUE, buildPushBuildingUpdate(req.getOperation(), building)));
        component.notifyChanged();
        logHomeLandUpgradeBuild(player, 1, currCfg.getId(), nextLevelCfg.getId());
        player.responseSuccess(protocolType);
    }

    /**
     * 移动建筑
     */
    public void moveBuild(HomeLandBuildingUpdateReq req, int protocolType) {
        String uuid = req.getUuid();
        int posX = req.getPosX();
        int posY = req.getPosY();
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        if (!component.containsBuild(uuid)) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_NOT_FIND);
            return;
        }
        HomeLandBuilding build = component.getHomeBuildById(uuid);
        // 检查是否能被占用
        HomeLandMap map = mapComponent.getMap(entity.getThemeId());
        if (map == null) {
            sendError(protocolType, Status.SysError.CONFIG_ERROR);
            return;
        }
        map.removeViewPoint(build);
        if (!map.canMove(posX, posY, build)) {
            logger.info("HomeLand moveBuild:{},pox:{},poy:{}", uuid, posX, posY);
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_INVALID_POS);
            return;
        }
        build.setX(posX);
        build.setY(posY);
        map.addViewPoint(build);
        entity.notifyUpdate();
        sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_UPDATE_S_VALUE, buildPushBuildingUpdate(req.getOperation(), build)));
        player.responseSuccess(protocolType);
    }

    /**
     * 放置建筑
     */
    public void placeBuild(HomeLandBuildingUpdateReq req, int protocolType) {
        int buildCfgId = req.getBuildCfgId();
        int posX = req.getPosX();
        int posY = req.getPosY();
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        if (!component.containsWareHouse(buildCfgId)) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_NOT_FIND);
            return;
        }
        //相同类型建筑判断最大数量
        //落下
        HomeLandBuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, buildCfgId);
        if (buildingCfg == null) {
            sendError(protocolType, Status.SysError.CONFIG_ERROR);
            return;
        }
        HomeLandBuildingTypeCfg buildingTypeCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingTypeCfg.class, buildingCfg.getBuildType());
        if (buildingTypeCfg == null) {
            sendError(protocolType, Status.SysError.CONFIG_ERROR);
            return;
        }
        if (!component.checkMapBuildSetMax(buildingTypeCfg.getBuildType(), buildingTypeCfg.getMaxSetNumber())) {
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_MAP_BUILD_MAX);
            return;
        }
        HomeLandBuilding buildToMap = HomeLandBuilding.valueOf(buildCfgId);
        // 检查是否能被占用
        HomeLandMap map = mapComponent.getMap(entity.getThemeId());
        if (map == null) {
            sendError(protocolType, Status.SysError.CONFIG_ERROR);
            return;
        }
        if (!map.canMove(posX, posY, buildToMap)) {
            logger.info("HomeLand placeBuild:{},pox:{},poy:{}", buildCfgId, posX, posY);
            sendError(protocolType, Status.HOMELANDError.HOME_LAND_INVALID_POS);
            return;
        }
        buildToMap.setX(posX);
        buildToMap.setY(posY);
        component.addMapBuild(buildToMap.getUid(), buildToMap);
        map.addViewPoint(buildToMap);
        component.removeWareHouse(buildCfgId);
        sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_UPDATE_S_VALUE, buildPushBuildingUpdate(req.getOperation(), buildToMap)));
        component.notifyChanged();
        player.responseSuccess(protocolType);
    }

    private HomeLandBuildingUpdatePush.Builder buildPushBuildingUpdate(HomeLandBuildingOperation operation, HomeLandBuilding... building) {
        HomeLandBuildingUpdatePush.Builder builder = HomeLandBuildingUpdatePush.newBuilder();
        builder.setOperation(operation);
        for (HomeLandBuilding homelandBuilding : building) {
            builder.addBuilding(homelandBuilding.toPB());
        }
        return builder;
    }

    /**
     * 查看自己或他人的家园
     *
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code2.HOME_BUILDING_INFO_C_VALUE)
    public boolean onHomeInfo(HawkProtocol protocol) {
        HomeLandBuildingInfoReq req = protocol.parseProtocol(HomeLandBuildingInfoReq.getDefaultInstance());
        if (HawkOSOperator.isEmptyString(req.getTargetPlayerId()) || HawkOSOperator.isEmptyString(req.getTargetServerId())) {
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }
        if (HawkOSOperator.isEmptyString(req.getPlayerId()) || HawkOSOperator.isEmptyString(req.getServerId())) {
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }
        String mainServerId = GlobalData.getInstance().getMainServerId(req.getServerId());
        String targetMainServerId = GlobalData.getInstance().getMainServerId(req.getTargetServerId());
        boolean liked = req.getLiked();
        if (GlobalData.getInstance().isLocalPlayer(req.getPlayerId())) {
            Player sourcePlayer = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
            if (sourcePlayer == null) {
                sendError(protocol.getType(), Status.SysError.PLAYER_INVALID);
                return false;
            }
            PlayerHomeLandEntity entity = sourcePlayer.getData().getHomeLandEntity();
            if (!sourcePlayer.checkHomeLandFuncUnlock()) {
                sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_UNLOCK);
                return false;
            }
            checkFuncUnlock();
            initHomeLand(entity);
            liked = entity.getComponent().getLikeComp().getDailyLikeList().containsKey(req.getTargetPlayerId());
        }
        if (GlobalData.getInstance().isLocalPlayer(req.getTargetPlayerId())) {
            Player targetPlayer = GlobalData.getInstance().makesurePlayer(req.getTargetPlayerId());
            if (targetPlayer == null) {
                CrossProxy.getInstance().sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, SysProtocol.HPErrorCode.newBuilder().setHpCode(protocol.getType())
                        .setErrCode(Status.SysError.PLAYER_INVALID_VALUE)
                        .setErrFlag(0)), mainServerId, req.getPlayerId());
                return false;
            }
            if (!targetPlayer.checkHomeLandFuncUnlock()) {
                CrossProxy.getInstance().sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, SysProtocol.HPErrorCode.newBuilder().setHpCode(protocol.getType())
                        .setErrCode(Status.HOMELANDError.HOME_LAND_VISIT_UNLOCK_VALUE)
                        .setErrFlag(0)), mainServerId, req.getPlayerId());
                return false;
            }
            PlayerHomeLandEntity entity = targetPlayer.getData().getHomeLandEntity();
            boolean isView = req.getPlayerId().equals(req.getTargetPlayerId());
            HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code2.HOME_BUILDING_INFO_S_VALUE, entity.getComponent().toPB(isView, liked)
                    .setPlayerId(targetPlayer.getId())
                    .setServerId(targetPlayer.getMainServerId())
                    .setGuildTag(targetPlayer.getGuildTag())
                    .setPlayerName(targetPlayer.getName())
                    .setIsLogin(false));
            CrossProxy.getInstance().sendProtocol(respProtocol, mainServerId, req.getPlayerId());
        } else {
            HomeLandBuildingInfoReq.Builder crossReq = HomeLandBuildingInfoReq.newBuilder();
            crossReq.mergeFrom(req);
            crossReq.setPlayerId(req.getPlayerId());
            crossReq.setServerId(mainServerId);
            crossReq.setTargetServerId(targetMainServerId);
            crossReq.setLiked(liked);
            HomeLandService.getInstance().visitHomeView(crossReq);
        }
        player.responseSuccess(protocol.getType());
        return true;
    }

    private void initHomeLand(PlayerHomeLandEntity entity) {
        mapComponent.init();
        entity.getComponent().initHomeLandWareHouse();
        initHomeLandView(entity);
    }

    /**
     * 商店抽奖
     *
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code2.HOME_BUILDING_EXCHANGE_DRAW_C_VALUE)
    public boolean onExchangeDraw(HawkProtocol protocol) {
        if (!funcUnlocked) {
            sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_UNLOCK);
            return false;
        }
        HomeLandExchangeDrawReq req = protocol.parseProtocol(HomeLandExchangeDrawReq.getDefaultInstance());
        if (req.getDrawType() != 1) {
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
        if (component.getShopComp().getDailyDrawTimes() >= cfg.getShopMaxTimes()) {
            sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_DAILY_LIMIT);
            return false;
        }
        int drawTimes = component.getShopComp().getDrawTimes();
        HomeLandGachaCfg gachaCfg = component.getShopComp().findPool(drawTimes);
        if (gachaCfg == null) {
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }
        ConsumeItems consume = ConsumeItems.valueOf();
        consume.addConsumeInfo(gachaCfg.getCostItem(), false);
        if (!consume.checkConsume(player, protocol.getType())) {
            sendError(protocol.getType(), Status.Error.ITEM_NOT_ENOUGH);
            return false;
        }
        consume.consumeAndPush(player, Action.HOME_LAND_SHOP_DRAW);
        int awardId = component.getShopComp().gacha(drawTimes + 1, gachaCfg);
        if (awardId <= 0) {
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }
        AwardItems awardItems = AwardItems.valueOf();
        awardItems.addAward(awardId);
        awardItems.rewardTakeAffectAndPush(player, Action.HOME_LAND_GET_BUILD);
        List<HomeLandWareHousePB> awardWareHouse = new ArrayList<>();
        for (ItemInfo awardItem : awardItems.getAwardItems()) {
            ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, awardItem.getItemId());
            if (itemCfg == null) {
                continue;
            }
            HomeLandBuildingCfg itemBuildCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, itemCfg.getBuildId());
            if (itemBuildCfg == null) {
                continue;
            }
            int itemCount = Math.toIntExact(awardItem.getCount());
            for (int count = itemCount; count > 0; count--) {
                HomeLandWareHousePB.Builder wareHouseBuilder = HomeLandWareHousePB.newBuilder();
                wareHouseBuilder.setBuildCfgId(itemBuildCfg.getId());
                wareHouseBuilder.setItemCount(1);
                awardWareHouse.add(wareHouseBuilder.build());
            }
        }
        component.getShopComp().setDrawTimes(component.getShopComp().getDrawTimes() + 1);
        component.getShopComp().setDailyDrawTimes(component.getShopComp().getDailyDrawTimes() + req.getDrawType());
        component.notifyShopChange(awardWareHouse);
        logger.info("HomeLand player:{}, drawType:{},gachaId:{},shopInfo:{}", player.getId(), req.getDrawType(), gachaCfg.getId(), component.getShopComp().toString());
        player.responseSuccess(protocol.getType());
        return true;
    }

    public void collectRecruit(HomeLandBuilding building) {
        AwardItems awardItems = AwardItems.valueOf();
        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
        long now = HawkTime.getMillisecond();
        if (building.getBuildCfg().getResPerHour() <= 0) {
            return;
        }
        long collectInterval = now - building.getLastHarvestTime();
        // 收取时间间隔小于1s时不让收取
        if (collectInterval < 1000) {
            return;
        }
        long res = resStore(building.getBuildCfg(), collectInterval);
        awardItems.addItem(Const.ItemType.TOOL_VALUE, cfg.getCurrencyItem().getItemId(), res);
        building.setLastHarvestTime(now);
        awardItems.rewardTakeAffectAndPush(player, Action.HOME_LAND_COLLECT_RES);
    }

    /**
     * 主建筑收取
     *
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code2.HOME_BUILDING_COLLECT_RECRUITS_C_VALUE)
    public boolean onCollectRecruit(HawkProtocol protocol) {
        if (!funcUnlocked) {
            sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_UNLOCK);
            return false;
        }
        HomeLandCollectRecruitsReq req = protocol.parseProtocol(HomeLandCollectRecruitsReq.getDefaultInstance());
        if (req.getUuidList().isEmpty()) {
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        long now = HawkTime.getMillisecond();
        //上一次的收取时间
        Map<HomeLandBuilding, Integer> buildingCollectIntervals = new HashMap<>();
        AwardItems awardItems = AwardItems.valueOf();
        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
        for (String uuid : req.getUuidList()) {
            if (!component.containsBuild(uuid)) {
                continue;
            }
            HomeLandBuilding building = component.getHomeBuildById(uuid);
            if (building.getBuildCfg().getResPerHour() <= 0) {
                continue;
            }
            long collectInterval = now - building.getLastHarvestTime();
            // 收取时间间隔小于1s时不让收取
            if (collectInterval < 1000) {
                logger.error("collect resource interval too short, playerId: {}, buildCfgId: {}, uuid: {}, lastTime: {}",
                        player.getId(), building.getConfigId(), uuid, building.getLastHarvestTime());
                continue;
            }
            long res = resStore(building.getBuildCfg(), collectInterval);
            awardItems.addItem(Const.ItemType.TOOL_VALUE, cfg.getCurrencyItem().getItemId(), res);
            building.setLastHarvestTime(now);
            buildingCollectIntervals.put(building, (int) collectInterval / 1000);
            logger.info("collect resource, playerId: {}, buildCfgId: {}, uuid: {}, lastTime: {}, timeLong: {}, res:{}",
                    player.getId(), building.getConfigId(), uuid, building.getLastHarvestTime(), collectInterval, res);
        }
        if (awardItems.hasAwardItem()) {
            awardItems.rewardTakeAffectAndPush(player, Action.HOME_LAND_COLLECT_RES);
        }
        if (!buildingCollectIntervals.isEmpty()) {
            component.getShopComp().setLastCollectRecruit(now);
            component.pushBuildCollect();
            entity.notifyUpdate();
        }
        sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_UPDATE_S_VALUE, buildPushBuildingUpdate(HomeLandBuildingOperation.BUILDING_COLLECT, buildingCollectIntervals.keySet().toArray(new HomeLandBuilding[0]))));
        player.responseSuccess(protocol.getType());
        return true;
    }

    /**
     * 可收取资源建筑当前储量
     *
     * @param buildCfg
     * @param timeLong 产出资源的时长
     * @return
     */
    public long resStore(HomeLandBuildingCfg buildCfg, long timeLong) {
        double realOutputRate = buildCfg.getResPerHour();
        // 资源建筑最大储量
        double realOutputLimit = realOutputRate / buildCfg.getResPerHour() * buildCfg.getResLimit();
        // 产出量
        long product = (long) (timeLong * 1.0D / GsConst.HOUR_MILLI_SECONDS * realOutputRate);
        product = (long) Math.min(realOutputLimit, product);
        return product;
    }

    /**
     * 点赞
     *
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code2.HOME_BUILDING_THEME_LIKE_C_VALUE)
    public boolean onHomeLike(HawkProtocol protocol) {
        HomeLandThemeLikeReq req = protocol.parseProtocol(HomeLandThemeLikeReq.getDefaultInstance());
        if (HawkOSOperator.isEmptyString(req.getTargetPlayerId()) || HawkOSOperator.isEmptyString(req.getTargetServerId())) {
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }
        if (HawkOSOperator.isEmptyString(req.getPlayerId()) || HawkOSOperator.isEmptyString(req.getServerId())) {
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }
        boolean isLiked = req.getLiked();
        String mainServerId = GlobalData.getInstance().getMainServerId(req.getServerId());
        String targetMainServerId = GlobalData.getInstance().getMainServerId(req.getTargetServerId());
        //推送点赞成功
        if (GlobalData.getInstance().isLocalPlayer(req.getPlayerId())) {
            Player sourcePlayer = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
            if (sourcePlayer == null) {
                sendError(protocol.getType(), Status.SysError.PLAYER_INVALID);
                return false;
            }
            if (!sourcePlayer.checkHomeLandFuncUnlock()) {
                sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_UNLOCK);
                return false;
            }
            PlayerHomeLandEntity sourceEntity = sourcePlayer.getData().getHomeLandEntity();
            HomeLandComponent component = sourceEntity.getComponent();
            HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
            if (!component.getLikeComp().getDailyLikeList().containsKey(req.getTargetPlayerId())) {
                if (component.getLikeComp().getDailyLikeList().size() >= cfg.getThumbsUpMaxTimes()) {
                    sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_REPEAT_LIKE);
                    return false;
                }
            }
            isLiked = component.CheckLikeAndSet(req.getTargetPlayerId());
        }
        if (GlobalData.getInstance().isLocalPlayer(req.getTargetPlayerId())) {
            Player targetPlayer = GlobalData.getInstance().makesurePlayer(req.getTargetPlayerId());
            if (targetPlayer == null) {
                CrossProxy.getInstance().sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, SysProtocol.HPErrorCode.newBuilder().setHpCode(protocol.getType())
                        .setErrCode(Status.SysError.PLAYER_INVALID_VALUE)
                        .setErrFlag(0)), mainServerId, req.getPlayerId());
                return false;
            }
            if (!targetPlayer.checkHomeLandFuncUnlock()) {
                CrossProxy.getInstance().sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, SysProtocol.HPErrorCode.newBuilder().setHpCode(protocol.getType())
                        .setErrCode(Status.HOMELANDError.HOME_LAND_VISIT_UNLOCK_VALUE)
                        .setErrFlag(0)), mainServerId, req.getPlayerId());
                return false;
            }
            PlayerHomeLandEntity targetEntity = targetPlayer.getData().getHomeLandEntity();
            targetEntity.getComponent().notifyLikeChange(isLiked);
            HawkProtocol crossProtocol = HawkProtocol.valueOf(HP.code2.HOME_BUILDING_LIKE_PUSH_S_VALUE, targetEntity.getComponent().buildLikePush(isLiked));
            logHomeLandLike(targetPlayer, req.getPlayerId(), targetEntity.getLikes(), isLiked);
            CrossProxy.getInstance().sendProtocol(crossProtocol, mainServerId, req.getPlayerId());
        } else {
            HomeLandThemeLikeReq.Builder corssBuilder = HomeLandThemeLikeReq.newBuilder();
            corssBuilder.mergeFrom(req);
            corssBuilder.setLiked(isLiked);
            corssBuilder.setServerId(mainServerId);
            corssBuilder.setTargetServerId(targetMainServerId);
            HomeLandService.getInstance().likeHomeView(corssBuilder);
        }
        player.responseSuccess(protocol.getType());
        return true;
    }

    /**
     * 切换主题
     *
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code2.HOME_BUILDING_CHANGE_THEME_C_VALUE)
    public boolean onChangeTheme(HawkProtocol protocol) {
        ChangeHomeLandThemeReq req = protocol.parseProtocol(ChangeHomeLandThemeReq.getDefaultInstance());
        int theme = req.getThemeId();
        //判断解锁条件
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        if (!component.getLikeComp().getDailyLikeList().containsKey(theme)) {
            sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_THEME_UNLOCK);
            return false;
        }
        HomeLandMap map = mapComponent.getMap(theme);
        if (map == null) {
            sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
            return false;
        }
        long now = HawkTime.getMillisecond();
        List<HomeLandBuilding> unPlaceBuild = new ArrayList<>();
        for (HomeLandBuilding build : component.getMapBuildComp().getBuildingMap().values()) {
            if (!map.tryOccupied(build.getX(), build.getY(), build.getWidth(), build.getHeight())) {
                build.setLastHarvestTime(now);
                map.addViewPoint(build);
            } else {
                unPlaceBuild.add(build);
            }
        }
        for (HomeLandBuilding homelandBuilding : unPlaceBuild) {
            component.addWareHouse(homelandBuilding.getConfigId(), homelandBuilding.getBuildType());
            component.removeMapBuild(homelandBuilding.getUid());
        }
        entity.setThemeId(theme);
        component.notifyChanged();
        player.responseSuccess(protocol.getType());
        return true;
    }

    /**
     * 拜访
     *
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code2.HOME_BUILDING_RANK_C_VALUE)
    public boolean onRankInfo(HawkProtocol protocol) {
        HomeLandRankReq req = protocol.parseProtocol(HomeLandRankReq.getDefaultInstance());
        HomeLandRankResp.Builder resp = HomeLandRankResp.newBuilder();
        resp.setRankType(req.getRankType());
        resp.setTab(req.getTab());
        HomeLandRankImpl rankImpl = HomeLandService.getInstance().getRankByType(HomeLandRankType.getValue(req.getTab()),
                HomeLandRankServerType.getValue(req.getRankType()), player.getGuildId());
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        long score = component.getRankScore(HomeLandRankType.getValue(req.getTab()));
        if (rankImpl == null) {
            resp.setMyRank(buildRankInfo(HomeLandRank.valueOf(player.getId(), score), player));
            sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_RANK_S_VALUE, resp));
            return false;
        }
        List<HomeLand.HomeLandRankMsg> rankList = rankImpl.getRankList();
        resp.addAllRankItems(rankList);
        HomeLandRank myRank = rankImpl.getRank(player.getId());
        if (myRank != null) {
            myRank.setScore(score);
            resp.setMyRank(buildRankInfo(myRank, player));
        }
        sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_RANK_S_VALUE, resp));
        player.responseSuccess(protocol.getType());
        return true;
    }

    /**
     * 加入联盟
     *
     * @return
     */
    @MessageHandler
    private boolean onGuildJoinMsg(GuildJoinMsg msg) {
        if (!funcUnlocked) {
            return true;
        }
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        component.updateHomeLandRank();
        return true;
    }

    /**
     * 退盟
     *
     * @param msg
     */
    @MessageHandler
    private void onQuitGuild(GuildQuitMsg msg) {
        if (!funcUnlocked) {
            return;
        }
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        component.updateHomeLandRank();
    }

    public HomeLand.HomeLandRankMsg buildRankInfo(HomeLandRank homeLandRank, Player playerInfo) {
        HomeLand.HomeLandRankMsg.Builder rankInfo = HomeLand.HomeLandRankMsg.newBuilder();
        rankInfo.setPlayerId(playerInfo.getId());
        rankInfo.setPlayerName(playerInfo.getName());
        rankInfo.setIcon(playerInfo.getIcon());
        rankInfo.setPfIcon(playerInfo.getPfIcon());
        rankInfo.setGuildTag(playerInfo.getGuildTag());
        rankInfo.setRank(homeLandRank.getRank() <= 0 ? -1 : homeLandRank.getRank());
        rankInfo.setScore(homeLandRank.getScore());
        rankInfo.setServerId(playerInfo.getMainServerId());
        rankInfo.setGuildName(playerInfo.getGuildName());
        return rankInfo.build();
    }

    /**
     * 激活繁荣度属性,一键激活
     *
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code2.HOME_BUILDING_PROSPERITY_ACTIVE_C_VALUE)
    public boolean onActiveProsperityAttr(HawkProtocol protocol) {
        if (!funcUnlocked) {
            sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_UNLOCK);
            return false;
        }
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        ConfigIterator<HomeLandProsperityAttrCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(HomeLandProsperityAttrCfg.class);
        if (cfgIter.isEmpty()) {
            sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
            return false;
        }
        Set<Integer> allAttr = new HashSet<>();
        AwardItems awardItems = AwardItems.valueOf();
        for (HomeLandProsperityAttrCfg attrCfg : cfgIter) {
            if (entity.getProsperity() < attrCfg.getNeedProsperity()) {
                continue;
            }
            if (component.getAttrComp().getActiveProsperityAttrSet().contains(attrCfg.getId())) {
                continue;
            }
            awardItems.addItemInfos(attrCfg.getRewardItem());
            allAttr.add(attrCfg.getId());
        }
        if (!allAttr.isEmpty()) {
            component.getAttrComp().getActiveProsperityAttrSet().addAll(allAttr);
            component.notifyProsperityAttrChange();
            logHomeLandActiveAttr(player, entity.getProsperity(), allAttr);
        }
        awardItems.rewardTakeAffectAndPush(player, Action.HOME_LAND_ACTIVE_ATTR, true);
        player.responseSuccess(protocol.getType());
        return false;
    }

    /**
     * 分享
     *
     * @param protocol
     * @return
     */
    @SuppressWarnings("deprecation")
    @ProtocolHandler(code = HP.code2.HOME_BUILDING_SHARE_C_VALUE)
    public boolean onHomeShare(HawkProtocol protocol) {
        if (!funcUnlocked) {
            sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_UNLOCK);
            return false;
        }
        HomeLandShareReq req = protocol.parseProtocol(HomeLandShareReq.getDefaultInstance());
        if (req.getShareTo() != 1 && req.getShareTo() != 2) {
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }
        if (HawkOSOperator.isEmptyString(req.getPlayerName())) {
            sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
            return false;
        }
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        long now = HawkTime.getMillisecond();
        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
        if (now - entity.getShareTime() < cfg.getShareCdTime()) {
            sendError(protocol.getType(), Status.HOMELANDError.HOME_LAND_LIKE_CD_VALUE);
            return false;
        }
        Const.NoticeCfgId noticeId = req.getPlayerId().equals(player.getId()) ? Const.NoticeCfgId.HOME_LAND_SELF_SHARE : Const.NoticeCfgId.HOME_LAND_OTHER_SHARE;
        Const.ChatType chatType = req.getShareTo() == 1 ? Const.ChatType.CHAT_WORLD : Const.ChatType.CHAT_ALLIANCE;
        ChatService.getInstance().addWorldBroadcastMsg(chatType, noticeId, player, req.getPlayerId(), req.getServerId(), req.getPlayerName());
        entity.setShareTime(now);
        player.responseSuccess(protocol.getType());
        return true;
    }

    /**
     * @param player
     */
    public static void logHomeLandActiveAttr(Player player, long prosperity, Set<Integer> attr) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("prosperity", prosperity);
            param.put("attrs", SerializeHelper.collectionToString(attr, SerializeHelper.BETWEEN_ITEMS));
            LogUtil.logActivityCommon(player, LogConst.LogInfoType.home_land_active_attr, param);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * @param player
     * @param operType 1:升级地图上建筑，2:升级背包中建筑
     */
    public static void logHomeLandUpgradeBuild(Player player, int operType, int buildId, int nextBuildId) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("operType", operType);
            param.put("buildId", buildId);
            param.put("nextBuildId", nextBuildId);
            LogUtil.logActivityCommon(player, LogConst.LogInfoType.home_land_upgrade, param);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * @param player
     */
    public static void logHomeLandLike(Player player, String targetPlayerId, int likes, boolean liked) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("targetPlayerId", targetPlayerId);
            param.put("likes", likes);
            param.put("liked", liked ? 1 : 0);
            LogUtil.logActivityCommon(player, LogConst.LogInfoType.home_land_like, param);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }
}
