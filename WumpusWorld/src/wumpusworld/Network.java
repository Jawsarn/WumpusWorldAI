package wumpusworld;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

/**
 * Created by Konrad on 2016-10-13.
 */
public class Network {
    private World m_world;
    private World m_previousWorld;
    private static final int INPUT_PER_QUAD = 7;
    private static final int INPUT_SPECIALS = 2;
    private static final int INPUT_SPECIAL_IS_IN_PIT = 0;
    private static final int INPUT_SPECIAL_HAS_ARROW =1;

    private static final int INPUT_BREEZE = 0;
    private static final int INPUT_STENCH = 1;
    private static final int INPUT_PIT = 2;
    private static final int INPUT_GOLD = 3;
    private static final int INPUT_ARROWED = 4;
    private static final int INPUT_UNDISCOVERD = 5;
    private static final int INPUT_WALL = 6;

    private static final int OUTPUTS_TOTAL = 10;

    private static final int OUTPUT_MOVE_FORWARD = 0;
    private static final int OUTPUT_MOVE_BACKWARD = 1;
    private static final int OUTPUT_MOVE_RIGHT = 2;
    private static final int OUTPUT_MOVE_LEFT = 3;
    private static final int OUTPUT_SHOOT_FORWARD = 4;
    private static final int OUTPUT_SHOOT_BACKWARD = 5;
    private static final int OUTPUT_SHOOT_RIGHT = 6;
    private static final int OUTPUT_SHOOT_LEFT = 7;
    private static final int OUTPUT_PICK_UP = 8;
    private static final int OUTPUT_CLIMB = 9;

    private static final Random rand = new Random(8080);

    private boolean m_testing;
    private int m_quadsX, m_quadsY;
    private int m_arrowedX = - 1, m_arrowedY = -1;
    private int m_hiddenLayerWeightCount;
    private int m_totalNumberOfInputs;
    private int m_bestOutput;
    private float m_utilityValue;

    private int[] m_input;
    private float[][] m_hiddenWeights1;
    private float [] m_hiddenLayer1;
    private float[][] m_outputWeights;
    private float[] m_output;

    boolean verbose = false;

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


        try {
            LoadWeights();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Init outputs
        m_hiddenLayer1 = new float[m_hiddenLayerWeightCount];
        m_output = new float[OUTPUTS_TOTAL];

    }

    private void LoadWeights() throws IOException {
        //open file
        List<String> Weights;
        Path path = FileSystems.getDefault().getPath("weights");
        Weights = Files.readAllLines(path);


        m_outputWeights = new float[OUTPUTS_TOTAL][m_hiddenLayerWeightCount];
        int offset = OUTPUTS_TOTAL*m_hiddenLayerWeightCount;
        for (int i = 0; i<OUTPUTS_TOTAL; i++)
        {
            for (int j = 0; j<m_hiddenLayerWeightCount;j++) {
                m_outputWeights[i][j] = Float.parseFloat(Weights.get(i*m_hiddenLayerWeightCount + j)); //readfileline
            }
        }

        m_hiddenWeights1 = new float[m_hiddenLayerWeightCount][m_quadsX*m_quadsY*INPUT_PER_QUAD + INPUT_SPECIALS];
        for (int i = 0; i<m_hiddenLayerWeightCount; i++)
        {
            for (int j = 0; j<m_totalNumberOfInputs;j++)
            {
                m_hiddenWeights1[i][j] = Float.parseFloat(Weights.get(offset + i*m_totalNumberOfInputs + j));
            }
        }
    }
    public void SaveWeights() throws IOException {
        Path path = FileSystems.getDefault().getPath("weights");

        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter("weights"));


        for (int i = 0; i<OUTPUTS_TOTAL; i++)
        {
            for (int j = 0; j<m_hiddenLayerWeightCount;j++) {
                //writeline
                outputWriter.write(Float.toString(m_outputWeights[i][j]));
                outputWriter.newLine();
            }
        }

        for (int i = 0; i<m_hiddenLayerWeightCount; i++)
        {
            for (int j = 0; j<m_totalNumberOfInputs;j++)
            {
                outputWriter.write(Float.toString(m_hiddenWeights1[i][j]));
                outputWriter.newLine();
            }
        }

