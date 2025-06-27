package chav1961.qu.util;

import java.util.BitSet;
import java.util.function.Function;

import chav1961.purelib.basic.NamedValue;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.qu.api.interfaces.GateDescriptor;
import chav1961.qu.api.interfaces.GateDescriptor.GateType;
import chav1961.qu.api.interfaces.GateMatrix;
import chav1961.qu.api.interfaces.GateMatrixType;
import chav1961.qu.api.interfaces.QubitDescriptor;
import chav1961.qu.util.GateDescriptionParser.Gate;

public class QuantumUtils {
	private static final QubitDescriptor[]	DUMMY_QUBITS = new QubitDescriptor[0]; 
	
	public static GateDescriptor parseGate(final CharSequence seq, final BitSet availableQubits, final Function<String,String> subst) throws SyntaxException {
		if (Utils.checkEmptyOrNullString(seq)) {
			throw new IllegalArgumentException("Gate descriptor can't be null or empty");
		}
		else if (availableQubits == null) {
			throw new NullPointerException("Available qubits can't be null");
		}
		else if (subst == null) {
			throw new NullPointerException("Substitution source can't be null");
		}
		else {
			final Gate	gate = GateDescriptionParser.parseGateDescriptorInternal(seq, subst, availableQubits.length());
			final NamedValue<Object>[]	values = new NamedValue[gate.getDescriptor().getParameters().length]; 
			
			for(int index = 0; index < values.length; index++) {
				values[index] = new NamedValue<Object>(gate.getDescriptor().getParameters()[index].name, gate.getParameterValues()[index]);
			}
			
			final GateDescriptor	desc = new GateDescriptor() {
						private QubitDescriptor[]	qubits = DUMMY_QUBITS;
						private QubitDescriptor[]	controls = DUMMY_QUBITS;
						
						@Override
						public void setQubits(final QubitDescriptor... desc) {
							if (desc == null || desc.length == 0 || Utils.checkArrayContent4Nulls(desc) >= 0) {
								throw new IllegalArgumentException("Array to set is null, empty or contaons nulls inside");
							}
							else {
								this.qubits = desc;
							}
						}
						
						@Override
						public void setQubit(final QubitDescriptor desc) {
							if (desc == null) {
								throw new NullPointerException("Qubit descriptor to set can't be null");
							}
							else {
								setQubits(desc);
							}
						}
						
						@Override
						public void setControls(final QubitDescriptor... desc) {
							if (desc == null || Utils.checkArrayContent4Nulls(desc) >= 0) {
								throw new IllegalArgumentException("Control descriptors to set is null or contains nulls inside");
							}
							else if (desc.length == 0) {
								this.controls = DUMMY_QUBITS;
							}
							else {
								this.controls = desc;
							}
						}
						
						@Override
						public GateType getType() {
							return gate.getDescriptor().getName();
						}
						
						@Override
						public double[] getShortMatrix() {
							return buildShortGateMatrix(getType(), getParameters());
						}
						
						@Override
						public QubitDescriptor[] getQubits() {
							return qubits;
						}
						
						@Override
						public QubitDescriptor getQubit() {
							return qubits.length == 0 ? null : qubits[0]; 
						}
						
						@Override
						public GateMatrix getMatrix() {
							return buildGateMatrix(this, GateMatrixType.COMMITATION_MATRIX);
						}
						
						@Override
						public QubitDescriptor[] getControls() {
							return controls;
						}

						@Override
						public NamedValue<Object>[] getParameters() {
							return values;
						}
					};
			return desc;
		}
	}
	
	public static double[] buildShortGateMatrix(final GateType type, final NamedValue<Object>... parameters) {
		if (type == null) {
			throw new NullPointerException("Gate type can't be null");
		}
		else if (parameters == null || Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Parameters list is null or contains nulls inside");
		}
		else {
			switch (type) {
				case H		:
					break;
				case P		:
					break;
				case SWAP	:
					break;
				default:
					throw new UnsupportedOperationException("Gate type ["+type+"] is not supported yet");
			}
			return null;		
		}
	}
	
	private static GateMatrix buildGateMatrix(final GateDescriptor desc, final GateMatrixType preferredMatrixType) {
		if (desc == null) {
			throw new NullPointerException("Gate descriptor can't be null");
		}
		else if (preferredMatrixType == null) {
			throw new NullPointerException("Preferred matrix type can't be null");
		}
		else {
			switch (desc.getType()) {
				case H		:
					break;
				case P		:
					break;
				case SWAP	:
					break;
				default:
					throw new UnsupportedOperationException("Gate type ["+desc.getType()+"] is not supported yet");
			}
			return null;		
		}
		
	}
}
