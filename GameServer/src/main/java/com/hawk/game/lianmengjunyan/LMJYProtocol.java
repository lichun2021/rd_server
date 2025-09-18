package com.hawk.game.lianmengjunyan;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.xid.HawkXID;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessage.Builder;
import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;

public class LMJYProtocol extends HawkProtocol {
	private HawkProtocol source;
	private ILMJYPlayer player;

	private LMJYProtocol() {
	}

	public static LMJYProtocol valueOf(HawkProtocol source, ILMJYPlayer player) {
		LMJYProtocol result = new LMJYProtocol();
		result.source = source;
		result.player = player;
		return result;
	}

	public ILMJYPlayer getPlayer() {
		return player;
	}

	public void setPlayer(ILMJYPlayer player) {
		this.player = player;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return source.getType();
	}

	@Override
	public void setType(int type) {
		// TODO Auto-generated method stub
		source.setType(type);
	}

	@Override
	public boolean checkType(int type) {
		// TODO Auto-generated method stub
		return source.checkType(type);
	}

	@Override
	public boolean checkType(ProtocolMessageEnum type) {
		// TODO Auto-generated method stub
		return source.checkType(type);
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return source.getSize();
	}

	@Override
	public int getReserve() {
		// TODO Auto-generated method stub
		return source.getReserve();
	}

	@Override
	public HawkProtocol setReserve(int reserve) {
		// TODO Auto-generated method stub
		return source.setReserve(reserve);
	}

	@Override
	public int getCrc() {
		// TODO Auto-generated method stub
		return source.getCrc();
	}

	@Override
	public HawkProtocol setCrc(int crc) {
		// TODO Auto-generated method stub
		return source.setCrc(crc);
	}

	@Override
	public boolean checkCrc(int crc) {
		// TODO Auto-generated method stub
		return source.checkCrc(crc);
	}

	@Override
	public void bindSession(HawkSession session) {
		// TODO Auto-generated method stub
		source.bindSession(session);
	}

	@Override
	public HawkSession getSession() {
		// TODO Auto-generated method stub
		return source.getSession();
	}

	@Override
	public HawkXID getAppObjId() {
		// TODO Auto-generated method stub
		return source.getAppObjId();
	}

	@Override
	public byte[] getData() {
		// TODO Auto-generated method stub
		return source.getData();
	}

	@Override
	public Builder<?> getBuilder() {
		// TODO Auto-generated method stub
		return source.getBuilder();
	}

	@Override
	public void setBuilder(Builder<?> builder) {
		// TODO Auto-generated method stub
		source.setBuilder(builder);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return source.toString();
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		source.clear();
	}

	@Override
	public boolean writeData(byte[] bytes) {
		// TODO Auto-generated method stub
		return source.writeData(bytes);
	}

	@Override
	public <T extends GeneratedMessage> T parseProtocol(T template) {
		// TODO Auto-generated method stub
		return source.parseProtocol(template);
	}

	@Override
	public boolean response(HawkProtocol protocol) {
		// TODO Auto-generated method stub
		return source.response(protocol);
	}

}
