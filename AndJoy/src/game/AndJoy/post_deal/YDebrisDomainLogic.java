package game.AndJoy.post_deal;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.extension.program.YTextureProgram;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YWriteBundle;
import ygame.math.MathUtils;
import ygame.math.YMatrix;
import ygame.math.vector.Vector3;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;
import ygame.transformable.YMover;

class YDebrisDomainLogic extends YADomainLogic {
	final private YMover mover = (YMover) new YMover().setShaft(new Vector3(0,
			0, 1));
	final private YSkeleton skeleton;
	final private YTexture texture;
	private Body body;

	YDebrisDomainLogic(YSkeleton skeleton, YTexture texture, Body body) {
		this.skeleton = skeleton;
		this.texture = texture;
		this.body = body;
	}

	@Override
	protected void onCycle(double dbElapseTime_s, YDomain domainContext,
			YWriteBundle bundle, YSystem system, YScene sceneCurrent,
			YMatrix matrix4pv, YMatrix matrix4Projection, YMatrix matrix4View) {
		Vec2 position = body.getWorldCenter();
		mover.setAngle(body.getAngle() * MathUtils.RAD2DEG).setX(position.x)
				.setY(position.y);

		sceneCurrent.getCurrentCamera()
		// .setX(position.x)
		// .setY(position.y)
				.setZ(100);
		YTextureProgram.YAdapter adapter = (YTextureProgram.YAdapter) domainContext
				.getParametersAdapter();
		adapter.paramMatrixPV(matrix4pv).paramMover(mover)
				.paramSkeleton(skeleton).paramTexture(texture);
	}

	@Override
	protected boolean onDealRequest(YRequest request, YSystem system,
			YScene sceneCurrent) {
		return false;
	}

}
