package TryTanks;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class TankBot extends BaseBot
{

    public static void runTank() throws GameActionException
    {
        while(true)
        {
            try
            {
                here = rc.getLocation();
                visibleEnemies = rc.senseNearbyRobots(-1,them);
                if (visibleEnemies.length > 0)
                {
                    if(!rc.hasMoved())
                    {
                        tryMove(here.directionTo(visibleEnemies[0].location));
                    }
                    else if (rc.canFirePentadShot())
                    {
                        rc.firePentadShot(rc.getLocation().directionTo(visibleEnemies[0].location));
                    }

                }
                if(!rc.hasMoved())
                    tryMove(here.directionTo(closetInitalEnemyArchonLocation()));
                if(rc.canFirePentadShot()  && rc.getTeamBullets() > 555)
                {
                    rc.firePentadShot(here.directionTo(closetInitalEnemyArchonLocation()));
                }
                Clock.yield();

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }
}
