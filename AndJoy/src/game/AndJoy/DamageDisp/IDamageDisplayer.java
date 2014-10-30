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

	/**
	 * 获取该实体当前所在的场景对象
	 * 
	 * @return
	 */
	public YScene getScene();

	/**
	 * 用于该实体受伤时进行回调，请在此处new DamageDomain(this,value)
	 * 
	 * @param value
	 *            伤害值
	 */
	public void onHurt(int value);

}
