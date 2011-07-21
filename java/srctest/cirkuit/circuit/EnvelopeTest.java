package cirkuit.circuit;

import static cirkuit.circuit.Envelope.TYPE_CLOSED_PATH;
import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class EnvelopeTest {

	@Test
	public void length() {
		Envelope envelope = new Envelope(TYPE_CLOSED_PATH);
		envelope.reset();
		envelope.addPoint(30, 30);
		envelope.addPoint(130, 30);
		envelope.addPoint(130, 130);
		envelope.addPoint(30, 130);
		envelope.npoints = 4;
		assertEquals(400.0, envelope.getLength(), 0);
	}
	
}
