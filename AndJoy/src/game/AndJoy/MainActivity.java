package game.AndJoy;

import game.AndJoy.common.Constants;
import game.AndJoy.common.YBox2dTestUtils;
import game.AndJoy.common.YSceneDomain;
import game.AndJoy.monster.concrete.YMonsterDomain;
import game.AndJoy.obstacle.ObstacleDomain;
import game.AndJoy.sprite.concrete.YSpriteDomain;

import java.lang.ref.SoftReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;

import ygame.domain.YADomainLogic;
import ygame.domain.YDomain;
import ygame.domain.YDomainView;
import ygame.extension.domain.YProgressBarDomain;
import ygame.extension.primitives.YRectangle;
import ygame.extension.program.YTextureProgram;
import ygame.extension.third_party.kankan.wheel.widget.WheelView;
import ygame.extension.third_party.kankan.wheel.widget.adapters.AbstractWheelAdapter;
import ygame.extension.tiled.YBaseParsePlugin;
import ygame.extension.tiled.YStaticImageLayerParsePlugin;
import ygame.extension.tiled.YStaticPolyLineTerrainParsePlugin;
import ygame.extension.tiled.YTiledParser;
import ygame.extension.with_third_party.YWorld;
import ygame.framebuffer.YFBOScene;
import ygame.framework.YIResultCallback;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YCamera;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.core.YSystem.YIOnFPSUpdatedListener;
import ygame.framework.core.YView;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.skeleton.YSkeleton;
import ygame.texture.YTexture;
import ygame.transformable.YMover;
import android.annotation.SuppressLint;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends Activity
{

	private YSystem system;

	public volatile boolean bRightPressing;
	public volatile boolean bLeftPressing;

	// private YSpriteDomain domainSprite;

	private YScene mainScene;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupViews();
		initMainScene();
	}

	private void setupViews()
	{
		// _____________YView
		YView yview = (YView) findViewById(R.id.YView);
		this.system = yview.SYSTEM;
		// _____________左右移动按键
		OnTouchListener lsn = new SteerLsn();
		findViewById(R.id.BtnLeft).setOnTouchListener(lsn);
		findViewById(R.id.BtnRight).setOnTouchListener(lsn);
		// _____________跳跃按键
		findViewById(R.id.BtnJump).setOnTouchListener(new JumpBtnLsn());
		// _____________攻击按键
		findViewById(R.id.BtnAttack).setOnTouchListener(
				new AttackBtnLsn());
		// _____________场景切换测试
		View btnSwitchScene = findViewById(R.id.BtnScene);
		btnSwitchScene.setOnLongClickListener(new SceneBtnLongLsn());
		btnSwitchScene.setOnClickListener(new SceneBtnLsn());
		// _____________技能齿轮
		WheelView wv = (WheelView) findViewById(R.id.wv_skills);
		wv.setWheelForeground(R.drawable.skill_wheel_val);
		wv.setWheelBackground(R.drawable.bg_orange);
		// 禁止使用自带背景、阴影效果，全部设置为透明
		wv.setShadowColor(Constants.COLOR_TRANSPARENT,
				Constants.COLOR_TRANSPARENT,
				Constants.COLOR_TRANSPARENT);
		wv.setVisibleItems(3);
		wv.setViewAdapter(new SkillAdapter(this));
		// _____________FPS显示文本
		final TextView tvFps = (TextView) findViewById(R.id.tvFPS);
		this.system.setOnFPSUpdatedListener(new FPSLsn(tvFps));
	}

	//@formatter:off
	private void initMainScene()
	{
		// _____________新建场景
		final YWorld world = new YWorld(new Vec2(0, -15f), system);
		mainScene = new YFBOScene(system, "野外");
		mainScene.addClockerPlugin(world);
		mainScene.requestEnter(null);

		// _____________测试相关，放入测试箱子、测试吊桥
//		YBox2dTestUtils.addTestBoxes(world, mainScene, getResources());
		YBox2dTestUtils.addTestBridge(new Vec2(92, 4.5f), new Vec2(108,
				4.5f), 1, world, mainScene, getResources());
		YBox2dTestUtils.addOneTestBox(world, mainScene, getResources(),
				new Vec2(-127, 8));

		// _____________新建精灵实体
//		domainSprite = new YSpriteDomain(Constants.SPRITE, world, this);
		// _____________新建怪物实体
//		YABaseDomain domainMonster1 = new YMonsterDomain(
//				Constants.MONSTER1, Constants.MONSTER1_HP,
//				world, this, -38, 8);
//		YABaseDomain domainMonster2 = new YMonsterDomain(
//				Constants.MONSTER2, Constants.MONSTER2_HP,
//				world, this, 110, 8);
		// _____________新建怪物血条
		YProgressBarDomain monster1Hp = new YProgressBarDomain(
				Constants.MONSTER1_HP, getResources(), 1.5f, 0.1f);
		YProgressBarDomain monster2Hp = new YProgressBarDomain(
				Constants.MONSTER2_HP, getResources(), 1.5f, 0.1f);
		// _____________新建测试障碍
		ObstacleDomain obstacleDomain = new ObstacleDomain("test_Obs",
				this, world);
		
		// _____________解析Tiled生成的静态地图
		new YTiledParser(mainScene, "2mi.json", this)
				.append(new YStaticImageLayerParsePlugin("map","base_bkg", "decoration_bkg"))
				.append(new YStaticPolyLineTerrainParsePlugin("map", world, "box2d_bodies"))
				.append(new YBaseParsePlugin(new Object[]{world , MainActivity.this},"dynamic"))
				.parse();
//		new YTiledParser(mainScene, "city.json", this)
//			.append(new YStaticImageLayerParsePlugin("map","background", "foreground"))
//			.append(new YStaticPolyLineTerrainParsePlugin("map", world, "obj"))
//			.parse();

		// _____________向场景添加上述新建的实体
		mainScene.addDomains(/*domainSprite,*//* domainMonster1,
				domainMonster2,*/ monster1Hp, monster2Hp,
				obstacleDomain, getBkgDomain());
		// _____________特别地，场景处理实体（处理场景切换的特效）
		mainScene.addDomains(new YSceneDomain("sd", getResources()));
		mainScene.getCurrentCamera().setZ(10);
	}
	//@formatter:on

	/**
	 * 获取背景实体
	 * 
	 * @return 背景实体
	 */
	private YDomain getBkgDomain()
	{
		YADomainLogic logic = new YADomainLogic()
		{
			//@formatter:off
			private YMover mover = (YMover) new YMover().setZ(-3f);
			private YSkeleton skeleton = new YRectangle(20, 20,false, true);
			private YTexture texture = new YTexture(BitmapFactory.decodeResource(getResources(),R.drawable.shan));
//			private YSkeleton skeleton = new YRectangle(15, 20,false, true);
//			private YTexture texture = new YTexture(BitmapFactory.decodeResource(getResources(),R.drawable.city_bkg));
			//@formatter:on

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

	private class JumpBtnLsn implements OnTouchListener
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				YSpriteDomain sprite = (YSpriteDomain) mainScene
						.queryDomainByKey("sprite");
				sprite.jump();
			}
			return false;
		}
	}

	private class AttackBtnLsn implements OnTouchListener
	{

		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				YSpriteDomain sprite = (YSpriteDomain) mainScene
						.queryDomainByKey("sprite");
				sprite.attack();
			}
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
				{
					YSpriteDomain sprite = (YSpriteDomain) mainScene
							.queryDomainByKey("sprite");
					sprite.walk();
				}
				break;

			case MotionEvent.ACTION_UP:
				if (bRight)
					bRightPressing = false;
				else
					bLeftPressing = false;
				{
					YSpriteDomain sprite = (YSpriteDomain) mainScene
							.queryDomainByKey("sprite");
					sprite.waiting();
				}
				break;

			default:
				break;
			}
			return false;
		}
	}

	private class SceneBtnLongLsn implements OnLongClickListener
	{
		@Override
		public boolean onLongClick(View arg0)
		{// TODO for test , by yunzhong
			return true;
		}
	}

	private class SceneBtnLsn implements OnClickListener
	{
		private boolean bWhich;
		private volatile boolean bSwitchFinish;

		@Override
		public void onClick(View view)
		{
			if (bSwitchFinish)
				return;
			bSwitchFinish = true;
			bWhich = !bWhich;
			if (bWhich)
			{
				//@formatter:off
				YScene scene = new YFBOScene(system, "测试");
				new YTiledParser(scene, "city.json", MainActivity.this)
					.append(new YStaticImageLayerParsePlugin("map_city","background", "foreground"))
//					.append(new YStaticPolyLineTerrainParsePlugin("map", world, "obj"))
					.parse();
				//@formatter:on
				// system.switchScene(scene,
				// new YIResultCallback()
				// {
				// public void onResultReceived(
				// Object objResult)
				// {
				// bSwitchFinish = false;
				// }
				// });
				scene.getCurrentCamera().setZ(10);
				system.forceSetCurrentScene(scene);
			} else
			{
				system.switchScene(mainScene,
						new YIResultCallback()
						{
							public void onResultReceived(
									Object objResult)
							{
								bSwitchFinish = false;
							}
						});
			}
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

	private static class FPSLsn implements YIOnFPSUpdatedListener
	{
		private NumberFormat formatter = new DecimalFormat("#0.00");
		private TextView tvFps;

		private FPSLsn(TextView tvFps)
		{
			this.tvFps = tvFps;
		}

		@Override
		public void onFPSUpdated(double fps)
		{
			tvFps.setText("fps:" + formatter.format(fps));
		}
	}

}
