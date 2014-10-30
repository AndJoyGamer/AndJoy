package game.AndJoy.DamageDisp;

import game.AndJoy.common.AndjoyApp;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.program.YTextureProgram;

public class DamageDomain extends YDomain {
	/**
	 * 伤害实体的key为随机生成
	 * 
	 * @param damageDisplayer 实现伤害显示接口的实体
	 * @param value 伤害数值
	 */
	public DamageDomain(IDamageDisplayer damageDisplayer, int value) {
		super("damage_" + (int) (Math.random() * 10000), new DamageLogic(
				damageDisplayer, value), new YDomainView(
				YTextureProgram.getInstance(AndjoyApp.getResource())));
		damageDisplayer.getScene().addDomains(this);
	}

}
