package com.hawk.game.gmscript;

import java.util.Map;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.util.GameUtil;

/**
 * 手动修改玩家平台（Android <-> iOS）
 * 
 * 使用方法:
 * http://localhost:8080/gmscript/changePlatform?playerId=7pt-46sj1e-1&toPlatform=ios
 * 
 * 参数说明:
 * - playerId: 玩家ID (必填)
 * - toPlatform: 目标平台 android 或 ios (必填)
 * 
 * 注意：数据库需要手动修改！
 * UPDATE player SET puid='openid#platform', platform='platform' WHERE id='playerId';
 * 
 * @author GM
 */
@HawkScript.Declare(id = "gmscript/changePlatform")
public class ChangePlatformHandler extends HawkScript {
    
    @Override
    public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
        String playerId = params.get("playerId");
        String toPlatform = params.get("toPlatform");
        
        // 参数校验
        if (HawkOSOperator.isEmptyString(playerId)) {
            return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "playerId is required");
        }
        
        if (HawkOSOperator.isEmptyString(toPlatform)) {
            return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "toPlatform is required (android or ios)");
        }
        
        toPlatform = toPlatform.toLowerCase();
        if (!toPlatform.equals("android") && !toPlatform.equals("ios")) {
            return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "toPlatform must be 'android' or 'ios'");
        }
        
        try {
            // 1. 获取玩家数据
            Player player = GlobalData.getInstance().makesurePlayer(playerId);
            if (player == null) {
                return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not found: " + playerId);
            }
            
            String openId = player.getOpenId();
            String serverId = player.getServerId();
            String oldPlatform = player.getPlatform();
            String playerName = player.getName();
            
            HawkLog.logPrintln("=== Change Platform Start ===");
            HawkLog.logPrintln("PlayerId: {}, OpenId: {}, ServerId: {}", playerId, openId, serverId);
            HawkLog.logPrintln("Old Platform: {}, New Platform: {}", oldPlatform, toPlatform);
            
            // 如果平台相同，无需修改
            if (oldPlatform.equals(toPlatform)) {
                return HawkScript.successResponse("platform is already " + toPlatform);
            }
            
            // 2. 先踢玩家下线
            if (player.isActiveOnline()) {
                player.kickout(com.hawk.game.protocol.Status.IdipMsgCode.IDIP_ACCOUNT_RESET_OFFLINE_VALUE, true, null);
                HawkLog.logPrintln("Player kicked offline");
            }
            
            // 3. Redis 操作 - AccountRoleInfo
            updateAccountRoleInfo(openId, serverId, playerId, playerName, oldPlatform, toPlatform);
            
            // 4. Redis 操作 - RecentServer
            updateRecentServer(openId, serverId, oldPlatform, toPlatform);
            
            // 5. Redis 操作 - 清理 AccountInfo 缓存
            String oldPuid = GameUtil.getPuidByPlatform(openId, oldPlatform);
            String cacheKey = "account_info:" + oldPuid + ":" + serverId;
            RedisProxy.getInstance().getRedisSession().del(cacheKey);
            HawkLog.logPrintln("Deleted Redis cache: {}", cacheKey);
            

            
            // 7. 内存操作 - 更新 AccountInfo 映射
            String newPuid = GameUtil.getPuidByPlatform(openId, toPlatform);
            updateMemoryAccountInfo(playerId, newPuid, serverId, playerName);
            
            HawkLog.logPrintln("=== Change Platform Success ===");
            
            JSONObject result = new JSONObject();
            result.put("playerId", playerId);
            result.put("openId", openId);
            result.put("oldPlatform", oldPlatform);
            result.put("newPlatform", toPlatform);
            result.put("oldPuid", oldPuid);
            result.put("newPuid", newPuid);
            result.put("message", "Redis and memory updated. Please manually update database!");
            result.put("sql", String.format(
                "UPDATE player SET puid='%s', platform='%s' WHERE id='%s';",
                newPuid, toPlatform, playerId
            ));
            
            return HawkScript.successResponse(result.toJSONString());
            
        } catch (Exception e) {
            HawkLog.errPrintln("Change platform failed: {}", e.getMessage());
            e.printStackTrace();
            return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, e.getMessage());
        }
    }
    
    /**
     * 更新 Redis AccountRoleInfo
     */
    private void updateAccountRoleInfo(String openId, String serverId, String playerId, 
                                       String playerName, String oldPlatform, String newPlatform) {
        try {
            // 获取旧的 AccountRoleInfo
            String key = "account_role:" + openId;
            String oldInnerKey = serverId + ":" + oldPlatform;
            String oldValue = RedisProxy.getInstance().getRedisSession().hGet(key, oldInnerKey);
            
            if (!HawkOSOperator.isEmptyString(oldValue)) {
                // 解析并修改
                JSONObject json = JSONObject.parseObject(oldValue);
                json.put("platform", newPlatform);
                
                // 删除旧记录
                RedisProxy.getInstance().getRedisSession().hDel(key, oldInnerKey);
                HawkLog.logPrintln("Deleted Redis AccountRoleInfo: {}:{}", key, oldInnerKey);
                
                // 添加新记录
                String newInnerKey = serverId + ":" + newPlatform;
                RedisProxy.getInstance().getRedisSession().hSet(key, newInnerKey, json.toJSONString());
                HawkLog.logPrintln("Added Redis AccountRoleInfo: {}:{}", key, newInnerKey);
            } else {
                // 如果 Redis 中没有，重新创建
                AccountRoleInfo roleInfo = GlobalData.getInstance().getAccountRoleInfo(playerId);
                if (roleInfo != null) {
                    roleInfo.setPlatform(newPlatform);
                    RedisProxy.getInstance().addAccountRole(roleInfo);
                    HawkLog.logPrintln("Created new AccountRoleInfo in Redis");
                }
            }
        } catch (Exception e) {
            HawkLog.errPrintln("Update AccountRoleInfo failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 更新 Redis RecentServer
     */
    private void updateRecentServer(String openId, String serverId, String oldPlatform, String newPlatform) {
        try {
            // RecentServer 的 key 格式: {areaId}:recent_server:{openId}
            // 尝试多种可能的格式
            String[] possibleKeys = {
                "recent_server:" + openId,
                "1:recent_server:" + openId,
                "2:recent_server:" + openId
            };
            
            for (String key : possibleKeys) {
                String oldInnerKey = serverId + ":" + oldPlatform;
                String oldValue = RedisProxy.getInstance().getRedisSession().hGet(key, oldInnerKey);
                
                if (!HawkOSOperator.isEmptyString(oldValue)) {
                    // 找到了，执行更新
                    String newInnerKey = serverId + ":" + newPlatform;
                    String timestamp = String.valueOf(HawkTime.getSeconds());
                    
                    // 删除旧记录
                    RedisProxy.getInstance().getRedisSession().hDel(key, oldInnerKey);
                    HawkLog.logPrintln("Deleted Redis RecentServer: {}:{}", key, oldInnerKey);
                    
                    // 添加新记录
                    RedisProxy.getInstance().getRedisSession().hSet(key, newInnerKey, timestamp);
                    HawkLog.logPrintln("Added Redis RecentServer: {}:{} = {}", key, newInnerKey, timestamp);
                    
                    break;
                }
            }
        } catch (Exception e) {
            HawkLog.errPrintln("Update RecentServer failed: {}", e.getMessage());
            throw e;
        }
    }
    

    
    /**
     * 更新内存中的 AccountInfo 映射
     */
    private void updateMemoryAccountInfo(String playerId, String newPuid, 
                                        String serverId, String playerName) {
        try {
            // 删除旧映射
            GlobalData.getInstance().removeAccountInfoOnly(playerId);
            HawkLog.logPrintln("Removed old AccountInfo from memory");
            
            // 添加新映射
            GlobalData.getInstance().updateAccountInfo(newPuid, serverId, playerId, 0, playerName);
            HawkLog.logPrintln("Updated AccountInfo in memory: puid={}", newPuid);
        } catch (Exception e) {
            HawkLog.errPrintln("Update memory AccountInfo failed: {}", e.getMessage());
            throw e;
        }
    }
}

