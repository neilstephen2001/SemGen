package semsim.model.computational;

import java.util.HashSet;
import java.util.Set;

import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalDependency;

/** A Computation represents how the value of a SemSim {@link DataStructure} is determined, computationally. */

public class Computation extends ComputationalModelComponent{

	private PhysicalDependency physicalDependency;
	private Set<DataStructure> outputs = new HashSet<DataStructure>();
	private Set<DataStructure> inputs = new HashSet<DataStructure>();
	private String computationalCode;
	private String mathML;
	
	/**
	 * Class constructor with no output(s) specified
	 */
	public Computation(){
		physicalDependency = new PhysicalDependency();
	}
	
	/**
	 * Class constructor with a single {@link DataStructure} set as the computation's output
	 * @param output The output DataStructure of the Computation
	 */
	public Computation(DataStructure output){
		physicalDependency = new PhysicalDependency();
		outputs.add(output);
	}
	
	/**
	 * Class constructor that specifies a set of {@link DataStructure}s that the computation solves
	 * @param outputs The output DataStructures of the Computation
	 */
	public Computation(Set<DataStructure> outputs){
		outputs = new HashSet<DataStructure>();
		outputs.addAll(outputs);
	}
	
	/**
	 * Add a {@link DataStructure} to the Computation's set of inputs
	 * @param input The DataStructure to add as an input
	 * @return The set of all inputs for the Computation
	 */
	public Set<DataStructure> addInput(DataStructure input){
		if(!inputs.contains(input)){
			inputs.add(input);
			input.addUsedToCompute(getOutputs());
		}
		return inputs;
	}
	
	/**
	 * @return A string representation of the computational code used to solve the output
	 * DataStructure(s)
	 */
	public String getComputationalCode() {
		return computationalCode;
	}
	
	/**
	 * @return The set of {@link DataStructure} inputs used in the Computation
	 */
	public Set<DataStructure> getInputs(){
		return inputs;
	}
	
	/**
	 * @return The MathML representation of the computational code required to 
	 * solve the output(s)
	 */
	public String getMathML() {
		return mathML;
	}
	
	/**
	 * @return The {@link PhysicalDependency} associated with this computation
	 */
	public PhysicalDependency getPhysicalDependency(){
		return physicalDependency;
	}
	
	/**
	 * Set the string representation of the computational code used to solve
	 * the output(s)
	 * @param code
	 */
	public void setComputationalCode(String code){
		computationalCode = code;
	}
	
	/**
	 * Set the inputs required to compute the output(s)
	 * @param inputs The required inputs for the computation
	 */
	public void setInputs(Set<DataStructure> inputs){
		this.inputs = new HashSet<DataStructure>();
		this.inputs.addAll(inputs);
	}
	
	/**
	 * Set the MathML representation of the computational code that
	 * solves the output(s)
	 * @param mathml The MathML code as a string
	 */
	public void setMathML(String mathml){
		mathML = mathml;
	}
	
	/**
	 * Set the {@link PhysicalDependency} associated with the computation
	 * @param pd A PhysicalDependency
	 */
	public void setPhysicalDependency(PhysicalDependency pd){
		physicalDependency = pd;
	}

	/**
	 * Set the outputs solved by the computation
	 * @param outputs The solved outputs
	 */
	public void setOutputs(Set<DataStructure> outputs) {
		this.outputs = outputs;
	}

	/**
	 * @return The DataStructures solved by the computation
	 */
	public Set<DataStructure> getOutputs() {
		return outputs;
	}
}