package com.hawk.game.module.lianmengtaiboliya.order;

import com.hawk.game.module.lianmengtaiboliya.worldpoint.ITBLYBuilding;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYBuildState;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYChronoSphere;

public class TBLYOrderChrono {
	public final long MissileCoolDownTime = TBLYChronoSphere.getCfg().getMissileCoolDownTime() * 1000;

	private long controlTime = 3600000;
	private long effectEnd;
	private long startTime;
	private ITBLYBuilding target;
	private TBLYChronoSphere parent;

	public TBLYChronoSphere getParent() {
		return parent;
	}

	public void onTick() {
		if (target != null) {
			if (effectEnd < getParent().getParent().getCurTimeMil() || target.getState() != TBLYBuildState.ZHAN_LING) {
				target.getShowOrder().remove(TBLYOrderCollection.shuangbeijifen);
				target = null;
				startTime = 0;
				effectEnd = 0;
			}
		}
	}
	
	public long getReadyTime() {
		long cool = MissileCoolDownTime - controlTime;
		return getParent().getParent().getCurTimeMil() + cool; // 核弹发射OK时间
	}

	public long getContDown() {
		return MissileCoolDownTime - getControlTime();
	}

	public void setParent(TBLYChronoSphere parent) {
		this.parent = parent;
	}

	public long getControlTime() {
		return controlTime;
	}

	public void setControlTime(long controlTime) {
		this.controlTime = controlTime;
	}

	public long getEffectEnd() {
		return effectEnd;
	}

	public void setEffectEnd(long effectEnd) {
		this.effectEnd = effectEnd;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public ITBLYBuilding getTarget() {
		return target;
	}

	public void setTarget(ITBLYBuilding target) {
		this.target = target;
	}

	public void addControlTime(long timePass) {
		controlTime += timePass;
	}

}
