package game.AndJoy.sprite.concrete;

import game.AndJoy.MainActivity;
import game.AndJoy.R;
import game.AndJoy.DamageDisp.DamageDomain;
import game.AndJoy.DamageDisp.DamageReq;
import game.AndJoy.DamageDisp.IDamageDisplayer;
import game.AndJoy.common.Constants;
import game.AndJoy.monster.concrete.YMonsterDomain;
import game.AndJoy.sprite.concrete.YSpriteDomain.SpriteReq;

import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import ygame.common.YConstants;
import ygame.common.YConstants.Orientation;
import ygame.extension.domain.sprite.YASpriteDomainLogic;
import ygame.extension.domain.sprite.YIStateClocker;
import ygame.extension.tiled.domain.YDestructibleTerrainDomain;
import ygame.extension.with_third_party.YIOnContactListener;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YBaseDomain;
import ygame.state_machine.StateMachine;
import ygame.state_machine.YIAction;
import ygame.state_machine.builder.YStateMachineBuilder;
import ygame.texture.YTileSheet;
import android.util.Log;
import android.widget.ProgressBar;

class YSpriteLogic extends YASpriteDomainLogic implements IDamageDisplayer
{
	private float fFrames;
	// 受伤状态维持的周期计数
	private int iDamageCounts;
	private MainActivity activity;
	private Vec2 vecAntiGrav;
	private boolean ifInRadar;
	// 怪物是否在右边
	private boolean ifRightSide;
	// 怪物实体的KEY
	private String monsterKey;

	private boolean bOnLand;

	private boolean bRight = true;
	private int hp = 200;

	private YSystem system;

	protected YSpriteLogic(World world, MainActivity activity, float initX,
			float initY, float skeletonSideLen)
	{
		super(new YTileSheet(R.drawable.hero_big,
				activity.getResources(), 3, 22),
				skeletonSideLen, world);
		// fInitX_M = 105;//for yewai
		// fInitY_M = 8;
		fInitX_M = initX;
		fInitY_M = initY;

		this.activity = activity;
		vecAntiGrav = new Vec2(world.getGravity());
		// damageDomain = new DamageDomain("damage", activity);
	}

	/**
	 * 设计精灵实体的刚体结构：</br> 先在图纸上设计刚体结构，然后转为代码
	 * <p>
	 * <img src="./picExplain/body_structure.jpg" alt="精灵刚体结构" border="1" />
	 * </p>
	 * 
	 * @see game.AndJoy.sprite.YASpriteDomainLogic#designBody(org.jbox2d.dynamics.Body)
	 */
	@Override
	protected void designBody(Body body)
	{
		// 主体部分（Main）
		FixtureDef def = new FixtureDef();
		def.density = 5;
		def.friction = 0f;
		def.restitution = 0f;
		final float fBodySideLen = fSkeletonSideLen * 0.7f;
		CircleShape shapeBody = new CircleShape();
		shapeBody.setRadius(fBodySideLen / 2.5f);
		def.shape = shapeBody;
		Fixture fixtureMain = body.createFixture(def);
		fixtureMain.setOnContactListener(new MainContactLsn());

		// XXX
		// 足部感应器（foot）
		PolygonShape shapeFoot = new PolygonShape();
		shapeFoot.setAsBox(fBodySideLen / 6, fBodySideLen / 10,
				new Vec2(0, -fBodySideLen / 2f), 0);
		def.shape = shapeFoot;
		def.friction = 0.5f;
		def.restitution = 0;
		def.density = 10;
		def.userData = "foot";
		def.isSensor = true;
		Fixture fixtureFoot = body.createFixture(def);
		fixtureFoot.setOnContactListener(new FootContactLsn());

		// 感应攻击雷达
		CircleShape shapeRadar = new CircleShape();
		shapeRadar.setRadius(fBodySideLen / 2);

		def.isSensor = true;
		def.friction = 0f;
		def.density = 0f;
		def.shape = shapeRadar;
		Fixture fixtureRadar2 = body.createFixture(def);
		fixtureRadar2.setOnContactListener(new RadarContactLsn());

		vecAntiGrav.mulLocal(-body.getMass());
	}

