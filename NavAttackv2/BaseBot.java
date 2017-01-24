package NavAttackv2;

import battlecode.common.*;


public class BaseBot {
    public static final int NUM_GARDENERS_CHANNEL = 0;
    public static final int NUM_LUMBERJACKS_CHANNEL = 1;
    public static final int NUM_SCOUTS_CHANNEL = 2;
    public static final int URGENTLY_NEED_LUMBERJACKS_CHANNEL = 3;
    public static final int EARLY_GAME_SCOUT_SPAWNED_CHANNEL = 5;
    public static final int SCOUT_FOUND_CONTAINED_TANK_CHANNEL = 6;
    public static final int CONTAINED_TANK_X_CHANNEL = 7;
    public static final int CONTAINED_TANK_Y_CHANNEL = 8;
    public static final int FOUND_ENEMY_ARCHON_CHANNEL = 9;
    public static final int ENEMY_ARCHON_X = 10;
    public static final int ENEMY_ARCHON_Y = 11;
    public static final int FOUND_ENEMY_GARDENER_CHANNEL = 12;
    public static final int ENEMY_GARDENER_X = 13;
    public static final int ENEMY_GARDENER_Y = 14;
    public static final int FRIENDLY_ARCHON_UNDER_ATTACK_CHANNEL = 15;
    public static final int FRIENDLY_ARCHON_X = 16;
    public static final int FRIENDLY_ARCHON_Y = 17;
    public static final int FRIENDLY_GARDENER_UNDER_ATTACK_CHANNEL = 18;
    public static final int FRIENDLY_GARDENER_X = 19;
    public static final int FRIENDLY_GARDENER_Y = 20;

    public static RobotController rc;
    public static MapLocation here;
    public static Team us;
    public static Team them;
    public static int myID;
    public static RobotType myType;


    public static int numberOfInitialArchon;

    public static MapLocation[] ourInitialArchonLocations;
    public static MapLocation[] theirInitialArchonLocations;

    public static MapLocation centerOfOurInitialArchons;
    public static MapLocation centerOfTheirInitialArchons;
    public static MapLocation centerOfAllInitialArchons;

    public static RobotInfo[] visibleEnemies = null;
    public static RobotInfo[] visibleAllies = null;

    public static TreeInfo[] visibleAlliedTrees = null;
    public static TreeInfo[] visibleEnemyTrees = null;
    public static TreeInfo[] visibleNeutralTrees = null;

    public static BulletInfo[] bulletsInSenseRadius = null;

    public static void init(RobotController myRC)
    {
        rc = myRC;
        us = rc.getTeam();
        them = us.opponent();
        myID = rc.getID();
        myType = rc.getType();
        ourInitialArchonLocations = rc.getInitialArchonLocations(us);
        theirInitialArchonLocations = rc.getInitialArchonLocations(them);
        numberOfInitialArchon = ourInitialArchonLocations.length;
        centerOfOurInitialArchons = new MapLocation(0,0);
        centerOfTheirInitialArchons = new MapLocation(0,0);
        centerOfAllInitialArchons = new MapLocation(0,0);
        for (MapLocation a : ourInitialArchonLocations)
        {
            centerOfOurInitialArchons = FastMath.addVec(centerOfOurInitialArchons, a);
        }
        for (MapLocation a : theirInitialArchonLocations)
        {
            centerOfTheirInitialArchons = FastMath.addVec(centerOfTheirInitialArchons, a);
        }
        centerOfAllInitialArchons = FastMath.addVec(centerOfOurInitialArchons, centerOfTheirInitialArchons);
        centerOfOurInitialArchons = FastMath.multiplyVec(1.0 / (double)numberOfInitialArchon, centerOfOurInitialArchons);
        centerOfTheirInitialArchons = FastMath.multiplyVec(1.0 / (double)numberOfInitialArchon, centerOfTheirInitialArchons);
        centerOfAllInitialArchons = FastMath.multiplyVec(0.5 / (double)numberOfInitialArchon, centerOfAllInitialArchons);
        here = rc.getLocation();

        //prevDirection = here.directionTo(centerOfTheirInitialArchons);
        //currentMotionType = motionType.direct;
    }



