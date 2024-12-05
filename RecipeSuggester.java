/*
 * This java file handles asking the user for ingredients they wish to make a dish for.
 * After asking for all ingredients from the user, it shows them dishes they can make with the ingredients.
 * While also showing them the steps required to make it.
 * 
 * Author: Nelson McFadyen
 * Last Updated: Dec, 04, 2024
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RecipeSuggester {

    private static final String API_URL = "https://www.themealdb.com/api/json/v1/1/filter.php?i=";

    public static void main(String[] args) {
        // Force IPv4
        System.setProperty("java.net.preferIPv4Stack", "true");
        List<String> ingredients = new ArrayList<>();

        System.out.println("Welcome to the Recipe Suggester!");
        System.out.println();

        // Use the AsciiArt class to display the fridge
        AsciiArt.showFridgeArt();

        System.out.println("Enter ingredients one by one. Type 'done' when you are finished:");
    
        // Use try-with-resources to ensure the Scanner is closed
        try (Scanner scanner = new Scanner(System.in)) {
            // Collect ingredients from the user
            while (true) {
                System.out.print("Ingredient: ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("done")) {
                    break;
                }
                if (!input.isEmpty()) {
                    ingredients.add(input);
                }
            }
        } // Scanner is automatically closed here
    
        if (ingredients.isEmpty()) {
            System.out.println("No ingredients entered. Exiting...");
            return;
        }
    
        System.out.println("Searching for recipes...");
        String ingredientQuery = String.join(",", ingredients);
    
        // Fetch recipes
        try {
            String response = sendHttpRequest(ingredientQuery);
            displayRecipes(response);
        } catch (Exception e) {
            System.out.println("Error fetching recipes: " + e.getMessage());
        }
    }

    // Method to send HTTP request
    private static String sendHttpRequest(String ingredients) throws Exception {
        String urlString = API_URL + ingredients;

        // Create a URI and convert it to a URL
        URI uri = new URI(urlString);
        URL url = uri.toURL();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            output.append(line);
        }
        conn.disconnect();
        return output.toString();
    }

    // Method to display recipes from the JSON response
    private static void displayRecipes(String jsonResponse) {
        if (jsonResponse.contains("\"meals\":null")) {
            System.out.println("No recipes found with the given ingredients.");
            return;
        }

        System.out.println("Recipes that can be made with the given ingredients:");
        String[] meals = jsonResponse.split("\"strMeal\":\"");
        for (int i = 1; i < meals.length; i++) {
            String meal = meals[i].split("\"")[0];  // Extract recipe name
            System.out.println("- " + meal);
        }
    }
}
