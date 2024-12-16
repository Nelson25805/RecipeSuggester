import java.io.BufferedReader;
import java.io.FileWriter;
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

        System.out.println();
        System.out.println("Welcome to the Recipe Suggester!");
        System.out.println();
        AsciiArt.showFridgeArt();

        // Use the AsciiArt class to display the fridge (if available)
        // AsciiArt.showFridgeArt();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("What would you like to do?");
                System.out.println("1. Search for recipes by ingredient");
                System.out.println("2. Exit");
                System.out.print("Choice: ");

                String choice = scanner.nextLine().trim();

                if ("1".equals(choice)) {
                    searchForRecipes(scanner);
                } else if ("2".equals(choice)) {
                    System.out.println("Thank you for using Recipe Suggester! Goodbye!");
                    break;
                } else {
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            }
        }
    }

    private static void searchForRecipes(Scanner scanner) {
        boolean validIngredientFound = false;

        while (!validIngredientFound) {
            System.out.println(
                    "Enter the main ingredient you want to cook with (or type 'exit' to return to the main menu):");
            String mainIngredient = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(mainIngredient)) {
                System.out.println("Returning to the main menu...");
                return;
            }

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

    private static String sendHttpRequest(String urlString) throws Exception {
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

    private static List<String[]> displayRecipes(String jsonResponse) {
        System.out.println("Recipes that can be made with the given ingredient:");
        String[] meals = jsonResponse.split("\"strMeal\":\"");
        List<String[]> recipes = new ArrayList<>();

        for (int i = 1; i < meals.length; i++) {
            String meal = meals[i].split("\"")[0];
            String id = meals[i].split("\"idMeal\":\"")[1].split("\"")[0];
            recipes.add(new String[] { meal, id });
            System.out.println(i + ". " + meal);
        }
        return recipes;
    }

    private static void promptRecipeSelection(List<String[]> recipes, Scanner scanner) {
        while (true) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. View details for a recipe");
            System.out.println("2. Search for another ingredient");
            System.out.println("3. Return to the main menu");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                selectRecipe(recipes, scanner);
            } else if ("2".equals(choice)) {
                return;
            } else if ("3".equals(choice)) {
                System.out.println("Returning to the main menu...");
                return;
            } else {
                System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private static void selectRecipe(List<String[]> recipes, Scanner scanner) {
        int choice;

        while (true) {
            try {
                System.out.println("Enter the number of the recipe you want to see more details for:");
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
        fetchRecipeDetails(selectedRecipe[1], scanner);
    }

    private static void fetchRecipeDetails(String recipeId, Scanner scanner) {
        System.out.println("Fetching details for the selected recipe...");

        try {
            String response = sendHttpRequest(RECIPE_DETAILS_URL + recipeId);

            // Extract recipe details
            String meal = response.split("\"strMeal\":\"")[1].split("\"")[0];
            String instructions = response.split("\"strInstructions\":\"")[1].split("\",\"")[0];
            String mealThumb = response.split("\"strMealThumb\":\"")[1].split("\"")[0];

            // Extract ingredients and measures
            List<String> ingredients = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                String ingredientKey = "\"strIngredient" + i + "\":\"";
                String measureKey = "\"strMeasure" + i + "\":\"";

                if (response.contains(ingredientKey) && response.contains(measureKey)) {
                    String ingredient = response.split(ingredientKey)[1].split("\"")[0];
                    String measure = response.split(measureKey)[1].split("\"")[0];
                    if (!ingredient.isBlank() && !measure.isBlank()) {
                        ingredients.add(measure + " " + ingredient);
                    }
                }
            }

            String formattedInstructions = formatInstructions(instructions);

            // Display recipe details
            System.out.println("\nRecipe: " + meal);
            System.out.println("Ingredients:");
            for (String ingredient : ingredients) {
                System.out.println("- " + ingredient);
            }
            System.out.println("\nInstructions:\n" + formattedInstructions);
            System.out.println("\nRecipe Thumbnail: " + mealThumb);

            // Save recipe to file
            saveRecipeToFile(meal, formattedInstructions, ingredients, mealThumb, scanner);

        } catch (Exception e) {
            System.out.println("Error fetching recipe details: " + e.getMessage());
        }
    }

    private static String formatInstructions(String instructions) {
        String cleanedInstructions = instructions
                .replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replaceAll("\\\\u00d7", "x")
                .replaceAll("\\\\u00b0", "°")
                .replaceAll("\\\\u2013", "-")
                .replaceAll("\\\\u00e9", "é")
                .replaceAll("\\\\u2019", "'")
                .replaceAll("\\\\u200b", "")
                .replaceAll("\\\\t|\\t", "")
                .replaceAll("\\\\", "")
                .trim();

        String[] steps = cleanedInstructions.split("(?<=[.!?])\s+(?=[A-Z0-9])");
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

    private static void saveRecipeToFile(String recipeName, String instructions, List<String> ingredients,
            String thumbnailUrl, Scanner scanner) {
        System.out.println("\nWould you like to save this recipe for later? (yes/no)");
        String userResponse = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(userResponse)) {
            java.io.File recipesDir = new java.io.File("Recipes");
            if (!recipesDir.exists() && !recipesDir.mkdir()) {
                System.out.println("Failed to create directory: Recipes");
                return;
            }

            String baseFileName = recipeName.replaceAll("[^a-zA-Z0-9\\s]", "").replace(" ", "_");
            String fileName = baseFileName + ".txt";
            java.io.File recipeFile = new java.io.File(recipesDir, fileName);

            int counter = 1;
            while (recipeFile.exists()) {
                fileName = baseFileName + "_" + counter + ".txt";
                recipeFile = new java.io.File(recipesDir, fileName);
                counter++;
            }

            try (FileWriter writer = new FileWriter(recipeFile)) {
                writer.write("Recipe: " + recipeName + "\n\n");
                writer.write("Ingredients:\n");
                for (String ingredient : ingredients) {
                    writer.write("- " + ingredient + "\n");
                }
                writer.write("\nInstructions:\n" + instructions + "\n\n");
                writer.write("Recipe Thumbnail: " + thumbnailUrl + "\n");
                System.out.println("Recipe saved to file: " + recipeFile.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Error saving the recipe: " + e.getMessage());
            }
        } else {
            System.out.println("Recipe not saved.");
        }
    }

}
