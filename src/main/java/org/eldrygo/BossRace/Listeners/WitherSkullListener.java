package org.eldrygo.BossRace.Listeners;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.eldrygo.SpeedrunBoss;

public class WitherSkullListener implements Listener {

    private SpeedrunBoss plugin;
    private final double probabilityMultiplier = plugin.getConfig().getDouble("configuration.wither_skull_multiplier");

    public WitherSkullListener(SpeedrunBoss plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        // Verificamos que la entidad muerta es un Wither Skeleton
        if (entity.getType() == EntityType.WITHER_SKELETON) {
            // Probabilidad base de caída sin looting (2.5%)
            double baseDropChance = 0.025;

            // Si el asesino es un jugador, podemos aplicar la probabilidad modificada por Looting
            if (entity instanceof LivingEntity livingEntity) {
                Player killer = livingEntity.getKiller(); // Obtenemos el killer (jugador que mató al mob)

                if (killer != null) {
                    // Obtenemos el nivel de Looting de la espada del jugador
                    int lootingLevel = getLootingLevel(killer);

                    // Aumentamos la probabilidad con base en el nivel de looting
                    double lootingBonus = 0.01 * lootingLevel; // 1% por nivel de Looting

                    // Aplicamos la bonificación de Looting a la probabilidad base
                    baseDropChance += lootingBonus;
                }
            }

            // Ahora aplicamos el multiplicador extra (por ejemplo x2)
            double modifiedChance = baseDropChance * probabilityMultiplier;

            // Generamos un valor aleatorio y lo comparamos con la probabilidad
            double randomValue = Math.random();

            // Si el valor aleatorio es menor que la probabilidad, agregamos la cabeza de Wither
            if (randomValue < modifiedChance) {
                event.getDrops().add(new ItemStack(Material.WITHER_SKELETON_SKULL, 1, (short) 1)); // Añadimos la cabeza de Wither
            }
        }
    }

    // Método que obtiene el nivel de Looting de la espada del jugador
    private int getLootingLevel(Player player) {
        player.getInventory().getItemInMainHand();
        if (player.getInventory().getItemInMainHand().getType() == Material.DIAMOND_SWORD) {

            ItemStack weapon = player.getInventory().getItemInMainHand();
            if (weapon.hasItemMeta() && weapon.getItemMeta().hasEnchant(Enchantment.LOOTING)) {
                return weapon.getItemMeta().getEnchantLevel(Enchantment.LOOTING);
            }
        }
        return 0; // No tiene looting
    }
}