package org.example;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.JDA;

public class BotListeners extends ListenerAdapter {

    private JDA jda;  // Declare JDA variable here

    // No need to pass jda in the constructor, we can access it from the event
    @Override
    public void onReady(ReadyEvent event) {
        // This is triggered when the bot is successfully logged in and ready
        jda = event.getJDA();  // Now we initialize jda from the event
        System.out.println("Bot is ready and connected to Discord!");

        // Start the stock checking process when the bot is ready
        startStockChecking();  // Start the stock checking without passing jda
    }

    // Start the stock checking process by calling the PokemonScraper's method
    private void startStockChecking() {
        System.out.println("Starting the stock checking process...");
        PokemonScraper.startCheckingStock(jda);  // Now we use the initialized jda
    }
}

