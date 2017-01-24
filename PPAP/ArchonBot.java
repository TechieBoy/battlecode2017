package PPAP;

import battlecode.common.*;

import static PPAP.Messaging.*;

public class ArchonBot extends BaseBot
{
    private static int lastGardenerSpawnRound = 0;
    public static void runArchon() throws GameActionException
    {
        while (true)
        {
            try
            {
                lastGardenerSpawnRound = rc.readBroadcast(LAST_GARDENER_SPAWNED_CHANNEL);
                updateRobotInfos();
                callForHelpIfInDanger();
                tryDodge();
                Direction dir = null;
                here = rc.getLocation();
                Direction d = here.directionTo(centerOfTheirInitialArchons);
                RobotInfo friendlies[] = rc.senseNearbyRobots(-1, us);
                if(friendlies.length > 0)
                {
                    for (RobotInfo i : friendlies)
                    {
                        if (i.getType() == RobotType.GARDENER)
                        {
                            Direction hereToTheirArchon = here.directionTo(centerOfTheirInitialArchons);
                            Direction hereToOurGardener = here.directionTo(i.location);
                            float degreesBetweenGardenerAndArchon = hereToTheirArchon.degreesBetween(hereToOurGardener);
                            if (Math.abs(degreesBetweenGardenerAndArchon) > 30)
                            {
                                dir = hereToTheirArchon.rotateLeftDegrees(degreesBetweenGardenerAndArchon / 2);
                            }
                        }
                    }
                }
                else
                {
                    for (int i = -120; i <= 120; i++)
                    {
                        dir = d.rotateLeftDegrees(i);
                        if (rc.canHireGardener(dir))
                        {
                            break;
                        }
                        else
                            dir = null;
                    }
                }
                if(dir == null)
                    dir = rc.getLocation().directionTo(centerOfTheirInitialArchons).rotateLeftDegrees((float) (Math.random() * (180) - 90));
                    if (rc.canHireGardener(dir) && isViableToSpawnGardeners())
                    {
                        rc.hireGardener(dir);
                        rc.broadcast(LAST_GARDENER_SPAWNED_CHANNEL,rc.getRoundNum());
                        int curr_gardeners = rc.readBroadcast(NUM_GARDENERS_CHANNEL);
                        rc.broadcast(NUM_GARDENERS_CHANNEL,(curr_gardeners+1));
                        if(curr_gardeners % 4 == 0)
                        {
                            rc.broadcast(NEED_TANK_GARDENER_CHANNEL,1);
                        }
                        else
                        {
                            rc.broadcast(NEED_TANK_GARDENER_CHANNEL,0);
                        }

                    }
                    if (rc.getTeamBullets() > 1000)
                    {
                        rc.donate(200f);
                    }
                    visibleAlliedTrees = rc.senseNearbyTrees(-1, us);
                    friendlies = rc.senseNearbyRobots(-1, us);
                    here = rc.getLocation();
                    for(RobotInfo i : friendlies)
                    {
                        if(i.getType() == RobotType.GARDENER)
                        {
                            Direction oppToGardener = i.location.directionTo(here);
                            if(rc.canMove(oppToGardener) && !rc.hasMoved()){
                                tryMove(oppToGardener);
                            }
                        }
                    }
                    if(visibleAlliedTrees.length > 0)
                    {
                        here = rc.getLocation();
                        TreeInfo firstTree = visibleAlliedTrees[0];
                        Direction oppToFirstTree = firstTree.location.directionTo(here);
                        if(rc.canMove(oppToFirstTree) && !rc.hasMoved()){
                            if(!tryMove(oppToFirstTree))
                            {
                                tryMove(here.directionTo(centerOfAllInitialArchons));
                            }
                        }
                    }
                    Clock.yield();
                } catch(Exception e)
                {
                    e.printStackTrace();
                }


            }
        }

    private static void tryDodge() throws GameActionException
        {
            if (bulletsInSenseRadius.length > 0)
            {
                for (int i = bulletsInSenseRadius.length; i-- > 0; )
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
                        if (rc.canMove(directionToRobot.rotateLeftDegrees(90)))
                        {
                            tryMove(directionToRobot.rotateLeftDegrees(90));
                        } else if (rc.canMove(directionToRobot.rotateRightDegrees(90)))
                        {
                            tryMove(directionToRobot.rotateRightDegrees(90));
                        }
                        break;
                    }
                }
            } else if (!rc.hasMoved() && visibleEnemies.length > 0)
            {
                here = rc.getLocation();
                RobotInfo firstEnemy = visibleEnemies[0];
                Direction oppToFirstEnemy = firstEnemy.location.directionTo(here);
                if(rc.canMove(oppToFirstEnemy) && !rc.hasMoved()){
                    tryMove(oppToFirstEnemy);
                }
            }
        }

        private static void callForHelpIfInDanger() throws GameActionException
        {
            RobotInfo attackers[] = rc.senseNearbyRobots(5f, them);
            if(rc.getHealth() < RobotType.ARCHON.maxHealth/1.35 && attackers.length > 0)
            {
                rc.broadcast(FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL,1);
                rc.broadcast(URGENTLY_NEED_LUMBERJACKS_CHANNEL,1);
                rc.broadcast(FRIENDLY_ARCHON_X,Float.floatToIntBits(rc.getLocation().x));
                rc.broadcast(FRIENDLY_ARCHON_Y,Float.floatToIntBits(rc.getLocation().y));
            }

        }


    private static boolean isViableToSpawnGardeners() throws GameActionException
    {
        int numGardeners = rc.readBroadcast(NUM_GARDENERS_CHANNEL);
        int numTrees = rc.getTreeCount();
        if(rc.getTeamBullets() > 500)
            return true;
        if(rc.getRoundNum() < 90 && numGardeners == 1)
            return false;
        else if(rc.getTeamBullets() > RobotType.GARDENER.bulletCost*1.2)
        {
            if (numGardeners < 2 || numTrees == 0  || rc.getRobotCount() - numberOfInitialArchon < 3 )
                return true;
            else return (numTrees / numGardeners >= 3);
        }
        int numRound = rc.getRoundNum();
        return (numRound - rc.readBroadcast(LAST_GARDENER_SPAWNED_CHANNEL) > 20) && (rc.getTeamBullets()>RobotType.SOLDIER.bulletCost*1.2);
    }
}
