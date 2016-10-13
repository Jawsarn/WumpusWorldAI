package wumpusworld;

/**
 * Created by Konrad on 2016-10-13.
 */
public class Network {
    private World m_world;
    private static final int INPUT_PER_QUAD = 7;
    private static final int INPUT_SPECIALS = 1;
    private static final int INPUT_BREEZE = 1;
    private static final int INPUT_STENCH = 2;
    private static final int INPUT_PIT = 3;
    private static final int INPUT_GOLD = 4;
    private static final int INPUT_ARROWED = 5;
    private static final int INPUT_UNDISCOVERD = 6;
    private static final int INPUT_WALL = 7;

    private int m_quadsX, m_quadsY;
    private int m_arrowedX, m_arrowedY;
    private int[] m_input;
    public Network(World world, int p_quadsX, int p_quadsY)
    {
        m_quadsX = p_quadsX;
        m_quadsY=p_quadsY;
        m_world = world;
    }
    private void SetInputOfQuad(int p_quadX, int p_quadY, int p_inputValuesStart)
    {
        if (m_world.isValidPosition(p_quadX,p_quadY))
        {
            m_input[p_inputValuesStart + INPUT_WALL] = 1;
        }
        else {
            if (m_world.isUnknown(p_quadX, p_quadY)) {
                m_input[p_inputValuesStart + INPUT_UNDISCOVERD] = 1;
            }
            else {
                if (m_world.hasBreeze(p_quadX, p_quadY)) {
                    m_input[p_inputValuesStart + INPUT_BREEZE] = 1;
                }
                if (m_world.hasStench(p_quadX, p_quadY)) {
                    m_input[p_inputValuesStart + INPUT_STENCH] = 1;
                }
                if (m_world.hasPit(p_quadX, p_quadY)) {
                    m_input[p_inputValuesStart + INPUT_PIT] = 1;
                }
                if (m_world.hasGlitter(p_quadX, p_quadY)) {
                    m_input[p_inputValuesStart + INPUT_GOLD] = 1;
                }
            }
            if (!m_world.hasArrow() && p_quadX == m_arrowedX && p_quadY == m_arrowedY) // Arrowed
            {
                m_input[p_inputValuesStart + INPUT_ARROWED] = 1;
            }
        }
    }
    private void BuildInput()
    {
        m_input = new int[m_quadsX*m_quadsY*INPUT_PER_QUAD + INPUT_SPECIALS];
        int posX = m_world.getPlayerX();
        int posY = m_world.getPlayerY();
        int strideX = (m_quadsX-1)/2;
        int strideY = (m_quadsY-1)/2;
        if (m_world.getDirection() == World.DIR_UP)
        {
            for (int y = 0; y<m_quadsY; y++)
            {
                for (int x = 0; x<m_quadsX; x++)
                {
                    int QuadX = posX - strideX + x;
                    int QuadY = posY - strideY + y;

                    int quadInputValuesStart = (y*m_quadsX + x) * INPUT_PER_QUAD;
                    SetInputOfQuad(QuadX, QuadY, quadInputValuesStart);
                }
            }
        }
        if (m_world.getDirection() == World.DIR_DOWN)
        {
            for (int y = 0; y<m_quadsY; y++)
            {
                for (int x = 0; x<m_quadsX; x++)
                {
                    int QuadX = posX + strideX - x;
                    int QuadY = posY + strideY - y;

                    int quadInputValuesStart = (y*m_quadsX + x) * INPUT_PER_QUAD;
                    SetInputOfQuad(QuadX, QuadY, quadInputValuesStart);
                }
            }
        }
        if (m_world.getDirection() == World.DIR_RIGHT)
        {
            // This should be correct
            for (int x = 0; x<m_quadsX; x++)
            {
                for (int y = 0; y<m_quadsY; y++)
                {
                    int QuadX = posX + strideX - x;
                    int QuadY = posY - strideY + y;

                    int quadInputValuesStart = (x*m_quadsY + y) * INPUT_PER_QUAD;
                    SetInputOfQuad(QuadX, QuadY, quadInputValuesStart);
                }
            }
        }
        if(m_world.getDirection() == World.DIR_LEFT)
        {
            // This should be correct
            for (int x = 0; x<m_quadsX; x++)
            {
                for (int y = 0; y<m_quadsY; y++)
                {
                    int QuadX = posX - strideX + x;
                    int QuadY = posY + strideY - y;

                    int quadInputValuesStart = (x*m_quadsY + y) * INPUT_PER_QUAD;
                    SetInputOfQuad(QuadX, QuadY, quadInputValuesStart);
                }
            }
        }

    }

    private void RunThroughNetwork()
    {

    }

    private void PropagateWeightChange()
    {

    }

    private void UtilityFunction()
    {

    }

    public void Run()
    {

    }

    public void SaveWeightsToFile()
    {

    }

    public void LoadWeightsFromFile(String p_fileName)
    {

    }
}
