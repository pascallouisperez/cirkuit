package cirkuit.ai;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Color.*;
import java.util.Vector;
import java.io.File;
import java.net.URL;

import cirkuit.circuit.*;

public class Tester extends JFrame {
    public Tester() {
        super();
        final Circuit circuit = new Circuit();
        circuit.load("shared/circuits/complex.ckt");
        CircuitAnalyser circuitAnalyser = new CircuitAnalyser(circuit);
        final Envelope opt = circuitAnalyser.optimalPath();
        setLocation(100, 100);
        setSize(circuit.getWidth()+100, circuit.getHeight()+100);
		setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Circuit Analyser Tester");
        getContentPane().add(new JPanel() {
            public void paint(Graphics g) {
                circuit.draw(g);
                g.setColor(Color.BLACK);
                opt.draw(g);
            }
        }, BorderLayout.CENTER);
    }
    
    public static void main(String args[]) {
       Tester t = new Tester();
       t.show();
    }
}
