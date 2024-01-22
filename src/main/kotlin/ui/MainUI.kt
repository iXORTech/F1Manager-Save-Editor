package ui

import dto.Save

/*
 * MainUI.kt
 * F1Manager-Save-Editor
 *
 * Created by Qian Qian "Cubik" on Monday Jan. 22.
 */

class MainUI(val save: Save) {
    companion object {
        /**
         * A list containing the pairs of commands that can be used in the main menu.
         * Each pair contains the command itself and a description of the command.
         */
        val commandList: List<Pair<String, String>> =
            listOf(
                Pair("exit", "Exits the program.")
            )
    }

    /**
     * The main menu.
     */
    fun ui() {
        var command: String
        do {
            println("========================================")
            println("Main Menu")
            println("Enter a command to continue.")
            println("Commands:")
            commandList.forEach { println("${it.first}: ${it.second}") }
            print("> ")
            command = readln()
            println("========================================")
            if (commandList.find { it.first == command } == null) {
                println("Invalid command.")
                continue

            }
        } while (command != "exit")
    }
}
