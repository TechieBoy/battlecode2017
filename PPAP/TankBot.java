package PPAP;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;

public class TankBot extends BaseBot
{

    private static Direction[] fireDirections = {Direction.getNorth(), Direction.getNorth().rotateRightDegrees(30),Direction.getNorth().rotateRightDegrees(60),
            Direction.getEast(), Direction.getEast().rotateRightDegrees(30),Direction.getEast().rotateRightDegrees(60), Direction.getSouth(), Direction.getSouth().rotateRightDegrees(30),
            Direction.getSouth().rotateRightDegrees(60), Direction.getWest(), Direction.getWest().rotateRightDegrees(30), Direction.getWest().rotateRightDegrees(60)};

    private static float senseRadius = rc.getType().sensorRadius;
    private static Direction bounce = here.directionTo(closetInitalEnemyArchonLocation());
    private static int enemyArchonNumber=0;

    public static void runTank() throws GameActionException
    {

        while(true)
        {
            try
            {
                here = rc.getLocation();

                visibleEnemies = rc.senseNearbyRobots(-1,them);
                if(visibleEnemies!=null && visibleEnemies.length>0)
                {
                    tryMove(here.directionTo(visibleEnemies[0].location));
                    visibleEnemies = rc.senseNearbyRobots(-1,them);
                }
                if(!rc.hasMoved())
                {
                    if(enemyArchonNumber>(numberOfInitialArchon-1))
                        wander();
                    else if(here.distanceTo(theirInitialArchonLocations[enemyArchonNumber])<rc.getType().bodyRadius)
                        tryMove(here.directionTo(theirInitialArchonLocations[enemyArchonNumber]));
                    else
                        enemyArchonNumber++;
                }

                if(visibleEnemies.length>0 && rc.canFirePentadShot())
                {
                    int fireDirectionIndex = 0,enemiesInDirection;
                    int maxEnemies = 0;
                    for(int i=fireDirections.length; i-->0;)
                    {
                        enemiesInDirection= rc.senseNearbyRobots(here.add(fireDirections[i],senseRadius/2),senseRadius/2,them).length;
                        if(enemiesInDirection>maxEnemies)
                        {
                            fireDirectionIndex = i;
                            maxEnemies = enemiesInDirection;
                        }
                    }
                    rc.firePentadShot(fireDirections[fireDirectionIndex]);
                }
                else if(visibleEnemies.length>0 && rc.canFireTriadShot())
                {

//                    int[] heatmap = new int[fireDirections.length];
                    int fireDirectionIndex = 0,enemiesInDirection;
                    int maxEnemies = 0;
                    for(int i=fireDirections.length; i-->0;)
                    {
                        enemiesInDirection= rc.senseNearbyRobots(here.add(fireDirections[i],senseRadius/2),senseRadius/2,them).length;
                        if(enemiesInDirection>maxEnemies)
                        {
                            fireDirectionIndex = i;
                            maxEnemies = enemiesInDirection;
                        }
                    }
                    rc.fireTriadShot(fireDirections[fireDirectionIndex]);
                }


                Clock.yield();

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }
    private static void wander() throws GameActionException
    {
        if (!rc.hasMoved())
        {
            if (!rc.canMove(bounce))
            {
                here = rc.getLocation();
                Direction dir = here.directionTo(closetInitalEnemyArchonLocation());
                bounce = dir.rotateLeftDegrees((float) ((Math.random() * 180) - 90));

            } else
                tryMove(bounce);
        }

    }
}
