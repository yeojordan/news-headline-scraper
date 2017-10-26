# News Headline Scraper
COMP3003 Assignment: Scraper for News Headlines

---

### Summary

The News Headline Scraper, will retrieve headlines from plugins (news sources) and display them in a GUI for the user. The user has the ability to force an update of all plugins, that are loaded upon start up. As well as being able to cancel all active downloads. The GUI will display the current time in accordance with your machine's time and alert you of issues that arise during the running of the program.

---

### Running the Program

The following commands are to be run from the outer most directory containing the project wide *build.gradle* file.

#### Method 1: If gradle is installed

The following will create the jar file for the program

````
gradle build
````

Run the newly created jar

````
java -jar build/libs/NewsHeadlines.jar <plugin1> <plugin2> <plugin3>
````

The parameters for the above command should be the name of the plugin's (all lowercase), the same as their plugin name. Space separated. ie. arstechnica bbc nytimes

Removing the artifacts built

````
gradle clean
````

---

#### Method 2: If gradle is not installed

Included in the repository is a gradle wrapper.

The following will create the jar file for the program

````
./gradlew build
````

Run the newly created jar

````
java -jar build/libs/NewsHeadlines.jar <plugin1> <plugin2> <plugin3>
````

The parameters for the above command should be the name of the plugin's (all lowercase), the same as their plugin name. Space separated. ie. arstechnica bbc nytimes

Removing the artifacts built

````
./gradlew clean
````

---

### Extensibility

For future plugins to be added, besides the functionality needing to be correct, the name of the plugin/sub-project should match that of the corresponding java file.
