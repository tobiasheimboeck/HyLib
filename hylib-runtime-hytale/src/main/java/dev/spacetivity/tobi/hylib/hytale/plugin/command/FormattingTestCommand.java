package dev.spacetivity.tobi.hylib.hytale.plugin.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.spacetivity.tobi.hylib.hytale.api.HyMessages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Test command to demonstrate message formatting features.
 * Use this command to create screenshots for CurseForge description.
 * 
 * Usage: /formattingtest
 */
public class FormattingTestCommand extends AbstractPlayerCommand {

    public FormattingTestCommand() {
        super("formattingtest", "Shows formatted message examples for screenshots");
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        // Example 1: Gradient
        playerRef.sendMessage(HyMessages.parse("<gradient:red:blue>Welcome to HyLib!</gradient>"));
        
        // Example 2: Multiple colors
        playerRef.sendMessage(HyMessages.parse("<green>Success!</green> <yellow>Player joined</yellow> <gray>the game</gray>"));
        
        // Example 3: Prefix with colors
        playerRef.sendMessage(HyMessages.parse("<blue>[<gold>HyLib<blue>]<gray> Your language has been changed!"));
        
        // Example 4: More gradient examples
        playerRef.sendMessage(HyMessages.parse("<gradient:green:blue>Server Status:</gradient> <green>Online</green>"));
        
        // Example 5: Mixed formatting
        playerRef.sendMessage(HyMessages.parse("<red>Error:</red> <yellow>Player not found</yellow>"));
        
        // Example 6: Complex example
        playerRef.sendMessage(HyMessages.parse("<gradient:purple:pink>HyLib</gradient> <gray>|</gray> <green>Ready</green> <gray>|</gray> <blue>Version 1.0.0</blue>"));
    }
}
