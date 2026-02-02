import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorJuego {
	// Mapa de ID Partida -> Objeto Partida
	private static Map<String, Partida> partidasEnCurso = new ConcurrentHashMap<>();
	private static final int PUERTO_SERVER = 6000;

	public static void main(String[] args) {
		System.out.println("--- SERVIDOR DE JUEGOS INICIADO ---");
		try (ServerSocket serverSocket = new ServerSocket(PUERTO_SERVER)) {
			while (true) {
				Socket socket = serverSocket.accept();
				new Thread(new ManejadorCliente(socket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Clase interna para gestionar cada partida
	public static class Partida {
		String id;
		String tipoJuego;
		List<JugadorInfo> jugadores = new ArrayList<>();
		int maxJugadores;

		public Partida(String id, String tipoJuego, int maxJugadores) {
			this.id = id;
			this.tipoJuego = tipoJuego;
			this.maxJugadores = maxJugadores;
		}

		public synchronized boolean estaLlena() {
			return jugadores.size() >= maxJugadores;
		}

		public synchronized void agregarJugador(JugadorInfo jugador) {
			// El primero en llegar es el anfitrión
			if (jugadores.isEmpty()) {
				jugador.setEsAnfitrion(true);
			}
			jugadores.add(jugador);
		}

		public synchronized List<JugadorInfo> getJugadores() {
			return new ArrayList<>(jugadores);
		}
	}

	// Hilo que atiende a cada conexión entrante
	static class ManejadorCliente implements Runnable {
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;

		public ManejadorCliente(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				String linea = in.readLine(); // Protocolo: ACCION;DATOS...
				if (linea == null) return;

				String[] partes = linea.split(";");
				String accion = partes[0];

				if (accion.equalsIgnoreCase("NUEVO_JUEGO")) {
					// Protocolo: NUEVO_JUEGO;TIPO;NICK;HOST;PUERTO
					procesarNuevoJuego(partes);
				} else if (accion.equalsIgnoreCase("FINALIZAR")) {
					// Protocolo: FINALIZAR;ID_PARTIDA
					String idPartida = partes[1];
					partidasEnCurso.remove(idPartida);
					System.out.println("Partida finalizada y eliminada: " + idPartida);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void procesarNuevoJuego(String[] datos) throws InterruptedException {
			String tipoJuego = datos[1];
			String nick = datos[2];
			String hostIp = datos[3];
			int puertoPropio = Integer.parseInt(datos[4]); // Parseo directo a int

			JugadorInfo nuevoJugador = new JugadorInfo(nick, hostIp, puertoPropio);
			Partida partidaAsignada = null;

			// 1. Buscar partida disponible o crear una (Sincronización crítica)
			synchronized (ServidorJuego.class) {
				for (Partida p : partidasEnCurso.values()) {
					if (p.tipoJuego.equals(tipoJuego) && !p.estaLlena()) {
						partidaAsignada = p;
						break;
					}
				}

				if (partidaAsignada == null) {
					String id = UUID.randomUUID().toString().substring(0, 8);
					// Para dados son 2 jugadores
					partidaAsignada = new Partida(id, tipoJuego, 2);
					partidasEnCurso.put(id, partidaAsignada);
					System.out.println("Nueva partida creada: " + id + " para " + tipoJuego);
				}
			}

			// 2. Lógica de espera dentro de la instancia de la partida
			synchronized (partidaAsignada) {
				partidaAsignada.agregarJugador(nuevoJugador);

				if (!partidaAsignada.estaLlena()) {
					System.out.println("Jugador " + nick + " esperando rival en partida " + partidaAsignada.id);
					// Esperar a que llegue el otro jugador
					partidaAsignada.wait();
				} else {
					System.out.println("Partida " + partidaAsignada.id + " llena. Notificando jugadores.");
					// Despertar a los que esperan
					partidaAsignada.notifyAll();
				}
			}

			// 3. Enviar respuesta al cliente (Nick, Host, Puerto, EsAnfitrion, ID_Partida)
			// Se envía la lista de TODOS los jugadores conectados
			StringBuilder respuesta = new StringBuilder("INICIO_PARTIDA;");
			respuesta.append(partidaAsignada.id).append(";"); // ID para finalizar luego

			// Serializamos la lista de jugadores en el string
			for (JugadorInfo j : partidaAsignada.getJugadores()) {
				respuesta.append(j.getNickname()).append(",")
						.append(j.getHostIp()).append(",")
						.append(j.getPuerto()).append(",")
						.append(j.isEsAnfitrion()).append("#");
			}

			// Informar específicamente si ESTE cliente es anfitrión
			respuesta.append(";").append(nuevoJugador.isEsAnfitrion());

			out.println(respuesta.toString());
			// No cerramos el socket inmediatamente si queremos mantener conexión,
			// pero para este ejercicio el cliente maneja la lógica y luego avisa para desconectar.
			// Aquí cerramos este hilo de atención, el cliente se va a jugar P2P.
			try { socket.close(); } catch(IOException e) {}
		}
	}
}
