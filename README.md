# Quarkus Power

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.power/quarkus-power?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.power/quarkus-power)

## Basic usage

NOTE: Currently only works on macOS. The power monitoring is performed using the bundled `powermetrics` tool, which
requires `sudo` access. For convenience and security, it's recommended you add your user to the `sudoers` file, giving
it passwordless access to `/usr/bin/powermetrics` (and possibly, only that).

This extension is an experiment to measure and display the power consumption of your application as it runs in Dev mode.

To use the extension:

1. Clone this repository locally
2. Build the code using `mvn install`
3. Add the extension to the application which energy consumption you wish to measure. Since the extension is not yet
   released, you will need to add it manually as a dependency to your application:
    ```xml
   <dependency>
     <groupId>io.quarkiverse.power</groupId>
     <artifactId>quarkus-power</artifactId>
    <version>999-SNAPSHOT</version>
   </dependency>
   ``` 
4. Start your application in dev mode: `quarkus dev`
5. Enter the dev mode terminal by pressing `:` (column)
6. You should have a new `power` command available, type `power -h` for more information
7. You can start power measurement with `power start` and stop it with `power stop`, at which time the power consumption
   of your app will be displayed.
8. You can also ask for power to be measured for a given duration by using the `-s` option when
   calling `power start`. In this case, there's no need to call `power stop`, the energy consumed during the specified
   time will be automatically displayed once the time period is elapsed.