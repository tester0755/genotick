package com.alphatica.genotick.processor;

import com.alphatica.genotick.genotick.Prediction;
import com.alphatica.genotick.genotick.RobotData;
import com.alphatica.genotick.instructions.AddDoubleToRegister;
import com.alphatica.genotick.instructions.AddDoubleToVariable;
import com.alphatica.genotick.instructions.AddRegisterToRegister;
import com.alphatica.genotick.instructions.AddRegisterToVariable;
import com.alphatica.genotick.instructions.AddVariableToVariable;
import com.alphatica.genotick.instructions.AverageOfColumn;
import com.alphatica.genotick.instructions.DataInstruction;
import com.alphatica.genotick.instructions.DecrementRegister;
import com.alphatica.genotick.instructions.DecrementVariable;
import com.alphatica.genotick.instructions.DivideRegisterByDouble;
import com.alphatica.genotick.instructions.DivideRegisterByRegister;
import com.alphatica.genotick.instructions.DivideRegisterByVariable;
import com.alphatica.genotick.instructions.DivideVariableByDouble;
import com.alphatica.genotick.instructions.DivideVariableByRegister;
import com.alphatica.genotick.instructions.DivideVariableByVariable;
import com.alphatica.genotick.instructions.HighestOfColumn;
import com.alphatica.genotick.instructions.IncrementRegister;
import com.alphatica.genotick.instructions.IncrementVariable;
import com.alphatica.genotick.instructions.Instruction;
import com.alphatica.genotick.instructions.InstructionList;
import com.alphatica.genotick.instructions.LowestOfColumn;
import com.alphatica.genotick.instructions.MoveDataToRegister;
import com.alphatica.genotick.instructions.MoveDataToVariable;
import com.alphatica.genotick.instructions.MoveDoubleToRegister;
import com.alphatica.genotick.instructions.MoveDoubleToVariable;
import com.alphatica.genotick.instructions.MoveRegisterToRegister;
import com.alphatica.genotick.instructions.MoveRegisterToVariable;
import com.alphatica.genotick.instructions.MoveRelativeDataToRegister;
import com.alphatica.genotick.instructions.MoveRelativeDataToVariable;
import com.alphatica.genotick.instructions.MoveVariableToRegister;
import com.alphatica.genotick.instructions.MoveVariableToVariable;
import com.alphatica.genotick.instructions.MultiplyRegisterByDouble;
import com.alphatica.genotick.instructions.MultiplyRegisterByRegister;
import com.alphatica.genotick.instructions.MultiplyRegisterByVariable;
import com.alphatica.genotick.instructions.MultiplyVariableByDouble;
import com.alphatica.genotick.instructions.MultiplyVariableByVariable;
import com.alphatica.genotick.instructions.NaturalLogarithmOfData;
import com.alphatica.genotick.instructions.NaturalLogarithmOfRegister;
import com.alphatica.genotick.instructions.NaturalLogarithmOfVariable;
import com.alphatica.genotick.instructions.ReturnRegisterAsResult;
import com.alphatica.genotick.instructions.ReturnVariableAsResult;
import com.alphatica.genotick.instructions.SqRootOfRegister;
import com.alphatica.genotick.instructions.SqRootOfVariable;
import com.alphatica.genotick.instructions.SubtractDoubleFromRegister;
import com.alphatica.genotick.instructions.SubtractDoubleFromVariable;
import com.alphatica.genotick.instructions.SubtractRegisterFromRegister;
import com.alphatica.genotick.instructions.SubtractRegisterFromVariable;
import com.alphatica.genotick.instructions.SubtractVariableFromRegister;
import com.alphatica.genotick.instructions.SubtractVariableFromVariable;
import com.alphatica.genotick.instructions.SumOfColumn;
import com.alphatica.genotick.instructions.SwapRegisters;
import com.alphatica.genotick.instructions.SwapVariables;
import com.alphatica.genotick.instructions.TerminateInstructionList;
import com.alphatica.genotick.instructions.ZeroOutRegister;
import com.alphatica.genotick.instructions.ZeroOutVariable;
import com.alphatica.genotick.population.Robot;
import com.alphatica.genotick.population.RobotExecutor;
import com.alphatica.genotick.population.RobotExecutorSettings;
import com.alphatica.genotick.ui.UserInputOutputFactory;
import com.alphatica.genotick.ui.UserOutput;

import java.util.Arrays;

public class SimpleProcessor extends Processor implements RobotExecutor {

    private static final int MAX_JUMP = 255;
    private final double[] registers = new double[totalRegisters];
    private final UserOutput output = UserInputOutputFactory.getUserOutput();
    private Robot robot;
    private RobotData data;
    private int dataColumns;
    private double robotResult;
    private boolean finished;
    private InstructionList instructionList;
    private boolean terminateInstructionList;
    private int totalInstructionExecuted;
    private int instructionLimitMultiplier;
    private int robotInstructionLimit;
    private int dataMaximumOffset;
    private int ignoreColumns;

