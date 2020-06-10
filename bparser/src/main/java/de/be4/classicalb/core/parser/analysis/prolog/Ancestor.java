package de.be4.classicalb.core.parser.analysis.prolog;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.*;

/**
 * Contains the name of the previous machine that we read and that got us to the actual machine
 * Contains the node of the reference that we followed to get to the current state
 */
public class Ancestor {

	private final String name;
	private final MachineReference machineReference;


	public  Ancestor(String name, MachineReference machineReference) throws BCompoundException {
		Node transition = machineReference.getNode();
		if(transition instanceof AUsesMachineClause || transition instanceof ASeesMachineClause || transition instanceof AMachineReference
		|| transition instanceof AImplementationMachineParseUnit || transition instanceof ARefinementMachineParseUnit ) {
			this.name = name;
			this.machineReference = machineReference;
		}else{
			throw new BCompoundException(new BException(name, "Ancestor conforms not the expected type " + transition.getClass(), null));
		}
	}

	public String getName() {
		return name;
	}

	public MachineReference getMachineReference() {return machineReference;}

	@Override
	public String toString() {
		return  "---" + resolveType() + "--->" + name ;
	}

	private String resolveType(){
		if(machineReference.getNode() instanceof  AUsesMachineClause){
			return "uses";
		}
		if(machineReference.getNode() instanceof  ASeesMachineClause){
			return "sees";
		}
		if(machineReference.getNode() instanceof  AImplementationMachineParseUnit){
			return "implements";
		}
		if(machineReference.getNode() instanceof  ARefinementMachineParseUnit){
			return "refines";
		}
		if(machineReference.getNode() instanceof  AReferencesMachineClause){
			return "extends/includes/imports";
		}
		return "";
	}
}
