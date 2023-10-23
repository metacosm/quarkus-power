# Quarkus Power

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.power/quarkus-power?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.power/quarkus-power)

## Basic usage

NOTE: Currently only works on macOS. 

This extension is an experiment to measure and display the power consumption of your application as it runs in Dev mode.

To use the extension:
1. Clone this repository locally
2. Build the code using `mvn install`
3. Add the extension to the application which energy consumption you wish to measure. Since the extension is not yet released, you will need to add it manually as a dependency to your application:
    ```xml
   <dependency>
     <groupId>io.quarkiverse.power</groupId>
     <artifactId>quarkus-power</artifactId>
    <version>999-SNAPSHOT</version>
   </dependency>
   ``` 
4. Start your application in dev mode: `quarkus dev`
5. Enter the dev mode terminal by pressing `:` (column)
6. You should have a new `power` command available, type `power --help` for more information
7. You can start power measurement with `power start` and stop it with `power stop`