package dev.playersarmor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
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
import java.util.List;
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
                    .refreshAfterWrite(Duration.ofMinutes(5))
                    .build(new CacheLoader<>() {
                        @Override
                        public CompletableFuture<Optional<SkinTextures>> load(String username) {
                            return PlayerSkinCache.load(username);
                        }

                        @Override
                        public ListenableFuture<CompletableFuture<Optional<SkinTextures>>> reload(
                                String username, CompletableFuture<Optional<SkinTextures>> previous) {
                            return retainLoadedSkin(previous, PlayerSkinCache.load(username));
                        }
                    });

    private PlayerSkinCache() {
    }

    public static int reload() {
        List<String> usernames = List.copyOf(CACHE.asMap().keySet());
        CACHE.invalidateAll();
        usernames.forEach(CACHE::getUnchecked);
        return usernames.size();
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

    private static ListenableFuture<CompletableFuture<Optional<SkinTextures>>> retainLoadedSkin(
            CompletableFuture<Optional<SkinTextures>> previous,
            CompletableFuture<Optional<SkinTextures>> refresh) {
        SettableFuture<CompletableFuture<Optional<SkinTextures>>> result = SettableFuture.create();
        refresh.whenComplete((skin, error) -> result.set(error == null && skin != null && skin.isPresent()
                ? CompletableFuture.completedFuture(skin)
                : previous));
        return result;
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
