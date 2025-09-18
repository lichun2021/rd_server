package com.hawk.game.service.tiberium;

import java.util.Arrays;
import java.util.List;

import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;

import com.hawk.game.protocol.TiberiumWar.TLWFinalStage;

public interface TiberiumConst {
	/**
	 * ELO首秀初始化额外积分参数
	 */
	public static final int ELO_EXT_SCORE_PARAM = 100000000;

	/** 赛季入围联盟数量 */
	public static final int GUILD_PICK_CNT = 256;

	/** 赛季小组数量 */
	public static final int TEAM_CNT = 32;

	/** 赛季小组赛每队人数 */
	public static final int TEAM_MEMBER_CNT = 8;

	/** S组今后赛入围名次范围 (每组排名第1、2,每组第3名 排序取1-32)*/
	public static final HawkTuple2<Integer, Integer> S_RANGE = new HawkTuple2<Integer, Integer>(1, 2);
	public static final HawkTuple3<Integer,Integer,Integer> S_RANGE_EXT = new HawkTuple3<Integer, Integer,Integer>(3, 1,32);
	/** A组今后赛入围名次范围 (每组排名第4、5,每组第3名 排序取33-48,每组第6名 排序取1-16)*/
	public static final HawkTuple2<Integer, Integer> A_RANGE = new HawkTuple2<Integer, Integer>(4, 5);
	public static final HawkTuple3<Integer,Integer,Integer> A_RANGE_EXT1 = new HawkTuple3<Integer, Integer,Integer>(3, 33,48);
	public static final HawkTuple3<Integer,Integer,Integer> A_RANGE_EXT2 = new HawkTuple3<Integer, Integer,Integer>(6, 1,16);
	/** B组今后赛入围名次范围 (每组排名第7、8,每组第三6名 排序取17-48)*/
	public static final HawkTuple2<Integer, Integer> B_RANGE = new HawkTuple2<Integer, Integer>(7, 8);
	public static final HawkTuple3<Integer,Integer,Integer> B_RANGE_EXT = new HawkTuple3<Integer, Integer,Integer>(6, 17,48);

	/** 淘汰赛组别列表 */
	public static final List<TLWGroupType> FINAL_WAR_GROUPS = Arrays.asList(TLWGroupType.S_GROUP, TLWGroupType.A_GROUP);

	/**
	 * 决斗开启轮次
	 */
	//public static final int FINAL_START_TERMID = 12;
	
	/**
	 * 淘汰赛开始期数
	 */
	public static final int ELIMINATION_START_TERMID = 8;
	/**
	 * 最终对决场次
	 */
	public static final int ELIMINATION_FINAL_TERMID = 21;
	/**
	 * 赛季标识偏移
	 */
	public static final int SEASON_OFFSET = 10000;

	/**
	 * 联赛相关redis存储有效期(s)
	 */
	public static final int TLW_EXPIRE_SECONDS = 24 * 3600 * 360;

	public enum TWActivityState {
		/** 关闭 */
		CLOSE(-1),
		/** 未开启 */
		NOT_OPEN(0),
		/** 报名阶段 */
		SIGN(1),
		/** 匹配阶段 */
		MATCH(2),
		/** 战斗阶段 */
		WAR(3);

		int value;

		TWActivityState(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}
	}

	public enum FightState {
		/** 未开启 */
		NOT_OPEN,
		/** 开启中 */
		OPEN,
		/** 已结束 */
		FINISH,
		/** 已发奖 */
		AWARDED;
	}

	public enum RoomState {
		/** 未初始化 */
		NOT_INIT,
		/** 初始化完成 */
		INITED,
		/** 已结束 */
		CLOSE,
		/** 初始化失败 */
		INITED_FAILED;
	}

	/**
	 * 联赛轮次类型
	 * 
	 * @author admin
	 */
	public enum TLWWarType {
		/** 小组赛 */
		TEAM_WAR(1),
		/** 淘汰赛 */
		FINAL_WAR(2);
		int value;

		TLWWarType(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}

		public static TLWWarType getType(int value) {
			for (TLWWarType type : values()) {
				if (type.value == value) {
					return type;
				}
			}
			return null;
		}
	}

	/**
	 * 联赛联盟类型
	 * 
	 * @author admin
	 *
	 */
	public enum TLWGroupType {
		/** 普通组 */
		NORMAL(0, 1),
		/** 常规赛小组 */
		TEAM_GROUP(1, 2),
		/** 挑战组 */
		S_GROUP(2, 6),
		/** 挑战组 */
		A_GROUP(3, 5),
		/** 挑战组 */
		B_GROUP(4, 4),
		/** 被淘汰 */
		KICK_OUT(5, 3);

		int value;

		/** 排行顺序 */
		int rankOrder;

		TLWGroupType lowGroup;

		TLWGroupType(int value, int rankOrder) {
			this.value = value;
			this.rankOrder = rankOrder;
		}

		public int getNumber() {
			return value;
		}

		public int getRankOrder() {
			return rankOrder;
		}
		public static TLWGroupType getType(int value) {
			for (TLWGroupType group : values()) {
				if (group.value == value) {
					return group;
				}
			}
			return null;
		}
	}

	/**
	 * 淘汰赛组别
	 */
	public enum TLWEliminationGroupType{
		/** 无*/
		ELIMINATION_NONE(0),
		/** 淘汰赛胜者组*/
		ELIMINATION_WIN(1),
		/** 淘汰赛败者组*/
		ELIMINATION_LOSS(2);
		int value;
		private TLWEliminationGroupType(int value) {
			this.value = value;
		}
		public int getValue() {
			return value;
		}
		public static TLWEliminationGroupType getType(int value) {
			for (TLWEliminationGroupType type : values()) {
				if (type.value == value) {
					return type;
				}
			}
			return null;
		}
	}
	
