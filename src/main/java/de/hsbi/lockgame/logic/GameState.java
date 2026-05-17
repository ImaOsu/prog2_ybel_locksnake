package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import java.util.List;
import java.util.ArrayList;

public final class GameState {

    private final Level level;
    private final Snake snake;
    private final List<Pin> pins;
    private final Status status;
    private final Direction pendingDirection;

    public GameState(
        Level level,
        Snake snake,
        List<Pin> pins,
        Status status,
        Direction pendingDirection
    ) {
        this.level = level;
        this.snake = snake;
        this.pins = pins;
        this.status = status;
        this.pendingDirection = pendingDirection;
    }

    public Level level() { return level; }
    public Snake snake() { return snake; }
    public List<Pin> pins() { return pins; }
    public Status status() { return status; }
    public Direction pendingDirection() { return pendingDirection; }

    public GameState tick() {

        // 1) Spiel läuft nicht → nichts tun
        if (!status.isRunning()) {
            return this;
        }

        // 2) Keine Blickrichtung → nichts tun
        if (pendingDirection == null || pendingDirection == Direction.NONE) {
            return this;
        }

        // 3) Kopfposition + nächste Position
        Position head = snake.head();
        Position next = snake.nextHead(pendingDirection);

        // 4) Spielfeldgrenzen
        if (!level.isInside(next)) {
            return new GameState(level, snake, pins, Status.LOST_OUT_OF_BOUNDS, pendingDirection);
        }

        // 5) Wand
        if (level.cellAt(next) == CellType.WALL) {
            return new GameState(level, snake, pins, status, Direction.NONE);
        }

        // 6) Selbstkollision
        if (snake.occupies(next)) {
            return new GameState(level, snake, pins, Status.LOST_SELF_COLLISION, pendingDirection);
        }

        // 7) Pin finden
        Pin pinAtNext = null;
        for (Pin p : pins) {
            if (p.position().equals(next)) {
                pinAtNext = p;
                break;
            }
        }

        // 8) Pin-Logik
        if (pinAtNext != null) {

            // Blockiert?
            if (pinAtNext.state().isSet() ||
                pinAtNext.activationDirection() != pendingDirection) {

                return new GameState(level, snake, pins, status, Direction.NONE);
            }

            // Pin aktivieren
            List<Pin> newPins = new ArrayList<>();
            for (Pin p : pins) {
                if (p == pinAtNext) {
                    newPins.add(p.withState(Pin.State.HIGH));
                } else {
                    newPins.add(p);
                }
            }

            // Gewinnbedingung
            boolean allHigh = newPins.stream().allMatch(x -> x.state().isSet());
            Status newStatus = allHigh ? Status.WON : Status.RUNNING;

            // Schlange bleibt stehen
            return new GameState(level, snake, newPins, newStatus, pendingDirection);
        }

        // 9) Normale Bewegung
        Snake moved = snake.grow(pendingDirection);

        // 10) Gewinnbedingung
        boolean allHigh = pins.stream().allMatch(x -> x.state().isSet());
        Status newStatus = allHigh ? Status.WON : Status.RUNNING;

        return new GameState(level, moved, pins, newStatus, pendingDirection);
    }

    public enum Status {
        RUNNING,
        WON,
        LOST_SELF_COLLISION,
        LOST_OUT_OF_BOUNDS;

        public boolean isRunning() {
            return this == RUNNING;
        }
    }
}