        outputWriter.flush();
        outputWriter.close();
    }


    public void UpdateWorld(World p_world)
    {
        m_world = p_world;
    }

    private void InitializeOutputWeights()
    {
        // Random rand = new Random(5);
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
        //Random rand = new Random(4);
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
        if (!m_world.isValidPosition(p_quadX,p_quadY))
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

    private void SetSpecialInput()
    {
        if (m_world.isInPit()) {
            m_input[m_quadsX * m_quadsY * INPUT_PER_QUAD + INPUT_SPECIAL_IS_IN_PIT] = 1;
        }
        else
        {
            m_input[m_quadsX * m_quadsY * INPUT_PER_QUAD + INPUT_SPECIAL_IS_IN_PIT] = 0;
        }
        m_input[m_quadsX * m_quadsY * INPUT_PER_QUAD + INPUT_SPECIAL_HAS_ARROW] = m_world.hasArrow() ? 1 : 0;
    }

    private void BuildInput()
    {
        m_input = new int[m_quadsX*m_quadsY*INPUT_PER_QUAD + INPUT_SPECIALS];
        SetSpecialInput();

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
                    int QuadY = posY + strideY - y;

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
                    int QuadY = posY - strideY + y;

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
                    int QuadY = posY + strideY - y;

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
                    int QuadY = posY - strideY + y;

                    int quadInputValuesStart = (x*m_quadsY + y) * INPUT_PER_QUAD;
                    SetInputOfQuad(QuadX, QuadY, quadInputValuesStart);
                }
            }
        }
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

    private float SigmoidDelta(float p_input)
    {
        return p_input * (1.0f-p_input);
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
        int posXBefore = m_world.getPlayerX();
        int posYBefore = m_world.getPlayerY();
        switch (m_bestOutput)
        {
            case OUTPUT_MOVE_FORWARD:
            {
                m_world.doAction(World.A_MOVE);
                if(verbose)
                    System.out.println("Moving Forward");
                break;
            }
            case OUTPUT_MOVE_BACKWARD:
            {
                m_world.doAction(World.A_TURN_LEFT);
                m_world.doAction(World.A_TURN_LEFT);
                m_world.doAction(World.A_MOVE);
                if(verbose)
                    System.out.println("Moving backward");
                break;
            }
            case OUTPUT_MOVE_RIGHT:
            {
                m_world.doAction(World.A_TURN_RIGHT);
                m_world.doAction(World.A_MOVE);
                if(verbose)
                    System.out.println("Moving right");
                break;
            }
            case OUTPUT_MOVE_LEFT:
            {
                m_world.doAction(World.A_TURN_LEFT);
                m_world.doAction(World.A_MOVE);
                if(verbose)
                    System.out.println("Moving left");
                break;
            }
            case OUTPUT_SHOOT_FORWARD:
            {
                m_world.doAction(World.A_SHOOT);
                if(verbose)
                    System.out.println("shooting forward");
                break;
            }
            case OUTPUT_SHOOT_BACKWARD:
            {
                m_world.doAction(World.A_TURN_LEFT);
                m_world.doAction(World.A_TURN_LEFT);
                m_world.doAction(World.A_SHOOT);
                if(verbose)
                    System.out.println("shooting backward");
                break;
            }
            case OUTPUT_SHOOT_RIGHT:
            {
                m_world.doAction(World.A_TURN_RIGHT);
                m_world.doAction(World.A_SHOOT);
                if(verbose)
                    System.out.println("shooting right");
                break;
            }
            case OUTPUT_SHOOT_LEFT:
            {
                m_world.doAction(World.A_TURN_LEFT);
                m_world.doAction(World.A_SHOOT);
                if(verbose)
                    System.out.println("shooting left");
                break;
            }
            case OUTPUT_PICK_UP:
            {
                m_world.doAction(World.A_GRAB);
                if(verbose)
                    System.out.println("grabbing gold");
                break;
            }
            case OUTPUT_CLIMB:
            {
                m_world.doAction(World.A_CLIMB);
                if(verbose)
                    System.out.println("climbing pit");
                break;
            }
        }
    }

    private void PropagateWeightChange()
    {
        // how much the output weights was off
        float errorOutput = m_utilityValue - m_output[m_bestOutput];
        float errorOutputDelta = errorOutput * SigmoidDelta(m_output[m_bestOutput]);

        // The error for each hidden node(only for best output)
        float[] errorHiddenError = new float[m_hiddenLayerWeightCount];
        for (int i = 0; i<m_hiddenLayerWeightCount; i++)
        {
            errorHiddenError[i] = errorOutputDelta * m_outputWeights[m_bestOutput][i];

        }

        // Delta for each hidden node
        float[] errorHiddenDelta = new float[m_hiddenLayerWeightCount];
        for(int i = 0; i <m_hiddenLayerWeightCount; ++i)
        {
            errorHiddenDelta[i] = errorHiddenError[i] * SigmoidDelta(m_hiddenLayer1[i]);
        }

        // Change output weights according to delta
        for (int i = 0; i<m_hiddenLayerWeightCount; i++)
        {
            m_outputWeights[m_bestOutput][i] += m_hiddenLayer1[i]*errorOutputDelta;
        }

        // Change hidden weights according to delta
        for (int i=0;i<m_hiddenLayerWeightCount; i++)
        {
            for(int j = 0; j<m_totalNumberOfInputs; j++)
            {
                m_hiddenWeights1[i][j] += m_input[j] * errorHiddenDelta[i];
            }
        }
    }

    private void UtilityFunction()
    {
        // Check to see what our sensor is telling us about the (new) quad
        m_utilityValue = 0.5f;
        int posX = m_world.getPlayerX();
        int posY = m_world.getPlayerY();
        if (m_world.hasWumpus(posX,posY))
        {
            m_utilityValue =0.0f;
        }
        else if (m_bestOutput == OUTPUT_PICK_UP)
        {
            if (m_world.hasGold())
            {
                if(verbose)
                    System.out.println("GRABBED GOLD!!! WIN!!!");
                m_utilityValue = 1.0f;
            }
            else
                m_utilityValue = 0.0f;
        }
        else if (m_world.isInPit())
        {
            m_utilityValue = 0.0f;
        }
        else if(m_previousWorld.hasGlitter(m_previousWorld.getPlayerX(), m_previousWorld.getPlayerY()))
        {
            m_utilityValue = 0.0f;
        }
        // If we just shot
        else if (m_bestOutput == OUTPUT_SHOOT_RIGHT ||
                m_bestOutput == OUTPUT_SHOOT_LEFT ||
                m_bestOutput == OUTPUT_SHOOT_FORWARD||
                m_bestOutput == OUTPUT_SHOOT_BACKWARD)
        {
            // if we hit wumpus
            if (m_previousWorld.hasStench(posX,posY) && !m_world.hasStench(posX,posY))
                m_utilityValue = 1.0f;
            // if we missed wumpus or shot randomly
            else
                m_utilityValue = 0.0f;
        }
        // If we walked into wall
        else if (m_previousWorld.getPlayerX() == m_world.getPlayerX() &&
                m_previousWorld.getPlayerY() == m_world.getPlayerY() &&
                (m_bestOutput == OUTPUT_MOVE_LEFT || m_bestOutput == OUTPUT_MOVE_FORWARD ||
                m_bestOutput == OUTPUT_MOVE_BACKWARD || m_bestOutput == OUTPUT_MOVE_RIGHT))
        {
            m_utilityValue = 0.0f;
        }
        else if (m_bestOutput == OUTPUT_CLIMB)
        {
            if (m_previousWorld.isInPit())
            {
                m_utilityValue = 1.0f;
            }
            else
                m_utilityValue=0.0f;
        }
        else if (m_previousWorld.isUnknown(posX,posY))
        {
            m_utilityValue = 1.0f;
        }

    }

    public void Run()
    {
        BuildInput();
        RunThroughNetwork();
        if (!m_testing) {
            PerformActionOnBestOutput();
        }
        else
        {
            m_previousWorld = m_world.cloneWorld();

            if (rand.nextFloat()>0.66f)
            {
                if(verbose)
                    System.out.println("---RANDOMING MOVE---");
                m_bestOutput = rand.nextInt(OUTPUTS_TOTAL);
            }
            PerformActionOnBestOutput();
            UtilityFunction();
            PropagateWeightChange();
        }
    }
}
