package game.AndJoy.DamageDisp;

import ygame.domain.YDomain;
import ygame.framework.core.YScene;

public interface IDamageDisplayer {

	/**
	 * 返回一个包含当前坐标的float数组，0标号为X坐标，1标号为Y坐标
	 * 
	 * @return
	 */
	public float[] getCurrentXY();

	public YScene getScene();

	public void onHurt(int value);

}
