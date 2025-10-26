Project paused until i figure how to run the processor automatically ( without waiting for compile ) or how to make autocompletion available before the code generation.
Because i'll be able to generate the hibernate code, but without autocompletion it's just useless
maven depedencies :
```xml
    <dependencies>
        <dependency>
            <groupId>fr.daliush.searchable</groupId>
            <artifactId>searchable-annotations</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source><target>21</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>fr.daliush.searchable</groupId>
                            <artifactId>searchable-processor</artifactId>
                            <version>1.0-SNAPSHOT</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
```
