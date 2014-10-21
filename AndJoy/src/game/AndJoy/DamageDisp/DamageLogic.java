package game.AndJoy.DamageDisp;

import android.app.Activity;
import game.AndJoy.R;
import game.AndJoy.common.Constants;
import game.AndJoy.sprite.concrete.YSpriteDomain;
import ygame.domain.YABaseShaderProgram;
import ygame.domain.YABaseShaderProgram.YABaseParametersAdapter;
import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.primitives.YSquare;
import ygame.extension.program.YTileProgram;
import ygame.extension.program.YTileProgram.YAdapter;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.core.YView;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTileSheet;
import ygame.transformable.YMover;

public class DamageLogic extends YADomainLogic {

	// Activity Yactivity;
	// YDomainView domainView = new
	// YDomainView(YTileProgram.getInstance(Yactivity
	// .getResources()));
	YSkeleton skeleton = new YSquare(20, false, true);

	YTileSheet tileSheet;

	private YMover mover = new YMover();

	// YTileSheet tileSheet = new YTileSheet(R.drawable.damage,
	// Yactivity.getResources(), 6, 12);

	public DamageLogic(YTileSheet tileSheet) {
		this.tileSheet = tileSheet;
	}

	@Override
	protected void onCycle(double dbElapseTime_s, YDomain domainContext,
			YWriteBundle bundle, YSystem system, YScene sceneCurrent,
			YMatrix matrix4pv, YMatrix matrix4Projection, YMatrix matrix4View) {
		// TODO Auto-generated method stub
		YTileProgram.YAdapter adapter = (YAdapter) domainContext
				.getParametersAdapter();
		adapter.paramMatrixPV(matrix4pv).paramMover(mover)
				.paramSkeleton(skeleton).paramFrameSheet(tileSheet)
				.paramFramePosition(1, 1);

		mover.setX((44 - 128) * 5).setY(0);

	}

	@Override
	protected boolean onDealRequest(YRequest request, YSystem system,
			YScene sceneCurrent, YBaseDomain domainContext) {
		// TODO Auto-generated method stub
		DamageReq dr = (DamageReq) request;
		mover.setX(dr.pos[0]).setY(dr.pos[1]);
		return true;
	}

}
