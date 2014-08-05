package game.AndJoy.monster.concrete;

import game.AndJoy.MainActivity;
import game.AndJoy.R;
import game.AndJoy.monster.YAMonsterDomainLogic;
import game.AndJoy.monster.YIMonsterStateClocker;
import game.AndJoy.sprite.YASpriteDomainLogic;
import game.AndJoy.sprite.YIStateClocker;
import game.AndJoy.sprite.concrete.YSpriteDomain;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import ygame.extension.with_third_party.YIOnContactListener;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.state_machine.StateMachine;
import ygame.state_machine.YIAction;
import ygame.state_machine.builder.YStateMachineBuilder;
import ygame.texture.YTileSheet;


class YMonsterLogic extends YAMonsterDomainLogic<YMonsterDomain>
{
	private float fFrames;
	private MainActivity activity;
	private Vec2 vecAntiGrav;
	private YSpriteDomain domainSprite;

	protected YMonsterLogic(World world, MainActivity activity, YSpriteDomain domainSprite) {
		super(new YTileSheet(R.drawable.hero_big, activity.getResources(), 3,
				22), 13, world);
		// fInitX_M = 200;
		fInitX_M = (44 - 128) * 5 + 80;
		// for FeiKuai
		// fInitX_M = -60;
		fInitY_M = 20;
		this.activity = activity;
		vecAntiGrav = new Vec2(world.getGravity());
		this.domainSprite = domainSprite;
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
		def.density = 1;
		def.friction = 0f;
		def.restitution = 0f;
		def.userData = "main";
		final float fBodySideLen = fSkeletonSideLen * 0.5f;
		CircleShape shapeBody = new CircleShape();
		shapeBody.setRadius(fBodySideLen / 2);
		def.shape = shapeBody;
		body.createFixture(def);

		// XXX
		// 足部感应器（foot）
		PolygonShape shapeFoot = new PolygonShape();
		shapeFoot.setAsBox(fBodySideLen / 4, fBodySideLen / 10,
				new Vec2(0, -fBodySideLen / 2), 0);
		def.shape = shapeFoot;
		def.friction = 0.5f;
		def.density = 10;
		def.isSensor = true;
		body.createFixture(def);

		// 感应行走雷达

		CircleShape shapeRadar1 = new CircleShape();
		shapeRadar1.setRadius(fBodySideLen * 3);

		def.isSensor = true;
		def.friction = 0f;
		def.density = 0f;
		def.shape = shapeRadar1;
		Fixture fixtureRadar1 = body.createFixture(def);
		fixtureRadar1.setOnContactListener(new Radar1ContactLsn());
		
		// 感应攻击雷达
		CircleShape shapeRadar2 = new CircleShape();
		shapeRadar2.setRadius(fBodySideLen);

		def.isSensor = true;
		def.friction = 0f;
		def.density = 0f;
		def.shape = shapeRadar2;
		Fixture fixtureRadar2 = body.createFixture(def);
		fixtureRadar2.setOnContactListener(new Radar2ContactLsn());
		
		
		vecAntiGrav.mulLocal(body.getMass() * 5);
	}

	@Override
	protected boolean onDealRequest(YRequest request, YSystem system,
			YScene sceneCurrent)
	{
		if (request.iKEY == domainContext.TO_WALK.iKEY)
		{
			Vec2 vec2 = new Vec2(body.getLinearVelocity());
			vec2.x = bRight ? 10 : -10;
			body.applyLinearImpulse(
					vec2.subLocal(body.getLinearVelocity())
							.mulLocal(body.getMass()),
					body.getPosition());
		}
		return super.onDealRequest(request, system, sceneCurrent);
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
	protected YIMonsterStateClocker designStateMachine(
			YStateMachineBuilder<YIMonsterStateClocker, YRequest, YAMonsterDomainLogic<?>> builder)
	{
		YMonsterState.WAIT.setStateClocker(new WaitClocker());
		YMonsterState.WALK.setStateClocker(new WalkClocker());
		YMonsterState.ATTACK1.setStateClocker(new Attack1Cloker());

		// 待机到行走
		builder.newTransition().from(YMonsterState.WAIT)
				.to(YMonsterState.WALK)
				.on(domainContext.TO_WALK);
		// 行走到待机
		builder.newTransition().from(YMonsterState.WALK)
				.to(YMonsterState.WAIT)
				.on(domainContext.TO_WAIT);

		// 待机到攻1
		// 行走到攻1
		builder.newTransition().from(YMonsterState.WAIT)
				.to(YMonsterState.ATTACK1)
				.on(domainContext.TO_ATTACK1);
		builder.newTransition().from(YMonsterState.WALK)
				.to(YMonsterState.ATTACK1)
				.on(domainContext.TO_ATTACK1);
		builder.onEntry(YMonsterState.ATTACK1).perform(
				new AttackEnterAction());
		//攻1到行走
		builder.newTransition().from(YMonsterState.ATTACK1).to(YMonsterState.WALK).on(domainContext.TO_WALK);
		return YMonsterState.WAIT;
	}

	@Override
	protected void confirmOrientation()
	{
	}

	private void resetState()
	{
		if (activity.bLeftPressing || activity.bRightPressing)
			stateMachine.forceSetState(YMonsterState.WALK);
		else
			stateMachine.forceSetState(YMonsterState.WAIT);
	}

	/******************************** 各状态之详细描述 *****************************************/
	/******************************** 基础 *****************************************/
	private class BaseClocker implements YIMonsterStateClocker
	{
		final private int iFPS;
		final private int iFrameNum;
		final private int iColStartIndex;
		final private int iRowStartIndex;

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
				YAMonsterDomainLogic<?> domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			int iFrame = (int) ((fFrames += fElapseTime_s * iFPS) % iFrameNum);
			iRowIndex = iRowStartIndex;
			iColumnIndex = iColStartIndex + iFrame;
			body.applyForce(vecAntiGrav, body.getPosition());
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
				YAMonsterDomainLogic<?> domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			bLockOrientation = false;
			super.onClock(fElapseTime_s, domainLogicContext,
					system, sceneCurrent);
			body.setLinearVelocity(vec2);
		}
	}

