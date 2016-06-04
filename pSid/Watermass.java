package tutorial;

public class Watermass {
	public float volume;
	public float dbo;

	public Watermass(float v, float dbo){
		this.volume = v;
		this.dbo = dbo;
	}
	
	public String toString(){
		return "Water mass with volume "+this.volume+" and DBO: "+this.dbo;
	}
}
