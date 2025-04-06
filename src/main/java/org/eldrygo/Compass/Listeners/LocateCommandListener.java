package org.eldrygo.Compass.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.command.ConsoleCommandSender;

public class LocateCommandListener implements Listener {

    // Guardar el resultado del comando
    private String locateResult = "";

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        // Comprobamos si el comando ejecutado es "/locate"
        if (event.getCommand().startsWith("locate")) {
            String message = event.getCommand();
            if (message.contains("The nearest") && message.contains("is located at")) {
                // Aquí capturamos el mensaje que contiene las coordenadas
                String[] parts = message.split(" ");
                String coords = parts[parts.length - 1]; // Ejemplo: "x=123, z=456"

                // Extraemos las coordenadas
                String[] coordsParts = coords.split(",");
                int x = Integer.parseInt(coordsParts[0].split("=")[1]);
                int z = Integer.parseInt(coordsParts[1].split("=")[1]);

                // Guardamos el resultado para su posterior uso
                locateResult = "x=" + x + ", z=" + z;
                System.out.println("Ubicación de la estructura más cercana: " + locateResult);
            }
        }
    }

    // Método para obtener el resultado capturado
    public String getLocateResult() {
        return locateResult;
    }
}