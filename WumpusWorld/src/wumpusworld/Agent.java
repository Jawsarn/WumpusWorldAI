package wumpusworld;

/**
 * Interface for creating a Wumpus World solver
 * agent.
 * 
 * @author Johan Hagelbäck
 */
public interface Agent 
{
    /**
     * Asks the agent to execute an action.
     */
    public void doAction();

    public void UpdateWorld(World p_world);

    public void SaveData();
}
