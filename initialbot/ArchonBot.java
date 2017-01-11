package initialbot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class ArchonBot extends BaseBot
{
    public static void runArchon() throws GameActionException
    {
        System.out.println("I'm an archon!");
        while (true)
        {
            try
            {
                updateRobotInfos();
                if (bulletsInSenseRadius.length > 0)
                {
                    for (int i = bulletsInSenseRadius.length - 1; i-- > 0; )
                    {
                        // Get relevant bullet information
                        Direction propagationDirection = bulletsInSenseRadius[i].dir;
                        MapLocation bulletLocation = bulletsInSenseRadius[i].location;

                        // Calculate bullet relations to this robot
                        Direction directionToRobot = bulletLocation.directionTo(here);
                        float theta = propagationDirection.radiansBetween(directionToRobot);
                        float distToRobot = bulletLocation.distanceTo(here);
                        float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

                        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
                        if (Math.abs(theta) < Math.PI / 2 && perpendicularDist <= rc.getType().bodyRadius)
                        {
                            if (rc.canMove(directionToRobot.getWest()))
                            {
                                tryMove(directionToRobot.getWest());
                            } else if (rc.canMove(directionToRobot.getEast()))
                            {
                                tryMove(directionToRobot.getEast());
                            }
                            break;
                        }
                    }
                } else if (!rc.hasMoved() && visibleEnemies.length > 0)
                {
                    for (int i = visibleEnemies.length; i-- > 0; )
                    {
                        Direction enemyToUs = visibleEnemies[i].location.directionTo(here);
                        Direction usToEnemy = here.directionTo(visibleEnemies[i].location);
                        if (rc.canMove(usToEnemy))
                        {
                            tryMove(enemyToUs);
                            break;
                        }
                    }
                }
                else
                {
                    if (!rc.hasMoved() && rc.canMove(here.directionTo(centerOfOurInitialArchons)))
                        tryMove(here.directionTo(centerOfOurInitialArchons));
                }
                Direction dir = new Direction((float) Math.random() * 2 * (float) Math.PI);
                if (rc.canHireGardener(dir) && Math.random() * rc.getRoundNum() < rc.getRoundLimit() * 0.1)
                {
                    rc.hireGardener(dir);
                }
                if(rc.getTeamBullets()>310)
                {
                    rc.donate(10.0f);
                }
                Clock.yield();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
