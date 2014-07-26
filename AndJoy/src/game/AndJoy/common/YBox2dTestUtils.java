package game.AndJoy.common;

import org.jbox2d.collision.shapes.PolygonShape;
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
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.math.vector.Vector3;
import ygame.skeleton.YSkeleton;
import ygame.transformable.YMover;
import android.content.res.Resources;

public final class YBox2dTestUtils {
	private YBox2dTestUtils() {
	}

	public static void addTestBridge(Vec2 vec2Start, Vec2 vec2End,
			float fTileLen, World world, YScene scene, Resources resources) {
		final int N = (int) (vec2End.sub(vec2Start).x / fTileLen);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(fTileLen / 2, 1f);

		FixtureDef fd = new FixtureDef();
		fd.shape = shape;
		fd.density = 3.0f;
		fd.friction = 0.6f;

		RevoluteJointDef jd = new RevoluteJointDef();
		YSkeleton skeleton = new YRectangle(fTileLen, 1f, true, false);
		Body prevBody = newStaticBody(vec2Start, world);
		for (int i = 0; i < N; ++i) {
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			bd.position.set(vec2Start.x + 0.05f + fTileLen / 2 + fTileLen * i,
					vec2Start.y);
			Body body = world.createBody(bd);
			body.createFixture(fd);

			scene.addDomains(newBridgeComponent("test_bridge" + i, body,
					skeleton, resources));
			Vec2 anchor = new Vec2(vec2Start.x + 0.05f + fTileLen * i,
					vec2Start.y);
			jd.initialize(prevBody, body, anchor);
			world.createJoint(jd);

			prevBody = body;
		}

		Vec2 anchor = new Vec2(vec2End.x - 0.05f, vec2End.y);
		jd.initialize(prevBody, newStaticBody(vec2End, world), anchor);
		world.createJoint(jd);
	}

	private static YDomain newBridgeComponent(String KEY, final Body body,
			final YSkeleton skeleton, Resources resources) {
		YADomainLogic logic = new YADomainLogic() {

			private YMover mover = (YMover) new YMover().setZ(-1).setShaft(
					new Vector3(0, 0, 1));

			@Override
			protected boolean onDealRequest(YRequest request, YSystem system,
					YScene sceneCurrent) {
				return false;
			}

			@Override
			protected void onCycle(double dbElapseTime_s,
					YDomain domainContext, YWriteBundle bundle, YSystem system,
					YScene sceneCurrent, YMatrix matrix4pv,
					YMatrix matrix4Projection, YMatrix matrix4View) {
				Vec2 position = body.getPosition();
				mover.setX(position.x).setY(position.y)
						.setAngle(body.getAngle() * 180 / 3.1415f);
				YSimpleProgram.YAdapter adapter = (YSimpleProgram.YAdapter) domainContext
						.getParametersAdapter();
				adapter.paramMatrixPV(matrix4pv).paramMover(mover)
						.paramSkeleton(skeleton);
			}
		};

		YDomainView view = new YDomainView(
				YSimpleProgram.getInstance(resources));
		// view.iDrawMode = GLES20.GL_LINE_LOOP;

		return new YDomain(KEY, logic, view);
	}

	private static Body newStaticBody(Vec2 vecPosition, World world) {
		BodyDef bd = new BodyDef();
		bd.type = BodyType.STATIC;
		bd.position.set(vecPosition);
		Body body = world.createBody(bd);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.05f, 0.05f);
		body.createFixture(shape, 0);
		return body;
	}

	public static void addTestBox(World world, YScene scene, Resources resources) {
		final float fTileSize = 5;
		YSkeleton skeleton = new YSquare(fTileSize, true, false);
		YSimpleProgram program = YSimpleProgram.getInstance(resources);
		for (int i = 0; i < 3; i++)
			// scene.addDomains(newTestBoxDomain(world, skeleton,
			// program, "test_box" + i, -100,
			// 10 * i + 10, fTileSize));
			scene.addDomains(newTestBoxDomain(world, skeleton, program,
					"test_box" + i, (176 - 128) * 5, 10 * i + 40, fTileSize));
	}

	private static YDomain newTestBoxDomain(World world,
			final YSkeleton skeleton, YSimpleProgram program, String KEY,
			float fInitX_M, float fInitY_M, float fTileSize) {
		final Body body = newTestBoxBody(world, fInitX_M, fInitY_M, fTileSize);

		YADomainLogic logic = new YADomainLogic() {

			private YMover mover = (YMover) new YMover().setZ(2).setShaft(
					new Vector3(0, 0, 1));

			@Override
			protected boolean onDealRequest(YRequest request, YSystem system,
					YScene sceneCurrent) {
				return false;
			}

			@Override
			protected void onCycle(double dbElapseTime_s,
					YDomain domainContext, YWriteBundle bundle, YSystem system,
					YScene sceneCurrent, YMatrix matrix4pv,
					YMatrix matrix4Projection, YMatrix matrix4View) {
				Vec2 position = body.getPosition();
				mover.setX(position.x).setY(position.y)
						.setAngle(body.getAngle() * 180 / 3.1415f);
				YSimpleProgram.YAdapter adapter = (YSimpleProgram.YAdapter) domainContext
						.getParametersAdapter();
				adapter.paramMatrixPV(matrix4pv).paramMover(mover)
						.paramSkeleton(skeleton);
			}
		};

		YDomainView view = new YDomainView(program);
		// view.iDrawMode = GLES20.GL_LINE_LOOP;

		body.setUserData(new YDomain(KEY, logic, view));
		return (YDomain) body.getUserData();
	}

	private static Body newTestBoxBody(World world, float fInitX_M,
			float fInitY_M, float fBODY_SIZE_M) {
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
}
