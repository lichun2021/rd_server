package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkWeightFactor;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuple5;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.impl.hero1110.Buff12574;
import com.hawk.game.battle.effect.impl.hero1112.Debuff12611;
import com.hawk.game.battle.effect.impl.hero1114.Debuff12674;
import com.hawk.game.battle.guarder.GuarderPlayer;
import com.hawk.game.battle.sssSolomon.ISSSSolomonPet;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BattleSoldierHonorCfg;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Mail.PB12541Detail;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.RandomUtil;

/**
 * 战斗单元
 */
public abstract class BattleSoldier implements IBattleSoldier {
	/** 触发什么排下, 最多攻击次数 */
	public static final int QIAN_PAI_MAX = 10;
	private String uuid;
	/** 本家所携带的英雄. 不包括队友 */
	private List<Integer> heros = Collections.emptyList();
	/**
	 * 玩家id
	 */
	private String playerId = "";
	private String playerName = "";
	/**
	 * 战斗单元所属于部队
	 */
	private BattleTroop troop = null;
	/**
	 * 士兵id
	 */
	private int soldierId = 0;
	private int soldierStar = 0;
	private int soldierStep = 0;
	private int soldierSkillLevel = 0;
	private int soldierMilitaryLevel = 0;
	/**
	 * 士兵配置
	 */
	private BattleSoldierCfg soldierCfg = null;
	private BattleSoldierHonorCfg solderStarCfg;
	/**
	 * 攻击技能配置
	 */
	private Map<PBSoldierSkill, BattleSoldierSkillCfg> skillMap;

	private Table<BattleTupleType.Type, SoldierType, HawkTuple3<Integer, Integer, List<Integer>>> effPerNum;

	private Map<EffType, Integer> effMap;

	protected EnumMap<SoldierType, Double> kezhiXishuMap;
	/**
	 * 原始数量
	 */
	private int oriCnt = 0;
	/**
	 * 死亡数量
	 */
	private int deadCnt = 0;

	/** 影子部队数量 */
	private int shadowCnt = 0;
	/**黑洞中数量*/
	private int jiDiEnXi = 0;
	private int jiDiEn1536 = 0;
	private int jiDiEn1537 = 0;
	private String jiDiEnXiPid;
	private BattleSoldier bake1631;
	private double jijia1633Num;
	private int kunNa1652Help;
	private int kunNa1653Kill;
	// 12086转移死兵
	private int hero12086cnt;
	/**】军威：战斗开始前，使敌方 XX% 的部队军心动摇，无法参与战斗*/
	private int eff12111Cnt;

	/**
	 * 击杀数量 被击杀士兵ID - 数量
	 */
	private Map<BattleSoldier, Integer> killCntMap;
	private Map<SoldierType, Integer> killTypeCntMap;
	private double killPower;
	/**playerId:dead*/
	private Map<String, Integer> jiDiEnXiCntMap;

	private Map<Integer, Integer> roundDead;

	private HawkTuple3<Integer, Integer, Integer> eff12163Debuff = HawkTuples.tuple(0, 0, 0);
	private int eff12163DebuffExtry;
	protected int eff12163DebuffCnt;
	/**
	 * 坐标
	 */
	private int xPos = 0;
	/**
	 * 进入时间
	 */
	private int stepIn = 0;

	/** 默认攻击频率 */
	final static int BASE_ATK_FREQUENCY = 1000;

	/** 每一回合打出伤害总和   用Integer比较省*/
	private HashMultimap<Integer, Integer> rountHurtMap = HashMultimap.create(50, 3);
	/** 每一回合受到伤害总和   用Integer比较省*/
	private HashMultimap<Integer, Integer> rountHurtedMap = HashMultimap.create(50, 3);

	/** 各种原因额外获得的攻击力 */
	private int extryHurt;
	private int shadowDead;
	private double shadowRate;
	/**1528叠加层数*/
	private int debuf1528Cen;
	private int[] debuf1529Val;
	private int[] debuf1530Val;

	private int debfuSkill814Cen;
	private int[] debfuSkill814Val;
	public int debufSkill724Val;

	private BattleSoldier_1 bake1631Soldier; // 被谁保护
	private int dodgeCnt; // 闪避出发次数
	private int extrAtktimes; // 连击数
	private Map<Integer, Integer> debuff1639RoundMap;
	private int attackedCnt; // 受到攻击次数
	private int attackCnt; // 攻击次数
	private Map<Integer, Integer> buff1652RoundMap;

	// 处于无敌状态, 不可受伤
	private boolean invincible;
	// ssss 所罗门无敌
	private BattleSoldier solomonPet;
	private boolean sss1671BiZhong;

	private List<HawkTuple2<String, Object[]>> extryLog = new ArrayList<>();

	private int debuff12005ContinueRound;
	private int debuff12161ContinueRound;
	private int debuff12006Atk;
	private int debuff12007Def;
	private int debuff12008HP;
	private int debuff12063Atk;
	private int debuff12064Def;
	private int debuff12065HP;
	private int debuff12272Fatk;

	private int round12085;
	private int cnt12085;
	private int val12085;
	private int buffhp12092;
	private int buffhp12092Round;
	private BattleSoldier tank12086;
	// 12086 转移死亡数
	private int eff12086Zhuan;
	private int extround12112;
	private LinkedList<Integer> debuff12206;
	private int debuff12206Val;
	protected int debuff12517Val;
	private HawkTuple2<Integer, Integer> skill744Debuff;
	private HawkTuple2<Integer, Integer> buff12302 = HawkTuples.tuple(0, 0);
	private HawkTuple3<Integer, Integer, Integer> buff12333 = HawkTuples.tuple(0, 0, 0);
	private HawkTuple5<Integer, Integer, Integer, Integer, BattleSoldier_7> buff12337 = HawkTuples.tuple(0, 0, 0, 0, null);
	private HawkTuple2<Integer, Double> skill12362Debuff;
	private HawkTuple2<Integer, Double> skill11042Debuff;
	private HawkTuple2<Integer, Double> skill11043Debuff, skill12642Debuff;
	private HawkTuple3<Integer, Integer, BattleSoldier_4> skill12461Debuff;
	private HawkTuple2<Integer, Integer> skill12465Buff;
	private HawkTuple2<Integer, Integer> skill12116Buff, skill461Buff, man12701;
	protected Buff12574 buff12574 = new Buff12574();

	private int effect12116Maxinum;
	private BattleSoldier eff12541Soldier; // 控制的对方兵
	private int eff12541SoldierKill;
	protected int debuffEffect12553Value;
	protected int debuffEffect12554Value;
	protected int effect12515Num;
	protected int debuff12571Num;
	protected int debuffSkill24601;
	// 雷感状态
	protected Map<String, Debuff12611> sorek12611Debuff = new HashMap<>();
	protected Map<String, Debuff12674> astiaya12674Debuff = new HashMap<>();
	private Map<BattleSoldier_3, Integer> debuff34601 = new LinkedHashMap<>();
	private long forceFieldMarch;
	
	public void incrDebuf1528Val(int val1529, int val1530) {
		debuf1528Cen++;
		if (debuf1529Val == null) {
			debuf1529Val = new int[ConstProperty.getInstance().getEffect1529Maximum()];
			debuf1530Val = new int[ConstProperty.getInstance().getEffect1530Maximum()];
		}
		debuf1529Val[debuf1528Cen % debuf1529Val.length] = val1529;
		debuf1530Val[debuf1528Cen % debuf1530Val.length] = val1530;
	}

	public void incrDebfuSkill814Val(int val, int maxCen) {
		debfuSkill814Cen++;
		if (debfuSkill814Val == null) {
			debfuSkill814Val = new int[maxCen];
		}
		debfuSkill814Val[debfuSkill814Cen % debfuSkill814Val.length] = val;
	}

	/**
	 * 战斗单元构造
	 * 
	 * @param soldierCfg
	 *            配置对象
	 * @param count
	 *            数量>0
	 * @return
	 */
	public void init(Player player, BattleSoldierCfg soldierCfg, int count, int shadowCnt) {
		Preconditions.checkNotNull(soldierCfg, "BattleSoldier cfg can not be null");
		Preconditions.checkArgument(count > 0, "BattleSoldier count can not <= 0");
		this.playerId = player.getId();
		this.playerName = player.getName();
		this.killCntMap = new HashMap<>();
		this.killTypeCntMap = new HashMap<>();
		this.jiDiEnXiCntMap = new HashMap<>();
		this.roundDead = new HashMap<>();
		this.oriCnt = count;
		this.shadowCnt = shadowCnt;
		this.soldierId = soldierCfg.getId();
		this.soldierCfg = soldierCfg;
		// 影子部队万分比
		this.shadowRate = GsConst.EFF_RATE * shadowCnt / oriCnt;
		this.uuid = playerName + "_" + soldierId;
		// 影子部队比例校正
		shadowRate = Math.max(0, shadowRate);
		shadowRate = Math.min(10000, shadowRate);

		if (Objects.nonNull(player)) {
			soldierStar = player.getSoldierStar(soldierId);
			soldierStep = player.getSoldierStep(soldierId);
			soldierSkillLevel = player.getSoldierPlantSkillLevel(soldierId);
			soldierMilitaryLevel = player.getSoldierPlantMilitaryLevel(soldierId);
			if (soldierCfg.isPlantSoldier()) { // 最少也要8星才有泰能兵呢
				soldierStar = Math.max(8, soldierStar);
			}
		}
		this.solderStarCfg = HawkConfigManager.getInstance().getCombineConfig(BattleSoldierHonorCfg.class, soldierId, soldierStar, soldierSkillLevel, soldierMilitaryLevel);
		if (Objects.isNull(solderStarCfg)) {
			solderStarCfg = HawkConfigManager.getInstance().getConfigIterator(BattleSoldierHonorCfg.class).stream().filter(cfg -> cfg.getId() == soldierId).findAny().orElse(null);
		}

		Preconditions.checkNotNull(solderStarCfg, "BattleSoldierHonorCfg cfg can not be null soldierId = " + soldierId + " star = " + soldierStar + " slevel " + soldierSkillLevel);
		effPerNum = ConcurrentHashTable.create();
		effMap = new HashMap<>();

		Map<PBSoldierSkill, BattleSoldierSkillCfg> map = new HashMap<>();
		for (Integer skillId : solderStarCfg.getSkillIdList()) {
			BattleSoldierSkillCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, skillId);
			if (Objects.nonNull(skillCfg)) {
				map.put(skillCfg.getIndex(), skillCfg);
			}
		}
		skillMap = ImmutableMap.copyOf(map);

