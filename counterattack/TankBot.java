package counterattack;

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
                while (visibleEnemies.length > 0)
                {
                    if (rc.canFirePentadShot())
                    {
                        rc.firePentadShot(rc.getLocation().directionTo(visibleEnemies[0].location));
                    }
                    else
                    if(!rc.hasMoved())
                    {
                        tryMove(here.directionTo(visibleEnemies[0].location));
                    }
                    here = rc.getLocation();
                    visibleEnemies = rc.senseNearbyRobots(-1,them);
                }
                if(!rc.hasMoved())
                    tryMove(here.directionTo(closetInitalEnemyArchonLocation()));
                if(rc.canFirePentadShot() && rc.readBroadcast(NUM_GARDENERS_CHANNEL) > 3 && rc.getTeamBullets() > 500)
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
