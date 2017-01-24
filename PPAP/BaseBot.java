package PPAP;

import battlecode.common.*;


public class BaseBot
{

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
        centerOfOurInitialArchons = new MapLocation(0, 0);
        centerOfTheirInitialArchons = new MapLocation(0, 0);
        centerOfAllInitialArchons = new MapLocation(0, 0);
        for (MapLocation a : ourInitialArchonLocations)
        {
            centerOfOurInitialArchons = FastMath.addVec(centerOfOurInitialArchons, a);
        }
        for (MapLocation a : theirInitialArchonLocations)
        {
            centerOfTheirInitialArchons = FastMath.addVec(centerOfTheirInitialArchons, a);
        }
        centerOfAllInitialArchons = FastMath.addVec(centerOfOurInitialArchons, centerOfTheirInitialArchons);
        centerOfOurInitialArchons = FastMath.multiplyVec(1.0 / (double) numberOfInitialArchon, centerOfOurInitialArchons);
        centerOfTheirInitialArchons = FastMath.multiplyVec(1.0 / (double) numberOfInitialArchon, centerOfTheirInitialArchons);
        centerOfAllInitialArchons = FastMath.multiplyVec(0.5 / (double) numberOfInitialArchon, centerOfAllInitialArchons);
        here = rc.getLocation();