	/*************************** 关于行走状态 **********************************/
	private class WalkClocker extends BaseClocker
	{
		private final Vec2 vecRight = new Vec2(30, -30);
		private final Vec2 vecLeft = new Vec2(-30, -30);

		WalkClocker()
		{
			super(8, 6, 4, 0);
		}

		@Override
		public void onClock(float fElapseTime_s,
				YAMonsterDomainLogic<?> domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			bLockOrientation = false;
			super.onClock(fElapseTime_s, domainLogicContext,
					system, sceneCurrent);
			Vec2 vec2 = bRight ? vecRight : vecLeft;
			body.setLinearVelocity(vec2);
		}
	}

	/*************************** 关于攻击1状态 **********************************/
	private class Attack1Cloker implements YIMonsterStateClocker {
		// private int[] i_arrFrameIndex =
		// { 23, 24, 25, 26, 27, 28, 29, 19, 22, 58 };
		// private int[] i_arrFrameIndex =
		// { 17, 18, };
		private int[] i_arrFrameIndex = { 23, 24, 25, 20, 21, 1, 2, 3, 4 };

		@Override
		public void onClock(float fElapseTime_s,
				YAMonsterDomainLogic<?> domainLogicContext, YSystem system,
				YScene sceneCurrent) {
			int iFrame = (int) ((fFrames += fElapseTime_s * 10) % i_arrFrameIndex.length);

			iRowIndex = (i_arrFrameIndex[iFrame] - 1) / 22;
			iColumnIndex = (i_arrFrameIndex[iFrame] - 1) % 22;
			body.applyForce(vecAntiGrav, body.getPosition());
		}
	}

	private class AttackEnterAction implements
	YIAction<YIMonsterStateClocker, YRequest, YAMonsterDomainLogic<?>> {

		@Override
		public void onTransition(
				YIMonsterStateClocker from,
				YIMonsterStateClocker to,
				YRequest causedBy,
				YAMonsterDomainLogic<?> context,
				StateMachine<YIMonsterStateClocker, YRequest, YAMonsterDomainLogic<?>> stateMachine){
			
		}
	}

	private class Radar1ContactLsn implements YIOnContactListener {

		@Override
		public void beginContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			System.out.println("进入雷达范围");
			if (fixtureOther.m_userData == "foot") {
				if (fixtureOther.getBody().getPosition().x < fixture.getBody()
						.getPosition().x)
					bRight = false;
				else
					bRight = true;
				domainContext.sendRequest(domainContext.TO_WALK);

			}

		}

		@Override
		public void endContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			System.out.println("离开雷达范围");
			if (fixtureOther.m_userData == "foot") {
				if (fixtureOther.getBody().getPosition().x < fixture.getBody()
						.getPosition().x)
					bRight = false;
				else
					bRight = true;
				domainContext.sendRequest(domainContext.TO_WAIT);

			}
		}
	}

	private class Radar2ContactLsn implements YIOnContactListener {

		@Override
		public void beginContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			System.out.println("进入攻击范围");
			if (fixtureOther.m_userData == "foot") {
				if (fixtureOther.getBody().getPosition().x < fixture.getBody()
						.getPosition().x)
					bRight = false;
				else
					bRight = true;
				domainContext.sendRequest(domainContext.TO_ATTACK1);
				domainSprite.sendRequest(domainSprite.TO_DAMAGE);
			}

		}

		@Override
		public void endContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			System.out.println("离开攻击范围");
			if (fixtureOther.m_userData == "foot") {
				if (fixtureOther.getBody().getPosition().x < fixture.getBody()
						.getPosition().x)
					bRight = false;
				else
					bRight = true;
				domainContext.sendRequest(domainContext.TO_WALK);

			}
		}
	}
}
