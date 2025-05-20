package io.github.francescodonnini.collectors.ast;


import com.sun.source.util.JavacTask;
import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.CsvJavaClassApi;
import io.github.francescodonnini.model.JavaClass;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CodeDuplicationCounterTest {
    static Random random = new Random();

    public static void main(String[] args) throws ConfigurationException, IOException, GitAPIException {
        if (args.length != 2) {
            System.exit(-1);
        }
        var settings = new IniSettings(args[0]);
        var classes = getClasses(Path.of(settings.getString("dataPath"), args[1].toUpperCase()));
        var o = getRandomClass(classes);
        if (o.isEmpty()) {
            return;
        }
        var clazz = o.get();
        var file = createTempFile(Path.of(settings.getString("gitBasePath"), args[1].toLowerCase()).toString(), clazz);
        var compiler = ToolProvider.getSystemJavaCompiler();
        var units = compiler
                .getStandardFileManager(null, null, null)
                .getJavaFileObjects(file);
        var task = (JavacTask) compiler.getTask(null, null, null, null, null, units);
        var codeDuplicationCounter = new CodeDuplicationCounter();
        for (var cu : task.parse()) {
            cu.accept(codeDuplicationCounter, null);
            codeDuplicationCounter.getFlatTree().forEach(System.out::println);
        }
    }

    private static Optional<JavaClass> getRandomClass(List<JavaClass> classes) {
        var i = random.nextInt(classes.size());
        return Optional.ofNullable(classes.get(i));
    }

    private static Path createTempFile(String projectPath, JavaClass clazz) throws IOException, GitAPIException {
        var git = createGit(projectPath);
        try {
            checkout(git, clazz.getCommit());
            var file = Files.createTempFile(null, ".java");
            try (var fr = new FileReader(clazz.getAbsolutePath().toFile());
                 var out = new FileWriter(file.toFile())) {
                var buffer = new char[4096];
                while (fr.read(buffer) != -1) {
                    out.write(buffer);
                }
            }
            return file;
        } finally {
            checkout(git, "master");
            git.close();
        }
    }

    private static Git createGit(String projectPath) throws IOException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(projectPath, ".git"))
                .build();
        return new Git(repository);
    }

    private static void checkout(Git git, String branch) throws GitAPIException {
        git.checkout().setName(branch).call();
    }

    private static List<JavaClass> getClasses(Path dataPath) throws IOException {
        var api = new CsvJavaClassApi(dataPath.resolve("classes.csv").toString());
        return api.getLocal();
    }
}