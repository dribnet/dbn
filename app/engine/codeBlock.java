import java.util.Vector;
import java.util.Hashtable;

public class codeBlock {
	Vector symbols;
	boolean isForever;
	boolean testLess;
	boolean isConditional = false;
	boolean runOnceOnly;
	String variable;
	int curval;
	int endval;
	int stepval;
	int curSymbol;
	int value;
	ParserHandler ph;
	Hashtable variables=null;
	Vector variableNames=null;
	codeBlock parent=null;
}
