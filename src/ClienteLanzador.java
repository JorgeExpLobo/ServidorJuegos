import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteLanzador {
	public static void main(String[] args) {
		Thread hilo = new Thread(new ClienteRunnable());
		hilo.start();
	}
}

class ClienteRunnable implements Runnable {
	@Override
	public void run() {
		try (Socket socket = new Socket("localhost", 6000)) {
			PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
			String ejemploPersona = "12345678B;PEPE;Pérez;García;-8";
			pw.println(ejemploPersona);
		} catch (IOException e) {
			System.out.println("Excepción: " + e.getMessage());
		}
	}
}
