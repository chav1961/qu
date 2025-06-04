package chav1961.qu.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.qu.api.interfaces.GateDescriptor;
import chav1961.qu.api.interfaces.GateDescriptor.GateType;

class GateDescriptionParser {
	static final char	EOF = '\uFFFF';
	static final int	CONST_PI = 0;
	static final int	CONST_E	= 1;
	static final int	FUNC_SQRT	= 0;

	private static Map<String, InternalGateDescriptor>	STANDARD_GATES = new HashMap<>();
	private static final double	TWO_PI = 2 * Math.PI;
	
	static {
		STANDARD_GATES.put(GateDescriptor.GateType.SWAP.name(), 
				new InternalGateDescriptor(GateDescriptor.GateType.SWAP, 
						new GateParameter("qbit1", GateParameterType.QBIT_NUMBER, true, true),
						new GateParameter("qbit2", GateParameterType.QBIT_NUMBER, true, true)
				));
		STANDARD_GATES.put(GateDescriptor.GateType.P.name(),
				new InternalGateDescriptor(GateDescriptor.GateType.P, 
						new GateParameter("qbit", GateParameterType.QBIT_NUMBER, true, true),
						new GateParameter("phase", GateParameterType.REAL_VALUE, true, false)
				)
		);
		STANDARD_GATES.put(GateDescriptor.GateType.H.name(),
				new InternalGateDescriptor(GateDescriptor.GateType.H, 
						new GateParameter("qbits", GateParameterType.QBIT_LIST, true, true)
				)
		);
	}
	
	static Gate parseGateDescriptorInternal(final CharSequence seq, final Function<String,String> subst, final int maxQbitNumber) throws SyntaxException {
		final List<Lexema>	lex = new ArrayList<>();
		int	len;
		
		if ((len = parseLex(seq, 0, lex)) < seq.length()-1) {
			throw new SyntaxException(0, len, "Unparsed tail in the input");
		}
		else {
			return buildGateDescriptor(lex.toArray(new Lexema[lex.size()]), 0, subst, maxQbitNumber);
		}
	}	
	
