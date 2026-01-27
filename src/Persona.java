public class Persona {
	private String dni;
	private String nombre;
	private String apellido1;
	private String apellido2;
	private int edad;
	public Persona(String dni, String nombre, String apellido1, String
			apellido2, int edad) {
		this.dni = dni;
		this.nombre = nombre;
		this.apellido1 = apellido1;
		this.apellido2 = apellido2;
		this.edad = edad;
	}
	public String getDni() {
		return dni;
	}
	public String getNombre() {
		return nombre;
	}
	public String getApellido1() {
		return apellido1;
	}
	public String getApellido2() {
		return apellido2;
	}
	public int getEdad() {
		return edad;
	}
}