package org.eldrygo.Cinematics;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.SpeedrunBoss;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CinematicSequence {
    private final Player player;
    private final List<SequenceAction> actions = new ArrayList<>();
    private final SpeedrunBoss plugin;
    private BukkitRunnable task;
    private boolean isRunning = false;
    private final String cinematicName;

    public CinematicSequence(Player player, SpeedrunBoss plugin, String cinematicName) {
        this.player = player;
        this.plugin = plugin;
        this.cinematicName = cinematicName;
    }

    public CinematicSequence addAction(Consumer<Player> action) {
        actions.add(new SequenceAction(action, 0));
        return this;
    }

    public CinematicSequence addDelay(long ticks) {
        actions.add(new SequenceAction(null, ticks));
        return this;
    }

    public CinematicSequence then(Consumer<Player> action) {
        return addAction(action);
    }

    public CinematicSequence waitTicks(long ticks) {
        return addDelay(ticks);
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

                if (action.getAction() != null) {
                    action.getAction().accept(player);
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
    public Optional<String> getRunningCinematic() {
        return isRunning ? Optional.of(cinematicName) : Optional.empty();
    }

    public String getName() {
        return cinematicName;
    }

    public void reset() {
        stop();
        actions.clear();
    }

    private static class SequenceAction {
        private final Consumer<Player> action;
        private final long delay;

        public SequenceAction(Consumer<Player> action, long delay) {
            this.action = action;
            this.delay = delay;
        }

        public Consumer<Player> getAction() {
            return action;
        }

        public long getDelay() {
            return delay;
        }
    }
}