    public static SimpleProcessor createProcessor() {
        return new SimpleProcessor();
    }

    @Override
    public Prediction executeRobot(RobotData robotData, Robot robot) {
        prepare(robotData, robot);
        robotInstructionLimit = robot.getLength() * instructionLimitMultiplier;
        try {
            return executeRobotMain();
        } catch (NotEnoughDataException |
                TooManyInstructionsExecuted |
                ArithmeticException ex) {
            return Prediction.OUT;
        }
    }

    @Override
    public void setSettings(RobotExecutorSettings settings) {
        instructionLimitMultiplier = settings.instructionLimit;
    }

    private void prepare(RobotData robotData, Robot robot) {
        this.robot = robot;
        this.data = robotData;
        dataColumns = data.getColumns();
        finished = false;
        instructionList = null;
        terminateInstructionList = false;
        totalInstructionExecuted = 0;
        dataMaximumOffset = robot.getMaximumDataOffset();
        ignoreColumns = robot.getIgnoreColumns();
        zeroOutRegisters();
    }

    private void zeroOutRegisters() {
        Arrays.fill(registers, 0.0);
    }

    private Prediction executeRobotMain()  {
        executeInstructionList(robot.getMainFunction());
        if (finished) {
            return Prediction.getPrediction(robotResult);
        } else {
            return Prediction.getPrediction(registers[0]);
        }
    }

    private void executeInstructionList(InstructionList list)  {
        list.zeroOutVariables();
        terminateInstructionList = false;
        int instructionPointer = 0;
        instructionList = list;
        do {
            Instruction instruction = list.getInstruction(instructionPointer++);
            instruction.executeOn(this);
            totalInstructionExecuted++;
            if(totalInstructionExecuted > robotInstructionLimit) {
                break;
            }
        } while (!terminateInstructionList && !finished);
    }


    private SimpleProcessor() {
    }

    @Override
    public void execute(SwapRegisters ins) {
        int reg1 = ins.getRegister1();
        int reg2 = ins.getRegister2();
        double tmp = registers[reg1];
        registers[reg1] = registers[reg2];
        registers[reg2] = tmp;
    }

    @Override
    public void execute(IncrementRegister ins) {
        int reg = ins.getRegister();
        registers[reg]++;
    }

    @Override
    public void execute(MoveDoubleToRegister ins) {
        int reg = ins.getRegister();
        registers[reg] = ins.getDoubleArgument();
    }

    @Override
    public void execute(AddDoubleToRegister ins) {
        int reg = ins.getRegister();
        registers[reg] += ins.getDoubleArgument();
    }

    @Override
    public void execute(ZeroOutRegister ins) {
        int reg = ins.getRegister();
        registers[reg] = 0;
    }

    @Override
    public void execute(ReturnRegisterAsResult ins) {
        int reg = ins.getRegister();
        robotResult = registers[reg];
        finished = true;
    }

    @Override
    public void execute(@SuppressWarnings("unused") TerminateInstructionList ins) {
        terminateInstructionList = true;
    }

    @Override
    public void execute(DecrementRegister ins) {
        int reg = ins.getRegister();
        registers[reg]--;
    }

    @Override
    public void execute(MoveRegisterToRegister ins) {
        int reg1 = ins.getRegister1();
        int reg2 = ins.getRegister2();
        registers[reg1] = registers[reg2];
    }

    @Override
    public void execute(MoveVariableToRegister ins) {
        int reg = ins.getRegister();
        registers[reg] = instructionList.getVariable(ins.getVariableArgument());
    }

    @Override
    public void execute(MoveRegisterToVariable ins) {
        int reg = ins.getRegister();
        instructionList.setVariable(ins.getVariableArgument(), registers[reg]);
    }

    @Override
    public void execute(MultiplyRegisterByRegister ins) {
        int reg1 = ins.getRegister1();
        int reg2 = ins.getRegister2();
        registers[reg1] *= registers[reg2];
    }

    @Override
    public void execute(MultiplyRegisterByVariable ins) {
        int reg = ins.getRegister();
        registers[reg] *= instructionList.getVariable(ins.getVariableArgument());
    }

    @Override
    public void execute(MultiplyVariableByVariable ins) {
        double var1 = instructionList.getVariable(ins.getVariable1Argument());
        double var2 = instructionList.getVariable(ins.getVariable2Argument());
        instructionList.setVariable(ins.getVariable1Argument(), var1 * var2);
    }

    @Override
    public void execute(SubtractDoubleFromRegister ins) {
        int reg = ins.getRegister();
        registers[reg] -= ins.getDoubleArgument();
    }

