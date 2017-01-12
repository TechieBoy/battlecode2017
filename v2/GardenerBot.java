package v2;

import battlecode.common.*;

public class GardenerBot extends BaseBot
{
    private static final Direction holeTowardsEnemy= here.directionTo(closetInitalEnemyArchonLocation());
    private static final int roundSpawned = rc.getRoundNum();
    private static Direction prevDirection = holeTowardsEnemy;
    private static Direction bounce = Direction.getWest();

    private static Direction getNextDirection(Direction prevDirection)
    {
        return prevDirection.rotateLeftDegrees(60);
    }

    private static int howManyTreesCanBePlanted(MapLocation location)
    {
        int howMany = 0;
        Direction dir = location.directionTo(closetInitalEnemyArchonLocation());
        for (int i = 0; i <= 360; i += 60)
        {

            if (rc.canPlantTree(dir.rotateLeftDegrees(i)) && rc.senseNearbyRobots(2.2f).length == 0 &&
                    rc.senseNearbyTrees(2.2f).length == 0)
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

    public static void runGardener() throws GameActionException
    {
        try
        {
            while (rc.getRoundNum() - roundSpawned < 20 )
            {
                here = rc.getLocation();
                if (howManyTreesCanBePlanted(here) >= 4)
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

                Direction dir = getNextDirection(prevDirection);
                if (rc.hasTreeBuildRequirements() && rc.canPlantTree(dir) && dir != holeTowardsEnemy)
                {
                    rc.plantTree(dir);
                    prevDirection = dir;
                }
                else if(rc.hasRobotBuildRequirements(RobotType.LUMBERJACK) && rc.canBuildRobot(RobotType.LUMBERJACK,dir) && dir==holeTowardsEnemy && rc.getRoundNum()>500)
                {
                    rc.buildRobot(RobotType.LUMBERJACK,dir);
                    prevDirection = dir;
                }
                visibleAlliedTrees = rc.senseNearbyTrees(1.5f, us);
                if (visibleAlliedTrees.length > 0)
                {
                    for (int i = visibleAlliedTrees.length; i-- > 0; )
                    {
                        rc.water(visibleAlliedTrees[i].getID());
                        Clock.yield();
                    }
                }
                Clock.yield();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
