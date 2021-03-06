package game.AndJoy.sprite.concrete;

import ygame.extension.domain.sprite.YASpriteDomainLogic;
import ygame.extension.domain.sprite.YIStateClocker;
import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;

enum YSpriteState implements YIStateClocker
{
	WAIT("待机"), WALK("行走"), JUMP("跳跃"), ATTACK1("攻击1"),DAMAGE("受伤");

	private YIStateClocker clocker;
	private String strName;

	private YSpriteState(String strName)
	{
		this.strName = strName;
	}

	void setStateClocker(YIStateClocker clocker)
	{
		this.clocker = clocker;
	}

	@Override
	public void onClock(float fElapseTime_s,
			YASpriteDomainLogic domainLogicContext,
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
