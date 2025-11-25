package dev.jsinco.malts.integration.compiled;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jsinco.malts.Malts;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.configuration.files.Lang;
import dev.jsinco.malts.integration.Integration;
import dev.jsinco.malts.utility.Couple;
import dev.jsinco.malts.utility.Text;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;


public class UpdateCheckIntegration implements Integration.Compiled {

    private static final String CONST_URL = "https://api.github.com/repos/BreweryTeam/Malts/releases/latest";
    private static final String DOWNLOAD_URL = "https://modrinth.com/plugin/malts/version/%s";
    private static final String CONST_JSON_FIELD = "tag_name";
    private static final String UNRESOLVED = "UNRESOLVED";

    @SuppressWarnings("UnstableApiUsage")
    private final String localVersion = Malts.getInstance().getPluginMeta().getVersion();
    private String resolvedLatestVersion = UNRESOLVED;
    private boolean updateAvailable = false;


    @Override
    public void register() {
        resolveLatest().thenAccept(resolved -> {
            this.resolvedLatestVersion = resolved;
            Text.debug("Resolved latest Malts version from GitHub: " + resolvedLatestVersion);

            int localVer = parseVersion(localVersion);
            int latestVer = parseVersion(resolvedLatestVersion);
            if (!resolvedLatestVersion.equals(UNRESOLVED) && latestVer > localVer) {
                this.updateAvailable = true;

                Lang lang = ConfigManager.get(Lang.class);
                Component updateMsg = lang.entry(Lang::updateAvailable, false,
                        Couple.of("{version}", localVersion),
                        Couple.of("{latest}", resolvedLatestVersion),
                        Couple.of("{url}", String.format(DOWNLOAD_URL, resolvedLatestVersion)));
                if (updateMsg != null) {
                    String msg = PlainTextComponentSerializer.plainText().serialize(updateMsg);
                    Text.log(msg);
                }
            }
        });
    }

    @Override
    public String name() {
        return "UpdateChecker";
    }


    @Override
    public boolean canRegister() {
        return ConfigManager.get(Config.class).updateChecker();
    }


    public boolean isUpdateAvailable() {
        return updateAvailable && ConfigManager.get(Config.class).updateChecker();
    }

    public void sendUpdateMessage(Audience audience) {
        Lang lang = ConfigManager.get(Lang.class);
        lang.entry(Lang::updateAvailable, audience,
                Couple.of("{version}", localVersion),
                Couple.of("{latest}", resolvedLatestVersion),
                Couple.of("{url}", String.format(DOWNLOAD_URL, resolvedLatestVersion))
        );
    }

    private CompletableFuture<String> resolveLatest() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CONST_URL))
                .GET()
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                return jsonResponse.get(CONST_JSON_FIELD).getAsString();
            } catch (IOException | InterruptedException e) {
                Text.warn("Failed to resolve latest Malts version from GitHub. (No connection?)");
                return UNRESOLVED;
            }
        });

    }

    private int parseVersion(String version) {
        StringBuilder sb = new StringBuilder();
        for (char c : version.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return Integer.parseInt(sb.toString());
    }
}
