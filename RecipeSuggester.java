/*
 * This java file handles asking the user for a main ingredient they wish to make a dish for.
 * If no recipes are found for the given ingredient, it prompts the user to enter another one.
 * Once a valid ingredient is found, it shows dishes that can be made with it and allows the user to select one to see detailed information.
 * 
 * Author: Nelson McFadyen
 * Last Updated: Dec, 06, 2024
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
    private static final String RECIPE_DETAILS_URL = "https://www.themealdb.com/api/json/v1/1/lookup.php?i=";

    public static void main(String[] args) {
        // Force IPv4
        System.setProperty("java.net.preferIPv4Stack", "true");

        System.out.println("Welcome to the Recipe Suggester!");
        System.out.println();

        // Use the AsciiArt class to display the fridge
        AsciiArt.showFridgeArt();

        // Use try-with-resources to ensure the Scanner is closed
        try (Scanner scanner = new Scanner(System.in)) {
            String mainIngredient;
            boolean validIngredientFound = false;

            while (!validIngredientFound) {
                System.out.println("Enter the main ingredient you want to cook with:");
                mainIngredient = scanner.nextLine().trim();

                if (mainIngredient.isEmpty()) {
                    System.out.println("Ingredient cannot be empty. Please try again.");
                    continue;
                }

                System.out.println("Searching for recipes with: " + mainIngredient + "...");

                try {
                    String response = sendHttpRequest(API_URL + mainIngredient);

                    if (response.contains("\"meals\":null")) {
                        System.out.println("No recipes found with the ingredient: " + mainIngredient);
                        System.out.println("Please enter a different ingredient.");
                    } else {
                        List<String[]> recipes = displayRecipes(response);
                        validIngredientFound = true;
                        promptRecipeSelection(recipes, scanner);
                    }

                } catch (Exception e) {
                    System.out.println("Error fetching recipes: " + e.getMessage());
                }
            }
        }
    }

    // Method to send HTTP request
    private static String sendHttpRequest(String urlString) throws Exception {
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
    private static List<String[]> displayRecipes(String jsonResponse) {
        System.out.println("Recipes that can be made with the given ingredient:");
        String[] meals = jsonResponse.split("\"strMeal\":\"");
        List<String[]> recipes = new ArrayList<>();

        for (int i = 1; i < meals.length; i++) {
            String meal = meals[i].split("\"")[0]; // Extract recipe name
            String id = meals[i].split("\"idMeal\":\"")[1].split("\"")[0]; // Extract recipe ID
            recipes.add(new String[] { meal, id });
            System.out.println(i + ". " + meal);
        }
        return recipes;
    }

    // Method to prompt the user to select a recipe for more details
    private static void promptRecipeSelection(List<String[]> recipes, Scanner scanner) {
        System.out.println("\nEnter the number of the recipe you want to see more details for:");
        int choice;

        while (true) {
            try {
                System.out.print("Choice: ");
                choice = Integer.parseInt(scanner.nextLine().trim());

                if (choice < 1 || choice > recipes.size()) {
                    System.out.println("Invalid choice. Please select a valid recipe number.");
                } else {
                    break;
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        String[] selectedRecipe = recipes.get(choice - 1);
        fetchRecipeDetails(selectedRecipe[1]);
    }

    // Method to fetch and display detailed information about a recipe
    private static void fetchRecipeDetails(String recipeId) {
        System.out.println("Fetching details for the selected recipe...");

        try {
            String response = sendHttpRequest(RECIPE_DETAILS_URL + recipeId);

            // Display detailed information
            String meal = response.split("\"strMeal\":\"")[1].split("\"")[0];
            String instructions = response.split("\"strInstructions\":\"")[1].split("\",\"")[0];

            System.out.println("\nRecipe: " + meal);
            System.out.println("Instructions:\n" + formatInstructions(instructions));

        } catch (Exception e) {
            System.out.println("Error fetching recipe details: " + e.getMessage());
        }
    }

    // Helper method to format instructions for better readability and consistent
    // numbering
    private static String formatInstructions(String instructions) {
        // Clean and normalize the text
        String cleanedInstructions = instructions
                .replace("\\r\\n", "\n") // Replace Windows-style newlines
                .replace("\\n", "\n")   // Replace other newline styles
                .replaceAll("\\\\u00d7", "x") // Replace Unicode multiplication symbol
                .replaceAll("\\\\u200b", "") // Remove zero-width spaces
                .replaceAll("\\\\t|\\t", "") // Remove escaped tabs and actual tab characters
                .replaceAll("\\\\", "")      // Remove extraneous backslashes
                .trim(); // Remove leading and trailing whitespace
    
        // Split into steps based on existing newlines or sentence-ending punctuation
        String[] steps = cleanedInstructions.split("(?<=[.!?])\\s+(?=[A-Z0-9])"); 
    
        // Ensure all instructions are numbered with consistent spacing
        StringBuilder numberedInstructions = new StringBuilder();
        int stepNumber = 1;
    
        for (String step : steps) {
            step = step.trim();
            if (!step.isEmpty()) {
                // Add numbering if not already numbered
                if (!step.matches("^\\d+\\.\\s.*")) {
                    step = stepNumber + ". " + step;
                }
                numberedInstructions.append(step).append("\n");
                stepNumber++;
            }
        }
    
        return numberedInstructions.toString().trim();
    }
}