        //prevDirection = here.directionTo(centerOfTheirInitialArchons);
        //currentMotionType = motionType.direct;
    }


    public static MapLocation closetInitalEnemyArchonLocation()
    {
        MapLocation ret = null;
        float minDistSq = Float.MAX_VALUE;
        for (int i = theirInitialArchonLocations.length; i-- > 0; )
        {
            float distSq = here.distanceSquaredTo(theirInitialArchonLocations[i]);
            if (distSq < minDistSq)
            {
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
        for (int i = ourInitialArchonLocations.length; i-- > 0; )
        {
            float distSq = here.distanceSquaredTo(ourInitialArchonLocations[i]);
            if (distSq < minDistSq)
            {
                minDistSq = distSq;
                ret = ourInitialArchonLocations[i];
            }
        }
        return ret;
    }


    public static void updateRobotInfos()
    {
        visibleAllies = rc.senseNearbyRobots(-1, us);
        visibleEnemies = rc.senseNearbyRobots(-1, them);
        visibleAlliedTrees = rc.senseNearbyTrees(-1, us);
        visibleEnemyTrees = rc.senseNearbyTrees(-1, them);
        visibleNeutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        bulletsInSenseRadius = rc.senseNearbyBullets();
    }

    public static boolean tryMove(Direction dir) throws GameActionException
    {
        return tryMove(dir, 10, 10);
    }

    public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException
    {

        // First, try intended direction
        if (rc.canMove(dir) && !rc.hasMoved())
        {
            rc.move(dir);
            return true;
        }

        int currentCheck = 1;

        if (!rc.hasMoved())
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
        return new Direction((float) Math.random() * 2 * (float) Math.PI);
    }

    private static char motionType = 'd';//direct,bug
    private static Direction bugDirection = randomDirection();
    private static TreeInfo[] allNearbyTrees = null;
    private static TreeInfo myTree = null;

    public static void bugNav(MapLocation target) throws GameActionException
    {
        if (target != null && !rc.hasMoved())
        {
            Direction dir = here.directionTo(target);

            if (motionType == 'd')
            {
                allNearbyTrees = rc.senseNearbyTrees(here.add(dir, myType.strideRadius * 2), myType.strideRadius * 2.718f, null);
                if (tryMove(dir))
                {
                    rc.setIndicatorLine(here, here.add(dir, 20f), 0, 255, 0);
                }
                else
                {
                    motionType = 'b';
                    if (allNearbyTrees.length > 1)
                    {
                        if (dir.degreesBetween(allNearbyTrees[0].location.directionTo(allNearbyTrees[1].location)) < dir.degreesBetween(allNearbyTrees[1].location.directionTo(allNearbyTrees[0].location)))
                        {
                            bugDirection = allNearbyTrees[0].location.directionTo(allNearbyTrees[1].location);
                            myTree = allNearbyTrees[0];
                        }
                        else
                        {
                            bugDirection = allNearbyTrees[1].location.directionTo(allNearbyTrees[0].location);
                            myTree = allNearbyTrees[0];
                        }
                        tryMove(bugDirection);
                        rc.setIndicatorLine(here, here.add(bugDirection, 20f), 0, 0, 255);
                        rc.setIndicatorDot(allNearbyTrees[0].location, 0, 0, 0);
                        rc.setIndicatorDot(allNearbyTrees[1].location, 255, 255, 255);
                    }
                    else if (allNearbyTrees.length == 1)
                    {
                        if (here.y > allNearbyTrees[0].location.y)
                            bugDirection = here.directionTo(allNearbyTrees[0].location).rotateRightDegrees(90);
                        else
                            bugDirection = here.directionTo(allNearbyTrees[0].location).rotateLeftDegrees(90);
                        tryMove(bugDirection);
                        myTree = allNearbyTrees[0];
                        rc.setIndicatorLine(here, here.add(bugDirection, 20f), 0, 0, 255);
                        rc.setIndicatorDot(allNearbyTrees[0].location, 0, 0, 0);
                    }
                    else
                    {
                        //I M STUCK;
                        tryMove(dir, 5, 30);
                        motionType = 'd';
                    }
                }
            }
            else
            {
                allNearbyTrees = rc.senseNearbyTrees(here.add(bugDirection, myType.strideRadius * 2), myType.strideRadius * 2, null);

                if (rc.canMove(dir) && rc.senseNearbyTrees(here.add(dir, 3 * myType.strideRadius), myType.bodyRadius, null).length == 0)
                {
                    motionType = 'd';
                    tryMove(dir);
                    rc.setIndicatorLine(here, here.add(dir, 20f), 255, 25, 255);
                    return;
                }

                if (tryMove(bugDirection, 2, 20))
                {
                    rc.setIndicatorLine(here, here.add(bugDirection, 20f), 0, 0, 255);
                }
                else
                {
                    if (allNearbyTrees.length > 1 && myTree != null)
                    {
                        int i = 0, j;
                        while (i < allNearbyTrees.length && allNearbyTrees[i] != myTree)
                        {
                            i++;
                        }
                        j = i + 1;
                        while (j < allNearbyTrees.length && allNearbyTrees[j] != myTree)
                        {
                            j++;
                        }

                        if (i == allNearbyTrees.length || j == allNearbyTrees.length || i == j)
                        {
                            bugDirection = allNearbyTrees[0].location.directionTo(allNearbyTrees[1].location);
                            myTree = allNearbyTrees[0];
                            tryMove(bugDirection);
                            rc.setIndicatorLine(here, here.add(bugDirection, 20f), 255, 0, 0);
                            rc.setIndicatorDot(allNearbyTrees[0].location, 0, 0, 0);
                        }
                        else
                        {
                            if (bugDirection.degreesBetween(allNearbyTrees[i].location.directionTo(allNearbyTrees[j].location)) < bugDirection.degreesBetween(allNearbyTrees[j].location.directionTo(allNearbyTrees[i].location)))
                            {
                                bugDirection = allNearbyTrees[i].location.directionTo(allNearbyTrees[j].location);
                                myTree = allNearbyTrees[i];
                            }
                            else
                            {
                                bugDirection = allNearbyTrees[j].location.directionTo(allNearbyTrees[i].location);
                                myTree = allNearbyTrees[i];
                            }
                            tryMove(bugDirection);
                            rc.setIndicatorLine(here, here.add(bugDirection, 20f), 255, 0, 0);
                            rc.setIndicatorDot(allNearbyTrees[i].location, 0, 0, 0);
                            rc.setIndicatorDot(allNearbyTrees[j].location, 0, 0, 0);
                        }
                    }
                    else if (allNearbyTrees.length == 1)
                    {
                        if (here.y > allNearbyTrees[0].location.y)
                            bugDirection = here.directionTo(allNearbyTrees[0].location).rotateRightDegrees(90);
                        else
                            bugDirection = here.directionTo(allNearbyTrees[0].location).rotateLeftDegrees(90);
                        tryMove(bugDirection);
                        myTree = allNearbyTrees[0];
                        rc.setIndicatorDot(allNearbyTrees[0].location, 0, 0, 0);
                        rc.setIndicatorLine(here, here.add(bugDirection, 20f), 255, 0, 0);
                    }
                    else
                    {
                        //I M STUCK;
                        tryMove(dir, 5, 30);
                        motionType = 'd';
                        rc.setIndicatorLine(here, here.add(dir, 20f), 255, 0, 0);
                    }
                }
            }
        }
    }


}
