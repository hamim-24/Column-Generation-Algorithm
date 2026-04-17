import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Error: CPLEX jar and library path must be provided.");
            return;
        }

        String cplexJar = args[0];
        String cplexLib = args[1];
        Scanner scanner = new Scanner(System.in);
        Path root = Paths.get("").toAbsolutePath().normalize();
        Path cspBin = root.resolve("bin/csp");
        Path vrpBin = root.resolve("bin/vrp");
        Path cspDir = root.resolve("CSP");
        Path vrpDir = root.resolve("VRP");

        if (!cspBin.toFile().exists() || !vrpBin.toFile().exists()) {
            System.err.println("Error: Compiled module bins not found.");
            if (!cspBin.toFile().exists()) {
                System.err.println("Missing: " + cspBin);
            }
            if (!vrpBin.toFile().exists()) {
                System.err.println("Missing: " + vrpBin);
            }
            scanner.close();
            return;
        }

        while (true) {
            System.out.println("\n=== Column Generation Project Menu ===");
            System.out.println("1) CSP");
            System.out.println("2) VRP");
            System.out.println("3) Exit");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    launchProject("CSPMain", cspBin, cspDir, cplexJar, cplexLib);
                    break;
                case "2":
                    launchProject("VRPMain", vrpBin, vrpDir, cplexJar, cplexLib);
                    break;
                case "3":
                    System.out.println("Exiting.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private static void launchProject(String mainClass, Path classpathDir, Path workingDir, String cplexJar, String cplexLib) {
        try {
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("--enable-native-access=ALL-UNNAMED");
            command.add("-cp");
            command.add(classpathDir.toString() + File.pathSeparator + cplexJar);
            command.add("-Djava.library.path=" + cplexLib);
            command.add(mainClass);

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.inheritIO();
            builder.directory(workingDir.toFile());
            Process process = builder.start();
            int exitCode = process.waitFor();
            System.out.println("Process finished with exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to launch: " + mainClass);
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
