package game.AndJoy.post_deal1;

import game.AndJoy.R;
import game.AndJoy.post_deal1.YXuanZhuanProgram.YXuanZhuanAdapter;
import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;
import ygame.transformable.YMover;


public class YGridLogic extends YADomainLogic
{

	private YMover mover = new YMover();
	private YSkeleton skeleton;
	private YTexture texture;

	@Override
	protected void onAttach(YSystem system, YBaseDomain domainContext)
	{
		super.onAttach(system, domainContext);
		skeleton = new TextureRect();
		texture = new YTexture(R.drawable.shan, system.getContext()
				.getResources());
	}

	@Override
	protected void onCycle(double dbElapseTime_s, YDomain domainContext,
			YWriteBundle bundle, YSystem system,
			YScene sceneCurrent, YMatrix matrix4pv,
			YMatrix matrix4Projection, YMatrix matrix4View)
	{
		YXuanZhuanAdapter adapter = (YXuanZhuanAdapter) domainContext
				.getParametersAdapter();
		adapter.paramMatrixPV(matrix4pv).paramMover(mover)
				.paramSkeleton(skeleton).paramTexture(texture);
	}

	@Override
	protected boolean onDealRequest(YRequest request, YSystem system,
			YScene sceneCurrent)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
