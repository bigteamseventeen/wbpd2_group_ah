# YR3_WEB_PLATFORM_DEVELOPMENT_2

Web Platform Development 2 (M3I322955-18-B) 
LAB 1

To execute the web server you need to open up this folder inside Intellij IDEA and allow 
it to update all maven projects. After Maven is updated, you need to add a new Build Configuration and set 
the main class to `uk.ac.alc.wpd2.callumcarmicheal.messageboard.web.Main` and the working directory to the bin 
folder as this contains all the templates.

As for the console, this requires Command Prompt or a Linux terminal, a error will be thrown if launched from inside
intellij as the in IDE terminal does not work with `system.console()` 

To launch the application from a terminal (console mode), open a command prompt in the `target` folder and 
type `java uk.ac.alc.wpd2.callumcarmicheal.messageboard.console.Main`.