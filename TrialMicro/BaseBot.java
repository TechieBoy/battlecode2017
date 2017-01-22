package TrialMicro;

import battlecode.common.*;


public class BaseBot {

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
        return tryMove(dir,20,5);
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

    public static void bugNav(MapLocation target) throws GameActionException
    {
        if(!rc.hasMoved())
        {
            Direction dir = here.directionTo(target);
            if (rc.canMove(dir) && motionType == 'd')
            {
                rc.setIndicatorLine(here,here.add(dir,20f),0,255,0);
                rc.move(dir);
            }
            else
            {
                visibleNeutralTrees = rc.senseNearbyTrees(rc.getType().sensorRadius/1.5f, Team.NEUTRAL);
                if (visibleNeutralTrees.length > 1)
                {
                    Direction minDirection = null;
                    Direction bugDirection = null;
                    float minangle = 360;
                    for(int i = visibleNeutralTrees.length; i-->1;)
                    {
                        bugDirection = visibleNeutralTrees[0].location.directionTo(visibleNeutralTrees[i].location);
                        if(Math.abs(dir.degreesBetween(bugDirection))<minangle)
                        {
                            minDirection = bugDirection;
                            minangle = Math.abs(dir.degreesBetween(bugDirection));
                        }
                    }
                    rc.setIndicatorLine(here,here.add(minDirection,20f),0,0,255);
                    tryMove(minDirection);
                }
                else
                {
                    rc.setIndicatorLine(here,here.add(dir,20f),255,0,0);
                    tryMove(dir);
                }
            }
        }
    }

}
