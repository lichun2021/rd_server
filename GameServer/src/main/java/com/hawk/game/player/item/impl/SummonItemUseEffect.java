package com.hawk.game.player.item.impl;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.module.PlayerWorldModule;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.object.MapBlock;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.game.world.thread.tasks.SummonMonsterTask;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import java.util.List;
import java.util.Optional;

public class SummonItemUseEffect extends AbstractItemUseEffect {

    @Override
    public int itemType() {
        return Const.ToolType.CALL_MONSTER_VALUE;
    }

    @Override
    public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
        if (checkSummon(player, itemCfg) <= 0) {
            player.sendError(protoType, Status.Error.ITEM_NOT_FOUND_VALUE, 0);
            return false;
        }
        return true;
    }

    private int checkSummon(Player player, ItemCfg itemCfg) {
        PlayerWorldModule module = player.getModule(GsConst.ModuleType.WORLD_MODULE);
        // 野怪id
        int monsterId = itemCfg.getWorldEnemy();
        WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
        // 野怪配置为空
        if (monsterCfg == null) {
            HawkLog.errPrintln("SummonItem failed, monsterCfg null, playerId:{}, itemId:{}, monsterId:{}", player.getId(), itemCfg.getId(), monsterId);
            return -1;
        }
        Optional<ActivityBase> opActivityBase = ActivityManager.getInstance().getActivity(monsterCfg.getRelateActivity());
        if (opActivityBase.isPresent()) {
            ActivityBase activityBase = opActivityBase.get();
            if (!activityBase.isOpening(player.getId()) && activityBase.getActivityType().equals(ActivityType.RADIATION_WAR_TWO_ACTIVITY)) {
                return -1;
            }
        }
        //玩家坐标
        int[] pos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
        String disCfgStr = ConstProperty.getInstance().getWorldEnemyDistance();
        String[] disCfg = disCfgStr.split("_");
        int minDis = Integer.parseInt(disCfg[0]);  //2距离范围
        int maxDis = 200;
        List<Point> freePoint = module.searchFreeNearPointPos(pos, minDis, maxDis);
        //找到点返回
        int monsterPointId = 0;
        for (Point point : freePoint) {
            // 超出地图范围
            int posX = point.getX();
            int posY = point.getY();
            int pointId = point.getId();
            int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
            int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
            if (posX >= worldMaxX || posX <= 0 || posY >= worldMaxY || posY <= 0) {
                continue;
            }
            // 请求点为阻挡点
            if (MapBlock.getInstance().isStopPoint(pointId)) {
                continue;
            }
            // 请求点再国王领地内
            if (WorldPointService.getInstance().isInCapitalArea(pointId)) {
                continue;
            }
            // 中心点不是空闲点
            Point centerPoint = WorldPointService.getInstance().getAreaPoint(posX, posY, true);
            if (centerPoint == null) {
                continue;
            }

            // 占用点奇偶判断
            if (!centerPoint.canMonsterGen(World.MonsterType.valueOf(monsterCfg.getType()))) {
                continue;
            }

            // 野怪占用半径
            int monsterRadius = WorldMonsterService.getInstance().getMonsterRadius(monsterId);

            // 获取周围点
            List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, monsterRadius);
            if (aroundPoints.size() != 2 * monsterRadius * (monsterRadius - 1)) {
                continue;
            }
            monsterPointId = pointId;
            break;
        }
        if (monsterPointId == 0) {
            HawkLog.errPrintln("SummonItem find free point failed,playerId:{}", player.getId());
            return -1;
        }
        return monsterPointId;
    }

    @Override
    public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
        int monsterPointId = checkSummon(player, itemCfg);
        if (monsterPointId <= 0) {
            return false;
        }
        int monsterId = itemCfg.getWorldEnemy();
        // 投递世界线程执行
        WorldThreadScheduler.getInstance().postWorldTask(new SummonMonsterTask(player.getId(), monsterPointId, monsterId));
        return true;
    }
}