    @Override
    public void execute(MoveDoubleToVariable ins) {
        instructionList.setVariable(ins.getVariableArgument(), ins.getDoubleArgument());
    }

    @Override
    public void execute(DivideRegisterByRegister ins) {
        int reg1 = ins.getRegister1();
        int reg2 = ins.getRegister2();
        registers[reg1] /= registers[reg2];
    }

    @Override
    public void execute(DivideRegisterByVariable ins) {
        int reg = ins.getRegister();
        double var = instructionList.getVariable(ins.getVariableArgument());
        registers[reg] /= var;
    }

    @Override
    public void execute(DivideVariableByVariable ins) {
        double var1 = instructionList.getVariable(ins.getVariable1Argument());
        double var2 = instructionList.getVariable(ins.getVariable2Argument());
        instructionList.setVariable(ins.getVariable1Argument(), var1 / var2);
    }

    @Override
    public void execute(DivideVariableByRegister ins) {
        double var = instructionList.getVariable(ins.getVariableArgument());
        int reg = ins.getRegister();
        instructionList.setVariable(ins.getVariableArgument(), var / registers[reg]);
    }

    @Override
    public void execute(ReturnVariableAsResult ins) {
        robotResult = instructionList.getVariable(ins.getVariableArgument());
        finished = true;
    }

    @Override
    public void execute(AddRegisterToRegister ins) {
        int reg1 = ins.getRegister1();
        int reg2 = ins.getRegister2();
        registers[reg1] += registers[reg2];
    }

    @Override
    public void execute(SubtractRegisterFromRegister ins) {
        int reg1 = ins.getRegister1();
        int reg2 = ins.getRegister2();
        registers[reg1] -= registers[reg2];
    }

    @Override
    public void execute(MoveDataToRegister ins) {
        int reg = ins.getRegister();
        int offset = fixOffset(ins.getDataOffsetIndex());
        int column = fixColumn(ins.getDataColumnIndex());
        registers[reg] = data.getData(column, offset);
    }


    @Override
    public void execute(MoveDataToVariable ins) {
        int offset = fixOffset(ins.getDataOffsetIndex());
        int column = fixColumn(ins.getDataColumnIndex());
        double value = data.getData(column, offset);
        instructionList.setVariable(ins.getVariableArgument(),value);
    }

    @Override
    public void execute(MoveRelativeDataToRegister ins)  {
        int reg = ins.getRegister();
        int varOffset = getRelativeOffset(ins);
        int column = fixColumn(ins.getDataColumnIndex());
        registers[reg] = data.getData(column, varOffset);
    }

    @Override
    public void execute(MoveRelativeDataToVariable ins) {
        int varOffset = getRelativeOffset(ins);
        int column = fixColumn(ins.getDataColumnIndex());
        double value = data.getData(column, varOffset);
        instructionList.setVariable(ins.getVariableArgument(), value);
    }

    @Override
    public void execute(NaturalLogarithmOfData ins) {
        int column = fixColumn(ins.getDataColumnIndex());
        int offset = fixOffset(ins.getDataOffsetIndex());
        double value = Math.log(data.getData(column,offset));
        registers[ins.getRegister()] = value;
    }

    @Override
    public void execute(NaturalLogarithmOfRegister ins) {
        double value = Math.log(registers[ins.getRegister2()]);
        registers[ins.getRegister1()] = value;
    }

    @Override
    public void execute(NaturalLogarithmOfVariable ins) {
        double value = Math.log(instructionList.getVariable(ins.getVariable2Argument()));
        instructionList.setVariable(ins.getVariable1Argument(),value);
    }

    @Override
    public void execute(SqRootOfRegister ins) {
        double value = Math.pow(registers[ins.getRegister2()], 0.5);
        registers[ins.getRegister1()] = value;
    }

    @Override
    public void execute(SqRootOfVariable ins) {
        double value = Math.pow(instructionList.getVariable(ins.getVariable2Argument()), 0.5);
        instructionList.setVariable(ins.getVariable1Argument(),value);
    }

    @Override
    public void execute(SumOfColumn ins) {
        int column = fixColumn(ins.getRegister1());
        int length = fixOffset(registers[ins.getRegister2()]);
        registers[0] = getSum(column,length);
    }

    @Override
    public void execute(AverageOfColumn ins) {
        int column = fixColumn(ins.getRegister1());
        int length = fixOffset(registers[ins.getRegister2()]);
        double sum = getSum(column, length);
        registers[0] = sum / length;
    }


    private double getSum(int column, int length) {
        double sum = 0;
        for(int i = 0; i < length; i++) {
            sum += data.getData(column,i);
        }
        return sum;
    }

    @Override
    public void execute(SwapVariables ins) {
        double var1 = instructionList.getVariable(ins.getVariable1Argument());
        double var2 = instructionList.getVariable(ins.getVariable2Argument());
        instructionList.setVariable(ins.getVariable1Argument(), var2);
        instructionList.setVariable(ins.getVariable2Argument(), var1);
    }

