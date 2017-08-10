package bradleyross.music.restart;

import javax.sound.midi.MidiMessage;

public class ParseMessage {
	protected static int debugFlag = 1;
	public static void setDebugFlag(int value) {
		debugFlag = value;
	}
	public static int getDebugFlag() {
		return debugFlag;
	}

	public static String parseMessage(MidiMessage message) {

		StringBuffer build2 = new StringBuffer();
		int length = message.getLength();
		int status = message.getStatus();
		int typeCode = status/16;
		int channel = status % 16;
		byte[] data = message.getMessage();
		if (debugFlag > 0) {
			StringBuffer inter = new StringBuffer();
			inter.append("LogReceiver.send - Status: " +
					Integer.toString(status) + " Length: " +
					Integer.toString(length) + " " +
					Integer.toString(typeCode) + " " +
					Integer.toString(channel));
			for (int i = 0; i < length; i++) {
				inter.append(" " + String.format("%O2x", data[i]));
			}
			System.out.println(inter.toString());
		}

		int data2 = -1;
		int data3 = -1;
		if (length > 1) {
			data2 = data[2];
			if (data2 < 0) {
				data2 = data2 + 256;
			}
		}
		if (length > 2) {
			data3 = data[3];
			if (data3 < 0) {
				data3 = data3 + 256;
			}
		}

		String typeName;
		if (typeCode == 8) {
			typeName = "Note Off";
		} else if (typeCode == 9) {
			typeName = "Note On";
		} else if (typeCode == 10) {
			typeName = "Polyphonic Aftertouch";
		} else if (typeCode == 11) {
			typeName = "Control/Mode Change";
		} else if (typeCode == 12) {
			typeName = "Program Change";
		} else if (typeCode == 13) {
			typeName = "Channel Aftertouch";
		} else if (typeCode == 14) {
			typeName = "Pitch Bend Change";
		} else {
			typeName = "Not Listed";
		}
		
		if (typeCode >= 8 && typeCode <= 10) {

			build2.append(typeName + " - Chan " + Integer.toString(channel) +
					" Note: " + Integer.toString(data2) +
					" Vel/Press: " + Integer.toString(data3) +
					System.lineSeparator());
		} else if (typeCode == 12) {
			build2.append(typeName + " - Chan " + Integer.toString(channel) +
					" Data " + Integer.toString(data2) +
					System.lineSeparator());
		} else if (typeCode >= 11 && typeCode <= 14) {
			build2.append(typeName + " - Chan " + Integer.toString(channel) +
					" Data 1: " + Integer.toString(data2) +
					" Data 2: " + Integer.toString(data3) +
					System.lineSeparator());
		} else {
			build2.append("     " +
					Integer.toString(status, 16) + ", " +
					Integer.toString(length));
			build2.append(", ");
			for (int i = 0; i < data.length; i++) {
				/*  
				 *  Byte.toUnsignedInt(byte) appears to
				 *  start with Java 8.
				 */
				int temp = Byte.toUnsignedInt(data[i]);
				if (temp < 16) {
					build2.append("0" + Integer.toString(temp,16));
				} else {
					build2.append(Integer.toString(temp,16));
				}
				if (i > 10) {
					break;
				}
			}
			build2.append(" ");
		}
		return build2.toString();
	}
}
