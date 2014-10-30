package game.AndJoy.DamageDisp;

import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.extension.primitives.YSquare;
import ygame.extension.program.YTextureProgram;
import ygame.extension.program.YTextureProgram.YAdapter;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;
import ygame.transformable.YMover;

public class DamageLogic extends YADomainLogic {

	YSkeleton skeleton = new YSquare(0.5f, false, true);

	private YMover mover = new YMover();
	private YTexture texture;
	private IDamageDisplayer damageDisplayer;
	 private int hurtMaxNum = 20;
	private int hurtCount = 0;
	private float Yoffset = 0;
	/** 伤害数值位移的距离相关 **/
	private float timeStep = 0.1f;


	public DamageLogic(IDamageDisplayer damageDisplayer, int hurtNum) {
		this.damageDisplayer = damageDisplayer;
		this.texture = new YTexture(NumBitmap.getNumBtmByValues(hurtNum));
	}

	@Override
	protected void onCycle(double dbElapseTime_s, YDomain domainContext,
			YWriteBundle bundle, YSystem system, YScene sceneCurrent,
			YMatrix matrix4pv, YMatrix matrix4Projection, YMatrix matrix4View) {
		YTextureProgram.YAdapter adapter = (YAdapter) domainContext
				.getParametersAdapter();
		adapter.paramMatrixPV(matrix4pv).paramMover(mover)
				.paramSkeleton(skeleton).paramTexture(texture);
		if (hurtCount < hurtMaxNum) {
			Yoffset += dbElapseTime_s * 0.1f / timeStep;
			mover.setX((float) (damageDisplayer.getCurrentXY()[0]))
					.setY((float) (damageDisplayer.getCurrentXY()[1] + Yoffset))
					.setZ(2f);
			hurtCount++;
		} else if (hurtCount >= hurtMaxNum) {
			sceneCurrent.removeDomains(domainContext);
		}

	}

	@Override
	protected boolean onDealRequest(YRequest request, YSystem system,
			YScene sceneCurrent, YBaseDomain domainContext) {
		return false;
	}

}
