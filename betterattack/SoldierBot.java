package betterattack;

import battlecode.common.*;

public class SoldierBot extends BaseBot
{
    private static Direction bounce = here.directionTo(closetInitalEnemyArchonLocation());
    public static void runSoldier() throws GameActionException
    {
        while (true)
        {
            try
            {

                here = rc.getLocation();
                if(rc.readBroadcast(FOUND_ENEMY_ARCHON_CHANNEL) == 1){
                    MapLocation archonLocation = new MapLocation(ENEMY_ARCHON_X,ENEMY_ARCHON_Y);
                    rc.setIndicatorLine(here,archonLocation,255,0,0);
                    if(here.isWithinDistance(archonLocation,RobotType.SOLDIER.sensorRadius))
                    {
                        System.out.println("Soldier within distance");
                        RobotInfo allBots[] = rc.senseNearbyRobots(archonLocation,RobotType.SOLDIER.sensorRadius,them);
                        if(allBots.length > 0){
                            for(int i=allBots.length -1; i-->0;)
                            {
                                boolean foundArchon = false;
                                if(allBots[i].getType() == RobotType.ARCHON){
                                    foundArchon = true;
                                }
                                if(!foundArchon)
                                    rc.broadcast(FOUND_ENEMY_ARCHON_CHANNEL,0);
                            }
                        }
                        else
                        {
                            rc.broadcast(FOUND_ENEMY_ARCHON_CHANNEL,0);
                            System.out.println("Cleared global shit");
                        }
                    }
                }
                visibleEnemies = rc.senseNearbyRobots(-1,them);
                if (visibleEnemies.length > 0)
                {
                    for(RobotInfo robotInfo : visibleEnemies)
                    {
                        if(robotInfo.getType() == RobotType.ARCHON)
                        {
                            if (rc.canFirePentadShot())
                            {
                                rc.firePentadShot(rc.getLocation().directionTo(robotInfo.location));
                            }

                        }
                        else if(robotInfo.getType() == RobotType.GARDENER)
                        {
                            if (rc.canFireTriadShot())
                            {
                                rc.fireTriadShot(rc.getLocation().directionTo(robotInfo.location));
                            }

                        }
                        else
                        {
                            if (rc.canFireSingleShot())
                            {
                                rc.fireSingleShot(rc.getLocation().directionTo(robotInfo.location));
                            }

                        }
                    }
                    here = rc.getLocation();
                }
                if(!rc.hasMoved())
                {
                    if(!respondToBroadCasts())
                    {
                        visibleEnemies = rc.senseNearbyRobots(-1,them);
                        if(visibleEnemies.length > 0)
                        {
                            if(rc.canMove(here.directionTo(visibleEnemies[0].location)))
                            {
                                tryMove(here.directionTo(visibleEnemies[0].location));
                            }
                        }
                        else
                        {
                            wander();
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
    private static boolean respondToBroadCasts() throws GameActionException
    {
        here = rc.getLocation();
        boolean friendlyArchonUnderAttack = rc.readBroadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL) == 1;
        boolean foundEnemyGardener = rc.readBroadcast(FOUND_ENEMY_GARDENER_CHANNEL) == 1;
        boolean foundArchon = rc.readBroadcast(FOUND_ENEMY_ARCHON_CHANNEL) == 1;
        if(friendlyArchonUnderAttack)
        {
            MapLocation friendlyArchonLocation = new MapLocation((float)rc.readBroadcast(FRIENDLY_ARCHON_X),(int)rc.readBroadcast(FRIENDLY_ARCHON_Y));
            tryMove(here.directionTo(friendlyArchonLocation));
            return true;
        }
        else if(foundArchon)
        {
            MapLocation enemyArchonLocation = new MapLocation((float)rc.readBroadcast(ENEMY_ARCHON_X),(int)rc.readBroadcast(ENEMY_ARCHON_Y));
            tryMove(here.directionTo(enemyArchonLocation));
            return true;
        }
        else if(foundEnemyGardener)
        {
            MapLocation enemyGardenerLocation = new MapLocation((float)rc.readBroadcast(ENEMY_GARDENER_X),(int)rc.readBroadcast(ENEMY_GARDENER_Y));
            tryMove(here.directionTo(enemyGardenerLocation));
            return true;
        }
        return false;
    }
}
