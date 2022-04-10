package hairclinic;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.util.Objects;

public class RestAndPeace extends AdvancedRobot {


    public static Point2D.Double TOP_LEFT = new Point2D.Double(100, 700);
    public static Point2D.Double TOP_RIGHT = new Point2D.Double(700, 700);
    public static Point2D.Double BOT_RIGHT = new Point2D.Double(700, 100);
    public Enemy enemy;
    public Point2D.Double myPosition;
    public Point2D.Double lastDestination = TOP_RIGHT;
    public Point2D.Double nextDestination = TOP_LEFT;

    @Override
    public void run() {
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        while (true) {
            myPosition = new Point2D.Double(getX(), getY());
            if (getOthers() > 0 && getTime() > 9) {
                destroyEnemy();
            }
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        var angle = getHeadingRadians() + e.getBearingRadians();
        enemy = new Enemy(
                e.getName(),
                new Point2D.Double(myPosition.x + e.getDistance() * Math.sin(angle), myPosition.y + e.getDistance() * Math.cos(angle)),
                e.getEnergy(),
                (e.getEnergy() > 0 || getOthers() > 0)
        );
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
    }

    public void destroyEnemy() {
        if (!enemy.isAlive) {
            return;
        }
        if (getGunTurnRemaining() == 0 && getEnergy() > 1) {
            setFire(1);
            setTurnGunRightRadians(Utils.normalRelativeAngle(
                    Math.atan2(enemy.lastPosition.x - getX(), enemy.lastPosition.y - getY()) - getGunHeadingRadians()));
        }
        double distanceToNextDestination = myPosition.distance(nextDestination);
        if (distanceToNextDestination < 5) {
            if (nextDestination == TOP_LEFT) {
                lastDestination = TOP_LEFT;
                nextDestination = TOP_RIGHT;
            } else if (lastDestination == TOP_LEFT && nextDestination == TOP_RIGHT) {
                nextDestination = BOT_RIGHT;
            } else if (nextDestination == BOT_RIGHT) {
                lastDestination = BOT_RIGHT;
                nextDestination = TOP_RIGHT;
            } else if (lastDestination == BOT_RIGHT && nextDestination == TOP_RIGHT) {
                nextDestination = TOP_LEFT;
            }
        }

        double angle = Math.atan2(nextDestination.x - myPosition.x, nextDestination.y - myPosition.y) - getHeadingRadians();
        double direction = 1;

        if (Math.cos(angle) < 0) {
            angle += 3;
            direction = -1;
        }

        setAhead(distanceToNextDestination * direction);
        setTurnRightRadians(angle = Utils.normalRelativeAngle(angle));
        setMaxVelocity(Math.abs(angle) > 1 ? 0 : 8d);
    }

    public class Enemy {
        public String name;
        public Point2D.Double lastPosition;
        public double energy;
        public boolean isAlive;

        public Enemy(String name, Point2D.Double lastPosition, double energy, boolean isAlive) {
            this.name = name;
            this.lastPosition = lastPosition;
            this.energy = energy;
            this.isAlive = isAlive;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Enemy)) {
                return false;
            }
            Enemy other = (Enemy) obj;

            return Objects.equals(this.name, other.name) &&
                    Objects.equals(this.lastPosition, other.lastPosition) &&
                    Objects.equals(this.energy, other.energy) &&
                    Objects.equals(this.isAlive, other.isAlive);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, lastPosition, energy, isAlive);
        }
    }

    @Override
    public String toString() {
        return "RestAndPeace{" +
                "enemy=" + enemy +
                ", myPosition=" + myPosition +
                '}';
    }
}
