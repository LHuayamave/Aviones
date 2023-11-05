/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logica;

import java.awt.Color;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


public class VentanillaL extends Thread {

    private static final int NUM_ASIENTOS = 30;
    private static JLabel[] asientosLabels;
    private static Queue<Integer> asientosDisponibles = new ConcurrentLinkedQueue<>();
    private static Semaphore mutex = new Semaphore(1);

    private int numeroAsiento;
    private int lastAsiento;
    private String nombreCliente;
    private String mensaje;
    private String NumVentanilla;

    private String accion;

    private static JTextArea textArea;
    
    public int getLastAsiento() {
        return lastAsiento;
    }

    public void setLastAsiento(int lastAsiento) {
        this.lastAsiento = lastAsiento;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String Accion) {
        this.accion = Accion;
    }

    public static void setTextArea(JTextArea textArea) {
        VentanillaL.textArea = textArea;
    }

    public static void setAsientosLabels(JLabel[] labels) {
        asientosLabels = labels;
    }

    public static int getNUM_ASIENTOS() {
        return NUM_ASIENTOS;
    }

    public String getNumVentanilla() {
        return NumVentanilla;
    }

    public void setNumVentanilla(String NumVentanilla) {
        this.NumVentanilla = NumVentanilla;
    }

    public void setInicio(boolean inicio) {
        this.inicio = inicio;
    }
    private boolean inicio;

    public VentanillaL() {
        this.inicio = false;
    }

    public VentanillaL(int numeroAsiento, String nombreCliente, String mensaje) {
        this.numeroAsiento = numeroAsiento;
        this.nombreCliente = nombreCliente;
        this.mensaje = mensaje;
    }

    public int getNumeroAsiento() {
        return numeroAsiento;
    }

    public void setNumeroAsiento(int numeroAsiento) {
        this.numeroAsiento = numeroAsiento;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getMensaje() {
        return mensaje;
    }

    @Override
    public void run() {
        try {

            while (!inicio) {
                Thread.sleep(100); // Espera corta antes de comprobar la variable inicio
            }

            // Espera aleatoria para simular concurrencia
            System.out.println("ENTRO EL HILO" + this.getNumVentanilla());
            Thread.sleep(new Random().nextInt(4500));

            if (accion == "reservar") {
                while (!reservarAsiento()) {
                    System.out.println("La ventanilla " + this.getNumVentanilla() + " está esperando el asiento " + numeroAsiento);
                    Thread.sleep(new Random().nextInt(1000));
                }
            } else if (accion == "cambiar") {
                System.out.println("Se va a ejecutar un cambio");
                while (!cambiarAsiento()) {
                    System.out.println("La ventanilla " + this.getNumVentanilla() + " está esperando el asiento " + numeroAsiento);
                    Thread.sleep(new Random().nextInt(1000));
                }
            }

            mensaje = ("La ventanilla " + this.getNumVentanilla() + " reservó el asiento " + numeroAsiento + " para " + nombreCliente + "\n");
            System.out.println("La ventanilla " + this.getNumVentanilla() + " reservó el asiento " + numeroAsiento + " para " + nombreCliente);
            SwingUtilities.invokeLater(() -> textArea.append(mensaje));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean reservarAsiento() throws InterruptedException {
        mutex.acquire(); // Adquirir el semáforo para acceder a la sección crítica

        boolean asientoReservado = false;

        if (asientosDisponibles.contains(numeroAsiento)) {
            asientosDisponibles.remove(numeroAsiento);
            asientoReservado = true;
            asientosLabels[numeroAsiento - 1].setBackground(Color.ORANGE);
            asientosLabels[numeroAsiento - 1].setToolTipText("Reservado por: " + this.getNombreCliente());
        }

        mutex.release(); // Liberar el semáforo después de acceder a la sección crítica

        return asientoReservado;
    }

    public void eliminarReserva() throws InterruptedException {
        mutex.acquire();

        System.out.println("Se procede a eliminar desde Ventanilla" + this.getNumVentanilla());

        if (!asientosDisponibles.contains(numeroAsiento)) {
            asientosDisponibles.add(numeroAsiento);
            asientosLabels[numeroAsiento - 1].setBackground(Color.GREEN);
            asientosLabels[numeroAsiento - 1].setToolTipText(null);
            mensaje = ("La ventanilla " + this.getNumVentanilla() + " liberó el asiento " + numeroAsiento + "\n");
            SwingUtilities.invokeLater(() -> textArea.append(mensaje));
        }

        mutex.release();
    }

    public boolean cambiarAsiento() throws InterruptedException {
        mutex.acquire();
        int nuevoAsiento = this.getNumeroAsiento();
        int AsientoAnterior = this.getLastAsiento();
        boolean asientoCambiado = false;
        if (asientosDisponibles.contains(nuevoAsiento)) {
            asientosDisponibles.add(AsientoAnterior);
            numeroAsiento = nuevoAsiento;
            asientosDisponibles.remove(nuevoAsiento);
            asientoCambiado = true;
            asientosLabels[AsientoAnterior - 1].setBackground(Color.GREEN);
            asientosLabels[numeroAsiento - 1].setBackground(Color.orange);
            asientosLabels[numeroAsiento - 1].setToolTipText("Reservado por: " + this.getNombreCliente());
            mensaje = ("La ventanilla " + this.getNumVentanilla() + " cambió de asiento del asiento " + AsientoAnterior + " a " + nuevoAsiento + "\n");
            SwingUtilities.invokeLater(() -> textArea.append(mensaje));
        }
        mutex.release();
        System.out.println(asientoCambiado);
        return asientoCambiado;
    }

    private int obtenerAsientoDisponible() {
        int nuevoAsiento = -1;
        for (int i = 1; i <= NUM_ASIENTOS; i++) {
            if (asientosDisponibles.contains(i)) {
                nuevoAsiento = i;
                break;
            }
        }
        return nuevoAsiento;
    }

    public static void inicializarAsientos() {
        for (int i = 1; i <= NUM_ASIENTOS; i++) {
            asientosDisponibles.add(i);
        }
    }
}
