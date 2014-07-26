package game.AndJoy.post_deal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.program.YTextureProgram;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YClusterDomain;
import ygame.framework.core.YSystem;
import ygame.math.vector.Vector2;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;
import ygame.utils.YTextFileUtils;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.opengl.GLES20;

public class YDebrisClusterDomain extends YClusterDomain
{
	private World world;
	private YTexture texture;
	private YSystem system;

	public YDebrisClusterDomain(String KEY, World world, YTexture texture)
	{
		super(KEY);
		this.world = world;
		this.texture = texture;
	}

	@Override
	@SuppressLint("WrongCall")
	protected void onDraw(YSystem system)
	{
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		super.onDraw(system);
	}

	@Override
	protected void onAttach(YSystem system)
	{
		this.system = system;
		super.onAttach(system);
		try
		{
			addComponentDomains(
					createDebrisDomains(system.getContext()
							.getResources(), world,
							texture), system);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Collection<YABaseDomain> createDebrisDomains(
			Resources resources, World world, YTexture texture)
			throws JSONException
	{
		List<YABaseDomain> domains = new LinkedList<YABaseDomain>();
		Debris[] debriss = parse(YTextFileUtils.getStringFromAssets(
				"smashTest.json", resources));
		int i = 0;
		for (Debris debris : debriss)
		{
			YSkeleton skeleton = new YDebrisTriangle(
					debris.f_arrTexCoord,
					debris.f_arrPosCoord);
			// Body body = designBody(debris, world);
			domains.add(new YDomain(
					"smash" + (++i),
					new YDebrisDomainLogic(skeleton,
							texture, debris.body),
					new YDomainView(YTextureProgram
							.getInstance(resources))));
		}
		// domains.remove(1);
		// System.out.println("domainSize" + domains.size());
		return domains;
	}

	private Body designBody(Debris debris, World world)
	{
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;
		final Body body = world.createBody(bd);

		PolygonShape shape = new PolygonShape();
		Vec2[] vertices = new Vec2[debris.vertexsOrigine.length];
		final float fRadio = 0.07f;
		for (int i = 0; i < vertices.length; i++)
		{
			Vec2 vec2 = new Vec2();
			vec2.x = debris.vertexsAbsolute[i].x * fRadio;
			vec2.y = debris.vertexsAbsolute[i].y * fRadio;
			vertices[i] = vec2;
		}
		shape.set(vertices, vertices.length);
		FixtureDef fd = new FixtureDef();
		fd.shape = shape;
		fd.density = 1f;
		fd.friction = 0.2f;
		fd.restitution = 0f;
		body.createFixture(fd);
		return body;
	}

	private Debris[] parse(String strJson) throws JSONException
	{
		JSONTokener tokener = new JSONTokener(strJson);
		JSONObject jsonTree = (JSONObject) tokener.nextValue();
		JSONArray objArray = jsonTree.getJSONArray("objects");
		final int length = objArray.length();
		Debris[] debriss = new Debris[length];
		for (int i = 0; i < length; i++)
		{
			JSONObject jsonDeb = objArray.getJSONObject(i);
			debriss[i] = parseDebrisOrigine(jsonDeb);

			calculateAbsoluteCoords(debriss[i]);
			calculateTexCoords(debriss[i], 1024, 1024);
			calculatePosCoords(debriss[i], 0.07f, system);
		}
		return debriss;
	}

	private void calculatePosCoords(Debris debris, float fRadio,
			YSystem system)
	{
		// int iOffset = 0;
		//
		// // for (Vector2 vec2 : debris.vertexsAbsolute)
		// // {
		// // debris.f_arrPosCoord[iOffset++] = vec2.x;
		// // debris.f_arrPosCoord[iOffset++] = vec2.y;
		// // }
		// for (int i = 0; i < 6; i++)
		// {
		// float f = 0 == i % 2 ? system.YVIEW.getWidth()
		// : system.YVIEW.getHeight();
		// debris.f_arrPosCoord[i] = debris.f_arrTexCoord[i] * f;
		// }
		//
		// for (int i = 0; i < debris.f_arrPosCoord.length; i++)
		// debris.f_arrPosCoord[i] *= fRadio;

		// testCal(debris, fRadio, system.YVIEW.getWidth(),
		// system.YVIEW.getHeight());
		testCal(debris, fRadio, 1024, 1024);
	}

	private void testCal(Debris debris, float fRadio, float fWidth,
			float fHeight)
	{
		BodyDef def = new BodyDef();
		def.setType(BodyType.DYNAMIC);
		Body body = world.createBody(def);
		PolygonShape shape = new PolygonShape();
		Vec2[] vec2s = new Vec2[3];
		for (int i = 0; i < vec2s.length; i++)
		{
			Vec2 vec2 = new Vec2(debris.f_arrTexCoord[i * 2],
					debris.f_arrTexCoord[i * 2 + 1]);
			vec2.addLocal(-0.5f, -0.5f);
			vec2.x = vec2.x * fWidth * fRadio;
			vec2.y = vec2.y * fHeight * fRadio;

			vec2s[i] = vec2;
		}
		shape.set(vec2s, vec2s.length);
		FixtureDef defFix = new FixtureDef();
		defFix.setFriction(0);
		defFix.setShape(shape);
		defFix.setDensity(200);
		body.createFixture(defFix);

		// body.createFixture(shape, 1);
		// Vec2 position = body.getPosition();
		world.step(1.0f / 60, 8, 3);
		Vec2 vec2BodyMassCenter = body.getWorldCenter();
		
		// Vec2 position = body.getPosition();
		// world.destroyBody(body);

		int kk = 0;
		for (Vec2 vec2 : vec2s)
		{
			Vec2 sub = vec2.sub(vec2BodyMassCenter);
			debris.f_arrPosCoord[kk * 3] = sub.x;
			debris.f_arrPosCoord[kk * 3 + 1] = sub.y;
			debris.f_arrPosCoord[kk * 3 + 2] = 0;
			kk++;
		}
		debris.body = body;
		body.applyLinearImpulse(new Vec2(30, 20), body.getPosition());
	}

	private void calculateTexCoords(Debris debris, int iWidth, int iHeight)
	{
		final Vector2 vOFFSET = new Vector2(0, -1);
		int iOffset = 0;
		for (int i = 0; i < debris.vertexsOrigine.length; i++)
		{
			Vector2 vec2 = new Vector2();
			vec2.x = debris.vertexsAbsolute[i].x / (float) iWidth;
			vec2.y = debris.vertexsAbsolute[i].y / (float) iHeight;
			vec2.addLocal(vOFFSET);
//			vec2.y = -vec2.y;

			debris.f_arrTexCoord[iOffset++] = vec2.x;
			debris.f_arrTexCoord[iOffset++] = vec2.y;
		}
	}

	private static void calculateAbsoluteCoords(Debris debris)
	{
		for (int i = 0; i < debris.vertexsOrigine.length; i++)
			debris.vertexsAbsolute[i] = debris.vertexsOrigine[i]
					.add(debris.positionOrigine);
	}

	private static Debris parseDebrisOrigine(JSONObject jsonDebris)
			throws JSONException
	{
		Debris debris = new Debris();
		JSONArray polygon = jsonDebris.getJSONArray("polygon");
		for (int i = 0; i < polygon.length(); i++)
		{
			JSONObject vertex = polygon.getJSONObject(i);
			double x = vertex.getDouble("x");
			double y = vertex.getDouble("y");
			debris.vertexsOrigine[i].x = (float) x;
			debris.vertexsOrigine[i].y = (float) y;
		}
		double x = jsonDebris.getDouble("x");
		double y = jsonDebris.getDouble("y");
		debris.positionOrigine.x = (float) x;
		debris.positionOrigine.y = (float) y;
		return debris;
	}

	private static class Debris
	{
		public Body body;
		final private Vector2[] vertexsOrigine = new Vector2[3];
		final private Vector2 positionOrigine = new Vector2();

		// final private Vector2[] vertexsNormalAbsolute = new
		// Vector2[3];
		final private Vector2[] vertexsAbsolute = new Vector2[3];

		private float[] f_arrTexCoord = new float[6];
		private float[] f_arrPosCoord = new float[9];

		public Debris()
		{
			for (int i = 0; i < vertexsOrigine.length; i++)
			{
				vertexsOrigine[i] = new Vector2();
				// vertexsNormalAbsolute[i] = new Vector2();
			}
		}
	}

}
