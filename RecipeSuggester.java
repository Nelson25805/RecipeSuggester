/*
 * This java file handles asking the user for a main ingredient they wish to make a dish for.
 * If no recipes are found for the given ingredient, it prompts the user to enter another one.
 * Once a valid ingredient is found, it shows dishes that can be made with it.
 * 
 * Author: Nelson McFadyen
 * Last Updated: Dec, 06, 2024
 */

 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URL;
 import java.util.Scanner;
 
 public class RecipeSuggester {
 
     private static final String API_URL = "https://www.themealdb.com/api/json/v1/1/filter.php?i=";
 
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
                     String response = sendHttpRequest(mainIngredient);
 
                     if (response.contains("\"meals\":null")) {
                         System.out.println("No recipes found with the ingredient: " + mainIngredient);
                         System.out.println("Please enter a different ingredient.");
                     } else {
                         displayRecipes(response);
                         validIngredientFound = true;
                     }
 
                 } catch (Exception e) {
                     System.out.println("Error fetching recipes: " + e.getMessage());
                 }
             }
         }
     }
 
     // Method to send HTTP request
     private static String sendHttpRequest(String ingredient) throws Exception {
         String urlString = API_URL + ingredient;
 
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
         System.out.println("Recipes that can be made with the given ingredient:");
         String[] meals = jsonResponse.split("\"strMeal\":\"");
         for (int i = 1; i < meals.length; i++) {
             String meal = meals[i].split("\"")[0];  // Extract recipe name
             System.out.println("- " + meal);
         }
     }
 }
 