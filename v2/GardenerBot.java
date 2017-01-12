package v2;

import battlecode.common.*;

public class GardenerBot extends BaseBot
{
    private static Direction prevDirection = here.directionTo(closetInitalEnemyArchonLocation());

    private static Direction getNextDirection(Direction prevDirection){
        return prevDirection.rotateLeftDegrees(60);
    }

    private static int howManyTreesCanBePlanted(MapLocation location){
        int howMany = 0;
        Direction dir = location.directionTo(closetInitalEnemyArchonLocation());
        for(int i=0;i<=360;i+= 60){
            if(rc.canPlantTree(dir.rotateLeftDegrees(i))){
                howMany++;
            }
        }
        return howMany;
    }

    private static void wander(int i) throws GameActionException{
        if(i<10)
            tryMove(Direction.getNorth());
        if(i>=10 && i< 20)
            tryMove(Direction.getWest());
        if(i>=20 && i< 40)
            tryMove(Direction.getSouth());
        if(i>=40 && i<= 50)
            tryMove(Direction.getEast());

    }

    public static void runGardener() throws GameActionException
    {
        try
        {
            for (int i = 50; i-- > 0; )
            {
                if(!rc.hasMoved())
                    wander(i);
                here = rc.getLocation();
                if (howManyTreesCanBePlanted(here) >= 5)
                {
                    break ;
                }
            }
        }catch (GameActionException e){
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
                if (rc.hasTreeBuildRequirements() && rc.canPlantTree(dir))
                {
                    rc.plantTree(dir);
                    prevDirection = dir;
                }
//                else
//                {
//                    if(!rc.hasMoved())
//                        wander((int)(Math.random()*60));
////                    dir = randomDirection();
////                    if (rc.canBuildRobot(RobotType.LUMBERJACK, dir))
////                    {
////                        rc.buildRobot(RobotType.LUMBERJACK, dir);
////                    }
//                }
                visibleAlliedTrees = rc.senseNearbyTrees(1.5f,us);
                if(visibleAlliedTrees.length>0)
                {
                    for(int i=visibleAlliedTrees.length; i-->0;)
                    {
                        //if(visibleAlliedTrees[i].getHealth() < 30)
                        //{
                            rc.water(visibleAlliedTrees[i].getID());
                        //}
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
