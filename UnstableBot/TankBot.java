package UnstableBot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;

public class TankBot extends BaseBot
{

    private static Direction[] fireDirections = {Direction.getNorth(), Direction.getNorth().rotateRightDegrees(45),
    Direction.getEast(), Direction.getEast().rotateRightDegrees(45), Direction.getSouth(), Direction.getSouth().rotateRightDegrees(45),
    Direction.getWest(), Direction.getWest().rotateRightDegrees(45)};

    private static float senseRadius = rc.getType().sensorRadius;

    public static void runTank() throws GameActionException
    {
        while(true)
        {
            try
            {
                here = rc.getLocation();

                visibleEnemies = rc.senseNearbyRobots(-1,them);

                if(rc.canMove(closetInitalEnemyArchonLocation()))
                {
                    tryMove(here.directionTo(closetInitalEnemyArchonLocation()));
                }

                if(visibleEnemies.length>0 && rc.canFirePentadShot())
                {

                    int[] heatmap = new int[fireDirections.length];
                    int fireDirectionIndex = 0,enemiesInDirection;
                    int maxEnemies = 0;
                    for(int i=fireDirections.length; i-->0;)
                    {
                        enemiesInDirection= rc.senseNearbyRobots(here.add(fireDirections[i],senseRadius),senseRadius,them).length;
                        if(enemiesInDirection>maxEnemies)
                        {
                            fireDirectionIndex = i;
                        }
                    }
                    rc.firePentadShot(fireDirections[fireDirectionIndex]);
                }
                else
                {
                    if(visibleEnemies.length>0 && rc.canFireTriadShot())
                    {

                        int[] heatmap = new int[fireDirections.length];
                        int fireDirectionIndex = 0,enemiesInDirection;
                        int maxEnemies = 0;
                        for(int i=fireDirections.length; i-->0;)
                        {
                            enemiesInDirection= rc.senseNearbyRobots(here.add(fireDirections[i],senseRadius),senseRadius,them).length;
                            if(enemiesInDirection>maxEnemies)
                            {
                                fireDirectionIndex = i;
                            }
                        }
                        rc.fireTriadShot(fireDirections[fireDirectionIndex]);
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
