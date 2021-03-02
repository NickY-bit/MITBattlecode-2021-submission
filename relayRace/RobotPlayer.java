package firstBot;
import battlecode.common.*;
import java.util.*;

import java.util.Map;

//TODO
// - add bidding

public class RobotPlayer {
    static RobotController rc;
    static MapLocation rl;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static int slanderer = 0;
    static int turnCount;
    static int wanderDirection = (int)(Math.random() * directions.length);
    static int spawnI = 0;
    static int immobile = 0;
    static List<Integer> creationTurn = new List<Integer>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<Integer> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(Integer integer) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends Integer> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Integer> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public Integer get(int index) {
            return null;
        }

        @Override
        public Integer set(int index, Integer element) {
            return null;
        }

        @Override
        public void add(int index, Integer element) {

        }

        @Override
        public Integer remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @Override
        public ListIterator<Integer> listIterator() {
            return null;
        }

        @Override
        public ListIterator<Integer> listIterator(int index) {
            return null;
        }

        @Override
        public List<Integer> subList(int fromIndex, int toIndex) {
            return null;
        }
    };
    static final int SLANDERER_INF_FRAC = 2;
    static final int POLITICIAN_INF_FRAC = 16;
    static final int MUCKRAKER_INF_FRAC = 12;
    static final int BID_FRAC = 50;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        firstBot.RobotPlayer.rc = rc;

        turnCount = 0;

        while (true) {
            turnCount += 1;
            rl = rc.getLocation();
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        int sensorRadius = rc.getType().sensorRadiusSquared;
        Team enemy = rc.getTeam().opponent();
        RobotInfo[] enemyBots = rc.senseNearbyRobots(sensorRadius, enemy);
        RobotInfo[] friends = rc.senseNearbyRobots(sensorRadius, rc.getTeam());

        enemyBots = rc.senseNearbyRobots(sensorRadius, enemy);
        //if there are enemies nearby, then find the enemy with the largest constitution and create a politician with greater constitution
        if (enemyBots.length != 0) {
            int greatestCon = 20;
            for (RobotInfo eni : enemyBots) {
                if (eni.getConviction() > greatestCon) {
                    greatestCon = eni.getConviction();
                }
            }
            if (rc.getInfluence() / POLITICIAN_INF_FRAC > greatestCon * 2 && rc.canBuildRobot(RobotType.POLITICIAN, directions[spawnI], greatestCon * 2)) {
                rc.buildRobot(RobotType.POLITICIAN, directions[spawnI], greatestCon * 2);
                spawnI++;
            } else if (rc.canBuildRobot(RobotType.POLITICIAN, directions[spawnI], greatestCon + 1)) {
                rc.buildRobot(RobotType.POLITICIAN, directions[spawnI], greatestCon + 1);
                spawnI++;
            }
                /*
                flag definitions:
                1000001 - north
                1000002 - northeast
                1000003 - east
                1000004 - southeast
                1000005 - south
                1000006 - southwest
                1000007 - west
                1000008 - northwest
                 */
            Direction nearestEnemy = directionToNearestEnemy(enemyBots);
            switch (nearestEnemy) {
                case NORTH:
                    rc.setFlag(1000001);
                    break;
                case NORTHEAST:
                    rc.setFlag(1000002);
                    break;
                case NORTHWEST:
                    rc.setFlag(1000008);
                    break;
                case EAST:
                    rc.setFlag(1000003);
                    break;
                case WEST:
                    rc.setFlag(1000007);
                    break;
                case SOUTHEAST:
                    rc.setFlag(1000004);
                    break;
                case SOUTHWEST:
                    rc.setFlag(1000006);
                    break;
                case SOUTH:
                    rc.setFlag(1000005);
                    break;
            }
        }

        if (rc.getFlag(rc.getID()) != 0) {
            rc.setFlag(0);
        }
        //old system for keeping track of slanderers, does not keep track of "active" slanderers
        /* slanderer = 0;
        for (RobotInfo fri : friends) {
            if (fri.type == RobotType.SLANDERER) {
                slanderer++;
                break;
            }
        }*/

        if(creationTurn.size() != 0) {
            for (int turn : creationTurn) {
                if (turnCount - turn >= 50) {
                    slanderer--;
                    creationTurn.remove(turn);
                }
            }
        }

        if (slanderer < 8 || (rc.getInfluence() / SLANDERER_INF_FRAC > 320 && slanderer < 8)) {
            if (rc.getInfluence() / SLANDERER_INF_FRAC > 320) {
                if (rc.canBuildRobot(RobotType.SLANDERER, randomDirection(), 320)) {
                    rc.buildRobot(RobotType.SLANDERER, randomDirection(), 320);
                    creationTurn.add(turnCount);
                }
            } else if (rc.canBuildRobot(RobotType.SLANDERER, randomDirection(), rc.getInfluence() / SLANDERER_INF_FRAC)) {
                rc.buildRobot(RobotType.SLANDERER, randomDirection(), rc.getInfluence() / SLANDERER_INF_FRAC);
                creationTurn.add(turnCount);
            }
        }
        if (spawnI >= 0 && spawnI <= 5 && rc.getInfluence() > 400) {
            if (rc.canBuildRobot(RobotType.POLITICIAN, directions[spawnI], rc.getInfluence() / POLITICIAN_INF_FRAC)) {
                rc.buildRobot(RobotType.POLITICIAN, directions[spawnI], rc.getInfluence() / POLITICIAN_INF_FRAC);
            }
            spawnI++;
        } else if (spawnI > 5) {
            if (rc.canBuildRobot(RobotType.MUCKRAKER, directions[spawnI], 1)) {
                rc.buildRobot(RobotType.MUCKRAKER, directions[spawnI], 1);
            }
            spawnI++;
        }
        if (spawnI >= directions.length) {
            spawnI = 0;
        }

        if (rc.getInfluence() / BID_FRAC > 2 && rc.canBid(rc.getInfluence() / BID_FRAC)) {
            rc.bid(rc.getInfluence() / BID_FRAC);
        } else if (rc.canBid(2)) {
            rc.bid(2);
        }
    }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        int sensorRadius = rc.getType().sensorRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        RobotInfo[] neutral = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);
        RobotInfo[] enemies = rc.senseNearbyRobots(sensorRadius, enemy);
        RobotInfo[] friends = rc.senseNearbyRobots(sensorRadius, rc.getTeam());
        if (rc.getFlag(rc.getID()) != 0) {
            rc.setFlag(0);
        }
        if ((attackable.length != 0 || neutral.length != 0) && rc.canEmpower(actionRadius)) {
            rc.empower(actionRadius);
            return;
        } else if(enemies.length != 0) {
            Direction nearestEnemy = directionToNearestEnemy(enemies);
            rc.setFlag(1000010);
            if (tryMove(nearestEnemy)) {
                System.out.println("Enemy spotted!");
            }
        } else if(friends.length != 0) {
            for (RobotInfo fri : friends) {
                if (rc.canGetFlag(fri.ID)) {
                    int deviate;
                    switch (rc.getFlag(fri.ID)) {
                        case 1000001:
                            tryMove(Direction.NORTH);
                            break;
                        case 1000002:
                            tryMove(Direction.NORTHEAST);
                            break;
                        case 1000003:
                            tryMove(Direction.EAST);
                            break;
                        case 1000004:
                            tryMove(Direction.SOUTHEAST);
                            break;
                        case 1000005:
                            tryMove(Direction.SOUTH);
                            break;
                        case 1000006:
                            tryMove(Direction.SOUTHWEST);
                            break;
                        case 1000007:
                            tryMove(Direction.WEST);
                            break;
                        case 1000008:
                            tryMove(Direction.NORTHWEST);
                            break;
                        case 1000010:
                            tryMove(rl.directionTo(fri.location));
                            break;
                        default:
                            deviate = (int) (Math.random() * 3) - 1;
                            if (wanderDirection + deviate == 8) {
                                deviate = -7;
                            } else if (wanderDirection + deviate == -1) {
                                deviate = 7;
                            }
                            if (tryMove(directions[wanderDirection + deviate])) {
                                immobile = 0;
                            } else {
                                immobile++;
                                if (immobile > (int)((1 / rc.sensePassability(rc.getLocation())) * 2)) {
                                    wanderDirection = (int)(Math.random() * directions.length);
                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    static void runSlanderer() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        RobotInfo[] enemies = rc.senseNearbyRobots(sensorRadius, enemy);
        RobotInfo[] nextTo = rc.senseNearbyRobots(1, rc.getTeam());
        Direction EBDirection = null;
        for (RobotInfo robot: nextTo) {
            if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                MapLocation curDir = rc.getLocation();
                EBDirection = curDir.directionTo(robot.location);
                tryMove(flipDirection(EBDirection));
            }
        }

        //if you spot an enemy, run!
        if (enemies.length != 0) {
            Direction nearestEnemy = directionToNearestEnemy(enemies);
            switch (nearestEnemy) {
                case NORTH:
                    tryMove(Direction.SOUTH);
                    break;
                case NORTHEAST:
                    tryMove(Direction.SOUTHWEST);
                    break;
                case NORTHWEST:
                    tryMove(Direction.SOUTHEAST);
                    break;
                case EAST:
                    tryMove(Direction.WEST);
                    break;
                case WEST:
                    tryMove(Direction.EAST);
                    break;
                case SOUTHEAST:
                    tryMove(Direction.NORTHWEST);
                    break;
                case SOUTHWEST:
                    tryMove(Direction.NORTHEAST);
                    break;
                case SOUTH:
                    tryMove(Direction.NORTH);
                    break;
            }
        }

        if (turnCount > 50) {
            tryMove(randomDirection());
        }

    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        int sensorRadius = rc.getType().sensorRadiusSquared;
        int deviate = 0;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        RobotInfo[] enemies = rc.senseNearbyRobots(sensorRadius, enemy);
        if (rc.getFlag(rc.getID()) != 0) {
            rc.setFlag(0);
        }
        if (attackable.length != 0) {
            for (RobotInfo robot : attackable) {
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
                        System.out.println("e x p o s e d");
                        rc.expose(robot.location);
                        return;
                    }
                }
            }
        } else if(enemies.length != 0) {
            Direction nearestEnemy = directionToNearestEnemy(enemies);
            rc.setFlag(1000020);
            if (tryMove(nearestEnemy)) {
            }
        } else {
            deviate = (int) (Math.random() * 3) - 1;
            if (wanderDirection + deviate == 8) {
                deviate = -7;
            } else if (wanderDirection + deviate == -1) {
                deviate = 7;
            }
            if (tryMove(directions[wanderDirection + deviate])) {
                immobile = 0;
            } else {
                immobile++;
                if (immobile > 5) {
                    wanderDirection = (int)(Math.random() * directions.length);
                }
            }
        }
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    //first used this just for enemies. Then realized that it could be used for any unit type
    /**
     * Returns the direction of the nearest instance of a unit
     *
     * @param enemies Array of sensed units
     * @return the direction of the nearest unit
     */
    static Direction directionToNearestEnemy(RobotInfo[] enemies) {
        Direction nearestEnemy = randomDirection();
            int closest = Integer.MAX_VALUE;
            for (RobotInfo eni : enemies) {
                if (rl.distanceSquaredTo(eni.getLocation()) < closest) {
                    nearestEnemy = rl.directionTo(eni.getLocation());
                }
            }
        return nearestEnemy;
    }

    static Direction flipDirection(Direction dir) {
        Direction opp = Direction.NORTH;
        switch (dir) {
            case NORTH:
                opp = Direction.SOUTH;
                break;
            case NORTHEAST:
                opp = Direction.SOUTHWEST;
                break;
            case NORTHWEST:
                opp = Direction.SOUTHEAST;
                break;
            case EAST:
                opp = Direction.WEST;
                break;
            case WEST:
                opp = Direction.EAST;
                break;
            case SOUTHEAST:
                opp = Direction.NORTHWEST;
                break;
            case SOUTHWEST:
                opp = Direction.NORTHEAST;
                break;
            case SOUTH:
                opp = Direction.NORTH;
                break;
        }
        return opp;
    }
}
