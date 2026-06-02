# Quarkus Power

[![Version](https://img.shields.io/maven-central/v/net.laprun.sustainability/quarkus-power?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/net.laprun.sustainability/quarkus-power)

This extension is an experiment to measure and display the power consumption of your application as it runs in Dev mode.
Only Linux/amd64 and macOS (amd64/apple silicon) are supported at the moment. See below for platform-specific
requirements.

## Requirements

### power-server

This extension relies on retrieving power consumption measurements from an application
called [power-server](https://github.com/metacosm/power-server). As the extension was being developed, it became obvious that more work was required on the measuring backend. As a result, the work on the extension got paused while issues where addressed on `power-server`. This means that this extension is still very rough and is expected to have bugs. This also means that it currently requires an older version of `power-server`, which you can find here: https://github.com/metacosm/power-server/releases/tag/0.2.2.2. Please download the appropriate version for your OS.

Once downloaded, you can unpack the archive and navigate to the `bin` directory to find the `power-server` binary that you can then run
using: `sudo power-server`. `sudo` to get access to the underlying OS' power reporting layer

Note:
On macOS, you will most likely get a dialog telling you that the application cannot be opened because Apple cannot
verify it. You can follow the steps as described [here](https://www.macobserver.com/tips/how-to/fixing-macos-cannot-verify-app-free-malware/) to work around this issue, or you can issue the following command, in the `bin` directory:

```shell
sudo xattr -rd com.apple.quarantine ./power-server
```

## Usage

To use the extension:

1. Add the extension to the application which energy consumption you wish to measure. You can add it as a dependency to
   your application:
    ```xml
   <dependency>
     <groupId>net.laprun.sustainability</groupId>
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