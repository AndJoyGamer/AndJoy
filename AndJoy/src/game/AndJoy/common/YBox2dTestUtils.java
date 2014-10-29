package game.AndJoy.common;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.primitives.YRectangle;
import ygame.extension.primitives.YSquare;
import ygame.extension.program.YSimpleProgram;
import ygame.extension.program.YSimpleProgram.YAdapter;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.math.vector.Vector3;
import ygame.skeleton.YSkeleton;
import ygame.transformable.YMover;
import android.content.res.Resources;

public final class YBox2dTestUtils
{
	private YBox2dTestUtils()
	{
	}

	public static void addTestBridge(Vec2 vec2Start, Vec2 vec2End,
			float fTileLen, World world, YScene scene,
			Resources resources)
	{
		final int N = (int) (vec2End.sub(vec2Start).x / fTileLen);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(fTileLen / 2, 0.25f / 2);

		FixtureDef fd = new FixtureDef();
		fd.shape = shape;
		fd.density = 10.0f;
		fd.friction = 1f;
		fd.restitution = 0;

		RevoluteJointDef jd = new RevoluteJointDef();
		YSkeleton skeleton = new YRectangle(fTileLen, 0.25f, true,
				false);
		Body prevBody = newStaticBody(vec2Start, world);
		for (int i = 0; i < N; ++i)
		{
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			bd.position.set(vec2Start.x + 0.05f + fTileLen / 2
					+ fTileLen * i, vec2Start.y);
			Body body = world.createBody(bd);
			String domainKey = "map_test_bridge" + i;
			// body.setDomainKey(domainKey);
			body.createFixture(fd);

			scene.addDomains(newBridgeComponent(domainKey, body,
					skeleton, resources));
			Vec2 anchor = new Vec2(vec2Start.x + 0.05f + fTileLen
					* i, vec2Start.y);
			jd.initialize(prevBody, body, anchor);
			world.createJoint(jd);

			prevBody = body;
		}

		Vec2 anchor = new Vec2(vec2End.x - 0.05f, vec2End.y);
		jd.initialize(prevBody, newStaticBody(vec2End, world), anchor);
		world.createJoint(jd);
	}

	private static YDomain newBridgeComponent(String KEY, final Body body,
			final YSkeleton skeleton, Resources resources)
	{
		YADomainLogic logic = new YADomainLogic()
		{

			private YMover mover = (YMover) new YMover().setZ(-1)
					.setShaft(new Vector3(0, 0, 1));

			@Override
			protected boolean onDealRequest(YRequest request,
					YSystem system, YScene sceneCurrent,
					YBaseDomain domainContext)
			{
				return false;
			}

			@Override
			protected void onCycle(double dbElapseTime_s,
					YDomain domainContext,
					YWriteBundle bundle, YSystem system,
					YScene sceneCurrent, YMatrix matrix4pv,
					YMatrix matrix4Projection,
					YMatrix matrix4View)
			{
				Vec2 position = body.getPosition();
				mover.setX(position.x)
						.setY(position.y)
						.setAngle(body.getAngle() * 180 / 3.1415f);
				YSimpleProgram.YAdapter adapter = (YSimpleProgram.YAdapter) domainContext
						.getParametersAdapter();
				adapter.paramMatrixPV(matrix4pv)
						.paramMover(mover)
						.paramSkeleton(skeleton);
			}
		};

		YDomainView view = new YDomainView(
				YSimpleProgram.getInstance(resources));
		// view.iDrawMode = GLES20.GL_LINE_LOOP;

		return new YDomain(KEY, logic, view);
	}