	@Override
	protected boolean onDealRequest(YRequest request, YSystem system,
			YScene sceneCurrent, YBaseDomain domain)
	{
		if (request.iKEY == YSpriteDomain.TO_WALK)
		{
			Vec2 vec2 = new Vec2(body.getLinearVelocity());
			vec2.x = bRight ? 1 : -1;
			body.applyLinearImpulse(
					vec2.subLocal(body.getLinearVelocity())
							.mulLocal(body.getMass()),
					body.getPosition());
		}
		return super.onDealRequest(request, system, sceneCurrent,
				domain);
	}

	/**
	 * 
	 * 设计精灵实体的状态机：</br> 先在图纸上设计状态机结构，然后转为代码
	 * <p>
	 * <img src="./picExplain/state_machine.jpg" alt="精灵状态机" border="1" />
	 * </p>
	 * 
	 * @see game.AndJoy.sprite.YASpriteDomainLogic#designStateMachine(ygame.state_machine.builder.YStateMachineBuilder)
	 */
	@Override
	protected YIStateClocker designStateMachine(
			YStateMachineBuilder<YIStateClocker, YRequest, YASpriteDomainLogic> builder)
	{
		YSpriteState.WAIT.setStateClocker(new WaitClocker());
		YSpriteState.WALK.setStateClocker(new WalkClocker());
		YSpriteState.JUMP.setStateClocker(new JumpClocker());
		YSpriteState.ATTACK1.setStateClocker(new Attack1Cloker());
		YSpriteState.DAMAGE.setStateClocker(new DamageCloker());

		// 待机到行走
		builder.newTransition().from(YSpriteState.WAIT)
				.to(YSpriteState.WALK)
				.on(new SpriteReq(YSpriteDomain.TO_WALK));
		// 行走到待机
		builder.newTransition().from(YSpriteState.WALK)
				.to(YSpriteState.WAIT)
				.on(new SpriteReq(YSpriteDomain.TO_WAIT));

		// 待机到跳
		// 行走到跳
		JumpUpAction action = new JumpUpAction();
		builder.newTransition().from(YSpriteState.WAIT)
				.to(YSpriteState.JUMP)
				.on(new SpriteReq(YSpriteDomain.TO_JUMP))
				.perform(action);
		builder.newTransition().from(YSpriteState.WALK)
				.to(YSpriteState.JUMP)
				.on(new SpriteReq(YSpriteDomain.TO_JUMP))
				.perform(action);
		builder.onEntry(YSpriteState.JUMP).perform(
				new JumpEnterAction());

		// 待机到攻1
		// 行走到攻1
		builder.newTransition().from(YSpriteState.WAIT)
				.to(YSpriteState.ATTACK1)
				.on(new SpriteReq(YSpriteDomain.TO_ATTACK1));
		builder.newTransition().from(YSpriteState.WALK)
				.to(YSpriteState.ATTACK1)
				.on(new SpriteReq(YSpriteDomain.TO_ATTACK1));
		builder.onEntry(YSpriteState.ATTACK1).perform(
				new AttackEnterAction());

		// 待机到受伤
		// 行走到受伤
		builder.newTransition().from(YSpriteState.WAIT)
				.to(YSpriteState.DAMAGE)
				.on(new SpriteReq(YSpriteDomain.TO_DAMAGE));
		builder.newTransition().from(YSpriteState.WALK)
				.to(YSpriteState.DAMAGE)
				.on(new SpriteReq(YSpriteDomain.TO_DAMAGE));
		builder.onEntry(YSpriteState.DAMAGE).perform(
				new DamageEnterAction());

		// 跳到受伤
		builder.newTransition().from(YSpriteState.JUMP)
				.to(YSpriteState.DAMAGE)
				.on(new SpriteReq(YSpriteDomain.TO_DAMAGE));

		// 进入或离开行走状态
		WalkEnterQuitAction walkEnterQuitAction = new WalkEnterQuitAction();
		builder.onEntry(YSpriteState.WALK).perform(walkEnterQuitAction);
		builder.onExit(YSpriteState.WALK).perform(walkEnterQuitAction);
		return YSpriteState.JUMP;
	}

	@Override
	protected YConstants.Orientation updateCurrentOrientation()
	{
		if (activity.bRightPressing)
		{
			bRight = true;
			return YConstants.Orientation.RIGHT;
		} else if (activity.bLeftPressing)
		{
			bRight = false;
			return YConstants.Orientation.LEFT;
		}
		return super.updateCurrentOrientation();
	}

	private void resetState()
	{
		if (activity.bLeftPressing || activity.bRightPressing)
			stateMachine.forceSetState(YSpriteState.WALK);
		else
			stateMachine.forceSetState(YSpriteState.WAIT);
	}