	/**
	 * 战场类型
	 */
	public enum TLWBattleType{
		/** 普通组 */
		NORMAL(0),
		/** 淘汰赛胜者组对战*/
		ELIMINATION_WIN_GROUP_BATTLE(1),
		/** 淘汰赛败者组对战*/
		ELIMINATION_LOSS_GROUP_BATTLE(2),
		/** 淘汰赛决赛组对战*/
		ELIMINATION_FINAL_GROUP_BATTLE(3),
		/** 小组内*/
		TEAM_GROUP_BATTLE(4);
		
		int value;
		private TLWBattleType(int value) {
			this.value = value;
		}
		public int getValue() {
			return value;
		}
		
		public static TLWBattleType getType(int value) {
			for (TLWBattleType type : values()) {
				if (type.value == value) {
					return type;
				}
			}
			return null;
		}
	}
	
	/**
	 * 决赛阶段
	 * 
	 * @author admin
	 */
	public enum TLWWarStage {
		/** 128进64 */
		TO_64(1, TLWFinalStage.TLW128_64),
		/** 64进32 */
		TO_32(1, TLWFinalStage.TLW64_32),
		/** 32进16 */
		TO_16(1, TLWFinalStage.TLW32_16),
		/** 16进8 */
		TO_8(1, TLWFinalStage.TLW16_8),
		/** 8进4 */
		TO_4(2, TLWFinalStage.TLW8_4),
		/** 4进2 */
		TO_2(3, TLWFinalStage.TLW4_2),
		/** 2进1 */
		TO_1(4, TLWFinalStage.TLW2_1);

		private int value;

		private TLWFinalStage stage;

		private TLWWarStage nestStage;

		private TLWWarStage lastStage;

		private boolean isFinalBattle = false;

		static {
			TO_8.setNestStage(TO_4);
			TO_4.setNestStage(TO_2);
			TO_2.setNestStage(TO_1);
			TO_1.setNestStage(TO_1);
			TO_8.setLastStage(TO_8);
			TO_4.setLastStage(TO_8);
			TO_2.setLastStage(TO_4);
			TO_1.setLastStage(TO_2);
			TO_1.setTop(true);
		}

		public int getValue() {
			return value;
		}

		TLWWarStage(int value, TLWFinalStage stage) {
			this.value = value;
			this.stage = stage;
		}

		public TLWWarStage getNestStage() {
			return nestStage;
		}

		private void setNestStage(TLWWarStage nestStage) {
			this.nestStage = nestStage;
		}

		public TLWWarStage getLastStage() {
			return lastStage;
		}

		private void setLastStage(TLWWarStage lastStage) {
			this.lastStage = lastStage;
		}

		public boolean isTop() {
			return isFinalBattle;
		}

		private void setTop(boolean isTop) {
			this.isFinalBattle = isTop;
		}

		public TLWFinalStage getStage() {
			return stage;
		}

		public void setStage(TLWFinalStage stage) {
			this.stage = stage;
		}

		public static TLWWarStage getType(int value) {
			for (TLWWarStage stage : values()) {
				if (stage.value == value) {
					return stage;
				}
			}
			return null;
		}

		/**
		 * 根据期数获取决赛阶段
		 * 
		 * @param termId
		 * @return
		 */
		public static TLWWarStage getStageByTermId(int termId) {
			switch (termId) {
			case 12:
				return TLWWarStage.TO_64;
			case 13:
				return TLWWarStage.TO_32;
			case 14:
				return TLWWarStage.TO_16;
			case 15:
				return TLWWarStage.TO_8;
			case 16:
				return TLWWarStage.TO_4;
			case 17:
				return TLWWarStage.TO_2;
			case 18:
				return TLWWarStage.TO_1;

			default:
				break;
			}
			return null;
		}

		/**
		 * 根据阶段获取期数
		 * 
		 * @return
		 */
		public int getTermId() {
			switch (this) {
			case TO_64:
				return 12;
			case TO_32:
				return 13;
			case TO_16:
				return 14;
			case TO_8:
				return 15;
			case TO_4:
				return 16;
			case TO_2:
				return 17;
			case TO_1:
				return 18;
			default:
				break;
			}
			return -1;
		}
	}

	public enum TLWActivityState {
		/** 关闭 */
		TLW_CLOSE(-1),
		/** 未开启 */
		TLW_NOT_OPEN(1),
		/** 准备阶段 */
		TLW_PEACE(2),
		/** 匹配阶段 */
		TLW_MATCH(3),
		/** 出战管理 */
		TLW_WAR_MANGE(4),
		/** 出战等待 */
		TLW_WAR_WAIT(5),
		/** 战斗阶段 */
		TLW_WAR_OPEN(6),
		/** 赛季结束展示阶段,客户端展示不做区分,与TLW_PEACE阶段一样 */
		TLW_END_SHOW(2);

		int value;

		TLWActivityState(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}
	}

	enum EloReason {
		/** 初始化积分 */
		INIT(1),
		/** 初始化积分-含战力 */
		INIT_WITH_POWER(2),
		/** 战斗后结算 */
		WAR_CALC(3),
		/** 消极出战扣除 */
		ABSENT_CALC(4),
		/** 赛季末结算 */
		SEASON_END_CALC(5),;
		int value;

		EloReason(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}
	}

}
