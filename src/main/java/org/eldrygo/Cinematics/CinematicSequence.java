package org.eldrygo.Cinematics;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.SpeedrunBoss;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CinematicSequence {
    private final Player player;
    private final List<SequenceAction> actions = new ArrayList<>();
    private final SpeedrunBoss plugin;

    public CinematicSequence(Player player, SpeedrunBoss plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    // Agregar acción para el jugador
    public CinematicSequence addAction(Consumer<Player> action) {
        actions.add(new SequenceAction(action, 0)); // Acción sin delay
        return this;
    }

    // Agregar un delay entre las acciones
    public CinematicSequence addDelay(long ticks) {
        actions.add(new SequenceAction(null, ticks)); // Solo delay
        return this;
    }

    // Iniciar la secuencia de acciones
    public void start() {
        new BukkitRunnable() {
            int currentAction = 0;
            long delayRemaining = 0; // Variable para controlar el delay entre acciones

            @Override
            public void run() {
                if (currentAction >= actions.size()) {
                    cancel(); // Termina cuando se han ejecutado todas las acciones
                    return;
                }

                SequenceAction action = actions.get(currentAction);

                // Si hay un delay pendiente, se espera
                if (delayRemaining > 0) {
                    delayRemaining--;  // Reducir el delay
                    return;  // No hacer nada, solo esperar
                }

                if (action.getAction() != null) {
                    // Ejecutar la acción
                    action.getAction().accept(player);
                }

                // Establecer el delay para la siguiente acción si existe
                delayRemaining = action.getDelay();

                // Avanzar a la siguiente acción
                currentAction++;
            }
        }.runTaskTimer(plugin, 0L, 1L);  // Ejecutar la secuencia inmediatamente
    }

    // Clase para representar una acción con su correspondiente delay
    static class SequenceAction {
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
