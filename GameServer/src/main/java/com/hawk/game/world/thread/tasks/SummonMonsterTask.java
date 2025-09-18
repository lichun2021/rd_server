package com.hawk.game.world.thread.tasks;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SummonMonsterEvent;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import java.util.List;

/**
 * 使用道具创建大世界monster
 *
 * @author zhy
 */
public class SummonMonsterTask extends WorldTask {
    int monsterPointId = 0;
    String playerId;
    int monsterId;

    public SummonMonsterTask(String playerId, int monsterPointId, int monsterId) {
        super(GsConst.WorldTaskType.WORLD_MONSTER_POINT_GENERATE);
        this.monsterPointId = monsterPointId;
        this.playerId = playerId;
        this.monsterId = monsterId;
    }


    @Override
    public boolean onInvoke() {
        WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
        // 野怪配置为空
        if (monsterCfg == null) {
            HawkLog.errPrintln("SummonItem failed, monsterCfg null, playerId:{}, monsterId:{}", playerId, monsterId);
            return false;
        }
        int[] pos = GameUtil.splitXAndY(monsterPointId);
        int posX = pos[0];
        int posY = pos[1];
        // 中心点不是空闲点
        Point centerPoint = WorldPointService.getInstance().getAreaPoint(posX, posY, true);
        if (centerPoint == null) {
            HawkLog.errPrintln("SummonItem failed, centerPoint point not free, playerId:{}, posX:{}, posY:{}", playerId, posX, posY);
            return false;
        }
        // 占用点奇偶判断
        if (!centerPoint.canMonsterGen(World.MonsterType.valueOf(monsterCfg.getType()))) {
            HawkLog.errPrintln("SummonItem failed, centerPoint can't gen monster point, playerId:{}, posX:{}, posY:{}", playerId, posX, posY);
            return false;
        }

        // 野怪占用半径
        int monsterRadius = WorldMonsterService.getInstance().getMonsterRadius(monsterId);

        // 获取周围点
        List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(monsterPointId, monsterRadius);
        if (aroundPoints.size() != 2 * monsterRadius * (monsterRadius - 1)) {
            HawkLog.errPrintln("SummonItem failed, arround points has been occupy, playerId:{}, posX:{}, posY:{}", playerId, posX, posY);
            return false;
        }
        // 中心点所在区域
        AreaObject areaObj = WorldPointService.getInstance().getArea(posX, posY);
        // 中心点所在资源带
        int zoneId = WorldUtil.getPointResourceZone(posX, posY);

        // 生成野怪点
        WorldPoint worldPoint = new WorldPoint(posX, posY, areaObj.getId(), zoneId, World.WorldPointType.MONSTER_VALUE);
        worldPoint.setMonsterId(monsterId);
        worldPoint.setLifeStartTime(HawkTime.getMillisecond());
        // 设置怪物血量
        int maxEnemyBlood = WorldMonsterService.getInstance().getMaxEnemyBlood(monsterId);
        worldPoint.setRemainBlood(maxEnemyBlood);

        // 创建玩家使用的世界点信息
        if (!WorldPointService.getInstance().createWorldPoint(worldPoint)) {
            HawkLog.errPrintln("SummonItem failed, createWorldPoint failed, playerId:{}, posX:{}, posY:{}", playerId, posX, posY);
            return false;
        }
        // 区域添加野怪boss点
        areaObj.addMonsterBoss(monsterPointId);
        Player player = GlobalData.getInstance().getActivePlayer(playerId);
        if (player != null) {
            //返回玩家消息
            World.WorldSummonItemResp.Builder resp = World.WorldSummonItemResp.newBuilder();
            resp.setTargetX(worldPoint.getX());
            resp.setTargetY(worldPoint.getY());
            resp.setSuccess(true);
            resp.setMonsterId(monsterId);
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.WORLD_SUMMON_ITEM_S, resp));
        }
        ActivityManager.getInstance().postEvent(new SummonMonsterEvent(playerId, monsterId));
        return true;
    }

}
