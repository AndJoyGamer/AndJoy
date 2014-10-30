package game.AndJoy.DamageDisp;

import ygame.framework.core.YRequest;

public class DamageReq extends YRequest {
//	private float[] pos;
	private int damageValue;
	public static final int DAMAGE_KEY = 1233198243;

	public DamageReq( int value) {
		super(DAMAGE_KEY);
//		this.pos = pos;
		this.damageValue = value;
	}

//	public void setPos(float x, float y) {
//		pos[0] = x;
//		pos[1] = y;
//	}
//
//	public float[] getPos() {
//		return pos;
//	}

	public int getDamageValue() {
		return damageValue;
	}

	public void setDamageValue(int damageValue) {
		this.damageValue = damageValue;
	}

}
