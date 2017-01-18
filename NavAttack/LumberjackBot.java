package NavAttack;

import battlecode.common.*;

public class LumberjackBot extends BaseBot
{
    private static Direction bounce = here.directionTo(closetInitalAlliedArchonLocation());

    private static void wander() throws GameActionException
    {
        if(rc.readBroadcast(NUM_LUMBERJACKS_CHANNEL) < 3)
        {
            RobotInfo[] friendlies = rc.senseNearbyRobots(-1,us);

        }
        here = rc.getLocation();
        Direction dir = here.directionTo(closetInitalAlliedArchonLocation());
        if (!rc.hasMoved())
        {
            RobotInfo robotInfo[] = rc.senseNearbyRobots(-1,us);
            for(RobotInfo i : robotInfo){
                if(i.getType() == RobotType.GARDENER){
                    dir = here.directionTo(i.location);
                }
            }
            if (!rc.canMove(bounce))
            {
                if(Math.random() > 0.6)
                    bounce = dir.rotateLeftDegrees((float)((Math.random()*180) - 90));
                else
                    bounce = bounce.opposite();

            } else
                tryMove(bounce);
        }

    }
    public static void runLumberjack() throws GameActionException
    {

        while(true)
        {
            try
            {
//                here = rc.getLocation();
//                bugNav(closetInitalEnemyArchonLocation());
                respondToBroadCasts();
                here = rc.getLocation();
                visibleEnemies = rc.senseNearbyRobots(-1,them);
                while (visibleEnemies.length > 0)
                {
                    if (rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS,them).length>0 && rc.canStrike())
                    {
                        rc.strike();
                    }
                    else if(!rc.hasMoved())
                    {
                        tryMove(here.directionTo(visibleEnemies[0].location));
                    }
                    here = rc.getLocation();
                    visibleEnemies = rc.senseNearbyRobots(-1,them);
                }
                TreeInfo[] info = rc.senseNearbyTrees(-1,Team.NEUTRAL);
                if (info.length > 0)
                {
                    for(TreeInfo i : info)
                    {
                        if(i.containedRobot != null)
                        {
                            if(rc.canChop(i.ID))
                            {
                                rc.chop(i.ID);
                            }
                            else
                                if(!rc.hasMoved())
                                tryMove(here.directionTo(i.location));
                        }
                    }
                    if(rc.canShake(info[0].ID))
                    {
                        rc.shake(info[0].ID);
                    }
                    if (rc.canChop(info[0].ID))
                    {
                        rc.chop(info[0].ID);
                    }
                    else
                    {
                        if(!rc.hasMoved())
                            tryMove(here.directionTo(info[0].location));
                    }
                }
                else{
                    wander();
                }
                if(rc.getTeamBullets() > 800)
                {
                    rc.donate(250);
                }
                rc.broadcast(NUM_LUMBERJACKS_CHANNEL,rc.getRobotCount());
                Clock.yield();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static boolean respondToBroadCasts() throws GameActionException
    {
        here = rc.getLocation();
        boolean friendlyGardenerUnderAttack = rc.readBroadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL) == 1;
        boolean friendlyArchonUnderAttack = rc.readBroadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL) == 1;
        if(friendlyGardenerUnderAttack)
        {
            MapLocation friendlyGardenerLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_GARDENER_X)),Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_GARDENER_Y)));
            if(here.isWithinDistance(friendlyGardenerLocation,6f))
            {
                if(rc.senseNearbyRobots(-1,them).length == 0)
                    rc.broadcast(FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL,0);
            }
            else
                tryMove(here.directionTo(friendlyGardenerLocation));
            return true;
        }
        else if(friendlyArchonUnderAttack)
        {
            MapLocation friendlyArchonLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_X)),Float.intBitsToFloat(rc.readBroadcast(FRIENDLY_ARCHON_Y)));
            if(here.isWithinDistance(friendlyArchonLocation,RobotType.SOLDIER.sensorRadius))
            {
                if(rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadius,them).length == 0)
                {
                    rc.broadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL, 0);
                    rc.broadcast(URGENTLY_NEED_LUMBERJACKS_CHANNEL,0);
                }
            }
            else
                tryMove(here.directionTo(friendlyArchonLocation));
            return true;
        }
        return false;
    }
}