	private static Body newStaticBody(Vec2 vecPosition, World world)
	{
		BodyDef bd = new BodyDef();
		bd.type = BodyType.STATIC;
		bd.position.set(vecPosition);
		Body body = world.createBody(bd);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.05f, 0.05f);
		body.createFixture(shape, 0);
		return body;
	}

	public static void addTestBoxes(World world, YScene scene,
			Resources resources)
	{
		final float fTileSize = 5;
		YSkeleton skeleton = new YSquare(fTileSize, true, false);
		YSimpleProgram program = YSimpleProgram.getInstance(resources);
		for (int i = 0; i < 3; i++)
			// scene.addDomains(newTestBoxDomain(world, skeleton,
			// program, "test_box" + i, -100,
			// 10 * i + 10, fTileSize));
			scene.addDomains(newTestBoxDomain(world, skeleton,
					program, "test_box" + i,
					(176 - 128) * 5, 10 * i + 40, fTileSize));
	}

	public static void addOneTestBox(World world, YScene scene,
			Resources resources, Vec2 position)
	{
		YSkeleton skeleton = new YSquare(1, true, false);
		YSimpleProgram program = YSimpleProgram.getInstance(resources);
		scene.addDomains(newTestBoxDomain(world, skeleton, program,
				"one_test_box", position.x, position.y, 1));
	}

	private static YDomain newTestBoxDomain(World world,
			final YSkeleton skeleton, YSimpleProgram program,
			String KEY, float fInitX_M, float fInitY_M,
			float fTileSize)
	{
		final Body body = newTestBoxBody(world, fInitX_M, fInitY_M,
				fTileSize);

		YADomainLogic logic = new YADomainLogic()
		{

			private YMover mover = (YMover) new YMover().setZ(2)
					.setShaft(new Vector3(0, 0, 1));

			@Override
			protected boolean onDealRequest(YRequest request,
					YSystem system, YScene sceneCurrent,
					YBaseDomain domainContext)
			{
				return false;
			}

			@Override
			protected void onCycle(double dbElapseTime_s,
					YDomain domainContext,
					YWriteBundle bundle, YSystem system,
					YScene sceneCurrent, YMatrix matrix4pv,
					YMatrix matrix4Projection,
					YMatrix matrix4View)
			{
				Vec2 position = body.getPosition();
				mover.setX(position.x)
						.setY(position.y)
						.setAngle(body.getAngle() * 180 / 3.1415f);
				YSimpleProgram.YAdapter adapter = (YSimpleProgram.YAdapter) domainContext
						.getParametersAdapter();
				adapter.paramMatrixPV(matrix4pv)
						.paramMover(mover)
						.paramSkeleton(skeleton);
			}
		};

		YDomainView view = new YDomainView(program);
		// view.iDrawMode = GLES20.GL_LINE_LOOP;

		body.setUserData(new YDomain(KEY, logic, view));
		return (YDomain) body.getUserData();
	}

	private static Body newTestBoxBody(World world, float fInitX_M,
			float fInitY_M, float fBODY_SIZE_M)
	{
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;
		bd.position.set(fInitX_M, fInitY_M);
		final Body body = world.createBody(bd);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(fBODY_SIZE_M / 2.0f, fBODY_SIZE_M / 2.0f);
		FixtureDef fd = new FixtureDef();
		fd.shape = shape;
		fd.density = 1f;
		fd.friction = 0.2f;
		fd.restitution = 0f;
		body.createFixture(fd);

		return body;
	}

	public static void addTestSeeSaw(final Vec2 vec2Pos, final World world,
			YScene scene, Resources resources)
	{
		YADomainLogic logic = new YADomainLogic()
		{
			final float radio = (float) (180 / Math.PI);
			private Body seesawBody = newSeeSawBaseBody(world,
					vec2Pos.x, vec2Pos.y);
			private YMover mover = (YMover) new YMover()
					.setX(vec2Pos.x).setY(vec2Pos.y).setZ(0.03f)
					.setShaft(new Vector3(0, 0, 1));
			private YSkeleton skeleton = new YRectangle(5, 0.25f,
					true, false);

			@Override
			protected boolean onDealRequest(YRequest request,
					YSystem system, YScene sceneCurrent,
					YBaseDomain domainContext)
			{
				return false;
			}

			@Override
			protected void onCycle(double dbElapseTime_s,
					YDomain domainContext,
					YWriteBundle bundle, YSystem system,
					YScene sceneCurrent, YMatrix matrix4pv,
					YMatrix matrix4Projection,
					YMatrix matrix4View)
			{
				Vec2 pos = seesawBody.getPosition();
				float angle = seesawBody.getAngle();
				mover.setX(pos.x).setY(pos.y)
						.setAngle(angle * radio);
				YSimpleProgram.YAdapter adapter = (YAdapter) domainContext
						.getParametersAdapter();
				adapter.paramMatrixPV(matrix4pv)
						.paramMover(mover)
						.paramSkeleton(skeleton);
			}
		};
		YDomainView view = new YDomainView(
				YSimpleProgram.getInstance(resources));
		YDomain seesaw = new YDomain("testSeeSaw", logic, view);

		scene.addDomains(seesaw);
	}

	private static Body newSeeSawBaseBody(World world, float initX,
			float initY)
	{
		BodyDef bd = new BodyDef();
		bd.position.set(initX, initY + 0.5f);
		bd.type = BodyType.DYNAMIC;
		Body body = world.createBody(bd);

		PolygonShape box = new PolygonShape();
		box.setAsBox(2.5f, 0.15f);
		body.createFixture(box, 1f);

		BodyDef bdBase = new BodyDef();
		bdBase.type = BodyType.STATIC;
		bdBase.position.set(new Vec2(initX, initY - 0.5f));
		Body bodyBase = world.createBody(bdBase);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.05f, 0.5f);
		bodyBase.createFixture(shape, 0);

		RevoluteJointDef jd = new RevoluteJointDef();
		jd.initialize(bodyBase, body, new Vec2(initX, initY));
		jd.lowerAngle = -8.0f * MathUtils.PI / 180.0f;
		jd.upperAngle = 8.0f * MathUtils.PI / 180.0f;
		jd.enableLimit = true;
		world.createJoint(jd);

		body.applyAngularImpulse(100.0f);
		return body;
	}
}
