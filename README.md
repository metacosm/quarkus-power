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
4. You can either interact with the extension via the terminal using the `power` command, type `power -h` for more information or via the Dev UI, by pressing `d` to open the UI in a browser window and then clicking on the `Measure` link of the `Power` card. Either way, you can start and stop power measurement and (hopefully) see the results. If you have methods annotated with the extension-provided `@PowerMeasure` annotation, the power for each invocation should show up in the `Measures` part of the `Power` UI (note that there is currently an issue with the results display).
