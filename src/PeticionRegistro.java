import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class PeticionRegistro implements Runnable {
	Socket socket;
	ArrayList<Persona> listaPersonas;
	public PeticionRegistro(Socket socket, ArrayList<Persona> listaPersonas) {
		this.socket = socket;
		this.listaPersonas = listaPersonas;
	}
	public void run() {
		try (InputStream is = socket.getInputStream();
		     InputStreamReader isr = new InputStreamReader(is);
		     BufferedReader bfr = new BufferedReader(isr)){

			String persona = bfr.readLine();
			registrarPersona(persona.split(";"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean existePersona(String dni) {
		for (Persona p : listaPersonas) {
			if (p.getDni().equalsIgnoreCase(dni)) {
				return true;
			}
		}
		return false;
	}

	private void registrarPersona(String[] partes) {
		String dni = partes[0];
		String nombre = partes[1];
		String ap1 = partes[2];
		String ap2 = partes[3];
		int edad = Integer.parseInt(partes[4]);
		if (existePersona(dni)) {
			System.err.println("ERROR: Registro ya existe");
		} else if (validarDatos(partes)) {
			Persona p = new Persona(dni, nombre, ap1, ap2, edad);
			listaPersonas.add(p);
			mostrarPersona(p);
		}
	}

	public static void mostrarPersona(Persona p) {
		System.out.println("Persona registrada:\nDNI= " + p.getDni() + "\nNombre= " + p.getNombre()
				+ "\nApellido 1= " + p.getApellido1() + "\nApellido 2= " + p.getApellido2()
				+ "\nEdad= " + p.getEdad());
	}

	public boolean validarDatos(String[] partes) {
		int edad = Integer.parseInt(partes[4]);
		if (partes[0].length() != 9) {
			System.err.println("ERROR: DNI inválido");
			return false;
		} else if (partes[1].isBlank() || partes[2].isBlank()) {
			System.err.println("ERROR: Falta el nombre o primer apellido");
			return false;
		} else if (edad >= 115 || edad < 0) {
			System.err.println("ERROR: Edad inválida");
			return false;
		}
		return true;
	}
}