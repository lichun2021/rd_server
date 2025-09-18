package com.hawk.game.config;

import java.util.ArrayList;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/stronger_method_build.xml")
public class StrongerMethodBuildCfg extends HawkConfigBase {
	//
	protected final int id;
	@Id
	protected final int buildType;
	protected final double percent;
	protected final double lvl1;
	protected final double lvl2;
	protected final double lvl3;
	protected final double lvl4;
	protected final double lvl5;
	protected final double lvl6;
	protected final double lvl7;
	protected final double lvl8;
	protected final double lvl9;
	protected final double lvl10;
	protected final double lvl11;
	protected final double lvl12;
	protected final double lvl13;
	protected final double lvl14;
	protected final double lvl15;
	protected final double lvl16;
	protected final double lvl17;
	protected final double lvl18;
	protected final double lvl19;
	protected final double lvl20;
	protected final double lvl21;
	protected final double lvl22;
	protected final double lvl23;
	protected final double lvl24;
	protected final double lvl25;
	protected final double lvl26;
	protected final double lvl27;
	protected final double lvl28;
	protected final double lvl29;
	protected final double lvl30;
	protected final double lvl31;
	protected final double lvl32;
	protected final double lvl33;
	protected final double lvl34;
	protected final double lvl35;
	protected final double lvl36;
	protected final double lvl37;
	protected final double lvl38;
	protected final double lvl39;
	protected final double lvl40;
	protected final double lvl41;
	protected final double lvl42;
	protected final double lvl43;
	protected final double lvl44;
	protected final double lvl45;
	protected final double lvl46;
	protected final double lvl47;
	protected final double lvl48;
	protected final double lvl49;
	protected final double lvl50;
	
	protected ArrayList<Double> levels = new ArrayList<Double>(30);

	public StrongerMethodBuildCfg() {
		id = 1;
		buildType = 2010;
		percent = 40;
		lvl1 = 0;
		lvl2 = 0;
		lvl3 = 0;
		lvl4 = 0;
		lvl5 = 0;
		lvl6 = 0;
		lvl7 = 0;
		lvl8 = 0;
		lvl9 = 0;
		lvl10 = 0;
		lvl11 = 0;
		lvl12 = 0;
		lvl13 = 0;
		lvl14 = 0;
		lvl15 = 0;
		lvl16 = 0;
		lvl17 = 0;
		lvl18 = 0;
		lvl19 = 0;
		lvl20 = 0;
		lvl21 = 0;
		lvl22 = 0;
		lvl23 = 0;
		lvl24 = 0;
		lvl25 = 0;
		lvl26 = 0;
		lvl27 = 0;
		lvl28 = 0;
		lvl29 = 0;
		lvl30 = 0;
		lvl31 = 0;
		lvl32 = 0;
		lvl33 = 0;
		lvl34 = 0;
		lvl35 = 0;
		lvl36 = 0;
		lvl37 = 0;
		lvl38 = 0;
		lvl39 = 0;
		lvl40 = 0;
		lvl41 = 0;
		lvl42 = 0;
		lvl43 = 0;
		lvl44 = 0;
		lvl45 = 0;
		lvl46 = 0;
		lvl47 = 0;
		lvl48 = 0;
		lvl49 = 0;
		lvl50 = 0;
	}
	
	public double getScore( int level ){
		if(level > 0 && level <= 38){
			return levels.get(level -1);
		}
		return 0;
	}
	
	public int getId() {
		return id;
	}

	public int getBuildType() {
		return buildType;
	}

	public double getPercent() {
		return percent;
	}

	public double getLvl1() {
		return lvl1;
	}

	public double getLvl2() {
		return lvl2;
	}

	public double getLvl3() {
		return lvl3;
	}

	public double getLvl4() {
		return lvl4;
	}

	public double getLvl5() {
		return lvl5;
	}

	public double getLvl6() {
		return lvl6;
	}

	public double getLvl7() {
		return lvl7;
	}

	public double getLvl8() {
		return lvl8;
	}

	public double getLvl9() {
		return lvl9;
	}

	public double getLvl10() {
		return lvl10;
	}

