package game.AndJoy.monster.concrete;

import game.AndJoy.MainActivity;
import game.AndJoy.R;
import game.AndJoy.common.Constants;
import game.AndJoy.monster.YAMonsterDomainLogic;
import game.AndJoy.monster.YIMonsterStateClocker;
import game.AndJoy.sprite.concrete.YSpriteDomain;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import ygame.common.YConstants.Orientation;
import ygame.domain.YDomain;
import ygame.extension.domain.YProgressBarDomain;
import ygame.extension.with_third_party.YIOnContactListener;
import ygame.framework.core.YABaseDomain;
import ygame.framework.core.YRequest;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;
import ygame.framework.domain.YBaseDomain;
import ygame.framework.domain.YWriteBundle;
import ygame.math.YMatrix;
import ygame.state_machine.StateMachine;
import ygame.state_machine.YIAction;
import ygame.state_machine.builder.YStateMachineBuilder;
import ygame.texture.YTileSheet;


class YMonsterLogic extends YAMonsterDomainLogic<YMonsterDomain>
{
	private int iDamageCounts;
	private float fFrames;
	private boolean ifOnLand = false;
	private Vec2 vecAntiGrav;
	private YSystem system;
	//是否在雷达1范围内
	private boolean ifInRadar1;
	//是否在雷达2范围内
	private boolean ifInRadar2;
	
	private int hp = 100;
	
	final YIMonsterStateClocker wait = new WaitClocker();
	final WalkClocker walk = new WalkClocker();
	final Attack1Cloker attack1 = new Attack1Cloker();
	final DamageClocker damage = new DamageClocker();
	final DeadClocker dead = new DeadClocker();
	final private String keyHP;

	protected YMonsterLogic(World world, String keyHP, MainActivity activity, float fInitX_M, float fInitY_M) {
		super(new YTileSheet(R.drawable.hero_big, activity.getResources(), 3,22), 2.5f, world);
		this.fInitX_M = fInitX_M;
		this.fInitY_M = fInitY_M;
		vecAntiGrav = new Vec2(world.getGravity());
		this.keyHP = keyHP;
	}
	
	@Override
	protected void onAttach(YSystem system, YBaseDomain domainContext)
	{
		super.onAttach(system, domainContext);
		this.system = system;
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
		def.userData = "monsterMain";
		final float fBodySideLen = fSkeletonSideLen * 0.7f;
		CircleShape shapeBody = new CircleShape();
		shapeBody.setRadius(fBodySideLen / 2.5f);
		def.shape = shapeBody;
		body.createFixture(def);

		// XXX
		// 足部感应器（foot）
		PolygonShape shapeFoot = new PolygonShape();
		shapeFoot.setAsBox(fBodySideLen / 6, fBodySideLen / 10,
				new Vec2(0, -fBodySideLen / 2f), 0);
		def.shape = shapeFoot;
		def.friction = 0.5f;
		def.density = 10;
		def.isSensor = true;
		body.createFixture(def).setOnContactListener(new FootContactLsn());

		// 感应行走雷达
		CircleShape shapeRadar1 = new CircleShape();
		shapeRadar1.setRadius(fBodySideLen * 3f);

		def.isSensor = true;
		def.friction = 0f;
		def.density = 0f;
		def.shape = shapeRadar1;
		Fixture fixtureRadar1 = body.createFixture(def);
		fixtureRadar1.setOnContactListener(new Radar1ContactLsn());
		
		// 感应攻击雷达
		CircleShape shapeRadar2 = new CircleShape();
		shapeRadar2.setRadius(fBodySideLen/2);

		def.isSensor = true;
		def.friction = 0f;
		def.density = 0f;
		def.shape = shapeRadar2;
		Fixture fixtureRadar2 = body.createFixture(def);
		fixtureRadar2.setOnContactListener(new Radar2ContactLsn());
		
		
		vecAntiGrav.mulLocal(-body.getMass());
	}

	@Override
	protected boolean onDealRequest(YRequest request, YSystem system,
			YScene sceneCurrent , YBaseDomain domain)
	{
		if (request.iKEY == domainContext.TO_WALK.iKEY)
		{
			Vec2 vec2 = new Vec2(body.getLinearVelocity());
			vec2.x = bRight ? 1 : -1;
			body.applyLinearImpulse(
					vec2.subLocal(body.getLinearVelocity())
							.mulLocal(body.getMass()),
					body.getPosition());
		}
		return super.onDealRequest(request, system, sceneCurrent , domain);
	}
	
