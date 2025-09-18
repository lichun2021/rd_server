package com.hawk.game.module.nationMilitary.rank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.nationMilitary.cfg.NationMilitaryCfg;
import com.hawk.game.module.nationMilitary.cfg.NationMilitaryConstCfg;
import com.hawk.game.module.nationMilitary.cfg.NationMilitaryRankTimeCfg;
import com.hawk.game.module.nationMilitary.cfg.NationMilitaryResetTimeCfg;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Rank.NationMilitaryRankInfo;

import redis.clients.jedis.Tuple;

public class NationMilitaryRankObj {
	private final static NationMilitaryRankObj instance = new NationMilitaryRankObj();

	private final String RESET_TERM_SCORE_KEY = "nation_military_score:";

	private final String DING_BANG_KEY = "nation_military_rank:";

	/**高级军衔最小军功值*/
	int shangbangScore;
	int maxRankNum;
	int resetTerm;
	int dingBangTerm;
	long nextResetTime;
	long nextDingBangTime;

	// 军衔榜
	private MilitaryRankCollection rankCollection;
	// 军功榜
	private MilitaryRankCollection rankCollectionTwo;

	/** 最小上榜分数*/
	private int calshangBangScore() {
		int result = Integer.MAX_VALUE;
		ConfigIterator<NationMilitaryCfg> it = HawkConfigManager.getInstance().getConfigIterator(NationMilitaryCfg.class);
		for (NationMilitaryCfg cfg : it) {
			if (cfg.getAdvanced() > 0) {
				result = Math.min(result, cfg.getMerit());
			}
		}
		return result;
	}

	/**排行最多*/
	private int calmaxRankNum() {
		int result = 0;
		ConfigIterator<NationMilitaryCfg> it = HawkConfigManager.getInstance().getConfigIterator(NationMilitaryCfg.class);
		for (NationMilitaryCfg cfg : it) {
			if (cfg.getAdvanced() > 0) {
				result += cfg.getNum();
			}
		}
		return result;
	}

	/**
	 *  分数重置期, 定榜分数梆定. 重置后必须登录才能上记分
	 */
	private int calResetTerm() {
		ConfigIterator<NationMilitaryResetTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(NationMilitaryResetTimeCfg.class);
		int term = 0;
		long now = HawkTime.getMillisecond();
		for (NationMilitaryResetTimeCfg cfg : it) {
			if (cfg.getResetTimeLongValue() > now) {
				break;
			}
			term = Math.max(term, cfg.getId());
		}
		return term;
	}

	/**定榜期 取对应resetTerm 分数排名*/
	private int calDingBangTerm() {
		ConfigIterator<NationMilitaryRankTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(NationMilitaryRankTimeCfg.class);
		int term = 0;
		long now = HawkTime.getMillisecond();
		for (NationMilitaryRankTimeCfg cfg : it) {
			if (cfg.getResetTimeLongValue() > now) {
				break;
			}
			term = Math.max(term, cfg.getId());
		}
		return term;
	}

	/**
	 * 矫正添加军功值, 每一期跨服有上限
	 * @param addVal
	 * @param service
	 * @return 实际可增加值 
	 */
	public int correctBattleMilitaryExp(Player player, int addVal) {
		NationMilitaryEntity commanderEntity = player.getData().getNationMilitaryEntity();
		NationMilitaryConstCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(NationMilitaryConstCfg.class);
		addVal = Math.min(addVal, kvcfg.getMax() - commanderEntity.getNationMilitaryExp());
		addVal = Math.min(addVal, kvcfg.getCrossMax() - commanderEntity.getNationMilitaryBattleExp());
		return Math.max(0, addVal);
	}

