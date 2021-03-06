package game.AndJoy;

import game.AndJoy.Y2SandboxProgram.Y2SandboxAdapter;
import game.AndJoy.common.Constants;
import game.AndJoy.common.YBox2dTestUtils;
import game.AndJoy.common.YSceneDomain;
import game.AndJoy.monster.concrete.YMonsterDomain;
import game.AndJoy.sprite.concrete.YSpriteDomain;

import java.lang.ref.SoftReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.json.JSONException;

import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.YTiledJsonParser;
import ygame.extension.domain.YProgressBarDomain;
import ygame.extension.domain.tilemap.YTileMapDomain;
import ygame.extension.primitives.YRectangle;
import ygame.extension.primitives.YSphere;
import ygame.extension.primitives.YSquare;
import ygame.extension.program.YTextureProgram;
import ygame.extension.third_party.kankan.wheel.widget.WheelView;
import ygame.extension.third_party.kankan.wheel.widget.adapters.AbstractWheelAdapter;
import ygame.extension.with_third_party.YTiledJson_Box2dParser;
import ygame.extension.with_third_party.YWorld;
import ygame.framebuffer.YFBOScene;
import ygame.framework.YIResultCallback;
import ygame.framework.core.YCamera;
import ygame.framework.core.YClusterDomain;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.core.YSystem.YIOnFPSUpdatedListener;
import ygame.framework.core.YView;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.math.vector.Vector3;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;
import ygame.transformable.YMover;
import ygame.utils.YTextFileUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private YSystem system;

	public volatile boolean bRightPressing;
	public volatile boolean bLeftPressing;

	private YSpriteDomain domainSprite;
	private YClusterDomain domainMap = null;
	private YMonsterDomain domainMonster2;
	private YMonsterDomain domainMonster1;

	private YScene mainScene;

	private YDomain[] feiKuaiMapDomains;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		YView yview = (YView) findViewById(R.id.YView);
		this.system = yview.SYSTEM;
		// this.system.bFPS_Debug = true;

		final TextView tvFps = (TextView) findViewById(R.id.tvFPS);
		this.system.setOnFPSUpdatedListener(new YIOnFPSUpdatedListener()
		{
			private NumberFormat formatter = new DecimalFormat(
					"#0.00");

			@Override
			public void onFPSUpdated(double fps)
			{
				tvFps.setText("fps:" + formatter.format(fps));
			}
		});

		// 设置拥有box2d世界的场景
		YWorld world = new YWorld(new Vec2(0, -100));
		mainScene = getBox2dScene(world);
		mainScene.requestEnter(null);
		// system.forceSetCurrentScene(mainScene);

		// 输入相关
		OnTouchListener lsn = new SteerLsn();
		findViewById(R.id.BtnLeft).setOnTouchListener(lsn);
		findViewById(R.id.BtnRight).setOnTouchListener(lsn);
		findViewById(R.id.BtnJump).setOnTouchListener(new JumpBtnLsn());
		findViewById(R.id.BtnXuanfeng).setOnTouchListener(
				new AttackBtnLsn());
		findViewById(R.id.BtnScene).setOnLongClickListener(
				new SceneBtnLongLsn());
		WheelView wv = (WheelView) findViewById(R.id.wv_skills);
		wv.setWheelBackground(R.drawable.btn_blue);
		wv.setShadowColor(0x22FFFFFF, 0x11AAAAAA, 0x22FFFFFF);
		wv.setVisibleItems(3);
		wv.setViewAdapter(new SkillAdapter(this));

		// 测试相关，放入测试箱子、测试吊桥
		YBox2dTestUtils.addTestBox(world, mainScene, getResources());
		YBox2dTestUtils.addTestBridge(new Vec2(92 * 5, 22), new Vec2(
				108 * 5, 22), 10, world, mainScene,
				getResources());

		// 新建地图实体
		// try {
		// YTiledJsonParser parser = new YTiledJson_Box2dParser(world,
		// YTextFileUtils.getStringFromAssets("2mi.json",
		// getResources()), 5);
		// domainMap = new YTileMapDomain("map", getResources(),
		// R.drawable.yewai, parser, 8, 1);
		// // domainMap = new YBlockMapDomain("block",
		// // R.raw.mi2_huge, parser);
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }

		// 新建精灵实体
		domainSprite = new YSpriteDomain(Constants.SPRITE, world, this);
		// 新建怪物实体
		domainMonster1 = new YMonsterDomain(Constants.MONSTER1,
				Constants.MONSTER1_HP, world, this, -340, 20);
		domainMonster2 = new YMonsterDomain(Constants.MONSTER2,
				Constants.MONSTER2_HP, world, this, -200, 20);
		YProgressBarDomain monster1Hp = new YProgressBarDomain(
				Constants.MONSTER1_HP, getResources(), 5, 0.5f);
		YProgressBarDomain monster2Hp = new YProgressBarDomain(
				Constants.MONSTER2_HP, getResources(), 5, 0.5f);
		// 向场景添加各个实体
		// mainScene.addDomains(domainMap, domainSprite,
		// getBkgDomain());
		// feiKuaiMapDomains = getFeiKuaiMapDomains(world);
		//
		// mainScene.addDomains(feiKuaiMapDomains);
		mainScene.addDomains(getFeiKuaiMapDomains(world));
		mainScene.addDomains(domainSprite, domainMonster1,
				domainMonster2, getBkgDomain());
		mainScene.addDomains(monster1Hp, monster2Hp);

		// mainScene.addDomains(new YMonsterDomain("m3", world, this,
		// -100, 20));
		// mainScene.addDomains(new YMonsterDomain("m4", world, this,
		// -130, 20));
		// mainScene.addDomains(new YMonsterDomain("m5", world, this,
		// -160, 20));
		// mainScene.addDomains(new YMonsterDomain("m6", world, this,
		// -190, 20));
		// mainScene.addDomains(new YMonsterDomain("m7", world, this,
		// -220, 20));
		// for (int i = 0; i < 10; i++)
		// mainScene.addDomains(new YMonsterDomain("mm" + i,
		// world, this, -250 - 30 * i, 20));

		// mainScene.addDomains(domainMonster, getBkgDomain());
		// mainScene.addDomains(getTest2SandBoxDomain());//球
		// // 特别地，场景处理实体
		mainScene.addDomains(new YSceneDomain("sd", getResources()));

		// mainScene.getCurrentCamera().setZ(160 * 2);
		// mainScene.addDomains(new YDebrisClusterDomain("fff", world,
		// new YTexture(R.drawable.shan, getResources())));
		// mainScene.addDomains(getTestGridSkeletonDomain());
	}

	private YDomain[] getFeiKuaiMapDomains(World world)
	{
		YDomain[] domains = new YDomain[16];
		YSkeleton skeleton = new YSquare(16 * 5, false, true);
		int[] R_drawable =
		{ R.drawable.map0, R.drawable.map1, R.drawable.map2,
				R.drawable.map3, R.drawable.map4,
				R.drawable.map5, R.drawable.map6,
				R.drawable.map7, R.drawable.map8,
				R.drawable.map9, R.drawable.map10,
				R.drawable.map11, R.drawable.map12,
				R.drawable.map13, R.drawable.map14,
				R.drawable.map15, };

		for (int i = 0; i < R_drawable.length; i++)
			domains[i] = getFeiKuaiMapDomain(i + "feiKuai",
					(-7.5f + i) * 16 * 5, skeleton,
					new YTexture(R_drawable[i],
							getResources()));
		// domains[0] = getFeiKuaiMapDomain("FenKuaiLeft", -40,
		// skeleton,
		// new YTexture(R.drawable.fenkuai_left,
		// getResources()));
		// domains[1] = getFeiKuaiMapDomain("FenKuaiRight", 40,
		// skeleton,
		// new YTexture(R.drawable.fenkuai_right,
		// getResources()));

		try
		{
			new YTiledJson_Box2dParser(world,
					YTextFileUtils.getStringFromAssets(
							"2mi.json",
							getResources()), 5);
		} catch (JSONException e)
		{
			e.printStackTrace();
		}

		return domains;
	}

	private YDomain getFeiKuaiMapDomain(String Key, final float fX,
			final YSkeleton skeleton, final YTexture texture)
	{
		YADomainLogic logic = new YADomainLogic()
		{

			private YMover mover = (YMover) new YMover().setX(fX);

			// .setZ(-2.5f);

			@Override
			protected boolean onDealRequest(YRequest request,
					YSystem system, YScene sceneCurrent,
					YBaseDomain domainContext)
			{
				// TODO Auto-generated method stub
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
				YTextureProgram.YAdapter adapter = (YTextureProgram.YAdapter) domainContext
						.getParametersAdapter();
				adapter.paramMatrixPV(matrix4pv)
						.paramMover(mover)
						.paramSkeleton(skeleton)
						.paramTexture(texture);
			}
		};
		YDomainView view = new YDomainView(
				YTextureProgram.getInstance(getResources()));
		return new YDomain(Key, logic, view);
	}

	// private YDomain getTestGridSkeletonDomain()
	// {
	// YADomainLogic logic = new YADomainLogic()
	// {
	//
	// private YMover mover = new YMover();
	// private YSkeleton skeleton = new TextureRect();
	//
	// @Override
	// protected boolean onDealRequest(YRequest request,
	// YSystem system, YScene sceneCurrent)
	// {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// protected void onCycle(double dbElapseTime_s,
	// YDomain domainContext,
	// YWriteBundle bundle, YSystem system,
	// YScene sceneCurrent, YMatrix matrix4pv,
	// YMatrix matrix4Projection,
	// YMatrix matrix4View)
	// {
	// YSimpleParamAdapter adapter = (YSimpleParamAdapter) domainContext
	// .getParametersAdapter();
	// adapter.paramMatrixPV(matrix4pv)
	// .paramMover(mover)
	// .paramSkeleton(skeleton);
	// }
	// };
	//
	// YDomainView view = new YDomainView(
	// YSimpleProgram.getInstance(getResources()));
	// view.iDrawMode = GLES20.GL_TRIANGLES;
	// return new YDomain("gggg", logic, view);
	// }

	private YScene getBox2dScene(final YWorld world)
	{
		// YFBOScene scene = new YFBOScene(system, "野外")
		// {
		// @Override
		// protected void onClockCycle(double dbDeltaTime_s)
		// {
		// world.step(1.0f / 60, 8, 3);
		// super.onClockCycle(dbDeltaTime_s);
		// }
		// };
		// YFBOScene.setFrameBufferQuality(1f);

		// BodyDef def = new BodyDef();
		// def.type = BodyType.STATIC;
		// def.position = new Vec2(0, -50);
		// Body ground = world.createBody(def);
		// PolygonShape shape = new PolygonShape();
		// shape.setAsBox(1000, 10);
		// FixtureDef defFix = new FixtureDef();
		// defFix.setShape(shape);
		// defFix.setFriction(0);
		// ground.createFixture(defFix);

		YFBOScene scene = new YFBOScene(system, "野外");
		scene.addClockerPlugin(world);
		return scene;
	}

	/**
	 * 获取背景实体
	 * 
	 * @return 背景实体
	 */
	private YDomain getBkgDomain()
	{
		YADomainLogic logic = new YADomainLogic()
		{
			private YMover mover = (YMover) new YMover().setZ(-3f);
			private YSkeleton skeleton = new YRectangle(20 * 5,
					20 * 5, false, true);
			// private YSkeleton skeleton = new YRectangle(15 * 5,
			// 10 * 5, false, true);
			private YTexture texture = new YTexture(
					BitmapFactory.decodeResource(
							getResources(),
							R.drawable.shan));

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
				// 背景跟着摄像机移动，使得玩家觉得背景水平方向没有移动
				YCamera camera = sceneCurrent
						.getCurrentCamera();
				mover.setX(camera.getX());

				YTextureProgram.YAdapter adapter = (YTextureProgram.YAdapter) domainContext
						.getParametersAdapter();
				adapter.paramMatrixPV(matrix4pv)
						.paramMover(mover)
						.paramSkeleton(skeleton)
						.paramTexture(texture);
			}
		};

		YDomainView view = new YDomainView(
				YTextureProgram.getInstance(getResources()));
		return new YDomain("bkg", logic, view);
	}

	private YDomain getTest2SandBoxDomain()
	{
		YADomainLogic logic = new YADomainLogic()
		{

			private YMover mover = (YMover) new YMover()
					.setShaft(new Vector3(1, 1, 0));
			// private YSkeleton skeleton = new YSquare(12, false,
			// false);
			private YSkeleton skeleton = new YSphere(4, null);

			// private YSkeleton skeleton = new YRectangle(320, 120,
			// false, false);

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
				YCamera camera = sceneCurrent
						.getCurrentCamera();
				mover.setX(camera.getX())
						.setY(camera.getY() + 8f)
						.setAngle((float) (mover
								.getAngle() + dbElapseTime_s * 45));
				Y2SandboxAdapter adapter = (Y2SandboxAdapter) domainContext
						.getParametersAdapter();
				adapter.paramMatrixPV(matrix4pv)
						.paramMover(mover)
						.paramSkeleton(skeleton);
			}
		};

		YDomainView view = new YDomainView(new Y2SandboxProgram(
				getResources()));

		return new YDomain("mo", logic, view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private boolean bWhich;
	private volatile boolean bSwitchFinish;

	public void dealScene(View view) throws JSONException
	{
		// YFBOScene scene = (YFBOScene) system.getCurrentScene();
		// scene.enableSceneAsTexture(b = !b);
		if (bSwitchFinish)
			return;
		bSwitchFinish = true;
		bWhich = !bWhich;
		if (bWhich)
		{
			YScene scene = new YFBOScene(system, "测试");
			YTiledJsonParser parser = new YTiledJsonParser(
					YTextFileUtils.getStringFromAssets(
							"test.json",
							getResources()), 5);
			scene.addDomains(new YTileMapDomain("mapCity",
					getResources(), R.drawable.test_tile,
					parser, 1, 1), new YSceneDomain("sdn",
					getResources()));
			system.switchScene(scene, new YIResultCallback()
			{
				public void onResultReceived(Object objResult)
				{
					bSwitchFinish = false;
				}
			});
		} else
		{
			// mainScene.addDomains(getPostDealDomainfff());
			system.switchScene(mainScene, new YIResultCallback()
			{
				public void onResultReceived(Object objResult)
				{
					bSwitchFinish = false;
				}
			});
		}
	}

	private class JumpBtnLsn implements OnTouchListener
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				domainSprite.jump();
			return false;
		}
	}

	private class AttackBtnLsn implements OnTouchListener
	{

		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				domainSprite.attack();
			return false;
		}
	}

	private class SteerLsn implements OnTouchListener
	{

		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			boolean bRight = v.getId() == R.id.BtnRight;
			switch (event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				if (bRight)
					bRightPressing = true;
				else
					bLeftPressing = true;
				domainSprite.walk();
				break;

			case MotionEvent.ACTION_UP:
				if (bRight)
					bRightPressing = false;
				else
					bLeftPressing = false;
				domainSprite.waiting();
				break;

			default:
				break;
			}
			return false;
		}
	}

	private class SceneBtnLongLsn implements OnLongClickListener
	{
		private boolean bFlag;

		@Override
		public boolean onLongClick(View arg0)
		{
			bFlag = !bFlag;
			if (bFlag)
				system.getCurrentScene().removeDomains(
						feiKuaiMapDomains);
			else
				system.getCurrentScene().addDomains(
						feiKuaiMapDomains);
			return true;
		}
	}

	private static class SkillAdapter extends AbstractWheelAdapter
	{
		// Image size
		final int IMAGE_WIDTH = 120;
		final int IMAGE_HEIGHT = 90;

		//@formatter:off
		private final int items[] = new int[]
		{ 		R.drawable.skill1 , 
				R.drawable.skill2 , 
				R.drawable.skill3 , 
				R.drawable.skill4 , 
				R.drawable.skill5 , };
		//@formatter:on

		// Cached images
		private List<SoftReference<Bitmap>> images;

		// Layout inflater
		private Context context;

		/**
		 * Constructor
		 */
		public SkillAdapter(Context context)
		{
			this.context = context;
			images = new ArrayList<SoftReference<Bitmap>>(
					items.length);
			for (int id : items)
			{
				images.add(new SoftReference<Bitmap>(
						loadImage(id)));
			}
		}

		/**
		 * Loads image from resources
		 */
		private Bitmap loadImage(int id)
		{
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), id);
			Bitmap scaled = Bitmap.createScaledBitmap(bitmap,
					IMAGE_WIDTH, IMAGE_HEIGHT - 10, true);
			bitmap.recycle();
			return getRoundedCornerBitmap(scaled);
		}

		@Override
		public int getItemsCount()
		{
			return items.length;
		}

		// Layout params for image view
		final LayoutParams params = new LayoutParams(IMAGE_WIDTH,
				IMAGE_HEIGHT);

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent)
		{
			ImageView img;
			if (cachedView != null)
			{
				img = (ImageView) cachedView;
			} else
			{
				img = new ImageView(context);
			}
			img.setLayoutParams(params);
			SoftReference<Bitmap> bitmapRef = images.get(index);
			Bitmap bitmap = bitmapRef.get();
			if (bitmap == null)
			{
				bitmap = loadImage(items[index]);
				images.set(index, new SoftReference<Bitmap>(
						bitmap));
			}
			img.setImageBitmap(bitmap);

			return img;
		}

		// 生成圆角图片
		public static Bitmap getRoundedCornerBitmap(Bitmap bitmap)
		{
			try
			{
				Bitmap output = Bitmap.createBitmap(
						bitmap.getWidth(),
						bitmap.getHeight(),
						Config.ARGB_8888);
				Canvas canvas = new Canvas(output);
				final Paint paint = new Paint();
				final Rect rect = new Rect(0, 0,
						bitmap.getWidth(),
						bitmap.getHeight());
				final RectF rectF = new RectF(new Rect(0, 0,
						bitmap.getWidth(),
						bitmap.getHeight()));
				final float roundPx = 14;
				paint.setAntiAlias(true);
				canvas.drawARGB(0, 0, 0, 0);
				paint.setColor(Color.BLACK);
				canvas.drawRoundRect(rectF, roundPx, roundPx,
						paint);
				paint.setXfermode(new PorterDuffXfermode(
						Mode.SRC_IN));

				final Rect src = new Rect(0, 0,
						bitmap.getWidth(),
						bitmap.getHeight());

				canvas.drawBitmap(bitmap, src, rect, paint);
				bitmap.recycle();
				return output;
			} catch (Exception e)
			{
				return bitmap;
			}
		}
	}

}
