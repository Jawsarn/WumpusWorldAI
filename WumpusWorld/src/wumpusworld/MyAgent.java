package wumpusworld;

import java.io.IOException;

/**
 * Contains starting code for creating your own Wumpus World agent.
 * Currently the agent only make a random decision each turn.
 * 
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent
{
    private World w;
    private Network m_network;
    int rnd;
    
    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        w = world;
        m_network = new Network(w,3,3,true, 35);
    }

    public void UpdateWorld(World p_world)
    {
        w = p_world;
        m_network.UpdateWorld(w);
    }

    public void SaveData()
    {
        try {
            m_network.SaveWeights();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
            
    /**
     * Asks your solver agent to execute an action.
     */

    public void doAction()
    {
        //Location of the player
        int cX = w.getPlayerX();
        int cY = w.getPlayerY();
        
        MakeMove();

                
    }    
    
     /**
     * Genertes a random instruction for the Agent.
     */
    public int decideRandomMove()
    {
      return (int)(Math.random() * 4);
    }
    
    public void MakeMove()
    {
        m_network.Run();
    }
}

