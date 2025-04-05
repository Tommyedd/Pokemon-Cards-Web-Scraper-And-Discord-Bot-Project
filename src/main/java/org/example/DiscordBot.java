package org.example;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import javax.security.auth.login.LoginException;

public class DiscordBot {

    public static void main(String[] args) throws LoginException {
        // Initialize the Discord bot
        JDA jda = JDABuilder.createDefault("") // bot token goes here
                .setActivity(Activity.listening("to the pokemon deals"))
                .addEventListeners(new BotListeners())  // Add listeners without passing JDA here
                .build();

        // The bot is now ready to process events
    }
}

