package game.AndJoy.obstacle;

import game.AndJoy.MainActivity;
import game.AndJoy.R;
import game.AndJoy.common.Constants;
import game.AndJoy.sprite.concrete.YSpriteDomain;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import ygame.common.YConstants.Orientation;
import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.extension.primitives.YSquare;
import ygame.extension.program.YTileProgram;
import ygame.extension.program.YTileProgram.YAdapter;
import ygame.extension.with_third_party.YIOnContactListener;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTileSheet;
import ygame.transformable.YMover;

public class ObstacleLogic extends YADomainLogic {

	private YMover mover = (YMover) new YMover().setX(68).setY(-3).setZ(0.2f);
	private YSkeleton skeleton = new YSquare(1.5f, false, true);
	private MainActivity activity;
	private YTileSheet tileSheet;
	private World world;
	private Body body;
	
	
	private float fFrames = 0;

	protected ObstacleLogic(MainActivity activity, World world) {
		this.activity = activity;
		this.world = world;
	}

	@Override
	protected void onAttach(YSystem system, YBaseDomain domainContext) {
		// TODO Auto-generated method stub
		super.onAttach(system, domainContext);
		tileSheet = new YTileSheet(R.drawable.fire, activity.getResources(), 4, 4);
		
		//设计刚体
		BodyDef bodyDef = new BodyDef();
		FixtureDef def = new FixtureDef();
		bodyDef.type = BodyType.STATIC;
		bodyDef.position.set(mover.getX(), mover.getY());
		this.body = world.createBody(bodyDef);
		body.setDomain(domainContext);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.5f, 2.5f,
				new Vec2(0, 0), 0);
		def.shape = shape;
		body.createFixture(def).setOnContactListener(new fireLsn());
	}

	@Override
	protected void onCycle(double dbElapseTime_s, YDomain domainContext,
			YWriteBundle bundle, YSystem system, YScene sceneCurrent,
			YMatrix matrix4pv, YMatrix matrix4Projection, YMatrix matrix4View) {
		// TODO Auto-generated method stub
		YTileProgram.YAdapter parametersAdapter = (YAdapter) domainContext
				.getParametersAdapter();

		int iColumnIndex  = (int) ((fFrames += dbElapseTime_s * 10) % 4);
		parametersAdapter.paramMatrixPV(matrix4pv).paramMover(mover)
				.paramSkeleton(skeleton).paramFrameSheet(tileSheet)
				.paramFramePosition(1, iColumnIndex);
	}

	@Override
	protected boolean onDealRequest(YRequest request, YSystem system,
			YScene sceneCurrent, YBaseDomain domainContext) {
		// TODO Auto-generated method stub
		return false;
	}

	private class fireLsn implements YIOnContactListener{
		//精灵是否在火焰的右边
		protected boolean bRight = false;
		@Override
		public void beginContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			// TODO Auto-generated method stub

			System.out.println(domainOther.KEY);
			// 如果与精灵发生碰撞，精灵受伤
			if(domainOther.KEY.equals(Constants.SPRITE)){
				if (fixtureOther.getBody()
						.getPosition().x < fixture
						.getBody()
						.getPosition().x)
					bRight = false;
				else
					bRight = true;
				YSpriteDomain sprite  = (YSpriteDomain) domainOther;
				sprite.damage(bRight ? Orientation.RIGHT : Orientation.LEFT);
			}
			
		}

		@Override
		public void endContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
