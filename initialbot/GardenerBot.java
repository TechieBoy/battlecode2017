package initialbot;

import battlecode.common.*;

public class GardenerBot extends BaseBot
{
    static int myLumberJacks;
    static int myTrees;
    static int mySoldiers;
    static int myScouts;

    public static void runGardener() throws GameActionException
    {
        System.out.println("I'm a gardener!");
        System.out.println(GameConstants.BROADCAST_MAX_CHANNELS);

        for(int i=10; i-->0;)
        {
            here = rc.getLocation();
            tryMove(centerOfAllInitialArchons.directionTo(here));
        }

        while (true)
        {
            try
            {
                updateRobotInfos();
                here = rc.getLocation();
                checkIfAtWar();

                if(!atWar)
                {
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
                                if (rc.canBuildRobot(RobotType.SOLDIER, directionToRobot))
                                {
                                    rc.buildRobot(RobotType.SOLDIER, directionToRobot);
                                }

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
                    }
                    else if (!rc.hasMoved() && visibleEnemies.length > 0)
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
                    Direction dir = randomDirection();
                    if (rc.hasTreeBuildRequirements() && rc.canPlantTree(dir))
                    {
                        rc.plantTree(dir);
                    }
                    else if (Math.random() > 0.8)
                    {
                        dir = randomDirection();
                        if (rc.canBuildRobot(RobotType.LUMBERJACK, dir))
                        {
                            rc.buildRobot(RobotType.LUMBERJACK, dir);
                        }
                    }
                    else
                    {
                        dir = randomDirection();
                        if (rc.canBuildRobot(RobotType.SOLDIER, dir))
                        {
                            rc.buildRobot(RobotType.SOLDIER, dir);
                        }
                    }
                    visibleAlliedTrees = rc.senseNearbyTrees(1.5f, us);
                    if (visibleAlliedTrees.length > 0)
                    {
                        for (int i = visibleAlliedTrees.length; i-- > 0; )
                        {
                            if (rc.canWater(visibleAlliedTrees[i].getID()))
                            {
                                rc.water(visibleAlliedTrees[i].getID());
                                Clock.yield();
                            }
                        }
                    }
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
