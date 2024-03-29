package v2;

import battlecode.common.*;

public class GardenerBot extends BaseBot
{
    private static final Direction HOLE_TOWARDS_ENEMY = here.directionTo(closetInitalEnemyArchonLocation());
    private static final int ROUND_SPAWNED = rc.getRoundNum();
    private static Direction prevDirection = Direction.getNorth().rotateRightDegrees(60);
    private static Direction bounce = here.directionTo(centerOfTheirInitialArchons);

    private static int treesBuiltByMe = 0;


    public static void runGardener() throws GameActionException
    {
        try
        {
            wander();
            while (true)
            {
                here = rc.getLocation();
                if ((howManyTreesCanBePlanted(here) >= 4 || rc.getRoundNum() - ROUND_SPAWNED > 60) && here.distanceTo(closetInitalAlliedArchonLocation()) > 5 )
                {
                    break;
                } else if (!rc.hasMoved())
                    wander();
            }
        } catch (GameActionException e)
        {
            e.printStackTrace();
        }

        while (true)
        {
            try
            {
                int numTreesAlreadyOnMap = rc.readBroadcast(NUM_TREES_CHANNEL);
                dodgeBulletsAndDefend();

                Direction dir = getNextDirection(prevDirection);
                here = rc.getLocation();
                rc.setIndicatorDot(here.add(dir,2),255,0,0);
                if(Math.abs(dir.degreesBetween(HOLE_TOWARDS_ENEMY)) >= 30)
                {
                    if (rc.hasTreeBuildRequirements() && (haveAllPreviousGardenersBuiltTheirTrees() || treesBuiltByMe < 5))
                    {
                        if(rc.canPlantTree(dir))
                        {
                                rc.plantTree(dir);
                                rc.broadcast(NUM_TREES_CHANNEL, numTreesAlreadyOnMap + 1);
                                treesBuiltByMe++;
                        }
                    }
                }
                else if(Math.abs(dir.degreesBetween(HOLE_TOWARDS_ENEMY)) < 30)
                {
                        if (rc.hasRobotBuildRequirements(RobotType.LUMBERJACK))
                        {
                            if (rc.canBuildRobot(RobotType.LUMBERJACK, dir))
                            {
                                rc.buildRobot(RobotType.LUMBERJACK, dir);
                            }
                        }

                }
                prevDirection = dir;

                visibleAlliedTrees = rc.senseNearbyTrees(1.5f, us);
                if (visibleAlliedTrees.length > 0)
                {
                    for (int i = visibleAlliedTrees.length; i-- > 0; )
                    {
                        if(rc.canWater(visibleAlliedTrees[i].getID()))
                        {
                            rc.water(visibleAlliedTrees[i].getID());
                            Clock.yield();
                        }
                    }
                }
                Clock.yield();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public static void dodgeBulletsAndDefend() throws GameActionException
    {
        bulletsInSenseRadius = rc.senseNearbyBullets();
        visibleEnemies = rc.senseNearbyRobots(-1,them);
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
    }


    private static Direction getNextDirection(Direction prev)
    {
        return prev.rotateLeftDegrees(60);
    }

    private static boolean haveAllPreviousGardenersBuiltTheirTrees() throws GameActionException
    {
        int numGardeners = rc.readBroadcast(NUM_GARDENERS_CHANNEL);
        int numTrees = rc.readBroadcast(NUM_TREES_CHANNEL);
        return (numTrees/numGardeners >= 4 || numGardeners == 0 || numTrees == 0);
    }

    private static int howManyTreesCanBePlanted(MapLocation location)
    {
        int howMany = 0;
        Direction dir = location.directionTo(closetInitalEnemyArchonLocation());
        for (int i = 0; i <= 360; i += 60)
        {
            if (rc.canPlantTree(dir.rotateLeftDegrees(i)) && rc.senseNearbyRobots(3.8f,us).length == 0 &&
                    rc.senseNearbyTrees(5f).length == 0)
            {
                howMany++;
            }
        }
        return howMany;
    }

    private static void wander() throws GameActionException
    {
        if (!rc.hasMoved())
        {
            if (!rc.canMove(bounce))
            {
                bounce = randomDirection();
            } else
                tryMove(bounce);
        }

    }
}
