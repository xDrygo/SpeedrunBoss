package org.eldrygo.Cinematics.Models;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.SpeedrunBoss;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CinematicSequence {
    private final List<Player> players;
    private final SpeedrunBoss plugin;
    private final CinematicType cinematicType;
    private final List<SequenceAction> actions = new ArrayList<>();
    private BukkitRunnable task;
    private boolean isRunning = false;

    public CinematicSequence(List<Player> players, SpeedrunBoss plugin, CinematicType cinematicType) {
        this.players = players;
        this.plugin = plugin;
        this.cinematicType = cinematicType;
    }

    public CinematicSequence addAction(Consumer<Player> action) {
        actions.add(new SequenceAction(action, 0));
        return this;
    }
    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public CinematicSequence addDelay(long ticks) {
        actions.add(new SequenceAction((Consumer<Player>) null, ticks));
        return this;
    }

    public CinematicSequence addGlobalAction(Runnable action) {
        actions.add(new SequenceAction(action, 0));
        return this;
    }

    public CinematicSequence addGlobalDelay(long ticks) {
        actions.add(new SequenceAction((Runnable) null, ticks));
        return this;
    }

    public CinematicSequence then(Consumer<Player> action) {
        return addAction(action);
    }

    public CinematicSequence thenGlobal(Runnable action) {
        return addGlobalAction(action);
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;

        task = new BukkitRunnable() {
            int currentAction = 0;
            long ticksUntilNextAction = 0;

            @Override
            public void run() {
                if (currentAction >= actions.size()) {
                    stop();
                    return;
                }

                SequenceAction action = actions.get(currentAction);

                if (ticksUntilNextAction > 0) {
                    ticksUntilNextAction--;
                    return;
                }

                if (action.getPlayerAction() != null) {
                    for (Player p : players) {
                        action.getPlayerAction().accept(p);
                    }
                } else if (action.getGlobalAction() != null) {
                    action.getGlobalAction().run();
                }

                ticksUntilNextAction = action.getDelay();
                currentAction++;
            }
        };

        task.runTaskTimer(plugin, 0L, 1L);
    }

    public void stop() {
        if (!isRunning) return;
        if (task != null) task.cancel();
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Optional<CinematicType> getRunningCinematicType() {
        return isRunning ? Optional.of(cinematicType) : Optional.empty();
    }

    public String getCinematicId() {
        return cinematicType.getId();
    }

    public CinematicType getType() {
        return cinematicType;
    }

    private static class SequenceAction {
        private final Consumer<Player> playerAction;
        private final Runnable globalAction;
        private final long delay;

        public SequenceAction(Consumer<Player> action, long delay) {
            this.playerAction = action;
            this.globalAction = null;
            this.delay = delay;
        }

        public SequenceAction(Runnable action, long delay) {
            this.playerAction = null;
            this.globalAction = action;
            this.delay = delay;
        }

        public Consumer<Player> getPlayerAction() {
            return playerAction;
        }

        public Runnable getGlobalAction() {
            return globalAction;
        }

        public long getDelay() {
            return delay;
        }
    }
}