    @Override
    public void execute(SubtractVariableFromRegister ins) {
        int reg = ins.getRegister();
        registers[reg] -= instructionList.getVariable(ins.getVariableArgument());
    }

    @Override
    public void execute(DivideRegisterByDouble ins) {
        int reg = ins.getRegister();
        registers[reg] /= ins.getDoubleArgument();
    }

    @Override
    public void execute(MultiplyRegisterByDouble ins) {
        int reg = ins.getRegister();
        registers[reg] *= ins.getDoubleArgument();
    }

    @Override
    public void execute(DivideVariableByDouble ins) {
        double var = instructionList.getVariable(ins.getVariableArgument());
        double result = var / ins.getDoubleArgument();
        instructionList.setVariable(ins.getVariableArgument(), result);
    }

    @Override
    public void execute(MultiplyVariableByDouble ins) {
        double var = instructionList.getVariable(ins.getVariableArgument());
        double result = var * ins.getDoubleArgument();
        instructionList.setVariable(ins.getVariableArgument(), result);
    }

    @Override
    public void execute(ZeroOutVariable ins) {
        instructionList.setVariable(ins.getVariableArgument(), 0.0);
    }

    @Override
    public void execute(IncrementVariable ins) {
        double var = instructionList.getVariable(ins.getVariableArgument());
        var++;
        instructionList.setVariable(ins.getVariableArgument(), var);
    }

    @Override
    public void execute(DecrementVariable ins) {
        double var = instructionList.getVariable(ins.getVariableArgument());
        var--;
        instructionList.setVariable(ins.getVariableArgument(), var);
    }

    @Override
    public void execute(AddDoubleToVariable ins) {
        double var = instructionList.getVariable(ins.getVariableArgument());
        double result = var + ins.getDoubleArgument();
        instructionList.setVariable(ins.getVariableArgument(), result);
    }

    @Override
    public void execute(SubtractDoubleFromVariable ins) {
        double var = instructionList.getVariable(ins.getVariableArgument());
        double result = var - ins.getDoubleArgument();
        instructionList.setVariable(ins.getVariableArgument(), result);
    }

    @Override
    public void execute(AddRegisterToVariable ins) {
        double var = instructionList.getVariable(ins.getVariableArgument());
        int register = ins.getRegister();
        double result = var + registers[register];
        instructionList.setVariable(ins.getVariableArgument(), result);
    }

    @Override
    public void execute(SubtractRegisterFromVariable ins) {
        double var = instructionList.getVariable(ins.getVariableArgument());
        int register = ins.getRegister();
        double result = var - registers[register];
        instructionList.setVariable(ins.getVariableArgument(), result);
    }

    @Override
    public void execute(AddVariableToVariable ins) {
        double var1 = instructionList.getVariable(ins.getVariable1Argument());
        double var2 = instructionList.getVariable(ins.getVariable2Argument());
        double result = var1 + var2;
        instructionList.setVariable(ins.getVariable1Argument(), result);
    }

    @Override
    public void execute(SubtractVariableFromVariable ins) {
        double var1 = instructionList.getVariable(ins.getVariable1Argument());
        double var2 = instructionList.getVariable(ins.getVariable2Argument());
        double result = var1 - var2;
        instructionList.setVariable(ins.getVariable1Argument(),result);
    }

    @Override
    public void execute(MoveVariableToVariable ins) {
        double var = instructionList.getVariable(ins.getVariable2Argument());
        instructionList.setVariable(ins.getVariable1Argument(),var);
    }

    @Override
    public void execute(HighestOfColumn ins) {
        int column = fixColumn(ins.getRegister1());
        int length = fixOffset(registers[ins.getRegister2()]);
        double highest = data.getData(column,0);
        for(int i = 1; i < length; i++) {
            double check = data.getData(column,i);
            if(check > highest) {
                highest = check;
            }
        }
        registers[0] = highest;
    }

    @Override
    public void execute(LowestOfColumn ins) {
        int column = fixColumn(ins.getRegister1());
        int length = fixOffset(registers[ins.getRegister2()]);
        double lowest = data.getData(column,0);
        for(int i = 1; i < length; i++) {
            double check = data.getData(column,i);
            if(check < lowest) {
                lowest = check;
            }
        }
        registers[0] = lowest;
    }

    private int getRelativeOffset(DataInstruction ins) {
        double value = instructionList.getVariable(ins.getDataOffsetIndex());
        return fixOffset(value);
    }

    private int fixOffset(double value) {
        return (int)Math.abs(value % dataMaximumOffset);
    }
    private int fixColumn(double value) {
        return ignoreColumns + (int)Math.abs(value % (dataColumns - ignoreColumns));
    }


}