		kezhiXishuMap = new EnumMap<>(SoldierType.class);
	}

	public void initKezhi() {
		BattleSoldierSkillCfg kezhijineng = getSkill(keZhiJiNengId());
		String kezhiStr = kezhijineng.getP2();
		if (getTroop().getBattle().getDuntype() == DungeonMailType.DYZZ) {
			kezhiStr = kezhijineng.getP6();
		}
		if (StringUtils.isNotEmpty(kezhiStr)) {
			List<String> tvList = Splitter.on("|").omitEmptyStrings().splitToList(kezhiStr);
			for (String t_v : tvList) {
				String[] kvArr = t_v.split("_");
				kezhiXishuMap.put(SoldierType.valueOf(NumberUtils.toInt(kvArr[0])), NumberUtils.toDouble(kvArr[1]) / GsConst.RANDOM_MYRIABIT_BASE);
			}
		}
	}

	/** 克制技能id */
	protected abstract PBSoldierSkill keZhiJiNengId();

	protected PBSoldierSkill honor10SkillId() {
		return null;
	}
	
	/**战斗开始前*/
	public void beforeWarfare(){
		
	}

	/** 新回合开始 */
	public void roundStart() {
		sorek12611DebuffValRoundStart();
		kunNa1652();
		eff12541SoldierAtk();
		effect12701Atk();
	}

	private void eff12541SoldierAtk() {
		try {
			if (getBattleRound() % ConstProperty.getInstance().effect12541AtkRound != 0 || eff12541Soldier == null || !eff12541Soldier.isAlive()) {
				return;
			}
			List<BattleSoldier> enes = eff12541Soldier.getTroop().getSoldierList().stream().filter(BattleSoldier::isAlive).filter(BattleSoldier::isSoldier)
					.filter(s -> s != eff12541Soldier).collect(Collectors.toList());
			if (enes.isEmpty()) {
				return;
			}

			BattleSoldier defSoldier = HawkRand.randomObject(enes);
			// 伤害公式
			double hurtVal = eff12541Soldier.hurtValBase(defSoldier, true, true) * GsConst.EFF_PER * getEffVal(EffType.EFF_12541);
			hurtVal = defSoldier.forceField(this, hurtVal);
			int round = troop.getBattle().getBattleRound();
			rountHurtMap.put(round, (int) hurtVal);

			int curCnt = defSoldier.getFreeCnt();
			int maxKillCnt = (int) Math.ceil(4.0f * hurtVal / defSoldier.getHpVal(this));
			maxKillCnt = Math.max(1, maxKillCnt);
			int killCnt = Math.min(maxKillCnt, curCnt);
			int deadArmyCount = defSoldier.getTroop().getDeadArmyCount();
			int total12541Max = (int) (deadArmyCount * GsConst.EFF_PER * ConstProperty.getInstance().effect12541LoseAdjust);
			killCnt = Math.min(total12541Max - eff12541SoldierKill, killCnt);

			eff12541SoldierKill += killCnt;
			defSoldier.addDeadCnt(killCnt);
			addKillCnt(defSoldier, killCnt);
			addDebugLog("【12541】{}对其友方随机部队进行 1（effect12541AtkNum） 次额外攻击 hurtval:{} DeadArmyCount:{} kill:{}", eff12541Soldier.getUUID(), hurtVal, deadArmyCount, killCnt);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 新回合开始 */
	public void roundStart2() {
	}

	private void kunNa1652() {
		if (getEffVal(EffType.HERO_1652) <= 0) {
			return;
		}
		int battleRound = getTroop().getBattle().getBattleRound();
		boolean five = battleRound % 5 == 0;
		if (!five) {
			return;
		}
		int count = 0;
		final int MAXBUFF = 2;
		for (BattleSoldier soldier : getTroop().getSoldierList()) {
			if (count >= MAXBUFF || getTroop().getBuff1652Cnt() >= ConstProperty.getInstance().getEffect1652Num()) {
				break;
			}
			if (!soldier.isAlive() || soldier.getFreeCnt() >= getFreeCnt() || soldier.getBuff1652(battleRound) > 0) {
				continue;
			}
			boolean bfalse = soldier.getType() == SoldierType.TANK_SOLDIER_1 || soldier.getType() == SoldierType.TANK_SOLDIER_2
					|| soldier.getType() == SoldierType.CANNON_SOLDIER_7 || soldier.getType() == SoldierType.CANNON_SOLDIER_8;
			if (!bfalse) {
				continue;
			}
			soldier.setBuff1652Round(battleRound, getEffVal(EffType.HERO_1652));
			soldier.setBuff1652Round(battleRound + 1, getEffVal(EffType.HERO_1652));
			count++;
			this.kunNa1652Help++;
			getTroop().setBuff1652Cnt(getTroop().getBuff1652Cnt() + 1);
			troop.getBattle().addDebugLog("###  {} 释放1652 目标 {} !{}", getUUID(), soldier.getUUID(), this.kunNa1652Help);
		}
		List<BattleSoldier> list = new ArrayList<>(getTroop().getEnemyTroop().getSoldierList());
		Collections.shuffle(list);
		for (BattleSoldier soldier : list) {
			if (count >= MAXBUFF || getTroop().getBuff1652Cnt() >= ConstProperty.getInstance().getEffect1652Num()) {
				break;
			}
			if (!soldier.isAlive() || soldier.getFreeCnt() >= getFreeCnt()) {
				continue;
			}
			boolean bfalse = soldier.getType() == SoldierType.FOOT_SOLDIER_5 || soldier.getType() == SoldierType.FOOT_SOLDIER_6;
			if (!bfalse) {
				continue;
			}
			int kill = (int) Math.max(1, soldier.getFreeCnt() * GsConst.EFF_PER * getEffVal(EffType.HERO_1653));
			IFootSoldier foot = (IFootSoldier) soldier;
			kill = foot.kunNaXiuZhen(kill);
			kill = soldier.xinPian1658(kill, true);
			int effVal1672 = soldier.getEffVal(EffType.HERO_1672);
			kill = (int) (kill * (1 - effVal1672 * GsConst.EFF_PER));
			kill = Math.max(1, kill);
			soldier.addDeadCnt(kill);
			addKillCnt(soldier, kill);
			this.kunNa1653Kill += kill;
			count++;
			getTroop().setBuff1652Cnt(getTroop().getBuff1652Cnt() + 1);
			troop.getBattle().addDebugLog("###  {} 释放1653 目标 {} 击杀 {} !{} effVal1672 {}", getUUID(), soldier.getUUID(), kill, this.kunNa1653Kill, effVal1672);
		}
	}

	public String getUUID() {
		return uuid;
	}

	/**
	 * 取得技能, 如果没有返回空技能.
	 * 久远不会返回null
	 * @param index
	 * @return
	 */
	public BattleSoldierSkillCfg getSkill(PBSoldierSkill index) {
		if (Objects.isNull(index)) {
			return EmptyBattleSoldierSkill.getInstance();
		}
//		if (index.getNumber() / 100 != getType().getNumber()) {
//			throw new RuntimeException("Code error " + toString() + index);
//		}
		return skillMap.getOrDefault(index, EmptyBattleSoldierSkill.getInstance());
	}

	/** 是否有此技能 */
	public boolean hasSkill(PBSoldierSkill index) {
		return skillMap.containsKey(index);
	}

	/** 是近战单位*/
	public boolean isJinZhan() {
		return isJinzhan(getType());
	}

	/** 是远程单位*/
	public boolean isYuanCheng() {
		return isYuanCheng(getType());
	}

	public static boolean isSoldier(SoldierType soldierType) {
		return soldierType.getNumber() <= 8;
	}

	public boolean isSoldier() {
		return isSoldier(getType());
	}

	public static boolean isJinzhan(SoldierType type) {
		if (type == SoldierType.TANK_SOLDIER_1
				|| type == SoldierType.TANK_SOLDIER_2
				|| type == SoldierType.PLANE_SOLDIER_3
				|| type == SoldierType.CANNON_SOLDIER_8) {
			return true;
		}
		return false;
	}

	/** 是远程单位*/
	public static boolean isYuanCheng(SoldierType type) {
		if (type == SoldierType.PLANE_SOLDIER_4
				|| type == SoldierType.FOOT_SOLDIER_5
				|| type == SoldierType.FOOT_SOLDIER_6
				|| type == SoldierType.CANNON_SOLDIER_7) {
			return true;
		}
		return false;
	}

	/**
	 * 尝试发动技能
	 * 
	 * @param skill
	 * @return
	 */
	public boolean trigerSkill(BattleSoldierSkillCfg skill) {
		if (Objects.isNull(skill) || skill instanceof EmptyBattleSoldierSkill) {
			return false;
		}
		return trigerSkill(skill, skill.getTrigger());
	}

	public boolean trigerSkill(BattleSoldierSkillCfg skill, EffType... types) {
		if (Objects.isNull(skill) || skill instanceof EmptyBattleSoldierSkill) {
			return false;
		}
		int add = 0;
		for (EffType type : types) {
			add += getEffVal(type);
		}
		return trigerSkill(skill, skill.getTrigger() + add);
	}

	/** 发动技能,指定概率 */
	public boolean trigerSkill(BattleSoldierSkillCfg skill, int weight) {
		if (Objects.isNull(skill) || skill instanceof EmptyBattleSoldierSkill) {
			return false;
		}

		boolean bfalse = weight >= HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE);
		if (bfalse) {
			skillTrigerLog(skill, weight);
		}
		return bfalse;
	}

	public void skillTrigerLog(BattleSoldierSkillCfg skill, int weight) {
		if (Objects.nonNull(troop)) {
			troop.getBattle().addDebugLog("### P={} soldier {} 触发技能 {}",
					weight, soldierId, skill.toString());
		}
	}

	public static BattleSoldier valueOf(Player player, BattleSoldierCfg soldierCfg, int count, int shadowCnt) {
		SoldierType type = SoldierType.valueOf(soldierCfg.getType());
		BattleSoldier result = null;
		switch (type) {
		case TANK_SOLDIER_1:
			result = new BattleSoldier_1();
			break;
		case TANK_SOLDIER_2:
			result = new BattleSoldier_2();
			break;
		case PLANE_SOLDIER_3:
			result = new BattleSoldier_3();
			break;
		case PLANE_SOLDIER_4:
			result = new BattleSoldier_4();
			break;
		case FOOT_SOLDIER_5:
			result = new BattleSoldier_5();
			break;
		case FOOT_SOLDIER_6:
			result = new BattleSoldier_6();
			break;
		case CANNON_SOLDIER_7:
			result = new BattleSoldier_7();
			break;
		case CANNON_SOLDIER_8:
			result = new BattleSoldier_8();
			break;
		case BARTIZAN_100:
			result = new BattleSoldier_100();
			break;
		case WEAPON_LANDMINE_101:
			result = new BattleSoldier_101();
			break;
		case WEAPON_ACKACK_102:
			result = new BattleSoldier_102();
			break;
		case WEAPON_ANTI_TANK_103:
			result = new BattleSoldier_103();
			break;
		default:
			break;
		}
		if (type != result.getType()) {
			throw new RuntimeException("Create soldier error!" + result + "  " + type);
		}
		result.init(player, soldierCfg, count, shadowCnt);
		return result;
	}

	public abstract SoldierType getType();
	
	public final int getTypeNum(){
		return getType().getNumber();
	}

	public BattleSoldierCfg getSoldierCfg() {
		return soldierCfg;
	}

	/**
	 * 清理，销毁
	 */
	public void clear() {
		soldierCfg = null;

		skillMap = null;

		effPerNum.clear();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("CAMP", troop.getTroop().shortName())
				.add("playerName", playerName)
				.add("soldierId", soldierId)
				.add("[TYPE]", soldierCfg.getType())
				.add("atkVal", solderStarCfg.getAttack())
				.add("defVal", solderStarCfg.getDefence())
				.add("hpVal", getHpVal())
				.add("xPos", xPos)
				.add("oriCnt", oriCnt)
				.add("deadCnt", deadCnt)
				.add("killCnt", getKillCnt())
				.toString();
	}

	public double getHpVal() {
		return getHpVal(null);
	}

	/**
	 * 单个兵的HP
	 */
	public double getHpVal(BattleSoldier atkSoldier) {
		HawkTuple2<Integer, Integer> tuple = tupleValue(BattleTupleType.Type.HP, SoldierType.XXXXXXXXXXXMAN);
		// 集结英雄S
		BattleTroop tartroop = getTroop().getEnemyTroop();
		int eff1622 = Math.min(ConstProperty.getInstance().getEffect1622Maxinum(), tartroop.lossRatePct()) * getEffVal(EffType.HERO_1622);
		int eff1625 = Math.min(ConstProperty.getInstance().getEffect1625Maxinum(), troop.lossRatePct()) * getEffVal(EffType.HERO_1625);
		// 集结英雄E

		int d1529 = Objects.isNull(debuf1529Val) ? 0 : IntStream.of(debuf1529Val).sum();
		int d11012 = debuff11012Val();
		int d1681 = debuff1681Val();
		int eff12391 = getEffVal(EffType.HERO_12391) * getTroop().lossRatePct();
		int eff12676 = getBattleRound() % 2 == 0 ? getEffVal(EffType.HERO_12676) : 0;
		int totalEff = tuple.first - d1529 + skillHPExactly() + eff1622 + eff1625 - d11012 - d1681 - debuff12008HP + buff12040Val - debuff12065HP + hpBuff12092()
				- skill744DebuffVal() + buff12302.second + getBuff12337HpVal() + eff12391 - skill11043DebuffVal() + getEffVal(EffType.HERO_12464) + getEffVal(EffType.HERO_12483)
				- debuffEffect12553Value - debuffEffect12554Value
				+ getBuff12574Val(EffType.EFF_12574) + skill12602Val() - skill12642DebuffVal() + eff12676;
		if (Objects.nonNull(atkSoldier)) {
			totalEff += skillHPExactly(atkSoldier);
			totalEff -= atkSoldier.skillIgnoreTargetHP(this);
		}
		if (isYuanCheng()) {
			totalEff += getTroop().skill74501Buff;
		}
		double hp = solderStarCfg.getHp() * (1 - getEffVal(EffType.BASE_HPD) * GsConst.EFF_PER)
				+ tupleValue(BattleTupleType.Type.HP_BASE, SoldierType.XXXXXXXXXXXMAN).first * GsConst.EFF_PER;
		double result = hp * GsConst.EFF_PER * (GsConst.EFF_RATE + totalEff);

		result *= (1 - getEffVal(EffType.GC_1496) * GsConst.EFF_PER);
		result = tbly17XXVal(result);
		result = crossTalent100XXVal(BattleTupleType.Type.NATION_HP, result);

		return Math.max(result, hp);
	}

	private int skill12602Val() {
		int effVal = getEffVal(EffType.HERO_12602);
		if (effVal == 0) {
			return 0;
		}
		List<BattleSoldier> tankAll = getTroop().getSoldierList().stream().filter(BattleSoldier::isTank).collect(Collectors.toList());
		double deadCnt = 0;
		double oriCnt = 0;
		for (BattleSoldier tank : tankAll) {
			deadCnt += tank.getDeadCnt();
			oriCnt += tank.getOriCnt();
		}
		return (int) ((deadCnt / oriCnt) / (ConstProperty.getInstance().effect12602AllNumLimit * GsConst.EFF_PER)) * effVal;
	}

	public double getCfgHpVal() {
		return solderStarCfg.getHp();
	}

	/**
	 * 设置作用号数据
	 */
	public void putEffPerNum(BattleTupleType.Type tuple, SoldierType type, HawkTuple3<Integer, Integer, List<Integer>> effPerNum) {
		this.effPerNum.put(tuple, type, effPerNum);
	}

	public HawkTuple2<Integer, Integer> tupleValue(BattleTupleType.Type tupleType, SoldierType tarType) {
		if (effPerNum.contains(tupleType, tarType)) {
			return effPerNum.get(tupleType, tarType);
		}
		return effPerNum.get(tupleType, SoldierType.XXXXXXXXXXXMAN);
	}

	/** 取得百分比数值的组成部分*/
	public List<Integer> tuplePerList(BattleTupleType.Type tupleType, SoldierType tarType) {
		if (effPerNum.contains(tupleType, tarType)) {
			return effPerNum.get(tupleType, tarType).third;
		}
		return effPerNum.get(tupleType, SoldierType.XXXXXXXXXXXMAN).third;
	}

	public void setEffVal(EffType type, int val) {
		int oldVal = effMap.getOrDefault(type, 0);
		if (val > oldVal) {
			effMap.put(type, val);
		}
	}

	public Table<BattleTupleType.Type, SoldierType, HawkTuple3<Integer, Integer, List<Integer>>> getEffPerNum() {
		return effPerNum;
	}

	public void setEffPerNum(Table<BattleTupleType.Type, SoldierType, HawkTuple3<Integer, Integer, List<Integer>>> effPerNum) {
		this.effPerNum = effPerNum;
	}

	public void setEffMap(Map<EffType, Integer> effMap) {
		this.effMap = effMap;
	}

	public Map<EffType, Integer> getEffMap() {
		return effMap;
	}

	public int getEffVal(EffType eff) {
		return effMap.getOrDefault(eff, 0);
	}

	/**
	 * 攻击距离
	 */
	public int getAtkDis() {
		return soldierCfg.getAttackDis();
	}

	/**
	 * 获取死兵转伤兵比例
	 * 
	 * @return
	 */
	public double getDeadToWoundPer() {
		HawkTuple2<Integer, Integer> tuple = tupleValue(BattleTupleType.Type.DEAD_TO_WOUND, SoldierType.XXXXXXXXXXXMAN);
		return tuple.first * GsConst.EFF_PER;
	}

	/**
	 * 获取攻击值（每回合重新计算）
	 * 
	 * @param tarType
	 *            敌方类型
	 */
	private double getAtkVal(BattleSoldier defSoldier, double totalEffNum, boolean fire) {
		int eff12392 = getEffVal(EffType.HERO_12392) * getTroop().lossRatePct();
		double attack = solderStarCfg.getAttack() * (1 - getEffVal(EffType.BASE_ATKD) * GsConst.EFF_PER)
				+ tupleValue(BattleTupleType.Type.ATK_BASE, SoldierType.XXXXXXXXXXXMAN).first * GsConst.EFF_PER;
		double val = attack * GsConst.EFF_PER * (totalEffNum + eff12392);
		int battleFreeCnt = battleFreeCnt();
		double result = val * Math.pow(battleFreeCnt, BattleConst.Const.POW.getNumber() * 0.01);

		{// 锦标赛专用号
			result = result * (1 - getEffVal(EffType.GC_1494) * GsConst.EFF_PER);
		}
		result = tbly17XXVal(result);
		result = crossTalent100XXVal(BattleTupleType.Type.NATION_ATK, result);
		troop.getBattle().addDebugLog(
				"###{} battleFreeCnt = {} 单兵功击={} 输出值={} {}加成总数 = {} ",
				getUUID(), battleFreeCnt, val, result, fire ? "超能攻击" : "攻击", totalEffNum);
		return result;
	}

	/** 攻击力线性加成*/
	private double atkEffLineNum(BattleSoldier defSoldier) {
		SoldierType tarType = defSoldier.getType();
		int skillPer = skillAtkExactly(defSoldier);
		int extryPer = getExtryHurt();
		// 集结英雄S
		BattleTroop defTroop = defSoldier.getTroop();
		int eff1620 = Math.min(ConstProperty.getInstance().getEffect1620Maxinum(), defTroop.lossRatePct()) * getEffVal(EffType.HERO_1620);
		int eff1623 = Math.min(ConstProperty.getInstance().getEffect1623Maxinum(), troop.lossRatePct()) * getEffVal(EffType.HERO_1623);
		int eff184 = defSoldier.getEffVal(EffType.WAR_SELF_MARCH_ATK_DEBUF);
		// 集结英雄E
		HawkTuple2<Integer, Integer> tuple = tupleValue(BattleTupleType.Type.ATK, tarType);
		int dskill814 = Objects.isNull(debfuSkill814Val) ? 0 : IntStream.of(debfuSkill814Val).sum();

		// 在这里汇总上面得到的加成. 加成都是做用号值 不要除10000
		double eff12662Val = Math.min(troop.planAttackCnt, ConstProperty.getInstance().effect12662Maxinum) * getEffVal(EffType.EFF_12662) * GsConst.EFF_PER * ConstProperty.getInstance().effect12662SoldierAdjustMap.getOrDefault(getType(), 10000);
		int skillIgnore = defSoldier.skillIgnoreTargetAtk(this);
		int eff12675 = getBattleRound() % 2 == 1 ? getEffVal(EffType.HERO_12675) : 0;
		double totalEffNum = GsConst.EFF_RATE + skillPer + extryPer + tuple.first - eff184 - dskill814 + eff1620 + eff1623 - debufSkill724Val - debuff12006Atk - debuff12063Atk
				- skill744DebuffVal() + getBuff12333Val(false) - skill11042DebuffVal() + getEffVal(EffType.HERO_12464) - debuffEffect12553Value - debuffEffect12554Value
				+ getBuff12574Val(EffType.EFF_12574)
				- getDebuff12561(defSoldier) - debuff44601 
				+ eff12662Val - skillIgnore  - getAstiaya12674DebuffVal(false) + eff12675;

		totalEffNum = Math.max(5000, totalEffNum); // 最多降到一半防御
		return totalEffNum;
	}

	/** 攻击力线性加成*/
	private double atkFireEffLineNum(BattleSoldier defSoldier) {
		SoldierType tarType = defSoldier.getType();
		// 集结英雄E
		HawkTuple2<Integer, Integer> tuple = tupleValue(BattleTupleType.Type.ATKFIRE, tarType);
		int skillPer = skillFireAtkExactly(defSoldier);
		double totalEffNum = tuple.first + getBuff12333Val(true) + getBuff12574Val(EffType.EFF_12575);
		int eff12223Num = Math.min(troop.attackCnt, ConstProperty.getInstance().getEffect12223Maxinum()) * getEffVal(EffType.HERO_12223);
		return totalEffNum + eff12223Num + skillPer - debuff12272Fatk - skill11042DebuffVal();
	}

	/**
	 * 获取防御值（每回合重新计算）
	 * 
	 * @param tarType
	 *            敌方类型
	 */
	private double getDefVal(BattleSoldier atkSoldier, double totalEffNum, boolean fire) {

		double defence = solderStarCfg.getDefence() * (1 - getEffVal(EffType.BASE_DEFD) * GsConst.EFF_PER)
				+ tupleValue(BattleTupleType.Type.DEF_BASE, SoldierType.XXXXXXXXXXXMAN).first * GsConst.EFF_PER;
		double val = defence * GsConst.EFF_PER * totalEffNum;
		// val = Math.max(val, defence); // 不低于原始生命值
		int battleFreeCnt = battleFreeCnt();
		double result = val * Math.pow(battleFreeCnt, BattleConst.Const.POW.getNumber() * 0.01);
		{// 百分比破甲
			int cutDefSelf = getEffVal(EffType.EFF_1427);
			int cutDefTart = atkSoldier.getEffVal(EffType.EFF_1428);
			result = result * (1 - cutDefSelf * GsConst.EFF_PER) * (1 - cutDefTart * GsConst.EFF_PER);
			result = Math.max(0, result);
		}

		{// 锦标赛专用号
			result = result * (1 - getEffVal(EffType.GC_1495) * GsConst.EFF_PER);
		}
		result = tbly17XXVal(result);
		result = crossTalent100XXVal(BattleTupleType.Type.NATION_DEF, result);

		troop.getBattle().addDebugLog("### {} battleFreeCnt = {} 单兵防御={} 输出值={} {}加成总值 = {} ",
				getUUID(), battleFreeCnt, val, result, fire ? "超能防御" : "防御", totalEffNum);
		return result;
	}

	private double defEffLineNum(BattleSoldier atkSoldier) {
		BattleTroop atkTroop = atkSoldier.getTroop();
		SoldierType tarType = atkSoldier.getType();
		int skillPer = skillDefExactly(atkSoldier);
		// 集结英雄S
		int eff1621 = Math.min(ConstProperty.getInstance().getEffect1621Maxinum(), atkTroop.lossRatePct()) * getEffVal(EffType.HERO_1621);
		int eff1624 = Math.min(ConstProperty.getInstance().getEffect1624Maxinum(), troop.lossRatePct()) * getEffVal(EffType.HERO_1624);
		// 集结英雄E
		HawkTuple2<Integer, Integer> tuple = tupleValue(BattleTupleType.Type.DEF, tarType);
		int effPer = tuple != null ? tuple.first : 0;
		// int effNum = tuple != null ? tuple.second : 0;
		// 【1530】单人出征或集结时，如果采矿车超过部队总数的10%，当采矿车触发AOE时，将减少AOE打击的所有目标 XX% 的部队防御，最多可叠加 N 层（持续至战斗结束）。
		int d1530 = Objects.isNull(debuf1530Val) ? 0 : IntStream.of(debuf1530Val).sum();
		int ignoreDef = atkSoldier.skillIgnoreTargetDef(this)
				+ atkSoldier.tupleValue(BattleTupleType.Type.CUT_DEF, this.getType()).first
				+ d1530;
		ignoreDef += getDebuff1639(getTroop().getBattle().getBattleRound());
		int eff12391 = getEffVal(EffType.HERO_12391) * getTroop().lossRatePct();
		int eff12676 = getBattleRound() % 2 == 0 ? getEffVal(EffType.HERO_12676) : 0;
		// 在这里汇总上面得到的加成. 加成都是做用号值 不要除10000
		double totalEffNum = GsConst.EFF_RATE + skillPer + effPer + eff1621 + eff1624 - ignoreDef - debuff12007Def - debuff12064Def - skill744DebuffVal() + getBuff12337DefVal()
				+ eff12391 - skill11043DebuffVal() + getEffVal(EffType.HERO_12464) + getEffVal(EffType.HERO_12483) - debuffEffect12553Value - debuffEffect12554Value
				+ getBuff12574Val(EffType.EFF_12574)
				- skill12642DebuffVal() - debuffSkill24601 + eff12676;
		return totalEffNum;
	}

	private double defFireEffLineNum(BattleSoldier atkSoldier) {
		SoldierType tarType = atkSoldier.getType();
		// 集结英雄E
		HawkTuple2<Integer, Integer> tuple = tupleValue(BattleTupleType.Type.DEFFIRE, tarType);
		int effPer = tuple != null ? tuple.first : 0;

		// 在这里汇总上面得到的加成. 加成都是做用号值 不要除10000
		double totalEffNum = effPer - getAstiaya12674DebuffVal(true);
		return totalEffNum;
	}

	/**
	 * 获取伤害值
	 * 
	 * @param type
	 *            敌方类型
	 */
	protected double getHurtVal(BattleSoldier defSoldier, double reducePer) {
		double hurtVal = hurtValBase(defSoldier, true, true);

		SoldierType type = defSoldier.getType();

		HawkTuple2<Integer, Integer> tuple = tupleValue(BattleTupleType.Type.HURT, type);
		int effPer = tuple != null ? tuple.first : 0;
		int effNum = tuple != null ? tuple.second : 0;

		int skillPer = skillHurtExactly(defSoldier);
		int eff12393 = (1 - getTroop().lossRate()) > ConstProperty.getInstance().getEffect12393Maxinum() * GsConst.EFF_PER
				? getEffVal(EffType.HERO_12393) : 0;
		int eff12451 = eff12451Val();
		effPer = effPer + eff12393 + eff12451 + buff12651Val(defSoldier);
		hurtVal = hurtVal * (GsConst.EFF_RATE + skillPer + effPer) / GsConst.EFF_RATE + effNum;
		final double oldHurtVal = hurtVal;
		// 伤害加
		hurtVal = addHurtValPct(defSoldier, hurtVal);
		final double afterAddHurtValPct = hurtVal;
		// 减伤害
		hurtVal = defSoldier.reduceHurtVal(this, hurtVal);
		hurtVal = hurtVal * (1 - reducePer);
		hurtVal = Math.max(1, hurtVal);
		hurtVal = defSoldier.jiji1633(hurtVal);
		// - 超能伤害修正 = 【攻击方超能强化 - 防御方超能抵御】*超能伤害修正系数/10000
		double xiuzhen = (tupleValue(Type.ATKFIRE_MHT, SoldierType.XXXXXXXXXXXMAN).first - defSoldier.tupleValue(Type.DEFFIRE_MHT, SoldierType.XXXXXXXXXXXMAN).first)
				* ConstProperty.getInstance().getSuperDamageCof() * GsConst.EFF_PER;
		xiuzhen = Math.max(xiuzhen, ConstProperty.getInstance().getSuperDamageLimitMinMax().first);
		xiuzhen = Math.min(xiuzhen, ConstProperty.getInstance().getSuperDamageLimitMinMax().second);

		hurtVal = hurtVal * (1 + xiuzhen * GsConst.EFF_PER);

		hurtVal = defSoldier.reduceHurtVal12085(this, hurtVal);

		if (skill12461DeBuffVal() > 0 && skill12461DeBuffFrom() != defSoldier) {
			hurtVal = skill12461Debuff.third.chanDou(this, hurtVal);
		}
		if (defSoldier.getType() != SoldierType.TANK_SOLDIER_1) { // 灵魂链接 复杂的系统简单做, 写死就得了. 后期可以考虑引入enentbus
			hurtVal = defSoldier.soulLink(this, hurtVal);
		}
		hurtVal = defSoldier.forceField(this, hurtVal);
		troop.getBattle().addDebugLog("### {} VS {},oldHurtVal:{} afterAddHurtValPct:{} hurtVal={},skillPer={},effPer={},effNum={}, reducePer={}, 超能伤害修正={}",
				soldierId, defSoldier.getSoldierId(), oldHurtVal,afterAddHurtValPct, hurtVal, skillPer, effPer, effNum, reducePer, xiuzhen);
		return hurtVal;
	}

	protected double hurtValBase(BattleSoldier defSoldier, boolean atk, boolean atkfire) {
		double atkVal = getPhysicsAtkVal(defSoldier);
		double defVal = defSoldier.getPhysicsDefVal(this);

		double atkfireVal = getFireAtkVal(defSoldier);
		double deffireVal = defSoldier.getFireDefVal(this);
		// atkfireVal*atkfireVal/(atkfireVal+deffireVal);

		double atkhurtVal = 0;
		double firehurtVal = 0;
		if (atk && atkVal > 0) {
			atkhurtVal = atkVal * atkVal / (atkVal + defVal);
		}
		if (atkfire && atkfireVal > 0) {
			firehurtVal = atkfireVal * atkfireVal / (atkfireVal + deffireVal);
		}

		// 伤害公式 @周伟
		double hurtVal = atkhurtVal + firehurtVal;
		double kezhiVal = kezhiXishuMap.getOrDefault(defSoldier.getType(), 0D);
		hurtVal = hurtVal * (1 + kezhiVal);
		troop.getBattle().addDebugLog("### soldier {} 克制 {} val {}, atk={},def={}, atkfireVal={},deffireVal={},  hurtVal {}", getUUID(), defSoldier.getUUID(), kezhiVal, atkVal,
				defVal, atkfireVal, deffireVal, hurtVal, hurtVal);
		return hurtVal;
	}

	private int eff12451Val() {
		if (getEffVal(EffType.EFF_12451) <= 0) {
			return 0;
		}
		int cen = (int) (deadCnt * GsConst.EFF_RATE / oriCnt / ConstProperty.getInstance().getEffect12451ConditionalRatio());
		if (cen < 1) {
			return 0;
		}
		cen = Math.min(cen, ConstProperty.getInstance().getEffect12451Maxinum());
		int result = (int) (cen * getEffVal(EffType.EFF_12451) * ConstProperty.getInstance().getEffect12451DamageAdjustMap().getOrDefault(getType(), 0)
				* GsConst.EFF_PER);
		addDebugLog("【12451】进攻其他指挥官基地战斗时，自身战损每达到 XX.XX%， 造成伤害额外 +{}", result);
		return result;
	}

	int skill544DebuffVal() {
		HawkTuple2<Integer, Integer> skill544Debuff = getSkill544Debuff();
		if (Objects.isNull(skill544Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill544Debuff.first) {
			return 0;
		}
		addDebugLog("### {} skill 544 debuf 受到攻击时受到伤害+ {} ", getUUID(), skill544Debuff.second);
		return skill544Debuff.second;
	}

	public double getPhysicsDefVal(BattleSoldier atkSoldier) {
		double defEffLineNum = defEffLineNum(atkSoldier);
		return getDefVal(atkSoldier, defEffLineNum, false);
	}

	public double getPhysicsAtkVal(BattleSoldier defSoldier) {
		double atkEffLineNum = atkEffLineNum(defSoldier);
		return getAtkVal(defSoldier, atkEffLineNum, false);
	}

	public double getFireDefVal(BattleSoldier atkSoldier) {
		double defEffLineNum = defFireEffLineNum(atkSoldier);
		return getDefVal(atkSoldier, defEffLineNum, true);
	}

	public double getFireAtkVal(BattleSoldier defSoldier) {
		double atkEffLineNum = atkFireEffLineNum(defSoldier);
		return getAtkVal(defSoldier, atkEffLineNum, true);
	}

	/**
	 * 防方减伤
	 * 
	 * @param tarType
	 *            攻方type
	 * @param hurtVal
	 *            攻方造成的伤害值
	 */
	private double reduceHurtVal(BattleSoldier atkSoldier, double hurtVal) {
		final double inHurtVal = hurtVal;
		SoldierType tarType = atkSoldier.getType();
		HawkTuple2<Integer, Integer> tuple = tupleValue(BattleTupleType.Type.REDUCE_HURT, tarType);
		int effPer = tuple != null ? tuple.first : 0;
		int effNum = tuple != null ? tuple.second : 0;
		int skillPer = skillReduceHurtPer(atkSoldier);
		hurtVal = hurtVal - effNum;
		hurtVal = hurtVal * (GsConst.EFF_RATE - effPer - skillPer) / GsConst.EFF_RATE;

		hurtVal = reduceHurtValPct(atkSoldier, hurtVal);

		hurtVal = Math.max(0, hurtVal);
		troop.getBattle().addDebugLog("###Soldier={} atkhurtVal = {}  ReduceHurtVal: hurtVal ={} ,effPer={},effNum={} skillPer={}",
				getSoldierId(), inHurtVal, hurtVal, effPer, effNum, skillPer);

		return hurtVal;
	}

	private double reduceHurtVal12085(BattleSoldier atkSoldier, double hurtVal) {
		if (val12085 > 0 && round12085 >= getBattleRound()) {
			double reduce = Math.min(val12085, hurtVal);
			hurtVal -= reduce;
			val12085 -= reduce;
			addDebugLog("远程庇护 {} 12085 - {}, 剩余 {}", getUUID(), reduce, val12085);
		}
		return hurtVal;
	}

	/** 直接百分比+最终值*/
	public double addHurtValPct(BattleSoldier defSoldier, double hurtVal) {

		List<Integer> list = tuplePerList(BattleTupleType.Type.HURT_PCT, defSoldier.getType());
		for (int effVal : list) {
			hurtVal *= (1 + effVal * GsConst.EFF_PER);
			troop.getBattle().addDebugLog("###Soldier={} effVal={} 伤害+的作用号在计算时为累乘算式, 操作后伤害值 {}", getUUID(), effVal, hurtVal);
		}
		hurtVal = hurtVal * (1 - skill12362DebuffVal() * GsConst.EFF_PER);
		if (debuff12517Val > 0) {
			hurtVal = hurtVal
					* (1 - debuff12517Val * GsConst.EFF_PER * ConstProperty.getInstance().effect12517SoldierAdjustMap.getOrDefault(defSoldier.getType(), 10000) * GsConst.EFF_PER);
			addDebugLog("###{} 【12517】 - 且受伤害的单位的- 触发条件本质为重力缠绕解除，关联【12515】- 伤害效率降低 {}", getUUID(), debuff12517Val, hurtVal);
		}
		return hurtVal;
	}

	/** 直接百分比减少最终值*/
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {

		List<Integer> list = tuplePerList(BattleTupleType.Type.REDUCE_HURT_PCT, atkSoldier.getType());
		for (int effVal : list) {
			hurtVal *= (1 - effVal * GsConst.EFF_PER);
			troop.getBattle().addDebugLog("###Soldier={} effVal={} 伤害减少的作用号在计算时为累乘算式, 操作后伤害值 {}", getUUID(), effVal, hurtVal);
		}
		hurtVal *= (1 - getBuff1652(getTroop().getBattle().getBattleRound()) * GsConst.EFF_PER);
		hurtVal *= (1 - getEffVal(EffType.EFF_12103) * GsConst.EFF_PER - getEffVal(EffType.EFF_12104) * GsConst.EFF_PER);

		int num12114 = (int) (deadCnt * 1D / oriCnt * GsConst.EFF_RATE / ConstProperty.getInstance().getEffect12114LossThresholdValue());
		hurtVal *= (1 - getEffVal(EffType.EFF_12114) * GsConst.EFF_PER * num12114);

		int eff12224Num = Math.min(atkSoldier.getTroop().attackCnt, ConstProperty.getInstance().getEffect12224Maxinum()) * getEffVal(EffType.HERO_12224);
		hurtVal *= (1 - GsConst.EFF_PER * eff12224Num);

		int eff12394 = (1 - getTroop().lossRate()) < ConstProperty.getInstance().getEffect12394Maxinum() * GsConst.EFF_PER
				? getEffVal(EffType.HERO_12394) : 0;
		hurtVal *= (1 - GsConst.EFF_PER * eff12394);
		if (getEffVal(EffType.EFF_12491) > 0 && ConstProperty.getInstance().getEffect12491RoundAdjustMap().containsKey(getBattleRound())) {
			// （1 - 【衰减修正值】/10000）*敌方兵种修正系数/10000
			double roundAdjust = GsConst.EFF_PER * ConstProperty.getInstance().getEffect12491RoundAdjustMap().getOrDefault(getBattleRound(), 0);
			double soldierAdjust = GsConst.EFF_PER * ConstProperty.getInstance().getEffect12491SoldierAdjustMap().getOrDefault(atkSoldier.getType(), 0);
			hurtVal = hurtVal * (1 - GsConst.EFF_PER * getEffVal(EffType.EFF_12491) * roundAdjust) * soldierAdjust;
		}
		if (skill12465BuffVal() > 0) {
			hurtVal *= (1 - GsConst.EFF_PER * skill12465BuffVal());
			addDebugLog("非直升机单位提供空域护航效果，使其受到伤害减少 {}", skill12465BuffVal());
		}
		if (skill12116BuffVal() > 0) {
			hurtVal *= (1 - GsConst.EFF_PER * skill12116BuffVal());
			addDebugLog("【12116】每第 5 回合开始时，为己方累计战损比率排序前 1 名的战斗单位提供战场保护，使其受到伤害减少 +XX.XX% {}", skill12116BuffVal());
		}
		if (getEffVal(EffType.EFF_1325) > 0 && getEffVal(EffType.EFF_1326) > 0) {
			int effval = (int) (getEffVal(EffType.EFF_1325) * (1 - (getBattleRound() - 1) * 1D / (getEffVal(EffType.EFF_1326) - 1))); // 【30（作用号1326）+己方队伍中核心科技品阶总和*25.00%（effect1325RankRatio）】回合时衰减至0.00%
			if (effval > 0) {
				hurtVal *= (1 - GsConst.EFF_PER * effval);
				addDebugLog("【1325~1326】在战斗中，自身所有单位受到伤害减少 {}", effval);
			}
		}
		if (getBuff12574Val(EffType.EFF_12576) > 0) {
			hurtVal *= (1 - GsConst.EFF_PER * getBuff12574Val(EffType.EFF_12576) * GsConst.EFF_PER
					* ConstProperty.getInstance().effect12576SoldierAdjustMap.getOrDefault(atkSoldier.getType(), 10000));
			addDebugLog("12576】获得伤害吸收护盾，护盾可以吸收该单位 {}", getBuff12574Val(EffType.EFF_12576));
		}
		int buff12561 = getBuff12561(atkSoldier);
		if (buff12561 > 0) {
			hurtVal *= (1 - GsConst.EFF_PER * buff12561);
		}
		int skill14601BuffVal = skill14601BuffVal();
		if (skill14601BuffVal > 0) {
			hurtVal *= (1 - GsConst.EFF_PER * skill14601BuffVal);
			addDebugLog("【14601】战斗开始时获得纳米护盾，使己方前排单位受到伤害减少 +{}% ", skill12116BuffVal());
		}
		
		int man12701 = man12701Val();
		if (man12701 > 0) {
			hurtVal *= (1 + GsConst.EFF_PER * man12701);
			addDebugLog("【12701】{}使该其受到额外 【12701】 {} ", getUUID(), man12701);
		}
		
		return hurtVal;
	}

	/** 遇到需要叠加减伤覆盖 */
	public int skillReduceHurtPer(BattleSoldier atkSoldier) {
		return 0;
	}

	/**
	 * 增加攻击力数值技能请覆盖 例: 战术飞行器（弓骑兵)资源战挂载(资源战)在资源战斗时有攻击加成
	 * 
	 * @return 万分比
	 */
	public int skillAtkExactly(BattleSoldier defSoldier) {
		return 0;
	}

	public int skillFireAtkExactly(BattleSoldier defSoldier) {
		return 0;
	}

	public void addExtryHurt(int val) {
		this.extryHurt += val;
	}

	public int getExtryHurt() {
		return extryHurt;
	}

	public void setExtryHurt(int extryHurt) {
		this.extryHurt = extryHurt;
	}

	/**
	 * 增加防御力数值技能请覆盖 例:宙斯防御系统(攻城防御)在攻城时有防御加成
	 * 
	 * @return 万分比
	 */
	public int skillDefExactly(BattleSoldier atkSoldier) {
		return 0;
	}

	public int skillHPExactly() {
		return 0;
	}

	public int skillHPExactly(BattleSoldier atkSoldier) {
		return 0;
	}
	
	/**忽略目标攻击加成  / 万分比*/
	public int skillIgnoreTargetAtk(BattleSoldier battleSoldier) {
		// TODO Auto-generated method stub
		return 0;
	}

	/** 忽略目标护甲值 (破甲) 万分比 */
	public int skillIgnoreTargetDef(BattleSoldier target) {
		return 0;
	}

	/** 忽略目标生命加成  万分比*/
	public int skillIgnoreTargetHP(BattleSoldier target) {
		return 0;
	}

	/** 闪避攻击 */
	final boolean skillDodge(BattleSoldier target) {
		Integer first = skillDogeEffval(target);
		boolean bfalse = first > HawkRand.randInt(1, 10000);
		if (bfalse) {
			dodgeCnt++;
			troop.getBattle().addDebugLog("###Soldier={} 闪避攻击={} , Attacker={}  累计闪避 {}",
					getUUID(), first, target.getUUID(), dodgeCnt);
		}
		return bfalse;
	}

	/** 闪避攻击数值, 万分比数  */
	protected int skillDogeEffval(BattleSoldier target) {
		return tupleValue(BattleTupleType.Type.DODGE, target.getType()).first;
	}

	/**
	 * 增加最终伤害数值技能请覆盖 例:主战坦克（枪兵）致命瞄准系统(致命一击)1级trigger=触发概率;damage=伤害参数;targetType=目标类型,有一定概率造成3倍伤害
	 * 
	 * @return 万分比
	 */
	public int skillHurtExactly(BattleSoldier defSoldier) {
		int result = 0;
		if (defSoldier.isYuanCheng()) {
			int[] arr12036 = ConstProperty.getInstance().getEffect12036EffectiveRoundArr();
			if (getBattleRound() >= arr12036[0] && getBattleRound() <= arr12036[1]) {
				result += getEffVal(EffType.EFF_12036);
			}
		}
		result -= debuff12039Val;
		if (getType() != SoldierType.PLANE_SOLDIER_3) {
			result += getEffVal(EffType.EFF_12101);
			result += getEffVal(EffType.EFF_12102);
		}
		if (getType() != SoldierType.PLANE_SOLDIER_3 && getBattleRound() <= ConstProperty.getInstance().getEffect12113ContinueRound()) {
			result += getEffVal(EffType.EFF_12113);
		}
		result += defSoldier.skill544DebuffVal();
		if (this instanceof ISSSSolomonPet) {
			result += getEffVal(EffType.EFF_12242);
		}
		return result;
	}

	// /**
	// * 减少最终伤害数值技能请覆盖 例:装甲坦克（盾兵）反步兵装甲(盾牌)1级trigger=触发概率;damage=伤害参数;targetType=目标类型,减少来自轻步兵和重型步兵的伤害
	// *
	// * @return 万分比
	// */
	// public int skillReduceHurtExactly(BattleSoldier atkSoldier) {
	// return 0;
	// }

	/**
	 * 尝试移动或进攻
	 * 
	 * @param enemyTroop
	 *            敌方部队
	 * @param round
	 *            回合数
	 * @param action
	 *            行动数
	 */
	public void tryMoveOrAttack(BattleTroop enemyTroop, int action) {
		// 没有防守方，不行动
		BattleSoldier defSoldier = getTargetSoldier(enemyTroop);
		if (defSoldier == null || !defSoldier.isAlive()) {
			troop.getBattle().addDebugLog(toString() + " STOP for no def soldier");
			return;
		}

		// 不在攻击范围，则移动 +1表示中间2个0
		if (!withinAtkDis(defSoldier)) {
			if (soldierCfg.getMoveStep() > 0) {
				setStepIn(action);
				xPos -= soldierCfg.getMoveStep();
			}
			troop.getBattle().addDebugLog(toString() + " MOVE");
		}

		try {
			beforeAttack(defSoldier);
			if (Objects.nonNull(solomonPet) && defSoldier.isAlive()) {
				solomonPet.beforeAttack(defSoldier);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		if (!withinAtkDis(defSoldier)) {
			return;
		}

		if (defSoldier == null || !defSoldier.isAlive()) {
			defSoldier = getTargetSoldier(enemyTroop);
		}
		if (defSoldier == null || !defSoldier.isAlive()) {
			troop.getBattle().addDebugLog(toString() + " STOP for no def soldier");
			return;
		}

		// 回合数检查
		int round = troop.getBattle().getBattleRound();
		if (isDebuff1639Round(round)) {
			troop.getBattle().addDebugLog("### {} 雷达干扰 取消出手", getUUID());
			return;
		}

		int attackTimes = atkTimes(round);

		for (int i = 0; i < attackTimes; i++) {
			if (i != 0) {
				defSoldier = getTargetSoldier(enemyTroop);
				if (defSoldier == null || !defSoldier.isAlive() || !withinAtkDis(defSoldier)) {
					return;
				}
			}
			try {
				attackOnce(defSoldier, 0, 0, getAtkDis());
				if (Objects.nonNull(solomonPet) && defSoldier.isAlive()) {
					troop.getBattle().addDebugLog("### 无敌的宠物");
					solomonPet.setxPos(xPos);
					solomonPet.attackOnce(defSoldier, 0, 0, getAtkDis());
				}
			} catch (Exception e) {
				HawkException.catchException(e);
				DungeonRedisLog.log("battleException", "{}\r\n{}", e, e.getStackTrace());
			}
		}
	}

	public int extryAtkRount() {
		return extround12112;
	}

	public int getBattleRound() {
		return this.getTroop().getBattle().getBattleRound();
	}

	/**每回合移动后, 攻击前*/
	public void beforeAttack(BattleSoldier defSoldier) {
	}

	public boolean withinAtkDis(BattleSoldier defSoldier, int atkDis) {
		return getxPos() + defSoldier.getxPos() + 1 <= atkDis;
	}

	public boolean withinAtkDis(BattleSoldier defSoldier) {
		return withinAtkDis(defSoldier, getAtkDis());
	}

	/**
	 * 本回合功击数
	 * 
	 * @param round
	 * @return
	 */
	public int atkTimes(int round) {
		int attackFrequency = getAtkRound() > 0 ? getAtkRound() : BASE_ATK_FREQUENCY;
		int attackTimes;

		attackTimes = (int) (1.0f * BASE_ATK_FREQUENCY / attackFrequency);

		if (attackTimes > 1) {

			// 回合数到攻击一次
		} else if ((round * BASE_ATK_FREQUENCY) % attackFrequency == 0) {
			attackTimes = 1;

			// 箭塔第一回合必打
		} else if (round == 1 && getType() == SoldierType.BARTIZAN_100) {
			attackTimes = 1;
		}
		return attackTimes;
	}

	/** 累计连击数*/
	protected void incExtrAtktimes(int cnt) {
		extrAtktimes += cnt;
	}

	public void attackOnce(BattleSoldier defSoldier, int atkTimes, double reducePer, int atkDis) {
		this.attackOnce(defSoldier, atkTimes, reducePer, atkDis, true);
	}

	/**
	 * 立即进攻一次
	 * 
	 * @param defSoldier
	 */
	protected void attackOnce(BattleSoldier defSoldier, int atkTimes, double reducePer, int atkDis, boolean typeRandom) {
		if (defSoldier.skillDodge(this) || defSoldier.isInvincible()) {
			return;
		}
		if (!defSoldier.isAlive() || !defSoldier.canBeAttack()) {
			return;
		}

		SoldierType defSoldierType = defSoldier.getType();
		// 根据defSoldier类型随机选择范围内敌人
		if (typeRandom) {
			defSoldier = typeRandom(defSoldier, atkDis);
		}

		defSoldier = defSoldier.bakeSaveMe();

		// 伤害公式
		double hurtVal = getHurtVal(defSoldier, reducePer);

		int round = troop.getBattle().getBattleRound();
		rountHurtMap.put(round, (int) hurtVal);
		defSoldier.rountHurtedMap.put(round, (int) hurtVal);

		int curCnt = defSoldier.getFreeCnt();
		int maxKillCnt = (int) Math.ceil(4.0f * hurtVal / defSoldier.getHpVal(this));
		maxKillCnt = Math.max(1, maxKillCnt);
		int killCnt = Math.min(maxKillCnt, curCnt);
		killCnt = defSoldier.xinPian1658(killCnt, false);
		killCnt = defSoldier.eff12086(this, killCnt);
		// 防御武器自损机制
		if (soldierCfg.isDefWeapon()) {
			int hasCnt = getFreeCnt();
			int deadRate = getWeaponDeadRate(defSoldier.getLevel());
			int selfDead = (int) Math.ceil(1.0f * killCnt * (GsConst.EFF_RATE - getCostReduce()) / GsConst.EFF_RATE / deadRate);
			if (selfDead > hasCnt) {
				selfDead = hasCnt;
			}
			addDeadCnt(selfDead);
			defSoldier.addKillCnt(this, selfDead);
		}

		defSoldier.addDeadCnt(killCnt);
		addKillCnt(defSoldier, killCnt);
		// 被攻击
		defSoldier.attacked(this, killCnt);
		this.attackOver(defSoldier, killCnt, hurtVal);

		troop.getBattle().addDebugLog("### hurtVal {} kill {} ", hurtVal, killCnt);
		troop.getBattle().addDebugLog("### {}", this);
		troop.getBattle().addDebugLog("### {}", defSoldier);

		double atkPer = killCnt * 1D / maxKillCnt; // 实际打出的伤害
		atkTimes = atkTimes + 1;
		if (atkPer < 0.1 && atkTimes < QIAN_PAI_MAX) {
			defSoldier = getQianPaiTargetSoldier(defSoldier);
			if (Objects.nonNull(defSoldier) && defSoldier.isAlive()) {
				reducePer = atkPer + reducePer;
				troop.getBattle().addDebugLog("### 千排触发  killCnt={} maxKillCnt={} reducePer={}  ", killCnt, maxKillCnt, reducePer);
				attackOnce(defSoldier, atkTimes, reducePer, Integer.MAX_VALUE);// 这里会调到子类
			}
		}
		troop.getBattle().addDebugLog("------------------------------------------------------------------------------------------------");
	}
	
	/***
	 * @param defSoldier
	 * @param hurtRate 伤害率  万分比 5000
	 */
	@Deprecated
	public void additionalAtk(BattleSoldier defSoldier, int hurtRate , boolean atk, boolean atkfire,String logstr){
		if(!defSoldier.canBeAttack()){
			return;
		}
		double hurtVal = hurtValBase(defSoldier, atk, atkfire) * GsConst.EFF_PER * hurtRate;
		hurtVal = defSoldier.forceField(this, hurtVal);
		if (hurtVal > 0) {
			int round = troop.getBattle().getBattleRound();
			rountHurtMap.put(round, (int) hurtVal);
			int curCnt = defSoldier.getFreeCnt();
			int maxKillCnt = (int) Math.ceil(4.0f * hurtVal / defSoldier.getHpVal());
			maxKillCnt = Math.max(1, maxKillCnt);
			int killCnt = Math.min(maxKillCnt, curCnt);
			defSoldier.addDeadCnt(killCnt);
			addKillCnt(defSoldier, killCnt);
			
			addDebugLog(logstr + " atk:{} tar: {} hurt: {} kill: {} ", getUUID(), defSoldier.getUUID(), hurtVal, killCnt);
		}
	}
	
	/***
	 * @param defSoldier
	 * @param hurtRate 伤害率  万分比 5000
	 */
	public void additionalAttack(BattleSoldier defSoldier, int hurtRate , boolean atk, boolean atkfire,String logstr){
		if(!defSoldier.canBeAttack()){
			return;
		}
		double hurtVal = getHurtVal(defSoldier, 0) * GsConst.EFF_PER * hurtRate;
		final double inputHurtVal = hurtVal;
		if (hurtVal > 0) {

			int round = troop.getBattle().getBattleRound();
			rountHurtMap.put(round, (int) hurtVal);
			int curCnt = defSoldier.getFreeCnt();
			int maxKillCnt = (int) Math.ceil(4.0f * hurtVal / defSoldier.getHpVal());
			maxKillCnt = Math.max(1, maxKillCnt);
			int killCnt = Math.min(maxKillCnt, curCnt);
			defSoldier.addDeadCnt(killCnt);
			addKillCnt(defSoldier, killCnt);
			addDebugLog(logstr + "additionalAttack atk:{} tar: {} inputHurtVal:{} hurtRate:{}  hurt: {} kill: {} ", getUUID(), defSoldier.getUUID(),inputHurtVal,hurtRate, hurtVal, killCnt);
		}
	}

	public BattleSoldier typeRandom(BattleSoldier defSoldier, int atkDis) {
		SoldierType defSoldierType = defSoldier.getType();
		List<BattleSoldier> tarList = defSoldier.getTroop().getSoldierList().stream()
				.filter(BattleSoldier::canBeAttack)
				.filter(BattleSoldier::isAlive)
				.filter(tar -> tar.getType() == defSoldierType)
				.filter(tar -> withinAtkDis(tar, atkDis))
				.collect(Collectors.toList());
		if (!tarList.isEmpty()) {
			HawkWeightFactor<BattleSoldier> hf = new HawkWeightFactor<>();
			for (BattleSoldier so : tarList) {
				hf.addWeightObj(so.getSoldierCfg().getWeight(), so);
			}
			defSoldier = hf.randomObj();
		}
		return defSoldier;
	}

	private int eff12086(BattleSoldier attacker, final int killCnt) {
		try {
			if (round12085 < getBattleRound()) {
				return killCnt;
			}
			// 因急救防护效果有 XX.XX%*该防御坦克属性加成/攻击方属性加成
			// （该比率数值最低50%，最高200%；属性加成为自身攻击加成、防御加成和生命加成均值）的部队直接恢复
			double rate = tank12086.effperAvg() / attacker.effperAvg();
			rate = Math.max(rate, ConstProperty.getInstance().getEffect12086RateMin() * GsConst.EFF_PER);
			rate = Math.min(rate, ConstProperty.getInstance().getEffect12086RateMax() * GsConst.EFF_PER);
			int zhijieHuifu = (int) (killCnt * GsConst.EFF_PER * tank12086.getEffVal(EffType.EFF_12086) * rate);
			zhijieHuifu = Math.min(killCnt, zhijieHuifu);
			addDebugLog("【12086】勠力同心: {} avg:{} , {} avg:{} 12086:{}", tank12086.getUUID(), tank12086.effperAvg(), attacker.getUUID(), attacker.effperAvg(),
					tank12086.getEffVal(EffType.EFF_12086));
			// 预计分担总数
			final int totalTrans = (int) ((killCnt - zhijieHuifu) * GsConst.EFF_PER * ConstProperty.getInstance().getEffect12086TransferRate());

			List<BattleSoldier> fendanzheList = new ArrayList<>();
			int fendanzheall = 0; // 分担者总数
			for (BattleSoldier soldier : getTroop().getSoldierList()) {
				if (soldier.isJinZhan() && soldier.isAlive()) {
					fendanzheList.add(soldier);
					fendanzheall += soldier.getFreeCnt();
				}
			}

			int fendan = 0; // 分担死亡
			boolean hasFree = true;
			AtomicLongMap<String> countMap = AtomicLongMap.create();
			while (fendan < fendanzheall && fendan < totalTrans && hasFree) {
				hasFree = false;
				final int wantdei = Math.max(1, (totalTrans - fendan) / fendanzheList.size());
				for (BattleSoldier soldier : fendanzheList) {
					if (!soldier.isAlive()) {
						continue;
					}
					int dei = Math.min(wantdei, soldier.getFreeCnt());
					fendan += dei;
					soldier.addDeadCnt(dei);
					attacker.addKillCnt(soldier, dei);
					countMap.addAndGet(soldier.getUUID(), dei);
					if (fendan >= fendanzheall || fendan >= totalTrans) {
						break;
					}
					hasFree = true;
				}
			}

			eff12086Zhuan += fendan;
			getTroop().eff12086ZhuanAll += fendan;
			addDebugLog("【12086】勠力同心:{}, 原 {} 恢复 {}  -> 分 {} 细: {}", getUUID(), killCnt, zhijieHuifu, fendan, countMap);
			return killCnt - zhijieHuifu - fendan;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return killCnt;
	}

	private double effperAvg() {
		double atkper = this.tupleValue(BattleTupleType.Type.ATK, SoldierType.XXXXXXXXXXXMAN).first;
		double defper = this.tupleValue(BattleTupleType.Type.DEF, SoldierType.XXXXXXXXXXXMAN).first;
		double hpper = this.tupleValue(BattleTupleType.Type.HP, SoldierType.XXXXXXXXXXXMAN).first;
		double avg = (atkper + defper + hpper) / 3;
		return avg;
	}

	private BattleSoldier bakeSaveMe() {
		if (Objects.isNull(bake1631Soldier) || !bake1631Soldier.isAlive()) {
			return this;
		}
		int bakeRate = bake1631Soldier.getEffVal(EffType.HERO_1631) * (getAtkDis() - bake1631Soldier.getAtkDis()) + ConstProperty.getInstance().getEffect1631Per();
		troop.getBattle().addDebugLog("### 巴克 正义防御 准备中 概率:{}, 原目标:{}", bakeRate, this.getSoldierId());
		if (Math.random() < bakeRate * GsConst.EFF_PER) {
			troop.getBattle().addDebugLog("### 巴克 正义防御 触发了 概率:{}, 原目标:{}", bakeRate, this.getSoldierId());
			bake1631Soldier.setBakeProtect(true);
			return bake1631Soldier;
		}
		return this;
	}

	/**
	 * 攻击结束
	 * 
	 * @param defSoldier
	 *            被攻击者
	 * @param killCnt
	 *            击杀
	 */
	protected void attackOver(BattleSoldier defSoldier, int killCnt, double hurtVal) {
		attackCnt++;
		if (isYuanCheng() || isJinZhan()) {
			troop.attackCnt++;
		}
		if (isPlan()) {
			troop.planAttackCnt++;
		}
		sorek12611DebuffHurt();
	}

	public double soulLink(BattleSoldier atkSoldier, double hurtVal) {
		for (BattleSoldier soldier : getTroop().getSoldierList()) {
			if (!soldier.canBeAttack() || soldier.getEffVal(EffType.EFF_12282) <= 0) {
				continue;
			}
			hurtVal = soldier.soulLinkAilinna(soldier, atkSoldier, hurtVal);
			getTroop().setSoulLinkClose(true);
		}

		return kaluolinSoulLink(atkSoldier, hurtVal);
	}

	private double soulLinkAilinna(BattleSoldier maxtank, BattleSoldier atkSoldier, double hurtVal) {
		int val = maxtank.getEffVal(EffType.EFF_12282);

		double soulHurt = 0;
		int freeTank = maxtank.getFreeCnt();

		soulHurt = hurtVal * GsConst.EFF_PER * val;
		soulHurt = maxtank.forceField(atkSoldier, soulHurt);
		int curCnt = maxtank.getFreeCnt();
		int maxKillCnt = (int) Math.ceil(4.0f * soulHurt / maxtank.getHpVal());
		maxKillCnt = Math.max(1, maxKillCnt);
		int killCnt = Math.min(maxKillCnt, curCnt);

		maxtank.addDeadCnt(killCnt);
		atkSoldier.addKillCnt(maxtank, killCnt);

		// 被攻击
		maxtank.attacked(atkSoldier, killCnt);

		troop.getBattle().addDebugLog("### 灵魂链接12282 {}   坦克{} 分担伤害{} 死亡{}", maxtank.getUUID(), freeTank, soulHurt, killCnt);
		return hurtVal - soulHurt;
	}

	private double kaluolinSoulLink(BattleSoldier atkSoldier, double hurtVal) {
		if (getTroop().isSoulLinkClose()) {
			return hurtVal;
		}
		BattleSoldier maxtank = null;
		int total = 0;
		for (BattleSoldier soldier : getTroop().getSoldierList()) {
			if (!soldier.canBeAttack()) {
				continue;
			}
			total += soldier.getFreeCnt();
			if (soldier.getType() == SoldierType.TANK_SOLDIER_1) {
				if (maxtank == null || soldier.getFreeCnt() > maxtank.getFreeCnt()) {
					maxtank = soldier;
				}
			}
		}

		if (maxtank == null || maxtank.getFreeCnt() * 1D / total < 0.1) {
			getTroop().setSoulLinkClose(true);
			return hurtVal;
		}
		int val = maxtank.getEffVal(EffType.EFF_1431);
		if (val <= 0) {
			getTroop().setSoulLinkClose(true);
			return hurtVal;
		}

		double soulHurt = 0;
		int freeTank = maxtank.getFreeCnt();

		soulHurt = hurtVal * GsConst.EFF_PER * val;
		soulHurt = maxtank.forceField(atkSoldier, soulHurt);
		int curCnt = maxtank.getFreeCnt();
		int maxKillCnt = (int) Math.ceil(4.0f * soulHurt / maxtank.getHpVal());
		maxKillCnt = Math.max(1, maxKillCnt);
		int killCnt = Math.min(maxKillCnt, curCnt);

		maxtank.addDeadCnt(killCnt);
		atkSoldier.addKillCnt(maxtank, killCnt);

		// 被攻击
		maxtank.attacked(atkSoldier, killCnt);

		troop.getBattle().addDebugLog("### 灵魂链接   总兵力{} 坦克{} 分担伤害{} 死亡{}", total, freeTank, soulHurt, killCnt);
		return hurtVal - soulHurt;
	}

	/** 受到攻击 */
	protected void attacked(BattleSoldier attacker, int deadCnt) {
		attackedCnt++;
		if (jiDiEnXi > 0) {
			double xisipct = GsConst.EFF_PER * (jiDiEn1537 + jiDiEn1536);
			int xiSi = (int) Math.min(deadCnt * xisipct, jiDiEnXi);
			jiDiEnXi -= xiSi;
			this.addDeadCnt(xiSi);
			attacker.addKillCnt(this, xiSi);
			this.jiDiEnXiCntMap.merge(jiDiEnXiPid, xiSi, (v1, v2) -> v1 + v2);
			troop.getBattle().addDebugLog("吉迪恩黑 ->{} 剩余:{} 死亡:{}", this, jiDiEnXi, xiSi);
		}
	}

	/** 概率降低死亡*/
	private int xinPian1658(int killCnt, boolean kunNa) {
		final int oldKillCnt = killCnt;
		if (getEffVal(EffType.HERO_1658) <= 0) {
			return killCnt;
		}
		if (!kunNa && killCnt * 1D / getOriCnt() < 0.01) {
			return killCnt;
		}

		int per = kunNa ? ConstProperty.getInstance().getEffect1658Per2() : ConstProperty.getInstance().getEffect1658Per();
		if (per >= HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE)) {
			killCnt = (int) (killCnt * GsConst.EFF_PER * (GsConst.EFF_RATE - getEffVal(EffType.HERO_1658)));
		}
		troop.getBattle().addDebugLog("{} 皮肤1658 ->{} 概率 {} 输入 {} 实际击杀{}", getUUID(), getEffVal(EffType.HERO_1658), per, oldKillCnt, killCnt);
		return killCnt;
	}

	/**
	 * 获取防守战斗单元
	 * 
	 * @param firstTypes
	 *            类型优先
	 */
	public BattleSoldier getTargetSoldier(BattleTroop enemyTroop) {
		List<SoldierType> firstTypes = soldierCfg.getFirstTypes();
		if (firstTypes != null && firstTypes.size() > 0) {

			for (BattleSoldier tmpSoldier : enemyTroop.getSoldierList()) {
				if (!tmpSoldier.isAlive() || !tmpSoldier.canBeAttack()) {
					continue;
				}
				if (!withinAtkDis(tmpSoldier)) {
					continue;
				}

				if (firstTypes.contains(tmpSoldier.getType())) {
					return tmpSoldier;
				}
			}
		}

		return enemyTroop.getDefSoldier();
	}

	public BattleSoldier getQianPaiTargetSoldier(BattleSoldier defSoldier) {
		for (BattleSoldier tmpSoldier : defSoldier.getTroop().getSoldierList()) {
			if (!tmpSoldier.isAlive() || !tmpSoldier.canBeAttack()) {
				continue;
			}

			if (defSoldier.getType() == tmpSoldier.getType()) {
				return tmpSoldier;
			}
		}
		return getTargetSoldier(defSoldier.getTroop());
	}

	/**
	 * 陷阱伤亡配比
	 */
	private int getWeaponDeadRate(int defLv) {
		int lvDiff = getLevel() - defLv;
		if (lvDiff == 4)
			return 8;
		else if (lvDiff == 3)
			return 5;
		else if (lvDiff == 2)
			return 3;
		else if (lvDiff == 1)
			return 2;
		else
			return 1;
	}

	/**
	 * 活着的
	 */
	public int getFreeCnt() {
		return oriCnt - deadCnt - jiDiEnXi - eff12111Cnt;
	}

	/** 出战数量 */
	private int battleFreeCnt() {
		double eff1429pct = getEffVal(EffType.EFF_1429) * GsConst.EFF_PER;
		int battleRound = this.getTroop().getBattle().getBattleRound();
		int xxx = roundDead.getOrDefault(battleRound - 1, 0) + roundDead.getOrDefault(battleRound - 2, 0);
		return (int) (getFreeCnt() + xxx * eff1429pct);
	}

	public int getSoldierId() {
		return soldierId;
	}

	public int getOriCnt() {
		return oriCnt;
	}

	public void setOriCnt(int oriCnt) {
		this.oriCnt = oriCnt;
	}

	public int getShadowCnt() {
		return shadowCnt;
	}

	public int getDeadCnt() {
		return deadCnt;
	}

	public void addDeadCnt(final int roundDeadCnt) {
		this.deadCnt += roundDeadCnt;
		this.deadCnt = Math.min(this.deadCnt, this.oriCnt);

		this.shadowDead = (int) Math.ceil(shadowRate * GsConst.EFF_PER * this.deadCnt);
		if (canBeAttack()) {
			this.troop.addArmyDeadCnt(roundDeadCnt);
		}
		roundDead.merge(getBattleRound(), roundDeadCnt, (v1, v2) -> v1 + v2);
	}

	/**是万分数 3000 = 30% 掉坑+1*/
	public double getShadowRate() {
		return shadowRate;
	}

	public int getTotalKillCnt() {
		return killTypeCntMap.values().stream().mapToInt(Integer::intValue).sum();
	}

	/** 击杀类型的数量*/
	public int getKillTypeCnt(SoldierType type) {
		return this.killTypeCntMap.getOrDefault(type, 0);
	}

	/** 击杀兵种id - 数量*/
	public Map<Integer, Integer> getKillCnt() {
		Map<Integer, Integer> result = new HashMap<>(killCntMap.size());
		for (Entry<BattleSoldier, Integer> ent : killCntMap.entrySet()) {
			BattleSoldier killed = ent.getKey();
			int number = ent.getValue();
			result.merge(killed.getSoldierId(), number, (v1, v2) -> v1 + v2);
		}
		return result;
	}

	public Map<BattleSoldier, Integer> getKillDetail() {
		return killCntMap;
	}

	/**吉迪恩玩家 : 黑洞内死兵数*/
	public Map<String, Integer> getJiDiEnXiCntMap() {
		return jiDiEnXiCntMap;
	}

	public void setKillCnt(Map<Integer, Integer> killCnt) {
		// this.killCnt = killCnt;
	}

	public void addKillCnt(BattleSoldier soldier, int kill) {
		this.killCntMap.merge(soldier, kill, (v1, v2) -> v1 + v2);
		this.killTypeCntMap.merge(soldier.getType(), kill, (v1, v2) -> v1 + v2);
		this.killPower += kill * soldier.getSoldierCfg().getPower();
	}

	public String getPlayerId() {
		return playerId;
	}

	public boolean isAlive() {
		return getFreeCnt() > 0;
	}

	public int getxPos() {
		return xPos;
	}

	public void setxPos(int pos) {
		this.xPos = pos;
	}

	public int getStepIn() {
		return stepIn;
	}

	public void setStepIn(int stepIn) {
		this.stepIn = stepIn;
	}

	public int getAtkRound() {
		return soldierCfg.getAttackRound();
	}

	public int getCostReduce() {
		return 0;
	}

	public void setTroop(BattleTroop troop) {
		this.troop = troop;
	}

	public BattleTroop getTroop() {
		return troop;
	}

	public int getLevel() {
		return soldierCfg.getLevel();
	}

	public int getBuildingWeight() {
		try {
			switch (soldierCfg.getBuilding()) {
			case BuildingType.BARRACKS_VALUE:
				return 1;
			case BuildingType.WAR_FACTORY_VALUE:
				return 2;
			case BuildingType.AIR_FORCE_COMMAND_VALUE:
				return 4;
			case BuildingType.REMOTE_FIRE_FACTORY_VALUE:
				return 3;
			default:
				break;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 1;
	}

	/**
	 * 是否可移动
	 */
	public boolean canMove() {
		return soldierCfg.getMoveStep() > 0;
	}

	/**
	 * 是否可被攻击
	 */
	public boolean canBeAttack() {
		if (GuarderPlayer.isGuarderPlayer(getPlayerId())) {
			return false;
		}
		return getType().getNumber() <= SoldierType.CANNON_SOLDIER_8_VALUE && !isInvincible() && isAlive();
	}

	/**
	 * 损失战力
	 */
	public double getLostPower() {
		int dcreaseper = getEffVal(EffType.HERO_1666) > 0 ? getEffVal(EffType.HERO_1666) : getEffVal(EffType.EFF_1425);
		dcreaseper = (int) (dcreaseper * (1 + getEffVal(EffType.EFF_12241) * GsConst.EFF_PER));
		return soldierCfg.getPower() * (deadCnt - shadowDead * GsConst.EFF_PER * dcreaseper);
	}

	/**
	 * 剩余战力
	 */
	public double getLeftPower() {
		return soldierCfg.getPower() * getFreeCnt();
	}

	public List<Integer> getHeros() {
		return heros;
	}

	public void setHeros(List<Integer> heros) {
		this.heros = heros;
	}

	/** 上一出手回合最高伤害值*/
	public int getLastAtkRoundMaxHurt() {
		int battleRound = getTroop().getBattle().getBattleRound();
		for (int i = battleRound; i > 0; i--) {
			if (rountHurtMap.containsKey(i)) {
				return Collections.max(rountHurtMap.get(i));
			}
		}
		return 0;
	}

	public int getRountHurtVal(int rount) {
		int result = 0;
		for (Integer v : rountHurtMap.get(rount)) {
			result += v;
		}
		return result;
	}

	/**回合受到的伤害*/
	public int getRountHurtedVal(int rount) {
		int result = 0;
		for (Integer v : rountHurtedMap.get(rount)) {
			result += v;
		}
		return result;
	}

	/**回合攻击次数*/
	public int getRoundHurtTimes(int round) {
		if (rountHurtMap.containsKey(round)) {
			return rountHurtMap.get(round).size();
		}
		return 0;
	}

	/**当前合攻击次数*/
	public int getRoundHurtTimes() {
		int battleRound = getTroop().getBattle().getBattleRound();
		return getRoundHurtTimes(battleRound);
	}

	public int getSoldierStar() {
		return soldierStar;
	}

	public int getSoldierPlantLevel() {
		return soldierSkillLevel;
	}

	public int getShadowDead() {
		return shadowDead;
	}

	public int getJiDiEnXi() {
		return jiDiEnXi;
	}

	public void setJiDiEnXi(String jiDiEnXiPid, int jiDiEnXi, int jiDiEn1536, int jiDiEn1537) {
		this.jiDiEnXiPid = jiDiEnXiPid;
		this.jiDiEnXi = jiDiEnXi;
		this.jiDiEn1536 = jiDiEn1536;
		this.jiDiEn1537 = jiDiEn1537;
	}

	/**是飞机*/
	public boolean isPlan() {
		return false;
	}

	/**是步兵*/
	public boolean isFoot() {
		return false;
	}

	/**是坦克*/
	public boolean isTank() {
		return false;
	}

	public BattleSoldier getBake1631() {
		return bake1631;
	}

	public void setBake1631(BattleSoldier bake1631) {
		this.bake1631 = bake1631;
	}

	public BattleSoldier_1 getBake1631Soldier() {
		return bake1631Soldier;
	}

	public void setBake1631Soldier(BattleSoldier_1 bake1631Soldier) {
		this.bake1631Soldier = bake1631Soldier;
	}

	public int getSoldierStep() {
		return soldierStep;
	}

	/** 泰伯1701- 1716系列*/
	protected double tbly17XXVal(double result) {
		HawkTuple2<Integer, Integer> tuple = tupleValue(BattleTupleType.Type.TBLY17XX_ATK_DEF_HP, SoldierType.XXXXXXXXXXXMAN);
		return result * (1 + tuple.first * GsConst.EFF_PER);
	}

	protected double crossTalent100XXVal(BattleTupleType.Type tupleType, double result) {
		HawkTuple2<Integer, Integer> tuple = tupleValue(tupleType, SoldierType.XXXXXXXXXXXMAN);
		return result * (1 + tuple.first * GsConst.EFF_PER);
	}

	public void setJijia1633(double dunNum) {
		jijia1633Num = dunNum;
	}

	public double jiji1633(double hurtVal) {
		if (jijia1633Num <= 0) {
			return hurtVal;
		}

		double xishou = Math.min(jijia1633Num, hurtVal);
		jijia1633Num -= xishou;
		troop.getBattle().addDebugLog("###Soldier:{} 机甲1633吸收伤害 原值:{} 吸收值:{} 护盾剩余:{}", soldierId, hurtVal, xishou, jijia1633Num);
		return hurtVal - xishou;
	}

	/**
	 * 战后组装用于结算. 这里有一部分死转伤的计算. 
	 */
	public ArmyInfo calcArmyInfo(double selfCoverRete) {
		ArmyInfo armyInfo = new ArmyInfo();
		armyInfo.setPlayerId(getPlayerId());
		armyInfo.setArmyId(getSoldierId());
		armyInfo.setKillCount(getTotalKillCnt());
		armyInfo.setKillPower(killPower);
		armyInfo.mergeKillInfo(getKillCnt());
		Map<Long, Integer> killDetail = calcKillDetail(getKillDetail());
		armyInfo.mergeKillDetail(killDetail);
		armyInfo.mergeBlackHoleDead(getJiDiEnXiCntMap());
		armyInfo.setStar(getSoldierStar());
		armyInfo.setPlantStep(getSoldierStep());
		armyInfo.setPlantSkillLevel(soldierSkillLevel);
		armyInfo.setPlantMilitaryLevel(soldierMilitaryLevel);
		// 所有部队总数
		int totalUnit = getOriCnt();
		// 影子部队数量
		int shadowCnt = getShadowCnt();
		// 真实部队数量
		int totalArmyCnt = totalUnit - shadowCnt;

		// 影子部队万分比
		double shadowRate = GsConst.EFF_RATE * shadowCnt / totalUnit;
		// 影子部队比例校正
		shadowRate = Math.max(0, shadowRate);
		shadowRate = Math.min(10000, shadowRate);
		// 影子部队死亡数量(向上取整)
		int shadowLoseCnt = getShadowDead();
		// 影子部队死亡数修正,死兵数不超过影子部队总数
		shadowLoseCnt = Math.min(shadowLoseCnt, shadowCnt);

		// 普通死亡士兵按照比例减少
		int realLoseCnt = getDeadCnt() - shadowLoseCnt;

		int deadCnt = (int) Math.ceil(realLoseCnt * selfCoverRete * GsConst.EFF_PER);
		// 死兵数数据修正
		deadCnt = Math.min(deadCnt, realLoseCnt);
		deadCnt = Math.max(deadCnt, 0);

		// 死亡士兵按照比例转化成伤兵
		int dead2WoundCnt = (int) Math.ceil(deadCnt * getDeadToWoundPer());

		// 死兵转伤兵数量数据修正
		dead2WoundCnt = Math.min(dead2WoundCnt, deadCnt);
		dead2WoundCnt = Math.max(dead2WoundCnt, 0);

		deadCnt -= dead2WoundCnt;

		armyInfo.setDeadCount(deadCnt);
		armyInfo.setWoundedCount(dead2WoundCnt);
		armyInfo.setTotalCount(totalArmyCnt);
		armyInfo.setRealLoseCount(realLoseCnt);
		armyInfo.setShadowCnt(shadowCnt);
		armyInfo.setShadowDeadCnt(shadowLoseCnt);
		armyInfo.setDisBattlePoint(getLostPower());
		armyInfo.setDodgeCnt(dodgeCnt);
		armyInfo.setExtrAtktimes(extrAtktimes);
		armyInfo.setKunNa1652Help(kunNa1652Help);
		armyInfo.setKunNa1653Kill(kunNa1653Kill);
		if (eff12086Zhuan > 0) {
			armyInfo.setEff12086Zhuan(eff12086Zhuan);
			armyInfo.setEff12086ZhuanAll(getTroop().eff12086ZhuanAll);
		}
		if (Objects.nonNull(solomonPet)) {
			armyInfo.setSssSLMPet(solomonPet.calcArmyInfo(selfCoverRete));
		}
		armyInfo.setEff12111Cnt(eff12111Cnt);
		if (Objects.nonNull(eff12541Soldier)) {
			PB12541Detail.Builder builder12541 = PB12541Detail.newBuilder();
			builder12541.setKill(eff12541SoldierKill).setPlayerName(eff12541Soldier.playerName).setSoldierId(eff12541Soldier.getSoldierId());
			getTroop().getBattle().getEff12541map().put(playerId, builder12541.build());
		}
		return armyInfo;
	}

	/**
	 * 计算击杀详情
	 * @param killDetail
	 * @return
	 */
	private Map<Long, Integer> calcKillDetail(Map<BattleSoldier, Integer> killDetail) {
		Map<Long, Integer> detailMap = new HashMap<>();
		if (killDetail == null) {
			return detailMap;
		}
		for (Entry<BattleSoldier, Integer> entry : killDetail.entrySet()) {
			BattleSoldier soldier = entry.getKey();
			int cnt = entry.getValue();
			int star = soldier.getSoldierStar();
			int armyId = soldier.getSoldierId();
			long calcedId = BattleService.calcArmyId(armyId, star);
			if (detailMap.containsKey(calcedId)) {
				detailMap.put(calcedId, detailMap.get(calcedId) + cnt);
			} else {
				detailMap.put(calcedId, cnt);
			}
		}
		return detailMap;
	}

	public boolean isDebuff1639Round(int round) {
		if (Objects.isNull(debuff1639RoundMap)) {
			return false;
		}
		return debuff1639RoundMap.containsKey(round);
	}

	public int getDebuff1639(int round) {
		if (Objects.isNull(debuff1639RoundMap)) {
			return 0;
		}
		return debuff1639RoundMap.getOrDefault(round, 0);
	}

	public void setDebuff1639Round(int debuff1639Round, int debuff1639) {
		if (Objects.isNull(this.debuff1639RoundMap)) {
			this.debuff1639RoundMap = new HashMap<>();
		}
		this.debuff1639RoundMap.put(debuff1639Round, debuff1639);
	}

	public int getBuff1652(int round) {
		if (Objects.isNull(buff1652RoundMap)) {
			return 0;
		}
		return buff1652RoundMap.getOrDefault(round, 0);
	}

	public void setBuff1652Round(int debuff1639Round, int debuff1639) {
		if (Objects.isNull(this.buff1652RoundMap)) {
			this.buff1652RoundMap = new HashMap<>();
		}
		this.buff1652RoundMap.put(debuff1639Round, debuff1639);
	}

	public void incrDebufSkill224Val(int p1, int int1) {
	}

	public void incrDebufSkill424Val(int valhp, int valdef, int maxCen) {
	}

	public void setSkill624Debuff(HawkTuple2<Integer, Integer> debuff) {
	}

	public void setSkill544Debuff(HawkTuple2<Integer, Integer> debuff) {
	}

	public HawkTuple2<Integer, Integer> getSkill544Debuff() {
		return null;
	}

	public HawkTuple2<Integer, Integer> getSkill744Debuff() {
		return skill744Debuff;
	}

	public void setSkill744Debuff(HawkTuple2<Integer, Integer> skill744Debuff) {
		this.skill744Debuff = skill744Debuff;
	}

	int skill744DebuffVal() {
		if (Objects.isNull(skill744Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill744Debuff.first) {
			return 0;
		}
		addDebugLog("### {} skill 744 debuf 降低其攻击、防御、生命 {} ", getUUID(), skill744Debuff.second);
		return skill744Debuff.second;
	}

	public void incrDebufSkill724Val(int maxCen, int val) {
		debufSkill724Val = Math.min(debufSkill724Val + val, maxCen * val);
		getTroop().getBattle().addDebugLog("### {} Add skill 724 debuf atk {} ", getUUID(), debufSkill724Val);
	}

	/**
	 * 是否有站位在自己之后的1-8
	 * @return
	 */
	public boolean hasHouPai() {
		for (BattleSoldier so : getTroop().getSoldierList()) {
			if (so.getType().getNumber() > 8 || so == this || !isAlive()) {
				continue;
			}
			if (getxPos() < so.getxPos()) {
				return true;
			}
		}
		return false;
	}

	private Map<String, Integer> debuff11012map;

	public void addDebuff11012(String playerId, int val) {
		if (val <= 0) {
			return;
		}
		if (Objects.isNull(debuff11012map)) {
			debuff11012map = new HashMap<>();
		}
		debuff11012map.put(playerId, val);
	}

	public int debuff11012Val() {
		if (Objects.isNull(debuff11012map)) {
			return 0;
		}
		int sesult = debuff11012map.values().stream().sorted(Comparator.comparingInt(Integer::intValue).reversed()).limit(ConstProperty.getInstance().getEffect11012TimesLimit())
				.mapToInt(Integer::intValue)
				.sum();

		getTroop().getBattle().addDebugLog("### {} ARMOUR_11012 debuf {} ", getUUID(), sesult);
		return sesult;
	}

	/**取得10星堡垒技能做用号*/
	public int getHonor10EffVal(EffType effType) {
		BattleSoldierSkillCfg skill34 = getSkill(honor10SkillId());
		return skill34.getHonor10buff(effType);
	}

	public int getAttackedCnt() {
		return attackedCnt;
	}

	public int getAttackCnt() {
		return attackCnt;
	}

	public boolean isInvincible() {
		return invincible;
	}

	public void setInvincible(boolean invincible) {
		this.invincible = invincible;
	}

	public BattleSoldier getSolomonPet() {
		return solomonPet;
	}

	public void setSolomonPet(BattleSoldier solomonPet) {
		this.solomonPet = solomonPet;
	}

	public boolean isSss1671BiZhong() {
		return sss1671BiZhong;
	}

	public void setSss1671BiZhong(boolean sss1671BiZhong) {
		this.sss1671BiZhong = sss1671BiZhong;
	}

	private int debuff1681Val;

	public void addDebuff1681(int val) {
		if (val <= 0) {
			return;
		}
		debuff1681Val += val;
		debuff1681Val = Math.min(debuff1681Val, ConstProperty.getInstance().getEffect1681MaxValue());
	}

	public int debuff1681Val() {
		return debuff1681Val;
	}

	public void addDebugLog(final String messagePattern, final Object... arguments) {
		if (Objects.isNull(getTroop())) {
			extryLog.add(HawkTuples.tuple(messagePattern, arguments));
			return;
		}
		getTroop().getBattle().addDebugLog(messagePattern, arguments);
	}

	public void flushExtryLog() {
		Iterator<HawkTuple2<String, Object[]>> it = extryLog.iterator();
		while (it.hasNext()) {
			HawkTuple2<String, Object[]> type = it.next();
			getTroop().getBattle().addDebugLog(type.first, type.second);
			it.remove();
		}
	}

	public int getDebuff12005ContinueRound() {
		return debuff12005ContinueRound;
	}

	public void setDebuff12005ContinueRound(int debuff12005ContinueRound) {
		this.debuff12005ContinueRound = debuff12005ContinueRound;
	}

	public int getDebuff12161ContinueRound() {
		return debuff12161ContinueRound;
	}

	public void setDebuff12161ContinueRound(int debuff12161ContinueRound) {
		this.debuff12161ContinueRound = debuff12161ContinueRound;
	}

	public int getDebuff12006Atk() {
		return debuff12006Atk;
	}

	public void setDebuff12006Atk(int debuff12006Atk) {
		this.debuff12006Atk = debuff12006Atk;
	}

	public int getDebuff12007Def() {
		return debuff12007Def;
	}

	public void setDebuff12007Def(int debuff12007Def) {
		this.debuff12007Def = debuff12007Def;
	}

	public int getDebuff12008HP() {
		return debuff12008HP;
	}

	public void setDebuff12008HP(int debuff12008hp) {
		debuff12008HP = debuff12008hp;
	}

	public Map<EffType, Integer> getEffMapClientShow() {
		return new HashMap<>(getEffMap());
	}

	protected void mergeClientShow(Map<EffType, Integer> result, EffType from, EffType... show) {
		if (result.containsKey(from)) {
			int val = result.remove(from);
			for (EffType type : show) {
				result.merge(type, val, (v1, v2) -> v1 + v2);
			}
		}
	}

	int debuff12039Val;
	int debuff12039cen;

	public void addDebuff12039(int effVal) {
		if (effVal > 0 && debuff12039cen < ConstProperty.getInstance().getEffect12039Maxinum()) {
			debuff12039Val += effVal;
			debuff12039cen++;
			addDebugLog("{} debuf 12039 :{}", getUUID(), debuff12039Val);
		}
	}

	int buff12040Val;
	int buff12040cen;
	private int debuff44601;

	public void addBuff12040(int effVal) {
		if (effVal > 0 && buff12040cen < ConstProperty.getInstance().getEffect12040Maxinum()) {
			buff12040Val += effVal;
			buff12040cen++;
			addDebugLog("{} buf 12040 :{}", getUUID(), buff12040Val);
		}
	}

	public void addDebuff12063Atk(int val, int max) {
		debuff12063Atk += val;
		debuff12063Atk = Math.min(debuff12063Atk, max);
		addDebugLog("{} debuf 12063 :{}", getUUID(), debuff12063Atk);
	}

	public void addDebuff12064Def(int val, int max) {
		debuff12064Def += val;
		debuff12064Def = Math.min(debuff12064Def, max);
		addDebugLog("{} debuf 12064 :{}", getUUID(), debuff12064Def);
	}

	public void addDebuff12065HP(int val, int max) {
		debuff12065HP += val;
		debuff12065HP = Math.min(debuff12065HP, max);
		addDebugLog("{} debuf 12065 :{}", getUUID(), debuff12065HP);
	}

	public void addDebuff12272FAtk(int val, int max) {
		debuff12272Fatk += val;
		debuff12272Fatk = Math.min(debuff12272Fatk, max);
		addDebugLog("{} debuf 12272 :{}", getUUID(), debuff12272Fatk);
	}

	public boolean addBuff12085(BattleSoldier source, int endRound, int val, int buffhpRound, int buffhp) {
		if (endRound != round12085) {
			val12085 = 0;
			cnt12085 = 0;
			buffhp12092 = 0;
			buffhp12092Round = 0;
			tank12086 = null;
		}
		if (cnt12085 >= ConstProperty.getInstance().getEffect12085GetLimit()) {
			return false;
		}
		round12085 = endRound;
		cnt12085++;
		val12085 += val;
		if (tank12086 == null || source.effperAvg() > tank12086.effperAvg()) {
			tank12086 = source;
		}

		buffhp12092Round = buffhpRound;
		buffhp12092 = Math.max(buffhp12092, buffhp);
		addDebugLog("远程庇护 {} buff {} 12085 :{} 12092:{}", getUUID(), val, val12085, buffhp12092);
		return true;
	}

	public int hpBuff12092() {
		if (buffhp12092Round >= getBattleRound()) {
			return buffhp12092;
		}
		return 0;
	}

	public int getEff12111Cnt() {
		return eff12111Cnt;
	}

	public void setEff12111Cnt(int eff12111Cnt) {
		this.eff12111Cnt = eff12111Cnt;
	}

	public int getExtround12112() {
		return extround12112;
	}

	public void setExtround12112(int extround12112) {
		this.extround12112 = extround12112;
	}

	public int getSoldierMilitaryLevel() {
		return soldierMilitaryLevel;
	}

	public void roundEnd() {
		fuhuo12339();
		sorek12611DebuffOverHurt();
		debuff34601Atk();
	}

	/**
	 * 点燃状态
	 */
	public boolean isDianran() {
		return getDebuff12005ContinueRound() >= getBattleRound()
				|| getDebuff12161ContinueRound() >= getBattleRound();
	}

	public void clearDianRan() {
		setDebuff12005ContinueRound(0);
		setDebuff12161ContinueRound(0);
	}

	public HawkTuple3<Integer, Integer, Integer> getEff12163Debuff() {
		return eff12163Debuff;
	}

	public void setEff12163Debuff(HawkTuple3<Integer, Integer, Integer> debuff) {
		eff12163DebuffExtry = 0;
		this.eff12163Debuff = debuff;
		eff12163DebuffCnt++;
	}

	public HawkTuple3<Integer, Integer, Integer> eff12163DebuffVal() {
		try {
			if (this instanceof ISSSSolomonPet) {
				BattleSoldier parent = ((ISSSSolomonPet) this).getParent();
				return parent.eff12163DebuffVal();
			}

			if (Objects.isNull(eff12163Debuff)) {
				return HawkTuples.tuple(0, 0, 0);
			}
			int round = getTroop().getBattle().getBattleRound();
			if (round > eff12163Debuff.first) {
				eff12163DebuffExtry = 0;
				return HawkTuples.tuple(0, 0, 0);
			}
			double per = (10000 + eff12163DebuffExtry) * 0.0001;
			return HawkTuples.tuple(eff12163Debuff.first, (int) (eff12163Debuff.second * per), (int) (eff12163Debuff.third * per));

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HawkTuples.tuple(0, 0, 0);
	}

	public void roundStart3() {
		// TODO Auto-generated method stub

	}

	public int getEff12163DebuffExtry() {
		return eff12163DebuffExtry;
	}

	public void setEff12163DebuffExtry(int eff12163DebuffExtry) {
		this.eff12163DebuffExtry = eff12163DebuffExtry;
	}

	protected void addDebuff12206Val(int val) {
		if (debuff12206 == null) {
			debuff12206 = new LinkedList<>();
		}
		debuff12206.add(val);
		if (debuff12206.size() > ConstProperty.getInstance().getEffect12206Maxinum()) {
			debuff12206.removeLast();
		}
		debuff12206Val = debuff12206.stream().mapToInt(Integer::intValue).sum();
	}

	protected int getDebuff12206Val() {
		return debuff12206Val;
	}

	public void addBuff12302(int effVal) {
		if (buff12302.first >= ConstProperty.getInstance().getEffect12302MaxTimes()) {
			return;
		}

		buff12302 = HawkTuples.tuple(buff12302.first + 1, buff12302.second + effVal);

		addDebugLog(" # {}  12302 hp ceng:{} val:{}", getUUID(), buff12302.first, buff12302.second);
	}

	public boolean canAddBuff12302() {
		if (!isJinZhan() || !isAlive()) {
			return false;
		}
		if (buff12302 == null) {
			return true;
		}
		if (buff12302.first >= ConstProperty.getInstance().getEffect12302MaxTimes()) {
			return false;
		}
		return true;
	}

	public void setBuff12333(int atkval, int firVal) {
		if (!isYuanCheng() || buff12333.first == getBattleRound() || atkval == 0) {
			return;
		}
		buff12333 = HawkTuples.tuple(getBattleRound(), atkval, firVal);
		addDebugLog(" # 12333于本回合为至多 2 个友军远程单位提供火力支援，使 {} 超能攻击{}、攻击增加 {}（该效果无法叠加）", getUUID(), buff12333.third, buff12333.second);
	}

	public int getBuff12333Val(boolean fire) {
		if (isYuanCheng() && buff12333.first == getBattleRound()) {
			if (fire) {
				return buff12333.third;
			}
			return buff12333.second;
		}
		return 0;
	}

	public void setBuff12574(Buff12574 buff12574) {
		this.buff12574 = buff12574;
	}

	public int getBuff12574Val(EffType eff) {
		if (buff12574.round > 0 && buff12574.round < getBattleRound()) {
			// addDebugLog("##{} 无聚焦增幅护盾 {}", getUUID(), eff);
			return 0;
		}

		// addDebugLog("##{} 聚焦增幅护盾 {} 12574:{} 12574:{} 12576:{} 12577:{} 12578:{}", getUUID(), eff , buff12574.eff12574,buff12574.eff12575,buff12574.eff12576,buff12574.eff12577,buff12574.eff12578);
		switch (eff) {
		case EFF_12574:
			return buff12574.eff12574;
		case EFF_12575:
			return buff12574.eff12575;
		case EFF_12576:
			return buff12574.eff12576;
		case EFF_12577:
			return buff12574.eff12577;
		case EFF_12578:
			return buff12574.eff12578;
		default:
			break;
		}
		return 0;
	}

	public void setBuff12337(BattleSoldier_7 from, int hpval, int defVal, int val12339) {
		if (!isYuanCheng() || buff12337.first == getBattleRound() || hpval == 0) {
			return;
		}
		buff12337 = HawkTuples.tuple(getBattleRound(), hpval, val12339, defVal, from);
		addDebugLog(" # 12337于本回合为至多 2 个友军远程单位提供战场援护，使{}防御 {}、生命增加 {}（该效果无法叠加）", getUUID(), buff12337.fourth, buff12337.second);
	}

	public int getBuff12337HpVal() {
		if (isYuanCheng() && buff12337.first == getBattleRound()) {
			return buff12337.second;
		}
		return 0;
	}

	public int getBuff12337DefVal() {
		if (isYuanCheng() && buff12337.first == getBattleRound()) {
			return buff12337.fourth;
		}
		return 0;
	}

	private void fuhuo12339() {
		if (isYuanCheng() && buff12337.first == getBattleRound()) {
			int dead = roundDead.getOrDefault(buff12337.first, 0) + roundDead.getOrDefault(buff12337.first - 1, 0);
			if (dead == 0) {
				return;
			}
			int fuhuo = (int) (dead * GsConst.EFF_PER * buff12337.third);
			fuhuo = Math.max(fuhuo, 1);
			this.deadCnt -= fuhuo;
			buff12337.fifth.eff12339Cnt += fuhuo;
			buff12337.fifth.eff12339Power += fuhuo * soldierCfg.getPower();
			addDebugLog(" # {} 12339在战斗处于偶数回合时，开启聚能环护模式:  并在本回合结束时恢复{}于最近的 2 个回合损失的 {}的部队", buff12337.fifth.getUUID(), getUUID(), fuhuo);
		}
	}

	public HawkTuple2<Integer, Double> getSkill12362Debuff() {
		return skill12362Debuff;
	}

	public void setSkill12362Debuff(HawkTuple2<Integer, Double> skill12362Debuff) {
		if (this.skill12362Debuff != null && this.skill12362Debuff.first >= getBattleRound()) {
			return;
		}
		this.skill12362Debuff = skill12362Debuff;
	}

	int skill12362DebuffVal() {
		if (Objects.isNull(skill12362Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill12362Debuff.first) {
			return 0;
		}
		addDebugLog("### {} 【12362】精准打击: 被定点猎杀命中的敌方单位，有 XX.XX% 的概率因武器遭受打击而使其丧失 XX.XX% 的伤害效率 ", getUUID(), skill12362Debuff.second.intValue());
		return skill12362Debuff.second.intValue();
	}

	public HawkTuple2<Integer, Double> getSkill11042Debuff() {
		return skill11042Debuff;
	}

	public void setSkill11042Debuff(HawkTuple2<Integer, Double> skill11042Debuff) {
		if (this.skill11042Debuff != null && this.skill11042Debuff.first >= getBattleRound()) {
			return;
		}
		this.skill11042Debuff = skill11042Debuff;
	}

	int skill11042DebuffVal() {
		if (Objects.isNull(skill11042Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill11042Debuff.first) {
			return 0;
		}
		addDebugLog("### {} 【11042】效果A：超能攻击、攻击减少 XX%（该效果不可叠加，持续 10 回合）（此效果对空军 翻倍）{} {}", getUUID(), skill11042Debuff.second.intValue());
		return skill11042Debuff.second.intValue();
	}

	public HawkTuple2<Integer, Double> getSkill11043Debuff() {
		return skill11043Debuff;
	}

	public void setSkill11043Debuff(HawkTuple2<Integer, Double> skill11043Debuff) {
		if (this.skill11043Debuff != null && this.skill11043Debuff.first >= getBattleRound()) {
			return;
		}
		this.skill11043Debuff = skill11043Debuff;
	}

	int skill11043DebuffVal() {
		if (Objects.isNull(skill11043Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill11043Debuff.first) {
			return 0;
		}
		addDebugLog("### {} 【11043】效果B：防御、生命减少 XX%（该效果不可叠加，持续 10 回合）（此效果对空军 翻倍）{} {}", getUUID(), skill11043Debuff.second.intValue());
		return skill11043Debuff.second.intValue();
	}

	public void setSkill12461DeBuff(HawkTuple3<Integer, Integer, BattleSoldier_4> skill12461Debuff) {
		if (this.skill12461Debuff != null && this.skill12461Debuff.first >= getBattleRound()) {
			return;
		}
		this.skill12461Debuff = skill12461Debuff;
		if (solomonPet != null) {
			solomonPet.setSkill12461DeBuff(skill12461Debuff);
		}
	}

	int skill12461DeBuffVal() {
		if (Objects.isNull(skill12461Debuff)) {
			return 0;
		}
		if (!skill12461Debuff.third.isAlive()) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill12461Debuff.first) {
			return 0;
		}
		return skill12461Debuff.second.intValue();
	}

	public BattleSoldier skill12461DeBuffFrom() {
		if (Objects.isNull(skill12461Debuff)) {
			return null;
		}
		if (!skill12461Debuff.third.isAlive()) {
			return null;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill12461Debuff.first) {
			return null;
		}
		return skill12461Debuff.third;
	}

	public void setSkill12465Buff(HawkTuple2<Integer, Integer> skill12465Buff) {
		this.skill12465Buff = skill12465Buff;
	}

	int skill12465BuffVal() {
		if (Objects.isNull(skill12465Buff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill12465Buff.first) {
			return 0;
		}
		return skill12465Buff.second;
	}

	public int getEffect12116Maxinum() {
		return effect12116Maxinum;
	}

	public void setEffect12116Maxinum(int effect12116Maxinum) {
		this.effect12116Maxinum = effect12116Maxinum;
	}

	/** 兵种1-8损失比例  0 到 1  */
	public double lossRate() {
		return deadCnt * 1.0 / oriCnt;
	}
	
	public int lossRatePct() {
		return (int) (lossRate() * 100);
	}
	
	public void setSkill12116Buff(HawkTuple2<Integer, Integer> skill12116Buff) {
		this.skill12116Buff = skill12116Buff;
	}

	int skill12116BuffVal() {
		if (Objects.isNull(skill12116Buff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill12116Buff.first) {
			return 0;
		}
		return skill12116Buff.second;
	}

	public void setSkill14601Buff(HawkTuple2<Integer, Integer> skill461Buff) {
		this.skill461Buff = skill461Buff;
	}

	int skill14601BuffVal() {
		if (Objects.isNull(skill461Buff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill461Buff.first) {
			return 0;
		}
		return skill461Buff.second;
	}

	public void setEff12541Soldier(BattleSoldier eff12541Soldier) {
		this.eff12541Soldier = eff12541Soldier;
	}

	public int getBuff12561(BattleSoldier tar) {
		int round = getTroop().getBattle().getBattleRound();
		if (getEffVal(EffType.EFF_12561) > 0 && round >= ConstProperty.getInstance().effect12561AtkRound
				&& round % ConstProperty.getInstance().effect12561AtkRound < ConstProperty.getInstance().effect12561ContinueRound) {
			int buff12561 = (int) (getEffVal(EffType.EFF_12561) * GsConst.EFF_PER * ConstProperty.getInstance().effect12561SoldierAdjustMap.getOrDefault(tar.getType(), 10000));
			addDebugLog("使自身所有单位受到伤害减少 20%【12561】  {}", buff12561);
			return buff12561;
		}
		return 0;
	}

	public int getDebuff12561(BattleSoldier def) {
		if (def.getBuff12561(this) > 0) {
			addDebugLog("【12561】敌方所有单位攻击减少  {}", ConstProperty.getInstance().effect12561BaseVaule);
			return ConstProperty.getInstance().effect12561BaseVaule;
		}
		return 0;
	}

	public void addSorek12611Debuff(BattleSoldier atk) {
		if (getSorek12611DebuffVal(atk) > 0) {
			return;
		}

		int cnt = 0;
		for (Debuff12611 debuff : sorek12611Debuff.values()) {
			if (debuff.round >= getBattleRound()) {
				cnt++;
			}
		}
		if (cnt >= ConstProperty.getInstance().effect12611Maxinum) {
			return;
		}
		Debuff12611 debuff = new Debuff12611();
		debuff.round = getBattleRound() + ConstProperty.getInstance().effect12611ContinueRound - 1;
		debuff.value = atk.getEffVal(EffType.HERO_12612) + atk.getEffVal(EffType.HERO_12633);
		debuff.atk = atk;
		sorek12611Debuff.put(atk.getPlayerId(), debuff);
		addDebugLog("##{} -> {} 【雷感状态】 {} 层 结束 {}", atk.getUUID(), getUUID(), cnt + 1, debuff.round);
	}

	public int getSorek12611DebuffVal(BattleSoldier atk) {
		if (!sorek12611Debuff.containsKey(atk.getPlayerId())) {
			return 0;
		}
		Debuff12611 debuff = sorek12611Debuff.get(atk.playerId);
		if (debuff.round < getBattleRound()) {
			return 0;
		}
		return debuff.value;
	}

	public void sorek12611DebuffValRoundStart() {
		sorek12611Debuff.values().forEach(debuff -> debuff.effect12613Num = 0);
	}

	public void sorek12611DebuffHurt() {
		for (Debuff12611 debuff : sorek12611Debuff.values()) {
			if (debuff.round < getBattleRound() || getFreeCnt() == 0 || debuff.effect12613Num >= ConstProperty.getInstance().effect12613Maxinum) {
				continue;
			}
			int hurtRate = debuff.atk.getEffVal(EffType.HERO_12613) + debuff.atk.getEffVal(EffType.HERO_12631);
			debuff.atk.additionalAtk(this,hurtRate, true, true, "### 【12613】扰乱效应：【雷感状态】的单位每次造成伤害时，受到一次雷感伤害");
			debuff.effect12613Num++;
		}
	}

	public void sorek12611DebuffOverHurt() {
		for (Debuff12611 debuff : sorek12611Debuff.values()) {
			if (debuff.round != getBattleRound() || getFreeCnt() == 0) {
				continue;
			}
			int hurtRate = debuff.atk.getEffVal(EffType.HERO_12614) + ConstProperty.getInstance().effect12614BaseVaule;
			debuff.atk.additionalAtk(this, hurtRate, true, true, "### 【12614】闭环效应：【雷感状态】结束时，受到伤害");
		}
	}

	public void setSkill12642Debuff(HawkTuple2<Integer, Double> skill12642Debuff) {
		if (this.skill12642Debuff != null && this.skill12642Debuff.first >= getBattleRound()) {
			return;
		}
		this.skill12642Debuff = skill12642Debuff;
	}

	int skill12642DebuffVal() {
		if (Objects.isNull(skill12642Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > skill12642Debuff.first) {
			return 0;
		}
		addDebugLog("### {} 【12642】效果B：防御、生命减少 XX%（该效果不可叠加，持续 10 回合）（此效果对空军 翻倍）{} {}", getUUID(), skill12642Debuff.second.intValue());
		return skill12642Debuff.second.intValue();
	}

	private int buff12651Val(BattleSoldier def) {
		SoldierType type = def.getType();
		boolean bfalse = type.getNumber() == 1 || type.getNumber() == 2 || type.getNumber() == 5 || type.getNumber() == 6;
		if (!bfalse) {
			return 0;
		}
		int battleRound = getBattleRound();
		int cround = battleRound % ConstProperty.getInstance().effect12651AtkRound;
		if (battleRound > 1 && cround >= 0 && cround < ConstProperty.getInstance().effect12651ContinueRound) {
			return (int) (getEffVal(EffType.EFF_12651) * GsConst.EFF_PER * ConstProperty.getInstance().effect12651SoldierAdjustMap.getOrDefault(def.getType(), 10000));
		}
		return 0;
	}

	public void debuff34601(BattleSoldier_3 atk) {
		BattleSoldierSkillCfg scfg = atk.getSkill(PBSoldierSkill.SOLDIER_SKILL_34601);
		if (scfg.getP2IntVal() <= 0 || debuff34601.containsKey(atk) || debuff34601.size() >= scfg.getP3IntVal()) {
			return;
		}
		if (scfg.getP1().contains(getType().getNumber() + "")) {
			debuff34601.put(atk, scfg.getP4IntVal() + getBattleRound() - 1);
		}
	}

	private void debuff34601Atk() {
		List<BattleSoldier_3> all = new ArrayList<>(debuff34601.keySet());
		for (BattleSoldier_3 atk : all) {
			BattleSoldierSkillCfg scfg = atk.getSkill(PBSoldierSkill.SOLDIER_SKILL_34601);
			
			atk.additionalAtk(this,scfg.getP2IntVal(), true, true, "### 【34601】对敌方单位攻击后点燃目标，使其每回合结束时受到+XX%伤害（该效果可叠加，至多 X 层，持续X回合）");
			
			if (debuff34601.get(atk) <= getBattleRound()) {
				debuff34601.remove(atk);
			}
		}

	}

	public void setDebuff44601(int effPer) {
		this.debuff44601 = effPer;
		
	}
	
	public void setMan12701(HawkTuple2<Integer, Integer> man12701) {
		this.man12701 = man12701;
	}

	int man12701Val() {
		if (Objects.isNull(man12701)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > man12701.first) {
			return 0;
		}
		
		int result = (int) (man12701.second * GsConst.EFF_PER * ConstProperty.getInstance().effect12701SoldierAdjustMap.getOrDefault(getType(), 0));
		return result;
	}
	
	public void effect12701Atk() {
		try {
			if (getEffVal(EffType.EFF_12701) <= 0) {
				return;
			}
			if (getBattleRound() != 1 && getBattleRound() % ConstProperty.getInstance().effect12701AtkRound != 0) {
				return;
			}

			List<BattleSoldier> enemyList = getTroop().getEnemyTroop().getSoldierList().stream().filter(BattleSoldier::canBeAttack).filter(s -> s.man12701Val() == 0)
					.collect(Collectors.toList());
			String tarpid = RandomUtil.randomWeightObject(enemyList).getPlayerId();

			for (BattleSoldier tar : enemyList) {
				if (!tar.getPlayerId().equals(tarpid) || !tar.isJinZhan()) {
					continue;
				}
				int first = getBattleRound() + ConstProperty.getInstance().effect12701ContinueRound - 1;
				HawkTuple2<Integer, Integer> man12701 = HawkTuples.tuple(first, getEffVal(EffType.EFF_12701));
				tar.setMan12701(man12701);
				addDebugLog("【12701】进攻战斗时，每X（effect12701AtkRound）回合随机选中敌方某指挥官全部近战部队卷入 {}", tar.getUUID());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void addAstiaya12674Debuff(BattleSoldier atk) {
		if (getSorek12611DebuffVal(atk) > 0) {
			return;
		}

		int cnt = 0;
		for (Debuff12674 debuff : astiaya12674Debuff.values()) {
			if (debuff.effect12674ContinueRound >= getBattleRound()) {
				cnt++;
			}
		}
		if (cnt >= ConstProperty.getInstance().effect12674Maxinum) {
			return;
		}
		Debuff12674 debuff = new Debuff12674();
		debuff.effect12674ContinueRound = getBattleRound() + ConstProperty.getInstance().effect12674ContinueRound - 1;
		debuff.effect12674BaseVaule1 = ConstProperty.getInstance().effect12674BaseVaule1;
		debuff.effect12674BaseVaule2 = ConstProperty.getInstance().effect12674BaseVaule2;
		astiaya12674Debuff.put(atk.getPlayerId(), debuff);
		addDebugLog("##{} -> {} 【12674】协同攻击 命中后降低其攻击+500.00%（effect12674BaseVaule1），超能攻击+100.00%（effect12674BaseVaule2） 结束 {}", atk.getUUID(), getUUID(), cnt + 1, debuff.effect12674ContinueRound);
	}
	
	public int getAstiaya12674DebuffVal(boolean fire) {
		int result = 0;
		for (Debuff12674 debuff : astiaya12674Debuff.values()) {

			if (debuff.effect12674ContinueRound >= getBattleRound()) {
				if (fire) {
					result += debuff.effect12674BaseVaule2;
				} else {

					result += debuff.effect12674BaseVaule1;
				}
			}
		}
		if(result>0){
			addDebugLog("【12674】协同攻击 降低攻击/超能攻击  {}" , result);
		}
		return result;
	}

	public long getForceFieldMarch() {
		return forceFieldMarch;
	}

	public void setForceFieldMarch(long forceFieldMarch) {
		this.forceFieldMarch = forceFieldMarch;
	}
	
	public double forceField(BattleSoldier atk, double hurtVal){
		if(atk.getTroop() == getTroop()){
			addDebugLog("**我日,护盾吸收错位了. 快通知芦文涛");
		}
		return getTroop().forceField(atk, this, hurtVal);
	}
}
