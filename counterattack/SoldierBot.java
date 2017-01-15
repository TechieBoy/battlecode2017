package counterattack;

import battlecode.common.*;

import java.util.concurrent.Callable;

public class SoldierBot extends BaseBot
{

    public static void runSoldier() throws GameActionException
    {
        while (true)
        {
            try
            {
                here = rc.getLocation();
                visibleEnemies = rc.senseNearbyRobots(-1,them);
                if (visibleEnemies.length > 0)
                {
                    for(RobotInfo robotInfo : visibleEnemies)
                    {
                        if(robotInfo.getType() == RobotType.ARCHON)
                        {
                            if (rc.canFirePentadShot())
                            {
                                rc.firePentadShot(rc.getLocation().directionTo(robotInfo.location));
                            }

                        }
                        else if(robotInfo.getType() == RobotType.GARDENER)
                        {
                            if (rc.canFireTriadShot())
                            {
                                rc.fireTriadShot(rc.getLocation().directionTo(robotInfo.location));
                            }

                        }
                        else
                        {
                            if (rc.canFireSingleShot())
                            {
                                rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
                            }

                        }
                    }
                    here = rc.getLocation();
                }
                if(!rc.hasMoved())
                {
                    if(!respondToBroadCasts())
                    {
                        visibleEnemies = rc.senseNearbyRobots(-1,them);
                        if(visibleEnemies.length > 0)
                        {
                            if(rc.canMove(here.directionTo(visibleEnemies[0].location)))
                            {
                                tryMove(here.directionTo(visibleEnemies[0].location));
                            }
                        }
                        else
                        {
                            tryMove(here.directionTo(closetInitalEnemyArchonLocation()));
                        }
                    }
                }
                Clock.yield();

            } catch (Exception e)
            {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
}