	// XXX temp code，之后会有一个死亡状态与之对应，目前暂时用一个标志位表示
	private boolean bDead;

	private void resetGame()
	{
		if (bDead)
			return;
		bDead = true;
		activity.runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				activity.initMainScene();
			}
		});
	}

	/******************************** 各状态之详细描述 *****************************************/
	/******************************** 基础 *****************************************/
	private class BaseClocker implements YIStateClocker
	{
		final private int iFPS;
		final private int iFrameNum;
		final private int iColStartIndex;
		final private int iRowStartIndex;
		private ProgressBar progressBar = (ProgressBar) activity
				.findViewById(R.id.hp_bar);

		BaseClocker(int iFPS, int iFrameNum, int iColStartIndex,
				int iRowStartIndex)
		{
			this.iFPS = iFPS;
			this.iFrameNum = iFrameNum;
			this.iColStartIndex = iColStartIndex;
			this.iRowStartIndex = iRowStartIndex;
		}

		@Override
		public void onClock(float fElapseTime_s,
				YASpriteDomainLogic domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			int iFrameCur = (int) ((fFrames += fElapseTime_s * iFPS) % iFrameNum);
			iRowIndex = iRowStartIndex;
			iColumnIndex = iColStartIndex + iFrameCur;
			if (bOnLand)
				body.applyForce(vecAntiGrav, body.getPosition());
			progressBar.setProgress(hp);
		}
	}

	/*************************** 关于待机状态 **********************************/
	private class WaitClocker extends BaseClocker
	{
		private final Vec2 vec2 = new Vec2(0, 0);

		WaitClocker()
		{
			super(6, 4, 0, 0);
		}

		@Override
		public void onClock(float fElapseTime_s,
				YASpriteDomainLogic domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			bLockOrientation = false;
			super.onClock(fElapseTime_s, domainLogicContext,
					system, sceneCurrent);
			body.setLinearVelocity(vec2);
		}
	}

	/*************************** 关于行走状态 **********************************/
	private class WalkEnterQuitAction implements
			YIAction<YIStateClocker, YRequest, YASpriteDomainLogic>
	{
		private int streamID;

		@Override
		public void onTransition(
				YIStateClocker from,
				YIStateClocker to,
				YRequest causedBy,
				YASpriteDomainLogic context,
				StateMachine<YIStateClocker, YRequest, YASpriteDomainLogic> stateMachine)
		{
			if (YSpriteState.WALK == to)
				streamID = system.getAudioManager().playSound(
						R.raw.sprite_walk, 100, 1f, 1);
			else
				system.getAudioManager().pauseSound(streamID);
		}
	}

	private class WalkClocker extends BaseClocker
	{
		private final Vec2 vecRight = new Vec2(4, -2);
		private final Vec2 vecLeft = new Vec2(-4, -2);

		WalkClocker()
		{
			super(8, 6, 4, 0);
		}

		@Override
		public void onClock(float fElapseTime_s,
				YASpriteDomainLogic domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			bLockOrientation = false;
			super.onClock(fElapseTime_s, domainLogicContext,
					system, sceneCurrent);
			Vec2 vec2 = bRight ? vecRight : vecLeft;
			body.setLinearVelocity(vec2);
		}
	}

	/*************************** 关于跳跃状态 **********************************/
	private class JumpClocker implements YIStateClocker
	{

		@Override
		public void onClock(float fElapseTime_s,
				YASpriteDomainLogic domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			iRowIndex = 1;
			Vec2 velocity = body.getLinearVelocity();
			if (velocity.y > 0.5)
				iColumnIndex = 18;
			else
				iColumnIndex = 19;
		}
	}

	private class JumpUpAction implements
			YIAction<YIStateClocker, YRequest, YASpriteDomainLogic>
	{

		@Override
		public void onTransition(
				YIStateClocker from,
				YIStateClocker to,
				YRequest causedBy,
				YASpriteDomainLogic context,
				StateMachine<YIStateClocker, YRequest, YASpriteDomainLogic> stateMachine)
		{
			bLockOrientation = true;
			Vec2 v1 = body.getLinearVelocity();
			Vec2 v2 = new Vec2(v1.x, 15);

			body.applyLinearImpulse(
					v2.subLocal(v1)
							.mulLocal(body.getMass()),
					body.getPosition());
		}
	}

	private class JumpEnterAction implements
			YIAction<YIStateClocker, YRequest, YASpriteDomainLogic>
	{

		@Override
		public void onTransition(
				YIStateClocker from,
				YIStateClocker to,
				YRequest causedBy,
				YASpriteDomainLogic context,
				StateMachine<YIStateClocker, YRequest, YASpriteDomainLogic> stateMachine)
		{
			Vec2 v1 = body.getLinearVelocity();
			if (Math.abs(v1.x) <= 4)
				return;

			Vec2 v2 = new Vec2(4 * v1.x / Math.abs(v1.x), v1.y);

			body.applyLinearImpulse(
					v2.subLocal(v1)
							.mulLocal(body.getMass()),
					body.getPosition());
		}
	}

	@Override
	protected void onAttach(YSystem system, YBaseDomain domainContext)
	{
		super.onAttach(system, domainContext);
		this.system = system;
	}

	/*************************** 关于攻击1状态 **********************************/
	private class Attack1Cloker implements YIStateClocker
	{
		// private int[] i_arrFrameIndex =
		// { 23, 24, 25, 26, 27, 28, 29, 19, 22, 58 };
		// private int[] i_arrFrameIndex =
		// { 17, 18, };
		private int[] i_arrFrameIndex =
		{ 23, 24, 25, 20, 21, 0 };

		@Override
		public void onClock(float fElapseTime_s,
				YASpriteDomainLogic domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			int iFrame = (int) ((fFrames += fElapseTime_s * 10) % i_arrFrameIndex.length);
			if (i_arrFrameIndex.length - 1 == iFrame)
			{
				resetState();
				return;
			}

			iRowIndex = (i_arrFrameIndex[iFrame] - 1) / 22;
			iColumnIndex = (i_arrFrameIndex[iFrame] - 1) % 22;
			body.applyForce(vecAntiGrav, body.getPosition());

			if (ifRightSide == bRight && ifInRadar)
			// 确定精灵方向与怪物所在方向相同
			{
				YMonsterDomain monster = (YMonsterDomain) system
						.queryDomainByKey(monsterKey);
				if (null != monster)
					monster.sendRequest(monster.TO_DAMAGE);
			}
		}
	}

	private class AttackEnterAction implements
			YIAction<YIStateClocker, YRequest, YASpriteDomainLogic>
	{
		private Vec2 vec2Right = new Vec2(10, -10);
		private Vec2 vec2Left = new Vec2(-10, -10);

		@Override
		public void onTransition(
				YIStateClocker from,
				YIStateClocker to,
				YRequest causedBy,
				YASpriteDomainLogic context,
				StateMachine<YIStateClocker, YRequest, YASpriteDomainLogic> stateMachine)
		{
			body.applyForce(vecAntiGrav, body.getPosition());
			bLockOrientation = true;
			fFrames = 0;
			Vec2 v1 = body.getLinearVelocity();
			Vec2 v2 = new Vec2(bRight ? vec2Right : vec2Left);
			body.applyLinearImpulse(
					v2.subLocal(v1)
							.mulLocal(body.getMass()),
					body.getPosition());

		}
	}

	/*************************** 关于受伤状态 **********************************/
	private class DamageCloker extends BaseClocker
	{
		DamageCloker()
		{
			super(0, 1, 10, 0);
		}

		@Override
		public void onClock(float fElapseTime_s,
				YASpriteDomainLogic domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			super.onClock(fElapseTime_s, domainLogicContext,
					system, sceneCurrent);
			Log.d("伤害显示", "进入了受伤onclock");

			hp--;
			if (hp <= 0)
			{
				resetGame();
				return;
			}
			if (iDamageCounts == 0)
			{
				YScene scene = system.getCurrentScene();
				DamageDomain damageDomain = new DamageDomain(
						(YSpriteLogic) domainLogicContext,
						activity);
				float[] pos = new float[]
				{ mover.getX(), mover.getY() };

				damageDomain.sendRequest(new DamageReq(pos,
						(int) (Math.random() * 1000)));
				scene.addDomains(damageDomain);
			}
			if (iDamageCounts++ > 15)
			{
				iDamageCounts = 0;
				// scene.removeDomains("damage");
				resetState();
			}
		}
	}

	private class DamageEnterAction implements
			YIAction<YIStateClocker, YRequest, YASpriteDomainLogic>
	{
		private Vec2 vec2Right = new Vec2(4, -2);
		private Vec2 vec2Left = new Vec2(-4, -2);

		@Override
		public void onTransition(
				YIStateClocker from,
				YIStateClocker to,
				YRequest causedBy,
				YASpriteDomainLogic context,
				StateMachine<YIStateClocker, YRequest, YASpriteDomainLogic> stateMachine)
		{
			Log.d("伤害显示", "进入了受伤状态");
			iDamageCounts = 0;
			// body.applyForce(vecAntiGrav, body.getPosition());
			SpriteReq req = (SpriteReq) causedBy;
			boolean bAttackFromRight = Orientation.RIGHT == req.orientation;
			Vec2 v1 = body.getLinearVelocity();
			Vec2 v2 = new Vec2(bAttackFromRight ? vec2Right
					: vec2Left);
			body.applyLinearImpulse(
					v2.subLocal(v1)
							.mulLocal(body.getMass()),
					body.getPosition());
			system.getAudioManager().playSound(R.raw.sprite_heart,
					1, 1, 1);
		}
	}

	/**************************************************************************/

	private class FootContactLsn implements YIOnContactListener
	{
		private int iFootFloorContact;

		@Override
		public void beginContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther, Contact contact)
		{
			// XXX temp code
			// 目前框架实现为：domainOther为null时，表示碰到了地面（地图障碍物）
			// 之后可能有改动，会把地图实体的引用传过来
			if (null == domainOther
					|| domainOther.KEY.equals("map"))
			{
				++iFootFloorContact;
				if (fixtureOther.m_shape instanceof EdgeShape)
				{
					EdgeShape es = (EdgeShape) fixtureOther.m_shape;
					if (Math.abs(es.m_vertex2.x
							- es.m_vertex1.x) < 0.2)
						// 太陡峭视为碰到了竖直折线，不看做是地面
						return;
				}

				String objName = (String) fixtureOther
						.getUserData();
				if (Constants.FIXTURE_DEAD_LINE.equals(objName))
				{
					resetGame();
					return;
				}

				bOnLand = true;
				if (YSpriteState.JUMP == stateMachine
						.getCurrentState())
					resetState();
			}
		}

		@Override
		public void endContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther, Contact contact)
		{
			// XXX temp code
			// 目前框架实现为：domainOther为null时，表示碰到了地面（地图障碍物）
			// 之后可能有改动，会把地图实体的引用传过来
			if (null == domainOther
					|| domainOther.KEY.equals("map"))
			{
				--iFootFloorContact;
				if (iFootFloorContact == 0)
				{
					bOnLand = false;
					if (YSpriteState.JUMP != stateMachine
							.getCurrentState())
						stateMachine.forceSetState(YSpriteState.JUMP);
				}
			}
		}
	}

	private class MainContactLsn implements YIOnContactListener
	{
		private WorldManifold fold = new WorldManifold();

		@Override
		public void beginContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther, Contact contact)
		{
			if (null != domainOther
					&& domainOther.KEY.contains("destroy"))
			{
				if (stateMachine.getCurrentState() == YSpriteState.ATTACK1)
				{
					YDestructibleTerrainDomain dtd = (YDestructibleTerrainDomain) domainOther;
					contact.getWorldManifold(fold);
					for (Vec2 pointContact : fold.points)
						dtd.destroyCircle(
								pointContact.x,
								pointContact.y,
								0.3f);
				}
			}
		}

		@Override
		public void endContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther, Contact contact)
		{
			// TODO Auto-generated method stub

		}
	}

	private class RadarContactLsn implements YIOnContactListener
	{

		@Override
		public void beginContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther, Contact contact)
		{
			if (null != domainOther
					&& domainOther.KEY.contains("monster")
					&& !fixtureOther.isSensor())
			{// 与之碰撞的实体确实为怪物
				ifRightSide = fixtureOther.getBody()
						.getPosition().x > fixture
						.getBody().getPosition().x ? true
						: false;
				ifInRadar = true;
				monsterKey = domainOther.KEY;
			}
		}

		@Override
		public void endContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther, Contact contact)
		{
			ifInRadar = false;
			monsterKey = null;
		}
	}

	@Override
	public float[] getCurrentXY()
	{
		// TODO Auto-generated method stub
		return new float[]
		{ mover.getX(), mover.getY() };
	}

}
