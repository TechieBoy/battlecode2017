package counterattack;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class SoldierBot extends BaseBot
{

    public static void runSoldier() throws GameActionException
    {
        while (true)
        {
            try
            {
                here = rc.getLocation();
                visibleEnemies = rc.senseNearbyRobots(-1,them);
                if (visibleEnemies.length > 0)
                {
                    if (rc.canFireSingleShot())
                    {
                        rc.fireSingleShot(rc.getLocation().directionTo(visibleEnemies[0].location));
                    }
                    else
                        if(!rc.hasMoved() && rc.canMove(here.directionTo(visibleEnemies[0].location)))
                        {
                            tryMove(here.directionTo(visibleEnemies[0].location));
                        }
                    here = rc.getLocation();
                    visibleEnemies = rc.senseNearbyRobots(-1,them);
                }
                if(!rc.hasMoved())
                {
                    respondToBroadCasts();
                }
                Clock.yield();

            } catch (Exception e)
            {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    private static void respondToBroadCasts() throws GameActionException
    {
        here = rc.getLocation();
        boolean friendlyArchonUnderAttack = rc.readBroadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL) == 1;
        if(friendlyArchonUnderAttack)
        {
            MapLocation archonLocation = new MapLocation((float)rc.readBroadcast(FRIENDLY_ARCHON_X),(int)rc.readBroadcast(FRIENDLY_ARCHON_Y));
            tryMove(here.directionTo(archonLocation));
            return;
        }
        boolean foundArchon = rc.readBroadcast(FOUND_ENEMY_ARCHON_CHANNEL) == 1;
        if(foundArchon)
        {
            MapLocation archonLocation = new MapLocation((float)rc.readBroadcast(ENEMY_ARCHON_X),(int)rc.readBroadcast(ENEMY_ARCHON_Y));
            tryMove(here.directionTo(archonLocation));
            return;
        }
        boolean foundGardener = rc.readBroadcast(FOUND_ENEMY_GARDENER_CHANNEL) == 1;
        if(foundGardener)
        {
            MapLocation gardenerLocation = new MapLocation((float)rc.readBroadcast(ENEMY_GARDENER_X),(int)rc.readBroadcast(ENEMY_GARDENER_Y));
            tryMove(here.directionTo(gardenerLocation));
            return;
        }
    }
}
