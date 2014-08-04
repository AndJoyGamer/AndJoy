package game.AndJoy.monster;

import ygame.framework.core.YScene;
import ygame.framework.core.YSystem;

public interface YIMonsterStateClocker
{
	void onClock(float fElapseTime_s, YAMonsterDomainLogic<?> domainLogicContext,
			YSystem system, YScene sceneCurrent);
}
