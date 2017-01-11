package initialbot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class ScoutBot extends BaseBot
{

    public static void runScout() throws GameActionException
    {
        System.out.println("I'm a Scout!");
        while (true)
        {
            updateRobotInfos();
            Clock.yield();
        }
    }
}
