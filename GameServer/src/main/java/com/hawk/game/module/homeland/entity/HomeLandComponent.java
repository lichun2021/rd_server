package com.hawk.game.module.homeland.entity;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.homeland.cfg.*;
import com.hawk.game.module.homeland.rank.HomeLandPlayerRankInfo;
import com.hawk.game.module.homeland.rank.HomeLandRank;
import com.hawk.game.module.homeland.rank.HomeLandService;
import com.hawk.game.module.homeland.rank.HomeLandRankType;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.HomeLand;
import com.hawk.log.Action;
import com.hawk.log.LogConst;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import java.util.*;
import java.util.stream.Collectors;

public class HomeLandComponent {
    //地图建筑
    private final HLMapBuildComp mapBuildComp = new HLMapBuildComp();
    //背包
    private final HLWareHouseComp wareHouseComp = new HLWareHouseComp();
    //点赞列表
    private final HLLikeComp likeComp = new HLLikeComp();
    //图鉴
    private final HLCollectComp collectComp = new HLCollectComp();
    //主题
    private final HLThemeComp themeComp = new HLThemeComp();
    //激活的属性
    private final HLAttrComp attrComp = new HLAttrComp();
    //激活的属性
    private final HLShopComp shopComp = new HLShopComp();

    private final PlayerHomeLandEntity entity;

    private Map<Const.EffType, Integer> effectMap = ImmutableMap.of();
    /**
     * 上次刷新时间
     */
    private long lastTickTime = 0;

    public HomeLandComponent(PlayerHomeLandEntity entity) {
        this.entity = entity;

    }

    public void init() {
        this.mapBuildComp.mergeFrom(entity.getBuildingData());
        this.wareHouseComp.mergeFrom(entity.getWarehouseData());
        this.likeComp.mergeFrom(entity.getDailyLikes());
        this.collectComp.mergeFrom(entity.getBuildingCollect());
        this.themeComp.mergeFrom(entity.getThemes());
        this.attrComp.mergeFrom(entity.getActiveProsperityAttr());
        this.shopComp.mergeFrom(entity.getShopInfo());
        loadEffectVal();
    }

    public boolean CheckLikeAndSet(String playerId) {
        boolean isLiked = false;
        if (likeComp.getDailyLikeList().containsKey(playerId)) {
            likeComp.getDailyLikeList().remove(playerId);
        } else {
            likeComp.getDailyLikeList().put(playerId, HomeLandDailyLikes.valueOf(playerId));
            isLiked = true;
        }
        entity.notifyUpdate();
        return isLiked;
    }

    public void initHomeLandWareHouse() {
        HomeLandMapCfg mapCfg = HawkConfigManager.getInstance().getConfigByIndex(HomeLandMapCfg.class, 0);
        if (mapCfg == null) {
            return;
        }
        if (!themeComp.getThemeList().contains(mapCfg.getMapId())) {
            HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
            entity.setThemeId(mapCfg.getMapId());
            themeComp.getThemeList().add(mapCfg.getMapId());
            AwardItems awardItems = AwardItems.valueOf();
            awardItems.addItemInfos(cfg.getSendCurrency());
            awardItems.rewardTakeAffectAndPush(getParent(), Action.HOME_LAND_INIT);
        }
    }

