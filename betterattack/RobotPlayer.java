package betterattack;

import battlecode.common.RobotController;

public strictfp class RobotPlayer extends BaseBot
{

    @SuppressWarnings("unused")
    public static void run(RobotController myRc) throws Exception
    {
        init(myRc);
        switch (rc.getType())
        {
            case ARCHON:
                ArchonBot.runArchon();
                break;
            case GARDENER:
                GardenerBot.runGardener();
                break;
            case SOLDIER:
                SoldierBot.runSoldier();
                break;
            case TANK:
                TankBot.runTank();
                break;
            case SCOUT:
                ScoutBot.runScout();
                break;
            case LUMBERJACK:
                LumberjackBot.runLumberjack();
                break;
            default:
                throw new Exception("How did we reach here?!");
        }
	}

}
