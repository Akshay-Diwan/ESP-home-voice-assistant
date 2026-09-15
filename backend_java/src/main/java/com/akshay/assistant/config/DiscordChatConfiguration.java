package com.akshay.assistant.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DiscordChatConfiguration {
    @Value("${discord.bot.token}")
    private String token;

    @Bean
    public JDA discordJda() throws InterruptedException {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("DISCORD_BOT_TOKEN is not configured Token = " + token);
        }

        return JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .build()
                .awaitReady();
    }
}
