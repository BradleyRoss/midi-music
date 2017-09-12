package bradleyross.music.framework;

public interface ExtendedMidiDeviceInfo {
	public byte[] getUniqueId();
	public Object getEndpointReference();
	public String getDisplayName();
	public void setDisplayName(String value);
}
