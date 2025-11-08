import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Launcher universal para Windows y Linux
 * Detecta el sistema operativo y ejecuta la aplicación con los parámetros correctos
 */
public class Launcher {
    
    private static final String JAR_NAME = "gestion-farmacia-1.0.0.jar";
    private static final String JAR_PATH = "target/" + JAR_NAME;
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("  FarmaCiando - Sistema de Gestión de Farmacia");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();
        
        // Detectar sistema operativo
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("Sistema operativo: " + System.getProperty("os.name"));
        System.out.println("Arquitectura: " + System.getProperty("os.arch"));
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println();
        
        // Verificar que existe el JAR
        File jarFile = new File(JAR_PATH);
        if (!jarFile.exists()) {
            System.err.println("ERROR: No se encontró el archivo " + JAR_PATH);
            System.err.println("Por favor, compila el proyecto primero con: mvn package");
            System.exit(1);
        }
        
        System.out.println("Iniciando aplicación...");
        System.out.println();
        
        try {
            List<String> command = new ArrayList<>();
            
            // Comando base
            command.add("java");
            
            // Módulos JavaFX necesarios
            command.add("--module-path");
            
            if (os.contains("win")) {
                // Windows
                String javaFxPath = findJavaFxPath();
                if (javaFxPath != null) {
                    command.add(javaFxPath);
                } else {
                    // Si no encuentra JavaFX, intenta sin module-path (puede estar en el JAR)
                    command.remove(command.size() - 1); // Eliminar --module-path
                }
            } else {
                // Linux/Mac - JavaFX está incluido en el JAR o en el sistema
                command.remove(command.size() - 1); // Eliminar --module-path ya que no es necesario
            }
            
            // Agregar módulos solo si se agregó module-path
            if (command.contains("--module-path")) {
                command.add("--add-modules");
                command.add("javafx.controls,javafx.fxml,javafx.swing");
            }
            
            // JAR ejecutable
            command.add("-jar");
            command.add(JAR_PATH);
            
            // Construir proceso
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO(); // Heredar salida estándar y error
            processBuilder.directory(new File(".")); // Directorio actual
            
            // Iniciar proceso
            Process process = processBuilder.start();
            
            // Esperar a que termine
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                System.err.println("\nLa aplicación terminó con código de error: " + exitCode);
            }
            
        } catch (IOException e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("La aplicación fue interrumpida: " + e.getMessage());
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }
    
    /**
     * Intenta encontrar la ruta de JavaFX en Windows
     */
    private static String findJavaFxPath() {
        // Ubicaciones comunes de JavaFX
        String[] possiblePaths = {
            System.getProperty("user.home") + "\\.m2\\repository\\org\\openjfx",
            "C:\\Program Files\\Java\\javafx-sdk\\lib",
            "C:\\javafx-sdk\\lib",
            System.getenv("JAVAFX_HOME")
        };
        
        for (String path : possiblePaths) {
            if (path != null) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    return path;
                }
            }
        }
        
        return null; // No encontrado
    }
}
