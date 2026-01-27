import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    static ArrayList<Persona> listaPersonas = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        System.out.println("Creando socket servidor");
        Socket newSocket = null;
        try (ServerSocket serverSocket = new ServerSocket()) {
            System.out.println("Realizando el bind");
            InetSocketAddress addr = new InetSocketAddress("localhost", 6000);
            serverSocket.bind(addr);
            System.out.println("Aceptando conexiones");
            if (listaPersonas.isEmpty())
                System.out.println("Lista de personas vacía (inicio programa)");
            while (true) {
                newSocket = serverSocket.accept();
                System.out.println("Conexion recibida");
                PeticionRegistro p = new PeticionRegistro(newSocket, listaPersonas);
                Thread hilo = new Thread(p);
                hilo.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (newSocket != null) {
                newSocket.close();
            }
        }
    }
}