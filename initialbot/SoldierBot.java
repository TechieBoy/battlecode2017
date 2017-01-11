package initialbot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class SoldierBot extends BaseBot
{

    public static void runSoldier() throws GameActionException
    {
        System.out.println("I'm an soldier!");

        while (true)
        {
            Clock.yield();
        }
    }
}
