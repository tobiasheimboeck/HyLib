package dev.spacetivity.tobi.database.hytale.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import dev.spacetivity.tobi.database.api.DatabaseProvider;
import dev.spacetivity.tobi.database.hytale.repository.TestRepository;


/**
 * Demo Command für das TestRepository.
 * Einfache Demo: Fügt beim Ausführen eine Test-Entity in die Datenbank ein.
 * 
 * Verwendung: /tobitest
 */
public class TestDbCommand extends AbstractPlayerCommand {

    private final TestRepository repository;

    public TestDbCommand() {
        super("tobitest", "Test Command für Database Repository Demo");
        // Hole Repository über RepositoryLoader - unterstützt Subtypen ohne Cast
        this.repository = DatabaseProvider.getApi()
                .getRepositoryLoader()
                .getRepository(TestRepository.class);
    }

    @Override
    public void execute(CommandContext commandContext, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
        TestRepository.TestEntity entity = new TestRepository.TestEntity("Demo", "Test Value");
        repository.insert(entity);
        
        sendMessage(playerRef, "Demo Entity erfolgreich eingefügt!");
        sendMessage(playerRef, "Name: Demo");
        sendMessage(playerRef, "Value: Test Value");
    }

    private void sendMessage(PlayerRef playerRef, String message) {
        playerRef.sendMessage(Message.raw(message));
    }
}
