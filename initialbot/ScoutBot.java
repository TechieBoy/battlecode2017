package initialbot;

import battlecode.common.*;

public class ScoutBot extends BaseBot
{
    static TreeInfo lastTreeSeen[] = new TreeInfo[50];
    static int curr = 0;
    static boolean haveBeenToCenter = false;
    public static void runScout() throws GameActionException
    {
        System.out.println("I'm a Scout!");
        while (true)
        {
            try
            {
                here = rc.getLocation();
                if (!haveBeenToCenter)
                {
                    if (here.distanceTo(centerOfAllInitialArchons) < 2.5)
                    {
                        haveBeenToCenter = true;
                        System.out.println("Center has come" + here.directionTo(centerOfAllInitialArchons));
                    } else
                    {
                        if(rc.canMove(here.directionTo(centerOfAllInitialArchons)) && !rc.hasMoved())
                            System.out.println(here.directionTo(centerOfAllInitialArchons));
                            tryMove(here.directionTo(centerOfAllInitialArchons));
                    }
                    visibleNeutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
                    if (visibleNeutralTrees.length > 0)
                    {
                        for (int i = visibleNeutralTrees.length; i-- > 0; )
                        {
                            for (int j = curr; i-- > 0; )
                            {
                                if (lastTreeSeen[j] != visibleNeutralTrees[i])
                                {
                                    if (curr >= 50)
                                        break;
                                    if(curr < 0)
                                        curr = 0;
                                    lastTreeSeen[curr] = visibleNeutralTrees[i];
                                    curr++;
                                }
                            }

                        }
                    }

                }
                top:
                if (here.distanceTo(centerOfAllInitialArchons) < 2.5)
                {
                    haveBeenToCenter = true;
                    TreeInfo info = lastTreeSeen[curr];
                    if (here.distanceTo(info.location) < info.getRadius())
                    {
                        visibleAllies = rc.senseNearbyRobots(-1, us);
                        for (int i = visibleAllies.length; i-- > 0; )
                        {
                            if (visibleAllies[i].getType() == RobotType.SCOUT)
                            {
                                curr--;
                                break top;
                            }
                        }
                        visibleEnemies = rc.senseNearbyRobots(-1, them);
                        if (visibleEnemies.length > 0)
                        {
                            rc.broadcast(0, (int) visibleEnemies[0].location.x);
                            rc.broadcast(0, (int) visibleEnemies[0].location.y);
                            rc.broadcast(0, 1);
                            if(rc.canFireSingleShot())
                                rc.fireSingleShot(here.directionTo(visibleEnemies[0].location));
                            Clock.yield();
                        }
                    } else
                    {
                        if(rc.canMove(here.directionTo(info.location)) && !rc.hasMoved())
                         tryMove(here.directionTo(info.location));
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
