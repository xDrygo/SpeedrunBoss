package org.eldrygo.Cinematics.Managers;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

public class FireworkManager {

    private final Random random = new Random();

    /**
     * Spawnea fuegos artificiales en un anillo alrededor del jugador
     * @param player Jugador alrededor del cual se spawnean
     * @param amount Cantidad de fuegos artificiales
     * @param radius Radio del anillo
     */
    public void spawnFireworksRing(Player player, int amount, double radius) {
        World world = player.getWorld();
        Location center = player.getLocation().add(0, 1, 0); // un poco por encima del jugador

        for (int i = 0; i < amount; i++) {
            double angle = 2 * Math.PI * i / amount;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location spawnLocation = new Location(world, x, center.getY(), z);

            Firework firework = world.spawn(spawnLocation, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();

            meta.addEffect(FireworkEffect.builder()
                    .withColor(getRandomColor())
                    .withFade(getRandomColor())
                    .with(FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)])
                    .trail(random.nextBoolean())
                    .flicker(random.nextBoolean())
                    .build());

            meta.setPower(1);
            firework.setFireworkMeta(meta);
        }
    }

    private Color getRandomColor() {
        return rainbowColors[random.nextInt(rainbowColors.length)];
    }

    Color[] rainbowColors = new Color[] {
            Color.fromRGB(255, 53, 53),
            Color.fromRGB(255, 117, 71),
            Color.fromRGB(255, 160, 65),
            Color.fromRGB(255, 214, 93),
            Color.fromRGB(255, 255, 67),
            Color.fromRGB(215, 255, 96),
            Color.fromRGB(184, 255, 112),
            Color.fromRGB(87, 255, 87),
            Color.fromRGB(93, 255, 174),
            Color.fromRGB(99, 255, 255),
            Color.fromRGB(104, 180, 255),
            Color.fromRGB(78, 78, 255),
            Color.fromRGB(127, 84, 255),
            Color.fromRGB(170, 85, 255),
            Color.fromRGB(212, 82, 255),
            Color.fromRGB(255, 106, 255)
    };
}
