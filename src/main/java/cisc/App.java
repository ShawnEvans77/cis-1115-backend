package cisc;

import io.javalin.Javalin;
import java.io.*;

public class App {
    public static void main(String[] args) throws IOException {

        // javal
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {

                cors.add(it -> it.anyHost());
            });
        }).start(7070);

        app.get("/", ctx -> ctx.result("Backend is working!"));

        app.post("/submit", ctx -> {

            try {
                // we fetching the code from react monaco and playing with it
                String userCode = ctx.body();

                fileWrite(userCode);
                // this is for building the command javac Main.java compiling the program
                Process compile = new ProcessBuilder("javac", "Main.java").directory(new File("temp"))
                        .redirectErrorStream(true).start();

                int compileExit = compile.waitFor();

                if (compileExit != 0) {
                    // this is a compiler error handler. waitFor() returns exit value of whatever we
                    // did and lets u know if it worked
                    // this is for when it got compiler error
                    BufferedReader reader = new BufferedReader(new InputStreamReader(compile.getInputStream()));
                    StringBuilder errorMessage = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) { // this is to construct error msg
                        errorMessage.append(line).append("\n");
                    }

                    ctx.status(400).result("Compile error:\n" + errorMessage.toString());
                } else {
                    ctx.result("it compiled");

                    // if exit code isnt 0 this means everythings fine and it compiled

                    Process run = new ProcessBuilder("java", "Main").directory(new File("temp"))
                            .redirectErrorStream(true).start();

                    int runExit = run.waitFor();

                    BufferedReader outputReader = new BufferedReader(new InputStreamReader(run.getInputStream()));

                    StringBuilder output = new StringBuilder();

                    String line;

                    while ((line = outputReader.readLine()) != null) {
                        output.append(line).append("\n");
                    }

                    if (runExit != 0) {
                        ctx.status(400).result("Runtime error:\n" + output.toString());

                    } else {
                        ctx.result("output: " + output.toString());
                    }

                }

            } catch (Exception e) {
                ctx.status(500).result("it doesnt work lil bro: " + e.getMessage());

            }
        });
    }

    // i chose printwriter and throwing ioexception for old times sake
    // is kinda funny
    // the final project i ever did as cs undergrad, im using file file = new file,
    // throws exception
    // printwriter
    // its all the basics i learned from cs1115 appearing one last time as a senior

    // anyway, this is a function for creating the java file which we are executing
    // its based on the code the user sends on the frontend.
    public static void fileWrite(String code) throws IOException {
        File file = new File("temp");

        if (!file.exists()) {
            file.mkdir();
        }

        File javaFile = new File(file, "Main.java");

        PrintWriter writer = new PrintWriter(javaFile);

        writer.print(code);

        writer.close();
    }
}
