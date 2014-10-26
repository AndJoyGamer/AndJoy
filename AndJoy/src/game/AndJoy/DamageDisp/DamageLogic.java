package game.AndJoy.DamageDisp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;
import game.AndJoy.R;
import game.AndJoy.common.Constants;
import game.AndJoy.sprite.concrete.YSpriteDomain;
import ygame.domain.YABaseShaderProgram;
import ygame.domain.YABaseShaderProgram.YABaseParametersAdapter;
import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.primitives.YSquare;
import ygame.extension.program.YTextureProgram;
import ygame.extension.program.YTextureProgram.YAdapter;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.core.YView;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;
import ygame.texture.YTileSheet;
import ygame.transformable.YMover;

public class DamageLogic extends YADomainLogic {

	YSkeleton skeleton = new YSquare(0.5f, false, true);

	private YMover mover = new YMover();
	private boolean isHurt = false;
	private int hurtNum = 0;
	private int hurtCount = 0;
	private YTexture texture = new YTexture(Bitmap.createBitmap(1, 1,
			Config.ARGB_8888));

	private IDamageDisplayer damageDisplayer;

	public DamageLogic(IDamageDisplayer damageDisplayer) {
		this.damageDisplayer = damageDisplayer;
	}

	// YTileSheet tileSheet = new YTileSheet(R.drawable.damage,
	// Yactivity.getResources(), 6, 12);

	@Override
	protected void onCycle(double dbElapseTime_s, YDomain domainContext,
			YWriteBundle bundle, YSystem system, YScene sceneCurrent,
			YMatrix matrix4pv, YMatrix matrix4Projection, YMatrix matrix4View) {
		YTextureProgram.YAdapter adapter = (YAdapter) domainContext
				.getParametersAdapter();
		adapter.paramMatrixPV(matrix4pv).paramMover(mover)
				.paramSkeleton(skeleton).paramTexture(texture);
		Log.d("DamageLogic", "oncycle");
		float timeStep = 0.005f;
		if (isHurt && hurtCount < 20) {
			mover.setX((float) (damageDisplayer.getCurrentXY()[0]))
					.setY((float) (damageDisplayer.getCurrentXY()[1] + timeStep
							* hurtCount * 0.2f / dbElapseTime_s)).setZ(2f);
			hurtCount++;
			// mover.setX((44 - 128) * 5).setY(0);
		} else if (hurtCount >= 20) {
			isHurt = false;
			hurtCount = 0;
			// 无伤害状态下创建一个透明的小图片；不过一般情况下此句应该是多余的，因为domain会被remove掉，是否保留为待定
			texture = new YTexture(Bitmap.createBitmap(1, 1, Config.ARGB_8888));
			system.getCurrentScene().removeDomains(domainContext);
		}
		// else {
		// YTileProgram.YAdapter adapter = (YAdapter) domainContext
		// .getParametersAdapter();
		// adapter.paramMatrixPV(matrix4pv)
		// .paramMover(mover)
		// .paramSkeleton(skeleton)
		// .paramFrameSheet(
		// new YTileSheet(
		// NumBitmap.getDmgBtmByValues(hurtNum), 1, 1))
		// .paramFramePosition(0, 0);
		// }

	}

	@Override
	protected boolean onDealRequest(YRequest request, YSystem system,
			YScene sceneCurrent, YBaseDomain domainContext) {
		DamageReq dr = (DamageReq) request;
		// mover.setX(dr.getPos()[0]).setY(dr.getPos()[1]);
		hurtNum = dr.getDamageValue();
		texture = new YTexture(NumBitmap.getDmgBtmByValues(hurtNum));
		isHurt = true;
		return true;
	}

}
