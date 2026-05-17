package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import de.hsbi.lockgame.ui.GamePanel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class GameEngine {

    private GameState state;
    private final List<Consumer<GameState>> observers = new ArrayList<>();
    private GamePanel panel;

    public GameEngine(Level level) {

        // Schlange erzeugen
        List<Position> startBody = new ArrayList<>();
        startBody.add(level.snakeStart());
        Snake snake = new Snake(startBody);

        // Pins übernehmen
        List<Pin> pins = level.pins();

        // Startzustand erzeugen
        this.state = new GameState(
            level,
            snake,
            pins,
            GameState.Status.RUNNING,
            Direction.NONE
        );
    }

    public GameState state() {
        return state;
    }

    public void setGamePanel(GamePanel panel) {
        this.panel = panel;

        // GamePanel als Observer registrieren
        addObserver(panel::update);
    }

    // Observer registrieren
    public void addObserver(Consumer<GameState> obs) {
        observers.add(obs);
    }

    // Alle Observer benachrichtigen
    private void notifyObservers() {
        observers.forEach(o -> o.accept(state));
    }

    // Wird von der Tastatur (GamePanel) aufgerufen
    public void update(Direction d) {

        // Neue Blickrichtung setzen → neuer GameState
        this.state = new GameState(
            state.level(),
            state.snake(),
            state.pins(),
            state.status(),
            d
        );

        // GUI aktualisieren
        notifyObservers();
    }

    // Wird vom Timer im main() aufgerufen
    public void tick() {

        // Spiellogik ausführen
        this.state = this.state.tick();

        // GUI aktualisieren
        notifyObservers();
    }
}
