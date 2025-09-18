package com.hawk.game.player.strength;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.util.HawkClassScaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.SoldierStrengthBaseCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.player.strength.imp.bonus.StrengthBonusImp;
import com.hawk.game.player.strength.imp.bonus.StrengthType;
import com.hawk.game.player.strength.imp.core.StrengthBaseImp;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;

/**
 * 玩家战力工厂类
 * 
 * @author Golden
 *
 */
public class PlayerStrengthFactory {

	public static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 单例
	 */
	private static PlayerStrengthFactory instance;

	/**
	 * 战力基础
	 */
	private List<StrengthBaseImp> baseImps;
	
	/**
	 * 战力加成
	 */
	private Map<Integer, StrengthBonusImp> bonusImps;

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static PlayerStrengthFactory getInstance() {
		if (instance == null) {
			instance = new PlayerStrengthFactory();
		}
		return instance;
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		try {
			baseImps = new ArrayList<>();
			String packageName = StrengthBaseImp.class.getPackage().getName();
			ClassPath classPath = ClassPath.from(IHeroSkill.class.getClassLoader());
			ImmutableSet<ClassInfo> set = classPath.getTopLevelClasses(packageName);
			for (ClassInfo info : set) {
				Class<?> cls = info.load();
				if (cls.isInterface()) {
					continue;
				}
				baseImps.add((StrengthBaseImp) cls.newInstance());
			}
			
			bonusImps = new HashMap<>();
			packageName = StrengthBonusImp.class.getPackage().getName();
			List<Class<?>> classList = HawkClassScaner.scanClassesFilter(packageName, StrengthType.class);
			for (Class<?> cls : classList) {
				if (!cls.isAnnotationPresent(StrengthType.class)) {
					continue;
				}
				bonusImps.put(cls.getAnnotation(StrengthType.class).strengthType(), (StrengthBonusImp) cls.newInstance());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 计算战力
	 */
	public long calcStrength(Player player) {
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		String redisKey = "playerStrength" + player.getId();
		String stringValue = redisSession.getString(redisKey);
		if (!HawkOSOperator.isEmptyString(stringValue)) {
			logger.info("clac player strength, playerId:{}, strength:{}", player.getId(), Long.parseLong(stringValue));
			return Long.parseLong(stringValue);
		}
		
		long startTime = HawkTime.getMillisecond();
		
		JSONObject soldierStrength = new JSONObject();
		long strength = 0;
		for (int soldierType : GsConst.calcStrengthSoldierType) {
			try {
				long typeStrength = calcStrengtehBySolderType(player, SoldierType.valueOf(soldierType),false);
				strength = Math.max(typeStrength, strength);
				soldierStrength.put(String.valueOf(soldierType), typeStrength);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		strength = Math.max(strength, 0);
		redisSession.setString(redisKey, String.valueOf(strength));

		logger.info("calc player strength, playerId:{}, strength:{}, costTime:{}", player.getId(), strength, HawkTime.getMillisecond() - startTime);
		
		LogUtil.logStrength(player, soldierStrength.toJSONString(), strength);
		return strength;
	}
	
	/**
	 * 根据兵种类型获取战力
	 * @param soldierType
	 * @return
	 */
	public long calcStrengtehBySolderType(Player player, SoldierType soldierType,boolean detailLog) {
		int armyId = GameUtil.getMaxLevelArmyId(player, soldierType);
		if (armyId == 0) {
			return 0L;
		}
		
		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		int level = cfg.getLevel();
		int star = player.getSoldierStar(armyId);
		
		// 基础属性配置
		SoldierStrengthBaseCfg strengthBaseCfg = HawkConfigManager.getInstance().getCombineConfig(SoldierStrengthBaseCfg.class, level, star);
	
		// 基础属性加成
		int baseAtkAttr = 0;
		int baseHpAttr = 0;
		for (StrengthBaseImp baseImp : baseImps) {
			try {
				long startTime = HawkTime.getMillisecond();
				
				PlayerStrengthCell cell = new PlayerStrengthCell();
				baseImp.calc(player.getData(), soldierType, cell);
				baseAtkAttr += cell.getAtk();
				baseHpAttr += cell.getHp();
				//详细记录
				if(detailLog){
					logger.info("calc player strength detail,playerId:{}, SoldierType:{}, StrengthType:{}, atk:{}, hp:{}", 
							player.getId(), soldierType.getNumber(),baseImp.getStrengthType(), cell.getAtk(),cell.getHp());
				}
				long costTime = HawkTime.getMillisecond() - startTime;
				if (costTime > 20) {
					logger.info("calc player strength, cost too much time, playerId:{}, type:{}, costTime:{}", player.getId(), baseImp.getStrengthType(), costTime);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		// 额外属性加成
		int bounsAtkAttr = 0;
		int bounsHpAttr = 0;
		for (StrengthBonusImp bounsImp : bonusImps.values()) {
			try {
				long startTime = HawkTime.getMillisecond();
				
				PlayerStrengthCell cell = new PlayerStrengthCell();
				bounsImp.calc(player, soldierType, cell);
				bounsAtkAttr += cell.getAtk();
				bounsHpAttr += cell.getHp();
				//详细记录
				if(detailLog){
					logger.info("calc player strength detail,playerId:{}, SoldierType:{}, StrengthType:{}, atk:{}, hp:{}", 
							player.getId(), soldierType.getNumber(),bounsImp.getStrengthType(), cell.getAtk(),cell.getHp());
				}
				long costTime = HawkTime.getMillisecond() - startTime;
				if (costTime > 20) {
					logger.info("calc player strength, cost too much time, playerId:{}, type:{}, costTime:{}", player.getId(), bounsImp.getStrengthType(), costTime);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		double baseStrength = strengthBaseCfg.getBaseValue() * (GsConst.EFF_RATE + baseAtkAttr) * (GsConst.EFF_RATE + baseHpAttr) * GsConst.EFF_PER * GsConst.EFF_PER * GsConst.EFF_PER;
		double strength = baseStrength * (GsConst.EFF_RATE + bounsAtkAttr) * (GsConst.EFF_RATE + bounsHpAttr) * GsConst.EFF_PER * GsConst.EFF_PER;
		//详细记录
		if(detailLog){
			logger.info("calc player strength detail,playerId:{}, SoldierType:{}, SoldierTypeStrength:{}", player.getId(), soldierType.getNumber(),strength);
		}
		return (long) strength;
	}
}
