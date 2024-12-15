/*
 * This java file handles printing ascii artwork for the user to help visualize the task.
 * 
 * Author: Nelson McFadyen
 * Last Updated: Dec, 04, 2024
 */
public class AsciiArt {

    // Method to display fridge ASCII art
    public static void showFridgeArt() {
        String fridge = """
         _______________________
        |                       |
        | What can we help make |
        |     for you today?    |
        |                    () |
        |_______________________|
        |_______________________|
        |                       |
        |   .-------.           |
        |   |       |           |
        |   |  | |  |           |
        |   |__|_|__|        () |
        |                       |
        |                       |
        |                       |
        |                       |
        |                       |
        |_______________________| 
        """;
        System.out.println(fridge);
    }

    // Add more artwork methods here if needed
}
