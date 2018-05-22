package application;

public class GameController
{
	private static GameController instance = null;
	
	private boolean started = false;
	private String side = "";
	private String currentTurn = "";
	
	private GameController() { }
	
	public static GameController getInstance()
	{
		if(instance == null)
			instance = new GameController();
		return instance;
	}
	
	public void setSide(String s) { side = s; }
	public String getSide() { return side; }
	
	public void setTurn(String s) { currentTurn = s; }
	public String getTurn() { return currentTurn; }
	
	public boolean getStarted() { return started; }
	public void start() { started = true; }
	
	public boolean yourTurn() { return getTurn().equals(getSide()); }
	
}