    private void loadEffectVal() {
        Map<Const.EffType, Integer> homeLandTmp = new HashMap<>();
        ConfigIterator<HomeLandBuildingTypeCfg> typeCfgConfigIterator = HawkConfigManager.getInstance().getConfigIterator(HomeLandBuildingTypeCfg.class);
        for (HomeLandBuildingTypeCfg buildingTypeCfg : typeCfgConfigIterator) {
            List<HomeLandBuilding> topBuildingsOfMultiTypes = mapBuildComp.getBuildingMap().values().stream()
                    .filter(building -> buildingTypeCfg.getBuildType() == building.getBuildType())
                    .sorted(Comparator.comparingInt((HomeLandBuilding building) -> building.getBuildCfg().getLevel()).reversed())
                    .limit(buildingTypeCfg.getMaxNumber())
                    .collect(Collectors.toList());
            for (HomeLandBuilding topBuildingsOfMultiType : topBuildingsOfMultiTypes) {
                for (Map.Entry<Const.EffType, Integer> effectEntry : topBuildingsOfMultiType.getBuildCfg().getEffectMap().entrySet()) {
                    homeLandTmp.merge(effectEntry.getKey(), effectEntry.getValue(), Integer::sum);
                }
            }
        }

        for (int cfgId : attrComp.getActiveProsperityAttrSet()) {
            HomeLandProsperityAttrCfg attrCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandProsperityAttrCfg.class, cfgId);
            if (attrCfg == null) {
                continue;
            }
            for (Map.Entry<Const.EffType, Integer> effectEntry : attrCfg.getEffectMap().entrySet()) {
                homeLandTmp.merge(effectEntry.getKey(), effectEntry.getValue(), Integer::sum);
            }
        }
        effectMap = ImmutableMap.copyOf(homeLandTmp);
    }

    public Player getParent() {
        return GlobalData.getInstance().makesurePlayer(entity.getPlayerId());
    }

    public void notifyCollectChanged() {
        Player player = getParent();
        if (player == null) {
            return;
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_COLLECT_S_VALUE, collectComp.buildCollectPush()));
        entity.notifyUpdate();
    }

    public void notifyWareHouseAndCollectChange(int updateBuildCfgId) {
        Player player = getParent();
        if (player == null) {
            return;
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_COLLECT_S_VALUE, collectComp.buildCollectPush()));
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_WARE_HOUSE_S_VALUE, wareHouseComp.buildWarHousePush().setUpdateBuildCfgId(updateBuildCfgId)));
        entity.notifyUpdate();
    }

    public void notifyWareHouseChanged() {
        Player player = getParent();
        if (player == null) {
            return;
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_WARE_HOUSE_S_VALUE, wareHouseComp.buildWarHousePush()));
        entity.notifyUpdate();
    }

    public void notifyProsperityChanged() {
        Player player = getParent();
        if (player == null) {
            return;
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_PROSPERITY_PUSH_S, buildProsperityPush()));
        entity.notifyUpdate();
    }

    public void notifyShopChange(List<HomeLand.HomeLandWareHousePB> awardWareHouse) {
        Player player = getParent();
        if (player == null) {
            return;
        }
        entity.notifyUpdate();
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_EXCHANGE_DRAW_S_VALUE, buildShopPush(awardWareHouse)));
    }

    public void notifyChanged() {
        Player player = getParent();
        if (player == null) {
            return;
        }
        updateHistoryProsperity();
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_WARE_HOUSE_S_VALUE, wareHouseComp.buildWarHousePush()));
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_PROSPERITY_PUSH_S, buildProsperityPush()));
        syncEffectHomeLand();
        player.refreshPowerElectric(LogConst.PowerChangeReason.HOME_LAND_MODULE);
        updateHomeLandRank(HomeLandRankType.PROSPERITY, mapBuildComp.getCurrentProsperity());
        entity.notifyUpdate();
    }

    public void updateHomeLandRank() {
        updateHomeLandRank(HomeLandRankType.PROSPERITY, getMapBuildComp().getCurrentProsperity());
        updateHomeLandRank(HomeLandRankType.LIKE, entity.getLikes());
    }

    private void updateHomeLandRank(HomeLandRankType rankType, long score) {
        Player player = getParent();
        if (player == null) {
            return;
        }
        HomeLandService.getInstance().updateRank(rankType, HomeLandRank.valueOf(player.getId(),
                player.getGuildId(), player.getMainServerId(), score), buildPlayerInfo());
    }

    public HomeLandPlayerRankInfo buildPlayerInfo() {
        HomeLandPlayerRankInfo playerInfo = new HomeLandPlayerRankInfo();
        Player player = getParent();
        if (player == null) {
            return null;
        }
        playerInfo.setPlayerId(player.getId());
        playerInfo.setPlayerName(player.getName());
        playerInfo.setGuildTag(player.getGuildTag());
        playerInfo.setIcon(player.getIcon());
        playerInfo.setPfIcon(player.getPfIcon());
        playerInfo.setServerId(player.getMainServerId());
        playerInfo.setGuildName(player.getGuildName());
        return playerInfo;
    }

    public void notifyProsperityAttrChange() {
        syncEffectHomeLand();
        getParent().sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_PROSPERITY_PUSH_S, buildProsperityPush()));
        entity.notifyUpdate();
    }

    public void syncEffectHomeLand() {
        try {
            loadEffectVal();
            List<Const.EffType> changeList = new ArrayList<>(effectMap.keySet());
            getParent().getEffect().syncEffect(getParent(), changeList.toArray(new Const.EffType[0]));
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    //获取作用号，建筑的和繁荣度的
    public int getEffect(Const.EffType effType) {
        return effectMap.getOrDefault(effType, 0);
    }

    public void dailyReset() {
        long now = HawkTime.getMillisecond();
        if (entity.getLastDailyLikeTime() <= 0) {
            entity.setLastDailyLikeTime(HawkTime.getMillisecond());
        }
        if (HawkTime.isCrossDay(now, entity.getLastDailyLikeTime(), 0)) {
            likeComp.getDailyLikeList().clear();
            entity.setLastDailyLikeTime(now);
            shopComp.setDailyDrawTimes(0);
            entity.notifyUpdate();
        }
    }

    public HomeLand.HomeLandExchangePush.Builder buildShopPush(List<HomeLand.HomeLandWareHousePB> awardWareHouse) {
        HomeLand.HomeLandExchangePush.Builder builder = HomeLand.HomeLandExchangePush.newBuilder();
        HomeLand.HomeLandExchangePB.Builder shopItem = buildShopPb();
        shopItem.addAllItems(awardWareHouse);
        builder.setShopInfo(shopItem);
        return builder;
    }

    public HomeLand.HomeLandExchangePB.Builder buildShopPb() {
        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
        HomeLand.HomeLandExchangePB.Builder builder = HomeLand.HomeLandExchangePB.newBuilder();
        builder.setDailyLeftTimes(cfg.getShopMaxTimes() - shopComp.getDailyDrawTimes());
        builder.setDrawTimes(shopComp.getDrawTimes());
        builder.setDrawCost(shopComp.getDrawCost());
        return builder;
    }

    public HomeLand.HPHomeLandBuildingInfoSync.Builder toPB(boolean isView, boolean liked) {
        HomeLand.HPHomeLandBuildingInfoSync.Builder builder = HomeLand.HPHomeLandBuildingInfoSync.newBuilder();
        builder.setThemeId(entity.getThemeId());
        builder.addAllBuildings(mapBuildComp.buildingPBList());
        builder.setProsperityInfo(buildProsperityPb());
        if (isView) {
            builder.addAllWareHouses(wareHouseComp.buildingWarHousePBList());
            builder.setShopInfo(buildShopPb());
        }
        builder.setCollect(collectComp.buildCollectPush().getCollect());
        builder.setLikeInfo(likeComp.buildLikePb(liked, entity.getLikes()));
        return builder;
    }

    public boolean checkProsperity(int limit) {
        return entity.getProsperity() < limit;
    }

    public boolean checkCanDisassembly(int buildCfgId, int amount) {
        if (!containsWareHouse(buildCfgId)) {
            return false;
        }
        int buildCount = wareHouseComp.getWareHouseMap().get(buildCfgId).getCount();
        return amount <= buildCount;
    }

    public static HomeLandComponent create(PlayerHomeLandEntity entity) {
        HomeLandComponent component = new HomeLandComponent(entity);
        component.init();
        entity.recordHomeLandObj(component);
        return component;
    }

    public void addMapBuild(String uuid, HomeLandBuilding buildToMap) {
        mapBuildComp.getBuildingMap().putIfAbsent(uuid, buildToMap);
    }

    public void removeMapBuild(String uuid) {
        mapBuildComp.getBuildingMap().remove(uuid);
    }

    public boolean checkMapBuildSetMax(int buildType, int max) {
        long buildingLimit = mapBuildComp.getBuildingMap().values().stream().filter(v -> v.getBuildType() == buildType).count();
        return buildingLimit < max;
    }

    public boolean containsBuild(String uuid) {
        return mapBuildComp.getBuildingMap().containsKey(uuid);
    }

    public boolean containsWareHouse(int buildCfgId) {
        HomeLandWareHouse wareHouse = wareHouseComp.getWareHouseMap().get(buildCfgId);
        if (wareHouse == null) {
            return false;
        }
        return wareHouse.getCount() > 0;
    }

    public Optional<HomeLandBuilding> getMainBuild(int mainBuildType) {
        return mapBuildComp.getBuildingMap().values().stream().filter(v -> v.getBuildType() == mainBuildType).findAny();
    }

    public HomeLandBuilding getHomeBuildById(String uuid) {
        return mapBuildComp.getBuildingMap().get(uuid);
    }

    public void addCollect(int buildType) {
        collectComp.getBuildingCollectList().put(buildType, HomeLandCollect.valueOf(buildType));
    }

    /**
     * 背包中的建筑是否满足升级条件
     *
     * @param itemInfos
     * @return
     */
    public List<ItemInfo> getItemCost(List<ItemInfo> itemInfos) {
        List<ItemInfo> temp = new ArrayList<>();
        for (ItemInfo itemInfo : itemInfos) {
            ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
            if (itemCfg == null) {
                continue;
            }
            if (itemCfg.getItemType() == Const.ToolType.HOME_LAND_BUILD_VALUE) {
                continue;
            }
            temp.add(itemInfo);
        }
        return temp;
    }

    /**
     * 背包中的建筑是否满足升级条件
     *
     * @param itemInfos
     * @return
     */
    public List<ItemInfo> getBuildWareHouseCost(List<ItemInfo> itemInfos) {
        List<ItemInfo> temp = new ArrayList<>();
        for (ItemInfo itemInfo : itemInfos) {
            ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
            if (itemCfg == null) {
                continue;
            }
            HomeLandBuildingCfg itemBuildCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, itemCfg.getBuildId());
            if (itemBuildCfg == null) {
                continue;
            }
            ItemInfo buildItemInfo = new ItemInfo(itemInfo.getType(), itemBuildCfg.getId(), itemInfo.getCount());
            temp.add(buildItemInfo);
        }
        return temp;
    }

    /**
     * 地图上的建筑是否满足升级条件
     *
     * @param itemInfos
     * @return
     */
    public boolean checkMapBuildCost(List<ItemInfo> itemInfos) {
        for (ItemInfo itemInfo : itemInfos) {
            int itemCount = Math.toIntExact(itemInfo.getCount());
            HomeLandWareHouse wareHouse = wareHouseComp.getWareHouseMap().get(itemInfo.getItemId());
            if (wareHouse == null) {
                return false;
            }
            if (wareHouse.getCount() < itemCount) {
                return false;
            }
        }
        return true;
    }

    /**
     * 背包中的建筑是否满足升级条件
     *
     * @param itemInfos
     * @return
     */
    public boolean checkWareHouseCost(List<ItemInfo> itemInfos, int buildId) {
        for (ItemInfo itemInfo : itemInfos) {
            int itemCount = Math.toIntExact(itemInfo.getCount());
            HomeLandWareHouse wareHouse = wareHouseComp.getWareHouseMap().get(itemInfo.getItemId());
            if (wareHouse == null) {
                return false;
            }
            if (buildId == itemInfo.getItemId()) {
                itemCount = itemCount + 1;
            }
            if (wareHouse.getCount() < itemCount) {
                return false;
            }
        }
        return true;
    }

    public void costWareHouse(List<ItemInfo> buildItemInfo) {
        for (ItemInfo itemInfo : buildItemInfo) {
            int itemCount = Math.toIntExact(itemInfo.getCount());
            removeWareHouse(itemInfo.getItemId(), itemCount);
        }
    }

    public void removeWareHouse(int buildCfgId, int count) {
        wareHouseComp.removeWareHouse(buildCfgId, count);
    }

    public void removeWareHouse(int buildCfgId) {
        removeWareHouse(buildCfgId, 1);
    }

    public void addWareHouse(int buildCfgId, int buildType, int amount) {
        wareHouseComp.addWareHouse(buildCfgId, amount);
        addCollect(buildType);
    }

    public void addWareHouse(int buildCfgId, int buildType) {
        wareHouseComp.addWareHouse(buildCfgId, 1);
        addCollect(buildType);
    }

    public HomeLand.HomeLandProsperityPush.Builder buildProsperityPush() {
        HomeLand.HomeLandProsperityPush.Builder builder = HomeLand.HomeLandProsperityPush.newBuilder();
        builder.setProsperity(buildProsperityPb());
        return builder;
    }

    public HomeLand.HomeLandProsperityPB.Builder buildProsperityPb() {
        HomeLand.HomeLandProsperityPB.Builder builder = HomeLand.HomeLandProsperityPB.newBuilder();
        builder.setProsperity(mapBuildComp.getCurrentProsperity());
        builder.setHistoryProsperity(entity.getProsperity());
        builder.addAllActiveProsperities(attrComp.getActiveProsperityAttrSet());
        return builder;
    }

    public HomeLand.HomeLandThemeLikePush.Builder buildLikePush(boolean liked) {
        return likeComp.buildLikePush(liked, entity.getLikes());
    }

    public void updateHistoryProsperity() {
        long currentProsperity = mapBuildComp.getCurrentProsperity();
        if (this.entity.getProsperity() < currentProsperity) {
            this.entity.setProsperity(currentProsperity);
        }
    }

    public void notifyLikeChange(boolean isLiked) {
        int addLiked = isLiked ? entity.getLikes() + 1 : entity.getLikes() - 1;
        entity.setLikes(addLiked);
        entity.notifyUpdate();
        updateHomeLandRank(HomeLandRankType.LIKE, entity.getLikes());
    }

    public HomeLand.HomeLandCollectRecruitsPush.Builder buildCollectPush() {
        HomeLand.HomeLandCollectRecruitsPush.Builder builder = HomeLand.HomeLandCollectRecruitsPush.newBuilder();
        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
        builder.setNextHarvestTime(getShopComp().getLastCollectRecruit() + cfg.getCollectRecruitPushCd());
        return builder;
    }

    public void pushBuildCollect() {
        Player player = getParent();
        if (player != null) {
            if (getShopComp().getLastCollectRecruit() == 0) {
                getShopComp().setLastCollectRecruit(HawkTime.getMillisecond());
            }
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.HOME_BUILDING_COLLECT_RECRUITS_S, buildCollectPush()));
        }
    }

    //推送可收取
    public void onTick() {
        long currentTime = HawkApp.getInstance().getCurrentTime();
        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
        if (lastTickTime > 0 && currentTime >= lastTickTime + cfg.getCollectRecruitPushCd()) {
            lastTickTime = currentTime;
            pushBuildCollect();
        }
    }

    public long getRankScore(HomeLandRankType rankType) {
        long score = 0;
        Player player = getParent();
        if (player == null) {
            return score;
        }
        if (!player.checkHomeLandFuncUnlock()) {
            return score;
        }
        if (rankType == HomeLandRankType.PROSPERITY) {
            score = getMapBuildComp().getCurrentProsperity();
        } else {
            score = entity.getLikes();
        }
        return score;
    }

    public HLMapBuildComp getMapBuildComp() {
        return mapBuildComp;
    }

    public HLWareHouseComp getWareHouseComp() {
        return wareHouseComp;
    }

    public HLLikeComp getLikeComp() {
        return likeComp;
    }

    public HLCollectComp getCollectComp() {
        return collectComp;
    }

    public HLThemeComp getThemeComp() {
        return themeComp;
    }

    public HLAttrComp getAttrComp() {
        return attrComp;
    }

    public HLShopComp getShopComp() {
        return shopComp;
    }
}