    public static MapLocation closetInitalEnemyArchonLocation()
    {
        MapLocation ret = null;
        float minDistSq = Float.MAX_VALUE;
        for (int i = theirInitialArchonLocations.length; i --> 0; ) {
            float distSq = here.distanceSquaredTo(theirInitialArchonLocations[i]);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                ret = theirInitialArchonLocations[i];
            }
        }
        return ret;
    }

    public static MapLocation closetInitalAlliedArchonLocation()
    {
        MapLocation ret = null;
        float minDistSq = Float.MAX_VALUE;
        for (int i = ourInitialArchonLocations.length; i --> 0; ) {
            float distSq = here.distanceSquaredTo(ourInitialArchonLocations[i]);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                ret = ourInitialArchonLocations[i];
            }
        }
        return ret;
    }

    public static void broadcastEnemyLocation(int statusFlag, int x, int y, RobotInfo robotInfo) throws GameActionException
    {
        here = rc.getLocation();
        rc.broadcast(statusFlag, 1);
        rc.broadcast(x, Float.floatToIntBits(robotInfo.location.x));
        rc.broadcast(y, Float.floatToIntBits(robotInfo.location.y));

    }

    public static void updateRobotInfos()
    {
        visibleAllies = rc.senseNearbyRobots(-1, us);
        visibleEnemies =  rc.senseNearbyRobots(-1, them);
        visibleAlliedTrees = rc.senseNearbyTrees(-1,us);
        visibleEnemyTrees = rc.senseNearbyTrees(-1,them);
        visibleNeutralTrees = rc.senseNearbyTrees(-1,Team.NEUTRAL);
        bulletsInSenseRadius = rc.senseNearbyBullets();
    }

    public static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,10,10);
    }

    public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir) && !rc.hasMoved()) {
            rc.move(dir);
            return true;
        }

        int currentCheck = 1;

        if(!rc.hasMoved())
        {

            while (currentCheck <= checksPerSide)
            {
                // Try the offset of the left side
                if (rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck)))
                {
                    rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
                    return true;
                }
                // Try the offset on the right side
                if (rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck)))
                {
                    rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
                    return true;
                }
                // No move performed, try slightly further
                currentCheck++;
            }
        }

        // A move never happened, so return false.
        return false;
    }

    static Direction randomDirection()
    {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    private static char motionType = 'd';//direct,bug
    private static Direction bugDirection = randomDirection();


    private static TreeInfo[] allNearbyTrees = null;

    public static void bugNav(MapLocation target) throws GameActionException
    {
        if(!rc.hasMoved())
        {
            here = rc.getLocation();
            Direction dir = here.directionTo(target);
            allNearbyTrees = rc.senseNearbyTrees();

            if(motionType == 'd')
            {
                if (canTryMove(dir,10))
                {
                    rc.setIndicatorLine(here, here.add(dir, 20f), 0, 255, 0);
                    tryMove(dir,1,10);
                }
                else
                {
                    if (allNearbyTrees !=null && allNearbyTrees.length>1)
                    {
                        bugDirection = dir;
                        float minAngle = Float.MAX_VALUE;
                        int minIndex = 0;
                        for (int i = allNearbyTrees.length; i-->1; )
                        {
                            if (Math.abs(bugDirection.degreesBetween(allNearbyTrees[0].location.directionTo(allNearbyTrees[i].location))) < minAngle && canTryMove(allNearbyTrees[0].location.directionTo(allNearbyTrees[i].location),15))
                            {
                                minAngle = Math.abs(dir.degreesBetween(allNearbyTrees[0].location.directionTo(allNearbyTrees[i].location)));
                                bugDirection = allNearbyTrees[0].location.directionTo(allNearbyTrees[i].location);
                                minIndex = i;
                            }
                            else if (Math.abs(bugDirection.degreesBetween(allNearbyTrees[i].location.directionTo(allNearbyTrees[0].location))) < minAngle && canTryMove(allNearbyTrees[i].location.directionTo(allNearbyTrees[0].location),15))
                            {
                                minAngle = Math.abs(dir.degreesBetween(allNearbyTrees[i].location.directionTo(allNearbyTrees[0].location)));
                                bugDirection = allNearbyTrees[i].location.directionTo(allNearbyTrees[0].location);
                                minIndex = i;
                            }

                        }
                        if(allNearbyTrees[0].location.distanceTo(allNearbyTrees[minIndex].location)>2.5*allNearbyTrees[0].getRadius())
                        {
                            bugDirection = bugDirection.rotateLeftDegrees(bugDirection.degreesBetween(dir)/2);
                        }
                        rc.setIndicatorLine(here, here.add(bugDirection, 20f), 255, 255, 255);
//                        rc.setIndicatorDot(allNearbyTrees[minIndex], 0, 0, 0);
//                        rc.setIndicatorDot(allNearbyTrees[0], 255, 0, 0);
                        tryMove(bugDirection);
                        motionType = 'b';
                    }
                    else
                    {
                        rc.setIndicatorLine(here, here.add(dir, 20f), 255, 0, 0);
                        tryMove(dir);
                    }
                }
            }
            else if(motionType == 'b')
            {
                if(canTryMove(dir,20))
                {
                    tryMove(dir);
                    motionType = 'd';
                    rc.setIndicatorLine(here, here.add(dir, 20f), 255, 0, 0);
                }
                else
                {
                    if(canTryMove(bugDirection,30))
                    {
                        tryMove(bugDirection,4,10);
                        rc.setIndicatorLine(here, here.add(bugDirection, 20f), 0, 0, 255);
                    }
                    else
                    {
                        if (allNearbyTrees !=null && allNearbyTrees.length>1)
                        {
                            bugDirection = dir;
                            float minAngle = Float.MAX_VALUE;
                            int minIndex = 0;
                            for (int i = allNearbyTrees.length; i-->1; )
                            {
                                if (Math.abs(dir.degreesBetween(allNearbyTrees[0].location.directionTo(allNearbyTrees[i].location))) < minAngle && canTryMove(allNearbyTrees[0].location.directionTo(allNearbyTrees[i].location),15))
                                {
                                    minAngle = Math.abs(dir.degreesBetween(allNearbyTrees[0].location.directionTo(allNearbyTrees[i].location)));
                                    bugDirection = allNearbyTrees[0].location.directionTo(allNearbyTrees[i].location);
                                    minIndex = i;
                                }
                                else if (Math.abs(dir.degreesBetween(allNearbyTrees[i].location.directionTo(allNearbyTrees[0].location))) < minAngle && canTryMove(allNearbyTrees[i].location.directionTo(allNearbyTrees[0].location),15))
                                {
                                    minAngle = Math.abs(dir.degreesBetween(allNearbyTrees[i].location.directionTo(allNearbyTrees[0].location)));
                                    bugDirection = allNearbyTrees[i].location.directionTo(allNearbyTrees[0].location);
                                    minIndex = i;
                                }
                            }
                            if(allNearbyTrees[0].location.distanceTo(allNearbyTrees[minIndex].location)>4*rc.getType().bodyRadius)
                            {
                                bugDirection = bugDirection.rotateLeftDegrees(bugDirection.degreesBetween(dir)/2);
                            }
                            rc.setIndicatorLine(here, here.add(bugDirection, 20f), 0, 0, 255);
//                            rc.setIndicatorDot(allNearbyTrees[minIndex], 0, 0, 0);
//                            rc.setIndicatorDot(allNearbyTrees[0], 255, 0, 0);
                            tryMove(bugDirection);
                            motionType = 'b';
                        }
                        else
                        {
                            tryMove(dir);
                            rc.setIndicatorLine(here,here.add(dir,20f),255,255,0);
                        }
                    }
                }
            }
        }
    }

    public static boolean canTryMove(Direction dir, int degrees)
    {
        for(int i=degrees; i-->0;)
        {
            if(rc.canMove(dir.rotateLeftDegrees(i)))
            {
                return true;
            }
            else if(rc.canMove(dir.rotateRightDegrees(i)))
            {
                return true;
            }
        }
        return false;
    }

}