	public double getLvl11() {
		return lvl11;
	}

	public double getLvl12() {
		return lvl12;
	}

	public double getLvl13() {
		return lvl13;
	}

	public double getLvl14() {
		return lvl14;
	}

	public double getLvl15() {
		return lvl15;
	}

	public double getLvl16() {
		return lvl16;
	}

	public double getLvl17() {
		return lvl17;
	}

	public double getLvl18() {
		return lvl18;
	}

	public double getLvl19() {
		return lvl19;
	}

	public double getLvl20() {
		return lvl20;
	}

	public double getLvl21() {
		return lvl21;
	}

	public double getLvl22() {
		return lvl22;
	}

	public double getLvl23() {
		return lvl23;
	}

	public double getLvl24() {
		return lvl24;
	}

	public double getLvl25() {
		return lvl25;
	}

	public double getLvl26() {
		return lvl26;
	}

	public double getLvl27() {
		return lvl27;
	}

	public double getLvl28() {
		return lvl28;
	}

	public double getLvl29() {
		return lvl29;
	}

	public double getLvl30() {
		return lvl30;
	}

	public double getLvl31() {
		return lvl31;
	}

	public double getLvl32() {
		return lvl32;
	}

	public double getLvl33() {
		return lvl33;
	}

	public double getLvl34() {
		return lvl34;
	}

	public double getLvl35() {
		return lvl35;
	}

	public double getLvl36() {
		return lvl36;
	}

	public double getLvl37() {
		return lvl37;
	}

	public double getLvl38() {
		return lvl38;
	}
	
	public double getLvl39() {
		return lvl39;
	}
	
	public double getLvl40() {
		return lvl40;
	}
	
	/**
	 * @return the lvl41
	 */
	public double getLvl41() {
		return lvl41;
	}

	/**
	 * @return the lvl42
	 */
	public double getLvl42() {
		return lvl42;
	}

	/**
	 * @return the lvl43
	 */
	public double getLvl43() {
		return lvl43;
	}

	/**
	 * @return the lvl44
	 */
	public double getLvl44() {
		return lvl44;
	}

	/**
	 * @return the lvl45
	 */
	public double getLvl45() {
		return lvl45;
	}

	
	public double getLvl46() {
		return lvl46;
	}
	
	
	public double getLvl47() {
		return lvl47;
	}
	
	
	public double getLvl48() {
		return lvl48;
	}
	
	public double getLvl49() {
		return lvl49;
	}
	
	public double getLvl50() {
		return lvl50;
	}
	@Override
	protected boolean assemble() {
		levels.add(lvl1);
		levels.add(lvl2);
		levels.add(lvl3);
		levels.add(lvl4);
		levels.add(lvl5);
		levels.add(lvl6);
		levels.add(lvl7);
		levels.add(lvl8);
		levels.add(lvl9);
		levels.add(lvl10);
		levels.add(lvl11);
		levels.add(lvl12);
		levels.add(lvl13);
		levels.add(lvl14);
		levels.add(lvl15);
		levels.add(lvl16);
		levels.add(lvl17);
		levels.add(lvl18);
		levels.add(lvl19);
		levels.add(lvl20);
		levels.add(lvl21);
		levels.add(lvl22);
		levels.add(lvl23);
		levels.add(lvl24);
		levels.add(lvl25);
		levels.add(lvl26);
		levels.add(lvl27);
		levels.add(lvl28);
		levels.add(lvl29);
		levels.add(lvl30);
		levels.add(lvl31);
		levels.add(lvl32);
		levels.add(lvl33);
		levels.add(lvl34);
		levels.add(lvl35);
		levels.add(lvl36);
		levels.add(lvl37);
		levels.add(lvl38);
		levels.add(lvl39);
		levels.add(lvl40);
		levels.add(lvl41);
		levels.add(lvl42);
		levels.add(lvl43);
		levels.add(lvl44);
		levels.add(lvl45);
		levels.add(lvl46);
		levels.add(lvl47);
		levels.add(lvl48);
		levels.add(lvl49);
		levels.add(lvl50);
		return super.assemble();
	}
}
