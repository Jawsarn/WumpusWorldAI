package wumpusworld;

import java.util.Random;

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

    private static final int OUTPUTS_TOTAL = 7;

    private boolean m_testing;
    private int m_quadsX, m_quadsY;
    private int m_arrowedX, m_arrowedY;
    private int m_hiddenLayerWeightCount;
    private int m_totalNumberOfInputs;
    private int m_bestOutput;

    private int[] m_input;
    private float[][] m_hiddenWeights1;
    private float [] m_hiddenLayer1;
    private float[][] m_outputWeights;
    private float[] m_output;
    public Network(World world, int p_quadsX, int p_quadsY, boolean p_testing, int p_hiddenLayerWeightCount)
    {
        m_quadsX = p_quadsX;
        m_quadsY=p_quadsY;
        m_world = world;
        m_testing =p_testing;
        m_hiddenLayerWeightCount=p_hiddenLayerWeightCount;
        m_totalNumberOfInputs = m_quadsX*m_quadsY*INPUT_PER_QUAD + INPUT_SPECIALS;
        InitializeHiddenWeights();
        InitializeOutputWeights();

        // Init outputs
        m_hiddenLayer1 = new float[m_hiddenLayerWeightCount];
        m_output = new float[OUTPUTS_TOTAL];
    }

    private void InitializeOutputWeights()
    {
        Random rand = new Random(2);
        m_outputWeights = new float[OUTPUTS_TOTAL][m_hiddenLayerWeightCount];
        for (int i = 0; i<OUTPUTS_TOTAL; i++)
        {
            for (int j = 0; j<m_hiddenLayerWeightCount;j++)
            {
                m_outputWeights[i][j] = rand.nextFloat() * 2.0f - 1.0f;
            }
        }
    }

    private void InitializeHiddenWeights()
    {
        Random rand = new Random(1);
        m_hiddenWeights1 = new float[m_hiddenLayerWeightCount][m_quadsX*m_quadsY*INPUT_PER_QUAD + INPUT_SPECIALS];

        for (int i = 0; i<m_hiddenLayerWeightCount; i++)
        {
            for (int j = 0; j<m_totalNumberOfInputs;j++)
            {
                m_hiddenWeights1[i][j] = rand.nextFloat() * 2.0f - 1.0f;
            }
        }
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
        m_input[m_quadsX*m_quadsY*INPUT_PER_QUAD] = m_world.hasArrow() ? 1 : 0;
    }

    // Should be between 0-m_hiddenLayerWeightCount
    private float SuperDotHidden(int p_hidden)
    {
        float r_out = 0;
        for (int i = 0; i < m_totalNumberOfInputs; ++i)
        {
            r_out += m_input[i] * m_hiddenWeights1[p_hidden][i];
        }
        return r_out;
    }

    // p_output should be between 0-7 for now
    private float SuperDotOutput(int p_output)
    {
        float r_out = 0;
        for (int i = 0; i < m_hiddenLayerWeightCount; ++i)
        {
            r_out += m_hiddenLayer1[i] * m_outputWeights[p_output][i];
        }
        return r_out;
    }

    // Cred to http://iamtrask.github.io/2015/07/12/basic-python-network/
    // and https://medium.com/technology-invention-and-more/how-to-build-a-simple-neural-network-in-9-lines-of-python-code-cc8f23647ca1#.s1oagwp9h
    private float Sigmoid(float p_input)
    {
        return (float)(1.0f / (1.0f + Math.exp((double)-p_input)));
    }

    private void RunThroughNetwork()
    {
        // Input with hidden weight
        for (int i = 0; i < m_hiddenLayerWeightCount; ++i)
        {
            // Signoid of dotted wiehgts and input
            m_hiddenLayer1[i] = Sigmoid(SuperDotHidden(i));
        }

        // Hidden with output weights
        for (int i = 0; i < OUTPUTS_TOTAL; ++i)
        {
            // Signoid of dotted wiehgts and input
            m_output[i] = Sigmoid(SuperDotOutput(i));
        }

        // Find best output
        m_bestOutput = 0;
        float m_bestOutputVal = m_output[m_bestOutput];
        for (int i = 1; i < OUTPUTS_TOTAL; ++i)
        {
            if(m_output[i] > m_bestOutputVal)
            {
                m_bestOutput = i;
                m_bestOutputVal = m_output[i];
            }
        }
    }

    private void PerformActionOnBestOutput()
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
        BuildInput();
        RunThroughNetwork();
        PerformActionOnBestOutput();
        if (m_testing)
        {
            PropagateWeightChange();
        }
    }

    public void SaveWeightsToFile()
    {

    }

    public void LoadWeightsFromFile(String p_fileName)
    {

    }
}
