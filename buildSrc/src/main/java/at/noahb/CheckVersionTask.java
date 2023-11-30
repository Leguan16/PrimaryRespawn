package at.noahb;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.WriteProperties;

import java.util.Objects;

public class CheckVersionTask extends DefaultTask {


    @TaskAction
    public void checkVersion() {
        String version = getProject().getVersion().toString();

    }
}