	@Override
	protected void onCycle(double dbElapseTime_s, YDomain domainContext,
			YWriteBundle bundle, YSystem system,
			YScene sceneCurrent, YMatrix matrix4pv,
			YMatrix matrix4Projection, YMatrix matrix4View)
	{
		super.onCycle(dbElapseTime_s, domainContext, bundle, system, sceneCurrent,
				matrix4pv, matrix4Projection, matrix4View);
		YProgressBarDomain domainHP = (YProgressBarDomain) system.queryDomainByKey(keyHP);
		if(null != domainHP)
		{
			Vec2 pos = body.getPosition();
			domainHP.setPosition(pos.x, pos.y + fSkeletonSideLen * 0.6f / 2 + 0.1f, 0.2f);
			domainHP.setProgress(1 - hp / 100.0f);
		}
		// 5.hp为0时移除实体
		if (hp == 0)
		{
			stateMachine.forceSetState(dead);
			system.getCurrentScene().removeDomains(keyHP);
		}
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
		// 待机到行走
		builder.newTransition().from(wait)
				.to(walk)
				.on(domainContext.TO_WALK);
		// 行走到待机
		builder.newTransition().from(walk)
				.to(wait)
				.on(domainContext.TO_WAIT);

		// 待机到攻1
		// 行走到攻1
		builder.newTransition().from(wait)
				.to(attack1)
				.on(domainContext.TO_ATTACK1);
		builder.newTransition().from(walk)
				.to(attack1)
				.on(domainContext.TO_ATTACK1);
		builder.onEntry(attack1).perform(
				new AttackEnterAction());
		//攻1到行走
		//builder.newTransition().from(attack1).to(walk).on(domainContext.TO_WALK);
		
		//行走到受伤
		builder.newTransition().from(walk).to(damage).on(domainContext.TO_DAMAGE);
		//攻1到受伤
		builder.newTransition().from(attack1).to(damage).on(domainContext.TO_DAMAGE);
		//受伤到行走
		builder.newTransition().from(damage).to(walk).on(domainContext.TO_WALK);
		builder.onEntry(damage).perform(new DamageEnterAction());
		
		//各种状态到死亡
		builder.newTransition().fromAll().to(dead).on(domainContext.TO_DEAD);
		builder.onEntry(dead).perform(new DeadEnterAction());
		return wait;
	}

	@Override
	protected void confirmOrientation()
	{
	}

	private void resetState()
	{
		if (ifInRadar2)
			stateMachine.forceSetState(attack1);
		else if(ifInRadar1)
			stateMachine.forceSetState(walk);
		else
			stateMachine.forceSetState(wait);
	}
	
	@Override
	public String toString()
	{
		return domainContext.KEY;
	}

