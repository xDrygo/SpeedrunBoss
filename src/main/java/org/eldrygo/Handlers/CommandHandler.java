package org.eldrygo.Handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.eldrygo.Cinematics.CinematicSequence;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Cinematics.Managers.CountdownBossBarManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Managers.StunManager;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.ModifierManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.LoadUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor {
    private final ChatUtils chatUtils;
    private final XTeamsAPI xTeamsAPI;
    private final TeamDataManager teamDataManager;
    private final ConfigManager configManager;
    private final GracePeriodManager gracePeriodManager;
    private final ModifierManager modifierManager;
    private final SpeedrunBoss plugin;
    private final EventManager eventManager;
    private final PVPManager pvpManager;
    private final BroadcastManager broadcastManager;
    private final LoadUtils loadUtils;
    private final StunManager stunManager;
    private final CountdownBossBarManager countdownBossBarManager;
    private final CinematicManager cinematicManager;
    private final CinematicSequence cinematicSequence;

    public CommandHandler(ChatUtils chatUtils, XTeamsAPI xTeamsAPI, TeamDataManager teamDataManager, ConfigManager configManager, GracePeriodManager gracePeriodManager, ModifierManager modifierManager, SpeedrunBoss plugin, EventManager eventManager, PVPManager pvpManager, BroadcastManager broadcastManager, LoadUtils loadUtils, StunManager stunManager, CountdownBossBarManager countdownBossBarManager, CinematicManager cinematicManager, CinematicSequence cinematicSequence) {
        this.chatUtils = chatUtils;
        this.xTeamsAPI = xTeamsAPI;
        this.teamDataManager = teamDataManager;
        this.configManager = configManager;
        this.gracePeriodManager = gracePeriodManager;
        this.modifierManager = modifierManager;
        this.plugin = plugin;
        this.eventManager = eventManager;
        this.pvpManager = pvpManager;
        this.broadcastManager = broadcastManager;
        this.loadUtils = loadUtils;
        this.stunManager = stunManager;
        this.countdownBossBarManager = countdownBossBarManager;
        this.cinematicManager = cinematicManager;
        this.cinematicSequence = cinematicSequence;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            handleSPBInfo(sender);
            return false;
        }

        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("admin")) {
            return handleAdmin(sender, args);
        }
        sender.sendMessage(chatUtils.getMessage("error.unknown_command", (Player) sender));
        return false;
    }

    private void handleSPBInfo(CommandSender sender) {
        List<String> spbInfo = chatUtils.getMessageConfig().getStringList("playerutils.spbinfo.string");
        Player target;
        if (!(sender instanceof Player)) {
            target = null;
            sender.sendMessage(chatUtils.getMessage("error.only_by_player", target));
        } else {
            target = (Player) sender;
            if (spbInfo.isEmpty()) {
                sender.sendMessage(chatUtils.getMessage("error.no_spb_info", target));
                return;
            }
            for (String line : spbInfo) {
                String teamName = xTeamsAPI.getPlayerTeamName(target);
                String teamDisplayName = xTeamsAPI.getPlayerTeamDisplayName(target);

                String formattedLine = PlaceholderAPI.setPlaceholders(target, line);
                String wardenKilled = teamDataManager.hasKilledBoss(teamName, "warden") ? "#a0ff72✔" : "#ff7272✖";
                String witherKilled = teamDataManager.hasKilledBoss(teamName, "wither") ? "#a0ff72✔" : "#ff7272✖";
                String elderGuardianKilled = teamDataManager.hasKilledBoss(teamName, "elder_guardian") ? "#a0ff72✔" : "#ff7272✖";
                String enderDragonKilled = teamDataManager.hasKilledBoss(teamName, "ender_dragon") ? "#a0ff72✔" : "#ff7272✖";


                // Asegurarse de que `getTeamMembers` devuelva una lista
                List<String> teamMembers = new ArrayList<>(xTeamsAPI.getTeamMembers(teamName));  // Convertir el Set a List si es necesario

                // Aplicar formato a cada miembro
                String noMembersMsg = ChatUtils.formatColor(configManager.getMessageString("playerutils.spbinfo.no_members"));
                String memberFormat = configManager.getMessageString("playerutils.spbinfo.members");
                String separator = configManager.getMessageString("playerutils.spbinfo.members_separator");
                String teamMembersFormatted = teamMembers.isEmpty() ? noMembersMsg :
                        teamMembers.stream()
                                .map(member -> memberFormat.replace("%member_name%", member))
                                .collect(Collectors.joining(separator));

                teamDisplayName = ChatUtils.formatColor(teamDisplayName);
                wardenKilled = ChatUtils.formatColor(wardenKilled);
                witherKilled = ChatUtils.formatColor(witherKilled);
                elderGuardianKilled = ChatUtils.formatColor(elderGuardianKilled);
                enderDragonKilled = ChatUtils.formatColor(enderDragonKilled);
                teamMembersFormatted = ChatUtils.formatColor(teamMembersFormatted);

                sender.sendMessage(ChatUtils.formatColor(formattedLine)
                        .replace("%team_name%", teamName)
                        .replace("%team_display_name%", teamDisplayName)
                        .replace("%team_members%", teamMembersFormatted)
                        .replace("%warden_killed%", wardenKilled)
                        .replace("%wither_killed%", witherKilled)
                        .replace("%elderguardian_killed%", elderGuardianKilled)
                        .replace("%enderdragon_killed%", enderDragonKilled));
            }
        }
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(chatUtils.getMessage("error.unknown_command", null));
            } else {
                sender.sendMessage(chatUtils.getMessage("error.unknown_command", (Player) sender));
            }
            return false;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "graceperiod" -> {
                if (!sender.hasPermission("spb.admin.graceperiod") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleGracePeriod(sender, args);
                }
            }
            case "pvp" -> {
                if (!sender.hasPermission("spb.admin.pvp") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handlePvP(sender, args);
                }
            }
            case "stun" -> {
                if (!sender.hasPermission("spb.admin.stun") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleStun(sender, args);
                }
            }
            case "event" -> {
                if (!sender.hasPermission("spb.admin.event") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    handleEvent(sender, args);
                }
            }
            case "broadcast" -> {
                if (!sender.hasPermission("spb.admin.broadcast") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleBroadcast(sender, args);
                }
            }
            case "killerhandle" -> {
                if (!sender.hasPermission("spb.admin.killerhandle") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleKillerHandle(sender, args);
                }
            }
            case "task" -> {
                if (!sender.hasPermission("spb.admin.task") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleTask(sender, args);
                }
            }
            case "cinematic" -> {
                if (!sender.hasPermission("spb.admin.killerhandle") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleCinematic(sender, args);
                }
            }
            case "reload" -> {
                if (!sender.hasPermission("spb.admin.reload") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleReload(sender);
                }
            }
            case "help" -> {
                if (!sender.hasPermission("spb.admin.help") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleHelp(sender);
                }
            }
        }
        return false;
    }

    private boolean handleBroadcast(CommandSender sender, String[] args) {
        Player target = (sender instanceof Player) ? (Player) sender : null;

        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
            return false;
        }

        // Une todos los argumentos desde el tercero y convierte \n en salto real
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        message = message.replace("\\n", "\n");

        broadcastManager.sendCommandBroadcastMessage(target, message);
        return true;
    }

    private void handleEvent(CommandSender sender, String[] args) {
        Player target;
        if (!(sender instanceof Player)) {
            target = null;
        } else {
            target = (Player) sender;
        }
        if (args.length == 2) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
        }
        String action = args[2].toLowerCase();
        switch (action) {
            case "start" -> eventManager.startEvent(target);
            case "end" -> eventManager.endEvent(target);
            case "pause" -> eventManager.pauseEvent(target);
            case "resume" -> eventManager.resumeEvent(target);
        }
    }

    private boolean handlePvP(CommandSender sender, String[] args) {
        Player target = (sender instanceof Player) ? (Player) sender : null;
        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
            return false;
        }
        String action = args[2].toLowerCase();
        boolean silent = args.length >= 4 && args[3].equalsIgnoreCase("silent");
        switch (action) {
            case "enable" -> {
                pvpManager.forceStartPvP();
                sender.sendMessage(chatUtils.getMessage("administration.pvp.enable", target));
                if (!silent) {
                    broadcastManager.sendPVPEnableMessage();
                }
            }
            case "disable" -> {
                pvpManager.stopPvP();
                sender.sendMessage(chatUtils.getMessage("administration.pvp.disable", target));
                if (!silent) {
                    broadcastManager.sendPVPDisableMessage();
                }
            }
            default -> {
                sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
                return false;
            }
        }
        return true;
    }
    private boolean handleStun(CommandSender sender, String[] args) {
        Player target = (sender instanceof Player) ? (Player) sender : null;
        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
            return false;
        }
        String action = args[2].toLowerCase();
        switch (action) {
            case "enable" -> {
                stunManager.stunAllPlayers();
                sender.sendMessage(chatUtils.getMessage("administration.stun.enable", target));
            }
            case "disable" -> {
                stunManager.unStunAllPlayers();
                sender.sendMessage(chatUtils.getMessage("administration.stun.disable", target));
            }
            case "player" -> {
                if (args.length < 5) {
                    sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
                    return false;
                }

                Player targetCmd = Bukkit.getPlayer(args[3]);
                if (targetCmd != null && targetCmd.isOnline()) {
                    String subaction = args[4].toLowerCase();
                    switch (subaction) {
                        case "true" -> {
                            boolean withTime = args.length >= 6;
                            if (withTime) {
                                try {
                                    long time = Long.parseLong(args[5]);
                                    stunManager.stunPlayer(targetCmd, time);
                                    sender.sendMessage(chatUtils.getMessage("administration.stun.player.true.withtime_success", target)
                                            .replace("%target%", targetCmd.getName())
                                            .replace("%time%", String.valueOf(time)));
                                } catch (NumberFormatException e) {
                                    sender.sendMessage(chatUtils.getMessage("error.invalid_number", target));
                                }
                            } else {
                                stunManager.stunPlayerIndefinitely(targetCmd);
                                sender.sendMessage(chatUtils.getMessage("administration.stun.player.true.success", target)
                                        .replace("%target%", targetCmd.getName()));
                            }
                        }
                        case "false" -> {
                            stunManager.unStunPlayer(targetCmd);
                            sender.sendMessage(chatUtils.getMessage("administration.stun.player.false.success", target)
                                    .replace("%target%", targetCmd.getName()));
                        }
                        default -> sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
                    }
                } else {
                    sender.sendMessage(chatUtils.getMessage("administration.stun.player_not_found", target));
                }
            }
            default -> {
                sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
                return false;
            }
        }
        return true;
    }

    private boolean handleGracePeriod(CommandSender sender, String[] args) {
        Player target;
        if (!(sender instanceof Player)) {
            target = null;
        } else {
            target = (Player) sender;
        }
        if (args.length == 2) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
            return false;
        }

        String action = args[2].toLowerCase();
        switch (action) {
            case "forcestart" -> {
                if (args.length < 4) {
                    sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
                    return false;
                } else {
                    if (modifierManager.isGracePeriodActive()) {
                        sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestart.already", target));
                    } else {
                        GracePeriodManager gracePeriodManager = new GracePeriodManager(plugin, xTeamsAPI, broadcastManager, chatUtils);
                        gracePeriodManager.startGracePeriod();
                        sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestart.success", target));
                    }
                }
            }
            case "forcestop" -> {
                if (gracePeriodManager != null) {
                    gracePeriodManager.stopGracePeriod();
                    sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestop.success", target));
                } else {
                    sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestop.already", target));
                }
            }
        }
        return false;
    }
    private boolean handleKillerHandle(CommandSender sender, String[] args) {
        Player msgTarget;
        if (!(sender instanceof Player)) {
            msgTarget = null;
        } else {
            msgTarget = (Player) sender;
        }
        if (args.length < 3) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(chatUtils.getMessage("error.only_by_player", msgTarget));
            } else {
                giveKillerHandle((Player) sender);
                plugin.getLogger().info("⚠ The player " + sender + " received the Killer Handle by himself.");
                sender.sendMessage(chatUtils.getMessage("administration.killer_handle.give_self", (Player) sender));
            }
            return false;
        } else {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(chatUtils.getMessage("error.player_not_found", target));
                return true;
            } else {
                giveKillerHandle(target);
                plugin.getLogger().info("⚠ The player " + target + " received the Killer Handle by " + sender + ".");
                sender.sendMessage(chatUtils.getMessage("administration.killer_handle.give_other", target));
                return false;
            }
        }
    }
    private void giveKillerHandle(Player target) {
        // Crea la espada (puedes cambiar el Material a otro tipo si lo deseas)
        ItemStack sword = new ItemStack(Material.GOLDEN_SWORD);

        // Obtén el ItemMeta de la espada para poder modificar su nombre y encantamientos
        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {
            // Establecer el nombre de la espada
            meta.setDisplayName(ChatUtils.formatColor("%prefix% #ffd085Kill Handle ⚔").replace("%prefix%", plugin.prefix));

            // Agregar el encantamiento
            meta.addEnchant(Enchantment.SHARPNESS, 999, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            // Establecer el ItemMeta de la espada
            sword.setItemMeta(meta);
        }

        // Darle la espada al jugador
        target.getInventory().addItem(sword);
    }

    private boolean handleCinematic(CommandSender sender, String[] args) {
        boolean isConsole = !(sender instanceof Player);

        if (args.length < 2) {  // Verificar si se proporciona el subcomando
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", isConsole ? null : (Player) sender));
            return false;
        }

        // Asegurar que solo se ejecute si el sender es un jugador
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String subcommand = args[1].toLowerCase();

            // Manejar subcomando "start"
            if ("start".equals(subcommand)) {
                String cinematic = "start";
                return handleStartCinematic(player, isConsole, cinematic);
            }

            // Manejar subcomando "stop"
            if ("stop".equals(subcommand)) {
                return handleStopCinematic(player, isConsole, args);
            }

            // Subcomando no válido
            player.sendMessage(chatUtils.getMessage("error.invalid_usage", player));
        } else {
            // Si el comando no es ejecutado por un jugador
            sender.sendMessage(chatUtils.getMessage("error.only_players_can_use_this", null));
        }

        return false;
    }

    private boolean handleStartCinematic(Player player, boolean isConsole, String cinematic) {
        if (cinematicSequence.isRunning()) {
            player.sendMessage(chatUtils.getMessage("administration.cinematic.start.cinematic_already_running", isConsole ? null : player));
            return false;
        }

        cinematicManager.startCinematic(cinematic);  // Método para iniciar la cinemática
        player.sendMessage(chatUtils.getMessage("administration.cinematic.start.success", isConsole ? null : player)
                .replace("%cinematic%", cinematic));
        return true;  // Cambiado a true para indicar éxito
    }

    private boolean handleStopCinematic(Player player, boolean isConsole, String[] args) {
        if (!cinematicSequence.isRunning()) {
            player.sendMessage(chatUtils.getMessage("administration.cinematic.stop.no_cinematic_running", isConsole ? null : player));
            return false;
        }

        Optional<String> runningCinematic = cinematicSequence.getRunningCinematic();

        String cinematic = runningCinematic.orElse("none");

        // Verificar si se requiere confirmación
        if (args.length < 3) {
            List<String> confirmationLines = chatUtils.getMessageConfig().getStringList("administration.cinematic.stop.confirmation_request");

            if (confirmationLines.isEmpty()) {
                confirmationLines = List.of(
                        " ",
                        "%prefix% #FF3535&lCinematic Stop Confirmation ⚠",
                        " ",
                        " #ccccccYou are about to stop the cinematic &f%cinematic%#cccccc.",
                        " &7For security reasons, we need you to confirm the action of stopping it.",
                        " ",
                        "  #fff18d&lTo confirm, type &r#fff9cc/spb admin cinematic stop confirm",
                        " ",
                        "#FF4444&o(This action can't be undone.)",
                        " "
                );
            }

            for (String line : confirmationLines) {
                player.sendMessage(ChatUtils.formatColor(line
                        .replace("%cinematic%", cinematic)
                        .replace("%prefix%", plugin.prefix)));
            }
            return false;
        }

        // Si el jugador escribe "confirm", detener la cinemática
        if ("confirm".equalsIgnoreCase(args[2])) {
            cinematicSequence.stop();
            player.sendMessage(chatUtils.getMessage("administration.cinematic.stop.success", isConsole ? null : player)
                    .replace("%cinematic%", cinematic));
            return true;
        }

        // Comando inválido
        player.sendMessage(chatUtils.getMessage("error.invalid_usage", isConsole ? null : player));
        return false;
    }


    private final Map<String, String> pendingTaskCancellations = new HashMap<>();

    private boolean handleTask(CommandSender sender, String[] args) {
        boolean isConsole = !(sender instanceof Player);
        String senderId = isConsole ? "CONSOLE" : ((Player) sender).getUniqueId().toString();

        if (args.length < 4) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", isConsole ? null : (Player) sender));
            return false;
        }

        String action = args[2].toLowerCase();
        String task = args[3].toLowerCase();

        switch (action) {
            case "cancel" -> {
                boolean taskExists = switch (task) {
                    case "graceperiod" -> gracePeriodManager.gracePeriodTask != null;
                    case "pvp" -> pvpManager.pvpTask != null;
                    case "bbcountdown" -> countdownBossBarManager.bossbarTask != null;
                    default -> false;
                };

                if (!taskExists) {
                    sender.sendMessage(chatUtils.getMessage("administration.task.cancel.task_not_initialized", isConsole ? null : (Player) sender)
                            .replace("%task%", task));
                    return false;
                }

                pendingTaskCancellations.put(senderId, task);
                List<String> confirmationLines = chatUtils.getMessageConfig().getStringList("administration.task.cancel.confirmation_request");

                if (confirmationLines.isEmpty()) {
                    confirmationLines = List.of(
                            " ",
                            "%prefix% #FF3535&lTask Cancel Confirmation ⚠",
                            " ",
                            " #ccccccYou are about to cancel the task &f%task%#cccccc.",
                            " &7For security reasons, we need you to confirm the action of canceling the task.",
                            " ",
                            "  #fff18d&lTo confirm, type &r#fff9cc/spb admin task confirm &r%task%",
                            " ",
                            "#FF4444&o(This action can't be undone.)",
                            " "
                    );
                }

                for (String line : confirmationLines) {
                    sender.sendMessage(ChatUtils.formatColor(line
                            .replace("%task%", task)
                            .replace("%prefix%", plugin.prefix)));
                }
            }

            case "confirm" -> {
                String pending = pendingTaskCancellations.get(senderId);
                if (pending == null || !pending.equalsIgnoreCase(task)) {
                    sender.sendMessage(chatUtils.getMessage("administration.task.cancel.no_pending_confirmation", isConsole ? null : (Player) sender));
                    return false;
                }

                pendingTaskCancellations.remove(senderId);
                switch (task) {
                    case "graceperiod" -> {
                        gracePeriodManager.gracePeriodTask.cancel();
                        gracePeriodManager.gracePeriodTask = null;
                    }
                    case "pvp" -> {
                        pvpManager.pvpTask.cancel();
                        pvpManager.pvpTask = null;
                    }
                    case "bbcountdown" -> {
                        countdownBossBarManager.bossbarTask.cancel();
                        countdownBossBarManager.bossbarTask = null;
                    }
                    default -> {
                        sender.sendMessage(chatUtils.getMessage("administration.task.cancel.task_not_found", isConsole ? null : (Player) sender)
                                .replace("task", task));
                        return false;
                    }
                }

                sender.sendMessage(chatUtils.getMessage("administration.task.cancel.success", isConsole ? null : (Player) sender)
                        .replace("%task%", task));
                return true;
            }

            default -> {
                sender.sendMessage(chatUtils.getMessage("error.invalid_usage", isConsole ? null : (Player) sender));
                return false;
            }
        }
        return false;
    }
    private boolean handleReload(CommandSender sender) {
        String executor;
        if (sender instanceof Player) {
            sender.sendMessage(chatUtils.getMessage("administration.reload_command.success", (Player) sender));
            executor = String.valueOf(sender);
        } else {
            executor = "console";
        }
        loadUtils.loadConfig();
        plugin.getLogger().info("✔ Sucessfully reloaded config by" + executor + ".");
        return false;
    }
    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("                         &r&lSpeedrun#fd3c9e&lBoss &8» &r&fHelp"));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("                 #fff18d&lɪɴꜰᴏʀᴍᴀᴛɪᴏɴ ᴄᴏᴍᴍᴀɴᴅꜱ"));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ #707070» #ccccccDisplays player-need information."));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("                    #fff18d&lᴘʟᴜɢɪɴ ᴄᴏᴍᴍᴀɴᴅꜱ"));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ʜᴇʟᴘ #707070» #ccccccShows this help message"));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ʀᴇʟᴏᴀᴅ #707070» #ccccccReloads the plugin configuration"));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("                #fff18d&lᴀᴅᴍɪɴɪꜱᴛʀᴀᴛɪᴏɴ ᴄᴏᴍᴍᴀɴᴅꜱ"));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ᴘᴠᴘ <ᴇɴᴀʙʟᴇ / ᴅɪꜱᴀʙʟᴇ> <ꜱɪʟᴇɴᴛ> #707070- #ccccccManage the PvP status."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ꜱᴛᴜɴ <ᴇɴᴀʙʟᴇ / ᴅɪꜱᴀʙʟᴇ/ ᴘʟᴀʏᴇʀ> <ᴘʟᴀʏᴇʀ> <ꜱᴛᴀᴛᴜꜱ> #707070- #ccccccManage the Stun on players."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ᴇᴠᴇɴᴛ <ꜱᴛᴀʀᴛ / ᴘᴀᴜꜱᴇ / ʀᴇꜱᴜᴍᴇ / ᴇɴᴅ> #707070- #ccccccManage the event task."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ɢʀᴀᴄᴇᴘᴇʀɪᴏᴅ <ꜰᴏʀᴄᴇꜱᴛᴀʀᴛ / ꜰᴏʀᴄᴇꜱᴛᴏᴘ> <ꜱᴇᴄᴏɴᴅꜱ> #707070- #ccccccManage the Grace Period."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ʙʀᴏᴀᴅᴄᴀꜱᴛ <ᴍᴇꜱꜱᴀɢᴇ> #707070- #ccccccBroadcast the message of the arg."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ɢɪᴠᴇᴄᴏᴍᴘᴀꜱꜱ <ᴘʟᴀʏᴇʀ> #707070- #ccccccGive the Structure compass."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ᴋɪʟʟᴇʀʜᴀɴᴅʟᴇ <ᴘʟᴀʏᴇʀ> #707070- #ccccccGive the Killer Handle."));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        return false;
    }
}
