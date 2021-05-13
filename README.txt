iceybones Scheduler - A simple customer and appointment management application.
Author: Christopher Griswold - cgrisw4@wgu.edu
Version: 1.0
Date: 05/06/2021
IDE: IntelliJ Community 2021.1.1
JDK: openjdk version "11.0.8" 2020-07-14 LTS
JavaFX: JavaFX-SDK-11.0.2
MySQL Connector: mysql-connector-java-8.0.22
How to run on Windows: With Java 11 installed, simply double click the Launcher_WIN.jar file.
How to run on Linux: With Java 11 installed, open a terminal at this directory and run the command "java -jar Launcher.jar".
How to run inside IntelliJ: With all the necessary componens installed and configured, create a new run configuration and 
	add the following VM options "--module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml". Press the green RUN icon.
The additional report that is included in the reporting dashboard is a visual representation of the relative time each contact has spent in meetings(appointments). In essence this illustrates who is putting the most time in and who may be slaking off.
NOTE: Use the clock button in the upper right corner of the application window to view appointments by month and week. TIP: Hover over buttons to vew a tooltip explaining their function.
NOTE: A webinar was put out recently stating that overlapping appointment restrictions are only in regards to customers, and that both contacts and users must be able to be in multiple appointments at the same time. This seems counter intuitive but after speaking with a course instructor they confirmed that to be the case, although there has been some confusion on the subject. I went ahead and implemented that behavior but feel compelled to mention it as I just worry that it could be a bit of a gray area. Likewise with the appointment notifications upon login only applying to the user that has logged in. For example, logging in as "test" will not trigger notifications for any appointments that have been sheduled for the other user "admin". Anyway, I hope you enjoy using the application as much as I did building it!
