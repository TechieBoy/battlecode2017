package soldierjack;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class ArchonBot extends BaseBot
{


    public static void runArchon() throws GameActionException
    {
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
                Direction dir = rc.getLocation().directionTo(centerOfTheirInitialArchons).rotateLeftDegrees((float)(Math.random()*(180) - 90));
                if (rc.canHireGardener(dir) && gardenersPresentHaveDoneTheirJob() && rc.readBroadcast(NUM_GARDENERS_CHANNEL) < 14)
                {
                    rc.hireGardener(dir);
                    rc.broadcast(NUM_GARDENERS_CHANNEL, (rc.readBroadcast(NUM_GARDENERS_CHANNEL) + 1));
                }
                if (rc.getTeamBullets() > 310)
                {
                    rc.donate(100f);
                }
                Clock.yield();
            } catch (Exception e)
            {
                e.printStackTrace();
            }


        }
    }

    private static boolean gardenersPresentHaveDoneTheirJob() throws GameActionException
    {
        int numGardeners = rc.readBroadcast(NUM_GARDENERS_CHANNEL);
        int numTrees = rc.readBroadcast(NUM_TREES_CHANNEL);
        if (numGardeners == 0)
            return true;
        else return (numTrees/numGardeners >= 3);

    }
}
