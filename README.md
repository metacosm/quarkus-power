# Quarkus Power

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.power/quarkus-power?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.power/quarkus-power)

This extension is an experiment to measure and display the power consumption of your application as it runs in Dev mode.
Only Linux/amd64 and macOS (amd64/apple silicon) are supported at the moment. See below for platform-specific
requirements.

## Requirements

### power-server

This extension relies on retrieving power consumption measurements from an application
called [power-server](https://github.com/metacosm/power-server). This application needs to be run using `sudo` to get
access to the underlying OS' power reporting layer. Please download the appropriate version for your OS from
the [release page](https://github.com/metacosm/power-server/releases/). Once downloaded, you can unpack
the archive and navigate to the `bin` directory to find the `power-server` binary that you can then run
using: `sudo power-server`.

## Usage

To use the extension:

1. Add the extension to the application which energy consumption you wish to measure. You can add it as a dependency to
   your application:
    ```xml
   <dependency>
     <groupId>net.laprun.sustainability.power.quarkus</groupId>
     <artifactId>quarkus-power</artifactId>
    <version>${project.version}</version>
   </dependency>
   ``` 
2. Start your application in dev mode: `quarkus dev`
3. Enter the dev mode terminal by pressing `:` (column)
4. You should have a new `power` command available, type `power -h` for more information
5. You can start power measurement with `power start` and stop it with `power stop`, at which time the power consumption
   of your app will be displayed.
6. You can also ask for power to be measured for a given duration by using the `-s` option when
   calling `power start`. In this case, there's no need to call `power stop`, the energy consumed during the specified
   time will be automatically displayed once the time period is elapsed.