	/******************************** 各状态之详细描述 *****************************************/
	/******************************** 基础 *****************************************/
	private class BaseClocker implements YIMonsterStateClocker
	{
		final int iFPS;
		final int iFrameNum;
		final int iColStartIndex;
		final int iRowStartIndex;

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
			if (!ifOnLand)
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
			if(!ifOnLand){
				body.applyLinearImpulse(new Vec2(0,-body.getMass()), body.getPosition());
			}else{
				body.setLinearVelocity(vec2);
			}
		}
	}

	/*************************** 关于行走状态 **********************************/
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
//		 private int[] i_arrFrameIndex =
//		 { 23, 24, 25, 26, 27, 28, 29, 19, 22, 58 };
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
			
			if(3 == iFrame)
			{
				YSpriteDomain sprite = (YSpriteDomain) system
						.queryDomainByKey(Constants.SPRITE);
				if (null != sprite)
					sprite.damage(bRight ? Orientation.RIGHT
							: Orientation.LEFT);
			}
			if(8 == iFrame){
				resetState();
			}
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
			fFrames = 0;
		}
	}

	/*************************** 关于受伤状态 **********************************/
	private class DamageClocker extends BaseClocker
	{
		DamageClocker()
		{
			super(0, 1, 10, 0);
		}
		
		@Override
		public void onClock(float fElapseTime_s,
				YAMonsterDomainLogic<?> domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			super.onClock(fElapseTime_s, domainLogicContext, system, sceneCurrent);
			hp--;
			if(iDamageCounts ++ > 30)
				resetState();
		}
	}
	
	private class DamageEnterAction implements YIAction<YIMonsterStateClocker, YRequest, YAMonsterDomainLogic<?>>
	{

		@Override
		public void onTransition(
				YIMonsterStateClocker from,
				YIMonsterStateClocker to,
				YRequest causedBy,
				YAMonsterDomainLogic<?> context,
				StateMachine<YIMonsterStateClocker, YRequest, YAMonsterDomainLogic<?>> stateMachine) {
			// TODO Auto-generated method stub
			iDamageCounts = 0;
		}
	}
	
	/*************************** 关于死亡状态 **********************************/
	private class DeadClocker extends BaseClocker
	{
		DeadClocker()
		{
			super(2, 4, 10, 0);
		}
		
		@Override
		public void onClock(float fElapseTime_s,
				YAMonsterDomainLogic<?> domainLogicContext,
				YSystem system, YScene sceneCurrent)
		{
			int iFrame = (int) ((fFrames += fElapseTime_s * iFPS) % iFrameNum);
			iRowIndex = iRowStartIndex;
			iColumnIndex = iColStartIndex + iFrame;
			if (13 == iColumnIndex)
			{
				system.getCurrentScene().removeDomains(
						domainContext.KEY);
				world.destroyBody(body);
			}
//			world = null;
		}
	}
	
	private class DeadEnterAction implements YIAction<YIMonsterStateClocker, YRequest, YAMonsterDomainLogic<?>>
	{
		@Override
		public void onTransition(
				YIMonsterStateClocker from,
				YIMonsterStateClocker to,
				YRequest causedBy,
				YAMonsterDomainLogic<?> context,
				StateMachine<YIMonsterStateClocker, YRequest, YAMonsterDomainLogic<?>> stateMachine) {
			fFrames = 0;
			bLockOrientation = true;
//			Vec2 v1 = body.getLinearVelocity();
//			Vec2 v2 = new Vec2(!bRight ? vec2Right
//					: vec2Left);
//			body.applyLinearImpulse(
//					v2.subLocal(v1)
//							.mulLocal(body.getMass()),
//					body.getPosition());
		}
	}
	
	
	private class Radar1ContactLsn implements YIOnContactListener {

		@Override
		public void beginContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			if (null != domainOther
					&& domainOther.KEY
							.equals(Constants.SPRITE))
			{// 与之碰撞的实体确实为精灵
//				if (fixtureOther.m_userData == "foot")
				{
					if (fixtureOther.getBody()
							.getPosition().x < fixture
							.getBody()
							.getPosition().x)
						bRight = false;
					else
						bRight = true;
					domainContext.sendRequest(domainContext.TO_WALK);
					ifInRadar1 = true;
				}
			}

		}

		@Override
		public void endContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			if (null != domainOther
					&& domainOther.KEY
							.equals(Constants.SPRITE))
			{// 与之碰撞的实体确实为精灵
//				if (fixtureOther.m_userData == "foot")
				{
					if (fixtureOther.getBody()
							.getPosition().x < fixture
							.getBody()
							.getPosition().x)
						bRight = false;
					else
						bRight = true;
					domainContext.sendRequest(domainContext.TO_WAIT);
					ifInRadar1 = false;
				}
			}
		}
	}

	private class Radar2ContactLsn implements YIOnContactListener {

		@Override
		public void beginContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			if (null != domainOther && domainOther.KEY.equals(Constants.SPRITE))
			{//与之碰撞的实体确实为精灵
//				YSpriteDomain sprite = (YSpriteDomain) domainOther;
//				if (fixtureOther.m_userData == "foot")
				{
					if (fixtureOther.getBody()
							.getPosition().x < fixture
							.getBody()
							.getPosition().x)
						bRight = false;
					else
						bRight = true;
					domainContext.sendRequest(domainContext.TO_ATTACK1);
					ifInRadar2 = true;
					
//					YSpriteDomain sprite = (YSpriteDomain) system
//							.queryDomainByKey(Constants.SPRITE);
//					if (null != sprite)
//						sprite.damage(bRight ? Orientation.RIGHT
//								: Orientation.LEFT);
					// domainSprite.sendRequest(domainSprite.TO_DAMAGE);
//					sprite.damage(bRight ? Orientation.RIGHT : Orientation.LEFT);
				}
			}

		}

		@Override
		public void endContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			if (null != domainOther
					&& domainOther.KEY
							.equals(Constants.SPRITE))
			{// 与之碰撞的实体确实为精灵
//				if (fixtureOther.m_userData == "foot")
				{
					if (fixtureOther.getBody()
							.getPosition().x < fixture
							.getBody()
							.getPosition().x)
						bRight = false;
					else
						bRight = true;
					domainContext.sendRequest(domainContext.TO_WALK);
					ifInRadar2 = false;
				}
			}
		}
	}
	
	private class FootContactLsn implements YIOnContactListener{

		private int iFootContact;
		@Override
		public void beginContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			//XXX temp code
			//目前框架实现为：domainOther为null时，表示碰到了地面（地图障碍物）；
			//之后可能有改动，会把地图实体的引用传过来
			if(null == domainOther || "map".equals(domainOther.KEY))
			{
				iFootContact++;
				ifOnLand = true;
			}
		}

		@Override
		public void endContact(Fixture fixture, Fixture fixtureOther,
				YABaseDomain domainOther) {
			//XXX temp code
			//目前框架实现为：domainOther为null时，表示碰到了地面（地图障碍物）
			//之后可能有改动，会把地图实体的引用传过来
			if(null == domainOther || "map".equals(domainOther.KEY))
			{
				iFootContact--;
				if(iFootContact==0){
					ifOnLand=false;
				}
			}
		}
	}
}
