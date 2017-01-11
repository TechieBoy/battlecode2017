package initialbot;

import battlecode.common.*;

public class GardenerBot extends BaseBot
{

    public static void runGardener() throws GameActionException
    {
        System.out.println("I'm a gardener!");

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
                            if(rc.canBuildRobot(RobotType.SOLDIER,directionToRobot))
                            {
                                rc.buildRobot(RobotType.SOLDIER,directionToRobot);
                            }

                            if (rc.canMove(directionToRobot.getWest()))
                            {
                                tryMove(directionToRobot.getWest());
                            }
                            else if (rc.canMove(directionToRobot.getEast()))
                            {
                                tryMove(directionToRobot.getEast());
                            }
                            break;
                        }
                    }
                }
                else if (!rc.hasMoved() && visibleEnemies.length > 0)
                {
                    for (int i = visibleEnemies.length; i-->0; )
                    {
                        Direction enemyToUs = visibleEnemies[i].location.directionTo(here);
                        Direction usToEnemy = here.directionTo(visibleEnemies[i].location);
                        if (rc.canMove(usToEnemy))
                        {
                            tryMove(enemyToUs);
                            break;
                        }
                    }
                } else
                {
                    if (!rc.hasMoved() && rc.canMove(here.directionTo(centerOfOurInitialArchons)))
                        tryMove(here.directionTo(centerOfOurInitialArchons));
                }
                Direction dir = randomDirection();
                if (rc.hasTreeBuildRequirements() && rc.canPlantTree(dir))
                {
                    rc.plantTree(dir);
                }
                else
                {
                    dir = randomDirection();
                    if (rc.canBuildRobot(RobotType.SCOUT, dir))
                    {
                        rc.buildRobot(RobotType.SCOUT, dir);
                    }
                }
                visibleAlliedTrees = rc.senseNearbyTrees(1.5f,us);
                if(visibleAlliedTrees.length>0)
                {
                    for(int i=visibleAlliedTrees.length; i-->0;)
                    {
                        rc.water(visibleAlliedTrees[i].getID());
                        Clock.yield();
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
