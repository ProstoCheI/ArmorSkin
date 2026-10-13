package dev.playersarmor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.util.UndashedUuid;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class PlayerSkinCache {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final LoadingCache<String, CompletableFuture<Optional<SkinTextures>>> CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(256)
                    .expireAfterWrite(Duration.ofMinutes(5))
                    .build(CacheLoader.from(PlayerSkinCache::load));

    private PlayerSkinCache() {
    }

    @Nullable
    public static SkinTextures getSkin(String username) {
        if (!ArmorNameParser.isValidUsername(username)) {
            return null;
        }

        return CACHE.getUnchecked(username.toLowerCase(Locale.ROOT))
                .getNow(Optional.empty())
                .orElse(null);
    }

    private static CompletableFuture<Optional<SkinTextures>> load(String username) {
        MinecraftClient client = MinecraftClient.getInstance();
        return CompletableFuture.supplyAsync(() -> findProfile(client, username), Util.getIoWorkerExecutor())
                .thenComposeAsync(profile -> profile == null
                        ? CompletableFuture.<SkinTextures>completedFuture(null)
                        : client.getSkinProvider().fetchSkinTextures(profile), client)
                .handle((skin, error) -> {
                    if (error != null) {
                        PlayerArmorClient.LOGGER.warn("Failed to load skin for {}", username, error);
                    }
                    return Optional.ofNullable(skin);
                });
    }

    @Nullable
    private static GameProfile findProfile(MinecraftClient client, String username) {
        try {
            HttpResponse<String> response = lookupUsername(
                    "https://api.minecraftservices.com/minecraft/profile/lookup/name/", username);
            if (response.statusCode() == 403) {
                response = lookupUsername("https://api.mojang.com/users/profiles/minecraft/", username);
            }

            if (response.statusCode() == 404 || response.statusCode() == 204) {
                return null;
            }
            if (response.statusCode() != 200) {
                throw new IOException("Profile lookup returned HTTP " + response.statusCode());
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            ProfileResult result = client.getSessionService()
                    .fetchProfile(UndashedUuid.fromStringLenient(json.get("id").getAsString()), true);
            return result == null ? null : result.profile();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    private static HttpResponse<String> lookupUsername(String endpoint, String username)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint + username))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
