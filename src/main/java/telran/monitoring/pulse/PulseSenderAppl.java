package telran.monitoring.pulse;

import java.net.*;
import java.util.*;
import java.util.stream.IntStream;

import telran.monitoring.pulse.dto.SensorData;

public class PulseSenderAppl {
	private static final int N_PACKETS = 100;
	private static final long TIMEOUT = 500;
	private static final int N_PATIENTS = 5;
	private static final int MIN_PULSE_VALUE = 50;
	private static final int MAX_PULSE_VALUE = 200;
	private static final int JUMP_PROBABILITY = 15;
	private static final int JUMP_POSITIVE_PROBABILITY = 70;
	private static final int MIN_JUMP_PERCENT = 10;
	private static final int MAX_JUMP_PERCENT = 100;
	private static final String HOST = "localhost";
	private static final int PORT = 4000;
	private static Random random = new Random();
	static DatagramSocket socket;
	private static Map<Long, Integer> patientPulseMap = new HashMap<>();

	public static void main(String[] args) throws Exception {
		socket = new DatagramSocket();
		IntStream.rangeClosed(1, N_PACKETS).forEach(PulseSenderAppl::sendPulse);

	}

	static void sendPulse(int seqNumber) {
		SensorData data = getRandomSensorData(seqNumber);
		String jsonData = data.toString();
		sendDatagramPacket(jsonData);
		try {
			Thread.sleep(TIMEOUT);
		} catch (InterruptedException e) {

		}
	}

	private static void sendDatagramPacket(String jsonData) {
		byte[] buffer = jsonData.getBytes();
		try {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(HOST), PORT);
			socket.send(packet);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private static SensorData getRandomSensorData(int seqNumber) {

		long patientId = random.nextInt(1, N_PATIENTS + 1);
		int value = getRandomPulseValue(patientId);
		return new SensorData(seqNumber, patientId, value, System.currentTimeMillis());
	}

	private static int getRandomPulseValue(long patientId) {
	    Integer previousPulse = patientPulseMap.get(patientId);
	    int pulse;

	    if (previousPulse == null) {
	        pulse = generateRandomPulse();
	    } else if (isJumpOccurred()) {
	        pulse = generateNewPulseWithJump(previousPulse);
	    } else {
	        pulse = previousPulse;
	    }

	    patientPulseMap.put(patientId, pulse);
	    return pulse;
	}

	private static int generateRandomPulse() {
	    return random.nextInt(MIN_PULSE_VALUE, MAX_PULSE_VALUE + 1);
	}

	private static boolean isJumpOccurred() {
	    return random.nextInt(100) < JUMP_PROBABILITY;
	}

	private static int generateNewPulseWithJump(int previousPulse) {
	    boolean positiveJump = random.nextInt(100) < JUMP_POSITIVE_PROBABILITY;
	    int jumpPercent = random.nextInt(MIN_JUMP_PERCENT, MAX_JUMP_PERCENT + 1);
	    return calculateJumpedPulse(previousPulse, positiveJump, jumpPercent);
	}

	private static int calculateJumpedPulse(int previousPulse, boolean positiveJump, int jumpPercent) {
	    int jumpValue = previousPulse * jumpPercent / 100;
	    int newPulse = positiveJump ? previousPulse + jumpValue : previousPulse - jumpValue;
	    newPulse = Math.max(MIN_PULSE_VALUE, Math.min(MAX_PULSE_VALUE, newPulse));	    
	    return newPulse;
	}

}
