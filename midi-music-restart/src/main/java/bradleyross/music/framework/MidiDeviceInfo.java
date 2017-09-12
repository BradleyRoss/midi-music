package bradleyross.music.framework;

import javax.sound.midi.MidiDevice.Info;

public abstract class MidiDeviceInfo extends Info implements ExtendedMidiDeviceInfo {
	protected String vendor = null;
	protected String name = null;
	protected String description = null;
	protected String version = null;
	protected byte[] uniqueId;
	protected Object endpointReference;
	protected String displayName = null;
	/**
	 * Constructor based on {@link javax.sound.midi.MidiDevice.Info}.
	 * @param name
	 * @param vendor
	 * @param description
	 * @param version
	 * @param uniqueId
	 * @param endpointReference
	 */
	public MidiDeviceInfo (String name, String vendor, String description, String version,
			byte[] uniqueId, Object endpointReference) {
		super(name, vendor, description, version);
		this.name = name;
		this.vendor = vendor;
		this.description = description;
		this.version = version;
		this.uniqueId = uniqueId;
		this.endpointReference = endpointReference;
		this.displayName = this.name;
	}
	/**
	 * Constructor using extended information.
	 * @param name
	 * @param vendor
	 * @param description
	 * @param version
	 */
	public MidiDeviceInfo (String name, String vendor, String description,
			String version) {
		super(name, vendor, description, version);
		this.name = name;
		this.vendor = vendor;
		this.description = description;
		this.version = version;
		this.displayName = name;
	}
	@Override
	public byte[] getUniqueId() {
		return uniqueId;
	}

	@Override
	public Object getEndpointReference() {
		return endpointReference;
	}


	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String value) {
		displayName = value;
	}

}
