package game.AndJoy.DamageDisp;

import game.AndJoy.MainActivity;
import game.AndJoy.common.AndjoyApp;
import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.program.YTextureProgram;
import ygame.extension.program.YTileProgram;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.texture.YTileSheet;

public class DamageDomain extends YDomain {
	/**
	 * 伤害实体的key为随机生成
	 * 
	 * @param KEY
	 * @param activity
	 */
	public DamageDomain(IDamageDisplayer damageDisplayer) {
		super("damage_" + (int) (Math.random() * 10000), new DamageLogic(
				damageDisplayer), new YDomainView(
				YTextureProgram.getInstance(AndjoyApp.getResource())));
	}

	// public static DamageDomain getDomain(){
	// new DamageDomain("damage", logic, view)
	// }

}
