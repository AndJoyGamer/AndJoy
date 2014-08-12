package game.AndJoy.monster.concrete;

import game.AndJoy.monster.YAMonsterDomainLogic;
import game.AndJoy.monster.YIMonsterStateClocker;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;

enum YMonsterState implements YIMonsterStateClocker
{
	WAIT("待机"), WALK("行走"), ATTACK1("攻击1"),DAMAGE("受伤"),DEAD("死亡");

	private YIMonsterStateClocker clocker;
	private String strName;

	private YMonsterState(String strName)
	{
		this.strName = strName;
	}

	void setStateClocker(YIMonsterStateClocker clocker)
	{
		this.clocker = clocker;
	}

	@Override
	public void onClock(float fElapseTime_s,
			YAMonsterDomainLogic<?> domainLogicContext,
			YSystem system, YScene sceneCurrent)
	{
		clocker.onClock(fElapseTime_s, domainLogicContext, system,
				sceneCurrent);
	}

	@Override
	public String toString()
	{
		return strName;
	}

}
