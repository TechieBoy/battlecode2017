package rankedbot;

import battlecode.common.*;

public class ArchonBot extends BaseBot
{
    static int c = 0;

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
                if (rc.readBroadcast(URGENTLY_NEED_GARDENERS_CHANNEL) == 1)
                {
                    while (true)
                    {
                        Direction gardenerDir = Direction.getEast();
                        for (int i = 0; i < 360; i += 10)
                        {
                            if (rc.canHireGardener(gardenerDir))
                            {
                                rc.hireGardener(gardenerDir);
                                rc.broadcast(NUM_GARDENERS_CHANNEL, (rc.readBroadcast(NUM_GARDENERS_CHANNEL) + 1));
                            }
                        }

                        if (rc.readBroadcast(NUM_GARDENERS_CHANNEL) >= 3)
                        {
                            rc.broadcast(URGENTLY_NEED_GARDENERS_CHANNEL, 0);
                            break;
                        }
                    }
                }
                Direction dir = null;
                Direction d = Direction.getEast();
                for (int i = 0; i < 360; i += 10)
                {
                    dir = d.rotateLeftDegrees(i);
                    if (rc.canHireGardener(dir))
                    {
                        break;
                    }
                }
                if(dir == null)
                    dir = rc.getLocation().directionTo(centerOfTheirInitialArchons).rotateLeftDegrees((float) (Math.random() * (180) - 90));
                    if ((rc.canHireGardener(dir) && (gardenersPresentHaveDoneTheirJob() && rc.readBroadcast(NUM_GARDENERS_CHANNEL) < 14)) || ((rc.canHireGardener(dir) && rc.readBroadcast(NUM_LUMBERJACKS_CHANNEL) >= 80)))
                    {
                        rc.hireGardener(dir);
                        rc.broadcast(NUM_GARDENERS_CHANNEL, (rc.readBroadcast(NUM_GARDENERS_CHANNEL) + 1));
                    }
                    if (rc.getTeamBullets() > 310)
                    {
                        rc.donate(100f);
                    }
                    Clock.yield();
                } catch(Exception e)
                {
                    e.printStackTrace();
                }


            }
        }

        private static Direction[] gardenerSpawnDirections () {
        Direction[] spawn = new Direction[50];
        Direction dir = Direction.getEast();
        for (int i = 0; i < 360; i += 10)
        {
            Direction n = dir.rotateLeftDegrees(i);
            if (rc.canHireGardener(n))
            {
                spawn[c] = n;
                c++;
            }
        }
        return spawn;
    }

    private static boolean gardenersPresentHaveDoneTheirJob() throws GameActionException
    {
        int numGardeners = rc.readBroadcast(NUM_GARDENERS_CHANNEL);
        int numTrees = rc.readBroadcast(NUM_TREES_CHANNEL);
        if (numGardeners == 0)
            return true;
        else return (numTrees / numGardeners >= 2.5);

    }
}
