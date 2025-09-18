package com.hawk.game.player.hero.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.hawk.os.HawkException;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.config.HeroSkillCfg;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.LMJYRoomManager;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengstarwars.SWConst.SWState;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLConst.FGYLState;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZConst.XHJZState;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBSkillInfo;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

public abstract class ISSSHeroSkill extends IHeroSkill {
	Map<String, SkillCastInfo> cdMap = new HashMap<>();
	private int lastShowEffect;

	@Override
	public void casting(Object... args) {
		if (!isCooling()) {
			return;
		}

		Player player = getParent().getParent().getParent();
		HeroSkillCfg skillcfg = getCfg();

		SkillCastInfo info = getCastInfo();
		long curTime = GsApp.getInstance().getCurrentTime();
		info.setCastTime(curTime);
		info.setEffectTime(effectTime() + curTime);
		int eff12115 = player.getEffect().getEffVal(EffType.EFF_12115);
		if (getParent().getParent().getParent().getDYZZState() == DYZZState.GAMEING) {
			info.setCoolDown(info.getEffectTime() - eff12115 * 1000);
		} else if (player.isInDungeonMap() && getParent().getParent().getParent().getYQZZState() != YQZZState.GAMEING) {
			info.setCoolDown(info.getEffectTime() + skillcfg.getProficiencyCdRaid() * 1000 - eff12115 * 1000);
		} else {
			info.setCoolDown(info.getEffectTime() + skillcfg.getProficiencyCdWorld() * 1000 - eff12115 * 1000);
		}
	}

	public abstract int effectTime();

	@Override
	public void tick() {
		if (lastShowEffect == getShowProficiencyEffect()) {
			return;
		}
		lastShowEffect = getShowProficiencyEffect();

		SkillCastInfo info = getCastInfo();
		if (StringUtils.isEmpty(info.getMarchId())) {
			return;
		}
		IWorldMarch march;
		Player player = getParent().getParent().getParent();
		if (player.getCYBORGState() == CYBORGState.GAMEING) {
			ICYBORGPlayer sender = CYBORGRoomManager.getInstance().makesurePlayer(player.getId());
			march = sender.getParent().getMarch(info.getMarchId());
		} else if (player.getTBLYState() == TBLYState.GAMEING) {
			ITBLYPlayer sender = TBLYRoomManager.getInstance().makesurePlayer(player.getId());
			march = sender.getParent().getMarch(info.getMarchId());
		} else if (player.getSwState() == SWState.GAMEING) {
			ISWPlayer sender = SWRoomManager.getInstance().makesurePlayer(player.getId());
			march = sender.getParent().getMarch(info.getMarchId());
		} else if (player.getLmjyState() == PState.GAMEING) {
			ILMJYPlayer sender = LMJYRoomManager.getInstance().makesurePlayer(player.getId());
			march = sender.getParent().getMarch(info.getMarchId());
		} else if (player.getDYZZState() == DYZZState.GAMEING) {
			IDYZZPlayer sender = DYZZRoomManager.getInstance().makesurePlayer(player.getId());
			march = sender.getParent().getMarch(info.getMarchId());
		} else if (player.getYQZZState() == YQZZState.GAMEING) {
			IYQZZPlayer sender = YQZZRoomManager.getInstance().makesurePlayer(player.getId());
			march = sender.getParent().getMarch(info.getMarchId());
		} else if (player.getXhjzState() == XHJZState.GAMEING) {
			IXHJZPlayer sender = XHJZRoomManager.getInstance().makesurePlayer(player.getId());
			march = sender.getParent().getMarch(info.getMarchId());
		} else if (player.getFgylState() == FGYLState.GAMEING) {
			IFGYLPlayer sender = FGYLRoomManager.getInstance().makesurePlayer(player.getId());
			march = sender.getParent().getMarch(info.getMarchId());
		} else {
			march = WorldMarchService.getInstance().getMarch(info.getMarchId());
		}
		if (Objects.isNull(march)) {
			return;
		}
		march.updateMarch();
	}

	private SkillCastInfo getCastInfo() {
		if(getCfg().getProficiencyAuto()==1){
			return SkillCastInfo.ProficiencyAuto;
		}
		
		String mapkey = "default";
		try {
			mapkey = getParent().getParent().getParent().getDungeonMap();
			if (getParent().getParent().getParent().getYQZZState() == YQZZState.GAMEING) {
				mapkey = ""; // 月战副本用大世界的
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		if (!cdMap.containsKey(mapkey)) {
			cdMap.put(mapkey, new SkillCastInfo());
		}

		return cdMap.get(mapkey);

	}

	@Override
	public int skillID() {
		PlayerHero hero = getParent().getParent();
		return hero.getConfig().getPassiveSkillList().get(hero.getStar() - 1);
	}

	@Override
	public boolean isCooling() {
		return getCastInfo().getCoolDown() < GsApp.getInstance().getCurrentTime();
	}

	public boolean isEffecting() {
		return getCastInfo().getEffectTime() > GsApp.getInstance().getCurrentTime();
	}

	@Override
	public void goMarch(IWorldMarch march) {
		if (march != null) {
			SkillCastInfo info = getCastInfo();
			info.setMarchId(march.getMarchId());
		}
	}

	@Override
	public void afterBattle(IBattleIncome income, BattleOutcome battleOutcome) {
		// TODO Auto-generated method stub

	}

	@Override
	public void backFromMarch(IWorldMarch march) {
		SkillCastInfo info = getCastInfo();
		info.setMarchId("");
	}

	@Override
	public boolean isProficiencySkill() {
		return true;
	}

	@Override
	public String serializ() {
		// 考虑到未来维护可能需要兼容. 这里扔使用array
		JSONObject obj = new JSONObject();
		obj.put("exp", getExp());
		JSONObject cinfo = new JSONObject();
		for (Entry<String, SkillCastInfo> ent : cdMap.entrySet()) {
			cinfo.put(ent.getKey(), ent.getValue().serializ());
		}
		obj.put("cinfo", cinfo);

		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		try {
			JSONObject obj = JSONObject.parseObject(serialiedStr);
			setExp(obj.getIntValue("exp"));
			JSONObject cinfo = obj.getJSONObject("cinfo");
			cdMap.clear();
			for (Entry<String, Object> ent : cinfo.entrySet()) {
				SkillCastInfo info = new SkillCastInfo();
				info.mergeFrom(ent.getValue().toString());
				cdMap.put(ent.getKey(), info);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public PBSkillInfo toPBobj() {
		SkillCastInfo info = getCastInfo();
		PBSkillInfo.Builder bul = PBSkillInfo.newBuilder();
		bul.setHeroId(getParent().getParent().getCfgId())
				.setSkillId(skillID())
				.setTotalExp(getExp())
				.setLevel(getLevel())
				.addAllEffect(effectVal())
				.setCastTime(info.getCastTime())
				.setCoolDown(info.getCoolDown())
				.setEffectTime(info.getEffectTime())
				.setCastAble(true)
				.setSssSkill(true);
		return bul.build();
	}
}
