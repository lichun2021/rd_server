package com.hawk.game.guild.manor;

public enum GuildDragonTrapState {
	
	
	PEACE(0) {
		@Override
		public GuildDragonTrapState getNext() {
			return FIGHT;
		}
	},
	FIGHT(1) {
		@Override
		public GuildDragonTrapState getNext() {
			return WAIT;
		}
	},
	WAIT(2) {
		@Override
		public GuildDragonTrapState getNext() {
			return PEACE;
		}
	};
	
	
	
 
		
	private int state;
	private GuildDragonTrapState(int state) {
		this.state = state;
	}
	public int getVal(){
		return this.state;
	}
	
	public abstract GuildDragonTrapState getNext();
	
	public static GuildDragonTrapState valueOf(int val){
		switch (val) {
		case 0:return PEACE;
		case 1:return FIGHT;
		case 2:return WAIT;
		default: return PEACE;
		}
	}
}