	/**
	 * 矫正添加军功值增加非战斗获得军功值
	 * @param player
	 * @param addVal
	 * @return 实际可增加值 
	 */
	public int correctMilitaryExp(Player player, int addVal) {
		NationMilitaryConstCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(NationMilitaryConstCfg.class);
		NationMilitaryEntity commanderEntity = player.getData().getNationMilitaryEntity();
		addVal = Math.min(addVal, kvcfg.getMax() - commanderEntity.getNationMilitaryExp());
		return Math.max(0, addVal);
	}

	/**
	 * 更新榜单分数
	 * @param player
	 */
	public void zaddScore(Player player) {
		double score = player.getData().getNationMilitaryEntity().getNationMilitaryExp();
		if (score < shangbangScore) { // 低于上榜分数
			return;
		}

		double t = HawkTime.getMillisecond() * 0.0000000000001;
		score -= t; // 同分数, 先达到的一方在前

		String key = resetScoreTermKey(player.getMainServerId());
		RedisProxy.getInstance().getRedisSession().zAdd(key, score, player.getId());
	}

	private NationMilitaryRankObj() {
	}

	private long lastrefreshRankTwo;
	public void refreshRank() {
		try {
			if(GsApp.getInstance().getCurrentTime() - lastrefreshRankTwo < TimeUnit.MINUTES.toMillis(1)){
				return;
			}
			lastrefreshRankTwo = GsApp.getInstance().getCurrentTime();
			this.shangbangScore = calshangBangScore();
			this.maxRankNum = calmaxRankNum();
			this.resetTerm = calResetTerm();
			this.nextResetTime = calNextResetTime();
			this.dingBangTerm = calDingBangTerm();
			this.nextDingBangTime = calNextDingBangTime();
			
			refreshRankCollection();
			refreshRankCollectionTwo();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	private long calNextDingBangTime() {
		ConfigIterator<NationMilitaryRankTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(NationMilitaryRankTimeCfg.class);
		long now = HawkTime.getMillisecond();
		for (NationMilitaryRankTimeCfg cfg : it) {
			if (cfg.getResetTimeLongValue() > now) {
				return cfg.getResetTimeLongValue();
			}
		}
		return Long.MAX_VALUE;
	}

	private void refreshRankCollection() {
		final String dingBangkey =DING_BANG_KEY + GsConfig.getInstance().getServerId() + ":" + resetTerm + ":" + dingBangTerm;
		if (!RedisProxy.getInstance().getRedisSession().exists(dingBangkey)) {
			this.rankCollection = dingBang();
			RedisProxy.getInstance().getRedisSession().setString(dingBangkey, this.rankCollection.serializ());
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				player.getData().getNationMilitaryEntity().getNeedCheck().set(true);
			}
		}

		if (Objects.isNull(this.rankCollection)) {
			MilitaryRankCollection rankC = new MilitaryRankCollection();
			rankC.mergeFrom(RedisProxy.getInstance().getRedisSession().getString(dingBangkey));
			this.rankCollection = rankC;
		}
		this.rankCollection.buildSortedRank();

	}

	private void refreshRankCollectionTwo() {
		this.rankCollectionTwo = dingBangTwo();
		this.rankCollectionTwo.buildSortedRank();
	}

	private long calNextResetTime() {
		ConfigIterator<NationMilitaryResetTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(NationMilitaryResetTimeCfg.class);
		long time = Long.MAX_VALUE;
		long now = HawkTime.getMillisecond();
		for (NationMilitaryResetTimeCfg cfg : it) {
			if (cfg.getResetTimeLongValue() < now) {
				continue;
			}
			time = Math.min(time, cfg.getResetTimeLongValue());
		}
		return time;
	}

	/**定榜
	 * @return */
	private MilitaryRankCollection dingBang() {

		List<NationMilitaryCfg> advCfgs = HawkConfigManager.getInstance().getConfigIterator(NationMilitaryCfg.class).stream()
				.filter(cfg -> cfg.getAdvanced() > 0)
				.sorted(Comparator.comparingInt(NationMilitaryCfg::getLevel).reversed())
				.collect(Collectors.toList());

		List<NationMilitaryCfg> shangbangList = new LinkedList<>();
		for (NationMilitaryCfg cfg : advCfgs) {
			for (int i = 0; i < cfg.getNum(); i++) {
				shangbangList.add(cfg);
			}
		}

		// 每次定榜只有一次上榜机会,且排名不改变
		Set<Tuple> rankSet = RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(resetScoreTermKey(GsConfig.getInstance().getServerId()), 0, maxRankNum - 1, 0);
		List<Tuple> scoreList = new ArrayList<>(rankSet);
		MilitaryRankCollection rankC = new MilitaryRankCollection();
		int rank = 1;

		for (NationMilitaryCfg mcfg : shangbangList) {
			if (scoreList.isEmpty()) {
				break;
			}
			Tuple tuple = scoreList.get(0);
			if (Math.ceil(tuple.getScore()) < mcfg.getMerit()) {
				continue;
			}
			tuple = scoreList.remove(0);

			String playerId = tuple.getElement();
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (Objects.isNull(player)) {
				continue;
			}
			MilitaryRank rankInfo = new MilitaryRank();
			rankInfo.setPlayerId(playerId);
			rankInfo.setRank(rank);
			rankInfo.setCfgId(mcfg.getLevel());
			rankInfo.setScore((int) Math.ceil(tuple.getScore()));
			rankC.addRank(rankInfo);
			rank++;
		}
		return rankC;
	}

	private MilitaryRankCollection dingBangTwo() {
		// 每次定榜只有一次上榜机会,且排名不改变
		Set<Tuple> rankSet = RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(resetScoreTermKey(GsConfig.getInstance().getServerId()), 0, 100 - 1, 0);
		List<Tuple> scoreList = new ArrayList<>(rankSet);
		MilitaryRankCollection rankC = new MilitaryRankCollection();
		int rank = 1;

		for (Tuple tuple : scoreList) {
			String playerId = tuple.getElement();
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (Objects.isNull(player)) {
				continue;
			}
			MilitaryRank rankInfo = new MilitaryRank();
			rankInfo.setPlayerId(playerId);
			rankInfo.setRank(rank);
			// rankInfo.setCfgId(mcfg.getLevel());
			rankC.addRank(rankInfo);
			rankInfo.setScore((int) Math.ceil(tuple.getScore()));
			rank++;
		}
		return rankC;
	}

	private String resetScoreTermKey(String serverId) {
		int resetTermId = getResetTerm();
		return RESET_TERM_SCORE_KEY + GlobalData.getInstance().getMainServerId(serverId) + ":" + resetTermId;
	}

	public static NationMilitaryRankObj getInstance() {
		return instance;
	}

	public String getRESET_TERM_SCORE_KEY() {
		return RESET_TERM_SCORE_KEY;
	}

	public String getDING_BANG_KEY() {
		return DING_BANG_KEY;
	}

	public int getShangbangScore() {
		return shangbangScore;
	}

	public int getMaxRankNum() {
		return maxRankNum;
	}

	public int getResetTerm() {
		return resetTerm;
	}

	public int getDingBangTerm() {
		return dingBangTerm;
	}

	public MilitaryRankCollection getRankCollection() {
		return rankCollection;
	}

	public Optional<MilitaryRank> getPlayerMilitaryRank(String id) {
		return Optional.ofNullable(rankCollection.getRankByPlayerId(id));
	}

	public List<NationMilitaryRankInfo> getSortedRank() {
		return rankCollection.getSortedRank();
	}

	/** 下次分数重置*/
	public long getNextResetTime() {
		return nextResetTime;
	}

	public long getNextDingBangTime() {
		return nextDingBangTime;
	}

	public List<NationMilitaryRankInfo> getSortedRankTwo() {
		return rankCollectionTwo.getSortedRank();
	}

	public MilitaryRankCollection getRankCollectionTwo() {
		return rankCollectionTwo;
	}

}