	static int parseLex(final CharSequence seq, int from, final List<Lexema> lex) throws SyntaxException {
		final StringBuilder	sb = new StringBuilder();
		char	currentChar;
		
loop:	for(;;) {
			final int	startLex = from = skipBlank(seq, from);
			
			switch (seq.charAt(from)) {
				case EOF :
					lex.add(new Lexema(0, startLex, Lexema.LexType.EOF));
					break loop;
				case '~' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.TILDE));
					from++;
					break;
				case '(' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.OPEN));
					from++;
					break;
				case ')' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.CLOSE));
					from++;
					break;
				case '[' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.OPEN_B));
					from++;
					break;
				case ']' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.CLOSE_B));
					from++;
					break;
				case ':' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.COLON));
					from++;
					break;
				case ',' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.DIV));
					from++;
					break;
				case '+' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.ADD_OP, '+'));
					from++;
					break;
				case '-' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.ADD_OP, '-'));
					from++;
					break;
				case '*' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.MUL_OP, '*'));
					from++;
					break;
				case '/' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.MUL_OP, '/'));
					from++;
					break;
				case '=' :
					lex.add(new Lexema(0, startLex, Lexema.LexType.EQUALS));
					from++;
					break;
				case '.' :
					if (seq.charAt(from+1) == '.') {
						lex.add(new Lexema(0, startLex, Lexema.LexType.RANGE));
						from+=2;
					}
					else {
						throw new SyntaxException(0, startLex, "Unknown lexema");
					}
					break;
				case '$' :
					from++;
					sb.setLength(0);
					while (Character.isJavaIdentifierPart(currentChar = seq.charAt(from))) {
						sb.append(currentChar);
						from++;
					}
					lex.add(new Lexema(0, startLex, Lexema.LexType.REFERENCE_NAME, sb.toString()));
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					boolean	isInteger = true;
					
					sb.setLength(0);
					while (Character.isDigit(currentChar = seq.charAt(from))) {
						sb.append(currentChar);
						from++;
					}
					if (seq.charAt(from) == '.' && seq.charAt(from+1) != '.') {
						isInteger = false;
						sb.append('.');
						from++;
						while (Character.isDigit(currentChar = seq.charAt(from))) {
							sb.append(currentChar);
							from++;
						}
					}
					if (isInteger) {
						lex.add(new Lexema(0, startLex, Lexema.LexType.INTEGER_CONST, Integer.valueOf(sb.toString())));
					}
					else {
						lex.add(new Lexema(0, startLex, Lexema.LexType.REAL_CONST, Double.valueOf(sb.toString())));
					}
					if (seq.charAt(from) == 'e' || seq.charAt(from) == 'E') {
						lex.add(new Lexema(0, startLex, Lexema.LexType.NAMED_CONSTANT, CONST_E));
						from++;
					}
					break;
				default :
					if (Character.isJavaIdentifierStart(seq.charAt(from))) {
						sb.setLength(0);
						while (Character.isJavaIdentifierPart(currentChar = seq.charAt(from))) {
							sb.append(currentChar);
							from++;
						}
						final String	name = sb.toString();
						
						switch (name.toUpperCase()) {
							case "PI" :
								lex.add(new Lexema(0, startLex, Lexema.LexType.NAMED_CONSTANT, CONST_PI));
								break;
							case "E" :
								lex.add(new Lexema(0, startLex, Lexema.LexType.NAMED_CONSTANT, CONST_E));
								break;
							case "I" :
								lex.add(new Lexema(0, startLex, Lexema.LexType.I));
								break;
							case "SQRT" :
								lex.add(new Lexema(0, startLex, Lexema.LexType.FUNCTION_NAME, FUNC_SQRT));
								break;
							default :
								if (STANDARD_GATES.containsKey(name.toUpperCase())) {
									lex.add(new Lexema(0, startLex, Lexema.LexType.GATE_NAME, name));
								}
								else {
									lex.add(new Lexema(0, startLex, Lexema.LexType.PARAMETER_NAME, name));
								}
								break;
						}
					}
					else {
						throw new SyntaxException(0, startLex, "Unknown lexema");
					}
			}
		}
		return from;
	}

	private static Gate buildGateDescriptor(final Lexema[] lex, int from, final Function<String,String> subst, final int maxQbitNumber) throws SyntaxException {
		if (lex[from].type != Lexema.LexType.GATE_NAME) {
			throw new SyntaxException(lex[from].row, lex[from].col, "Gate name is missing");
		}
		else {
			final InternalGateDescriptor	desc = STANDARD_GATES.get(lex[from++].stringValue.toUpperCase());
			
			if (lex[from].type != Lexema.LexType.OPEN_B) {
				throw new SyntaxException(lex[from].row, lex[from].col, "Missing '['");
			}
			else {
				final GateParameter	parms[] = desc.getParameters();
				final Object[]		values = new Object[parms.length];
				int	parmNo = 0;
				
				do {
					from++;
					if (lex[from].type == Lexema.LexType.PARAMETER_NAME) {
						boolean	found = false;
						int	index = 0;
						
						for (GateParameter item : parms) {
							if (item.name.equalsIgnoreCase(lex[from].stringValue)) {
								if (lex[++from].type != Lexema.LexType.EQUALS) {
									throw new SyntaxException(lex[from].row, lex[from].col, "Missing '='");
								}
								else if (item.positional) {
									throw new SyntaxException(lex[from].row, lex[from].col, "Using this parameter as key/value pair is not supported");
								}
								else {
									from = parseParameter(lex, from + 1, subst, maxQbitNumber, values, index, item);
									found = true;
									break;
								}
							}
							index++;
						}
						if (!found) {
							throw new SyntaxException(lex[from].row, lex[from].col, "Unknown parameter name for this gate");
						}
					}
					else {
						from = parseParameter(lex, from, subst, maxQbitNumber, values, parmNo, parms[parmNo]);
						parmNo++;
					}
				} while (lex[from].type == Lexema.LexType.DIV && parmNo < parms.length);

				for (int index = 0; index < parms.length; index++) {
					if (values[index] == null && parms[index].mandatory) {
						throw new SyntaxException(lex[from].row, lex[from].col, "Mandatory parameter ["+parms[index].name+"] is not typed");
					}
				}
				
				if (lex[from].type != Lexema.LexType.CLOSE_B) {
					throw new SyntaxException(lex[from].row, lex[from].col, "Missing ']'");
				}
				else {
					from++;
				}
				for(int index = 0; index < parms.length; index++) {
					if (parms[index].mandatory && values[index] == null) {
						throw new SyntaxException(lex[from].row, lex[from].col, "Mandatory parameter #["+index+"] is not typed for gate");
					}
				}

				if (lex[from].type == Lexema.LexType.COLON) {
					if (lex[++from].type != Lexema.LexType.OPEN_B) {
						throw new SyntaxException(lex[from].row, lex[from].col, "Missing '['");
					}
					else {
						final Object[]		value = new Object[1];
						
						from = parseIntList(lex, from, subst, maxQbitNumber, true, value, 0);
						
						if (lex[from].type != Lexema.LexType.EOF) {
							throw new SyntaxException(lex[from].row, lex[from].col, "Unparsed tail in the string");
						}
						else {
							return new Gate(desc, values, (int[])value[0]); 
						}
					}
				}
				else {
					if (lex[from].type != Lexema.LexType.EOF) {
						throw new SyntaxException(lex[from].row, lex[from].col, "Unparsed tail in the string");
					}
					else {
						return new Gate(desc, values);
					}
				}
			}
		}
	}
	
	static int parseParameter(final Lexema[] lex, int from, final Function<String,String> subst, final int maxQbitNumber, final Object[] values, final int parmNo, final GateParameter parm) throws SyntaxException {
		switch (parm.type) {
			case COMPLEX_VALUE	:
				from = parseComplex(Depth.ADD, lex, from, subst, values, parmNo);
				break;
			case QBIT_NUMBER	:
				from = parseInt(Depth.ADD, lex, from, subst, values, parmNo);
				final int val = toInteger(lex[from].row, lex[from].col, values[parmNo]);
				
				if (val < 0 || val > maxQbitNumber) {
					throw new SyntaxException(lex[from].row, lex[from].col, "Qbit value ["+val+"] out of 0.."+maxQbitNumber);
				}
				break;
			case QBIT_LIST		:
				from = parseIntList(lex, from, subst, maxQbitNumber, false, values, parmNo);
				break;
			case REAL_VALUE		:
				from = parseReal(Depth.ADD, lex, from, subst, values, parmNo);
				break;
			default :
				throw new UnsupportedOperationException("Parameter type ["+parm.type+"] is not supported yet");
		}
		return from;
	}
	
	static int parseInt(final Depth depth, final Lexema[] lex, int from, final Function<String,String> subst, final Object[] target, final int targetNo) throws SyntaxException {
		switch (depth) {
			case RANGE	:
				throw new UnsupportedOperationException("Range is not supported here");
			case ADD	:
				from = parseInt(Depth.MUL, lex, from, subst, target, targetNo);
				if (lex[from].type == Lexema.LexType.ADD_OP) {
					final Object[]	values = new Object[] {target[targetNo], null};
					
					do {
						final char	op = (char) lex[from].intValue;
						final int	row = lex[from].row;
						final int	col = lex[from].col;

						from = parseInt(Depth.MUL, lex, from+1, subst, values, 1);
						switch (op) {
							case '+' :
								values[0] = toInteger(row, col, values[0]) + toInteger(row, col, values[1]);
								break;
							case '-' :
								values[0] = toInteger(row, col, values[0]) - toInteger(row, col, values[1]);
								break;
							default :
								throw new UnsupportedOperationException("Additive op ["+op+"] is not supported yet");
						}
					} while (lex[from].type == Lexema.LexType.ADD_OP);
					target[targetNo] = values[0];
				}
				break;
			case MUL	:
				from = parseInt(Depth.UNARY, lex, from, subst, target, targetNo);
				if (lex[from].type == Lexema.LexType.MUL_OP) {
					final Object[]	values = new Object[] {target[targetNo], null};
					
					do {
						final char	op = (char) lex[from].intValue;
						final int	row = lex[from].row;
						final int	col = lex[from].col;

						from = parseInt(Depth.UNARY, lex, from+1, subst, values, 1);
						switch (op) {
							case '*' :
								values[0] = toInteger(row, col, values[0]) * toInteger(row, col, values[1]);
								break;
							case '/' :
								values[0] = toInteger(row, col, values[0]) / toInteger(row, col, values[1]);
								break;
							default :
								throw new UnsupportedOperationException("Multiplicative op ["+op+"] is not supported yet");
						}
					} while (lex[from].type == Lexema.LexType.MUL_OP);
					target[targetNo] = values[0];
				}
				break;
			case UNARY	:
				if (lex[from].type == Lexema.LexType.ADD_OP) {
					final char	op = (char) lex[from].intValue;
					final int	row = lex[from].row;
					final int	col = lex[from].col;

					from = parseInt(Depth.TERM, lex, from+1, subst, target, targetNo);
					switch (op) {
						case '+' :
							break;
						case '-' :
							target[targetNo] = -toInteger(row, col, target[targetNo]);
							break;
						default :
							throw new UnsupportedOperationException("Additive op ["+op+"] is not supported yet");
					}
				}
				else {
					from = parseInt(Depth.TERM, lex, from, subst, target, targetNo);
				}
				break;
			case TERM	:
				switch (lex[from].type) {
					case INTEGER_CONST:
						target[targetNo] = lex[from++].intValue; 
						break;
					case REAL_CONST:
						throw new SyntaxException(lex[from].row, lex[from].col, "Real constant inside integer argument");
					case OPEN	:
						from = parseInt(Depth.ADD, lex, from + 1, subst, target, targetNo);
						if (lex[from].type != Lexema.LexType.CLOSE) {
							throw new SyntaxException(lex[from].row, lex[from].col, "Missing ')'");
						}
						else {
							from++;
						}
						break;
					case REFERENCE_NAME:
						target[targetNo] = toValue(getReferencedValue(lex[from].row, lex[from].col, subst, lex[from].stringValue), subst, Integer.class);
						from++;
						break;
					default		:
						throw new SyntaxException(lex[from].row, lex[from].col, "Missing or invalid operand");
				}
				break;
			default:
				throw new UnsupportedOperationException("Depth ["+depth+"] is not supported yet");
		}
		return from;
	}

	static int parseReal(final Depth depth, final Lexema[] lex, int from, final Function<String,String> subst, final Object[] target, final int targetNo) throws SyntaxException {
		switch (depth) {
			case RANGE	:
				throw new UnsupportedOperationException("Range is not supported here");
			case ADD	:
				from = parseReal(Depth.MUL, lex, from, subst,target, targetNo);
				if (lex[from].type == Lexema.LexType.ADD_OP) {
					final Object[]	values = new Object[] {target[targetNo], null};
					
					do {
						final char	op = (char) lex[from].intValue;
						final int	row = lex[from].row;
						final int	col = lex[from].col;

						from = parseReal(Depth.MUL, lex, from + 1, subst, values, 1);
						switch (op) {
							case '+' :
								values[0] = toDouble(row, col, values[0]) + toDouble(row, col, values[1]);
								break;
							case '-' :
								values[0] = toDouble(row, col, values[0]) - toDouble(row, col, values[1]);
								break;
							default :
								throw new UnsupportedOperationException("Additive op ["+op+"] is not supported yet");
						}
					} while (lex[from].type == Lexema.LexType.ADD_OP);
					target[targetNo] = values[0];
				}
				break;
			case MUL	:
				from = parseReal(Depth.UNARY, lex, from, subst, target, targetNo);
				if (lex[from].type == Lexema.LexType.MUL_OP) {
					final Object[]	values = new Object[] {target[targetNo], null};
					
					do {
						final char	op = (char) lex[from].intValue;
						final int	row = lex[from].row;
						final int	col = lex[from].col;

						from = parseReal(Depth.UNARY, lex, from + 1, subst, values, 1);
						switch (op) {
							case '*' :
								values[0] = toDouble(row, col, values[0]) * toDouble(row, col, values[1]);
								break;
							case '/' :
								values[0] = toDouble(row, col, values[0]) / toDouble(row, col, values[1]);
								break;
							default :
								throw new UnsupportedOperationException("Additive op ["+op+"] is not supported yet");
						}
					} while (lex[from].type == Lexema.LexType.MUL_OP);
					target[targetNo] = values[0];
				}
				break;
			case UNARY	:
				if (lex[from].type == Lexema.LexType.ADD_OP) {
					final char	op = (char) lex[from].intValue;
					final int	row = lex[from].row;
					final int	col = lex[from].col;

					from = parseReal(Depth.TERM, lex, from + 1, subst, target, targetNo);
					switch (op) {
						case '+' :
							break;
						case '-' :
							target[targetNo] = -toDouble(row, col, target[targetNo]);
							break;
						default :
							throw new UnsupportedOperationException("Additive op ["+op+"] is not supported yet");
					}
				}
				else {
					from = parseReal(Depth.TERM, lex, from, subst, target, targetNo);
				}
				break;
			case TERM	:
				switch (lex[from].type) {
					case INTEGER_CONST : case REAL_CONST :
						final double	val = lex[from].type == Lexema.LexType.INTEGER_CONST ? lex[from++].intValue : lex[from++].doubleValue;
						
						if (lex[from].type == Lexema.LexType.NAMED_CONSTANT && lex[from].intValue == CONST_E) {
							from = parseInt(Depth.UNARY, lex, from + 1, subst, target, targetNo);
							final int	power = toInteger(lex[from].row, lex[from].col, target[targetNo]);
							
							target[targetNo] = val * Math.pow(10, power);
						}
						else {
							target[targetNo] = val;
						}
						break;
					case NAMED_CONSTANT	:
						switch (lex[from].intValue) {
							case CONST_PI :
								target[targetNo] = Math.PI;
								break;
							case CONST_E :
								target[targetNo] = Math.E;
								break;
							default :
								throw new UnsupportedOperationException("Named constant id ["+lex[from].intValue+"] is not supported yet");
						}
						from++;
						break;
					case FUNCTION_NAME	:
						switch (lex[from].intValue) {
							case FUNC_SQRT :
								if (lex[from+1].type != Lexema.LexType.OPEN) {
									throw new SyntaxException(lex[from].row, lex[from].col, "Missing '('");
								}
								else {
									from = parseReal(Depth.ADD, lex, from + 2, subst, target, targetNo);
									if (lex[from].type != Lexema.LexType.CLOSE) {
										throw new SyntaxException(lex[from].row, lex[from].col, "Missing ')'");
									}
									else {
										target[targetNo] = Math.sqrt(toDouble(lex[from].row, lex[from].col, target[targetNo]));
										from++;
									}
								}
								break;
							default :
								throw new UnsupportedOperationException("Func id ["+lex[from].intValue+"] is not supported yet");
						}
						break;
					case OPEN	:
						from = parseReal(Depth.ADD, lex, from + 1, subst, target, targetNo);
						if (lex[from].type != Lexema.LexType.CLOSE) {
							throw new SyntaxException(lex[from].row, lex[from].col, "Missing ')'");
						}
						else {
							from++;
						}
						break;
					case REFERENCE_NAME:
						target[targetNo] = toValue(getReferencedValue(lex[from].row, lex[from].col, subst, lex[from].stringValue), subst, Double.class);
						from++;
						break;
					default		:
						throw new SyntaxException(lex[from].row, lex[from].col, "Missing or invalid operand");
				}
				break;
			default:
				throw new UnsupportedOperationException("Depth ["+depth+"] is not supported yet");
		}
		return from;
	}

	static int parseComplex(final Depth depth, final Lexema[] lex, int from, final Function<String,String> subst, final Object[] target, final int targetNo) throws SyntaxException {
		switch (depth) {
			case RANGE	:
				throw new UnsupportedOperationException("Range is not supported here");
			case ADD	:
				from = parseComplex(Depth.MUL, lex, from, subst, target, targetNo);
				if (lex[from].type == Lexema.LexType.ADD_OP) {
					final Object[]	values = new Object[] {target[targetNo], null};
					
					do {
						final char	op = (char) lex[from].intValue;
						final int	row = lex[from].row;
						final int	col = lex[from].col;

						from = parseComplex(Depth.MUL, lex, from + 1, subst, values, 1);
						switch (op) {
							case '+' :
								values[0] = toComplex(row, col, values[0]).add(toComplex(row, col, values[1]));
								break;
							case '-' :
								values[0] = toComplex(row, col, values[0]).subtract(toComplex(row, col, values[1]));
								break;
							default :
								throw new UnsupportedOperationException("Additive op ["+op+"] is not supported yet");
						}
					} while (lex[from].type == Lexema.LexType.ADD_OP);
					target[targetNo] = values[0];
				}
				break;
			case MUL	:
				from = parseComplex(Depth.UNARY, lex, from, subst, target, targetNo);
				if (lex[from].type == Lexema.LexType.MUL_OP) {
					final Object[]	values = new Object[] {target[targetNo], null};
					
					do {
						final char	op = (char) lex[from].intValue;
						final int	row = lex[from].row;
						final int	col = lex[from].col;

						from = parseComplex(Depth.UNARY, lex, from + 1, subst, values, 1);
						switch (op) {
							case '*' :
								values[0] = toComplex(row, col, values[0]).multiply(toComplex(row, col, values[1]));
								break;
							case '/' :
								values[0] = toComplex(row, col, values[0]).divide(toComplex(row, col, values[1]));
								break;
							default :
								throw new UnsupportedOperationException("Additive op ["+op+"] is not supported yet");
						}
					} while (lex[from].type == Lexema.LexType.MUL_OP);
					target[targetNo] = values[0];
				}
				break;
			case UNARY	:
				if (lex[from].type == Lexema.LexType.ADD_OP) {
					final char	op = (char) lex[from].intValue;
					final int	row = lex[from].row;
					final int	col = lex[from].col;

					from = parseComplex(Depth.TERM, lex, from + 1, subst, target, targetNo);
					switch (op) {
						case '+' :
							break;
						case '-' :
 							target[targetNo] = new ComplexValue(false, -1, 0).multiply(toComplex(row, col, target[targetNo]));
							break;
						default :
							throw new UnsupportedOperationException("Additive op ["+op+"] is not supported yet");
					}
				}
				else {
					from = parseComplex(Depth.TERM, lex, from, subst, target, targetNo);
				}
				break;
			case TERM	:
				switch (lex[from].type) {
					case INTEGER_CONST : case REAL_CONST :
						final double	val = lex[from].type == Lexema.LexType.INTEGER_CONST ? lex[from++].intValue : lex[from++].doubleValue;
						
						if (lex[from].type == Lexema.LexType.NAMED_CONSTANT && lex[from].intValue == CONST_E) {
							try {
								from = parseInt(Depth.UNARY, lex, from + 1, subst, target, targetNo);
								final int	power = toInteger(lex[from].row, lex[from].col, target[targetNo]);

								if (lex[from].type == Lexema.LexType.I) {
									target[targetNo] = new ComplexValue(true, val, power);
									from++;
								}
								else {
									target[targetNo] = new ComplexValue(false, val * Math.pow(10, power), 0);
								}
							} catch (SyntaxException exc) {
								from = parseComplex(Depth.UNARY, lex, from + 1, subst, target, targetNo);
								final ComplexValue	calc = toComplex(lex[from].row, lex[from].col, target[targetNo]).toRealImage();

								target[targetNo] = new ComplexValue(true, val * Math.exp(calc.realOrMagnitide), val * calc.imageOrArgument);
							}
						}
						else {
							if (lex[from].type == Lexema.LexType.I) {
								target[targetNo] = new ComplexValue(false, 0, val);
								from++;
							}
							else {
								target[targetNo] = new ComplexValue(false, val, 0);							
							}
						}
						break;
					case I :
						target[targetNo] = new ComplexValue(false, 0, 1);
						from++;
						break;
					case NAMED_CONSTANT	:
						switch (lex[from].intValue) {
							case CONST_PI :
								target[targetNo] = new ComplexValue(false, Math.PI, 0);
								break;
							case CONST_E :
								target[targetNo] = new ComplexValue(false, Math.E, 0);
								break;
							default :
								throw new UnsupportedOperationException("Named constant id ["+lex[from].intValue+"] is not supported yet");
						}
						from++;
						break;
					case FUNCTION_NAME	:
						switch (lex[from].intValue) {
							case FUNC_SQRT :
								if (lex[from+1].type != Lexema.LexType.OPEN) {
									throw new SyntaxException(lex[from].row, lex[from].col, "Missing '('");
								}
								else {
									from = parseComplex(Depth.ADD, lex, from + 2, subst, target, targetNo);
									if (lex[from].type != Lexema.LexType.CLOSE) {
										throw new SyntaxException(lex[from].row, lex[from].col, "Missing ')'");
									}
									else {
										final ComplexValue	calc = toComplex(lex[from].row, lex[from].col, target[targetNo]).toExponent();
										
										target[targetNo] = new ComplexValue(true, Math.sqrt(calc.realOrMagnitide), calc.imageOrArgument / 2);
										from++;
									}
								}
								break;
							default :
								throw new UnsupportedOperationException("Function id ["+lex[from].intValue+"] is not supported yet");
						}
						break;
					case OPEN	:
						from = parseComplex(Depth.ADD, lex, from + 1, subst, target, targetNo);
						if (lex[from].type != Lexema.LexType.CLOSE) {
							throw new SyntaxException(lex[from].row, lex[from].col, "Missing ')'");
						}
						else {
							from++;
						}
						break;
					case REFERENCE_NAME:
						target[targetNo] = toValue(getReferencedValue(lex[from].row, lex[from].col, subst, lex[from].stringValue), subst, ComplexValue.class);
						from++;
						break;
					default		:
						throw new SyntaxException(lex[from].row, lex[from].col, "Missing or invalid operand");
				}
				break;
			default:
				throw new UnsupportedOperationException("Depth ["+depth+"] is not supported yet");
		}
		return from;
	}

	static int parseIntList(final Lexema[] lex, int from, final Function<String,String> subst, final int maxRange, final boolean enableTildes, final Object[] target, final int targetNo) throws SyntaxException {
		final List<int[]>	ranges = new ArrayList<>();
		final int[]	temp = new int[2];
		
		if (lex[from].type == Lexema.LexType.OPEN) {
			do {
				from = parseRange(lex, from + 1, subst, maxRange, enableTildes, temp);
				ranges.add(temp.clone());
			} while (lex[from].type == Lexema.LexType.DIV);
			
			if (lex[from].type != Lexema.LexType.CLOSE) {
				throw new SyntaxException(lex[from].row, lex[from].col, "Missing ')'");
			}
			else {
				from++;
			}
		}
		else if (lex[from].type == Lexema.LexType.OPEN_B) {
			do {
				from = parseRange(lex, from + 1, subst, maxRange, enableTildes, temp);
				ranges.add(temp.clone());
			} while (lex[from].type == Lexema.LexType.DIV);
			
			if (lex[from].type != Lexema.LexType.CLOSE_B) {
				throw new SyntaxException(lex[from].row, lex[from].col, "Missing ']'");
			}
			else {
				from++;
			}
		}
		else {
			from = parseRange(lex, from, subst, maxRange, enableTildes, temp);
			ranges.add(temp.clone());
		}
		int	count = 0;
		
		for(int[] item : ranges) {
			count += (item[1] & Gate.QBIT_MASK) - (item[0] & Gate.QBIT_MASK) + 1;
		}
		final int[]	result = new int[count];
		int	displ = 0;
		
		for(int[] item : ranges) {
			final int	mask = item[0] & Gate.INVERSION_MASK;
					
			for (int value = item[0] & Gate.QBIT_MASK; value <= (item[1] & Gate.QBIT_MASK); value++) {
				result[displ++] = value | mask;
			}
		}
		target[targetNo] = result;
		return from;
	}	
	
	private static int parseRange(final Lexema[] lex, int from, final Function<String,String> subst, final int maxRange, final boolean enableTildes, final int[] ranges) throws SyntaxException {
		final Object[]	val = new Object[2];
		final boolean[]	tildes = new boolean[2];
		final int		low, high;
		
		if (enableTildes && lex[from].type == Lexema.LexType.TILDE) {
			tildes[0] = true;
			from++;
		}
		from = parseInt(Depth.ADD, lex, from, subst, val, 0);
		if (lex[from].type == Lexema.LexType.RANGE) {
			final int	row = lex[from].row;
			final int	col = lex[from].col;
			
			from++;
			if (enableTildes && lex[from].type == Lexema.LexType.TILDE) {
				tildes[1] = true;
				from++;
			}
			from = parseInt(Depth.ADD, lex, from, subst, val, 1);
			if (tildes[0] != tildes[1]) {
				throw new SyntaxException(row, col, "Illegal use of tildes: either both or no ranges must be marked with them");
			}
			else {
				low = toInteger(row, col, val[0]);
				high = toInteger(row, col, val[1]);
				
				if (low > high) {
					throw new SyntaxException(row, col, "Illegal ranges: low range ["+low+"] is greated than high ["+high+"]");
				}
				else if (low < 0 || low > maxRange) {
					throw new SyntaxException(row, col, "Low range value ["+low+"] out of 0.."+maxRange);
				}
				else if (high < 0 || high > maxRange) {
					throw new SyntaxException(row, col, "High range value ["+high+"] out of 0.."+maxRange);
				}
			}
		}
		else {
			low = high = toInteger(lex[from].row, lex[from].col, val[0]);
			if (low < 0 || low > maxRange) {
				throw new SyntaxException(lex[from].row, lex[from].col, "List value ["+low+"] out of 0.."+maxRange);
			}
		}
		ranges[0] = (tildes[0] ? Gate.INVERSION_MASK : 0) | low;
		ranges[1] = (tildes[1] ? Gate.INVERSION_MASK : 0) | high;
		return from;
	}
	
	private static String getReferencedValue(final int row, final int col, final Function<String,String> subst, final String name) throws SyntaxException {
		final String	result = subst.apply(name);

		if (result == null) {
			throw new SyntaxException(row, col, "Referenced value ["+name+"] not found anywhere");
		}
		else {
			return result;
		}
	}

	private static <T> T toValue(final String value, final Function<String,String> subst, final Class<T> awaited) throws SyntaxException {
		final List<Lexema>	lexList = new ArrayList<>();
		int	len;
		
		if ((len = parseLex(value+EOF, 0, lexList)) < value.length()) {
			throw new SyntaxException(0, len, "Unparsed tail in the input");
		}
		else {
			final Object[]	result = new Object[1];
			final Lexema[]	lex = lexList.toArray(new Lexema[lexList.size()]);
			
			if (Integer.class.isAssignableFrom(awaited) || int.class.isAssignableFrom(awaited)) {
				parseInt(Depth.ADD, lex, 0, subst, result, 0);
				return awaited.cast(result[0]);
			}
			else if (Double.class.isAssignableFrom(awaited) || double.class.isAssignableFrom(awaited)) {
				parseReal(Depth.ADD, lex, 0, subst, result, 0);
				return awaited.cast(result[0]);
			}
			else if (ComplexValue.class.isAssignableFrom(awaited)) {
				parseComplex(Depth.ADD, lex, 0, subst, result, 0);
				return awaited.cast(result[0]);
			}
			else {
				throw new UnsupportedOperationException("Awaited class ["+awaited+"] is not suported yet");
			}
		}
	}
	
	private static int toInteger(final int row, final int col, final Object value) throws SyntaxException {
		if (value instanceof Integer) {
			return ((Integer)value).intValue();
		}
		else {
			throw new SyntaxException(row, col, "Value ["+value+"] can't be converted to integer");
		}
	}

	private static double toDouble(final int row, final int col, final Object value) throws SyntaxException {
		if (value instanceof Number) {
			return ((Number)value).doubleValue();
		}
		else {
			throw new SyntaxException(row, col, "Value ["+value+"] can't be converted to double");
		}
	}

	private static ComplexValue toComplex(final int row, final int col, final Object value) throws SyntaxException {
		if (value instanceof ComplexValue) {
			return (ComplexValue)value;
		}
		else {
			throw new SyntaxException(row, col, "Value ["+value+"] can't be converted to complex");
		}
	}
	
	static int skipBlank(final CharSequence seq, int from) {
		while (Character.isWhitespace(seq.charAt(from))) {
			from++;
		}
		return from;
	}

	static class ComplexValue {
		public final boolean	exponentForm;
		public final double		realOrMagnitide;
		public final double		imageOrArgument;
		
		public ComplexValue(boolean exponentForm, double realOrMagnotide, double imageOrArgument) {
			this.exponentForm = exponentForm;
			this.realOrMagnitide = realOrMagnotide;
			this.imageOrArgument = imageOrArgument;
		}

		public ComplexValue add(final ComplexValue another) {
			if (another == null) {
				throw new NullPointerException("Another value can't be null");
			}
			else {
				final ComplexValue	left = this.toRealImage(), right = another.toRealImage();
				
				return new ComplexValue(false, left.realOrMagnitide + right.realOrMagnitide, left.imageOrArgument + right.imageOrArgument);
			}
		}

		public ComplexValue subtract(final ComplexValue another) {
			if (another == null) {
				throw new NullPointerException("Another value can't be null");
			}
			else {
				final ComplexValue	left = this.toRealImage(), right = another.toRealImage();
				
				return new ComplexValue(false, left.realOrMagnitide - right.realOrMagnitide, left.imageOrArgument - right.imageOrArgument);
			}
		}

		public ComplexValue multiply(final ComplexValue another) {
			if (another == null) {
				throw new NullPointerException("Another value can't be null");
			}
			else {
				final ComplexValue	left = this.toExponent(), right = another.toExponent();
				
				return new ComplexValue(true, left.realOrMagnitide * right.realOrMagnitide, left.imageOrArgument + right.imageOrArgument);
			}
		}
		
		public ComplexValue divide(final ComplexValue another) {
			if (another == null) {
				throw new NullPointerException("Another value can't be null");
			}
			else {
				final ComplexValue	left = this.toExponent(), right = another.toExponent();
				
				return new ComplexValue(true, left.realOrMagnitide / right.realOrMagnitide, left.imageOrArgument - right.imageOrArgument);
			}
		}

		public ComplexValue toExponent() {
			if (exponentForm) {
				return this;
			}
			else {
				final double	magnitude = Math.sqrt(realOrMagnitide * realOrMagnitide + imageOrArgument * imageOrArgument);
				final double	argument = Math.atan2(imageOrArgument, realOrMagnitide) % TWO_PI;
				
				return new ComplexValue(true, magnitude, argument);
			}
		}
		
		public ComplexValue toRealImage() {
			if (!exponentForm) {
				return this;
			}
			else {
				final double	real = realOrMagnitide * Math.cos(imageOrArgument);
				final double	image = realOrMagnitide * Math.sin(imageOrArgument);

				return new ComplexValue(false, real, image);
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (exponentForm ? 1231 : 1237);
			long temp;
			temp = Double.doubleToLongBits(imageOrArgument);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(realOrMagnitide);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ComplexValue other = (ComplexValue) obj;
			if (exponentForm != other.exponentForm) return false;
			if (Double.doubleToLongBits(imageOrArgument) != Double.doubleToLongBits(other.imageOrArgument)) return false;
			if (Double.doubleToLongBits(realOrMagnitide) != Double.doubleToLongBits(other.realOrMagnitide)) return false;
			return true;
		}

		public boolean equals(final ComplexValue another, final double epsilon) {
			if (another == null) {
				throw new NullPointerException("Another value to compare can't be null");
			}
			else if (another.exponentForm != this.exponentForm) {
				if (exponentForm) {
					return equals(another.toExponent(), epsilon);
				}
				else {
					return equals(another.toRealImage(), epsilon);
				}
			}
			else {
				return Math.abs(realOrMagnitide - another.realOrMagnitide) < epsilon && Math.abs(imageOrArgument - another.imageOrArgument) < epsilon; 
			}
		}
		
		@Override
		public String toString() {
			if (exponentForm) {
				return ""+realOrMagnitide+"exp"+(imageOrArgument < 0 ? imageOrArgument : "+"+imageOrArgument)+"i";
			}
			else {
				return ""+realOrMagnitide+(imageOrArgument < 0 ? imageOrArgument : "+"+imageOrArgument)+"i";
			}
		}
	}

	static enum GateParameterType {
		QBIT_NUMBER,
		QBIT_LIST,
		REAL_VALUE,
		COMPLEX_VALUE
	}

	static class GateParameter {
		public final String		name;
		public final GateParameterType	type;
		public final boolean	mandatory;
		public final boolean	positional;

		public GateParameter(final String name, final GateParameterType type, final boolean mandatory, final boolean positional) {
			this.name = name;
			this.type = type;
			this.mandatory = mandatory;
			this.positional = positional;
		}

		@Override
		public String toString() {
			return "Parameter [name=" + name + ", type=" + type + ", mandatory=" + mandatory + ", positional=" + positional + "]";
		}
	}
	
	static class InternalGateDescriptor {
		private final GateType	gateName;
		private final GateParameter[] 	parameters;
		
		private InternalGateDescriptor(final GateType name, final GateParameter... parameters) {
			this.gateName = name;
			this.parameters = parameters;
		}
		
		public GateType getName() {
			return gateName;
		}
		
		public GateParameter[] getParameters() {
			return parameters;
		}
		
		@Override
		public String toString() {
			return "GateDescriptor [gateName=" + gateName + ", parameters=" + Arrays.toString(parameters) + "]";
		}
	}

	static class Gate {
		public static final int		QBIT_MASK = 0xFF;
		public static final int		INVERSION_MASK = 0x0100;
		private static final int[]	DUMMY = new int[0];
		
		private final InternalGateDescriptor	desc;
		private final Object[]	values;
		private final int[]		conditions;

		public Gate(final InternalGateDescriptor desc, final Object[] values) {
			this(desc, values, DUMMY);
		}
		
		public Gate(final InternalGateDescriptor desc, final Object[] values, final int[] conditions) {
			this.desc = desc;
			this.values = values;
			this.conditions = conditions;
		}

		public InternalGateDescriptor getDescriptor() {
			return desc;
		}
		
		public Object[] getParameterValues() {
			return values;
		}
		
		public int[] getConditions() {
			return conditions;
		}
		
		@Override
		public String toString() {
			return "Gate [desc=" + desc + ", values=" + Arrays.toString(values) + ", conditions=" + Arrays.toString(conditions) + "]";
		}
	}
	
	static enum Depth {
		RANGE,
		ADD,
		MUL,
		UNARY,
		TERM
	}
	
	static class Lexema {
		static enum LexType {
			INTEGER_CONST,
			REAL_CONST,
			NAMED_CONSTANT,
			FUNCTION_NAME,
			REFERENCE_NAME,
			PARAMETER_NAME,
			GATE_NAME,
			I,
			OPEN,
			CLOSE,
			OPEN_B,
			CLOSE_B,
			COLON,
			DIV,
			TILDE,
			EQUALS,
			MUL_OP,
			ADD_OP,
			RANGE,
			EOF
		}
		
		final int		row; 
		final int		col; 
		final LexType	type;
		final int		intValue;
		final double	doubleValue;
		final String	stringValue;
		
		Lexema(final int row, final int col, final LexType type) {
			this.row = row;
			this.col = col;
			this.type = type;
			this.intValue = 0;
			this.doubleValue = 0;
			this.stringValue = null;
		}

		Lexema(final int row, final int col, final LexType type, final int value) {
			this.row = row;
			this.col = col;
			this.type = type;
			this.intValue = value;
			this.doubleValue = 0;
			this.stringValue = null;
		}

		Lexema(final int row, final int col, final LexType type, final double value) {
			this.row = row;
			this.col = col;
			this.type = type;
			this.intValue = 0;
			this.doubleValue = value;
			this.stringValue = null;
		}

		Lexema(final int row, final int col, final LexType type, final String value) {
			this.row = row;
			this.col = col;
			this.type = type;
			this.intValue = 0;
			this.doubleValue = 0;
			this.stringValue = value;
		}

		@Override
		public String toString() {
			return "Lexema [row=" + row + ", col=" + col + ", type=" + type + ", intValue=" + intValue
					+ ", doubleValue=" + doubleValue + ", stringValue=" + stringValue + "]";
		}
	}
}
