package game.AndJoy.DamageDisp;

import ygame.framework.core.YRequest;

public class DamageReq extends YRequest {
	public float[] pos;

	public void setPos(float x, float y) {
		pos[0] = x;
		pos[1] = y;
	}

	public DamageReq(int iKEY) {
		super(iKEY);
		// TODO Auto-generated constructor stub
	}

}
