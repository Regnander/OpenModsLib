package openmods.calc.types.multi;

import java.util.List;
import openmods.calc.BinaryOperator;
import openmods.calc.IExecutable;
import openmods.calc.SymbolCall;
import openmods.calc.Value;
import openmods.calc.parsing.BinaryOpNode;
import openmods.calc.parsing.IExprNode;
import openmods.calc.parsing.SymbolCallNode;
import openmods.calc.parsing.SymbolGetNode;
import openmods.calc.parsing.SymbolOpNode;

class DotExprNode extends BinaryOpNode<TypedValue> {

	private final TypeDomain domain;

	public DotExprNode(IExprNode<TypedValue> left, IExprNode<TypedValue> right, BinaryOperator<TypedValue> operator, TypeDomain domain) {
		super(operator, left, right);
		this.domain = domain;
	}

	@Override
	public void flatten(List<IExecutable<TypedValue>> output) {
		left.flatten(output);
		flattenKeyNode(output, right);
	}

	// algorithm: if node has children, recurse, otherwise try to extract key (to be placed on the right side of dot)
	private void flattenKeyNode(List<IExecutable<TypedValue>> output, IExprNode<TypedValue> target) {
		if (target instanceof MethodCallNode) { // non-terminal: "left" node has children
			final MethodCallNode call = (MethodCallNode)target;
			flattenKeyNode(output, call.target); // recurse into left node
			call.flattenArgsAndCall(output); // unwrap rest
		} else if (target instanceof SymbolCallNode) { // terminal node: symbol call
			final SymbolCallNode<TypedValue> call = (SymbolCallNode<TypedValue>)target;
			convertSymbolNodeToKey(output, call);
			output.add(operator);
			appendSymbolApply(output, call.getChildren());
		} else if (target instanceof SymbolGetNode) { // terminal node: symbol get - convert to string
			convertSymbolNodeToKey(output, (SymbolGetNode<TypedValue>)target);
			output.add(operator);
		} else if (target instanceof RawCodeExprNode) { // terminal node: with (.{...}) statement
			target.flatten(output);
			output.add(new SymbolCall<TypedValue>(TypedCalcConstants.SYMBOL_WITH, 2, 1));
		} else { // terminal node - anything else (possible something that returns string)
			target.flatten(output);
			output.add(operator);
		}
	}

	private static void appendSymbolApply(List<IExecutable<TypedValue>> output, Iterable<IExprNode<TypedValue>> children) {
		int applyArgs = 1;
		for (IExprNode<TypedValue> child : children) {
			child.flatten(output);
			applyArgs++;
		}

		output.add(new SymbolCall<TypedValue>(TypedCalcConstants.SYMBOL_APPLY, applyArgs, 1));
	}

	private void convertSymbolNodeToKey(List<IExecutable<TypedValue>> output, SymbolOpNode<TypedValue> target) {
		output.add(Value.create(domain.create(String.class, target.symbol())));
	}
}