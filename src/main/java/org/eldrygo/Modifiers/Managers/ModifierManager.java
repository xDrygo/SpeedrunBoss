package org.eldrygo.Modifiers.Managers;

public class ModifierManager {
    private long gracePeriod;
    private long gracePeriodStartTime;

    public long getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(long gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    // Establecer el inicio del periodo de gracia
    public void startGracePeriod() {
        this.gracePeriodStartTime = System.currentTimeMillis(); // Guarda el tiempo actual al iniciar el periodo
    }

    // Verificar si el periodo de gracia está activo
    public boolean isGracePeriodActive() {
        // Si gracePeriodStartTime es 0, significa que no se ha iniciado el periodo de gracia.
        if (gracePeriodStartTime == 0) {
            return false; // No ha comenzado
        }

        // Si el tiempo actual es menor que el tiempo de inicio + duración del periodo de gracia, está activo
        return System.currentTimeMillis() - gracePeriodStartTime < gracePeriod;
    }
}
