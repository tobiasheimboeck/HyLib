package dev.spacetivity.tobi.hylib.hytale.plugin.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.spacetivity.tobi.hylib.hytale.api.HyMessages;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LangKey;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LocalizationService;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Placeholder;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayer;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

// /language --name <language>
public class LanguageCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> nameArg;

    public LanguageCommand() {
        super("language", "Sets your language");

        this.nameArg = withRequiredArg("name", "name of language", ArgTypes.STRING);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        String newLanguageName = nameArg.get(commandContext);

        Lang newLang = Lang.of(newLanguageName);

        LocalizationService localizationService = HytaleProvider.getApi().getLocalizationService();

        if (!localizationService.isLanguageAvailable(newLang)) {
            HyMessages.sendTranslated(playerRef, LangKey.of("player.language.not_exist"), Placeholder.of("language", newLanguageName));
            return;
        }

        HyPlayerService playerService = HytaleProvider.getApi().getHyPlayerService();
        HyPlayer hyPlayer = playerService.getOnlineHyPlayer(playerRef);

        if (hyPlayer == null) {
            HyMessages.sendTranslated(playerRef, LangKey.of("player.not_exist"));
            return;
        }

        Lang currentLanguage = hyPlayer.getLanguage();

        if (currentLanguage.getCode().equalsIgnoreCase(newLanguageName)) {
            HyMessages.sendTranslated(playerRef, LangKey.of("player.language.already_selected"));
            return;
        }

        playerService.changeLanguage(playerRef.getUuid(), newLang);

        HyMessages.sendTranslated(playerRef, LangKey.of("player.language.changed"), Placeholder.of("language", newLang.getCode()));
    }
}